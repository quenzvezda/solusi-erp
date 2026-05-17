# Brainstorming Follow-up: Vendor Bill Multi-Currency & UI Flow

> Tanggal: 2026-05-11  
> Konteks: Follow-up setelah implementasi Vendor Bill Sprint 5 dan manual QA awal  
> Referensi utama: [2026-05-10-vendor-bill-sprint5.md](2026-05-10-vendor-bill-sprint5.md)  
> Status: **FINAL — semua open questions resolved, siap untuk implementation plan**

---

## Executive Summary

Diskusi ini adalah lanjutan dari hasil implementasi Vendor Bill Sprint 5. Setelah halaman create Vendor Bill berhasil dibuka tanpa error Thymeleaf, ditemukan bahwa UX create form belum usable untuk proses bisnis sebenarnya karena masih memakai input angka untuk `vendorId` dan `currencyId`.

Selain itu, ada gap desain multi-currency: Vendor Bill hanya menyimpan `currency_id`, sementara PO dan GR sudah memiliki `exchangeRate`, dan master currency sudah memiliki boolean `is_default` dengan IDR sebagai default currency di migration/seeder. Agar tidak menjadi tech debt sebelum branch naik ke production/VPS, Vendor Bill, Journal, dan posting AP perlu dirapikan sejak sekarang untuk mendukung original currency dan base currency.

Keputusan utama:
- Vendor Bill create flow akan diubah menjadi 2-step wizard.
- Vendor Bill menyimpan `exchangeRate`.
- Exchange rate default diwarisi dari source reference PO/GR, tetapi user boleh override.
- Journal line menyimpan original amount dan base amount.
- FX variance Vendor Bill memakai dua journal variable: `VB_FX_LOSS_AMT` dan `VB_FX_GAIN_AMT`.

---

## Current Problem

### UI Create Vendor Bill

Form create saat ini menampilkan:
- Vendor sebagai input angka.
- Currency sebagai input angka.
- Tombol `Load GRs` tanpa konteks jelas.

Secara teknis halaman sudah render, tetapi dari sisi user workflow belum dapat dipakai dengan nyaman. User tidak tahu vendor/currency ID, dan tidak jelas GR mana yang eligible untuk dijadikan invoice.

### Multi-Currency Gap

Vendor Bill saat ini hanya menyimpan:
- `currency_id`

Padahal sistem sudah memiliki pola:
- PO memiliki `exchangeRate`.
- GR memiliki `exchangeRate`.
- Inventory valuation memakai konsep amount dengan currency/rate/base amount.
- Currency master memiliki `is_default`, dengan IDR sebagai default currency.

Jika base currency sistem adalah IDR dan invoice vendor memakai USD, Vendor Bill perlu menyimpan kurs snapshot, misalnya USD 1 = IDR 17.400.

---

## Confirmed Business Rules From Sprint 5

Berdasarkan brainstorming Vendor Bill Sprint 5:

- Vendor Bill dapat dibuat dari satu atau beberapa GR.
- Multi-PO diperbolehkan.
- Semua selected references wajib memiliki vendor/supplier yang sama.
- Semua selected references wajib memiliki currency yang sama.
- GR wajib `COMPLETED`.
- GR line masih memiliki outstanding quantity yang belum di-bill.
- `qty_billed` harus lebih dari 0 dan tidak boleh melebihi outstanding quantity.
- Harga masih strict lock dari PO/GR; user tidak mengubah unit price di Vendor Bill.

Referensi:
- [Keputusan GR reference scope](2026-05-10-vendor-bill-sprint5.md)
- [Business Rules: Three-Way Match](2026-05-10-vendor-bill-sprint5.md)
- [User Flow Vendor Bill](2026-05-10-vendor-bill-sprint5.md)

---

## Proposed UI Flow: 2-Step Vendor Bill Wizard

### Step 1: Select Billable References

Halaman pertama menampilkan list referensi yang dapat dijadikan Vendor Bill.

Untuk tahap sekarang, source type yang aktif:
- `GOODS_RECEIPT`

Di masa depan, source type dapat diperluas:
- `SERVICE_ENTRY_SHEET`
- direct expense/service document
- reference lain yang menimbulkan tagihan vendor

Kolom yang disarankan:
- Source Type
- Source Document Code
- Source Date
- Vendor
- Currency
- Exchange Rate from Source
- Outstanding Amount
- Outstanding Qty summary
- Status

Behavior:
- User memilih satu atau lebih reference dengan checkbox.
- Pilihan lintas page sebaiknya tersimpan di client state.
- Backend tetap wajib revalidate semua selected reference saat lanjut ke step 2.
- Semua selected reference harus vendor sama.
- Semua selected reference harus currency sama.
- Jika vendor/currency berbeda, user tidak boleh lanjut.

### Step 2: Create Vendor Bill Header & Lines

Setelah selected references valid:

Header:
- Vendor locked dari selected references.
- Currency locked dari selected references.
- Exchange Rate auto-filled dari source reference.
- Exchange Rate tetap editable.
- Vendor Invoice Number wajib.
- Bill Date wajib.
- Due Date wajib dan `dueDate >= billDate`.
- Notes optional.

Lines:
- Lines auto-populate dari selected reference lines.
- `qty_billed` default = outstanding qty.
- `qty_billed` bisa dikurangi untuk partial invoice.
- Unit price locked.
- Tax amount informational/0 untuk GR-based bill sesuai Sprint 5.
- Amount original dan base amount dihitung konsisten dari selected currency dan exchange rate.

---

## Exchange Rate Inheritance Rule

Default exchange rate Vendor Bill:

1. Jika currency = default currency, default `exchangeRate = 1`.
2. Jika hanya satu source reference dipilih, default mengikuti exchange rate source tersebut.
3. Jika banyak source reference dipilih dan semua exchange rate sama, default mengikuti rate tersebut.
4. Jika banyak source reference dipilih dan exchange rate berbeda, UI menampilkan kondisi mixed rate dan user wajib mengisi exchange rate Vendor Bill secara manual.
5. User tetap boleh mengganti exchange rate karena invoice rate dapat berbeda dari PO/GR rate.

Catatan: exchange rate pada Vendor Bill adalah snapshot rate saat invoice/vendor bill diakui, bukan selalu sama dengan PO/GR.

---

## Journal Multi-Currency Decision

Keputusan: gunakan desain journal yang menyimpan original amount dan base amount per line.

Journal line menyimpan minimal:
- `original_currency_id`
- `exchange_rate`
- `original_debit_amount`
- `original_credit_amount`
- `debit_amount` dalam base currency
- `credit_amount` dalam base currency

Ledger tetap balance dalam base currency, sementara audit original currency tetap tersedia pada detail journal.

Contoh:
- Original: USD 100
- Exchange Rate: 17.400
- Base Amount: IDR 1.740.000

---

## Vendor Bill FX Variance

Jika GR dan Vendor Bill memakai currency yang sama tetapi exchange rate berbeda, selisih base currency harus dipost sebagai FX gain/loss.

Formula:

```text
grir_base_amount = nilai GR/IR yang dipost saat GR dalam base currency
ap_base_amount   = vendor bill original amount x bill exchange rate
fx_variance      = ap_base_amount - grir_base_amount
```

Jika `fx_variance > 0`:

```text
DR GR/IR Clearing      grir_base_amount
DR FX Loss             fx_variance
CR Accounts Payable    ap_base_amount
```

Jika `fx_variance < 0`:

```text
DR GR/IR Clearing      grir_base_amount
CR FX Gain             abs(fx_variance)
CR Accounts Payable    ap_base_amount
```

Jika `fx_variance = 0`, FX loss/gain bernilai 0 dan journal line di-skip.

---

## Journal Variables

Vendor Bill variables yang disarankan:

```text
VB_GRIR_CLEARING_AMT
VB_AP_TOTAL
VB_FX_LOSS_AMT
VB_FX_GAIN_AMT
VB_TAX_AMT
```

Schema position:

| Variable | Position |
|---|---|
| `VB_GRIR_CLEARING_AMT` | Debit |
| `VB_AP_TOTAL` | Credit |
| `VB_FX_LOSS_AMT` | Debit |
| `VB_FX_GAIN_AMT` | Credit |
| `VB_TAX_AMT` | Debit |

Alasan memakai dua variable loss/gain:
- Accounting schema engine saat ini lebih cocok dengan variable yang posisinya fixed.
- Tidak perlu membuat satu variable dengan posisi debit/credit dinamis.

---

## FX Variance vs Price Variance

FX variance berbeda dari price variance.

Untuk Sprint 5 Vendor Bill:
- Unit price masih strict lock dari PO/GR.
- User tidak mengubah harga di Vendor Bill.
- Maka selisih yang muncul karena rate berbeda adalah FX variance.

Price variance belum perlu diimplementasikan sekarang. Jika nanti invoice original amount berbeda dari GR original amount karena harga atau quantity/value dispute, itu perlu variable terpisah seperti:

```text
VB_PRICE_VARIANCE_LOSS_AMT
VB_PRICE_VARIANCE_GAIN_AMT
```

Untuk sekarang, price variance tetap out of scope.

---

## Suggested Technical Direction

### 1. Introduce Generic AP Billable Reference

Jangan hardcode UI dan use case sebagai GR-only picker.

Buat konsep read model umum:

```text
BillableApReference
- sourceType
- sourceId
- sourceCode
- sourceDate
- vendorId
- vendorName
- currencyId
- currencyCode
- exchangeRate
- outstandingAmount
- status
```

Untuk sekarang provider pertama hanya:

```text
GoodsReceiptBillableReferenceProvider
```

Saat Service Entry Sheet masuk, tinggal tambah provider baru tanpa membongkar wizard.

### 2. Vendor Bill Header

Tambahkan:
- `exchange_rate`

Pertimbangkan juga:
- `base_currency_id`

Jika base currency selalu diambil dari default currency master saat posting, `base_currency_id` bisa disimpan di journal saja. Tetapi untuk audit dokumen, menyimpan base currency di Vendor Bill juga lebih eksplisit.

### 3. Journal Engine

Upgrade journal line agar mendukung original dan base amount.

Posting command perlu membawa:
- event currency
- event exchange rate
- variable values original amount
- calculated base amount

Atau, untuk menjaga schema engine tetap sederhana:
- command tetap menerima variable base amount untuk balancing
- ditambah metadata original currency/rate/original amount untuk journal lines

Detail desain final perlu diputuskan di plan implementasi.

---

## Open Implementation Questions — All Resolved ✅

| # | Pertanyaan | Keputusan | Alasan |
|---|---|---|---|
| 1 | `base_currency_id` disimpan di setiap dokumen finansial, atau cukup di Journal Entry? | ✅ **Journal Entry saja** | Konsisten dengan pola GR (tidak simpan `base_currency_id`). Base currency (IDR) praktis tidak berubah. Dokumen sumber cukup simpan `currency_id` + `exchange_rate`, base currency di-resolve saat posting via lookup `Currency.isDefault = true`. |
| 2 | Mixed exchange rate di selected references? | ✅ **Wajib manual input** (sudah confirmed di section Exchange Rate Inheritance Rule) | Rule #4: jika banyak source reference dipilih dan exchange rate berbeda, UI tampilkan kondisi mixed rate dan user wajib mengisi rate Vendor Bill secara manual. |
| 3 | VB line simpan original/base split eksplisit, atau compute saat posting? | ✅ **Compute saat posting** — header `exchange_rate` × line `line_total` | Konsisten dengan pola GR line (tidak simpan base amount terpisah). Satu sumber kebenaran: header rate × line amount. Remainder method di line terakhir untuk cegah rounding akumulatif. |
| 4 | Existing GR journal data perlu migration backfill? | ✅ **Tidak backfill** | Project learning/MVP — data existing adalah dev/testing data, bukan production. Kolom baru di `acc_journal_lines` cukup `NULLABLE` dengan semantic: `NULL` = transaksi base currency / single currency (backward compatible). |
| 5 | Journal Entry UI perlu ditingkatkan untuk tampilkan original currency dan base amount? | ✅ **Ya, upgrade minimal di Sprint 5** | Tanpa UI, tidak bisa verifikasi FX variance journal saat manual QA. Scope dibatasi: hanya **detail view** (read-only), tambah kolom Currency/Rate/Original DR/Original CR. Conditional render — hanya muncul jika `original_currency_id != NULL`. Journal lama (GR tanpa original currency) tampil "—". |

---

## Recommended Next Step

Buat implementation plan terpisah sebelum coding.

Plan sebaiknya dibagi menjadi beberapa task:

1. Journal multi-currency foundation.
2. Vendor Bill exchange rate persistence.
3. Vendor Bill FX variance journal variables.
4. AP billable reference abstraction.
5. Vendor Bill 2-step wizard UI.
6. Regression tests dan Thymeleaf tests.
7. Spec review update terhadap Vendor Bill Sprint 5 docs.

Urutan ini mengurangi risiko karena fondasi journal dan data model diselesaikan sebelum UI wizard bergantung pada struktur baru.

---

## Implementation Note — 2026-05-12

Brainstorming ini sudah diimplementasikan pada branch `feature/sprint-5-vendor-bill` sebagai follow-up Vendor Bill Sprint 5.

Key implementation notes:

- Journal line sudah mendukung multi-currency audit fields: original currency, exchange rate, original debit, dan original credit, sementara balancing tetap memakai base debit/credit.
- Vendor Bill sekarang menyimpan `exchange_rate` sebagai snapshot kurs invoice dan menggunakannya saat posting AP.
- Vendor Bill confirmation sekarang menghitung base AP amount dari Vendor Bill rate dan membandingkannya dengan GR/IR base amount dari GR rate.
- FX variance Vendor Bill diposting melalui `VB_FX_LOSS_AMT` atau `VB_FX_GAIN_AMT`; zero amount tetap mengikuti schema engine behavior untuk skip line.
- Dev seeder CoA dan accounting schema sudah ditambahkan untuk Foreign Exchange Gain/Loss agar konfigurasi manual tidak tertinggal saat reset database dev.
- Create Vendor Bill flow sudah menjadi 2-step wizard: pilih billable AP references terlebih dahulu, lalu form header/lines diprefill dari selected references.
- AP billable reference dibuat generik melalui provider abstraction; implementasi aktif saat ini adalah Goods Receipt provider.
- Journal Entry detail sudah menampilkan kolom multi-currency secara conditional saat journal line memiliki original currency.
- Regression tests dan Thymeleaf/static template tests sudah disesuaikan dengan flow wizard dan FX variables baru.

Known follow-up:

- Mixed exchange rate selected references masih perlu diperhalus pada UX agar user mendapat indikator eksplisit ketika rate source berbeda dan wajib mengisi Vendor Bill exchange rate manual.
- Step 2 wizard masih perlu validasi server-side yang lebih eksplisit untuk memastikan selected references tetap satu vendor dan satu currency setelah redirect/prefill.

