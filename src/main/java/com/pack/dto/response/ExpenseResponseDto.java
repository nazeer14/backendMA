package com.pack.dto.response;

import com.pack.enums.Category;
import com.pack.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Expense response payload")
@Builder
public record ExpenseResponseDto(

        @Schema(description = "Unique identifier of the expense")
        UUID id,

        @Schema(description = "User who owns the expense")
        UUID userId,

        @Schema(description = "Username of the owner")
        String fullName,

        @Schema(description = "Team ID if this is a team expense")
        UUID teamId,

        @Schema(description = "Team name if this is a team expense")
        String teamName,

        @Schema(description = "Title of the expense")
        String title,

        @Schema(description = "Amount of the expense")
        BigDecimal amount,

        @Schema(description = "Category of the expense")
        Category category,

        @Schema(description = "Payment method used")
        PaymentMethod paymentMethod,

        @Schema(description = "Date when the expense occurred")
        LocalDate expenseDate,

        @Schema(description = "Additional notes")
        String notes,

        @Schema(description = "Whether the expense is approved")
        Boolean isApproved,

        @Schema(description = "Timestamp when the record was created")
        OffsetDateTime createdAt,

        @Schema(description = "Timestamp when the record was last updated")
        OffsetDateTime updatedAt
) {}