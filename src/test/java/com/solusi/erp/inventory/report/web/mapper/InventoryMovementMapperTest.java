package com.solusi.erp.inventory.report.web.mapper;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerJpaRepository;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityJpaRepository;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.report.web.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryMovementMapperTest {

    @Mock
    private CurrencyLookupProvider currencyLookupProvider;

    @Mock
    private AuditMapperHelper auditMapperHelper;

    @Mock
    private JpaProductRepository productRepository;

    @Mock
    private ContainerJpaRepository containerJpaRepository;

    @Mock
    private GridJpaRepository gridJpaRepository;

    @Mock
    private FacilityJpaRepository facilityJpaRepository;

    @InjectMocks
    private InventoryMovementMapperImpl mapper;

    // ── resolveCurrencyAlias() ──────────────────────────────────────────────

    @Test
    @DisplayName("resolveCurrencyAlias() returns null when currencyId is null")
    void resolveCurrencyAlias_nullCurrencyId() {
        String result = mapper.resolveCurrencyAlias(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolveCurrencyAlias() returns null when lookup returns null")
    void resolveCurrencyAlias_lookupReturnsNull() {
        when(currencyLookupProvider.resolve(1L)).thenReturn(null);

        String result = mapper.resolveCurrencyAlias(1L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolveCurrencyAlias() parses alias from standard subText format '$ - USD'")
    void resolveCurrencyAlias_standardFormat() {
        LookupDto lookup = new LookupDto(1L, "US Dollar", "$ - USD");
        when(currencyLookupProvider.resolve(1L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(1L);

        assertThat(result).isEqualTo("USD");
    }

    @Test
    @DisplayName("resolveCurrencyAlias() parses alias from format 'Rp - IDR'")
    void resolveCurrencyAlias_indonesianRupiah() {
        LookupDto lookup = new LookupDto(2L, "Indonesian Rupiah", "Rp - IDR");
        when(currencyLookupProvider.resolve(2L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(2L);

        assertThat(result).isEqualTo("IDR");
    }

    @Test
    @DisplayName("resolveCurrencyAlias() parses alias from format '€ - EUR'")
    void resolveCurrencyAlias_euro() {
        LookupDto lookup = new LookupDto(3L, "Euro", "€ - EUR");
        when(currencyLookupProvider.resolve(3L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(3L);

        assertThat(result).isEqualTo("EUR");
    }

    @Test
    @DisplayName("resolveCurrencyAlias() handles delimiter with spaces '¥ - JPY'")
    void resolveCurrencyAlias_withSpaces() {
        LookupDto lookup = new LookupDto(4L, "Japanese Yen", "¥ - JPY");
        when(currencyLookupProvider.resolve(4L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(4L);

        assertThat(result).isEqualTo("JPY");
    }

    @Test
    @DisplayName("resolveCurrencyAlias() returns empty string when delimiter missing (fallback)")
    void resolveCurrencyAlias_noDelimiter() {
        LookupDto lookup = new LookupDto(5L, "Mystery Currency", "XYZ");
        when(currencyLookupProvider.resolve(5L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(5L);

        // Surgical fallback: if delimiter missing, use empty string to preserve nullability intent
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("resolveCurrencyAlias() handles null subText gracefully")
    void resolveCurrencyAlias_nullSubText() {
        LookupDto lookup = new LookupDto(6L, "Some Currency", null);
        when(currencyLookupProvider.resolve(6L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(6L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("resolveCurrencyAlias() handles empty subText gracefully")
    void resolveCurrencyAlias_emptySubText() {
        LookupDto lookup = new LookupDto(7L, "Another Currency", "");
        when(currencyLookupProvider.resolve(7L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(7L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("resolveCurrencyAlias() trims whitespace from parsed alias")
    void resolveCurrencyAlias_trimsWhitespace() {
        LookupDto lookup = new LookupDto(8L, "Currency", "$ -   USD   ");
        when(currencyLookupProvider.resolve(8L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(8L);

        assertThat(result).isEqualTo("USD");
    }

    @Test
    @DisplayName("resolveCurrencyAlias() prefers payload.alias over parsing subText")
    void resolveCurrencyAlias_prefersPayloadAlias() {
        LookupDto lookup = new LookupDto(
            9L, 
            "US Dollar", 
            "$ - USD",
            Map.of("symbol", "$", "alias", "USD")
        );
        when(currencyLookupProvider.resolve(9L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(9L);

        assertThat(result).isEqualTo("USD");
    }

    @Test
    @DisplayName("resolveCurrencyAlias() falls back to parsing when payload.alias is null")
    void resolveCurrencyAlias_fallbackWhenPayloadAliasNull() {
        LookupDto lookup = new LookupDto(
            10L, 
            "Euro", 
            "€ - EUR",
            Map.of("symbol", "€") // No alias in payload
        );
        when(currencyLookupProvider.resolve(10L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(10L);

        assertThat(result).isEqualTo("EUR");
    }

    @Test
    @DisplayName("resolveCurrencyAlias() falls back to parsing when payload is null")
    void resolveCurrencyAlias_fallbackWhenPayloadNull() {
        LookupDto lookup = new LookupDto(11L, "British Pound", "£ - GBP", null);
        when(currencyLookupProvider.resolve(11L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(11L);

        assertThat(result).isEqualTo("GBP");
    }

    @Test
    @DisplayName("resolveCurrencyAlias() handles non-String payload.alias gracefully")
    void resolveCurrencyAlias_handlesNonStringPayloadAlias() {
        LookupDto lookup = new LookupDto(
            12L, 
            "Currency", 
            "X - XYZ",
            Map.of("alias", 123) // Non-string value
        );
        when(currencyLookupProvider.resolve(12L)).thenReturn(lookup);

        String result = mapper.resolveCurrencyAlias(12L);

        // Should fall back to parsing subText
        assertThat(result).isEqualTo("XYZ");
    }

    // ── toResponse() integration ───────────────────────────────────────────

    @Test
    @DisplayName("toResponse() maps currencyAlias correctly using CurrencyLookupProvider")
    void toResponse_mapsCurrencyAlias() {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setTransactionDate(LocalDateTime.of(2024, 1, 15, 10, 0));
        entity.setProductId(100L);
        entity.setContainerId(200L);
        entity.setQuantity(BigDecimal.TEN);
        entity.setMovementType(MovementType.RECEIPT);
        entity.setReversalOfMovementId(700L);
        
        CurrencyAmount unitCost = new CurrencyAmount();
        unitCost.setCurrencyId(1L);
        unitCost.setOriginalAmount(new BigDecimal("50.00"));
        unitCost.setLocalAmount(new BigDecimal("50.00"));
        entity.setUnitCost(unitCost);

        LookupDto currencyLookup = new LookupDto(1L, "US Dollar", "$ - USD");
        when(currencyLookupProvider.resolve(1L)).thenReturn(currencyLookup);

        InventoryMovementResponse response = mapper.toResponse(entity);

        assertThat(response.getCurrencyAlias()).isEqualTo("USD");
        assertThat(response.getUnitCostOriginal()).isEqualTo(new BigDecimal("50.00"));
        assertThat(response.getUnitCostLocal()).isEqualTo(new BigDecimal("50.00"));
        assertThat(response.getReversalOfMovementId()).isEqualTo(700L);
    }

    @Test
    @DisplayName("toResponse() computes totalCostLocal from absolute quantity and local unit cost")
    void toResponse_computesTotalCostLocal() {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setTransactionDate(LocalDateTime.of(2024, 1, 15, 10, 0));
        entity.setProductId(100L);
        entity.setContainerId(200L);
        entity.setQuantity(new BigDecimal("-5"));
        entity.setMovementType(MovementType.ISSUE);

        CurrencyAmount unitCost = new CurrencyAmount();
        unitCost.setCurrencyId(null);
        unitCost.setLocalAmount(new BigDecimal("5000000.00"));
        entity.setUnitCost(unitCost);

        InventoryMovementResponse response = mapper.toResponse(entity);

        assertThat(response.getTotalCostLocal()).isEqualByComparingTo(new BigDecimal("25000000.00"));
    }

    @Test
    @DisplayName("toResponse() leaves totalCostLocal null when unit cost is absent")
    void toResponse_nullUnitCostLeavesTotalCostLocalNull() {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setTransactionDate(LocalDateTime.of(2024, 1, 15, 10, 0));
        entity.setProductId(100L);
        entity.setContainerId(200L);
        entity.setQuantity(new BigDecimal("5"));
        entity.setMovementType(MovementType.RECEIPT);
        entity.setUnitCost(null);

        InventoryMovementResponse response = mapper.toResponse(entity);

        assertThat(response.getTotalCostLocal()).isNull();
    }

    @Test
    @DisplayName("toResponse() leaves currencyAlias null when unitCost is null")
    void toResponse_nullUnitCost() {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setTransactionDate(LocalDateTime.of(2024, 1, 15, 10, 0));
        entity.setProductId(100L);
        entity.setContainerId(200L);
        entity.setQuantity(BigDecimal.TEN);
        entity.setMovementType(MovementType.RECEIPT);
        entity.setUnitCost(null);

        InventoryMovementResponse response = mapper.toResponse(entity);

        assertThat(response.getCurrencyAlias()).isNull();
    }

    @Test
    @DisplayName("toResponse() leaves currencyAlias null when currencyId is null")
    void toResponse_nullCurrencyId() {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setTransactionDate(LocalDateTime.of(2024, 1, 15, 10, 0));
        entity.setProductId(100L);
        entity.setContainerId(200L);
        entity.setQuantity(BigDecimal.TEN);
        entity.setMovementType(MovementType.RECEIPT);
        
        CurrencyAmount unitCost = new CurrencyAmount();
        unitCost.setCurrencyId(null);
        unitCost.setOriginalAmount(new BigDecimal("50.00"));
        unitCost.setLocalAmount(new BigDecimal("50.00"));
        entity.setUnitCost(unitCost);

        InventoryMovementResponse response = mapper.toResponse(entity);

        assertThat(response.getCurrencyAlias()).isNull();
    }
}
