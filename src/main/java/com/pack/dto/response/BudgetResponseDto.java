package com.pack.dto.response;

import com.pack.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Budget response payload, including live spend tracking")
@Builder
public record BudgetResponseDto(

        @Schema(description = "Unique identifier of the budget")
        UUID id,

        @Schema(description = "UUID of the owning user")
        UUID userId,

        @Schema(description = "Username of the owning user")
        String username,

        @Schema(description = "Spending category")
        Category category,

        @Schema(description = "Budget month (1-12)")
        Integer budgetMonth,

        @Schema(description = "Budget year")
        Integer budgetYear,

        @Schema(description = "Maximum spending limit")
        BigDecimal amountLimit,

        @Schema(description = "Total amount spent so far in this category/period")
        BigDecimal amountSpent,

        @Schema(description = "Remaining budget (limit - spent), floored at 0")
        BigDecimal amountRemaining,

        @Schema(description = "Percentage of the budget consumed so far")
        BigDecimal utilizationPercent,

        @Schema(description = "Whether the budget has been exceeded")
        Boolean isExceeded,

        @Schema(description = "Whether spend has crossed the alert threshold")
        Boolean isAlertTriggered,

        @Schema(description = "Alert threshold percentage")
        BigDecimal alertThresholdPercent,

        @Schema(description = "Whether notifications are enabled")
        Boolean notificationsEnabled,

        @Schema(description = "Optional notes")
        String notes,

        @Schema(description = "Whether this budget auto-renews monthly")
        Boolean autoRenew,

        @Schema(description = "Optimistic-locking version")
        Long version,

        @Schema(description = "Timestamp when the budget was created")
        OffsetDateTime createdAt,

        @Schema(description = "Timestamp when the budget was last updated")
        OffsetDateTime updatedAt
) {}
