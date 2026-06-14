package com.solusi.erp.inventory.goodsissue.application.usecase.command;

@FunctionalInterface
public interface DeleteGoodsIssueUseCase {
    void execute(Long id);
}
