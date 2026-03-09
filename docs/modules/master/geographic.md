# Spesifikasi Fungsional: Geographic (Wilayah)

## 1. Ringkasan Modul
Modul Geographic digunakan untuk mengelola data wilayah administratif secara hierarkis. Data ini menjadi referensi utama untuk pengisian alamat di berbagai modul lain (seperti Business Partner, Warehouse, dsb) guna menjamin validitas data lokasi.

## 2. Struktur Data (Hierarki)
Modul ini menggunakan pola **Self-Referencing Relationship** (relasi ke diri sendiri) untuk membentuk struktur pohon (tree).

### Tipe Wilayah (`GeographicType`):
1.  **COUNTRY**: Tingkat tertinggi (Negara). Tidak memiliki `parent_id`. Contoh: Indonesia, Malaysia.
2.  **STATE_PROVINCE**: Tingkat kedua (Provinsi/Negara Bagian). Wajib memiliki `parent_id` yang merujuk ke tipe `COUNTRY`. Contoh: Jawa Barat, DKI Jakarta.
3.  **CITY_MUNICIPALITY**: Tingkat ketiga (Kota/Kabupaten). Wajib memiliki `parent_id` yang merujuk ke tipe `STATE_PROVINCE` atau dalam kasus tertentu langsung ke `COUNTRY`. Contoh: Bandung, Jakarta Pusat.

## 3. Aturan Bisnis (Business Rules)
1.  **Unique Code**: Kode wilayah (ISO Code atau internal) harus unik secara sistem.
2.  **Parent Validation**:
    *   Tipe `COUNTRY` tidak boleh memiliki induk.
    *   Tipe `STATE_PROVINCE` harus merujuk ke `COUNTRY`.
    *   Tipe `CITY_MUNICIPALITY` harus merujuk ke `STATE_PROVINCE`.
3.  **Soft Delete**: Mengikuti standar `BaseModel`, data master wilayah menggunakan flag `isActive`. Menghapus wilayah induk secara otomatis akan menonaktifkan anak-anaknya jika diimplementasikan di level Service.
4.  **Read Only Seed**: Data negara standar biasanya disediakan melalui migrasi database (Seed data).

## 4. Integrasi API (Dynamic Lookup)
Untuk mendukung UI/UX yang dinamis (seperti pada form Business Partner), `GeographicController` menyediakan endpoint khusus:

| Method | Endpoint | Kegunaan |
| :--- | :--- | :--- |
| `GET` | `/master/geographics/api/countries` | Mengambil semua daftar negara aktif. |
| `GET` | `/master/geographics/api/provinces?countryId={id}` | Mengambil provinsi berdasarkan negara. |
| `GET` | `/master/geographics/api/cities?parentId={id}` | Mengambil kota berdasarkan provinsi. |
| `GET` | `/master/geographics/api/hierarchy/{id}` | Mengambil silsilah lengkap (Negara > Prov > Kota) berdasarkan ID Kota. |

## 5. Panduan Penggunaan di UI
### Pola Dropdown Berantai
Saat mengimplementasikan form alamat, gunakan urutan berikut:
1.  **Event `onchange` Negara**: Reset dropdown Provinsi & Kota, lalu panggil `/api/provinces?countryId=...`.
2.  **Event `onchange` Provinsi**: Reset dropdown Kota, lalu panggil `/api/cities?parentId=...`.
3.  **Reverse Lookup**: Jika sistem menyimpan hanya `city_id`, saat mode **Edit**, UI harus memanggil `/api/hierarchy/{cityId}` untuk menentukan nilai default Negara dan Provinsi agar dropdown terpilih secara otomatis.

## 6. Contoh Data (Database Seed)
```sql
-- Negara
INSERT INTO geographics (code, name, type, parent_id) VALUES ('ID', 'Indonesia', 'COUNTRY', NULL);

-- Provinsi (Induk: Indonesia)
INSERT INTO geographics (code, name, type, parent_id) VALUES ('ID-JB', 'Jawa Barat', 'STATE_PROVINCE', 1);

-- Kota (Induk: Jawa Barat)
INSERT INTO geographics (code, name, type, parent_id) VALUES ('ID-BDO', 'Bandung', 'CITY_MUNICIPALITY', 2);
```
