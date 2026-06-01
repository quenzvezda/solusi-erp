package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;

@FunctionalInterface
public interface GoodsIssueInUseChecker {
    void assertNotInUse(GoodsIssue goodsIssue);
}
