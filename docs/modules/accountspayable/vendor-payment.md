# Vendor Payment (AP Payment)

Dokumen ini merangkum implementasi aktual Vendor Payment pada modul **Finance & Accounting > Account Payable > Vendor Payment**.

## 1. Ringkasan Implementasi Saat Ini

Vendor Payment adalah dokumen pembayaran ke vendor yang mengalokasikan dana ke satu atau lebih Vendor Bill yang berstatus `CONFIRMED` (memiliki outstanding amount > 0).

Flow yang aktif saat ini:

1. User membuat payment baru, memilih vendor dan currency.
2. Sistem otomatis memuat daftar Vendor Bill yang payable (CONFIRMED, outstanding > 0) berdasarkan vendor + currency.
3. User memilih bank account melalui modal selector (difilter berdasarkan currency).
4. User mengisi payment amount, payment date, exchange rate, dan mengalokasikan paid amount ke masing-masing bill line.
5. Validasi: total applied harus sama dengan payment amount (unapplied = 0).
6. Draft Vendor Payment disimpan.
7. Vendor Payment dikonfirmasi.
8. Sistem mem-post journal `VENDOR_PAYMENT` dan mengupdate status pembayaran pada Vendor Bill terkait.

Lifecycle:

```text
DRAFT -> CONFIRMED
DRAFT -> CANCELLED
```

Catatan:
- `CONFIRMED` dan `CANCELLED` tidak bisa diedit atau dihapus.
- Edit dan delete hanya tersedia untuk status `DRAFT`.
- Cancel hanya tersedia dari halaman detail untuk status `DRAFT`.
- Saat confirm, sistem memvalidasi total paid amount == payment amount.
- Saat confirm, sistem mengupdate `paidAmount` dan `outstandingAmount` pada Vendor Bill terkait, serta mengubah status VB ke `PARTIAL_PAID` atau `PAID` sesuai kondisi.

## 2. Kontrak Fitur yang Aktif

| Area | Kondisi saat ini |
|---|---|
| List | `/accounts-payable/vendor-payments` dengan filter keyword, vendor, status (HTMX search) |
| Create form | `/accounts-payable/vendor-payments/create` |
| Edit form | `/accounts-payable/vendor-payments/edit/{id}` |
| Detail | `/accounts-payable/vendor-payments/{id}` |
| Save draft | `POST /accounts-payable/vendor-payments` via AJAX form |
| Update draft | `PUT /accounts-payable/vendor-payments/{id}` via AJAX form |
| Confirm | `POST /accounts-payable/vendor-payments/{id}/confirm` |
| Cancel | `POST /accounts-payable/vendor-payments/{id}/cancel` |
| Delete | `DELETE /accounts-payable/vendor-payments/{id}` via HTMX row delete |
| Payable bills API | `GET /accounts-payable/vendor-payments/payable-bills?vendorId=&currencyId=` (JSON) |
| Bank account selector | `GET /accounts-payable/vendor-payments/selectors/bank-accounts?currencyId=` (HTMX fragment) |

## 3. Source dan Matching

Vendor Payment mengalokasikan pembayaran ke Vendor Bill.

Syarat Vendor Bill bisa dialokasikan:
- Status VB `CONFIRMED` (atau `PARTIAL_PAID`)
- `outstandingAmount > 0`
- Vendor dan currency VB sama dengan yang dipilih di payment header

Saat vendor atau currency berubah di form, allocation lines di-clear dan dimuat ulang dari endpoint `/payable-bills`.

## 4. Model Data yang Dipakai

### A. Header Vendor Payment

| Field | Keterangan |
|---|---|
| `code` | Nomor internal dari sequence `VENDOR-PAYMENT` (format: `VP-{yyyyMM}-{seq}`) |
| `vendorId` | Vendor yang dibayar |
| `currencyId` | Currency pembayaran |
| `bankAccountId` | Bank account sumber dana (dipilih via modal selector, difilter per currency) |
| `paymentDate` | Tanggal pembayaran |
| `exchangeRate` | Kurs pembayaran ke base currency |
| `paymentAmount` | Total jumlah yang dibayarkan |
| `status` | `DRAFT`, `CONFIRMED`, atau `CANCELLED` |
| `reference` | Nomor referensi eksternal (opsional) |
| `notes` | Catatan (opsional) |

### B. Line Vendor Payment (Allocation)

| Field | Keterangan |
|---|---|
| `vendorBillId` | ID Vendor Bill yang dialokasikan |
| `billCode` | Kode Vendor Bill (snapshot) |
| `outstandingAmount` | Sisa outstanding VB saat payment dibuat |
| `paidAmount` | Jumlah yang dialokasikan untuk VB ini |

## 5. Validasi

### Domain-level (VendorPayment.java):
- `paymentAmount` harus > 0 (`msg.err.vp.amount.positive`)
- `lines` tidak boleh kosong (`msg.err.vp.lines.required`)
- Edit hanya boleh pada status DRAFT (`msg.err.vp.edit.only.draft`)
- Confirm hanya boleh pada status DRAFT (`msg.err.vp.confirm.only.draft`)
- Saat confirm: total `paidAmount` semua lines harus == `paymentAmount` (`msg.err.vp.amount.mismatch`)
- Cancel hanya boleh pada status DRAFT (`msg.err.vp.cancel.only.draft`)

### Client-side (form.js):
- Minimal satu line harus memiliki `paidAmount > 0`
- `paymentAmount` harus sama dengan total applied (unapplied harus 0)

## 6. Accounting Saat Confirm

`ConfirmVendorPaymentUseCaseImpl` mem-post journal event `VENDOR_PAYMENT`.

Perhitungan:
```text
paymentAmountBase = paymentAmount * exchangeRate
apAmountBase      = sum(lines.paidAmount) * exchangeRate
fxDiff            = apAmountBase - paymentAmountBase
fxLoss            = fxDiff > 0 ? fxDiff : 0
fxGain            = fxDiff < 0 ? abs(fxDiff) : 0
```

Variable yang dikirim:

| Variable | Nilai | Posisi schema umum |
|---|---|---|
| `VP_AP_AMT` | AP amount base (total paid * rate) | Debit |
| `VP_BANK_OUT_AMT` | Payment amount base | Credit |
| `VP_FX_LOSS_AMT` | FX loss jika ada | Debit |
| `VP_FX_GAIN_AMT` | FX gain jika ada | Credit |

Journal entry tipikal (tanpa FX variance):

```text
DR Accounts Payable          xxx
CR Bank / Cash               xxx
```

Account override: `VP_BANK_OUT_AMT` dapat di-override dengan COA dari bank account yang dipilih (jika `bankCoaId` tersedia).

Setelah journal posting, sistem memanggil `VendorBillPaymentUpdatePort.updatePaymentStatus(billIds)` untuk mengupdate status pembayaran pada Vendor Bill terkait.

## 7. Interaksi Komponen UI

### 7.1 Autocomplete (TomSelect)
- **Vendor** (`#vp-vendor`): autocomplete dari `/api/lookup/parties`
- **Currency** (`#vp-currency`): autocomplete dari `/api/lookup/master/currencies`

Cascading behavior: perubahan vendor atau currency akan:
1. Clear allocation lines
2. Clear bank account selection
3. Reload payable bills dari API

### 7.2 Bank Account Modal Selector
- Trigger: tombol `#btn-select-bank-account`
- Modal: `#bank-account-modal` dengan body `#bank-account-selector-body`
- Endpoint: `/accounts-payable/vendor-payments/selectors/bank-accounts?currencyId=...`
- Filter: hanya bank account dengan currency yang sama dan memiliki COA
- Selection: klik `.js-bank-account-select` → set `#bankAccountId` (hidden) + `#bankAccountDisplay` (readonly text)
- Clear: tombol `#btn-clear-bank-account`

### 7.3 Currency Rate Lock
- Menggunakan `ERP.CurrencyRateLock.init` dengan `currencySelectId: "vp-currency"`
- Jika currency adalah default (base currency), exchange rate input menjadi readonly dengan value 1

### 7.4 Allocation Lines
- Dimuat otomatis dari `/accounts-payable/vendor-payments/payable-bills?vendorId=&currencyId=`
- Setiap line menampilkan: bill code, outstanding amount (readonly), paid amount (editable, AutoNumeric `currency`)
- Line bisa dihapus via `.remove-line-btn`
- Reindex otomatis setelah remove

### 7.5 Recap
- **Payment Amount**: dari input `paymentAmount`
- **Applied**: sum dari semua `.paid-amount-input`
- **Unapplied**: payment amount - applied (merah jika != 0, hijau jika == 0)

## 8. Detail Page Actions

Halaman detail (`/accounts-payable/vendor-payments/{id}`) menampilkan:
- Header info (code, payment date, exchange rate, payment amount, bank account)
- Link ke journal entry (hanya jika CONFIRMED)
- Allocation table (bill code → outstanding → paid)
- Action buttons (hanya untuk DRAFT):
  - **Edit**: link ke `/accounts-payable/vendor-payments/edit/{id}`
  - **Confirm** (`.btn-confirm-payment`): `ErpForm.postAction` → POST `/{id}/confirm` → redirect ke detail
  - **Cancel** (`.btn-cancel-payment`): `ErpForm.postAction` → POST `/{id}/cancel` → redirect ke detail

## 9. List Page Features

- Filter: keyword (HTMX keyup delay 500ms), status dropdown (HTMX on change)
- HTMX target: `#vendor-payment-table-container`
- Kolom: Code, Payment Date, Bank Account, Amount, Status, Actions
- Actions per row (DRAFT only): View, Edit, Delete (modal confirm)
- Delete menggunakan HTMX row delete pattern (`fragments/modals :: delete-confirm`)

## 10. Clean Architecture Notes

Vendor Payment tidak membaca repository Vendor Bill secara langsung.

Akses data lintas slice dilakukan melalui port/adapter:
- `PayableVendorBillQueryPort` — query outstanding bills per vendor+currency
- `VendorBillPaymentUpdatePort` — update payment status pada VB setelah confirm

Web layer memakai use case, lookup providers (Party, Currency, BankAccount), dan web mapper. Web tidak menginjeksi repository dari slice lain.

## 11. Referensi Terkait

- [Vendor Bill](vendor-bill.md)
- [Journal Entry](../accounting/journal-entry.md)
- [Journal Auto-Posting Engine](../../architecture/journal-posting-engine.md)
- [Bank Account](../master/bank-account.md)
- [Numeric Standards](../../spec/numeric-standards.md)
- [Modal Selector](../../spec/modal-selector.md)
- [Autocomplete Generic](../../spec/autocomplete-generic.md)
- [Action Buttons](../../spec/action-buttons.md)
