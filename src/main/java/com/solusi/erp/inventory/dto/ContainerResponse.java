package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Response DTO for Container.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContainerResponse extends BaseAuditResponse {
    private Long gridId;
    private String gridName;
    private Long facilityId;
    private String facilityName;
    private String code;
    private String name;
    private String barcode;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal maxWeight;
    private String note;
    private Boolean isActive;
}
