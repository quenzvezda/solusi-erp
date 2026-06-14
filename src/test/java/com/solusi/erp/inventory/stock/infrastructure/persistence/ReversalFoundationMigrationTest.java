package com.solusi.erp.inventory.stock.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReversalFoundationMigrationTest {

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
    void migration_adds_linked_stock_and_valuation_reversal_columns() {
        assertColumnExists("inv_movements", "reversal_of_movement_id");
        assertColumnExists("inv_valuation_layers", "reversal_of_movement_id");
        assertConstraintExists("inv_movements", "fk_inv_mov_reversal_of");
        assertConstraintExists("inv_movements", "uk_inv_mov_reversal_of");
        assertConstraintExists("inv_valuation_layers", "fk_val_layer_reversal_of_movement");
        assertIndexExists("inv_movements", "idx_inv_mov_reference");
        assertIndexExists("inv_movements", "idx_inv_mov_reversal_lookup");
        assertIndexExists("inv_valuation_layers", "idx_val_layer_reversal_movement");
    }

    @Test
    void mariadb_and_h2_migrations_keep_reversal_contract_in_sync() throws IOException {
        String maria = Files.readString(Path.of("src/main/resources/db/migration/V69__Add_Generic_Reversal_Foundation.sql"));
        String h2 = Files.readString(Path.of("src/main/resources/db/migration-h2/V69__Add_Generic_Reversal_Foundation.sql"));

        assertThat(maria).contains("reversal_of_movement_id");
        assertThat(h2).contains("reversal_of_movement_id");
        assertThat(maria).contains("fk_inv_mov_reversal_of", "uk_inv_mov_reversal_of");
        assertThat(h2).contains("fk_inv_mov_reversal_of", "uk_inv_mov_reversal_of");
        assertThat(maria).contains("fk_val_layer_reversal_of_movement");
        assertThat(h2).contains("fk_val_layer_reversal_of_movement");
        assertThat(maria).contains("idx_inv_mov_reference", "idx_inv_mov_reversal_lookup", "idx_val_layer_reversal_movement");
        assertThat(h2).contains("idx_inv_mov_reference", "idx_inv_mov_reversal_lookup", "idx_val_layer_reversal_movement");
        assertThat(maria).doesNotContain("GOODS_ISSUE_REVERSAL");
        assertThat(h2).doesNotContain("GOODS_ISSUE_REVERSAL");
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

    private void assertConstraintExists(String tableName, String constraintName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.table_constraints
                WHERE LOWER(table_name) = ?
                  AND LOWER(constraint_name) = ?
                """, Integer.class, tableName, constraintName);
        assertThat(count).isEqualTo(1);
    }

    private void assertIndexExists(String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.indexes
                WHERE LOWER(table_name) = ?
                  AND LOWER(index_name) = ?
                """, Integer.class, tableName, indexName);
        assertThat(count).isEqualTo(1);
    }
}
