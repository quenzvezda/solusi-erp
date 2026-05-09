# Journal Entry — Business & Functional Specification

> Modul: `Finance & Accounting > General Ledger > Journal Entry`  
> Permission: `JOURNAL-ENTRY_READ`  
> Route: `/accounting/journal-entries`

---

## 1. Ringkasan Bisnis

**Journal Entry** adalah catatan akuntansi double-entry yang merekam dampak finansial dari setiap transaksi operasional. Setiap transaksi yang mengubah posisi keuangan perusahaan (penerimaan barang, pembayaran, penjualan) **wajib** menghasilkan jurnal yang seimbang: total Debit = total Kredit.

Dalam sistem ini, journal entry dibuat secara **otomatis** (auto-posting) ketika transaksi operasional dikonfirmasi — pengguna tidak perlu membuat jurnal secara manual untuk transaksi yang didukung sistem.

---

## 2. Sumber Jurnal (Event Types)

Sistem mendukung 8 jenis event yang dapat memicu auto-posting jurnal:

| Event Type | Trigger Operasional | Status |
|---|---|---|
| `GOODS_RECEIPT` | GR dikonfirmasi (Complete) | ✅ Live |
| `VENDOR_BILL` | Invoice vendor dikonfirmasi | 🔲 Sprint 5 |
| `VENDOR_PAYMENT` | Pembayaran ke vendor dikonfirmasi | 🔲 Sprint 5 |
| `CUSTOMER_INVOICE` | Invoice ke customer dikonfirmasi | 🔲 Sprint 6+ |
| `GOODS_ISSUE` | Barang keluar gudang dikonfirmasi | 🔲 Sprint 6+ |
| `CUSTOMER_RECEIPT` | Pembayaran dari customer diterima | 🔲 Sprint 6+ |
| `STOCK_ADJUSTMENT_IN` | Penyesuaian stok positif dikonfirmasi | 🔲 Sprint 6+ |
| `STOCK_ADJUSTMENT_OUT` | Penyesuaian stok negatif dikonfirmasi | 🔲 Sprint 6+ |

---

## 3. Alur AP Cycle (Procurement → Pembayaran)

Berikut gambaran lengkap alur jurnal dalam siklus pembelian:

```
PO (disetujui)
    │  → Tidak ada jurnal
    ▼
Goods Receipt (Complete) ──────────────────────────── ✅ Auto-Journal
    │  DR Merchandise Inventory (1310)   = nilai barang
    │  DR Tax Receivable / Input VAT (1230) = PPN masukan
    │  CR GR/IR Clearing (2120)          = total bruto
    ▼
Vendor Bill (Confirm) ─────────────────────────────── 🔲 Auto-Journal
    │  DR GR/IR Clearing (2120)          = nilai GR yang di-match
    │  CR Accounts Payable (2110)        = hutang ke vendor
    ▼
Vendor Payment (Confirm) ──────────────────────────── 🔲 Auto-Journal
       DR Accounts Payable (2110)        = jumlah dibayar
       CR Main Bank Account (1120)       = kas keluar
```

### Peran GR/IR Clearing Account (2120)

GR/IR Clearing adalah akun **temporer** yang berfungsi sebagai jembatan antara penerimaan barang dan invoice vendor:

| Kondisi | Posisi GR/IR |
|---|---|
| Setelah GR, invoice belum datang | Credit (outstanding) |
| Invoice sudah di-match dengan GR | Nol / Balanced |

Jika invoice vendor berbeda nilai dari GR → selisih dicatat sebagai **price variance**.

---

## 4. Detail Jurnal per Event

### 4.1 GOODS_RECEIPT

**Trigger:** `CompleteGoodsReceiptUseCase` selesai dieksekusi.

| Variable | Akun COA | Posisi |
|---|---|---|
| `GR_INVENTORY_AMT` | 1310 — Merchandise Inventory | **Debit** |
| `GR_TAX_AMT` | 1230 — Tax Receivable (Input VAT) | **Debit** |
| `GR_GRAND_TOTAL` | 2120 — GR/IR Clearing | **Credit** |

> `GR_GRAND_TOTAL = GR_INVENTORY_AMT + GR_TAX_AMT`

Karena Input VAT diklaim pada saat GR, maka pada saat Vendor Bill, `VB_TAX_AMT = 0`.

---

### 4.2 VENDOR_BILL *(planned Sprint 5)*

**Trigger:** `ConfirmVendorBillUseCase` (belum diimplementasi).

| Variable | Akun COA | Posisi |
|---|---|---|
| `VB_GRIR_CLEARING_AMT` | 2120 — GR/IR Clearing | **Debit** |
| `VB_TAX_AMT` | 1230 — Tax Receivable (Input VAT) | **Debit** |
| `VB_AP_TOTAL` | 2110 — Accounts Payable | **Credit** |

> `VB_TAX_AMT = 0` jika tax sudah diklaim di GR.  
> `VB_TAX_AMT > 0` hanya jika invoice dibuat langsung tanpa GR sebelumnya.

---

### 4.3 VENDOR_PAYMENT *(planned Sprint 5)*

**Trigger:** `ConfirmVendorPaymentUseCase` (belum diimplementasi).

| Variable | Akun COA | Posisi |
|---|---|---|
| `VP_AP_AMT` | 2110 — Accounts Payable | **Debit** |
| `VP_BANK_OUT_AMT` | 1120 — Main Bank Account | **Credit** |

---

### 4.4 CUSTOMER_INVOICE *(planned Sprint 6+)*

| Variable | Akun COA | Posisi |
|---|---|---|
| `CI_AR_AMT` | 1210 — Trade Receivable | **Debit** |
| `CI_REVENUE_AMT` | 4110 — Product Sales | **Credit** |
| `CI_TAX_AMT` | 2130 — Tax Payable | **Credit** |

---

### 4.5 GOODS_ISSUE *(planned Sprint 6+)*

| Variable | Akun COA | Posisi |
|---|---|---|
| `GI_COGS_AMT` | 5110 — COGS Material | **Debit** |
| `GI_INVENTORY_AMT` | 1310 — Merchandise Inventory | **Credit** |

---

### 4.6 CUSTOMER_RECEIPT *(planned Sprint 6+)*

| Variable | Akun COA | Posisi |
|---|---|---|
| `CR_BANK_IN_AMT` | 1120 — Main Bank Account | **Debit** |
| `CR_AR_AMT` | 1210 — Trade Receivable | **Credit** |

---

### 4.7 STOCK_ADJUSTMENT_IN *(planned Sprint 6+)*

| Variable | Akun COA | Posisi |
|---|---|---|
| `SAI_INVENTORY_AMT` | 1310 — Merchandise Inventory | **Debit** |
| `SAI_GAIN_AMT` | 4230 — Inventory Adjustment Gain | **Credit** |

---

### 4.8 STOCK_ADJUSTMENT_OUT *(planned Sprint 6+)*

| Variable | Akun COA | Posisi |
|---|---|---|
| `SAO_LOSS_AMT` | 5130 — Inventory Adjustment Loss | **Debit** |
| `SAO_INVENTORY_AMT` | 1310 — Merchandise Inventory | **Credit** |

---

## 5. Aturan Bisnis (Business Rules)

### 5.1 Double-Entry Balance
Setiap Journal Entry **wajib** balanced: `Σ Debit = Σ Kredit`.  
Jika tidak balance → `DomainException: msg.error.journal.unbalanced` dan transaksi induk di-rollback.

### 5.2 Idempotency
Satu sumber dokumen hanya boleh menghasilkan **satu** journal entry.  
Constraint: `UNIQUE (source_type, source_id)` di database.  
Jika posting dieksekusi ulang untuk sumber yang sama → diabaikan (skip).

### 5.3 Accounting Schema Wajib Ada
Sebelum posting, sistem mencari **Active Accounting Schema** untuk event type yang bersangkutan.  
Jika tidak ditemukan → `DomainException: msg.error.journal.schema.notfound` dan transaksi di-rollback.

### 5.4 Immutability
Journal Entry yang sudah di-post **tidak dapat diubah atau dihapus**.  
Status selalu `POSTED` — tidak ada status `DRAFT` untuk auto-posting.  
Koreksi dilakukan via jurnal pembalik (reversal journal) — *belum diimplementasi*.

### 5.5 Posting Sinkronus & Atomik
Auto-posting berjalan **dalam transaksi yang sama** dengan transaksi induk.  
Jika posting gagal → seluruh transaksi induk (GR complete, dll.) ikut di-rollback.

---

## 6. Struktur Data Journal Entry

```
Journal Entry Header
├── Journal Code         : JNL-000001 (format: JNL- + 6 digit ID)
├── Event Type           : GOODS_RECEIPT | VENDOR_BILL | ...
├── Source Type          : String (e.g. "GOODS_RECEIPT")
├── Source ID            : Long (FK ke dokumen asal)
├── Source Code          : String (e.g. "GR-202605-00001")
├── Posting Date         : LocalDate
├── Description          : String (auto-generated)
└── Status               : POSTED

Journal Lines (minimal 2 baris)
├── Line No              : urutan (1, 2, 3, ...)
├── Account              : COA account (code + name)
├── Debit Amount         : Decimal (0 jika credit line)
└── Credit Amount        : Decimal (0 jika debit line)
```

---

## 7. UI / Halaman

Saat ini fitur Journal Entry hanya menyediakan tampilan **read-only**:

### 7.1 Daftar Jurnal (`/accounting/journal-entries`)
**Filter yang tersedia:**
- Source Type (dropdown semua event types)
- Source Code (pencarian teks)
- Journal Code (pencarian ID)
- Posting Date From / To (range tanggal)

**Kolom tabel:**
| Kolom | Keterangan |
|---|---|
| Journal Code | JNL-XXXXXX, klik ke detail |
| Source Type | Tipe event (label i18n) |
| Source Code | Kode dokumen asal (link ke GR jika ada) |
| Posting Date | Tanggal posting |
| Total Debit | Jumlah debit |
| Total Credit | Jumlah kredit |
| Status | Badge hijau: POSTED |

### 7.2 Detail Jurnal (`/accounting/journal-entries/{id}`)
Menampilkan header + tabel lines dengan kolom Account (nama + kode), Debit, Credit, dan baris total di bagian bawah.

---

## 8. Accounting Schema (Konfigurasi Posting Rules)

Posting rules dikonfigurasi melalui modul **Accounting Schema** (UI tersedia).  
Detail: lihat [docs/modules/accounting/accounting-schema.md](accounting-schema.md).

Setiap schema memiliki:
- **Event Type** — jenis transaksi yang ditangani
- **Lines** — daftar baris: variable → akun COA → posisi (Debit/Credit)
- **Is Active** — hanya schema aktif yang dipakai saat posting

Untuk menambah event baru:
1. Tambah nilai di `SchemaEventType` enum
2. Tambah variable(s) di `JournalVariable` enum
3. Buat record Accounting Schema via UI
4. Hubungkan ke use case transaksi baru

---

## 9. Referensi Terkait

- [Accounting Schema](accounting-schema.md)
- [Chart of Accounts (COA)](coa.md)
- [Fiscal Year & Period](fiscal-year-period.md)
- [Journal Auto-Posting Architecture](../../architecture/journal-posting-engine.md)
- [Sprint 5 — Vendor Bill Roadmap](../../roadmap/sprint-5-vendor-bill.md)
