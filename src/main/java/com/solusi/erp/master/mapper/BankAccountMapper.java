package com.solusi.erp.master.mapper;

import com.solusi.erp.master.dto.BankAccountRequestDto;
import com.solusi.erp.master.dto.BankAccountResponseDto;
import com.solusi.erp.master.model.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    @Mapping(target = "cityId", source = "city.id")
    @Mapping(target = "cityName", source = "city.name")
    @Mapping(target = "partyId", source = "party.id")
    @Mapping(target = "partyName", source = "party.name")
    BankAccountResponseDto toDto(BankAccount entity);

    @Mapping(target = "city", ignore = true)
    @Mapping(target = "party", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    BankAccount toEntity(BankAccountRequestDto dto);

    @Mapping(target = "city", ignore = true)
    @Mapping(target = "party", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "code", ignore = true) // Do not update code
    void updateEntityFromDto(BankAccountRequestDto dto, @MappingTarget BankAccount entity);
}
