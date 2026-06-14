package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;

import java.time.LocalDate;
import java.util.List;

public record GoodsIssueCancelView(
        Long id,
        String code,
        LocalDate issueDate,
        GoodsIssueReferenceType referenceType,
        String referenceCode,
        Long facilityId,
        String facilityName,
        List<GoodsIssueCancelLineView> lines
) {
    public boolean directCancelAllowed() {
        return referenceType == GoodsIssueReferenceType.MANUAL;
    }
}
