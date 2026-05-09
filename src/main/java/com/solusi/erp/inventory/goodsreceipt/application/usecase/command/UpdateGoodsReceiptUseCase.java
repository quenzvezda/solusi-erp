package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;

import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface UpdateGoodsReceiptUseCase {
    GoodsReceipt execute(Long id, LocalDate receiptDate, String note, List<GoodsReceiptLineCommand> lines);
}
