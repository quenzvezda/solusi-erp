package com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import com.solusi.erp.core.domain.model.AuditMetadata;
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
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorBillPaymentUpdateAdapterTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private VendorBillPaymentUpdateAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new VendorBillPaymentUpdateAdapter(jdbcTemplate);
    }

    @Test
    void lockAndValidatePayment_should_lock_bills_and_validate_current_outstanding() throws Exception {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    String sql = invocation.getArgument(0);
                    if (sql.contains("FOR UPDATE")) {
                        return List.of(10L);
                    }
                    RowMapper<?> mapper = invocation.getArgument(2);
                    ResultSet rs = resultSetForOpenBill();
                    return List.of(mapper.mapRow(rs, 0));
                });

        adapter.lockAndValidatePayment(payment());

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, org.mockito.Mockito.times(2))
                .query(sqlCaptor.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        assertThat(sqlCaptor.getAllValues().get(0)).contains("FOR UPDATE");
        assertThat(sqlCaptor.getAllValues().get(1)).contains("vb.document_status", "vb.settlement_status", "outstanding_amount");
    }

    @Test
    void updateSettlementStatus_should_update_settlement_status_only() {
        adapter.updateSettlementStatus(List.of(10L, 11L));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), paramsCaptor.capture());
        assertThat(sqlCaptor.getValue()).contains("SET vb.settlement_status = CASE");
        assertThat(sqlCaptor.getValue()).contains("THEN 'SETTLED'");
        assertThat(sqlCaptor.getValue()).contains("THEN 'PARTIALLY_SETTLED'");
        assertThat(sqlCaptor.getValue()).contains("ELSE 'OPEN'");
        assertThat(sqlCaptor.getValue()).contains("vb.document_status = 'CONFIRMED'");
        assertThat(sqlCaptor.getValue()).doesNotContain("vb.status");
        assertThat(paramsCaptor.getValue().getValue("billIds")).isEqualTo(List.of(10L, 11L));
    }

    private ResultSet resultSetForOpenBill() throws Exception {
        ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(10L);
        when(rs.getLong("vendor_id")).thenReturn(1L);
        when(rs.getLong("currency_id")).thenReturn(2L);
        when(rs.getString("document_status")).thenReturn("CONFIRMED");
        when(rs.getString("settlement_status")).thenReturn("OPEN");
        when(rs.getBigDecimal("total_amount")).thenReturn(new BigDecimal("500.0000"));
        when(rs.getBigDecimal("paid_amount")).thenReturn(BigDecimal.ZERO);
        when(rs.getBigDecimal("outstanding_amount")).thenReturn(new BigDecimal("500.0000"));
        return rs;
    }

    private VendorPayment payment() {
        return new VendorPayment(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "VP-001",
                1L,
                2L,
                3L,
                LocalDate.of(2026, 6, 1),
                BigDecimal.ONE,
                new BigDecimal("500.0000"),
                VendorPaymentStatus.CONFIRMED,
                "REF",
                "notes",
                List.of(new VendorPaymentLine(null, 10L, "VB-001", new BigDecimal("500.0000"), new BigDecimal("500.0000")))
        );
    }
}
