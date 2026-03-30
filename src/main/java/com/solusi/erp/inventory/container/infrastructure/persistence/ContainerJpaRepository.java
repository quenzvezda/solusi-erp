package com.solusi.erp.inventory.container.infrastructure.persistence;

import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityEntity;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContainerJpaRepository extends JpaRepository<ContainerEntity, Long> {

    @Query("SELECT c FROM ContainerEntity c " +
           "WHERE LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT 1 FROM GridEntity g WHERE g.id = c.gridId " +
           "AND LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
           "EXISTS (SELECT 1 FROM GridEntity g, FacilityEntity f " +
           "WHERE g.id = c.gridId AND f.id = g.facilityId " +
           "AND LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ContainerEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM ContainerEntity c " +
           "WHERE c.gridId = :gridId AND (" +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ContainerEntity> searchByGrid(@Param("keyword") String keyword,
                                       @Param("gridId") Long gridId,
                                       Pageable pageable);

    @Query("SELECT c FROM ContainerEntity c " +
           "WHERE EXISTS (SELECT 1 FROM GridEntity g WHERE g.id = c.gridId AND g.facilityId = :facilityId) " +
           "AND (LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ContainerEntity> searchByFacility(@Param("keyword") String keyword,
                                           @Param("facilityId") Long facilityId,
                                           Pageable pageable);

    Page<ContainerEntity> findByGridId(Long gridId, Pageable pageable);

    boolean existsByGridIdAndCode(Long gridId, String code);

    boolean existsByGridIdAndCodeAndIdNot(Long gridId, String code, Long id);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);
}
