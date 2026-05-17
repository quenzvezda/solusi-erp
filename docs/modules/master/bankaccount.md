# Spesifikasi Fungsional: Bank Account (Akun Bank/Kas)

## 1. Ringkasan Modul
Modul Bank Account digunakan untuk mengelola data rekening bank atau akun kas yang dimiliki oleh entitas bisnis (Party). Modul ini menyimpan informasi detail perbankan yang nantinya akan digunakan dalam berbagai transaksi keuangan seperti pembayaran, penerimaan, dan rekonsiliasi kas/bank.

## 2. Fitur Utama
*   **Holder Association**: Setiap akun bank wajib dikaitkan dengan satu `Party` sebagai pemilik/pemegang akun (Holder). Ini memungkinkan pelacakan rekening milik perusahaan sendiri maupun rekening milik vendor/pelanggan.
*   **Geographic Integration**: Menyimpan informasi lokasi (Kota) di mana cabang bank tersebut berada, terintegrasi secara langsung dengan modul `Geographic`.
*   **Account Types**: Mendukung klasifikasi akun menjadi dua tipe utama melalui Enum `AccountType`:
    *   `CASH`: Digunakan untuk akun kas fisik (misal: Kas Kecil, Kas Pusat).
    *   `BANK`: Digunakan untuk akun rekening bank resmi.
*   **Code Generation**: Kode akun di-generate otomatis oleh sistem menggunakan `SequenceGeneratorService` dengan format `BA-{seq}` (contoh: `BA-0001`).
*   **Soft Delete**: Mendukung penghapusan halus menggunakan flag `isActive = false` untuk menjaga integritas data pada transaksi historis.

## 3. Struktur Data (Database Schema)
Tabel Utama: `bank_accounts`

| Kolom | Tipe | Deskripsi |
| :--- | :--- | :--- |
| `id` | `BIGINT` | Primary Key (Auto Increment). |
| `code` | `VARCHAR` | Kode unik otomatis (BA-XXXX). |
| `bank_name` | `VARCHAR` | Nama Bank atau nama identitas Kas (misal: BCA, Kas Kantor). |
| `branch` | `VARCHAR` | Nama kantor cabang bank. |
| `city_id` | `BIGINT` | FK ke `geographics` (Kota/Kabupaten). |
| `party_id` | `BIGINT` | FK ke `parties` (Pemegang/Pemilik akun). |
| `account_name`| `VARCHAR` | Nama pemilik rekening sesuai yang terdaftar di bank. |
| `account_no` | `VARCHAR` | Nomor rekening bank atau nomor identifikasi akun kas. |
| `account_type`| `VARCHAR` | Tipe akun: `CASH` atau `BANK`. |
| `currency_id` | `BIGINT` | FK ke currency akun bank/kas. |
| `coa_id` | `BIGINT` | FK ke Chart of Account kas/bank yang dipakai jurnal. |
| `note` | `TEXT` | Catatan tambahan atau informasi pendukung (Opsional). |
| `is_active` | `BOOLEAN` | Status aktif akun (1 = Aktif, 0 = Nonaktif). |

## 4. Aturan Bisnis (Business Rules)
1.  **Mandatory Fields**: Semua field (Bank Name, Branch, City, Holder, Account Name, Account No, Type, Currency, COA) wajib diisi, kecuali `note` dan `code` (yang diisi otomatis oleh sistem).
2.  **Unique Code**: Kode akun bersifat unik dan tidak boleh ada duplikasi di seluruh sistem.
3.  **Relasi Entitas**:
    *   **City**: Harus merujuk pada entitas `Geographic` yang sudah ada dan aktif.
    *   **Holder**: Harus merujuk pada entitas `Party` yang sudah ada dan aktif.
    *   **Currency**: Harus merujuk pada currency yang tersedia melalui lookup currency.
    *   **COA**: Harus merujuk pada Chart of Account yang dipilih melalui selector COA. COA ini menjadi akun kas/bank untuk journal line transaksi pembayaran, termasuk bank credit line Vendor Payment.
4.  **Soft Delete**: DILARANG menggunakan *Hard Delete*. Record yang dihapus hanya akan diubah statusnya menjadi `isActive = false`.
5.  **Read-Only Code**: Field `code` pada form UI wajib diset sebagai `readonly` dengan gaya visual `bg-light`.

## 5. Panduan Implementasi UI
### Autocomplete & Lookup
Karena modul ini bergantung pada data `Geographic` (Kota) dan `Party` (Holder) yang bisa berjumlah ribuan, implementasi form menggunakan library **TomSelect** untuk pencarian asinkron:
*   **City Lookup**: Memanggil endpoint `/api/lookup/geographics/cities`.
*   **Party Lookup**: Memanggil endpoint `/api/lookup/parties` (melalui `PartyLookupController`).
*   **Currency Lookup**: Field Currency menggunakan autocomplete standar dengan path `master/currencies`.

### COA Selector
Field COA menggunakan modal selector scoped Bank Account:
*   Tombol icon search membuka selector COA.
*   Selector mengambil data dari endpoint `/master/bank-accounts/selectors/coa`.
*   Pilihan COA mengisi hidden `coaId` dan display readonly `code - name`.
*   Controller menggunakan use case/lookup provider, bukan repository langsung, agar tetap sesuai boundary web layer.

### Internationalization (i18n)
Semua label, judul halaman, dan pesan validasi dikelola melalui `messages.properties` dan `messages_id.properties` di bawah namespace `master.bank-account.*`.

## 6. Keamanan (Security & Permissions)
Otorisasi dikelola menggunakan standar *Fine-Grained Authority* proyek:
*   `BANK-ACCOUNT_READ`: Memberikan izin untuk melihat daftar dan detail akun bank.
*   `BANK-ACCOUNT_CREATE`: Memberikan izin untuk mengakses form tambah dan menyimpan data baru.
*   `BANK-ACCOUNT_UPDATE`: Memberikan izin untuk mengakses form edit dan memperbarui data.
*   `BANK-ACCOUNT_DELETE`: Memberikan izin untuk melakukan aksi hapus (soft-delete).
