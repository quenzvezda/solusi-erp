package com.solusi.erp.system.monitoring.infrastructure.adapter;

import com.solusi.erp.system.monitoring.domain.model.LogEntry;
import com.solusi.erp.system.monitoring.domain.model.LogViewResult;
import com.solusi.erp.system.monitoring.domain.model.ServerSession;
import com.solusi.erp.system.monitoring.domain.port.LogReaderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter that reads log entries from log files on disk.
 * Parses the pipe-delimited format: timestamp|LEVEL|thread|logger|message
 */
public class LogReaderAdapter implements LogReaderPort {

    private static final Logger log = LoggerFactory.getLogger(LogReaderAdapter.class);
    private static final String SEPARATOR = "\\|";
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final ZoneId WIB = ZoneId.of("Asia/Jakarta");
    private static final String SESSION_MARKER = "Starting SolusiProgramErpApplication";
    private static final int MAX_TAIL_LINES = 10_000;

    private final Path logDirectory;

    public LogReaderAdapter(String logDirectory) {
        this.logDirectory = Path.of(logDirectory);
    }

    @Override
    public LogViewResult readRecentLogs(int limit, Set<String> levelFilters, String keyword,
                                         Integer sessionId, String timeRange) {
        Path logFile = logDirectory.resolve("erp.log");
        if (!Files.exists(logFile)) {
            log.debug("Log file not found: {}", logFile);
            return new LogViewResult(List.of(), 0, 0, 0, 0, 0);
        }

        try {
            List<String> allLines = tailFile(logFile, MAX_TAIL_LINES);
            List<LogEntry> entries = parseLogLines(allLines);

            // 1) Time range filter
            LocalDateTime cutoff = computeCutoff(timeRange);
            if (cutoff != null) {
                entries = entries.stream()
                        .filter(e -> isAfterCutoff(e, cutoff))
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            // 2) Session filter
            if (sessionId != null) {
                List<ServerSession> sessions = detectSessions();
                ServerSession selected = sessions.stream()
                        .filter(s -> s.id() == sessionId)
                        .findFirst().orElse(null);
                if (selected != null) {
                    entries = entries.stream()
                            .filter(e -> isWithinSession(e, selected))
                            .collect(Collectors.toCollection(ArrayList::new));
                }
            }

            // 3) Level filter (multi-select)
            if (levelFilters != null && !levelFilters.isEmpty()) {
                entries = entries.stream()
                        .filter(e -> levelFilters.contains(e.level()))
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            // 4) Keyword filter
            if (keyword != null && !keyword.isBlank()) {
                entries = entries.stream()
                        .filter(e -> matchesKeyword(e, keyword))
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            // Count totals BEFORE applying limit
            long totalMatched = entries.size();
            long errorCount = entries.stream().filter(LogEntry::isError).count();
            long warnCount = entries.stream().filter(LogEntry::isWarn).count();
            long infoCount = entries.stream().filter(e -> "INFO".equals(e.level())).count();
            long debugCount = entries.stream().filter(e -> "DEBUG".equals(e.level())).count();

            List<LogEntry> limited = entries.stream().limit(limit).toList();

            return new LogViewResult(limited, totalMatched, errorCount, warnCount, infoCount, debugCount);
        } catch (IOException e) {
            log.error("Failed to read log file: {}", logFile, e);
            return new LogViewResult(List.of(), 0, 0, 0, 0, 0);
        }
    }

    @Override
    public List<ServerSession> detectSessions() {
        Path logFile = logDirectory.resolve("erp.log");
        if (!Files.exists(logFile)) return List.of();

        try {
            List<String> startTimestamps = new ArrayList<>();
            try (BufferedReader reader = Files.newBufferedReader(logFile, StandardCharsets.UTF_8)) {
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null && count < 100_000) {
                    count++;
                    String[] parts = line.split(SEPARATOR, 5);
                    if (parts.length >= 5 && parts[4].trim().startsWith(SESSION_MARKER)) {
                        startTimestamps.add(parts[0].trim());
                    }
                }
            }

            if (startTimestamps.isEmpty()) return List.of();

            List<ServerSession> sessions = new ArrayList<>();
            for (int i = startTimestamps.size() - 1; i >= 0; i--) {
                int id = startTimestamps.size() - i;
                String start = startTimestamps.get(i);
                String end = (i < startTimestamps.size() - 1) ? startTimestamps.get(i + 1) : null;
                boolean current = (i == startTimestamps.size() - 1);
                String label = current
                        ? "Current Session (" + formatSessionTime(start) + ")"
                        : "Session " + id + " (" + formatSessionTime(start)
                          + " → " + formatSessionTime(end) + ")";
                sessions.add(new ServerSession(id, start, end, current, label));
            }
            return sessions;
        } catch (IOException e) {
            log.error("Failed to detect sessions", e);
            return List.of();
        }
    }

    @Override
    public InputStream getLogFileStream(boolean errorOnly) {
        Path logFile = errorOnly
                ? logDirectory.resolve("erp-error.log")
                : logDirectory.resolve("erp.log");
        try {
            if (Files.exists(logFile)) {
                return Files.newInputStream(logFile);
            }
        } catch (IOException e) {
            log.error("Failed to open log file for download: {}", logFile, e);
        }
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public long getLogFileSize(boolean errorOnly) {
        Path logFile = errorOnly
                ? logDirectory.resolve("erp-error.log")
                : logDirectory.resolve("erp.log");
        try {
            if (Files.exists(logFile)) {
                return Files.size(logFile);
            }
        } catch (IOException e) {
            log.error("Failed to get log file size: {}", logFile, e);
        }
        return 0;
    }

    // ─── Internal helpers ───────────────────────────────────────────────────

    /**
     * Reads the last N lines from a file efficiently using RandomAccessFile.
     */
    List<String> tailFile(Path file, int maxLines) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            long fileLength = raf.length();
            if (fileLength == 0) return List.of();

            List<String> lines = new ArrayList<>();
            long pos = fileLength - 1;
            StringBuilder sb = new StringBuilder();

            while (pos >= 0 && lines.size() < maxLines) {
                raf.seek(pos);
                int ch = raf.read();
                if (ch == '\n') {
                    if (!sb.isEmpty()) {
                        lines.add(sb.reverse().toString());
                        sb = new StringBuilder();
                    }
                } else {
                    sb.append((char) ch);
                }
                pos--;
            }
            if (!sb.isEmpty()) {
                lines.add(sb.reverse().toString());
            }

            Collections.reverse(lines);
            return lines;
        }
    }

    /**
     * Parses pipe-delimited log lines, merging multi-line stack traces with the preceding entry.
     */
    List<LogEntry> parseLogLines(List<String> lines) {
        List<LogEntry> entries = new ArrayList<>();
        LogEntry current = null;
        StringBuilder stackTrace = new StringBuilder();

        for (String line : lines) {
            String[] parts = line.split(SEPARATOR, 5);
            if (parts.length >= 4 && isValidLevel(parts[1])) {
                if (current != null) {
                    entries.add(finalizeEntry(current, stackTrace));
                    stackTrace = new StringBuilder();
                }
                String rawLogger = parts[3].trim();
                current = new LogEntry(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        shortenLogger(rawLogger),
                        rawLogger,
                        parts.length >= 5 ? parts[4].trim() : "",
                        null
                );
            } else if (current != null) {
                if (!stackTrace.isEmpty()) stackTrace.append("\n");
                stackTrace.append(line);
            }
        }
        if (current != null) {
            entries.add(finalizeEntry(current, stackTrace));
        }

        Collections.reverse(entries);
        return entries;
    }

    private LogEntry finalizeEntry(LogEntry entry, StringBuilder stackTrace) {
        String st = stackTrace.isEmpty() ? null : stackTrace.toString();
        return new LogEntry(entry.timestamp(), entry.level(), entry.thread(),
                entry.logger(), entry.fullLogger(), entry.message(), st);
    }

    private boolean isValidLevel(String level) {
        return switch (level.trim()) {
            case "TRACE", "DEBUG", "INFO", "WARN", "ERROR" -> true;
            default -> false;
        };
    }

    private boolean matchesKeyword(LogEntry entry, String keyword) {
        String lowerKw = keyword.toLowerCase();
        return entry.message().toLowerCase().contains(lowerKw)
                || entry.logger().toLowerCase().contains(lowerKw)
                || (entry.fullLogger() != null && entry.fullLogger().toLowerCase().contains(lowerKw))
                || (entry.stackTrace() != null && entry.stackTrace().toLowerCase().contains(lowerKw));
    }

    private LocalDateTime computeCutoff(String timeRange) {
        if (timeRange == null || timeRange.isBlank()) return null;
        LocalDateTime now = LocalDateTime.now(WIB);
        return switch (timeRange) {
            case "15m" -> now.minusMinutes(15);
            case "1h" -> now.minusHours(1);
            case "6h" -> now.minusHours(6);
            case "24h" -> now.minusHours(24);
            case "7d" -> now.minusDays(7);
            default -> null;
        };
    }

    private boolean isAfterCutoff(LogEntry entry, LocalDateTime cutoff) {
        LocalDateTime ts = parseTimestamp(entry.timestamp());
        return ts != null && !ts.isBefore(cutoff);
    }

    private boolean isWithinSession(LogEntry entry, ServerSession session) {
        LocalDateTime ts = parseTimestamp(entry.timestamp());
        if (ts == null) return false;
        LocalDateTime start = parseTimestamp(session.startedAt());
        if (start == null) return false;
        if (ts.isBefore(start)) return false;
        if (session.endedAt() != null) {
            LocalDateTime end = parseTimestamp(session.endedAt());
            if (end != null && !ts.isBefore(end)) return false;
        }
        return true;
    }

    private LocalDateTime parseTimestamp(String ts) {
        if (ts == null) return null;
        try {
            return LocalDateTime.parse(ts.trim(), TIMESTAMP_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatSessionTime(String ts) {
        if (ts == null) return "?";
        LocalDateTime dt = parseTimestamp(ts);
        if (dt == null) return ts;
        return dt.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"));
    }

    String shortenLogger(String logger) {
        if (logger.length() <= 40) return logger;
        int lastDot = logger.lastIndexOf('.');
        if (lastDot > 0) {
            String className = logger.substring(lastDot + 1);
            String packagePart = logger.substring(0, lastDot);
            StringBuilder sb = new StringBuilder();
            for (String part : packagePart.split("\\.")) {
                if (!sb.isEmpty()) sb.append('.');
                sb.append(part.charAt(0));
            }
            sb.append('.').append(className);
            return sb.toString();
        }
        return logger;
    }
}
