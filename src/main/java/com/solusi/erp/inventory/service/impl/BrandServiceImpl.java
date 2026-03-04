package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.dto.BrandRequest;
import com.solusi.erp.inventory.dto.BrandResponse;
import com.solusi.erp.inventory.mapper.BrandMapper;
import com.solusi.erp.inventory.model.Brand;
import com.solusi.erp.inventory.repository.BrandRepository;
import com.solusi.erp.inventory.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Implementation of BrandService.
 */
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository repository;
    private final BrandMapper mapper;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> findAll(String keyword, Pageable pageable) {
        Page<Brand> page;
        if (StringUtils.hasText(keyword)) {
            page = repository.search(keyword, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse findById(Long id) {
        Brand entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.brand.notfound")));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandRequest getEditData(Long id) {
        Brand entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.brand.notfound")));
        
        return BrandRequest.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .note(entity.getNote())
                .build();
    }

    @Override
    @Transactional
    public void create(BrandRequest request) {
        Brand entity = mapper.toEntity(request);
        
        // Auto-generate code
        String generatedCode = sequenceGeneratorService.generate("BRAND");
        entity.setCode(generatedCode);
        
        repository.save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, BrandRequest request) {
        Brand entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.brand.notfound")));

        // Validate unique code excluding current id
        if (StringUtils.hasText(request.getCode()) && repository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new RuntimeException(getMessage("msg.error.brand.duplicate-code"));
        }

        mapper.updateEntityFromRequest(request, entity);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(getMessage("msg.error.brand.notfound"));
        }
        repository.deleteById(id);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
