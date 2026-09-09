package com.pack.dto;

import com.pack.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

@Schema(description = "Filter criteria for querying budgets")
public record BudgetFilterDto(

        @Schema(description = "Filter by user ID")
        UUID userId,

        @Schema(description = "Filter by category")
        Category category,

        @Schema(description = "Filter by budget month (1-12)")
        @Min(1) @Max(12)
        Integer budgetMonth,

        @Schema(description = "Filter by budget year")
        Integer budgetYear,

        @Schema(description = "Filter by auto-renew flag")
        Boolean autoRenew,

        @Schema(description = "Include soft-deleted budgets in results", defaultValue = "false")
        Boolean includeDeleted
) {
    public BudgetFilterDto {
        if (includeDeleted == null) includeDeleted = false;
    }
}
