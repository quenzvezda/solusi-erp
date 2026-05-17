package com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorbill.domain.port.VendorBillPaymentSummaryPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VendorBillPaymentSummaryAdapter implements VendorBillPaymentSummaryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public VendorBillPaymentSummaryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PaymentSummary getPaymentSummary(Long vendorBillId) {
        if (vendorBillId == null) return null;
        return getPaymentSummaries(List.of(vendorBillId)).get(vendorBillId);
    }

    @Override
    public Map<Long, PaymentSummary> getPaymentSummaries(List<Long> vendorBillIds) {
        if (vendorBillIds == null || vendorBillIds.isEmpty()) return Map.of();

        String sql = """
                SELECT vb.id AS vendor_bill_id,
                       COALESCE(SUM(vpl.paid_amount), 0) AS paid_amount,
                       vb.total_amount - COALESCE(SUM(vpl.paid_amount), 0) AS outstanding_amount
                FROM ap_vendor_bills vb
                LEFT JOIN ap_vendor_payment_lines vpl ON vpl.vendor_bill_id = vb.id
                    AND vpl.vendor_payment_id IN (
                        SELECT vp.id FROM ap_vendor_payments vp WHERE vp.status = 'CONFIRMED'
                    )
                WHERE vb.id IN (:vendorBillIds)
                GROUP BY vb.id, vb.total_amount
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("vendorBillIds", vendorBillIds);

        List<PaymentSummary> summaries = jdbcTemplate.query(sql, params, (rs, rowNum) -> new PaymentSummary(
                rs.getLong("vendor_bill_id"),
                rs.getBigDecimal("paid_amount"),
                rs.getBigDecimal("outstanding_amount")
        ));

        Map<Long, PaymentSummary> result = new HashMap<>();
        for (PaymentSummary summary : summaries) {
            result.put(summary.vendorBillId(), summary);
        }
        return result;
    }
}
