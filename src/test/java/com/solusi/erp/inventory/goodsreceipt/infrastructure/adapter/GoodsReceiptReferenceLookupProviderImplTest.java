package com.solusi.erp.inventory.goodsreceipt.infrastructure.adapter;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GoodsReceiptReferenceLookupProviderImplTest {

    @Test
    @DisplayName("resolves purchase-order code for goods receipt header display")
    void resolvesPurchaseOrderReferenceCode() {
        PurchaseOrderRepository repository = mock(PurchaseOrderRepository.class);
        PurchaseOrder purchaseOrder = mock(PurchaseOrder.class);
        when(purchaseOrder.getCode()).thenReturn("PO-202604-00005");
        when(repository.findById(5L)).thenReturn(Optional.of(purchaseOrder));

        GoodsReceiptReferenceLookupProviderImpl provider =
                new GoodsReceiptReferenceLookupProviderImpl(repository);

        assertThat(provider.resolveReferenceCode(GoodsReceiptReferenceType.PURCHASE_ORDER, 5L))
                .isEqualTo("PO-202604-00005");
    }
}
