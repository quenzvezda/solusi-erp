package com.solusi.erp.master.geographic.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.model.GeographicType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Geographic detail/audit view.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GeographicDetailResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private GeographicType type;
    private Long parentId;
    private String parentName;
    private Boolean isActive;
}
