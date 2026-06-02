package com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnLine;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseReturnPersistenceMapperTest {

    private final PurchaseReturnPersistenceMapper mapper = new PurchaseReturnPersistenceMapperImpl();

    @Test
    void toDomain_preservesHeaderLineSnapshotsAndAuditMetadata() {
        LocalDateTime now = LocalDateTime.now();
        PurchaseReturnEntity entity = entity(now);

        PurchaseReturn domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getMetadata().version()).isEqualTo(2L);
        assertThat(domain.getStatus()).isEqualTo(PurchaseReturnStatus.CONFIRMED);
        assertThat(domain.getReason()).isEqualTo(PurchaseReturnReason.DAMAGED);
        assertThat(domain.getSubmittedByUserId()).isEqualTo(8L);
        assertThat(domain.getGeneratedGoodsIssueId()).isEqualTo(9L);
        assertThat(domain.getLines()).singleElement().satisfies(line -> {
            assertThat(line.getId()).isEqualTo(11L);
            assertThat(line.getContainerId()).isEqualTo(40L);
            assertThat(line.getSerialNumbers()).isEqualTo("SER-001");
            assertThat(line.getValuationReferenceLineId()).isEqualTo(60L);
            assertThat(line.getInventoryAmount()).isEqualByComparingTo("100");
        });
    }

    @Test
    void toEntityAndLines_preserveDomainCopiesAndParentReference() {
        PurchaseReturn domain = PurchaseReturn.reconstitute(
                AuditMetadata.empty(), "PRT-001", LocalDate.of(2026, 6, 1), "GOODS_RECEIPT",
                1L, "GR-001", 2L, "PO-001", 3L, 4L, 5L, BigDecimal.ONE,
                PurchaseReturnStatus.DRAFT, PurchaseReturnReason.DAMAGED, "note", null, null,
                List.of(line())
        );

        PurchaseReturnEntity entity = mapper.toEntity(domain);
        List<PurchaseReturnLineEntity> lines = mapper.toLineEntityList(domain.getLines(), entity);

        assertThat(entity.getCode()).isEqualTo("PRT-001");
        assertThat(entity.getReferenceCode()).isEqualTo("GR-001");
        assertThat(lines).singleElement().satisfies(line -> {
            assertThat(line.getHeader()).isSameAs(entity);
            assertThat(line.getGoodsReceiptLineId()).isEqualTo(1L);
            assertThat(line.getReason()).isEqualTo(PurchaseReturnReason.DAMAGED);
        });
    }

    private PurchaseReturnEntity entity(LocalDateTime now) {
        PurchaseReturnEntity entity = new PurchaseReturnEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setCreatedBy(6L);
        entity.setCreatedDate(now);
        entity.setUpdatedBy(7L);
        entity.setUpdatedDate(now);
        entity.setCode("PRT-001");
        entity.setReturnDate(LocalDate.of(2026, 6, 1));
        entity.setReferenceType("GOODS_RECEIPT");
        entity.setReferenceId(1L);
        entity.setReferenceCode("GR-001");
        entity.setPurchaseOrderId(2L);
        entity.setPurchaseOrderCode("PO-001");
        entity.setSupplierId(3L);
        entity.setFacilityId(4L);
        entity.setCurrencyId(5L);
        entity.setExchangeRate(BigDecimal.ONE);
        entity.setStatus(PurchaseReturnStatus.CONFIRMED);
        entity.setReason(PurchaseReturnReason.DAMAGED);
        entity.setNote("note");
        entity.setSubmittedByUserId(8L);
        entity.setGeneratedGoodsIssueId(9L);

        PurchaseReturnLineEntity line = new PurchaseReturnLineEntity();
        line.setId(11L);
        line.setVersion(3);
        line.setCreatedBy(6L);
        line.setCreatedDate(now);
        line.setUpdatedBy(7L);
        line.setUpdatedDate(now);
        line.setHeader(entity);
        line.setGoodsReceiptLineId(1L);
        line.setProductId(10L);
        line.setSerialized(true);
        line.setQuantity(BigDecimal.ONE);
        line.setUomId(20L);
        line.setBaseQuantity(BigDecimal.ONE);
        line.setFacilityId(30L);
        line.setGridId(35L);
        line.setContainerId(40L);
        line.setSerialNumbers("SER-001");
        line.setReason(PurchaseReturnReason.DAMAGED);
        line.setValuationReferenceType("GOODS_RECEIPT");
        line.setValuationReferenceId(50L);
        line.setValuationReferenceLineId(60L);
        line.setUnitCost(new BigDecimal("100"));
        line.setInventoryAmount(new BigDecimal("100"));
        line.setTaxReversalAmount(BigDecimal.ZERO);
        line.setClearingAmount(BigDecimal.ZERO);
        entity.getLines().add(line);
        return entity;
    }

    private PurchaseReturnLine line() {
        return PurchaseReturnLine.create(
                1L, 10L, false, BigDecimal.ONE, 20L, BigDecimal.ONE, 30L, 35L, 40L,
                null, PurchaseReturnReason.DAMAGED, null, "GOODS_RECEIPT", 50L, 60L,
                new BigDecimal("100"), new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO
        );
    }
}
