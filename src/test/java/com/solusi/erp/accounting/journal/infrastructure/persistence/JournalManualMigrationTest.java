package com.solusi.erp.accounting.journal.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JournalManualMigrationTest {

    private static final List<String> MANUAL_PERMISSIONS = List.of(
            "JOURNAL-ENTRY_CREATE",
            "JOURNAL-ENTRY_UPDATE",
            "JOURNAL-ENTRY_DELETE",
            "JOURNAL-ENTRY_POST",
            "JOURNAL-ENTRY_REVERSE"
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
    void migration_adds_manual_journal_columns_permissions_and_unique_reversal_guard() {
        assertColumnExists("acc_journal_entries", "currency_id");
        assertColumnExists("acc_journal_entries", "exchange_rate");
        assertColumnExists("acc_journal_entries", "reference_no");
        assertColumnExists("acc_journal_entries", "reversal_of_id");
        assertColumnExists("acc_journal_lines", "description");

        assertSourceIdAcceptsNull();
        assertManualPermissionsExistUnderJournalGroup();
        assertManualPermissionsAreGrantedToAdmin();
        assertNullableReversalUniqueConstraintAllowsManyNullsButRejectsDuplicateOriginal();
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

    private void assertSourceIdAcceptsNull() {
        jdbcTemplate.update("""
                INSERT INTO acc_journal_entries
                    (event_type, source_type, source_id, posting_date, status)
                VALUES (?, ?, NULL, ?, ?)
                """, "MANUAL", "MIGRATION_NULL_SOURCE", LocalDate.of(2026, 5, 31), "DRAFT");
    }

    private void assertManualPermissionsExistUnderJournalGroup() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM permissions p
                JOIN permission_groups pg ON pg.id = p.permission_group_id
                WHERE pg.code = 'ACC-05'
                  AND p.name IN (?, ?, ?, ?, ?)
                """, Integer.class, MANUAL_PERMISSIONS.toArray());

        assertThat(count).isEqualTo(MANUAL_PERMISSIONS.size());
    }

    private void assertManualPermissionsAreGrantedToAdmin() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM role_permissions rp
                JOIN roles r ON r.id = rp.role_id
                JOIN permissions p ON p.id = rp.permission_id
                WHERE r.name = 'ROLE_ADMIN'
                  AND p.name IN (?, ?, ?, ?, ?)
                """, Integer.class, MANUAL_PERMISSIONS.toArray());

        assertThat(count).isEqualTo(MANUAL_PERMISSIONS.size());
    }

    private void assertNullableReversalUniqueConstraintAllowsManyNullsButRejectsDuplicateOriginal() {
        jdbcTemplate.update("""
                INSERT INTO acc_journal_entries
                    (event_type, source_type, source_id, posting_date, status, reversal_of_id)
                VALUES (?, ?, NULL, ?, ?, NULL)
                """, "MANUAL", "MIGRATION_NULL_REVERSAL_A", LocalDate.of(2026, 5, 31), "DRAFT");
        jdbcTemplate.update("""
                INSERT INTO acc_journal_entries
                    (event_type, source_type, source_id, posting_date, status, reversal_of_id)
                VALUES (?, ?, NULL, ?, ?, NULL)
                """, "MANUAL", "MIGRATION_NULL_REVERSAL_B", LocalDate.of(2026, 5, 31), "DRAFT");

        jdbcTemplate.update("""
                INSERT INTO acc_journal_entries
                    (event_type, source_type, source_id, posting_date, status)
                VALUES (?, ?, NULL, ?, ?)
                """, "MANUAL", "MIGRATION_REVERSAL_ORIGINAL", LocalDate.of(2026, 5, 31), "POSTED");
        Long originalId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM acc_journal_entries
                WHERE source_type = 'MIGRATION_REVERSAL_ORIGINAL'
                """, Long.class);

        jdbcTemplate.update("""
                INSERT INTO acc_journal_entries
                    (event_type, source_type, source_id, posting_date, status, reversal_of_id)
                VALUES (?, ?, NULL, ?, ?, ?)
                """, "MANUAL", "MIGRATION_REVERSAL_FIRST", LocalDate.of(2026, 5, 31), "POSTED", originalId);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO acc_journal_entries
                    (event_type, source_type, source_id, posting_date, status, reversal_of_id)
                VALUES (?, ?, NULL, ?, ?, ?)
                """, "MANUAL", "MIGRATION_REVERSAL_SECOND", LocalDate.of(2026, 5, 31), "POSTED", originalId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
