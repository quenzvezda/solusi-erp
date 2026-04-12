# Supplier Price List (SPL)

Dokumen ini menjelaskan spesifikasi fungsional dan teknis untuk fitur **Supplier Price List** (Daftar Harga Supplier) di dalam modul Pengadaan (Procurement).

## 1. Ikhtisar (Overview)

Supplier Price List adalah catatan harga resmi yang disepakati antara perusahaan dan supplier untuk produk tertentu. Fitur ini berfungsi sebagai **referensi harga** saat membuat Purchase Order (PO), sehingga bagian pengadaan tidak perlu mencari atau mengetik ulang harga setiap kali memesan.

**Kapan SPL digunakan?**
- Saat supplier memberikan katalog harga baru atau memperbarui harga existing.
- Sebagai acuan harga default saat mengisi baris item pada form PO.
- Untuk membandingkan harga antar supplier untuk produk yang sama.

## 2. Model Data & Atribut Utama

### Header SPL

| Field | Keterangan | Wajib |
|-------|-----------|-------|
| `code` | Nomor dokumen otomatis (format: `SPL-XXXXX`) | Ya (auto) |
| `supplierId` | Supplier pemilik daftar harga | Ya |
| `productId` | Produk yang dihargai | Ya |
| `uomId` | Satuan harga produk (otomatis terisi dari master produk) | Ya |
| `currencyId` | Mata uang harga (misal: IDR, USD) | Ya |
| `unitPrice` | Harga per satuan | Ya |
| `minQuantity` | Kuantitas minimum pembelian untuk harga ini berlaku | Tidak |
| `effectiveFrom` | Tanggal mulai berlaku | Ya |
| `effectiveTo` | Tanggal berakhir (kosong = berlaku selamanya) | Tidak |
| `note` | Catatan tambahan | Tidak |
| `active` | Status aktif/nonaktif | Ya (default: aktif) |

> **Catatan:** SPL **tidak memiliki** status alur kerja (workflow). Pengelolaan dilakukan melalui flag `active` — harga dapat dinonaktifkan tanpa dihapus dari sistem.

## 3. Aturan Bisnis (Business Rules)

### A. Validasi Data
1. **Harga Positif**: `unitPrice` wajib > 0. Sistem menolak jika harga nol atau negatif.
2. **Rentang Tanggal**: `effectiveTo` tidak boleh lebih awal dari `effectiveFrom`. Jika `effectiveTo` dikosongkan, harga dianggap berlaku tanpa batas.
3. **Supplier Terkunci**: Field `supplierId` tidak dapat diubah setelah SPL dibuat. Untuk supplier yang berbeda, buat SPL baru.
4. **Validasi Date Picker (UI)**: Saat `effectiveFrom` diisi, date picker `effectiveTo` otomatis dibatasi agar tidak bisa memilih tanggal yang lebih kecil dari `effectiveFrom`.

### B. Pengelolaan Aktif/Nonaktif
- SPL **tidak dapat dihapus** jika sudah pernah digunakan sebagai referensi harga.
- Untuk menonaktifkan harga yang sudah kadaluarsa, gunakan toggle **Aktif** di form edit.
- Soft delete hanya dilakukan melalui `deactivate()` — data tetap tersimpan di database untuk keperluan audit.

### C. Harga Berganda untuk Produk yang Sama
- Satu supplier boleh memiliki lebih dari satu SPL untuk produk yang sama, dengan rentang tanggal yang berbeda (misal: harga lama Q1 vs harga baru Q2).
- Saat lookup dari PO, sistem mengambil SPL yang **aktif** dan **effectiveFrom ≤ tanggal hari ini ≤ effectiveTo** (atau effectiveTo kosong).

## 4. Standar UI/UX

- **Autocomplete**: Field supplier dan produk menggunakan komponen autocomplete generik — cukup ketik nama untuk mencari.
- **UoM Auto-Fill Read-Only**: Saat produk dipilih, field UoM otomatis mengikuti UoM default produk dan dikunci (read-only) agar tidak diubah manual.
- **Date Picker**: Field `effectiveFrom` dan `effectiveTo` menggunakan date picker standar (format `dd/MM/yyyy`).
- **Date Range Guard**: `effectiveTo` otomatis mengikuti batas minimum `effectiveFrom` pada Flatpickr.
- **Active Toggle**: Checkbox atau toggle untuk mengaktifkan/menonaktifkan SPL saat membuat atau mengedit.
- **Daftar (List)**: Menampilkan kolom kode, supplier, produk, harga, satuan, berlaku dari/sampai, dan status aktif. Nilai `Unit Price` ditampilkan dengan simbol mata uang di depan (contoh: `Rp 1,000,000.00`) agar tidak ambigu. Pencarian (`keyword`) mendukung kode SPL, nama supplier, dan nama produk.

## 5. Integrasi & Relasi Antar Modul

```
SPL ──────────────────────────────────────────────────────►
                                                           PO Form
                                                           (unitPrice autocomplete)
SPL ─► (estimasi harga tersedia saat membuat PR line)
```

| Dari | Ke | Keterangan |
|------|----|-----------|
| SPL | Purchase Order (PO) | Saat menambah baris item di form PO, sistem dapat meng-autofill `unitPrice` berdasarkan SPL yang aktif untuk kombinasi supplier + produk |
| SPL | Purchase Requisition (PR) | SPL digunakan sebagai referensi saat mengisi `estimatedUnitPrice` di baris PR |

> SPL bersifat **referensi opsional** — pengguna tetap dapat mengetik harga secara manual jika SPL belum ada atau tidak ditemukan.

## 6. Keamanan (Security)

Fitur ini dilindungi oleh otoritas berikut:

| Otoritas | Akses yang Diberikan |
|----------|---------------------|
| `SPL_READ` | Melihat daftar dan detail SPL |
| `SPL_CREATE` | Menambah SPL baru |
| `SPL_UPDATE` | Mengubah data SPL yang ada |
| `SPL_DELETE` | Menghapus/menonaktifkan SPL |

## 7. Skenario Input Data

### Skenario 1: Mendaftarkan Harga Supplier Baru

**Konteks:** PT Maju Jaya (supplier alat tulis) memberikan katalog harga baru untuk tahun 2026. Anda perlu mendaftarkan harga kertas A4 ke sistem.

**Langkah-langkah:**

1. Buka menu **Pengadaan → Supplier Price List**, klik tombol **+ Tambah Baru**.
2. Isi form dengan data berikut:

   | Field | Nilai |
   |-------|-------|
   | Supplier | PT Maju Jaya |
   | Produk | Kertas HVS A4 80gr |
   | Satuan | RIM |
   | Mata Uang | IDR |
   | Harga per Satuan | 45.000 |
   | Kuantitas Minimum | 10 |
   | Berlaku Dari | 01/01/2026 |
   | Berlaku Sampai | 31/12/2026 |
   | Catatan | Harga katalog Q1-Q4 2026 |
   | Aktif | ✅ Ya |

3. Klik **Simpan**. Sistem akan menyimpan SPL dengan kode otomatis (misal: `SPL-00001`).

**Hasil yang diharapkan:** SPL tersimpan dan muncul di daftar. Ketika bagian pengadaan membuat PO ke PT Maju Jaya dengan item Kertas HVS A4 80gr, harga Rp 45.000/rim ter-autofill secara otomatis.

---

### Skenario 2: Memperbarui Harga karena Kenaikan Harga Pasar

**Konteks:** PT Maju Jaya menginformasikan kenaikan harga mulai April 2026. Anda perlu menutup harga lama dan mendaftarkan harga baru.

**Langkah-langkah:**

1. **Tutup harga lama**: Buka SPL `SPL-00001`, klik **Edit**, ubah `Berlaku Sampai` menjadi **31/03/2026**, klik Simpan.
2. **Daftarkan harga baru**: Klik **+ Tambah Baru**, isi form:

   | Field | Nilai |
   |-------|-------|
   | Supplier | PT Maju Jaya |
   | Produk | Kertas HVS A4 80gr |
   | Satuan | RIM |
   | Mata Uang | IDR |
   | Harga per Satuan | 48.500 |
   | Kuantitas Minimum | 10 |
   | Berlaku Dari | 01/04/2026 |
   | Berlaku Sampai | *(kosong — berlaku selamanya)* |
   | Catatan | Harga revisi per April 2026 |
   | Aktif | ✅ Ya |

3. Klik **Simpan**. SPL baru tersimpan (misal: `SPL-00002`).

**Hasil yang diharapkan:** Dua SPL kini ada untuk produk yang sama — `SPL-00001` berlaku s.d. 31 Maret, `SPL-00002` berlaku ab 1 April. PO yang dibuat setelah 1 April akan otomatis mengambil harga baru Rp 48.500.
