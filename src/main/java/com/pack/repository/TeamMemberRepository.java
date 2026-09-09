package com.pack.repository;

import com.pack.entity.TeamMember;
import com.pack.enums.MembershipStatus;
import com.pack.enums.TeamRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID>,
        JpaSpecificationExecutor<TeamMember>, TeamMemberRepositoryCustom {

    // ─── Join-fetch safe lookups ──────────────────────────────────────────────

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user JOIN FETCH tm.team " +
            "LEFT JOIN FETCH tm.invitedBy WHERE tm.id = :id")
    Optional<TeamMember> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user LEFT JOIN FETCH tm.invitedBy " +
            "WHERE tm.team.id = :teamId")
    Page<TeamMember> findByTeamIdWithDetails(@Param("teamId") UUID teamId, Pageable pageable);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.team WHERE tm.user.id = :userId")
    Page<TeamMember> findByUserIdWithTeam(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user " +
            "WHERE tm.team.id = :teamId AND tm.status = :status")
    List<TeamMember> findByTeamIdAndStatus(@Param("teamId") UUID teamId, @Param("status") MembershipStatus status);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user " +
            "WHERE tm.team.id = :teamId AND tm.role = :role AND tm.status = 'ACTIVE'")
    List<TeamMember> findActiveByTeamIdAndRole(@Param("teamId") UUID teamId, @Param("role") TeamRole role);

    // ─── Membership existence / lookup ────────────────────────────────────────

    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);

    @Query("SELECT CASE WHEN COUNT(tm) > 0 THEN true ELSE false END FROM TeamMember tm " +
            "WHERE tm.team.id = :teamId AND tm.user.id = :userId AND tm.status = 'ACTIVE'")
    boolean existsActiveByTeamIdAndUserId(@Param("teamId") UUID teamId, @Param("userId") UUID userId);

    @Query("SELECT tm.role FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.user.id = :userId AND tm.status = 'ACTIVE'")
    Optional<TeamRole> findActiveRoleByTeamIdAndUserId(@Param("teamId") UUID teamId, @Param("userId") UUID userId);

    // ─── Counts ───────────────────────────────────────────────────────────────

    long countByTeamIdAndStatus(UUID teamId, MembershipStatus status);

    long countByTeamIdAndRoleAndStatus(UUID teamId, TeamRole role, MembershipStatus status);

    long countByUserIdAndStatus(UUID userId, MembershipStatus status);

    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.status = 'ACTIVE'")
    long countActiveByTeamId(@Param("teamId") UUID teamId);

    /** Used by the last-owner guard before demoting/removing/suspending an OWNER. */
    @Query("SELECT COUNT(tm) FROM TeamMember tm " +
            "WHERE tm.team.id = :teamId AND tm.role = 'OWNER' AND tm.status = 'ACTIVE'")
    long countActiveOwners(@Param("teamId") UUID teamId);

    @Query("SELECT tm.role, COUNT(tm) FROM TeamMember tm " +
            "WHERE tm.team.id = :teamId AND tm.status = 'ACTIVE' GROUP BY tm.role")
    List<Object[]> countActiveGroupedByRole(@Param("teamId") UUID teamId);

    /** Same aggregation, exposed under the name TeamServiceImpl.getSummary() expects. */
    @Query("SELECT tm.role, COUNT(tm) FROM TeamMember tm " +
            "WHERE tm.team.id = :teamId AND tm.status = 'ACTIVE' GROUP BY tm.role")
    List<Object[]> countActiveGroupedByRoleForTeam(@Param("teamId") UUID teamId);

    // ─── Mutations ────────────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE TeamMember tm SET tm.role = :role WHERE tm.id = :id")
    int updateRole(@Param("id") UUID id, @Param("role") TeamRole role);

    @Modifying
    @Query("UPDATE TeamMember tm SET tm.status = :status, tm.statusReason = :reason, " +
            "tm.leftAt = CASE WHEN :status = 'REMOVED' THEN :now ELSE tm.leftAt END " +
            "WHERE tm.id = :id")
    int updateStatus(@Param("id") UUID id,
                     @Param("status") MembershipStatus status,
                     @Param("reason") String reason,
                     @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE TeamMember tm SET tm.status = 'ACTIVE', tm.joinedAt = :now WHERE tm.id = :id")
    int markAccepted(@Param("id") UUID id, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("DELETE FROM TeamMember tm WHERE tm.team.id = :teamId")
    void deleteAllByTeamId(@Param("teamId") UUID teamId);

    /**
     * Demotes the current owner's membership row to ADMIN and promotes the
     * target user's row to OWNER, in one round trip. Used exclusively by
     * TeamService.transferOwnership() — keeps the two-row swap atomic and
     * out of the Team module, since membership rows belong to this module.
     */
    @Modifying
    @Query("UPDATE TeamMember tm SET tm.role = CASE " +
            "  WHEN tm.user.id = :currentOwnerId THEN com.pack.enums.TeamRole.ADMIN " +
            "  WHEN tm.user.id = :newOwnerId THEN com.pack.enums.TeamRole.OWNER " +
            "  ELSE tm.role END " +
            "WHERE tm.team.id = :teamId AND tm.user.id IN (:currentOwnerId, :newOwnerId)")
    void swapOwnerRole(@Param("teamId") UUID teamId,
                       @Param("currentOwnerId") UUID currentOwnerId,
                       @Param("newOwnerId") UUID newOwnerId);

    // ─── Team ID list for a user (active memberships only) ────────────────────

    @Query("SELECT tm.team.id FROM TeamMember tm WHERE tm.user.id = :userId AND tm.status = 'ACTIVE'")
    List<UUID> findActiveTeamIdsByUserId(@Param("userId") UUID userId);
}