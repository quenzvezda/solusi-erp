package com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter;

import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.core.exception.DomainException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VendorBillPaymentUpdateAdapter implements VendorBillPaymentUpdatePort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public VendorBillPaymentUpdateAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void lockAndValidatePayment(VendorPayment payment) {
        if (payment == null || payment.getLines() == null || payment.getLines().isEmpty()) {
            throw new DomainException("msg.err.vp.lines.required");
        }

        Map<Long, BigDecimal> requestedAmountByBillId = new HashMap<>();
        for (VendorPaymentLine line : payment.getLines()) {
            BigDecimal previous = requestedAmountByBillId.putIfAbsent(line.getVendorBillId(), line.getPaidAmount());
            if (previous != null) {
                throw new DomainException("msg.err.vp.vendor.bill.duplicate");
            }
        }

        List<Long> vendorBillIds = requestedAmountByBillId.keySet().stream().toList();
        lockVendorBills(vendorBillIds);
        Map<Long, LockedVendorBill> lockedBills = loadLockedVendorBills(vendorBillIds);

        if (lockedBills.size() != vendorBillIds.size()) {
            throw new DomainException("msg.err.vp.vendor.bill.not.found");
        }

        for (VendorPaymentLine line : payment.getLines()) {
            LockedVendorBill bill = lockedBills.get(line.getVendorBillId());
            validateLockedBill(payment, line, bill);
        }
    }

    @Override
    public void updateSettlementStatus(List<Long> vendorBillIds) {
        if (vendorBillIds == null || vendorBillIds.isEmpty()) return;

        String sql = """
                UPDATE ap_vendor_bills vb
                SET vb.settlement_status = CASE
                    WHEN ((
                          SELECT COALESCE(SUM(vpl.paid_amount), 0)
                          FROM ap_vendor_payment_lines vpl
                          JOIN ap_vendor_payments vp ON vp.id = vpl.vendor_payment_id
                          WHERE vpl.vendor_bill_id = vb.id AND vp.status = 'CONFIRMED'
                         ) + (
                          SELECT COALESCE(SUM(dmal.applied_gross_original), 0)
                          FROM ap_debit_memo_allocation_lines dmal
                          JOIN ap_debit_memo_allocations dma ON dma.id = dmal.debit_memo_allocation_id
                          WHERE dmal.vendor_bill_id = vb.id AND dma.status = 'CONFIRMED'
                         ) >= vb.total_amount THEN 'SETTLED'
                    WHEN ((
                          SELECT COALESCE(SUM(vpl.paid_amount), 0)
                          FROM ap_vendor_payment_lines vpl
                          JOIN ap_vendor_payments vp ON vp.id = vpl.vendor_payment_id
                          WHERE vpl.vendor_bill_id = vb.id AND vp.status = 'CONFIRMED'
                         ) + (
                          SELECT COALESCE(SUM(dmal.applied_gross_original), 0)
                          FROM ap_debit_memo_allocation_lines dmal
                          JOIN ap_debit_memo_allocations dma ON dma.id = dmal.debit_memo_allocation_id
                          WHERE dmal.vendor_bill_id = vb.id AND dma.status = 'CONFIRMED'
                         ) > 0 THEN 'PARTIALLY_SETTLED'
                    ELSE 'OPEN'
                END
                WHERE vb.id IN (:billIds)
                  AND vb.document_status = 'CONFIRMED'
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("billIds", vendorBillIds);

        jdbcTemplate.update(sql, params);
    }

    private void lockVendorBills(List<Long> vendorBillIds) {
        String sql = """
                SELECT vb.id
                FROM ap_vendor_bills vb
                WHERE vb.id IN (:billIds)
                FOR UPDATE
                """;
        jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("billIds", vendorBillIds), (rs, rowNum) -> rs.getLong("id"));
    }

    private Map<Long, LockedVendorBill> loadLockedVendorBills(List<Long> vendorBillIds) {
        String sql = """
                SELECT vb.id,
                       vb.vendor_id,
                       vb.currency_id,
                       vb.document_status,
                       vb.settlement_status,
                       vb.total_amount,
                       COALESCE(payment.paid_amount, 0) AS paid_amount,
                       vb.total_amount
                           - COALESCE(payment.paid_amount, 0)
                           - COALESCE(dma.debit_memo_applied_amount, 0) AS outstanding_amount
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
                WHERE vb.id IN (:billIds)
                """;

        List<LockedVendorBill> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource().addValue("billIds", vendorBillIds),
                (rs, rowNum) -> new LockedVendorBill(
                        rs.getLong("id"),
                        rs.getLong("vendor_id"),
                        rs.getLong("currency_id"),
                        rs.getString("document_status"),
                        rs.getString("settlement_status"),
                        rs.getBigDecimal("total_amount"),
                        rs.getBigDecimal("paid_amount"),
                        rs.getBigDecimal("outstanding_amount")
                )
        );
        return rows.stream().collect(Collectors.toMap(LockedVendorBill::id, row -> row));
    }

    private void validateLockedBill(VendorPayment payment, VendorPaymentLine line, LockedVendorBill bill) {
        if (!payment.getVendorId().equals(bill.vendorId())) {
            throw new DomainException("msg.err.vp.vendor.bill.vendor.mismatch");
        }
        if (!payment.getCurrencyId().equals(bill.currencyId())) {
            throw new DomainException("msg.err.vp.vendor.bill.currency.mismatch");
        }
        if (!"CONFIRMED".equals(bill.documentStatus())) {
            throw new DomainException("msg.err.vp.vendor.bill.not.payable");
        }
        Set<String> openStatuses = Set.of("OPEN", "PARTIALLY_SETTLED");
        if (!openStatuses.contains(bill.settlementStatus())) {
            throw new DomainException("msg.err.vp.vendor.bill.settled");
        }
        if (bill.outstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.vendor-bill.outstanding.changed");
        }
        if (line.getPaidAmount().compareTo(bill.outstandingAmount()) > 0) {
            throw new DomainException("msg.error.vendor-bill.outstanding.changed");
        }
    }

    private record LockedVendorBill(
            Long id,
            Long vendorId,
            Long currencyId,
            String documentStatus,
            String settlementStatus,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount
    ) {}
}
