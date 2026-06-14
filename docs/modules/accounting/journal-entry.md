# Journal Entry — Business & Functional Specification

> Modul: `Finance & Accounting > General Ledger > Journal Entry`  
> Permission: `JOURNAL-ENTRY_READ`  
> Route: `/accounting/journal-entries`

---

## 1. Ringkasan Bisnis

**Journal Entry** adalah catatan akuntansi double-entry yang merekam dampak finansial dari setiap transaksi operasional. Setiap transaksi yang mengubah posisi keuangan perusahaan (penerimaan barang, pembayaran, penjualan) **wajib** menghasilkan jurnal yang seimbang: total Debit = total Kredit.

Dalam sistem ini, journal entry memiliki dua jalur:

- **Auto-posting** dari transaksi operasional yang sudah didukung sistem. Jurnal langsung `POSTED`, immutable, dan mengikuti Accounting Schema.
- **Manual journal** untuk penyesuaian akuntansi. Pengguna membuat `DRAFT`, mengubah/menghapus selama draft, lalu `POST`. Koreksi jurnal manual dilakukan lewat reversal journal.

---

## 2. Sumber Jurnal (Event Types)

Sistem mendukung 8 jenis event yang dapat memicu auto-posting jurnal, ditambah pseudo-event `MANUAL` untuk jurnal yang dibuat langsung oleh pengguna:

| Event Type | Trigger Operasional | Status |
|---|---|---|
| `GOODS_RECEIPT` | GR dikonfirmasi (Complete) | ✅ Live |
| `VENDOR_BILL` | Invoice vendor dikonfirmasi | ✅ Live |
| `VENDOR_PAYMENT` | Pembayaran ke vendor dikonfirmasi | 🔲 Sprint 5 |
| `CUSTOMER_INVOICE` | Invoice ke customer dikonfirmasi | 🔲 Sprint 6+ |
| `GOODS_ISSUE` | Barang keluar gudang dikonfirmasi | 🔲 Sprint 6+ |
| `CUSTOMER_RECEIPT` | Pembayaran dari customer diterima | 🔲 Sprint 6+ |
| `STOCK_ADJUSTMENT_IN` | Penyesuaian stok positif dikonfirmasi | 🔲 Sprint 6+ |
| `STOCK_ADJUSTMENT_OUT` | Penyesuaian stok negatif dikonfirmasi | 🔲 Sprint 6+ |
| `MANUAL` | Input jurnal manual oleh user | ✅ Live |

---

## 3. Alur AP Cycle (Procurement → Pembayaran)

Berikut gambaran lengkap alur jurnal dalam siklus pembelian:

```
PO (disetujui)
    │  → Tidak ada jurnal
    ▼
Goods Receipt (Complete) ──────────────────────────── ✅ Auto-Journal
    │  DR Merchandise Inventory (1310)   = nilai barang net/DPP
    │  CR GR/IR Clearing (2120)          = nilai barang net/DPP
    ▼
Vendor Bill (Confirm) ─────────────────────────────── ✅ Auto-Journal
    │  DR GR/IR Clearing (2120)          = nilai GR net yang di-match
    │  DR Tax Receivable / Input VAT (1230) = PPN masukan invoice
    │  CR Accounts Payable (2110)        = total bruto invoice
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

> Tax timing saat ini invoice-based: `GR_TAX_AMT = 0` dan baris tax di-skip oleh engine.  
> `GR_GRAND_TOTAL = GR_INVENTORY_AMT` sehingga GR/IR hanya menampung nilai net/DPP.

Input VAT diakui saat Vendor Bill dikonfirmasi, bukan saat GR complete.

---

### 4.2 VENDOR_BILL

**Trigger:** `ConfirmVendorBillUseCase` selesai dieksekusi.

| Variable | Akun COA | Posisi |
|---|---|---|
| `VB_GRIR_CLEARING_AMT` | 2120 — GR/IR Clearing | **Debit** |
| `VB_TAX_AMT` | 1230 — Tax Receivable (Input VAT) | **Debit** |
| `VB_FX_LOSS_AMT` | FX Loss account | **Debit** |
| `VB_AP_TOTAL` | 2110 — Accounts Payable | **Credit** |
| `VB_FX_GAIN_AMT` | FX Gain account | **Credit** |

> `VB_GRIR_CLEARING_AMT = subtotal/DPP invoice`  
> `VB_TAX_AMT = tax invoice`  
> `VB_AP_TOTAL = subtotal + tax`

Untuk invoice multi-currency, nilai journal dipost dalam base currency menggunakan exchange rate Vendor Bill. FX variance pada clearing GR/IR dipost ke variable `VB_FX_LOSS_AMT` atau `VB_FX_GAIN_AMT` jika ada selisih kurs pada porsi net/DPP.

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
Auto-posted journal selalu `POSTED` dan **tidak dapat diubah atau dihapus**.

Manual journal memiliki lifecycle:

1. `DRAFT` dapat dibuat, diubah, dan dihapus.
2. `POSTED` tidak dapat diubah atau dihapus.
3. Koreksi `POSTED` dilakukan dengan reversal journal yang menukar debit/kredit dan terhubung ke jurnal asal.
4. Reversal journal tidak dapat di-reverse lagi.

Primitive internal `ReversePostedJournalUseCase` dapat membalik manual journal dan auto-journal yang sudah `POSTED`. Reversal ini membuat journal baru yang menukar debit/kredit dari final `JournalLine` asal dan menyimpan `reversalOfId`; ia tidak menjalankan Accounting Schema ulang dengan amount negatif. Aksi UI `/accounting/journal-entries/{id}/reverse` tetap dibatasi untuk manual journal melalui permission `JOURNAL-ENTRY_REVERSE`.

### 5.5 Manual Journal Period Guard
Posting manual dan reversal hanya boleh dilakukan pada accounting period yang terbuka. Draft masih bisa disiapkan, tetapi posting/reversal gagal jika tanggal posting berada di periode tertutup atau tidak tersedia.

### 5.6 Posting Sinkronus & Atomik
Auto-posting berjalan **dalam transaksi yang sama** dengan transaksi induk.  
Jika posting gagal → seluruh transaksi induk (GR complete, dll.) ikut di-rollback.

---

## 6. Struktur Data Journal Entry

```
Journal Entry Header
├── Journal Code         : JNL-000001 (format: JNL- + 6 digit ID)
├── Event Type           : GOODS_RECEIPT | VENDOR_BILL | ...
├── Source Type          : String (e.g. "GOODS_RECEIPT")
├── Source ID            : Long nullable (FK ke dokumen asal; null untuk manual)
├── Source Code          : String nullable (e.g. "GR-202605-00001")
├── Posting Date         : LocalDate
├── Currency ID          : Long nullable untuk compatibility row lama; wajib untuk manual
├── Exchange Rate        : Decimal nullable untuk compatibility row lama; wajib > 0 untuk manual
├── Reference No         : String nullable
├── Reversal Of ID       : Long nullable, terisi pada jurnal reversal
├── Description          : String (auto-generated)
└── Status               : DRAFT | POSTED

Journal Lines (minimal 2 baris)
├── Line No              : urutan (1, 2, 3, ...)
├── Account              : COA account (code + name)
├── Debit Amount         : Decimal (0 jika credit line)
├── Credit Amount        : Decimal (0 jika debit line)
├── Original Currency ID : Long nullable
├── Original Debit       : Decimal
├── Original Credit      : Decimal
├── Exchange Rate        : Decimal nullable
└── Description          : String nullable
```

---

## 7. UI / Halaman

Fitur Journal Entry menyediakan daftar/detail untuk semua jurnal, plus form manual journal untuk user yang memiliki permission terkait.

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
Menampilkan header + tabel lines dengan kolom Account (nama + kode), Debit, Credit, memo, dan baris total di bagian bawah. Untuk jurnal manual:

- Draft menampilkan aksi edit, delete, dan post.
- Posted menampilkan aksi reverse jika belum pernah dibalik.
- Reversal menampilkan link ke jurnal asal.
- Jurnal asal yang sudah dibalik menampilkan link ke reversal.

### 7.3 Manual Journal Form

Route:

- `GET /accounting/journal-entries/create`
- `GET /accounting/journal-entries/edit/{id}`
- `POST /accounting/journal-entries`
- `PUT /accounting/journal-entries/{id}`
- `DELETE /accounting/journal-entries/{id}`
- `POST /accounting/journal-entries/{id}/post`
- `POST /accounting/journal-entries/{id}/reverse`

Validasi utama:

- minimal dua line;
- setiap line wajib memilih akun posting aktif;
- header currency wajib aktif;
- exchange rate wajib `> 0`;
- jika currency adalah default currency, exchange rate wajib `1`;
- total original debit wajib sama dengan total original credit.

### 7.4 Permissions

Permission manual journal:

- `JOURNAL-ENTRY_CREATE`
- `JOURNAL-ENTRY_UPDATE`
- `JOURNAL-ENTRY_DELETE`
- `JOURNAL-ENTRY_POST`
- `JOURNAL-ENTRY_REVERSE`

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

Deferred items:

- Approval workflow manual journal belum diterapkan.
- Attachment/supporting document belum tersedia.
- Import jurnal massal belum tersedia.

---

## 9. Referensi Terkait

- [Accounting Schema](accounting-schema.md)
- [Chart of Accounts (COA)](coa.md)
- [Fiscal Year & Period](fiscal-year-period.md)
- [Journal Auto-Posting Architecture](../../architecture/journal-posting-engine.md)
- [Sprint 5 — Vendor Bill Roadmap](../../roadmap/sprint-5-vendor-bill.md)
