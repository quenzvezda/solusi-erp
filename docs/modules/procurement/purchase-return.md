# Purchase Return

## 1. Ringkasan

Purchase Return mencatat pengembalian barang yang sudah diterima melalui Goods Receipt (GR) kepada supplier. Implementasi Phase 1 mencakup pemilihan GR eligible, draft return, approval, reservasi stok, dan konfirmasi outbound melalui Goods Issue (GI).

Debit Memo dan event accounting khusus Purchase Return belum termasuk Phase 1.

## 2. Alur UI

1. Buka daftar Purchase Return dan pilih **Create**.
2. Pada halaman pemilihan sumber, filter completed GR berdasarkan keyword, supplier, PO, atau rentang tanggal penerimaan.
3. Pilih satu GR eligible.
4. Form draft menampilkan snapshot GR, PO, supplier, facility, currency, kurs, serta seluruh slice lokasi yang masih returnable dengan qty awal `0`.
5. Isi qty positif untuk barang non-serial. Qty tidak boleh melebihi outstanding pada container aktual.
6. Untuk barang serial, pilih serial dari modal. Sistem mengelompokkan pilihan per container aktual dan menurunkan qty dari jumlah serial.
7. Simpan draft, submit ke approval, lalu confirm setelah status `APPROVED`.

## 3. Lifecycle

| Status | Keterangan |
|---|---|
| `DRAFT` | Draft dapat diubah dan dibatalkan. |
| `SUBMITTED` | Stok sudah direservasi dan approval sedang berjalan. |
| `APPROVED` | Approval selesai; retur dapat dikonfirmasi atau dibatalkan. |
| `REJECTED` | Approval ditolak; reservasi dilepas. |
| `CANCELLED` | Draft, submission, atau retur approved dibatalkan sesuai guard use case. |
| `CONFIRMED` | GI outbound sudah selesai dan reservasi sudah dikonsumsi. |

## 4. Reservasi Stok

Saat submit, Purchase Return membuat ownership pada `inv_stock_reservations` dengan owner type `PURCHASE_RETURN`. Reservasi mengunci valuation reference GR asal, produk, lokasi aktual, qty, dan serial bila ada.

Query availability mengurangi reservasi aktif setelah valuation layer diagregasi. Ini mencegah stok yang sudah ditahan Purchase Return dipakai outbound lain dan mencegah reservasi dikurangi berulang ketika satu origin valuation terpecah menjadi beberapa layer fragment.

Reservasi dilepas ketika submission dibatalkan atau approval ditolak. Saat confirm berhasil, GI mengeluarkan stok memakai movement `ISSUE_RESERVED`, lalu ownership reservasi dikonsumsi setelah journal berhasil dipost.

## 5. Goods Issue dan Accounting Phase 1

Konfirmasi Purchase Return membuat GI dengan:

- `referenceType=PURCHASE_RETURN`
- `issueDate` sama dengan `returnDate`
- supplier, facility, currency, dan exchange rate dari snapshot Purchase Return
- valuation reference GR asal per line
- container aktual dan serial aktual

Phase 1 tetap memakai schema event accounting generik `GOODS_ISSUE`. Adapter Purchase Return sengaja mengembalikan placeholder `billPosted=false` dan clearing account kosong karena Debit Memo belum tersedia.

## 6. Batas Wajib Phase 2

Phase 2 wajib:

1. menambahkan Debit Memo;
2. menambahkan event schema accounting khusus `PURCHASE_RETURN`;
3. memigrasikan confirm flow dari journal generik GI ke journal Purchase Return;
4. memakai status billing dan clearing account aktual;
5. menangani reversal pajak, GR/IR, AP, dan FX sesuai kondisi invoice.

## 7. Otorisasi

| Permission | Akses |
|---|---|
| `PURCHASE-RETURN_READ` | Daftar dan detail |
| `PURCHASE-RETURN_CREATE` | Pemilihan GR dan pembuatan draft |
| `PURCHASE-RETURN_UPDATE` | Perubahan draft |
| `PURCHASE-RETURN_SUBMIT` | Submit ke approval |
| `PURCHASE-RETURN_CONFIRM` | Konfirmasi approved return |
| `PURCHASE-RETURN_CANCEL` | Pembatalan submission atau approved return |
