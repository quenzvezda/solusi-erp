package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.Facility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Facility.
 */
@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    @Query("SELECT f FROM Facility f " +
           "WHERE LOWER(f.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Facility> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
