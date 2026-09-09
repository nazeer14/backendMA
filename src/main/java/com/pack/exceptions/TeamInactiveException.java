package com.pack.exceptions;

import com.pack.exceptions.TeamException;

import java.util.UUID;

public class TeamInactiveException extends TeamException {
    public TeamInactiveException(UUID teamId) {
        super(String.format("Team %s is inactive and cannot be modified or joined", teamId));
    }
}