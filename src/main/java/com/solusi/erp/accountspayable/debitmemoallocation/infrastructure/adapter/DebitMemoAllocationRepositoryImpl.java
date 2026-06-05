package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.adapter;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationHistory;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationEntity;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationJpaRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationLineEntity;
import com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence.DebitMemoAllocationPersistenceMapper;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DebitMemoAllocationRepositoryImpl implements DebitMemoAllocationRepository {

    private final DebitMemoAllocationJpaRepository jpaRepository;
    private final DebitMemoAllocationPersistenceMapper mapper;

    public DebitMemoAllocationRepositoryImpl(DebitMemoAllocationJpaRepository jpaRepository,
                                             DebitMemoAllocationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DebitMemoAllocation save(DebitMemoAllocation allocation) {
        DebitMemoAllocationEntity entity = mapper.toEntity(allocation);
        entity.getLines().clear();
        List<DebitMemoAllocationLineEntity> lineEntities = mapper.toLineEntityList(allocation.getLines(), entity);
        entity.getLines().addAll(lineEntities);
        DebitMemoAllocationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<DebitMemoAllocation> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<DebitMemoAllocation> findAll(String keyword,
                                             Long debitMemoId,
                                             DebitMemoAllocationStatus status,
                                             LocalDate allocationDateFrom,
                                             LocalDate allocationDateTo,
                                             Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        org.springframework.data.domain.Page<DebitMemoAllocationEntity> springPage = jpaRepository.findAllFiltered(
                normalizedKeyword, debitMemoId, status, allocationDateFrom, allocationDateTo, springPageable);
        return new Page<>(
                springPage.getContent().stream().map(mapper::toDomain).toList(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public boolean existsActiveConsumptionByDebitMemoId(Long debitMemoId) {
        return jpaRepository.existsActiveConsumptionByDebitMemoId(debitMemoId);
    }

    @Override
    public BigDecimal sumConfirmedAppliedByDebitMemoId(Long debitMemoId) {
        BigDecimal value = jpaRepository.sumConfirmedAppliedByDebitMemoId(debitMemoId);
        return value == null ? BigDecimal.ZERO : value;
    }

    @Override
    public Map<Long, BigDecimal> sumConfirmedAppliedByDebitMemoIds(Collection<Long> debitMemoIds) {
        if (debitMemoIds == null || debitMemoIds.isEmpty()) {
            return Map.of();
        }
        return jpaRepository.sumConfirmedAppliedByDebitMemoIds(debitMemoIds).stream()
                .collect(Collectors.toMap(
                        DebitMemoAllocationJpaRepository.DebitMemoAllocationSumProjection::getDebitMemoId,
                        projection -> projection.getAppliedAmount() == null ? BigDecimal.ZERO : projection.getAppliedAmount()
                ));
    }

    @Override
    public List<DebitMemoAllocationHistory> findHistoryByDebitMemoId(Long debitMemoId) {
        return jpaRepository.findHistoryByDebitMemoId(debitMemoId).stream()
                .map(mapper::toDomain)
                .flatMap(allocation -> toHistory(allocation, (Long) null).stream())
                .toList();
    }

    @Override
    public List<DebitMemoAllocationHistory> findHistoryByVendorBillId(Long vendorBillId) {
        return jpaRepository.findHistoryByVendorBillId(vendorBillId).stream()
                .map(mapper::toDomain)
                .flatMap(allocation -> toHistory(allocation, vendorBillId).stream())
                .toList();
    }

    private List<DebitMemoAllocationHistory> toHistory(DebitMemoAllocation allocation, Long vendorBillId) {
        return allocation.getLines().stream()
                .filter(line -> vendorBillId == null || vendorBillId.equals(line.getVendorBillId()))
                .map(line -> toHistory(allocation, line))
                .toList();
    }

    private DebitMemoAllocationHistory toHistory(DebitMemoAllocation allocation, DebitMemoAllocationLine line) {
        return new DebitMemoAllocationHistory(
                allocation.getId(),
                allocation.getCode(),
                allocation.getDebitMemoId(),
                allocation.getDebitMemoCode(),
                line.getVendorBillId(),
                line.getVendorBillCode(),
                allocation.getAllocationDate(),
                allocation.getStatus(),
                line.getAppliedGrossOriginal(),
                line.getApReductionBase(),
                allocation.getApplyJournalEntryId(),
                allocation.getReversalJournalEntryId(),
                allocation.getReversalDate()
        );
    }
}
