package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;

public class DeleteGoodsIssueUseCaseImpl implements DeleteGoodsIssueUseCase {

    private final GoodsIssueRepository goodsIssueRepository;

    public DeleteGoodsIssueUseCaseImpl(GoodsIssueRepository goodsIssueRepository) {
        this.goodsIssueRepository = goodsIssueRepository;
    }

    @Override
    public void execute(Long id) {
        GoodsIssue goodsIssue = goodsIssueRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.gi.notfound"));
        if (goodsIssue.getStatus() == GoodsIssueStatus.COMPLETED) {
            throw new DomainException("msg.error.gi.completed.immutable");
        }
        if (goodsIssue.getStatus() == GoodsIssueStatus.CANCELLED) {
            throw new DomainException("msg.error.gi.cancelled.immutable");
        }
        goodsIssueRepository.delete(goodsIssue);
    }
}
