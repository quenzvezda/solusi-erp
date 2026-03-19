# Stock Adjustment

Dokumen ini menjelaskan spesifikasi fungsional dan teknis untuk fitur **Stock Adjustment** (Penyesuaian Stok) di dalam modul Inventori.

## 1. Ikhtisar (Overview)
Stock Adjustment digunakan untuk melakukan koreksi jumlah stok fisik secara manual di dalam gudang. Fitur ini biasanya digunakan setelah kegiatan *Stock Opname* atau untuk memperbaiki selisih stok yang ditemukan secara tidak sengaja.

## 2. Model Data & Atribut Utama
- **Header**:
    - `code`: Nomor dokumen otomatis (Format: `ADJ-yyMM-XXXXX`).
    - `transactionDate`: Tanggal efektif penyesuaian stok.
    - `facility`: Gudang utama tempat penyesuaian dilakukan.
    - `totalCost`: Total nilai penyesuaian dalam mata uang asli dan lokal (IDR).
    - `status`: **DRAFT** atau **COMPLETED**.
- **Lines**:
    - `product`: Barang yang disesuaikan.
    - `grid`: Area spesifik di gudang.
    - `container`: Bin/rak spesifik.
    - `quantity`: Jumlah barang (positif menambah stok, negatif mengurangi).
    - `unitCost`: HPP per unit saat penyesuaian dilakukan.

## 3. Workflow & Aturan Bisnis (Business Rules)

### A. Status Lifecycle
1.  **DRAFT**: 
    - Penyesuaian baru tersimpan di database.
    - **Belum ada efek** terhadap jumlah stok fisik maupun nilai aset.
    - Data masih dapat diubah (Edit) atau dihapus.
2.  **COMPLETED**:
    - Dipicu dengan menekan tombol **"Process to Inventory"**.
    - Status berubah menjadi permanen dan **tidak dapat diubah lagi**.
    - Memicu mutasi stok fisik di tabel `inv_stock_balances`.
    - Memicu pencatatan sejarah di `inv_movements`.
    - Memicu penambahan/pengurangan di `inv_valuation_layers` (FIFO).

### B. Aturan Valuasi & UoM
- Semua kuantitas yang diinput akan otomatis dikonversi ke **Base UoM** produk sebelum disimpan ke saldo stok.
- Penyesuaian stok positif akan membuat **Valuation Layer** baru dengan harga `unitCost` yang diinput.
- Penyesuaian stok negatif akan mengonsumsi stok menggunakan logika **FIFO** (First-In, First-Out).

## 4. Standar UI/UX
- **Autocomplete**: Menggunakan standar `Autocomplete Generic` dengan cascading Facility -> Grid -> Container.
- **Dynamic Recap**: Menampilkan ringkasan total nilai dokumen secara real-time di sisi kanan atas form.
- **Fixed Table Layout**: Tabel item menggunakan layout tetap untuk mencegah horizontal scrollbar pada input data yang padat.
- **Auto-Date**: Tanggal transaksi otomatis terisi dengan tanggal hari ini saat pembuatan dokumen baru.

## 5. Keamanan (Security)
Fitur ini dilindungi oleh otoritas berikut:
- `STOCK-ADJUSTMENT_READ`: Melihat daftar dan detail adjustment.
- `STOCK-ADJUSTMENT_CREATE`: Menambah adjustment baru.
- `STOCK-ADJUSTMENT_UPDATE`: Mengubah data adjustment berstatus DRAFT.
- `STOCK-ADJUSTMENT_DELETE`: Menghapus adjustment berstatus DRAFT.
- `STOCK-ADJUSTMENT_PROCESS`: Memproses adjustment dari DRAFT ke COMPLETED.
- `LOOKUP_INVENTORY`: Melakukan pencarian autocomplete produk dan lokasi.
