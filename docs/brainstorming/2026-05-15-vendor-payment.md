# Vendor Payment — Design Brainstorming

Tanggal: 2026-05-15
Sprint: 5 (Accounts Payable)

## Executive Summary

Vendor Payment adalah dokumen pembayaran ke supplier yang mengalokasikan dana dari Bank Account ke satu atau lebih Vendor Bill yang masih outstanding. Fitur ini melengkapi siklus AP: PO → GR → Vendor Bill → **Vendor Payment**.

Desain mengikuti pattern 1 Payment : N Bills dengan partial payment support. Setiap payment confirm akan mem-post journal `VENDOR_PAYMENT` secara otomatis dan mengupdate status pembayaran pada Vendor Bill terkait.

---

## 1. Pre-requisite: Bank Account Refactor

Sebelum Vendor Payment bisa diimplementasi, master Bank Account perlu ditambahkan field berikut:

| Field | Tipe | Keterangan |
|-------|------|------------|
| `currencyId` | Long (FK → currencies) | Currency rekening bank |
| `coaId` | Long (FK → acc_chart_of_accounts) | Link ke akun buku besar untuk journal posting |
| `accountType` | Enum: `CASH`, `BANK_TRANSFER`, `GIRO`, `CREDIT_CARD` | Refactor dari String ke Enum |

Relasi COA ↔ Bank Account:
- **Many-to-One**: banyak bank account bisa point ke 1 COA, tapi 1 bank account hanya punya 1 COA.
- Contoh: COA "1120 Main Bank Account" bisa punya 2 rekening (IDR dan USD).

### Seeder Update

Bank Account seeder (`D060__master_bank_accounts.sql`) perlu diupdate:
- `BA-DEMO-01` (BCA Operasional) → `currencyId` = IDR, `coaId` = 1120, `accountType` = `BANK_TRANSFER`
- `BA-DEMO-02` (Mandiri Payroll) → `currencyId` = IDR, `coaId` = 1130, `accountType` = `BANK_TRANSFER`
- `BA-DEMO-03` (Kas Pusat) → `currencyId` = IDR, `coaId` = 1110, `accountType` = `CASH`
- `BA-DEMO-04` (Rekening Supplier) → `currencyId` = IDR, `coaId` = NULL (bukan milik perusahaan), `accountType` = `BANK_TRANSFER`

---

## 2. Domain Model

### A. VendorPayment (Aggregate Root)

| Field | Tipe | Keterangan |
|-------|------|------------|
| `id` | Long | PK |
| `code` | String | Auto-generated dari sequence `VENDOR-PAYMENT` |
| `vendorId` | Long | FK → parties |
| `currencyId` | Long | Currency pembayaran |
| `bankAccountId` | Long | FK → bank_accounts (sumber dana) |
| `paymentDate` | LocalDate | Tanggal pembayaran, default today |
| `exchangeRate` | BigDecimal | Kurs pembayaran ke base currency |
| `paymentAmount` | BigDecimal | Total amount yang ditransfer (header) |
| `status` | Enum | `DRAFT`, `CONFIRMED`, `CANCELLED` |
| `reference` | String | Nomor referensi transfer/giro (opsional) |
| `notes` | String | Catatan (opsional) |
| `lines` | List | Allocation lines |
| + audit fields | | extends BaseModel |

### B. VendorPaymentLine (Value Object / Child Entity)

| Field | Tipe | Keterangan |
|-------|------|------------|
| `id` | Long | PK |
| `vendorBillId` | Long | FK → vendor_bills yang dibayar |
| `billCode` | String | Snapshot VB code untuk display |
| `outstandingAmount` | BigDecimal | Outstanding saat payment dibuat (snapshot) |
| `paidAmount` | BigDecimal | Jumlah yang dialokasikan ke bill ini |

### C. Status Enum

```java
public enum VendorPaymentStatus {
    DRAFT,
    CONFIRMED,
    CANCELLED
}
```

---

## 3. Business Flow

### Create Flow

```
1. User masuk halaman create
2. Pilih Vendor (autocomplete)
3. Pilih Currency (autocomplete, seperti PO)
4. Pilih Bank Account (modal selector, filtered by: isActive + currencyId match + coaId NOT NULL)
   → Payment Type otomatis terisi readonly dari bank account
5. Input Payment Date (flatpickr, default today)
6. Input Exchange Rate (readonly=1 jika base currency)
7. Sistem auto-load semua VB CONFIRMED/PARTIAL_PAID milik vendor+currency dengan outstanding > 0
8. User input Payment Amount di header
9. User alokasikan amount per bill di lines
10. Recap section update real-time:
    - Payment Amount: [dari header]
    - Applied: [sum of line paidAmount]
    - Unapplied: [payment amount - applied]
11. Save draft
```

### Behavior Rules

- Ganti Vendor ATAU Currency → lines di-reset (clear semua allocation)
- Lines yang paidAmount = 0 → tidak disimpan ke DB
- Edit: DRAFT fully editable (sama seperti create flow)
- Delete: hanya dari list, hanya status DRAFT

### Lifecycle

```
DRAFT → CONFIRMED
DRAFT → CANCELLED
```

CONFIRMED tidak bisa diedit, dihapus, atau di-cancel (cancel/void = deferred).

---

## 4. Validation Rules (Domain-level)

| Rule | Error Message Key |
|------|-------------------|
| Payment Amount > 0 | `msg.err.vp.amount.required` |
| Minimal 1 line dengan paidAmount > 0 | `msg.err.vp.lines.required` |
| Per line: paidAmount ≤ outstanding | `msg.err.vp.line.exceeds.outstanding` |
| Sum of line paidAmount = Payment Amount (unapplied must be 0) | `msg.err.vp.amount.mismatch` |
| Bank Account harus punya coaId | `msg.err.vp.bank.no.coa` |
| Bank Account currency harus match header currency | `msg.err.vp.bank.currency.mismatch` |
| VB yang dialokasikan harus milik vendor yang sama | `msg.err.vp.bill.vendor.mismatch` |

---

## 5. Vendor Bill Status Update

Saat Vendor Payment di-confirm, sistem menghitung total paid per VB:

```
total_paid = SUM(paidAmount) dari semua VendorPaymentLine CONFIRMED yang merujuk VB tersebut
```

Update `VendorBillStatus`:
- `total_paid = 0` → `CONFIRMED` (tetap, belum ada payment)
- `0 < total_paid < totalAmount` → `PARTIAL_PAID`
- `total_paid = totalAmount` → `PAID`

Field `PARTIAL_PAID` dan `PAID` sudah ada di enum `VendorBillStatus` yang existing.

---

## 6. Journal Posting

### Event: `VENDOR_PAYMENT`

Saat confirm, post journal dengan variable:

| Variable | Nilai | Posisi |
|----------|-------|--------|
| `VP_AP_AMT` | Total applied amount dalam base currency (at VB rate) | Debit |
| `VP_BANK_OUT_AMT` | Total payment amount dalam base currency (at payment rate) | Credit |
| `VP_FX_LOSS_AMT` | FX loss jika payment rate > VB rate | Debit |
| `VP_FX_GAIN_AMT` | FX gain jika payment rate < VB rate | Credit |

### Contoh Journal

Skenario: VB rate 15,800 IDR/USD, Payment rate 15,900 IDR/USD, paying USD 10,000

```
DR  Accounts Payable (2110)     158,000,000  (10,000 × 15,800)
DR  Foreign Exchange Loss (5140)  1,000,000  (10,000 × (15,900 - 15,800))
CR  Main Bank Account (1120)    159,000,000  (10,000 × 15,900)
```

Skenario single currency (IDR, rate = 1):

```
DR  Accounts Payable (2110)     10,000,000
CR  Main Bank Account (1120)    10,000,000
```

### FX Calculation

```
fx_variance = paymentAmount × (paymentRate - weightedAvgVbRate)

Jika fx_variance > 0 → FX Loss (debit)
Jika fx_variance < 0 → FX Gain (credit, absolute value)
Jika fx_variance = 0 → tidak ada FX entry
```

Weighted average VB rate dihitung dari rate masing-masing VB yang dibayar, proporsional terhadap paidAmount.

### JournalVariable Addition

Perlu tambahkan ke enum `JournalVariable`:
- `VP_FX_LOSS_AMT(SchemaEventType.VENDOR_PAYMENT)`
- `VP_FX_GAIN_AMT(SchemaEventType.VENDOR_PAYMENT)`

(Note: `VP_AP_AMT` dan `VP_BANK_OUT_AMT` sudah ada)

---

## 7. UI/UX Specification

### List View (`/accounts-payable/vendor-payments`)

| Kolom | Keterangan |
|-------|------------|
| Code | Link ke detail |
| Vendor | Nama vendor |
| Payment Date | Tanggal bayar |
| Bank Account | Nama bank + nomor rekening |
| Payment Amount | Formatted numeric |
| Status | Badge (DRAFT/CONFIRMED/CANCELLED) |

Filter: keyword, vendor, status
Action: Delete (DRAFT only, dari list via HTMX row delete)

### Create/Edit Form

```
┌─────────────────────────────────────────────────────────┐
│ HEADER                                                   │
├─────────────────────────────────────────────────────────┤
│ Code           : [auto-generated, readonly, bg-light]    │
│ Vendor         : [autocomplete]                          │
│ Currency       : [autocomplete, seperti PO]              │
│ Bank Account   : [modal selector popup]                  │
│   → Payment Type: [readonly, auto-fill dari bank acct]  │
│ Payment Date   : [flatpickr, default today]              │
│ Exchange Rate  : [input / readonly=1 jika base curr]     │
│ Payment Amount : [erp-number input]                      │
│ Reference      : [text input, opsional]                  │
│ Notes          : [textarea, opsional]                    │
├─────────────────────────────────────────────────────────┤
│ RECAP (live JS update)                                   │
│   Payment Amount :  10,000,000                           │
│   Applied        :   9,000,000                           │
│   Unapplied      :   1,000,000                           │
├─────────────────────────────────────────────────────────┤
│ ALLOCATION LINES (auto-loaded)                           │
├──────┬──────────┬─────────┬───────────┬────────────────┤
│ VB # │ Bill Date│ Due Date│Outstanding│ Amount to Pay  │
├──────┼──────────┼─────────┼───────────┼────────────────┤
│VB001 │2026-04-01│2026-05-01│ 5,000,000│ [erp-number]   │
│VB002 │2026-04-15│2026-05-15│ 5,000,000│ [erp-number]   │
│VB003 │2026-04-20│2026-05-20│ 5,000,000│ [erp-number]   │
└──────┴──────────┴─────────┴───────────┴────────────────┘
```

### Detail/View Page (CONFIRMED)

- Semua field readonly
- Lines menampilkan VB Code sebagai hyperlink ke `/accounts-payable/vendor-bills/{id}`
- Section bawah: link ke Journal Entry yang di-post

---

## 8. Clean Architecture Structure

```
accountspayable/
  vendorpayment/
    application/
      usecase/
        command/
          CreateVendorPaymentUseCase
          UpdateVendorPaymentUseCase
          ConfirmVendorPaymentUseCase
          CancelVendorPaymentUseCase
          DeleteVendorPaymentUseCase
        query/
          GetVendorPaymentListUseCase
          GetVendorPaymentDetailUseCase
          GetPayableVendorBillsUseCase  ← load outstanding VBs
    domain/
      model/
        VendorPayment
        VendorPaymentLine
        VendorPaymentStatus
      port/
        PayableVendorBillQueryPort  ← cross-slice read port
        VendorBillPaymentUpdatePort ← cross-slice write port (update VB status)
      repository/
        VendorPaymentRepository
    infrastructure/
      adapter/
        PayableVendorBillQueryAdapter
        VendorBillPaymentUpdateAdapter
        VendorPaymentRepositoryImpl
      config/
        VendorPaymentConfig
      persistence/
        VendorPaymentEntity
        VendorPaymentLineEntity
        VendorPaymentJpaRepository
        VendorPaymentPersistenceMapper
    web/
      controller/
        VendorPaymentController
      dto/
        VendorPaymentSaveRequest
        VendorPaymentLineRequest
        VendorPaymentSummaryResponse
        VendorPaymentDetailResponse
        PayableVendorBillResponse  ← DTO untuk outstanding VB list
      mapper/
        VendorPaymentWebMapper
```

### Cross-Slice Ports

Vendor Payment tidak boleh inject VendorBill repository langsung. Akses via port:

- `PayableVendorBillQueryPort`: query VB CONFIRMED/PARTIAL_PAID dengan outstanding > 0 per vendor+currency
- `VendorBillPaymentUpdatePort`: update status VB (PARTIAL_PAID/PAID) setelah payment confirm

---

## 9. Accounting Schema Seeder

Perlu tambahkan schema untuk event `VENDOR_PAYMENT`:

| Event | Variable | Account | Position |
|-------|----------|---------|----------|
| VENDOR_PAYMENT | VP_AP_AMT | 2110 (Accounts Payable) | DEBIT |
| VENDOR_PAYMENT | VP_BANK_OUT_AMT | *dynamic from bank account COA* | CREDIT |
| VENDOR_PAYMENT | VP_FX_LOSS_AMT | 5140 (Foreign Exchange Loss) | DEBIT |
| VENDOR_PAYMENT | VP_FX_GAIN_AMT | 4240 (Foreign Exchange Gain) | CREDIT |

**Resolved — Account Override Map (Opsi B):**

`VP_BANK_OUT_AMT` membutuhkan COA dinamis dari Bank Account yang dipilih user. Solusi: tambahkan field `accountOverrides` di `JournalPostingCommand`:

```java
public record JournalPostingCommand(
    // ... existing fields ...
    Map<JournalVariable, BigDecimal> values,
    Long originalCurrencyId,
    BigDecimal exchangeRate,
    Map<JournalVariable, BigDecimal> originalValues,
    Map<JournalVariable, Long> accountOverrides  // NEW
)
```

Engine logic (di `PostJournalForEventUseCaseImpl`):

```java
Long accountId = command.accountOverrides() != null
    ? command.accountOverrides().getOrDefault(schemaLine.getVar(), schemaLine.getAccountId())
    : schemaLine.getAccountId();
```

Keuntungan:
- **Backward compatible** — event lain (GR, VB) tidak kirim override, tetap pakai schema COA
- Schema tetap bermakna sebagai default/template (VP_BANK_OUT_AMT → COA 1120 sebagai default)
- Perubahan engine minimal (1 field + 2 baris logic)
- Reusable untuk Customer Receipt nanti (CR juga dynamic bank COA)

---

## 10. Permission & Menu

Permission set:
- `VENDOR-PAYMENT_READ`
- `VENDOR-PAYMENT_CREATE`
- `VENDOR-PAYMENT_UPDATE`
- `VENDOR-PAYMENT_DELETE`

Menu: Finance & Accounting > Account Payable > Vendor Payment
URL: `/accounts-payable/vendor-payments`

PermissionGroup: buat entry baru atau gabung dengan group AP existing.

---

## 11. Deferred Items

| Item | Alasan Defer | Target Sprint |
|------|-------------|---------------|
| Cancel/Void Payment setelah CONFIRMED | Kompleksitas rollback status VB + reversal journal | Sprint 6+ |
| Down Payment / Payment on Account | Flow terpisah, perlu prepayment account dan allocation later | Sprint 7+ |
| Bank Reconciliation | Butuh statement import dan matching engine | Sprint 8+ |
| Payment Approval Workflow | Bisa ditambahkan setelah core flow stabil | Sprint 6+ |

---

## 12. Implementation Order (Recommended)

1. **Bank Account Refactor** — tambah `currencyId`, `coaId`, enum `PaymentType` (refactor dari String `accountType`) + migration + seeder update
2. **Journal Engine Enhancement** — tambah field `accountOverrides` di `JournalPostingCommand` + logic override di `PostJournalForEventUseCaseImpl`
3. **JournalVariable** — tambah `VP_FX_LOSS_AMT`, `VP_FX_GAIN_AMT`
4. **Accounting Schema Seeder** — tambah schema lines untuk `VENDOR_PAYMENT` (default COA 1120 untuk `VP_BANK_OUT_AMT`)
5. **Flyway Migration** — DDL table `vendor_payments` + `vendor_payment_lines`
6. **Domain Model** — `VendorPayment`, `VendorPaymentLine`, `VendorPaymentStatus`, ports
7. **Infrastructure** — entity, JPA repo, persistence mapper, cross-slice adapters
8. **Application Use Cases** — command (create, update, confirm, cancel, delete) + query (list, detail, payable bills)
9. **Web Layer** — controller, DTOs, web mapper, Thymeleaf templates (list, form, view, bank account modal selector)
10. **VB Status Update** — implement `VendorBillPaymentUpdatePort` (update PARTIAL_PAID/PAID on confirm)
11. **Testing** — unit + integration + template contract tests

---

## 13. Resolved Design Decisions

| # | Question | Decision |
|---|----------|----------|
| 1 | Dynamic COA pada journal posting | **Account Override Map** — tambah field `Map<JournalVariable, Long> accountOverrides` di `JournalPostingCommand`. Engine resolve override dulu, fallback ke schema. Backward compatible. |
| 2 | FX calculation strategy | **Per-line** — hitung FX variance per allocation line (selisih rate VB vs rate Payment × paidAmount), lalu sum semua line untuk total FX gain/loss di journal. Lebih akurat untuk audit trail. |

### FX Per-Line Calculation Detail

```
Untuk setiap VendorPaymentLine:
  vb_rate = vendorBill.exchangeRate
  vp_rate = vendorPayment.exchangeRate
  
  ap_base_amount = line.paidAmount × vb_rate       (amount at VB rate)
  bank_base_amount = line.paidAmount × vp_rate     (amount at payment rate)
  
  fx_variance = bank_base_amount - ap_base_amount
  Jika fx_variance > 0 → FX Loss (bayar lebih mahal)
  Jika fx_variance < 0 → FX Gain (bayar lebih murah)

Total journal:
  VP_AP_AMT = SUM(ap_base_amount)         → DR Accounts Payable
  VP_BANK_OUT_AMT = SUM(bank_base_amount) → CR Bank (dynamic COA)
  VP_FX_LOSS_AMT = SUM(positive fx_variance)  → DR FX Loss
  VP_FX_GAIN_AMT = SUM(|negative fx_variance|) → CR FX Gain
```
