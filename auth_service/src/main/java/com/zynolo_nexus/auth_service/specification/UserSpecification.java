package com.zynolo_nexus.auth_service.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.zynolo_nexus.auth_service.dto.search.UserSearchCriteria;
import com.zynolo_nexus.auth_service.model.User;

import jakarta.persistence.criteria.Predicate;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> fromCriteria(UserSearchCriteria criteria) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria == null) {
                return builder.conjunction();
            }
            if (StringUtils.hasText(criteria.getUsername())) {
                predicates.add(builder.like(
                        builder.lower(root.get("email")),
                        "%" + criteria.getUsername().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(criteria.getEmail())) {
                predicates.add(builder.like(
                        builder.lower(root.get("email")),
                        "%" + criteria.getEmail().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(criteria.getStatus())) {
                predicates.add(builder.equal(
                        builder.lower(root.get("status")),
                        criteria.getStatus().toLowerCase()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
