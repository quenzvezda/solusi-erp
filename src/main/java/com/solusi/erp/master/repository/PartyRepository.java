package com.solusi.erp.master.repository;

import com.solusi.erp.master.model.Party;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Party.
 */
@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {

    @Query("SELECT p FROM Party p " +
           "LEFT JOIN p.roles r " +
           "WHERE LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Party> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
