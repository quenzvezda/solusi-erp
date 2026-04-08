# System Monitoring & Logging Architecture

## Overview

This document describes the system monitoring and logging architecture implemented in SOL-37 (Improvement - Production Logging & Real-time Monitoring). The system provides real-time visibility into application health, performance metrics, and logs without requiring direct SSH access to the VPS.

**Business Value:**
- **Production Visibility**: Admin/IT can view logs from web UI without SSH terminal access
- **Real-time Monitoring**: Live health status for Database, MinIO, and Disk services
- **Error Triaging**: Multi-level log filtering, search, and stack trace viewing
- **Compliance & Audit**: Centralized log retention (7 days), searchable history, export capability
- **Reduced Operational Friction**: No need for `journalctl` + `CTRL+F` workaround; professional log viewer with proper filtering

---

## 1. Logging Stack Overview

### 1.1 Logback Configuration

The application uses **Logback** (SLF4J implementation) with automatic log rotation and retention.

**Location**: `src/main/resources/logback-spring.xml`

#### Configuration Details

```xml
<property name="LOG_DIR" value="${LOG_DIR:-logs}"/>
<property name="LOG_FILE" value="${LOG_DIR}/erp.log"/>
<property name="ERROR_LOG_FILE" value="${LOG_DIR}/erp-error.log"/>
```

#### File Appenders

| Appender | Purpose | Retention | Pattern |
|----------|---------|-----------|---------|
| **CONSOLE** | Dev/test output | N/A | `%d{yyyy-MM-dd HH:mm:ss.SSS, Asia/Jakarta} [%thread] %-5level %logger{36} - %msg%n` |
| **FILE** (erp.log) | All log levels | 7 days, max 500MB total | `%d\|%level\|%thread\|%logger{60}\|%msg%n` |
| **ERROR_FILE** (erp-error.log) | ERROR level only | 14 days, max 200MB total | `%d\|%level\|%thread\|%logger{60}\|%msg%n%ex` |
| **MEMORY** | In-memory cyclic buffer (500 entries) | Runtime only | Used by monitoring UI for live tail |

#### Timestamp Handling

- **Timezone**: Asia/Jakarta (WIB, UTC+7)
- **Format**: `yyyy-MM-dd HH:mm:ss.SSS`
- **Display in UI**: Two-line format — date on line 1, time on line 2 (space optimization)

### 1.2 Log Rotation & Retention

**Mechanism**: Automatic Logback rolling policy — **NOT a cron job**

```xml
<rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
    <fileNamePattern>${LOG_DIR}/erp-%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
    <maxHistory>7</maxHistory>
    <maxFileSize>50MB</maxFileSize>
    <totalSizeCap>500MB</totalSizeCap>
</rollingPolicy>
```

**How it works:**

1. **Rollover triggers daily** (at midnight) or when a file exceeds 50MB
2. **Naming**: Files are timestamped and numbered
   - `erp-2026-04-08.0.log.gz`
   - `erp-2026-04-08.1.log.gz` (if file 0 reached 50MB)
3. **Automatic deletion**: Files older than 7 days are automatically removed
4. **Capacity management**: If total folder size exceeds 500MB, oldest files are deleted first

**Timeline Example** (7-day rolling window):

```
[Kept] erp-2026-04-02.0.log.gz  ← Day 7
[Kept] erp-2026-04-03.0.log.gz  ← Day 6
...
[Kept] erp-2026-04-08.0.log.gz  ← Day 1 (current)
[Deleted] erp-2026-04-01.0.log.gz  ← Older than 7 days
```

**Key Points:**
- No manual cleanup required — Logback handles rotation internally
- Error logs retained for **14 days** (longer than general logs for audit purposes)
- Files are **gzip compressed** to reduce disk usage
- Compression reduces size to ~20-30% of original

---

## 2. Monitoring Dashboard Architecture

### 2.1 Module Structure

**Location**: `src/main/java/com/solusi/erp/system/monitoring/`

```
monitoring/
├── domain/
│   ├── model/
│   │   ├── LogEntry.java          # DTO: log line with level, timestamp, thread, logger, message
│   │   ├── ServiceHealth.java       # DTO: health status of a service (UP/DOWN/DEGRADED)
│   │   └── SystemHealthSnapshot.java # Aggregate: snapshot of all system metrics
│   └── port/
│       ├── HealthCheckPort.java     # Interface: health check for external services
│       └── LogReaderPort.java        # Interface: read and filter logs
├── infrastructure/
│   ├── adapter/
│   │   ├── HealthCheckAdapter.java   # Implementation: DB/MinIO/Disk health checks
│   │   └── LogReaderAdapter.java     # Implementation: read logs from file + memory buffer
│   └── config/
│       └── MonitoringConfig.java     # Spring configuration, bean wiring
├── application/
│   └── service/
│       └── MonitoringQueryService.java # Use case: aggregate health + logs
├── web/
│   ├── controller/
│   │   └── MonitoringController.java  # REST endpoints
│   └── dto/
│       └── MonitoringHealthResponse.java # JSON response model
└── [tests] (36 tests total)
```

**Design Pattern**: Clean Architecture + DDD
- Domain models are persistence-agnostic
- Adapters (HealthCheckAdapter, LogReaderAdapter) implement domain ports
- Service orchestrates queries across adapters
- Controller exposes REST API and renders templates

### 2.2 Core Components

#### HealthCheckAdapter

Checks real-time status of critical services:

```java
public ServiceHealth checkDatabase();    // SELECT 1 query
public ServiceHealth checkMinIO();       // HEAD /minio/ping
public ServiceHealth checkDiskSpace();   // df command parse
public JvmMetrics getJvmMetrics();       // Runtime.getRuntime() stats
```

**Status values:**
- `UP` — Service responsive
- `DOWN` — Service unreachable or error
- `DEGRADED` — Service slow or partially available

#### LogReaderAdapter

Reads logs from two sources:

1. **File-based** (persistent logs):
   - Parses `erp.log` files in `logs/` directory
   - Filters by timestamp, log level, logger name, message content
   - Supports date range queries (from/to)

2. **Memory buffer** (live logs, current session only):
   - Logback `CyclicBufferAppender` holds last 500 entries
   - Used for real-time display when UI polls
   - Cleared on app restart

**Log Entry Structure**:
```java
class LogEntry {
    LocalDateTime timestamp;  // Parsed from pattern: %d{yyyy-MM-dd HH:mm:ss.SSS}
    LogLevel level;           // ERROR, WARN, INFO, DEBUG, TRACE
    String thread;            // Thread name
    String logger;            // Class logger name (abbreviated)
    String message;           // Log message text
    String stackTrace;        // Exception trace (if present)
}
```

---

## 3. Monitoring UI Features

**Route**: `GET /monitoring` (permission-gated)  
**Template**: `src/main/resources/templates/system/monitoring/index.html`

### 3.1 Health Status Cards

Real-time status display for:
- Database (connection pool status)
- MinIO (S3 object storage)
- Disk Space (filesystem usage)
- JVM Metrics (uptime, heap memory, active threads)

**Auto-refresh**: Every 10 seconds (configurable via UI toggle)

### 3.2 Log Viewer

#### Filtering & Search

| Feature | Implementation |
|---------|-----------------|
| **Level Filter** | Multi-select checkboxes (ERROR, WARN, INFO, DEBUG) |
| **Time Range** | Presets: Last 15m, 1h, 6h, 24h, 7d + Custom range modal |
| **Keyword Search** | HTMX live search (debounced) in message column |
| **Logger Filter** | Shows unique logger names with count |

#### Display & Interaction

| Feature | Details |
|---------|---------|
| **Two-line timestamp** | Date on line 1, HH:mm:ss on line 2 (space optimization) |
| **Expandable rows** | Click row to expand full details + stack trace |
| **Copy functionality** | Click log-level badge to copy full row info to clipboard |
| **Load More** | Pagination: loads 50 rows at a time (prevents UI lag) |
| **Download** | Export filtered logs as text file (respects active filters) |

#### Auto-refresh (Polling)

- **Method**: HTMX polling on `GET /monitoring/api/logs`
- **Interval**: Default 10s, user-configurable (5s - 60s)
- **Behavior**: Fetches new entries since last poll, appends to table
- **Smart polling**: Pauses expansion when user is viewing stack trace (prevents closing on refresh)

### 3.3 Session Detection

When server restarts, logs are segmented by session. UI shows:
- Current session logs (since last restart)
- Previous session logs (historical)
- Session boundary markers

**Limitation**: With frequent restarts, session list grows. Current UI handles up to 10 sessions; beyond that requires scroll or select dropdown.

---

## 4. Permission & Security

### 4.1 Role-Based Access Control

**Permissions** (SYS-01 permission group):
- `MONITORING_READ` — View logs and health status
- `MONITORING_DOWNLOAD` — Export/download log files

**Role Grant**:
- Automatic grant to `ROLE_ADMIN` on first app startup (Flyway migration)
- Custom roles can be assigned via Permission Management UI

### 4.2 Access Control Implementation

```java
@PreAuthorize("hasAuthority('MONITORING_READ')")
@GetMapping("/monitoring")
public String monitoringPage() { ... }

@PreAuthorize("hasAuthority('MONITORING_DOWNLOAD')")
@GetMapping("/monitoring/api/logs/download")
public ResponseEntity<?> downloadLogs() { ... }
```

---

## 5. Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                          Application                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐           ┌──────────────┐                   │
│  │ Spring Logs  │────→ Logback ────→ Multiple Appenders          │
│  └──────────────┘           └──────────────┘                    │
│                                     │                           │
│                    ┌────────────────┼────────────────┐           │
│                    ↓                ↓                ↓            │
│              ┌──────────┐   ┌──────────────┐   ┌────────┐        │
│              │Console   │   │FILE Appenders│   │MEMORY  │        │
│              │(Dev)     │   │(Persistent)  │   │Buffer  │        │
│              └──────────┘   └──────────────┘   └────────┘        │
│                                     │                ↑            │
│              ┌──────────────────────┘                │            │
│              ↓                                       │            │
│  ┌─────────────────────────────┐                   │            │
│  │ Disk: logs/                 │                   │            │
│  │ ├─ erp.log (current)        │     (Read on      │            │
│  │ ├─ erp-2026-04-08.0.log.gz  │      polling)     │            │
│  │ └─ erp-error.log            │                   │            │
│  └─────────────────────────────┘                   │            │
│                                                    │            │
└────────────────────────────────────────────────────┼─────────────┘
                                                    │
                                    ┌───────────────┴────────┐
                                    │                        │
                          ┌─────────▼────────┐   ┌──────────▼───┐
                          │ LogReaderAdapter  │   │ HealthCheck  │
                          │ (File + Memory)   │   │ Adapter      │
                          └─────────┬─────────┘   └──────┬───────┘
                                    │                    │
                          ┌─────────▼────────────────────▼────────┐
                          │ MonitoringQueryService (Orchestrator) │
                          └─────────┬─────────────────────────────┘
                                    │
                          ┌─────────▼──────────────┐
                          │ MonitoringController   │
                          │ (REST API + Templates) │
                          └─────────┬──────────────┘
                                    │
                          ┌─────────▼──────────────────┐
                          │ Web Browser                │
                          │ /monitoring               │
                          │ - Health cards            │
                          │ - Log table (HTMX)        │
                          │ - Polling (10s default)   │
                          └────────────────────────────┘
```

---

## 6. Request-Response Flow (Example)

### Scenario: User filters logs for "ERROR" in last 1 hour

**1. UI Form Submit**

```html
<!-- Filter form (GET) -->
GET /monitoring?levels=ERROR&timeRange=1h&search=
```

**2. Controller Processing**

```java
@GetMapping("/monitoring")
public String monitoringPage(
    @RequestParam(defaultValue = "ERROR,WARN") String levels,
    @RequestParam(defaultValue = "1h") String timeRange,
    @RequestParam(defaultValue = "") String search,
    Model model
) {
    // Parse levels: ["ERROR", "WARN"]
    // Parse timeRange: now() - 1 hour
    
    List<LogEntry> logs = monitoringQueryService.getFilteredLogs(
        levels, timeRange, search, limit=50
    );
    
    model.addAttribute("logs", logs);
    return "system/monitoring/index";
}
```

**3. Backend Service**

```
MonitoringQueryService.getFilteredLogs()
    → LogReaderAdapter.readLogs(dateFrom, dateTo, levels, keyword)
        → Read disk files: erp-*.log.gz files in range
        → Filter by level & keyword
        → Parse stack traces
        → Return 50 entries (Load More pagination)
    → HealthCheckAdapter.getSystemHealth()
        → DB check, MinIO check, Disk space, JVM metrics
    → Combine into model
```

**4. Template Rendering**

```html
<div id="log-table-container">
    <table>
        <tbody>
            <tr data-log-id="1">
                <td class="log-level-badge bg-danger">ERROR</td>
                <td class="log-timestamp">2026-04-08<br/>14:22:31</td>
                <td class="log-logger">c.s.e.i.c.OrderService</td>
                <td class="log-message">Payment gateway timeout: stripe_api_error</td>
            </tr>
            <!-- ... more rows ... -->
        </tbody>
        <tfoot>
            <tr id="load-more-row" hx-get="/monitoring/api/logs/more?offset=50" ...>
                <td colspan="4"><button>Load More (50 entries)</button></td>
            </tr>
        </tfoot>
    </table>
</div>
```

**5. Polling (Auto-refresh)**

```javascript
// Every 10 seconds
GET /monitoring/api/logs?lastTimestamp=2026-04-08T14:22:31&levels=ERROR
    → Returns only new logs since last poll
    → HTMX appends to table (preserves expansion state)
```

---

## 7. Database & State Management

### 7.1 Permission Setup (Flyway V42)

```sql
INSERT INTO permission_group (code, name, description)
VALUES ('SYS-01', 'System Management', 'System monitoring, maintenance, admin tasks');

INSERT INTO permission (code, name, group_id)
VALUES 
    ('MONITORING_READ', 'View Logs & Health', <group_id>),
    ('MONITORING_DOWNLOAD', 'Download Logs', <group_id>);

-- Auto-grant to ROLE_ADMIN
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.code = 'ROLE_ADMIN' AND p.code IN ('MONITORING_READ', 'MONITORING_DOWNLOAD');
```

### 7.2 No Dedicated Logging Table

**Design decision**: Logs are NOT stored in database — only on disk.

**Rationale**:
- Database insert overhead would slow down application
- Disk is cheaper for storage than database
- File-based logs are portable (can be archived, rotated independently)
- Logback handles rotation efficiently
- Web UI reads from disk files on-demand

### 7.3 Session Detection (No DB Table)

Sessions are detected by:
- Reading timestamps in log filenames: `erp-2026-04-08.0.log.gz`
- Comparing with application startup time (stored in `EnvironmentProcessor` or context)
- Grouping logs into sessions (current session + historical)

---

## 8. i18n Support

**Supported Languages**: English (EN), Indonesian (ID)

| Key | EN | ID |
|-----|--|----|
| `monitoring.title` | System Monitoring | Monitoring Sistem |
| `monitoring.health.database` | Database | Database |
| `monitoring.logs.level.error` | Error | Kesalahan |
| `monitoring.logs.custom.modal.title` | Custom Date Range | Rentang Tanggal Kustom |
| ... | ... | ... |

**Messages Files**:
- `src/main/resources/messages.properties` (English)
- `src/main/resources/messages_id.properties` (Indonesian)

---

## 9. Testing Strategy

### 9.1 Test Coverage (36 Tests Total)

| Test Class | Count | Purpose |
|------------|-------|---------|
| **MonitoringQueryServiceTest** | 10 | Service logic: filtering, health aggregation |
| **LogReaderAdapterTest** | 8 | Log file parsing, filtering, date range queries |
| **HealthCheckAdapterTest** | 6 | Service health checks (DB, MinIO, Disk, JVM) |
| **MonitoringControllerTest** | 4 | Controller endpoints, permission checks |
| **MonitoringTemplateTest** | 4 | Thymeleaf rendering, i18n resolution |
| **MonitoringIntegrationTest** | 4 | End-to-end flow: request → template → response |

### 9.2 Playwright E2E Tests

**Test Protocol**: `@docs/AGENTS.md`

- [ ] Health cards display and auto-refresh
- [ ] Filter logs by level (multi-select)
- [ ] Filter logs by time range (preset + custom modal)
- [ ] Search keyword in message column
- [ ] Expand/collapse stack trace
- [ ] Copy row on badge click
- [ ] Load More pagination
- [ ] Download logs respects active filters
- [ ] Polling auto-refresh without closing expanded rows
- [ ] Rate limiting on manual refresh (5-10s delay)

---

## 10. Common Issues & Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| **"No logs visible"** | Log files > 7 days old were deleted; app just restarted | Check `logs/` folder; wait for new logs to generate |
| **"Permission denied"** | User lacks `MONITORING_READ` | Admin grants permission via Permission Management UI |
| **"Download button disabled"** | User lacks `MONITORING_DOWNLOAD` | Admin grants permission |
| **"Memory buffer empty at startup"** | Memory appender holds only current session logs | Older logs available in disk files (erp-*.log.gz) |
| **"Timestamp wrong timezone"** | Server timezone differs from WIB (UTC+7) | Check system TZ; Logback hardcodes Asia/Jakarta in pattern |
| **"Polling too slow"** | Network latency or backend CPU spike | Increase poll interval; check application performance |
| **"Load More button in wrong place"** | Bootstrap CSS conflict | Clear browser cache; reload page |

---

## 11. Performance & Scalability

### 11.1 Log File Size Management

Typical application generates:
- **General log** (~50MB/day) → rolls daily + 50MB size limit
- **Error log** (~5MB/day) → rolls daily
- **Compression**: gzip reduces to ~15% of original

**Example disk usage** (7-day window):
```
logs/
├── erp-2026-04-02.0.log.gz     (2.3 MB)
├── erp-2026-04-03.0.log.gz     (2.5 MB)
├── erp-2026-04-04.0.log.gz     (2.1 MB)
├── erp-2026-04-05.0.log.gz     (2.4 MB)
├── erp-2026-04-06.0.log.gz     (2.2 MB)
├── erp-2026-04-07.0.log.gz     (2.3 MB)
├── erp-2026-04-08.0.log.gz     (1.8 MB)  ← current, incomplete
├── erp.log                     (23.4 MB) ← uncompressed, in-use
├── erp-error-2026-04-08.0.log  (0.4 MB)
└── erp-error.log               (0.5 MB)
```

**Total**: ~40 MB (well within 500MB cap)

### 11.2 Polling Performance

**Optimization techniques**:
1. **Partial response**: Poll returns only new entries since `lastTimestamp`
2. **Pagination**: UI loads 50 rows at a time, more on demand
3. **HTMX fragment swap**: Only appends new rows, no full page reload
4. **Memory buffer**: First ~500 entries served from memory (no file I/O)

**Expected response time**:
- Fresh poll (memory buffer): <50ms
- File-based query (larger range): 100-500ms depending on file size

---

## 12. Deployment Checklist

- [x] Logback configured with WIB timezone + rolling policy
- [x] `logs/` directory created and writable by app user
- [x] Nginx reverse proxy configured (no special headers needed)
- [x] Permissions seeded (ROLE_ADMIN auto-grant on V42 migration)
- [x] HTTPS enabled (`app.solusi-program.site`)
- [x] i18n locale initialized (default EN, user can switch to ID)
- [ ] Monitor disk usage weekly (may need adjustment if >7 days retention needed)
- [ ] Backup logs periodically if audit compliance required

---

## 13. Future Enhancements

| Feature | Complexity | Business Value |
|---------|-----------|-----------------|
| **WebSocket real-time logs** | High | Better UX than polling; lower server CPU |
| **Log statistics dashboard** (ERROR count/hour chart) | Medium | Identify patterns, peak error times |
| **Alert notifications** (email on ERROR threshold) | Medium | Proactive issue detection |
| **Saved filter presets** (favorite filter combos) | Low | Faster triaging for ops team |
| **Elasticsearch integration** (centralized logging) | Very High | Suitable for multi-node deployments |
| **Log retention policy UI** (change 7 days dynamically) | Low | Compliance with custom retention rules |
| **Highlight search keywords** in log message | Very Low | Better readability |

---

## Appendix A: File Structure Reference

```
src/main/java/com/solusi/erp/system/monitoring/
├── application/
│   └── service/
│       └── MonitoringQueryService.java
├── domain/
│   ├── model/
│   │   ├── LogEntry.java
│   │   ├── ServiceHealth.java
│   │   └── SystemHealthSnapshot.java
│   └── port/
│       ├── HealthCheckPort.java
│       └── LogReaderPort.java
├── infrastructure/
│   ├── adapter/
│   │   ├── HealthCheckAdapter.java
│   │   └── LogReaderAdapter.java
│   └── config/
│       └── MonitoringConfig.java
└── web/
    ├── controller/
    │   └── MonitoringController.java
    └── dto/
        └── MonitoringHealthResponse.java

src/main/resources/
├── logback-spring.xml (Logging config)
├── application.yaml (Actuator endpoint config)
├── messages.properties (i18n EN)
├── messages_id.properties (i18n ID)
├── templates/system/monitoring/
│   └── index.html (UI template)
└── db/migration/
    └── V42__Add_System_Monitoring_Permissions.sql

src/test/java/com/solusi/erp/system/monitoring/
├── application/service/MonitoringQueryServiceTest.java
├── infrastructure/adapter/
│   ├── HealthCheckAdapterTest.java
│   └── LogReaderAdapterTest.java
├── web/controller/MonitoringControllerTest.java
├── web/template/MonitoringTemplateTest.java
└── integration/MonitoringIntegrationTest.java
```

---

## Appendix B: Related Documentation

- **Security**: `@docs/modules/security/permission-groups.md`
- **Deployment**: `@docs/deployment/VPS-FRESH-UBUNTU-SETUP.md`
- **i18n**: See application.yaml locale configuration
- **Logback official**: https://logback.qos.ch/
- **Thymeleaf templating**: `@docs/modules/web/template-engine.md`

