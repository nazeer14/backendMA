package com.pack.dto.request;

import com.pack.enums.Category;
import com.pack.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request payload for creating or updating an expense")
public record ExpenseRequestDto(

        @Schema(description = "ID of the user who owns this expense", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "User ID is required")
        UUID userId,

        @Schema(description = "ID of the team (optional for personal expenses)", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID teamId,

        @Schema(description = "Title of the expense", example = "Team lunch")
        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
        String title,

        @Schema(description = "Amount of the expense", example = "150.00")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        @DecimalMax(value = "999999999.99", message = "Amount exceeds maximum allowed value")
        @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
        BigDecimal amount,

        @Schema(description = "Category of the expense", example = "FOOD")
        @NotNull(message = "Category is required")
        Category category,

        @Schema(description = "Payment method used", example = "CREDIT_CARD")
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @Schema(description = "Date when the expense occurred", example = "2024-01-15")
        @NotNull(message = "Expense date is required")
        @PastOrPresent(message = "Expense date cannot be in the future")
        LocalDate expenseDate,

        @Schema(description = "Additional notes about the expense", example = "Client entertainment")
        @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
        String notes,

        @Schema(description = "Whether the expense is approved", defaultValue = "true")
        Boolean isApproved
) {
    public ExpenseRequestDto {
        if (isApproved == null) {
            isApproved = true;
        }
    }
}