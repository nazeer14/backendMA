package com.pack.exceptions;


import java.util.UUID;

public class DuplicateMemberException extends TeamException {
    public DuplicateMemberException(UUID userId, UUID teamId) {
        super(String.format("User %s is already a member of team %s", userId, teamId));
    }
}