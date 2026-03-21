package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.dto.FacilityRequest;
import com.solusi.erp.inventory.dto.FacilityResponse;
import com.solusi.erp.inventory.form.FacilityUIForm;
import com.solusi.erp.inventory.mapper.WarehouseMapper;
import com.solusi.erp.inventory.model.Facility;
import com.solusi.erp.inventory.repository.FacilityRepository;
import com.solusi.erp.inventory.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Implementation of FacilityService.
 */
@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository repository;
    private final WarehouseMapper mapper;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public LookupDto getLookupFacility(Long id) {
        Facility f = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.facility.notfound")));
        return new LookupDto(f.getId(), f.getName(), f.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacilityResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupDto> lookupFacilities(String keyword, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return repository.search(keyword, pageable).getContent().stream()
                .map(f -> new LookupDto(f.getId(), f.getName(), f.getCode()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FacilityResponse> findAll(String keyword, Pageable pageable) {
        Page<Facility> page;
        if (StringUtils.hasText(keyword)) {
            page = repository.search(keyword, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FacilityResponse findById(Long id) {
        Facility entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.facility.notfound")));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FacilityRequest getEditData(Long id) {
        Facility entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.facility.notfound")));
        return mapper.toRequest(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FormViewDto<FacilityRequest, FacilityUIForm, FacilityResponse> getFormView(Long id) {
        Facility entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.facility.notfound")));

        return FormViewDto.<FacilityRequest, com.solusi.erp.inventory.form.FacilityUIForm, FacilityResponse>builder()
                .request(mapper.toRequest(entity))
                .ui(mapper.toFacilityUIForm(entity))
                .audit(mapper.toResponse(entity))
                .build();
    }

    @Override
    @Transactional
    public FacilityResponse create(FacilityRequest request) {
        Facility entity = mapper.toEntity(request);
        String code = sequenceGeneratorService.generate("FACILITY");
        entity.setCode(code);
        Facility savedEntity = repository.save(entity);
        return mapper.toResponse(savedEntity);
    }

    @Override
    @Transactional
    public FacilityResponse update(Long id, FacilityRequest request) {
        Facility entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.facility.notfound")));

        if (StringUtils.hasText(request.getCode()) && repository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new RuntimeException(getMessage("msg.error.facility.duplicate-code"));
        }

        mapper.updateEntityFromRequest(request, entity);
        Facility updatedEntity = repository.save(entity);
        return mapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(getMessage("msg.error.facility.notfound"));
        }
        repository.deleteById(id);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
