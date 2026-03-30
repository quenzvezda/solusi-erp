package com.solusi.erp.inventory.uomconversion.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;
import com.solusi.erp.inventory.uomconversion.infrastructure.persistence.UomConversionEntity;
import com.solusi.erp.inventory.uomconversion.infrastructure.persistence.UomConversionJpaRepository;
import com.solusi.erp.inventory.uomconversion.infrastructure.persistence.UomConversionPersistenceMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UomConversionRepositoryImpl implements UomConversionRepository {

    private final UomConversionJpaRepository jpaRepo;
    private final UomConversionPersistenceMapper mapper;

    public UomConversionRepositoryImpl(UomConversionJpaRepository jpaRepo,
                                        UomConversionPersistenceMapper mapper) {
        this.jpaRepo = jpaRepo;
        this.mapper = mapper;
    }

    @Override
    public UomConversion save(UomConversion domain) {
        UomConversionEntity entity;
        if (domain.getMetadata().id() == null) {
            entity = new UomConversionEntity();
        } else {
            entity = jpaRepo.findById(domain.getMetadata().id())
                .orElseThrow(() -> new RuntimeException("UomConversion not found: " + domain.getMetadata().id()));
        }

        entity.setProductId(domain.getProductId());
        entity.setFromUomId(domain.getFromUomId());
        entity.setToUomId(domain.getToUomId());
        entity.setConversionFactor(domain.getConversionFactor());

        return mapper.toDomain(jpaRepo.save(entity));
    }

    @Override
    public Optional<UomConversion> findById(Long id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<UomConversion> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<UomConversionEntity> springPage =
            jpaRepo.search(keyword, springPageable);
        return new Page<>(
            springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements()
        );
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepo.existsById(id);
    }

    @Override
    public boolean existsByProductIdAndFromUomId(Long productId, Long fromUomId) {
        return jpaRepo.existsByProductIdAndFromUomId(productId, fromUomId);
    }

    @Override
    public boolean existsByProductIdAndFromUomIdAndIdNot(Long productId, Long fromUomId, Long id) {
        return jpaRepo.existsByProductIdAndFromUomIdAndIdNot(productId, fromUomId, id);
    }

    @Override
    public List<UomConversion> findByProductId(Long productId) {
        return jpaRepo.findByProductId(productId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
