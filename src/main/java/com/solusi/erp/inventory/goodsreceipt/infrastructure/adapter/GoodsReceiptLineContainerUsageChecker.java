package com.solusi.erp.inventory.goodsreceipt.infrastructure.adapter;

import org.springframework.stereotype.Component;

@Component
public class GoodsReceiptLineContainerUsageChecker {

    public boolean isContainerInUse(Long containerId) {
        return true;
    }
}
