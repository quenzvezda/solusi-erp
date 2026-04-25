# Purchase Requisition (PR)

Dokumen ini menjelaskan spesifikasi fungsional dan teknis untuk fitur **Purchase Requisition** (Permintaan Pembelian) di dalam modul Pengadaan (Procurement).

## 1. Ikhtisar (Overview)

Purchase Requisition adalah dokumen internal yang dibuat oleh karyawan atau departemen untuk **meminta pengadaan barang/jasa** ke bagian pembelian. PR berfungsi sebagai titik awal proses pengadaan formal — sebelum Purchase Order (PO) dibuat ke supplier, permintaan harus melalui alur persetujuan (approval) terlebih dahulu.

**Kapan PR digunakan?**
- Saat departemen membutuhkan barang dan meminta bagian pengadaan untuk membelikannya.
- Sebagai dokumen kontrol internal agar pembelian tidak dilakukan secara sembarangan (harus ada persetujuan atasan).
- Sebagai referensi wajib untuk membuat **PO tipe STANDARD** — PO hanya dapat dibuat jika ada PR yang sudah disetujui dari supplier yang sama.

## 2. Model Data & Atribut Utama

### Header PR

| Field | Keterangan | Wajib |
|-------|-----------|-------|
| `code` | Nomor dokumen otomatis (format: `PR-yyMM-XXXXX`) | Ya (auto) |
| `requestDate` | Tanggal pengajuan permintaan | Ya |
| `requesterId` | Karyawan/user yang mengajukan permintaan | Ya (dipilih via autocomplete) |
| `facilityId` | Gudang/lokasi tujuan penerimaan barang | Ya |
| `department` | Nama departemen pemohon | Tidak |
| `suggestedSupplierId` | Supplier utama yang dirujuk untuk estimasi harga / konversi STANDARD PO | Ya |
| `currencyId` | Mata uang estimasi harga | Ya |
| `priority` | Tingkat urgensi: `LOW`, `NORMAL`, `HIGH`, `URGENT` | Ya (default: NORMAL) |
| `status` | Status dokumen (lihat lifecycle di bawah) | Ya (auto) |
| `note` | Catatan tambahan | Tidak |

### Baris PR (Lines)

| Field | Keterangan | Wajib |
|-------|-----------|-------|
| `productId` | Produk yang diminta | Ya |
| `quantity` | Jumlah yang diminta (wajib > 0) | Ya |
| `uomId` | Satuan jumlah (misal: PCS, BOX) | Ya |
| `requiredDate` | Tanggal kebutuhan barang | Tidak |
| `estimatedUnitPrice` | Estimasi harga per satuan (referensi dari SPL aktif berdasarkan supplier + currency header, atau manual) | Tidak |
| `note` | Catatan per baris | Tidak |

> **Catatan implementasi saat ini:** Supplier dan currency berada di level **header**. Selector/autofill harga mengikuti kombinasi header `suggestedSupplierId` + `currencyId` dengan item di masing-masing baris.

## 3. Workflow & Aturan Bisnis (Business Rules)

### A. Status Lifecycle

```
DRAFT ──► SUBMITTED ──► APPROVED
                   │
                   └──► REJECTED
DRAFT/SUBMITTED/APPROVED ──► CANCELLED
```

| Status | Deskripsi | Aksi yang Tersedia |
|--------|-----------|-------------------|
| **DRAFT** | PR baru dibuat, belum diajukan | Edit, Hapus, Submit, Cancel |
| **SUBMITTED** | Sudah diajukan, menunggu persetujuan | Cancel (oleh pembuat), Approve/Reject (oleh approver) |
| **APPROVED** | Disetujui oleh approver | Cancel, Rujuk sebagai STANDARD PO |
| **REJECTED** | Ditolak oleh approver | Tidak ada aksi lanjutan (buat PR baru jika diperlukan) |
| **CANCELLED** | Dibatalkan | Tidak ada aksi lanjutan |

> **Catatan implementasi saat ini:** Enum `CONVERTED` sudah ada di kode, tetapi flow runtime saat ini masih memposisikan PR yang sudah disetujui sebagai referensi STANDARD PO berdasarkan **remaining qty** per line. Dokumen ini mengikuti perilaku runtime yang aktif, bukan target-state yang belum di-wire penuh.

### B. Aturan Submit
- PR **tidak dapat di-submit** jika tidak memiliki minimal satu baris item.
- Setelah submit, status berubah ke **SUBMITTED** dan dokumen masuk ke antrian approval.

### C. Alur Approval
- Saat PR di-submit, sistem membuat `ApprovalRequest` secara otomatis (melalui event `OnPurchaseRequisitionSubmittedEvent`).
- Approver dapat melihat banner persetujuan dan riwayat keputusan di halaman **detail PR**.
- Approver yang sedang berjalan (approver aktif) dapat melakukan **Approve** atau **Reject** langsung dari halaman detail.
- Setelah keputusan diambil, status PR berubah ke **APPROVED** atau **REJECTED** secara otomatis.

### D. Aturan Update & Hapus
- PR hanya dapat **diubah** saat berstatus **DRAFT**.
- PR hanya dapat **dihapus** (soft delete, `active = false`) saat berstatus **DRAFT**.
- PR yang sudah SUBMITTED atau lebih tidak dapat diedit — harus dibatalkan terlebih dahulu jika ada perubahan.

## 4. Standar UI/UX

- **Inline Line Editor**: Setiap baris item ditambahkan langsung di tabel inline lewat tombol **+ Tambah Item**. Tidak ada lagi offcanvas drawer untuk line item.
- **Header-first Entry**: User harus mengisi **Requester**, **Facility**, **Supplier**, dan **Currency** di header sebelum dapat menambah line baru.
- **Auto-fill UoM & SPL Price**: Saat produk dipilih, sistem mengisi UoM default dari produk dan mencoba mengambil `estimatedUnitPrice` dari SPL aktif berdasarkan supplier + currency header serta `requiredDate` pada baris.
- **Header Change Reset**: Perubahan requester/facility/supplier/currency pada header akan mengosongkan line yang sudah ada untuk mencegah data estimasi menjadi tidak sinkron.
- **Priority Badge**: Kolom prioritas ditampilkan dengan badge berwarna (URGENT = merah, HIGH = oranye, NORMAL = biru, LOW = abu-abu).
- **Approval History Drawer**: Klik tombol **Riwayat Approval** untuk melihat seluruh rantai keputusan (siapa yang approve/reject, kapan, dan catatan keputusan).
- **Approval Action Banner**: Jika user adalah approver aktif, tombol aksi approve/reject tampil sebagai banner aksi pada halaman detail.
- **Status Badge**: Header PR menampilkan badge status yang berubah warna sesuai kondisi dokumen.

## 5. Integrasi & Relasi Antar Modul

```
SPL ────────────────────────────────────────────────────────►
                                                               PR Form
                                                               (estimatedUnitPrice referensi)

PR (APPROVED) ─────────────────────────────────────────────►
                                                               PO STANDARD
                                                               (referensi wajib + line selector berbasis remaining qty)
```

| Dari | Ke | Keterangan |
|------|----|-----------|
| SPL | PR | `estimatedUnitPrice` dapat diambil dari SPL aktif untuk kombinasi supplier header + produk + UoM + currency |
| PR | PO (STANDARD) | PR berstatus APPROVED dapat menjadi referensi wajib saat membuat PO tipe STANDARD. Header supplier / facility / currency PO diturunkan dari PR, dan line dipilih berdasarkan remaining qty |
| PO | PR Line Tracking | STANDARD PO menyimpan `prId` dan `prLineId` untuk pelacakan line asal; perilaku selector saat ini berbasis remaining qty, bukan perubahan status header PR secara otomatis |

## 6. Keamanan (Security)

Fitur ini dilindungi oleh otoritas berikut:

| Otoritas | Akses yang Diberikan |
|----------|---------------------|
| `PR_READ` | Melihat daftar dan detail PR |
| `PR_CREATE` | Membuat PR baru |
| `PR_UPDATE` | Mengubah PR berstatus DRAFT |
| `PR_DELETE` | Menghapus PR berstatus DRAFT |
| `PR_SUBMIT` | Mengajukan PR ke approval |
| `PR_UPDATE` | Juga digunakan untuk aksi **Cancel** pada PR |
| `LOOKUP_INVENTORY` | Mencari produk via autocomplete |
| `LOOKUP_PURCHASING` | Mencari supplier via autocomplete |
| `LOOKUP_SUPPLIER-PRICE-LIST` | Mengambil estimasi harga otomatis dari SPL aktif |

## 7. Skenario Input Data

### Skenario 1: Membuat dan Mengajukan PR (Alur Normal)

**Konteks:** Departemen IT membutuhkan 5 unit laptop untuk karyawan baru yang mulai bulan depan. Staf procurement membuat PR dan mengajukannya ke manager untuk disetujui.

**Langkah-langkah:**

1. Buka menu **Pengadaan → Purchase Requisition**, klik **+ Tambah Baru**.
2. Isi header PR:

   | Field | Nilai |
   |-------|-------|
   | Tanggal Pengajuan | 05/04/2026 |
   | Requester | Budi Santoso |
   | Gudang Tujuan | Gudang Utama Jakarta |
   | Departemen | Information Technology |
   | Supplier Disarankan | PT Techno Nusantara |
   | Mata Uang | IDR |
   | Prioritas | HIGH |
   | Catatan | Untuk onboarding karyawan baru bulan Mei |

3. Klik **+ Tambah Item** untuk menambah satu baris inline di tabel, lalu isi baris pertama:

   | Field | Nilai |
   |-------|-------|
   | Produk | Laptop 14 inch Core i5 |
   | Jumlah | 5 |
   | Satuan | PCS |
   | Tanggal Dibutuhkan | 01/05/2026 |
   | Estimasi Harga | 8.500.000 |
   | Catatan | Minimal RAM 16GB |

4. Klik **Simpan** di form utama.

   **Hasil:** PR tersimpan dengan status **DRAFT** dan kode otomatis (misal: `PR-2604-00001`).

5. Klik tombol **Submit untuk Persetujuan**. Konfirmasi dialog yang muncul.

   **Hasil:** Status berubah ke **SUBMITTED**. Notifikasi dikirim ke approver.

---

### Skenario 2: PR Ditolak dan Direvisi

**Konteks:** Manager menolak PR laptop karena estimasi harga tidak realistis — harga pasaran lebih rendah. Staf harus membuat PR baru dengan harga yang telah dikoreksi.

**Langkah-langkah (sudut pandang manager/approver):**

1. Buka PR `PR-2604-00001` di halaman detail.
2. Di panel **Informasi Approval** sebelah kanan, klik tombol **Tolak**.
3. Isi alasan penolakan: *"Estimasi harga terlalu tinggi. Harga pasar saat ini berkisar Rp 7.200.000–7.500.000. Mohon direvisi."*
4. Klik **Konfirmasi Tolak**.

   **Hasil:** Status PR berubah ke **REJECTED**.

**Langkah-langkah (sudut pandang staf, membuat PR baru):**

5. Karena PR yang ditolak tidak dapat diedit, staf membuat PR baru (`+ Tambah Baru`).
6. Isi data yang sama, namun ubah **Estimasi Harga** menjadi `7.400.000`.
7. Submit PR baru untuk persetujuan ulang.

   **Hasil:** PR baru `PR-2604-00002` masuk ke antrian approval.

---

### Skenario 3: PR Disetujui dan Siap Dijadikan Referensi PO

**Konteks:** Manager menyetujui PR laptop. Bagian pengadaan kini dapat membuat Purchase Order ke PT Techno Nusantara berdasarkan PR yang telah disetujui.

**Langkah-langkah (sudut pandang approver):**

1. Buka PR `PR-2604-00002` di halaman detail.
2. Di panel **Informasi Approval**, klik tombol **Setujui**.
3. Isi catatan persetujuan (opsional): *"Harga sudah sesuai. Silakan proses pembelian."*
4. Klik **Konfirmasi Setujui**.

   **Hasil:** Status PR berubah ke **APPROVED**. PR ini kini dapat digunakan sebagai referensi PO STANDARD.

**Langkah selanjutnya:** Bagian pengadaan membuat PO STANDARD dengan merujuk ke PR `PR-2604-00002` — lihat [Skenario 2 di purchase-order.md](./purchase-order.md#skenario-2-membuat-standard-po-berdasarkan-pr-yang-disetujui).
