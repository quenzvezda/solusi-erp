package com.solusi.erp.inventory.productcategory.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.model.ProductCategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductCategorySaveRequest extends BaseAuditResponse {

    @Size(max = 50, message = "{label.product.category.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.product.category.name} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.product.category.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.product.category.type} {validation.notnull.suffix}")
    private ProductCategoryType type;

    private String note;
}
