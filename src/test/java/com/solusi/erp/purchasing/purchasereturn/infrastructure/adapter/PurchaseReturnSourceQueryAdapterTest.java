package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.EligibleGoodsReceiptRow;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableGrLineSlice;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableSerialRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseReturnSourceQueryAdapterTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private PurchaseReturnSourceQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PurchaseReturnSourceQueryAdapter(jdbcTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findEligibleGoodsReceipts_filtersCompletedStockReservationsSupplierPoAndDates() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        adapter.findEligibleGoodsReceipts("GR", 3L, 4L,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2));

        SqlCapture capture = captureQuery();
        assertThat(capture.sql()).contains("gr.status = :completedStatus");
        assertThat(capture.sql()).contains("inv_stock_reservations");
        assertThat(capture.sql()).contains("vl.remaining_quantity - COALESCE(reserved.reserved_qty, 0)");
        assertThat(capture.params().getValue("completedStatus")).isEqualTo("COMPLETED");
        assertThat(capture.params().getValue("activeReservationStatus")).isEqualTo("ACTIVE");
        assertThat(capture.params().getValue("supplierId")).isEqualTo(3L);
        assertThat(capture.params().getValue("purchaseOrderId")).isEqualTo(4L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findReturnableGrLineSlices_groupsByActualContainerAndExcludesSelectionKeysInQuery() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        adapter.findReturnableGrLineSlices(1L, "product", List.of("11:22"));

        SqlCapture capture = captureQuery();
        assertThat(capture.sql()).contains("grl.is_serialized = FALSE");
        assertThat(capture.sql()).contains("SUM(remaining_quantity) AS remaining_quantity");
        assertThat(capture.sql()).contains("CAST(vl.container_id AS CHAR)");
        assertThat(capture.sql()).contains("NOT IN (:excludedKeys)");
        assertThat(capture.sql()).contains("GROUP BY");
        assertThat(capture.params().getValue("excludedKeys")).isEqualTo(List.of("11:22"));
        assertThat(capture.params().getValue("excludedKeysEmpty")).isEqualTo(false);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findReturnableSerials_usesCurrentLayerContainerAndExcludesReservedOrSelectedSerials() {
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        adapter.findReturnableSerials(1L, 11L, "SER", List.of("11:22:SER-001"));

        SqlCapture capture = captureQuery();
        assertThat(capture.sql()).contains("container.id = vl.container_id");
        assertThat(capture.sql()).contains("grl.is_serialized = TRUE");
        assertThat(capture.sql()).contains("vl.serial_number IS NOT NULL");
        assertThat(capture.sql()).contains("NOT IN (:excludedKeys)");
        assertThat(capture.params().getValue("grLineId")).isEqualTo(11L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findEligiblePurchaseOrders_limitsLookupAndUsesHumanReadableCode() {
        List<LookupDto> expected = List.of(new LookupDto(4L, "PO-001", "PO-001"));
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn((List) expected);

        List<LookupDto> actual = adapter.findEligiblePurchaseOrders("PO", 10);

        assertThat(actual).isEqualTo(expected);
        SqlCapture capture = captureQuery();
        assertThat(capture.sql()).contains("LIMIT :limit");
        assertThat(capture.params().getValue("limit")).isEqualTo(10);
    }

    private SqlCapture captureQuery() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(), any(RowMapper.class));
        return new SqlCapture(sqlCaptor.getValue(), paramsCaptor.getValue());
    }

    private record SqlCapture(String sql, MapSqlParameterSource params) {
    }
}
