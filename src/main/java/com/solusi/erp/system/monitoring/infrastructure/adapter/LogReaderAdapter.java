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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private static final Pattern ARCHIVED_LOG_DATE_PATTERN =
            Pattern.compile("erp-(\\d{4}-\\d{2}-\\d{2})\\.\\d+\\.log(?:\\.gz)?$");

    private final Path logDirectory;

    public LogReaderAdapter(String logDirectory) {
        this.logDirectory = Path.of(logDirectory);
    }

    @Override
    public LogViewResult readRecentLogs(int limit, Set<String> levelFilters, String keyword,
                                         Integer sessionId, String timeRange,
                                         String dateFrom, String dateTo) {
        Path logFile = logDirectory.resolve("erp.log");
        if (!Files.exists(logFile)) {
            log.debug("Log file not found: {}", logFile);
            return new LogViewResult(List.of(), 0, 0, 0, 0, 0);
        }

        try {
            // 1) Compute time window early — needed to decide which archived files to read
            LocalDateTime[] window = computeTimeWindow(timeRange, dateFrom, dateTo);
            LocalDateTime windowFrom = window[0];
            LocalDateTime windowTo   = window[1];

            // 2) Collect lines from current log + relevant archived files
            List<String> allLines = new ArrayList<>();

            // Read archived .log.gz (and non-compressed archived) files when range requires history
            if (windowFrom != null) {
                List<Path> archivedFiles = collectRelevantArchivedFiles(windowFrom);
                for (Path archived : archivedFiles) {
                    try {
                        allLines.addAll(readArchivedLogLines(archived));
                    } catch (IOException e) {
                        log.warn("Failed to read archived log file {}: {}", archived, e.getMessage());
                    }
                }
            }

            // Always read the current erp.log (tail)
            allLines.addAll(tailFile(logFile, MAX_TAIL_LINES));

            List<LogEntry> entries = parseLogLines(allLines);

            // 3) Time range filter
            if (windowFrom != null || windowTo != null) {
                entries = entries.stream()
                        .filter(e -> isWithinWindow(e, windowFrom, windowTo))
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            // 4) Session filter
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

            // 5) Level filter (multi-select)
            if (levelFilters != null && !levelFilters.isEmpty()) {
                entries = entries.stream()
                        .filter(e -> levelFilters.contains(e.level()))
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            // 6) Keyword filter
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
                if (sessions.size() >= 10) break; // Limit to 10 most recent sessions
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

    @Override
    public void clearLog() {
        Path logFile = logDirectory.resolve("erp.log");
        try {
            Files.writeString(logFile, "", StandardCharsets.UTF_8);
            log.info("Log file truncated by user request: {}", logFile);
        } catch (IOException e) {
            log.error("Failed to truncate log file: {}", logFile, e);
            throw new RuntimeException("Failed to clear log file", e);
        }

        // Delete all archived log files (erp-YYYY-MM-DD.N.log.gz / .log) and erp-error.log
        try (var stream = Files.list(logDirectory)) {
            stream
                .filter(p -> {
                    String name = p.getFileName().toString();
                    return ARCHIVED_LOG_DATE_PATTERN.matcher(name).find()
                        || name.equals("erp-error.log");
                })
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                        log.info("Deleted archived log file: {}", p.getFileName());
                    } catch (IOException ex) {
                        log.warn("Failed to delete archived log file: {}", p.getFileName(), ex);
                    }
                });
        } catch (IOException e) {
            log.warn("Failed to list log directory for archived cleanup: {}", e.getMessage());
        }
    }

    @Override
    public InputStream getAllLogsZipStream() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos);
                 var stream = Files.list(logDirectory)) {
                List<Path> files = stream
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.startsWith("erp") &&
                            (name.endsWith(".log") || name.endsWith(".log.gz") || name.endsWith(".gz"));
                    })
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());
                for (Path file : files) {
                    zos.putNextEntry(new ZipEntry(file.getFileName().toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException e) {
            log.error("Failed to create log ZIP archive", e);
            throw new RuntimeException("Failed to create log ZIP archive", e);
        }
    }

    /**
     * Collects archived log files (erp-*.log.gz and erp-*.log) whose date is on or after
     * the start of (windowFrom date - 1 day), to account for logs near midnight rollover.
     * Returns files sorted by date ascending so they are merged before erp.log.
     */
    List<Path> collectRelevantArchivedFiles(LocalDateTime windowFrom) {
        LocalDate cutoffDate = windowFrom.toLocalDate().minusDays(1);
        try (var stream = Files.list(logDirectory)) {
            return stream
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        Matcher m = ARCHIVED_LOG_DATE_PATTERN.matcher(name);
                        if (!m.find()) return false;
                        try {
                            LocalDate fileDate = LocalDate.parse(m.group(1));
                            return !fileDate.isBefore(cutoffDate);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .sorted(Comparator.comparing(p -> {
                        Matcher m = ARCHIVED_LOG_DATE_PATTERN.matcher(p.getFileName().toString());
                        return m.find() ? m.group(1) : "";
                    }))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("Failed to list log directory for archived files: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Reads all lines from an archived log file (supports both .gz and plain .log).
     */
    List<String> readArchivedLogLines(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        String name = file.getFileName().toString();
        InputStream raw = Files.newInputStream(file);
        InputStream in = name.endsWith(".gz") ? new GZIPInputStream(raw) : raw;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
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

    private LocalDateTime[] computeTimeWindow(String timeRange, String dateFrom, String dateTo) {
        // Custom range: use explicit from/to dates if provided
        if ("custom".equals(timeRange) || (dateFrom != null && !dateFrom.isBlank())
                || (dateTo != null && !dateTo.isBlank())) {
            LocalDateTime from = parseDateInput(dateFrom);
            LocalDateTime to   = parseDateInput(dateTo);
            return new LocalDateTime[]{from, to};
        }
        // Preset range: compute cutoff from "now"
        if (timeRange == null || timeRange.isBlank()) return new LocalDateTime[]{null, null};
        LocalDateTime now = LocalDateTime.now(WIB);
        LocalDateTime cutoff = switch (timeRange) {
            case "15m" -> now.minusMinutes(15);
            case "1h"  -> now.minusHours(1);
            case "6h"  -> now.minusHours(6);
            case "24h" -> now.minusHours(24);
            case "7d"  -> now.minusDays(7);
            default    -> null;
        };
        return new LocalDateTime[]{cutoff, null};
    }

    private boolean isWithinWindow(LogEntry entry, LocalDateTime from, LocalDateTime to) {
        LocalDateTime ts = parseTimestamp(entry.timestamp());
        if (ts == null) return false;
        if (from != null && ts.isBefore(from)) return false;
        if (to   != null && ts.isAfter(to))   return false;
        return true;
    }

    private LocalDateTime parseDateInput(String input) {
        if (input == null || input.isBlank()) return null;
        try {
            return LocalDateTime.parse(input.trim(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            return null;
        }
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
