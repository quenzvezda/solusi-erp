package com.solusi.erp.inventory.report.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryMovementResponse extends BaseAuditResponse {
    private LocalDateTime transactionDate;

    private Long productId;
    private String productCode;
    private String productName;

    private Long containerId;
    private String containerCode;
    private String facilityName;

    private String serialNumber;
    private BigDecimal quantity;
    private MovementType movementType;
    private ReferenceType referenceType;
    private Long referenceId;
    private String referenceCode;

    private BigDecimal unitCostOriginal;
    private BigDecimal unitCostLocal;
    private BigDecimal totalCostLocal;
    private String currencyAlias;
}
