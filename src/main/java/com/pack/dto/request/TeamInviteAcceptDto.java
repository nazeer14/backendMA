package com.pack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload for accepting a team invite via token")
public record TeamInviteAcceptDto(

        @Schema(description = "Invite token from the invitation link", example = "abc123xyz")
        @NotBlank(message = "Invite token is required")
        String inviteToken,

        @Schema(description = "UUID of the user accepting the invite")
        @NotNull(message = "User ID is required")
        UUID userId
) {}