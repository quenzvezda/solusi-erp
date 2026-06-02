package com.solusi.erp.purchasing.purchasereturn.domain.port;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.EligibleGoodsReceiptRow;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableGrLineSlice;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableSerialRow;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseReturnSourceQueryPort {

    List<EligibleGoodsReceiptRow> findEligibleGoodsReceipts(
            String keyword, Long supplierId, Long purchaseOrderId,
            LocalDate receiptDateFrom, LocalDate receiptDateTo);

    Optional<EligibleGoodsReceiptRow> findEligibleGoodsReceiptById(Long goodsReceiptId);

    List<LookupDto> findEligiblePurchaseOrders(String query, int limit);

    List<ReturnableGrLineSlice> findReturnableGrLineSlices(
            Long goodsReceiptId, String keyword, List<String> excludedSelectionKeys);

    List<ReturnableSerialRow> findReturnableSerials(
            Long goodsReceiptId, Long goodsReceiptLineId, String keyword,
            List<String> excludedSelectionKeys);
}
