package com.solusi.erp.inventory.goodsissue.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;

import java.util.Optional;

public interface GoodsIssueRepository {

    Page<GoodsIssue> findAll(String keyword,
                             GoodsIssueReferenceType referenceType,
                             Long referenceId,
                             Pageable pageable);

    GoodsIssue save(GoodsIssue goodsIssue);

    Optional<GoodsIssue> findById(Long id);

    void delete(GoodsIssue goodsIssue);

    boolean existsByCode(String code);

    boolean existsByReference(GoodsIssueReferenceType referenceType, Long referenceId);
}
