# Debit Memo (Vendor Return Credit)

Dokumen ini merangkum implementasi Phase D untuk Vendor Debit Memo pada modul **Finance & Accounting > Accounts Payable > Debit Memos**.

## 1. Ringkasan

Debit Memo adalah dokumen AP yang dibuat otomatis dari Purchase Return yang berhasil dikonfirmasi. Setiap Purchase Return `CONFIRMED` memiliki tepat satu Debit Memo, dijaga oleh guard application dan constraint database `UNIQUE (purchase_return_id)`.

Flow aktif:

1. Purchase Return disubmit dan diapprove.
2. User mengonfirmasi Purchase Return.
3. Sistem membuat dan menyelesaikan generated Goods Issue untuk outbound stock.
4. Sistem membuat Debit Memo berstatus `OPEN`.
5. Purchase Return ditandai `CONFIRMED`.

Debit Memo tidak mem-post journal saat dibuat. Journal pengurangan AP, reversal Input VAT, dan aplikasi ke Vendor Bill didefer ke Phase E melalui Debit Memo Allocation.

## 2. Kontrak Fitur

| Area | Kondisi saat ini |
|---|---|
| List | `/accounts-payable/debit-memos` dengan filter keyword, vendor, settlement status, dan rentang memo date |
| Detail | `/accounts-payable/debit-memos/{id}` |
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
| `grossAmountBase`, `dppAmountBase`, `taxAmountBase` | Phase D memakai nilai yang sama dengan original karena Purchase Return belum memisahkan original/base untuk snapshot Debit Memo |
| `settlementStatus` | `OPEN`, `PARTIALLY_SETTLED`, `SETTLED`, atau `CANCELLED` |

Line Debit Memo menyimpan snapshot per Purchase Return line:

| Field | Keterangan |
|---|---|
| `purchaseReturnLineId` | Source line Purchase Return |
| `productId` | Produk yang diretur |
| `quantity` | Qty return |
| `uomId` | UoM transaksi |
| `dppAmountOriginal` / `taxAmountOriginal` | Nilai DPP dan tax source |
| `dppAmountBase` / `taxAmountBase` | Nilai base Phase D |

Source dan monetary snapshot bersifat immutable setelah Debit Memo dibuat.

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

Phase D belum memiliki Debit Memo Allocation atau Vendor Refund. Karena itu settlement recap pada list/detail masih:

- `settledAmount = 0`
- `refundedAmount = 0`
- `remainingAmount = grossAmountOriginal`

Status `PARTIALLY_SETTLED` dan `SETTLED` disiapkan di domain untuk Phase E, tetapi konsumsi aktual belum aktif.

## 6. Cancellation

Cancel hanya tersedia untuk Debit Memo `OPEN`.

Cancel ditolak jika:

- status bukan `OPEN`;
- metadata/settlement state sudah tidak memenuhi guard domain;
- Phase E nanti menemukan confirmed allocation consumption.

Phase D memakai port konsumsi allocation yang selalu mengembalikan false karena tabel Debit Memo Allocation belum ada.

## 7. Accounting dan Deferred Scope

Debit Memo Core tidak mem-post journal saat creation. Purchase Return confirm tetap hanya mem-post journal inventory/GRIR melalui generated Goods Issue dan event `PURCHASE_RETURN`.

Deferred ke Phase E:

- Debit Memo Allocation document;
- aplikasi Debit Memo ke Vendor Bill;
- journal `DEBIT_MEMO_APPLICATION`;
- update `debitMemoAppliedAmount` Vendor Bill;
- settlement recap berbasis allocation confirmed;
- allocation history aktual pada halaman Debit Memo.

Vendor Refund juga belum aktif pada Phase D.

## 8. Otorisasi

| Permission | Akses |
|---|---|
| `DEBIT-MEMO_READ` | Daftar dan detail Debit Memo |
| `DEBIT-MEMO_UPDATE-METADATA` | Update metadata eksternal |
| `DEBIT-MEMO_CANCEL` | Cancel Debit Memo `OPEN` |

## 9. Referensi Terkait

- [Purchase Return](../procurement/purchase-return.md)
- [Goods Issue](../inventory/goods-issue.md)
- [Vendor Bill](vendor-bill.md)
- [Vendor Payment](vendor-payment.md)
- [Action Buttons](../../spec/action-buttons.md)
- [Form Submission](../../spec/form-submission.md)
