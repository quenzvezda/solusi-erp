package com.solusi.erp.inventory.facility.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.model.Address;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository;
import com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class FacilityPersistenceMapper {

    private final GeographicJpaRepository geographicRepository;
    private final PartyJpaRepository partyRepository;

    public FacilityPersistenceMapper(GeographicJpaRepository geographicRepository, PartyJpaRepository partyRepository) {
        this.geographicRepository = geographicRepository;
        this.partyRepository = partyRepository;
    }

    public Facility toDomain(FacilityEntity entity) {
        if (entity == null) return null;
        AuditMetadata metadata = new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
        Long ownerId = entity.getOwnerId();
        String ownerName = findOwnerName(ownerId);
        String addressLine1 = entity.getAddress() != null ? entity.getAddress().getAddressLine1() : null;
        Long cityId = entity.getAddress() != null ? entity.getAddress().getCityId() : null;
        String cityName = findCityName(cityId);
        String postalCode = entity.getAddress() != null ? entity.getAddress().getPostalCode() : null;
        return new Facility(metadata, entity.getCode(), entity.getName(), ownerId, ownerName,
            addressLine1, cityId, cityName, postalCode, entity.getNote(), entity.getIsActive());
    }

    public FacilityEntity toEntity(Facility domain) {
        if (domain == null) return null;
        FacilityEntity entity = new FacilityEntity();
        if (domain.getMetadata().id() != null) entity.setId(domain.getMetadata().id());
        if (domain.getMetadata().version() != null) entity.setVersion(domain.getMetadata().version().intValue());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setNote(domain.getNote());
        entity.setIsActive(domain.getIsActive() != null ? domain.getIsActive() : Boolean.TRUE);
        entity.setOwnerId(domain.getOwnerId());
        Address address = new Address();
        address.setAddressLine1(domain.getAddressLine1());
        address.setCityId(domain.getCityId());
        address.setPostalCode(domain.getPostalCode());
        entity.setAddress(address);
        return entity;
    }

    private String findOwnerName(Long ownerId) {
        if (ownerId == null) return null;
        return partyRepository.findById(ownerId)
                .map(com.solusi.erp.master.party.infrastructure.persistence.Party::getName)
                .orElse(null);
    }

    private String findCityName(Long cityId) {
        if (cityId == null) return null;
        return geographicRepository.findById(cityId)
                .map(com.solusi.erp.master.geographic.infrastructure.persistence.Geographic::getName)
                .orElse(null);
    }
}
