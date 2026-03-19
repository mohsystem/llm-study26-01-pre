package com.um.springbootprojstructure.user.repository;

import com.um.springbootprojstructure.user.domain.User;
import com.um.springbootprojstructure.user.domain.UserRole;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {
    private UserSpecifications() {}

    public static Specification<User> hasRole(UserRole role) {
        if (role == null) return null;
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> isActive(Boolean active) {
        if (active == null) return null;
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }
}

