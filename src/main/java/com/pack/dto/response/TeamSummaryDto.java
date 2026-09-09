package com.pack.dto.response;

import com.pack.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

@Schema(description = "Aggregated statistics for a team")
public record TeamSummaryDto(

        @Schema(description = "Team ID")
        UUID teamId,

        @Schema(description = "Team name")
        String teamName,

        @Schema(description = "Total number of members")
        long totalMembers,

        @Schema(description = "Member count broken down by role")
        Map<TeamRole, Long> membersByRole,

        @Schema(description = "Whether the team is currently active")
        Boolean active
) {}