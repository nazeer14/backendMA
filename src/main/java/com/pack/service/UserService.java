package com.pack.service;

import com.pack.dto.UserCreateRequest;
import com.pack.dto.UserResponse;
import com.pack.dto.UserUpdateRequest;
import com.pack.enums.Role;

import java.util.List;
import java.util.UUID;

public interface UserService {

    // --- CRUD ---
    UserResponse createUser(UserCreateRequest request);

    UserResponse getUserById(UUID userId);

    UserResponse getUserByEmail(String emailId);

    UserResponse getUserByPhone(String phone);

    UserResponse updateUser(UUID userId, UserUpdateRequest request);

    void deleteUser(UUID userId);

    // --- Listing ---
    List<UserResponse> getAllUsers();

    List<UserResponse> getUsersByRole(Role role);

    List<UserResponse> searchUsersByName(String name);

    // --- Verification ---
    UserResponse verifyUser(UUID userId);

    UserResponse unverifyUser(UUID userId);

    List<UserResponse> getUnverifiedUsers();

    // --- Currency ---
    UserResponse updateCurrency(UUID userId, String currency);

    // --- Team Sharing feature ---
    List<UserResponse> getUsersByEmails(List<String> emails);

    boolean existsByEmail(String emailId);

    boolean existsByPhone(String phone);

    // --- Stats ---
    long countUsersByRole(Role role);

    long countVerifiedUsers();

    long countUnverifiedUsers();
}