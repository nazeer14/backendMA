package com.pack.repository;

import com.pack.entity.Team;
import com.pack.entity.TeamMember;
import com.pack.entity.User;
import com.pack.enums.MembershipStatus;
import com.pack.enums.TeamRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public class TeamMemberRepositoryImpl implements TeamMemberRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void createOwnerMembership(UUID teamId, UUID ownerId) {
        Team teamRef = entityManager.getReference(Team.class, teamId);
        User ownerRef = entityManager.getReference(User.class, ownerId);

        OffsetDateTime now = OffsetDateTime.now();
        TeamMember ownerMembership = TeamMember.builder()
                .team(teamRef)
                .user(ownerRef)
                .role(TeamRole.OWNER)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(now)
                .build();

        entityManager.persist(ownerMembership);
    }
}