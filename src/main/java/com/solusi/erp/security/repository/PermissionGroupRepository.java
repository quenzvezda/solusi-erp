package com.solusi.erp.security.repository;

import com.solusi.erp.security.model.PermissionGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Long> {

    @Query("SELECT DISTINCT pg FROM PermissionGroup pg JOIN pg.permissions p " +
           "WHERE p.name IN :authorities " +
           "AND (LOWER(pg.nameId) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(pg.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(pg.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PermissionGroup> searchAllowedMenus(@Param("keyword") String keyword, @Param("authorities") Collection<String> authorities, Pageable pageable);

    @Query("SELECT pg FROM PermissionGroup pg WHERE " +
           "(:keyword IS NULL OR LOWER(pg.nameId) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(pg.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(pg.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PermissionGroup> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT pg FROM PermissionGroup pg JOIN pg.permissions p WHERE p.name IN :authorities")
    List<PermissionGroup> findAllByAuthorities(@Param("authorities") Collection<String> authorities);
}
