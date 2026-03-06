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
        
        return ProductRequest.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .barcode(entity.getBarcode())
                .note(entity.getNote())
                .categoryId(entity.getCategory().getId())
                .uomId(entity.getUom().getId())
                .brandId(entity.getBrand() != null ? entity.getBrand().getId() : null)
                .hscode(entity.getHscode())
                .isActive(entity.getIsActive())
                .isSerialized(entity.getIsSerialized())
                .minStock(entity.getMinStock())
                .maxStock(entity.getMaxStock())
                .weightNet(entity.getWeightNet())
                .weightGross(entity.getWeightGross())
                .weightUomId(entity.getWeightUom() != null ? entity.getWeightUom().getId() : null)
                .length(entity.getLength())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .dimensionUomId(entity.getDimensionUom() != null ? entity.getDimensionUom().getId() : null)
                .build();
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
