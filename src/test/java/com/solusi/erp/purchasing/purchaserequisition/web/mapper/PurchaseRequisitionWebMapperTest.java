package com.solusi.erp.purchasing.purchaserequisition.web.mapper;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.purchasing.purchaserequisition.application.usecase.command.LineInput;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionLine;
import com.solusi.erp.purchasing.purchaserequisition.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseRequisitionWebMapperTest {

    @Mock
    private CurrencyLookupProvider currencyLookupProvider;

    private TestableMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TestableMapper();
        mapper.setCurrencyLookupProvider(currencyLookupProvider);
    }

    // ── getCurrencyCode() ─────────────────────────────────────────────────

    @Test
    @DisplayName("getCurrencyCode() returns null when currencyId is null")
    void getCurrencyCode_nullId() {
        String result = mapper.getCurrencyCode(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCurrencyCode() returns null when lookup returns null")
    void getCurrencyCode_lookupReturnsNull() {
        when(currencyLookupProvider.resolve(1L)).thenReturn(null);
        String result = mapper.getCurrencyCode(1L);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCurrencyCode() returns alias from payload when available")
    void getCurrencyCode_returnsAlias() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("alias", "USD");
        payload.put("symbol", "$");
        LookupDto dto = new LookupDto(1L, "US Dollar", "$ - USD", payload);
        when(currencyLookupProvider.resolve(1L)).thenReturn(dto);

        String result = mapper.getCurrencyCode(1L);

        assertThat(result).isEqualTo("USD");
    }

    @Test
    @DisplayName("getCurrencyCode() returns null when payload has no alias")
    void getCurrencyCode_noAliasInPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("symbol", "$");
        LookupDto dto = new LookupDto(1L, "US Dollar", "$ - USD", payload);
        when(currencyLookupProvider.resolve(1L)).thenReturn(dto);

        String result = mapper.getCurrencyCode(1L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCurrencyCode() handles empty payload gracefully")
    void getCurrencyCode_emptyPayload() {
        LookupDto dto = new LookupDto(1L, "US Dollar", "$ - USD", new HashMap<>());
        when(currencyLookupProvider.resolve(1L)).thenReturn(dto);

        String result = mapper.getCurrencyCode(1L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCurrencyCode() handles null payload gracefully")
    void getCurrencyCode_nullPayload() {
        LookupDto dto = new LookupDto(1L, "US Dollar", "$ - USD", null);
        when(currencyLookupProvider.resolve(1L)).thenReturn(dto);

        String result = mapper.getCurrencyCode(1L);

        assertThat(result).isNull();
    }

    // Test helper class to expose getCurrencyCode method
    static class TestableMapper extends PurchaseRequisitionWebMapper {
        
        void setCurrencyLookupProvider(CurrencyLookupProvider provider) {
            super.currencyLookupProvider = provider;
        }
        
        @Override
        public String getCurrencyCode(Long id) {
            return super.getCurrencyCode(id);
        }

        // Stub required abstract methods
        @Override
        public PurchaseRequisitionSummaryResponse toSummaryResponse(PurchaseRequisition domain) {
            return null;
        }

        @Override
        public PurchaseRequisitionDetailResponse toDetailResponse(PurchaseRequisition domain) {
            return null;
        }

        @Override
        public PurchaseRequisitionSaveRequest toSaveRequest(PurchaseRequisition domain) {
            return null;
        }

        @Override
        public PurchaseRequisitionLineRequest toLineRequest(PurchaseRequisitionLine line) {
            return null;
        }

        @Override
        public PurchaseRequisitionLineResponse toLineResponse(PurchaseRequisitionLine line) {
            return null;
        }

        @Override
        public LineInput toLineInput(PurchaseRequisitionLineRequest request) {
            return null;
        }
    }
}
