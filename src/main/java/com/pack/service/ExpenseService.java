package com.pack.service;

import com.pack.dto.request.ExpenseFilterDto;
import com.pack.dto.request.ExpenseRequestDto;
import com.pack.dto.response.ExpenseResponseDto;
import com.pack.dto.response.ExpenseSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ExpenseService {

    /** Create a new expense */
    ExpenseResponseDto create(ExpenseRequestDto requestDto);

    /** Get a single expense by ID */
    ExpenseResponseDto getById(UUID id);

    /** Update an expense (full update) */
    ExpenseResponseDto update(UUID id, ExpenseRequestDto requestDto);

    /** Partially update approval status */
    ExpenseResponseDto updateApprovalStatus(UUID id, Boolean isApproved);

    /** Delete an expense by ID */
    void delete(UUID id);

    /** Search/filter expenses with pagination */
    Page<ExpenseResponseDto> search(ExpenseFilterDto filter, Pageable pageable);

    /** Get all expenses for a specific user */
    Page<ExpenseResponseDto> getByUserId(UUID userId, Pageable pageable);

    /** Get all expenses for a specific team */
    Page<ExpenseResponseDto> getByTeamId(UUID teamId, Pageable pageable);

    /** Get aggregated summary for a user */
    ExpenseSummaryDto getSummaryByUserId(UUID userId);

    /** Get aggregated summary for a team */
    ExpenseSummaryDto getSummaryByTeamId(UUID teamId);

    /** Bulk approve all pending expenses in a team */
    int approveAllPendingByTeamId(UUID teamId);

    /** Bulk update approval status for a list of expense IDs */
    int bulkUpdateApprovalStatus(List<UUID> ids, Boolean isApproved);
}