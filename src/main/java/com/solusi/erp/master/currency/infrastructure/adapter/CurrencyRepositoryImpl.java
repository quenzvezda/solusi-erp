package com.solusi.erp.master.currency.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.util.PageableMapper;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;
import com.solusi.erp.master.currency.infrastructure.persistence.CurrencyPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of CurrencyRepository.
 * Bridges Domain and Infrastructure Persistence.
 */
public class CurrencyRepositoryImpl implements CurrencyRepository {

    private final com.solusi.erp.master.repository.CurrencyRepository jpaRepository;
    private final CurrencyPersistenceMapper mapper;

    public CurrencyRepositoryImpl(
            com.solusi.erp.master.repository.CurrencyRepository jpaRepository,
            CurrencyPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Currency save(Currency domain) {
        com.solusi.erp.master.model.Currency entity = mapper.toEntity(domain);
        com.solusi.erp.master.model.Currency saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Currency> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Currency> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.master.model.Currency> springPage =
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
    public boolean existsByAlias(String alias) {
        return jpaRepository.findByAlias(alias).isPresent();
    }

    @Override
    public List<Currency> findByIsDefaultTrue() {
        return jpaRepository.findByIsDefaultTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Currency> findByIsActiveTrue() {
        return jpaRepository.findByIsActiveTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
