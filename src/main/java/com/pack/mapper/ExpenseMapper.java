package com.pack.mapper;

import com.pack.dto.request.ExpenseRequestDto;
import com.pack.dto.response.ExpenseResponseDto;
import com.pack.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponseDto toResponseDto(Expense expense) {

        if (expense == null) {
            return null;
        }

        return ExpenseResponseDto.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .paymentMethod(expense.getPaymentMethod())
                .expenseDate(expense.getExpenseDate())
                .notes(expense.getNotes())
                .isApproved(expense.getIsApproved())
                .userId(expense.getUser() != null ? expense.getUser().getId() : null)
                .fullName(expense.getUser() != null ? expense.getUser().getFullName() : null)
                .teamId(expense.getTeam() != null ? expense.getTeam().getId() : null)
                .teamName(expense.getTeam() != null ? expense.getTeam().getTeamName() : null)
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }

    public Expense toEntity(ExpenseRequestDto dto) {

        if (dto == null) {
            return null;
        }

        return Expense.builder()
                .title(dto.title())
                .amount(dto.amount())
                .category(dto.category())
                .paymentMethod(dto.paymentMethod())
                .expenseDate(dto.expenseDate())
                .notes(dto.notes())
                .build();
    }

    public void updateEntityFromDto(ExpenseRequestDto dto, Expense expense) {

        if (dto == null || expense == null) {
            return;
        }

        if (dto.title() != null) {
            expense.setTitle(dto.title());
        }

        if (dto.amount() != null) {
            expense.setAmount(dto.amount());
        }

        if (dto.category() != null) {
            expense.setCategory(dto.category());
        }

        if (dto.paymentMethod() != null) {
            expense.setPaymentMethod(dto.paymentMethod());
        }

        if (dto.expenseDate() != null) {
            expense.setExpenseDate(dto.expenseDate());
        }

        if (dto.notes() != null) {
            expense.setNotes(dto.notes());
        }
    }
}