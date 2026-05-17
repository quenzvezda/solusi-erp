package com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

public class PayableVendorBillQueryAdapter implements PayableVendorBillQueryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PayableVendorBillQueryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PayableVendorBillView> findPayableVendorBills(Long vendorId, Long currencyId) {
        String sql = """
                SELECT vb.id AS vendor_bill_id,
                       vb.code AS bill_code,
                       vb.total_amount,
                       COALESCE(SUM(vpl.paid_amount), 0) AS paid_amount,
                       vb.total_amount - COALESCE(SUM(vpl.paid_amount), 0) AS outstanding_amount
                FROM ap_vendor_bills vb
                LEFT JOIN ap_vendor_payment_lines vpl ON vpl.vendor_bill_id = vb.id
                    AND vpl.vendor_payment_id IN (
                        SELECT vp.id FROM ap_vendor_payments vp WHERE vp.status = 'CONFIRMED'
                    )
                WHERE vb.vendor_id = :vendorId
                  AND vb.currency_id = :currencyId
                  AND vb.status IN ('CONFIRMED', 'PARTIAL_PAID')
                GROUP BY vb.id, vb.code, vb.total_amount
                HAVING vb.total_amount - COALESCE(SUM(vpl.paid_amount), 0) > 0
                ORDER BY vb.code
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("vendorId", vendorId)
                .addValue("currencyId", currencyId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new PayableVendorBillView(
                rs.getLong("vendor_bill_id"),
                rs.getString("bill_code"),
                rs.getBigDecimal("total_amount"),
                rs.getBigDecimal("paid_amount"),
                rs.getBigDecimal("outstanding_amount")
        ));
    }
}
