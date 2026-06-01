package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;

@FunctionalInterface
public interface GetGoodsIssueCreateViewUseCase {
    GoodsIssue execute(GoodsIssueReferenceType referenceType, Long referenceId);
}
