package com.solusi.erp.inventory.product.infrastructure.persistence;

import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.model.Brand;
import com.solusi.erp.inventory.model.ProductCategory;
import com.solusi.erp.inventory.model.UnitOfMeasure;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "uomId", source = "uom.id")
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "weightUomId", source = "weightUom.id")
    @Mapping(target = "dimensionUomId", source = "dimensionUom.id")
    Product toDomain(ProductEntity entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", source = "metadata.version")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "toCategory")
    @Mapping(target = "uom", source = "uomId", qualifiedByName = "toUom")
    @Mapping(target = "brand", source = "brandId", qualifiedByName = "toBrand")
    @Mapping(target = "weightUom", source = "weightUomId", qualifiedByName = "toUom")
    @Mapping(target = "dimensionUom", source = "dimensionUomId", qualifiedByName = "toUom")
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "isSerialized", source = "serialized")
    ProductEntity toEntity(Product domain);

    default AuditMetadata toAuditMetadata(ProductEntity entity) {
        return new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
    }

    @Named("toCategory")
    default ProductCategory toCategory(Long id) {
        if (id == null) return null;
        ProductCategory category = new ProductCategory();
        category.setId(id);
        return category;
    }

    @Named("toUom")
    default UnitOfMeasure toUom(Long id) {
        if (id == null) return null;
        UnitOfMeasure uom = new UnitOfMeasure();
        uom.setId(id);
        return uom;
    }

    @Named("toBrand")
    default Brand toBrand(Long id) {
        if (id == null) return null;
        Brand brand = new Brand();
        brand.setId(id);
        return brand;
    }
}
