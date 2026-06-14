package com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VendorBillSettlementMigrationTest {

    @Test
    void migration_splits_legacy_status_into_document_and_settlement_statuses() {
        DriverManagerDataSource dataSource = newDataSource();

        Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .outOfOrder(true)
                .locations("classpath:db/migration-h2")
                .target("71")
                .load()
                .migrate();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        insertLegacyVendorBills(jdbcTemplate);

        Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .outOfOrder(true)
                .locations("classpath:db/migration-h2")
                .load()
                .migrate();

        assertColumnExists(jdbcTemplate, "ap_vendor_bills", "document_status");
        assertColumnExists(jdbcTemplate, "ap_vendor_bills", "settlement_status");
        assertColumnMissing(jdbcTemplate, "ap_vendor_bills", "status");
        assertColumnIsNotNullable(jdbcTemplate, "ap_vendor_bills", "document_status");

        assertMappedStatus(jdbcTemplate, "VB-MIG-DRAFT", "DRAFT", null);
        assertMappedStatus(jdbcTemplate, "VB-MIG-CONF", "CONFIRMED", "OPEN");
        assertMappedStatus(jdbcTemplate, "VB-MIG-PART", "CONFIRMED", "PARTIALLY_SETTLED");
        assertMappedStatus(jdbcTemplate, "VB-MIG-PAID", "CONFIRMED", "SETTLED");
        assertMappedStatus(jdbcTemplate, "VB-MIG-CANC", "CANCELLED", null);

        assertIndexExists(jdbcTemplate, "ap_vendor_bills", "idx_ap_vendor_bills_document_status");
        assertIndexExists(jdbcTemplate, "ap_vendor_bills", "idx_ap_vendor_bills_settlement_status");
        assertIndexExists(jdbcTemplate, "ap_vendor_bills", "idx_ap_vendor_bills_payable_lookup");
    }

    @Test
    void mariadb_and_h2_migration_files_keep_required_contracts_in_sync() throws IOException {
        String mariaDb = readResource("db/migration/V72__Refactor_Vendor_Bill_Settlement_Status.sql");
        String h2 = readResource("db/migration-h2/V72__Refactor_Vendor_Bill_Settlement_Status.sql");

        for (String requiredToken : List.of(
                "document_status",
                "settlement_status",
                "PARTIAL_PAID",
                "PARTIALLY_SETTLED",
                "PAID",
                "SETTLED",
                "DROP COLUMN status",
                "idx_ap_vendor_bills_document_status",
                "idx_ap_vendor_bills_settlement_status",
                "idx_ap_vendor_bills_payable_lookup"
        )) {
            assertThat(mariaDb).contains(requiredToken);
            assertThat(h2).contains(requiredToken);
        }
    }

    private DriverManagerDataSource newDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
                + ";DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=VALUE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private void insertLegacyVendorBills(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.batchUpdate("""
                INSERT INTO ap_vendor_bills
                    (id, code, vendor_id, vendor_invoice_number, bill_date, due_date, currency_id,
                     exchange_rate, status, subtotal, tax_amount, total_amount, notes,
                     created_date, updated_date, version)
                VALUES
                    (?, ?, 9001, ?, DATE '2026-06-01', DATE '2026-06-30', 9001,
                     1.000000, ?, 100.0000, 0.0000, 100.0000, NULL,
                     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1)
                """, List.of(
                new Object[]{9901L, "VB-MIG-DRAFT", "INV-DRAFT", "DRAFT"},
                new Object[]{9902L, "VB-MIG-CONF", "INV-CONF", "CONFIRMED"},
                new Object[]{9903L, "VB-MIG-PART", "INV-PART", "PARTIAL_PAID"},
                new Object[]{9904L, "VB-MIG-PAID", "INV-PAID", "PAID"},
                new Object[]{9905L, "VB-MIG-CANC", "INV-CANC", "CANCELLED"}
        ));
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    private void assertMappedStatus(
            JdbcTemplate jdbcTemplate,
            String code,
            String expectedDocumentStatus,
            String expectedSettlementStatus
    ) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT document_status, settlement_status
                FROM ap_vendor_bills
                WHERE code = ?
                """, code);

        assertThat(row.get("document_status")).isEqualTo(expectedDocumentStatus);
        assertThat(row.get("settlement_status")).isEqualTo(expectedSettlementStatus);
    }

    private void assertColumnExists(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        assertThat(columnCount(jdbcTemplate, tableName, columnName)).isEqualTo(1);
    }

    private void assertColumnMissing(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        assertThat(columnCount(jdbcTemplate, tableName, columnName)).isZero();
    }

    private int columnCount(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE LOWER(table_name) = ?
                  AND LOWER(column_name) = ?
                """, Integer.class, tableName, columnName);
        return count == null ? 0 : count;
    }

    private void assertColumnIsNotNullable(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        String nullable = jdbcTemplate.queryForObject("""
                SELECT is_nullable
                FROM information_schema.columns
                WHERE LOWER(table_name) = ?
                  AND LOWER(column_name) = ?
                """, String.class, tableName, columnName);
        assertThat(nullable).isEqualToIgnoringCase("NO");
    }

    private void assertIndexExists(JdbcTemplate jdbcTemplate, String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.indexes
                WHERE LOWER(table_name) = ?
                  AND LOWER(index_name) = ?
                """, Integer.class, tableName, indexName);
        assertThat(count).isNotNull();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    private String readResource(String path) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(stream).as("resource %s", path).isNotNull();
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
