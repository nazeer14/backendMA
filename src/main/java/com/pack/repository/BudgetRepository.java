package com.pack.repository;

import com.pack.entity.Budget;
import com.pack.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID>, JpaSpecificationExecutor<Budget> {

    // ─── Join-fetch safe lookups ──────────────────────────────────────────────

    @Query("SELECT b FROM Budget b JOIN FETCH b.user WHERE b.id = :id AND b.isDeleted = false")
    Optional<Budget> findByIdActive(@Param("id") UUID id);

    @Query("SELECT b FROM Budget b JOIN FETCH b.user WHERE b.id = :id")
    Optional<Budget> findByIdIncludingDeleted(@Param("id") UUID id);

    // ─── Uniqueness check (create-time) ───────────────────────────────────────

    boolean existsByUserIdAndCategoryAndBudgetMonthAndBudgetYearAndIsDeletedFalse(
            UUID userId, Category category, Integer budgetMonth, Integer budgetYear);

    Optional<Budget> findByUserIdAndCategoryAndBudgetMonthAndBudgetYearAndIsDeletedFalse(
            UUID userId, Category category, Integer budgetMonth, Integer budgetYear);

    // ─── User + period queries ────────────────────────────────────────────────

    @Query("SELECT b FROM Budget b JOIN FETCH b.user " +
           "WHERE b.user.id = :userId AND b.budgetMonth = :month AND b.budgetYear = :year " +
           "AND b.isDeleted = false")
    List<Budget> findByUserIdAndPeriod(
            @Param("userId") UUID userId,
            @Param("month") Integer month,
            @Param("year") Integer year);

    @Query("SELECT b FROM Budget b JOIN FETCH b.user " +
           "WHERE b.user.id = :userId AND b.isDeleted = false")
    Page<Budget> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    Page<Budget> findByUserIdAndCategoryAndIsDeletedFalse(UUID userId, Category category, Pageable pageable);

    // ─── Auto-renew rollover support ──────────────────────────────────────────

    @Query("SELECT b FROM Budget b JOIN FETCH b.user " +
           "WHERE b.autoRenew = true AND b.budgetMonth = :month AND b.budgetYear = :year " +
           "AND b.isDeleted = false")
    List<Budget> findAutoRenewableForPeriod(
            @Param("month") Integer month,
            @Param("year") Integer year);

    // ─── Soft delete ──────────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE Budget b SET b.isDeleted = true WHERE b.id = :id")
    void softDelete(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Budget b SET b.isDeleted = false WHERE b.id = :id")
    void restore(@Param("id") UUID id);

    // ─── Aggregation ──────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(b.amountLimit), 0) FROM Budget b " +
           "WHERE b.user.id = :userId AND b.budgetMonth = :month AND b.budgetYear = :year " +
           "AND b.isDeleted = false")
    java.math.BigDecimal sumLimitByUserIdAndPeriod(
            @Param("userId") UUID userId,
            @Param("month") Integer month,
            @Param("year") Integer year);

    // ─── Existence / count ────────────────────────────────────────────────────

    boolean existsByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndIsDeletedFalse(UUID userId);

    long countByUserIdAndBudgetMonthAndBudgetYearAndIsDeletedFalse(UUID userId, Integer month, Integer year);
}
