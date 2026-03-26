package com.solusi.erp.inventory.product.web.mapper;

import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.web.dto.ProductDetailResponse;
import com.solusi.erp.inventory.product.web.dto.ProductSaveRequest;
import com.solusi.erp.inventory.product.web.dto.ProductSummaryResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.repository.ProductCategoryRepository;
import com.solusi.erp.inventory.repository.UnitOfMeasureRepository;
import com.solusi.erp.inventory.repository.BrandRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Web Mapper for Product module.
 * Maps between Domain Model and Web DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public abstract class ProductWebMapper {

    @Autowired
    protected ProductCategoryRepository categoryRepository;
    @Autowired
    protected UnitOfMeasureRepository uomRepository;
    @Autowired
    protected BrandRepository brandRepository;

    @Mapping(target = "categoryName", source = "categoryId", qualifiedByName = "getCategoryName")
    @Mapping(target = "categoryType", source = "categoryId", qualifiedByName = "getCategoryType")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    @Mapping(target = "uomCode", source = "uomId", qualifiedByName = "getUomCode")
    @Mapping(target = "brandName", source = "brandId", qualifiedByName = "getBrandName")
    public abstract ProductSummaryResponse toSummaryResponse(Product domain);

    @Mapping(target = "categoryName", source = "categoryId", qualifiedByName = "getCategoryName")
    @Mapping(target = "categoryType", source = "categoryId", qualifiedByName = "getCategoryType")
    @Mapping(target = "uomName", source = "uomId", qualifiedByName = "getUomName")
    @Mapping(target = "uomCode", source = "uomId", qualifiedByName = "getUomCode")
    @Mapping(target = "brandName", source = "brandId", qualifiedByName = "getBrandName")
    @Mapping(target = "weightUomCode", source = "weightUomId", qualifiedByName = "getUomCode")
    @Mapping(target = "dimensionUomCode", source = "dimensionUomId", qualifiedByName = "getUomCode")
    public abstract ProductDetailResponse toDetailResponse(Product domain);

    public abstract ProductSaveRequest toSaveRequest(Product domain);

    @AfterMapping
    protected void mapAuditFields(Product domain, @MappingTarget com.solusi.erp.core.dto.BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            // Note: AuditMapperHelper will be used for mapping names if source was BaseModel, 
            // but here we manually map dates/id/version from our AuditMetadata record.
        }
    }

    @Named("getCategoryName")
    protected String getCategoryName(Long id) {
        if (id == null) return null;
        return categoryRepository.findById(id).map(c -> c.getName()).orElse(null);
    }

    @Named("getCategoryType")
    protected String getCategoryType(Long id) {
        if (id == null) return null;
        return categoryRepository.findById(id).map(c -> c.getType().name()).orElse(null);
    }

    @Named("getUomName")
    protected String getUomName(Long id) {
        if (id == null) return null;
        return uomRepository.findById(id).map(u -> u.getName()).orElse(null);
    }

    @Named("getUomCode")
    protected String getUomCode(Long id) {
        if (id == null) return null;
        return uomRepository.findById(id).map(u -> u.getCode()).orElse(null);
    }

    @Named("getBrandName")
    protected String getBrandName(Long id) {
        if (id == null) return null;
        return brandRepository.findById(id).map(b -> b.getName()).orElse(null);
    }
}
