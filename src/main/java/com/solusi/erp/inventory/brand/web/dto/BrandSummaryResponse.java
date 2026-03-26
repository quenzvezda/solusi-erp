package com.solusi.erp.inventory.brand.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Brand list view.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BrandSummaryResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private String note;
}
