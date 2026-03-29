package com.solusi.erp.security.role.domain.repository;

import com.solusi.erp.security.role.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Role save(Role role);

    Optional<Role> findById(Long id);

    Optional<Role> findByName(String name);

    List<Role> findAll();

    void delete(Role role);

    boolean existsByName(String name);
}

