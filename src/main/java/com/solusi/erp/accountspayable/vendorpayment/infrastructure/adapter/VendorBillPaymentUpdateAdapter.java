package com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

public class VendorBillPaymentUpdateAdapter implements VendorBillPaymentUpdatePort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public VendorBillPaymentUpdateAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void updatePaymentStatus(List<Long> vendorBillIds) {
        if (vendorBillIds == null || vendorBillIds.isEmpty()) return;

        String sql = """
                UPDATE ap_vendor_bills vb
                SET vb.status = CASE
                    WHEN (SELECT COALESCE(SUM(vpl.paid_amount), 0)
                          FROM ap_vendor_payment_lines vpl
                          JOIN ap_vendor_payments vp ON vp.id = vpl.vendor_payment_id
                          WHERE vpl.vendor_bill_id = vb.id AND vp.status = 'CONFIRMED'
                         ) >= vb.total_amount THEN 'PAID'
                    WHEN (SELECT COALESCE(SUM(vpl.paid_amount), 0)
                          FROM ap_vendor_payment_lines vpl
                          JOIN ap_vendor_payments vp ON vp.id = vpl.vendor_payment_id
                          WHERE vpl.vendor_bill_id = vb.id AND vp.status = 'CONFIRMED'
                         ) > 0 THEN 'PARTIAL_PAID'
                    ELSE 'CONFIRMED'
                END
                WHERE vb.id IN (:billIds)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("billIds", vendorBillIds);

        jdbcTemplate.update(sql, params);
    }
}
