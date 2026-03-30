package com.solusi.erp.inventory.adjustment.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustmentLineItem;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;
import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentEntity;
import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentJpaRepository;
import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentLineEntity;
import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentPersistenceMapper;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter: bridges domain StockAdjustmentRepository and JPA infrastructure.
 */
public class StockAdjustmentRepositoryImpl implements StockAdjustmentRepository {

    private final StockAdjustmentJpaRepository jpaRepository;
    private final StockAdjustmentPersistenceMapper mapper;

    public StockAdjustmentRepositoryImpl(
            StockAdjustmentJpaRepository jpaRepository,
            StockAdjustmentPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<StockAdjustment> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<StockAdjustmentEntity> springPage =
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
    public Optional<StockAdjustment> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public StockAdjustment save(StockAdjustment domain) {
        StockAdjustmentEntity entity;
        Long domainId = domain.getMetadata().id();

        if (domainId == null) {
            entity = new StockAdjustmentEntity();
        } else {
            entity = jpaRepository.findById(domainId)
                    .orElseThrow(() -> new RuntimeException("StockAdjustment not found: " + domainId));
        }

        entity.setCode(domain.getCode());
        entity.setTransactionDate(domain.getTransactionDate());
        entity.setStatus(domain.getStatus());
        entity.setNote(domain.getNote());
        entity.setFacilityId(domain.getFacilityId());

        entity.getTotalCost().setCurrencyId(domain.getCurrencyId());
        entity.getTotalCost().setExchangeRate(domain.getExchangeRate());
        entity.getTotalCost().setOriginalAmount(domain.getTotalAmountOriginal());
        entity.getTotalCost().setLocalAmount(domain.getTotalAmountLocal());

        entity.getLines().clear();
        for (StockAdjustmentLineItem lineItem : domain.getLines()) {
            StockAdjustmentLineEntity line = new StockAdjustmentLineEntity();
            line.setHeader(entity);
            line.setProductId(lineItem.getProductId());
            line.setContainerId(lineItem.getContainerId());
            line.setGridId(lineItem.getGridId());
            line.setUomId(lineItem.getUomId());
            line.setConversionFactor(lineItem.getConversionFactor());
            line.setQuantity(lineItem.getQuantity());
            line.setUnitCost(lineItem.getUnitCost());
            line.setTotalAmount(lineItem.getTotalAmount());
            line.setSerialNumber(lineItem.getSerialNumber());
            entity.getLines().add(line);
        }

        StockAdjustmentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}

