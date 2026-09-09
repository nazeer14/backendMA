package com.pack.exceptions;

public class TeamMemberException extends RuntimeException {
    public TeamMemberException(String message) { super(message); }
    public TeamMemberException(String message, Throwable cause) { super(message, cause); }
}