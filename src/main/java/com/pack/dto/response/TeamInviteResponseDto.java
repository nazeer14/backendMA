package com.pack.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response returned when generating or fetching a team invite link")
public record TeamInviteResponseDto(

        @Schema(description = "Team ID")
        UUID teamId,

        @Schema(description = "Team name")
        String teamName,

        @Schema(description = "The invite token (embed in invite URL)")
        String inviteToken,

        @Schema(description = "Full invite URL for sharing")
        String inviteUrl
) {}