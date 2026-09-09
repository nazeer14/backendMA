package com.pack.exceptions;

import com.pack.exceptions.TeamException;

public class InvalidInviteTokenException extends TeamException {
    public InvalidInviteTokenException() {
        super("Invite token is invalid or has expired");
    }
    public InvalidInviteTokenException(String token) {
        super(String.format("Invite token '%s' is invalid or has expired", token));
    }
}