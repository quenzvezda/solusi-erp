package com.solusi.erp.inventory.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.dto.UnitOfMeasureRequest;
import com.solusi.erp.inventory.dto.UnitOfMeasureResponse;
import com.solusi.erp.inventory.model.UnitOfMeasure;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Mapper for UnitOfMeasure.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface UnitOfMeasureMapper {

    UnitOfMeasure toEntity(UnitOfMeasureRequest request);

    UnitOfMeasureResponse toResponse(UnitOfMeasure entity);

    UnitOfMeasureRequest toRequest(UnitOfMeasure entity);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(UnitOfMeasureRequest request, @MappingTarget UnitOfMeasure entity);
}
