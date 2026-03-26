package com.solusi.erp.inventory.productcategory.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.model.ProductCategoryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductCategoryDetailResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private ProductCategoryType type;
    private String note;
}
