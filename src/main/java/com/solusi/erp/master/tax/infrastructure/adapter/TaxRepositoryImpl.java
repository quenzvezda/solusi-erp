package com.solusi.erp.master.tax.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.util.PageableMapper;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.domain.repository.TaxRepository;
import com.solusi.erp.master.tax.infrastructure.persistence.TaxPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of TaxRepository.
 * Bridges Domain and Infrastructure Persistence.
 */
public class TaxRepositoryImpl implements TaxRepository {

    private final com.solusi.erp.master.repository.TaxRepository jpaRepository;
    private final TaxPersistenceMapper mapper;

    public TaxRepositoryImpl(
            com.solusi.erp.master.repository.TaxRepository jpaRepository,
            TaxPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Tax save(Tax domain) {
        com.solusi.erp.master.model.Tax entity = mapper.toEntity(domain);
        com.solusi.erp.master.model.Tax saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Tax> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Tax> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.master.model.Tax> springPage =
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
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.findByCode(code).isPresent();
    }
}
