package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.dto.FacilityRequest;
import com.solusi.erp.inventory.dto.FacilityResponse;
import com.solusi.erp.inventory.mapper.WarehouseMapper;
import com.solusi.erp.inventory.model.Facility;
import com.solusi.erp.inventory.repository.FacilityRepository;
import com.solusi.erp.inventory.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    @Transactional
    public void create(FacilityRequest request) {
        Facility entity = mapper.toEntity(request);
        String code = sequenceGeneratorService.generate("FACILITY");
        entity.setCode(code);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, FacilityRequest request) {
        Facility entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.facility.notfound")));

        if (StringUtils.hasText(request.getCode()) && repository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new RuntimeException(getMessage("msg.error.facility.duplicate-code"));
        }

        mapper.updateEntityFromRequest(request, entity);
        repository.save(entity);
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
