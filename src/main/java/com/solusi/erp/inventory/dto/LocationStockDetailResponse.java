package com.solusi.erp.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for On-Hand Detail per Location.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationStockDetailResponse {
    private String facilityName;
    private String gridCode;
    private String containerCode;
    private String serialNumber;
    private BigDecimal onHand;
    private BigDecimal reserved;
    private BigDecimal available;
    private BigDecimal inTransit;
}
