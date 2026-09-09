package com.pack.repository;

import com.pack.entity.Expense;
import com.pack.enums.Category;
import com.pack.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

    // ─── Basic Finders ────────────────────────────────────────────────────────

    @Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.id = :id")
    Optional<Expense> findByIdWithUser(@Param("id") UUID id);

    @Query("SELECT e FROM Expense e JOIN FETCH e.user LEFT JOIN FETCH e.team WHERE e.id = :id")
    Optional<Expense> findByIdWithDetails(@Param("id") UUID id);

    // ─── User-scoped Queries ──────────────────────────────────────────────────

    Page<Expense> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.user.id = :userId ORDER BY e.expenseDate DESC")
    List<Expense> findRecentByUserId(@Param("userId") UUID userId, Pageable pageable);

    // ─── Team-scoped Queries ──────────────────────────────────────────────────

    Page<Expense> findByTeamId(UUID teamId, Pageable pageable);

    @Query("SELECT e FROM Expense e JOIN FETCH e.user WHERE e.team.id = :teamId AND e.isApproved = false")
    List<Expense> findPendingApprovalByTeamId(@Param("teamId") UUID teamId);

    // ─── Date-range Queries ───────────────────────────────────────────────────

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :from AND :to")
    Page<Expense> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.team.id = :teamId AND e.expenseDate BETWEEN :from AND :to")
    Page<Expense> findByTeamIdAndDateRange(
            @Param("teamId") UUID teamId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable);

    // ─── Aggregation Queries ──────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :from AND :to")
    BigDecimal sumAmountByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.team.id = :teamId AND e.expenseDate BETWEEN :from AND :to")
    BigDecimal sumAmountByTeamIdAndDateRange(
            @Param("teamId") UUID teamId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user.id = :userId GROUP BY e.category")
    List<Object[]> sumAmountGroupedByCategoryForUser(@Param("userId") UUID userId);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.team.id = :teamId GROUP BY e.category")
    List<Object[]> sumAmountGroupedByCategoryForTeam(@Param("teamId") UUID teamId);

    @Query("SELECT e.category, COUNT(e) FROM Expense e WHERE e.user.id = :userId GROUP BY e.category")
    List<Object[]> countGroupedByCategoryForUser(@Param("userId") UUID userId);

    // ─── Category / Payment Filters ───────────────────────────────────────────

    Page<Expense> findByUserIdAndCategory(UUID userId, Category category, Pageable pageable);

    Page<Expense> findByUserIdAndPaymentMethod(UUID userId, PaymentMethod paymentMethod, Pageable pageable);

    Page<Expense> findByUserIdAndIsApproved(UUID userId, Boolean isApproved, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND e.category = :category " +
            "AND e.isApproved = true " +
            "AND FUNCTION('MONTH', e.expenseDate) = :month " +
            "AND FUNCTION('YEAR', e.expenseDate) = :year")
    BigDecimal sumApprovedAmountByUserCategoryAndMonth(
            @Param("userId") UUID userId,
            @Param("category") Category category,
            @Param("month") Integer month,
            @Param("year") Integer year);

    // ─── Bulk Operations ──────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE Expense e SET e.isApproved = true WHERE e.team.id = :teamId AND e.isApproved = false")
    int approveAllPendingByTeamId(@Param("teamId") UUID teamId);

    @Modifying
    @Query("UPDATE Expense e SET e.isApproved = :status WHERE e.id IN :ids")
    int updateApprovalStatusByIds(@Param("ids") List<UUID> ids, @Param("status") Boolean status);

    // ─── Existence Checks ─────────────────────────────────────────────────────

    boolean existsByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndTeamId(UUID id, UUID teamId);

    // ─── Count Queries ────────────────────────────────────────────────────────

    long countByUserId(UUID userId);

    long countByTeamId(UUID teamId);

    long countByUserIdAndIsApproved(UUID userId, Boolean isApproved);
}