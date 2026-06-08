package com.solusi.erp.purchasing.purchasereturn.domain.port;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReversalLine;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseReturnInventoryReversalPort {

    List<PurchaseReturnReversibleMovement> findReversibleMovements(PurchaseReturn purchaseReturn);

    List<PurchaseReturnReversalLine> reverseStockMovements(PurchaseReturn purchaseReturn,
                                                           LocalDate reversalDate,
                                                           String reversalReason,
                                                           List<PurchaseReturnStockReversalTarget> targets);

    void cancelGeneratedGoodsIssue(PurchaseReturn purchaseReturn, LocalDate reversalDate, String reversalReason);
}
