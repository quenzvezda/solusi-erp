package com.solusi.erp.master.mapper;

import com.solusi.erp.master.dto.*;
import com.solusi.erp.master.model.*;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct Mapper for Party and its components.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PartyMapper {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "identifications", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    Party toEntity(PartyRequest request);

    @Mapping(target = "roleCodes", source = "roles", qualifiedByName = "mapRoleCodes")
    @Mapping(target = "roleNames", source = "roles", qualifiedByName = "mapRoleNames")
    PartyResponse toResponse(Party entity);

    @Named("mapRoleCodes")
    default Set<String> mapRoleCodes(Set<PartyRoleType> roles) {
        return roles.stream().map(PartyRoleType::getCode).collect(Collectors.toSet());
    }

    @Named("mapRoleNames")
    default Set<String> mapRoleNames(Set<PartyRoleType> roles) {
        return roles.stream().map(PartyRoleType::getName).collect(Collectors.toSet());
    }

    @Mapping(target = "typeId", source = "type.id")
    @Mapping(target = "typeCode", source = "type.code")
    @Mapping(target = "typeName", source = "type.name")
    PartyIdentificationResponse toResponse(PartyIdentification entity);

    @Mapping(target = "cityId", source = "city.id")
    @Mapping(target = "cityName", source = "city.name")
    @Mapping(target = "provinceName", source = "city", qualifiedByName = "mapProvinceName")
    @Mapping(target = "countryName", source = "city", qualifiedByName = "mapCountryName")
    PartyAddressResponse toResponse(PartyAddress entity);

    PartyContactResponse toResponse(PartyContact entity);

    @Mapping(target = "party", ignore = true)
    @Mapping(target = "type", ignore = true) // Handled in service
    PartyIdentification toEntity(PartyIdentificationRequest request);

    @Mapping(target = "party", ignore = true)
    @Mapping(target = "city", ignore = true) // Handled in service/manual
    PartyAddress toEntity(PartyAddressRequest request);

    @Mapping(target = "party", ignore = true)
    PartyContact toEntity(PartyContactRequest request);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "identifications", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    void updateEntityFromRequest(PartyRequest request, @MappingTarget Party entity);

    @Named("mapProvinceName")
    default String mapProvinceName(Geographic city) {
        if (city == null || city.getParent() == null) return null;
        if (city.getParent().getType() == GeographicType.STATE_PROVINCE) {
            return city.getParent().getName();
        }
        return null;
    }

    @Named("mapCountryName")
    default String mapCountryName(Geographic city) {
        if (city == null) return null;
        Geographic current = city.getParent();
        while (current != null) {
            if (current.getType() == GeographicType.COUNTRY) {
                return current.getName();
            }
            current = current.getParent();
        }
        return null;
    }
}
