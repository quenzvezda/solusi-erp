package com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableApReference;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableApReferenceProvider;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

public class GoodsReceiptBillableReferenceProvider implements BillableApReferenceProvider {

    private static final String SOURCE_TYPE = "GOODS_RECEIPT";
    private static final String STATUS_CONFIRMED = "CONFIRMED";

    private static final String SQL_FIND_REFERENCES = """
            SELECT
                gr.id AS source_id,
                gr.code AS source_code,
                gr.receipt_date AS source_date,
                gr.supplier_id AS vendor_id,
                p.name AS vendor_name,
                gr.currency_id AS currency_id,
                c.code AS currency_code,
                gr.exchange_rate AS exchange_rate,
                COALESCE(SUM((grl.quantity_received - COALESCE(billed.billed_qty, 0))
                    * (grl.gr_ir_amount / NULLIF(grl.quantity_received, 0))), 0) AS outstanding_amount,
                COUNT(CASE WHEN (grl.quantity_received - COALESCE(billed.billed_qty, 0)) > 0 THEN 1 END) AS outstanding_line_count,
                gr.status AS status
            FROM pur_goods_receipts gr
            JOIN pur_goods_receipt_lines grl ON grl.header_id = gr.id
            JOIN parties p ON p.id = gr.supplier_id
            JOIN master_currencies c ON c.id = gr.currency_id
            LEFT JOIN (
                SELECT vbl.gr_line_id, COALESCE(SUM(vbl.qty_billed), 0) AS billed_qty
                FROM ap_vendor_bill_lines vbl
                JOIN ap_vendor_bills vb ON vb.id = vbl.bill_id
                WHERE vb.status = :confirmedStatus
                GROUP BY vbl.gr_line_id
            ) billed ON billed.gr_line_id = grl.id
            WHERE (:vendorId IS NULL OR gr.supplier_id = :vendorId)
              AND (:currencyId IS NULL OR gr.currency_id = :currencyId)
              AND gr.status = 'COMPLETED'
              AND (grl.quantity_received - COALESCE(billed.billed_qty, 0)) > 0
            GROUP BY gr.id, gr.code, gr.receipt_date, gr.supplier_id, p.name,
                     gr.currency_id, c.code, gr.exchange_rate, gr.status
            ORDER BY gr.receipt_date DESC, gr.id DESC
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public GoodsReceiptBillableReferenceProvider(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getSourceType() {
        return SOURCE_TYPE;
    }

    @Override
    public List<BillableApReference> findBillableReferences(Long vendorId, Long currencyId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("confirmedStatus", STATUS_CONFIRMED)
                .addValue("vendorId", vendorId)
                .addValue("currencyId", currencyId);
        return jdbcTemplate.query(SQL_FIND_REFERENCES, params, rowMapper());
    }

    private static RowMapper<BillableApReference> rowMapper() {
        return (rs, rowNum) -> new BillableApReference(
                SOURCE_TYPE,
                rs.getLong("source_id"),
                rs.getString("source_code"),
                rs.getDate("source_date").toLocalDate(),
                rs.getLong("vendor_id"),
                rs.getString("vendor_name"),
                rs.getLong("currency_id"),
                rs.getString("currency_code"),
                rs.getBigDecimal("exchange_rate"),
                rs.getBigDecimal("outstanding_amount"),
                rs.getInt("outstanding_line_count"),
                rs.getString("status")
        );
    }
}
