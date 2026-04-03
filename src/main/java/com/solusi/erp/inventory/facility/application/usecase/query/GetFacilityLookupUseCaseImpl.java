package com.solusi.erp.inventory.facility.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.inventory.facility.domain.repository.FacilityRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetFacilityLookupUseCaseImpl implements GetFacilityLookupUseCase {

    private final FacilityRepository repository;

    public GetFacilityLookupUseCaseImpl(FacilityRepository repository) {
        this.repository = repository;
    }

    @Override
    public LookupDto getById(Long id) {
        Facility facility = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.facility.notfound"));
        return toLookupDto(facility);
    }

    @Override
    public List<LookupDto> search(String keyword, int limit) {
        return repository.search(keyword, limit).stream()
            .map(this::toLookupDto)
            .collect(Collectors.toList());
    }

    private LookupDto toLookupDto(Facility facility) {
        return new LookupDto(facility.getId(), facility.getName(), facility.getCode());
    }
}
