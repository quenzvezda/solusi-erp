package com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorbill.domain.port.VendorBillSettlementSummaryPort.SettlementSummary;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorBillSettlementSummaryAdapterTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private VendorBillSettlementSummaryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new VendorBillSettlementSummaryAdapter(jdbcTemplate);
    }

    @Test
    void getSettlementSummaries_should_query_confirmed_payments_and_dma_without_outstanding_filter() {
        List<SettlementSummary> expected = List.of(
                new SettlementSummary(4L, new BigDecimal("50000000.00"), new BigDecimal("5000000.00"), new BigDecimal("20000000.00"), VendorBillSettlementStatus.PARTIALLY_SETTLED),
                new SettlementSummary(5L, BigDecimal.ZERO, new BigDecimal("10000000.00"), BigDecimal.ZERO, VendorBillSettlementStatus.SETTLED)
        );
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(expected);

        Map<Long, SettlementSummary> actual = adapter.getSettlementSummaries(List.of(4L, 5L));

        assertThat(actual).containsEntry(4L, expected.get(0));
        assertThat(actual).containsEntry(5L, expected.get(1));
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramCaptor.capture(), any(RowMapper.class));
        assertThat(sqlCaptor.getValue()).contains("vp.status = 'CONFIRMED'");
        assertThat(sqlCaptor.getValue()).contains("ap_debit_memo_allocation_lines");
        assertThat(sqlCaptor.getValue()).contains("dma.status = 'CONFIRMED'");
        assertThat(sqlCaptor.getValue()).contains("debit_memo_applied_amount");
        assertThat(sqlCaptor.getValue()).contains("- COALESCE(dma.debit_memo_applied_amount, 0)");
        assertThat(sqlCaptor.getValue()).contains("raw_outstanding_amount < 0 THEN 0");
        assertThat(sqlCaptor.getValue()).contains("PARTIALLY_SETTLED");
        assertThat(sqlCaptor.getValue()).doesNotContain("HAVING");
        assertThat(paramCaptor.getValue().getValue("vendorBillIds")).isEqualTo(List.of(4L, 5L));
    }

    @Test
    void getSettlementSummaries_should_return_empty_map_for_empty_ids() {
        Map<Long, SettlementSummary> actual = adapter.getSettlementSummaries(List.of());

        assertThat(actual).isEmpty();
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void getSettlementSummary_should_return_single_summary() {
        SettlementSummary expected = new SettlementSummary(4L, new BigDecimal("50000000.00"), new BigDecimal("5000000.00"), new BigDecimal("20000000.00"), VendorBillSettlementStatus.PARTIALLY_SETTLED);
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(expected));

        SettlementSummary actual = adapter.getSettlementSummary(4L);

        assertThat(actual).isEqualTo(expected);
    }
}
