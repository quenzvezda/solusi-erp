package com.solusi.erp.inventory.goodsissue.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoodsIssuePersistenceMapperTest {

    private final GoodsIssuePersistenceMapper mapper = Mappers.getMapper(GoodsIssuePersistenceMapper.class);

    @Test
    void toDomain_mapsAllHeaderFieldsAndAuditFields() {
        GoodsIssueEntity entity = headerEntity();
        entity.setLines(List.of());

        GoodsIssue domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(100L);
        assertThat(domain.getMetadata().version()).isEqualTo(2L);
        assertThat(domain.getMetadata().createdBy()).isEqualTo(1L);
        assertThat(domain.getMetadata().updatedBy()).isEqualTo(2L);
        assertThat(domain.getCode()).isEqualTo("GI-202606-00001");
        assertThat(domain.getIssueDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(domain.getReferenceType()).isEqualTo(GoodsIssueReferenceType.PURCHASE_RETURN);
        assertThat(domain.getReferenceId()).isEqualTo(7L);
        assertThat(domain.getReferenceCode()).isEqualTo("PRTN-0007");
        assertThat(domain.getPartyId()).isEqualTo(11L);
        assertThat(domain.getPartyType()).isEqualTo(GoodsIssuePartyType.SUPPLIER);
        assertThat(domain.getFacilityId()).isEqualTo(3L);
        assertThat(domain.getCurrencyId()).isEqualTo(1L);
        assertThat(domain.getExchangeRate()).isEqualByComparingTo("1.000000");
        assertThat(domain.getStatus()).isEqualTo(GoodsIssueStatus.DRAFT);
        assertThat(domain.getNote()).isEqualTo("Test note");
        assertThat(domain.getLines()).isEmpty();
    }

    @Test
    void toDomain_mapsAllLineSnapshotsAndDefensiveCopiesLines() {
        GoodsIssueLineEntity line = lineEntity();
        GoodsIssueEntity entity = headerEntity();
        entity.setLines(new ArrayList<>(List.of(line)));

        GoodsIssue domain = mapper.toDomain(entity);

        assertThat(domain.getLines()).hasSize(1);
        assertThat(domain.getLines()).isUnmodifiable();

        GoodsIssueLine mapped = domain.getLines().getFirst();
        assertThat(mapped.getId()).isEqualTo(501L);
        assertThat(mapped.getMetadata().version()).isEqualTo(3L);
        assertThat(mapped.getReferenceLineId()).isEqualTo(101L);
        assertThat(mapped.getProductId()).isEqualTo(201L);
        assertThat(mapped.getSerialized()).isFalse();
        assertThat(mapped.getQuantityIssued()).isEqualByComparingTo("2.0000");
        assertThat(mapped.getUomId()).isEqualTo(1L);
        assertThat(mapped.getBaseQuantity()).isEqualByComparingTo("2.0000");
        assertThat(mapped.getFacilityId()).isEqualTo(3L);
        assertThat(mapped.getGridId()).isEqualTo(4L);
        assertThat(mapped.getContainerId()).isEqualTo(5L);
        assertThat(mapped.getSerialNumber()).isEqualTo("SN-001");
        assertThat(mapped.getUnitCost()).isEqualByComparingTo("150.000000");
        assertThat(mapped.getInventoryAmount()).isEqualByComparingTo("300.0000");
        assertThat(mapped.getTaxBaseAmount()).isEqualByComparingTo("280.0000");
        assertThat(mapped.getTaxAmount()).isEqualByComparingTo("20.0000");
        assertThat(mapped.getClearingAmount()).isEqualByComparingTo("300.0000");
        assertThat(mapped.getValuationRefType()).isEqualTo("GOODS_RECEIPT");
        assertThat(mapped.getValuationRefId()).isEqualTo(301L);
        assertThat(mapped.getValuationRefLineId()).isEqualTo(401L);
    }

    @Test
    void toEntity_mapsHeaderLineAndAuditFields() {
        GoodsIssue domain = new GoodsIssue(
                new AuditMetadata(100L, 2L, LocalDateTime.of(2026, 6, 1, 9, 0), 1L,
                        LocalDateTime.of(2026, 6, 1, 10, 0), 2L),
                "GI-202606-00001",
                LocalDate.of(2026, 6, 1),
                GoodsIssueReferenceType.PURCHASE_RETURN,
                7L,
                "PRTN-0007",
                11L,
                GoodsIssuePartyType.SUPPLIER,
                3L,
                1L,
                new BigDecimal("1.000000"),
                GoodsIssueStatus.DRAFT,
                "Test note",
                List.of(lineDomain())
        );

        GoodsIssueEntity entity = mapper.toEntity(domain);
        entity.setLines(new ArrayList<>(mapper.toLineEntityList(domain.getLines(), entity)));

        assertThat(entity.getId()).isEqualTo(100L);
        assertThat(entity.getVersion()).isEqualTo(2);
        assertThat(entity.getCode()).isEqualTo("GI-202606-00001");
        assertThat(entity.getIssueDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(entity.getReferenceType()).isEqualTo(GoodsIssueReferenceType.PURCHASE_RETURN);
        assertThat(entity.getReferenceId()).isEqualTo(7L);
        assertThat(entity.getReferenceCode()).isEqualTo("PRTN-0007");
        assertThat(entity.getPartyId()).isEqualTo(11L);
        assertThat(entity.getPartyType()).isEqualTo(GoodsIssuePartyType.SUPPLIER);
        assertThat(entity.getStatus()).isEqualTo(GoodsIssueStatus.DRAFT);
        assertThat(entity.getLines()).hasSize(1);
        assertThat(entity.getLines().getFirst().getHeader()).isSameAs(entity);
        assertThat(entity.getLines().getFirst().getValuationRefLineId()).isEqualTo(401L);
    }

    private static GoodsIssueEntity headerEntity() {
        GoodsIssueEntity entity = new GoodsIssueEntity();
        entity.setId(100L);
        entity.setCode("GI-202606-00001");
        entity.setIssueDate(LocalDate.of(2026, 6, 1));
        entity.setReferenceType(GoodsIssueReferenceType.PURCHASE_RETURN);
        entity.setReferenceId(7L);
        entity.setReferenceCode("PRTN-0007");
        entity.setPartyId(11L);
        entity.setPartyType(GoodsIssuePartyType.SUPPLIER);
        entity.setFacilityId(3L);
        entity.setCurrencyId(1L);
        entity.setExchangeRate(new BigDecimal("1.000000"));
        entity.setStatus(GoodsIssueStatus.DRAFT);
        entity.setNote("Test note");
        entity.setVersion(2);
        entity.setCreatedBy(1L);
        entity.setCreatedDate(LocalDateTime.of(2026, 6, 1, 9, 0));
        entity.setUpdatedBy(2L);
        entity.setUpdatedDate(LocalDateTime.of(2026, 6, 1, 10, 0));
        return entity;
    }

    private static GoodsIssueLineEntity lineEntity() {
        GoodsIssueLineEntity line = new GoodsIssueLineEntity();
        line.setId(501L);
        line.setVersion(3);
        line.setReferenceLineId(101L);
        line.setProductId(201L);
        line.setSerialized(Boolean.FALSE);
        line.setQuantityIssued(new BigDecimal("2.0000"));
        line.setUomId(1L);
        line.setBaseQuantity(new BigDecimal("2.0000"));
        line.setFacilityId(3L);
        line.setGridId(4L);
        line.setContainerId(5L);
        line.setSerialNumber("SN-001");
        line.setUnitCost(new BigDecimal("150.000000"));
        line.setInventoryAmount(new BigDecimal("300.0000"));
        line.setTaxBaseAmount(new BigDecimal("280.0000"));
        line.setTaxAmount(new BigDecimal("20.0000"));
        line.setClearingAmount(new BigDecimal("300.0000"));
        line.setValuationRefType("GOODS_RECEIPT");
        line.setValuationRefId(301L);
        line.setValuationRefLineId(401L);
        return line;
    }

    private static GoodsIssueLine lineDomain() {
        return GoodsIssueLine.reconstitute(
                new AuditMetadata(501L, 3L, null, null, null, null),
                101L, 201L, Boolean.FALSE,
                new BigDecimal("2.0000"), 1L, new BigDecimal("2.0000"),
                3L, 4L, 5L, "SN-001",
                new BigDecimal("150.000000"), new BigDecimal("300.0000"),
                new BigDecimal("280.0000"), new BigDecimal("20.0000"), new BigDecimal("300.0000"),
                "GOODS_RECEIPT", 301L, 401L
        );
    }
}
