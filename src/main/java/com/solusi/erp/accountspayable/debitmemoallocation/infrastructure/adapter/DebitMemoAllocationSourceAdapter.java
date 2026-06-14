package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.adapter;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class DebitMemoAllocationSourceAdapter implements DebitMemoAllocationSourcePort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DebitMemoAllocationSourceAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void lockDebitMemo(Long debitMemoId) {
        String sql = """
                SELECT dm.id
                FROM ap_debit_memos dm
                WHERE dm.id = :debitMemoId
                FOR UPDATE
                """;
        jdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("debitMemoId", debitMemoId),
                (rs, rowNum) -> rs.getLong("id"));
    }

    @Override
    public void lockVendorBills(Collection<Long> vendorBillIds) {
        if (vendorBillIds == null || vendorBillIds.isEmpty()) {
            return;
        }
        String sql = """
                SELECT vb.id
                FROM ap_vendor_bills vb
                WHERE vb.id IN (:vendorBillIds)
                FOR UPDATE
                """;
        jdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("vendorBillIds", vendorBillIds),
                (rs, rowNum) -> rs.getLong("id"));
    }

    @Override
    public Optional<DebitMemoSnapshot> findDebitMemoSnapshot(Long debitMemoId) {
        String sql = debitMemoSnapshotSql() + " WHERE dm.id = :debitMemoId";
        List<DebitMemoSnapshot> rows = jdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("debitMemoId", debitMemoId),
                (rs, rowNum) -> new DebitMemoSnapshot(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getLong("vendor_id"),
                        rs.getLong("currency_id"),
                        rs.getBigDecimal("gross_amount_original"),
                        rs.getBigDecimal("dpp_amount_original"),
                        rs.getBigDecimal("tax_amount_original"),
                        rs.getBigDecimal("dpp_amount_base"),
                        rs.getBigDecimal("tax_amount_base"),
                        rs.getBigDecimal("remaining_amount_original"),
                        rs.getBigDecimal("applied_gross_original"),
                        rs.getBigDecimal("applied_dpp_original"),
                        rs.getBigDecimal("applied_tax_original"),
                        rs.getBigDecimal("grir_reversal_base"),
                        rs.getBigDecimal("tax_reversal_base")
                ));
        return rows.stream().findFirst();
    }

    @Override
    public Optional<VendorBillSnapshot> findVendorBillSnapshot(Long vendorBillId) {
        String sql = vendorBillSnapshotSql() + " WHERE s.id = :vendorBillId";
        List<VendorBillSnapshot> rows = jdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("vendorBillId", vendorBillId),
                (rs, rowNum) -> new VendorBillSnapshot(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getLong("vendor_id"),
                        rs.getLong("currency_id"),
                        rs.getBigDecimal("total_amount"),
                        rs.getBigDecimal("exchange_rate"),
                        rs.getBigDecimal("outstanding_amount")
                ));
        return rows.stream().findFirst();
    }

    @Override
    public Page<EligibleVendorBill> findEligibleVendorBills(Long debitMemoId, String keyword, Pageable pageable) {
        DebitMemoSnapshot debitMemo = findDebitMemoSnapshot(debitMemoId).orElse(null);
        if (debitMemo == null) {
            return new Page<>(List.of(), pageable.page(), pageable.size(), 0);
        }
        String sql = vendorBillSnapshotSql() + """
                WHERE s.vendor_id = :vendorId
                  AND s.currency_id = :currencyId
                  AND s.document_status = 'CONFIRMED'
                  AND s.settlement_status IN ('OPEN', 'PARTIALLY_SETTLED')
                  AND s.outstanding_amount > 0
                  AND (:keyword IS NULL OR :keyword = '' OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
                ORDER BY s.code
                LIMIT :limit OFFSET :offset
                """;
        MapSqlParameterSource params = pagingParams(keyword, pageable)
                .addValue("vendorId", debitMemo.vendorId())
                .addValue("currencyId", debitMemo.currencyId());
        List<EligibleVendorBill> rows = jdbcTemplate.query(sql, params, (rs, rowNum) -> new EligibleVendorBill(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getBigDecimal("total_amount"),
                rs.getBigDecimal("outstanding_amount"),
                rs.getBigDecimal("exchange_rate")
        ));
        return new Page<>(rows, pageable.page(), pageable.size(), rows.size());
    }

    @Override
    public Page<EligibleDebitMemo> findEligibleDebitMemos(Long vendorBillId, String keyword, Pageable pageable) {
        VendorBillSnapshot vendorBill = findVendorBillSnapshot(vendorBillId).orElse(null);
        if (vendorBill == null) {
            return new Page<>(List.of(), pageable.page(), pageable.size(), 0);
        }
        String sql = debitMemoSnapshotSql() + """
                WHERE dm.vendor_id = :vendorId
                  AND dm.currency_id = :currencyId
                  AND dm.settlement_status IN ('OPEN', 'PARTIALLY_SETTLED')
                  AND dm.gross_amount_original - COALESCE(consumed.applied_gross_original, 0) > 0
                  AND (:keyword IS NULL OR :keyword = '' OR LOWER(dm.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
                ORDER BY dm.code
                LIMIT :limit OFFSET :offset
                """;
        MapSqlParameterSource params = pagingParams(keyword, pageable)
                .addValue("vendorId", vendorBill.vendorId())
                .addValue("currencyId", vendorBill.currencyId());
        List<EligibleDebitMemo> rows = jdbcTemplate.query(sql, params, (rs, rowNum) -> new EligibleDebitMemo(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getBigDecimal("gross_amount_original"),
                rs.getBigDecimal("remaining_amount_original")
        ));
        return new Page<>(rows, pageable.page(), pageable.size(), rows.size());
    }

    private MapSqlParameterSource pagingParams(String keyword, Pageable pageable) {
        int page = pageable == null ? 0 : pageable.page();
        int size = pageable == null ? 20 : pageable.size();
        return new MapSqlParameterSource()
                .addValue("keyword", keyword == null ? null : keyword.trim())
                .addValue("limit", size)
                .addValue("offset", page * size);
    }

    private String debitMemoSnapshotSql() {
        return """
                SELECT dm.id,
                       dm.code,
                       dm.vendor_id,
                       dm.currency_id,
                       dm.gross_amount_original,
                       dm.dpp_amount_original,
                       dm.tax_amount_original,
                       dm.dpp_amount_base,
                       dm.tax_amount_base,
                       COALESCE(consumed.applied_gross_original, 0) AS applied_gross_original,
                       COALESCE(consumed.applied_dpp_original, 0) AS applied_dpp_original,
                       COALESCE(consumed.applied_tax_original, 0) AS applied_tax_original,
                       COALESCE(consumed.grir_reversal_base, 0) AS grir_reversal_base,
                       COALESCE(consumed.tax_reversal_base, 0) AS tax_reversal_base,
                       dm.gross_amount_original - COALESCE(consumed.applied_gross_original, 0) AS remaining_amount_original
                FROM ap_debit_memos dm
                LEFT JOIN (
                    SELECT debit_memo_id,
                           SUM(total_applied_gross_original) AS applied_gross_original,
                           SUM(total_dpp_original) AS applied_dpp_original,
                           SUM(total_tax_original) AS applied_tax_original,
                           SUM(total_grir_reversal_base) AS grir_reversal_base,
                           SUM(total_tax_reversal_base) AS tax_reversal_base
                    FROM ap_debit_memo_allocations
                    WHERE status = 'CONFIRMED'
                    GROUP BY debit_memo_id
                ) consumed ON consumed.debit_memo_id = dm.id
                """;
    }

    private String vendorBillSnapshotSql() {
        return """
                SELECT s.*
                FROM (
                    SELECT vb.id,
                           vb.code,
                           vb.vendor_id,
                           vb.currency_id,
                           vb.document_status,
                           vb.settlement_status,
                           vb.total_amount,
                           COALESCE(vb.exchange_rate, 1.000000) AS exchange_rate,
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
                ) s
                """;
    }
}
