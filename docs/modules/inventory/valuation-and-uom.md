# Inventory Valuation (FIFO) & UOM Conversion

Dokumen ini menjelaskan mekanisme internal penghitungan Harga Pokok Penjualan (HPP) dan konversi satuan barang di sistem Solusi ERP.

## 1. Mekanisme Konversi UOM
Untuk menjaga konsistensi data, semua angka stok di database disimpan dalam **Base UOM** (satuan terkecil, misal: *Pieces*).

### Alur Konversi:
1.  **Input**: User menginput transaksi (misal: Goods Receipt) dalam satuan *Dus*.
2.  **Lookup**: Sistem mencari faktor pengali di tabel `product_uom_conversions` untuk produk tersebut.
3.  **Process**: Angka *Dus* dikalikan dengan faktor (misal: 24) untuk mendapatkan angka *Pieces*.
4.  **Storage**: Angka hasil konversi inilah yang dikirim ke `StockService`.

---

## 2. FIFO Valuation Engine
Sistem menggunakan metode **First-In, First-Out (FIFO)** untuk menghitung nilai aset dan HPP secara akurat.

### A. Valuation Layers (`inv_valuation_layers`)
Setiap kali ada barang masuk, sistem membuat sebuah "lapisan" (layer) harga.
*   **Inbound**: Membuat baris baru dengan `remaining_quantity` sama dengan jumlah masuk.
*   **Outbound**: Mengurangi `remaining_quantity` dari layer yang memiliki tanggal pendaftaran (`createdDate`) paling lama.

### B. Multi-Currency Costing
Setiap layer menyimpan biaya dalam dua mata uang menggunakan komponen `@Embeddable CurrencyAmount`:
*   **Original Amount**: Harga dalam mata uang transaksi (misal: USD).
*   **Local Amount**: Harga yang sudah dikonversi ke mata uang dasar (misal: IDR) menggunakan `exchange_rate` pada saat transaksi.

### C. Contoh Kasus FIFO:
1.  **GR-01**: Masuk 10 unit @$5. (Layer 1 dibuat)
2.  **GR-02**: Masuk 10 unit @$6. (Layer 2 dibuat)
3.  **Sales**: Jual 12 unit.
    *   Sistem mengambil 10 unit dari **Layer 1** ($50).
    *   Sistem mengambil 2 unit dari **Layer 2** ($12).
    *   **Total HPP (COGS)**: $62.
    *   **Sisa Stok**: 8 unit di Layer 2.

---

## 3. Komponen Teknis
*   **UomConversionService**: Menangani logika perkalian satuan.
*   **ValuationService**: Menangani pembuatan dan konsumsi layer FIFO.
*   **CurrencyAmount**: Objek standar untuk penyimpanan nilai finansial multi-currency.
