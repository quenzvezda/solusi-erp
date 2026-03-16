# Modul Permission Group

Modul ini bertanggung jawab untuk mengelola pengelompokan otoritas (_Permission_) ke dalam entitas logis yang disebut **Permission Group**. 

---

## 1. Fungsi Utama

### A. Pengelompokan Otoritas (Security Logical Grouping)
Setiap _Permission_ (seperti `USERS_READ`, `USERS_CREATE`) kini dikaitkan dengan satu `PermissionGroup`. Hal ini mempermudah sistem dalam mengidentifikasi "Modul" atau "Fitur" mana yang sedang diakses atau diatur.

### B. Definisi Menu Navigasi (Search Menu Identity)
`PermissionGroup` berfungsi sebagai identitas menu yang muncul pada fitur **Global Search Menu**. Data yang dikelola meliputi:
- **Nama (Localized)**: Label menu yang muncul di hasil pencarian.
- **Breadcrumb (Localized)**: Memberikan konteks lokasi menu tersebut di dalam aplikasi.
- **URL Path**: Alamat tujuan navigasi saat menu dipilih.

---

## 2. Struktur Data

### Entitas `PermissionGroup`

| Field | Tipe Data | Deskripsi |
|-------|-----------|-----------|
| `code` | `String` | Kode unik grup (contoh: `SEC-01`, `INV-01`). |
| `name_id/en` | `String` | Nama grup dalam Bahasa Indonesia dan Inggris. |
| `breadcrumb_id/en` | `String` | Struktur breadcrumb (contoh: `Master > Pajak`). |
| `url_path` | `String` | Path relative modul tersebut (contoh: `/master/tax`). |
| `permissions` | `List<Permission>` | Daftar otoritas granular yang masuk ke grup ini. |

---

## 3. Integrasi UI

### A. Halaman Role Management
Daftar permission di halaman form **Role** kini dikelompokkan berdasarkan `PermissionGroup`. Admin dapat dengan mudah melihat izin apa saja yang diberikan untuk satu modul tertentu melalui _Card_ yang terpisah.

### B. Global Search Menu
Sistem secara otomatis akan mengizinkan menu (Permission Group) muncul di pencarian jika user memiliki minimal satu permission yang terhubung ke grup tersebut.

---

## 4. Standar Penggunaan (Standard Operating Procedure)

1. **Pembuatan Modul Baru**: Saat membuat modul CRUD baru, developer **WAJIB** membuat satu `PermissionGroup` di database (biasanya melalui Flyway migration).
2. **Penghubungan Permission**: Semua `Permission` yang dibuat untuk modul tersebut harus diset `permission_group_id`-nya ke grup yang baru dibuat.
3. **Penamaan Localized**: Pastikan `name` dan `breadcrumb` diisi dalam kedua bahasa (`id` dan `en`) untuk mendukung fitur i18n aplikasi.
