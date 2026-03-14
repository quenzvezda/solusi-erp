package com.solusi.erp.master.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.dto.BankAccountRequest;
import com.solusi.erp.master.dto.BankAccountResponse;
import com.solusi.erp.master.model.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface BankAccountMapper {

    @Mapping(target = "cityId", source = "city.id")
    @Mapping(target = "cityName", source = "city.name")
    @Mapping(target = "partyId", source = "party.id")
    @Mapping(target = "partyName", source = "party.name")
    BankAccountResponse toResponse(BankAccount entity);

    @Mapping(target = "cityId", source = "city.id")
    @Mapping(target = "partyId", source = "party.id")
    BankAccountRequest toRequest(BankAccount entity);

    @Mapping(target = "city", ignore = true)
    @Mapping(target = "party", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    BankAccount toEntity(BankAccountRequest request);

    @Mapping(target = "city", ignore = true)
    @Mapping(target = "party", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "code", ignore = true) // Do not update code
    void updateEntityFromRequest(BankAccountRequest request, @MappingTarget BankAccount entity);
}
