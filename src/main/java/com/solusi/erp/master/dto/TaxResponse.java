package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for displaying Tax information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaxResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private BigDecimal rate;
    private String note;
    private Boolean isSubtract;
    private Boolean isActive;
}
