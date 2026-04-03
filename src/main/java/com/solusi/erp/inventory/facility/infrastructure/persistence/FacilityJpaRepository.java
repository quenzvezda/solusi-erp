package com.solusi.erp.inventory.facility.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityJpaRepository extends JpaRepository<FacilityEntity, Long> {

    @Query("SELECT f FROM FacilityEntity f " +
           "WHERE LOWER(f.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<FacilityEntity> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
