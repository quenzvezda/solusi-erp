package com.solusi.erp.inventory.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.dto.BrandRequest;
import com.solusi.erp.inventory.dto.BrandResponse;
import com.solusi.erp.inventory.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Mapper for Brand.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface BrandMapper {

    Brand toEntity(BrandRequest request);

    BrandResponse toResponse(Brand entity);

    void updateEntityFromRequest(BrandRequest request, @MappingTarget Brand entity);
}
