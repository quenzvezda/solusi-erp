# Journal Web Layer & UI Implementation

**Date**: 2026-05-03  
**Status**: Brainstorm Complete → Ready for Implementation  
**Approach**: Query-Based (Approach A) with Clean Architecture

---

## Executive Summary

Build web layer + UI for Journal Entry viewing (read-only). Users can:
- **List** journal entries with filters (source type, date range, source code, journal code)
- **View detail** (header + lines table)
- **Link from GR detail** to posted journal

Architecture follows **Query-Based pattern** (query port + use cases) for clean separation and reusability.

---

## Architecture Overview

### 1. Domain Layer (Query Port)

**New Interface**: `JournalEntryQueryPort`
```
Location: src/main/java/com/solusi/erp/accounting/journal/domain/port/

Methods:
- findJournalEntries(filter: JournalEntryFilter, pageable: Pageable) → Page<JournalEntry>
- getJournalEntryDetail(id: Long) → Optional<JournalEntry>

Filter fields:
  - sourceType: String (GOODS_RECEIPT, etc)
  - sourceCode: String (GR-0001, etc)
  - journalCode: String (JNL-0001, etc)
  - postingDateFrom: LocalDate
  - postingDateTo: LocalDate
```

### 2. Application Layer (Query Use Cases)

**New Use Cases**:
- `FindJournalEntriesUseCase` — list with pagination + filters
- `GetJournalEntryDetailUseCase` — fetch single entry with lines

Location: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/query/`

### 3. Infrastructure Layer (Query Port Implementation)

**New Class**: `JournalEntryQueryPortImpl`
- Implement `JournalEntryQueryPort`
- Use JPA Specification for dynamic filtering
- Support pagination + sorting

Location: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/adapter/`

### 4. Web Layer

**New Controller**: `JournalEntryController`
```
Endpoints:
- GET /accounting/journal-entries → list page (Thymeleaf)
- GET /accounting/journal-entries/api → list API (JSON, for AJAX filter)
- GET /accounting/journal-entries/{id} → detail page (Thymeleaf)
```

**New Web Mapper**: `JournalEntryWebMapper`
- JournalEntry → JournalEntryListResponse
- JournalEntry → JournalEntryDetailResponse

**New DTOs**:
- `JournalEntryListResponse` — for list view
- `JournalEntryDetailResponse` — for detail view
- `JournalLineResponse` — for lines in detail

### 5. Menu & Permissions

**Menu Hierarchy**:
```
Finance & Accounting > General Ledger > Journal Entry
```

**Permission Entry** (add to V55 migration):
```sql
INSERT INTO permission_groups (name, breadcrumb_id, breadcrumb_en, icon_class, url, created_by, created_date)
VALUES (
  'Journal Entry',
  'Finance & Accounting > General Ledger > Journal Entry',
  'Finance & Accounting > General Ledger > Journal Entry',
  'ti-receipt-2',
  '/accounting/journal-entries',
  1,
  NOW()
);
```

### 6. UI Templates

**List Page** (`journal-entry-list.html`):
- Filter section (source type, date range, source code, journal code)
- Table with columns: Journal Code, Source Type, Source Code, Posting Date, Total Debit, Total Credit, Created By, Created Date
- Pagination
- Row click → detail page

**Detail Page** (`journal-entry-detail.html`):
- Header section: Journal Code, Source Type, Source Code, Posting Date, Description, Status
- Lines table: Account, Debit, Credit
- Summary card: Total Debit, Total Credit, Balanced indicator
- Back button → list page
- Link to source (GR detail) if applicable

---

## Implementation Steps

### Phase 1: Domain & Application (Backend Logic)
1. Create `JournalEntryQueryPort` interface
2. Create `JournalEntryFilter` record
3. Create `FindJournalEntriesUseCase` + impl
4. Create `GetJournalEntryDetailUseCase` + impl
5. Create `JournalEntryQueryPortImpl` with JPA Specification

### Phase 2: Web Layer (Controller & Mapping)
6. Create `JournalEntryController`
7. Create `JournalEntryWebMapper`
8. Create response DTOs
9. Wire use cases in `JournalConfig`

### Phase 3: UI & Menu
10. Create `journal-entry-list.html` template
11. Create `journal-entry-detail.html` template
12. Add page-specific JavaScript (filter, sorting, pagination)
13. Update V55 migration: add permission_groups entry
14. Add menu link in sidebar (auto via permission_groups)

### Phase 4: Integration
15. Add link from GR detail page to posted journal (if exists)
16. Test list + detail + filters
17. Test permission/access control

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Query Port Pattern** | Separates read logic from write logic; reusable for reports/dashboards |
| **JPA Specification** | Dynamic filtering without N+1 queries; supports complex filters |
| **Read-Only** | No edit/delete for now; immutable journal entries |
| **Pagination** | Standard Spring Data pagination for large datasets |
| **Thymeleaf + AJAX** | Consistent with existing GR pattern; filter without page reload |
| **Link from GR** | Audit trail: user can trace GR → journal posting |

---

## Data Model (Reference)

**JournalEntry** (already exists):
- id, eventType, sourceType, sourceId, sourceCode, journalDate, description, status, createdBy, createdDate

**JournalLine** (already exists):
- id, journalEntryId, accountId, debit, credit

**Filter Criteria**:
- sourceType: GOODS_RECEIPT (extensible for future)
- sourceCode: GR-0001, GR-0002, etc
- journalCode: auto-generated (future: if needed)
- postingDateFrom/To: date range
- pagination: page, size, sort

---

## Testing Strategy

- **Unit Tests**: Query port impl (filter logic, pagination)
- **Integration Tests**: Use cases with mock repository
- **Web Tests**: Controller endpoints (list API, detail page)
- **E2E Tests**: List → filter → detail → back (Playwright)

---

## Future Extensions

- Export journal to CSV/PDF
- Journal approval workflow (if needed)
- Manual journal entry creation (currently auto-only)
- Journal posting rules (policy engine for other event types)
- Reconciliation view (match journal to GL)

---

## Files to Create/Modify

### New Files:
```
src/main/java/com/solusi/erp/accounting/journal/domain/port/
  └─ JournalEntryQueryPort.java

src/main/java/com/solusi/erp/accounting/journal/domain/model/
  └─ JournalEntryFilter.java (record)

src/main/java/com/solusi/erp/accounting/journal/application/usecase/query/
  ├─ FindJournalEntriesUseCase.java
  ├─ FindJournalEntriesUseCaseImpl.java
  ├─ GetJournalEntryDetailUseCase.java
  └─ GetJournalEntryDetailUseCaseImpl.java

src/main/java/com/solusi/erp/accounting/journal/infrastructure/adapter/
  └─ JournalEntryQueryPortImpl.java

src/main/java/com/solusi/erp/accounting/journal/web/controller/
  └─ JournalEntryController.java

src/main/java/com/solusi/erp/accounting/journal/web/mapper/
  └─ JournalEntryWebMapper.java

src/main/java/com/solusi/erp/accounting/journal/web/dto/
  ├─ JournalEntryListResponse.java
  ├─ JournalEntryDetailResponse.java
  └─ JournalLineResponse.java

src/main/resources/templates/accounting/journal/
  ├─ journal-entry-list.html
  └─ journal-entry-detail.html

src/main/resources/static/js/accounting/journal/
  └─ journal-entry-list.js (filter, sorting)

src/main/resources/db/migration/
  └─ V55__Add_Journal_Core.sql (UPDATE: add permission_groups entry)
```

### Modified Files:
```
src/main/java/com/solusi/erp/accounting/journal/infrastructure/config/
  └─ JournalConfig.java (wire new use cases)

src/main/resources/templates/inventory/goods-receipt/
  └─ goods-receipt-detail.html (add link to journal if posted)
```

---

## Next Steps

1. **Confirm this plan** with user
2. **Start implementation** with Phase 1 (domain + application)
3. **Code review checkpoint** after Phase 2
4. **UI testing** in Phase 3
5. **Integration testing** in Phase 4

---

## Notes

- V55 migration already has journal tables; just add permission_groups entry
- Reuse existing patterns: GR list/detail for UI structure
- Query port can be extended for future event types (SALES_RETURN, PRODUCTION, etc)
- No approval workflow for now (immutable entries)
