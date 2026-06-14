package com.solusi.erp.inventory.goodsissue.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GoodsIssueMigrationTest {

    private static final List<String> GOODS_ISSUE_PERMISSIONS = List.of(
            "GOODS-ISSUE_READ",
            "GOODS-ISSUE_CREATE",
            "GOODS-ISSUE_UPDATE",
            "GOODS-ISSUE_DELETE",
            "GOODS-ISSUE_COMPLETE",
            "GOODS-ISSUE_CANCEL"
    );

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
    void migration_adds_goods_issue_tables_sequence_menu_and_permissions() {
        assertTableExists("inv_goods_issues");
        assertTableExists("inv_goods_issue_lines");
        assertColumnExists("inv_goods_issues", "reference_type");
        assertColumnExists("inv_goods_issues", "facility_id");
        assertColumnExists("inv_goods_issues", "cancelled_date");
        assertColumnExists("inv_goods_issues", "cancel_reason");
        assertColumnExists("inv_goods_issue_lines", "valuation_ref_type");
        assertColumnExists("inv_goods_issue_lines", "valuation_ref_id");
        assertColumnExists("inv_goods_issue_lines", "valuation_ref_line_id");

        assertSequenceRegistrationExists();
        assertPermissionGroupExists();
        assertPermissionsExistUnderGoodsIssueGroup();
        assertPermissionsAreGrantedToAdmin();
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

    private void assertSequenceRegistrationExists() {
        String pattern = jdbcTemplate.queryForObject("""
                SELECT format_pattern
                FROM system_sequences
                WHERE module_code = 'GOODS_ISSUE'
                """, String.class);

        assertThat(pattern).isEqualTo("GI-{date:yyyyMM}-{seq}");
    }

    private void assertPermissionGroupExists() {
        String urlPath = jdbcTemplate.queryForObject("""
                SELECT url_path
                FROM permission_groups
                WHERE code = 'INV-13'
                """, String.class);

        assertThat(urlPath).isEqualTo("/inventory/goods-issues");
    }

    private void assertPermissionsExistUnderGoodsIssueGroup() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM permissions p
                JOIN permission_groups pg ON pg.id = p.permission_group_id
                WHERE pg.code = 'INV-13'
                  AND p.name IN (?, ?, ?, ?, ?, ?)
                """, Integer.class, GOODS_ISSUE_PERMISSIONS.toArray());

        assertThat(count).isEqualTo(GOODS_ISSUE_PERMISSIONS.size());
    }

    private void assertPermissionsAreGrantedToAdmin() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM role_permissions rp
                JOIN roles r ON r.id = rp.role_id
                JOIN permissions p ON p.id = rp.permission_id
                WHERE r.name = 'ROLE_ADMIN'
                  AND p.name IN (?, ?, ?, ?, ?, ?)
                """, Integer.class, GOODS_ISSUE_PERMISSIONS.toArray());

        assertThat(count).isEqualTo(GOODS_ISSUE_PERMISSIONS.size());
    }
}
