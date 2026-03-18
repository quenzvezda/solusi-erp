package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.Grid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Grid.
 */
@Repository
public interface GridRepository extends JpaRepository<Grid, Long> {

    @Query("SELECT g FROM Grid g " +
           "JOIN g.facility f " +
           "WHERE LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Grid> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT g FROM Grid g " +
           "JOIN g.facility f " +
           "WHERE f.id = :facilityId AND (" +
           "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Grid> searchByFacility(@Param("keyword") String keyword, @Param("facilityId") Long facilityId, Pageable pageable);

    Page<Grid> findByFacilityId(Long facilityId, Pageable pageable);

    boolean existsByFacilityIdAndCode(Long facilityId, String code);

    boolean existsByFacilityIdAndCodeAndIdNot(Long facilityId, String code, Long id);
}
