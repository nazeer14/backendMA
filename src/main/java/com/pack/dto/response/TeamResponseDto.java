package com.pack.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Team response payload")
public record TeamResponseDto(

        @Schema(description = "Unique identifier of the team")
        UUID id,

        @Schema(description = "UUID of the team owner")
        UUID ownerId,

        @Schema(description = "Full name of the team owner")
        String ownerFullName,

        @Schema(description = "Name of the team")
        String teamName,

        @Schema(description = "Invite token for joining the team")
        String inviteToken,

        @Schema(description = "Icon URL — managed via the Image upload API, read-only here")
        String iconUrl,

        @Schema(description = "Whether the team is active")
        Boolean active,

        @Schema(description = "Total number of active members in the team")
        long memberCount,

        @Schema(description = "Timestamp when the team was created")
        OffsetDateTime createdAt,

        @Schema(description = "Timestamp when the team was last updated")
        OffsetDateTime updatedAt
) {}