package com.pack.dto.request;

import com.pack.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for changing a team member's role")
public record RoleChangeRequestDto(

        @Schema(description = "New role to assign", example = "ADMIN")
        @NotNull(message = "Role is required")
        TeamRole newRole
) {}