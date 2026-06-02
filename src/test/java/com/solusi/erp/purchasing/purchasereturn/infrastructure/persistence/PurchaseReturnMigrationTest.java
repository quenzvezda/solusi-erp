package com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseReturnMigrationTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void migrateH2Schema() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
                + ";DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=VALUE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .outOfOrder(true)
                .locations("classpath:db/migration-h2")
                .load()
                .migrate();

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void migration_adds_purchase_return_and_reservation_schema() {
        assertTableExists("inv_stock_reservations");
        assertTableExists("pur_purchase_returns");
        assertTableExists("pur_purchase_return_lines");
        assertColumnExists("inv_stock_reservations", "owner_ref_type");
        assertColumnExists("inv_stock_reservations", "valuation_ref_line_id");
        assertColumnExists("pur_purchase_returns", "generated_gi_id");
        assertColumnExists("pur_purchase_return_lines", "tax_reversal_amount");
        assertIndexExists("inv_stock_reservations", "idx_stock_res_owner_status");
        assertIndexExists("inv_stock_reservations", "idx_stock_res_active_container");
        assertIndexExists("inv_stock_reservations", "idx_stock_res_valuation");
        assertIndexExists("inv_stock_reservations", "idx_stock_res_serial");
        assertIndexExists("pur_purchase_returns", "idx_purchase_return_source_gr");
        assertIndexExists("pur_purchase_returns", "idx_purchase_return_source_po");
        assertSequenceRegistrationExists();
    }

    @Test
    void mariadb_and_h2_migrations_keep_required_contracts_in_sync() throws IOException {
        String mariaDb = readResource("db/migration/V67__Add_Purchase_Return_Phase_1.sql");
        String h2 = readResource("db/migration-h2/V67__Add_Purchase_Return_Phase_1.sql");

        for (String requiredToken : new String[]{
                "inv_stock_reservations",
                "pur_purchase_returns",
                "pur_purchase_return_lines",
                "owner_ref_type",
                "valuation_ref_line_id",
                "idx_stock_res_owner_status",
                "idx_stock_res_active_container",
                "idx_stock_res_valuation",
                "idx_stock_res_serial",
                "idx_purchase_return_source_gr",
                "idx_purchase_return_source_po",
                "uk_purchase_return_generated_gi",
                "'PURCHASE_RETURN'",
                "'PRT-{date:yyyyMM}-{seq}'"
        }) {
            assertThat(mariaDb).contains(requiredToken);
            assertThat(h2).contains(requiredToken);
        }
    }

    private void assertTableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE LOWER(table_name) = ?
                """, Integer.class, tableName);
        assertThat(count).isEqualTo(1);
    }

    private void assertColumnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE LOWER(table_name) = ?
                  AND LOWER(column_name) = ?
                """, Integer.class, tableName, columnName);
        assertThat(count).isEqualTo(1);
    }

    private void assertIndexExists(String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.indexes
                WHERE LOWER(table_name) = ?
                  AND LOWER(index_name) = ?
                """, Integer.class, tableName, indexName);
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    private void assertSequenceRegistrationExists() {
        String pattern = jdbcTemplate.queryForObject("""
                SELECT format_pattern
                FROM system_sequences
                WHERE module_code = 'PURCHASE_RETURN'
                """, String.class);
        assertThat(pattern).isEqualTo("PRT-{date:yyyyMM}-{seq}");
    }

    private String readResource(String path) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(stream).as("resource %s", path).isNotNull();
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
