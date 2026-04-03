package com.solusi.erp.inventory.grid.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.grid.domain.repository.GridRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetGridLookupUseCaseImpl implements GetGridLookupUseCase {

    private final GridRepository repository;

    public GetGridLookupUseCaseImpl(GridRepository repository) {
        this.repository = repository;
    }

    @Override
    public LookupDto getById(Long id) {
        Grid grid = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.grid.notfound"));
        return toLookupDto(grid);
    }

    @Override
    public List<LookupDto> search(String keyword, int limit) {
        return repository.search(keyword, null, limit).stream()
            .map(this::toLookupDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<LookupDto> search(String keyword, Long facilityId, int limit) {
        return repository.search(keyword, facilityId, limit).stream()
            .map(this::toLookupDto)
            .collect(Collectors.toList());
    }

    private LookupDto toLookupDto(Grid grid) {
        return new LookupDto(grid.getId(), grid.getName(), grid.getCode());
    }
}
