package com.pack.entity;

import com.pack.enums.IncomeSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "incomes",
        indexes = {
                @Index(name = "idx_income_user", columnList = "user_id"),
                @Index(name = "idx_income_date", columnList = "income_date"),
                @Index(name = "idx_income_source", columnList = "income_source")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Income extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_income_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_source", nullable = false)
    private IncomeSource incomeSource;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "income_date", nullable = false)
    private LocalDate incomeDate;

    @Column(columnDefinition = "TEXT")
    private String description;
}