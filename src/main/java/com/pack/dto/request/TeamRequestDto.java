package com.pack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request payload for creating or updating a team. iconUrl is intentionally excluded — set it via the Image upload API.")
public record TeamRequestDto(


        @Schema(description = "UUID of the team owner", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "Owner ID is required")
        UUID ownerId,

        @Schema(description = "Name of the team", example = "Engineering Alpha")
        @NotBlank(message = "Team name is required")
        @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
        String teamName,

        @Schema(description = "Whether the team is active", defaultValue = "true")
        Boolean active
) {
    public TeamRequestDto {
        if (active == null) active = true;
    }
}