# Brainstorming: Vendor Bill & Vendor Payment (Sprint 5)

> Tanggal: 2026-05-10  
> Peserta: Quenz + Copilot CLI  
> Mode: Adaptive (Socratic + First Principles) → Review Socratic  
> Status: **FINAL v3 — semua pertanyaan terbuka resolved + critic findings incorporated**

---

## Executive Summary

Sprint 5 fokus pada dua modul Accounts Payable: **Vendor Bill** (three-way matching PO+GR+Invoice) dan **Vendor Payment** (partial + batch payment). Purchase Return di-defer ke Sprint 5+. Fondasi journal sudah sangat solid dari GR — pola yang sama tinggal direplikasi untuk dua event baru (`VENDOR_BILL`, `VENDOR_PAYMENT`).

---

## Keputusan yang Sudah Dikonfirmasi

| Topik | Keputusan |
|---|---|
| Nomor dokumen Vendor Bill | **Dual**: internal auto-gen `VB-yymm-XXXXX` + `vendor_invoice_number` (input manual) |
| Payment granularity | **Partial payment** + **batch** (1 Payment → banyak Bill), constraint: same vendor + same currency |
| Price handling | **Strict lock dari PO** — harga tidak bisa diubah di Bill; tidak ada price variance posting |
| GR reference scope | **Multi-PO** diperbolehkan, constraint: same vendor + same currency |
| Purchase Return | **Defer ke Sprint 5+** |
| Payment UX flow | **Payment-centric**: user buat Payment dari menu VP, lalu pilih Bill mana yang di-cover |
| Metode pembayaran | Pilih **BankAccount** dari master (entitas terpisah dari COA) |
| BankAccount → COA | Tambahkan field `coaId` ke BankAccount master agar journal fleksibel per bank |
| `line_total` formula | **Gross proportional** dari GR: `qty_billed × (gr_line.grIrAmount / gr_line.quantityReceived)` — BUKAN `qty × unit_price` net |
| `qty_billed` tracking | **Computed on-the-fly** dari `vendor_bill_lines` (DDD-clean, AP tidak tulis ke tabel GR) |
| Cancel setelah CONFIRMED | **Blokir** — reversal journal defer ke Sprint 5+; CONFIRMED tidak bisa di-cancel |
| Approval flow | **Tidak ada** — cukup permission-based (`VENDOR_BILL_CONFIRM` hanya untuk role tertentu) |
| Menu placement | **Menu baru: Accounts Payable (AP)** — sejajar dengan Purchasing di sidebar |
| `vendor_invoice_number` unik | **Ya** — `UNIQUE (vendor_id, vendor_invoice_number)` di DB, cegah double-entry invoice |
| `due_date` | **Wajib diisi** (NOT NULL), input manual, validasi `due_date >= bill_date` |
| Billing status di detail GR | **Ya** — tampilkan per line: Belum Di-bill / Partial Billed / Fully Billed (computed, tidak disimpan) |
| PO status → BILLED trigger | **Defer ke Sprint 5+** — `ConfirmVendorBillUseCase` tidak update status PO |
| Filter list page VB & VP | **Vendor + Status + Bill Date + Document Number + Due Date** |

---

## Arsitektur: Database Schema

> Next Flyway migration: mulai dari **V57**

### V57 — BankAccount Enhancement

```sql
ALTER TABLE bank_accounts
    ADD COLUMN coa_id BIGINT REFERENCES acc_coa(id);
```

### V58 — Add Vendor Bill Module

```sql
-- Vendor Bill Header
CREATE TABLE vendor_bills (
    id                    BIGSERIAL PRIMARY KEY,
    bill_number           VARCHAR(20)    NOT NULL UNIQUE,  -- VB-yymm-XXXXX
    vendor_invoice_number VARCHAR(100)   NOT NULL,         -- nomor dari vendor
    vendor_id             BIGINT         NOT NULL REFERENCES parties(id),
    currency_id           BIGINT         NOT NULL REFERENCES currencies(id),
    bill_date             DATE           NOT NULL,
    due_date              DATE           NOT NULL,   -- wajib diisi; due_date >= bill_date
    status                VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    -- status: DRAFT | CONFIRMED | PARTIAL_PAID | PAID | CANCELLED
    -- ⚠️ CANCELLED hanya valid dari DRAFT; CONFIRMED tidak bisa di-cancel di Sprint 5
    subtotal              NUMERIC(19,2)  NOT NULL DEFAULT 0,
    tax_amount            NUMERIC(19,2)  NOT NULL DEFAULT 0,
    total_amount          NUMERIC(19,2)  NOT NULL DEFAULT 0,
    paid_amount           NUMERIC(19,2)  NOT NULL DEFAULT 0,
    outstanding_amount    NUMERIC(19,2)  NOT NULL DEFAULT 0,
    notes                 TEXT,
    version               BIGINT         NOT NULL DEFAULT 0,
    created_by            BIGINT         REFERENCES users(id),
    created_date          TIMESTAMP,
    updated_by            BIGINT         REFERENCES users(id),
    updated_date          TIMESTAMP
);

-- Bridge: Bill ↔ GR (many-to-many)
CREATE TABLE vendor_bill_gr_refs (
    bill_id  BIGINT NOT NULL REFERENCES vendor_bills(id) ON DELETE CASCADE,
    gr_id    BIGINT NOT NULL REFERENCES goods_receipts(id),
    PRIMARY KEY (bill_id, gr_id)
);

-- Bill Lines (satu per GR line yang di-bill)
CREATE TABLE vendor_bill_lines (
    id                BIGSERIAL PRIMARY KEY,
    bill_id           BIGINT         NOT NULL REFERENCES vendor_bills(id) ON DELETE CASCADE,
    gr_line_id        BIGINT         NOT NULL REFERENCES gr_lines(id),
    product_id        BIGINT,
    product_name      VARCHAR(255),  -- snapshot
    description       VARCHAR(500),
    qty_billed        NUMERIC(19,4)  NOT NULL,
    uom_id            BIGINT,
    uom_name          VARCHAR(50),   -- snapshot
    unit_price        NUMERIC(19,4)  NOT NULL,  -- snapshot dari PO/GR (net, untuk display)
    inventory_amount  NUMERIC(19,2)  NOT NULL,  -- proportional inventoryAmt dari GR line (informational)
    tax_amount        NUMERIC(19,2)  NOT NULL DEFAULT 0, -- selalu 0 (sudah diklaim di GR, informational)
    line_total        NUMERIC(19,2)  NOT NULL,  -- ← GROSS: qty_billed × (gr_line.grIrAmount / gr_line.quantityReceived)
    -- line_total adalah satu-satunya yang dipakai untuk VB_GRIR_CLEARING_AMT
    version           BIGINT         NOT NULL DEFAULT 0
);
-- PENTING: line_total ≠ (inventory_amount + tax_amount)
-- line_total = proporsi grIrAmount dari GR line berdasarkan qty_billed / qty_received
-- Ini memastikan DR GR/IR Clearing di VB = tepat CR yang diposting saat GR

-- Sequence + Permissions (insert ke system_sequences, permission_groups, permissions, role_permissions)
-- (lihat V50 sebagai contoh pattern)

-- Tambahan constraint vendor_invoice_number unik per vendor:
ALTER TABLE vendor_bills
    ADD CONSTRAINT uq_vendor_bills_invoice_number UNIQUE (vendor_id, vendor_invoice_number);

### V59 — Add Vendor Payment Module

```sql
-- Vendor Payment Header
CREATE TABLE vendor_payments (
    id              BIGSERIAL PRIMARY KEY,
    payment_number  VARCHAR(20)    NOT NULL UNIQUE,  -- VP-yymm-XXXXX
    vendor_id       BIGINT         NOT NULL REFERENCES parties(id),
    currency_id     BIGINT         NOT NULL REFERENCES currencies(id),
    payment_date    DATE           NOT NULL,
    bank_account_id BIGINT         NOT NULL REFERENCES bank_accounts(id),
    total_amount    NUMERIC(19,2)  NOT NULL DEFAULT 0,
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    -- status: DRAFT | CONFIRMED | CANCELLED
    notes           TEXT,
    version         BIGINT         NOT NULL DEFAULT 0,
    created_by      BIGINT         REFERENCES users(id),
    created_date    TIMESTAMP,
    updated_by      BIGINT         REFERENCES users(id),
    updated_date    TIMESTAMP
);

-- Payment ↔ Bill Allocation (many-to-many dengan jumlah)
CREATE TABLE vendor_payment_allocations (
    id               BIGSERIAL PRIMARY KEY,
    payment_id       BIGINT         NOT NULL REFERENCES vendor_payments(id) ON DELETE CASCADE,
    bill_id          BIGINT         NOT NULL REFERENCES vendor_bills(id),
    allocated_amount NUMERIC(19,2)  NOT NULL,
    -- constraint: allocated_amount ≤ bill.outstanding_amount pada waktu alokasi
    UNIQUE (payment_id, bill_id)
);
```

---

## Arsitektur: Package Java

Ikuti pola existing — bounded context baru di root level:

```
com.solusi.erp.accountspayable
├── vendorbill
│   ├── domain
│   │   ├── model
│   │   │   ├── VendorBill.java              (Aggregate Root)
│   │   │   ├── VendorBillLine.java
│   │   │   ├── VendorBillStatus.java        (enum: DRAFT, CONFIRMED, PARTIAL_PAID, PAID, CANCELLED)
│   │   │   └── VendorBillGrRef.java
│   │   ├── repository
│   │   │   └── VendorBillRepository.java
│   │   └── policy
│   │       └── VendorBillJournalPolicy.java  (mirip GoodsReceiptJournalPolicy)
│   ├── application
│   │   └── usecase
│   │       ├── command
│   │       │   ├── CreateVendorBillUseCase(Impl).java
│   │       │   ├── UpdateVendorBillUseCase(Impl).java
│   │       │   ├── ConfirmVendorBillUseCase(Impl).java   ← trigger auto-journal
│   │       │   ├── CancelVendorBillUseCase(Impl).java
│   │       │   └── DeleteVendorBillUseCase(Impl).java
│   │       └── query
│   │           ├── FindVendorBillsUseCase(Impl).java
│   │           ├── GetVendorBillDetailUseCase(Impl).java
│   │           └── GetVendorBillCreateViewUseCase(Impl).java  ← prefill dari GR
│   ├── infrastructure
│   │   ├── persistence
│   │   │   ├── VendorBillEntity.java
│   │   │   ├── VendorBillLineEntity.java
│   │   │   ├── VendorBillGrRefEntity.java
│   │   │   ├── VendorBillJpaRepository.java
│   │   │   └── VendorBillPersistenceMapper.java
│   │   ├── adapter
│   │   │   └── VendorBillRepositoryImpl.java
│   │   └── config
│   │       └── VendorBillConfig.java
│   └── web
│       ├── controller
│       │   └── VendorBillController.java
│       ├── dto
│       │   ├── VendorBillSaveRequest.java
│       │   ├── VendorBillDetailResponse.java
│       │   └── VendorBillSummaryResponse.java
│       └── mapper
│           └── VendorBillWebMapper.java
└── vendorpayment
    └── (struktur serupa)
```

---

## Arsitektur: Status Lifecycle

### Vendor Bill

```
DRAFT ──► CONFIRMED ──► PARTIAL_PAID ──► PAID
  │
  └──► CANCELLED  (hanya dari DRAFT — CONFIRMED tidak bisa di-cancel di Sprint 5)
```

| Status | Keterangan |
|---|---|
| `DRAFT` | Bisa diedit: tambah/hapus GR refs, ubah qty_billed, hapus |
| `CONFIRMED` | Journal VENDOR_BILL ter-post; tidak bisa diedit atau di-cancel |
| `PARTIAL_PAID` | Ada alokasi payment, tapi outstanding > 0 |
| `PAID` | `outstanding_amount = 0` |
| `CANCELLED` | **Hanya dari DRAFT** — reversal dari CONFIRMED defer ke Sprint 5+ |

### Vendor Payment

```
DRAFT ──► CONFIRMED
  │
  └──► CANCELLED  (hanya dari DRAFT)
```

| Status | Keterangan |
|---|---|
| `DRAFT` | Bisa edit alokasi bill, ubah jumlah, hapus |
| `CONFIRMED` | Journal VENDOR_PAYMENT ter-post; bills' `paid_amount` & `outstanding_amount` diupdate |
| `CANCELLED` | Hanya dari DRAFT (setelah CONFIRMED tidak bisa reverse di Sprint 5) |

---

## Business Rules: Three-Way Match

```
Syarat GR bisa dipilih di Vendor Bill:
  1. GR.status = COMPLETED
  2. GR.vendor_id = bill.vendor_id
  3. GR.currency_id = bill.currency_id
  4. Masih ada GR line dengan outstanding qty > 0

Outstanding qty per GR line (computed on-the-fly, DDD-clean):
  outstanding_qty = gr_line.quantity_received
                  - SUM(vbl.qty_billed)
                    FROM vendor_bill_lines vbl
                    JOIN vendor_bills vb ON vbl.bill_id = vb.id
                    WHERE vbl.gr_line_id = gr_line.id
                      AND vb.status NOT IN ('DRAFT', 'CANCELLED')

Syarat line qty_billed valid:
  qty_billed > 0
  qty_billed ≤ outstanding_qty (computed di atas)

Formula line_total (PENTING — harus GROSS, bukan net):
  line_total = qty_billed × (gr_line.grIrAmount / gr_line.quantityReceived)
             = proporsi gross GR/IR amount yang di-bill

  Bukan: qty_billed × unit_price  ← SALAH (hanya net, GR/IR tidak akan balance)

  ⚠️ ROUNDING RULE (cegah akumulasi floating point):
  Jika satu GR line di-bill dalam beberapa bill terpisah (partial → partial → sisa),
  bill TERAKHIR untuk GR line tersebut harus pakai REMAINDER:
    line_total_last = gr_line.grIrAmount - SUM(line_total dari bill sebelumnya yang sudah CONFIRMED)
  Ini memastikan Σ(semua line_total per GR line) = grIrAmount persis.

Harga di bill line:
  unit_price = snapshot dari GR line (yang berasal dari PO)
  Tidak bisa diubah user → strict price lock

tax_amount di bill line = 0 selalu
  (Input VAT sudah diklaim saat GR → tidak double claim)
  Disimpan sebagai informational: proportional gr_line.taxAmount

inventory_amount di bill line = informational:
  inventory_amount = qty_billed × (gr_line.inventoryAmount / gr_line.quantityReceived)
```

---

## Business Rules: Vendor Payment

```
Syarat Bill bisa dipilih di Vendor Payment:
  1. Bill.status IN (CONFIRMED, PARTIAL_PAID)
  2. Bill.vendor_id = payment.vendor_id
  3. Bill.currency_id = payment.currency_id  ← currency di-lock dari header Payment; wajib match
  4. Bill.outstanding_amount > 0

Validasi currency di Payment header:
  - currency_id ditetapkan saat Payment dibuat
  - Hanya Bill dengan currency yang sama yang bisa dipilih
  - Saat user pilih Bill di modal, filter wajib by currency_id yang sudah dipilih
  - Jika currency belum dipilih, tampilkan semua; setelah dipilih, filter diterapkan

Syarat alokasi valid:
  allocated_amount > 0
  allocated_amount ≤ bill.outstanding_amount

Total payment:
  payment.total_amount = Σ allocated_amount semua bill yang dipilih

Saat payment CONFIRMED:
  foreach allocation:
    bill.paid_amount += allocation.allocated_amount
    bill.outstanding_amount -= allocation.allocated_amount
    if bill.outstanding_amount = 0: bill.status = PAID
    else: bill.status = PARTIAL_PAID
```

---

## Auto-Journal: Schema Event Types

### VENDOR_BILL

Trigger: `ConfirmVendorBillUseCase` selesai dieksekusi.

| Variable | Akun COA | Posisi |
|---|---|---|
| `VB_GRIR_CLEARING_AMT` | 2120 — GR/IR Clearing | **Debit** |
| `VB_TAX_AMT` | 1230 — Tax Receivable (Input VAT) | **Debit** *(selalu 0, baris skip otomatis)* |
| `VB_AP_TOTAL` | 2110 — Accounts Payable | **Credit** |

> `VB_GRIR_CLEARING_AMT = VB_AP_TOTAL = Σ bill_lines.line_total`  
> `line_total` per baris = **gross proportional** dari grIrAmount GR line  
> Jurnal ini membalik **persis** sisi Credit yang diposting saat GR (balance sempurna).  
>
> **Contoh:** GR 10 unit, gross Rp 1.110.000. Bill 5 unit → DR GR/IR 555.000 = CR AP 555.000.  
> Sisa 5 unit di GR/IR = Rp 555.000 (menunggu bill berikutnya).

### VENDOR_PAYMENT

Trigger: `ConfirmVendorPaymentUseCase` selesai dieksekusi.

| Variable | Akun COA | Posisi |
|---|---|---|
| `VP_AP_AMT` | 2110 — Accounts Payable | **Debit** |
| `VP_BANK_OUT_AMT` | bank_account.coa_id (dinamis) | **Credit** |

> `VP_AP_AMT = VP_BANK_OUT_AMT = payment.total_amount`  
> COA kredit diambil dari `bank_account.coaId` — itulah kenapa BankAccount perlu di-enhance.

---

## Enhancement BankAccount Master

Field baru yang perlu ditambahkan:

```java
// BankAccount domain model
private Long coaId;
private String coaCode;  // snapshot untuk display
private String coaName;  // snapshot untuk display
```

- `coaId` wajib diisi saat create/update BankAccount
- Saat VP journal dipost, lookup `bank_account.coaId` untuk menentukan akun kredit
- **MVP Note:** Satu BankAccount → satu COA cukup untuk Sprint 5. Extension point untuk multi-COA (e.g., sub-akun per currency) direncanakan di Sprint berikutnya.

---

## User Flow Lengkap

### Vendor Bill

```
1. Buka menu Accounts Payable → Vendor Bills → + Tambah Baru
2. Pilih Vendor (autocomplete)
3. Currency otomatis terisi dari Vendor default, bisa diubah
4. Input vendor_invoice_number (nomor dari fisik invoice vendor)
5. Isi bill_date dan due_date
6. Klik "Tambah GR" → modal selector:
   - Filter: hanya GR COMPLETED dari vendor + currency yang sama
   - Filter tambahan: hanya GR yang masih punya outstanding qty (belum fully billed)
   - User centang satu atau lebih GR
7. Setelah GR dipilih, bill_lines otomatis ter-populate:
   - product, description, qty_received, unit_price dari GR/PO
   - qty_billed default = outstanding qty (bisa dikurangi jika vendor invoice partial)
   - tax_amount = 0 (read-only)
8. Simpan → status DRAFT
9. Review detail, klik "Konfirmasi"
10. Konfirmasi dialog → ConfirmVendorBillUseCase:
    - Validasi period OPEN
    - Post journal VENDOR_BILL
    - Update bill status → CONFIRMED
11. Link ke journal detail tersedia di halaman detail Bill
```

### Vendor Payment

```
1. Buka menu Accounts Payable → Vendor Payments → + Tambah Baru
2. Pilih Vendor (autocomplete)
3. Pilih Currency (constraint: harus sama dengan bill yang mau dibayar)
4. Isi payment_date
5. Pilih Bank Account (dari master, filtered by currency match dengan COA)
6. Klik "Pilih Bill" → modal selector:
   - Filter: Bill CONFIRMED/PARTIAL_PAID dari vendor + currency yang sama
   - Tampilkan: bill_number, vendor_invoice_number, total, outstanding_amount
   - User centang satu atau lebih Bill
7. Per Bill yang dipilih, isi allocated_amount:
   - Default = outstanding_amount (full pay)
   - Bisa dikurangi untuk partial
8. total_amount = Σ allocated_amount (otomatis terhitung)
9. Simpan → status DRAFT
10. Klik "Konfirmasi" → ConfirmVendorPaymentUseCase:
    - Validasi period OPEN
    - Post journal VENDOR_PAYMENT
    - Update setiap bill: paid_amount, outstanding_amount, status
    - Update payment status → CONFIRMED
11. Link ke journal detail tersedia di halaman detail Payment
```

---

## Urutan Implementasi yang Disarankan

### Fase 1: Enhancement BankAccount (prerequisite)
1. Tambah `coaId` ke BankAccount domain, entity, migration (V57)
2. Update form BankAccount (tambah field COA selector)
3. Update mapper + test

### Fase 2: Vendor Bill Core
4. Flyway V58 (tables + sequences + permissions)
5. Domain model: `VendorBill`, `VendorBillLine`, `VendorBillStatus`, enum `VendorBillGrRef`
6. Repository + JPA entity + mapper
7. Use cases: Create, Update, Delete, Cancel, GetCreateView (dengan GR picker)
8. `ConfirmVendorBillUseCase` + `VendorBillJournalPolicy`
    - ⚠️ **Concurrency guard**: re-validate `outstanding_qty` per GR line dengan `version` lock sebelum post journal — cegah race condition jika dua user confirm bill yang sama GR line secara bersamaan
9. Tambah `VENDOR_BILL` ke `SchemaEventType` enum + `VB_*` ke `JournalVariable` enum
10. Controller + Web layer
11. Thymeleaf templates: list, form (create/edit), detail
12. Unit tests domain + use case + web layer

### Fase 3: Vendor Payment Core
13. Flyway V59 (tables + sequences + permissions)
14. Domain model: `VendorPayment`, `VendorPaymentAllocation`, `VendorPaymentStatus`
15. Repository + JPA entity + mapper
16. Use cases: Create, Update, Delete, Cancel, GetCreateView (bill picker)
17. `ConfirmVendorPaymentUseCase` + logic update bill paid_amount/status
18. Tambah `VENDOR_PAYMENT` ke `SchemaEventType` enum + `VP_*` ke `JournalVariable` enum
19. Controller + Web layer
20. Thymeleaf templates: list, form (create/edit dengan allocation table), detail
21. Unit tests + integration tests

### Fase 4: Integrasi & Linking
22. Detail PO → link ke Vendor Bills (filter by PO's GRs)
23. Detail GR → tampilkan **billing status per line** (Belum Di-bill / Partial Billed / Fully Billed) — computed via query ke `vendor_bill_lines`, tidak disimpan di `gr_lines`
24. Detail Bill → link ke Payment history

---

## Semua Pertanyaan Terbuka — Resolved ✅

| Pertanyaan | Keputusan |
|---|---|
| `vendor_invoice_number` unik per vendor? | ✅ **Ya** — `UNIQUE (vendor_id, vendor_invoice_number)` in V58 |
| `due_date` wajib atau opsional? Bisa di-derive? | ✅ **Wajib, input manual**; `due_date >= bill_date`; tidak auto-derive dari PO |
| Tampilan billing status di detail GR? | ✅ **Ya** — per line: Belum Di-bill / Partial Billed / Fully Billed (computed, tidak disimpan) |
| PO status → BILLED otomatis di Sprint 5? | ✅ **Defer ke Sprint 5+** — ConfirmVendorBillUseCase tidak update status PO |
| Filter list page VB & VP? | ✅ **Vendor + Status + Bill Date + Document Number + Due Date** |

---

**Keputusan yang sudah di-close di sesi review:**

- ✅ Cancel setelah CONFIRMED: **diblokir** (reversal defer Sprint 5+)
- ✅ Cancel Payment setelah CONFIRMED: **diblokir** (sama)
- ✅ Approval flow: **tidak ada** — cukup permission-based
- ✅ Menu placement: **AP menu baru** sejajar Purchasing
- ✅ `qty_billed` tracking: **computed on-the-fly** (DDD-clean)
- ✅ `line_total` formula: **gross proportional dari grIrAmount**

---

## Temuan Critic Agent (Independent Review)

Critic agent dijalankan secara independen — berikut temuan yang sudah direspon/incorporated ke dokumen ini:

| # | Severity | Temuan | Status |
|---|---|---|---|
| 1 | 🔴 KRITIKAL | **Rounding akumulatif**: partial billing GR line lintas beberapa bill bisa mengakumulasi floating point error — Σ(line_total) ≠ grIrAmount | ✅ Fixed — tambah ROUNDING RULE: bill terakhir pakai remainder method |
| 2 | 🔴 KRITIKAL | **PO status → BILLED** tidak terdefinisi: ConfirmVendorBillUseCase tidak menyebutkan apakah update PO status | ✅ Resolved — defer ke Sprint 5+, PO status tidak diupdate di Sprint 5 |
| 3 | 🟡 PENTING | **`UNIQUE (vendor_id, vendor_invoice_number)`** belum ada di V58 SQL schema | ✅ Fixed — constraint ditambahkan ke V58 |
| 4 | 🟡 PENTING | **Payment currency vs Bill currency mismatch** — perlu validasi eksplisit di VP business rules | ✅ Fixed — ditambahkan currency lock + filter logic ke VP business rules |
| 5 | 🟡 PENTING | **`due_date` derivation** tidak jelas | ✅ Resolved — mandatory manual, tidak auto-derive, >= bill_date |
| 6 | 🟡 PENTING | **BankAccount single-COA** adalah MVP decision yang perlu didokumentasikan sebagai extension point | ✅ Fixed — MVP note ditambahkan ke section BankAccount Enhancement |
| 7 | 🟡 PENTING | **Status lifecycle contradistortion**: diagram menunjukkan `CONFIRMED → CANCELLED` tapi keputusan memblokir path itu | ✅ Fixed — diagram & tabel dikoreksi, CANCELLED hanya dari DRAFT |
| 8 | 🔵 MINOR | **Race condition** di `ConfirmVendorBillUseCase`: dua user confirm bill dengan GR line sama bersamaan | ✅ Fixed — concurrency guard note ditambahkan ke implementasi use case |

---

## Referensi

- [Sprint 5 Roadmap](../roadmap/sprint-5-vendor-bill.md)
- [Purchase Order Spec](../modules/procurement/purchase-order.md)
- [Goods Receipt Spec](../modules/inventory/goods-receipt.md)
- [Journal Entry Spec](../modules/accounting/journal-entry.md)
