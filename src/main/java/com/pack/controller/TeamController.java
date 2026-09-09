package com.pack.controller;

import com.pack.dto.request.TeamRequestDto;
import com.pack.dto.response.*;
import com.pack.exceptions.ErrorResponse;
import com.pack.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team metadata, invite links, and ownership. Membership operations live under /api/v1/team-members.")
public class TeamController {

    private static final int MAX_PAGE_SIZE = 100;

    private final TeamService teamService;

    // ═══════════════════════════════════════════════════════════════════════════
    //  TEAM CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping
    @Operation(summary = "Create a new team",
            description = "Creates a team and automatically enrolls the owner as an ACTIVE OWNER membership.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Team created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Owner not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Team name already in use by this owner")
    })
    public ResponseEntity<ApiResponse<TeamResponseDto>> create(@Valid @RequestBody TeamRequestDto dto) {
        log.info("POST /api/v1/teams - owner: {}", dto.ownerId());
        TeamResponseDto created = teamService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Team created successfully"));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Get team by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<ApiResponse<TeamResponseDto>> getById(
            @Parameter(description = "Team UUID") @PathVariable UUID teamId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getById(teamId)));
    }

    @GetMapping
    @Operation(summary = "Get all teams (admin)")
    public ResponseEntity<ApiResponse<Page<TeamResponseDto>>> getAll(
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size,
            @RequestParam(defaultValue = "createdAt")               String sortBy,
            @RequestParam(defaultValue = "DESC")                    Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getAll(buildPageable(page, size, sortBy, direction))));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get teams by owner")
    public ResponseEntity<ApiResponse<Page<TeamResponseDto>>> getByOwner(
            @PathVariable UUID ownerId,
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.getByOwnerId(ownerId, buildPageable(page, size, "createdAt", Sort.Direction.DESC))));
    }

    @GetMapping("/search")
    @Operation(summary = "Search teams by name keyword")
    public ResponseEntity<ApiResponse<Page<TeamResponseDto>>> search(
            @RequestParam @NotBlank String keyword,
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.searchByName(keyword, buildPageable(page, size, "teamName", Sort.Direction.ASC))));
    }

    @PutMapping("/{teamId}")
    @Operation(summary = "Update team metadata (full update)",
            description = "Updates teamName and active flag. iconUrl is not writable here — use the Image upload API.")
    public ResponseEntity<ApiResponse<TeamResponseDto>> update(
            @PathVariable UUID teamId, @Valid @RequestBody TeamRequestDto dto) {
        log.info("PUT /api/v1/teams/{}", teamId);
        return ResponseEntity.ok(ApiResponse.success(teamService.update(teamId, dto), "Team updated successfully"));
    }

    @PatchMapping("/{teamId}/active")
    @Operation(summary = "Activate or deactivate a team")
    public ResponseEntity<ApiResponse<TeamResponseDto>> toggleActive(
            @PathVariable UUID teamId, @RequestParam Boolean active) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.toggleActive(teamId, active), active ? "Team activated" : "Team deactivated"));
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "Delete a team and all its memberships")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Team deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID teamId) {
        log.warn("DELETE /api/v1/teams/{}", teamId);
        teamService.delete(teamId);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  INVITE TOKEN
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/{teamId}/invite/generate")
    @Operation(summary = "Generate (or regenerate) an invite token")
    public ResponseEntity<ApiResponse<TeamInviteResponseDto>> generateInvite(@PathVariable UUID teamId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.generateInviteToken(teamId), "Invite token generated"));
    }

    @DeleteMapping("/{teamId}/invite/revoke")
    @Operation(summary = "Revoke the current invite token")
    public ResponseEntity<ApiResponse<Void>> revokeInvite(@PathVariable UUID teamId) {
        teamService.revokeInviteToken(teamId);
        return ResponseEntity.ok(ApiResponse.success(null, "Invite token revoked"));
    }

    @GetMapping("/join/preview")
    @Operation(summary = "Preview team info from invite token (before joining)")
    public ResponseEntity<ApiResponse<TeamResponseDto>> previewInvite(@RequestParam @NotBlank String token) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getByInviteToken(token)));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  OWNERSHIP
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/{teamId}/transfer-ownership")
    @Operation(summary = "Transfer team ownership",
            description = "New owner must already be an ACTIVE member. Demotes current owner's membership role to ADMIN.")
    public ResponseEntity<ApiResponse<TeamResponseDto>> transferOwnership(
            @PathVariable UUID teamId, @RequestParam UUID newOwnerId) {
        return ResponseEntity.ok(ApiResponse.success(
                teamService.transferOwnership(teamId, newOwnerId), "Ownership transferred successfully"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ANALYTICS
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/{teamId}/summary")
    @Operation(summary = "Get aggregated team statistics (active member count by role)")
    public ResponseEntity<ApiResponse<TeamSummaryDto>> getSummary(@PathVariable UUID teamId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getSummary(teamId)));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sortBy, Sort.Direction direction) {
        return PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), Sort.by(direction, sortBy));
    }
}