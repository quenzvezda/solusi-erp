package com.solusi.erp.inventory.uom.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.model.UomType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response DTO for UnitOfMeasure list view.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UomSummaryResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private UomType type;
}
