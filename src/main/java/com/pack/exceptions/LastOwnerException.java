package com.pack.exceptions;

import java.util.UUID;

/** Thrown when an operation would leave a team with zero OWNER-role members. */
public class LastOwnerException extends TeamMemberException {
    public LastOwnerException(UUID teamId) {
        super(String.format(
                "Cannot complete this action: team %s would be left without an owner. " +
                        "Promote another member to OWNER first.", teamId));
    }
}