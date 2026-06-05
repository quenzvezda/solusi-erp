package com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort.PayableVendorBillView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayableVendorBillQueryAdapterTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private PayableVendorBillQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PayableVendorBillQueryAdapter(jdbcTemplate);
    }

    @Test
    void findPayableVendorBills_should_filter_confirmed_open_or_partial_settlement_bills() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        List<PayableVendorBillView> result = adapter.findPayableVendorBills(10L, 20L);

        assertThat(result).isEmpty();
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(), any(RowMapper.class));
        assertThat(sqlCaptor.getValue()).contains("vb.document_status = 'CONFIRMED'");
        assertThat(sqlCaptor.getValue()).contains("vb.settlement_status IN ('OPEN', 'PARTIALLY_SETTLED')");
        assertThat(sqlCaptor.getValue()).contains("vp.status = 'CONFIRMED'");
        assertThat(sqlCaptor.getValue()).contains("ap_debit_memo_allocation_lines");
        assertThat(sqlCaptor.getValue()).contains("dma.status = 'CONFIRMED'");
        assertThat(sqlCaptor.getValue()).contains("- COALESCE(dma.debit_memo_applied_amount, 0)");
        assertThat(sqlCaptor.getValue()).contains("s.outstanding_amount > 0");
        assertThat(sqlCaptor.getValue()).doesNotContain("vb.status");
        assertThat(paramsCaptor.getValue().getValue("vendorId")).isEqualTo(10L);
        assertThat(paramsCaptor.getValue().getValue("currencyId")).isEqualTo(20L);
    }
}
