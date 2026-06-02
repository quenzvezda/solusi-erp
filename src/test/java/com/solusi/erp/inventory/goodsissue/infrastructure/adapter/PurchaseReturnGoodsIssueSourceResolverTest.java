package com.solusi.erp.inventory.goodsissue.infrastructure.adapter;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.port.PurchaseReturnGoodsIssueSourcePort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PurchaseReturnGoodsIssueSourceResolverTest {

    @Test
    void resolve_approvedPurchaseReturn_buildsDraftWithSnapshots() {
        PurchaseReturnGoodsIssueSourcePort sourcePort = mock(PurchaseReturnGoodsIssueSourcePort.class);
        when(sourcePort.findHeader(1L)).thenReturn(Optional.of(header()));
        when(sourcePort.findEligibleLines(1L)).thenReturn(List.of(line()));

        GoodsIssue result = new PurchaseReturnGoodsIssueSourceResolver(sourcePort).resolve(1L);

        assertThat(result.getIssueDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(result.getReferenceType()).isEqualTo(GoodsIssueReferenceType.PURCHASE_RETURN);
        assertThat(result.getPartyType()).isEqualTo(GoodsIssuePartyType.SUPPLIER);
        assertThat(result.getLines()).hasSize(1);
        assertThat(result.getLines().get(0).getContainerId()).isEqualTo(40L);
        assertThat(result.getLines().get(0).getValuationRefType()).isEqualTo("GOODS_RECEIPT");
        assertThat(result.getLines().get(0).getValuationRefLineId()).isEqualTo(11L);
    }

    private PurchaseReturnGoodsIssueSourcePort.HeaderSnapshot header() {
        return new PurchaseReturnGoodsIssueSourcePort.HeaderSnapshot(
                1L, "PRT-001", LocalDate.of(2026, 6, 1), 3L, 30L, 5L, BigDecimal.ONE, false, null);
    }

    private PurchaseReturnGoodsIssueSourcePort.LineSnapshot line() {
        return new PurchaseReturnGoodsIssueSourcePort.LineSnapshot(
                100L, 10L, "P-001", "Product", false, BigDecimal.ONE, BigDecimal.ONE,
                20L, "PCS", "Piece", 30L, 35L, "A", "Main", 40L, "BIN", "Bin",
                null, 1L, 11L, "GOODS_RECEIPT", 1L, 11L, new BigDecimal("100"),
                new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("100"));
    }
}
