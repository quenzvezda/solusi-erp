package com.solusi.erp.inventory.stock.infrastructure.persistence;

import com.solusi.erp.inventory.report.web.dto.LocationStockDetailResponse;
import com.solusi.erp.inventory.report.web.dto.ProductStockSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Stock Balance.
 */
@Repository
public interface StockBalanceJpaRepository extends JpaRepository<StockBalanceEntity, Long> {

    Optional<StockBalanceEntity> findByProductIdAndContainerIdAndSerialNumber(Long productId, Long containerId, String serialNumber);

    boolean existsByContainerId(Long containerId);

    @Query("SELECT new com.solusi.erp.inventory.report.web.dto.ProductStockSummaryResponse(" +
           "p.id, p.code, p.name, u.code, " +
           "SUM(sb.quantity), SUM(sb.reservedQuantity), SUM(sb.quantity - sb.reservedQuantity), SUM(sb.inTransitQuantity)) " +
           "FROM StockBalanceEntity sb, " +
           "com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity p, " +
           "com.solusi.erp.inventory.uom.infrastructure.persistence.UomEntity u " +
           "WHERE p.id = sb.productId " +
           "AND u.id = p.uomId " +
           "AND (:keyword IS NULL OR p.code LIKE %:keyword% OR p.name LIKE %:keyword%) " +
           "GROUP BY p.id, p.code, p.name, u.code")
    Page<ProductStockSummaryResponse> getOnHandSummary(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT new com.solusi.erp.inventory.report.web.dto.LocationStockDetailResponse(" +
           "f.name, g.code, c.code, sb.serialNumber, " +
           "sb.quantity, sb.reservedQuantity, (sb.quantity - sb.reservedQuantity), sb.inTransitQuantity) " +
           "FROM StockBalanceEntity sb, " +
           "com.solusi.erp.inventory.container.infrastructure.persistence.ContainerEntity c, " +
           "com.solusi.erp.inventory.grid.infrastructure.persistence.GridEntity g, " +
           "com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityEntity f " +
           "WHERE c.id = sb.containerId " +
           "AND g.id = c.gridId " +
           "AND f.id = g.facilityId " +
           "AND sb.productId = :productId")
    List<LocationStockDetailResponse> getOnHandDetail(@Param("productId") Long productId);
}
