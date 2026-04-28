package com.solusi.erp.accounting.coa.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaJpaRepository;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CoaRepositoryImpl implements CoaRepository {

    private final CoaJpaRepository jpaRepository;
    private final CoaPersistenceMapper mapper;

    public CoaRepositoryImpl(CoaJpaRepository jpaRepository, CoaPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ChartOfAccount save(ChartOfAccount domain) {
        var entity = mapper.toEntity(domain);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ChartOfAccount> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<ChartOfAccount> findAll(String keyword, Pageable pageable) {
        var springPageable = PageableMapper.toSpring(pageable);
        var springPage = (keyword != null && !keyword.isBlank())
                ? jpaRepository.search(keyword, springPageable)
                : jpaRepository.findAllOrdered(springPageable);
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

    @Override
    public List<ChartOfAccount> search(String keyword, int limit) {
        return jpaRepository.searchForLookup(
                keyword != null ? keyword : "",
                PageRequest.of(0, limit)
        ).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ChartOfAccount> findAllActive() {
        return jpaRepository.findAllForSelector()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
