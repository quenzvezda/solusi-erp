package com.solusi.erp.inventory.product.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of GetProductLookupUseCase.
 * Pure Java.
 */
public class GetProductLookupUseCaseImpl implements GetProductLookupUseCase {

    private final ProductRepository productRepository;
    private final UomJpaRepository uomRepository;

    public GetProductLookupUseCaseImpl(ProductRepository productRepository, UomJpaRepository uomRepository) {
        this.productRepository = productRepository;
        this.uomRepository = uomRepository;
    }

    @Override
    public LookupDto getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.product.not-found"));
        return new LookupDto(p.getId(), p.getName(), p.getCode(), buildPayload(p));
    }

    @Override
    public List<LookupDto> search(String keyword, int limit) {
        Page<Product> page = productRepository.findAll(keyword, Pageable.of(0, limit));
        return page.content().stream()
                .map(p -> new LookupDto(p.getId(), p.getName(), p.getCode(), buildPayload(p)))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildPayload(Product product) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("isSerialized", product.isSerialized());
        payload.put("uomId", product.getUomId());

        if (product.getUomId() != null) {
            uomRepository.findById(product.getUomId()).ifPresent(uom -> {
                payload.put("uomName", uom.getName());
                payload.put("uomCode", uom.getCode());
            });
        }

        return payload;
    }
}
