package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.Container;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Container.
 */
@Repository
public interface ContainerRepository extends JpaRepository<Container, Long> {

    @Query("SELECT c FROM Container c " +
           "JOIN c.grid g " +
           "JOIN g.facility f " +
           "WHERE LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Container> search(@Param("keyword") String keyword, Pageable pageable);

    Page<Container> findByGridId(Long gridId, Pageable pageable);

    boolean existsByGridIdAndCode(Long gridId, String code);

    boolean existsByGridIdAndCodeAndIdNot(Long gridId, String code, Long id);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);
}
