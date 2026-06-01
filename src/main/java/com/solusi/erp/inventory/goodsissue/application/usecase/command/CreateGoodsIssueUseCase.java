package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface CreateGoodsIssueUseCase {
    GoodsIssue execute(LocalDate issueDate,
                       GoodsIssueReferenceType referenceType,
                       Long referenceId,
                       String referenceCode,
                       Long partyId,
                       GoodsIssuePartyType partyType,
                       Long facilityId,
                       Long currencyId,
                       BigDecimal exchangeRate,
                       String note,
                       List<GoodsIssueLineCommand> lines);
}
