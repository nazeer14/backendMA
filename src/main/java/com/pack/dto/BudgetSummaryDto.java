package com.pack.dto;

import com.pack.dto.response.BudgetResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Aggregated budget overview for a user across all categories in a period")
public record BudgetSummaryDto(

        @Schema(description = "Budget month")
        Integer budgetMonth,

        @Schema(description = "Budget year")
        Integer budgetYear,

        @Schema(description = "Total of all category limits combined")
        BigDecimal totalLimit,

        @Schema(description = "Total spent across all categories")
        BigDecimal totalSpent,

        @Schema(description = "Total remaining across all categories")
        BigDecimal totalRemaining,

        @Schema(description = "Overall utilization percentage")
        BigDecimal overallUtilizationPercent,

        @Schema(description = "Number of budgets currently exceeded")
        long exceededCount,

        @Schema(description = "Number of budgets that have crossed their alert threshold")
        long alertTriggeredCount,

        @Schema(description = "Per-category budget breakdown")
        List<BudgetResponseDto> budgets
) {}
