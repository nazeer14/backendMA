package com.pack.repository;

import java.util.UUID;

/**
 * Custom insert-style operations that Spring Data derived/JPQL queries can't
 * express directly (JPQL has no INSERT). Implemented by TeamMemberRepositoryImpl
 * and merged into TeamMemberRepository via Spring Data's Impl-suffix convention.
 */
public interface TeamMemberRepositoryCustom {

    /**
     * Creates the initial OWNER membership row for a freshly created team.
     * Called once, exclusively from TeamService.create(), immediately after
     * the Team row is persisted.
     */
    void createOwnerMembership(UUID teamId, UUID ownerId);
}