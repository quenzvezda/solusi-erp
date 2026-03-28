package com.solusi.erp.master.repository;

import com.solusi.erp.master.model.Geographic;
import com.solusi.erp.master.model.GeographicType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeographicRepository extends JpaRepository<Geographic, Long> {

        @Query("SELECT g FROM Geographic g WHERE " +
                        "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<Geographic> search(@Param("keyword") String keyword, Pageable pageable);

        Page<Geographic> findByParentId(Long parentId, Pageable pageable);

        // -----------------------------------------------------------------------
        // Lookup queries for TomSelect autocomplete (returns Page for limit support)
        // -----------------------------------------------------------------------

        @Query("SELECT g FROM Geographic g WHERE g.type = :type AND g.isActive = true " +
                        "AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(g.code) LIKE LOWER(CONCAT('%', :q, '%')))")
        Page<Geographic> lookupByType(@Param("type") GeographicType type,
                        @Param("q") String q,
                        Pageable pageable);

        @Query("SELECT g FROM Geographic g LEFT JOIN FETCH g.parent p " +
                        "WHERE g.parent.id = :parentId AND g.isActive = true " +
                        "AND LOWER(g.name) LIKE LOWER(CONCAT('%', :q, '%'))")
        Page<Geographic> lookupProvincesByParent(@Param("parentId") Long parentId,
                        @Param("q") String q,
                        Pageable pageable);

        @Query("SELECT g FROM Geographic g LEFT JOIN FETCH g.parent p LEFT JOIN FETCH p.parent " +
                        "WHERE g.parent.id = :parentId AND g.isActive = true " +
                        "AND LOWER(g.name) LIKE LOWER(CONCAT('%', :q, '%'))")
        Page<Geographic> lookupCitiesByParent(@Param("parentId") Long parentId,
                        @Param("q") String q,
                        Pageable pageable);

        @Query("SELECT g FROM Geographic g LEFT JOIN FETCH g.parent p " +
                        "WHERE g.type = :type AND g.isActive = true " +
                        "AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(g.code) LIKE LOWER(CONCAT('%', :q, '%')))")
        Page<Geographic> lookupProvincesByType(@Param("type") GeographicType type,
                        @Param("q") String q,
                        Pageable pageable);

        @Query("SELECT g FROM Geographic g LEFT JOIN FETCH g.parent p LEFT JOIN FETCH p.parent " +
                        "WHERE g.type = :type AND g.isActive = true " +
                        "AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(g.code) LIKE LOWER(CONCAT('%', :q, '%')))")
        Page<Geographic> lookupCitiesByType(@Param("type") GeographicType type,
                        @Param("q") String q,
                        Pageable pageable);

        Optional<Geographic> findByIdAndIsActiveTrue(Long id);

        java.util.List<Geographic> findByTypeAndIsActiveTrue(GeographicType type);

        boolean existsByCode(String code);
}
