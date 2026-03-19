# Roadmap: Sales & Distribution Module

Modul ini menangani siklus **Order-to-Cash (O2C)**, dari penawaran harga hingga penerimaan pembayaran dari pelanggan.

## Phase 1: Sales Master Data
*   [x] **Customer Management**: Terintegrasi dengan `BusinessPartner` (Role: CUSTOMER).
*   [ ] **Customer Price List / Discount Policy**: Aturan harga per level customer (Gold, Silver, dsb).

## Phase 2: Sales Operations
*   **Sales Quotation**: Penawaran harga ke pelanggan.
*   **Sales Order (SO)**: Konfirmasi pemesanan dari pelanggan.
    *   **Inventory Reservation**: Saat SO di-confirm, sistem mencatat `Reserved Quantity` di inventory agar barang tidak dijual ke orang lain.
*   **Delivery Planning**: Perencanaan pengiriman berdasarkan ketersediaan stok.

## Phase 3: Shipping & Realization
*   **Delivery Order (DO)**: Dokumen instruksi pengeluaran barang (Packing List).
*   **Goods Issue (GI)**: Pengeluaran barang fisik dari gudang.
    *   **Stock Update**: Mengurangi `On-Hand` dan `Reserved`, mencatat `inv_movements`.
*   **Delivery Order Realization (POD)**: Konfirmasi barang telah sampai ke pelanggan (Proof of Delivery).
    *   Jika sistem menggunakan status **In-Transit**, status ini baru dilepaskan setelah realisasi.

## Phase 4: Account Receivable (AR) & Billing
*   **Sales Billing / Invoice**: Penagihan ke pelanggan berdasarkan barang yang sudah terkirim (DO Realized).
*   **Receipt / Payment Entry**: Pencatatan uang masuk dari pelanggan (Cash/Bank).
*   **Sales Return**: Pengembalian barang dari pelanggan.

## Phase 5: Sales Analytics
*   **Sales Performance**: Analisis penjualan per produk/brand/customer.
*   **Profitability Analysis**: Laporan untung rugi per transaksi berdasarkan HPP FIFO.

---
*Last Updated: 2026-03-18*
