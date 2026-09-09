package com.pack.controller;

import com.pack.dto.request.MembershipStatusChangeDto;
import com.pack.dto.request.RoleChangeRequestDto;
import com.pack.dto.request.TeamMemberRequestDto;
import com.pack.dto.response.ApiResponse;
import com.pack.dto.response.MembershipSummaryDto;
import com.pack.dto.response.TeamMemberResponseDto;
import com.pack.enums.MembershipStatus;
import com.pack.enums.TeamRole;
import com.pack.exceptions.ErrorResponse;
import com.pack.service.TeamMemberService;
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
@RequestMapping("/api/v1/team-members")
@RequiredArgsConstructor
@Tag(name = "Team Members", description = "Standalone membership lifecycle: add, invite, accept, role/status changes, removal. OWNER role is never settable here — see /api/v1/teams/{id}/transfer-ownership.")
public class TeamMemberController {

    private static final int MAX_PAGE_SIZE = 100;

    private final TeamMemberService memberService;

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Add a member directly", description = "Adds a user to a team with ACTIVE status immediately.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Member added"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team or user not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Membership already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "OWNER role requested directly")
    })
    public ResponseEntity<ApiResponse<TeamMemberResponseDto>> addMember(@Valid @RequestBody TeamMemberRequestDto dto) {
        log.info("POST /api/v1/team-members - team: {}, user: {}", dto.teamId(), dto.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(memberService.addMember(dto, false), "Member added"));
    }

    @PostMapping("/invite")
    @Operation(summary = "Invite a member", description = "Creates a PENDING membership requiring acceptance via /accept.")
    public ResponseEntity<ApiResponse<TeamMemberResponseDto>> inviteMember(@Valid @RequestBody TeamMemberRequestDto dto) {
        log.info("POST /api/v1/team-members/invite - team: {}, user: {}", dto.teamId(), dto.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(memberService.addMember(dto, true), "Invitation sent"));
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept a pending invite")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invite accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Membership is not in PENDING state")
    })
    public ResponseEntity<ApiResponse<TeamMemberResponseDto>> acceptInvite(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.acceptInvite(id), "Invite accepted"));
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get a membership by its own ID")
    public ResponseEntity<ApiResponse<TeamMemberResponseDto>> getById(
            @Parameter(description = "Membership UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getById(id)));
    }

    @GetMapping("/team/{teamId}/user/{userId}")
    @Operation(summary = "Get the membership record for a specific user in a specific team")
    public ResponseEntity<ApiResponse<TeamMemberResponseDto>> getByTeamAndUser(
            @PathVariable UUID teamId, @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getByTeamAndUser(teamId, userId)));
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "List members of a team (paginated)")
    public ResponseEntity<ApiResponse<Page<TeamMemberResponseDto>>> getByTeam(
            @PathVariable UUID teamId,
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size,
            @RequestParam(defaultValue = "createdAt")               String sortBy,
            @RequestParam(defaultValue = "ASC")                     Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(
                memberService.getByTeam(teamId, buildPageable(page, size, sortBy, direction))));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List all teams a user belongs to (paginated)")
    public ResponseEntity<ApiResponse<Page<TeamMemberResponseDto>>> getByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size) {
        return ResponseEntity.ok(ApiResponse.success(
                memberService.getByUser(userId, buildPageable(page, size, "createdAt", Sort.Direction.DESC))));
    }

    @GetMapping("/team/{teamId}/role/{role}")
    @Operation(summary = "List all ACTIVE members of a team with a specific role")
    public ResponseEntity<ApiResponse<List<TeamMemberResponseDto>>> getActiveByRole(
            @PathVariable UUID teamId, @PathVariable TeamRole role) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getActiveByRole(teamId, role)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search memberships by team, user, role, and/or status")
    public ResponseEntity<ApiResponse<Page<TeamMemberResponseDto>>> search(
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) TeamRole role,
            @RequestParam(required = false) MembershipStatus status,
            @RequestParam(defaultValue = "0")  @Min(0)              int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100)    int size) {
        return ResponseEntity.ok(ApiResponse.success(
                memberService.search(teamId, userId, role, status,
                        buildPageable(page, size, "createdAt", Sort.Direction.DESC))));
    }

    // ─── UPDATE: role & status ──────────────────────────────────────────────────

    @PatchMapping("/{id}/role")
    @Operation(summary = "Change a member's role",
            description = "OWNER cannot be set here. Blocked with 422 if it would demote the team's last active OWNER.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Would leave the team without an owner, or OWNER requested directly")
    })
    public ResponseEntity<ApiResponse<TeamMemberResponseDto>> changeRole(
            @PathVariable UUID id, @Valid @RequestBody RoleChangeRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(memberService.changeRole(id, dto), "Role updated"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change membership lifecycle status",
            description = "Suspend, reactivate, or mark removed. Blocked with 422 if removing/suspending the last active OWNER.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Would leave the team without an owner")
    })
    public ResponseEntity<ApiResponse<TeamMemberResponseDto>> changeStatus(
            @PathVariable UUID id, @Valid @RequestBody MembershipStatusChangeDto dto) {
        return ResponseEntity.ok(ApiResponse.success(memberService.changeStatus(id, dto), "Status updated"));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-remove a member",
            description = "Sets status to REMOVED and stamps leftAt. Row is retained for audit history.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Member removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Would leave the team without an owner")
    })
    public ResponseEntity<Void> removeMember(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        memberService.removeMember(id, reason);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    @Operation(summary = "Permanently delete a membership row", description = "Irreversible. Prefer the soft DELETE endpoint.")
    public ResponseEntity<Void> hardDelete(@PathVariable UUID id) {
        log.warn("DELETE /api/v1/team-members/{}/hard", id);
        memberService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── ANALYTICS ────────────────────────────────────────────────────────────

    @GetMapping("/team/{teamId}/summary")
    @Operation(summary = "Get membership counts for a team (active/pending/suspended, by role)")
    public ResponseEntity<ApiResponse<MembershipSummaryDto>> getSummary(@PathVariable UUID teamId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getSummary(teamId)));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sortBy, Sort.Direction direction) {
        return PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), Sort.by(direction, sortBy));
    }
}