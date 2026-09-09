package com.pack.service.impl;

import com.pack.dto.BudgetFilterDto;
import com.pack.dto.BudgetSummaryDto;
import com.pack.dto.request.BudgetRequestDto;
import com.pack.dto.response.BudgetResponseDto;
import com.pack.entity.Budget;
import com.pack.entity.User;
import com.pack.exceptions.BusinessException;
import com.pack.exceptions.ConcurrentModificationException;
import com.pack.exceptions.DuplicateBudgetException;
import com.pack.exceptions.ResourceNotFoundException;
import com.pack.mapper.BudgetMapper;
import com.pack.repository.BudgetRepository;
import com.pack.repository.BudgetSpecification;
import com.pack.repository.ExpenseRepository;
import com.pack.repository.UserRepository;
import com.pack.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository  budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository    userRepository;
    private final BudgetMapper      budgetMapper;

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public BudgetResponseDto create(BudgetRequestDto dto) {
        log.info("Creating budget for user: {}, category: {}, period: {}/{}",
                dto.userId(), dto.category(), dto.budgetMonth(), dto.budgetYear());

        User user = findUserOrThrow(dto.userId());

        if (budgetRepository.existsByUserIdAndCategoryAndBudgetMonthAndBudgetYearAndIsDeletedFalse(
                dto.userId(), dto.category(), dto.budgetMonth(), dto.budgetYear())) {
            throw new DuplicateBudgetException(dto.userId(), dto.category(), dto.budgetMonth(), dto.budgetYear());
        }

        Budget budget = budgetMapper.toEntity(dto);
        budget.setUser(user);

        Budget saved = budgetRepository.save(budget);
        log.info("Budget created with id: {}", saved.getId());
        return enrich(saved);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    public BudgetResponseDto getById(UUID id) {
        Budget budget = budgetRepository.findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        return enrich(budget);
    }

    @Override
    public Page<BudgetResponseDto> search(BudgetFilterDto filter, Pageable pageable) {
        return budgetRepository.findAll(BudgetSpecification.withFilter(filter), pageable)
                .map(this::enrich);
    }

    @Override
    public Page<BudgetResponseDto> getByUserId(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return budgetRepository.findByUserId(userId, pageable).map(this::enrich);
    }

    @Override
    public List<BudgetResponseDto> getByUserIdAndPeriod(UUID userId, Integer month, Integer year) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return budgetRepository.findByUserIdAndPeriod(userId, month, year)
                .stream()
                .map(this::enrich)
                .toList();
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public BudgetResponseDto update(UUID id, BudgetRequestDto dto) {
        log.info("Updating budget: {}", id);

        Budget budget = budgetRepository.findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));

        // category/month/year are immutable post-creation to preserve uniqueness
        // and historical integrity; only limit/notification/notes/auto-renew change.
        budgetMapper.updateEntityFromDto(dto, budget);

        try {
            Budget saved = budgetRepository.saveAndFlush(budget);
            return enrich(saved);
        } catch (OptimisticLockingFailureException ex) {
            throw new ConcurrentModificationException(id);
        }
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Soft-deleting budget: {}", id);
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget", id);
        }
        budgetRepository.softDelete(id);
    }

    @Override
    @Transactional
    public void hardDelete(UUID id) {
        log.warn("Hard-deleting budget: {}", id);
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget", id);
        }
        budgetRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BudgetResponseDto restore(UUID id) {
        log.info("Restoring budget: {}", id);
        Budget budget = budgetRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));

        if (!Boolean.TRUE.equals(budget.getIsDeleted())) {
            throw new BusinessException("Budget is not deleted, nothing to restore.");
        }

        // Guard against restoring into a slot now occupied by a newer active budget
        if (budgetRepository.existsByUserIdAndCategoryAndBudgetMonthAndBudgetYearAndIsDeletedFalse(
                budget.getUser().getId(), budget.getCategory(), budget.getBudgetMonth(), budget.getBudgetYear())) {
            throw new DuplicateBudgetException(
                    budget.getUser().getId(), budget.getCategory(), budget.getBudgetMonth(), budget.getBudgetYear());
        }

        budgetRepository.restore(id);
        budget.setIsDeleted(false);
        return enrich(budget);
    }

    // ─── Aggregation ──────────────────────────────────────────────────────────

    @Override
    public BudgetSummaryDto getSummary(UUID userId, Integer month, Integer year) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        List<BudgetResponseDto> budgets = getByUserIdAndPeriod(userId, month, year);

        BigDecimal totalLimit = budgets.stream()
                .map(BudgetResponseDto::amountLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = budgets.stream()
                .map(BudgetResponseDto::amountSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = totalLimit.subtract(totalSpent).max(BigDecimal.ZERO);

        BigDecimal overallUtilization = totalLimit.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.divide(totalLimit, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long exceededCount = budgets.stream().filter(BudgetResponseDto::isExceeded).count();
        long alertCount    = budgets.stream().filter(BudgetResponseDto::isAlertTriggered).count();

        return new BudgetSummaryDto(
                month, year,
                totalLimit, totalSpent, totalRemaining, overallUtilization,
                exceededCount, alertCount,
                budgets
        );
    }

    // ─── Rollover (scheduled-job entry point) ──────────────────────────────────

    @Override
    @Transactional
    public int rolloverAutoRenewBudgets(Integer fromMonth, Integer fromYear) {
        log.info("Rolling over auto-renew budgets from {}/{}", fromMonth, fromYear);

        YearMonth current = YearMonth.of(fromYear, fromMonth);
        YearMonth next = current.plusMonths(1);

        List<Budget> renewables = budgetRepository.findAutoRenewableForPeriod(fromMonth, fromYear);
        int createdCount = 0;

        for (Budget source : renewables) {
            boolean alreadyExists = budgetRepository
                    .existsByUserIdAndCategoryAndBudgetMonthAndBudgetYearAndIsDeletedFalse(
                            source.getUser().getId(), source.getCategory(),
                            next.getMonthValue(), next.getYear());

            if (alreadyExists) {
                log.debug("Skipping rollover for user {} category {} — budget already exists for {}/{}",
                        source.getUser().getId(), source.getCategory(), next.getMonthValue(), next.getYear());
                continue;
            }

            Budget renewed = Budget.builder()
                    .user(source.getUser())
                    .category(source.getCategory())
                    .budgetMonth(next.getMonthValue())
                    .budgetYear(next.getYear())
                    .amountLimit(source.getAmountLimit())
                    .alertThresholdPercent(source.getAlertThresholdPercent())
                    .notificationsEnabled(source.getNotificationsEnabled())
                    .notes(source.getNotes())
                    .autoRenew(true)
                    .build();

            budgetRepository.save(renewed);
            createdCount++;
        }

        log.info("Rollover complete: {} new budget(s) created for {}/{}",
                createdCount, next.getMonthValue(), next.getYear());
        return createdCount;
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    /**
     * Enriches a Budget entity with live spend data computed from the Expense
     * table, and derives utilization / exceeded / alert-triggered flags.
     * Only approved expenses count toward spend.
     */
    private BudgetResponseDto enrich(Budget budget) {
        BigDecimal spent = expenseRepository.sumApprovedAmountByUserCategoryAndMonth(
                budget.getUser().getId(),
                budget.getCategory(),
                budget.getBudgetMonth(),
                budget.getBudgetYear());

        if (spent == null) spent = BigDecimal.ZERO;

        BigDecimal remaining = budget.getAmountLimit().subtract(spent).max(BigDecimal.ZERO);

        BigDecimal utilization = budget.getAmountLimit().compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(budget.getAmountLimit(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        boolean exceeded = spent.compareTo(budget.getAmountLimit()) > 0;
        boolean alertTriggered = budget.getNotificationsEnabled()
                && utilization.compareTo(budget.getAlertThresholdPercent()) >= 0;

        return budgetMapper.toResponseDto(budget, spent, remaining, utilization, exceeded, alertTriggered);
    }
}
