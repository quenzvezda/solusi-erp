package com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence;

import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseOrderPersistenceMapperTest {

    private final PurchaseOrderPersistenceMapper mapper = Mappers.getMapper(PurchaseOrderPersistenceMapper.class);

    @Test
    void toDomain_mapsHeaderTaxSnapshot() {
        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        entity.setCode("PO-001");
        entity.setOrderDate(LocalDate.of(2026, 7, 14));
        entity.setSupplierId(1L);
        entity.setCurrencyId(1L);
        entity.setExchangeRate(BigDecimal.ONE);
        entity.setSubtotal(new BigDecimal("9009.0090"));
        entity.setTaxAmount(new BigDecimal("990.9910"));
        entity.setTotalAmount(new BigDecimal("10000.0000"));
        entity.setPaymentTermDays(30);
        entity.setPoType(PurchaseOrderType.DIRECT);
        entity.setActive(true);
        entity.setTaxId(10L);
        entity.setTaxCode("PPN-IN");
        entity.setTaxName("PPN 11% Inclusive");
        entity.setTaxRate(new BigDecimal("11.00"));
        entity.setTaxCalculationMode(TaxCalculationMode.INCLUSIVE);

        PurchaseOrder domain = mapper.toDomain(entity);

        assertThat(domain.getTaxId()).isEqualTo(10L);
        assertThat(domain.getTaxCode()).isEqualTo("PPN-IN");
        assertThat(domain.getTaxName()).isEqualTo("PPN 11% Inclusive");
        assertThat(domain.getTaxRate()).isEqualByComparingTo("11.00");
        assertThat(domain.getTaxCalculationMode()).isEqualTo(TaxCalculationMode.INCLUSIVE);
    }

    @Test
    void toEntity_mapsHeaderTaxSnapshot() {
        PurchaseOrderLine line = new PurchaseOrderLine(
                com.solusi.erp.core.domain.model.AuditMetadata.empty(), null,
                1L, new BigDecimal("1"), BigDecimal.ZERO, 1L,
                new BigDecimal("10000.00"), BigDecimal.ZERO,
                null, null
        );
        PurchaseOrder domain = PurchaseOrder.createNew(
                "PO-001", LocalDate.of(2026, 7, 14), null,
                1L, null, 1L, BigDecimal.ONE, 30, null, PurchaseOrderType.DIRECT,
                10L, "PPN-IN", "PPN 11% Inclusive", new BigDecimal("11.00"),
                TaxCalculationMode.INCLUSIVE, null, List.of(line)
        );

        PurchaseOrderEntity entity = mapper.toEntity(domain);

        assertThat(entity.getTaxId()).isEqualTo(10L);
        assertThat(entity.getTaxCode()).isEqualTo("PPN-IN");
        assertThat(entity.getTaxName()).isEqualTo("PPN 11% Inclusive");
        assertThat(entity.getTaxRate()).isEqualByComparingTo("11.00");
        assertThat(entity.getTaxCalculationMode()).isEqualTo(TaxCalculationMode.INCLUSIVE);
    }
}
