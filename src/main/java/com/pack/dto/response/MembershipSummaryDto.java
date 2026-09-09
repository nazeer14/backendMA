package com.pack.dto.response;

import com.pack.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

@Schema(description = "Aggregated membership statistics for a team")
public record MembershipSummaryDto(

        @Schema(description = "Team ID")
        UUID teamId,

        @Schema(description = "Total active members")
        long activeCount,

        @Schema(description = "Total pending (invited, not yet accepted) members")
        long pendingCount,

        @Schema(description = "Total suspended members")
        long suspendedCount,

        @Schema(description = "Active member count broken down by role")
        Map<TeamRole, Long> activeByRole
) {}