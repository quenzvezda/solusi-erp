package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnEntity;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnJpaRepository;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnLineEntity;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnPersistenceMapper;

import java.util.List;
import java.util.Optional;

public class PurchaseReturnRepositoryImpl implements PurchaseReturnRepository {

    private final PurchaseReturnJpaRepository jpaRepository;
    private final PurchaseReturnPersistenceMapper mapper;

    public PurchaseReturnRepositoryImpl(PurchaseReturnJpaRepository jpaRepository,
                                        PurchaseReturnPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PurchaseReturn save(PurchaseReturn purchaseReturn) {
        PurchaseReturnEntity entity = mapper.toEntity(purchaseReturn);
        List<PurchaseReturnLineEntity> lines = mapper.toLineEntityList(purchaseReturn.getLines(), entity);
        entity.getLines().clear();
        entity.getLines().addAll(lines);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<PurchaseReturn> findById(Long id) {
        return jpaRepository.findByIdWithLines(id).map(mapper::toDomain);
    }

    @Override
    public Page<PurchaseReturn> findAll(String keyword, PurchaseReturnStatus status, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        org.springframework.data.domain.Page<PurchaseReturnEntity> springPage;
        if (status != null && hasKeyword) {
            springPage = jpaRepository.searchByStatus(keyword, status, springPageable);
        } else if (status != null) {
            springPage = jpaRepository.findAllWithLinesByStatus(status, springPageable);
        } else if (hasKeyword) {
            springPage = jpaRepository.search(keyword, springPageable);
        } else {
            springPage = jpaRepository.findAllWithLines(springPageable);
        }
        return new Page<>(
                springPage.getContent().stream().map(mapper::toDomain).toList(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsConfirmedOrOpenBySource(String referenceType, Long referenceId) {
        return jpaRepository.existsConfirmedOrOpenBySource(referenceType, referenceId);
    }

    @Override
    public Optional<PurchaseReturn> findByGeneratedGoodsIssueId(Long generatedGoodsIssueId) {
        return jpaRepository.findByGeneratedGoodsIssueId(generatedGoodsIssueId).map(mapper::toDomain);
    }
}
