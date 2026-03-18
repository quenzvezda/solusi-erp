package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.dto.ProductRequest;
import com.solusi.erp.inventory.dto.ProductResponse;
import com.solusi.erp.inventory.mapper.ProductMapper;
import com.solusi.erp.inventory.model.Product;
import com.solusi.erp.inventory.repository.ProductRepository;
import com.solusi.erp.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Implementation of ProductService.
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(String keyword, Pageable pageable) {
        Page<Product> page;
        if (StringUtils.hasText(keyword)) {
            page = repository.search(keyword, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.product.notfound")));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRequest getEditData(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.product.notfound")));
        
        return mapper.toRequest(entity);
    }

    @Override
    @Transactional
    public void create(ProductRequest request) {
        Product entity = mapper.toEntity(request);
        
        // Auto-generate code
        String generatedCode = sequenceGeneratorService.generate("PRODUCT");
        entity.setCode(generatedCode);
        
        repository.save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, ProductRequest request) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.product.notfound")));

        // Validate unique code excluding current id
        if (StringUtils.hasText(request.getCode()) && repository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new RuntimeException(getMessage("msg.error.product.duplicate-code"));
        }

        mapper.updateEntityFromRequest(request, entity);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(getMessage("msg.error.product.notfound"));
        }
        repository.deleteById(id);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
