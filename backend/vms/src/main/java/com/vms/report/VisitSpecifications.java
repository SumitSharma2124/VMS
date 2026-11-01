package com.vms.report;

import com.vms.visit.Visit;
import com.vms.visit.VisitStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Locale;

public final class VisitSpecifications {
    private VisitSpecifications() {
    }

    public static Specification<Visit> forOrg(Long orgId) {
        return (root, query, builder) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("visitor", jakarta.persistence.criteria.JoinType.LEFT);
                query.distinct(true);
            }
            return builder.equal(root.get("org").get("id"), orgId);
        };
    }

    public static Specification<Visit> nameLike(String value) {
        if (value == null || value.isBlank()) return null;
        String pattern = "%" + escape(value.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, builder) -> builder.like(
                builder.lower(root.get("visitor").get("fullName")), pattern, '\\');
    }

    public static Specification<Visit> hostLike(String value) {
        if (value == null || value.isBlank()) return null;
        String pattern = "%" + escape(value.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, builder) -> builder.like(
                builder.lower(root.get("hostName")), pattern, '\\');
    }

    public static Specification<Visit> status(VisitStatus value) {
        return value == null ? null : (root, query, builder) -> builder.equal(root.get("status"), value);
    }

    public static Specification<Visit> requestedBetween(Instant from, Instant to) {
        if (from == null && to == null) return null;
        return (root, query, builder) -> {
            if (from == null) return builder.lessThan(root.get("requestedAt"), to);
            if (to == null) return builder.greaterThanOrEqualTo(root.get("requestedAt"), from);
            return builder.and(
                    builder.greaterThanOrEqualTo(root.get("requestedAt"), from),
                    builder.lessThan(root.get("requestedAt"), to));
        };
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
