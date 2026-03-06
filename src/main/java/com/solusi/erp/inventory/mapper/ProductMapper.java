package com.solusi.erp.inventory.mapper;

import com.solusi.erp.inventory.dto.ProductRequest;
import com.solusi.erp.inventory.dto.ProductResponse;
import com.solusi.erp.inventory.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Mapper for Product.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "uom.id", source = "uomId")
    @Mapping(target = "brand.id", source = "brandId")
    @Mapping(target = "weightUom.id", source = "weightUomId")
    @Mapping(target = "dimensionUom.id", source = "dimensionUomId")
    Product toEntity(ProductRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryType", source = "category.type")
    @Mapping(target = "uomId", source = "uom.id")
    @Mapping(target = "uomName", source = "uom.name")
    @Mapping(target = "uomCode", source = "uom.code")
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "weightUomId", source = "weightUom.id")
    @Mapping(target = "weightUomCode", source = "weightUom.code")
    @Mapping(target = "dimensionUomId", source = "dimensionUom.id")
    @Mapping(target = "dimensionUomCode", source = "dimensionUom.code")
    ProductResponse toResponse(Product entity);

    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "uom.id", source = "uomId")
    @Mapping(target = "brand.id", source = "brandId")
    @Mapping(target = "weightUom.id", source = "weightUomId")
    @Mapping(target = "dimensionUom.id", source = "dimensionUomId")
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product entity);
}
