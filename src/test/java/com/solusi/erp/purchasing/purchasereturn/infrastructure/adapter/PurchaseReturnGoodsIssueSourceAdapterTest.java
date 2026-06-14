package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseReturnGoodsIssueSourceAdapterTest {

    @Mock private NamedParameterJdbcTemplate jdbcTemplate;

    private PurchaseReturnGoodsIssueSourceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PurchaseReturnGoodsIssueSourceAdapter(jdbcTemplate);
    }

    @Test
    void findHeader_exposesApprovedReturnAndTemporaryPhaseOneMetadata() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        adapter.findHeader(1L);

        SqlCapture capture = captureQuery();
        assertThat(capture.sql()).contains("pr.status = 'APPROVED'");
        assertThat(capture.sql()).contains("FALSE AS bill_posted");
        assertThat(capture.sql()).contains("NULL AS clearing_account_id");
        assertThat(capture.params().getValue("purchaseReturnId")).isEqualTo(1L);
    }

    @Test
    void findEligibleLines_forwardsActualLocationAndOriginalGrValuationSnapshots() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        adapter.findEligibleLines(1L);

        SqlCapture capture = captureQuery();
        assertThat(capture.sql()).contains("prl.container_id");
        assertThat(capture.sql()).contains("pr.reference_id AS original_goods_receipt_id");
        assertThat(capture.sql()).contains("prl.valuation_ref_line_id");
        assertThat(capture.sql()).contains("prl.tax_reversal_amount");
    }

    @Test
    void hasCompletedGoodsIssue_checksOnlyCompletedPurchaseReturnGi() {
        when(jdbcTemplate.queryForObject(
                any(String.class), any(MapSqlParameterSource.class), eq(Long.class))).thenReturn(1L);

        assertThat(adapter.hasCompletedGoodsIssue(1L)).isTrue();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(
                sql.capture(), any(MapSqlParameterSource.class), eq(Long.class));
        assertThat(sql.getValue()).contains("gi.reference_type = 'PURCHASE_RETURN'");
        assertThat(sql.getValue()).contains("gi.status = 'COMPLETED'");
    }

    private SqlCapture captureQuery() {
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sql.capture(), params.capture(), any(RowMapper.class));
        return new SqlCapture(sql.getValue(), params.getValue());
    }

    private record SqlCapture(String sql, MapSqlParameterSource params) {
    }
}
