# Inventory Stock Utility

Dokumen ini menjelaskan penggunaan `StockService` sebagai pintu gerbang tunggal untuk semua mutasi stok di sistem Solusi ERP.

## 1. Konsep Dasar
`StockService` menjamin bahwa setiap perubahan saldo stok (`StockBalance`) selalu disertai dengan log audit (`InventoryMovement`) secara atomik.

### Status Stok yang Didukung
*   **On-Hand**: Kuantitas fisik nyata yang ada di lokasi (Container).
*   **Reserved**: Kuantitas yang sudah dipesan (Booking) tapi belum keluar gudang.
*   **Available**: Stok yang siap dijual (`On-Hand` - `Reserved`).
*   **In-Transit**: Stok dalam perjalanan (Inbound ke lokasi tujuan).

## 2. Penggunaan `StockService`

Semua modul (Sales, Procurement, dll.) **WAJIB** menggunakan `StockService.adjust(StockMovementPayload)` untuk merubah stok.

### Contoh Payload
```java
StockMovementPayload payload = StockMovementPayload.builder()
    .productId(1L)
    .containerId(10L)
    .quantity(new BigDecimal("5"))
    .uomId(2L) // Contoh: Dus (Akan dikonversi otomatis ke Pieces)
    .movementType(MovementType.RECEIPT)
    .referenceType(ReferenceType.GOODS_RECEIPT)
    .referenceId(100L)
    .referenceCode("GR-2026-0001")
    .currencyId(1L) // USD
    .exchangeRate(new BigDecimal("15500"))
    .netPrice(new BigDecimal("10.50")) // Harga per unit dalam USD
    .build();

stockService.adjust(payload);
```

### Aturan Penting
1.  **Automated UoM Conversion**: Jika `uomId` dikirimkan, `StockService` akan otomatis mengonversi `quantity` ke **Base UOM** menggunakan tabel konversi produk sebelum disimpan.
2.  **No Negative Stock**: Sistem akan melempar `RuntimeException` jika operasi mengakibatkan stok fisik atau reservasi menjadi negatif.
3.  **FIFO Costing**: Setiap transaksi `RECEIPT` atau `ADJUSTMENT` positif akan membuat **Valuation Layer** baru. Transaksi `ISSUE` akan mengonsumsi layer tertua.
4.  **Serial Number**: Jika produk bersifat *Serialized* dan `serialNumber` tidak diisi pada saat penambahan stok, sistem akan meng-generate otomatis (Format: `SN-YYMM-XXXXX`).


## 3. Dynamic Reporting Routing
Untuk laporan histori mutasi barang, kita menghindari JOIN berat ke tabel transaksi asal (seperti `sales_orders`). Sebagai gantinya, kita menggunakan pola **Reference Mapping**.

Di sisi UI/Controller, gunakan Map routing untuk membuat hyperlink dinamis:

| Reference Type | URL Pattern |
| :--- | :--- |
| `GOODS_RECEIPT` | `/inventory/goods-receipts/%d/edit` |
| `SALES_ORDER` | `/sales/orders/%d/edit` |
| `STOCK_ADJUSTMENT` | `/inventory/adjustments/%d/edit` |

Pola ini memungkinkan navigasi cepat dari laporan ke dokumen sumber tanpa beban query yang besar.

## 4. Goods Receipt (GR)

Goods Receipt adalah dokumen untuk mencatat penerimaan fisik barang dari supplier berdasarkan Purchase Order yang telah dikirim (status SENT/PARTIALLY_RECEIVED).

### 4.1 Konsep Umum

**Goods Receipt** (GR) mewakili event penerimaan barang masuk ke gudang dari supplier untuk memenuhi PO. Setiap GR terdiri dari:
- **Header GR**: Informasi dokumen (nomor, tanggal, referensi PO, facility)
- **Line Item GR**: Daftar produk beserta kuantitas yang diterima

### 4.2 Domain Entities

| Entity | Keterangan |
|--------|-----------|
| `GoodsReceipt` | Dokumen GR header (status: DRAFT / COMPLETED) |
| `GoodsReceiptLine` | Baris penerimaan barang per item |

### 4.3 Integration with StockService

Saat GR di-complete, sistem secara otomatis:
1. Menginvokeasi `StockService.adjust()` dengan `MovementType.RECEIPT` dan `ReferenceType.GOODS_RECEIPT`
2. Mengkonversi kuantitas ke **base UOM** untuk unit penyimpanan stok
3. Membuat **Valuation Layer** dengan harga cost dari line item PO (normalized ke base UOM)
4. Update status PO berdasarkan total received quantity

### 4.4 Normalized Cost Calculation

Harga cost per unit diambil dari **PO line item** dan dinormalisasi ke **base UOM**:

```
Normalized Cost = (Line Unit Price) / (PO Line UOM → Base UOM Conversion Factor)
```

Contoh:
- PO line: 10 BOX @ Rp 50.000/BOX, Base UOM = PCS, 1 BOX = 12 PCS
- Normalized Cost = Rp 50.000 / 12 = Rp 4.166,67 per PCS

Harga ini digunakan untuk FIFO costing layer dan valuasi stok.

### 4.5 Serialized Item Handling

Jika produk bersifat **Serialized**:
- GR form meminta input nomor seri per item yang diterima
- Serial number dapat di-import dari file atau di-generate sistem (format: `SN-YYMM-XXXXX`)
- Setiap serial number dipetakan ke Valuation Layer individual untuk tracking traceability


