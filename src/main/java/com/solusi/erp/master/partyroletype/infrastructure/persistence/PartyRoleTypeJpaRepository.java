package com.solusi.erp.master.partyroletype.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PartyRoleType.
 */
@Repository
public interface PartyRoleTypeJpaRepository extends JpaRepository<PartyRoleType, Long> {
    Optional<PartyRoleType> findByCode(String code);

    @Query("SELECT r FROM PartyRoleType r WHERE LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PartyRoleType> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}

