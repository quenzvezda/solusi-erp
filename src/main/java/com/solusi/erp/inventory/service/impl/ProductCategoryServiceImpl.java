package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.dto.ProductCategoryRequest;
import com.solusi.erp.inventory.dto.ProductCategoryResponse;
import com.solusi.erp.inventory.mapper.ProductCategoryMapper;
import com.solusi.erp.inventory.model.ProductCategory;
import com.solusi.erp.inventory.repository.ProductCategoryRepository;
import com.solusi.erp.inventory.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of ProductCategoryService.
 */
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository repository;
    private final ProductCategoryMapper mapper;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductCategoryResponse> findAll(String keyword, Pageable pageable) {
        Page<ProductCategory> page;
        if (StringUtils.hasText(keyword)) {
            page = repository.search(keyword, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCategoryResponse findById(Long id) {
        ProductCategory entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.product-category.notfound")));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCategoryRequest getEditData(Long id) {
        ProductCategory entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.product-category.notfound")));
        
        return mapper.toRequest(entity);
    }

    @Override
    @Transactional
    public void create(ProductCategoryRequest request) {
        ProductCategory entity = mapper.toEntity(request);
        
        // Auto-generate code
        String generatedCode = sequenceGeneratorService.generate("PRODUCT_CATEGORY");
        entity.setCode(generatedCode);
        
        repository.save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, ProductCategoryRequest request) {
        ProductCategory entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.product-category.notfound")));

        // Validate unique code excluding current id if user provided a custom code (optional, currently readonly in UI)
        if (StringUtils.hasText(request.getCode()) && repository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new RuntimeException(getMessage("msg.error.product-category.duplicate-code"));
        }

        mapper.updateEntityFromRequest(request, entity);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(getMessage("msg.error.product-category.notfound"));
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupDto> lookupCategories(String keyword, int limit) {
        // We use Pageable.ofSize(limit) or simple find with keyword
        return repository.search(keyword, Pageable.ofSize(limit))
                .getContent()
                .stream()
                .map(this::mapToLookupDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LookupDto getLookupCategory(Long id) {
        ProductCategory entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.product-category.notfound")));
        return mapToLookupDto(entity);
    }

    private LookupDto mapToLookupDto(ProductCategory entity) {
        return new LookupDto(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                Map.of("type", entity.getType().name())
        );
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
