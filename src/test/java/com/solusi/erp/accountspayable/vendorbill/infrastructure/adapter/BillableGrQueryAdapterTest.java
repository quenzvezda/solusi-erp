package com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrLineView;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrQueryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillableGrQueryAdapterTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private BillableGrQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BillableGrQueryAdapter(jdbcTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBillableGrs_should_query_with_confirmed_status() {
        List<BillableGrView> expected = List.of(new BillableGrView(1L, "GR-1", 2L, "PO-1", 3L, 4L));
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn((List) expected);

        List<BillableGrView> actual = adapter.findBillableGrs(3L, 4L);

        assertThat(actual).isEqualTo(expected);
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(any(String.class), paramCaptor.capture(), any(RowMapper.class));
        assertThat(paramCaptor.getValue().getValue("confirmedStatus")).isEqualTo("CONFIRMED");
        assertThat(paramCaptor.getValue().getValue("vendorId")).isEqualTo(3L);
        assertThat(paramCaptor.getValue().getValue("currencyId")).isEqualTo(4L);
    }

    @Test
    void findBillableGrLines_should_map_rows() {
        List<BillableGrLineView> expected = List.of(new BillableGrLineView(
                501L, 99L, 301L, "Product A", "P-001", new BigDecimal("10.0000"), 1L, "PCS",
                new BigDecimal("5.0000"), new BigDecimal("50.0000"),
                new BigDecimal("5.0000"), new BigDecimal("55.0000"), new BigDecimal("4.0000")
        ));
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(expected);

        List<BillableGrLineView> actual = adapter.findBillableGrLines(99L);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sumConfirmedBilledQtyByGrId_should_return_map() {
        Map<Long, BigDecimal> expected = Map.of(11L, new BigDecimal("2.5000"));
        when(jdbcTemplate.query(any(String.class), any(MapSqlParameterSource.class), any(ResultSetExtractor.class)))
                .thenReturn((Map) expected);

        Map<Long, BigDecimal> actual = adapter.sumConfirmedBilledQtyByGrId(9L);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getGrLineData_should_return_quantity_and_grir_amount() {
        BillableGrQueryPort.GrLineData expected = new BillableGrQueryPort.GrLineData(
                new BigDecimal("7.0000"),
                new BigDecimal("70.0000")
        );
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(expected);

        BillableGrQueryPort.GrLineData actual = adapter.getGrLineData(15L);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void sumConfirmedLineTotals_should_honor_excludeBillId() {
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), eq(BigDecimal.class)))
                .thenReturn(new BigDecimal("120.0000"));

        BigDecimal actual = adapter.sumConfirmedLineTotals(21L, 8L);

        assertThat(actual).isEqualByComparingTo("120.0000");
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).queryForObject(any(String.class), paramCaptor.capture(), eq(BigDecimal.class));
        assertThat(paramCaptor.getValue().getValue("confirmedStatus")).isEqualTo("CONFIRMED");
        assertThat(paramCaptor.getValue().getValue("excludeBillId")).isEqualTo(8L);
    }

    @Test
    void sumConfirmedBilledQty_should_honor_excludeBillId() {
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), eq(BigDecimal.class)))
                .thenReturn(new BigDecimal("3.0000"));

        BigDecimal actual = adapter.sumConfirmedBilledQty(21L, null);

        assertThat(actual).isEqualByComparingTo("3.0000");
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).queryForObject(any(String.class), paramCaptor.capture(), eq(BigDecimal.class));
        assertThat(paramCaptor.getValue().getValue("confirmedStatus")).isEqualTo("CONFIRMED");
        assertThat(paramCaptor.getValue().getValue("excludeBillId")).isNull();
    }
}
