package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.model.UnitOfMeasure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for UnitOfMeasure.
 */
@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {

    @Query("SELECT u FROM UnitOfMeasure u WHERE " +
           "LOWER(u.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UnitOfMeasure> search(@Param("keyword") String keyword, Pageable pageable);

    List<UnitOfMeasure> findByType(UomType type);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
