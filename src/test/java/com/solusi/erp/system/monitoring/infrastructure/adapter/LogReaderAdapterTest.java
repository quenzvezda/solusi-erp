package com.solusi.erp.system.monitoring.infrastructure.adapter;

import com.solusi.erp.system.monitoring.domain.model.LogEntry;
import com.solusi.erp.system.monitoring.domain.model.LogViewResult;
import com.solusi.erp.system.monitoring.domain.model.ServerSession;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LogReaderAdapter — Unit Test")
class LogReaderAdapterTest {

    @TempDir
    Path tempDir;

    private LogReaderAdapter adapter;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final ZoneId WIB = ZoneId.of("Asia/Jakarta");

    @BeforeEach
    void setUp() {
        adapter = new LogReaderAdapter(tempDir.toString());
    }

    private void writeLogFile(String filename, String content) throws IOException {
        Files.writeString(tempDir.resolve(filename), content);
    }

    private String recentTimestamp(int minutesAgo) {
        return LocalDateTime.now(WIB).minusMinutes(minutesAgo).format(FMT);
    }

    @Test
    @DisplayName("readRecentLogs returns empty result when log file does not exist")
    void readRecentLogs_noFile_returnsEmpty() {
        LogViewResult result = adapter.readRecentLogs(100, Set.of(), null, null, null, null, null);
        assertThat(result.totalMatched()).isZero();
    }

    @Test
    @DisplayName("readRecentLogs parses pipe-delimited log entries with fullLogger")
    void readRecentLogs_parsesEntries() throws IOException {
        String t1 = recentTimestamp(3);
        String t2 = recentTimestamp(2);
        String t3 = recentTimestamp(1);
        String logContent = t1 + "|INFO|main|com.solusi.erp.App|Application started\n"
                + t2 + "|WARN|main|com.solusi.erp.Svc|Slow query detected\n"
                + t3 + "|ERROR|main|com.solusi.erp.Svc|NullPointerException\n";
        writeLogFile("erp.log", logContent);

        LogViewResult result = adapter.readRecentLogs(100, Set.of(), null, null, null, null, null);

        assertThat(result.entries()).hasSize(3);
        assertThat(result.entries().get(0).level()).isEqualTo("ERROR");
        assertThat(result.entries().get(0).fullLogger()).isEqualTo("com.solusi.erp.Svc");
        assertThat(result.totalMatched()).isEqualTo(3);
        assertThat(result.errorCount()).isEqualTo(1);
        assertThat(result.warnCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("readRecentLogs filters by multiple levels")
    void readRecentLogs_filtersByMultipleLevels() throws IOException {
        String t1 = recentTimestamp(3);
        String t2 = recentTimestamp(2);
        String t3 = recentTimestamp(1);
        String logContent = t1 + "|INFO|main|com.solusi.erp.App|Info message\n"
                + t2 + "|ERROR|main|com.solusi.erp.App|Error message\n"
                + t3 + "|WARN|main|com.solusi.erp.App|Warn message\n";
        writeLogFile("erp.log", logContent);

        LogViewResult result = adapter.readRecentLogs(100, Set.of("ERROR", "WARN"), null, null, null, null, null);

        assertThat(result.entries()).hasSize(2);
        assertThat(result.entries()).allMatch(e -> "ERROR".equals(e.level()) || "WARN".equals(e.level()));
        assertThat(result.totalMatched()).isEqualTo(2);
    }

    @Test
    @DisplayName("readRecentLogs filters by keyword in message")
    void readRecentLogs_filtersByKeyword() throws IOException {
        String t1 = recentTimestamp(3);
        String t2 = recentTimestamp(2);
        String t3 = recentTimestamp(1);
        String logContent = t1 + "|INFO|main|com.solusi.erp.App|Application started\n"
                + t2 + "|INFO|main|com.solusi.erp.App|User logged in\n"
                + t3 + "|ERROR|main|com.solusi.erp.App|User session expired\n";
        writeLogFile("erp.log", logContent);

        LogViewResult result = adapter.readRecentLogs(100, Set.of(), "user", null, null, null, null);

        assertThat(result.entries()).hasSize(2);
        assertThat(result.entries()).allMatch(e -> e.message().toLowerCase().contains("user"));
    }

    @Test
    @DisplayName("readRecentLogs merges multi-line stack traces with preceding entry")
    void readRecentLogs_mergesStackTrace() throws IOException {
        String t1 = recentTimestamp(3);
        String t2 = recentTimestamp(2);
        String t3 = recentTimestamp(1);
        String logContent = t1 + "|INFO|main|com.solusi.erp.App|Application started\n"
                + t2 + "|ERROR|main|com.solusi.erp.App|Failed to process\n"
                + "java.lang.NullPointerException: null\n"
                + "\tat com.solusi.erp.Service.execute(Service.java:42)\n"
                + "\tat com.solusi.erp.Controller.handle(Controller.java:15)\n"
                + t3 + "|INFO|main|com.solusi.erp.App|Recovered\n";
        writeLogFile("erp.log", logContent);

        LogViewResult result = adapter.readRecentLogs(100, Set.of(), null, null, null, null, null);

        assertThat(result.entries()).hasSize(3);
        LogEntry errorEntry = result.entries().stream().filter(LogEntry::isError).findFirst().orElse(null);
        assertThat(errorEntry).isNotNull();
        assertThat(errorEntry.stackTrace()).contains("NullPointerException");
        assertThat(errorEntry.stackTrace()).contains("Service.java:42");
    }

    @Test
    @DisplayName("readRecentLogs respects limit parameter and reports total count")
    void readRecentLogs_respectsLimit() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append(recentTimestamp(50 - i))
                    .append("|INFO|main|com.solusi.erp.App|Message ").append(i).append("\n");
        }
        writeLogFile("erp.log", sb.toString());

        LogViewResult result = adapter.readRecentLogs(10, Set.of(), null, null, null, null, null);

        assertThat(result.entries()).hasSize(10);
        assertThat(result.totalMatched()).isEqualTo(50);
    }

    @Test
    @DisplayName("readRecentLogs filters by time range")
    void readRecentLogs_filtersByTimeRange() throws IOException {
        String old = LocalDateTime.now(WIB).minusHours(3).format(FMT);
        String recent = recentTimestamp(5);
        String logContent = old + "|INFO|main|com.solusi.erp.App|Old message\n"
                + recent + "|INFO|main|com.solusi.erp.App|Recent message\n";
        writeLogFile("erp.log", logContent);

        LogViewResult result = adapter.readRecentLogs(100, Set.of(), null, null, "1h", null, null);

        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().get(0).message()).isEqualTo("Recent message");
    }

    @Test
    @DisplayName("detectSessions finds server startup markers")
    void detectSessions_findsStartupMarkers() throws IOException {
        String t1 = "2026-04-07 10:00:00.000";
        String t2 = "2026-04-08 14:00:00.000";
        String logContent = t1 + "|INFO|main|com.solusi.erp.SolusiProgramErpApplication|Starting SolusiProgramErpApplication using Java 21\n"
                + t1 + "|INFO|main|com.solusi.erp.App|Some log\n"
                + t2 + "|INFO|main|com.solusi.erp.SolusiProgramErpApplication|Starting SolusiProgramErpApplication using Java 21\n"
                + t2 + "|INFO|main|com.solusi.erp.App|Another log\n";
        writeLogFile("erp.log", logContent);

        List<ServerSession> sessions = adapter.detectSessions();

        assertThat(sessions).hasSize(2);
        assertThat(sessions.get(0).current()).isTrue();
        assertThat(sessions.get(0).id()).isEqualTo(1);
        assertThat(sessions.get(1).id()).isEqualTo(2);
    }

    @Test
    @DisplayName("readRecentLogs filters by sessionId")
    void readRecentLogs_filtersBySession() throws IOException {
        String t1 = recentTimestamp(30);
        String t2 = recentTimestamp(10);
        String logContent = t1 + "|INFO|main|com.solusi.erp.SolusiProgramErpApplication|Starting SolusiProgramErpApplication using Java 21\n"
                + t1 + "|INFO|main|com.solusi.erp.App|Old session log\n"
                + t2 + "|INFO|main|com.solusi.erp.SolusiProgramErpApplication|Starting SolusiProgramErpApplication using Java 21\n"
                + t2 + "|INFO|main|com.solusi.erp.App|Current session log\n";
        writeLogFile("erp.log", logContent);

        LogViewResult result = adapter.readRecentLogs(100, Set.of(), null, 1, null, null, null);

        assertThat(result.entries()).hasSize(2);
        assertThat(result.entries()).anyMatch(e -> e.message().contains("Current session log"));
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

    @Test
    @DisplayName("readRecentLogs filters by custom date range (dateFrom and dateTo)")
    void readRecentLogs_filtersByCustomDateRange() throws IOException {
        DateTimeFormatter inputFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime base = LocalDateTime.now(WIB).minusHours(5);
        String t1 = base.minusHours(2).format(FMT);
        String t2 = base.format(FMT);
        String t3 = base.plusHours(2).format(FMT);
        String logContent = t1 + "|INFO|main|com.solusi.erp.App|Before range\n"
                + t2 + "|INFO|main|com.solusi.erp.App|In range\n"
                + t3 + "|INFO|main|com.solusi.erp.App|After range\n";
        writeLogFile("erp.log", logContent);

        String from = base.minusMinutes(30).format(inputFmt);
        String to   = base.plusMinutes(30).format(inputFmt);

        LogViewResult result = adapter.readRecentLogs(100, Set.of(), null, null, "custom", from, to);

        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().get(0).message()).isEqualTo("In range");
    }
}
