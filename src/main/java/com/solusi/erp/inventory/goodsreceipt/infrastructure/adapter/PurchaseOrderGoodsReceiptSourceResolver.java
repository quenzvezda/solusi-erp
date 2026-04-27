package com.solusi.erp.inventory.goodsreceipt.infrastructure.adapter;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.domain.port.GoodsReceiptSourceResolver;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PurchaseOrderGoodsReceiptSourceResolver implements GoodsReceiptSourceResolver {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;

    public PurchaseOrderGoodsReceiptSourceResolver(PurchaseOrderRepository purchaseOrderRepository,
                                                   ProductRepository productRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public GoodsReceiptReferenceType getReferenceType() {
        return GoodsReceiptReferenceType.PURCHASE_ORDER;
    }

    @Override
    public GoodsReceipt resolve(Long referenceId) {
        PurchaseOrder po = purchaseOrderRepository.findById(referenceId)
                .orElseThrow(() -> new DomainException("msg.error.po.notfound"));
        if (!po.getStatus().canReceive()) {
            throw new DomainException("msg.error.gr.po.invalid.status");
        }

        List<GoodsReceiptLine> lines = po.getLines().stream()
                .filter(line -> line.getOutstandingQuantity().compareTo(BigDecimal.ZERO) > 0)
                .map(line -> {
                    Boolean serialized = productRepository.findById(line.getProductId())
                            .map(Product::isSerialized)
                            .orElse(Boolean.FALSE);

                    return GoodsReceiptLine.prefill(
                            line.getId(),
                            line.getProductId(),
                            po.getFacilityId(),
                            serialized,
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
                    );
                })
                .toList();

        return GoodsReceipt.createNew(
                null,
                LocalDate.now(),
                GoodsReceiptReferenceType.PURCHASE_ORDER,
                po.getId(),
                po.getSupplierId(),
                po.getFacilityId(),
                po.getCurrencyId(),
                po.getExchangeRate(),
                lines
        );
    }
}
