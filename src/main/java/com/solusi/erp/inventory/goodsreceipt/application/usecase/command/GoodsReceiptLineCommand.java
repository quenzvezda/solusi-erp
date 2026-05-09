package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

import java.math.BigDecimal;

public record GoodsReceiptLineCommand(
        Long id,
        Long referenceLineId,
        Long productId,
        Boolean serialized,
        BigDecimal quantityReceived,
        Long uomId,
        Long containerId,
        String serialNumber
) {}
