package com.solusi.erp.inventory.productcategory.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetProductCategoryLookupUseCaseImpl implements GetProductCategoryLookupUseCase {
    private final ProductCategoryRepository repository;

    public GetProductCategoryLookupUseCaseImpl(ProductCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public LookupDto getById(Long id) {
        ProductCategory category = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.product-category.notfound"));
        return toLookupDto(category);
    }

    @Override
    public List<LookupDto> search(String keyword, int limit) {
        return repository.search(keyword, limit).stream()
            .map(this::toLookupDto)
            .collect(Collectors.toList());
    }

    private LookupDto toLookupDto(ProductCategory c) {
        return new LookupDto(c.getId(), c.getName(), c.getCode(), Map.of("type", c.getType().name()));
    }
}
