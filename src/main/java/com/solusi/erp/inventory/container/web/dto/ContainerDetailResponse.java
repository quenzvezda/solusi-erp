package com.solusi.erp.inventory.container.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContainerDetailResponse extends BaseAuditResponse {
    private Long gridId;
    private String gridName;
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
