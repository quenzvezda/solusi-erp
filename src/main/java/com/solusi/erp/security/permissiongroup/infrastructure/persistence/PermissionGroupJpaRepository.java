package com.solusi.erp.security.permissiongroup.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionGroupJpaRepository extends JpaRepository<PermissionGroup, Long> {

    @Query("SELECT pg FROM PermissionGroup pg WHERE " +
           "(:keyword IS NULL OR LOWER(pg.nameId) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(pg.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(pg.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PermissionGroup> search(@Param("keyword") String keyword, Pageable pageable);

    Optional<PermissionGroup> findByCode(String code);

    @Query("SELECT DISTINCT pg FROM PermissionGroup pg JOIN pg.permissions p " +
           "WHERE p.name IN :authorities " +
           "AND (LOWER(pg.nameId) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(pg.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(pg.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PermissionGroup> searchAllowedMenus(@Param("keyword") String keyword, @Param("authorities") Collection<String> authorities, Pageable pageable);

    @Query("SELECT DISTINCT pg FROM PermissionGroup pg JOIN pg.permissions p WHERE p.name IN :authorities ORDER BY pg.sortOrder")
    List<PermissionGroup> findAllByAuthorities(@Param("authorities") Collection<String> authorities);
}
