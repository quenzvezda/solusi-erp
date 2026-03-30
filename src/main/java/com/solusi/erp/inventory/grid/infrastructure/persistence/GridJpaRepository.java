package com.solusi.erp.inventory.grid.infrastructure.persistence;

import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GridJpaRepository extends JpaRepository<GridEntity, Long> {

    @Query("SELECT g FROM GridEntity g " +
           "WHERE LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT 1 FROM FacilityEntity f WHERE f.id = g.facilityId " +
           "AND LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<GridEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT g FROM GridEntity g " +
           "WHERE g.facilityId = :facilityId AND (" +
           "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<GridEntity> searchByFacility(@Param("keyword") String keyword,
                                      @Param("facilityId") Long facilityId,
                                      Pageable pageable);

    Page<GridEntity> findByFacilityId(Long facilityId, Pageable pageable);

    boolean existsByFacilityIdAndCode(Long facilityId, String code);

    boolean existsByFacilityIdAndCodeAndIdNot(Long facilityId, String code, Long id);
}
