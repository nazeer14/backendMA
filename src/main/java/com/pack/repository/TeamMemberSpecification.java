package com.pack.repository;

import com.pack.entity.TeamMember;
import com.pack.enums.MembershipStatus;
import com.pack.enums.TeamRole;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TeamMemberSpecification {

    private TeamMemberSpecification() {}

    public static Specification<TeamMember> withFilters(
            UUID teamId, UUID userId, TeamRole role, MembershipStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && Long.class != query.getResultType()) {
                root.fetch("user");
            }

            if (teamId != null) {
                predicates.add(cb.equal(root.get("team").get("id"), teamId));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}