package com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DebitMemoCoreMigrationTest {

    @Test
    void migration_creates_debit_memo_core_contract() {
        DriverManagerDataSource dataSource = newDataSource();

        Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .outOfOrder(true)
                .locations("classpath:db/migration-h2")
                .target("73")
                .load()
                .migrate();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        assertTableExists(jdbcTemplate, "ap_debit_memos");
        assertTableExists(jdbcTemplate, "ap_debit_memo_lines");

        for (String column : List.of(
                "code",
                "purchase_return_id",
                "purchase_return_code",
                "vendor_id",
                "currency_id",
                "memo_date",
                "gross_amount_original",
                "dpp_amount_original",
                "tax_amount_original",
                "gross_amount_base",
                "dpp_amount_base",
                "tax_amount_base",
                "settlement_status",
                "supplier_memo_number",
                "supplier_memo_date",
                "tax_document_number",
                "tax_document_date",
                "notes",
                "version"
        )) {
            assertColumnExists(jdbcTemplate, "ap_debit_memos", column);
        }

        for (String column : List.of(
                "debit_memo_id",
                "purchase_return_line_id",
                "product_id",
                "quantity",
                "uom_id",
                "dpp_amount_original",
                "tax_amount_original",
                "dpp_amount_base",
                "tax_amount_base",
                "version"
        )) {
            assertColumnExists(jdbcTemplate, "ap_debit_memo_lines", column);
        }

        assertColumnIsNotNullable(jdbcTemplate, "ap_debit_memos", "purchase_return_id");
        assertColumnIsNotNullable(jdbcTemplate, "ap_debit_memos", "settlement_status");
        assertColumnIsNotNullable(jdbcTemplate, "ap_debit_memo_lines", "purchase_return_line_id");

        assertUniqueConstraintExists(jdbcTemplate, "ap_debit_memos", "uk_ap_debit_memos_code");
        assertUniqueConstraintExists(jdbcTemplate, "ap_debit_memos", "uk_ap_debit_memos_purchase_return");
        assertUniqueConstraintExists(jdbcTemplate, "ap_debit_memos", "uk_ap_debit_memos_vendor_supplier_memo");
        assertUniqueConstraintExists(jdbcTemplate, "ap_debit_memos", "uk_ap_debit_memos_tax_document");
        assertIndexExists(jdbcTemplate, "ap_debit_memos", "idx_ap_debit_memos_vendor_status_date");
        assertIndexExists(jdbcTemplate, "ap_debit_memo_lines", "idx_ap_dm_lines_purchase_return_line");

        Integer sequenceCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM system_sequences
                WHERE module_code = 'DEBIT_MEMO'
                  AND format_pattern = 'DM-{date:yyyyMM}-{seq}'
                """, Integer.class);
        assertThat(sequenceCount).isEqualTo(1);

        Integer groupCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM permission_groups
                WHERE code = 'AP-03'
                  AND url_path = '/accounts-payable/debit-memos'
                """, Integer.class);
        assertThat(groupCount).isEqualTo(1);

        Integer permissionCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM permissions
                WHERE name IN ('DEBIT-MEMO_READ', 'DEBIT-MEMO_UPDATE-METADATA', 'DEBIT-MEMO_CANCEL')
                """, Integer.class);
        assertThat(permissionCount).isEqualTo(3);

        Integer adminGrantCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM role_permissions rp
                JOIN roles r ON r.id = rp.role_id
                JOIN permissions p ON p.id = rp.permission_id
                WHERE r.name = 'ROLE_ADMIN'
                  AND p.name IN ('DEBIT-MEMO_READ', 'DEBIT-MEMO_UPDATE-METADATA', 'DEBIT-MEMO_CANCEL')
                """, Integer.class);
        assertThat(adminGrantCount).isEqualTo(3);
    }

    @Test
    void mariadb_and_h2_migration_files_keep_required_contracts_in_sync() throws IOException {
        String mariaDb = readResource("db/migration/V73__Add_Debit_Memo_Core.sql");
        String h2 = readResource("db/migration-h2/V73__Add_Debit_Memo_Core.sql");

        for (String requiredToken : List.of(
                "ap_debit_memos",
                "ap_debit_memo_lines",
                "purchase_return_id",
                "CONSTRAINT uk_ap_debit_memos_purchase_return UNIQUE",
                "supplier_memo_number",
                "tax_document_number",
                "DEBIT_MEMO",
                "DM-{date:yyyyMM}-{seq}",
                "AP-03",
                "DEBIT-MEMO_READ",
                "DEBIT-MEMO_UPDATE-METADATA",
                "DEBIT-MEMO_CANCEL"
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

    private void assertTableExists(JdbcTemplate jdbcTemplate, String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE LOWER(table_name) = ?
                """, Integer.class, tableName);
        assertThat(count).isEqualTo(1);
    }

    private void assertColumnExists(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        assertThat(columnCount(jdbcTemplate, tableName, columnName)).isEqualTo(1);
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

    private void assertUniqueConstraintExists(JdbcTemplate jdbcTemplate, String tableName, String constraintName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.table_constraints
                WHERE LOWER(table_name) = ?
                  AND LOWER(constraint_name) = ?
                  AND constraint_type = 'UNIQUE'
                """, Integer.class, tableName, constraintName);
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
