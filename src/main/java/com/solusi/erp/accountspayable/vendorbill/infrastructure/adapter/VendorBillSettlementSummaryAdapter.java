package com.solusi.erp.accountspayable.vendorbill.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.port.VendorBillSettlementSummaryPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VendorBillSettlementSummaryAdapter implements VendorBillSettlementSummaryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public VendorBillSettlementSummaryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SettlementSummary getSettlementSummary(Long vendorBillId) {
        if (vendorBillId == null) return null;
        return getSettlementSummaries(List.of(vendorBillId)).get(vendorBillId);
    }

    @Override
    public Map<Long, SettlementSummary> getSettlementSummaries(List<Long> vendorBillIds) {
        if (vendorBillIds == null || vendorBillIds.isEmpty()) return Map.of();

        String sql = """
                SELECT s.vendor_bill_id,
                       s.paid_amount,
                       s.debit_memo_applied_amount,
                       CASE
                           WHEN s.raw_outstanding_amount < 0 THEN 0
                           ELSE s.raw_outstanding_amount
                       END AS outstanding_amount,
                       CASE
                           WHEN s.document_status <> 'CONFIRMED' THEN NULL
                           WHEN s.raw_outstanding_amount <= 0 THEN 'SETTLED'
                           WHEN s.paid_amount + s.debit_memo_applied_amount > 0 THEN 'PARTIALLY_SETTLED'
                           ELSE 'OPEN'
                       END AS settlement_status
                FROM (
                    SELECT vb.id AS vendor_bill_id,
                           vb.document_status,
                           COALESCE(payment.paid_amount, 0) AS paid_amount,
                           COALESCE(dma.debit_memo_applied_amount, 0) AS debit_memo_applied_amount,
                           vb.total_amount
                               - COALESCE(payment.paid_amount, 0)
                               - COALESCE(dma.debit_memo_applied_amount, 0) AS raw_outstanding_amount
                    FROM ap_vendor_bills vb
                    LEFT JOIN (
                        SELECT vpl.vendor_bill_id, SUM(vpl.paid_amount) AS paid_amount
                        FROM ap_vendor_payment_lines vpl
                        JOIN ap_vendor_payments vp ON vp.id = vpl.vendor_payment_id
                        WHERE vp.status = 'CONFIRMED'
                        GROUP BY vpl.vendor_bill_id
                    ) payment ON payment.vendor_bill_id = vb.id
                    LEFT JOIN (
                        SELECT dmal.vendor_bill_id, SUM(dmal.applied_gross_original) AS debit_memo_applied_amount
                        FROM ap_debit_memo_allocation_lines dmal
                        JOIN ap_debit_memo_allocations dma ON dma.id = dmal.debit_memo_allocation_id
                        WHERE dma.status = 'CONFIRMED'
                        GROUP BY dmal.vendor_bill_id
                    ) dma ON dma.vendor_bill_id = vb.id
                    WHERE vb.id IN (:vendorBillIds)
                ) s
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("vendorBillIds", vendorBillIds);

        List<SettlementSummary> summaries = jdbcTemplate.query(sql, params, (rs, rowNum) -> new SettlementSummary(
                rs.getLong("vendor_bill_id"),
                rs.getBigDecimal("paid_amount"),
                rs.getBigDecimal("debit_memo_applied_amount"),
                rs.getBigDecimal("outstanding_amount"),
                toSettlementStatus(rs.getString("settlement_status"))
        ));

        Map<Long, SettlementSummary> result = new HashMap<>();
        for (SettlementSummary summary : summaries) {
            result.put(summary.vendorBillId(), summary);
        }
        return result;
    }

    private VendorBillSettlementStatus toSettlementStatus(String status) {
        return status == null ? null : VendorBillSettlementStatus.valueOf(status);
    }
}
