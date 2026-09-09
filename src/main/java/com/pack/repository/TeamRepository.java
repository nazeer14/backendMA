package com.pack.repository;

import com.pack.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID>, JpaSpecificationExecutor<Team> {

    @Query("SELECT t FROM Team t JOIN FETCH t.owner WHERE t.id = :id")
    Optional<Team> findByIdWithOwner(@Param("id") UUID id);

    @Query("SELECT t FROM Team t JOIN FETCH t.owner WHERE t.inviteToken = :token")
    Optional<Team> findByInviteTokenWithOwner(@Param("token") String token);

    @Query("SELECT t FROM Team t JOIN FETCH t.owner WHERE t.owner.id = :ownerId")
    List<Team> findAllByOwnerIdWithOwner(@Param("ownerId") UUID ownerId);

    Page<Team> findByOwnerId(UUID ownerId, Pageable pageable);

    Page<Team> findByOwnerIdAndActive(UUID ownerId, Boolean active, Pageable pageable);

    Page<Team> findByActive(Boolean active, Pageable pageable);

    boolean existsByInviteToken(String inviteToken);

    @Modifying
    @Query("UPDATE Team t SET t.inviteToken = :token WHERE t.id = :teamId")
    void updateInviteToken(@Param("teamId") UUID teamId, @Param("token") String token);

    @Modifying
    @Query("UPDATE Team t SET t.inviteToken = NULL WHERE t.id = :teamId")
    void revokeInviteToken(@Param("teamId") UUID teamId);

    @Modifying
    @Query("UPDATE Team t SET t.active = :active WHERE t.id = :teamId")
    void updateActiveStatus(@Param("teamId") UUID teamId, @Param("active") Boolean active);

    /** Used by the Image API after upload — keeps Team's icon write path centralized in one place. */
    @Modifying
    @Query("UPDATE Team t SET t.iconUrl = :iconUrl WHERE t.id = :teamId")
    void updateIconUrl(@Param("teamId") UUID teamId, @Param("iconUrl") String iconUrl);

    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);

    boolean existsByTeamNameIgnoreCaseAndOwnerId(String teamName, UUID ownerId);

    long countByOwnerId(UUID ownerId);

    long countByActive(Boolean active);

    @Query("SELECT t FROM Team t JOIN FETCH t.owner WHERE LOWER(t.teamName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Team> searchByTeamName(@Param("keyword") String keyword, Pageable pageable);
}