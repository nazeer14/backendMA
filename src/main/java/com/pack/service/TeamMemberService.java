package com.pack.service;

import com.pack.dto.request.MembershipStatusChangeDto;
import com.pack.dto.request.RoleChangeRequestDto;
import com.pack.dto.request.TeamMemberRequestDto;
import com.pack.dto.response.MembershipSummaryDto;
import com.pack.dto.response.TeamMemberResponseDto;
import com.pack.enums.MembershipStatus;
import com.pack.enums.TeamRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TeamMemberService {

    /** Add a user to a team with a given role. Status defaults to ACTIVE (direct add) unless asInvite=true. */
    TeamMemberResponseDto addMember(TeamMemberRequestDto dto, boolean asInvite);

    /** Get a single membership by its own ID. */
    TeamMemberResponseDto getById(UUID id);

    /** Get the membership record for a specific user in a specific team. */
    TeamMemberResponseDto getByTeamAndUser(UUID teamId, UUID userId);

    /** Paginated members of a team. */
    Page<TeamMemberResponseDto> getByTeam(UUID teamId, Pageable pageable);

    /** Paginated teams a user belongs to. */
    Page<TeamMemberResponseDto> getByUser(UUID userId, Pageable pageable);

    /** Flexible search across team/user/role/status. */
    Page<TeamMemberResponseDto> search(UUID teamId, UUID userId, TeamRole role, MembershipStatus status, Pageable pageable);

    /** Accept a PENDING invite, transitioning it to ACTIVE and stamping joinedAt. */
    TeamMemberResponseDto acceptInvite(UUID membershipId);

    /** Change a member's role. Refuses if it would leave the team without any ACTIVE owner. */
    TeamMemberResponseDto changeRole(UUID id, RoleChangeRequestDto dto);

    /** Change lifecycle status (suspend, reactivate, mark removed). Refuses if it would remove the last owner. */
    TeamMemberResponseDto changeStatus(UUID id, MembershipStatusChangeDto dto);

    /** Convenience: marks status REMOVED with leftAt stamped — "soft" leave/remove. */
    void removeMember(UUID id, String reason);

    /** Permanently delete the membership row. Use sparingly — prefer removeMember for audit trail. */
    void hardDelete(UUID id);

    /** All ACTIVE members of a given role in a team. */
    List<TeamMemberResponseDto> getActiveByRole(UUID teamId, TeamRole role);

    /** Aggregated counts (active/pending/suspended, active-by-role) for a team. */
    MembershipSummaryDto getSummary(UUID teamId);
}