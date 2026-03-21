package com.solusi.erp.inventory.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.dto.ProductUomConversionRequest;
import com.solusi.erp.inventory.dto.ProductUomConversionResponse;
import com.solusi.erp.inventory.form.ProductUomUIForm;
import com.solusi.erp.inventory.model.ProductUomConversion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface ProductUomConversionMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productCode", source = "product.code")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "fromUomId", source = "fromUom.id")
    @Mapping(target = "fromUomName", source = "fromUom.name")
    @Mapping(target = "toUomId", source = "toUom.id")
    @Mapping(target = "toUomName", source = "toUom.name")
    ProductUomConversionResponse toResponse(ProductUomConversion entity);

    List<ProductUomConversionResponse> toResponseList(List<ProductUomConversion> entities);

    @Mapping(target = "productCode", source = "product.code")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "fromUomName", source = "fromUom.name")
    @Mapping(target = "toUomName", source = "toUom.name")
    ProductUomUIForm toUIForm(ProductUomConversion entity);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "fromUom", ignore = true)
    @Mapping(target = "toUom", ignore = true)
    ProductUomConversion toEntity(ProductUomConversionRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "fromUomId", source = "fromUom.id")
    @Mapping(target = "toUomId", source = "toUom.id")
    ProductUomConversionRequest toRequest(ProductUomConversion entity);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "fromUom", ignore = true)
    @Mapping(target = "toUom", ignore = true)
    void updateEntityFromRequest(ProductUomConversionRequest request, @MappingTarget ProductUomConversion entity);
}
