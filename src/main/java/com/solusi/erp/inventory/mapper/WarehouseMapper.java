package com.solusi.erp.inventory.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.dto.*;
import com.solusi.erp.inventory.model.Container;
import com.solusi.erp.inventory.model.Facility;
import com.solusi.erp.inventory.model.Grid;
import com.solusi.erp.master.model.Geographic;
import com.solusi.erp.master.model.Party;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Warehouse Hierarchy.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface WarehouseMapper {

    // --- Facility ---
    @Mapping(target = "owner", source = "ownerId")
    @Mapping(target = "address.addressLine1", source = "addressLine1")
    @Mapping(target = "address.city", source = "cityId")
    @Mapping(target = "address.postalCode", source = "postalCode")
    Facility toEntity(FacilityRequest request);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.name")
    @Mapping(target = "addressLine1", source = "address.addressLine1")
    @Mapping(target = "cityId", source = "address.city.id")
    @Mapping(target = "cityName", source = "address.city.name")
    @Mapping(target = "postalCode", source = "address.postalCode")
    FacilityResponse toResponse(Facility entity);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "addressLine1", source = "address.addressLine1")
    @Mapping(target = "cityId", source = "address.city.id")
    @Mapping(target = "postalCode", source = "address.postalCode")
    FacilityRequest toRequest(Facility entity);

    @Mapping(target = "owner", source = "ownerId")
    @Mapping(target = "address.addressLine1", source = "addressLine1")
    @Mapping(target = "address.city", source = "cityId")
    @Mapping(target = "address.postalCode", source = "postalCode")
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(FacilityRequest request, @MappingTarget Facility entity);

    // --- Grid ---
    @Mapping(target = "facility", source = "facilityId")
    Grid toEntity(GridRequest request);

    @Mapping(target = "facilityId", source = "facility.id")
    @Mapping(target = "facilityName", source = "facility.name")
    GridResponse toResponse(Grid entity);

    @Mapping(target = "facilityId", source = "facility.id")
    GridRequest toRequest(Grid entity);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @Mapping(target = "facility", source = "facilityId")
    void updateEntityFromRequest(GridRequest request, @MappingTarget Grid entity);

    // --- Container ---
    @Mapping(target = "grid", source = "gridId")
    @Mapping(target = "dimensions.length", source = "length")
    @Mapping(target = "dimensions.width", source = "width")
    @Mapping(target = "dimensions.height", source = "height")
    Container toEntity(ContainerRequest request);

    @Mapping(target = "gridId", source = "grid.id")
    @Mapping(target = "gridName", source = "grid.name")
    @Mapping(target = "facilityId", source = "grid.facility.id")
    @Mapping(target = "facilityName", source = "grid.facility.name")
    @Mapping(target = "length", source = "dimensions.length")
    @Mapping(target = "width", source = "dimensions.width")
    @Mapping(target = "height", source = "dimensions.height")
    ContainerResponse toResponse(Container entity);

    @Mapping(target = "gridId", source = "grid.id")
    @Mapping(target = "length", source = "dimensions.length")
    @Mapping(target = "width", source = "dimensions.width")
    @Mapping(target = "height", source = "dimensions.height")
    ContainerRequest toRequest(Container entity);

    @Mapping(target = "grid", source = "gridId")
    @Mapping(target = "dimensions.length", source = "length")
    @Mapping(target = "dimensions.width", source = "width")
    @Mapping(target = "dimensions.height", source = "height")
    void updateEntityFromRequest(ContainerRequest request, @MappingTarget Container entity);

    // --- Helper Mappers ---
    default Party mapParty(Long id) {
        if (id == null) return null;
        Party entity = new Party();
        entity.setId(id);
        return entity;
    }

    default Geographic mapGeographic(Long id) {
        if (id == null) return null;
        Geographic entity = new Geographic();
        entity.setId(id);
        return entity;
    }

    default Facility mapFacility(Long id) {
        if (id == null) return null;
        Facility entity = new Facility();
        entity.setId(id);
        return entity;
    }

    default Grid mapGrid(Long id) {
        if (id == null) return null;
        Grid entity = new Grid();
        entity.setId(id);
        return entity;
    }
}
