package com.pack.dto.request;

import com.pack.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload for adding a member to a team")
public record TeamMemberRequestDto(

        @Schema(description = "Team UUID the member is being added to")
        @NotNull(message = "Team ID is required")
        UUID teamId,

        @Schema(description = "UUID of the user being added")
        @NotNull(message = "User ID is required")
        UUID userId,

        @Schema(description = "Role to assign", example = "MEMBER")
        @NotNull(message = "Role is required")
        TeamRole role,

        @Schema(description = "UUID of the user performing the invite/add action (for audit trail)")
        UUID invitedByUserId
) {}