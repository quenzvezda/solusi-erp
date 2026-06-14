package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.infrastructure.service.GoodsIssueSourceResolverRegistry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GetGoodsIssueCreateViewUseCaseImpl implements GetGoodsIssueCreateViewUseCase {

    private final GoodsIssueSourceResolverRegistry resolverRegistry;

    public GetGoodsIssueCreateViewUseCaseImpl(GoodsIssueSourceResolverRegistry resolverRegistry) {
        this.resolverRegistry = resolverRegistry;
    }

    @Override
    public GoodsIssue execute(GoodsIssueReferenceType referenceType, Long referenceId) {
        if (referenceType == null && referenceId == null) {
            return blankManualDraft();
        }
        if (referenceType == null || referenceId == null) {
            throw new DomainException("msg.error.gi.reference.required");
        }
        return resolverRegistry.getResolver(referenceType).resolve(referenceId);
    }

    private GoodsIssue blankManualDraft() {
        return GoodsIssue.createNew(
                null,
                LocalDate.now(),
                GoodsIssueReferenceType.MANUAL,
                null,
                null,
                null,
                GoodsIssuePartyType.INTERNAL,
                null,
                null,
                BigDecimal.ONE,
                List.of()
        );
    }
}
