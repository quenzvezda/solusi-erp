# Solusi ERP — Spesifikasi Bisnis Lengkap (Sprint 1–7)

> **Versi:** 1.0 — Definitive Business Specification
> **Tech Stack:** Java 21 · Spring Boot 4.0.3 · MariaDB · Thymeleaf SSR · HTMX
> **Arsitektur:** Clean Architecture + DDD + CQRS (vertical slicing per fitur, monolith)
> **Target Pasar:** SME Indonesia — Trading, Manufacturing, Service, Retail
> **Base Currency:** IDR | Format Angka: Rp 10.000.000,00 | Tanggal: dd/MM/yyyy
> **Standar Acuan:** PSAK (konvergensi IFRS), SAK-ETAP, UU HPP No. 7/2021

---

## Daftar Isi

1. [Executive Summary](#executive-summary)
2. [Architecture Principles](#architecture-principles)
3. [Cross-Cutting Concerns](#cross-cutting-concerns)
4. [Sprint 1: Accounting Foundation ✅](#sprint-1-accounting-foundation-)
5. [Sprint 2–3: Procurement Operations](#sprint-23-procurement-operations)
6. [Sprint 4: Goods Receipt & Inventory](#sprint-4-goods-receipt--inventory)
7. [Sprint 5: Accounts Payable](#sprint-5-accounts-payable)
8. [Sprint 6: Accounting Core](#sprint-6-accounting-core)
9. [Sprint 7+: Financial Reports](#sprint-7-financial-reports)
10. [Appendix A: Complete COA Template](#appendix-a-complete-coa-template)
11. [Appendix B: Accounting Schema Mapping](#appendix-b-accounting-schema-mapping)
12. [Appendix C: Document Numbering Sequences](#appendix-c-document-numbering-sequences)
13. [Appendix D: System Settings & Feature Flags](#appendix-d-system-settings--feature-flags)
14. [Appendix E: Indonesian Tax Reference](#appendix-e-indonesian-tax-reference)

---

## Executive Summary

Solusi ERP adalah sistem Enterprise Resource Planning monolitik yang dirancang sebagai **template reusable** untuk perusahaan SME Indonesia. Dengan satu codebase dan konfigurasi per-klien (database terpisah, seed data per industri, feature flags), sistem ini dapat di-deploy ke 50+ klien dalam 1–2 minggu per deployment.

Dokumen ini mendeskripsikan siklus **Procure-to-Pay (P2P)** lengkap dalam 7 sprint:

| Sprint | Modul | Status | Deskripsi |
|--------|-------|--------|-----------|
| 1 | Accounting Foundation | ✅ DONE | COA, Accounting Schema, Fiscal Year/Period |
| 2–3 | Procurement Operations | 🔲 | Supplier Price List, PR, PO + Approval |
| 4 | Goods Receipt & Inventory | 🔲 | GR, FIFO Valuation, Stock Balance, Auto-Journal |
| 5 | Accounts Payable | 🔲 | Vendor Bill, 3-Way Matching, Return, Payment |
| 6 | Accounting Core | 🔲 | Manual Journal, GL View, Trial Balance, Period Closing |
| 7+ | Financial Reports | 🔲 | AP Aging, Laba Rugi, Neraca, Arus Kas |

**Prinsip Desain:**
- **"Odoo's UX, SAP's rigor"** — User-friendly interface dengan ketegasan akuntansi
- **"Configuration over code"** — 80% out-of-the-box, 20% konfigurasi (bukan kustomisasi kode)
- **"Indonesian-first"** — PPN 11%, format Rupiah, Bahasa Indonesia default
- **"One database, one JAR, one truth"** — Monolith simplicity

---

## Architecture Principles

### Deployment Model

```
[Solusi ERP Template Codebase]
         │
    ┌────┼────────────────┐
    ▼    ▼                ▼
Client A  Client B     Client C
(Trading) (Service)   (Manufacturing)
    │        │            │
  Config   Config       Config
  + Seed   + Seed       + Seed
  + COA    + COA        + COA
```

**Strategy:** Single codebase, per-client database, per-client `system_settings` + feature flags.

### Integration Pattern

```
┌─────────────────────────────────────────────────────────────────┐
│                   PROCURE-TO-PAY LIFECYCLE                       │
│                                                                   │
│  ┌────┐    ┌────┐    ┌─────────┐    ┌───────┐    ┌─────────┐  │
│  │ PR │───→│ PO │───→│ Goods   │───→│Vendor │───→│ Vendor  │  │
│  │    │    │    │    │ Receipt │    │ Bill  │    │ Payment │  │
│  └────┘    └──┬─┘    └────┬────┘    └───┬───┘    └────┬────┘  │
│               │           │             │              │        │
│          ┌────▼───┐  ┌────▼────────┐ ┌──▼─────────┐ ┌─▼──────┐│
│          │Approval│  │ Inventory   │ │ AP Ledger  │ │Bank/   ││
│          │System  │  │ +Movement   │ │ +3-Way     │ │Cash    ││
│          │(Generic│  │ +Valuation  │ │  Match     │ │Ledger  ││
│          │)       │  │ +Stock Bal. │ │            │ │        ││
│          └────────┘  └──────┬──────┘ └─────┬──────┘ └───┬────┘│
│                             │              │             │      │
│                      ┌──────▼──────────────▼─────────────▼───┐ │
│                      │     ACCOUNTING ENGINE (Auto-Journal)   │ │
│                      │ acc_accounting_schemas → Journal Entry  │ │
│                      │ AutoJournalService → Single Entry Point │ │
│                      └──────────────────┬────────────────────┘ │
│                                         │                       │
│                      ┌──────────────────▼────────────────────┐ │
│                      │       FINANCIAL REPORTING              │ │
│                      │ Trial Balance → Laba Rugi → Neraca     │ │
│                      └───────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Technical Patterns

| Pattern | Penjelasan |
|---------|-----------|
| **Event-Driven Decoupling** | Modul berkomunikasi via Spring Application Events (e.g., `GoodsReceiptCompletedEvent`) |
| **AutoJournalService** | Single entry point untuk semua auto-journal. Lookup schema → create journal → validate period |
| **Period Guard** | Semua journal posting di-validate terhadap `acc_accounting_periods.status = OPEN` |
| **Polymorphic Approval** | `appr_requests` table dengan `referenceType` + `referenceId` pattern |
| **Optimistic Locking** | `@Version` pada semua entity via BaseModel |
| **Soft Delete** | `is_active` flag — DILARANG hard delete untuk data master |

---

## Cross-Cutting Concerns

### 3.1 Approval Workflow

Sistem approval generik yang digunakan oleh PR, PO, dan dokumen lain.

**Tabel:** `appr_requests`

| Field | Tipe | Keterangan |
|-------|------|-----------|
| `id` | BIGINT PK | |
| `reference_type` | VARCHAR(50) | `PURCHASE_REQUISITION`, `PURCHASE_ORDER`, dll |
| `reference_id` | BIGINT | ID dokumen yang di-approve |
| `requester_id` | BIGINT FK→users | Yang mengajukan |
| `approver_id` | BIGINT FK→users | Yang meng-approve |
| `status` | VARCHAR(20) | `PENDING → APPROVED / REJECTED` |
| `note` | TEXT | Catatan approval/rejection |
| + audit fields | | |

**Configurable Approval Thresholds:**
```
system_settings:
  procurement.po.approval_thresholds = [
    {"max_amount": 50000000, "approver_role": "AUTO_APPROVE"},
    {"max_amount": 500000000, "approver_role": "PROCUREMENT_MANAGER"},
    {"max_amount": null, "approver_role": "FINANCE_DIRECTOR"}
  ]
```

- PO ≤ Rp 50 juta → Auto-approve (tanpa approval step)
- PO > Rp 50 juta → Single approver (Procurement Manager)
- PO > Rp 500 juta → Sequential approval (Manager → Director)

**Event Pattern:**
```
Source: PO Module
Event: ApprovalRequestedEvent("PURCHASE_ORDER", poId, requesterId, approverId)
Target: Approval System → create appr_requests row
Return: ApprovalCompletedEvent → PO status → APPROVED
        ApprovalRejectedEvent → PO status → CANCELLED
```

### 3.2 Document Numbering (Sequences)

Semua dokumen bisnis menggunakan `SequenceGeneratorService` dengan format configurable.

| Dokumen | Pattern Default | Pad | Reset |
|---------|----------------|-----|-------|
| Fiscal Year | `FY-{seq}` | 4 | NEVER |
| Price List | `SPL-{date:yyMM}-{seq}` | 5 | MONTHLY |
| Purchase Requisition | `PR-{date:yyyyMM}-{seq}` | 5 | MONTHLY |
| Purchase Order | `PO-{date:yyyyMM}-{seq}` | 5 | MONTHLY |
| Goods Receipt | `GR-{date:yyyyMM}-{seq}` | 5 | MONTHLY |
| Vendor Bill | `BILL-{date:yyyyMM}-{seq}` | 5 | MONTHLY |
| Vendor Payment | `PAY-{date:yyyyMM}-{seq}` | 5 | MONTHLY |
| Purchase Return | `PRTN-{date:yyyyMM}-{seq}` | 5 | MONTHLY |
| Journal Entry (Manual) | `JV-{date:yyyyMM}-{seq}` | 5 | MONTHLY |
| Journal Entry (Auto) | `AJ-{date:yyyyMM}-{seq}` | 5 | MONTHLY |

**Implementasi:**
- Row-level lock: `SELECT ... FROM system_sequences WHERE module_code = ? FOR UPDATE`
- `@Transactional(propagation = REQUIRES_NEW)` untuk atomicity
- Retry on sequence failure (1x after re-read)
- Configurable via UI: System > Document Sequences

### 3.3 Audit Trail

Semua entity extends `BaseModel` dengan:
- `createdBy`, `createdDate` (`@CreatedBy`, `@CreatedDate`)
- `updatedBy`, `updatedDate` (`@LastModifiedBy`, `@LastModifiedDate`)
- `version` (`@Version` — optimistic locking)

**High-Risk Operations (Audit Log wajib):**

| Operasi | Severity | Detail Log |
|---------|----------|-----------|
| Period reopen (CLOSED→OPEN) | 🔴 HIGH | User, timestamp, period, reason |
| PO > Rp 500 juta | 🟡 MEDIUM | User, PO code, amount |
| GR over-receipt (dalam tolerance) | 🟡 MEDIUM | User, GR code, variance % |
| Bill price variance (dalam tolerance) | 🟡 MEDIUM | User, Bill code, variance % |
| Journal reversal | 🔴 HIGH | User, original journal, reversal journal, reason |
| COA deactivation | 🟡 MEDIUM | User, account code/name |

### 3.4 Permission Model

Matriks peran per sprint:

| Persona | Sprint 1 | Sprint 2–3 | Sprint 4 | Sprint 5 | Sprint 6 | Sprint 7 |
|---------|----------|-----------|----------|----------|----------|----------|
| **Admin** | Setup COA, Schema, FY | Kelola Price List | — | — | — | — |
| **Purchasing Officer** | — | Buat PR, PO | — | Verifikasi harga bill | — | — |
| **Procurement Manager** | — | Approve PR & PO | — | Review AP | — | Lihat AP Aging |
| **Warehouse Staff** | — | — | Buat GR | — | — | — |
| **Accountant (AP Staff)** | Lihat COA | — | Lihat auto-journal GR | Buat Bill, match 3-way | Buat manual journal | Generate reports |
| **Finance Manager** | Lihat COA | Lihat outstanding PO | — | Approve payment | Review TB | Analisis laporan |

### 3.5 Feature Flags & System Settings

Setiap fitur dievaluasi terhadap 4 tipe bisnis:

| Simbol | Arti |
|--------|------|
| ● | Core — wajib ada |
| ◐ | Useful — biasanya dipakai |
| ○ | Optional — bisa di-toggle off |
| — | Not applicable |

**Global Feature Flags:**

| Flag | Default | Deskripsi |
|------|---------|-----------|
| `FEATURE_PURCHASE_REQUISITION` | `true` | Seluruh modul PR |
| `FEATURE_RFQ` | `false` | Request for Quotation sebelum PO |
| `FEATURE_MULTI_CURRENCY_PO` | `false` | PO dalam mata uang asing |
| `FEATURE_ADVANCE_PAYMENT` | `false` | Uang muka (DP) ke supplier |
| `FEATURE_SERIAL_TRACKING` | `false` | Serial number tracking per unit |
| `FEATURE_BATCH_TRACKING` | `false` | Batch/lot tracking |
| `FEATURE_QC_INSPECTION` | `false` | Quality control sebelum GR confirm |
| `FEATURE_WITHHOLDING_TAX` | `true` | PPh 23/4(2) withholding |
| `FEATURE_ADJUSTMENT_PERIOD` | `false` | Period 13 untuk year-end adjustments |
| `FEATURE_COST_CENTER` | `false` | Departmental accounting |

### 3.6 Indonesian Tax Framework (PPN, PPh, e-Faktur)

#### Jenis Pajak yang Didukung

| Jenis | Tarif | Dasar Hitung | Saat Potong/Pungut | Akun |
|-------|-------|-------------|-------------------|------|
| **PPN** | 11% | DPP (harga sebelum pajak) | Saat GR (accrual) | 1170 (Masukan), 2160 (Keluaran) |
| **PPh 23** | 2% | DPP (jasa profesional) | Saat pembayaran | 2170 PPh 23 Terutang |
| **PPh 4(2)** | 10% (sewa), 2-6% (konstruksi) | DPP | Saat pembayaran | 2175 PPh 4(2) Terutang |
| **PPh 22** | 2.5% (impor normal) | CIF + Bea Masuk | Saat impor/GR | 1175 Prepaid PPh 22 |

#### Status PKP (Pengusaha Kena Pajak)

- Party (Supplier/Customer) harus memiliki flag `is_pkp` (BOOLEAN DEFAULT FALSE)
- Jika supplier **PKP** → PO line wajib hitung PPN 11%, Faktur Pajak wajib
- Jika supplier **non-PKP** → PPN = 0, tidak ada Faktur Pajak
- Threshold PKP: omzet > Rp 4,8 miliar/tahun

#### Format Faktur Pajak (NSFP)

```
XXX-XXX.XX.XXXXXXXX
│   │   │  └─ 8 digit: Nomor Urut
│   │   └──── 2 digit: Tahun Pajak
│   └──────── 3 digit: Kode Cabang
└──────────── 3 digit: Kode Transaksi (010=normal, 020=bendahara, 040=DPP Nilai Lain)
```

#### Kurs Pajak vs Kurs BI

| Kurs | Sumber | Update | Penggunaan |
|------|--------|--------|-----------|
| Kurs Tengah BI | Bank Indonesia | Harian | Akuntansi harian, PO |
| Kurs Pajak (KMK) | Kementerian Keuangan | Mingguan (Rabu) | Faktur Pajak, pelaporan pajak |

---

## Sprint 1: Accounting Foundation ✅

### Sprint Goal

Membangun pondasi General Ledger (COA, Accounting Schema, Fiscal Year/Period) yang menjadi prasyarat auto-journaling dan pelaporan keuangan di seluruh modul transaksional.

### Status: ✅ SELESAI

Seluruh fitur Sprint 1 telah diimplementasikan — termasuk migrasi database, domain model, use case, persistence, dan UI Thymeleaf.

### 1.1 Chart of Accounts (COA)

**Tabel:** `acc_chart_of_accounts`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | |
| `code` | VARCHAR(20) | UNIQUE, NOT NULL | Kode akun (e.g., 1110) |
| `name` | VARCHAR(150) | NOT NULL | Nama akun |
| `account_type` | VARCHAR(20) | NOT NULL | `ASSET`, `LIABILITY`, `EQUITY`, `REVENUE`, `EXPENSE` |
| `normal_balance` | VARCHAR(10) | NOT NULL | `DEBIT`, `CREDIT` |
| `parent_id` | BIGINT | FK→self, NULLABLE | Hierarki parent-child |
| `level` | INT | DEFAULT 1 | 1=header group, 2=sub-group, 3=detail |
| `is_header` | BOOLEAN | DEFAULT FALSE | Header tidak boleh diposting |
| `is_active` | BOOLEAN | DEFAULT TRUE | Soft delete |
| + audit fields | | | version, created_by, created_date, updated_by, updated_date |

**DB Constraint:** `CHECK (id != parent_id)` — mencegah self-reference

**Business Rules:**
1. Akun header (`is_header = true`) tidak boleh menjadi target posting jurnal
2. Hierarki maksimal 3 level (configurable via `accounting.coa.max_levels`)
3. Kode akun immutable setelah digunakan dalam jurnal
4. Normal balance otomatis: ASSET/EXPENSE → DEBIT, LIABILITY/EQUITY/REVENUE → CREDIT
5. Deaktivasi parent dengan children aktif → **DITOLAK** (`coa.error.has-active-children`)
6. Delete akun yang sudah dipakai di jurnal → soft delete saja (`is_active = false`)
7. **Circular parent check**: validasi rekursif, traverse parent chain, tolak jika loop terdeteksi (`coa.error.circular-parent`)

**Status Flow:**
```
Active ──deactivate──→ Inactive
  ↑                      │
  └────activate──────────┘
  
(Delete hanya jika belum pernah dipakai di jurnal)
```

### 1.2 Accounting Schema

**Tabel:** `acc_accounting_schemas`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `event_type` | VARCHAR(50) | NOT NULL | Enum `SchemaEventType` |
| `description` | VARCHAR(255) | NULLABLE | Deskripsi event |
| `debit_account_id` | BIGINT | FK→acc_chart_of_accounts | Akun yang di-debit |
| `credit_account_id` | BIGINT | FK→acc_chart_of_accounts | Akun yang di-kredit |
| `is_active` | BOOLEAN | DEFAULT TRUE | |
| + audit fields | | | |

**Unique Key:** `(event_type, is_active)` — hanya satu schema aktif per event type.

**SchemaEventType Enum (Current):**
```java
public enum SchemaEventType {
    GOODS_RECEIPT,           // Sprint 4: DR Inventory, CR GR/IR Clearing
    VENDOR_BILL,             // Sprint 5: DR GR/IR Clearing, CR AP
    VENDOR_PAYMENT,          // Sprint 5: DR AP, CR Bank
    PURCHASE_RETURN,         // Sprint 5: DR GR/IR Clearing, CR Inventory
    CUSTOMER_INVOICE,        // Future O2C: DR AR, CR Revenue
    GOODS_ISSUE,             // Future O2C: DR COGS, CR Inventory
    CUSTOMER_RECEIPT,        // Future O2C: DR Bank, CR AR
    STOCK_ADJUSTMENT_IN,     // DR Inventory, CR Adj Gain
    STOCK_ADJUSTMENT_OUT     // DR Adj Loss, CR Inventory
}
```

### 1.3 Fiscal Year & Accounting Period

**Tabel:** `acc_fiscal_years`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `code` | VARCHAR(20) | UNIQUE | Auto: `FY-{seq}` pad 4 |
| `name` | VARCHAR(100) | NOT NULL | Nama tahun fiskal |
| `start_date` | DATE | NOT NULL | Tanggal mulai |
| `end_date` | DATE | NOT NULL | Tanggal berakhir |
| `is_active` | BOOLEAN | DEFAULT TRUE | |
| + audit fields | | | |

**Tabel:** `acc_accounting_periods`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `code` | VARCHAR(20) | UNIQUE | Auto-generated |
| `name` | VARCHAR(100) | NOT NULL | |
| `fiscal_year_id` | BIGINT | FK→acc_fiscal_years | |
| `start_date` | DATE | NOT NULL | |
| `end_date` | DATE | NOT NULL | |
| `status` | VARCHAR(20) | NOT NULL | `NEVER_OPENED`, `OPEN`, `CLOSED` |
| `period_number` | INT | NOT NULL | 1–12 (atau 13 jika adjustment period) |
| + audit fields | | | |

**Status Flow:**
```
NEVER_OPENED ──open──→ OPEN ──close──→ CLOSED
                         ↑                │
                         └──reopen────────┘
                         (ACCOUNTING_ADMIN only, with audit log)
```

**Business Rules:**
- 12 periods auto-generated saat Fiscal Year dibuat
- Tidak boleh ada 2 fiscal year overlapping (boundary check menggunakan DATE only)
- Jurnal hanya bisa diposting ke period `OPEN`
- Reopen period: hanya `ACCOUNTING_ADMIN`, semua journal baru mendapat flag `is_adjustment = true`
- Default fiscal year = Januari–Desember (sesuai tahun pajak Indonesia)

### 1.4 Akun Tambahan yang WAJIB untuk Sprint 2–7

**11+ akun yang harus ditambahkan** agar seluruh siklus P2P dan pelaporan berfungsi:

| Code | Nama | Tipe | Normal | Kegunaan |
|------|------|------|--------|----------|
| **1170** | PPN Masukan (VAT Input) | ASSET | DEBIT | PPN dibayar ke supplier PKP |
| **1175** | Prepaid PPh 22 | ASSET | DEBIT | PPh 22 impor (dikreditkan di SPT Tahunan) |
| **1180** | Uang Muka Pembelian | ASSET | DEBIT | Down payment ke supplier |
| **2150** | GR/IR Clearing | LIABILITY | CREDIT | Penampung sementara GR↔Bill |
| **2160** | PPN Keluaran (VAT Output) | LIABILITY | CREDIT | PPN dipungut dari customer |
| **2170** | PPh 23 Terutang | LIABILITY | CREDIT | Withholding tax atas jasa (2%) |
| **2175** | PPh 4(2) Terutang | LIABILITY | CREDIT | Final tax (sewa 10%, konstruksi 2-6%) |
| **2180** | PPh 22 Terutang | LIABILITY | CREDIT | WHT pembelian barang |
| **5130** | Purchase Price Variance | EXPENSE | DEBIT | Selisih harga PO vs Bill |
| **5140** | Inventory Adjustment Loss | EXPENSE | DEBIT | Stock opname negatif |
| **4140** | Inventory Adjustment Gain | REVENUE | CREDIT | Stock opname positif |
| **8110** | Laba Selisih Kurs | REVENUE | CREDIT | Forex gain |
| **8210** | Rugi Selisih Kurs | EXPENSE | DEBIT | Forex loss |
| **9110** | Income Summary | EQUITY | CREDIT | Year-end closing entries |

**Header accounts baru yang diperlukan:**

| Code | Nama | Tipe | Level | Header |
|------|------|------|-------|--------|
| 8000 | OTHER INCOME/EXPENSE | REVENUE | 1 | ✓ |
| 8100 | Other Income | REVENUE | 2 | ✓ |
| 8200 | Other Expense | EXPENSE | 2 | ✓ |
| 9000 | CLOSING ACCOUNTS | EQUITY | 1 | ✓ |
| 9100 | Period Close | EQUITY | 2 | ✓ |

---

## Sprint 2–3: Procurement Operations

### Sprint Goal

Mengimplementasikan siklus pengadaan dari permintaan internal hingga pemesanan ke supplier — mencakup Supplier Price List, Purchase Requisition (PR), dan Purchase Order (PO) dengan approval workflow. Sprint ini murni operasional — **belum menghasilkan jurnal akuntansi** (PO adalah komitmen, bukan transaksi).

### Dependencies
- ✅ Master Data: Party (role SUPPLIER, flag `is_pkp`), Product, UOM, Currency, Tax
- ✅ Inventory: Facility, Product Catalog
- ✅ Approval System (Generic Engine)
- ✅ Accounting Foundation (COA, Schema — untuk validasi period saja)

---

### 2.1 Supplier Price List

**Tabel:** `pur_supplier_price_lists`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | |
| `code` | VARCHAR(30) | UK, NOT NULL | Auto: `SPL-{yyMM}-{seq}` |
| `supplier_id` | BIGINT | FK→parties, NOT NULL | Supplier (Party role=SUPPLIER) |
| `product_id` | BIGINT | FK→inv_products, NOT NULL | Produk |
| `uom_id` | BIGINT | FK→inv_uoms, NOT NULL | Satuan harga |
| `currency_id` | BIGINT | FK→currencies, NOT NULL | Mata uang |
| `unit_price` | DECIMAL(19,4) | NOT NULL, > 0 | Harga satuan |
| `min_quantity` | DECIMAL(19,4) | DEFAULT 1 | Qty minimum untuk harga berlaku |
| `effective_from` | DATE | NOT NULL | Tanggal mulai berlaku |
| `effective_to` | DATE | NULLABLE | Tanggal berakhir (null = selamanya) |
| `note` | TEXT | NULLABLE | |
| `is_active` | BOOLEAN | DEFAULT TRUE | Soft delete |
| + audit fields | | | |

**Unique Constraint:** `(supplier_id, product_id, uom_id, currency_id, effective_from)`

**Business Rules:**
1. `effective_from` ≤ `effective_to` (jika diisi)
2. Tidak boleh ada overlap periode untuk kombinasi yang sama
3. Hanya price list aktif dan valid yang masuk auto-fill PO
4. `unit_price` > 0
5. Price resolution priority: (1) Active Price List, (2) Last Purchase Price dari PO terakhir, (3) Manual entry (dengan warning)

**Edge Cases:**
- Expired price list (`valid_to < today`) → tampilkan warning di autocomplete, boleh manual override
- Query aktif: `WHERE valid_from <= :today AND (valid_to IS NULL OR valid_to >= :today) AND is_active = true`

**Per-Business Type:** ● Trading, ● Manufacturing, ○ Service, ● Retail

---

### 2.2 Purchase Requisition (PR)

**Feature Flag:** `FEATURE_PURCHASE_REQUISITION` (default: `true`)

**Tabel:** `pur_purchase_requisitions`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `code` | VARCHAR(30) | UK, NOT NULL | Auto: `PR-{yyyyMM}-{seq}` |
| `request_date` | DATE | NOT NULL | |
| `requester_id` | BIGINT | FK→users | Yang mengajukan |
| `facility_id` | BIGINT | FK→inv_facilities | Gudang tujuan |
| `department` | VARCHAR(50) | NULLABLE | Departemen peminta |
| `priority` | VARCHAR(10) | NOT NULL | `LOW`, `NORMAL`, `HIGH`, `URGENT` |
| `status` | VARCHAR(20) | NOT NULL | Lihat state machine |
| `note` | TEXT | NULLABLE | |
| `is_active` | BOOLEAN | DEFAULT TRUE | |
| + audit fields | | | |

**Tabel:** `pur_purchase_requisition_lines`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `header_id` | BIGINT | FK→pur_purchase_requisitions | |
| `product_id` | BIGINT | FK→inv_products | |
| `quantity` | DECIMAL(19,4) | NOT NULL, > 0 | Qty yang diminta |
| `uom_id` | BIGINT | FK→inv_uoms | Satuan |
| `required_date` | DATE | NULLABLE | Tanggal dibutuhkan |
| `estimated_unit_price` | DECIMAL(19,4) | NULLABLE | Estimasi dari Price List |
| `suggested_supplier_id` | BIGINT | NULLABLE, FK→parties | Saran supplier |
| `converted_po_line_id` | BIGINT | NULLABLE, FK→pur_po_lines | Link setelah konversi |
| `note` | TEXT | NULLABLE | |
| + audit fields | | | |

**Status Flow:**
```
DRAFT ──submit──→ SUBMITTED ──approve──→ APPROVED ──convert_to_po──→ CONVERTED
  │                  │                       │
  └──cancel──→ CANCELLED ←───reject─────────┘
```

**Business Rules:**
1. PR DRAFT bisa di-edit/delete
2. SUBMITTED → masuk Approval System (`referenceType: PURCHASE_REQUISITION`)
3. APPROVED → bisa di-convert ke PO (partial: sebagian line, atau full)
4. Satu PR dapat generate **multiple PO** (split by supplier)
5. Submit membutuhkan minimal 1 line aktif
6. `quantity > 0`, product harus aktif, UOM harus aktif

**Edge Cases:**
- Concurrent submit: optimistic locking prevents double-submit
- Cancel PR yang sudah di-convert: DITOLAK jika ada PO yang sudah APPROVED+
- Auto-suggest supplier: dari price list termurah untuk product tersebut

**Per-Business Type:** ● Trading, ● Manufacturing, ○ Service (toggle off), ◐ Retail

---

### 2.3 Purchase Order (PO)

**Tabel:** `pur_purchase_orders`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `code` | VARCHAR(30) | UK, NOT NULL | Auto: `PO-{yyyyMM}-{seq}` |
| `order_date` | DATE | NOT NULL | Harus dalam period OPEN |
| `expected_date` | DATE | NULLABLE | Estimasi terima barang (≥ order_date) |
| `supplier_id` | BIGINT | FK→parties, NOT NULL | Must have SUPPLIER role, active |
| `facility_id` | BIGINT | FK→inv_facilities | Gudang tujuan |
| `currency_id` | BIGINT | FK→currencies, NOT NULL | Mata uang transaksi |
| `exchange_rate` | DECIMAL(19,6) | NOT NULL, > 0 | Kurs ke IDR saat PO dibuat |
| `subtotal` | DECIMAL(19,4) | | Σ line_subtotal |
| `tax_amount` | DECIMAL(19,4) | | PPN jika supplier PKP |
| `total_amount` | DECIMAL(19,4) | | subtotal + tax_amount |
| `status` | VARCHAR(30) | NOT NULL | Lihat state machine |
| `payment_term_days` | INT | DEFAULT 30 | Jatuh tempo pembayaran |
| `pr_id` | BIGINT | NULLABLE, FK→pur_purchase_requisitions | Source PR |
| `note` | TEXT | NULLABLE | |
| `is_active` | BOOLEAN | DEFAULT TRUE | |
| + audit fields | | | |

**Tabel:** `pur_purchase_order_lines`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `header_id` | BIGINT | FK→pur_purchase_orders | |
| `product_id` | BIGINT | FK→inv_products, NOT NULL | Product harus aktif |
| `quantity` | DECIMAL(19,4) | NOT NULL, > 0 | Qty order |
| `received_quantity` | DECIMAL(19,4) | DEFAULT 0 | Updated by GR |
| `uom_id` | BIGINT | FK→inv_uoms, NOT NULL | UOM harus aktif |
| `unit_price` | DECIMAL(19,4) | NOT NULL, > 0 | Harga satuan |
| `tax_rate` | DECIMAL(5,4) | DEFAULT 0.11 | 11% jika supplier PKP |
| `line_subtotal` | DECIMAL(19,4) | | qty × unit_price |
| `line_tax` | DECIMAL(19,4) | | line_subtotal × tax_rate |
| `line_total` | DECIMAL(19,4) | | line_subtotal + line_tax |
| `pr_line_id` | BIGINT | NULLABLE, FK | Source PR line |
| `note` | TEXT | NULLABLE | |
| + audit fields | | | |

**Status Flow:**
```
DRAFT ──submit──→ SUBMITTED ──approve──→ APPROVED ──send──→ SENT
  │                  │                                 │
  │                  │                          ┌──────┴──────────┐
  │                  │                     partial_gr         full_gr
  │                  │                          │                 │
  │                  │                  PARTIALLY_RECEIVED    FULLY_RECEIVED
  │                  │                                            │
  │                  │                                       ──→ BILLED ──→ CLOSED
  │                  │
  └──cancel──→ CANCELLED ←──reject──┘

Force Close: PARTIALLY_RECEIVED/FULLY_RECEIVED ──force_close──→ CLOSED
             (sisa backorder diabaikan, harus dicatat reason)
```

**Business Rules:**
1. **PO belum menghasilkan jurnal** — PO adalah komitmen off-balance-sheet
2. PO Approval wajib (configurable threshold) sebelum APPROVED
3. Harga di PO = dasar GR/IR Clearing dan baseline price variance
4. Multi-currency: `currency_id + exchange_rate + amount original currency`
5. PPN 11%: dihitung per line jika supplier PKP (`party.is_pkp = true`)
6. PO tidak bisa diedit setelah status ≥ SUBMITTED (redirect ke view-only)
7. PO yang sudah ada GR COMPLETED tidak bisa di-cancel (`po.error.has-completed-gr`)
8. Price auto-lookup dari `pur_supplier_price_lists` saat add PO line
9. `order_date` harus dalam accounting period OPEN

**PPN Calculation:**
```
If Supplier PKP:
  line_subtotal = qty_ordered × unit_price
  line_tax = line_subtotal × 0.11 (PPN 11%)
  line_total = line_subtotal + line_tax
  DPP = Σ line_subtotal, PPN = Σ line_tax, Total PO = DPP + PPN

If Supplier non-PKP:
  line_tax = 0
  line_total = line_subtotal
```

**Edge Cases:**
- Sequence lock: row-level lock on `system_sequences WHERE module_code = 'PO'`
- Cannot change base UOM of product jika sudah dipakai di transaksi (`product.error.uom-change-blocked`)
- Supplier harus aktif saat PO creation
- Cannot deactivate supplier with open POs

**Smart Buttons (Odoo Pattern):**
```html
<!-- PO Detail: Document Flow Section -->
<div class="btn-list">
  <a href="/goods-receipts?poId=XXX" class="btn btn-outline">
    <i class="icon-package"></i> Receipts <badge>3</badge>
  </a>
  <a href="/vendor-bills?poId=XXX" class="btn btn-outline">
    <i class="icon-invoice"></i> Bills <badge>1</badge>
  </a>
  <a href="/payments?poId=XXX" class="btn btn-outline">
    <i class="icon-cash"></i> Payments <badge>1</badge>
  </a>
</div>
```

**Per-Business Type:** ● Trading (high-volume), ● Manufacturing (raw material), ◐ Service (subcontractor), ● Retail (replenishment)

---

### 2.4 Integration Points (Sprint 2–3)

| Source | Event | Target | Data | Action |
|--------|-------|--------|------|--------|
| PR | `ApprovalRequestedEvent` | Approval | refType=PR, refId, requesterId | Create approval request |
| Approval | `ApprovalCompletedEvent` | PR | refType, refId | PR status → APPROVED |
| PR | Convert action | PO | PR lines data | Create PO DRAFT with PR reference |
| PO | `ApprovalRequestedEvent` | Approval | refType=PO, refId, requesterId | Create approval request |
| Approval | `ApprovalCompletedEvent` | PO | refType, refId | PO status → APPROVED |

**Financial Statement Impact:**

| Event | Balance Sheet | Income Statement | Cash Flow |
|-------|--------------|-----------------|-----------|
| PO Created/Approved | **Tidak ada** (off-balance sheet commitment) | Tidak ada | Tidak ada |

> **Catatan Auditor:** PO APPROVED yang belum received harus di-disclose di CaLK (Catatan atas Laporan Keuangan) jika material.

---

## Sprint 4: Goods Receipt & Inventory

### Sprint Goal

Goods Receipt adalah **titik pengakuan aset persediaan** (inventory recognition). Saat barang diterima, muncul kewajiban melalui GR/IR Clearing account — bukan langsung ke AP, karena invoice belum diterima. GR memicu: (1) stock balance update, (2) FIFO valuation layer creation, (3) inventory movement record, (4) auto-journal entry.

### Dependencies
- ✅ Sprint 2–3: Purchase Order (APPROVED status)
- ✅ Inventory: Facility, Grid, Container, StockBalance, ValuationLayer, InventoryMovement
- ✅ Accounting Foundation: COA (1150, 1170, 2150), Schema (GOODS_RECEIPT)

---

### 4.1 Goods Receipt

**Tabel:** `pur_goods_receipts`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `code` | VARCHAR(30) | UK, NOT NULL | Auto: `GR-{yyyyMM}-{seq}` |
| `receipt_date` | DATE | NOT NULL | Tanggal terima fisik |
| `po_id` | BIGINT | FK→pur_purchase_orders | Source PO |
| `supplier_id` | BIGINT | | Copied from PO |
| `facility_id` | BIGINT | FK→inv_facilities | Gudang penerima |
| `currency_id` | BIGINT | | Dari PO |
| `exchange_rate` | DECIMAL(19,6) | | Kurs saat GR (bisa beda dari PO) |
| `status` | VARCHAR(20) | NOT NULL | `DRAFT`, `COMPLETED` |
| `note` | TEXT | NULLABLE | |
| `is_active` | BOOLEAN | DEFAULT TRUE | |
| + audit fields | | | |

**Tabel:** `pur_goods_receipt_lines`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `header_id` | BIGINT | FK→pur_goods_receipts | |
| `po_line_id` | BIGINT | FK→pur_purchase_order_lines | Reference PO line |
| `product_id` | BIGINT | FK→inv_products | |
| `quantity` | DECIMAL(19,4) | NOT NULL, > 0 | Qty diterima (UOM input) |
| `uom_id` | BIGINT | FK→inv_uoms | UOM input user |
| `unit_price` | DECIMAL(19,4) | | Harga dari PO line |
| `grid_id` | BIGINT | NULLABLE, FK→inv_grids | Zona put-away |
| `container_id` | BIGINT | NULLABLE, FK→inv_containers | Bin lokasi |
| `note` | TEXT | NULLABLE | |
| + audit fields | | | |

**Status Flow:**
```
DRAFT ──complete──→ COMPLETED (irreversible)
  │
  └──delete──→ (removed, hanya jika masih DRAFT)
```

> GR **tidak memiliki approval step** — sudah di-approve di level PO. Status simpel: DRAFT → COMPLETED.

**Business Rules:**
1. GR hanya bisa dibuat dari PO status `APPROVED`, `SENT`, atau `PARTIALLY_RECEIVED`
2. GR line qty ≤ PO line outstanding qty (qty_ordered - qty_received)
3. Over-receipt tolerance: configurable (`gr.over_receipt_tolerance_pct = 10`)
   - Dalam tolerance → warning + audit log
   - Exceeds tolerance → reject (`gr.error.exceeds-tolerance`)
4. `receipt_date` harus dalam accounting period OPEN
5. Minimal 1 line sebelum complete
6. Product harus aktif, UOM harus aktif

---

### 4.2 FIFO Valuation

**Inventory Domain Model:**

```java
StockBalance {
    productId, containerId, serialNumber,
    quantity,           // On-Hand (fisik di gudang)
    reservedQuantity,   // Committed to orders (future SO)
    inTransitQuantity   // Dalam perjalanan antar-facility
}
// Available = quantity - reservedQuantity

ValuationLayer {
    productId, containerId, serialNumber,
    initialQuantity,     // Qty saat layer dibuat
    remainingQuantity,   // Qty tersisa (dikurangi saat ISSUE/COGS)
    unitCost: {
        currencyId, exchangeRate,
        originalAmount,   // Harga dalam mata uang PO
        localAmount       // Harga dalam IDR
    }
}
```

**FIFO Rule:** Saat stock keluar (ISSUE/COGS), layer dengan `remainingQuantity > 0` dikonsumsi dari **yang paling lama dibuat** (oldest `createdDate` first).

**UOM Conversion:** Terjadi saat GR — `stock_qty = input_qty × conversionFactor`
- Contoh: PO 10 BOX, conversionFactor BOX→PCS = 12, maka stock = 120 PCS
- ConversionFactor di-resolve per product (`UomConversion.productId`)

---

### 4.3 Proses GR Completion (Atomik)

**Saat GR status → COMPLETED, 5 hal terjadi dalam SATU `@Transactional` boundary:**

**Langkah 1 — Pre-validation (fail-fast):**
```
✓ Period OPEN pada receipt_date
✓ COA accounts aktif (1150, 1170, 2150)
✓ PO status valid (APPROVED/SENT/PARTIALLY_RECEIVED)
✓ Qty validation per line
```

**Langkah 2 — Stock Balance Update:**
```
Untuk setiap GR line:
  converted_qty = quantity × UOM conversionFactor
  StockBalance.applyMovement(RECEIPT, converted_qty)
  → quantity += converted_qty (dalam base UOM)
  (Buat StockBalance baru jika belum ada untuk product+container)
```

**Langkah 3 — Inventory Movement Record:**
```
InventoryMovement {
    product_id, container_id,
    movement_type: RECEIPT,
    qty: converted_qty,
    reference_type: GOODS_RECEIPT,
    reference_id: GR header id,
    unit_cost_original, unit_cost_local
}
```

**Langkah 4 — Valuation Layer Creation:**
```
ValuationLayer.createNew(
    productId, containerId,
    initialQuantity: converted_qty,
    remainingQuantity: converted_qty,
    unitCost: CostAmount(currencyId, exchangeRate, po_unit_price, localAmount)
)
→ localAmount = po_unit_price × exchangeRate / conversionFactor (per PCS)
```

**Langkah 5 — Auto-Journal Entry:**
```
Lookup: acc_accounting_schemas WHERE event_type = 'GOODS_RECEIPT'

Supplier PKP (PPN 11%):
  DR 1150 Persediaan            Rp inventory_amount
  DR 1170 PPN Masukan           Rp vat_amount (DPP × 11%)
     CR 2150 GR/IR Clearing               Rp (inventory + vat)

Supplier non-PKP:
  DR 1150 Persediaan            Rp inventory_amount
     CR 2150 GR/IR Clearing               Rp inventory_amount

Impor (Multi-Currency, jika applicable):
  DR 1150 Persediaan            Rp FOB_amount_IDR
  DR 1170 PPN Masukan           Rp PPN_impor
  DR 1175 PPh 22 Dibayar Muka   Rp PPh22_amount
     CR 2150 GR/IR Clearing               Rp total_IDR
```

**Langkah 6 — PO Status Update:**
```
PO line: received_quantity += GR line quantity
If ALL PO lines (received_qty >= qty_ordered) → PO status = FULLY_RECEIVED
Else → PO status = PARTIALLY_RECEIVED
```

> **CRITICAL: Jika auto-journal gagal → SELURUH transaksi rollback (GR tidak disimpan).**
> Error: `gr.error.journal-creation-failed`

---

### 4.4 Partial GR — Worked Example

**Skenario:** PO #PO-202601-00001 — 100 unit Laptop @ USD 500, Supplier PKP

**GR Pertama (GR-202601-00001): 60 unit, kurs 15.500**
```
DR 1150 Persediaan      Rp 465.000.000  (60 × 500 × 15.500)
DR 1170 PPN Masukan     Rp  51.150.000  (465.000.000 × 11%)
   CR 2150 GR/IR Clear            Rp 516.150.000

ValuationLayer #1: 60 PCS @ Rp 7.750.000/unit
PO Line: received_qty = 60, outstanding = 40
PO Status → PARTIALLY_RECEIVED
```

**GR Kedua (GR-202602-00001): 40 unit, kurs 15.800 (bulan berikutnya)**
```
DR 1150 Persediaan      Rp 316.000.000  (40 × 500 × 15.800)
DR 1170 PPN Masukan     Rp  34.760.000  (316.000.000 × 11%)
   CR 2150 GR/IR Clear            Rp 350.760.000

ValuationLayer #2: 40 PCS @ Rp 7.900.000/unit
PO Line: received_qty = 100, outstanding = 0
PO Status → FULLY_RECEIVED
```

**Hasil:** 2 valuation layer berbeda — **ini kunci FIFO**. Layer #1 (lebih murah) dikonsumsi lebih dulu saat COGS.

---

### 4.5 Edge Cases

| Skenario | Mitigasi |
|----------|---------|
| Over-receipt (qty > PO outstanding) | Configurable tolerance: `gr.over_receipt_tolerance_pct = 10`. Exceed → reject |
| GR saat PO cancelled | OptimisticLockException → GR fails. GR valid hanya untuk PO APPROVED/PARTIALLY_RECEIVED |
| Concurrent GR dari 2 warehouse | Optimistic lock pada PO line: `UPDATE SET qty_received = qty_received + :grQty WHERE version = :expected`. Retry 1x on conflict |
| Negative stock | `StockService.adjust()` validation: `if (qty + adjustment < 0) → reject` (`stock.error.insufficient-qty`) |
| Auto-journal gagal (COA inactive) | Pre-validate before transaction. If fail → entire rollback |
| Valuation layer cleanup | Layer NEVER deleted. `qty_remaining = 0` excluded from FIFO query but visible in audit |

**Financial Statement Impact:**

| Event | Balance Sheet | Income Statement | Cash Flow |
|-------|--------------|-----------------|-----------|
| GR Completed | ↑ Inventory (1150), ↑ VAT Input (1170), ↑ GR/IR Clearing (2150) | Tidak ada | Tidak ada (belum bayar) |

---

## Sprint 5: Accounts Payable

### Sprint Goal

Menyelesaikan siklus hutang: **Three-Way Matching** (PO ↔ GR ↔ Bill), pengakuan AP, penanganan return, dan pembayaran. Setiap transaksi menghasilkan auto-journal. **GR/IR Clearing account di-resolve** pada sprint ini.

### Dependencies
- ✅ Sprint 4: Goods Receipt (COMPLETED)
- ✅ Inventory: ValuationLayer (untuk Purchase Return)
- ✅ Accounting: COA (2110, 2150, 5130, 8110, 8210), Schema events

---

### 5.1 Vendor Bill

**Tabel:** `ap_vendor_bills`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `code` | VARCHAR(30) | UK, NOT NULL | Auto: `BILL-{yyyyMM}-{seq}` |
| `bill_date` | DATE | NOT NULL | Tanggal faktur supplier |
| `due_date` | DATE | NOT NULL | Jatuh tempo pembayaran |
| `supplier_id` | BIGINT | FK→parties | |
| `po_id` | BIGINT | FK→pur_purchase_orders | Reference PO |
| `currency_id` | BIGINT | | |
| `exchange_rate` | DECIMAL(19,6) | | Kurs saat bill |
| `subtotal` | DECIMAL(19,4) | | DPP |
| `tax_amount` | DECIMAL(19,4) | | PPN |
| `total_amount` | DECIMAL(19,4) | | DPP + PPN |
| `paid_amount` | DECIMAL(19,4) | DEFAULT 0 | Running total pembayaran |
| `status` | VARCHAR(20) | NOT NULL | Lihat state machine |
| `match_status` | VARCHAR(20) | | `UNMATCHED`, `PARTIAL`, `FULL` |
| `supplier_invoice_no` | VARCHAR(50) | NULLABLE | Nomor faktur asli dari supplier |
| `faktur_pajak_no` | VARCHAR(30) | NULLABLE | NSFP (wajib jika supplier PKP) |
| `faktur_pajak_date` | DATE | NULLABLE | Tanggal Faktur Pajak |
| `dpp_amount` | DECIMAL(19,4) | NULLABLE | Dasar Pengenaan Pajak |
| `ppn_amount` | DECIMAL(19,4) | NULLABLE | PPN = DPP × 11% |
| `pph_type` | VARCHAR(20) | NULLABLE | `PPH_23`, `PPH_42`, `PPH_22`, `NONE` |
| `pph_rate` | DECIMAL(5,4) | NULLABLE | e.g., 0.02 untuk jasa |
| `pph_amount` | DECIMAL(19,4) | NULLABLE | DPP × pph_rate |
| `note` | TEXT | NULLABLE | |
| `is_active` | BOOLEAN | DEFAULT TRUE | |
| + audit fields | | | |

**Tabel:** `ap_vendor_bill_lines`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `header_id` | BIGINT | FK→ap_vendor_bills | |
| `gr_line_id` | BIGINT | FK→pur_goods_receipt_lines | 3-way match reference |
| `po_line_id` | BIGINT | FK→pur_purchase_order_lines | PO reference |
| `product_id` | BIGINT | FK→inv_products | |
| `qty_billed` | DECIMAL(19,4) | NOT NULL | Qty di-bill |
| `unit_price` | DECIMAL(19,4) | NOT NULL | Harga per unit (mungkin beda dari PO) |
| `line_subtotal` | DECIMAL(19,4) | | qty × unit_price |
| `line_tax` | DECIMAL(19,4) | | PPN per line |
| `line_total` | DECIMAL(19,4) | | subtotal + tax |
| `price_variance` | DECIMAL(19,4) | | (unit_price_bill - unit_price_po) × qty |
| `note` | TEXT | NULLABLE | |
| + audit fields | | | |

**Status Flow:**
```
DRAFT ──match──→ CONFIRMED ──pay──→ PARTIALLY_PAID ──pay──→ PAID
  │                 │
  └──cancel──→ CANCELLED ←──cancel──┘
```

---

### 5.2 Three-Way Matching Algorithm

```
Three-Way Match:
  ┌──────────┐     ┌──────────┐     ┌──────────┐
  │ PO Line  │ ←──→│ GR Line  │ ←──→│ Bill Line│
  │ qty=100  │     │ qty=100  │     │ qty=100  │
  │ price=500│     │ price=500│     │ price=500│
  └──────────┘     └──────────┘     └──────────┘
```

**Configurable Strictness:**

| Setting | Default | Deskripsi |
|---------|---------|-----------|
| `bill.matching.mode` | `THREE_WAY` | `THREE_WAY`, `TWO_WAY` (tanpa GR check — service company), `NONE` |
| `bill.matching.price_tolerance_pct` | `5` | Harga boleh ±5% dari PO |
| `bill.matching.qty_tolerance_pct` | `0` | Qty harus exact match dengan GR |
| `bill.matching.require_gr_before_bill` | `true` | GR wajib ada sebelum bill |

**Tolerance Band Rules:**

| Variance | Action | Keterangan |
|----------|--------|-----------|
| ≤ 5% | ⚠️ Warning, auto-approve | Dalam toleransi, catat variance |
| 5% – 10% | ⚠️ Warning, perlu manager override | Butuh approval tambahan |
| > 10% | ❌ Hard block (reject) | Ditolak, harus revisi bill |

**10 Matching Scenarios:**

| # | Skenario | PO Qty | GR Qty | Bill Qty | Bill Price | Result |
|---|----------|--------|--------|----------|------------|--------|
| 1 | Perfect match | 100 | 100 | 100 | Same | ✅ Auto-approve |
| 2 | Bill < GR | 100 | 100 | 90 | Same | ⚠️ Allow, sisa bill nanti |
| 3 | Bill > GR | 100 | 80 | 100 | Same | ❌ Reject (cannot bill > received) |
| 4 | Price +5% | 100 | 100 | 100 | +5% | ⚠️ Tolerance, warning |
| 5 | Price -5% | 100 | 100 | 100 | -5% | ✅ Favorable variance |
| 6 | GR partial, bill full | 100 | 60 | 100 | Same | ❌ Reject |
| 7 | Multi-GR, single bill | 100 | 60+40 | 100 | Same | ✅ Accept |
| 8 | Multi-bill for 1 GR | 100 | 100 | 70+30 | Same | ✅ Partial billing |
| 9 | No GR, bill arrives | 100 | 0 | 100 | — | ❌ Reject (GR required) |
| 10 | Different currency | USD | USD | EUR | — | ❌ Reject (currency mismatch) |

**Validation Logic (pseudo-code):**
```java
// Qty check (strict)
if (bill.getTotalQty() > totalGrQty)
    return MatchResult.rejected("bill.error.qty-exceeds-gr");

// Price variance check per line
for (BillLine line : bill.getLines()) {
    variance = |line.unitPrice - poLine.unitPrice| / poLine.unitPrice;
    if (variance > 0.10) return MatchResult.rejected("bill.error.price-exceeds-tolerance");
    if (variance > 0.05) addWarning("bill.warn.price-variance-high");
    if (variance > 0) addWarning("bill.warn.price-differs");
}
```

---

### 5.3 Auto-Journal: Vendor Bill

**EVENT: VENDOR_BILL — Pengakuan Hutang Supplier**

```
Trigger: Bill status DRAFT → CONFIRMED (setelah 3-way match valid)

JOURNAL — Harga sama dengan PO (no variance):
  DR 2150 GR/IR Clearing        Rp 516.150.000
     CR 2110 Hutang Usaha                  Rp 516.150.000

JOURNAL — Harga berbeda (Bill > PO → price variance):
  DR 2150 GR/IR Clearing        Rp 516.150.000  (amount per GR)
  DR 5130 Purchase Price Var    Rp   5.000.000  (selisih unfavorable)
     CR 2110 Hutang Usaha                  Rp 521.150.000  (per bill)

JOURNAL — Harga berbeda (Bill < PO → favorable):
  DR 2150 GR/IR Clearing        Rp 516.150.000
     CR 2110 Hutang Usaha                  Rp 511.150.000
     CR 5130 Purchase Price Var            Rp   5.000.000

Tax Handling:
  - PPN sudah dicatat saat GR (di 1170)
  - Pada Bill, hanya memindahkan dari GR/IR ke AP
  - Faktur Pajak dari supplier harus cocok dengan PPN yang dicatat
```

---

### 5.4 Vendor Payment

**Tabel:** `ap_vendor_payments`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `code` | VARCHAR(30) | UK | Auto: `PAY-{yyyyMM}-{seq}` |
| `payment_date` | DATE | NOT NULL | |
| `supplier_id` | BIGINT | FK→parties | Must match bill supplier |
| `currency_id` | BIGINT | | |
| `exchange_rate` | DECIMAL(19,6) | | Kurs saat bayar |
| `amount` | DECIMAL(19,4) | NOT NULL, > 0 | Total pembayaran |
| `payment_method` | VARCHAR(20) | | `BANK_TRANSFER`, `CASH`, `GIRO`, `CHECK` |
| `bank_account_id` | BIGINT | NULLABLE, FK | |
| `giro_number` | VARCHAR(30) | NULLABLE | Untuk giro/check |
| `giro_date` | DATE | NULLABLE | Tanggal jatuh tempo giro |
| `status` | VARCHAR(20) | | `DRAFT → CONFIRMED → CANCELLED` |
| `note` | TEXT | NULLABLE | |
| + audit fields | | | |

**Tabel:** `ap_payment_allocations`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `payment_id` | BIGINT | FK→ap_vendor_payments | |
| `bill_id` | BIGINT | FK→ap_vendor_bills | |
| `allocated_amount` | DECIMAL(19,4) | | Amount allocated ke bill ini |
| + audit fields | | | |

**Business Rules:**
- Satu payment bisa dialokasikan ke multiple bills (partial payment)
- `SUM(allocations) ≤ payment.amount`
- Bill `paid_amount += allocation.allocated_amount`
- Bill status: `paid_amount > 0 AND < total → PARTIALLY_PAID`, `= total → PAID`
- Payment `supplier_id` MUST match bill `supplier_id` (`payment.error.supplier-mismatch`)
- Payment amount ≤ remaining bill balance (`payment.error.exceeds-bill-balance`)

**Auto-Journal: VENDOR_PAYMENT**

```
Skenario 1 — Pembayaran Barang (tanpa withholding):
  DR 2110 Hutang Usaha       Rp 555.000.000
     CR 1120 Bank                      Rp 555.000.000

Skenario 2 — Pembayaran Jasa (PPh 23 — 2%):
  Tagihan: DPP 20M + PPN 2,2M = 22,2M
  PPh 23 = 2% × DPP = 400K
  
  DR 2110 Hutang Usaha       Rp  22.200.000
     CR 1120 Bank                      Rp  21.800.000
     CR 2170 PPh 23 Terutang           Rp     400.000

Skenario 3 — Pembayaran Sewa (PPh 4(2) Final — 10%):
  Sewa DPP 120M + PPN 13,2M = 133,2M
  PPh 4(2) = 10% × DPP = 12M
  
  DR 2110 Hutang Usaha       Rp 133.200.000
     CR 1120 Bank                      Rp 121.200.000
     CR 2175 PPh 4(2) Terutang         Rp  12.000.000

Skenario 4 — Multi-Currency (Forex Variance):
  PO: USD 10K @ 15.500 = 155M, Payment: @ 15.800 = 158M
  
  DR 2110 Hutang Usaha       Rp 155.000.000
  DR 8210 Rugi Selisih Kurs  Rp   3.000.000
     CR 1120 Bank                      Rp 158.000.000
```

---

### 5.5 Purchase Return

**Tabel:** `ap_purchase_returns`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `code` | VARCHAR(30) | UK | Auto: `PRTN-{yyyyMM}-{seq}` |
| `return_date` | DATE | NOT NULL | |
| `gr_id` | BIGINT | FK→pur_goods_receipts | GR yang di-return |
| `supplier_id` | BIGINT | FK→parties | |
| `reason` | TEXT | NOT NULL | Alasan return |
| `status` | VARCHAR(20) | | `DRAFT → CONFIRMED → CANCELLED` |
| + audit fields | | | |

**Tabel:** `ap_purchase_return_lines`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `header_id` | BIGINT | FK | |
| `gr_line_id` | BIGINT | FK→pur_goods_receipt_lines | |
| `product_id` | BIGINT | FK→inv_products | |
| `qty_returned` | DECIMAL(19,4) | NOT NULL | ≤ GR line qty |
| `unit_cost` | DECIMAL(19,4) | | Dari **specific FIFO layer** |
| + audit fields | | | |

**Inventory Impact — Return CONFIRMED:**
1. `StockBalance.quantity -= return_qty` (validasi non-negative)
2. FIFO layer: consume **specific layer** (dari GR yang di-return — bukan oldest first)
3. InventoryMovement: `movement_type = ISSUE`, `reference_type = PURCHASE_RETURN`

**Auto-Journal: PURCHASE_RETURN**

```
Jika Bill BELUM di-post:
  DR 2150 GR/IR Clearing     Rp 55.500.000
     CR 1150 Persediaan                Rp 50.000.000
     CR 1170 PPN Masukan               Rp  5.500.000

Jika Bill SUDAH di-post (AP sudah recognized):
  DR 2110 Hutang Usaha       Rp 55.500.000
     CR 1150 Persediaan                Rp 50.000.000
     CR 1170 PPN Masukan               Rp  5.500.000

Notes:
  - Qty dari FIFO specific layer (bukan oldest first)
  - PPN Masukan yang sudah dikreditkan dikurangkan
  - Nota Retur harus didokumentasikan ke supplier
```

---

### 5.6 GR/IR Clearing Account Lifecycle

```
Timeline:
─────────────────────────────────────────────────
  [GR Completed]          [Bill Confirmed]        [Saldo Nol]
       │                       │                       │
  CR 2150 ← created      DR 2150 ← cleared            ✓
  (liability muncul)     (liability pindah ke AP)

Saldo 2150:
  After GR:   CR Rp 516.150.000 (barang diterima, invoice belum)
  After Bill: Rp 0               (cleared — match sempurna)

Jika saldo 2150 ≠ 0 pada akhir periode:
  → Ada GR yang belum di-match dengan Bill
  → HARUS di-disclose sebagai "Accrued Purchases"
  → Review wajib sebelum period closing
```

---

## Sprint 6: Accounting Core

### Sprint Goal

Membangun mesin jurnal sentral: **manual journal entry** untuk penyesuaian, **General Ledger view** untuk audit trail per akun, **Trial Balance** sebagai check keseimbangan, dan **Period Closing** procedures.

### Dependencies
- ✅ Sprint 1: COA, Schema, Fiscal Period
- ✅ Sprint 4–5: Auto-journals dari GR, Bill, Payment, Return

---

### 6.1 Journal Entry

**Tabel:** `acc_journal_entries`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `code` | VARCHAR(30) | UK, NOT NULL | `JV-{yyyyMM}-{seq}` (manual) atau `AJ-{yyyyMM}-{seq}` (auto) |
| `journal_date` | DATE | NOT NULL | Harus dalam period OPEN |
| `accounting_period_id` | BIGINT | FK→acc_accounting_periods | |
| `description` | VARCHAR(500) | NOT NULL | Narasi jurnal |
| `source_type` | VARCHAR(10) | NOT NULL | `MANUAL`, `AUTO` |
| `source_ref` | VARCHAR(50) | NULLABLE | e.g., "GR-202601-00001" |
| `currency_id` | BIGINT | | |
| `exchange_rate` | DECIMAL(19,6) | | |
| `total_debit` | DECIMAL(19,4) | NOT NULL | MUST = total_credit |
| `total_credit` | DECIMAL(19,4) | NOT NULL | MUST = total_debit |
| `status` | VARCHAR(20) | NOT NULL | `DRAFT`, `POSTED`, `REVERSED` |
| `is_reversed` | BOOLEAN | DEFAULT FALSE | |
| `is_reversal` | BOOLEAN | DEFAULT FALSE | |
| `is_adjustment` | BOOLEAN | DEFAULT FALSE | Flag untuk jurnal di reopened period |
| `reversal_of_id` | BIGINT | NULLABLE, FK→self | Untuk reversal entries |
| `posted_by_user_id` | BIGINT | NULLABLE | |
| `posted_date` | DATETIME | NULLABLE | |
| + audit fields | | | |

**Tabel:** `acc_journal_entry_lines`

| Kolom | Tipe | Constraint | Keterangan |
|-------|------|-----------|------------|
| `id` | BIGINT | PK | |
| `journal_entry_id` | BIGINT | FK | |
| `line_number` | INT | NOT NULL | |
| `account_id` | BIGINT | FK→acc_chart_of_accounts | MUST be `is_header = false` |
| `description` | VARCHAR(255) | NULLABLE | Narasi per line |
| `debit_amount` | DECIMAL(19,4) | DEFAULT 0 | Salah satu HARUS 0 |
| `credit_amount` | DECIMAL(19,4) | DEFAULT 0 | Salah satu HARUS 0 |
| `debit_amount_original` | DECIMAL(19,4) | NULLABLE | Foreign currency |
| `credit_amount_original` | DECIMAL(19,4) | NULLABLE | Foreign currency |
| `currency_id` | BIGINT | NULLABLE | |
| `exchange_rate` | DECIMAL(19,6) | NULLABLE | |
| `party_id` | BIGINT | NULLABLE, FK→parties | Sub-ledger tracking |
| + audit fields | | | |

**DB Constraint:**
```sql
CHECK ((debit_amount > 0 AND credit_amount = 0) OR (debit_amount = 0 AND credit_amount > 0))
```

**Status Flow:**
```
                        ┌──post──→ POSTED ──reverse──→ REVERSED
DRAFT ──────────────────┤
                        └──delete──→ (removed)

AUTO journal: langsung POSTED (skip DRAFT)
```

**Business Rules:**
1. **Balance Enforcement:** `SUM(debit) MUST = SUM(credit)` — zero tolerance (`journal.error.not-balanced`)
2. **Period Validation:** `journal_date` dalam period OPEN (`period.error.closed`)
3. **Account Validation:** Hanya akun `is_header = false` (`journal.error.account-invalid`)
4. **Minimum 2 lines** (minimal 1 DR + 1 CR)
5. **Auto-journal** (source_type=AUTO): langsung POSTED, tidak bisa edit
6. **Manual journal**: DRAFT→POSTED, bisa di-reverse
7. **Reversal:** Journal baru dengan DR↔CR swapped + `reversal_of_id` → original. Original status → REVERSED
8. **Journal NEVER deleted or edited after POSTED** — immutability for audit

**Validation Rules Catalog:**

| Field | Rule | Error Code |
|-------|------|-----------|
| `code` | NOT NULL, UNIQUE | `journal.error.code-required` |
| `journal_date` | Dalam period OPEN | `journal.error.date-invalid-period` |
| `description` | NOT NULL, max 500 chars | `journal.error.description-required` |
| `lines` | Min 2 (1 DR + 1 CR) | `journal.error.min-two-lines` |
| `line.account_id` | FK exists, active, leaf | `journal.error.account-invalid` |
| `SUM(debit)` | MUST = SUM(credit) | `journal.error.not-balanced` |

### 6.2 AutoJournalService Pattern

Single entry point untuk semua auto-journal dari modul operasional:

```java
@Service
public class AutoJournalService {
    public JournalEntry createAutoJournal(AutoJournalRequest request) {
        // 1. Lookup accounting schema by event_type
        // 2. Validate period OPEN
        // 3. Validate COA accounts active
        // 4. Build journal entry + lines
        // 5. Validate balance (SUM DR = SUM CR)
        // 6. Save with status POSTED
        // 7. Return created journal
    }
}

// Called by:
// - GoodsReceiptService.complete() → GOODS_RECEIPT event
// - VendorBillService.confirm() → VENDOR_BILL event
// - VendorPaymentService.confirm() → VENDOR_PAYMENT event
// - PurchaseReturnService.confirm() → PURCHASE_RETURN event
```

---

### 6.3 General Ledger View

```
Akun: 1150 - Persediaan
Periode: Januari 2026
───────────────────────────────────────────────────────────────
Tanggal   │ No. Bukti       │ Keterangan        │ Debit        │ Kredit      │ Saldo
──────────┼─────────────────┼───────────────────┼──────────────┼─────────────┼──────────────
01-Jan    │                 │ Saldo Awal        │              │             │ 150.000.000
05-Jan    │ GR-202601-00001 │ Receipt Laptop    │ 465.000.000  │             │ 615.000.000
12-Jan    │ SA-202601-00001 │ Stock Adj OUT     │              │  5.000.000  │ 610.000.000
20-Jan    │ GR-202601-00002 │ Receipt Keyboard  │  25.000.000  │             │ 635.000.000
──────────┼─────────────────┼───────────────────┼──────────────┼─────────────┼──────────────
          │                 │ Saldo Akhir       │ 490.000.000  │  5.000.000  │ 635.000.000
```

**Query Logic:**
- Opening = SUM(debit) - SUM(credit) dari semua periode sebelumnya
- Movement = journal lines dalam periode yang dipilih
- Ending = Opening + SUM(debit) - SUM(credit) periode ini
- Untuk akun CREDIT normal: Balance = SUM(credit) - SUM(debit)

**Performance Rules:**
- WAJIB filter by account + date range — no unfiltered queries
- Pagination: max 50 entries per page
- Index: `CREATE INDEX idx_jlines_account_date ON acc_journal_entry_lines(account_id, journal_date)`
- Drill-down: Journal Code → Journal Detail → Source Document

---

### 6.4 Trial Balance

```
PT. SOLUSI PRIMA MANDIRI
NERACA SALDO — Per 31 Januari 2026
═══════════════════════════════════════════════════════════════
Code │ Nama Akun                    │ Debit          │ Kredit
─────┼──────────────────────────────┼────────────────┼────────────────
1110 │ Kas IDR                      │   50.000.000   │
1120 │ Bank Mandiri                 │  800.000.000   │
1150 │ Persediaan                   │  635.000.000   │
1170 │ PPN Masukan                  │   53.900.000   │
2110 │ Hutang Usaha                 │                │   490.000.000
2150 │ GR/IR Clearing               │                │    53.900.000
3210 │ Laba Ditahan                 │                │   950.000.000
5130 │ Purchase Price Variance      │    5.000.000   │
     │ ...                          │                │
─────┼──────────────────────────────┼────────────────┼────────────────
     │ TOTAL                        │ 1.543.900.000  │ 1.543.900.000 ✓
═══════════════════════════════════════════════════════════════
```

**Rules:**
- Total Debit HARUS = Total Credit (invariant)
- Jika tidak balance → **system alert** (bug di journal engine)
- Semua kalkulasi: `BigDecimal` dengan `ROUND_HALF_UP`, scale 2
- Aggregasi di database: `SELECT account_id, SUM(debit), SUM(credit) FROM journal_lines GROUP BY account_id`
- **JANGAN** load semua entries ke Java lalu sum

---

### 6.5 Period Closing Checklist

**8 langkah validasi wajib sebelum close period:**

| No | Langkah | Tipe | Deskripsi |
|----|---------|------|-----------|
| 1 | Trial Balance Check | 🔴 BLOCKER | SUM(DR) = SUM(CR) — wajib balance |
| 2 | Unposted Journals | 🔴 BLOCKER | Tidak boleh ada DRAFT journals di period ini |
| 3 | GR/IR Clearing (2150) | 🟡 WARNING | Saldo ≠ 0 = ada GR belum di-bill (boleh, tapi harus di-ack) |
| 4 | Rekonsiliasi PPN | 🟡 WARNING | Saldo 1170 + 2160 sesuai SPT Masa? |
| 5 | Rekonsiliasi PPh | 🟡 WARNING | 2170/2175 sudah disetor? |
| 6 | Jurnal Penyesuaian | 🟡 WARNING | Depreciation, accrual, prepaid amortization |
| 7 | Inventory Reconciliation | 🟡 WARNING | GL saldo 1150 = SUM(valuation layers)? |
| 8 | Period Close | ACTION | Status → CLOSED, no new posting allowed |

**Inventory Reconciliation Formula:**
```
GL_inventory = SUM(debit) - SUM(credit) FROM journal_lines WHERE account_id = 1150
Stock_value  = SUM(remaining_qty × unit_cost_local) FROM valuation_layers
Variance     = |GL_inventory - Stock_value|

If variance > Rp 1 → WARNING (investigate)
If variance > Rp 1.000.000 → BLOCKER
```

---

### 6.6 Year-End Closing (3-Step Process)

**Step 1: Tutup semua Revenue & Expense ke Income Summary (9110)**
```
DR 4110 Product Sales         Rp XXX.XXX.XXX
DR 4210 Service Revenue       Rp  XX.XXX.XXX
   CR 9110 Income Summary               Rp XXX.XXX.XXX

DR 9110 Income Summary        Rp XXX.XXX.XXX
   CR 5110 COGS Material               Rp XXX.XXX.XXX
   CR 5210 Beban Gaji                   Rp  XX.XXX.XXX
   CR 5xxx (all expenses)               Rp  XX.XXX.XXX
```

**Step 2: Tutup Income Summary ke Retained Earnings**
```
If Laba (Credit balance di 9110):
  DR 9110 Income Summary      Rp XX.XXX.XXX
     CR 3210 Laba Ditahan              Rp XX.XXX.XXX

If Rugi (Debit balance di 9110):
  DR 3210 Laba Ditahan        Rp XX.XXX.XXX
     CR 9110 Income Summary            Rp XX.XXX.XXX
```

**Step 3: Verifikasi & Lock**
- Trial Balance harus balance setelah closing entries
- Revenue & Expense accounts harus saldo = 0
- Lock fiscal year (is_active = false)

---

### 6.7 Contoh Jurnal Penyesuaian

```
1. Penyusutan Aset Tetap (Bulanan):
  DR 5230 Beban Penyusutan         Rp 5.000.000
     CR 1240 Akum. Penyusutan              Rp 5.000.000

2. Accrual Gaji (Akhir Bulan):
  DR 5210 Beban Gaji              Rp 50.000.000
     CR 2140 Beban Masih Harus Dibayar    Rp 50.000.000

3. Penyetoran PPN Kurang Bayar:
  DR 2160 PPN Keluaran            Rp 30.000.000
     CR 1170 PPN Masukan                  Rp 25.000.000
     CR 1120 Bank                          Rp  5.000.000

4. Penyetoran PPh 23 ke Negara:
  DR 2170 PPh 23 Terutang          Rp 2.000.000
     CR 1120 Bank                          Rp 2.000.000
```

---

## Sprint 7+: Financial Reports

### Sprint Goal

Menghasilkan laporan keuangan sesuai **PSAK/SAK-ETAP**: Laba Rugi, Neraca, Arus Kas, dan AP Aging. Semua laporan harus dalam format Indonesia dengan format angka Rp.

### Dependencies
- ✅ Sprint 6: Journal Engine, GL, Trial Balance

---

### 7.1 AP Aging Report (Analisa Umur Hutang)

**Configurable Aging Buckets:**

| Setting | Default |
|---------|---------|
| `report.aging.bucket_1` | 30 |
| `report.aging.bucket_2` | 60 |
| `report.aging.bucket_3` | 90 |

**Format:**
```
PT. SOLUSI PRIMA MANDIRI
ANALISA UMUR HUTANG — Per 31/01/2026
═══════════════════════════════════════════════════════════════════════
Supplier          │ Total          │ Lancar    │ 1-30     │ 31-60    │ 61-90    │ > 90
──────────────────┼────────────────┼───────────┼──────────┼──────────┼──────────┼──────────
PT. ABC Supply    │ 555.000.000   │ 555M      │          │          │          │
CV. XYZ Trading   │ 120.000.000   │           │ 120M     │          │          │
PT. Delta Jaya    │  50.000.000   │           │          │          │  50M     │
──────────────────┼────────────────┼───────────┼──────────┼──────────┼──────────┼──────────
TOTAL             │ 725.000.000   │ 555M      │ 120M     │  0       │  50M     │  0
═══════════════════════════════════════════════════════════════════════

Calculation: aging_days = report_date - bill.due_date
Negative aging_days = "Lancar" (belum jatuh tempo)

KPI Summary:
  Total AP Outstanding: Rp 725.000.000
  Total Overdue: Rp 170.000.000
  Avg Days Late: 35 hari
```

**Drill-down:** Klik supplier → list bills. Klik bill → bill detail → source PO → GR.

**Catatan Pajak:** Hutang > 3 tahun tanpa klaim → diakui penghasilan (koreksi fiskal positif).

---

### 7.2 Laporan Laba Rugi (Income Statement)

```
PT. SOLUSI PRIMA MANDIRI
LAPORAN LABA RUGI
Untuk Periode Berakhir 31/12/2026

PENDAPATAN USAHA:
  Penjualan Barang (4110)               XXX.XXX.XXX
  Pendapatan Jasa (4210)                 XX.XXX.XXX
  Diskon Penjualan (4120)              (XX.XXX.XXX)
  Retur Penjualan (4130)                (X.XXX.XXX)
                                    ─────────────
  Pendapatan Usaha Bersih               XXX.XXX.XXX

HARGA POKOK PENJUALAN:
  HPP - Bahan Baku (5110)               XXX.XXX.XXX
  HPP - Tenaga Kerja (5120)              XX.XXX.XXX
                                    ─────────────
  Total HPP                             XXX.XXX.XXX

LABA KOTOR                               XXX.XXX.XXX

BEBAN USAHA:
  Beban Gaji (5210)                      XX.XXX.XXX
  Beban Utilitas (5220)                   X.XXX.XXX
  Beban Penyusutan (5230)                  X.XXX.XXX
  Beban Sewa (5250)                        X.XXX.XXX
  Beban Transport (5260)                   X.XXX.XXX
  Beban Pemasaran (5270)                   X.XXX.XXX
  Beban Pajak (5280)                       X.XXX.XXX
  Beban Admin Bank (5285)                  X.XXX.XXX
  Beban Materai (5290)                     X.XXX.XXX
  Purchase Price Variance (5130)           X.XXX.XXX
                                    ─────────────
  Total Beban Usaha                       XX.XXX.XXX

LABA USAHA                                XX.XXX.XXX

PENDAPATAN/(BEBAN) LAIN-LAIN:
  Laba Selisih Kurs (8110)                 X.XXX.XXX
  Pendapatan Bunga (8120)                  X.XXX.XXX
  Rugi Selisih Kurs (8210)               (X.XXX.XXX)
                                    ─────────────
  Total Lain-lain Bersih                   X.XXX.XXX

LABA BERSIH SEBELUM PPh                  XX.XXX.XXX
BEBAN PPh BADAN (22%)                   (X.XXX.XXX)
LABA BERSIH SETELAH PPh                  XX.XXX.XXX
═════════════════════════════════════════════════════
```

**Query Logic:**
```
Revenue  = SUM(credit) - SUM(debit) WHERE account_type = 'REVENUE'
Expense  = SUM(debit) - SUM(credit) WHERE account_type = 'EXPENSE'
Net Income = Revenue - Expense
```

**Features:** Period selector (Monthly/Quarterly/Annually), comparison vs previous period, hierarchical tree expandable, export PDF/Excel.

---

### 7.3 Neraca (Balance Sheet)

```
PT. SOLUSI PRIMA MANDIRI
NERACA
Per 31/12/2026

ASET
  Aset Lancar:
    Kas & Setara Kas (1110, 1120, 1130)     XXX.XXX.XXX
    Piutang Usaha (1140)                    XXX.XXX.XXX
    Persediaan (1150)                       XXX.XXX.XXX
    PPN Masukan (1170)                       XX.XXX.XXX
    PPh Dibayar di Muka (1175)                X.XXX.XXX
    Uang Muka Pembelian (1180)               XX.XXX.XXX
    Biaya Dibayar di Muka (1160)             XX.XXX.XXX
                                        ─────────────
    Total Aset Lancar                    X.XXX.XXX.XXX

  Aset Tetap:
    Tanah & Bangunan (1210)                 XXX.XXX.XXX
    Peralatan (1220)                         XX.XXX.XXX
    Kendaraan (1230)                         XX.XXX.XXX
    Akum. Penyusutan Peralatan (1240)      (XX.XXX.XXX)
    Akum. Penyusutan Kendaraan (1250)      (XX.XXX.XXX)
                                        ─────────────
    Total Aset Tetap                       XXX.XXX.XXX

TOTAL ASET                               X.XXX.XXX.XXX
═════════════════════════════════════════════════════════

KEWAJIBAN & EKUITAS
  Kewajiban Jangka Pendek:
    Hutang Usaha (2110)                     XXX.XXX.XXX
    Hutang Pajak (2130)                      XX.XXX.XXX
    GR/IR Clearing (2150)                    XX.XXX.XXX
    PPN Keluaran (2160)                      XX.XXX.XXX
    PPh 23 Terutang (2170)                    X.XXX.XXX
    PPh 4(2) Terutang (2175)                  X.XXX.XXX
    Beban Masih Harus Dibayar (2140)        XX.XXX.XXX
                                        ─────────────
    Total Kewajiban Jk. Pendek              XXX.XXX.XXX

  Kewajiban Jangka Panjang:
    Hutang Jangka Panjang (2210)            XXX.XXX.XXX

  Ekuitas:
    Modal Disetor (3110)                    XXX.XXX.XXX
    Agio Saham (3120)                        XX.XXX.XXX
    Laba Ditahan (3210)                     XXX.XXX.XXX
    Laba Tahun Berjalan (3220)               XX.XXX.XXX
                                        ─────────────
    Total Ekuitas                           XXX.XXX.XXX

TOTAL KEWAJIBAN & EKUITAS                X.XXX.XXX.XXX
═════════════════════════════════════════════════════════

Validasi: TOTAL ASET == TOTAL KEWAJIBAN + EKUITAS (WAJIB!)
```

**Invariant:** Jika `Total Assets ≠ Total Liabilities + Equity` → ERROR di header report. Root cause: jurnal tidak balance (seharusnya tidak mungkin terjadi).

---

### 7.4 Laporan Arus Kas (Cash Flow Statement — Indirect Method)

```
PT. SOLUSI PRIMA MANDIRI
LAPORAN ARUS KAS
Untuk Tahun Berakhir 31/12/2026

ARUS KAS DARI AKTIVITAS OPERASI:
  Laba Bersih                                 XX.XXX.XXX
  Penyesuaian untuk pos non-kas:
    Beban Penyusutan (5230)                    X.XXX.XXX
    Rugi Selisih Kurs (8210)                   X.XXX.XXX
    Laba Selisih Kurs (8110)                  (X.XXX.XXX)

  Perubahan Modal Kerja:
    Δ Piutang Usaha (1140)                  (XX.XXX.XXX)
    Δ Persediaan (1150)                     (XX.XXX.XXX)
    Δ PPN Masukan (1170)                     (X.XXX.XXX)
    Δ Hutang Usaha (2110)                    XX.XXX.XXX
    Δ Hutang Pajak (2130, 2170, 2175)         X.XXX.XXX
    Δ GR/IR Clearing (2150)                   X.XXX.XXX
                                         ─────────────
  Kas Bersih dari Aktivitas Operasi           XX.XXX.XXX

ARUS KAS DARI AKTIVITAS INVESTASI:
  Pembelian Aset Tetap                      (XX.XXX.XXX)
                                         ─────────────
  Kas Bersih dari Aktivitas Investasi       (XX.XXX.XXX)

ARUS KAS DARI AKTIVITAS PENDANAAN:
  Penerimaan Hutang Bank                     XX.XXX.XXX
  Pembayaran Hutang Bank                    (XX.XXX.XXX)
  Pembayaran Dividen                         (X.XXX.XXX)
                                         ─────────────
  Kas Bersih dari Aktivitas Pendanaan         X.XXX.XXX

KENAIKAN (PENURUNAN) BERSIH KAS             XX.XXX.XXX
KAS & SETARA KAS AWAL PERIODE               XX.XXX.XXX
KAS & SETARA KAS AKHIR PERIODE              XX.XXX.XXX
═════════════════════════════════════════════════════════

Reconciliation: Opening Cash + Cash Flow = Closing Cash
Closing Cash = GL balance akun 1110 + 1120 + 1130 (single source of truth)
```

---

### 7.5 Report Architecture

**Generation Pattern:**
- Semua laporan digenerate dari `acc_journal_entry_lines` — **single source of truth**
- Aggregasi di database (bukan Java) untuk performance
- Timeout: 30 detik; exceeded → suggest narrower date range
- All calculations: `BigDecimal` with `ROUND_HALF_UP`, scale 2
- Export: Apache POI (Excel) + JasperReports (PDF)

**Cash Flow Account Mapping (configurable):**

| Setting | Default |
|---------|---------|
| `report.cashflow.cash_accounts` | `1110,1120,1130` |
| `report.cashflow.operating_accounts` | `1140,1150,1170,2110,2130,2140,2150` |
| `report.cashflow.investing_accounts` | `1210,1220,1230` |
| `report.cashflow.financing_accounts` | `2120,2210,3110` |

---

## Appendix A: Complete COA Template

### COA Template — Trading Umum (60 akun)

| Code | Nama | Tipe | Normal | Level | Header |
|------|------|------|--------|-------|--------|
| **1000** | **ASET** | ASSET | DEBIT | 1 | ✓ |
| 1100 | Aset Lancar | ASSET | DEBIT | 2 | ✓ |
| 1110 | Kas IDR | ASSET | DEBIT | 3 | |
| 1120 | Bank - Rekening Utama | ASSET | DEBIT | 3 | |
| 1130 | Bank - Tabungan | ASSET | DEBIT | 3 | |
| 1140 | Piutang Usaha | ASSET | DEBIT | 3 | |
| 1150 | Persediaan | ASSET | DEBIT | 3 | |
| 1160 | Biaya Dibayar di Muka | ASSET | DEBIT | 3 | |
| 1170 | PPN Masukan | ASSET | DEBIT | 3 | |
| 1175 | PPh 22 Dibayar di Muka | ASSET | DEBIT | 3 | |
| 1180 | Uang Muka Pembelian | ASSET | DEBIT | 3 | |
| 1200 | Aset Tetap | ASSET | DEBIT | 2 | ✓ |
| 1210 | Tanah & Bangunan | ASSET | DEBIT | 3 | |
| 1220 | Peralatan | ASSET | DEBIT | 3 | |
| 1230 | Kendaraan | ASSET | DEBIT | 3 | |
| 1240 | Akum. Penyusutan - Peralatan | ASSET | CREDIT | 3 | |
| 1250 | Akum. Penyusutan - Kendaraan | ASSET | CREDIT | 3 | |
| **2000** | **KEWAJIBAN** | LIABILITY | CREDIT | 1 | ✓ |
| 2100 | Kewajiban Jangka Pendek | LIABILITY | CREDIT | 2 | ✓ |
| 2110 | Hutang Usaha | LIABILITY | CREDIT | 3 | |
| 2120 | Hutang Jangka Pendek | LIABILITY | CREDIT | 3 | |
| 2130 | Hutang Pajak | LIABILITY | CREDIT | 3 | |
| 2140 | Beban Masih Harus Dibayar | LIABILITY | CREDIT | 3 | |
| 2150 | GR/IR Clearing | LIABILITY | CREDIT | 3 | |
| 2160 | PPN Keluaran | LIABILITY | CREDIT | 3 | |
| 2170 | PPh 23 Terutang | LIABILITY | CREDIT | 3 | |
| 2175 | PPh 4(2) Terutang | LIABILITY | CREDIT | 3 | |
| 2180 | PPh 22 Terutang | LIABILITY | CREDIT | 3 | |
| 2200 | Kewajiban Jangka Panjang | LIABILITY | CREDIT | 2 | ✓ |
| 2210 | Hutang Jangka Panjang | LIABILITY | CREDIT | 3 | |
| **3000** | **EKUITAS** | EQUITY | CREDIT | 1 | ✓ |
| 3100 | Modal | EQUITY | CREDIT | 2 | ✓ |
| 3110 | Modal Disetor | EQUITY | CREDIT | 3 | |
| 3120 | Agio Saham | EQUITY | CREDIT | 3 | |
| 3200 | Laba | EQUITY | CREDIT | 2 | ✓ |
| 3210 | Laba Ditahan | EQUITY | CREDIT | 3 | |
| 3220 | Laba Tahun Berjalan | EQUITY | CREDIT | 3 | |
| **4000** | **PENDAPATAN** | REVENUE | CREDIT | 1 | ✓ |
| 4100 | Pendapatan Usaha | REVENUE | CREDIT | 2 | ✓ |
| 4110 | Penjualan Barang | REVENUE | CREDIT | 3 | |
| 4120 | Diskon Penjualan | REVENUE | DEBIT | 3 | |
| 4130 | Retur Penjualan | REVENUE | DEBIT | 3 | |
| 4140 | Inventory Adjustment Gain | REVENUE | CREDIT | 3 | |
| 4200 | Pendapatan Lain | REVENUE | CREDIT | 2 | ✓ |
| 4210 | Pendapatan Jasa | REVENUE | CREDIT | 3 | |
| **5000** | **BEBAN** | EXPENSE | DEBIT | 1 | ✓ |
| 5100 | Harga Pokok Penjualan | EXPENSE | DEBIT | 2 | ✓ |
| 5110 | HPP - Bahan Baku | EXPENSE | DEBIT | 3 | |
| 5120 | HPP - Tenaga Kerja | EXPENSE | DEBIT | 3 | |
| 5130 | Purchase Price Variance | EXPENSE | DEBIT | 3 | |
| 5140 | Inventory Adjustment Loss | EXPENSE | DEBIT | 3 | |
| 5200 | Beban Operasional | EXPENSE | DEBIT | 2 | ✓ |
| 5210 | Beban Gaji | EXPENSE | DEBIT | 3 | |
| 5220 | Beban Utilitas | EXPENSE | DEBIT | 3 | |
| 5230 | Beban Penyusutan | EXPENSE | DEBIT | 3 | |
| 5240 | Beban Perlengkapan | EXPENSE | DEBIT | 3 | |
| 5250 | Beban Sewa | EXPENSE | DEBIT | 3 | |
| 5260 | Beban Transport | EXPENSE | DEBIT | 3 | |
| 5270 | Beban Pemasaran | EXPENSE | DEBIT | 3 | |
| 5280 | Beban Pajak | EXPENSE | DEBIT | 3 | |
| 5285 | Beban Admin Bank | EXPENSE | DEBIT | 3 | |
| 5290 | Beban Materai | EXPENSE | DEBIT | 3 | |
| **8000** | **LAIN-LAIN** | REVENUE | CREDIT | 1 | ✓ |
| 8100 | Pendapatan Lain-lain | REVENUE | CREDIT | 2 | ✓ |
| 8110 | Laba Selisih Kurs | REVENUE | CREDIT | 3 | |
| 8120 | Pendapatan Bunga Bank | REVENUE | CREDIT | 3 | |
| 8200 | Beban Lain-lain | EXPENSE | DEBIT | 2 | ✓ |
| 8210 | Rugi Selisih Kurs | EXPENSE | DEBIT | 3 | |
| **9000** | **CLOSING** | EQUITY | CREDIT | 1 | ✓ |
| 9100 | Period Close | EQUITY | CREDIT | 2 | ✓ |
| 9110 | Income Summary | EQUITY | CREDIT | 3 | |

---

## Appendix B: Accounting Schema Mapping

| Event Type | DR Account | CR Account | Trigger | Sprint |
|-----------|-----------|-----------|---------|--------|
| `GOODS_RECEIPT` | 1150 Persediaan | 2150 GR/IR Clearing | GR → COMPLETED | 4 |
| `GOODS_RECEIPT` (PKP) | + 1170 PPN Masukan | (included in CR 2150) | GR → COMPLETED, supplier PKP | 4 |
| `GOODS_RECEIPT` (Impor) | + 1175 PPh 22 | (included in CR 2150) | GR → COMPLETED, impor | 4 |
| `VENDOR_BILL` | 2150 GR/IR Clearing | 2110 Hutang Usaha | Bill → CONFIRMED | 5 |
| `VENDOR_BILL` (variance) | + 5130 Price Variance | (or CR 5130 if favorable) | Bill price ≠ PO price | 5 |
| `VENDOR_PAYMENT` | 2110 Hutang Usaha | 1120 Bank | Payment → CONFIRMED | 5 |
| `VENDOR_PAYMENT` (PPh 23) | (included in DR 2110) | + 2170 PPh 23 | Payment jasa, withholding | 5 |
| `VENDOR_PAYMENT` (PPh 4(2)) | (included in DR 2110) | + 2175 PPh 4(2) | Payment sewa, withholding | 5 |
| `VENDOR_PAYMENT` (forex) | + 8210 Forex Loss | + 8110 Forex Gain | Kurs berubah | 5 |
| `PURCHASE_RETURN` (pre-bill) | 2150 GR/IR Clearing | 1150 Persediaan + 1170 PPN | Return → CONFIRMED | 5 |
| `PURCHASE_RETURN` (post-bill) | 2110 Hutang Usaha | 1150 Persediaan + 1170 PPN | Return → CONFIRMED | 5 |
| `STOCK_ADJUSTMENT_IN` | 1150 Persediaan | 4140 Inv Adj Gain | Adj+ approved | 4 |
| `STOCK_ADJUSTMENT_OUT` | 5140 Inv Adj Loss | 1150 Persediaan | Adj- approved | 4 |
| `CUSTOMER_INVOICE` | 1140 Piutang Usaha | 4110 Penjualan | Future O2C | — |
| `GOODS_ISSUE` | 5110 HPP | 1150 Persediaan | Future O2C | — |
| `CUSTOMER_RECEIPT` | 1120 Bank | 1140 Piutang Usaha | Future O2C | — |

---

## Appendix C: Document Numbering Sequences

| Module Code | Pattern | Pad | Reset | Contoh |
|------------|---------|-----|-------|--------|
| `FISCAL_YEAR` | `FY-{seq}` | 4 | NEVER | FY-0001, FY-0002 |
| `SUPPLIER_PRICE_LIST` | `SPL-{date:yyMM}-{seq}` | 5 | MONTHLY | SPL-2601-00001 |
| `PURCHASE_REQUISITION` | `PR-{date:yyyyMM}-{seq}` | 5 | MONTHLY | PR-202601-00001 |
| `PURCHASE_ORDER` | `PO-{date:yyyyMM}-{seq}` | 5 | MONTHLY | PO-202601-00001 |
| `GOODS_RECEIPT` | `GR-{date:yyyyMM}-{seq}` | 5 | MONTHLY | GR-202601-00001 |
| `VENDOR_BILL` | `BILL-{date:yyyyMM}-{seq}` | 5 | MONTHLY | BILL-202601-00001 |
| `VENDOR_PAYMENT` | `PAY-{date:yyyyMM}-{seq}` | 5 | MONTHLY | PAY-202601-00001 |
| `PURCHASE_RETURN` | `PRTN-{date:yyyyMM}-{seq}` | 5 | MONTHLY | PRTN-202601-00001 |
| `JOURNAL_MANUAL` | `JV-{date:yyyyMM}-{seq}` | 5 | MONTHLY | JV-202601-00001 |
| `JOURNAL_AUTO` | `AJ-{date:yyyyMM}-{seq}` | 5 | MONTHLY | AJ-202601-00001 |

**Concurrency:** Row-level lock pada `system_sequences`, `REQUIRES_NEW` propagation, retry 1x on failure.

---

## Appendix D: System Settings & Feature Flags

### Global Settings

| Key | Default | Deskripsi |
|-----|---------|-----------|
| `accounting.base_currency` | `IDR` | Mata uang dasar pelaporan |
| `accounting.coa.max_levels` | `3` | Kedalaman maksimum hierarki COA |
| `accounting.fiscal_year_start_month` | `1` | Bulan awal tahun fiskal (1=Jan) |
| `accounting.allow_period_reopen` | `true` | Izinkan reopen CLOSED period |
| `accounting.require_closing_checklist` | `false` | Wajib checklist sebelum close |

### Procurement Settings

| Key | Default | Deskripsi |
|-----|---------|-----------|
| `procurement.pr.approval_required` | `true` | PR wajib approval |
| `procurement.pr.auto_suggest_supplier` | `true` | Auto-suggest dari price list termurah |
| `procurement.po.approval_required` | `true` | PO wajib approval |
| `procurement.po.default_payment_term_days` | `30` | Default jatuh tempo |
| `procurement.po.allow_price_override` | `true` | Boleh ubah harga dari price list |
| `procurement.po.force_close_enabled` | `true` | Boleh force-close PO |
| `procurement.price_list.auto_fill_po` | `true` | Otomatis isi harga PO dari price list |

### Inventory Settings

| Key | Default | Deskripsi |
|-----|---------|-----------|
| `inventory.valuation_method` | `FIFO` | `FIFO`, `WEIGHTED_AVERAGE`, `STANDARD_COST` |
| `gr.over_receipt_tolerance_pct` | `10` | Toleransi over-receipt (%) |

### AP Settings

| Key | Default | Deskripsi |
|-----|---------|-----------|
| `bill.matching.mode` | `THREE_WAY` | `THREE_WAY`, `TWO_WAY`, `NONE` |
| `bill.matching.price_tolerance_pct` | `5` | Toleransi harga (%) |
| `bill.matching.qty_tolerance_pct` | `0` | Toleransi qty (0 = exact) |
| `bill.matching.require_gr_before_bill` | `true` | GR wajib sebelum bill |

### Report Settings

| Key | Default | Deskripsi |
|-----|---------|-----------|
| `report.aging.bucket_1` | `30` | Aging bucket pertama (hari) |
| `report.aging.bucket_2` | `60` | Aging bucket kedua |
| `report.aging.bucket_3` | `90` | Aging bucket ketiga |
| `report.cashflow.cash_accounts` | `1110,1120,1130` | Akun kas & setara kas |

### Feature Flags

| Flag | Default | Trading | Manufacturing | Service | Retail |
|------|---------|---------|--------------|---------|--------|
| `FEATURE_PURCHASE_REQUISITION` | `true` | ● | ● | ○ | ◐ |
| `FEATURE_RFQ` | `false` | ○ | ○ | — | — |
| `FEATURE_MULTI_CURRENCY_PO` | `false` | ◐ | ◐ | ○ | — |
| `FEATURE_ADVANCE_PAYMENT` | `false` | ◐ | ◐ | ◐ | ○ |
| `FEATURE_SERIAL_TRACKING` | `false` | ○ | ● | — | ○ |
| `FEATURE_BATCH_TRACKING` | `false` | ○ | ● | — | — |
| `FEATURE_QC_INSPECTION` | `false` | ○ | ● | — | — |
| `FEATURE_WITHHOLDING_TAX` | `true` | ● | ● | ● | ● |
| `FEATURE_ADJUSTMENT_PERIOD` | `false` | ○ | ○ | ○ | ○ |
| `FEATURE_COST_CENTER` | `false` | ○ | ◐ | ◐ | — |

---

## Appendix E: Indonesian Tax Reference

### Jadwal Penyetoran & Pelaporan Pajak

| Jenis Pajak | Setor Paling Lambat | Lapor SPT Masa |
|-------------|-------------------|---------------|
| PPh 21 | Tgl 10 bulan berikutnya | Tgl 20 bulan berikutnya |
| PPh 22 | Tgl 10 bulan berikutnya | Tgl 20 bulan berikutnya |
| PPh 23 | Tgl 10 bulan berikutnya | Tgl 20 bulan berikutnya |
| PPh 4(2) | Tgl 10 bulan berikutnya | Tgl 20 bulan berikutnya |
| PPN | Akhir bulan berikutnya | Akhir bulan berikutnya |

### Materai (Stamp Duty)

Per UU No. 10/2020: Dokumen dengan nilai **> Rp 5.000.000** wajib materai **Rp 10.000**.
Berlaku untuk: kwitansi, surat perjanjian, PO di atas threshold.

### Format Data Indonesia

| Elemen | Format | Contoh |
|--------|--------|--------|
| Mata Uang | Rp XXX.XXX.XXX,00 | Rp 10.500.000,00 |
| Tanggal | dd/MM/yyyy | 31/12/2026 |
| Persentase | X,XX% | 11,00% |
| Telepon | +62-XXX-XXXX-XXXX | +62-21-5555-1234 |

> **Implementation Note:** Sistem menyimpan `1250000.00` (format standar). Template cetak mengonversi ke format Indonesia (titik ribuan, koma desimal) tanpa mengubah storage.

### Identifikasi Pihak (Party ID Types)

| Tipe | Format | Keterangan |
|------|--------|-----------|
| NPWP | 15 digit (XX.XXX.XXX.X-XXX.XXX) | Nomor Pokok Wajib Pajak |
| NIK | 16 digit | Nomor Induk Kependudukan (format NPWP baru) |
| SIUP | Bervariasi | Surat Izin Usaha Perdagangan |
| NIB | 13 digit | Nomor Induk Berusaha (via OSS) |

### e-Faktur & Coretax Readiness

| Data Element | Source di ERP | Tujuan |
|-------------|-------------|--------|
| NPWP | `parties.identification_number` (type=NPWP) | Validasi e-Faktur |
| Nama | `parties.name` | Matching NPWP |
| NSFP | `ap_vendor_bills.faktur_pajak_no` | Tax invoice identifier |
| Tanggal FP | `ap_vendor_bills.faktur_pajak_date` | Determines tax period |
| DPP | `ap_vendor_bills.dpp_amount` | Tax basis |
| PPN | `ap_vendor_bills.ppn_amount` | Tax amount |

**Coretax (Per 1 Januari 2025):**
- Menggantikan e-Filing, e-Faktur, e-Billing dalam satu platform
- NSFP format auto-generated oleh Coretax
- API integration available (REST-based)
- Future field: `ap_vendor_bills.coretax_ref_id` (nullable)

---

> **Dokumen ini adalah spesifikasi definitif.** Implementer dapat membangun setiap sprint dari dokumen ini saja tanpa referensi ke proposal individual.
>
> **Sumber:** Merged dari 10 proposal spesialis — P4 (Accounting), P9 (Indonesian Tax), P6 (Inventory), P8 (Edge Cases), P3 (Architecture), P1 (User Stories), P2 (Integration), P7 (Configurability), P5 (UX), P10 (Best Practices).
