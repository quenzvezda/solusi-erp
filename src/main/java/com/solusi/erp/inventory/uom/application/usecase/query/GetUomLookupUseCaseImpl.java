package com.solusi.erp.inventory.uom.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.uom.domain.model.UomType;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetUomLookupUseCaseImpl implements GetUomLookupUseCase {

    private final UomRepository repository;

    public GetUomLookupUseCaseImpl(UomRepository repository) {
        this.repository = repository;
    }

    @Override
    public LookupDto getById(Long id) {
        UnitOfMeasure uom = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.uom.notfound"));
        return toLookupDto(uom);
    }

    @Override
    public List<LookupDto> search(String keyword, int limit) {
        return repository.search(keyword, limit).stream()
            .map(this::toLookupDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<LookupDto> findByType(UomType type) {
        return repository.findByType(type).stream()
            .map(this::toLookupDto)
            .collect(Collectors.toList());
    }

    private LookupDto toLookupDto(UnitOfMeasure uom) {
        return new LookupDto(uom.getId(), uom.getName(), uom.getCode());
    }
}
