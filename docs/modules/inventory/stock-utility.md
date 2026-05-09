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

Goods Receipt adalah dokumen inbound inventory yang saat ini dipakai untuk menerima barang dari **Purchase Order** yang sudah **SENT** atau **PARTIALLY_RECEIVED**. Secara domain, GR sudah memakai konsep referensi generik (`referenceType` + `referenceId`) agar di masa depan bisa dipakai untuk sumber lain seperti return atau hasil produksi.

Rangkuman detail bisnis GR dipisahkan ke dokumen khusus: [goods-receipt.md](./goods-receipt.md).

