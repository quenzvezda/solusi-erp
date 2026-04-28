# Goods Receipt (GR)

Dokumen ini merangkum **kondisi implementasi aktual** Goods Receipt pada codebase saat ini. Posisi GR sekarang adalah **hybrid**: kontrak create publik sudah bergerak ke model referensi generik, tetapi flow operasional yang benar-benar aktif masih **Purchase Order-driven**.

## 1. Ringkasan Implementasi Saat Ini

1. **Entrypoint create kanonik sudah generik**:
   - `GET /inventory/goods-receipts/create?referenceType=PURCHASE_ORDER&referenceId={id}`
2. **Legacy compatibility** masih ada:
   - `GET /inventory/goods-receipts/create?poId={id}` masih diterima sebagai fallback.
3. **Source yang benar-benar didukung baru `PURCHASE_ORDER`**. Reference type lain akan ditolak saat create/save.
4. **Create/update/complete masih memakai Purchase Order sebagai sumber bisnis utama** untuk validasi outstanding quantity, harga referensi, kurs, dan update status receiving.
5. **Lifecycle tetap sederhana**:
   - `DRAFT -> COMPLETED`
   - dokumen `COMPLETED` tidak bisa diubah lagi.
6. **Accounting period OPEN wajib lolos saat COMPLETE**, tetapi **accounting schema belum menjadi dependency wajib** di GR saat ini.

## 2. Kontrak Fitur yang Aktif

### A. Entrypoint & Surface

| Area | Kondisi saat ini |
|---|---|
| Create form | Dibuka dari detail PO, tetapi memakai parameter generik `referenceType/referenceId` |
| Resolver source | Sudah memakai `GoodsReceiptSourceResolverRegistry` |
| Resolver aktif | Baru `PurchaseOrderGoodsReceiptSourceResolver` |
| Save draft | Masih memanggil use case PO-based (`referenceId` diperlakukan sebagai `poId`) |
| Complete | Masih memuat ulang PO dari `receipt.getPoId()` |
| List | Dipakai sebagai audit/listing GR dengan keyword search |

### B. Scope Generic vs Scope Aktual

- **Sudah generik di public contract**
  - header menyimpan `referenceType` dan `referenceId`
  - line menyimpan `referenceLineId`
  - resolver registry sudah disiapkan per source type
- **Masih PO-only di business execution**
  - create hanya menerima `PURCHASE_ORDER`
  - save/update/complete masih load `PurchaseOrderRepository`
  - validasi outstanding masih dibandingkan ke line PO
  - kode referensi yang bisa di-resolve baru PO

## 3. Model Data yang Dipakai Sekarang

### A. Header GR

| Field | Keterangan |
|---|---|
| `code` | Nomor dokumen dari sequence `GOODS_RECEIPT` |
| `receiptDate` | Tanggal penerimaan fisik |
| `referenceType` | Jenis dokumen sumber, saat ini aktif: `PURCHASE_ORDER` |
| `referenceId` | ID dokumen sumber |
| `supplierId` | Snapshot supplier dari source |
| `facilityId` | Snapshot facility dari source |
| `currencyId` | Snapshot currency dari source |
| `exchangeRate` | Snapshot kurs dari source PO |
| `status` | `DRAFT` atau `COMPLETED` |
| `note` / `notes` | Catatan dokumen |

### B. Line GR

| Field | Keterangan |
|---|---|
| `referenceLineId` | Referensi line sumber. Untuk flow aktif sekarang, ini menunjuk line PO |
| `productId` | Produk yang diterima |
| `serialized` | Penanda item serial |
| `quantityReceived` | Qty transaksi yang diterima |
| `uomId` | UoM transaksi |
| `containerId` | Container tujuan; saat ini masih nullable di persistence dan belum divalidasi mandatory |
| `baseQuantity` | Snapshot qty base untuk valuasi; pada draft awal masih placeholder |
| `inventoryAmount` | Snapshot nilai inventory; saat ini masih placeholder pada draft/save flow |
| `taxBaseAmount` | Snapshot basis pajak; saat ini masih placeholder |
| `taxAmount` | Snapshot pajak; saat ini masih placeholder |
| `grIrAmount` | Snapshot GR/IR; saat ini masih placeholder |
| `serialNumber` | Draft serial disimpan sebagai CSV per line |

### C. Catatan Kompatibilitas Nama Field

- DTO save menerima alias lama `poLineId`, tetapi field kanoniknya sekarang adalah `referenceLineId`.
- Domain/entity masih menyediakan helper bridge `getPoId()` untuk flow `PURCHASE_ORDER`.
- Migrasi database sudah digeneralisasi dari `po_id` ke `reference_type/reference_id`, dan dari `po_line_id` ke `reference_line_id`.

## 4. Workflow & Aturan Bisnis Aktual

### A. Create Draft

1. User memulai dari detail PO.
2. Controller menerima `referenceType/referenceId` (atau fallback `poId`).
3. `GetGoodsReceiptCreateViewUseCase` meminta resolver sesuai `referenceType`.
4. Resolver PO:
   - memuat PO
   - memastikan status PO bisa receive
   - hanya mengambil line dengan outstanding qty > 0
   - mem-prefill GR line dengan qty `0`
   - mengambil flag `serialized` dari product

### B. Draft Form

- Header referensi (`referenceType`, `referenceCode`, supplier, facility, currency) tampil **read-only**.
- `referenceType` dan `referenceId` tetap disimpan sebagai hidden field untuk submit.
- Detail qty/UoM/serial diatur lewat **drawer**:
  - non-serialized: drawer qty + target UoM
  - serialized: drawer qty + target UoM + grid serial per unit base
- Draft serial disimpan sebagai **CSV** pada hidden field `serialNumber`.

### C. Save / Update Draft

- Hanya line dengan qty > 0 yang benar-benar dipersist.
- Outstanding quantity dibandingkan lagi ke kondisi PO terbaru.
- Jika source line tidak lagi cocok dengan kondisi PO terbaru, sistem melempar error stale draft.
- Walaupun UI punya tombol **Add Line**, line baru tetap harus punya `referenceLineId` yang valid terhadap PO aktif agar bisa lolos save/complete.

### D. Complete

Saat GR di-complete, sistem saat ini melakukan:

1. validasi `receiptDate` harus berada pada **accounting period OPEN**
2. memuat ulang PO sumber
3. validasi outstanding quantity terbaru
4. mengubah status GR menjadi `COMPLETED`
5. mem-post stock receipt ke `StockService`
6. untuk item serialized:
   - qty dikonversi ke base UOM
   - harus menghasilkan bilangan bulat
   - serial yang kurang akan **auto-generated**
   - stock diposting **1 unit per serial**
7. memanggil `po.recordReceipt(...)` untuk update received quantity dan status PO

## 5. Posisi Accounting Saat Ini

| Area | Status saat ini | Catatan |
|---|---|---|
| Accounting Period | **Wajib** | Dicek saat `COMPLETE` melalui `EnsureOpenPeriodForDateUseCase` |
| Accounting Schema | **Belum wajib** | Tidak ada lookup/validasi schema di slice GR saat ini |
| COA validation | **Belum ada** | Masih deferred |
| Real journal posting | **Belum ada** | GR saat ini belum membuat journal entry langsung |

Artinya, untuk pertanyaan dependency:

- **Period: ya, wajib ada dan harus OPEN saat COMPLETE**
- **Accounting schema: belum wajib untuk implementasi GR sekarang**

## 6. Integrasi dengan Purchase Order

- Hanya PO dengan status yang bisa receive yang dapat menjadi source GR.
- Completion GR akan menambah `receivedQuantity` pada line PO terkait.
- Status header PO akan bergerak mengikuti hasil receiving:
  - **PARTIALLY_RECEIVED** bila masih ada sisa
  - **FULLY_RECEIVED** bila seluruh line terpenuhi
- Kurs yang dipakai untuk stock valuation mengikuti **exchange rate PO**, bukan diubah bebas di GR.

## 7. Evolusi Domain yang Sudah Disiapkan

Enum `GoodsReceiptReferenceType` saat ini sudah menyiapkan:

| Enum | Status implementasi |
|---|---|
| `PURCHASE_ORDER` | Aktif |
| `SALES_RETURN` | Belum ada resolver/use case eksekusi |
| `MANUAL` | Belum didukung saat create/save |
| `PRODUCTION` | Belum ada resolver/use case eksekusi |

Jadi arah arsitekturnya memang **generic reference-based**, tetapi coverage bisnis yang benar-benar operasional saat ini masih **Goods Receipt from Purchase Order**.
