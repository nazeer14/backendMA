package com.pack.service.impl;

import com.pack.dto.request.TeamRequestDto;
import com.pack.dto.response.*;
import com.pack.entity.Team;
import com.pack.entity.User;
import com.pack.enums.TeamRole;
import com.pack.exceptions.*;
import com.pack.mapper.TeamMapper;
import com.pack.repository.TeamMemberRepository;
import com.pack.repository.TeamRepository;
import com.pack.repository.UserRepository;
import com.pack.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    private static final int TOKEN_LENGTH = 32;
    private static final int MAX_TOKEN_RETRIES = 5;
    private static final String TOKEN_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Value("${app.invite.base-url:http://localhost:8080/api/v1/teams/join}")
    private String inviteBaseUrl;

    private final TeamRepository       teamRepository;
    private final TeamMemberRepository memberRepository; // cross-module repo: member counts + ownership row only
    private final UserRepository       userRepository;
    private final TeamMapper           teamMapper;
    private final SecureRandom         secureRandom = new SecureRandom();

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TeamResponseDto create(TeamRequestDto dto) {
        log.info("Creating team '{}' for owner: {}", dto.teamName(), dto.ownerId());

        User owner = findUserOrThrow(dto.ownerId());

        if (teamRepository.existsByTeamNameIgnoreCaseAndOwnerId(dto.teamName(), dto.ownerId())) {
            throw new BusinessException(String.format("Owner already has a team named '%s'", dto.teamName()));
        }

        Team team = teamMapper.toEntity(dto);
        team.setOwner(owner);
        team = teamRepository.save(team);

        // Membership row creation delegated via repository directly here to avoid a circular
        // service dependency (TeamMemberService would need TeamService for validation, and vice
        // versa). The repository call is the agreed seam between the two modules.
        memberRepository.createOwnerMembership(team.getId(), owner.getId());

        log.info("Team created with id: {}", team.getId());
        return enrich(team);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    public TeamResponseDto getById(UUID teamId) {
        return enrich(findTeamOrThrow(teamId));
    }

    @Override
    public Page<TeamResponseDto> getAll(Pageable pageable) {
        return teamRepository.findAll(pageable).map(this::enrich);
    }

    @Override
    public Page<TeamResponseDto> getByOwnerId(UUID ownerId, Pageable pageable) {
        if (!userRepository.existsById(ownerId)) {
            throw new ResourceNotFoundException("User", ownerId);
        }
        return teamRepository.findByOwnerId(ownerId, pageable).map(this::enrich);
    }

    @Override
    public Page<TeamResponseDto> searchByName(String keyword, Pageable pageable) {
        return teamRepository.searchByTeamName(keyword, pageable).map(this::enrich);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TeamResponseDto update(UUID teamId, TeamRequestDto dto) {
        log.info("Updating team: {}", teamId);
        Team team = findTeamOrThrow(teamId);
        assertTeamActive(team);

        if (!team.getTeamName().equalsIgnoreCase(dto.teamName())
                && teamRepository.existsByTeamNameIgnoreCaseAndOwnerId(dto.teamName(), dto.ownerId())) {
            throw new BusinessException(String.format("Owner already has a team named '%s'", dto.teamName()));
        }

        teamMapper.updateEntityFromDto(dto, team);
        return enrich(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponseDto toggleActive(UUID teamId, Boolean active) {
        log.info("Setting team {} active={}", teamId, active);
        Team team = findTeamOrThrow(teamId);
        teamRepository.updateActiveStatus(teamId, active);
        team.setActive(active);
        return enrich(team);
    }

    @Override
    @Transactional
    public TeamResponseDto updateIconUrl(UUID teamId, String iconUrl) {
        log.info("Updating icon for team: {}", teamId);
        Team team = findTeamOrThrow(teamId);
        teamRepository.updateIconUrl(teamId, iconUrl);
        team.setIconUrl(iconUrl);
        return enrich(team);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(UUID teamId) {
        log.warn("Deleting team and all memberships: {}", teamId);
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", teamId);
        }
        memberRepository.deleteAllByTeamId(teamId);
        teamRepository.deleteById(teamId);
    }

    // ─── Invite Token ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TeamInviteResponseDto generateInviteToken(UUID teamId) {
        Team team = findTeamOrThrow(teamId);
        assertTeamActive(team);

        String token = generateUniqueToken();
        teamRepository.updateInviteToken(teamId, token);
        team.setInviteToken(token);

        log.info("Invite token generated for team: {}", teamId);
        return new TeamInviteResponseDto(team.getId(), team.getTeamName(), token, inviteBaseUrl + "?token=" + token);
    }

    @Override
    @Transactional
    public void revokeInviteToken(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", teamId);
        }
        teamRepository.revokeInviteToken(teamId);
    }

    @Override
    public TeamResponseDto getByInviteToken(String token) {
        Team team = teamRepository.findByInviteTokenWithOwner(token)
                .orElseThrow(InvalidInviteTokenException::new);
        return enrich(team);
    }

    // ─── Ownership Transfer ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public TeamResponseDto transferOwnership(UUID teamId, UUID newOwnerId) {
        log.info("Transferring ownership of team {} to user {}", teamId, newOwnerId);

        Team team = findTeamOrThrow(teamId);
        assertTeamActive(team);
        User newOwner = findUserOrThrow(newOwnerId);

        boolean isMember = memberRepository.existsActiveByTeamIdAndUserId(teamId, newOwnerId);
        if (!isMember) {
            throw new BusinessException("The new owner must already be an active member of the team.");
        }

        UUID currentOwnerId = team.getOwner().getId();
        memberRepository.swapOwnerRole(teamId, currentOwnerId, newOwnerId);

        team.setOwner(newOwner);
        Team saved = teamRepository.save(team);

        log.info("Ownership of team {} transferred from {} to {}", teamId, currentOwnerId, newOwnerId);
        return enrich(saved);
    }

    // ─── Analytics ────────────────────────────────────────────────────────────

    @Override
    public TeamSummaryDto getSummary(UUID teamId) {
        Team team = findTeamOrThrow(teamId);

        List<Object[]> rawCounts = memberRepository.countActiveGroupedByRoleForTeam(teamId);
        Map<TeamRole, Long> byRole = rawCounts.stream()
                .collect(Collectors.toMap(row -> (TeamRole) row[0], row -> (Long) row[1]));

        long total = byRole.values().stream().mapToLong(Long::longValue).sum();
        return new TeamSummaryDto(team.getId(), team.getTeamName(), total, byRole, team.getActive());
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private Team findTeamOrThrow(UUID teamId) {
        return teamRepository.findByIdWithOwner(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private void assertTeamActive(Team team) {
        if (Boolean.FALSE.equals(team.getActive())) {
            throw new TeamInactiveException(team.getId());
        }
    }

    private TeamResponseDto enrich(Team team) {
        long count = memberRepository.countActiveByTeamId(team.getId());
        TeamResponseDto base = teamMapper.toResponseDto(team);
        return new TeamResponseDto(
                base.id(), base.ownerId(), base.ownerFullName(),
                base.teamName(), base.inviteToken(), base.iconUrl(), base.active(),
                count, base.createdAt(), base.updatedAt()
        );
    }

    private String generateUniqueToken() {
        for (int attempt = 0; attempt < MAX_TOKEN_RETRIES; attempt++) {
            String token = buildRandomToken(TOKEN_LENGTH);
            if (!teamRepository.existsByInviteToken(token)) return token;
        }
        throw new BusinessException("Failed to generate a unique invite token. Please try again.");
    }

    private String buildRandomToken(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(TOKEN_CHARS.charAt(secureRandom.nextInt(TOKEN_CHARS.length())));
        }
        return sb.toString();
    }
}