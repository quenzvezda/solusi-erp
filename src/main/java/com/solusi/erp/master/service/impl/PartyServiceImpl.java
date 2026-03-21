package com.solusi.erp.master.service.impl;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.master.dto.*;
import com.solusi.erp.master.mapper.PartyMapper;
import com.solusi.erp.master.model.*;
import com.solusi.erp.master.repository.GeographicRepository;
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
    private final GeographicRepository geographicRepository;
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

        PartyRequest request = mapper.toRequest(entity);
        
        // Map Collections using mapper methods to ensure audit data is carried over
        request.setIdentifications(entity.getIdentifications().stream()
                .map(mapper::toRequest)
                .collect(Collectors.toList()));
        
        request.setAddresses(entity.getAddresses().stream()
                .map(mapper::toRequest)
                .collect(Collectors.toList()));
        
        request.setContacts(entity.getContacts().stream()
                .map(mapper::toRequest)
                .collect(Collectors.toList()));

        return request;
    }

    @Override
    @Transactional(readOnly = true)
    public FormViewDto<PartyRequest, Void, PartyResponse> getPartyEditView(Long id) {
        Party entity = findOrThrow(id);
        
        PartyRequest request = getEditData(id); // reuse existing logic for request
        
        return FormViewDto.<PartyRequest, Void, PartyResponse>builder()
                .request(request)
                .ui(null)
                .audit(mapper.toResponse(entity))
                .build();
    }

    @Override
    @Transactional
    public PartyResponse create(PartyRequest request) {
        validateSingleDefault(request);

        Party entity = mapper.toEntity(request);
        entity.setCode(sequenceGeneratorService.generate("PARTY"));

        syncRoles(entity, request.getRoleIds());
        syncIdentifications(entity, request.getIdentifications());
        syncAddresses(entity, request.getAddresses());
        syncContacts(entity, request.getContacts());

        Party saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PartyResponse update(Long id, PartyRequest request) {
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

        Party updated = repository.save(entity);
        return mapper.toResponse(updated);
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
                    existing.setTypes(req.getTypes() != null ? new HashSet<>(req.getTypes()) : new HashSet<>());
                    existing.setAddressLine1(req.getAddressLine1());
                    existing.setPostalCode(req.getPostalCode());
                    existing.setIsActive(Boolean.TRUE.equals(req.getIsActive()));
                    existing.setIsDefault(Boolean.TRUE.equals(req.getIsDefault()));
                    if (req.getCityId() != null) {
                        geographicRepository.findById(req.getCityId()).ifPresent(existing::setCity);
                    } else {
                        existing.setCity(null);
                    }
                    incomingIds.add(req.getId());
                } else {
                    PartyAddress addr = mapper.toEntity(req);
                    addr.setTypes(req.getTypes() != null ? new HashSet<>(req.getTypes()) : new HashSet<>());
                    addr.setIsActive(true);
                    addr.setIsDefault(Boolean.TRUE.equals(req.getIsDefault()));
                    if (req.getCityId() != null) {
                        geographicRepository.findById(req.getCityId()).ifPresent(addr::setCity);
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
