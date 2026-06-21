package com.solusi.erp.core.messaging.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventsMigrationTest {

    private static final Path MARIADB_MIGRATION = Path.of(
            "src/main/resources/db/migration/V76__Add_Outbox_Events.sql");
    private static final Path H2_MIGRATION = Path.of(
            "src/main/resources/db/migration-h2/V76__Add_Outbox_Events.sql");

    @Test
    void mariadbMigrationShouldCreateOutboxEventsContract() throws IOException {
        String sql = normalizedSql(MARIADB_MIGRATION);

        assertOutboxContract(sql);
        assertThat(sql).contains("payload_json longtext");
        assertThat(sql).contains("unique key uk_outbox_events_event_id");
    }

    @Test
    void h2MigrationShouldMirrorOutboxEventsContract() throws IOException {
        String sql = normalizedSql(H2_MIGRATION);

        assertOutboxContract(sql);
        assertThat(sql).contains("payload_json clob");
        assertThat(sql).contains("constraint uk_outbox_events_event_id unique");
    }

    private static String normalizedSql(Path path) throws IOException {
        return Files.readString(path)
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private static void assertOutboxContract(String sql) {
        assertThat(sql)
                .contains("create table if not exists outbox_events")
                .contains("event_id char(36)")
                .contains("event_type varchar")
                .contains("event_version int")
                .contains("aggregate_type varchar")
                .contains("aggregate_id varchar")
                .contains("topic varchar")
                .contains("message_key varchar")
                .contains("status varchar")
                .contains("attempt_count int")
                .contains("last_error")
                .contains("next_attempt_at")
                .contains("published_at")
                .contains("created_date")
                .contains("created_by_user_id")
                .contains("updated_date")
                .contains("updated_by_user_id")
                .contains("idx_outbox_status_next_attempt")
                .contains("idx_outbox_topic_status")
                .contains("idx_outbox_aggregate");
    }
}
