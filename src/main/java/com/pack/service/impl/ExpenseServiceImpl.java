package com.pack.service.impl;

import com.pack.dto.request.ExpenseFilterDto;
import com.pack.dto.request.ExpenseRequestDto;
import com.pack.dto.response.ExpenseResponseDto;
import com.pack.dto.response.ExpenseSummaryDto;
import com.pack.entity.Expense;
import com.pack.entity.Team;
import com.pack.entity.User;
import com.pack.enums.Category;
import com.pack.exceptions.BusinessException;
import com.pack.exceptions.ResourceNotFoundException;
import com.pack.mapper.ExpenseMapper;
import com.pack.repository.ExpenseRepository;
import com.pack.repository.ExpenseSpecification;
import com.pack.repository.TeamRepository;
import com.pack.repository.UserRepository;
import com.pack.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseServiceImpl implements ExpenseService {

    private static final int MAX_BULK_SIZE = 500;

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ExpenseMapper expenseMapper;

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ExpenseResponseDto create(ExpenseRequestDto dto) {
        log.info("Creating expense for user: {}", dto.userId());

        User user = findUserOrThrow(dto.userId());
        Team team = resolveTeam(dto.teamId());

        Expense expense = expenseMapper.toEntity(dto);
        expense.setUser(user);
        expense.setTeam(team);

        Expense saved = expenseRepository.save(expense);
        log.info("Expense created with id: {}", saved.getId());
        return expenseMapper.toResponseDto(saved);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    public ExpenseResponseDto getById(UUID id) {
        return expenseRepository.findByIdWithDetails(id)
                .map(expenseMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
    }

    @Override
    public Page<ExpenseResponseDto> search(ExpenseFilterDto filter, Pageable pageable) {
        log.debug("Searching expenses with filter: {}", filter);
        return expenseRepository
                .findAll(ExpenseSpecification.withFilter(filter), pageable)
                .map(expenseMapper::toResponseDto);
    }

    @Override
    public Page<ExpenseResponseDto> getByUserId(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return expenseRepository.findByUserId(userId, pageable)
                .map(expenseMapper::toResponseDto);
    }

    @Override
    public Page<ExpenseResponseDto> getByTeamId(UUID teamId, Pageable pageable) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", teamId);
        }
        return expenseRepository.findByTeamId(teamId, pageable)
                .map(expenseMapper::toResponseDto);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ExpenseResponseDto update(UUID id, ExpenseRequestDto dto) {
        log.info("Updating expense: {}", id);

        Expense expense = findExpenseOrThrow(id);
        User user = findUserOrThrow(dto.userId());
        Team team = resolveTeam(dto.teamId());

        expenseMapper.updateEntityFromDto(dto, expense);
        expense.setUser(user);
        expense.setTeam(team);

        return expenseMapper.toResponseDto(expenseRepository.save(expense));
    }

    @Override
    @Transactional
    public ExpenseResponseDto updateApprovalStatus(UUID id, Boolean isApproved) {
        log.info("Updating approval status for expense: {} to: {}", id, isApproved);
        Expense expense = findExpenseOrThrow(id);
        expense.setIsApproved(isApproved);
        return expenseMapper.toResponseDto(expenseRepository.save(expense));
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting expense: {}", id);
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense", id);
        }
        expenseRepository.deleteById(id);
    }

    // ─── Aggregation ──────────────────────────────────────────────────────────

    @Override
    public ExpenseSummaryDto getSummaryByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return buildSummary(
                expenseRepository.sumAmountGroupedByCategoryForUser(userId),
                expenseRepository.countGroupedByCategoryForUser(userId),
                expenseRepository.sumAmountByUserId(userId),
                userId, null
        );
    }

    @Override
    public ExpenseSummaryDto getSummaryByTeamId(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", teamId);
        }
        return buildSummary(
                expenseRepository.sumAmountGroupedByCategoryForTeam(teamId),
                Collections.emptyList(),
                BigDecimal.ZERO,
                null, teamId
        );
    }

    // ─── Bulk Operations ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public int approveAllPendingByTeamId(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", teamId);
        }
        int count = expenseRepository.approveAllPendingByTeamId(teamId);
        log.info("Approved {} pending expenses for team: {}", count, teamId);
        return count;
    }

    @Override
    @Transactional
    public int bulkUpdateApprovalStatus(List<UUID> ids, Boolean isApproved) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("Expense IDs list cannot be empty");
        }
        if (ids.size() > MAX_BULK_SIZE) {
            throw new BusinessException("Bulk operation limit exceeded. Maximum allowed: " + MAX_BULK_SIZE);
        }
        int updated = expenseRepository.updateApprovalStatusByIds(ids, isApproved);
        log.info("Bulk updated {} expenses approval status to: {}", updated, isApproved);
        return updated;
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private Expense findExpenseOrThrow(UUID id) {
        return expenseRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Team resolveTeam(UUID teamId) {
        if (teamId == null) return null;
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
    }

    private ExpenseSummaryDto buildSummary(
            List<Object[]> amountByCategory,
            List<Object[]> countByCategory,
            BigDecimal totalAmount,
            UUID userId,
            UUID teamId) {

        Map<Category, BigDecimal> amountMap = amountByCategory.stream()
                .collect(Collectors.toMap(
                        row -> (Category) row[0],
                        row -> (BigDecimal) row[1]
                ));

        Map<Category, Long> countMap = countByCategory.stream()
                .collect(Collectors.toMap(
                        row -> (Category) row[0],
                        row -> (Long) row[1]
                ));

        long totalCount = countMap.values().stream().mapToLong(Long::longValue).sum();

        BigDecimal total = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        BigDecimal average = totalCount > 0
                ? total.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal min = amountMap.values().stream().min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        BigDecimal max = amountMap.values().stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);

        // Derive approved/pending totals from repository if userId is set
        BigDecimal approvedTotal = BigDecimal.ZERO;
        BigDecimal pendingTotal = BigDecimal.ZERO;
        if (userId != null) {
            approvedTotal = expenseRepository.sumAmountByUserId(userId);
            pendingTotal = total.subtract(approvedTotal);
        }

        return new ExpenseSummaryDto(
                totalCount, total, average, min, max,
                amountMap, countMap,
                approvedTotal, pendingTotal
        );
    }
}
