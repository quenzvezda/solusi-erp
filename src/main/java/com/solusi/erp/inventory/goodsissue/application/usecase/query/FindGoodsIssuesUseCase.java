package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;

@FunctionalInterface
public interface FindGoodsIssuesUseCase {
    Page<GoodsIssue> execute(String keyword,
                             GoodsIssueReferenceType referenceType,
                             Long referenceId,
                             Pageable pageable);
}
