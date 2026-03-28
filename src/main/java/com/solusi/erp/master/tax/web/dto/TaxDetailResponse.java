package com.solusi.erp.master.tax.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaxDetailResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private BigDecimal rate;
    private String note;
    private Boolean isSubtract;
    private Boolean isActive;
}
