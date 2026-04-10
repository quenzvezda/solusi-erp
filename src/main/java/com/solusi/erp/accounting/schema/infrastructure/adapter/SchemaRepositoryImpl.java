package com.solusi.erp.accounting.schema.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaJpaRepository;
import com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaPersistenceMapper;

import java.util.Optional;
import java.util.stream.Collectors;

public class SchemaRepositoryImpl implements SchemaRepository {

    private final SchemaJpaRepository jpaRepository;
    private final SchemaPersistenceMapper mapper;

    public SchemaRepositoryImpl(SchemaJpaRepository jpaRepository, SchemaPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public AccountingSchema save(AccountingSchema domain) {
        var entity = mapper.toEntity(domain);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AccountingSchema> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<AccountingSchema> findAll(String keyword, Pageable pageable) {
        var springPageable = PageableMapper.toSpring(pageable);
        var springPage = (keyword != null && !keyword.isBlank())
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
    public boolean existsByEventTypeAndIsActiveTrue(SchemaEventType eventType) {
        return jpaRepository.existsByEventTypeAndIsActiveTrue(eventType.name());
    }

    @Override
    public Optional<AccountingSchema> findByEventTypeAndIsActiveTrue(SchemaEventType eventType) {
        return jpaRepository.findByEventTypeAndIsActiveTrue(eventType.name()).map(mapper::toDomain);
    }
}
