package com.pack.service;

import com.pack.dto.request.TeamRequestDto;
import com.pack.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TeamService {

    TeamResponseDto create(TeamRequestDto dto);

    TeamResponseDto getById(UUID teamId);

    Page<TeamResponseDto> getAll(Pageable pageable);

    Page<TeamResponseDto> getByOwnerId(UUID ownerId, Pageable pageable);

    Page<TeamResponseDto> searchByName(String keyword, Pageable pageable);

    TeamResponseDto update(UUID teamId, TeamRequestDto dto);

    TeamResponseDto toggleActive(UUID teamId, Boolean active);

    void delete(UUID teamId);

    TeamInviteResponseDto generateInviteToken(UUID teamId);

    void revokeInviteToken(UUID teamId);

    TeamResponseDto getByInviteToken(String token);

    /**
     * Called by the Image upload API after a successful S3 upload for owner type TEAM_ICON.
     * Centralizes the write so Team's icon column is never touched outside this path.
     */
    TeamResponseDto updateIconUrl(UUID teamId, String iconUrl);

    TeamSummaryDto getSummary(UUID teamId);

    /** Transfers ownership; delegates the membership-side role swap to TeamMemberService. */
    TeamResponseDto transferOwnership(UUID teamId, UUID newOwnerId);
}