package com.solusi.erp.inventory.product.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of GetProductLookupUseCase.
 * Pure Java.
 */
public class GetProductLookupUseCaseImpl implements GetProductLookupUseCase {

    private final ProductRepository productRepository;

    public GetProductLookupUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public LookupDto getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.product.not-found"));
        return new LookupDto(p.getId(), p.getName(), p.getCode(), Map.of(
            "isSerialized", p.isSerialized(),
            "uomId", p.getUomId()
        ));
    }

    @Override
    public List<LookupDto> search(String keyword, int limit) {
        Page<Product> page = productRepository.findAll(keyword, Pageable.of(0, limit));
        return page.content().stream()
                .map(p -> new LookupDto(p.getId(), p.getName(), p.getCode(), Map.of(
                    "isSerialized", p.isSerialized(),
                    "uomId", p.getUomId()
                )))
                .collect(Collectors.toList());
    }
}
