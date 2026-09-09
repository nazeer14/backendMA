package com.pack.exceptions;

import com.pack.enums.TeamRole;
import com.pack.exceptions.TeamException;

import java.util.UUID;

public class InsufficientRoleException extends TeamException {
    public InsufficientRoleException(UUID userId, TeamRole required) {
        super(String.format("User %s does not have the required role [%s] to perform this action", userId, required));
    }

    public InsufficientRoleException(String message) {
        super(message);
    }
}