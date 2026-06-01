package com.solusi.erp.inventory.goodsissue.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssueEntity;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssueJpaRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssueLineEntity;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssuePersistenceMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GoodsIssueRepositoryImpl implements GoodsIssueRepository {

    private final GoodsIssueJpaRepository jpaRepository;
    private final GoodsIssuePersistenceMapper mapper;

    public GoodsIssueRepositoryImpl(GoodsIssueJpaRepository jpaRepository,
                                    GoodsIssuePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<GoodsIssue> findAll(String keyword,
                                    GoodsIssueReferenceType referenceType,
                                    Long referenceId,
                                    Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasReferenceFilter = referenceType != null && referenceId != null;

        org.springframework.data.domain.Page<GoodsIssueEntity> springPage;
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
    public GoodsIssue save(GoodsIssue domain) {
        GoodsIssueEntity entity = mapper.toEntity(domain);

        entity.getLines().clear();
        List<GoodsIssueLineEntity> lineEntities = mapper.toLineEntityList(domain.getLines(), entity);
        entity.getLines().addAll(lineEntities);

        GoodsIssueEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<GoodsIssue> findById(Long id) {
        return jpaRepository.findByIdWithLines(id).map(mapper::toDomain);
    }

    @Override
    public void delete(GoodsIssue goodsIssue) {
        jpaRepository.delete(mapper.toEntity(goodsIssue));
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByReference(GoodsIssueReferenceType referenceType, Long referenceId) {
        return jpaRepository.existsByReference(referenceType, referenceId);
    }
}
