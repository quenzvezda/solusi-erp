package com.solusi.erp.inventory.brand.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetBrandLookupUseCaseImpl implements GetBrandLookupUseCase {

    private final BrandRepository repository;

    public GetBrandLookupUseCaseImpl(BrandRepository repository) {
        this.repository = repository;
    }

    @Override
    public LookupDto getById(Long id) {
        Brand brand = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.brand.notfound"));
        return toLookupDto(brand);
    }

    @Override
    public List<LookupDto> search(String keyword, int limit) {
        return repository.search(keyword, limit).stream()
            .map(this::toLookupDto)
            .collect(Collectors.toList());
    }

    private LookupDto toLookupDto(Brand brand) {
        return new LookupDto(brand.getId(), brand.getName(), brand.getCode());
    }
}
