package com.solusi.erp.inventory.productcategory.web.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.web.dto.ProductCategoryDetailResponse;
import com.solusi.erp.inventory.productcategory.web.dto.ProductCategorySaveRequest;
import com.solusi.erp.inventory.productcategory.web.dto.ProductCategorySummaryResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public abstract class ProductCategoryWebMapper {

    public abstract ProductCategorySummaryResponse toSummaryResponse(ProductCategory domain);

    public abstract ProductCategoryDetailResponse toDetailResponse(ProductCategory domain);

    public abstract ProductCategorySaveRequest toSaveRequest(ProductCategory domain);

    @AfterMapping
    protected void mapAuditFields(ProductCategory domain, @MappingTarget com.solusi.erp.core.dto.BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
        }
    }
}
