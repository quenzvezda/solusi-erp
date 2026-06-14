package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import java.math.BigDecimal;

public record GoodsIssueCancelLineCommand(
        Long goodsIssueLineId,
        Long originalMovementId,
        Long targetContainerId,
        String productLabel,
        String serialNumber,
        BigDecimal quantityIssued
) {
}
