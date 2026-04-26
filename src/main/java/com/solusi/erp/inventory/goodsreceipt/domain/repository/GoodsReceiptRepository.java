package com.solusi.erp.inventory.goodsreceipt.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;

import java.util.Optional;

public interface GoodsReceiptRepository {

    Page<GoodsReceipt> findAll(String keyword, Pageable pageable);

    GoodsReceipt save(GoodsReceipt goodsReceipt);

    Optional<GoodsReceipt> findById(Long id);

    void deleteById(Long id);

    long countByPoId(Long poId);
}
