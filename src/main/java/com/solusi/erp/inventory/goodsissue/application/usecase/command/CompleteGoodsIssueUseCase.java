package com.solusi.erp.inventory.goodsissue.application.usecase.command;

@FunctionalInterface
public interface CompleteGoodsIssueUseCase {
    void execute(Long id);
}
