package com.solusi.erp.inventory.mapper;

import com.solusi.erp.inventory.dto.ProductCategoryRequest;
import com.solusi.erp.inventory.dto.ProductCategoryResponse;
import com.solusi.erp.inventory.model.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Mapper for ProductCategory.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductCategoryMapper {

    ProductCategory toEntity(ProductCategoryRequest request);

    ProductCategoryResponse toResponse(ProductCategory entity);

    void updateEntityFromRequest(ProductCategoryRequest request, @MappingTarget ProductCategory entity);
}
