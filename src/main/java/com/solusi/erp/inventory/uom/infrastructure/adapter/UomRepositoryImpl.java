package com.solusi.erp.inventory.uom.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.util.PageableMapper;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of UomRepository.
 * Bridges Domain and Infrastructure Persistence.
 */
public class UomRepositoryImpl implements UomRepository {

    private final com.solusi.erp.inventory.repository.UnitOfMeasureRepository jpaRepository;
    private final UomPersistenceMapper mapper;

    public UomRepositoryImpl(
            com.solusi.erp.inventory.repository.UnitOfMeasureRepository jpaRepository,
            UomPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public UnitOfMeasure save(UnitOfMeasure domain) {
        com.solusi.erp.inventory.model.UnitOfMeasure entity = mapper.toEntity(domain);
        com.solusi.erp.inventory.model.UnitOfMeasure saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<UnitOfMeasure> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<UnitOfMeasure> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.inventory.model.UnitOfMeasure> springPage =
            (keyword != null && !keyword.isBlank())
                ? jpaRepository.search(keyword, springPageable)
                : jpaRepository.findAll(springPageable);
        return new Page<>(
            springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements()
        );
    }

    @Override
    public List<UnitOfMeasure> search(String keyword, int limit) {
        return jpaRepository.search(
                keyword != null ? keyword : "",
                PageRequest.of(0, limit)
            ).getContent().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return jpaRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public List<UnitOfMeasure> findByType(UomType type) {
        return jpaRepository.findByType(type).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
