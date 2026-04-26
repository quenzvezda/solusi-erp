package com.solusi.erp.inventory.goodsreceipt.domain.repository;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;

import java.util.Optional;

public interface GoodsReceiptRepository {

    GoodsReceipt save(GoodsReceipt goodsReceipt);

    Optional<GoodsReceipt> findById(Long id);
}
