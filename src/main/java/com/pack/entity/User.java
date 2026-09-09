package com.pack.entity;

import com.pack.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "emailId"),
                @UniqueConstraint(columnNames = "phone")
        }

)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User Information")
public class User extends BaseEntity {


    private String fullName;

    private String emailId;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(nullable = false)
    private String currency = "INR";

}