package com.solusi.erp.purchasing.supplierpricelist.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListEntity;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListJpaRepository;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SupplierPriceListRepositoryImpl implements SupplierPriceListRepository {

    private final SupplierPriceListJpaRepository jpaRepository;
    private final SupplierPriceListPersistenceMapper mapper;

    public SupplierPriceListRepositoryImpl(SupplierPriceListJpaRepository jpaRepository,
                                            SupplierPriceListPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SupplierPriceList save(SupplierPriceList domain) {
        SupplierPriceListEntity entity = mapper.toEntity(domain);
        SupplierPriceListEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SupplierPriceList> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<SupplierPriceList> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<SupplierPriceListEntity> springPage =
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
    public List<SupplierPriceList> search(String keyword, int limit) {
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
    public boolean existsOverlapping(Long supplierId, Long productId, Long uomId, Long currencyId,
                                      LocalDate effectiveFrom, LocalDate effectiveTo, Long excludeId) {
        return jpaRepository.existsOverlapping(supplierId, productId, uomId, currencyId,
            effectiveFrom, effectiveTo, excludeId);
    }
}
