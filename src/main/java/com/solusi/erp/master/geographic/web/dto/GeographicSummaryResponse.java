package com.solusi.erp.master.geographic.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.model.GeographicType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Geographic list view.
 * Fields correspond to ${item.xxx} expressions used in list.html template.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GeographicSummaryResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private GeographicType type;
    private Long parentId;
    private String parentName;
    private Boolean isActive;
}
