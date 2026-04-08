package com.solusi.erp.system.monitoring.infrastructure.adapter;

import com.solusi.erp.system.monitoring.domain.model.LogEntry;
import com.solusi.erp.system.monitoring.domain.port.LogReaderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Infrastructure adapter that reads log entries from log files on disk.
 * Parses the pipe-delimited format: timestamp|LEVEL|thread|logger|message
 */
public class LogReaderAdapter implements LogReaderPort {

    private static final Logger log = LoggerFactory.getLogger(LogReaderAdapter.class);
    private static final String SEPARATOR = "\\|";

    private final Path logDirectory;

    public LogReaderAdapter(String logDirectory) {
        this.logDirectory = Path.of(logDirectory);
    }

    @Override
    public List<LogEntry> readRecentLogs(int limit, String levelFilter, String keyword) {
        Path logFile = logDirectory.resolve("erp.log");
        if (!Files.exists(logFile)) {
            log.debug("Log file not found: {}", logFile);
            return List.of();
        }

        try {
            List<String> allLines = tailFile(logFile, limit * 3);
            List<LogEntry> entries = parseLogLines(allLines);

            return entries.stream()
                    .filter(e -> matchesLevel(e, levelFilter))
                    .filter(e -> matchesKeyword(e, keyword))
                    .limit(limit)
                    .toList();
        } catch (IOException e) {
            log.error("Failed to read log file: {}", logFile, e);
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

    /**
     * Reads the last N lines from a file efficiently using RandomAccessFile.
     */
    private List<String> tailFile(Path file, int maxLines) throws IOException {
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
    private List<LogEntry> parseLogLines(List<String> lines) {
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
                current = new LogEntry(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        shortenLogger(parts[3].trim()),
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
        return new LogEntry(entry.timestamp(), entry.level(), entry.thread(), entry.logger(), entry.message(), st);
    }

    private boolean isValidLevel(String level) {
        return switch (level.trim()) {
            case "TRACE", "DEBUG", "INFO", "WARN", "ERROR" -> true;
            default -> false;
        };
    }

    private boolean matchesLevel(LogEntry entry, String levelFilter) {
        if (levelFilter == null || levelFilter.isBlank() || "ALL".equalsIgnoreCase(levelFilter)) {
            return true;
        }
        return entry.level().equalsIgnoreCase(levelFilter);
    }

    private boolean matchesKeyword(LogEntry entry, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String lowerKw = keyword.toLowerCase();
        return entry.message().toLowerCase().contains(lowerKw)
                || entry.logger().toLowerCase().contains(lowerKw)
                || (entry.stackTrace() != null && entry.stackTrace().toLowerCase().contains(lowerKw));
    }

    private String shortenLogger(String logger) {
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
