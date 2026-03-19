# Roadmap: Procurement (Purchase) Module

Modul ini menangani siklus **Procure-to-Pay (P2P)**, mulai dari permintaan barang hingga pembayaran ke supplier.

## Phase 1: Purchase Master Data
*   [x] **Supplier Management**: Terintegrasi dengan `BusinessPartner` (Role: SUPPLIER).
*   [ ] **Supplier Price List**: Katalog harga khusus per supplier untuk otomasi PO.

## Phase 2: Purchase Operations
*   **Purchase Requisition (PR)**: Permintaan internal dari departemen/gudang.
*   **Purchase Order (PO)**: Dokumen kontrak pembelian ke supplier.
    *   Approval Workflow: Review oleh Manager sebelum PO dikirim.
*   **Goods Receipt (GR)**: Penerimaan barang fisik di gudang.
    *   Integrasi ke `Inventory Module` (Menciptakan `Valuation Layer` & `Movement`).
    *   Otomasi pembuatan Serial Number jika produk *serialized*.

## Phase 3: Accounts Payable (AP) & Billing
*   **Purchase Invoice (Vendor Bill)**: Pencatatan tagihan dari supplier berdasarkan data GR.
*   **Landed Cost**: Penambahan biaya kirim/pajak impor ke dalam nilai HPP barang.
*   **Payment**: Pembayaran ke supplier (Cash/Bank/Cek).
*   **Purchase Return**: Pengembalian barang ke supplier.

## Phase 4: Procurement Analytics
*   **Purchase History**: Analisis tren harga beli.
*   **Supplier Performance**: Rating supplier berdasarkan kecepatan kirim dan kualitas barang.

---
*Last Updated: 2026-03-18*
