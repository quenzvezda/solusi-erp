package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.dto.UnitOfMeasureRequest;
import com.solusi.erp.inventory.dto.UnitOfMeasureResponse;
import com.solusi.erp.inventory.mapper.UnitOfMeasureMapper;
import com.solusi.erp.inventory.model.UnitOfMeasure;
import com.solusi.erp.inventory.repository.UnitOfMeasureRepository;
import com.solusi.erp.inventory.service.UnitOfMeasureService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UnitOfMeasureService.
 */
@Service
@RequiredArgsConstructor
public class UnitOfMeasureServiceImpl implements UnitOfMeasureService {

    private final UnitOfMeasureRepository repository;
    private final UnitOfMeasureMapper mapper;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public Page<UnitOfMeasureResponse> findAll(String keyword, Pageable pageable) {
        Page<UnitOfMeasure> page;
        if (StringUtils.hasText(keyword)) {
            page = repository.search(keyword, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitOfMeasureResponse> findByType(UomType type) {
        return repository.findByType(type).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UnitOfMeasureResponse findById(Long id) {
        UnitOfMeasure entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.uom.notfound")));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitOfMeasureRequest getEditData(Long id) {
        UnitOfMeasure entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.uom.notfound")));
        
        return mapper.toRequest(entity);
    }

    @Override
    @Transactional
    public void create(UnitOfMeasureRequest request) {
        if (repository.existsByCode(request.getCode())) {
            throw new RuntimeException(getMessage("msg.error.uom.duplicate-code"));
        }
        
        UnitOfMeasure entity = mapper.toEntity(request);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, UnitOfMeasureRequest request) {
        UnitOfMeasure entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.uom.notfound")));

        if (repository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new RuntimeException(getMessage("msg.error.uom.duplicate-code"));
        }

        mapper.updateEntityFromRequest(request, entity);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(getMessage("msg.error.uom.notfound"));
        }
        repository.deleteById(id);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
