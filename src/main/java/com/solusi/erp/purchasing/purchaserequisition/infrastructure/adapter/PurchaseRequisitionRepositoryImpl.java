package com.solusi.erp.purchasing.purchaserequisition.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionEntity;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionJpaRepository;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionLineEntity;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionPersistenceMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PurchaseRequisitionRepositoryImpl implements PurchaseRequisitionRepository {

    private final PurchaseRequisitionJpaRepository jpaRepository;
    private final PurchaseRequisitionPersistenceMapper mapper;

    public PurchaseRequisitionRepositoryImpl(PurchaseRequisitionJpaRepository jpaRepository,
                                              PurchaseRequisitionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PurchaseRequisition save(PurchaseRequisition domain) {
        PurchaseRequisitionEntity entity = mapper.toEntity(domain);

        // Handle lines: clear existing and add new ones (orphanRemoval handles deletion)
        entity.getLines().clear();
        List<PurchaseRequisitionLineEntity> lineEntities =
                mapper.toLineEntityList(domain.getLines(), entity);
        entity.getLines().addAll(lineEntities);

        PurchaseRequisitionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PurchaseRequisition> findById(Long id) {
        return jpaRepository.findByIdWithLines(id).map(mapper::toDomain);
    }

    @Override
    public Page<PurchaseRequisition> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<PurchaseRequisitionEntity> springPage =
            (keyword != null && !keyword.isBlank())
                ? jpaRepository.search(keyword, springPageable)
                : jpaRepository.findAllWithJoin(springPageable);
        return new Page<>(
            springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements()
        );
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
