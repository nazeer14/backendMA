package com.pack.dto.response;

import com.pack.enums.MembershipStatus;
import com.pack.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Team membership response payload")
public record TeamMemberResponseDto(

        UUID id,
        UUID teamId,
        String teamName,
        UUID userId,

        @Schema(description = "Full name of the member")
        String userFullName,

        TeamRole role,
        MembershipStatus status,
        UUID invitedByUserId,

        @Schema(description = "Full name of the user who invited/added this member")
        String invitedByFullName,

        OffsetDateTime joinedAt,
        OffsetDateTime leftAt,
        String statusReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}