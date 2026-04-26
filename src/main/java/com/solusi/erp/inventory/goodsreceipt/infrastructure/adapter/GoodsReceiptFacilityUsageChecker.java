package com.solusi.erp.inventory.goodsreceipt.infrastructure.adapter;

import org.springframework.stereotype.Component;

@Component
public class GoodsReceiptFacilityUsageChecker {

    public boolean isFacilityInUse(Long facilityId) {
        return true;
    }
}
