# Roadmap: Inventory & Warehouse Management System (WMS)

Modul ini fokus pada pengelolaan penyimpanan fisik barang, pergerakan stok, dan valuasi persediaan.

## Phase 1: Foundation (Current) [x]
*   [x] **Master Data Brand**: CRUD Dasar.
*   [x] **Master Data Product**: Struktur data lengkap dengan dukungan multi-UoM.
*   [x] **Smart Sequence Generator**: Integrasi kode otomatis (PRD-XXXX).
*   [x] **Conditional UI Logic**: Validasi form berbasis tipe produk (STOCK vs NON-STOCK).

## Phase 2: Warehouse Hierarchy & Stock Status [x]
*   [x] **Multi-Level Storage Management**: Facility, Grid, dan Container.
*   [x] **Stock Quantities & Statuses**: Implementasi On-Hand, Reserved, dan Available.
*   [x] **Stock Balance Table (`inv_stock_balances`)**: Saldo real-time per lokasi fisik.

## Phase 3: Movements & Valuation [x]
*   [x] **Inventory Movements (`inv_movements`)**: Sejarah mutasi barang sebagai audit trail lengkap.
*   [x] **Valuation Layers (FIFO)**: Pencatatan HPP per batch masuk (Foundation).
*   [x] **Stock Adjustment**: Penyesuaian stok manual dengan alur DRAFT -> COMPLETED.
*   **Core Transactions (Future)**:
    *   **Goods Receipt (GR)**: Penerimaan dari Supplier (Integrasi: Procurement).
    *   **Goods Issue (GI)**: Pengeluaran untuk Customer (Integrasi: Sales).
    *   **Internal Transfer**: Pemindahan antar lokasi fisik atau gudang.

## Phase 4: Labeling & Serial Tracking [~]
*   [x] **Core Stock Utility**: Standardized service for atomic stock updates.
*   [x] **Serial Number Foundation**: Auto-generation and tracking in `inv_stock_balances`.
*   **Lot/Batch Management**: Grouping items by production or purchase batch.
*   **Expiration Tracking**: For perishable goods with automated alerts.
*   **Barcode Printing**: Pembuatan label barcode untuk identitas produk dan lokasi bin.

## Phase 5: Inventory Analytics [x]
*   [x] **On-Hand Quantity Report**: Laporan saldo stok saat ini per gudang/bin.
*   [x] **Stock Card (Kartu Stok)**: Mutasi kuantitas dan nilai secara kronologis (Professional Report).
*   **Cycle Counting (Stock Opname)**: Periodic physical inventory verification.
*   **Inventory Valuation**: Total nilai aset persediaan yang sinkron dengan General Ledger.

---
*Last Updated: 2026-03-19*
