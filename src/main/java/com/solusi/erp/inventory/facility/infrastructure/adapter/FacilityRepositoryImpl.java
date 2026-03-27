package com.solusi.erp.inventory.facility.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.inventory.facility.domain.repository.FacilityRepository;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FacilityRepositoryImpl implements FacilityRepository {

    private final com.solusi.erp.inventory.repository.FacilityRepository jpaRepository;
    private final FacilityPersistenceMapper mapper;

    public FacilityRepositoryImpl(
            com.solusi.erp.inventory.repository.FacilityRepository jpaRepository,
            FacilityPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Facility save(Facility domain) {
        com.solusi.erp.inventory.model.Facility entity = mapper.toEntity(domain);
        com.solusi.erp.inventory.model.Facility saved = jpaRepository.saveAndFlush(entity);
        // Reload to ensure lazy associations (owner, address.city) are available
        return jpaRepository.findById(saved.getId()).map(mapper::toDomain)
            .orElseThrow(() -> new IllegalStateException("Failed to reload saved Facility"));
    }

    @Override
    public Optional<Facility> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Facility> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageRequest.of(pageable.page(), pageable.size());
        org.springframework.data.domain.Page<com.solusi.erp.inventory.model.Facility> springPage =
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
    public List<Facility> search(String keyword, int limit) {
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
