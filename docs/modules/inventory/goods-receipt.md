# Goods Receipt (GR)

Dokumen ini menjelaskan spesifikasi fungsional dan teknis untuk fitur **Goods Receipt** pada modul Inventory.

## 1. Ikhtisar

Goods Receipt digunakan untuk mencatat penerimaan fisik barang ke gudang. Pada implementasi saat ini, flow create GR masih berasal dari **Purchase Order** yang sudah **SENT** atau **PARTIALLY_RECEIVED**. Namun model domain GR sudah digeneralisasi agar setiap dokumen penerimaan selalu menyimpan:

- `referenceType`
- `referenceId`

Pendekatan ini menjaga supaya GR bisa diperluas di masa depan untuk referensi lain seperti **Sales Return**, **manual receipt**, atau **hasil produksi internal**, tanpa mengubah konsep inti aggregate.

## 2. Model Data & Atribut Utama

### Header GR

| Field | Keterangan | Wajib |
|-------|-----------|-------|
| `code` | Nomor dokumen otomatis (format: `GR-yyMM-XXXXX`) | Ya (auto) |
| `receiptDate` | Tanggal penerimaan fisik barang | Ya |
| `referenceType` | Jenis dokumen sumber, mis. `PURCHASE_ORDER` | Ya |
| `referenceId` | ID dokumen sumber sesuai `referenceType` | Ya |
| `supplierId` | Pihak yang mengirim barang ke gudang | Ya |
| `facilityId` | Gudang/lokasi penerimaan | Ya |
| `currencyId` | Mata uang transaksi referensi | Ya |
| `exchangeRate` | Kurs ke mata uang dasar | Ya |
| `status` | `DRAFT` atau `COMPLETED` | Ya (auto) |
| `notes` | Catatan tambahan | Tidak |

### Line GR

| Field | Keterangan | Wajib |
|-------|-----------|-------|
| `poLineId` | Referensi line PO asal untuk flow PO | Kondisional |
| `productId` | Produk yang diterima | Ya |
| `quantityReceived` | Kuantitas yang benar-benar diterima | Ya (> 0) |
| `uomId` | Satuan input penerimaan | Ya |
| `containerId` | Container/lokasi simpan | Tidak |
| `serialNumber` | Nomor seri jika item serialized | Kondisional |
| `unitPrice` | Harga referensi per unit transaksi | Ya |
| `inventoryAmount` | Nilai persediaan line | Ya (auto) |
| `taxAmount` | Nilai pajak line | Ya (auto) |

## 3. Workflow & Aturan Bisnis

### A. Lifecycle

```
DRAFT ──► COMPLETED
```

| Status | Deskripsi | Aksi |
|--------|-----------|------|
| **DRAFT** | Draft penerimaan, masih bisa diedit/dihapus | Edit, Delete, Complete |
| **COMPLETED** | Penerimaan final, stok dan dokumen turunan sudah diposting | View only |

### B. Scope Implementasi Saat Ini

1. **Create flow tetap PO-only**: user membuat GR dari halaman detail PO, bukan dari tombol create global di list GR.
2. **GR list bersifat audit/listing**: halaman list dipakai untuk menelusuri histori dokumen GR yang sudah ada.
3. **Header snapshot read-only**: saat create/edit draft dari PO, form GR menampilkan ringkasan referensi seperti facility, supplier, currency, dan kode referensi sebagai informasi baca-saja.
4. **Reference generic di persistence**: database dan aggregate tidak lagi menyimpan `poId` langsung, tetapi `referenceType/referenceId`.

### C. Aturan Integrasi dengan Purchase Order

- Hanya PO dengan status **SENT** atau **PARTIALLY_RECEIVED** yang boleh menjadi sumber create GR.
- Setiap completion GR akan menambah `receivedQuantity` pada line PO terkait.
- Status PO berubah otomatis:
  - tetap **PARTIALLY_RECEIVED** bila masih ada sisa kuantitas
  - menjadi **FULLY_RECEIVED** bila seluruh line sudah terpenuhi

## 4. Integrasi Inventory & Costing

Saat GR di-complete, sistem:

1. memanggil `StockService.adjust(...)` dengan `MovementType.RECEIPT`
2. menyimpan jejak referensi stok sebagai `ReferenceType.GOODS_RECEIPT`
3. mengonversi quantity transaksi ke **base UOM**
4. membuat valuation layer FIFO berdasarkan harga unit yang sudah dinormalisasi

Rumus normalisasi cost:

```
Normalized Cost = Unit Price Transaksi / Faktor Konversi ke Base UOM
```

## 5. Catatan Evolusi Domain

Enum referensi GR saat ini menyiapkan beberapa nilai berikut:

| Enum | Tujuan |
|------|--------|
| `PURCHASE_ORDER` | Flow aktif saat ini |
| `SALES_RETURN` | Penerimaan balik dari customer |
| `MANUAL` | Penerimaan manual/non-PO |
| `PRODUCTION` | Hasil produksi internal |

Walaupun enum tersebut sudah tersedia, UI create yang benar-benar aktif sekarang masih **Purchase Order only**. Referensi selain PO adalah ruang ekspansi berikutnya, bukan flow yang sudah dibuka ke user.
