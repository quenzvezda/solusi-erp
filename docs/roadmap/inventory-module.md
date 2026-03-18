# Roadmap: Inventory & Warehouse Management System (WMS)

Modul ini fokus pada pengelolaan penyimpanan fisik barang, pergerakan stok, dan valuasi persediaan.

## Phase 1: Foundation (Current)
*   [x] **Master Data Brand**: CRUD Dasar.
*   [x] **Master Data Product**: Struktur data lengkap dengan dukungan multi-UoM.
*   [x] **Smart Sequence Generator**: Integrasi kode otomatis (PRD-XXXX).
*   [x] **Conditional UI Logic**: Validasi form berbasis tipe produk (STOCK vs NON-STOCK).

## Phase 2: Warehouse Hierarchy & Stock Status
*   [x] **Multi-Level Storage Management**: Facility, Grid, dan Container.
*   **Stock Quantities & Statuses**:
    *   **On-Hand (Physical)**: Stok nyata di dalam kontainer.
    *   **Reserved (Allocated)**: Stok yang sudah di-booking oleh Sales Order (SO).
    *   **Available (To-Sell)**: `On-Hand` - `Reserved`. Angka utama untuk jualan.
    *   **In-Transit**: Barang dalam perjalanan antar gudang atau ke pelanggan.
*   **Stock Balance Table (`inv_stock_balances`)**:
    *   Tabel saldo real-time per lokasi fisik dengan pemisahan kolom status stok.

## Phase 3: Movements & Valuation
*   **Inventory Movements (`inv_movements`)**:
    *   Sejarah mutasi barang sebagai audit trail lengkap.
*   **Valuation Layers (FIFO)**:
    *   Pencatatan HPP per batch masuk untuk akurasi laporan laba kotor.
*   **Core Transactions**:
    *   **Goods Receipt (GR)**: Penerimaan dari Supplier (Integrasi: Procurement).
    *   **Goods Issue (GI)**: Pengeluaran untuk Customer (Integrasi: Sales).
    *   **Internal Transfer**: Pemindahan antar lokasi fisik atau gudang.
    *   **Stock Adjustment**: Penyesuaian stok manual (hasil stock opname).

## Phase 4: Labeling & Serial Tracking
*   [x] **Core Stock Utility**: Standardized service for atomic stock updates.
*   [x] **Serial Number Foundation**: Auto-generation and tracking in `inv_stock_balances`.
*   **Lot/Batch Management**: Grouping items by production or purchase batch.
*   **Expiration Tracking**: For perishable goods with automated alerts.
*   **Barcode Printing**: Pembuatan label barcode untuk identitas produk dan lokasi bin.

## Phase 5: Inventory Analytics
*   **Cycle Counting (Stock Opname)**: Periodic physical inventory verification.
*   **On-Hand Quantity Report**: Laporan saldo stok saat ini per gudang/bin.
*   **Stock Card (Kartu Stok)**: Mutasi kuantitas dan nilai secara kronologis.
*   **Inventory Valuation**: Total nilai aset persediaan yang sinkron dengan General Ledger.

---
*Last Updated: 2026-03-18*
