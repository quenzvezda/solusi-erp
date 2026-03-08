package com.solusi.erp.master.repository;

import com.solusi.erp.master.model.Tax;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaxRepository extends JpaRepository<Tax, Long> {
    @Query("SELECT t FROM Tax t WHERE " +
            "(LOWER(t.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Tax> search(@Param("keyword") String keyword, Pageable pageable);

    Page<Tax> findByIsActiveTrue(Pageable pageable);

    Optional<Tax> findByCode(String code);
}
