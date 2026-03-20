package com.solusi.erp.inventory.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.dto.ProductRequest;
import com.solusi.erp.inventory.dto.ProductResponse;
import com.solusi.erp.inventory.form.ProductUIForm;
import com.solusi.erp.inventory.model.Product;
import com.solusi.erp.inventory.model.ProductCategory;
import com.solusi.erp.inventory.model.UnitOfMeasure;
import com.solusi.erp.inventory.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Mapper for Product.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface ProductMapper {

    // --- Entity to Request ---
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "uomId", source = "uom.id")
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "weightUomId", source = "weightUom.id")
    @Mapping(target = "dimensionUomId", source = "dimensionUom.id")
    ProductRequest toRequest(Product entity);

    // --- Entity to UI Form ---
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryCode", source = "category.code")
    @Mapping(target = "categoryType", source = "category.type")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "brandCode", source = "brand.code")
    @Mapping(target = "uomName", source = "uom.name")
    ProductUIForm toUIForm(Product entity);

    // --- Entity to Response ---
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

    // --- Request to Entity ---
    @Mapping(target = "category", source = "categoryId")
    @Mapping(target = "uom", source = "uomId")
    @Mapping(target = "brand", source = "brandId")
    @Mapping(target = "weightUom", source = "weightUomId")
    @Mapping(target = "dimensionUom", source = "dimensionUomId")
    Product toEntity(ProductRequest request);

    @Mapping(target = "category", source = "categoryId")
    @Mapping(target = "uom", source = "uomId")
    @Mapping(target = "brand", source = "brandId")
    @Mapping(target = "weightUom", source = "weightUomId")
    @Mapping(target = "dimensionUom", source = "dimensionUomId")
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product entity);

    default ProductCategory mapProductCategory(Long id) {
        if (id == null) return null;
        ProductCategory entity = new ProductCategory();
        entity.setId(id);
        return entity;
    }

    default UnitOfMeasure mapUnitOfMeasure(Long id) {
        if (id == null) return null;
        UnitOfMeasure entity = new UnitOfMeasure();
        entity.setId(id);
        return entity;
    }

    default Brand mapBrand(Long id) {
        if (id == null) return null;
        Brand entity = new Brand();
        entity.setId(id);
        return entity;
    }
}
