# Roadmap: Inventory & Stock Tracking Module

Dokumen ini merincikan rencana pengembangan fitur Inventory setelah Master Data (Product, Brand, Category, UoM) selesai diimplementasikan.

## Phase 1: Foundation (Current)
*   [x] **Master Data Brand**: CRUD Dasar.
*   [x] **Master Data Product**: Struktur data lengkap dengan dukungan multi-UoM (Base, Weight, Dimension).
*   [x] **Smart Sequence Generator**: Integrasi kode otomatis (PRD-XXXX).
*   [x] **Conditional UI Logic**: Validasi form berbasis tipe produk (STOCK vs NON-STOCK).

## Phase 2: Warehouse & Storage Hierarchy
*   [x] **Multi-Level Storage Management**:
    *   [x] **Facility**: Top-level building/warehouse management.
    *   [x] **Grid (Zone)**: Blocking/Area management within a facility (e.g., Aisle A, Cold Zone).
    *   [x] **Container (Bin)**: The smallest addressable unit where products are physically placed.
*   **Stock Balance Table**:
    *   Tabel `stock_balances` untuk menyimpan saldo stok real-time.
    *   Tracking kini lebih detail: `product_id`, `facility_id`, `grid_id`, dan `bin_id`.
    *   Constraint: `UNIQUE(product_id, bin_id)`.

## Phase 3: Inventory Transactions (Core WMS)
*   **Goods Receipt (GR)**: Penerimaan barang dari Supplier/Produksi.
    *   Logika **Serialization**: Jika `product.isSerialized = TRUE`, user wajib input Serial Number unik saat GR.
*   **Goods Issue (GI)**: Pengeluaran barang untuk Sales/Internal.
*   **Internal Transfer**: Pemindahan barang antar Warehouse.
*   **Stock Adjustment**: Penyesuaian stok jika ada selisih audit (Stock Opname).

## Phase 4: Advanced Tracking & Integration
*   **Serialized Item Tracking**:
    *   Tabel `inventory_items` untuk menyimpan status fisik setiap Serial Number (In Stock, Sold, Damaged).
*   **Hybrid Serialization**:
    *   **Manual Scan**: Untuk SN manufaktur asli (Laptop, HP).
    *   **Auto-Generate**: Untuk SN internal perusahaan.
*   **Barcode Printing**: Fitur untuk men-generate label barcode (EAN/Internal) dalam format PDF/ZPL.

## Phase 5: Inventory Analytics
*   **Stock Card (Buku Stok)**: Histori mutasi barang (Masuk, Keluar, Saldo) secara kronologis.
*   **Low Stock Alerts**: Notifikasi otomatis jika stok di bawah `min_stock`.
*   **Valuation**: Perhitungan nilai aset inventori (FIFO / Average Costing).

---
*Last Updated: 2026-03-05*
