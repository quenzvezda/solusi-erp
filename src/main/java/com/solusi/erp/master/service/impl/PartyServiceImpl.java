package com.solusi.erp.master.service.impl;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.master.dto.*;
import com.solusi.erp.master.mapper.PartyMapper;
import com.solusi.erp.master.model.*;
import com.solusi.erp.master.repository.PartyIdentificationTypeRepository;
import com.solusi.erp.master.repository.PartyRepository;
import com.solusi.erp.master.repository.PartyRoleTypeRepository;
import com.solusi.erp.master.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of PartyService.
 */
@Service
@RequiredArgsConstructor
public class PartyServiceImpl implements PartyService {

    private final PartyRepository repository;
    private final PartyRoleTypeRepository roleTypeRepository;
    private final PartyIdentificationTypeRepository idTypeRepository;
    private final PartyMapper mapper;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public Page<PartyResponse> findAll(String keyword, Pageable pageable) {
        Page<Party> page = StringUtils.hasText(keyword)
                ? repository.search(keyword, pageable)
                : repository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PartyResponse findById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PartyRequest getEditData(Long id) {
        Party entity = findOrThrow(id);

        return PartyRequest.builder()
                .id(entity.getId())
                .salutation(entity.getSalutation())
                .code(entity.getCode())
                .name(entity.getName())
                .type(entity.getType())
                .notes(entity.getNotes())
                .isActive(entity.getIsActive())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .roleIds(entity.getRoles().stream().map(PartyRoleType::getId).collect(Collectors.toSet()))
                .identifications(entity.getIdentifications().stream()
                        .map(i -> PartyIdentificationRequest.builder()
                                .id(i.getId())
                                .typeId(i.getType().getId())
                                .idNumber(i.getIdNumber())
                                .issuedDate(i.getIssuedDate())
                                .expiryDate(i.getExpiryDate())
                                .isActive(i.getIsActive())
                                .isDefault(i.getIsDefault())
                                .build())
                        .collect(Collectors.toList()))
                .addresses(entity.getAddresses().stream()
                        .map(a -> PartyAddressRequest.builder()
                                .id(a.getId())
                                .type(a.getType())
                                .addressLine1(a.getAddressLine1())
                                .city(a.getCity())
                                .province(a.getProvince())
                                .postalCode(a.getPostalCode())
                                .country(a.getCountry())
                                .isActive(a.getIsActive())
                                .isDefault(a.getIsDefault())
                                .build())
                        .collect(Collectors.toList()))
                .contacts(entity.getContacts().stream()
                        .map(c -> PartyContactRequest.builder()
                                .id(c.getId())
                                .label(c.getLabel())
                                .mobile(c.getMobile())
                                .phone(c.getPhone())
                                .email(c.getEmail())
                                .isActive(c.getIsActive())
                                .isDefault(c.getIsDefault())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    public void create(PartyRequest request) {
        validateSingleDefault(request);

        Party entity = mapper.toEntity(request);
        entity.setCode(sequenceGeneratorService.generate("PARTY"));

        syncRoles(entity, request.getRoleIds());
        syncIdentifications(entity, request.getIdentifications());
        syncAddresses(entity, request.getAddresses());
        syncContacts(entity, request.getContacts());

        repository.save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, PartyRequest request) {
        Party entity = findOrThrow(id);

        if (StringUtils.hasText(request.getCode()) && repository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new RuntimeException(getMessage("msg.error.party.duplicate-code"));
        }

        validateSingleDefault(request);
        mapper.updateEntityFromRequest(request, entity);

        syncRoles(entity, request.getRoleIds());
        syncIdentifications(entity, request.getIdentifications());
        syncAddresses(entity, request.getAddresses());
        syncContacts(entity, request.getContacts());

        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(getMessage("msg.error.party.notfound"));
        }
        repository.deleteById(id);
    }

    @Override
    public List<PartyRoleType> findAllRoleTypes() {
        return roleTypeRepository.findAll();
    }

    @Override
    public List<PartyIdentificationType> findAllIdTypes() {
        return idTypeRepository.findAll();
    }

    // ===================================================================
    // Private sync helpers — SOFT DELETE pattern
    // ===================================================================

    private void syncRoles(Party entity, Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            entity.setRoles(new HashSet<>());
            return;
        }
        entity.setRoles(new HashSet<>(roleTypeRepository.findAllById(roleIds)));
    }

    /**
     * Soft-delete sync for Identifications.
     * - Rows with incoming id → update fields (isActive, isDefault, etc.)
     * - Rows with no id → create new
     * - Existing DB rows NOT present in request → set isActive = false
     */
    private void syncIdentifications(Party entity, List<PartyIdentificationRequest> requests) {
        Map<Long, PartyIdentification> existingById = entity.getIdentifications().stream()
                .filter(i -> i.getId() != null)
                .collect(Collectors.toMap(PartyIdentification::getId, i -> i));

        Set<Long> incomingIds = new HashSet<>();

        if (requests != null) {
            for (PartyIdentificationRequest req : requests) {
                if (!StringUtils.hasText(req.getIdNumber()))
                    continue;

                if (req.getId() != null && existingById.containsKey(req.getId())) {
                    // Update existing
                    PartyIdentification existing = existingById.get(req.getId());
                    existing.setIdNumber(req.getIdNumber());
                    existing.setIssuedDate(req.getIssuedDate());
                    existing.setExpiryDate(req.getExpiryDate());
                    existing.setIsActive(Boolean.TRUE.equals(req.getIsActive()));
                    existing.setIsDefault(Boolean.TRUE.equals(req.getIsDefault()));
                    if (req.getTypeId() != null) {
                        idTypeRepository.findById(req.getTypeId()).ifPresent(existing::setType);
                    }
                    incomingIds.add(req.getId());
                } else {
                    // Create new
                    PartyIdentification iden = new PartyIdentification();
                    iden.setIdNumber(req.getIdNumber());
                    iden.setIssuedDate(req.getIssuedDate());
                    iden.setExpiryDate(req.getExpiryDate());
                    iden.setIsActive(true);
                    iden.setIsDefault(Boolean.TRUE.equals(req.getIsDefault()));
                    iden.setType(
                            req.getTypeId() != null ? idTypeRepository.findById(req.getTypeId()).orElse(null) : null);
                    entity.addIdentification(iden);
                }
            }
        }

        // Soft-delete rows not in the request
        existingById.forEach((existId, iden) -> {
            if (!incomingIds.contains(existId)) {
                iden.setIsActive(false);
                iden.setIsDefault(false);
            }
        });
    }

    /**
     * Soft-delete sync for Addresses.
     */
    private void syncAddresses(Party entity, List<PartyAddressRequest> requests) {
        Map<Long, PartyAddress> existingById = entity.getAddresses().stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(PartyAddress::getId, a -> a));

        Set<Long> incomingIds = new HashSet<>();

        if (requests != null) {
            for (PartyAddressRequest req : requests) {
                if (!StringUtils.hasText(req.getAddressLine1()))
                    continue;

                if (req.getId() != null && existingById.containsKey(req.getId())) {
                    PartyAddress existing = existingById.get(req.getId());
                    existing.setType(req.getType());
                    existing.setAddressLine1(req.getAddressLine1());
                    existing.setCity(req.getCity());
                    existing.setProvince(req.getProvince());
                    existing.setPostalCode(req.getPostalCode());
                    existing.setCountry(req.getCountry());
                    existing.setIsActive(Boolean.TRUE.equals(req.getIsActive()));
                    existing.setIsDefault(Boolean.TRUE.equals(req.getIsDefault()));
                    incomingIds.add(req.getId());
                } else {
                    PartyAddress addr = mapper.toEntity(req);
                    addr.setIsActive(true);
                    addr.setIsDefault(Boolean.TRUE.equals(req.getIsDefault()));
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

    /**
     * Soft-delete sync for Contacts.
     */
    private void syncContacts(Party entity, List<PartyContactRequest> requests) {
        Map<Long, PartyContact> existingById = entity.getContacts().stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(PartyContact::getId, c -> c));

        Set<Long> incomingIds = new HashSet<>();

        if (requests != null) {
            for (PartyContactRequest req : requests) {
                if (!StringUtils.hasText(req.getLabel()))
                    continue;

                if (req.getId() != null && existingById.containsKey(req.getId())) {
                    PartyContact existing = existingById.get(req.getId());
                    existing.setLabel(req.getLabel());
                    existing.setMobile(req.getMobile());
                    existing.setPhone(req.getPhone());
                    existing.setEmail(req.getEmail());
                    existing.setIsActive(Boolean.TRUE.equals(req.getIsActive()));
                    existing.setIsDefault(Boolean.TRUE.equals(req.getIsDefault()));
                    incomingIds.add(req.getId());
                } else {
                    PartyContact contact = mapper.toEntity(req);
                    contact.setIsActive(true);
                    contact.setIsDefault(Boolean.TRUE.equals(req.getIsDefault()));
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

    /**
     * Backend validation: each list may have at most 1 default.
     */
    private void validateSingleDefault(PartyRequest request) {
        checkSingleDefault(request.getIdentifications(), r -> Boolean.TRUE.equals(r.getIsDefault()),
                "msg.error.party.multiple-default.identification");
        checkSingleDefault(request.getAddresses(), r -> Boolean.TRUE.equals(r.getIsDefault()),
                "msg.error.party.multiple-default.address");
        checkSingleDefault(request.getContacts(), r -> Boolean.TRUE.equals(r.getIsDefault()),
                "msg.error.party.multiple-default.contact");
    }

    private <T> void checkSingleDefault(List<T> list, java.util.function.Predicate<T> isDefaultFn, String msgKey) {
        if (list == null)
            return;
        long count = list.stream().filter(isDefaultFn).count();
        if (count > 1) {
            throw new RuntimeException(getMessage(msgKey));
        }
    }

    private Party findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.party.notfound")));
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
