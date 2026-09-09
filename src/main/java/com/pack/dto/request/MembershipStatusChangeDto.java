package com.pack.dto.request;

import com.pack.enums.MembershipStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for changing a membership's lifecycle status")
public record MembershipStatusChangeDto(

        @Schema(description = "New status to set", example = "SUSPENDED")
        @NotNull(message = "Status is required")
        MembershipStatus status,

        @Schema(description = "Optional reason for the status change", example = "Inactive for 90+ days")
        @Size(max = 255, message = "Reason cannot exceed 255 characters")
        String reason
) {}