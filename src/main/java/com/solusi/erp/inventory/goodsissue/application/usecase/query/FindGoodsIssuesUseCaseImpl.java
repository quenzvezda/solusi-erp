package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;

public class FindGoodsIssuesUseCaseImpl implements FindGoodsIssuesUseCase {

    private final GoodsIssueRepository goodsIssueRepository;

    public FindGoodsIssuesUseCaseImpl(GoodsIssueRepository goodsIssueRepository) {
        this.goodsIssueRepository = goodsIssueRepository;
    }

    @Override
    public Page<GoodsIssue> execute(String keyword,
                                    GoodsIssueReferenceType referenceType,
                                    Long referenceId,
                                    Pageable pageable) {
        return goodsIssueRepository.findAll(keyword, referenceType, referenceId, pageable);
    }
}
