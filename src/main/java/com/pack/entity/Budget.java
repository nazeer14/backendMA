package com.pack.entity;

import com.pack.enums.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "budgets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_budget_user_category_month_year",
                        columnNames = {
                                "user_id",
                                "category",
                                "budget_month",
                                "budget_year"
                        }
                )
        },
        indexes = {
                @Index(name = "idx_budget_user", columnList = "user_id"),
                @Index(name = "idx_budget_month_year", columnList = "budget_month,budget_year"),
                @Index(name = "idx_budget_category", columnList = "category")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_budget_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @NotNull
    @Min(1)
    @Max(12)
    @Column(name = "budget_month", nullable = false)
    private Integer budgetMonth;

    @NotNull
    @Column(name = "budget_year", nullable = false)
    private Integer budgetYear;

    @NotNull
    @DecimalMin("0.01")
    @Column(
            name = "amount_limit",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal amountLimit;

    // ─── Forward-looking additions ─────────────────────────────────────────────

    /** Percentage (0-100) of amountLimit at which an alert/notification should fire. */
    @DecimalMin("1.0")
    @DecimalMax("100.0")
    @Builder.Default
    @Column(name = "alert_threshold_percent", precision = 5, scale = 2)
    private BigDecimal alertThresholdPercent = BigDecimal.valueOf(80);

    /** Whether threshold-breach notifications are enabled for this budget. */
    @NotNull
    @Builder.Default
    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled = true;

    /** Optional free-text note (e.g. "Excludes recurring subscriptions"). */
    @Column(length = 500)
    private String notes;

    /**
     * If true, this budget auto-renews into the following month at the same
     * amountLimit when a scheduled job rolls budgets forward. Enables
     * "recurring budget" UX without forcing the user to recreate it monthly.
     */
    @NotNull
    @Builder.Default
    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = false;

    /** Soft-delete flag — keeps historical budgets queryable for reporting even after removal. */
    @NotNull
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * Optimistic locking — protects against lost updates when a budget is
     * edited concurrently (e.g. user on two devices, or a background
     * rollover job racing a manual edit).
     */
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    // ─── Domain helpers ─────────────────────────────────────────────────────────

    @Transient
    public boolean isCurrentPeriod(int month, int year) {
        return this.budgetMonth == month && this.budgetYear == year;
    }
}
