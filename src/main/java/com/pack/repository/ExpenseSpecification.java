package com.pack.repository;

import com.pack.dto.request.ExpenseFilterDto;
import com.pack.entity.Expense;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ExpenseSpecification {

    private ExpenseSpecification() {}

    public static Specification<Expense> withFilter(ExpenseFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Fetch associations to avoid N+1 on listing
            if (query != null && Long.class != query.getResultType()) {
                root.fetch("user");
            }

            if (filter.userId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.userId()));
            }

            if (filter.teamId() != null) {
                predicates.add(cb.equal(root.get("team").get("id"), filter.teamId()));
            }

            if (filter.category() != null) {
                predicates.add(cb.equal(root.get("category"), filter.category()));
            }

            if (filter.paymentMethod() != null) {
                predicates.add(cb.equal(root.get("paymentMethod"), filter.paymentMethod()));
            }

            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), filter.dateFrom()));
            }

            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), filter.dateTo()));
            }

            if (filter.minAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.minAmount()));
            }

            if (filter.maxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.maxAmount()));
            }

            if (filter.isApproved() != null) {
                predicates.add(cb.equal(root.get("isApproved"), filter.isApproved()));
            }

            if (StringUtils.hasText(filter.titleKeyword())) {
                predicates.add(cb.like(cb.lower(root.get("title")),
                        "%" + filter.titleKeyword().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}