package com.solusi.erp.system.monitoring.infrastructure.adapter;

import com.solusi.erp.system.monitoring.domain.model.LogEntry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LogReaderAdapter — Unit Test")
class LogReaderAdapterTest {

    @TempDir
    Path tempDir;

    private LogReaderAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LogReaderAdapter(tempDir.toString());
    }

    private void writeLogFile(String filename, String content) throws IOException {
        Files.writeString(tempDir.resolve(filename), content);
    }

    @Test
    @DisplayName("readRecentLogs returns empty list when log file does not exist")
    void readRecentLogs_noFile_returnsEmpty() {
        List<LogEntry> result = adapter.readRecentLogs(100, null, null);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("readRecentLogs parses pipe-delimited log entries")
    void readRecentLogs_parsesEntries() throws IOException {
        String logContent = """
                2026-04-08 10:00:00|INFO|main|com.solusi.erp.App|Application started
                2026-04-08 10:00:01|WARN|main|com.solusi.erp.Svc|Slow query detected
                2026-04-08 10:00:02|ERROR|main|com.solusi.erp.Svc|NullPointerException
                """;
        writeLogFile("erp.log", logContent);

        List<LogEntry> result = adapter.readRecentLogs(100, null, null);

        assertThat(result).hasSize(3);
        // Entries are reversed (newest first)
        assertThat(result.get(0).level()).isEqualTo("ERROR");
        assertThat(result.get(1).level()).isEqualTo("WARN");
        assertThat(result.get(2).level()).isEqualTo("INFO");
    }

    @Test
    @DisplayName("readRecentLogs filters by level")
    void readRecentLogs_filtersByLevel() throws IOException {
        String logContent = """
                2026-04-08 10:00:00|INFO|main|com.solusi.erp.App|Info message
                2026-04-08 10:00:01|ERROR|main|com.solusi.erp.App|Error message
                2026-04-08 10:00:02|INFO|main|com.solusi.erp.App|Another info
                """;
        writeLogFile("erp.log", logContent);

        List<LogEntry> result = adapter.readRecentLogs(100, "ERROR", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).level()).isEqualTo("ERROR");
        assertThat(result.get(0).message()).isEqualTo("Error message");
    }

    @Test
    @DisplayName("readRecentLogs filters by keyword in message")
    void readRecentLogs_filtersByKeyword() throws IOException {
        String logContent = """
                2026-04-08 10:00:00|INFO|main|com.solusi.erp.App|Application started
                2026-04-08 10:00:01|INFO|main|com.solusi.erp.App|User logged in
                2026-04-08 10:00:02|ERROR|main|com.solusi.erp.App|User session expired
                """;
        writeLogFile("erp.log", logContent);

        List<LogEntry> result = adapter.readRecentLogs(100, null, "user");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> e.message().toLowerCase().contains("user"));
    }

    @Test
    @DisplayName("readRecentLogs merges multi-line stack traces with preceding entry")
    void readRecentLogs_mergesStackTrace() throws IOException {
        String logContent = """
                2026-04-08 10:00:00|INFO|main|com.solusi.erp.App|Application started
                2026-04-08 10:00:01|ERROR|main|com.solusi.erp.App|Failed to process
                java.lang.NullPointerException: null
                \tat com.solusi.erp.Service.execute(Service.java:42)
                \tat com.solusi.erp.Controller.handle(Controller.java:15)
                2026-04-08 10:00:02|INFO|main|com.solusi.erp.App|Recovered
                """;
        writeLogFile("erp.log", logContent);

        List<LogEntry> result = adapter.readRecentLogs(100, null, null);

        assertThat(result).hasSize(3);
        // The ERROR entry (second in chronological order, index 1 after reversal) should have stack trace
        LogEntry errorEntry = result.stream().filter(e -> "ERROR".equals(e.level())).findFirst().orElse(null);
        assertThat(errorEntry).isNotNull();
        assertThat(errorEntry.stackTrace()).contains("NullPointerException");
        assertThat(errorEntry.stackTrace()).contains("Service.java:42");
    }

    @Test
    @DisplayName("readRecentLogs respects limit parameter")
    void readRecentLogs_respectsLimit() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("2026-04-08 10:00:").append(String.format("%02d", i))
                    .append("|INFO|main|com.solusi.erp.App|Message ").append(i).append("\n");
        }
        writeLogFile("erp.log", sb.toString());

        List<LogEntry> result = adapter.readRecentLogs(10, null, null);

        assertThat(result).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("getLogFileStream returns file content for main log")
    void getLogFileStream_returnsMainLog() throws IOException {
        String content = "line1\nline2\n";
        writeLogFile("erp.log", content);

        InputStream stream = adapter.getLogFileStream(false);
        String result = new String(stream.readAllBytes());

        assertThat(result).isEqualTo(content);
    }

    @Test
    @DisplayName("getLogFileStream returns error log when errorOnly=true")
    void getLogFileStream_returnsErrorLog() throws IOException {
        String content = "error1\nerror2\n";
        writeLogFile("erp-error.log", content);

        InputStream stream = adapter.getLogFileStream(true);
        String result = new String(stream.readAllBytes());

        assertThat(result).isEqualTo(content);
    }

    @Test
    @DisplayName("getLogFileStream returns empty stream when file not found")
    void getLogFileStream_returnsEmptyWhenNotFound() {
        InputStream stream = adapter.getLogFileStream(false);
        assertThat(stream).isNotNull();
    }

    @Test
    @DisplayName("getLogFileSize returns correct file size")
    void getLogFileSize_returnsCorrectSize() throws IOException {
        String content = "some log content";
        writeLogFile("erp.log", content);

        long size = adapter.getLogFileSize(false);

        assertThat(size).isEqualTo(content.length());
    }

    @Test
    @DisplayName("getLogFileSize returns 0 when file not found")
    void getLogFileSize_returnsZeroWhenNotFound() {
        long size = adapter.getLogFileSize(false);
        assertThat(size).isEqualTo(0);
    }
}
