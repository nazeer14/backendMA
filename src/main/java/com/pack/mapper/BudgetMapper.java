package com.pack.mapper;

import com.pack.dto.request.BudgetRequestDto;
import com.pack.dto.response.BudgetResponseDto;
import com.pack.entity.Budget;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BudgetMapper {

    public Budget toEntity(BudgetRequestDto dto) {

        if (dto == null) {
            return null;
        }

        Budget budget = new Budget();

        budget.setCategory(dto.category());
        budget.setBudgetMonth(dto.budgetMonth());
        budget.setBudgetYear(dto.budgetYear());
        budget.setAmountLimit(dto.amountLimit());
        budget.setAlertThresholdPercent(dto.alertThresholdPercent());
        budget.setNotificationsEnabled(dto.notificationsEnabled());
        budget.setNotes(dto.notes());
        budget.setAutoRenew(dto.autoRenew());

        return budget;
    }

    public void updateEntityFromDto(
            BudgetRequestDto dto,
            Budget budget) {

        if (dto == null || budget == null) {
            return;
        }

        if (dto.category() != null) {
            budget.setCategory(dto.category());
        }

        if (dto.budgetMonth() != null) {
            budget.setBudgetMonth(dto.budgetMonth());
        }

        if (dto.budgetYear() != null) {
            budget.setBudgetYear(dto.budgetYear());
        }

        if (dto.amountLimit() != null) {
            budget.setAmountLimit(dto.amountLimit());
        }

        if (dto.alertThresholdPercent() != null) {
            budget.setAlertThresholdPercent(
                    dto.alertThresholdPercent()
            );
        }

        if (dto.notificationsEnabled() != null) {
            budget.setNotificationsEnabled(
                    dto.notificationsEnabled()
            );
        }

        if (dto.notes() != null) {
            budget.setNotes(dto.notes());
        }

        if (dto.autoRenew() != null) {
            budget.setAutoRenew(dto.autoRenew());
        }
    }

    public BudgetResponseDto toResponseDto(
            Budget budget,
            BigDecimal amountSpent,
            BigDecimal amountRemaining,
            BigDecimal utilizationPercent,
            Boolean isExceeded,
            Boolean isAlertTriggered) {

        if (budget == null) {
            return null;
        }

        return BudgetResponseDto.builder()
                .id(budget.getId())
                .userId(budget.getUser().getId())
                .username(budget.getUser().getFullName())
                .category(budget.getCategory())
                .budgetMonth(budget.getBudgetMonth())
                .budgetYear(budget.getBudgetYear())
                .amountLimit(budget.getAmountLimit())
                .alertThresholdPercent(
                        budget.getAlertThresholdPercent()
                )
                .notificationsEnabled(
                        budget.getNotificationsEnabled()
                )
                .notes(budget.getNotes())
                .autoRenew(budget.getAutoRenew())
                .amountSpent(amountSpent)
                .amountRemaining(amountRemaining)
                .utilizationPercent(utilizationPercent)
                .isExceeded(isExceeded)
                .isAlertTriggered(isAlertTriggered)
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}