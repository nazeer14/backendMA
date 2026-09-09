package com.pack.repository;

import com.pack.dto.BudgetFilterDto;
import com.pack.entity.Budget;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class BudgetSpecification {

    private BudgetSpecification() {}

    public static Specification<Budget> withFilter(BudgetFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && Long.class != query.getResultType()) {
                root.fetch("user");
            }

            if (!Boolean.TRUE.equals(filter.includeDeleted())) {
                predicates.add(cb.isFalse(root.get("isDeleted")));
            }

            if (filter.userId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.userId()));
            }

            if (filter.category() != null) {
                predicates.add(cb.equal(root.get("category"), filter.category()));
            }

            if (filter.budgetMonth() != null) {
                predicates.add(cb.equal(root.get("budgetMonth"), filter.budgetMonth()));
            }

            if (filter.budgetYear() != null) {
                predicates.add(cb.equal(root.get("budgetYear"), filter.budgetYear()));
            }

            if (filter.autoRenew() != null) {
                predicates.add(cb.equal(root.get("autoRenew"), filter.autoRenew()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
