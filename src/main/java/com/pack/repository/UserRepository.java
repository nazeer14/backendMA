package com.pack.repository;

import com.pack.entity.User;
import com.pack.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // --- Lookup ---
    Optional<User> findByEmailId(String emailId);

    Optional<User> findByPhone(String phone);

    boolean existsByEmailId(String emailId);

    boolean existsByPhone(String phone);

    // --- Role-based queries ---
    List<User> findByRole(Role role);

    // --- Verification ---
    List<User> findByIsVerified(Boolean isVerified);

    Optional<User> findByEmailIdAndIsVerified(String emailId, Boolean isVerified);

    // --- Currency ---
    List<User> findByCurrency(String currency);

    // --- Search ---
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> searchByFullName(@Param("name") String name);

    @Query("SELECT u FROM User u WHERE u.emailId = :emailId OR u.phone = :phone")
    Optional<User> findByEmailIdOrPhone(@Param("emailId") String emailId,
                                        @Param("phone") String phone);

    // --- Team feature: find multiple users by email list (for invitations) ---
    @Query("SELECT u FROM User u WHERE u.emailId IN :emails")
    List<User> findAllByEmailIdIn(@Param("emails") List<String> emails);

    // --- Stats ---
    long countByRole(Role role);

    long countByIsVerified(Boolean isVerified);
}