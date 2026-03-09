package com.solusi.erp.master.mapper;

import com.solusi.erp.master.dto.GeographicDto;
import com.solusi.erp.master.model.Geographic;
import org.mapstruct.*;

/**
 * Mapper for Geographic entity and DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GeographicMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    GeographicDto toDto(Geographic geographic);

    @Mapping(target = "parent", ignore = true) // Handled in Service
    Geographic toEntity(GeographicDto geographicDto);

    @Mapping(target = "parent", ignore = true) // Handled in Service
    void updateEntity(GeographicDto geographicDto, @MappingTarget Geographic geographic);
}
