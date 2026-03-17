# Spesifikasi Fungsional: Warehouse Hierarchy (Facility, Grid, Container)

## 1. Ringkasan Modul
Modul Warehouse Hierarchy adalah fondasi dari Warehouse Management System (WMS) yang mengelola struktur fisik tempat penyimpanan barang. Modul ini terdiri dari tiga level hierarki:
1.  **Facility**: Gedung atau gudang fisik teratas.
2.  **Grid (Zone)**: Area atau zonasi di dalam fasilitas (misal: Lantai, Lorong, Area Dingin).
3.  **Container (Bin)**: Titik terkecil tempat barang diletakkan (misal: Rak, Kotak, Palet).

## 2. Fitur Utama

### A. Facility (Level 1)
*   **Auto-generated Code**: Kode fasilitas digenerate otomatis dengan format `FAC-XXXX`.
*   **Integrated Address**: Menggunakan komponen `@Embeddable Address` yang terhubung dengan data `Geographic` (Kota) untuk standarisasi wilayah.
*   **Owner (Party)**: Setiap fasilitas harus dikaitkan dengan satu *Party* sebagai pemilik atau penanggung jawab.

### B. Grid / Zone (Level 2)
*   **Manual Unique Code**: Kode grid diinput secara manual (misal: `AISLE-A`) agar mudah dikenali staf gudang. Kode ini harus unik di dalam satu fasilitas yang sama.
*   **Facility Linking**: Setiap grid wajib merujuk ke satu fasilitas induk.
*   **Drill-down Navigation**: Tersedia tombol navigasi langsung dari daftar Facility ke daftar Grid terkait.

### C. Container / Bin (Level 3)
*   **Auto-generated Code**: Kode kontainer digenerate otomatis dengan format `BIN-XXXXX`.
*   **Barcode Support**: Setiap kontainer memiliki field `barcode` unik yang dapat digunakan untuk proses *scanning* saat mutasi barang.
*   **Physical Dimensions**: Menyimpan data dimensi fisik (Panjang, Lebar, Tinggi) dan `maxWeight` untuk membantu perhitungan kapasitas rak.
*   **Grid Linking**: Setiap kontainer wajib merujuk ke satu grid induk.

## 3. Aturan Bisnis (Business Rules)
1.  **Hierarki Ketat**: Penghapusan Facility akan berdampak pada Grid di bawahnya, dan penghapusan Grid akan berdampak pada Container di bawahnya (menggunakan FK Constraint).
2.  **Status Aktif (isActive)**: Jika sebuah Facility dinonaktifkan, maka secara logis seluruh Grid dan Container di dalamnya tidak dapat digunakan untuk transaksi stok baru.
3.  **Unique Barcode**: Barcode pada Container bersifat unik di seluruh sistem (global unique).
4.  **Security/Permissions**:
    *   `FACILITY_*`, `GRID_*`, `CONTAINER_*`: Hak akses standar CRUD (Read, Create, Update, Delete).
    *   `LOOKUP_PARTY`: Diperlukan agar form Facility dapat mencari data Owner secara asinkron.

## 4. Struktur Data & Komponen Teknis
*   **Address Embeddable**: Terletak di `com.solusi.erp.core.model.Address`.
*   **Dimensions Embeddable**: Terletak di `com.solusi.erp.inventory.model.Dimensions`.
*   **Sequence Generator**: Menggunakan module code `FACILITY` dan `CONTAINER` pada tabel `system_sequences`.

## 5. Navigasi & UI/UX
*   **Linked UI**: Pengguna didorong untuk masuk melalui hierarki (Facility -> Grid -> Bin) menggunakan tombol aksi di setiap baris tabel.
*   **Autocomplete**: Menggunakan library **TomSelect** untuk pemilihan Owner dan Kota pada form untuk mendukung dataset yang besar.
*   **Global Search**: Ketiga fitur ini sudah terdaftar dalam `PermissionGroup` sehingga muncul di kotak pencarian navbar global.
