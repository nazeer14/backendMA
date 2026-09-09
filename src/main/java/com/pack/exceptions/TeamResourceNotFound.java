package com.pack.exceptions;

import java.util.UUID;

public class TeamResourceNotFound extends TeamMemberException {
    public TeamResourceNotFound(String resource, UUID id) {
        super(String.format("%s not found with id: %s", resource, id));
    }
    public TeamResourceNotFound(String resource, String field, Object value) {
        super(String.format("%s not found with %s: %s", resource, field, value));
    }
}