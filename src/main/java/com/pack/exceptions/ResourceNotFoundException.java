package com.pack.exceptions;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message){super(message);}

    public ResourceNotFoundException(String resource, UUID id) {
        super(String.format("%s not found with id: %s", resource, id));
    }

    public ResourceNotFoundException(String resource, UUID id,UUID id2) {
        super(String.format("%s not found with TeamId: %s and UserId: %s", resource, id,id2));
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: %s", resource, field, value));
    }


}


