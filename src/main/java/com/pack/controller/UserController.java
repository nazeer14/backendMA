package com.pack.controller;

import com.pack.dto.UserCreateRequest;
import com.pack.dto.UserResponse;
import com.pack.dto.UserUpdateRequest;
import com.pack.enums.Role;
import com.pack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "USER API", description = "User Money Management Operations")
public class UserController {

    private final UserService userService;

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // POST   /api/v1/users                  → Register / create a user
    // GET    /api/v1/users/{id}             → Fetch user by ID
    // GET    /api/v1/users/email/{email}    → Fetch user by email (team invite look-up)
    // GET    /api/v1/users/phone/{phone}    → Fetch user by phone
    // PUT    /api/v1/users/{id}             → Update profile (name, phone, currency, role)
    // DELETE /api/v1/users/{id}             → Remove account
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Register a new user.
     * Used during onboarding; salary/expense features become available after verification.
     */
    @PostMapping
    @Operation(
            summary = "Create User",
            description = "Registers a new user account during onboarding. Salary, expense, and savings " +
                    "features remain locked until the account is verified."
    )
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get user profile by UUID.
     * Referenced from Dashboard, Savings Goals, and Team Expense screens.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get User By ID",
            description = "Fetches a single user's profile by UUID. Used by the Dashboard, Savings Goals, " +
                    "and Team Expense screens."
    )
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "UUID of the user to fetch") @PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Get user by email address.
     * Needed by Team Expense Sharing when inviting members via email.
     */
    @GetMapping("/email/{email}")
    @Operation(
            summary = "Get User By Email",
            description = "Fetches a user by their email address. Used by Team Expense Sharing when " +
                    "inviting members via email."
    )
    public ResponseEntity<UserResponse> getUserByEmail(
            @Parameter(description = "Email address of the user to fetch") @PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    /**
     * Get user by phone number.
     */
    @GetMapping("/phone/{phone}")
    @Operation(
            summary = "Get User By Phone",
            description = "Fetches a user by their registered phone number."
    )
    public ResponseEntity<UserResponse> getUserByPhone(
            @Parameter(description = "Phone number of the user to fetch") @PathVariable String phone) {
        return ResponseEntity.ok(userService.getUserByPhone(phone));
    }

    /**
     * Update user profile.
     * Supports updating fullName, phone, currency (multi-currency income tracking),
     * and role (admin / member for Team Expense Sharing).
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update User",
            description = "Updates a user's profile: fullName, phone, currency (for multi-currency income " +
                    "tracking), and role (admin / member, used by Team Expense Sharing)."
    )
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "UUID of the user to update") @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * Delete a user account and all associated data.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete User",
            description = "Permanently deletes a user account along with all associated data. This action " +
                    "cannot be undone."
    )
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "UUID of the user to delete") @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Listing & Search
    // GET /api/v1/users                     → All users (admin)
    // GET /api/v1/users/role/{role}         → Filter by role
    // GET /api/v1/users/search?name=        → Full-name search (team invite)
    // POST /api/v1/users/by-emails          → Bulk lookup for team invitations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * List all users. Intended for admin dashboards / analytics.
     */
    @GetMapping
    @Operation(
            summary = "Get All Users",
            description = "Returns every registered user. Intended for admin dashboards and analytics views."
    )
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Filter users by role (ADMIN / MEMBER / USER).
     * Used in Team Expense Sharing to list team admins or members.
     */
    @GetMapping("/role/{role}")
    @Operation(
            summary = "Get Users By Role",
            description = "Filters users by role (ADMIN / MEMBER / USER). Used by Team Expense Sharing to " +
                    "list team admins or members."
    )
    public ResponseEntity<List<UserResponse>> getUsersByRole(
            @Parameter(description = "Role to filter by: ADMIN, MEMBER, or USER") @PathVariable Role role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    /**
     * Search users by full name.
     * Used in the Team Expense Sharing invite flow.
     */
    @GetMapping("/search")
    @Operation(
            summary = "Search Users By Name",
            description = "Searches users by full name (partial match). Used in the Team Expense Sharing " +
                    "invite flow."
    )
    public ResponseEntity<List<UserResponse>> searchUsers(
            @Parameter(description = "Full or partial name to search for") @RequestParam String name) {
        return ResponseEntity.ok(userService.searchUsersByName(name));
    }

    /**
     * Bulk user look-up by email list.
     * Used when creating a team and inviting multiple members at once.
     */
    @PostMapping("/by-emails")
    @Operation(
            summary = "Get Users By Emails (Bulk)",
            description = "Looks up multiple users at once by a list of email addresses. Used when creating " +
                    "a team and inviting several members simultaneously."
    )
    public ResponseEntity<List<UserResponse>> getUsersByEmails(
            @Parameter(description = "List of email addresses to look up") @RequestBody List<String> emails) {
        return ResponseEntity.ok(userService.getUsersByEmails(emails));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Verification
    // PATCH /api/v1/users/{id}/verify       → Mark user as verified
    // PATCH /api/v1/users/{id}/unverify     → Revoke verification
    // GET   /api/v1/users/unverified        → List unverified accounts (admin)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verify a user account (e.g., after OTP / email confirmation).
     * Unlocks Expense Tracking, Salary & Income, and Savings Goal features.
     */
    @PatchMapping("/{id}/verify")
    @Operation(
            summary = "Verify User",
            description = "Marks a user account as verified (e.g., after OTP or email confirmation). " +
                    "Unlocks Expense Tracking, Salary & Income, and Savings Goal features."
    )
    public ResponseEntity<UserResponse> verifyUser(
            @Parameter(description = "UUID of the user to verify") @PathVariable UUID id) {
        return ResponseEntity.ok(userService.verifyUser(id));
    }

    /**
     * Revoke verification (admin action).
     */
    @PatchMapping("/{id}/unverify")
    @Operation(
            summary = "Unverify User",
            description = "Revokes a user's verification status. This is an admin-only action that re-locks " +
                    "verification-gated features."
    )
    public ResponseEntity<UserResponse> unverifyUser(
            @Parameter(description = "UUID of the user to unverify") @PathVariable UUID id) {
        return ResponseEntity.ok(userService.unverifyUser(id));
    }

    /**
     * List all unverified users for admin review.
     */
    @GetMapping("/unverified")
    @Operation(
            summary = "Get Unverified Users",
            description = "Lists all user accounts that are not yet verified, for admin review."
    )
    public ResponseEntity<List<UserResponse>> getUnverifiedUsers() {
        return ResponseEntity.ok(userService.getUnverifiedUsers());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Currency
    // PATCH /api/v1/users/{id}/currency     → Update preferred currency
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Update the user's preferred currency.
     * Affects Dashboard summary cards and net balance formula display.
     * Supports multi-currency income tracking (e.g., INR, USD, EUR).
     */
    @PatchMapping("/{id}/currency")
    @Operation(
            summary = "Update User Currency",
            description = "Updates the user's preferred currency (e.g., INR, USD, EUR). Affects how Dashboard " +
                    "summary cards and the net balance figure are displayed. Supports multi-currency income tracking."
    )
    public ResponseEntity<UserResponse> updateCurrency(
            @Parameter(description = "UUID of the user to update") @PathVariable UUID id,
            @Parameter(description = "New preferred currency code, e.g., INR, USD, EUR") @RequestParam String currency) {
        return ResponseEntity.ok(userService.updateCurrency(id, currency));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Existence checks
    // GET /api/v1/users/exists/email/{email}
    // GET /api/v1/users/exists/phone/{phone}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Check whether an email is already registered.
     * Used during sign-up and team invite validation.
     */
    @GetMapping("/exists/email/{email}")
    @Operation(
            summary = "Check Email Exists",
            description = "Checks whether an email address is already registered. Used during sign-up and " +
                    "to validate team invites before sending them."
    )
    public ResponseEntity<Map<String, Boolean>> existsByEmail(
            @Parameter(description = "Email address to check") @PathVariable String email) {
        return ResponseEntity.ok(Map.of("exists", userService.existsByEmail(email)));
    }

    /**
     * Check whether a phone number is already registered.
     */
    @GetMapping("/exists/phone/{phone}")
    @Operation(
            summary = "Check Phone Exists",
            description = "Checks whether a phone number is already registered."
    )
    public ResponseEntity<Map<String, Boolean>> existsByPhone(
            @Parameter(description = "Phone number to check") @PathVariable String phone) {
        return ResponseEntity.ok(Map.of("exists", userService.existsByPhone(phone)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Stats  (admin / analytics)
    // GET /api/v1/users/stats
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Aggregate user statistics for the admin analytics dashboard.
     * Returns total counts by verification status and role.
     */
    @GetMapping("/stats")
    @Operation(
            summary = "Get User Statistics",
            description = "Returns aggregate user statistics for the admin analytics dashboard: total users, " +
                    "verified/unverified counts, and counts broken down by role (ADMIN, MEMBER, USER)."
    )
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = Map.of(
                "totalUsers",       userService.getAllUsers().size(),
                "verifiedUsers",    userService.countVerifiedUsers(),
                "unverifiedUsers",  userService.countUnverifiedUsers(),
                "adminCount",       userService.countUsersByRole(Role.ADMIN),
                "memberCount",      userService.countUsersByRole(Role.MEMBER),
                "userCount",        userService.countUsersByRole(Role.USER)
        );
        return ResponseEntity.ok(stats);
    }
}