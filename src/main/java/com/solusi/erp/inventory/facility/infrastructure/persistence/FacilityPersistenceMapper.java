package com.solusi.erp.inventory.facility.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.model.Address;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.master.model.Geographic;
import com.solusi.erp.master.model.Party;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FacilityPersistenceMapper {

    default Facility toDomain(com.solusi.erp.inventory.model.Facility entity) {
        if (entity == null) return null;
        AuditMetadata metadata = new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
        Long ownerId = entity.getOwner() != null ? entity.getOwner().getId() : null;
        String ownerName = entity.getOwner() != null ? entity.getOwner().getName() : null;
        String addressLine1 = entity.getAddress() != null ? entity.getAddress().getAddressLine1() : null;
        Long cityId = (entity.getAddress() != null && entity.getAddress().getCity() != null)
            ? entity.getAddress().getCity().getId() : null;
        String cityName = (entity.getAddress() != null && entity.getAddress().getCity() != null)
            ? entity.getAddress().getCity().getName() : null;
        String postalCode = entity.getAddress() != null ? entity.getAddress().getPostalCode() : null;
        return new Facility(metadata, entity.getCode(), entity.getName(), ownerId, ownerName,
            addressLine1, cityId, cityName, postalCode, entity.getNote(), entity.getIsActive());
    }

    default com.solusi.erp.inventory.model.Facility toEntity(Facility domain) {
        if (domain == null) return null;
        com.solusi.erp.inventory.model.Facility entity = new com.solusi.erp.inventory.model.Facility();
        if (domain.getMetadata().id() != null) entity.setId(domain.getMetadata().id());
        if (domain.getMetadata().version() != null) entity.setVersion(domain.getMetadata().version().intValue());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setNote(domain.getNote());
        entity.setIsActive(domain.getIsActive() != null ? domain.getIsActive() : Boolean.TRUE);
        if (domain.getOwnerId() != null) {
            Party owner = new Party();
            owner.setId(domain.getOwnerId());
            entity.setOwner(owner);
        }
        Address address = new Address();
        address.setAddressLine1(domain.getAddressLine1());
        address.setPostalCode(domain.getPostalCode());
        if (domain.getCityId() != null) {
            Geographic city = new Geographic();
            city.setId(domain.getCityId());
            address.setCity(city);
        }
        entity.setAddress(address);
        return entity;
    }
}
