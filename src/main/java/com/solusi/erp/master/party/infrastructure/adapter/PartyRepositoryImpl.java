package com.solusi.erp.master.party.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.master.party.infrastructure.persistence.PartyAddress;
import com.solusi.erp.master.party.infrastructure.persistence.PartyContact;
import com.solusi.erp.master.party.infrastructure.persistence.PartyIdentification;
import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.model.PartyAddressData;
import com.solusi.erp.master.party.domain.model.PartyContactData;
import com.solusi.erp.master.party.domain.model.PartyIdentificationData;
import com.solusi.erp.master.party.domain.repository.PartyRepository;
import com.solusi.erp.master.party.infrastructure.persistence.PartyPersistenceMapper;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository;
import com.solusi.erp.master.party.infrastructure.persistence.PartyIdentificationTypeJpaRepository;
import com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleTypeJpaRepository;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PartyRepositoryImpl implements PartyRepository {

    private final com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository jpaPartyRepo;
    private final PartyRoleTypeJpaRepository roleTypeRepository;
    private final GeographicJpaRepository geographicRepository;
    private final PartyIdentificationTypeJpaRepository idTypeRepository;
    private final PartyPersistenceMapper persistenceMapper;

    public PartyRepositoryImpl(
            com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository jpaPartyRepo,
            PartyRoleTypeJpaRepository roleTypeRepository,
            GeographicJpaRepository geographicRepository,
            PartyIdentificationTypeJpaRepository idTypeRepository,
            PartyPersistenceMapper persistenceMapper) {
        this.jpaPartyRepo = jpaPartyRepo;
        this.roleTypeRepository = roleTypeRepository;
        this.geographicRepository = geographicRepository;
        this.idTypeRepository = idTypeRepository;
        this.persistenceMapper = persistenceMapper;
    }

    @Override
    public Party save(Party domain) {
        com.solusi.erp.master.party.infrastructure.persistence.Party entity;
        Long domainId = domain.getMetadata().id();

        if (domainId == null) {
            entity = new com.solusi.erp.master.party.infrastructure.persistence.Party();
        } else {
            entity = jpaPartyRepo.findById(domainId)
                    .orElseThrow(() -> new RuntimeException("Party not found: " + domainId));
        }

        entity.setCode(domain.getCode());
        entity.setSalutation(domain.getSalutation());
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setNotes(domain.getNotes());
        entity.setIsActive(domain.getIsActive());
        entity.setEmail(domain.getEmail());
        entity.setPhone(domain.getPhone());

        syncRoles(entity, domain.getRoleIds());
        syncContacts(entity, domain.getContacts());
        syncAddresses(entity, domain.getAddresses());
        syncIdentifications(entity, domain.getIdentifications());

        com.solusi.erp.master.party.infrastructure.persistence.Party saved = jpaPartyRepo.save(entity);
        return persistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Party> findById(Long id) {
        return jpaPartyRepo.findById(id).map(persistenceMapper::toDomain);
    }

    @Override
    public Page<Party> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.master.party.infrastructure.persistence.Party> springPage =
                (keyword != null && !keyword.isBlank())
                        ? jpaPartyRepo.search(keyword, springPageable)
                        : jpaPartyRepo.findAll(springPageable);
        return new Page<>(
                springPage.getContent().stream().map(persistenceMapper::toDomain).collect(Collectors.toList()),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public List<Party> findForLookup(String keyword) {
        return jpaPartyRepo.search(keyword != null ? keyword : "", PageRequest.of(0, 10))
                .getContent().stream()
                .map(persistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Party> findAvailableForUser(String keyword, Long excludePartyId) {
        String kw = keyword != null ? keyword : "";
        org.springframework.data.domain.Page<com.solusi.erp.master.party.infrastructure.persistence.Party> page;
        if (excludePartyId != null) {
            page = jpaPartyRepo.searchAvailableForUser(kw, excludePartyId, PageRequest.of(0, 10));
        } else {
            page = jpaPartyRepo.searchUnassigned(kw, PageRequest.of(0, 10));
        }
        return page.getContent().stream()
                .map(persistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        jpaPartyRepo.deleteById(id);
    }

    private void syncRoles(com.solusi.erp.master.party.infrastructure.persistence.Party entity, Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            entity.setRoles(new HashSet<>());
            return;
        }
        entity.setRoles(new HashSet<>(roleTypeRepository.findAllById(roleIds)));
    }

    private void syncContacts(com.solusi.erp.master.party.infrastructure.persistence.Party entity, List<PartyContactData> requests) {
        Map<Long, PartyContact> existingById = entity.getContacts().stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(PartyContact::getId, c -> c));
        Set<Long> incomingIds = new HashSet<>();
        if (requests != null) {
            for (PartyContactData req : requests) {
                if (req.label() == null || req.label().isBlank()) continue;
                if (req.id() != null && existingById.containsKey(req.id())) {
                    PartyContact existing = existingById.get(req.id());
                    existing.setLabel(req.label());
                    existing.setMobile(req.mobile());
                    existing.setPhone(req.phone());
                    existing.setEmail(req.email());
                    existing.setIsActive(Boolean.TRUE.equals(req.isActive()));
                    existing.setIsDefault(Boolean.TRUE.equals(req.isDefault()));
                    incomingIds.add(req.id());
                } else {
                    PartyContact contact = new PartyContact();
                    contact.setLabel(req.label());
                    contact.setMobile(req.mobile());
                    contact.setPhone(req.phone());
                    contact.setEmail(req.email());
                    contact.setIsActive(true);
                    contact.setIsDefault(Boolean.TRUE.equals(req.isDefault()));
                    entity.addContact(contact);
                }
            }
        }
        existingById.forEach((existId, contact) -> {
            if (!incomingIds.contains(existId)) {
                contact.setIsActive(false);
                contact.setIsDefault(false);
            }
        });
    }

    private void syncAddresses(com.solusi.erp.master.party.infrastructure.persistence.Party entity, List<PartyAddressData> requests) {
        Map<Long, PartyAddress> existingById = entity.getAddresses().stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(PartyAddress::getId, a -> a));
        Set<Long> incomingIds = new HashSet<>();
        if (requests != null) {
            for (PartyAddressData req : requests) {
                if (req.addressLine1() == null || req.addressLine1().isBlank()) continue;
                if (req.id() != null && existingById.containsKey(req.id())) {
                    PartyAddress existing = existingById.get(req.id());
                    existing.setTypes(req.types() != null ? new HashSet<>(req.types()) : new HashSet<>());
                    existing.setAddressLine1(req.addressLine1());
                    existing.setPostalCode(req.postalCode());
                    existing.setIsActive(Boolean.TRUE.equals(req.isActive()));
                    existing.setIsDefault(Boolean.TRUE.equals(req.isDefault()));
                    if (req.cityId() != null) {
                        geographicRepository.findById(req.cityId()).ifPresent(existing::setCity);
                    } else {
                        existing.setCity(null);
                    }
                    incomingIds.add(req.id());
                } else {
                    PartyAddress addr = new PartyAddress();
                    addr.setTypes(req.types() != null ? new HashSet<>(req.types()) : new HashSet<>());
                    addr.setAddressLine1(req.addressLine1());
                    addr.setPostalCode(req.postalCode());
                    addr.setIsActive(true);
                    addr.setIsDefault(Boolean.TRUE.equals(req.isDefault()));
                    if (req.cityId() != null) {
                        geographicRepository.findById(req.cityId()).ifPresent(addr::setCity);
                    }
                    entity.addAddress(addr);
                }
            }
        }
        existingById.forEach((existId, addr) -> {
            if (!incomingIds.contains(existId)) {
                addr.setIsActive(false);
                addr.setIsDefault(false);
            }
        });
    }

    private void syncIdentifications(com.solusi.erp.master.party.infrastructure.persistence.Party entity, List<PartyIdentificationData> requests) {
        Map<Long, PartyIdentification> existingById = entity.getIdentifications().stream()
                .filter(i -> i.getId() != null)
                .collect(Collectors.toMap(PartyIdentification::getId, i -> i));
        Set<Long> incomingIds = new HashSet<>();
        if (requests != null) {
            for (PartyIdentificationData req : requests) {
                if (req.idNumber() == null || req.idNumber().isBlank()) continue;
                if (req.id() != null && existingById.containsKey(req.id())) {
                    PartyIdentification existing = existingById.get(req.id());
                    existing.setIdNumber(req.idNumber());
                    existing.setIssuedDate(req.issuedDate());
                    existing.setExpiryDate(req.expiryDate());
                    existing.setIsActive(Boolean.TRUE.equals(req.isActive()));
                    existing.setIsDefault(Boolean.TRUE.equals(req.isDefault()));
                    if (req.typeId() != null) {
                        idTypeRepository.findById(req.typeId()).ifPresent(existing::setType);
                    }
                    incomingIds.add(req.id());
                } else {
                    PartyIdentification iden = new PartyIdentification();
                    iden.setIdNumber(req.idNumber());
                    iden.setIssuedDate(req.issuedDate());
                    iden.setExpiryDate(req.expiryDate());
                    iden.setIsActive(true);
                    iden.setIsDefault(Boolean.TRUE.equals(req.isDefault()));
                    iden.setType(req.typeId() != null ? idTypeRepository.findById(req.typeId()).orElse(null) : null);
                    entity.addIdentification(iden);
                }
            }
        }
        existingById.forEach((existId, iden) -> {
            if (!incomingIds.contains(existId)) {
                iden.setIsActive(false);
                iden.setIsDefault(false);
            }
        });
    }
}


