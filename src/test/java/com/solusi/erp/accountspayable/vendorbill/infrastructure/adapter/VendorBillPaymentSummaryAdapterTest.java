package com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorbill.domain.port.VendorBillPaymentSummaryPort.PaymentSummary;
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
class VendorBillPaymentSummaryAdapterTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private VendorBillPaymentSummaryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new VendorBillPaymentSummaryAdapter(jdbcTemplate);
    }

    @Test
    void getPaymentSummaries_should_query_confirmed_vendor_payments_without_outstanding_filter() {
        List<PaymentSummary> expected = List.of(
                new PaymentSummary(4L, new BigDecimal("50000000.00"), new BigDecimal("25000000.00")),
                new PaymentSummary(5L, new BigDecimal("10000000.00"), BigDecimal.ZERO)
        );
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(expected);

        Map<Long, PaymentSummary> actual = adapter.getPaymentSummaries(List.of(4L, 5L));

        assertThat(actual).containsEntry(4L, expected.get(0));
        assertThat(actual).containsEntry(5L, expected.get(1));
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramCaptor.capture(), any(RowMapper.class));
        assertThat(sqlCaptor.getValue()).contains("vp.status = 'CONFIRMED'");
        assertThat(sqlCaptor.getValue()).doesNotContain("HAVING");
        assertThat(paramCaptor.getValue().getValue("vendorBillIds")).isEqualTo(List.of(4L, 5L));
    }

    @Test
    void getPaymentSummaries_should_return_empty_map_for_empty_ids() {
        Map<Long, PaymentSummary> actual = adapter.getPaymentSummaries(List.of());

        assertThat(actual).isEmpty();
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void getPaymentSummary_should_return_single_summary() {
        PaymentSummary expected = new PaymentSummary(4L, new BigDecimal("50000000.00"), new BigDecimal("25000000.00"));
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(expected));

        PaymentSummary actual = adapter.getPaymentSummary(4L);

        assertThat(actual).isEqualTo(expected);
    }
}
