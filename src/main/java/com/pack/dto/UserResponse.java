package com.pack.dto;

import com.pack.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserResponse {

    private UUID id;
    private String fullName;
    private String emailId;
    private String phone;
    private Role role;
    private Boolean isVerified;
    private String currency;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
