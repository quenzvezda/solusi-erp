package com.solusi.erp.inventory.container.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetContainerLookupUseCaseImpl implements GetContainerLookupUseCase {

    private final ContainerRepository repository;

    public GetContainerLookupUseCaseImpl(ContainerRepository repository) {
        this.repository = repository;
    }

    @Override
    public LookupDto getById(Long id) {
        Container container = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.container.notfound"));
        return toLookupDto(container);
    }

    @Override
    public List<LookupDto> search(String keyword, int limit) {
        return repository.search(keyword, limit).stream()
            .map(this::toLookupDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<LookupDto> search(String keyword, Long gridId, Long facilityId, int limit) {
        return repository.search(keyword, gridId, facilityId, limit).stream()
            .map(this::toLookupDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<LookupDto> findAll() {
        return repository.search("", 10000).stream()
            .map(this::toLookupDto)
            .collect(Collectors.toList());
    }

    private LookupDto toLookupDto(Container container) {
        return new LookupDto(container.getId(), container.getName(), container.getCode(),
            Map.of(
                "gridId", container.getGridId() != null ? container.getGridId() : 0L,
                "gridName", container.getGridName() != null ? container.getGridName() : ""
            )
        );
    }
}
