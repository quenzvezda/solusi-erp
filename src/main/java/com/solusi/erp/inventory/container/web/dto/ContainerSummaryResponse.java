package com.solusi.erp.inventory.container.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContainerSummaryResponse extends BaseAuditResponse {
    private Long gridId;
    private String gridName;
    private String facilityName;
    private String code;
    private String name;
    private String barcode;
    private Boolean isActive;
}
