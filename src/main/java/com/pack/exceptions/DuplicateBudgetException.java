package com.pack.exceptions;

import com.pack.enums.Category;

import java.util.UUID;

public class DuplicateBudgetException extends BudgetException {
    public DuplicateBudgetException(UUID userId, Category category, int month, int year) {
        super(String.format(
                "A budget already exists for user %s, category %s, period %d/%d",
                userId, category, month, year));
    }
}
