package com.pack.dto.request;

import com.pack.enums.Category;
import com.pack.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Filter criteria for querying expenses")
public record ExpenseFilterDto(

        @Schema(description = "Filter by user ID")
        UUID userId,

        @Schema(description = "Filter by team ID")
        UUID teamId,

        @Schema(description = "Filter by category")
        Category category,

        @Schema(description = "Filter by payment method")
        PaymentMethod paymentMethod,

        @Schema(description = "Filter expenses from this date (inclusive)", example = "2024-01-01")
        @PastOrPresent(message = "Start date cannot be in the future")
        LocalDate dateFrom,

        @Schema(description = "Filter expenses to this date (inclusive)", example = "2024-12-31")
        LocalDate dateTo,

        @Schema(description = "Minimum amount filter", example = "10.00")
        BigDecimal minAmount,

        @Schema(description = "Maximum amount filter", example = "10000.00")
        BigDecimal maxAmount,

        @Schema(description = "Filter by approval status")
        Boolean isApproved,

        @Schema(description = "Search by title keyword")
        String titleKeyword
) {}