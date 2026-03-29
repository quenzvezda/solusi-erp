package com.solusi.erp.security.permission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionJpaRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}
