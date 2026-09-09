package com.pack.controller;

import com.pack.dto.BudgetFilterDto;
import com.pack.dto.BudgetSummaryDto;
import com.pack.dto.request.BudgetRequestDto;
import com.pack.dto.response.ApiResponse;
import com.pack.dto.response.BudgetResponseDto;
import com.pack.exceptions.ErrorResponse;
import com.pack.service.BudgetService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Monthly category budget management and spend tracking")
public class BudgetController {

    private static final int MAX_PAGE_SIZE = 100;

    private final BudgetService budgetService;

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new budget",
               description = "Creates a monthly spending limit for a user/category. Fails with 409 if one already exists for the same period.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Budget created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Budget already exists for this period")
    })
    public ResponseEntity<ApiResponse<BudgetResponseDto>> create(
            @Valid @RequestBody BudgetRequestDto requestDto) {
        log.info("POST /api/v1/budgets - user: {}, category: {}", requestDto.userId(), requestDto.category());
        BudgetResponseDto created = budgetService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Budget created successfully"));
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID", description = "Returns the budget enriched with live spend, utilization, and alert status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Budget found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Budget not found")
    })
    public ResponseEntity<ApiResponse<BudgetResponseDto>> getById(
            @Parameter(description = "Budget UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getById(id)));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get paginated budgets for a user")
    public ResponseEntity<ApiResponse<Page<BudgetResponseDto>>> getByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size,
            @RequestParam(defaultValue = "budgetYear")              String sortBy,
            @RequestParam(defaultValue = "DESC")                    Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(
                budgetService.getByUserId(userId, buildPageable(page, size, sortBy, direction))));
    }

    @GetMapping("/users/{userId}/period")
    @Operation(summary = "Get all budgets for a user in a specific month/year",
               description = "Returns every category budget for the given period, each enriched with spend data.")
    public ResponseEntity<ApiResponse<List<BudgetResponseDto>>> getByUserAndPeriod(
            @PathVariable UUID userId,
            @Parameter(description = "Month (1-12)") @RequestParam @Min(1) @Max(12) Integer month,
            @Parameter(description = "Year") @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.success(
                budgetService.getByUserIdAndPeriod(userId, month, year)));
    }

    @GetMapping("/users/{userId}/period/current")
    @Operation(summary = "Get all budgets for a user in the current month",
               description = "Convenience endpoint — equivalent to /period with today's month and year.")
    public ResponseEntity<ApiResponse<List<BudgetResponseDto>>> getCurrentPeriod(
            @PathVariable UUID userId) {
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                budgetService.getByUserIdAndPeriod(userId, now.getMonthValue(), now.getYear())));
    }

    // ─── SEARCH ───────────────────────────────────────────────────────────────

    @GetMapping("/search")
    @Operation(summary = "Search budgets with filters",
               description = "Dynamic filter search supporting userId, category, month, year, auto-renew, and soft-deleted inclusion")
    public ResponseEntity<ApiResponse<Page<BudgetResponseDto>>> search(
            @ParameterObject @Valid BudgetFilterDto filter,
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size,
            @RequestParam(defaultValue = "budgetYear")              String sortBy,
            @RequestParam(defaultValue = "DESC")                    Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(
                budgetService.search(filter, buildPageable(page, size, sortBy, direction))));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget",
               description = "Updates limit, alert threshold, notes, auto-renew, and notification settings. Category/month/year are immutable.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Budget updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Budget not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Concurrent modification detected")
    })
    public ResponseEntity<ApiResponse<BudgetResponseDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequestDto requestDto) {
        log.info("PUT /api/v1/budgets/{}", id);
        return ResponseEntity.ok(ApiResponse.success(
                budgetService.update(id, requestDto), "Budget updated successfully"));
    }

    // ─── DELETE / RESTORE ───────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a budget", description = "Marks the budget as deleted while preserving it for historical reporting.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Budget soft-deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Budget not found")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("DELETE /api/v1/budgets/{}", id);
        budgetService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    @Operation(summary = "Permanently delete a budget", description = "Irreversibly removes the budget record. Use with caution.")
    public ResponseEntity<Void> hardDelete(@PathVariable UUID id) {
        log.warn("DELETE /api/v1/budgets/{}/hard", id);
        budgetService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore a soft-deleted budget")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Budget restored"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "An active budget already occupies this slot")
    })
    public ResponseEntity<ApiResponse<BudgetResponseDto>> restore(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.restore(id), "Budget restored"));
    }

    // ─── SUMMARY ──────────────────────────────────────────────────────────────

    @GetMapping("/users/{userId}/summary")
    @Operation(summary = "Get aggregated budget summary for a user/period",
               description = "Combines all category budgets for the period into a single overview with totals and alert counts.")
    public ResponseEntity<ApiResponse<BudgetSummaryDto>> getSummary(
            @PathVariable UUID userId,
            @RequestParam @Min(1) @Max(12) Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getSummary(userId, month, year)));
    }

    // ─── ROLLOVER (admin / scheduled-job trigger) ──────────────────────────────

    @PostMapping("/rollover")
    @Operation(summary = "Roll over auto-renewing budgets to the next month",
               description = "Creates next-month budgets for every active budget flagged autoRenew=true, skipping periods that already have one. Intended for a scheduled job, exposed here for manual/admin trigger.")
    public ResponseEntity<ApiResponse<Integer>> rollover(
            @RequestParam @Min(1) @Max(12) Integer fromMonth,
            @RequestParam Integer fromYear) {
        int created = budgetService.rolloverAutoRenewBudgets(fromMonth, fromYear);
        return ResponseEntity.ok(ApiResponse.success(created,
                String.format("Rollover complete — %d budget(s) created", created)));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sortBy, Sort.Direction direction) {
        return PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), Sort.by(direction, sortBy));
    }
}
