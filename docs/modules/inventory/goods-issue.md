# Goods Issue (GI)

Dokumen ini merangkum kondisi implementasi aktual **Goods Issue** pada codebase saat ini. GI adalah dokumen inventory outbound generik, yaitu pasangan konseptual dari Goods Receipt untuk arus barang keluar.

## 1. Ringkasan

GI dipakai sebagai lapisan dokumen fisik/audit di atas stock ledger. Source module tidak boleh langsung mengeluarkan stock tanpa dokumen outbound ketika flow bisnisnya membutuhkan audit dokumen. GI manual memanggil `StockService.adjust()` dengan `MovementType.ISSUE`. Purchase Return memakai `MovementType.ISSUE_RESERVED` untuk mengonsumsi stock yang sudah direservasi saat submit.

Integrasi konkret Purchase Return aktif melalui resolver dan adapter source GI.

## 2. Lifecycle

| Status | Keterangan |
|---|---|
| `DRAFT` | Draft bisa dibuat, diubah, dihapus, dan dilengkapi line. |
| `COMPLETED` | Dokumen sudah mem-post stock issue dan auto journal. Normal edit/delete ditolak. |
| `CANCELLED` | Dokumen completed yang dibalik melalui linked stock movement reversal dan linked journal reversal. |

Transisi utama:

1. `DRAFT -> COMPLETED`
2. `COMPLETED -> CANCELLED`

Dokumen `COMPLETED` dan `CANCELLED` diperlakukan immutable untuk update draft.

Pembatalan langsung dari modul GI hanya berlaku untuk GI `MANUAL`. GI yang berasal dari dokumen sumber seperti Purchase Return tidak boleh dibatalkan dari layar GI; reversal harus dijalankan oleh modul sumber agar status dan audit bisnis sumber tetap konsisten.

## 3. Model Data

### Header

| Field | Keterangan |
|---|---|
| `code` | Nomor dokumen dari sequence `GOODS_ISSUE`. |
| `issueDate` | Tanggal barang keluar dan posting date. |
| `referenceType` | Source dokumen, misalnya `PURCHASE_RETURN` atau `MANUAL`. |
| `referenceId` | ID dokumen sumber. |
| `referenceCode` | Snapshot kode dokumen sumber. |
| `partyId` / `partyType` | Snapshot pihak bisnis, misalnya supplier untuk Purchase Return. |
| `facilityId` | Snapshot facility sumber. |
| `currencyId` / `exchangeRate` | Snapshot currency dan rate dari source. |
| `status` | `DRAFT`, `COMPLETED`, atau `CANCELLED`. |
| `note` | Catatan dokumen. |

### Line

| Field | Keterangan |
|---|---|
| `referenceLineId` | Line sumber, jika GI berasal dari dokumen lain. |
| `productId` | Produk yang dikeluarkan. |
| `serialized` | Penanda item serial. |
| `quantityIssued` / `baseQuantity` | Qty transaksi dan qty base untuk stock/valuation. |
| `uomId` | UoM transaksi. |
| `facilityId`, `gridId`, `containerId` | Snapshot lokasi barang keluar. |
| `serialNumber` | CSV serial per line untuk draft awal. |
| `unitCost`, `inventoryAmount`, `taxBaseAmount`, `taxAmount`, `clearingAmount` | Snapshot nilai saat complete. |
| `valuationRefType`, `valuationRefId`, `valuationRefLineId` | Referensi valuation layer spesifik. Untuk Purchase Return, ini menunjuk GR/GR line asal. |

## 4. Source Resolver Model

Core GI memakai `GoodsIssueSourceResolverRegistry` dan `GoodsIssueSourceResolver`.

Resolver bertugas membuat draft GI dari source:

1. memuat header source
2. mengisi snapshot reference, party, facility, currency, dan rate
3. mengisi eligible line
4. menyertakan valuation reference bila source membutuhkan specific-layer consumption

Jika source belum tersedia, core tetap compile melalui no-op lookup provider dan source selector akan menampilkan warning unsupported.

## 5. Stock Posting Dan Valuation

Saat `COMPLETE`, GI:

1. memastikan accounting period untuk `issueDate` masih OPEN
2. menghitung ulang base quantity dan amount snapshot
3. memanggil `StockService.adjust()` dengan `MovementType.ISSUE`, atau `MovementType.ISSUE_RESERVED` untuk source Purchase Return
4. mengirim `ReferenceType.GOODS_ISSUE`, GI id, dan GI code ke stock movement
5. meneruskan `valuationRefType`, `valuationRefId`, dan `valuationRefLineId` agar stock service dapat mengonsumsi layer spesifik
6. untuk serialized item, mem-post satu movement per serial dan mewajibkan base quantity bilangan bulat

Untuk Purchase Return, valuation wajib memakai GR asal agar nilai inventory keluar sama dengan penerimaan yang dikembalikan.

Saat GI manual `COMPLETED` dibatalkan, sistem membuat movement inbound baru untuk setiap movement outbound asal dan mengisi `reversal_of_movement_id` ke movement asal. `referenceType`, `referenceId`, dan `referenceCode` tetap menunjuk dokumen fisik GI yang sama sehingga stock card tetap bisa ditelusuri dari dokumen asal. Target container reversal wajib berada dalam facility yang sama; UI memberi default container historis issue, tetapi user dapat memilih container aktif lain dalam facility yang sama.

Valuation reversal tidak mengembalikan quantity ke layer GR lama secara tersembunyi. Sistem membuat inbound valuation layer baru dengan `reversalOfMovementId` dan historical issue unit cost dari movement asal. Layer reversal ini dapat dikonsumsi oleh FIFO outbound berikutnya seperti inbound layer normal.

## 6. Accounting

Core GI manual/generic memakai event accounting `GOODS_ISSUE` dengan variable:

| Variable | Makna |
|---|---|
| `GI_COGS_AMT` | Debit counterpart generic untuk GI. |
| `GI_INVENTORY_AMT` | Credit inventory amount. |

Accounting schema `GOODS_ISSUE` tetap disiapkan untuk outbound generic/manual dan tidak dipakai untuk Purchase Return-sourced GI.

Saat `referenceType=PURCHASE_RETURN`, GI completion tetap mem-post stock movement dengan `ReferenceType.GOODS_ISSUE`, GI id, dan GI code, tetapi auto journal diroute ke source-specific event `PURCHASE_RETURN`. Journal tersebut memakai `sourceType=PURCHASE_RETURN`, source id/code Purchase Return, dan variable:

| Variable | Makna |
|---|---|
| `PR_GRIR_CLEARING_AMT` | Debit GR/IR Clearing sebesar inventory amount historis dari line GI. |
| `PR_INVENTORY_AMT` | Credit inventory sebesar inventory amount historis dari line GI. |

Purchase Return journal tidak mem-post `taxAmount`, `taxReversalAmount`, atau `clearingAmount`. Reversal Input VAT, AP reduction, allocation settlement, dan FX tetap deferred ke Debit Memo Allocation/phase berikutnya. Core GI tidak mem-post FX variance untuk Purchase Return reversal; rate berasal dari source/original GR.

Cancellation GI manual membalik journal `GOODS_ISSUE` asal dengan linked reversal journal. Reversal journal menukar debit/kredit dari final `JournalLine` asal, menyimpan `reversalOfId`, dan tidak memanggil accounting schema dengan amount negatif.

## 7. Purchase Return Seam

Port `PurchaseReturnGoodsIssueSourcePort` mendefinisikan kontrak minimum yang harus disediakan module Purchase Return:

1. header lookup berisi purchase return code, supplier, facility, currency, exchange rate, bill posted flag, dan clearing account target
2. eligible return lines berisi product, qty, UoM, location, serial CSV, original GR/GR line, valuation refs, inventory amount, tax reversal amount, dan clearing amount
3. `hasCompletedGoodsIssue(purchaseReturnId)` sebagai guard idempotency agar satu Purchase Return tidak membuat GI completed ganda

Adapter `GoodsIssueSourceResolver` untuk Purchase Return sudah aktif. Confirm Purchase Return membuat GI dari snapshot return, menyelesaikan posting reserved issue, lalu mengonsumsi reservation setelah journal `PURCHASE_RETURN` berhasil diposting.

## 8. UI Behavior

GI UI terdiri dari list, detail, create/edit form, dan source-line selector.

Form memakai pola header-lines:

1. header source tampil sebagai snapshot read-only
2. source data tetap disimpan sebagai hidden fields untuk submit AJAX
3. line source-derived mengunci product/UOM/valuation ref
4. tombol Add Line membuka modal selector untuk source-based GI
5. manual blank line hanya disiapkan untuk `referenceType=MANUAL`
6. product, grid, dan container memakai autocomplete/TomSelect contract dengan Trinity data
7. qty/UOM/serial diedit melalui drawer
8. numeric input memakai AutoNumeric class dan page JS memakai `ErpNumeric.get/set`
9. submit validation berjalan pada capture phase sebelum AJAX handler global
10. dirty-form guard disuppressed untuk action complete/cancel yang intentional
11. GI manual `COMPLETED` menampilkan form cancel khusus yang meminta reversal date, alasan, dan target container per movement asal.
12. Detail GI menampilkan link jurnal asal dan jurnal reversal bila tersedia.

## 9. Deferred Items

1. Debit Memo, AP reduction, Input VAT reversal, allocation settlement, dan FX untuk Purchase Return.
2. Confirmed Purchase Return reversal orchestration tetap deferred ke Phase F walaupun primitive linked reversal generic sudah tersedia.
3. Manual GI business rules yang lengkap.
4. Sales/Delivery Order, scrap, internal use, production/consumption resolvers.
5. Dedicated serial detail table per line.
6. Playwright E2E flow untuk selector, drawer, save, complete, dan cancel.
