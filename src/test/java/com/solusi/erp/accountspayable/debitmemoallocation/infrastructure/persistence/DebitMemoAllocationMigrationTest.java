package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence;

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

class DebitMemoAllocationMigrationTest {

    @Test
    void migration_creates_debit_memo_allocation_contract() {
        DriverManagerDataSource dataSource = newDataSource();

        Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .outOfOrder(true)
                .locations("classpath:db/migration-h2")
                .target("74")
                .load()
                .migrate();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        assertTableExists(jdbcTemplate, "ap_debit_memo_allocations");
        assertTableExists(jdbcTemplate, "ap_debit_memo_allocation_lines");

        for (String column : List.of(
                "code",
                "debit_memo_id",
                "debit_memo_code",
                "allocation_date",
                "status",
                "total_applied_gross_original",
                "total_dpp_original",
                "total_tax_original",
                "total_grir_reversal_base",
                "total_tax_reversal_base",
                "total_ap_reduction_base",
                "total_fx_loss_base",
                "total_fx_gain_base",
                "apply_journal_entry_id",
                "reversal_journal_entry_id",
                "reversal_date",
                "reversal_reason",
                "notes",
                "version"
        )) {
            assertColumnExists(jdbcTemplate, "ap_debit_memo_allocations", column);
        }

        for (String column : List.of(
                "debit_memo_allocation_id",
                "vendor_bill_id",
                "vendor_bill_code",
                "debit_memo_remaining_at_draft",
                "vendor_bill_outstanding_at_draft",
                "applied_gross_original",
                "applied_dpp_original",
                "applied_tax_original",
                "grir_reversal_base",
                "tax_reversal_base",
                "vendor_bill_exchange_rate",
                "ap_reduction_base",
                "fx_loss_base",
                "fx_gain_base"
        )) {
            assertColumnExists(jdbcTemplate, "ap_debit_memo_allocation_lines", column);
        }

        assertColumnIsNotNullable(jdbcTemplate, "ap_debit_memo_allocations", "debit_memo_id");
        assertColumnIsNotNullable(jdbcTemplate, "ap_debit_memo_allocations", "status");
        assertColumnIsNotNullable(jdbcTemplate, "ap_debit_memo_allocation_lines", "vendor_bill_id");

        assertUniqueConstraintExists(jdbcTemplate, "ap_debit_memo_allocations", "uk_ap_dma_code");
        assertUniqueConstraintExists(jdbcTemplate, "ap_debit_memo_allocation_lines", "uk_ap_dma_lines_allocation_bill");
        assertIndexExists(jdbcTemplate, "ap_debit_memo_allocations", "idx_ap_dma_debit_memo_status");
        assertIndexExists(jdbcTemplate, "ap_debit_memo_allocations", "idx_ap_dma_status_date");
        assertIndexExists(jdbcTemplate, "ap_debit_memo_allocations", "idx_ap_dma_keyword");
        assertIndexExists(jdbcTemplate, "ap_debit_memo_allocation_lines", "idx_ap_dma_lines_vendor_bill");

        Integer sequenceCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM system_sequences
                WHERE module_code = 'DEBIT_MEMO_ALLOCATION'
                  AND format_pattern = 'DMA-{date:yyyyMM}-{seq}'
                """, Integer.class);
        assertThat(sequenceCount).isEqualTo(1);

        Integer groupCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM permission_groups
                WHERE code = 'AP-04'
                  AND url_path = '/accounts-payable/debit-memo-allocations'
                """, Integer.class);
        assertThat(groupCount).isEqualTo(1);

        List<String> permissions = List.of(
                "DEBIT-MEMO-ALLOCATION_READ",
                "DEBIT-MEMO-ALLOCATION_CREATE",
                "DEBIT-MEMO-ALLOCATION_UPDATE",
                "DEBIT-MEMO-ALLOCATION_CONFIRM",
                "DEBIT-MEMO-ALLOCATION_CANCEL",
                "DEBIT-MEMO-ALLOCATION_REVERSE"
        );

        Integer permissionCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM permissions
                WHERE name IN (?, ?, ?, ?, ?, ?)
                """, Integer.class, permissions.toArray());
        assertThat(permissionCount).isEqualTo(6);

        Integer adminGrantCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM role_permissions rp
                JOIN roles r ON r.id = rp.role_id
                JOIN permissions p ON p.id = rp.permission_id
                WHERE r.name = 'ROLE_ADMIN'
                  AND p.name IN (?, ?, ?, ?, ?, ?)
                """, Integer.class, permissions.toArray());
        assertThat(adminGrantCount).isEqualTo(6);

        Integer schemaCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM acc_accounting_schemas
                WHERE event_type = 'DEBIT_MEMO_APPLICATION'
                  AND is_active = TRUE
                """, Integer.class);
        assertThat(schemaCount).isEqualTo(1);
    }

    @Test
    void mariadb_h2_and_e2e_seed_keep_required_contracts_in_sync() throws IOException {
        String mariaDb = readResource("db/migration/V74__Add_Debit_Memo_Allocation.sql");
        String h2 = readResource("db/migration-h2/V74__Add_Debit_Memo_Allocation.sql");
        String e2eSeed = readResource("db/migration-h2/V9000__e2e_seed_data.sql");

        for (String requiredToken : List.of(
                "ap_debit_memo_allocations",
                "ap_debit_memo_allocation_lines",
                "debit_memo_id",
                "vendor_bill_id",
                "CONSTRAINT uk_ap_dma_code UNIQUE",
                "CONSTRAINT uk_ap_dma_lines_allocation_bill UNIQUE",
                "DEBIT_MEMO_ALLOCATION",
                "DMA-{date:yyyyMM}-{seq}",
                "AP-04",
                "/accounts-payable/debit-memo-allocations",
                "DEBIT-MEMO-ALLOCATION_READ",
                "DEBIT-MEMO-ALLOCATION_CREATE",
                "DEBIT-MEMO-ALLOCATION_UPDATE",
                "DEBIT-MEMO-ALLOCATION_CONFIRM",
                "DEBIT-MEMO-ALLOCATION_CANCEL",
                "DEBIT-MEMO-ALLOCATION_REVERSE",
                "DEBIT_MEMO_APPLICATION",
                "DMA_AP_AMT",
                "DMA_GRIR_CLEARING_AMT",
                "DMA_TAX_AMT",
                "DMA_FX_LOSS_AMT",
                "DMA_FX_GAIN_AMT"
        )) {
            assertThat(mariaDb).contains(requiredToken);
            assertThat(h2).contains(requiredToken);
        }

        for (String requiredToken : List.of(
                "DEBIT_MEMO_APPLICATION",
                "DMA_AP_AMT",
                "DMA_GRIR_CLEARING_AMT",
                "DMA_TAX_AMT",
                "DMA_FX_LOSS_AMT",
                "DMA_FX_GAIN_AMT"
        )) {
            assertThat(e2eSeed).contains(requiredToken);
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
