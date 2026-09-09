package com.pack.dto.request;

import com.pack.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "Request payload for creating or updating a budget")
public record BudgetRequestDto(

        @Schema(description = "UUID of the user who owns this budget", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "User ID is required")
        java.util.UUID userId,

        @Schema(description = "Spending category this budget applies to", example = "FOOD")
        @NotNull(message = "Category is required")
        Category category,

        @Schema(description = "Month the budget applies to (1-12)", example = "6")
        @NotNull(message = "Budget month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        Integer budgetMonth,

        @Schema(description = "Year the budget applies to", example = "2026")
        @NotNull(message = "Budget year is required")
        @Min(value = 2000, message = "Year must be 2000 or later")
        @Max(value = 2100, message = "Year must be 2100 or earlier")
        Integer budgetYear,

        @Schema(description = "Maximum spending limit for the period", example = "5000.00")
        @NotNull(message = "Amount limit is required")
        @DecimalMin(value = "0.01", message = "Amount limit must be at least 0.01")
        @DecimalMax(value = "999999999.99", message = "Amount limit exceeds maximum allowed value")
        @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
        BigDecimal amountLimit,

        @Schema(description = "Percentage of limit at which to trigger an alert", example = "80", defaultValue = "80")
        @DecimalMin(value = "1.0", message = "Alert threshold must be at least 1%")
        @DecimalMax(value = "100.0", message = "Alert threshold cannot exceed 100%")
        BigDecimal alertThresholdPercent,

        @Schema(description = "Whether breach notifications are enabled", defaultValue = "true")
        Boolean notificationsEnabled,

        @Schema(description = "Optional notes about this budget", example = "Excludes recurring subscriptions")
        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes,

        @Schema(description = "Whether this budget should auto-renew into the next month", defaultValue = "false")
        Boolean autoRenew
) {
    public BudgetRequestDto {
        if (alertThresholdPercent == null) alertThresholdPercent = BigDecimal.valueOf(80);
        if (notificationsEnabled == null) notificationsEnabled = true;
        if (autoRenew == null) autoRenew = false;
    }
}
