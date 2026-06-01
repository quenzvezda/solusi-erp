package com.solusi.erp.inventory.goodsissue.domain.model;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GoodsIssue Domain Model Tests")
class GoodsIssueTest {

    @Test
    @DisplayName("goods issue status exposes lifecycle helpers")
    void goodsIssueStatus_exposesLifecycleHelpers() {
        assertThat(GoodsIssueStatus.DRAFT.isEditable()).isTrue();
        assertThat(GoodsIssueStatus.DRAFT.canComplete()).isTrue();
        assertThat(GoodsIssueStatus.DRAFT.canCancel()).isFalse();

        assertThat(GoodsIssueStatus.COMPLETED.isEditable()).isFalse();
        assertThat(GoodsIssueStatus.COMPLETED.canComplete()).isFalse();
        assertThat(GoodsIssueStatus.COMPLETED.canCancel()).isTrue();

        assertThat(GoodsIssueStatus.CANCELLED.isEditable()).isFalse();
        assertThat(GoodsIssueStatus.CANCELLED.canComplete()).isFalse();
        assertThat(GoodsIssueStatus.CANCELLED.canCancel()).isFalse();
    }

    @Test
    @DisplayName("goods issue line is an immutable value object")
    void goodsIssueLine_isImmutableValueObject() {
        assertThat(Modifier.isFinal(GoodsIssueLine.class.getModifiers())).isTrue();
        assertThat(GoodsIssueLine.class.getDeclaredFields())
                .allMatch(field -> Modifier.isFinal(field.getModifiers()), "all fields final");
    }

    @Test
    @DisplayName("complete requires at least one positive-quantity line")
    void complete_requiresPositiveLine() {
        GoodsIssue issue = GoodsIssue.createNew(
                "GI-202606-00001",
                LocalDate.of(2026, 6, 1),
                GoodsIssueReferenceType.PURCHASE_RETURN,
                7L,
                "PRTN-0007",
                11L,
                GoodsIssuePartyType.SUPPLIER,
                3L,
                1L,
                BigDecimal.ONE,
                List.of(GoodsIssueLine.prefill(
                        101L, 201L, false,
                        BigDecimal.ZERO, 1L, BigDecimal.ZERO,
                        3L, 4L, 5L, null,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        "GOODS_RECEIPT", 301L, 401L
                ))
        );

        assertThatThrownBy(issue::complete)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.complete.no.lines");
    }

    @Test
    @DisplayName("completed and cancelled issues cannot be updated")
    void completedAndCancelledIssue_cannotBeUpdated() {
        GoodsIssueLine active = activeLine();
        GoodsIssue issue = newIssue(List.of(active));
        issue.complete();

        assertThatThrownBy(() -> issue.update(LocalDate.of(2026, 6, 2), "late edit", List.of(active)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.completed.immutable");

        issue.cancel();

        assertThatThrownBy(() -> issue.update(LocalDate.of(2026, 6, 3), "cancelled edit", List.of(active)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.cancelled.immutable");
    }

    @Test
    @DisplayName("cancel is only allowed after completion")
    void cancel_onlyAllowedAfterCompleted() {
        GoodsIssue issue = newIssue(List.of(activeLine()));

        assertThatThrownBy(issue::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gi.cancel.only.completed");

        issue.complete();
        issue.cancel();

        assertThat(issue.getStatus()).isEqualTo(GoodsIssueStatus.CANCELLED);
    }

    @Test
    @DisplayName("goods issue keeps typed source and valuation reference metadata")
    void createNew_keepsTypedReferenceMetadata() {
        GoodsIssueLine line = activeLine();
        GoodsIssue issue = newIssue(List.of(line));

        assertThat(issue.getReferenceType()).isEqualTo(GoodsIssueReferenceType.PURCHASE_RETURN);
        assertThat(issue.getReferenceId()).isEqualTo(7L);
        assertThat(issue.getReferenceCode()).isEqualTo("PRTN-0007");
        assertThat(issue.getPartyType()).isEqualTo(GoodsIssuePartyType.SUPPLIER);
        assertThat(line.getValuationRefType()).isEqualTo("GOODS_RECEIPT");
        assertThat(line.getValuationRefId()).isEqualTo(301L);
        assertThat(line.getValuationRefLineId()).isEqualTo(401L);
    }

    private static GoodsIssue newIssue(List<GoodsIssueLine> lines) {
        return GoodsIssue.createNew(
                "GI-202606-00001",
                LocalDate.of(2026, 6, 1),
                GoodsIssueReferenceType.PURCHASE_RETURN,
                7L,
                "PRTN-0007",
                11L,
                GoodsIssuePartyType.SUPPLIER,
                3L,
                1L,
                BigDecimal.ONE,
                lines
        );
    }

    private static GoodsIssueLine activeLine() {
        return GoodsIssueLine.prefill(
                101L, 201L, false,
                new BigDecimal("2.0000"), 1L, new BigDecimal("2.0000"),
                3L, 4L, 5L, "SN-001",
                new BigDecimal("150.000000"), new BigDecimal("300.0000"),
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("300.0000"),
                "GOODS_RECEIPT", 301L, 401L
        );
    }
}
