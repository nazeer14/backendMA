package com.pack.dto.response;

import com.pack.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Aggregated expense summary")
public record ExpenseSummaryDto(

        @Schema(description = "Total number of expenses")
        long totalCount,

        @Schema(description = "Total amount across all expenses")
        BigDecimal totalAmount,

        @Schema(description = "Average expense amount")
        BigDecimal averageAmount,

        @Schema(description = "Minimum expense amount")
        BigDecimal minAmount,

        @Schema(description = "Maximum expense amount")
        BigDecimal maxAmount,

        @Schema(description = "Total amount grouped by category")
        Map<Category, BigDecimal> amountByCategory,

        @Schema(description = "Count grouped by category")
        Map<Category, Long> countByCategory,

        @Schema(description = "Total amount of approved expenses")
        BigDecimal approvedTotal,

        @Schema(description = "Total amount of pending expenses")
        BigDecimal pendingTotal
) {}