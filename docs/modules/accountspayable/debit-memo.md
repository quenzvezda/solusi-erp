# Debit Memo (Vendor Return Credit)

Dokumen ini merangkum implementasi Vendor Debit Memo pada modul **Finance & Accounting > Accounts Payable > Debit Memos**.

## 1. Ringkasan

Debit Memo adalah dokumen AP yang dibuat otomatis dari Purchase Return yang berhasil dikonfirmasi. Setiap Purchase Return `CONFIRMED` memiliki tepat satu Debit Memo, dijaga oleh guard application dan constraint database `UNIQUE (purchase_return_id)`.

Flow aktif:

1. User mengonfirmasi Purchase Return.
2. Sistem membuat dan menyelesaikan generated Goods Issue untuk outbound stock.
3. Sistem membuat Debit Memo berstatus `OPEN`.
4. User membuat Debit Memo Allocation untuk menerapkan saldo Debit Memo ke satu atau lebih Vendor Bill.
5. Konfirmasi Allocation mem-post journal `DEBIT_MEMO_APPLICATION`, mengurangi outstanding Vendor Bill, dan menyegarkan settlement status Debit Memo/Vendor Bill.

Debit Memo core tetap tidak mem-post journal saat dibuat. Journal AP reduction dan tax/GRIR reversal terjadi saat Debit Memo Allocation dikonfirmasi.

## 2. Kontrak Fitur

| Area | Kondisi saat ini |
|---|---|
| List | `/accounts-payable/debit-memos` dengan filter keyword, vendor, settlement status, dan rentang memo date |
| Detail | `/accounts-payable/debit-memos/{id}` |
| Allocate shortcut | Detail menampilkan tombol Allocate untuk status `OPEN/PARTIALLY_SETTLED` dengan remaining > 0 |
| Allocation history | Detail menampilkan history DMA dan link ke detail allocation |
| Update metadata | `POST /accounts-payable/debit-memos/{id}/metadata` via AJAX JSON |
| Cancel | `POST /accounts-payable/debit-memos/{id}/cancel` |
| Source link | Detail menampilkan link ke Purchase Return dan generated Goods Issue |
| Purchase Return link | Detail Purchase Return menampilkan link ke Debit Memo jika sudah dibuat |

## 3. Data Snapshot

Header Debit Memo menyimpan snapshot:

| Field | Keterangan |
|---|---|
| `code` | Nomor internal sequence `DEBIT_MEMO` dengan format `DM-{yyyyMM}-{seq}` |
| `purchaseReturnId` / `purchaseReturnCode` | Source Purchase Return |
| `vendorId` | Supplier dari Purchase Return |
| `currencyId` | Currency dari Purchase Return |
| `memoDate` | Mengikuti `returnDate` Purchase Return |
| `grossAmountOriginal` | Total DPP + tax dari source return |
| `dppAmountOriginal` | Total clearing/DPP return |
| `taxAmountOriginal` | Total tax reversal return |
| `grossAmountBase`, `dppAmountBase`, `taxAmountBase` | Base amount untuk journal allocation |
| `settlementStatus` | `OPEN`, `PARTIALLY_SETTLED`, `SETTLED`, atau `CANCELLED` |

Line Debit Memo menyimpan snapshot per Purchase Return line. Source dan monetary snapshot bersifat immutable setelah Debit Memo dibuat.

## 4. Metadata Eksternal

Field berikut dapat diedit selama Debit Memo tidak `CANCELLED`:

- supplier memo number
- supplier memo date
- tax document number
- tax document date
- notes

Validasi friendly:

- Supplier memo number unik per vendor.
- Tax document number unik global.

## 5. Settlement Lifecycle

Lifecycle status settlement:

```text
OPEN -> PARTIALLY_SETTLED -> SETTLED
OPEN -> SETTLED
OPEN -> CANCELLED
```

Confirmed, non-reversed Debit Memo Allocation mengisi:

- `settledAmount`
- `remainingAmount = grossAmountOriginal - settledAmount - refundedAmount`
- status `PARTIALLY_SETTLED` atau `SETTLED`

Reversal DMA mengeluarkan konsumsi tersebut dari projection dan dapat mengembalikan status Debit Memo ke `OPEN` atau `PARTIALLY_SETTLED`.

## 6. Cancellation

Cancel hanya tersedia untuk Debit Memo `OPEN`.

Cancel ditolak jika:

- status bukan `OPEN`;
- ada Debit Memo Allocation berstatus `CONFIRMED` untuk Debit Memo tersebut.

DMA `CANCELLED` atau `REVERSED` tidak memblokir cancel karena tidak lagi menjadi active consumption.

## 7. Accounting

Debit Memo creation tidak mem-post journal. Konfirmasi Debit Memo Allocation mem-post event `DEBIT_MEMO_APPLICATION`:

| Variable | Nilai |
|---|---|
| `DMA_AP_AMT` | AP reduction base, debit AP |
| `DMA_GRIR_CLEARING_AMT` | GR/IR clearing reversal base, credit GR/IR |
| `DMA_TAX_AMT` | Input VAT reversal base, credit Input VAT |
| `DMA_FX_LOSS_AMT` | FX loss base, debit FX loss |
| `DMA_FX_GAIN_AMT` | FX gain base, credit FX gain |

Reversal DMA memakai generic linked journal reversal terhadap journal aplikasi tersebut.

Vendor Refund belum aktif.

## 8. Otorisasi

| Permission | Akses |
|---|---|
| `DEBIT-MEMO_READ` | Daftar dan detail Debit Memo |
| `DEBIT-MEMO_UPDATE-METADATA` | Update metadata eksternal |
| `DEBIT-MEMO_CANCEL` | Cancel Debit Memo `OPEN` tanpa active DMA consumption |
| `DEBIT-MEMO-ALLOCATION_CREATE` | Membuat DMA dari shortcut detail Debit Memo |

## 9. Referensi Terkait

- [Debit Memo Allocation](debit-memo-allocation.md)
- [Purchase Return](../procurement/purchase-return.md)
- [Goods Issue](../inventory/goods-issue.md)
- [Vendor Bill](vendor-bill.md)
- [Vendor Payment](vendor-payment.md)
- [Action Buttons](../../spec/action-buttons.md)
- [Form Submission](../../spec/form-submission.md)
