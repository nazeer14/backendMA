package com.pack.service.impl;

import com.pack.dto.request.MembershipStatusChangeDto;
import com.pack.dto.request.RoleChangeRequestDto;
import com.pack.dto.request.TeamMemberRequestDto;
import com.pack.dto.response.MembershipSummaryDto;
import com.pack.dto.response.TeamMemberResponseDto;
import com.pack.entity.Team;
import com.pack.entity.TeamMember;
import com.pack.entity.User;
import com.pack.enums.MembershipStatus;
import com.pack.enums.TeamRole;
import com.pack.exceptions.*;
import com.pack.mapper.TeamMemberMapper;
import com.pack.repository.TeamMemberRepository;
import com.pack.repository.TeamMemberSpecification;
import com.pack.repository.TeamRepository;
import com.pack.repository.UserRepository;
import com.pack.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamMemberServiceImpl implements TeamMemberService {

    private final TeamMemberRepository memberRepository;
    private final TeamRepository       teamRepository;
    private final UserRepository       userRepository;
    private final TeamMemberMapper     memberMapper;

    // ─── Add / Invite ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TeamMemberResponseDto addMember(TeamMemberRequestDto dto, boolean asInvite) {
        log.info("Adding user {} to team {} as role {} (invite={})",
                dto.userId(), dto.teamId(), dto.role(), asInvite);

        Team team = findTeamOrThrow(dto.teamId());
        User user = findUserOrThrow(dto.userId());

        if (memberRepository.existsByTeamIdAndUserId(dto.teamId(), dto.userId())) {
            throw new DuplicateMemberException(dto.userId(), dto.teamId());
        }

        // OWNER cannot be assigned through this path — it's set exactly once,
        // by TeamService.create(), or moved via TeamService.transferOwnership().
        if (dto.role() == TeamRole.OWNER) {
            throw new BusinessException("OWNER role cannot be assigned directly. " +
                    "It is set automatically on team creation, or via TeamService.transferOwnership().");
        }

        User invitedBy = dto.invitedByUserId() != null ? findUserOrThrow(dto.invitedByUserId()) : null;

        OffsetDateTime now = OffsetDateTime.now();
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(dto.role())
                .invitedBy(invitedBy)
                .status(asInvite ? MembershipStatus.PENDING : MembershipStatus.ACTIVE)
                .joinedAt(asInvite ? null : now)
                .build();

        TeamMember saved = memberRepository.save(member);
        log.info("Membership created: {}", saved.getId());
        return memberMapper.toResponseDto(saved);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    public TeamMemberResponseDto getById(UUID id) {
        return memberMapper.toResponseDto(findMemberOrThrow(id));
    }

    @Override
    public TeamMemberResponseDto getByTeamAndUser(UUID teamId, UUID userId) {
        TeamMember member = memberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", teamId, userId));
        return memberMapper.toResponseDto(member);
    }

    @Override
    public Page<TeamMemberResponseDto> getByTeam(UUID teamId, Pageable pageable) {
        if (!teamRepository.existsById(teamId)) throw new ResourceNotFoundException("Team", teamId);
        return memberRepository.findByTeamIdWithDetails(teamId, pageable).map(memberMapper::toResponseDto);
    }

    @Override
    public Page<TeamMemberResponseDto> getByUser(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) throw new ResourceNotFoundException("User", userId);
        return memberRepository.findByUserIdWithTeam(userId, pageable).map(memberMapper::toResponseDto);
    }

    @Override
    public Page<TeamMemberResponseDto> search(
            UUID teamId, UUID userId, TeamRole role, MembershipStatus status, Pageable pageable) {
        return memberRepository
                .findAll(TeamMemberSpecification.withFilters(teamId, userId, role, status), pageable)
                .map(memberMapper::toResponseDto);
    }

    @Override
    public List<TeamMemberResponseDto> getActiveByRole(UUID teamId, TeamRole role) {
        if (!teamRepository.existsById(teamId)) throw new ResourceNotFoundException("Team", teamId);
        return memberRepository.findActiveByTeamIdAndRole(teamId, role)
                .stream().map(memberMapper::toResponseDto).toList();
    }

    // ─── Lifecycle: invite acceptance ──────────────────────────────────────────

    @Override
    @Transactional
    public TeamMemberResponseDto acceptInvite(UUID membershipId) {
        log.info("Accepting invite for membership: {}", membershipId);
        TeamMember member = findMemberOrThrow(membershipId);

        if (member.getStatus() != MembershipStatus.PENDING) {
            throw new InvalidMembershipStateException(
                    String.format("Membership %s is not in PENDING state (current: %s)",
                            membershipId, member.getStatus()));
        }

        OffsetDateTime now = OffsetDateTime.now();
        memberRepository.markAccepted(membershipId, now);
        member.setStatus(MembershipStatus.ACTIVE);
        member.setJoinedAt(now);
        return memberMapper.toResponseDto(member);
    }

    // ─── Role change ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TeamMemberResponseDto changeRole(UUID id, RoleChangeRequestDto dto) {
        log.info("Changing role of membership {} to {}", id, dto.newRole());
        TeamMember member = findMemberOrThrow(id);

        if (dto.newRole() == TeamRole.OWNER) {
            throw new BusinessException("OWNER role cannot be assigned via role change. " +
                    "Use TeamService.transferOwnership() instead.");
        }

        if (member.getRole() == TeamRole.OWNER
                && member.getStatus() == MembershipStatus.ACTIVE
                && memberRepository.countActiveOwners(member.getTeam().getId()) <= 1) {
            throw new LastOwnerException(member.getTeam().getId());
        }

        memberRepository.updateRole(id, dto.newRole());
        member.setRole(dto.newRole());
        return memberMapper.toResponseDto(member);
    }

    // ─── Status change (suspend / reactivate / remove) ─────────────────────────

    @Override
    @Transactional
    public TeamMemberResponseDto changeStatus(UUID id, MembershipStatusChangeDto dto) {
        log.info("Changing status of membership {} to {}", id, dto.status());
        TeamMember member = findMemberOrThrow(id);

        boolean leavingActiveState = member.getStatus() == MembershipStatus.ACTIVE
                && dto.status() != MembershipStatus.ACTIVE;

        if (member.getRole() == TeamRole.OWNER && leavingActiveState
                && memberRepository.countActiveOwners(member.getTeam().getId()) <= 1) {
            throw new LastOwnerException(member.getTeam().getId());
        }

        OffsetDateTime now = OffsetDateTime.now();
        memberRepository.updateStatus(id, dto.status(), dto.reason(), now);

        member.setStatus(dto.status());
        member.setStatusReason(dto.reason());
        if (dto.status() == MembershipStatus.REMOVED) member.setLeftAt(now);

        return memberMapper.toResponseDto(member);
    }

    @Override
    @Transactional
    public void removeMember(UUID id, String reason) {
        changeStatus(id, new MembershipStatusChangeDto(MembershipStatus.REMOVED, reason));
    }

    @Override
    @Transactional
    public void hardDelete(UUID id) {
        log.warn("Hard-deleting membership: {}", id);
        TeamMember member = findMemberOrThrow(id);

        if (member.getRole() == TeamRole.OWNER
                && member.getStatus() == MembershipStatus.ACTIVE
                && memberRepository.countActiveOwners(member.getTeam().getId()) <= 1) {
            throw new LastOwnerException(member.getTeam().getId());
        }

        memberRepository.deleteById(id);
    }

    // ─── Aggregation ──────────────────────────────────────────────────────────

    @Override
    public MembershipSummaryDto getSummary(UUID teamId) {
        if (!teamRepository.existsById(teamId)) throw new ResourceNotFoundException("Team", teamId);

        long active    = memberRepository.countByTeamIdAndStatus(teamId, MembershipStatus.ACTIVE);
        long pending   = memberRepository.countByTeamIdAndStatus(teamId, MembershipStatus.PENDING);
        long suspended = memberRepository.countByTeamIdAndStatus(teamId, MembershipStatus.SUSPENDED);

        Map<TeamRole, Long> activeByRole = memberRepository.countActiveGroupedByRole(teamId).stream()
                .collect(Collectors.toMap(row -> (TeamRole) row[0], row -> (Long) row[1]));

        return new MembershipSummaryDto(teamId, active, pending, suspended, activeByRole);
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private TeamMember findMemberOrThrow(UUID id) {
        return memberRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", id));
    }

    private Team findTeamOrThrow(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}