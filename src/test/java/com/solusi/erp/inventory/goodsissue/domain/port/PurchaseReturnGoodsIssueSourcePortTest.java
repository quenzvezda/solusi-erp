package com.solusi.erp.inventory.goodsissue.domain.port;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseReturnGoodsIssueSourcePortTest {

    @Test
    void contractCarriesHeaderLinesValuationAndClearingDataNeededByGoodsIssue() {
        PurchaseReturnGoodsIssueSourcePort.HeaderSnapshot header =
                new PurchaseReturnGoodsIssueSourcePort.HeaderSnapshot(
                        70L,
                        "PRTN-0070",
                        11L,
                        3L,
                        1L,
                        new BigDecimal("1.0000"),
                        true,
                        4100L
                );
        PurchaseReturnGoodsIssueSourcePort.LineSnapshot line =
                new PurchaseReturnGoodsIssueSourcePort.LineSnapshot(
                        700L,
                        100L,
                        "PRD-001",
                        "USB Cable",
                        false,
                        new BigDecimal("2.00"),
                        new BigDecimal("2.00"),
                        9L,
                        "PCS",
                        "Pieces",
                        3L,
                        30L,
                        "GRID-01",
                        "Grid 01",
                        40L,
                        "BIN-01",
                        "Bin 01",
                        "",
                        55L,
                        550L,
                        "GOODS_RECEIPT",
                        55L,
                        550L,
                        new BigDecimal("1250.00"),
                        new BigDecimal("2500.00"),
                        new BigDecimal("275.00"),
                        new BigDecimal("2775.00")
                );
        PurchaseReturnGoodsIssueSourcePort port = new InMemoryPurchaseReturnPort(header, List.of(line), true);

        assertThat(port.findHeader(70L)).contains(header);
        assertThat(port.findEligibleLines(70L)).containsExactly(line);
        assertThat(port.hasCompletedGoodsIssue(70L)).isTrue();
        assertThat(line.valuationRefType()).isEqualTo("GOODS_RECEIPT");
        assertThat(line.valuationRefLineId()).isEqualTo(550L);
        assertThat(line.taxReversalAmount()).isEqualByComparingTo("275.00");
        assertThat(header.billPosted()).isTrue();
        assertThat(header.clearingAccountId()).isEqualTo(4100L);
    }

    private record InMemoryPurchaseReturnPort(
            PurchaseReturnGoodsIssueSourcePort.HeaderSnapshot header,
            List<PurchaseReturnGoodsIssueSourcePort.LineSnapshot> lines,
            boolean completedGoodsIssueExists
    ) implements PurchaseReturnGoodsIssueSourcePort {
        @Override
        public Optional<HeaderSnapshot> findHeader(Long purchaseReturnId) {
            return header.purchaseReturnId().equals(purchaseReturnId) ? Optional.of(header) : Optional.empty();
        }

        @Override
        public List<LineSnapshot> findEligibleLines(Long purchaseReturnId) {
            return header.purchaseReturnId().equals(purchaseReturnId) ? lines : List.of();
        }

        @Override
        public boolean hasCompletedGoodsIssue(Long purchaseReturnId) {
            return completedGoodsIssueExists && header.purchaseReturnId().equals(purchaseReturnId);
        }
    }
}
