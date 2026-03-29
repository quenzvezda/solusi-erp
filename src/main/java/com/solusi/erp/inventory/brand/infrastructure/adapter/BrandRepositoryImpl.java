package com.solusi.erp.inventory.brand.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;
import com.solusi.erp.inventory.brand.infrastructure.persistence.BrandPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of BrandRepository.
 * Bridges Domain and Infrastructure Persistence.
 */
public class BrandRepositoryImpl implements BrandRepository {

    private final com.solusi.erp.inventory.repository.BrandRepository jpaRepository;
    private final BrandPersistenceMapper mapper;

    public BrandRepositoryImpl(
            com.solusi.erp.inventory.repository.BrandRepository jpaRepository,
            BrandPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Brand save(Brand domain) {
        com.solusi.erp.inventory.model.Brand entity = mapper.toEntity(domain);
        com.solusi.erp.inventory.model.Brand saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Brand> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Brand> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.inventory.model.Brand> springPage =
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
    public List<Brand> search(String keyword, int limit) {
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
}
