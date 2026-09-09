package com.pack.controller;

import com.pack.dto.request.ExpenseFilterDto;
import com.pack.dto.request.ExpenseRequestDto;
import com.pack.dto.response.ApiResponse;
import com.pack.dto.response.ExpenseResponseDto;
import com.pack.dto.response.ExpenseSummaryDto;
import com.pack.exceptions.ErrorResponse;
import com.pack.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management APIs")
public class ExpenseController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ExpenseService expenseService;

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new expense", description = "Creates a new expense record for a user or team")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Expense created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or team not found")
    })
    public ResponseEntity<ApiResponse<ExpenseResponseDto>> create(
            @Valid @RequestBody ExpenseRequestDto requestDto) {
        log.info("POST /api/v1/expenses - Creating expense for user: {}", requestDto.userId());
        ExpenseResponseDto created = expenseService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Expense created successfully"));
    }

    // ─── READ (single) ────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<ApiResponse<ExpenseResponseDto>> getById(
            @Parameter(description = "Expense UUID", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getById(id)));
    }

    // ─── READ (paginated by user) ─────────────────────────────────────────────

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get paginated expenses for a user")
    public ResponseEntity<ApiResponse<Page<ExpenseResponseDto>>> getByUserId(
            @Parameter(description = "User UUID") @PathVariable UUID userId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "expenseDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(expenseService.getByUserId(userId, pageable)));
    }

    // ─── READ (paginated by team) ─────────────────────────────────────────────

    @GetMapping("/teams/{teamId}")
    @Operation(summary = "Get paginated expenses for a team")
    public ResponseEntity<ApiResponse<Page<ExpenseResponseDto>>> getByTeamId(
            @Parameter(description = "Team UUID") @PathVariable UUID teamId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(expenseService.getByTeamId(teamId, pageable)));
    }

    // ─── SEARCH ───────────────────────────────────────────────────────────────

    @GetMapping("/search")
    @Operation(
            summary = "Search expenses with filters",
            description = "Dynamic filter search supporting category, payment method, date range, amount range, approval status, and keyword"
    )
    public ResponseEntity<ApiResponse<Page<ExpenseResponseDto>>> search(
            @ParameterObject @Valid ExpenseFilterDto filter,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(expenseService.search(filter, pageable)));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing expense (full update)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<ApiResponse<ExpenseResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseRequestDto requestDto) {
        log.info("PUT /api/v1/expenses/{} - Updating expense", id);
        return ResponseEntity.ok(ApiResponse.success(expenseService.update(id, requestDto), "Expense updated successfully"));
    }

    // ─── PATCH approval status ─────────────────────────────────────────────────

    @PatchMapping("/{id}/approval")
    @Operation(summary = "Update approval status of an expense")
    public ResponseEntity<ApiResponse<ExpenseResponseDto>> updateApprovalStatus(
            @PathVariable UUID id,
            @RequestParam Boolean isApproved) {
        return ResponseEntity.ok(ApiResponse.success(
                expenseService.updateApprovalStatus(id, isApproved),
                "Approval status updated"));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Expense deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("DELETE /api/v1/expenses/{}", id);
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── SUMMARY ──────────────────────────────────────────────────────────────

    @GetMapping("/users/{userId}/summary")
    @Operation(summary = "Get aggregated expense summary for a user")
    public ResponseEntity<ApiResponse<ExpenseSummaryDto>> getUserSummary(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getSummaryByUserId(userId)));
    }

    @GetMapping("/teams/{teamId}/summary")
    @Operation(summary = "Get aggregated expense summary for a team")
    public ResponseEntity<ApiResponse<ExpenseSummaryDto>> getTeamSummary(
            @PathVariable UUID teamId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getSummaryByTeamId(teamId)));
    }

    // ─── BULK OPERATIONS ──────────────────────────────────────────────────────

    @PostMapping("/teams/{teamId}/approve-all")
    @Operation(summary = "Approve all pending expenses for a team")
    public ResponseEntity<ApiResponse<Integer>> approveAllPending(
            @PathVariable UUID teamId) {
        int count = expenseService.approveAllPendingByTeamId(teamId);
        return ResponseEntity.ok(ApiResponse.success(count,
                String.format("Approved %d pending expense(s)", count)));
    }

    @PatchMapping("/bulk/approval")
    @Operation(
            summary = "Bulk update approval status",
            description = "Update approval status for multiple expenses in a single request (max 500)"
    )
    public ResponseEntity<ApiResponse<Integer>> bulkUpdateApproval(
            @RequestParam List<UUID> ids,
            @RequestParam Boolean isApproved) {
        int updated = expenseService.bulkUpdateApprovalStatus(ids, isApproved);
        return ResponseEntity.ok(ApiResponse.success(updated,
                String.format("Updated %d expense(s)", updated)));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sortBy, Sort.Direction direction) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(page, safeSize, Sort.by(direction, sortBy));
    }
}