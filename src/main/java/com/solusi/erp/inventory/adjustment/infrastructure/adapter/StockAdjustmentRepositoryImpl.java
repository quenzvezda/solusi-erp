package com.solusi.erp.inventory.adjustment.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.util.PageableMapper;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustmentLineItem;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;
import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentPersistenceMapper;
import com.solusi.erp.inventory.model.StockAdjustmentLine;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter: bridges domain StockAdjustmentRepository and JPA infrastructure.
 */
public class StockAdjustmentRepositoryImpl implements StockAdjustmentRepository {

    private final com.solusi.erp.inventory.repository.StockAdjustmentRepository jpaRepository;
    private final com.solusi.erp.inventory.repository.FacilityRepository facilityRepository;
    private final com.solusi.erp.master.repository.CurrencyRepository currencyRepository;
    private final com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository productRepository;
    private final com.solusi.erp.inventory.repository.ContainerRepository containerRepository;
    private final com.solusi.erp.inventory.repository.GridRepository gridRepository;
    private final com.solusi.erp.inventory.repository.UnitOfMeasureRepository uomRepository;
    private final StockAdjustmentPersistenceMapper mapper;

    public StockAdjustmentRepositoryImpl(
            com.solusi.erp.inventory.repository.StockAdjustmentRepository jpaRepository,
            com.solusi.erp.inventory.repository.FacilityRepository facilityRepository,
            com.solusi.erp.master.repository.CurrencyRepository currencyRepository,
            com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository productRepository,
            com.solusi.erp.inventory.repository.ContainerRepository containerRepository,
            com.solusi.erp.inventory.repository.GridRepository gridRepository,
            com.solusi.erp.inventory.repository.UnitOfMeasureRepository uomRepository,
            StockAdjustmentPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.facilityRepository = facilityRepository;
        this.currencyRepository = currencyRepository;
        this.productRepository = productRepository;
        this.containerRepository = containerRepository;
        this.gridRepository = gridRepository;
        this.uomRepository = uomRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<StockAdjustment> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.inventory.model.StockAdjustment> springPage =
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
        com.solusi.erp.inventory.model.StockAdjustment entity;
        Long domainId = domain.getMetadata().id();

        if (domainId == null) {
            entity = new com.solusi.erp.inventory.model.StockAdjustment();
        } else {
            entity = jpaRepository.findById(domainId)
                    .orElseThrow(() -> new RuntimeException("StockAdjustment not found: " + domainId));
        }

        entity.setCode(domain.getCode());
        entity.setTransactionDate(domain.getTransactionDate());
        entity.setStatus(com.solusi.erp.inventory.model.StockAdjustment.AdjustmentStatus.valueOf(domain.getStatus().name()));
        entity.setNote(domain.getNote());
        entity.setFacility(facilityRepository.getReferenceById(domain.getFacilityId()));

        entity.getTotalCost().setCurrency(currencyRepository.getReferenceById(domain.getCurrencyId()));
        entity.getTotalCost().setExchangeRate(domain.getExchangeRate());
        entity.getTotalCost().setOriginalAmount(domain.getTotalAmountOriginal());
        entity.getTotalCost().setLocalAmount(domain.getTotalAmountLocal());

        entity.getLines().clear();
        for (StockAdjustmentLineItem lineItem : domain.getLines()) {
            StockAdjustmentLine line = new StockAdjustmentLine();
            line.setHeader(entity);
            line.setProduct(productRepository.getReferenceById(lineItem.getProductId()));
            line.setContainer(containerRepository.getReferenceById(lineItem.getContainerId()));
            if (lineItem.getGridId() != null) {
                line.setGrid(gridRepository.getReferenceById(lineItem.getGridId()));
            }
            if (lineItem.getUomId() != null) {
                line.setUom(uomRepository.getReferenceById(lineItem.getUomId()));
            }
            line.setConversionFactor(lineItem.getConversionFactor());
            line.setQuantity(lineItem.getQuantity());
            line.setUnitCost(lineItem.getUnitCost());
            line.setTotalAmount(lineItem.getTotalAmount());
            line.setSerialNumber(lineItem.getSerialNumber());
            entity.getLines().add(line);
        }

        com.solusi.erp.inventory.model.StockAdjustment saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
