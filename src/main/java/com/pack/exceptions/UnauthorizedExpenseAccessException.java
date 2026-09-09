package com.pack.exceptions;

import com.pack.exceptions.ExpenseException;

import java.util.UUID;

public class UnauthorizedExpenseAccessException extends ExpenseException {

    public UnauthorizedExpenseAccessException(UUID expenseId, UUID userId) {
        super(String.format("User %s is not authorized to access expense %s", userId, expenseId));
    }

    public UnauthorizedExpenseAccessException(String message) {
        super(message);
    }
}