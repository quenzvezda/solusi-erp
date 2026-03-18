package com.solusi.erp.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for On-Hand Summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockSummaryResponse {
    private Long productId;
    private String productCode;
    private String productName;
    private String uomCode;
    private BigDecimal totalOnHand;
    private BigDecimal totalReserved;
    private BigDecimal totalAvailable;
    private BigDecimal totalInTransit;
}
