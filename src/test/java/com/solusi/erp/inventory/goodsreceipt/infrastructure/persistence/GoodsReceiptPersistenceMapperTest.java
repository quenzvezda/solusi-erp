package com.solusi.erp.inventory.goodsreceipt.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoodsReceiptPersistenceMapperTest {

    private final GoodsReceiptPersistenceMapper mapper = Mappers.getMapper(GoodsReceiptPersistenceMapper.class);

    @Test
    void toDomain_mapsAllFields() {
        GoodsReceiptEntity entity = new GoodsReceiptEntity();
        entity.setId(100L);
        entity.setCode("GR-202407-00001");
        entity.setReceiptDate(LocalDate.of(2026, 7, 14));
        entity.setPoId(1L);
        entity.setSupplierId(2L);
        entity.setFacilityId(3L);
        entity.setCurrencyId(1L);
        entity.setExchangeRate(new BigDecimal("1.00"));
        entity.setStatus(GoodsReceiptStatus.DRAFT);
        entity.setNote("Test note");
        entity.setVersion(1);
        entity.setCreatedBy(1L);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setLines(List.of());

        GoodsReceipt domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(100L);
        assertThat(domain.getCode()).isEqualTo("GR-202407-00001");
        assertThat(domain.getReceiptDate()).isEqualTo(LocalDate.of(2026, 7, 14));
        assertThat(domain.getPoId()).isEqualTo(1L);
        assertThat(domain.getSupplierId()).isEqualTo(2L);
        assertThat(domain.getFacilityId()).isEqualTo(3L);
        assertThat(domain.getCurrencyId()).isEqualTo(1L);
        assertThat(domain.getExchangeRate()).isEqualByComparingTo("1.00");
        assertThat(domain.getStatus()).isEqualTo(GoodsReceiptStatus.DRAFT);
        assertThat(domain.getNote()).isEqualTo("Test note");
        assertThat(domain.getLines()).isEmpty();
    }

    @Test
    void toEntity_setsStatusString() {
        GoodsReceipt domain = new GoodsReceipt(
                new AuditMetadata(100L, 1L, LocalDateTime.now(), 1L, null, null),
                "GR-202407-00001",
                LocalDate.of(2026, 7, 14),
                1L, 2L, 3L, 1L,
                new BigDecimal("1.00"),
                GoodsReceiptStatus.DRAFT,
                "Test note",
                List.of()
        );

        GoodsReceiptEntity entity = mapper.toEntity(domain);

        assertThat(entity.getStatus()).isEqualTo(GoodsReceiptStatus.DRAFT);
        assertThat(entity.getCode()).isEqualTo("GR-202407-00001");
        assertThat(entity.getExchangeRate()).isEqualByComparingTo("1.00");
    }

    @Test
    void toDomain_defensiveCopiesLines() {
        GoodsReceiptLineEntity lineEntity = new GoodsReceiptLineEntity();
        lineEntity.setPoLineId(10L);
        lineEntity.setProductId(20L);
        lineEntity.setQuantityReceived(new BigDecimal("5.00"));

        GoodsReceiptEntity entity = new GoodsReceiptEntity();
        entity.setId(100L);
        entity.setCode("GR-001");
        entity.setReceiptDate(LocalDate.now());
        entity.setPoId(1L);
        entity.setSupplierId(2L);
        entity.setCurrencyId(1L);
        entity.setExchangeRate(BigDecimal.ONE);
        entity.setStatus(GoodsReceiptStatus.DRAFT);
        entity.setLines(new java.util.ArrayList<>(List.of(lineEntity)));

        GoodsReceipt domain = mapper.toDomain(entity);

        // Verify that the lines are immutable (unmodifiable)
        assertThat(domain.getLines()).hasSize(1);
        assertThat(domain.getLines()).isUnmodifiable();
    }
}
