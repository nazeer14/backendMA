package com.pack.service;

import com.pack.dto.BudgetFilterDto;
import com.pack.dto.BudgetSummaryDto;
import com.pack.dto.request.BudgetRequestDto;
import com.pack.dto.response.BudgetResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BudgetService {

    /** Create a new budget for a user/category/period. Fails if one already exists (active). */
    BudgetResponseDto create(BudgetRequestDto dto);

    /** Get a single budget by ID, enriched with live spend data. */
    BudgetResponseDto getById(UUID id);

    /** Full update of a budget. Category/month/year are immutable after creation. */
    BudgetResponseDto update(UUID id, BudgetRequestDto dto);

    /** Soft-delete a budget (kept for historical reporting). */
    void delete(UUID id);

    /** Permanently remove a budget record. */
    void hardDelete(UUID id);

    /** Restore a previously soft-deleted budget. */
    BudgetResponseDto restore(UUID id);

    /** Dynamic filtered + paginated search. */
    Page<BudgetResponseDto> search(BudgetFilterDto filter, Pageable pageable);

    /** Get all budgets for a user (paginated). */
    Page<BudgetResponseDto> getByUserId(UUID userId, Pageable pageable);

    /** Get all budgets for a user in a specific month/year, enriched with spend. */
    List<BudgetResponseDto> getByUserIdAndPeriod(UUID userId, Integer month, Integer year);

    /** Aggregated summary across all categories for a user/period. */
    BudgetSummaryDto getSummary(UUID userId, Integer month, Integer year);

    /**
     * Rolls forward all auto-renewing budgets from the given period into the next
     * month, skipping any that already exist for the destination period.
     * Intended to be invoked by a scheduled job.
     */
    int rolloverAutoRenewBudgets(Integer fromMonth, Integer fromYear);
}
