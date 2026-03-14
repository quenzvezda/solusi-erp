package com.solusi.erp.master.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.dto.CurrencyRequest;
import com.solusi.erp.master.dto.CurrencyResponse;
import com.solusi.erp.master.model.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface CurrencyMapper {

    CurrencyResponse toResponse(Currency entity);

    CurrencyRequest toRequest(Currency entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    Currency toEntity(CurrencyRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    void updateEntityFromRequest(CurrencyRequest request, @MappingTarget Currency entity);
}
