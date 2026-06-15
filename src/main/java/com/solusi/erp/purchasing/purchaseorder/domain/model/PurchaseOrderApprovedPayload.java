package com.solusi.erp.purchasing.purchaseorder.domain.model;

import java.math.BigDecimal;

public record PurchaseOrderApprovedPayload(
        Long poId,
        String poNumber,
        Long requesterUserId,
        Long requesterPartyId,
        String requesterName,
        String requesterEmail,
        Long approverPartyId,
        String approverName,
        String approvedAt,
        BigDecimal totalAmount,
        String currencyCode) {
}
