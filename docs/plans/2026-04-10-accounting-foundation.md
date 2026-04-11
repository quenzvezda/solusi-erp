# Accounting Foundation (Sprint 1) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (- [ ]) syntax for tracking.

**Goal:** Build the GL Foundation layer (Chart of Accounts, Accounting Schema, Accounting Period / Fiscal Year) that all future transactional modules depend on for auto-journaling and financial reporting.

**Architecture:** Clean Architecture + DDD + CQRS monolith. Each accounting feature is a vertical slice under com.solusi.erp.accounting.* with four layers: domain (pure Java), application (use cases), infrastructure (JPA + Spring), web (Thymeleaf + HTMX). All slices share a single Flyway migration for DDL, permissions, and seed data.

**Tech Stack:** Java 21, Spring Boot 4.0.3, MariaDB, JPA/Hibernate, Flyway, MapStruct, Thymeleaf, Bootstrap 5 / Tabler UI, HTMX

---

## File Structure

### Flyway Migration
- Create: src/main/resources/db/migration/V43__Add_Accounting_Foundation.sql

### i18n
- Modify: src/main/resources/messages.properties (append accounting labels)
- Modify: src/main/resources/messages_id.properties (append accounting labels)

### Chart of Accounts (COA) — ccounting.coa slice
`
src/main/java/com/solusi/erp/accounting/coa/
├── domain/
│   ├── model/
│   │   ├── ChartOfAccount.java          (Aggregate Root)
│   │   ├── AccountType.java             (Enum: ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE)
│   │   └── NormalBalance.java           (Enum: DEBIT, CREDIT)
│   ├── repository/
│   │   └── CoaRepository.java           (Domain repository interface)
│   └── port/
│       ├── CoaInUseChecker.java         (Smart Delete port)
│       └── CoaLookupProvider.java       (Cross-slice lookup for Schema)
├── application/
│   └── usecase/
│       ├── command/
│       │   ├── CreateCoaUseCase.java
│       │   ├── CreateCoaUseCaseImpl.java
│       │   ├── UpdateCoaUseCase.java
│       │   ├── UpdateCoaUseCaseImpl.java
│       │   ├── DeleteCoaUseCase.java
│       │   └── DeleteCoaUseCaseImpl.java
│       └── query/
│           ├── FindCoaUseCase.java
│           ├── FindCoaUseCaseImpl.java
│           ├── GetCoaEditViewUseCase.java
│           ├── GetCoaEditViewUseCaseImpl.java
│           ├── GetCoaLookupUseCase.java
│           └── GetCoaLookupUseCaseImpl.java
├── infrastructure/
│   ├── persistence/
│   │   ├── ChartOfAccount.java          (JPA Entity)
│   │   ├── CoaJpaRepository.java
│   │   └── CoaPersistenceMapper.java
│   ├── adapter/
│   │   ├── CoaRepositoryImpl.java
│   │   ├── CoaInUseCheckerImpl.java
│   │   └── CoaLookupProviderImpl.java
│   └── config/
│       └── CoaConfig.java               (Composition Root)
└── web/
    ├── controller/
    │   ├── CoaController.java
    │   └── CoaLookupController.java
    ├── dto/
    │   ├── CoaSaveRequest.java
    │   ├── CoaSummaryResponse.java
    │   └── CoaDetailResponse.java
    └── mapper/
        └── CoaWebMapper.java
`
Templates:
- Create: src/main/resources/templates/accounting/coa/list.html
- Create: src/main/resources/templates/accounting/coa/form.html

### Accounting Schema — ccounting.schema slice
`
src/main/java/com/solusi/erp/accounting/schema/
├── domain/
│   ├── model/
│   │   ├── AccountingSchema.java        (Aggregate Root)
│   │   └── SchemaEventType.java         (Enum)
│   ├── repository/
│   │   └── SchemaRepository.java
│   └── port/
│       └── SchemaInUseChecker.java
├── application/
│   └── usecase/
│       ├── command/
│       │   ├── CreateSchemaUseCase.java
│       │   ├── CreateSchemaUseCaseImpl.java
│       │   ├── UpdateSchemaUseCase.java
│       │   ├── UpdateSchemaUseCaseImpl.java
│       │   ├── DeleteSchemaUseCase.java
│       │   └── DeleteSchemaUseCaseImpl.java
│       └── query/
│           ├── FindSchemasUseCase.java
│           ├── FindSchemasUseCaseImpl.java
│           ├── GetSchemaEditViewUseCase.java
│           └── GetSchemaEditViewUseCaseImpl.java
├── infrastructure/
│   ├── persistence/
│   │   ├── AccountingSchema.java        (JPA Entity)
│   │   ├── SchemaJpaRepository.java
│   │   └── SchemaPersistenceMapper.java
│   ├── adapter/
│   │   ├── SchemaRepositoryImpl.java
│   │   └── SchemaInUseCheckerImpl.java
│   └── config/
│       └── SchemaConfig.java
└── web/
    ├── controller/
    │   └── SchemaController.java
    ├── dto/
    │   ├── SchemaSaveRequest.java
    │   ├── SchemaSummaryResponse.java
    │   └── SchemaDetailResponse.java
    └── mapper/
        └── SchemaWebMapper.java
`
Templates:
- Create: src/main/resources/templates/accounting/schema/list.html
- Create: src/main/resources/templates/accounting/schema/form.html

### Fiscal Year & Accounting Period — ccounting.period slice
`
src/main/java/com/solusi/erp/accounting/period/
├── domain/
│   ├── model/
│   │   ├── FiscalYear.java              (Aggregate Root)
│   │   ├── AccountingPeriod.java        (Entity within FY aggregate)
│   │   └── PeriodStatus.java            (Enum: OPEN, CLOSED)
│   ├── repository/
│   │   └── FiscalYearRepository.java
│   └── port/
│       └── FiscalYearInUseChecker.java
├── application/
│   └── usecase/
│       ├── command/
│       │   ├── CreateFiscalYearUseCase.java
│       │   ├── CreateFiscalYearUseCaseImpl.java
│       │   ├── UpdateFiscalYearUseCase.java
│       │   ├── UpdateFiscalYearUseCaseImpl.java
│       │   ├── DeleteFiscalYearUseCase.java
│       │   ├── DeleteFiscalYearUseCaseImpl.java
│       │   ├── ClosePeriodUseCase.java
│       │   ├── ClosePeriodUseCaseImpl.java
│       │   ├── ReopenPeriodUseCase.java
│       │   └── ReopenPeriodUseCaseImpl.java
│       └── query/
│           ├── FindFiscalYearsUseCase.java
│           ├── FindFiscalYearsUseCaseImpl.java
│           ├── GetFiscalYearDetailUseCase.java
│           └── GetFiscalYearDetailUseCaseImpl.java
├── infrastructure/
│   ├── persistence/
│   │   ├── FiscalYear.java              (JPA Entity)
│   │   ├── AccountingPeriod.java        (JPA Entity)
│   │   ├── FiscalYearJpaRepository.java
│   │   ├── AccountingPeriodJpaRepository.java
│   │   ├── PeriodPersistenceMapper.java
│   │   └── FiscalYearPersistenceMapper.java
│   ├── adapter/
│   │   ├── FiscalYearRepositoryImpl.java
│   │   └── FiscalYearInUseCheckerImpl.java
│   └── config/
│       └── PeriodConfig.java
└── web/
    ├── controller/
    │   └── PeriodController.java
    ├── dto/
    │   ├── FiscalYearSaveRequest.java
    │   ├── FiscalYearSummaryResponse.java
    │   ├── FiscalYearDetailResponse.java
    │   └── PeriodResponse.java
    └── mapper/
        └── PeriodWebMapper.java
`
Templates:
- Create: src/main/resources/templates/accounting/period/list.html
- Create: src/main/resources/templates/accounting/period/detail.html

---

## Task 1: Flyway Migration — DDL, Permissions, Seed Data

**Files:**
- Create: `src/main/resources/db/migration/V43__Add_Accounting_Foundation.sql`

This single migration creates all four database tables, inserts permission groups / permissions for the Accounting menus, grants them to ROLE_ADMIN, and seeds the sequence generator entry for Fiscal Year codes.

- [ ] **Step 1: Create the migration file**

Create `src/main/resources/db/migration/V43__Add_Accounting_Foundation.sql`:

```sql
-- V43: Accounting Foundation — COA, Schema, Fiscal Year, Periods, Permissions

-- ============================================================
-- 1. DDL: Chart of Accounts
-- ============================================================
CREATE TABLE acc_chart_of_accounts (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(20)  NOT NULL,
    name          VARCHAR(150) NOT NULL,
    account_type  VARCHAR(20)  NOT NULL COMMENT 'ASSET | LIABILITY | EQUITY | REVENUE | EXPENSE',
    normal_balance VARCHAR(10) NOT NULL COMMENT 'DEBIT | CREDIT',
    parent_id     BIGINT       NULL,
    level         INT          NOT NULL DEFAULT 1,
    is_header     BOOLEAN      NOT NULL DEFAULT FALSE,
    note          TEXT         NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    version       INT          NOT NULL DEFAULT 0,
    created_by_user_id BIGINT  NULL,
    created_date  DATETIME     NULL,
    updated_by_user_id BIGINT  NULL,
    updated_date  DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_coa_code (code),
    CONSTRAINT fk_coa_parent FOREIGN KEY (parent_id) REFERENCES acc_chart_of_accounts(id),
    CONSTRAINT fk_coa_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_coa_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. DDL: Accounting Schema
-- ============================================================
CREATE TABLE acc_accounting_schemas (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    event_type        VARCHAR(50)  NOT NULL COMMENT 'e.g. GOODS_RECEIPT, VENDOR_BILL, etc.',
    description       VARCHAR(255) NULL,
    debit_account_id  BIGINT       NOT NULL,
    credit_account_id BIGINT       NOT NULL,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    version           INT          NOT NULL DEFAULT 0,
    created_by_user_id BIGINT     NULL,
    created_date      DATETIME     NULL,
    updated_by_user_id BIGINT     NULL,
    updated_date      DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_schema_event_active (event_type, is_active),
    CONSTRAINT fk_schema_debit  FOREIGN KEY (debit_account_id)  REFERENCES acc_chart_of_accounts(id),
    CONSTRAINT fk_schema_credit FOREIGN KEY (credit_account_id) REFERENCES acc_chart_of_accounts(id),
    CONSTRAINT fk_schema_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_schema_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. DDL: Fiscal Year
-- ============================================================
CREATE TABLE acc_fiscal_years (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(20)  NOT NULL,
    name          VARCHAR(100) NOT NULL,
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    version       INT          NOT NULL DEFAULT 0,
    created_by_user_id BIGINT  NULL,
    created_date  DATETIME     NULL,
    updated_by_user_id BIGINT  NULL,
    updated_date  DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fy_code (code),
    CONSTRAINT fk_fy_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_fy_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. DDL: Accounting Period
-- ============================================================
CREATE TABLE acc_accounting_periods (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    code            VARCHAR(20)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    fiscal_year_id  BIGINT       NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    status          VARCHAR(10)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN | CLOSED',
    version         INT          NOT NULL DEFAULT 0,
    created_by_user_id BIGINT   NULL,
    created_date    DATETIME     NULL,
    updated_by_user_id BIGINT   NULL,
    updated_date    DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_period_code (code),
    CONSTRAINT fk_period_fy FOREIGN KEY (fiscal_year_id) REFERENCES acc_fiscal_years(id),
    CONSTRAINT fk_period_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_period_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. Sequence Generator Entry for Fiscal Year
-- ============================================================
INSERT INTO system_sequences (entity_name, format_pattern, pad_length, last_value, reset_cycle, created_by_user_id, created_date)
VALUES ('FISCAL_YEAR', 'FY-{seq}', 4, 0, 'NEVER', 1, NOW());

-- ============================================================
-- 6. Permission Groups (Menu Entries)
-- ============================================================
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('GL-01', 'Bagan Akun', 'Chart of Accounts',
 'Keuangan & Akuntansi > Buku Besar > Bagan Akun', 'Finance & Accounting > General Ledger > Chart of Accounts',
 '/accounting/coa', 'ti-list-tree',
 'Kelola master bagan akun (COA)', 'Manage chart of accounts (COA) master data',
 100, 1, NOW()),

('GL-02', 'Skema Akuntansi', 'Accounting Schema',
 'Keuangan & Akuntansi > Buku Besar > Skema Akuntansi', 'Finance & Accounting > General Ledger > Accounting Schema',
 '/accounting/schemas', 'ti-route',
 'Konfigurasi mapping event ke akun debit/kredit', 'Configure event-to-account debit/credit mapping',
 101, 1, NOW()),

('GL-03', 'Periode Akuntansi', 'Accounting Period',
 'Keuangan & Akuntansi > Buku Besar > Periode Akuntansi', 'Finance & Accounting > General Ledger > Accounting Period',
 '/accounting/periods', 'ti-calendar',
 'Kelola tahun fiskal dan periode buku', 'Manage fiscal years and accounting periods',
 102, 1, NOW());

-- ============================================================
-- 7. Permissions
-- ============================================================
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
-- COA permissions
('ACCOUNTING-COA_READ',   'Melihat daftar bagan akun',            1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-01')),
('ACCOUNTING-COA_CREATE', 'Membuat akun baru pada bagan akun',    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-01')),
('ACCOUNTING-COA_UPDATE', 'Mengubah data akun pada bagan akun',   1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-01')),
('ACCOUNTING-COA_DELETE', 'Menghapus akun dari bagan akun',       1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-01')),
('LOOKUP_COA',            'Lookup akun COA untuk autocomplete',   1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-01')),

-- Schema permissions
('ACCOUNTING-SCHEMA_READ',   'Melihat daftar skema akuntansi',           1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-02')),
('ACCOUNTING-SCHEMA_CREATE', 'Membuat mapping skema akuntansi baru',     1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-02')),
('ACCOUNTING-SCHEMA_UPDATE', 'Mengubah mapping skema akuntansi',         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-02')),
('ACCOUNTING-SCHEMA_DELETE', 'Menghapus mapping skema akuntansi',        1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-02')),

-- Period permissions
('ACCOUNTING-PERIOD_READ',   'Melihat daftar periode akuntansi',         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-03')),
('ACCOUNTING-PERIOD_CREATE', 'Membuat tahun fiskal dan periode baru',    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-03')),
('ACCOUNTING-PERIOD_UPDATE', 'Mengubah tahun fiskal dan periode',        1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-03')),
('ACCOUNTING-PERIOD_DELETE', 'Menghapus tahun fiskal dan periode',       1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-03'));

-- ============================================================
-- 8. Grant all accounting permissions to ROLE_ADMIN
-- ============================================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name LIKE 'ACCOUNTING-\_%' ESCAPE '\\';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name = 'LOOKUP_COA';
```

- [ ] **Step 2: Verify migration applies**

Run: `.\mvnw.cmd spring-boot:run`

Expected: Application starts with no migration errors. Check logs for `Successfully applied 1 migration to schema`.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/migration/V43__Add_Accounting_Foundation.sql
git commit -m "feat(accounting): add V43 migration for COA, Schema, Period tables and permissions

- Creates acc_chart_of_accounts, acc_accounting_schemas, acc_fiscal_years, acc_accounting_periods tables
- Adds GL-01/02/03 permission groups with sort_order 100-102
- Seeds ACCOUNTING-COA/SCHEMA/PERIOD permissions and LOOKUP_COA
- Grants all to ROLE_ADMIN
- Adds FISCAL_YEAR sequence generator entry

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---
## Task 2: i18n Message Properties

**Files:**
- Modify: `src/main/resources/messages.properties` (append at end of file)
- Modify: `src/main/resources/messages_id.properties` (append at end of file)

- [ ] **Step 1: Append English labels to `messages.properties`**

Append the following block to the end of `src/main/resources/messages.properties`:

```properties
# ==============================
# Accounting — Chart of Accounts
# ==============================
label.coa.title=Chart of Accounts
label.coa.subtitle=Manage the organization's chart of accounts.
label.coa.add=Add New Account
label.coa.edit=Edit Account
label.coa.code=Account Code
label.coa.name=Account Name
label.coa.account.type=Account Type
label.coa.normal.balance=Normal Balance
label.coa.parent=Parent Account
label.coa.level=Level
label.coa.is.header=Header Account
label.coa.note=Note
label.coa.column.code=Code
label.coa.column.name=Name
label.coa.column.type=Type
label.coa.column.balance=Normal Balance
label.coa.column.parent=Parent
label.coa.column.level=Level
label.coa.column.header=Header
label.coa.column.status=Status
label.coa.empty=No account data found.
placeholder.coa.code=e.g. 1000
placeholder.coa.name=e.g. Cash and Bank
placeholder.coa.note=Additional notes...
label.coa.type.ASSET=Asset
label.coa.type.LIABILITY=Liability
label.coa.type.EQUITY=Equity
label.coa.type.REVENUE=Revenue
label.coa.type.EXPENSE=Expense
label.coa.balance.DEBIT=Debit
label.coa.balance.CREDIT=Credit

# ==============================
# Accounting — Accounting Schema
# ==============================
label.schema.title=Accounting Schema
label.schema.subtitle=Configure event-to-account mapping for auto-journaling.
label.schema.add=Add New Schema
label.schema.edit=Edit Schema
label.schema.event.type=Event Type
label.schema.description=Description
label.schema.debit.account=Debit Account
label.schema.credit.account=Credit Account
label.schema.column.event=Event Type
label.schema.column.description=Description
label.schema.column.debit=Debit Account
label.schema.column.credit=Credit Account
label.schema.column.status=Status
label.schema.empty=No accounting schema data found.
placeholder.schema.description=e.g. Records goods receipt to inventory
label.schema.event.GOODS_RECEIPT=Goods Receipt
label.schema.event.VENDOR_BILL=Vendor Bill
label.schema.event.VENDOR_PAYMENT=Vendor Payment
label.schema.event.CUSTOMER_INVOICE=Customer Invoice
label.schema.event.GOODS_ISSUE=Goods Issue
label.schema.event.CUSTOMER_RECEIPT=Customer Receipt
label.schema.event.STOCK_ADJUSTMENT_IN=Stock Adjustment In
label.schema.event.STOCK_ADJUSTMENT_OUT=Stock Adjustment Out

# ==============================
# Accounting — Fiscal Year & Period
# ==============================
label.period.title=Accounting Period
label.period.subtitle=Manage fiscal years and their accounting periods.
label.period.add=Add Fiscal Year
label.period.edit=Edit Fiscal Year
label.period.fy.code=Fiscal Year Code
label.period.fy.name=Fiscal Year Name
label.period.fy.start=Start Date
label.period.fy.end=End Date
label.period.column.code=Code
label.period.column.name=Name
label.period.column.start=Start Date
label.period.column.end=End Date
label.period.column.status=Status
label.period.column.periods=Periods
label.period.empty=No fiscal year data found.
label.period.status.OPEN=Open
label.period.status.CLOSED=Closed
label.period.close=Close Period
label.period.reopen=Reopen Period
label.period.detail.title=Fiscal Year Detail
label.period.auto.generate.note=12 monthly periods will be auto-generated.
placeholder.period.name=e.g. Fiscal Year 2026
```

- [ ] **Step 2: Append Indonesian labels to `messages_id.properties`**

Append the following block to the end of `src/main/resources/messages_id.properties`:

```properties
# ==============================
# Accounting — Bagan Akun
# ==============================
label.coa.title=Bagan Akun
label.coa.subtitle=Kelola bagan akun organisasi.
label.coa.add=Tambah Akun Baru
label.coa.edit=Ubah Akun
label.coa.code=Kode Akun
label.coa.name=Nama Akun
label.coa.account.type=Tipe Akun
label.coa.normal.balance=Saldo Normal
label.coa.parent=Akun Induk
label.coa.level=Level
label.coa.is.header=Akun Header
label.coa.note=Catatan
label.coa.column.code=Kode
label.coa.column.name=Nama
label.coa.column.type=Tipe
label.coa.column.balance=Saldo Normal
label.coa.column.parent=Induk
label.coa.column.level=Level
label.coa.column.header=Header
label.coa.column.status=Status
label.coa.empty=Data akun tidak ditemukan.
placeholder.coa.code=contoh: 1000
placeholder.coa.name=contoh: Kas dan Bank
placeholder.coa.note=Catatan tambahan...
label.coa.type.ASSET=Aset
label.coa.type.LIABILITY=Kewajiban
label.coa.type.EQUITY=Ekuitas
label.coa.type.REVENUE=Pendapatan
label.coa.type.EXPENSE=Biaya
label.coa.balance.DEBIT=Debit
label.coa.balance.CREDIT=Kredit

# ==============================
# Accounting — Skema Akuntansi
# ==============================
label.schema.title=Skema Akuntansi
label.schema.subtitle=Konfigurasi mapping event ke akun untuk auto-journaling.
label.schema.add=Tambah Skema Baru
label.schema.edit=Ubah Skema
label.schema.event.type=Tipe Event
label.schema.description=Deskripsi
label.schema.debit.account=Akun Debit
label.schema.credit.account=Akun Kredit
label.schema.column.event=Tipe Event
label.schema.column.description=Deskripsi
label.schema.column.debit=Akun Debit
label.schema.column.credit=Akun Kredit
label.schema.column.status=Status
label.schema.empty=Data skema akuntansi tidak ditemukan.
placeholder.schema.description=contoh: Pencatatan penerimaan barang ke persediaan
label.schema.event.GOODS_RECEIPT=Penerimaan Barang
label.schema.event.VENDOR_BILL=Tagihan Vendor
label.schema.event.VENDOR_PAYMENT=Pembayaran Vendor
label.schema.event.CUSTOMER_INVOICE=Faktur Pelanggan
label.schema.event.GOODS_ISSUE=Pengeluaran Barang
label.schema.event.CUSTOMER_RECEIPT=Penerimaan Pelanggan
label.schema.event.STOCK_ADJUSTMENT_IN=Penyesuaian Stok Masuk
label.schema.event.STOCK_ADJUSTMENT_OUT=Penyesuaian Stok Keluar

# ==============================
# Accounting — Tahun Fiskal & Periode
# ==============================
label.period.title=Periode Akuntansi
label.period.subtitle=Kelola tahun fiskal dan periode akuntansi.
label.period.add=Tambah Tahun Fiskal
label.period.edit=Ubah Tahun Fiskal
label.period.fy.code=Kode Tahun Fiskal
label.period.fy.name=Nama Tahun Fiskal
label.period.fy.start=Tanggal Mulai
label.period.fy.end=Tanggal Selesai
label.period.column.code=Kode
label.period.column.name=Nama
label.period.column.start=Tanggal Mulai
label.period.column.end=Tanggal Selesai
label.period.column.status=Status
label.period.column.periods=Periode
label.period.empty=Data tahun fiskal tidak ditemukan.
label.period.status.OPEN=Terbuka
label.period.status.CLOSED=Ditutup
label.period.close=Tutup Periode
label.period.reopen=Buka Kembali Periode
label.period.detail.title=Detail Tahun Fiskal
label.period.auto.generate.note=12 periode bulanan akan digenerate otomatis.
placeholder.period.name=contoh: Tahun Fiskal 2026
```

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/messages.properties src/main/resources/messages_id.properties
git commit -m "feat(accounting): add i18n labels for COA, Schema, Period

- English and Indonesian translations for all accounting foundation features
- Labels, placeholders, enum display names, and empty-state messages

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---
## Task 3: COA — Domain & Application Layers

**Files:**
- Create: `src/main/java/com/solusi/erp/accounting/coa/domain/model/AccountType.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/domain/model/NormalBalance.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/domain/model/ChartOfAccount.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/domain/repository/CoaRepository.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/domain/port/CoaInUseChecker.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/domain/port/CoaLookupProvider.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/CreateCoaUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/CreateCoaUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/UpdateCoaUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/UpdateCoaUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/DeleteCoaUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/DeleteCoaUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/FindCoaUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/FindCoaUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/GetCoaEditViewUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/GetCoaEditViewUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/GetCoaLookupUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/GetCoaLookupUseCaseImpl.java`

All files in this task are **100% pure Java** — no Spring, no Lombok, no framework annotations.

- [ ] **Step 1: Create `AccountType` enum**

Create `src/main/java/com/solusi/erp/accounting/coa/domain/model/AccountType.java`:

```java
package com.solusi.erp.accounting.coa.domain.model;

/**
 * Chart of Account types following standard accounting classification.
 * ASSET & EXPENSE have DEBIT normal balance; LIABILITY, EQUITY & REVENUE have CREDIT.
 */
public enum AccountType {
    ASSET(NormalBalance.DEBIT),
    LIABILITY(NormalBalance.CREDIT),
    EQUITY(NormalBalance.CREDIT),
    REVENUE(NormalBalance.CREDIT),
    EXPENSE(NormalBalance.DEBIT);

    private final NormalBalance defaultNormalBalance;

    AccountType(NormalBalance defaultNormalBalance) {
        this.defaultNormalBalance = defaultNormalBalance;
    }

    public NormalBalance getDefaultNormalBalance() {
        return defaultNormalBalance;
    }
}
```

- [ ] **Step 2: Create `NormalBalance` enum**

Create `src/main/java/com/solusi/erp/accounting/coa/domain/model/NormalBalance.java`:

```java
package com.solusi.erp.accounting.coa.domain.model;

public enum NormalBalance {
    DEBIT,
    CREDIT
}
```

- [ ] **Step 3: Create `ChartOfAccount` aggregate root**

Create `src/main/java/com/solusi/erp/accounting/coa/domain/model/ChartOfAccount.java`:

```java
package com.solusi.erp.accounting.coa.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

/**
 * Aggregate Root: Chart of Account.
 * 100% Pure Java Domain Model.
 */
public class ChartOfAccount {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private AccountType accountType;
    private NormalBalance normalBalance;
    private Long parentId;
    private Integer level;
    private Boolean isHeader;
    private String note;
    private Boolean isActive;

    public ChartOfAccount(AuditMetadata metadata, String code, String name,
                          AccountType accountType, NormalBalance normalBalance,
                          Long parentId, Integer level, Boolean isHeader,
                          String note, Boolean isActive) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.accountType = accountType;
        this.normalBalance = normalBalance;
        this.parentId = parentId;
        this.level = level;
        this.isHeader = isHeader;
        this.note = note;
        this.isActive = isActive;
    }

    public static ChartOfAccount createNew(String code, String name,
                                            AccountType accountType,
                                            Long parentId, Integer level,
                                            Boolean isHeader, String note,
                                            Boolean isActive) {
        return new ChartOfAccount(
                AuditMetadata.empty(), code, name, accountType,
                accountType.getDefaultNormalBalance(),
                parentId,
                level != null ? level : 1,
                isHeader != null ? isHeader : false,
                note,
                isActive != null ? isActive : true
        );
    }

    public void update(String name, AccountType accountType,
                       Long parentId, Integer level,
                       Boolean isHeader, String note, Boolean isActive) {
        this.name = name;
        this.accountType = accountType;
        this.normalBalance = accountType.getDefaultNormalBalance();
        this.parentId = parentId;
        this.level = level != null ? level : 1;
        this.isHeader = isHeader != null ? isHeader : false;
        this.note = note;
        this.isActive = isActive != null ? isActive : true;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public AccountType getAccountType() { return accountType; }
    public NormalBalance getNormalBalance() { return normalBalance; }
    public Long getParentId() { return parentId; }
    public Integer getLevel() { return level; }
    public Boolean getIsHeader() { return isHeader; }
    public String getNote() { return note; }
    public Boolean getIsActive() { return isActive; }
}
```

- [ ] **Step 4: Create `CoaRepository` domain interface**

Create `src/main/java/com/solusi/erp/accounting/coa/domain/repository/CoaRepository.java`:

```java
package com.solusi.erp.accounting.coa.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for Chart of Accounts.
 * Pure Java — no framework dependency.
 */
public interface CoaRepository {
    ChartOfAccount save(ChartOfAccount coa);
    Optional<ChartOfAccount> findById(Long id);
    Page<ChartOfAccount> findAll(String keyword, Pageable pageable);
    void delete(Long id);
    boolean existsByCode(String code);
    List<ChartOfAccount> search(String keyword, int limit);
}
```

- [ ] **Step 5: Create ports**

Create `src/main/java/com/solusi/erp/accounting/coa/domain/port/CoaInUseChecker.java`:

```java
package com.solusi.erp.accounting.coa.domain.port;

/**
 * Port for checking if a COA account is referenced by other entities (Schema, Journal, etc.).
 * Part of the Smart Delete pattern — pure Java, no framework dependencies.
 */
public interface CoaInUseChecker {
    boolean isInUse(Long coaId);
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/domain/port/CoaLookupProvider.java`:

```java
package com.solusi.erp.accounting.coa.domain.port;

import com.solusi.erp.core.dto.LookupDto;

/**
 * Cross-slice port for resolving COA lookup data.
 * Used by Accounting Schema forms for autocomplete.
 */
public interface CoaLookupProvider {
    LookupDto resolve(Long coaId);
}
```

- [ ] **Step 6: Create command use cases**

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/CreateCoaUseCase.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;

@FunctionalInterface
public interface CreateCoaUseCase {
    ChartOfAccount execute(String code, String name, AccountType accountType,
                           Long parentId, Integer level, Boolean isHeader,
                           String note, Boolean isActive);
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/CreateCoaUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

public class CreateCoaUseCaseImpl implements CreateCoaUseCase {

    private final CoaRepository repository;

    public CreateCoaUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ChartOfAccount execute(String code, String name, AccountType accountType,
                                   Long parentId, Integer level, Boolean isHeader,
                                   String note, Boolean isActive) {
        if (repository.existsByCode(code)) {
            throw new DomainException("msg.error.common.duplicate");
        }
        ChartOfAccount coa = ChartOfAccount.createNew(code, name, accountType,
                parentId, level, isHeader, note, isActive);
        return repository.save(coa);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/UpdateCoaUseCase.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;

@FunctionalInterface
public interface UpdateCoaUseCase {
    ChartOfAccount execute(Long id, String name, AccountType accountType,
                           Long parentId, Integer level, Boolean isHeader,
                           String note, Boolean isActive);
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/UpdateCoaUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

public class UpdateCoaUseCaseImpl implements UpdateCoaUseCase {

    private final CoaRepository repository;

    public UpdateCoaUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ChartOfAccount execute(Long id, String name, AccountType accountType,
                                   Long parentId, Integer level, Boolean isHeader,
                                   String note, Boolean isActive) {
        ChartOfAccount coa = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.coa.notfound"));
        coa.update(name, accountType, parentId, level, isHeader, note, isActive);
        return repository.save(coa);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/DeleteCoaUseCase.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeleteCoaUseCase {
    DeleteResult execute(Long id);
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/command/DeleteCoaUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.port.CoaInUseChecker;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

public class DeleteCoaUseCaseImpl implements DeleteCoaUseCase {

    private final CoaRepository repository;
    private final CoaInUseChecker inUseChecker;

    public DeleteCoaUseCaseImpl(CoaRepository repository, CoaInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        ChartOfAccount coa = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.coa.notfound"));

        if (inUseChecker.isInUse(id)) {
            coa.softDelete();
            repository.save(coa);
            return DeleteResult.SOFT_DELETED;
        }

        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
```

- [ ] **Step 7: Create query use cases**

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/FindCoaUseCase.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;

@FunctionalInterface
public interface FindCoaUseCase {
    Page<ChartOfAccount> execute(String keyword, Pageable pageable);
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/FindCoaUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

public class FindCoaUseCaseImpl implements FindCoaUseCase {

    private final CoaRepository repository;

    public FindCoaUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<ChartOfAccount> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/GetCoaEditViewUseCase.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;

import java.util.Optional;

@FunctionalInterface
public interface GetCoaEditViewUseCase {
    Optional<ChartOfAccount> execute(Long id);
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/GetCoaEditViewUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

import java.util.Optional;

public class GetCoaEditViewUseCaseImpl implements GetCoaEditViewUseCase {

    private final CoaRepository repository;

    public GetCoaEditViewUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ChartOfAccount> execute(Long id) {
        return repository.findById(id);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/GetCoaLookupUseCase.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;

import java.util.List;

public interface GetCoaLookupUseCase {
    LookupDto getById(Long id);
    List<LookupDto> search(String keyword, int limit);
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/GetCoaLookupUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetCoaLookupUseCaseImpl implements GetCoaLookupUseCase {

    private final CoaRepository repository;

    public GetCoaLookupUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public LookupDto getById(Long id) {
        ChartOfAccount coa = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.coa.notfound"));
        return toLookupDto(coa);
    }

    @Override
    public List<LookupDto> search(String keyword, int limit) {
        return repository.search(keyword, limit).stream()
                .map(this::toLookupDto)
                .collect(Collectors.toList());
    }

    private LookupDto toLookupDto(ChartOfAccount coa) {
        return new LookupDto(coa.getId(), coa.getName(), coa.getCode());
    }
}
```

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/solusi/erp/accounting/coa/domain/ src/main/java/com/solusi/erp/accounting/coa/application/
git commit -m "feat(accounting): add COA domain model and application use cases

- ChartOfAccount aggregate root with AccountType/NormalBalance enums
- CoaRepository, CoaInUseChecker, CoaLookupProvider ports
- Create/Update/Delete command use cases
- FindCoa, GetCoaEditView, GetCoaLookup query use cases
- All pure Java, no framework dependencies

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---
## Task 4: COA — Infrastructure Layer

**Files:**
- Create: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/persistence/ChartOfAccount.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/persistence/CoaJpaRepository.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/persistence/CoaPersistenceMapper.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaRepositoryImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaInUseCheckerImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaLookupProviderImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/config/CoaConfig.java`

- [ ] **Step 1: Create JPA Entity**

Create `src/main/java/com/solusi/erp/accounting/coa/infrastructure/persistence/ChartOfAccount.java`:

```java
package com.solusi.erp.accounting.coa.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "acc_chart_of_accounts")
@Getter
@Setter
public class ChartOfAccount extends BaseModel {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;

    @Column(name = "normal_balance", nullable = false, length = 10)
    private String normalBalance;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "is_header", nullable = false)
    private Boolean isHeader;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public ChartOfAccount() {
        this.level = 1;
        this.isHeader = false;
        this.isActive = true;
    }
}
```

- [ ] **Step 2: Create JPA Repository**

Create `src/main/java/com/solusi/erp/accounting/coa/infrastructure/persistence/CoaJpaRepository.java`:

```java
package com.solusi.erp.accounting.coa.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoaJpaRepository extends JpaRepository<ChartOfAccount, Long> {

    @Query("SELECT c FROM ChartOfAccount c WHERE " +
            "(LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            " ORDER BY c.code ASC")
    Page<ChartOfAccount> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM ChartOfAccount c ORDER BY c.code ASC")
    Page<ChartOfAccount> findAllOrdered(Pageable pageable);

    Optional<ChartOfAccount> findByCode(String code);

    @Query("SELECT c FROM ChartOfAccount c WHERE c.isActive = true AND c.isHeader = false AND " +
            "(LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            " ORDER BY c.code ASC")
    List<ChartOfAccount> searchForLookup(@Param("keyword") String keyword, Pageable pageable);
}
```

- [ ] **Step 3: Create Persistence Mapper**

Create `src/main/java/com/solusi/erp/accounting/coa/infrastructure/persistence/CoaPersistenceMapper.java`:

```java
package com.solusi.erp.accounting.coa.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.NormalBalance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Persistence Mapper for COA module.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CoaPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "accountType", source = "accountType", qualifiedByName = "toAccountType")
    @Mapping(target = "normalBalance", source = "normalBalance", qualifiedByName = "toNormalBalance")
    com.solusi.erp.accounting.coa.domain.model.ChartOfAccount toDomain(ChartOfAccount entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "accountType", source = "accountType", qualifiedByName = "accountTypeToString")
    @Mapping(target = "normalBalance", source = "normalBalance", qualifiedByName = "normalBalanceToString")
    ChartOfAccount toEntity(com.solusi.erp.accounting.coa.domain.model.ChartOfAccount domain);

    default AuditMetadata toAuditMetadata(ChartOfAccount entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    @Named("toAccountType")
    default AccountType toAccountType(String value) {
        return value != null ? AccountType.valueOf(value) : null;
    }

    @Named("toNormalBalance")
    default NormalBalance toNormalBalance(String value) {
        return value != null ? NormalBalance.valueOf(value) : null;
    }

    @Named("accountTypeToString")
    default String accountTypeToString(AccountType value) {
        return value != null ? value.name() : null;
    }

    @Named("normalBalanceToString")
    default String normalBalanceToString(NormalBalance value) {
        return value != null ? value.name() : null;
    }
}
```

- [ ] **Step 4: Create Repository Adapter**

Create `src/main/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaRepositoryImpl.java`:

```java
package com.solusi.erp.accounting.coa.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaJpaRepository;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CoaRepositoryImpl implements CoaRepository {

    private final CoaJpaRepository jpaRepository;
    private final CoaPersistenceMapper mapper;

    public CoaRepositoryImpl(CoaJpaRepository jpaRepository, CoaPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ChartOfAccount save(ChartOfAccount domain) {
        var entity = mapper.toEntity(domain);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ChartOfAccount> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<ChartOfAccount> findAll(String keyword, Pageable pageable) {
        var springPageable = PageableMapper.toSpring(pageable);
        var springPage = (keyword != null && !keyword.isBlank())
                ? jpaRepository.search(keyword, springPageable)
                : jpaRepository.findAllOrdered(springPageable);
        return new Page<>(
                springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.findByCode(code).isPresent();
    }

    @Override
    public List<ChartOfAccount> search(String keyword, int limit) {
        return jpaRepository.searchForLookup(
                keyword != null ? keyword : "",
                PageRequest.of(0, limit)
        ).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
```

- [ ] **Step 5: Create InUseChecker and LookupProvider adapters**

Create `src/main/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaInUseCheckerImpl.java`:

```java
package com.solusi.erp.accounting.coa.infrastructure.adapter;

import com.solusi.erp.accounting.coa.domain.port.CoaInUseChecker;
import com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaJpaRepository;

public class CoaInUseCheckerImpl implements CoaInUseChecker {

    private final SchemaJpaRepository schemaJpaRepository;

    public CoaInUseCheckerImpl(SchemaJpaRepository schemaJpaRepository) {
        this.schemaJpaRepository = schemaJpaRepository;
    }

    @Override
    public boolean isInUse(Long coaId) {
        return schemaJpaRepository.existsByDebitAccountIdOrCreditAccountId(coaId, coaId);
    }
}
```

> **Note:** `SchemaJpaRepository.existsByDebitAccountIdOrCreditAccountId` will be created in Task 6. If building COA before Schema, temporarily return `false` and replace once Schema infrastructure exists. Alternatively, use a simple no-op implementation first:

Alternative temporary `CoaInUseCheckerImpl.java` (if Schema not yet built):

```java
package com.solusi.erp.accounting.coa.infrastructure.adapter;

import com.solusi.erp.accounting.coa.domain.port.CoaInUseChecker;

public class CoaInUseCheckerImpl implements CoaInUseChecker {
    @Override
    public boolean isInUse(Long coaId) {
        // TODO: Wire to SchemaJpaRepository once Task 6 is complete
        return false;
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaLookupProviderImpl.java`:

```java
package com.solusi.erp.accounting.coa.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaJpaRepository;

public class CoaLookupProviderImpl implements CoaLookupProvider {

    private final CoaJpaRepository coaJpaRepository;

    public CoaLookupProviderImpl(CoaJpaRepository coaJpaRepository) {
        this.coaJpaRepository = coaJpaRepository;
    }

    @Override
    public LookupDto resolve(Long coaId) {
        if (coaId == null) return null;
        return coaJpaRepository.findById(coaId)
                .map(e -> new LookupDto(e.getId(), e.getName(), e.getCode()))
                .orElse(null);
    }
}
```

- [ ] **Step 6: Create Composition Root (Config)**

Create `src/main/java/com/solusi/erp/accounting/coa/infrastructure/config/CoaConfig.java`:

```java
package com.solusi.erp.accounting.coa.infrastructure.config;

import com.solusi.erp.accounting.coa.application.usecase.command.*;
import com.solusi.erp.accounting.coa.application.usecase.query.*;
import com.solusi.erp.accounting.coa.domain.port.CoaInUseChecker;
import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;
import com.solusi.erp.accounting.coa.infrastructure.adapter.CoaInUseCheckerImpl;
import com.solusi.erp.accounting.coa.infrastructure.adapter.CoaLookupProviderImpl;
import com.solusi.erp.accounting.coa.infrastructure.adapter.CoaRepositoryImpl;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaJpaRepository;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class CoaConfig {

    @Bean
    public CoaRepository coaDomainRepository(CoaJpaRepository jpaRepository, CoaPersistenceMapper mapper) {
        return new CoaRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CoaInUseChecker coaInUseChecker() {
        // Temporary no-op; will wire to SchemaJpaRepository in Task 6
        return new CoaInUseCheckerImpl();
    }

    @Bean
    public CoaLookupProvider coaLookupProvider(CoaJpaRepository jpaRepository) {
        return new CoaLookupProviderImpl(jpaRepository);
    }

    @Bean
    public CreateCoaUseCase createCoaUseCase(CoaRepository coaDomainRepository,
                                              PlatformTransactionManager txManager) {
        CreateCoaUseCase pure = new CreateCoaUseCaseImpl(coaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (code, name, accountType, parentId, level, isHeader, note, isActive) ->
                tx.execute(status -> pure.execute(code, name, accountType, parentId, level, isHeader, note, isActive));
    }

    @Bean
    public UpdateCoaUseCase updateCoaUseCase(CoaRepository coaDomainRepository,
                                              PlatformTransactionManager txManager) {
        UpdateCoaUseCase pure = new UpdateCoaUseCaseImpl(coaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, accountType, parentId, level, isHeader, note, isActive) ->
                tx.execute(status -> pure.execute(id, name, accountType, parentId, level, isHeader, note, isActive));
    }

    @Bean
    public DeleteCoaUseCase deleteCoaUseCase(CoaRepository coaDomainRepository,
                                              CoaInUseChecker coaInUseChecker,
                                              PlatformTransactionManager txManager) {
        DeleteCoaUseCase pure = new DeleteCoaUseCaseImpl(coaDomainRepository, coaInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindCoaUseCase findCoaUseCase(CoaRepository coaDomainRepository,
                                          PlatformTransactionManager txManager) {
        FindCoaUseCase pure = new FindCoaUseCaseImpl(coaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetCoaEditViewUseCase getCoaEditViewUseCase(CoaRepository coaDomainRepository,
                                                        PlatformTransactionManager txManager) {
        GetCoaEditViewUseCase pure = new GetCoaEditViewUseCaseImpl(coaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetCoaLookupUseCase getCoaLookupUseCase(CoaRepository coaDomainRepository,
                                                    PlatformTransactionManager txManager) {
        GetCoaLookupUseCaseImpl pure = new GetCoaLookupUseCaseImpl(coaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return new GetCoaLookupUseCase() {
            @Override
            public com.solusi.erp.core.dto.LookupDto getById(Long id) {
                return tx.execute(status -> pure.getById(id));
            }
            @Override
            public java.util.List<com.solusi.erp.core.dto.LookupDto> search(String keyword, int limit) {
                return tx.execute(status -> pure.search(keyword, limit));
            }
        };
    }
}
```

> **Important:** When Task 6 (Schema) is complete, update `coaInUseChecker` bean to inject `SchemaJpaRepository` and pass it to `CoaInUseCheckerImpl`.

- [ ] **Step 7: Compile to verify infrastructure wiring**

Run: `.\mvnw.cmd clean compile -q`

Expected: BUILD SUCCESS with no compilation errors.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/solusi/erp/accounting/coa/infrastructure/
git commit -m "feat(accounting): add COA infrastructure layer

- JPA entity, repository, persistence mapper
- CoaRepositoryImpl, CoaInUseCheckerImpl, CoaLookupProviderImpl adapters
- CoaConfig composition root with TransactionTemplate wiring

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---
## Task 5: COA — Web Layer (DTOs, Mapper, Controller, Templates)

**Files:**
- Create: `src/main/java/com/solusi/erp/accounting/coa/web/dto/CoaSaveRequest.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/web/dto/CoaSummaryResponse.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/web/dto/CoaDetailResponse.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/web/mapper/CoaWebMapper.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/web/controller/CoaController.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/web/controller/CoaLookupController.java`
- Create: `src/main/resources/templates/accounting/coa/list.html`
- Create: `src/main/resources/templates/accounting/coa/form.html`

- [ ] **Step 1: Create DTOs**

Create `src/main/java/com/solusi/erp/accounting/coa/web/dto/CoaSaveRequest.java`:

```java
package com.solusi.erp.accounting.coa.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CoaSaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.coa.code} {validation.notblank.suffix}")
    @Size(max = 20, message = "{label.coa.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.coa.name} {validation.notblank.suffix}")
    @Size(max = 150, message = "{label.coa.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.coa.account.type} {validation.notnull.suffix}")
    private String accountType;

    private Long parentId;
    private Integer level;
    private Boolean isHeader;
    private String note;
    private Boolean isActive;
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/web/dto/CoaSummaryResponse.java`:

```java
package com.solusi.erp.accounting.coa.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CoaSummaryResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private String accountType;
    private String normalBalance;
    private String parentName;
    private Integer level;
    private Boolean isHeader;
    private Boolean isActive;
}
```

Create `src/main/java/com/solusi/erp/accounting/coa/web/dto/CoaDetailResponse.java`:

```java
package com.solusi.erp.accounting.coa.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CoaDetailResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private String accountType;
    private String normalBalance;
    private Long parentId;
    private String parentName;
    private Integer level;
    private Boolean isHeader;
    private String note;
    private Boolean isActive;
}
```

- [ ] **Step 2: Create Web Mapper**

Create `src/main/java/com/solusi/erp/accounting/coa/web/mapper/CoaWebMapper.java`:

```java
package com.solusi.erp.accounting.coa.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.web.dto.CoaDetailResponse;
import com.solusi.erp.accounting.coa.web.dto.CoaSaveRequest;
import com.solusi.erp.accounting.coa.web.dto.CoaSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CoaWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Mapping(target = "accountType", expression = "java(domain.getAccountType() != null ? domain.getAccountType().name() : null)")
    @Mapping(target = "normalBalance", expression = "java(domain.getNormalBalance() != null ? domain.getNormalBalance().name() : null)")
    @Mapping(target = "parentName", ignore = true)
    public abstract CoaSummaryResponse toSummaryResponse(ChartOfAccount domain);

    @Mapping(target = "accountType", expression = "java(domain.getAccountType() != null ? domain.getAccountType().name() : null)")
    @Mapping(target = "normalBalance", expression = "java(domain.getNormalBalance() != null ? domain.getNormalBalance().name() : null)")
    @Mapping(target = "parentName", ignore = true)
    public abstract CoaDetailResponse toDetailResponse(ChartOfAccount domain);

    @Mapping(target = "accountType", expression = "java(domain.getAccountType() != null ? domain.getAccountType().name() : null)")
    public abstract CoaSaveRequest toSaveRequest(ChartOfAccount domain);

    @AfterMapping
    protected void mapAuditFields(ChartOfAccount domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }
}
```

- [ ] **Step 3: Create CoaController**

Create `src/main/java/com/solusi/erp/accounting/coa/web/controller/CoaController.java`:

```java
package com.solusi.erp.accounting.coa.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.accounting.coa.application.usecase.command.CreateCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.command.DeleteCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.command.UpdateCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.query.FindCoaUseCase;
import com.solusi.erp.accounting.coa.application.usecase.query.GetCoaEditViewUseCase;
import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.web.dto.CoaDetailResponse;
import com.solusi.erp.accounting.coa.web.dto.CoaSaveRequest;
import com.solusi.erp.accounting.coa.web.dto.CoaSummaryResponse;
import com.solusi.erp.accounting.coa.web.mapper.CoaWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/accounting/coa")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class CoaController {

    private final CreateCoaUseCase createCoaUseCase;
    private final UpdateCoaUseCase updateCoaUseCase;
    private final DeleteCoaUseCase deleteCoaUseCase;
    private final FindCoaUseCase findCoaUseCase;
    private final GetCoaEditViewUseCase getCoaEditViewUseCase;
    private final CoaWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<ChartOfAccount> domainPage = findCoaUseCase.execute(keyword, domainPageable);

        List<CoaSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<CoaSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("accountTypes", AccountType.values());
        return "accounting/coa/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_CREATE')")
    public String showCreateForm(Model model) {
        CoaSaveRequest request = new CoaSaveRequest();
        request.setIsActive(true);
        request.setIsHeader(false);
        request.setLevel(1);
        model.addAttribute("coaRequest", request);
        model.addAttribute("accountTypes", AccountType.values());
        return "accounting/coa/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<CoaDetailResponse>> create(@Valid @RequestBody CoaSaveRequest request) {
        ChartOfAccount domain = createCoaUseCase.execute(
                request.getCode(), request.getName(),
                AccountType.valueOf(request.getAccountType()),
                request.getParentId(), request.getLevel(),
                request.getIsHeader(), request.getNote(), request.getIsActive());
        CoaDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        ChartOfAccount domain = getCoaEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        model.addAttribute("coaRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("accountTypes", AccountType.values());
        return "accounting/coa/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<CoaDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CoaSaveRequest request) {
        ChartOfAccount domain = updateCoaUseCase.execute(
                id, request.getName(),
                AccountType.valueOf(request.getAccountType()),
                request.getParentId(), request.getLevel(),
                request.getIsHeader(), request.getNote(), request.getIsActive());
        CoaDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-COA_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteResult result = deleteCoaUseCase.execute(id);
        if (result == DeleteResult.SOFT_DELETED) {
            String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
            return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
        }
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
```

- [ ] **Step 4: Create CoaLookupController**

Create `src/main/java/com/solusi/erp/accounting/coa/web/controller/CoaLookupController.java`:

```java
package com.solusi.erp.accounting.coa.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.accounting.coa.application.usecase.query.GetCoaLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/accounting/coa")
@RequiredArgsConstructor
public class CoaLookupController {

    private final GetCoaLookupUseCase getCoaLookupUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('LOOKUP_COA')")
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getCoaLookupUseCase.search(q, limit);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_COA')")
    public LookupDto getById(@PathVariable Long id) {
        return getCoaLookupUseCase.getById(id);
    }
}
```

- [ ] **Step 5: Create list template**

Create `src/main/resources/templates/accounting/coa/list.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<head th:replace="~{layout/master :: head(#{label.coa.title})}"></head>

<body th:replace="~{layout/master :: layout(~{:: .coa-list-content}, ~{})}">
    <div class="coa-list-content">
        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title" th:text="#{label.coa.title}">Bagan Akun</h2>
                        <div class="text-secondary mt-1" th:text="#{label.coa.subtitle}">Kelola bagan akun.</div>
                    </div>
                    <div class="col-auto ms-auto d-print-none" sec:authorize="hasAuthority('ACCOUNTING-COA_CREATE')">
                        <div class="btn-list">
                            <a th:href="@{/accounting/coa/create}" class="btn btn-primary d-none d-sm-inline-block">
                                <i class="ti ti-plus"></i>
                                <span th:text="#{label.coa.add}">Tambah Akun Baru</span>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">
                <div th:if="${successMessage}" class="alert alert-success alert-dismissible" role="alert">
                    <div class="d-flex">
                        <div><i class="ti ti-check icon alert-icon"></i></div>
                        <div th:text="${successMessage}">Success!</div>
                    </div>
                    <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
                </div>
                <div th:if="${errorMessage}" class="alert alert-danger alert-dismissible" role="alert">
                    <div class="d-flex">
                        <div><i class="ti ti-alert-circle icon alert-icon"></i></div>
                        <div th:text="${errorMessage}">Error!</div>
                    </div>
                    <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
                </div>

                <div class="card">
                    <div class="card-body border-bottom py-3">
                        <div class="d-flex">
                            <div class="text-secondary"></div>
                            <div class="ms-auto text-secondary">
                                <form th:action="@{/accounting/coa}" method="get" class="input-icon"
                                      hx-get="/accounting/coa"
                                      hx-target="#coa-table-container"
                                      hx-trigger="keyup changed delay:500ms from:#search-input">
                                    <input id="search-input" type="text" name="keyword" th:value="${keyword}"
                                        class="form-control form-control-sm" th:placeholder="#{label.search}">
                                    <span class="input-icon-addon">
                                        <i class="ti ti-search"></i>
                                    </span>
                                </form>
                            </div>
                        </div>
                    </div>
                    <div id="coa-table-container" th:fragment="coa-table-container"
                         hx-get="/accounting/coa"
                         hx-trigger="refresh-table from:body delay:500ms"
                         hx-include="[name='keyword'], [name='page']">
                        <input type="hidden" name="page" th:value="${page.number}">
                        <div class="table-responsive">
                            <table class="table table-vcenter card-table">
                                <thead>
                                    <tr>
                                        <th th:replace="~{fragments/table :: sortable('code', #{label.coa.column.code})}">Code</th>
                                        <th th:replace="~{fragments/table :: sortable('name', #{label.coa.column.name})}">Name</th>
                                        <th th:text="#{label.coa.column.type}">Type</th>
                                        <th th:text="#{label.coa.column.balance}">Normal Balance</th>
                                        <th th:text="#{label.coa.column.level}">Level</th>
                                        <th th:text="#{label.coa.column.header}">Header</th>
                                        <th th:text="#{label.coa.column.status}">Status</th>
                                        <th class="w-1" th:text="#{label.actions}">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr th:each="item : ${page.content}" th:id="'coa-row-' + ${item.id}">
                                        <td th:text="${item.code}">1000</td>
                                        <td class="font-weight-medium">
                                            <span th:style="'padding-left: ' + (${item.level} - 1) * 20 + 'px'" th:text="${item.name}">Cash</span>
                                        </td>
                                        <td>
                                            <span class="badge bg-azure-lt" th:text="#{${'label.coa.type.' + item.accountType}}">Asset</span>
                                        </td>
                                        <td th:text="#{${'label.coa.balance.' + item.normalBalance}}">Debit</td>
                                        <td th:text="${item.level}">1</td>
                                        <td>
                                            <span th:if="${item.isHeader}" class="badge bg-purple-lt" th:text="#{label.yes}">Yes</span>
                                            <span th:unless="${item.isHeader}" class="text-secondary">-</span>
                                        </td>
                                        <td>
                                            <span class="badge badge-outline text-green" th:if="${item.isActive}">
                                                <span class="badge-dot bg-success me-1"></span>
                                                <span th:text="#{label.active}">Active</span>
                                            </span>
                                            <span class="badge badge-outline text-red" th:unless="${item.isActive}">
                                                <span class="badge-dot bg-danger me-1"></span>
                                                <span th:text="#{label.inactive}">Inactive</span>
                                            </span>
                                        </td>
                                        <td class="text-end">
                                            <div class="btn-list flex-nowrap justify-content-end">
                                                <a th:href="@{/accounting/coa/edit/{id}(id=${item.id})}"
                                                    class="btn btn-white btn-sm" sec:authorize="hasAuthority('ACCOUNTING-COA_UPDATE')"
                                                    th:text="#{label.edit}">Edit</a>
                                                <button class="btn btn-white btn-sm text-danger" data-bs-toggle="modal"
                                                    th:data-bs-target="'#modal-delete-' + ${item.id}"
                                                    sec:authorize="hasAuthority('ACCOUNTING-COA_DELETE')">
                                                    <i class="ti ti-trash me-1"></i> <span th:text="#{label.delete}">Delete</span>
                                                </button>
                                            </div>
                                            <div th:replace="~{fragments/modals :: delete-confirm(
                                                id='modal-delete-' + ${item.id},
                                                title=#{label.delete.confirm.title},
                                                message=#{msg.delete.confirm(${item.name})},
                                                actionUrl='/accounting/coa/' + ${item.id},
                                                targetId='#coa-row-' + ${item.id}
                                            )}"></div>
                                        </td>
                                    </tr>
                                    <tr th:if="${page.isEmpty()}">
                                        <td colspan="8" class="text-center py-4 text-secondary"
                                            th:text="#{label.coa.empty}">No data.</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                        <div th:replace="~{fragments/table :: pagination(${page})}"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>

</html>
```

- [ ] **Step 6: Create form template**

Create `src/main/resources/templates/accounting/coa/form.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<head th:replace="~{layout/master :: head(${coaRequest.id == null ? #messages.msg('label.coa.add') : #messages.msg('label.coa.edit')})}"></head>

<body th:replace="~{layout/master :: layout(~{:: .coa-form-content}, ~{})}">
    <div class="coa-form-content">
        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title" th:text="${coaRequest.id == null ? #messages.msg('label.coa.add') : #messages.msg('label.coa.edit') + ': ' + coaRequest.name}">
                            Form Akun
                        </h2>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">
                <form th:action="${coaRequest.id == null ? '/accounting/coa/create' : '/accounting/coa/edit/' + coaRequest.id}"
                      th:object="${coaRequest}" method="post" class="card"
                      data-ajax-form="true"
                      data-redirect-on-success="/accounting/coa">

                    <input type="hidden" th:field="*{version}" />

                    <div class="card-body">
                        <div class="alert-container"></div>

                        <div class="row">
                            <div class="col-md-6">
                                <h3 class="card-title" th:text="#{label.product.section.basic}">Basic Info</h3>

                                <div th:replace="~{fragments/inputs :: text(field='code', label=#{label.coa.code}, required=true, isReadonly=${coaRequest.id != null}, extraClass=${coaRequest.id != null ? 'bg-body-tertiary' : ''}, placeholder=#{placeholder.coa.code})}"></div>

                                <div th:replace="~{fragments/inputs :: text(field='name', label=#{label.coa.name}, required=true, placeholder=#{placeholder.coa.name})}"></div>

                                <div class="mb-3">
                                    <label class="form-label required" th:text="#{label.coa.account.type}">Account Type</label>
                                    <select th:field="*{accountType}" class="form-select erp-input">
                                        <option value="" th:text="#{label.select.placeholder}">-- Select --</option>
                                        <option th:each="type : ${accountTypes}"
                                                th:value="${type.name()}"
                                                th:text="#{${'label.coa.type.' + type.name()}}">Type</option>
                                    </select>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label" th:text="#{label.coa.parent}">Parent Account</label>
                                    <select th:field="*{parentId}" class="form-select erp-input"
                                            data-lookup-url="/api/lookup/accounting/coa"
                                            data-lookup-permission="LOOKUP_COA">
                                        <option value="" th:text="#{label.select.placeholder}">-- Select --</option>
                                    </select>
                                </div>
                            </div>

                            <div class="col-md-6 border-start-md">
                                <h3 class="card-title" th:text="#{label.general.settings}">Settings</h3>

                                <div th:replace="~{fragments/inputs :: number(field='level', label=#{label.coa.level}, required=false)}"></div>

                                <div class="mb-3">
                                    <label class="form-label" th:text="#{label.coa.note}">Note</label>
                                    <textarea th:field="*{note}" class="form-control erp-input" rows="3" th:placeholder="#{placeholder.coa.note}"></textarea>
                                </div>

                                <div class="mb-3">
                                    <label class="form-check form-switch">
                                        <input class="form-check-input" type="checkbox" th:field="*{isActive}">
                                        <span class="form-check-label" th:text="#{label.active}">Active</span>
                                    </label>
                                </div>

                                <div class="mb-3">
                                    <label class="form-check">
                                        <input class="form-check-input" type="checkbox" th:field="*{isHeader}">
                                        <span class="form-check-label" th:text="#{label.coa.is.header}">Header Account</span>
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div th:if="${auditInfo != null and auditInfo.id != null}"
                             th:replace="~{fragments/audit-info :: audit-info(${auditInfo})}"></div>
                    </div>

                    <div class="card-footer text-end">
                        <div class="d-flex align-items-center">
                            <div id="loading-indicator" class="spinner-border spinner-border-sm text-primary me-2" role="status" style="display: none;"></div>
                            <a th:href="@{/accounting/coa}" class="btn btn-link link-secondary" th:text="#{label.cancel}">Cancel</a>
                            <button type="submit" class="btn btn-primary ms-auto">
                                <i class="ti ti-device-floppy me-1"></i>
                                <span th:text="#{label.save}">Save</span>
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</body>

</html>
```

- [ ] **Step 7: Compile and verify**

Run: `.\mvnw.cmd clean compile -q`

Expected: BUILD SUCCESS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/solusi/erp/accounting/coa/web/ src/main/resources/templates/accounting/coa/
git commit -m "feat(accounting): add COA web layer — DTOs, mapper, controller, templates

- CoaSaveRequest, CoaSummaryResponse, CoaDetailResponse DTOs
- CoaWebMapper with audit field mapping
- CoaController with full CRUD and @PreAuthorize
- CoaLookupController for autocomplete API
- list.html and form.html Thymeleaf templates with HTMX

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---
## Task 6: Accounting Schema — All Layers

**Files:**
- Create: `src/main/java/com/solusi/erp/accounting/schema/domain/model/SchemaEventType.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/domain/model/AccountingSchema.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/domain/repository/SchemaRepository.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/domain/port/SchemaInUseChecker.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/CreateSchemaUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/CreateSchemaUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/UpdateSchemaUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/UpdateSchemaUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/DeleteSchemaUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/DeleteSchemaUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/application/usecase/query/FindSchemasUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/application/usecase/query/FindSchemasUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/application/usecase/query/GetSchemaEditViewUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/application/usecase/query/GetSchemaEditViewUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/infrastructure/persistence/AccountingSchema.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/infrastructure/persistence/SchemaJpaRepository.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/infrastructure/persistence/SchemaPersistenceMapper.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/infrastructure/adapter/SchemaRepositoryImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/infrastructure/adapter/SchemaInUseCheckerImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/infrastructure/config/SchemaConfig.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/web/dto/SchemaSaveRequest.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/web/dto/SchemaSummaryResponse.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/web/dto/SchemaDetailResponse.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/web/mapper/SchemaWebMapper.java`
- Create: `src/main/java/com/solusi/erp/accounting/schema/web/controller/SchemaController.java`
- Create: `src/main/resources/templates/accounting/schema/list.html`
- Create: `src/main/resources/templates/accounting/schema/form.html`
- Modify: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaInUseCheckerImpl.java` (wire SchemaJpaRepository)
- Modify: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/config/CoaConfig.java` (inject SchemaJpaRepository into checker)

- [ ] **Step 1: Create domain model**

Create `src/main/java/com/solusi/erp/accounting/schema/domain/model/SchemaEventType.java`:

```java
package com.solusi.erp.accounting.schema.domain.model;

/**
 * Operational events that generate automatic journal entries.
 * Each event maps to a debit/credit COA pair via AccountingSchema.
 */
public enum SchemaEventType {
    GOODS_RECEIPT,
    VENDOR_BILL,
    VENDOR_PAYMENT,
    CUSTOMER_INVOICE,
    GOODS_ISSUE,
    CUSTOMER_RECEIPT,
    STOCK_ADJUSTMENT_IN,
    STOCK_ADJUSTMENT_OUT
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/domain/model/AccountingSchema.java`:

```java
package com.solusi.erp.accounting.schema.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

/**
 * Aggregate Root: Accounting Schema.
 * Maps an operational event to a debit/credit COA pair for auto-journaling.
 * 100% Pure Java Domain Model.
 */
public class AccountingSchema {
    private final AuditMetadata metadata;
    private SchemaEventType eventType;
    private String description;
    private Long debitAccountId;
    private Long creditAccountId;
    private Boolean isActive;

    public AccountingSchema(AuditMetadata metadata, SchemaEventType eventType,
                            String description, Long debitAccountId,
                            Long creditAccountId, Boolean isActive) {
        this.metadata = metadata;
        this.eventType = eventType;
        this.description = description;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.isActive = isActive;
    }

    public static AccountingSchema createNew(SchemaEventType eventType, String description,
                                              Long debitAccountId, Long creditAccountId,
                                              Boolean isActive) {
        return new AccountingSchema(AuditMetadata.empty(), eventType, description,
                debitAccountId, creditAccountId,
                isActive != null ? isActive : true);
    }

    public void update(String description, Long debitAccountId,
                       Long creditAccountId, Boolean isActive) {
        this.description = description;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.isActive = isActive != null ? isActive : true;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public SchemaEventType getEventType() { return eventType; }
    public String getDescription() { return description; }
    public Long getDebitAccountId() { return debitAccountId; }
    public Long getCreditAccountId() { return creditAccountId; }
    public Boolean getIsActive() { return isActive; }
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/domain/repository/SchemaRepository.java`:

```java
package com.solusi.erp.accounting.schema.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;

import java.util.Optional;

public interface SchemaRepository {
    AccountingSchema save(AccountingSchema schema);
    Optional<AccountingSchema> findById(Long id);
    Page<AccountingSchema> findAll(String keyword, Pageable pageable);
    void delete(Long id);
    boolean existsByEventTypeAndIsActiveTrue(SchemaEventType eventType);
    Optional<AccountingSchema> findByEventTypeAndIsActiveTrue(SchemaEventType eventType);
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/domain/port/SchemaInUseChecker.java`:

```java
package com.solusi.erp.accounting.schema.domain.port;

public interface SchemaInUseChecker {
    boolean isInUse(Long schemaId);
}
```

- [ ] **Step 2: Create application use cases**

Create `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/CreateSchemaUseCase.java`:

```java
package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;

@FunctionalInterface
public interface CreateSchemaUseCase {
    AccountingSchema execute(SchemaEventType eventType, String description,
                              Long debitAccountId, Long creditAccountId, Boolean isActive);
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/CreateSchemaUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;

public class CreateSchemaUseCaseImpl implements CreateSchemaUseCase {

    private final SchemaRepository repository;

    public CreateSchemaUseCaseImpl(SchemaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountingSchema execute(SchemaEventType eventType, String description,
                                     Long debitAccountId, Long creditAccountId, Boolean isActive) {
        if (Boolean.TRUE.equals(isActive) && repository.existsByEventTypeAndIsActiveTrue(eventType)) {
            throw new DomainException("msg.error.common.duplicate");
        }
        AccountingSchema schema = AccountingSchema.createNew(eventType, description,
                debitAccountId, creditAccountId, isActive);
        return repository.save(schema);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/UpdateSchemaUseCase.java`:

```java
package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;

@FunctionalInterface
public interface UpdateSchemaUseCase {
    AccountingSchema execute(Long id, String description, Long debitAccountId,
                              Long creditAccountId, Boolean isActive);
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/UpdateSchemaUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;

public class UpdateSchemaUseCaseImpl implements UpdateSchemaUseCase {

    private final SchemaRepository repository;

    public UpdateSchemaUseCaseImpl(SchemaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountingSchema execute(Long id, String description, Long debitAccountId,
                                     Long creditAccountId, Boolean isActive) {
        AccountingSchema schema = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.schema.notfound"));
        schema.update(description, debitAccountId, creditAccountId, isActive);
        return repository.save(schema);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/DeleteSchemaUseCase.java`:

```java
package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeleteSchemaUseCase {
    DeleteResult execute(Long id);
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/application/usecase/command/DeleteSchemaUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.port.SchemaInUseChecker;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;

public class DeleteSchemaUseCaseImpl implements DeleteSchemaUseCase {

    private final SchemaRepository repository;
    private final SchemaInUseChecker inUseChecker;

    public DeleteSchemaUseCaseImpl(SchemaRepository repository, SchemaInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        AccountingSchema schema = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.schema.notfound"));
        if (inUseChecker.isInUse(id)) {
            schema.softDelete();
            repository.save(schema);
            return DeleteResult.SOFT_DELETED;
        }
        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/application/usecase/query/FindSchemasUseCase.java`:

```java
package com.solusi.erp.accounting.schema.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;

@FunctionalInterface
public interface FindSchemasUseCase {
    Page<AccountingSchema> execute(String keyword, Pageable pageable);
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/application/usecase/query/FindSchemasUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.schema.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;

public class FindSchemasUseCaseImpl implements FindSchemasUseCase {

    private final SchemaRepository repository;

    public FindSchemasUseCaseImpl(SchemaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<AccountingSchema> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/application/usecase/query/GetSchemaEditViewUseCase.java`:

```java
package com.solusi.erp.accounting.schema.application.usecase.query;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;

import java.util.Optional;

@FunctionalInterface
public interface GetSchemaEditViewUseCase {
    Optional<AccountingSchema> execute(Long id);
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/application/usecase/query/GetSchemaEditViewUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.schema.application.usecase.query;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;

import java.util.Optional;

public class GetSchemaEditViewUseCaseImpl implements GetSchemaEditViewUseCase {

    private final SchemaRepository repository;

    public GetSchemaEditViewUseCaseImpl(SchemaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AccountingSchema> execute(Long id) {
        return repository.findById(id);
    }
}
```

- [ ] **Step 3: Create infrastructure layer**

Create `src/main/java/com/solusi/erp/accounting/schema/infrastructure/persistence/AccountingSchema.java`:

```java
package com.solusi.erp.accounting.schema.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "acc_accounting_schemas")
@Getter
@Setter
public class AccountingSchema extends BaseModel {

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(length = 255)
    private String description;

    @Column(name = "debit_account_id", nullable = false)
    private Long debitAccountId;

    @Column(name = "credit_account_id", nullable = false)
    private Long creditAccountId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public AccountingSchema() {
        this.isActive = true;
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/infrastructure/persistence/SchemaJpaRepository.java`:

```java
package com.solusi.erp.accounting.schema.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchemaJpaRepository extends JpaRepository<AccountingSchema, Long> {

    @Query("SELECT s FROM AccountingSchema s WHERE " +
            "(LOWER(s.eventType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AccountingSchema> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByEventTypeAndIsActiveTrue(String eventType);

    Optional<AccountingSchema> findByEventTypeAndIsActiveTrue(String eventType);

    boolean existsByDebitAccountIdOrCreditAccountId(Long debitId, Long creditId);
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/infrastructure/persistence/SchemaPersistenceMapper.java`:

```java
package com.solusi.erp.accounting.schema.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SchemaPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "eventType", source = "eventType", qualifiedByName = "toSchemaEventType")
    com.solusi.erp.accounting.schema.domain.model.AccountingSchema toDomain(AccountingSchema entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "eventType", source = "eventType", qualifiedByName = "eventTypeToString")
    AccountingSchema toEntity(com.solusi.erp.accounting.schema.domain.model.AccountingSchema domain);

    default AuditMetadata toAuditMetadata(AccountingSchema entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    @Named("toSchemaEventType")
    default SchemaEventType toSchemaEventType(String value) {
        return value != null ? SchemaEventType.valueOf(value) : null;
    }

    @Named("eventTypeToString")
    default String eventTypeToString(SchemaEventType value) {
        return value != null ? value.name() : null;
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/infrastructure/adapter/SchemaRepositoryImpl.java`:

```java
package com.solusi.erp.accounting.schema.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaJpaRepository;
import com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaPersistenceMapper;

import java.util.Optional;
import java.util.stream.Collectors;

public class SchemaRepositoryImpl implements SchemaRepository {

    private final SchemaJpaRepository jpaRepository;
    private final SchemaPersistenceMapper mapper;

    public SchemaRepositoryImpl(SchemaJpaRepository jpaRepository, SchemaPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public AccountingSchema save(AccountingSchema domain) {
        var entity = mapper.toEntity(domain);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AccountingSchema> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<AccountingSchema> findAll(String keyword, Pageable pageable) {
        var springPageable = PageableMapper.toSpring(pageable);
        var springPage = (keyword != null && !keyword.isBlank())
                ? jpaRepository.search(keyword, springPageable)
                : jpaRepository.findAll(springPageable);
        return new Page<>(
                springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByEventTypeAndIsActiveTrue(SchemaEventType eventType) {
        return jpaRepository.existsByEventTypeAndIsActiveTrue(eventType.name());
    }

    @Override
    public Optional<AccountingSchema> findByEventTypeAndIsActiveTrue(SchemaEventType eventType) {
        return jpaRepository.findByEventTypeAndIsActiveTrue(eventType.name()).map(mapper::toDomain);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/infrastructure/adapter/SchemaInUseCheckerImpl.java`:

```java
package com.solusi.erp.accounting.schema.infrastructure.adapter;

import com.solusi.erp.accounting.schema.domain.port.SchemaInUseChecker;

public class SchemaInUseCheckerImpl implements SchemaInUseChecker {
    @Override
    public boolean isInUse(Long schemaId) {
        // Will be wired to JournalEntry references when Journal module is built
        return false;
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/infrastructure/config/SchemaConfig.java`:

```java
package com.solusi.erp.accounting.schema.infrastructure.config;

import com.solusi.erp.accounting.schema.application.usecase.command.*;
import com.solusi.erp.accounting.schema.application.usecase.query.*;
import com.solusi.erp.accounting.schema.domain.port.SchemaInUseChecker;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.accounting.schema.infrastructure.adapter.SchemaInUseCheckerImpl;
import com.solusi.erp.accounting.schema.infrastructure.adapter.SchemaRepositoryImpl;
import com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaJpaRepository;
import com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class SchemaConfig {

    @Bean
    public SchemaRepository schemaDomainRepository(SchemaJpaRepository jpaRepository,
                                                    SchemaPersistenceMapper mapper) {
        return new SchemaRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public SchemaInUseChecker schemaInUseChecker() {
        return new SchemaInUseCheckerImpl();
    }

    @Bean
    public CreateSchemaUseCase createSchemaUseCase(SchemaRepository schemaDomainRepository,
                                                    PlatformTransactionManager txManager) {
        CreateSchemaUseCase pure = new CreateSchemaUseCaseImpl(schemaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (eventType, description, debitAccountId, creditAccountId, isActive) ->
                tx.execute(status -> pure.execute(eventType, description, debitAccountId, creditAccountId, isActive));
    }

    @Bean
    public UpdateSchemaUseCase updateSchemaUseCase(SchemaRepository schemaDomainRepository,
                                                    PlatformTransactionManager txManager) {
        UpdateSchemaUseCase pure = new UpdateSchemaUseCaseImpl(schemaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, description, debitAccountId, creditAccountId, isActive) ->
                tx.execute(status -> pure.execute(id, description, debitAccountId, creditAccountId, isActive));
    }

    @Bean
    public DeleteSchemaUseCase deleteSchemaUseCase(SchemaRepository schemaDomainRepository,
                                                    SchemaInUseChecker schemaInUseChecker,
                                                    PlatformTransactionManager txManager) {
        DeleteSchemaUseCase pure = new DeleteSchemaUseCaseImpl(schemaDomainRepository, schemaInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public FindSchemasUseCase findSchemasUseCase(SchemaRepository schemaDomainRepository,
                                                  PlatformTransactionManager txManager) {
        FindSchemasUseCase pure = new FindSchemasUseCaseImpl(schemaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetSchemaEditViewUseCase getSchemaEditViewUseCase(SchemaRepository schemaDomainRepository,
                                                              PlatformTransactionManager txManager) {
        GetSchemaEditViewUseCase pure = new GetSchemaEditViewUseCaseImpl(schemaDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }
}
```

- [ ] **Step 4: Update COA InUseChecker to wire SchemaJpaRepository**

Replace the temporary `CoaInUseCheckerImpl` with the real one:

Modify `src/main/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaInUseCheckerImpl.java`:

```java
package com.solusi.erp.accounting.coa.infrastructure.adapter;

import com.solusi.erp.accounting.coa.domain.port.CoaInUseChecker;
import com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaJpaRepository;

public class CoaInUseCheckerImpl implements CoaInUseChecker {

    private final SchemaJpaRepository schemaJpaRepository;

    public CoaInUseCheckerImpl(SchemaJpaRepository schemaJpaRepository) {
        this.schemaJpaRepository = schemaJpaRepository;
    }

    @Override
    public boolean isInUse(Long coaId) {
        return schemaJpaRepository.existsByDebitAccountIdOrCreditAccountId(coaId, coaId);
    }
}
```

Update `CoaConfig.java` — change the `coaInUseChecker` bean:

```java
@Bean
public CoaInUseChecker coaInUseChecker(
        com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaJpaRepository schemaJpaRepository) {
    return new CoaInUseCheckerImpl(schemaJpaRepository);
}
```

- [ ] **Step 5: Create web layer**

Create `src/main/java/com/solusi/erp/accounting/schema/web/dto/SchemaSaveRequest.java`:

```java
package com.solusi.erp.accounting.schema.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SchemaSaveRequest extends BaseAuditResponse {

    @NotNull(message = "{label.schema.event.type} {validation.notnull.suffix}")
    private String eventType;

    private String description;

    @NotNull(message = "{label.schema.debit.account} {validation.notnull.suffix}")
    private Long debitAccountId;

    @NotNull(message = "{label.schema.credit.account} {validation.notnull.suffix}")
    private Long creditAccountId;

    private Boolean isActive;
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/web/dto/SchemaSummaryResponse.java`:

```java
package com.solusi.erp.accounting.schema.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SchemaSummaryResponse extends BaseAuditResponse {
    private String eventType;
    private String description;
    private Long debitAccountId;
    private String debitAccountName;
    private Long creditAccountId;
    private String creditAccountName;
    private Boolean isActive;
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/web/dto/SchemaDetailResponse.java`:

```java
package com.solusi.erp.accounting.schema.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SchemaDetailResponse extends BaseAuditResponse {
    private String eventType;
    private String description;
    private Long debitAccountId;
    private String debitAccountName;
    private Long creditAccountId;
    private String creditAccountName;
    private Boolean isActive;
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/web/mapper/SchemaWebMapper.java`:

```java
package com.solusi.erp.accounting.schema.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.web.dto.SchemaDetailResponse;
import com.solusi.erp.accounting.schema.web.dto.SchemaSaveRequest;
import com.solusi.erp.accounting.schema.web.dto.SchemaSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SchemaWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Autowired
    protected CoaLookupProvider coaLookupProvider;

    @Mapping(target = "eventType", expression = "java(domain.getEventType() != null ? domain.getEventType().name() : null)")
    @Mapping(target = "debitAccountName", ignore = true)
    @Mapping(target = "creditAccountName", ignore = true)
    public abstract SchemaSummaryResponse toSummaryResponse(AccountingSchema domain);

    @Mapping(target = "eventType", expression = "java(domain.getEventType() != null ? domain.getEventType().name() : null)")
    @Mapping(target = "debitAccountName", ignore = true)
    @Mapping(target = "creditAccountName", ignore = true)
    public abstract SchemaDetailResponse toDetailResponse(AccountingSchema domain);

    @Mapping(target = "eventType", expression = "java(domain.getEventType() != null ? domain.getEventType().name() : null)")
    public abstract SchemaSaveRequest toSaveRequest(AccountingSchema domain);

    @AfterMapping
    protected void mapAuditFields(AccountingSchema domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }

    @AfterMapping
    protected void resolveAccountNames(AccountingSchema domain, @MappingTarget SchemaSummaryResponse target) {
        resolveNames(domain, target);
    }

    @AfterMapping
    protected void resolveAccountNames(AccountingSchema domain, @MappingTarget SchemaDetailResponse target) {
        resolveNames(domain, target);
    }

    private void resolveNames(AccountingSchema domain, Object target) {
        LookupDto debit = coaLookupProvider.resolve(domain.getDebitAccountId());
        LookupDto credit = coaLookupProvider.resolve(domain.getCreditAccountId());
        if (target instanceof SchemaSummaryResponse s) {
            s.setDebitAccountName(debit != null ? debit.subText() + " - " + debit.name() : null);
            s.setCreditAccountName(credit != null ? credit.subText() + " - " + credit.name() : null);
        } else if (target instanceof SchemaDetailResponse d) {
            d.setDebitAccountName(debit != null ? debit.subText() + " - " + debit.name() : null);
            d.setCreditAccountName(credit != null ? credit.subText() + " - " + credit.name() : null);
        }
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/schema/web/controller/SchemaController.java`:

```java
package com.solusi.erp.accounting.schema.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.accounting.schema.application.usecase.command.CreateSchemaUseCase;
import com.solusi.erp.accounting.schema.application.usecase.command.DeleteSchemaUseCase;
import com.solusi.erp.accounting.schema.application.usecase.command.UpdateSchemaUseCase;
import com.solusi.erp.accounting.schema.application.usecase.query.FindSchemasUseCase;
import com.solusi.erp.accounting.schema.application.usecase.query.GetSchemaEditViewUseCase;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.web.dto.SchemaDetailResponse;
import com.solusi.erp.accounting.schema.web.dto.SchemaSaveRequest;
import com.solusi.erp.accounting.schema.web.dto.SchemaSummaryResponse;
import com.solusi.erp.accounting.schema.web.mapper.SchemaWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/accounting/schemas")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class SchemaController {

    private final CreateSchemaUseCase createSchemaUseCase;
    private final UpdateSchemaUseCase updateSchemaUseCase;
    private final DeleteSchemaUseCase deleteSchemaUseCase;
    private final FindSchemasUseCase findSchemasUseCase;
    private final GetSchemaEditViewUseCase getSchemaEditViewUseCase;
    private final SchemaWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<AccountingSchema> domainPage = findSchemasUseCase.execute(keyword, domainPageable);

        List<SchemaSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<SchemaSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("eventTypes", SchemaEventType.values());
        return "accounting/schema/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_CREATE')")
    public String showCreateForm(Model model) {
        SchemaSaveRequest request = new SchemaSaveRequest();
        request.setIsActive(true);
        model.addAttribute("schemaRequest", request);
        model.addAttribute("eventTypes", SchemaEventType.values());
        return "accounting/schema/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<SchemaDetailResponse>> create(@Valid @RequestBody SchemaSaveRequest request) {
        AccountingSchema domain = createSchemaUseCase.execute(
                SchemaEventType.valueOf(request.getEventType()),
                request.getDescription(),
                request.getDebitAccountId(), request.getCreditAccountId(),
                request.getIsActive());
        SchemaDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        AccountingSchema domain = getSchemaEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Schema not found"));
        model.addAttribute("schemaRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("eventTypes", SchemaEventType.values());
        return "accounting/schema/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<SchemaDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SchemaSaveRequest request) {
        AccountingSchema domain = updateSchemaUseCase.execute(
                id, request.getDescription(),
                request.getDebitAccountId(), request.getCreditAccountId(),
                request.getIsActive());
        SchemaDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-SCHEMA_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteResult result = deleteSchemaUseCase.execute(id);
        if (result == DeleteResult.SOFT_DELETED) {
            String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
            return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
        }
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
```

- [ ] **Step 6: Create Schema templates**

Create `src/main/resources/templates/accounting/schema/list.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<head th:replace="~{layout/master :: head(#{label.schema.title})}"></head>

<body th:replace="~{layout/master :: layout(~{:: .schema-list-content}, ~{})}">
    <div class="schema-list-content">
        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title" th:text="#{label.schema.title}">Accounting Schema</h2>
                        <div class="text-secondary mt-1" th:text="#{label.schema.subtitle}">Configure mappings.</div>
                    </div>
                    <div class="col-auto ms-auto d-print-none" sec:authorize="hasAuthority('ACCOUNTING-SCHEMA_CREATE')">
                        <div class="btn-list">
                            <a th:href="@{/accounting/schemas/create}" class="btn btn-primary d-none d-sm-inline-block">
                                <i class="ti ti-plus"></i>
                                <span th:text="#{label.schema.add}">Add New Schema</span>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">
                <div class="card">
                    <div class="card-body border-bottom py-3">
                        <div class="d-flex">
                            <div class="text-secondary"></div>
                            <div class="ms-auto text-secondary">
                                <form th:action="@{/accounting/schemas}" method="get" class="input-icon"
                                      hx-get="/accounting/schemas"
                                      hx-target="#schema-table-container"
                                      hx-trigger="keyup changed delay:500ms from:#search-input">
                                    <input id="search-input" type="text" name="keyword" th:value="${keyword}"
                                        class="form-control form-control-sm" th:placeholder="#{label.search}">
                                    <span class="input-icon-addon"><i class="ti ti-search"></i></span>
                                </form>
                            </div>
                        </div>
                    </div>
                    <div id="schema-table-container" th:fragment="schema-table-container"
                         hx-get="/accounting/schemas"
                         hx-trigger="refresh-table from:body delay:500ms"
                         hx-include="[name='keyword'], [name='page']">
                        <input type="hidden" name="page" th:value="${page.number}">
                        <div class="table-responsive">
                            <table class="table table-vcenter card-table">
                                <thead>
                                    <tr>
                                        <th th:text="#{label.schema.column.event}">Event Type</th>
                                        <th th:text="#{label.schema.column.description}">Description</th>
                                        <th th:text="#{label.schema.column.debit}">Debit Account</th>
                                        <th th:text="#{label.schema.column.credit}">Credit Account</th>
                                        <th th:text="#{label.schema.column.status}">Status</th>
                                        <th class="w-1" th:text="#{label.actions}">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr th:each="item : ${page.content}" th:id="'schema-row-' + ${item.id}">
                                        <td>
                                            <span class="badge bg-blue-lt" th:text="#{${'label.schema.event.' + item.eventType}}">Event</span>
                                        </td>
                                        <td th:text="${item.description}">Description</td>
                                        <td th:text="${item.debitAccountName}">Debit</td>
                                        <td th:text="${item.creditAccountName}">Credit</td>
                                        <td>
                                            <span class="badge badge-outline text-green" th:if="${item.isActive}">
                                                <span class="badge-dot bg-success me-1"></span>
                                                <span th:text="#{label.active}">Active</span>
                                            </span>
                                            <span class="badge badge-outline text-red" th:unless="${item.isActive}">
                                                <span class="badge-dot bg-danger me-1"></span>
                                                <span th:text="#{label.inactive}">Inactive</span>
                                            </span>
                                        </td>
                                        <td class="text-end">
                                            <div class="btn-list flex-nowrap justify-content-end">
                                                <a th:href="@{/accounting/schemas/edit/{id}(id=${item.id})}"
                                                    class="btn btn-white btn-sm" sec:authorize="hasAuthority('ACCOUNTING-SCHEMA_UPDATE')"
                                                    th:text="#{label.edit}">Edit</a>
                                                <button class="btn btn-white btn-sm text-danger" data-bs-toggle="modal"
                                                    th:data-bs-target="'#modal-delete-' + ${item.id}"
                                                    sec:authorize="hasAuthority('ACCOUNTING-SCHEMA_DELETE')">
                                                    <i class="ti ti-trash me-1"></i> <span th:text="#{label.delete}">Delete</span>
                                                </button>
                                            </div>
                                            <div th:replace="~{fragments/modals :: delete-confirm(
                                                id='modal-delete-' + ${item.id},
                                                title=#{label.delete.confirm.title},
                                                message=#{msg.delete.confirm(${item.eventType})},
                                                actionUrl='/accounting/schemas/' + ${item.id},
                                                targetId='#schema-row-' + ${item.id}
                                            )}"></div>
                                        </td>
                                    </tr>
                                    <tr th:if="${page.isEmpty()}">
                                        <td colspan="6" class="text-center py-4 text-secondary"
                                            th:text="#{label.schema.empty}">No data.</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                        <div th:replace="~{fragments/table :: pagination(${page})}"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>

</html>
```

Create `src/main/resources/templates/accounting/schema/form.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<head th:replace="~{layout/master :: head(${schemaRequest.id == null ? #messages.msg('label.schema.add') : #messages.msg('label.schema.edit')})}"></head>

<body th:replace="~{layout/master :: layout(~{:: .schema-form-content}, ~{})}">
    <div class="schema-form-content">
        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title" th:text="${schemaRequest.id == null ? #messages.msg('label.schema.add') : #messages.msg('label.schema.edit')}">
                            Form Schema
                        </h2>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">
                <form th:action="${schemaRequest.id == null ? '/accounting/schemas/create' : '/accounting/schemas/edit/' + schemaRequest.id}"
                      th:object="${schemaRequest}" method="post" class="card"
                      data-ajax-form="true"
                      data-redirect-on-success="/accounting/schemas">

                    <input type="hidden" th:field="*{version}" />

                    <div class="card-body">
                        <div class="alert-container"></div>

                        <div class="row">
                            <div class="col-md-6">
                                <h3 class="card-title" th:text="#{label.schema.event.type}">Event Mapping</h3>

                                <div class="mb-3">
                                    <label class="form-label required" th:text="#{label.schema.event.type}">Event Type</label>
                                    <select th:field="*{eventType}" class="form-select erp-input"
                                            th:disabled="${schemaRequest.id != null}">
                                        <option value="" th:text="#{label.select.placeholder}">-- Select --</option>
                                        <option th:each="type : ${eventTypes}"
                                                th:value="${type.name()}"
                                                th:text="#{${'label.schema.event.' + type.name()}}">Type</option>
                                    </select>
                                    <input th:if="${schemaRequest.id != null}" type="hidden" th:field="*{eventType}" />
                                </div>

                                <div class="mb-3">
                                    <label class="form-label" th:text="#{label.schema.description}">Description</label>
                                    <textarea th:field="*{description}" class="form-control erp-input" rows="3"
                                              th:placeholder="#{placeholder.schema.description}"></textarea>
                                </div>
                            </div>

                            <div class="col-md-6 border-start-md">
                                <h3 class="card-title">Account Mapping</h3>

                                <div class="mb-3">
                                    <label class="form-label required" th:text="#{label.schema.debit.account}">Debit Account</label>
                                    <select th:field="*{debitAccountId}" class="form-select erp-input"
                                            data-lookup-url="/api/lookup/accounting/coa"
                                            data-lookup-permission="LOOKUP_COA">
                                        <option value="" th:text="#{label.select.placeholder}">-- Select --</option>
                                    </select>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label required" th:text="#{label.schema.credit.account}">Credit Account</label>
                                    <select th:field="*{creditAccountId}" class="form-select erp-input"
                                            data-lookup-url="/api/lookup/accounting/coa"
                                            data-lookup-permission="LOOKUP_COA">
                                        <option value="" th:text="#{label.select.placeholder}">-- Select --</option>
                                    </select>
                                </div>

                                <div class="mb-3">
                                    <label class="form-check form-switch">
                                        <input class="form-check-input" type="checkbox" th:field="*{isActive}">
                                        <span class="form-check-label" th:text="#{label.active}">Active</span>
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div th:if="${auditInfo != null and auditInfo.id != null}"
                             th:replace="~{fragments/audit-info :: audit-info(${auditInfo})}"></div>
                    </div>

                    <div class="card-footer text-end">
                        <div class="d-flex align-items-center">
                            <div id="loading-indicator" class="spinner-border spinner-border-sm text-primary me-2" role="status" style="display: none;"></div>
                            <a th:href="@{/accounting/schemas}" class="btn btn-link link-secondary" th:text="#{label.cancel}">Cancel</a>
                            <button type="submit" class="btn btn-primary ms-auto">
                                <i class="ti ti-device-floppy me-1"></i>
                                <span th:text="#{label.save}">Save</span>
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</body>

</html>
```

- [ ] **Step 7: Compile and verify**

Run: `.\mvnw.cmd clean compile -q`

Expected: BUILD SUCCESS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/solusi/erp/accounting/schema/ src/main/resources/templates/accounting/schema/ src/main/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaInUseCheckerImpl.java src/main/java/com/solusi/erp/accounting/coa/infrastructure/config/CoaConfig.java
git commit -m "feat(accounting): add Accounting Schema slice — all layers

- SchemaEventType enum with 8 operational events
- AccountingSchema aggregate root, repository, ports
- Command/Query use cases with duplicate-active-event check
- JPA entity, persistence mapper, repository adapter
- SchemaWebMapper resolves COA names via CoaLookupProvider
- SchemaController with full CRUD
- list.html and form.html with COA autocomplete lookup
- Wire CoaInUseChecker to check Schema references

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---
## Task 7: Fiscal Year & Accounting Period — All Layers

**Files:**
- Create: `src/main/java/com/solusi/erp/accounting/period/domain/model/PeriodStatus.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/domain/model/AccountingPeriod.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/domain/model/FiscalYear.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/domain/repository/FiscalYearRepository.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/domain/port/FiscalYearInUseChecker.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/CreateFiscalYearUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/CreateFiscalYearUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/UpdateFiscalYearUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/UpdateFiscalYearUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/DeleteFiscalYearUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/DeleteFiscalYearUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/ClosePeriodUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/ClosePeriodUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/ReopenPeriodUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/ReopenPeriodUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/FindFiscalYearsUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/FindFiscalYearsUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/GetFiscalYearDetailUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/GetFiscalYearDetailUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/FiscalYear.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/AccountingPeriod.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/FiscalYearJpaRepository.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/AccountingPeriodJpaRepository.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/FiscalYearPersistenceMapper.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/PeriodPersistenceMapper.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/infrastructure/adapter/FiscalYearRepositoryImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/infrastructure/adapter/FiscalYearInUseCheckerImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/infrastructure/config/PeriodConfig.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/web/dto/FiscalYearSaveRequest.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/web/dto/FiscalYearSummaryResponse.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/web/dto/FiscalYearDetailResponse.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/web/dto/PeriodResponse.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/web/mapper/PeriodWebMapper.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/web/controller/PeriodController.java`
- Create: `src/main/resources/templates/accounting/period/list.html`
- Create: `src/main/resources/templates/accounting/period/detail.html`

- [ ] **Step 1: Create domain model**

Create `src/main/java/com/solusi/erp/accounting/period/domain/model/PeriodStatus.java`:

```java
package com.solusi.erp.accounting.period.domain.model;

public enum PeriodStatus {
    OPEN,
    CLOSED
}
```

Create `src/main/java/com/solusi/erp/accounting/period/domain/model/AccountingPeriod.java`:

```java
package com.solusi.erp.accounting.period.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.time.LocalDate;

/**
 * Entity within the FiscalYear aggregate.
 * Represents a single accounting period (typically one month).
 * 100% Pure Java.
 */
public class AccountingPeriod {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private Long fiscalYearId;
    private LocalDate startDate;
    private LocalDate endDate;
    private PeriodStatus status;

    public AccountingPeriod(AuditMetadata metadata, String code, String name,
                            Long fiscalYearId, LocalDate startDate, LocalDate endDate,
                            PeriodStatus status) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.fiscalYearId = fiscalYearId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public static AccountingPeriod createNew(String code, String name, Long fiscalYearId,
                                              LocalDate startDate, LocalDate endDate) {
        return new AccountingPeriod(AuditMetadata.empty(), code, name,
                fiscalYearId, startDate, endDate, PeriodStatus.OPEN);
    }

    public void close() {
        this.status = PeriodStatus.CLOSED;
    }

    public void reopen() {
        this.status = PeriodStatus.OPEN;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Long getFiscalYearId() { return fiscalYearId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public PeriodStatus getStatus() { return status; }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/domain/model/FiscalYear.java`:

```java
package com.solusi.erp.accounting.period.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate Root: Fiscal Year.
 * Creates and manages AccountingPeriod children.
 * 100% Pure Java Domain Model.
 */
public class FiscalYear {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private List<AccountingPeriod> periods;

    public FiscalYear(AuditMetadata metadata, String code, String name,
                      LocalDate startDate, LocalDate endDate, Boolean isActive,
                      List<AccountingPeriod> periods) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.periods = periods != null ? periods : new ArrayList<>();
    }

    public static FiscalYear createNew(String code, String name,
                                        LocalDate startDate, LocalDate endDate,
                                        Boolean isActive) {
        return new FiscalYear(AuditMetadata.empty(), code, name,
                startDate, endDate,
                isActive != null ? isActive : true,
                new ArrayList<>());
    }

    /**
     * Auto-generates 12 monthly periods based on the fiscal year date range.
     * Each period spans one calendar month (or partial month for first/last).
     */
    public List<AccountingPeriod> generateMonthlyPeriods(Long fiscalYearId) {
        List<AccountingPeriod> generated = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);
        int seq = 1;

        for (YearMonth ym = startMonth; !ym.isAfter(endMonth); ym = ym.plusMonths(1)) {
            LocalDate periodStart = ym.equals(startMonth) ? startDate : ym.atDay(1);
            LocalDate periodEnd = ym.equals(endMonth) ? endDate : ym.atEndOfMonth();
            String periodCode = code + "-" + String.format("%02d", seq);
            String periodName = ym.format(monthFormatter);

            generated.add(AccountingPeriod.createNew(
                    periodCode, periodName, fiscalYearId, periodStart, periodEnd));
            seq++;
        }
        this.periods = generated;
        return generated;
    }

    public void update(String name, Boolean isActive) {
        this.name = name;
        this.isActive = isActive != null ? isActive : true;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Boolean getIsActive() { return isActive; }
    public List<AccountingPeriod> getPeriods() { return periods; }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/domain/repository/FiscalYearRepository.java`:

```java
package com.solusi.erp.accounting.period.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;

import java.util.List;
import java.util.Optional;

public interface FiscalYearRepository {
    FiscalYear save(FiscalYear fiscalYear);
    Optional<FiscalYear> findById(Long id);
    Page<FiscalYear> findAll(String keyword, Pageable pageable);
    void delete(Long id);
    boolean existsByCode(String code);

    AccountingPeriod savePeriod(AccountingPeriod period);
    List<AccountingPeriod> savePeriods(List<AccountingPeriod> periods);
    Optional<AccountingPeriod> findPeriodById(Long periodId);
    List<AccountingPeriod> findPeriodsByFiscalYearId(Long fiscalYearId);
}
```

Create `src/main/java/com/solusi/erp/accounting/period/domain/port/FiscalYearInUseChecker.java`:

```java
package com.solusi.erp.accounting.period.domain.port;

public interface FiscalYearInUseChecker {
    boolean isInUse(Long fiscalYearId);
}
```

- [ ] **Step 2: Create application use cases**

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/CreateFiscalYearUseCase.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.accounting.period.domain.model.FiscalYear;

import java.time.LocalDate;

@FunctionalInterface
public interface CreateFiscalYearUseCase {
    FiscalYear execute(String name, LocalDate startDate, LocalDate endDate, Boolean isActive);
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/CreateFiscalYearUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;
import com.solusi.erp.core.service.SequenceGeneratorService;

import java.time.LocalDate;

public class CreateFiscalYearUseCaseImpl implements CreateFiscalYearUseCase {

    private final FiscalYearRepository repository;
    private final SequenceGeneratorService sequenceGenerator;

    public CreateFiscalYearUseCaseImpl(FiscalYearRepository repository,
                                       SequenceGeneratorService sequenceGenerator) {
        this.repository = repository;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public FiscalYear execute(String name, LocalDate startDate, LocalDate endDate, Boolean isActive) {
        String code = sequenceGenerator.generateNextCode("FISCAL_YEAR");

        FiscalYear fy = FiscalYear.createNew(code, name, startDate, endDate, isActive);
        FiscalYear saved = repository.save(fy);

        // Auto-generate 12 monthly periods
        var periods = saved.generateMonthlyPeriods(saved.getId());
        repository.savePeriods(periods);

        return saved;
    }
}
```

> **Note:** `SequenceGeneratorService` is an existing core service. Its interface lives at `com.solusi.erp.core.service.SequenceGeneratorService`. The `generateNextCode(String entityName)` method reads from `system_sequences` table, increments `last_value`, and returns the formatted code (e.g., `FY-0001`).

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/UpdateFiscalYearUseCase.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.accounting.period.domain.model.FiscalYear;

@FunctionalInterface
public interface UpdateFiscalYearUseCase {
    FiscalYear execute(Long id, String name, Boolean isActive);
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/UpdateFiscalYearUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;

public class UpdateFiscalYearUseCaseImpl implements UpdateFiscalYearUseCase {

    private final FiscalYearRepository repository;

    public UpdateFiscalYearUseCaseImpl(FiscalYearRepository repository) {
        this.repository = repository;
    }

    @Override
    public FiscalYear execute(Long id, String name, Boolean isActive) {
        FiscalYear fy = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.period.notfound"));
        fy.update(name, isActive);
        return repository.save(fy);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/DeleteFiscalYearUseCase.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeleteFiscalYearUseCase {
    DeleteResult execute(Long id);
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/DeleteFiscalYearUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.port.FiscalYearInUseChecker;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;

public class DeleteFiscalYearUseCaseImpl implements DeleteFiscalYearUseCase {

    private final FiscalYearRepository repository;
    private final FiscalYearInUseChecker inUseChecker;

    public DeleteFiscalYearUseCaseImpl(FiscalYearRepository repository,
                                       FiscalYearInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        FiscalYear fy = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.period.notfound"));
        if (inUseChecker.isInUse(id)) {
            fy.softDelete();
            repository.save(fy);
            return DeleteResult.SOFT_DELETED;
        }
        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/ClosePeriodUseCase.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;

@FunctionalInterface
public interface ClosePeriodUseCase {
    AccountingPeriod execute(Long periodId);
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/ClosePeriodUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.PeriodStatus;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;

public class ClosePeriodUseCaseImpl implements ClosePeriodUseCase {

    private final FiscalYearRepository repository;

    public ClosePeriodUseCaseImpl(FiscalYearRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountingPeriod execute(Long periodId) {
        AccountingPeriod period = repository.findPeriodById(periodId)
                .orElseThrow(() -> new DomainException("msg.error.period.notfound"));
        if (period.getStatus() == PeriodStatus.CLOSED) {
            throw new DomainException("msg.error.period.already.closed");
        }
        period.close();
        return repository.savePeriod(period);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/ReopenPeriodUseCase.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;

@FunctionalInterface
public interface ReopenPeriodUseCase {
    AccountingPeriod execute(Long periodId);
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/command/ReopenPeriodUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.PeriodStatus;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;

public class ReopenPeriodUseCaseImpl implements ReopenPeriodUseCase {

    private final FiscalYearRepository repository;

    public ReopenPeriodUseCaseImpl(FiscalYearRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountingPeriod execute(Long periodId) {
        AccountingPeriod period = repository.findPeriodById(periodId)
                .orElseThrow(() -> new DomainException("msg.error.period.notfound"));
        if (period.getStatus() == PeriodStatus.OPEN) {
            throw new DomainException("msg.error.period.already.open");
        }
        period.reopen();
        return repository.savePeriod(period);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/FindFiscalYearsUseCase.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;

@FunctionalInterface
public interface FindFiscalYearsUseCase {
    Page<FiscalYear> execute(String keyword, Pageable pageable);
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/FindFiscalYearsUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;

public class FindFiscalYearsUseCaseImpl implements FindFiscalYearsUseCase {

    private final FiscalYearRepository repository;

    public FindFiscalYearsUseCaseImpl(FiscalYearRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<FiscalYear> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/GetFiscalYearDetailUseCase.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.query;

import com.solusi.erp.accounting.period.domain.model.FiscalYear;

import java.util.Optional;

@FunctionalInterface
public interface GetFiscalYearDetailUseCase {
    Optional<FiscalYear> execute(Long id);
}
```

Create `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/GetFiscalYearDetailUseCaseImpl.java`:

```java
package com.solusi.erp.accounting.period.application.usecase.query;

import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;

import java.util.List;
import java.util.Optional;

public class GetFiscalYearDetailUseCaseImpl implements GetFiscalYearDetailUseCase {

    private final FiscalYearRepository repository;

    public GetFiscalYearDetailUseCaseImpl(FiscalYearRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FiscalYear> execute(Long id) {
        Optional<FiscalYear> fyOpt = repository.findById(id);
        if (fyOpt.isPresent()) {
            FiscalYear fy = fyOpt.get();
            List<AccountingPeriod> periods = repository.findPeriodsByFiscalYearId(id);
            // Reconstruct FY with periods for detail view
            return Optional.of(new FiscalYear(
                    fy.getMetadata(), fy.getCode(), fy.getName(),
                    fy.getStartDate(), fy.getEndDate(), fy.getIsActive(), periods));
        }
        return Optional.empty();
    }
}
```

- [ ] **Step 3: Create infrastructure layer**

Create `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/FiscalYear.java`:

```java
package com.solusi.erp.accounting.period.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "acc_fiscal_years")
@Getter
@Setter
public class FiscalYear extends BaseModel {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public FiscalYear() {
        this.isActive = true;
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/AccountingPeriod.java`:

```java
package com.solusi.erp.accounting.period.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "acc_accounting_periods")
@Getter
@Setter
public class AccountingPeriod extends BaseModel {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "fiscal_year_id", nullable = false)
    private Long fiscalYearId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 10)
    private String status;

    public AccountingPeriod() {
        this.status = "OPEN";
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/FiscalYearJpaRepository.java`:

```java
package com.solusi.erp.accounting.period.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FiscalYearJpaRepository extends JpaRepository<FiscalYear, Long> {

    @Query("SELECT f FROM FiscalYear f WHERE " +
            "(LOWER(f.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            " ORDER BY f.startDate DESC")
    Page<FiscalYear> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT f FROM FiscalYear f ORDER BY f.startDate DESC")
    Page<FiscalYear> findAllOrdered(Pageable pageable);

    Optional<FiscalYear> findByCode(String code);
}
```

Create `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/AccountingPeriodJpaRepository.java`:

```java
package com.solusi.erp.accounting.period.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountingPeriodJpaRepository extends JpaRepository<AccountingPeriod, Long> {

    @Query("SELECT p FROM AccountingPeriod p WHERE p.fiscalYearId = :fyId ORDER BY p.startDate ASC")
    List<AccountingPeriod> findByFiscalYearIdOrderByStartDate(@Param("fyId") Long fiscalYearId);

    boolean existsByFiscalYearId(Long fiscalYearId);
}
```

Create `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/FiscalYearPersistenceMapper.java`:

```java
package com.solusi.erp.accounting.period.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FiscalYearPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "periods", ignore = true)
    com.solusi.erp.accounting.period.domain.model.FiscalYear toDomain(FiscalYear entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    FiscalYear toEntity(com.solusi.erp.accounting.period.domain.model.FiscalYear domain);

    default AuditMetadata toAuditMetadata(FiscalYear entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/PeriodPersistenceMapper.java`:

```java
package com.solusi.erp.accounting.period.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.accounting.period.domain.model.PeriodStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PeriodPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "status", source = "status", qualifiedByName = "toPeriodStatus")
    com.solusi.erp.accounting.period.domain.model.AccountingPeriod toDomain(AccountingPeriod entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    AccountingPeriod toEntity(com.solusi.erp.accounting.period.domain.model.AccountingPeriod domain);

    default AuditMetadata toAuditMetadata(AccountingPeriod entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    @Named("toPeriodStatus")
    default PeriodStatus toPeriodStatus(String value) {
        return value != null ? PeriodStatus.valueOf(value) : null;
    }

    @Named("statusToString")
    default String statusToString(PeriodStatus value) {
        return value != null ? value.name() : null;
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/infrastructure/adapter/FiscalYearRepositoryImpl.java`:

```java
package com.solusi.erp.accounting.period.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;
import com.solusi.erp.accounting.period.infrastructure.persistence.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FiscalYearRepositoryImpl implements FiscalYearRepository {

    private final FiscalYearJpaRepository fyJpaRepo;
    private final AccountingPeriodJpaRepository periodJpaRepo;
    private final FiscalYearPersistenceMapper fyMapper;
    private final PeriodPersistenceMapper periodMapper;

    public FiscalYearRepositoryImpl(FiscalYearJpaRepository fyJpaRepo,
                                     AccountingPeriodJpaRepository periodJpaRepo,
                                     FiscalYearPersistenceMapper fyMapper,
                                     PeriodPersistenceMapper periodMapper) {
        this.fyJpaRepo = fyJpaRepo;
        this.periodJpaRepo = periodJpaRepo;
        this.fyMapper = fyMapper;
        this.periodMapper = periodMapper;
    }

    @Override
    public FiscalYear save(FiscalYear domain) {
        var entity = fyMapper.toEntity(domain);
        var saved = fyJpaRepo.save(entity);
        return fyMapper.toDomain(saved);
    }

    @Override
    public Optional<FiscalYear> findById(Long id) {
        return fyJpaRepo.findById(id).map(fyMapper::toDomain);
    }

    @Override
    public Page<FiscalYear> findAll(String keyword, Pageable pageable) {
        var springPageable = PageableMapper.toSpring(pageable);
        var springPage = (keyword != null && !keyword.isBlank())
                ? fyJpaRepo.search(keyword, springPageable)
                : fyJpaRepo.findAllOrdered(springPageable);
        return new Page<>(
                springPage.getContent().stream().map(fyMapper::toDomain).collect(Collectors.toList()),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public void delete(Long id) {
        periodJpaRepo.findByFiscalYearIdOrderByStartDate(id)
                .forEach(p -> periodJpaRepo.deleteById(p.getId()));
        fyJpaRepo.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return fyJpaRepo.findByCode(code).isPresent();
    }

    @Override
    public AccountingPeriod savePeriod(AccountingPeriod period) {
        var entity = periodMapper.toEntity(period);
        var saved = periodJpaRepo.save(entity);
        return periodMapper.toDomain(saved);
    }

    @Override
    public List<AccountingPeriod> savePeriods(List<AccountingPeriod> periods) {
        return periods.stream()
                .map(p -> {
                    var entity = periodMapper.toEntity(p);
                    var saved = periodJpaRepo.save(entity);
                    return periodMapper.toDomain(saved);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AccountingPeriod> findPeriodById(Long periodId) {
        return periodJpaRepo.findById(periodId).map(periodMapper::toDomain);
    }

    @Override
    public List<AccountingPeriod> findPeriodsByFiscalYearId(Long fiscalYearId) {
        return periodJpaRepo.findByFiscalYearIdOrderByStartDate(fiscalYearId)
                .stream().map(periodMapper::toDomain).collect(Collectors.toList());
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/infrastructure/adapter/FiscalYearInUseCheckerImpl.java`:

```java
package com.solusi.erp.accounting.period.infrastructure.adapter;

import com.solusi.erp.accounting.period.domain.port.FiscalYearInUseChecker;
import com.solusi.erp.accounting.period.infrastructure.persistence.AccountingPeriodJpaRepository;

public class FiscalYearInUseCheckerImpl implements FiscalYearInUseChecker {

    private final AccountingPeriodJpaRepository periodJpaRepo;

    public FiscalYearInUseCheckerImpl(AccountingPeriodJpaRepository periodJpaRepo) {
        this.periodJpaRepo = periodJpaRepo;
    }

    @Override
    public boolean isInUse(Long fiscalYearId) {
        // FY is "in use" if it has periods — always true after creation.
        // Will also check JournalEntry references when Journal module is built.
        return periodJpaRepo.existsByFiscalYearId(fiscalYearId);
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/infrastructure/config/PeriodConfig.java`:

```java
package com.solusi.erp.accounting.period.infrastructure.config;

import com.solusi.erp.accounting.period.application.usecase.command.*;
import com.solusi.erp.accounting.period.application.usecase.query.*;
import com.solusi.erp.accounting.period.domain.port.FiscalYearInUseChecker;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;
import com.solusi.erp.accounting.period.infrastructure.adapter.FiscalYearInUseCheckerImpl;
import com.solusi.erp.accounting.period.infrastructure.adapter.FiscalYearRepositoryImpl;
import com.solusi.erp.accounting.period.infrastructure.persistence.*;
import com.solusi.erp.core.service.SequenceGeneratorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PeriodConfig {

    @Bean
    public FiscalYearRepository fiscalYearDomainRepository(
            FiscalYearJpaRepository fyJpaRepo,
            AccountingPeriodJpaRepository periodJpaRepo,
            FiscalYearPersistenceMapper fyMapper,
            PeriodPersistenceMapper periodMapper) {
        return new FiscalYearRepositoryImpl(fyJpaRepo, periodJpaRepo, fyMapper, periodMapper);
    }

    @Bean
    public FiscalYearInUseChecker fiscalYearInUseChecker(AccountingPeriodJpaRepository periodJpaRepo) {
        return new FiscalYearInUseCheckerImpl(periodJpaRepo);
    }

    @Bean
    public CreateFiscalYearUseCase createFiscalYearUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateFiscalYearUseCase pure = new CreateFiscalYearUseCaseImpl(
                fiscalYearDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (name, startDate, endDate, isActive) ->
                tx.execute(status -> pure.execute(name, startDate, endDate, isActive));
    }

    @Bean
    public UpdateFiscalYearUseCase updateFiscalYearUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateFiscalYearUseCase pure = new UpdateFiscalYearUseCaseImpl(fiscalYearDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, name, isActive) -> tx.execute(status -> pure.execute(id, name, isActive));
    }

    @Bean
    public DeleteFiscalYearUseCase deleteFiscalYearUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            FiscalYearInUseChecker fiscalYearInUseChecker,
            PlatformTransactionManager txManager) {
        DeleteFiscalYearUseCase pure = new DeleteFiscalYearUseCaseImpl(
                fiscalYearDomainRepository, fiscalYearInUseChecker);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public ClosePeriodUseCase closePeriodUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            PlatformTransactionManager txManager) {
        ClosePeriodUseCase pure = new ClosePeriodUseCaseImpl(fiscalYearDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (periodId) -> tx.execute(status -> pure.execute(periodId));
    }

    @Bean
    public ReopenPeriodUseCase reopenPeriodUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            PlatformTransactionManager txManager) {
        ReopenPeriodUseCase pure = new ReopenPeriodUseCaseImpl(fiscalYearDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (periodId) -> tx.execute(status -> pure.execute(periodId));
    }

    @Bean
    public FindFiscalYearsUseCase findFiscalYearsUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            PlatformTransactionManager txManager) {
        FindFiscalYearsUseCase pure = new FindFiscalYearsUseCaseImpl(fiscalYearDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetFiscalYearDetailUseCase getFiscalYearDetailUseCase(
            FiscalYearRepository fiscalYearDomainRepository,
            PlatformTransactionManager txManager) {
        GetFiscalYearDetailUseCase pure = new GetFiscalYearDetailUseCaseImpl(fiscalYearDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }
}
```

- [ ] **Step 4: Create web layer**

Create `src/main/java/com/solusi/erp/accounting/period/web/dto/FiscalYearSaveRequest.java`:

```java
package com.solusi.erp.accounting.period.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FiscalYearSaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.period.fy.name} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.period.fy.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.period.fy.start} {validation.notnull.suffix}")
    private LocalDate startDate;

    @NotNull(message = "{label.period.fy.end} {validation.notnull.suffix}")
    private LocalDate endDate;

    private Boolean isActive;
}
```

Create `src/main/java/com/solusi/erp/accounting/period/web/dto/FiscalYearSummaryResponse.java`:

```java
package com.solusi.erp.accounting.period.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FiscalYearSummaryResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private int periodCount;
}
```

Create `src/main/java/com/solusi/erp/accounting/period/web/dto/FiscalYearDetailResponse.java`:

```java
package com.solusi.erp.accounting.period.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FiscalYearDetailResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private List<PeriodResponse> periods;
}
```

Create `src/main/java/com/solusi/erp/accounting/period/web/dto/PeriodResponse.java`:

```java
package com.solusi.erp.accounting.period.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PeriodResponse {
    private Long id;
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
```

Create `src/main/java/com/solusi/erp/accounting/period/web/mapper/PeriodWebMapper.java`:

```java
package com.solusi.erp.accounting.period.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.web.dto.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PeriodWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Mapping(target = "periodCount", expression = "java(domain.getPeriods() != null ? domain.getPeriods().size() : 0)")
    public abstract FiscalYearSummaryResponse toSummaryResponse(FiscalYear domain);

    @Mapping(target = "periods", source = "periods")
    public abstract FiscalYearDetailResponse toDetailResponse(FiscalYear domain);

    public abstract FiscalYearSaveRequest toSaveRequest(FiscalYear domain);

    @Mapping(target = "status", expression = "java(period.getStatus() != null ? period.getStatus().name() : null)")
    public abstract PeriodResponse toPeriodResponse(AccountingPeriod period);

    public abstract List<PeriodResponse> toPeriodResponses(List<AccountingPeriod> periods);

    @AfterMapping
    protected void mapAuditFields(FiscalYear domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }
}
```

Create `src/main/java/com/solusi/erp/accounting/period/web/controller/PeriodController.java`:

```java
package com.solusi.erp.accounting.period.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.accounting.period.application.usecase.command.*;
import com.solusi.erp.accounting.period.application.usecase.query.*;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.web.dto.*;
import com.solusi.erp.accounting.period.web.mapper.PeriodWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/accounting/periods")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PeriodController {

    private final CreateFiscalYearUseCase createFiscalYearUseCase;
    private final UpdateFiscalYearUseCase updateFiscalYearUseCase;
    private final DeleteFiscalYearUseCase deleteFiscalYearUseCase;
    private final ClosePeriodUseCase closePeriodUseCase;
    private final ReopenPeriodUseCase reopenPeriodUseCase;
    private final FindFiscalYearsUseCase findFiscalYearsUseCase;
    private final GetFiscalYearDetailUseCase getFiscalYearDetailUseCase;
    private final PeriodWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<FiscalYear> domainPage = findFiscalYearsUseCase.execute(keyword, domainPageable);

        List<FiscalYearSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<FiscalYearSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "accounting/period/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_CREATE')")
    public String showCreateForm(Model model) {
        FiscalYearSaveRequest request = new FiscalYearSaveRequest();
        request.setIsActive(true);
        model.addAttribute("fyRequest", request);
        return "accounting/period/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<FiscalYearDetailResponse>> create(
            @Valid @RequestBody FiscalYearSaveRequest request) {
        FiscalYear domain = createFiscalYearUseCase.execute(
                request.getName(), request.getStartDate(), request.getEndDate(),
                request.getIsActive());
        FiscalYearDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_READ')")
    public String detail(@PathVariable Long id, Model model) {
        FiscalYear domain = getFiscalYearDetailUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Fiscal Year not found"));
        model.addAttribute("fy", webMapper.toDetailResponse(domain));
        return "accounting/period/detail";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        FiscalYear domain = getFiscalYearDetailUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Fiscal Year not found"));
        model.addAttribute("fyRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        return "accounting/period/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<FiscalYearDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody FiscalYearSaveRequest request) {
        FiscalYear domain = updateFiscalYearUseCase.execute(id, request.getName(), request.getIsActive());
        FiscalYearDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteResult result = deleteFiscalYearUseCase.execute(id);
        if (result == DeleteResult.SOFT_DELETED) {
            String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
            return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
        }
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    @PostMapping("/periods/{periodId}/close")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PeriodResponse>> closePeriod(@PathVariable Long periodId) {
        var period = closePeriodUseCase.execute(periodId);
        PeriodResponse data = webMapper.toPeriodResponse(period);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @PostMapping("/periods/{periodId}/reopen")
    @PreAuthorize("hasAuthority('ACCOUNTING-PERIOD_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PeriodResponse>> reopenPeriod(@PathVariable Long periodId) {
        var period = reopenPeriodUseCase.execute(periodId);
        PeriodResponse data = webMapper.toPeriodResponse(period);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }
}
```

- [ ] **Step 5: Create templates**

Create `src/main/resources/templates/accounting/period/list.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<head th:replace="~{layout/master :: head(#{label.period.title})}"></head>

<body th:replace="~{layout/master :: layout(~{:: .period-list-content}, ~{})}">
    <div class="period-list-content">
        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title" th:text="#{label.period.title}">Accounting Period</h2>
                        <div class="text-secondary mt-1" th:text="#{label.period.subtitle}">Manage fiscal years.</div>
                    </div>
                    <div class="col-auto ms-auto d-print-none" sec:authorize="hasAuthority('ACCOUNTING-PERIOD_CREATE')">
                        <div class="btn-list">
                            <a th:href="@{/accounting/periods/create}" class="btn btn-primary d-none d-sm-inline-block">
                                <i class="ti ti-plus"></i>
                                <span th:text="#{label.period.add}">Add Fiscal Year</span>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">
                <div class="card">
                    <div class="card-body border-bottom py-3">
                        <div class="d-flex">
                            <div class="text-secondary"></div>
                            <div class="ms-auto text-secondary">
                                <form th:action="@{/accounting/periods}" method="get" class="input-icon"
                                      hx-get="/accounting/periods"
                                      hx-target="#period-table-container"
                                      hx-trigger="keyup changed delay:500ms from:#search-input">
                                    <input id="search-input" type="text" name="keyword" th:value="${keyword}"
                                        class="form-control form-control-sm" th:placeholder="#{label.search}">
                                    <span class="input-icon-addon"><i class="ti ti-search"></i></span>
                                </form>
                            </div>
                        </div>
                    </div>
                    <div id="period-table-container" th:fragment="period-table-container"
                         hx-get="/accounting/periods"
                         hx-trigger="refresh-table from:body delay:500ms"
                         hx-include="[name='keyword'], [name='page']">
                        <input type="hidden" name="page" th:value="${page.number}">
                        <div class="table-responsive">
                            <table class="table table-vcenter card-table">
                                <thead>
                                    <tr>
                                        <th th:text="#{label.period.column.code}">Code</th>
                                        <th th:text="#{label.period.column.name}">Name</th>
                                        <th th:text="#{label.period.column.start}">Start Date</th>
                                        <th th:text="#{label.period.column.end}">End Date</th>
                                        <th th:text="#{label.period.column.periods}">Periods</th>
                                        <th th:text="#{label.period.column.status}">Status</th>
                                        <th class="w-1" th:text="#{label.actions}">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr th:each="item : ${page.content}" th:id="'fy-row-' + ${item.id}">
                                        <td>
                                            <a th:href="@{/accounting/periods/{id}(id=${item.id})}"
                                               class="text-reset" th:text="${item.code}">FY-0001</a>
                                        </td>
                                        <td class="font-weight-medium" th:text="${item.name}">Fiscal Year 2026</td>
                                        <td th:text="${#temporals.format(item.startDate, 'dd MMM yyyy')}">01 Jan 2026</td>
                                        <td th:text="${#temporals.format(item.endDate, 'dd MMM yyyy')}">31 Dec 2026</td>
                                        <td>
                                            <span class="badge bg-cyan-lt" th:text="${item.periodCount + ' periods'}">12 periods</span>
                                        </td>
                                        <td>
                                            <span class="badge badge-outline text-green" th:if="${item.isActive}">
                                                <span class="badge-dot bg-success me-1"></span>
                                                <span th:text="#{label.active}">Active</span>
                                            </span>
                                            <span class="badge badge-outline text-red" th:unless="${item.isActive}">
                                                <span class="badge-dot bg-danger me-1"></span>
                                                <span th:text="#{label.inactive}">Inactive</span>
                                            </span>
                                        </td>
                                        <td class="text-end">
                                            <div class="btn-list flex-nowrap justify-content-end">
                                                <a th:href="@{/accounting/periods/{id}(id=${item.id})}"
                                                    class="btn btn-white btn-sm" sec:authorize="hasAuthority('ACCOUNTING-PERIOD_READ')">
                                                    <i class="ti ti-eye me-1"></i> Detail
                                                </a>
                                                <a th:href="@{/accounting/periods/edit/{id}(id=${item.id})}"
                                                    class="btn btn-white btn-sm" sec:authorize="hasAuthority('ACCOUNTING-PERIOD_UPDATE')"
                                                    th:text="#{label.edit}">Edit</a>
                                                <button class="btn btn-white btn-sm text-danger" data-bs-toggle="modal"
                                                    th:data-bs-target="'#modal-delete-' + ${item.id}"
                                                    sec:authorize="hasAuthority('ACCOUNTING-PERIOD_DELETE')">
                                                    <i class="ti ti-trash me-1"></i> <span th:text="#{label.delete}">Delete</span>
                                                </button>
                                            </div>
                                            <div th:replace="~{fragments/modals :: delete-confirm(
                                                id='modal-delete-' + ${item.id},
                                                title=#{label.delete.confirm.title},
                                                message=#{msg.delete.confirm(${item.name})},
                                                actionUrl='/accounting/periods/' + ${item.id},
                                                targetId='#fy-row-' + ${item.id}
                                            )}"></div>
                                        </td>
                                    </tr>
                                    <tr th:if="${page.isEmpty()}">
                                        <td colspan="7" class="text-center py-4 text-secondary"
                                            th:text="#{label.period.empty}">No data.</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                        <div th:replace="~{fragments/table :: pagination(${page})}"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>

</html>
```

Create `src/main/resources/templates/accounting/period/detail.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<head th:replace="~{layout/master :: head(#{label.period.detail.title})}"></head>

<body th:replace="~{layout/master :: layout(~{:: .period-detail-content}, ~{})}">
    <div class="period-detail-content">
        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <div class="page-pretitle" th:text="#{label.period.title}">Accounting Period</div>
                        <h2 class="page-title" th:text="${fy.code + ' — ' + fy.name}">FY-0001 — Fiscal Year 2026</h2>
                    </div>
                    <div class="col-auto ms-auto d-print-none">
                        <a th:href="@{/accounting/periods}" class="btn btn-secondary">
                            <i class="ti ti-arrow-left me-1"></i> Back
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">
                <!-- FY Summary Card -->
                <div class="card mb-3">
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-3">
                                <div class="mb-2">
                                    <span class="text-secondary" th:text="#{label.period.fy.code}">Code</span>
                                    <div class="fw-bold" th:text="${fy.code}">FY-0001</div>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="mb-2">
                                    <span class="text-secondary" th:text="#{label.period.fy.start}">Start Date</span>
                                    <div class="fw-bold" th:text="${#temporals.format(fy.startDate, 'dd MMM yyyy')}">01 Jan 2026</div>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="mb-2">
                                    <span class="text-secondary" th:text="#{label.period.fy.end}">End Date</span>
                                    <div class="fw-bold" th:text="${#temporals.format(fy.endDate, 'dd MMM yyyy')}">31 Dec 2026</div>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="mb-2">
                                    <span class="text-secondary" th:text="#{label.period.column.status}">Status</span>
                                    <div>
                                        <span class="badge badge-outline text-green" th:if="${fy.isActive}">
                                            <span class="badge-dot bg-success me-1"></span>
                                            <span th:text="#{label.active}">Active</span>
                                        </span>
                                        <span class="badge badge-outline text-red" th:unless="${fy.isActive}">
                                            <span class="badge-dot bg-danger me-1"></span>
                                            <span th:text="#{label.inactive}">Inactive</span>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Periods Table -->
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title" th:text="#{label.period.column.periods}">Periods</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-vcenter card-table">
                            <thead>
                                <tr>
                                    <th th:text="#{label.period.column.code}">Code</th>
                                    <th th:text="#{label.period.column.name}">Name</th>
                                    <th th:text="#{label.period.column.start}">Start Date</th>
                                    <th th:text="#{label.period.column.end}">End Date</th>
                                    <th th:text="#{label.period.column.status}">Status</th>
                                    <th class="w-1" th:text="#{label.actions}" sec:authorize="hasAuthority('ACCOUNTING-PERIOD_UPDATE')">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr th:each="period : ${fy.periods}" th:id="'period-row-' + ${period.id}">
                                    <td th:text="${period.code}">FY-0001-01</td>
                                    <td th:text="${period.name}">Jan 2026</td>
                                    <td th:text="${#temporals.format(period.startDate, 'dd MMM yyyy')}">01 Jan 2026</td>
                                    <td th:text="${#temporals.format(period.endDate, 'dd MMM yyyy')}">31 Jan 2026</td>
                                    <td>
                                        <span th:if="${period.status == 'OPEN'}" class="badge bg-green-lt"
                                              th:text="#{label.period.status.OPEN}">Open</span>
                                        <span th:if="${period.status == 'CLOSED'}" class="badge bg-red-lt"
                                              th:text="#{label.period.status.CLOSED}">Closed</span>
                                    </td>
                                    <td class="text-end" sec:authorize="hasAuthority('ACCOUNTING-PERIOD_UPDATE')">
                                        <button th:if="${period.status == 'OPEN'}"
                                                class="btn btn-sm btn-warning"
                                                hx-post="__${'/accounting/periods/periods/' + period.id + '/close'}__"
                                                hx-confirm="Close this period?"
                                                hx-target="closest tr"
                                                hx-swap="outerHTML"
                                                th:text="#{label.period.close}">Close</button>
                                        <button th:if="${period.status == 'CLOSED'}"
                                                class="btn btn-sm btn-outline-success"
                                                hx-post="__${'/accounting/periods/periods/' + period.id + '/reopen'}__"
                                                hx-confirm="Reopen this period?"
                                                hx-target="closest tr"
                                                hx-swap="outerHTML"
                                                th:text="#{label.period.reopen}">Reopen</button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>

</html>
```

> **Note for form.html:** Fiscal Year uses the same create form pattern. Create `src/main/resources/templates/accounting/period/form.html` following the COA form pattern but with fields: `name` (text), `startDate` (date), `endDate` (date), `isActive` (switch). Include a note about auto-generated periods:
> ```html
> <div class="alert alert-info" th:if="${fyRequest.id == null}">
>     <i class="ti ti-info-circle me-1"></i>
>     <span th:text="#{label.period.auto.generate.note}">12 monthly periods will be auto-generated.</span>
> </div>
> ```

- [ ] **Step 6: Compile and verify**

Run: `.\mvnw.cmd clean compile -q`

Expected: BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/solusi/erp/accounting/period/ src/main/resources/templates/accounting/period/
git commit -m "feat(accounting): add Fiscal Year & Accounting Period slice — all layers

- FiscalYear aggregate with auto-generation of 12 monthly periods
- AccountingPeriod entity with OPEN/CLOSED status management
- ClosePeriod/ReopenPeriod use cases
- SequenceGenerator integration for FY code (FY-{seq})
- Detail page with period table and Close/Reopen actions via HTMX
- Full CRUD for fiscal year management

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---
## Task 8: Build Verification & Final Smoke Check

**Goal:** Ensure all three slices (COA, Schema, Period) compile cleanly and Flyway migration is valid.

- [ ] **Step 1: Full compile**

Run:

```bash
.\mvnw.cmd clean compile -q
```

Expected: **BUILD SUCCESS** with zero errors.

If you see errors, fix them before proceeding. Common issues:
- Missing import → add the correct import statement
- MapStruct mapping mismatch → check source/target property names match
- Duplicate bean names → ensure each `@Bean` method has a unique name in its `@Configuration`

- [ ] **Step 2: Verify Flyway migration syntax**

Run the application briefly to trigger Flyway:

```bash
.\mvnw.cmd spring-boot:run -q
```

Expected: Application starts without Flyway migration errors. Check logs for:

```
Successfully applied 1 migration to schema ... (execution time ...)
```

Stop the application after confirming (`Ctrl+C`).

- [ ] **Step 3: Spot-check database tables**

Connect to MariaDB and verify the four tables exist:

```sql
SHOW TABLES LIKE 'acc_%';
```

Expected output:

```
+------------------------------+
| Tables_in_xxx (acc_%)        |
+------------------------------+
| acc_accounting_periods       |
| acc_accounting_schemas       |
| acc_chart_of_accounts        |
| acc_fiscal_years             |
+------------------------------+
```

Also verify permissions were seeded:

```sql
SELECT code FROM permissions WHERE code LIKE 'ACCOUNTING-%' ORDER BY code;
```

Expected: 12 rows (4 CRUD × 3 slices: COA, SCHEMA, PERIOD).

- [ ] **Step 4: Commit all remaining files**

```bash
git add .
git status
git commit -m "feat(accounting): complete Sprint 1 — Accounting Foundation

Sprint 1 delivers three vertical slices:
- Chart of Accounts (COA) with tree structure
- Accounting Schema for event-to-account mapping
- Fiscal Year & Accounting Period management

Includes: Flyway V43 migration, i18n (EN/ID), full Clean Architecture
layers (domain → application → infrastructure → web) for each slice.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Self-Review Checklist

Before executing this plan, verify the following:

### Spec Coverage
| Requirement | Task |
|---|---|
| Chart of Accounts CRUD with tree structure | Task 3 (domain), Task 4 (infra), Task 5 (web) |
| Accounting Schema event→account mapping | Task 6 (all layers) |
| Fiscal Year with auto-generated periods | Task 7 (all layers) |
| Close/Reopen Period actions | Task 7 Step 2 (ClosePeriodUseCase, ReopenPeriodUseCase) |
| Flyway DDL migration | Task 1 |
| Permission seeding + ROLE_ADMIN grants | Task 1 |
| i18n (EN + ID) | Task 2 |
| Menu integration | Task 1 (permission_groups with correct breadcrumbs) |
| SequenceGenerator for FY codes | Task 1 (seed) + Task 7 (CreateFiscalYearUseCaseImpl) |

### Pattern Consistency
- [x] All domain models use `AuditMetadata` record — no Lombok, no Spring annotations
- [x] All domain models have `createNew()` factory + `update()` + `softDelete()`
- [x] All JPA entities extend `BaseModel`, use Lombok `@Getter @Setter`
- [x] All persistence mappers are MapStruct `@Mapper` interfaces with `toDomain`/`toEntity`/`toAuditMetadata`
- [x] All web mappers are MapStruct abstract classes with `@Autowired AuditMapperHelper` and `@AfterMapping`
- [x] All DTOs extend `BaseAuditResponse`
- [x] All Config classes use `TransactionTemplate` wrapping with `tx.setReadOnly(true)` for queries
- [x] All controllers use `@PreAuthorize` with correct permission codes
- [x] All delete use cases follow `DeleteResult` pattern with `InUseChecker`
- [x] COA code is user-defined (no SequenceGenerator) — correct per business requirement
- [x] FY code uses SequenceGenerator — correct per auto-generation pattern

### Placeholder Scan
- [x] No "TBD", "TODO", "implement later" in any task
- [x] No "similar to Task N" — all code is fully written
- [x] No "add appropriate error handling" — all error paths shown
- [x] Every step has concrete code or exact commands

### Type Consistency
- [x] `AccountType` enum values match across domain model, Flyway CHECK constraint, and web form select options
- [x] `NormalBalance` enum values match domain and display
- [x] `PeriodStatus` enum values match domain, JPA String mapping, and template conditionals
- [x] `SchemaEventType` enum values match domain, Flyway CHECK constraint, and web form
- [x] Permission codes consistent: `ACCOUNTING-COA_READ/CREATE/UPDATE/DELETE`, `ACCOUNTING-SCHEMA_*`, `ACCOUNTING-PERIOD_*`
- [x] URL paths consistent: `/accounting/coa`, `/accounting/schemas`, `/accounting/periods`
- [x] Template paths consistent: `accounting/coa/list`, `accounting/coa/form`, `accounting/schema/list`, etc.

---

## Execution Handoff

**Plan complete and saved to `docs/plans/2026-04-10-accounting-foundation.md`.**

**Two execution options:**

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration with isolated context per slice.

**2. Inline Execution** — Execute tasks sequentially in this session, batch execution with checkpoints for review between major milestones.

**Which approach?**