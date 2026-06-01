package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;

import java.util.Optional;

@FunctionalInterface
public interface GetGoodsIssueUseCase {
    Optional<GoodsIssue> execute(Long id);
}
