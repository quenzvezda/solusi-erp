package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;

public record DebitMemoSourceLinkView(
        Long id,
        String code,
        DebitMemoSettlementStatus settlementStatus
) {
}

