package com.pack.exceptions;

// ─── Base Exception ────────────────────────────────────────────────────────────

public class ExpenseException extends RuntimeException {
    public ExpenseException(String message) {
        super(message);
    }
    public ExpenseException(String message, Throwable cause) {
        super(message, cause);
    }
}