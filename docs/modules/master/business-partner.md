# Spesifikasi Fungsional: Business Partner (Party)

## 1. Ringkasan Modul
Modul Business Partner adalah pusat data untuk semua pihak yang berinteraksi dengan sistem ERP (Pelanggan, Vendor, Karyawan, dsb). Menggunakan pola **Party Model**, sistem ini menjamin konsistensi data dan mencegah redundansi.

## 2. Fitur Utama
*   **Multi-Role**: Satu entitas dapat berperan sebagai Supplier (pembelian) sekaligus Customer (penjualan).
*   **Dynamic Identifications**: Mendukung input dinamis untuk dokumen legal seperti KTP, NPWP, atau NIB.
*   **Flexible Addresses**: Mendukung banyak alamat per partner (Alamat Kantor, Gudang, Tagihan). Satu alamat dapat memiliki banyak fungsi sekaligus (misal: satu lokasi berfungsi sebagai Billing sekaligus Shipping). Alamat kini terintegrasi dengan modul **Geographic** (Negara, Provinsi, Kota).
*   **Contact Mechanism**: Mendukung banyak kontak PIC per partner (Mobile, Phone, Email) menggunakan tabel terpisah.
*   **Soft Delete**: Mendukung penghapusan halus (`isActive = false`) untuk mencegah hilangnya riwayat master data.
*   **Code Generation**: Kode partner di-generate otomatis dengan format `BP-XXXXX`.

## 3. Aturan Bisnis (Business Rules)
1.  **Nama Partner**: Wajib diisi.
2.  **Tipe Partner**: Wajib memilih antara `PERSON` (Individu) atau `ORGANIZATION` (Perusahaan).
3.  **Hapus Data**: DILARANG menggunakan *Hard Delete*. Sistem menggunakan *Soft Delete* (flag `isActive = false`) pada tabel anak (Address, ID, Contact) untuk menjaga integritas data historis. `orphanRemoval` di JPA diset `false`.
4.  **Aturan Default**: Hanya boleh ada maksimal 1 entri yang ditandai sebagai default (`isDefault = true`) untuk masing-masing koleksi alamat, identitas, dan kontak.
5.  **Security**:
    *   `PARTY_READ`: Akses daftar dan detail partner.
    *   `PARTY_CREATE`: Akses menambah partner baru.
    *   `PARTY_UPDATE`: Akses mengubah data partner.
    *   `PARTY_DELETE`: Akses menghapus partner.

## 4. Panduan Implementasi UI
### Dynamic Row Handling
Untuk form child (Identitas, Alamat, Kontak), UI menggunakan JavaScript murni (Vanilla JS) untuk:
1.  Menyisipkan baris baru berbasis template HTML tersembunyi.
2.  **Soft-Remove**: Saat tombol hapus diklik pada existing baris, UI menge-dim baris (meredupkannya) dan men-set flag `isActive = false` (serta menghilangkan `isDefault`), BUKAN menghapusnya dari DOM. Hal ini penting agar backend tetap menerima baris tersebut dan mengubah kolom `is_active` di DB menjadi 0.
3.  **Hidden ID**: Setiap baris child di form `.html` (untuk edit) wajib memiliki `<input type="hidden" th:field="*{list[__${stat.index}__].id}">` agar Hibernate tahu record mana yang sedang diupdate.
4.  **Dynamic Geographic Selection**: 
    *   Pemilihan alamat menggunakan 3 tingkat dropdown: Negara -> Provinsi -> Kota.
    *   Jika pengguna memilih Kota secara langsung, sistem otomatis melakukan *reverse-lookup* untuk mengisi Provinsi dan Negara.
    *   Jika pengguna memilih Negara/Provinsi terlebih dahulu, dropdown di bawahnya akan ter-filter secara otomatis.
    *   Data diambil secara asinkron (AJAX/Fetch) melalui API di `GeographicController`.

## 5. Metadata Lookup
Daftar peran dan tipe identitas dikelola melalui tabel referensi agar mudah ditambah di masa depan melalui database tanpa mengubah kode program.
*   **Seed Peran Dasar**: INTERNAL, CUSTOMER, SUPPLIER, EMPLOYEE, COURIER.
*   **Seed Identitas Dasar**: KTP, NPWP, NIB, PASSPORT.
