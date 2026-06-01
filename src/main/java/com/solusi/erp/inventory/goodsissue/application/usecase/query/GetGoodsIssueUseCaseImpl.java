package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;

import java.util.Optional;

public class GetGoodsIssueUseCaseImpl implements GetGoodsIssueUseCase {

    private final GoodsIssueRepository goodsIssueRepository;

    public GetGoodsIssueUseCaseImpl(GoodsIssueRepository goodsIssueRepository) {
        this.goodsIssueRepository = goodsIssueRepository;
    }

    @Override
    public Optional<GoodsIssue> execute(Long id) {
        return goodsIssueRepository.findById(id);
    }
}
