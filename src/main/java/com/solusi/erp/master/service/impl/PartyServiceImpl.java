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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        Page<Party> page;
        if (StringUtils.hasText(keyword)) {
            page = repository.search(keyword, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PartyResponse findById(Long id) {
        Party entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.party.notfound")));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PartyRequest getEditData(Long id) {
        Party entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.party.notfound")));
        
        PartyRequest request = PartyRequest.builder()
                .id(entity.getId())
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
                                .build())
                        .collect(Collectors.toList()))
                .build();
        
        return request;
    }

    @Override
    @Transactional
    public void create(PartyRequest request) {
        Party entity = mapper.toEntity(request);
        
        // Auto-generate code
        entity.setCode(sequenceGeneratorService.generate("PARTY"));
        
        syncRoles(entity, request.getRoleIds());
        syncIdentifications(entity, request.getIdentifications());
        syncAddresses(entity, request.getAddresses());
        
        repository.save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, PartyRequest request) {
        Party entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.party.notfound")));

        if (StringUtils.hasText(request.getCode()) && repository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new RuntimeException(getMessage("msg.error.party.duplicate-code"));
        }

        mapper.updateEntityFromRequest(request, entity);
        
        syncRoles(entity, request.getRoleIds());
        syncIdentifications(entity, request.getIdentifications());
        syncAddresses(entity, request.getAddresses());
        
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

    private void syncRoles(Party entity, Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            entity.setRoles(new HashSet<>());
            return;
        }
        List<PartyRoleType> roles = roleTypeRepository.findAllById(roleIds);
        entity.setRoles(new HashSet<>(roles));
    }

    private void syncIdentifications(Party entity, List<PartyIdentificationRequest> requests) {
        entity.getIdentifications().clear();
        if (requests != null) {
            for (PartyIdentificationRequest req : requests) {
                if (!StringUtils.hasText(req.getIdNumber())) continue;
                
                PartyIdentification iden = new PartyIdentification();
                iden.setIdNumber(req.getIdNumber());
                iden.setIssuedDate(req.getIssuedDate());
                iden.setExpiryDate(req.getExpiryDate());
                iden.setType(idTypeRepository.findById(req.getTypeId()).orElse(null));
                entity.addIdentification(iden);
            }
        }
    }

    private void syncAddresses(Party entity, List<PartyAddressRequest> requests) {
        entity.getAddresses().clear();
        if (requests != null) {
            for (PartyAddressRequest req : requests) {
                if (!StringUtils.hasText(req.getAddressLine1())) continue;
                
                PartyAddress addr = mapper.toEntity(req);
                entity.addAddress(addr);
            }
        }
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
