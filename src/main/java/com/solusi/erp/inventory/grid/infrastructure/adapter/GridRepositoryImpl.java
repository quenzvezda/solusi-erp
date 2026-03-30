package com.solusi.erp.inventory.grid.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.grid.domain.repository.GridRepository;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridEntity;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GridRepositoryImpl implements GridRepository {

    private final GridJpaRepository jpaRepository;
    private final GridPersistenceMapper mapper;

    public GridRepositoryImpl(
            GridJpaRepository jpaRepository,
            GridPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Grid save(Grid domain) {
        GridEntity entity = mapper.toEntity(domain);
        GridEntity saved = jpaRepository.saveAndFlush(entity);
        return jpaRepository.findById(saved.getId()).map(mapper::toDomain)
            .orElseThrow(() -> new IllegalStateException("Failed to reload saved Grid"));
    }

    @Override
    public Optional<Grid> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Grid> findAll(String keyword, Long facilityId, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<GridEntity> springPage;
        if (keyword != null && !keyword.isBlank() && facilityId != null) {
            springPage = jpaRepository.searchByFacility(keyword, facilityId, springPageable);
        } else if (keyword != null && !keyword.isBlank()) {
            springPage = jpaRepository.search(keyword, springPageable);
        } else if (facilityId != null) {
            springPage = jpaRepository.findByFacilityId(facilityId, springPageable);
        } else {
            springPage = jpaRepository.findAll(springPageable);
        }
        return new Page<>(
            springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements()
        );
    }

    @Override
    public List<Grid> search(String keyword, Long facilityId, int limit) {
        org.springframework.data.domain.Pageable springPageable = PageRequest.of(0, limit);
        if (facilityId != null) {
            return jpaRepository.searchByFacility(keyword != null ? keyword : "", facilityId, springPageable)
                .getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
        }
        return jpaRepository.search(keyword != null ? keyword : "", springPageable)
            .getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByFacilityIdAndCode(Long facilityId, String code) {
        return jpaRepository.existsByFacilityIdAndCode(facilityId, code);
    }

    @Override
    public boolean existsByFacilityIdAndCodeAndIdNot(Long facilityId, String code, Long id) {
        return jpaRepository.existsByFacilityIdAndCodeAndIdNot(facilityId, code, id);
    }
}
