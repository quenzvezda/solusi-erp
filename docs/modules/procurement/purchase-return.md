# Purchase Return

## 1. Ringkasan

Purchase Return mencatat pengembalian barang yang sudah diterima melalui Goods Receipt (GR) kepada supplier. Implementasi saat ini mencakup pemilihan GR eligible, draft return, approval, reservasi stok, konfirmasi outbound melalui Goods Issue (GI), posting journal inventory/GRIR khusus Purchase Return, pembuatan Vendor Debit Memo, dan full reversal untuk Purchase Return yang sudah `CONFIRMED`.

Pengurangan AP, reversal Input VAT, alokasi Debit Memo ke Vendor Bill, dan settlement lanjutan berjalan melalui Debit Memo Allocation.

## 2. Alur UI

1. Buka daftar Purchase Return dan pilih **Create**.
2. Pada halaman pemilihan sumber, filter completed GR berdasarkan keyword, supplier, PO, atau rentang tanggal penerimaan.
3. Pilih satu GR eligible.
4. Form draft menampilkan snapshot GR, PO, supplier, facility, currency, kurs, serta seluruh slice lokasi yang masih returnable dengan qty awal `0`.
5. Isi qty positif untuk barang non-serial. Qty tidak boleh melebihi outstanding pada container aktual.
6. Untuk barang serial, pilih serial dari modal. Sistem mengelompokkan pilihan per container aktual dan menurunkan qty dari jumlah serial.
7. Simpan draft, submit ke approval, lalu confirm setelah status `APPROVED`.
8. Setelah confirm berhasil, detail Purchase Return menampilkan link ke generated GI dan Debit Memo.
9. Untuk Purchase Return `CONFIRMED`, pilih **Reverse Purchase Return**, isi tanggal/alasan reversal, pilih target container inbound per movement, lalu submit. Detail akan menampilkan status `REVERSED`, metadata reversal, dan link jurnal reversal.

## 3. Lifecycle

| Status | Keterangan |
|---|---|
| `DRAFT` | Draft dapat diubah dan dibatalkan. |
| `SUBMITTED` | Stok sudah direservasi dan approval sedang berjalan. |
| `APPROVED` | Approval selesai; retur dapat dikonfirmasi atau dibatalkan. |
| `REJECTED` | Approval ditolak; reservasi dilepas. |
| `CANCELLED` | Draft, submission, atau retur approved dibatalkan sesuai guard use case. |
| `CONFIRMED` | GI outbound sudah selesai dan reservasi sudah dikonsumsi. |
| `REVERSED` | Final state untuk Purchase Return confirmed yang sudah dibalik penuh melalui stock reversal, journal reversal, GI cancellation, dan Debit Memo cancellation. |

## 4. Reservasi Stok

Saat submit, Purchase Return membuat ownership pada `inv_stock_reservations` dengan owner type `PURCHASE_RETURN`. Reservasi mengunci valuation reference GR asal, produk, lokasi aktual, qty, dan serial bila ada.

Query availability mengurangi reservasi aktif setelah valuation layer diagregasi. Ini mencegah stok yang sudah ditahan Purchase Return dipakai outbound lain dan mencegah reservasi dikurangi berulang ketika satu origin valuation terpecah menjadi beberapa layer fragment.

Reservasi dilepas ketika submission dibatalkan atau approval ditolak. Saat confirm berhasil, GI mengeluarkan stok memakai movement `ISSUE_RESERVED`, lalu ownership reservasi dikonsumsi setelah journal berhasil dipost.

## 5. Goods Issue dan Accounting

Konfirmasi Purchase Return membuat GI dengan:

- `referenceType=PURCHASE_RETURN`
- `issueDate` sama dengan `returnDate`
- supplier, facility, currency, dan exchange rate dari snapshot Purchase Return
- valuation reference GR asal per line
- container aktual dan serial aktual

GI tersebut tetap menjadi dokumen fisik outbound dan stock movement tetap memakai `ReferenceType.GOODS_ISSUE`. Accounting tidak lagi memakai schema generik `GOODS_ISSUE` untuk sumber Purchase Return; confirm flow mem-post journal dengan:

- `eventType=PURCHASE_RETURN`
- `sourceType=PURCHASE_RETURN`
- `sourceId` dan `sourceCode` dari Purchase Return
- `PR_GRIR_CLEARING_AMT = sum(GoodsIssueLine.inventoryAmount)` sebagai debit GR/IR Clearing
- `PR_INVENTORY_AMT = sum(GoodsIssueLine.inventoryAmount)` sebagai credit Inventory

Journal Purchase Return hanya membalik inventory dan GR/IR sebesar nilai inventory historis. `taxAmount`, `taxReversalAmount`, dan `clearingAmount` tidak diposting di journal ini.

Pada transaksi confirm yang sama, sistem membuat satu Debit Memo `OPEN` dari snapshot Purchase Return. Debit Memo menyimpan source Purchase Return, supplier, currency, return date sebagai memo date, serta DPP/tax/gross snapshot per line. Debit Memo creation tidak mem-post journal.

## 6. Confirmed Reversal

Purchase Return `CONFIRMED` tidak memakai cancel biasa. Reversal memakai status final `REVERSED` agar audit membedakan dokumen yang batal sebelum posting fisik dari dokumen yang pernah mem-post stock/accounting lalu dibalik.

Guard sebelum reversal:

1. Purchase Return harus `CONFIRMED` dan memiliki generated GI.
2. Debit Memo hasil Purchase Return harus ditemukan dan dikunci.
3. Tidak boleh ada Debit Memo Allocation berstatus `CONFIRMED`.
4. Confirmed applied amount Debit Memo harus `0`, sehingga remaining balance kembali full dan status Debit Memo `OPEN`.
5. Accounting period untuk `reversalDate` harus `OPEN`.
6. Semua outbound movement generated GI direverse penuh; target container wajib aktif dan berada dalam facility yang sama.

Urutan transaksi:

1. validasi dan lock Purchase Return;
2. validasi/lock Debit Memo dan guard DMA/full remaining;
3. validasi period reversal;
4. buat inbound stock movement reversal yang terhubung ke movement outbound asal melalui `reversalOfMovementId`;
5. reverse journal `PURCHASE_RETURN` asal memakai linked journal reversal;
6. mark generated GI menjadi `CANCELLED`;
7. cancel Debit Memo;
8. simpan snapshot `PurchaseReturnReversalLine`;
9. mark Purchase Return menjadi `REVERSED`.

Partial Purchase Return reversal, cross-facility reversal, Vendor Refund, dan reversal otomatis DMA dari layar Purchase Return belum termasuk MVP ini.

DMA blocker lintas modul sudah aktif: Purchase Return reversal ditolak selama Debit Memo hasil Purchase Return masih dikonsumsi oleh DMA `CONFIRMED`. Setelah DMA direverse atau dicancel dan Debit Memo kembali fully open, Purchase Return reversal dapat dilanjutkan.

## 7. Beyond MVP

Yang sengaja belum termasuk MVP:

1. Vendor Refund;
2. partial Purchase Return reversal;
3. cross-facility reversal dengan transfer terpisah;
4. reversal otomatis DMA dari layar Purchase Return.

## 8. Otorisasi

| Permission | Akses |
|---|---|
| `PURCHASE-RETURN_READ` | Daftar dan detail |
| `PURCHASE-RETURN_CREATE` | Pemilihan GR dan pembuatan draft |
| `PURCHASE-RETURN_UPDATE` | Perubahan draft |
| `PURCHASE-RETURN_SUBMIT` | Submit ke approval |
| `PURCHASE-RETURN_CONFIRM` | Konfirmasi approved return |
| `PURCHASE-RETURN_CANCEL` | Pembatalan submission atau approved return |
| `PURCHASE-RETURN_REVERSE` | Reversal Purchase Return yang sudah confirmed |
