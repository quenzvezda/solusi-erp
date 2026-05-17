package com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrLineView;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrQueryPort;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrView;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class BillableGrQueryAdapter implements BillableGrQueryPort {

    private static final String STATUS_CONFIRMED = "CONFIRMED";

    private static final String SQL_FIND_BILLABLE_GRS = """
            SELECT DISTINCT
                gr.id AS gr_id,
                gr.code AS gr_code,
                po.id AS po_id,
                po.code AS po_code,
                gr.supplier_id AS vendor_id,
                gr.currency_id AS currency_id,
                gr.exchange_rate AS exchange_rate
            FROM pur_goods_receipts gr
            JOIN pur_goods_receipt_lines grl ON grl.header_id = gr.id
            LEFT JOIN pur_purchase_orders po ON po.id = gr.reference_id
            LEFT JOIN (
                SELECT vbl.gr_line_id, COALESCE(SUM(vbl.qty_billed), 0) AS billed_qty
                FROM ap_vendor_bill_lines vbl
                JOIN ap_vendor_bills vb ON vb.id = vbl.bill_id
                WHERE vb.status = :confirmedStatus
                GROUP BY vbl.gr_line_id
            ) billed ON billed.gr_line_id = grl.id
            WHERE gr.supplier_id = :vendorId
              AND gr.currency_id = :currencyId
              AND (grl.quantity_received - COALESCE(billed.billed_qty, 0)) > 0
            ORDER BY gr.id DESC
            """;

    private static final String SQL_FIND_BILLABLE_GR_LINES = """
            SELECT
                grl.id AS gr_line_id,
                grl.header_id AS gr_id,
                grl.product_id,
                p.name AS product_name,
                p.code AS product_code,
                grl.quantity_received,
                grl.uom_id,
                uom.name AS uom_name,
                grl.unit_price,
                grl.inventory_amount,
                grl.tax_amount,
                grl.gr_ir_amount,
                (grl.quantity_received - COALESCE(billed.billed_qty, 0)) AS outstanding_qty
            FROM pur_goods_receipt_lines grl
            JOIN products p ON p.id = grl.product_id
            JOIN unit_of_measures uom ON uom.id = grl.uom_id
            LEFT JOIN (
                SELECT vbl.gr_line_id, COALESCE(SUM(vbl.qty_billed), 0) AS billed_qty
                FROM ap_vendor_bill_lines vbl
                JOIN ap_vendor_bills vb ON vb.id = vbl.bill_id
                WHERE vb.status = :confirmedStatus
                GROUP BY vbl.gr_line_id
            ) billed ON billed.gr_line_id = grl.id
            WHERE grl.header_id = :grId
              AND (grl.quantity_received - COALESCE(billed.billed_qty, 0)) > 0
            ORDER BY grl.id
            """;

    private static final String SQL_SUM_CONFIRMED_BILLED_QTY_BY_GR_ID = """
            SELECT vbl.gr_line_id, COALESCE(SUM(vbl.qty_billed), 0) AS billed_qty
            FROM ap_vendor_bill_lines vbl
            JOIN ap_vendor_bills vb ON vb.id = vbl.bill_id
            JOIN pur_goods_receipt_lines grl ON grl.id = vbl.gr_line_id
            WHERE vb.status = :confirmedStatus
              AND grl.header_id = :grId
            GROUP BY vbl.gr_line_id
            """;

    private static final String SQL_GET_GR_LINE_DATA = """
            SELECT grl.quantity_received, grl.inventory_amount, grl.tax_amount, grl.gr_ir_amount
            FROM pur_goods_receipt_lines grl
            WHERE grl.id = :grLineId
            """;

    private static final String SQL_SUM_CONFIRMED_LINE_TOTALS = """
            SELECT COALESCE(SUM(vbl.line_total), 0)
            FROM ap_vendor_bill_lines vbl
            JOIN ap_vendor_bills vb ON vb.id = vbl.bill_id
            WHERE vb.status = :confirmedStatus
              AND vbl.gr_line_id = :grLineId
              AND (:excludeBillId IS NULL OR vb.id <> :excludeBillId)
            """;

    private static final String SQL_SUM_CONFIRMED_TAX_AMOUNTS = """
            SELECT COALESCE(SUM(vbl.tax_amount), 0)
            FROM ap_vendor_bill_lines vbl
            JOIN ap_vendor_bills vb ON vb.id = vbl.bill_id
            WHERE vb.status = :confirmedStatus
              AND vbl.gr_line_id = :grLineId
              AND (:excludeBillId IS NULL OR vb.id <> :excludeBillId)
            """;

    private static final String SQL_SUM_CONFIRMED_BILLED_QTY = """
            SELECT COALESCE(SUM(vbl.qty_billed), 0)
            FROM ap_vendor_bill_lines vbl
            JOIN ap_vendor_bills vb ON vb.id = vbl.bill_id
            WHERE vb.status = :confirmedStatus
              AND vbl.gr_line_id = :grLineId
              AND (:excludeBillId IS NULL OR vb.id <> :excludeBillId)
            """;

    private static final String SQL_GET_GR_EXCHANGE_RATE = """
            SELECT gr.exchange_rate
            FROM pur_goods_receipts gr
            JOIN pur_goods_receipt_lines grl ON grl.header_id = gr.id
            WHERE grl.id = :grLineId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BillableGrQueryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<BillableGrView> findBillableGrs(Long vendorId, Long currencyId) {
        MapSqlParameterSource params = baseParams()
                .addValue("vendorId", vendorId)
                .addValue("currencyId", currencyId);
        return jdbcTemplate.query(SQL_FIND_BILLABLE_GRS, params, billableGrRowMapper());
    }

    @Override
    public List<BillableGrLineView> findBillableGrLines(Long grId) {
        MapSqlParameterSource params = baseParams().addValue("grId", grId);
        return jdbcTemplate.query(SQL_FIND_BILLABLE_GR_LINES, params, billableGrLineRowMapper());
    }

    @Override
    public Map<Long, BigDecimal> sumConfirmedBilledQtyByGrId(Long grId) {
        MapSqlParameterSource params = baseParams().addValue("grId", grId);
        return jdbcTemplate.query(SQL_SUM_CONFIRMED_BILLED_QTY_BY_GR_ID, params, rs -> {
            java.util.LinkedHashMap<Long, BigDecimal> totals = new java.util.LinkedHashMap<>();
            while (rs.next()) {
                totals.put(rs.getLong("gr_line_id"), rs.getBigDecimal("billed_qty"));
            }
            return totals;
        });
    }

    @Override
    public GrLineData getGrLineData(Long grLineId) {
        MapSqlParameterSource params = new MapSqlParameterSource("grLineId", grLineId);
        return jdbcTemplate.queryForObject(SQL_GET_GR_LINE_DATA, params,
                (rs, rowNum) -> new GrLineData(rs.getBigDecimal("quantity_received"), rs.getBigDecimal("inventory_amount"), rs.getBigDecimal("tax_amount"), rs.getBigDecimal("gr_ir_amount")));
    }

    @Override
    public BigDecimal sumConfirmedLineTotals(Long grLineId, Long excludeBillId) {
        MapSqlParameterSource params = baseParams()
                .addValue("grLineId", grLineId)
                .addValue("excludeBillId", excludeBillId);
        return jdbcTemplate.queryForObject(SQL_SUM_CONFIRMED_LINE_TOTALS, params, BigDecimal.class);
    }

    @Override
    public BigDecimal sumConfirmedTaxAmounts(Long grLineId, Long excludeBillId) {
        MapSqlParameterSource params = baseParams()
                .addValue("grLineId", grLineId)
                .addValue("excludeBillId", excludeBillId);
        return jdbcTemplate.queryForObject(SQL_SUM_CONFIRMED_TAX_AMOUNTS, params, BigDecimal.class);
    }

    @Override
    public BigDecimal sumConfirmedBilledQty(Long grLineId, Long excludeBillId) {
        MapSqlParameterSource params = baseParams()
                .addValue("grLineId", grLineId)
                .addValue("excludeBillId", excludeBillId);
        return jdbcTemplate.queryForObject(SQL_SUM_CONFIRMED_BILLED_QTY, params, BigDecimal.class);
    }

    @Override
    public BigDecimal getGrExchangeRate(Long grLineId) {
        MapSqlParameterSource params = new MapSqlParameterSource("grLineId", grLineId);
        return jdbcTemplate.queryForObject(SQL_GET_GR_EXCHANGE_RATE, params, BigDecimal.class);
    }

    private static MapSqlParameterSource baseParams() {
        return new MapSqlParameterSource("confirmedStatus", STATUS_CONFIRMED);
    }

    private static RowMapper<BillableGrView> billableGrRowMapper() {
        return (rs, rowNum) -> new BillableGrView(
                rs.getLong("gr_id"),
                rs.getString("gr_code"),
                rs.getLong("po_id"),
                rs.getString("po_code"),
                rs.getLong("vendor_id"),
                rs.getLong("currency_id"),
                rs.getBigDecimal("exchange_rate")
        );
    }

    private static RowMapper<BillableGrLineView> billableGrLineRowMapper() {
        return (rs, rowNum) -> new BillableGrLineView(
                rs.getLong("gr_line_id"),
                rs.getLong("gr_id"),
                rs.getLong("product_id"),
                rs.getString("product_name"),
                rs.getString("product_code"),
                rs.getBigDecimal("quantity_received"),
                rs.getLong("uom_id"),
                rs.getString("uom_name"),
                rs.getBigDecimal("unit_price"),
                rs.getBigDecimal("inventory_amount"),
                rs.getBigDecimal("tax_amount"),
                rs.getBigDecimal("gr_ir_amount"),
                rs.getBigDecimal("outstanding_qty")
        );
    }
}
