# Debit Memo Allocation

Debit Memo Allocation (DMA) adalah dokumen settlement AP yang menerapkan satu Debit Memo ke satu atau lebih Vendor Bill confirmed.

## 1. Lifecycle

```text
DRAFT -> CONFIRMED
DRAFT -> CANCELLED
CONFIRMED -> REVERSED
```

Draft menyimpan snapshot current balance tetapi tidak menjadi reservasi. Outstanding Vendor Bill dan remaining Debit Memo baru berubah setelah DMA dikonfirmasi.

## 2. Flow

1. User membuka Debit Memo detail dan memilih **Allocate**, atau membuka Vendor Bill detail dan memilih **Apply Debit Memo**.
2. Form DMA memilih Debit Memo dan Vendor Bill eligible.
3. User mengisi amount yang diaplikasikan per Vendor Bill.
4. Save menyimpan draft dan snapshot proration DPP/tax/base/FX.
5. Confirm mengunci Debit Memo dan Vendor Bill target, revalidasi current balance, post journal `DEBIT_MEMO_APPLICATION`, lalu update settlement projection.
6. Reverse membuat linked reversal journal dan mengembalikan settlement projection.

## 3. Eligibility

Vendor Bill eligible:

- `documentStatus = CONFIRMED`
- `settlementStatus = OPEN/PARTIALLY_SETTLED`
- `outstandingAmount > 0`
- vendor dan currency sama dengan Debit Memo

Debit Memo eligible:

- `settlementStatus = OPEN/PARTIALLY_SETTLED`
- `remainingAmount > 0`
- vendor dan currency sama dengan Vendor Bill

## 4. Accounting

Confirmed DMA mem-post `DEBIT_MEMO_APPLICATION` dengan variable:

| Variable | Posisi umum | Keterangan |
|---|---|---|
| `DMA_AP_AMT` | Debit | Mengurangi Accounts Payable |
| `DMA_GRIR_CLEARING_AMT` | Credit | Membalik GR/IR clearing dari return |
| `DMA_TAX_AMT` | Credit | Membalik Input VAT |
| `DMA_FX_LOSS_AMT` | Debit | Selisih kurs rugi |
| `DMA_FX_GAIN_AMT` | Credit | Selisih kurs untung |

Original/base snapshots disimpan pada line DMA. Journal memakai base values.

## 5. UI Routes

| Route | Fungsi |
|---|---|
| `/accounts-payable/debit-memo-allocations` | List DMA |
| `/accounts-payable/debit-memo-allocations/create` | Create form |
| `/accounts-payable/debit-memo-allocations/edit/{id}` | Edit draft |
| `/accounts-payable/debit-memo-allocations/{id}` | Detail |
| `POST /{id}/confirm` | Confirm draft |
| `POST /{id}/cancel` | Cancel draft |
| `POST /{id}/reverse` | Reverse confirmed |

List DMA memakai filter query-level keyword, vendor lookup, status, dan rentang allocation date. Kolom utama adalah code, allocation date, Debit Memo, vendor, currency, applied gross, status, dan actions. Pagination memakai fragment standar, dan sortable header hanya dipakai untuk field yang didukung query.

Form DMA memakai selector query-level dan paginated untuk Debit Memo serta Vendor Bill eligible. Selector tidak mengambil semua data untuk difilter di browser. Draft stale direvalidasi saat confirm agar outstanding Vendor Bill dan remaining Debit Memo terbaru tetap aman dari over-allocation.

Detail DMA menampilkan link ke Debit Memo, Vendor Bill lines, original application journal, dan reversal journal bila ada. Status `CONFIRMED`, `REVERSED`, `CANCELLED`, dan `DRAFT` memakai badge lifecycle yang berbeda. Setelah reversal, detail menampilkan reversal date, reason, reversal journal, dan state view-only.

## 6. Permissions

| Permission | Akses |
|---|---|
| `DEBIT-MEMO-ALLOCATION_READ` | List/detail |
| `DEBIT-MEMO-ALLOCATION_CREATE` | Create draft |
| `DEBIT-MEMO-ALLOCATION_UPDATE` | Edit draft |
| `DEBIT-MEMO-ALLOCATION_CONFIRM` | Confirm draft |
| `DEBIT-MEMO-ALLOCATION_CANCEL` | Cancel draft |
| `DEBIT-MEMO-ALLOCATION_REVERSE` | Reverse confirmed |
