package com.pack.exceptions;

import java.util.UUID;

public class ConcurrentModificationException extends BudgetException {
    public ConcurrentModificationException(UUID id) {
        super(String.format(
                "Budget %s was modified by another request. Please refresh and try again.", id));
    }
}
