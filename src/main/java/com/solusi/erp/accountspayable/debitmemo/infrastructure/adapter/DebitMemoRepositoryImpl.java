package com.solusi.erp.accountspayable.debitmemo.infrastructure.adapter;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoEntity;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoJpaRepository;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoLineEntity;
import com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoPersistenceMapper;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DebitMemoRepositoryImpl implements DebitMemoRepository {

    private final DebitMemoJpaRepository jpaRepository;
    private final DebitMemoPersistenceMapper mapper;

    public DebitMemoRepositoryImpl(DebitMemoJpaRepository jpaRepository, DebitMemoPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<DebitMemo> findAll(String keyword,
                                   Long vendorId,
                                   DebitMemoSettlementStatus settlementStatus,
                                   LocalDate memoDateFrom,
                                   LocalDate memoDateTo,
                                   Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        org.springframework.data.domain.Page<DebitMemoEntity> springPage = jpaRepository.findAllFiltered(
                normalizedKeyword, vendorId, settlementStatus, memoDateFrom, memoDateTo, springPageable);
        return new Page<>(
                springPage.getContent().stream().map(mapper::toDomain).toList(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public DebitMemo save(DebitMemo debitMemo) {
        DebitMemoEntity entity = mapper.toEntity(debitMemo);
        entity.getLines().clear();
        List<DebitMemoLineEntity> lineEntities = mapper.toLineEntityList(debitMemo.getLines(), entity);
        entity.getLines().addAll(lineEntities);
        DebitMemoEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void updateSettlementStatus(Long id, DebitMemoSettlementStatus settlementStatus) {
        jpaRepository.updateSettlementStatus(id, settlementStatus);
    }

    @Override
    public Optional<DebitMemo> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<DebitMemo> findByPurchaseReturnId(Long purchaseReturnId) {
        return jpaRepository.findByPurchaseReturnId(purchaseReturnId).map(mapper::toDomain);
    }

    @Override
    public Optional<DebitMemo> findByPurchaseReturnIdForUpdate(Long purchaseReturnId) {
        return jpaRepository.findByPurchaseReturnIdForUpdate(purchaseReturnId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPurchaseReturnId(Long purchaseReturnId) {
        return jpaRepository.existsByPurchaseReturnId(purchaseReturnId);
    }

    @Override
    public boolean existsSupplierMemoNumber(Long vendorId, String supplierMemoNumber, Long excludedId) {
        return jpaRepository.existsSupplierMemoNumber(vendorId, supplierMemoNumber, excludedId);
    }

    @Override
    public boolean existsTaxDocumentNumber(String taxDocumentNumber, Long excludedId) {
        return jpaRepository.existsTaxDocumentNumber(taxDocumentNumber, excludedId);
    }
}
