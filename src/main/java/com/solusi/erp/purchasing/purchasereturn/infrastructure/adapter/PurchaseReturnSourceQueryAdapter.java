package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.EligibleGoodsReceiptRow;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableGrLineSlice;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableSerialRow;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PurchaseReturnSourceQueryAdapter implements PurchaseReturnSourceQueryPort {

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String VALUATION_REFERENCE_GOODS_RECEIPT = "GOODS_RECEIPT";

    private static final String VALUATION_LAYER_AGGREGATE = """
            JOIN (
                SELECT product_id, container_id, serial_number, reference_type, reference_id,
                       reference_line_id, SUM(remaining_quantity) AS remaining_quantity,
                       MAX(unit_cost_amount_original) AS unit_cost_amount_original,
                       MAX(unit_cost_amount_local) AS unit_cost_amount_local
                FROM inv_valuation_layers
                GROUP BY product_id, container_id, serial_number, reference_type, reference_id,
                         reference_line_id
            ) vl ON vl.reference_type = :valuationReferenceType
            """;

    private static final String RESERVATION_AGGREGATE = """
            LEFT JOIN (
                SELECT valuation_ref_type, valuation_ref_id, valuation_ref_line_id, product_id,
                       container_id, serial_number, SUM(quantity) AS reserved_qty
                FROM inv_stock_reservations
                WHERE status = :activeReservationStatus
                GROUP BY valuation_ref_type, valuation_ref_id, valuation_ref_line_id, product_id,
                         container_id, serial_number
            ) reserved ON reserved.valuation_ref_type = vl.reference_type
                      AND reserved.valuation_ref_id = vl.reference_id
                      AND reserved.valuation_ref_line_id = vl.reference_line_id
                      AND reserved.product_id = vl.product_id
                      AND reserved.container_id = vl.container_id
                      AND (reserved.serial_number = vl.serial_number
                           OR (reserved.serial_number IS NULL AND vl.serial_number IS NULL))
            """;

    private static final String SQL_FIND_ELIGIBLE_GRS = """
            SELECT gr.id AS gr_id, gr.code AS gr_code, po.id AS po_id, po.code AS po_code,
                   gr.supplier_id, supplier.name AS supplier_name, gr.receipt_date,
                   gr.facility_id, facility.name AS facility_name, gr.currency_id,
                   currency.code AS currency_code, gr.exchange_rate,
                   COUNT(DISTINCT grl.id) AS eligible_line_count,
                   SUM(vl.remaining_quantity - COALESCE(reserved.reserved_qty, 0)) AS total_returnable_qty
            FROM pur_goods_receipts gr
            JOIN pur_goods_receipt_lines grl ON grl.header_id = gr.id
            JOIN pur_purchase_orders po ON po.id = gr.po_id
            JOIN parties supplier ON supplier.id = gr.supplier_id
            JOIN inv_facilities facility ON facility.id = gr.facility_id
            JOIN master_currencies currency ON currency.id = gr.currency_id
            """ + VALUATION_LAYER_AGGREGATE + """
                                        AND vl.reference_id = gr.id
                                        AND vl.reference_line_id = grl.id
                                        AND vl.product_id = grl.product_id
            """ + RESERVATION_AGGREGATE + """
            WHERE gr.status = :completedStatus
              AND (vl.remaining_quantity - COALESCE(reserved.reserved_qty, 0)) > 0
              AND (:grId IS NULL OR gr.id = :grId)
              AND (:supplierId IS NULL OR gr.supplier_id = :supplierId)
              AND (:purchaseOrderId IS NULL OR gr.po_id = :purchaseOrderId)
              AND (:receiptDateFrom IS NULL OR gr.receipt_date >= :receiptDateFrom)
              AND (:receiptDateTo IS NULL OR gr.receipt_date <= :receiptDateTo)
              AND (:keyword IS NULL OR LOWER(gr.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(po.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(supplier.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            GROUP BY gr.id, gr.code, po.id, po.code, gr.supplier_id, supplier.name, gr.receipt_date,
                     gr.facility_id, facility.name, gr.currency_id, currency.code, gr.exchange_rate
            ORDER BY gr.receipt_date DESC, gr.id DESC
            """;

    private static final String SQL_FIND_ELIGIBLE_POS = """
            SELECT DISTINCT po.id AS po_id, po.code AS po_code
            FROM pur_purchase_orders po
            JOIN pur_goods_receipts gr ON gr.po_id = po.id
            JOIN pur_goods_receipt_lines grl ON grl.header_id = gr.id
            """ + VALUATION_LAYER_AGGREGATE + """
                                        AND vl.reference_id = gr.id
                                        AND vl.reference_line_id = grl.id
                                        AND vl.product_id = grl.product_id
            """ + RESERVATION_AGGREGATE + """
            WHERE gr.status = :completedStatus
              AND (vl.remaining_quantity - COALESCE(reserved.reserved_qty, 0)) > 0
              AND (:keyword IS NULL OR LOWER(po.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY po.code
            LIMIT :limit
            """;

    private static final String SQL_FIND_GR_LINE_SLICES = """
            SELECT CONCAT(CAST(grl.id AS CHAR), ':', CAST(vl.container_id AS CHAR)) AS selection_key,
                   gr.id AS gr_id, grl.id AS gr_line_id, grl.product_id, product.name AS product_name,
                   product.code AS product_code, grl.is_serialized, grl.uom_id, uom.name AS uom_name,
                   uom.code AS uom_code, facility.id AS facility_id, facility.name AS facility_name,
                   grid.id AS grid_id, grid.code AS grid_code, container.id AS container_id,
                   container.code AS container_code,
                   SUM(vl.remaining_quantity - COALESCE(reserved.reserved_qty, 0)) AS outstanding_qty,
                   vl.reference_type AS valuation_ref_type, vl.reference_id AS valuation_ref_id,
                   vl.reference_line_id AS valuation_ref_line_id,
                   MAX(vl.unit_cost_amount_original) AS unit_cost,
                   SUM((vl.remaining_quantity - COALESCE(reserved.reserved_qty, 0))
                       * COALESCE(vl.unit_cost_amount_local, 0)) AS inventory_amount,
                   CASE WHEN grl.base_quantity = 0 THEN 0 ELSE
                       SUM(vl.remaining_quantity - COALESCE(reserved.reserved_qty, 0))
                       * grl.tax_amount / grl.base_quantity END AS tax_reversal_amount,
                   CASE WHEN grl.base_quantity = 0 THEN 0 ELSE
                       SUM(vl.remaining_quantity - COALESCE(reserved.reserved_qty, 0))
                       * grl.gr_ir_amount / grl.base_quantity END AS clearing_amount
            FROM pur_goods_receipts gr
            JOIN pur_goods_receipt_lines grl ON grl.header_id = gr.id
            JOIN products product ON product.id = grl.product_id
            JOIN unit_of_measures uom ON uom.id = grl.uom_id
            """ + VALUATION_LAYER_AGGREGATE + """
                                        AND vl.reference_id = gr.id
                                        AND vl.reference_line_id = grl.id
                                        AND vl.product_id = grl.product_id
            JOIN inv_containers container ON container.id = vl.container_id
            JOIN inv_grids grid ON grid.id = container.grid_id
            JOIN inv_facilities facility ON facility.id = grid.facility_id
            """ + RESERVATION_AGGREGATE + """
            WHERE gr.id = :grId
              AND gr.status = :completedStatus
              AND grl.is_serialized = FALSE
              AND vl.serial_number IS NULL
              AND (vl.remaining_quantity - COALESCE(reserved.reserved_qty, 0)) > 0
              AND (:keyword IS NULL OR LOWER(product.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(product.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(container.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:excludedKeysEmpty = TRUE OR CONCAT(CAST(grl.id AS CHAR), ':', CAST(vl.container_id AS CHAR))
                   NOT IN (:excludedKeys))
            GROUP BY gr.id, grl.id, grl.product_id, product.name, product.code, grl.is_serialized,
                     grl.uom_id, uom.name, uom.code, facility.id, facility.name, grid.id, grid.code,
                     container.id, container.code, vl.reference_type, vl.reference_id, vl.reference_line_id,
                     grl.base_quantity, grl.tax_amount, grl.gr_ir_amount
            ORDER BY product.code, container.code
            """;

    private static final String SQL_FIND_SERIALS = """
            SELECT CONCAT(CAST(grl.id AS CHAR), ':', CAST(vl.container_id AS CHAR), ':', vl.serial_number) AS selection_key,
                   gr.id AS gr_id, grl.id AS gr_line_id, grl.product_id, product.name AS product_name,
                   product.code AS product_code, grl.uom_id, uom.name AS uom_name, uom.code AS uom_code,
                   facility.id AS facility_id, facility.name AS facility_name, grid.id AS grid_id,
                   grid.code AS grid_code, container.id AS container_id, container.code AS container_code,
                   vl.serial_number, vl.reference_type AS valuation_ref_type, vl.reference_id AS valuation_ref_id,
                   vl.reference_line_id AS valuation_ref_line_id, vl.unit_cost_amount_original AS unit_cost,
                   COALESCE(vl.unit_cost_amount_local, 0) AS inventory_amount,
                   CASE WHEN grl.base_quantity = 0 THEN 0 ELSE grl.tax_amount / grl.base_quantity END AS tax_reversal_amount,
                   CASE WHEN grl.base_quantity = 0 THEN 0 ELSE grl.gr_ir_amount / grl.base_quantity END AS clearing_amount
            FROM pur_goods_receipts gr
            JOIN pur_goods_receipt_lines grl ON grl.header_id = gr.id
            JOIN products product ON product.id = grl.product_id
            JOIN unit_of_measures uom ON uom.id = grl.uom_id
            """ + VALUATION_LAYER_AGGREGATE + """
                                        AND vl.reference_id = gr.id
                                        AND vl.reference_line_id = grl.id
                                        AND vl.product_id = grl.product_id
            JOIN inv_containers container ON container.id = vl.container_id
            JOIN inv_grids grid ON grid.id = container.grid_id
            JOIN inv_facilities facility ON facility.id = grid.facility_id
            """ + RESERVATION_AGGREGATE + """
            WHERE gr.id = :grId
              AND grl.id = :grLineId
              AND gr.status = :completedStatus
              AND grl.is_serialized = TRUE
              AND vl.serial_number IS NOT NULL
              AND (vl.remaining_quantity - COALESCE(reserved.reserved_qty, 0)) > 0
              AND (:keyword IS NULL OR LOWER(vl.serial_number) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(container.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:excludedKeysEmpty = TRUE OR CONCAT(CAST(grl.id AS CHAR), ':', CAST(vl.container_id AS CHAR), ':', vl.serial_number)
                   NOT IN (:excludedKeys))
            ORDER BY vl.serial_number
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PurchaseReturnSourceQueryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<EligibleGoodsReceiptRow> findEligibleGoodsReceipts(
            String keyword, Long supplierId, Long purchaseOrderId,
            LocalDate receiptDateFrom, LocalDate receiptDateTo) {
        return jdbcTemplate.query(SQL_FIND_ELIGIBLE_GRS,
                baseParams(keyword)
                        .addValue("grId", null)
                        .addValue("supplierId", supplierId)
                        .addValue("purchaseOrderId", purchaseOrderId)
                        .addValue("receiptDateFrom", receiptDateFrom)
                        .addValue("receiptDateTo", receiptDateTo),
                eligibleGoodsReceiptRowMapper());
    }

    @Override
    public Optional<EligibleGoodsReceiptRow> findEligibleGoodsReceiptById(Long goodsReceiptId) {
        List<EligibleGoodsReceiptRow> rows = jdbcTemplate.query(SQL_FIND_ELIGIBLE_GRS,
                baseParams(null)
                        .addValue("grId", goodsReceiptId)
                        .addValue("supplierId", null)
                        .addValue("purchaseOrderId", null)
                        .addValue("receiptDateFrom", null)
                        .addValue("receiptDateTo", null),
                eligibleGoodsReceiptRowMapper());
        return rows.stream().findFirst();
    }

    @Override
    public List<LookupDto> findEligiblePurchaseOrders(String query, int limit) {
        return jdbcTemplate.query(SQL_FIND_ELIGIBLE_POS,
                baseParams(query).addValue("limit", limit),
                (rs, rowNum) -> new LookupDto(rs.getLong("po_id"), rs.getString("po_code"),
                        rs.getString("po_code"), Map.of()));
    }

    @Override
    public List<ReturnableGrLineSlice> findReturnableGrLineSlices(
            Long goodsReceiptId, String keyword, List<String> excludedSelectionKeys) {
        return jdbcTemplate.query(SQL_FIND_GR_LINE_SLICES,
                selectorParams(goodsReceiptId, keyword, excludedSelectionKeys),
                grLineSliceRowMapper());
    }

    @Override
    public List<ReturnableSerialRow> findReturnableSerials(
            Long goodsReceiptId, Long goodsReceiptLineId, String keyword,
            List<String> excludedSelectionKeys) {
        return jdbcTemplate.query(SQL_FIND_SERIALS,
                selectorParams(goodsReceiptId, keyword, excludedSelectionKeys)
                        .addValue("grLineId", goodsReceiptLineId),
                serialRowMapper());
    }

    private static MapSqlParameterSource selectorParams(Long goodsReceiptId, String keyword,
                                                        List<String> excludedSelectionKeys) {
        List<String> keys = excludedSelectionKeys == null || excludedSelectionKeys.isEmpty()
                ? List.of("__NONE__")
                : excludedSelectionKeys;
        return baseParams(keyword)
                .addValue("grId", goodsReceiptId)
                .addValue("excludedKeysEmpty", excludedSelectionKeys == null || excludedSelectionKeys.isEmpty())
                .addValue("excludedKeys", keys);
    }

    private static MapSqlParameterSource baseParams(String keyword) {
        return new MapSqlParameterSource()
                .addValue("completedStatus", STATUS_COMPLETED)
                .addValue("activeReservationStatus", STATUS_ACTIVE)
                .addValue("valuationReferenceType", VALUATION_REFERENCE_GOODS_RECEIPT)
                .addValue("keyword", keyword == null || keyword.isBlank() ? null : keyword.trim());
    }

    private static RowMapper<EligibleGoodsReceiptRow> eligibleGoodsReceiptRowMapper() {
        return (rs, rowNum) -> new EligibleGoodsReceiptRow(
                rs.getLong("gr_id"), rs.getString("gr_code"), rs.getLong("po_id"),
                rs.getString("po_code"), rs.getLong("supplier_id"), rs.getString("supplier_name"),
                rs.getObject("receipt_date", LocalDate.class), rs.getLong("facility_id"),
                rs.getString("facility_name"), rs.getLong("currency_id"), rs.getString("currency_code"),
                rs.getBigDecimal("exchange_rate"), rs.getLong("eligible_line_count"),
                rs.getBigDecimal("total_returnable_qty")
        );
    }

    private static RowMapper<ReturnableGrLineSlice> grLineSliceRowMapper() {
        return (rs, rowNum) -> new ReturnableGrLineSlice(
                rs.getString("selection_key"), rs.getLong("gr_id"), rs.getLong("gr_line_id"),
                rs.getLong("product_id"), rs.getString("product_name"), rs.getString("product_code"),
                rs.getBoolean("is_serialized"), rs.getLong("uom_id"), rs.getString("uom_name"),
                rs.getString("uom_code"), rs.getLong("facility_id"), rs.getString("facility_name"),
                rs.getLong("grid_id"), rs.getString("grid_code"), rs.getLong("container_id"),
                rs.getString("container_code"), rs.getBigDecimal("outstanding_qty"),
                rs.getString("valuation_ref_type"), rs.getLong("valuation_ref_id"),
                rs.getLong("valuation_ref_line_id"), rs.getBigDecimal("unit_cost"),
                rs.getBigDecimal("inventory_amount"), rs.getBigDecimal("tax_reversal_amount"),
                rs.getBigDecimal("clearing_amount")
        );
    }

    private static RowMapper<ReturnableSerialRow> serialRowMapper() {
        return (rs, rowNum) -> new ReturnableSerialRow(
                rs.getString("selection_key"), rs.getLong("gr_id"), rs.getLong("gr_line_id"),
                rs.getLong("product_id"), rs.getString("product_name"), rs.getString("product_code"),
                rs.getLong("uom_id"), rs.getString("uom_name"), rs.getString("uom_code"),
                rs.getLong("facility_id"), rs.getString("facility_name"), rs.getLong("grid_id"),
                rs.getString("grid_code"), rs.getLong("container_id"), rs.getString("container_code"),
                rs.getString("serial_number"), rs.getString("valuation_ref_type"),
                rs.getLong("valuation_ref_id"), rs.getLong("valuation_ref_line_id"),
                rs.getBigDecimal("unit_cost"), rs.getBigDecimal("inventory_amount"),
                rs.getBigDecimal("tax_reversal_amount"), rs.getBigDecimal("clearing_amount")
        );
    }
}
