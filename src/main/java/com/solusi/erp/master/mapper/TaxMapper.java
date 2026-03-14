package com.solusi.erp.master.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.dto.TaxRequest;
import com.solusi.erp.master.dto.TaxResponse;
import com.solusi.erp.master.model.Tax;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface TaxMapper {

    TaxResponse toResponse(Tax entity);

    TaxRequest toRequest(Tax entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    Tax toEntity(TaxRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    void updateEntityFromRequest(TaxRequest request, @MappingTarget Tax entity);
}
