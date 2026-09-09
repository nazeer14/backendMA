package com.pack.service.impl;

import com.pack.dto.UserCreateRequest;
import com.pack.dto.UserResponse;
import com.pack.dto.UserUpdateRequest;
import com.pack.entity.User;
import com.pack.enums.Role;
import com.pack.exceptions.ConflictException;
import com.pack.exceptions.ResourceNotFoundException;
import com.pack.repository.UserRepository;
import com.pack.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;

    // ─── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with email: {}", request.getEmailId());

        if (userRepo.existsByEmailId(request.getEmailId())) {
            throw new ConflictException("Email already registered: " + request.getEmailId());
        }
        if (userRepo.existsByPhone(request.getPhone())) {
            throw new ConflictException("Phone already registered: " + request.getPhone());
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmailId(request.getEmailId());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);
        user.setIsVerified(false);
        user.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");

        User saved = userRepo.save(user);
        log.info("User created with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        return toResponse(findUserOrThrow(userId));
    }

    @Override
    public UserResponse getUserByEmail(String emailId) {
        User user = userRepo.findByEmailId(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + emailId));
        return toResponse(user);
    }

    @Override
    public UserResponse getUserByPhone(String phone) {
        User user = userRepo.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phone));
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        log.info("Updating user: {}", userId);
        User user = findUserOrThrow(userId);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepo.existsByPhone(request.getPhone())) {
                throw new RuntimeException("Phone already in use: " + request.getPhone());
            }
            user.setPhone(request.getPhone());
        }
        if (request.getCurrency() != null) {
            user.setCurrency(request.getCurrency());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        return toResponse(userRepo.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user: {}", userId);
        User user = findUserOrThrow(userId);
        userRepo.delete(user);
    }

    // ─── Listing ───────────────────────────────────────────────────────────────

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepo.findByRole(role).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> searchUsersByName(String name) {
        return userRepo.searchByFullName(name).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Verification ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse verifyUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.setIsVerified(true);
        return toResponse(userRepo.save(user));
    }

    @Override
    @Transactional
    public UserResponse unverifyUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.setIsVerified(false);
        return toResponse(userRepo.save(user));
    }

    @Override
    public List<UserResponse> getUnverifiedUsers() {
        return userRepo.findByIsVerified(false).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Currency ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse updateCurrency(UUID userId, String currency) {
        User user = findUserOrThrow(userId);
        user.setCurrency(currency);
        return toResponse(userRepo.save(user));
    }

    // ─── Team Sharing ──────────────────────────────────────────────────────────

    @Override
    public List<UserResponse> getUsersByEmails(List<String> emails) {
        return userRepo.findAllByEmailIdIn(emails).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String emailId) {
        return userRepo.existsByEmailId(emailId);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepo.existsByPhone(phone);
    }

    // ─── Stats ─────────────────────────────────────────────────────────────────

    @Override
    public long countUsersByRole(Role role) {
        return userRepo.countByRole(role);
    }

    @Override
    public long countVerifiedUsers() {
        return userRepo.countByIsVerified(true);
    }

    @Override
    public long countUnverifiedUsers() {
        return userRepo.countByIsVerified(false);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private User findUserOrThrow(UUID userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .emailId(user.getEmailId())
                .phone(user.getPhone())
                .role(user.getRole())
                .isVerified(user.getIsVerified())
                .currency(user.getCurrency())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}