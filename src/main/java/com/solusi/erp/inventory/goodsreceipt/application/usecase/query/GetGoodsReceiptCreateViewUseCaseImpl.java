package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GetGoodsReceiptCreateViewUseCaseImpl implements GetGoodsReceiptCreateViewUseCase {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public GetGoodsReceiptCreateViewUseCaseImpl(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Override
    public GoodsReceipt execute(Long poId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new DomainException("msg.error.po.notfound"));
        if (!po.getStatus().canReceive()) {
            throw new DomainException("msg.error.gr.po.invalid.status");
        }
        List<GoodsReceiptLine> lines = po.getLines().stream()
                .filter(line -> line.getOutstandingQuantity().compareTo(BigDecimal.ZERO) > 0)
                .map(line -> GoodsReceiptLine.prefill(
                        line.getId(),
                        line.getProductId(),
                        po.getFacilityId(),
                        Boolean.FALSE,
                        BigDecimal.ZERO,
                        line.getUomId(),
                        null,
                        line.getUnitPrice(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null
                ))
                .toList();
        return GoodsReceipt.createNew(
                null,
                LocalDate.now(),
                po.getId(),
                po.getSupplierId(),
                po.getFacilityId(),
                po.getCurrencyId(),
                po.getExchangeRate(),
                lines
        );
    }
}
