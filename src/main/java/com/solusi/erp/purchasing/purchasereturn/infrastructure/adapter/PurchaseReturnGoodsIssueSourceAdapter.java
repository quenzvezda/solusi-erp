package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

import com.solusi.erp.inventory.goodsissue.domain.port.PurchaseReturnGoodsIssueSourcePort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Optional;

public class PurchaseReturnGoodsIssueSourceAdapter implements PurchaseReturnGoodsIssueSourcePort {

    private static final String SQL_FIND_HEADER = """
            SELECT pr.id,
                   pr.code,
                   pr.return_date,
                   pr.supplier_id,
                   pr.facility_id,
                   pr.currency_id,
                   pr.exchange_rate,
                   FALSE AS bill_posted,
                   NULL AS clearing_account_id
            FROM pur_purchase_returns pr
            WHERE pr.id = :purchaseReturnId
              AND pr.status = 'APPROVED'
            """;

    private static final String SQL_FIND_LINES = """
            SELECT prl.id,
                   prl.product_id,
                   p.code AS product_code,
                   p.name AS product_name,
                   prl.is_serialized,
                   prl.quantity,
                   prl.base_quantity,
                   prl.uom_id,
                   uom.code AS uom_code,
                   uom.name AS uom_name,
                   prl.facility_id,
                   prl.grid_id,
                   g.code AS grid_code,
                   g.name AS grid_name,
                   prl.container_id,
                   c.code AS container_code,
                   c.name AS container_name,
                   prl.serial_numbers,
                   pr.reference_id AS original_goods_receipt_id,
                   prl.goods_receipt_line_id AS original_goods_receipt_line_id,
                   prl.valuation_ref_type,
                   prl.valuation_ref_id,
                   prl.valuation_ref_line_id,
                   prl.unit_cost,
                   prl.inventory_amount,
                   prl.tax_reversal_amount,
                   prl.clearing_amount
            FROM pur_purchase_return_lines prl
            JOIN pur_purchase_returns pr ON pr.id = prl.header_id
            JOIN products p ON p.id = prl.product_id
            JOIN unit_of_measures uom ON uom.id = prl.uom_id
            JOIN inv_grids g ON g.id = prl.grid_id
            JOIN inv_containers c ON c.id = prl.container_id
            WHERE pr.id = :purchaseReturnId
              AND pr.status = 'APPROVED'
            ORDER BY prl.id
            """;

    private static final String SQL_HAS_COMPLETED_GOODS_ISSUE = """
            SELECT COUNT(*)
            FROM inv_goods_issues gi
            WHERE gi.reference_type = 'PURCHASE_RETURN'
              AND gi.reference_id = :purchaseReturnId
              AND gi.status = 'COMPLETED'
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PurchaseReturnGoodsIssueSourceAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<HeaderSnapshot> findHeader(Long purchaseReturnId) {
        List<HeaderSnapshot> rows = jdbcTemplate.query(
                SQL_FIND_HEADER,
                params(purchaseReturnId),
                (rs, rowNum) -> new HeaderSnapshot(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getDate("return_date").toLocalDate(),
                        rs.getLong("supplier_id"),
                        rs.getLong("facility_id"),
                        rs.getLong("currency_id"),
                        rs.getBigDecimal("exchange_rate"),
                        rs.getBoolean("bill_posted"),
                        nullableLong(rs, "clearing_account_id")
                )
        );
        return rows.stream().findFirst();
    }

    @Override
    public List<LineSnapshot> findEligibleLines(Long purchaseReturnId) {
        return jdbcTemplate.query(
                SQL_FIND_LINES,
                params(purchaseReturnId),
                (rs, rowNum) -> new LineSnapshot(
                        rs.getLong("id"),
                        rs.getLong("product_id"),
                        rs.getString("product_code"),
                        rs.getString("product_name"),
                        rs.getBoolean("is_serialized"),
                        rs.getBigDecimal("quantity"),
                        rs.getBigDecimal("base_quantity"),
                        rs.getLong("uom_id"),
                        rs.getString("uom_code"),
                        rs.getString("uom_name"),
                        rs.getLong("facility_id"),
                        rs.getLong("grid_id"),
                        rs.getString("grid_code"),
                        rs.getString("grid_name"),
                        rs.getLong("container_id"),
                        rs.getString("container_code"),
                        rs.getString("container_name"),
                        rs.getString("serial_numbers"),
                        rs.getLong("original_goods_receipt_id"),
                        rs.getLong("original_goods_receipt_line_id"),
                        rs.getString("valuation_ref_type"),
                        rs.getLong("valuation_ref_id"),
                        rs.getLong("valuation_ref_line_id"),
                        rs.getBigDecimal("unit_cost"),
                        rs.getBigDecimal("inventory_amount"),
                        rs.getBigDecimal("tax_reversal_amount"),
                        rs.getBigDecimal("clearing_amount")
                )
        );
    }

    @Override
    public boolean hasCompletedGoodsIssue(Long purchaseReturnId) {
        Long count = jdbcTemplate.queryForObject(
                SQL_HAS_COMPLETED_GOODS_ISSUE, params(purchaseReturnId), Long.class);
        return count != null && count > 0;
    }

    private MapSqlParameterSource params(Long purchaseReturnId) {
        return new MapSqlParameterSource("purchaseReturnId", purchaseReturnId);
    }

    private Long nullableLong(java.sql.ResultSet resultSet, String column) throws java.sql.SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }
}
