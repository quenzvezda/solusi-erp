# Vendor Bill (AP Invoice)

Dokumen ini merangkum implementasi aktual Vendor Bill pada modul **Finance & Accounting > Account Payable > Vendor Bill**.

## 1. Ringkasan Implementasi Saat Ini

Vendor Bill adalah dokumen invoice vendor yang dibuat dari Goods Receipt yang sudah `COMPLETED` dan masih memiliki quantity yang belum dibilling.

Flow yang aktif saat ini:

1. User memilih satu atau lebih Goods Receipt billable.
2. Sistem membuat draft Vendor Bill dengan line dari GR yang dipilih.
3. User mengisi nomor invoice vendor, tanggal bill, due date, exchange rate, dan notes.
4. Draft Vendor Bill disimpan.
5. Vendor Bill dikonfirmasi.
6. Sistem mem-post journal `VENDOR_BILL` secara sinkron dalam transaksi confirm.

Document lifecycle:

```text
DRAFT -> CONFIRMED
DRAFT -> CANCELLED
```

Settlement lifecycle untuk Vendor Bill yang sudah `CONFIRMED`:

```text
OPEN -> PARTIALLY_SETTLED -> SETTLED
OPEN -> SETTLED
```

Catatan:
- `CONFIRMED` dan `CANCELLED` adalah status dokumen final untuk kebutuhan edit/delete/cancel.
- Delete hanya tersedia dari halaman list untuk status `DRAFT`.
- Cancel hanya tersedia dari halaman detail untuk status `DRAFT`.
- Status settlement dan outstanding pada list/detail dihitung sebagai projection dari settlement document yang sudah confirmed.

## 2. Kontrak Fitur yang Aktif

| Area | Kondisi saat ini |
|---|---|
| List | `/accounts-payable/vendor-bills` dengan filter keyword, vendor, document status, settlement status |
| Select references | `/accounts-payable/vendor-bills/select-references` |
| Create form | `/accounts-payable/vendor-bills/create?selectedGrIds=...` |
| Save draft | `POST /accounts-payable/vendor-bills` via AJAX form |
| Update draft | `PUT /accounts-payable/vendor-bills/{id}` via AJAX form |
| Confirm | `POST /accounts-payable/vendor-bills/{id}/confirm` |
| Cancel | `POST /accounts-payable/vendor-bills/{id}/cancel` |
| Delete | `DELETE /accounts-payable/vendor-bills/{id}` via HTMX row delete |

## 3. Source dan Matching

Vendor Bill saat ini hanya mendukung source dari Goods Receipt.

Syarat GR bisa dipilih:
- status GR `COMPLETED`
- masih ada outstanding quantity yang belum dibilling di Vendor Bill `CONFIRMED`
- vendor dan currency antar selected GR harus sama

Jika selected GR memiliki exchange rate berbeda, create form menampilkan hint dan user wajib menentukan exchange rate invoice.

## 4. Model Data yang Dipakai

### A. Header Vendor Bill

| Field | Keterangan |
|---|---|
| `code` | Nomor internal dari sequence `VENDOR-BILL` |
| `vendorId` | Vendor invoice |
| `vendorInvoiceNumber` | Nomor invoice dari vendor, input manual |
| `billDate` | Tanggal invoice |
| `dueDate` | Tanggal jatuh tempo |
| `currencyId` | Currency invoice |
| `exchangeRate` | Kurs invoice ke base currency |
| `documentStatus` | `DRAFT`, `CONFIRMED`, atau `CANCELLED` |
| `settlementStatus` | `OPEN`, `PARTIALLY_SETTLED`, `SETTLED`, atau `NULL` untuk dokumen yang belum confirmed / cancelled |
| `subtotal` | Total DPP/net invoice |
| `taxAmount` | Total pajak invoice |
| `totalAmount` | Total gross invoice = subtotal + taxAmount |
| `paidAmount` | Total pembayaran dari Vendor Payment `CONFIRMED` |
| `debitMemoAppliedAmount` | Total Debit Memo Allocation berstatus `CONFIRMED` |
| `outstandingAmount` | Sisa outstanding = totalAmount - paidAmount - debitMemoAppliedAmount |
| `notes` | Catatan |

### B. Line Vendor Bill

| Field | Keterangan |
|---|---|
| `grLineId` | Line Goods Receipt yang ditagihkan |
| `productId` | Produk snapshot dari GR |
| `qtyBilled` | Quantity yang ditagihkan |
| `uomId` | UoM transaksi |
| `unitPrice` | Harga referensi dari source |
| `inventoryAmount` | DPP/net amount line |
| `taxAmount` | Pajak line |
| `lineTotal` | DPP/net amount line yang dipakai untuk clearing GR/IR |

`lineTotal` pada Vendor Bill adalah nilai net/DPP, bukan gross.
Gross per line ditampilkan sebagai `lineTotal + taxAmount`.

## 5. Tax Timing

Implementasi saat ini memakai tax timing invoice-based:

- Goods Receipt hanya mem-post inventory dan GR/IR sebesar nilai net/DPP.
- Goods Receipt tidak mengakui Input VAT.
- Vendor Bill mengakui Input VAT saat invoice dikonfirmasi.

Contoh:

```text
PO/GR DPP 108,000,000
Tax 11%   11,880,000
Gross    119,880,000
```

Goods Receipt journal:

```text
DR Merchandise Inventory     108,000,000
CR GR/IR Clearing            108,000,000
```

Vendor Bill journal:

```text
DR GR/IR Clearing            108,000,000
DR Tax Receivable/Input VAT    11,880,000
CR Accounts Payable          119,880,000
```

## 6. Proration dan Partial Billing

Vendor Bill line diprorata berdasarkan quantity:

```text
line_dpp = qty_billed / qty_received * gr_line.inventory_amount
line_tax = qty_billed / qty_received * gr_line.tax_amount
```

Untuk billing terakhir atas satu GR line, sisa pembulatan dihitung sebagai remainder agar total billing akhir sama dengan amount GR source:

```text
last_line_dpp = gr_line.inventory_amount - confirmed_dpp_before - current_accumulated_dpp
last_line_tax = gr_line.tax_amount - confirmed_tax_before - current_accumulated_tax
```

## 7. Draft Validation dan UI

Save draft membutuhkan minimal satu line aktif.

Validasi dilakukan di dua lapisan:
- client-side: form submit diblokir dan menampilkan warning modal standar jika line kosong
- server-side: create/update melempar error `msg.err.vb.lines.required`

Create form menampilkan recap:
- DPP
- Tax
- Total Invoice
- Total Base

Qty line mengikuti numeric standard:
- input text, bukan number
- class `erp-number-decimal`
- display 2 angka desimal


## 8. Settlement Visibility

Halaman list Vendor Bill menampilkan `documentStatus`, `settlementStatus`, dan `outstandingAmount`.

Halaman detail Vendor Bill menampilkan ringkasan settlement:
- `settlementStatus`
- `paidAmount` dari total line Vendor Payment berstatus `CONFIRMED`
- `debitMemoAppliedAmount` dari Debit Memo Allocation berstatus `CONFIRMED`
- `outstandingAmount = totalAmount - paidAmount - debitMemoAppliedAmount`
- shortcut **Apply Debit Memo** jika Vendor Bill confirmed, open/partial, outstanding > 0, dan ada Debit Memo eligible
- history Debit Memo Allocation confirmed/reversed yang pernah menyentuh bill tersebut

Settlement summary bersifat read-side projection; web layer tetap memakai use case/mapper dan tidak membaca repository Vendor Payment atau future Debit Memo Allocation secara langsung.

Debit Memo Allocation adalah settlement source kedua selain Vendor Payment. Draft DMA tidak mengurangi outstanding; hanya DMA `CONFIRMED` yang mengisi `debitMemoAppliedAmount`. DMA `REVERSED` dikeluarkan kembali dari projection.

## 9. Accounting Saat Confirm

`ConfirmVendorBillUseCase` mem-post journal event `VENDOR_BILL`.

Variable yang dikirim:

| Variable | Nilai | Posisi schema umum |
|---|---|---|
| `VB_GRIR_CLEARING_AMT` | subtotal/DPP base | Debit |
| `VB_TAX_AMT` | tax base | Debit |
| `VB_AP_TOTAL` | total gross base | Credit |
| `VB_FX_LOSS_AMT` | FX loss jika ada | Debit |
| `VB_FX_GAIN_AMT` | FX gain jika ada | Credit |

FX variance dihitung atas porsi net/GR-IR, bukan atas tax.

Accounting schema aktif untuk event `VENDOR_BILL` wajib ada saat confirm. Jika schema tidak ada, tidak aktif, atau hasil jurnal tidak balance, confirm rollback.

## 10. Clean Architecture Notes

Vendor Bill tidak membaca repository slice Goods Receipt secara langsung dari web/application command.

Akses data lintas slice dilakukan melalui port/adapter:
- `BillableGrQueryPort`
- `BillableGrLineView`
- billable reference provider

Web layer boleh memakai use case, lookup/query port, dan mapper web; web tidak boleh menginjeksi repository/JPA repository dari slice lain.

## 11. Referensi Terkait

- [Goods Receipt](../inventory/goods-receipt.md)
- [Journal Entry](../accounting/journal-entry.md)
- [Journal Auto-Posting Engine](../../architecture/journal-posting-engine.md)
- [Numeric Standards](../../spec/numeric-standards.md)
- [Action Buttons](../../spec/action-buttons.md)
