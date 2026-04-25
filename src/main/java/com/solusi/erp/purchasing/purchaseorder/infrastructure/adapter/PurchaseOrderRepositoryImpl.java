package com.solusi.erp.purchasing.purchaseorder.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PurchaseOrderEntity;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PurchaseOrderJpaRepository;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PurchaseOrderLineEntity;
import com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PurchaseOrderPersistenceMapper;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PurchaseOrderRepositoryImpl implements PurchaseOrderRepository {

    private final PurchaseOrderJpaRepository jpaRepository;
    private final PurchaseOrderPersistenceMapper mapper;

    public PurchaseOrderRepositoryImpl(PurchaseOrderJpaRepository jpaRepository,
                                       PurchaseOrderPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PurchaseOrder save(PurchaseOrder domain) {
        PurchaseOrderEntity entity = mapper.toEntity(domain);

        entity.getLines().clear();
        List<PurchaseOrderLineEntity> lineEntities =
                mapper.toLineEntityList(domain.getLines(), entity);
        entity.getLines().addAll(lineEntities);

        PurchaseOrderEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PurchaseOrder> findById(Long id) {
        return jpaRepository.findByIdWithLines(id).map(mapper::toDomain);
    }

    @Override
    public Page<PurchaseOrder> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<PurchaseOrderEntity> springPage =
            (keyword != null && !keyword.isBlank())
                ? jpaRepository.search(keyword, springPageable)
                : jpaRepository.findAllWithLines(springPageable);
        return new Page<>(
            springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements()
        );
    }

    @Override
    public Map<Long, BigDecimal> sumCommittedQuantityByPrLineIds(Set<Long> prLineIds) {
        if (prLineIds == null || prLineIds.isEmpty()) {
            return Map.of();
        }
        return jpaRepository.sumConsumedByPrLineIds(
                prLineIds,
                EnumSet.of(
                        com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus.SUBMITTED,
                        com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus.APPROVED,
                        com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus.SENT,
                        com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus.PARTIALLY_RECEIVED,
                        com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus.FULLY_RECEIVED,
                        com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus.BILLED,
                        com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus.CLOSED
                )
        ).stream().collect(Collectors.toMap(
                row -> row.prLineId(),
                row -> row.consumedQuantity()
        ));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
