package com.solusi.erp.inventory.goodsreceipt.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.persistence.GoodsReceiptEntity;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.persistence.GoodsReceiptJpaRepository;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.persistence.GoodsReceiptLineEntity;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.persistence.GoodsReceiptPersistenceMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GoodsReceiptRepositoryImpl implements GoodsReceiptRepository {

    private final GoodsReceiptJpaRepository jpaRepository;
    private final GoodsReceiptPersistenceMapper mapper;

    public GoodsReceiptRepositoryImpl(GoodsReceiptJpaRepository jpaRepository,
                                      GoodsReceiptPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public GoodsReceipt save(GoodsReceipt domain) {
        GoodsReceiptEntity entity = mapper.toEntity(domain);

        entity.getLines().clear();
        List<GoodsReceiptLineEntity> lineEntities =
                mapper.toLineEntityList(domain.getLines(), entity);
        entity.getLines().addAll(lineEntities);

        GoodsReceiptEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<GoodsReceipt> findById(Long id) {
        return jpaRepository.findByIdWithLines(id).map(mapper::toDomain);
    }

    @Override
    public Page<GoodsReceipt> findAll(String keyword,
                                      GoodsReceiptReferenceType referenceType,
                                      Long referenceId,
                                      Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasReferenceFilter = referenceType != null && referenceId != null;

        org.springframework.data.domain.Page<GoodsReceiptEntity> springPage;
        if (hasReferenceFilter && hasKeyword) {
            springPage = jpaRepository.searchByReference(keyword, referenceType, referenceId, springPageable);
        } else if (hasReferenceFilter) {
            springPage = jpaRepository.findAllWithLinesByReference(referenceType, referenceId, springPageable);
        } else if (hasKeyword) {
            springPage = jpaRepository.search(keyword, springPageable);
        } else {
            springPage = jpaRepository.findAllWithLines(springPageable);
        }

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

    @Override
    public long countByReference(GoodsReceiptReferenceType referenceType, Long referenceId) {
        return jpaRepository.countByReference(referenceType, referenceId);
    }
}
