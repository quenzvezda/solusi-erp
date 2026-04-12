# Purchase Order (PO)

Dokumen ini menjelaskan spesifikasi fungsional dan teknis untuk fitur **Purchase Order** (Pesanan Pembelian) di dalam modul Pengadaan (Procurement).

## 1. Ikhtisar (Overview)

Purchase Order adalah dokumen pembelian resmi yang diterbitkan perusahaan kepada supplier. PO merupakan **komitmen hukum** bahwa perusahaan akan membeli sejumlah barang/jasa dengan harga dan syarat yang telah disepakati. Dalam sistem ini, PO memiliki dua tipe:

| Tipe | Deskripsi |
|------|-----------|
| **DIRECT** | PO dibuat langsung ke supplier tanpa memerlukan Purchase Requisition (PR). Cocok untuk pembelian mendesak atau rutin yang tidak memerlukan persetujuan PR. |
| **STANDARD** | PO dibuat berdasarkan PR yang sudah disetujui (APPROVED). Wajib merujuk ke satu PR, dan supplier PO harus sama dengan supplier yang ada di baris PR tersebut. |

## 2. Model Data & Atribut Utama

### Header PO

| Field | Keterangan | Wajib |
|-------|-----------|-------|
| `code` | Nomor dokumen otomatis (format: `PO-yyMM-XXXXX`) | Ya (auto) |
| `poType` | Tipe PO: `DIRECT` atau `STANDARD` | Ya (default: DIRECT) |
| `orderDate` | Tanggal pembuatan PO | Ya |
| `expectedDate` | Tanggal estimasi penerimaan barang | Tidak |
| `supplierId` | Supplier yang dituju | Ya |
| `facilityId` | Gudang penerima barang | Ya |
| `currencyId` | Mata uang transaksi | Ya |
| `exchangeRate` | Kurs konversi ke IDR (wajib > 0) | Ya |
| `paymentTermDays` | Jangka waktu pembayaran (hari) | Ya |
| `prId` | Referensi PR (wajib untuk tipe STANDARD, null untuk DIRECT) | Kondisional |
| `subtotal` | Total sebelum pajak (dihitung otomatis) | Ya (auto) |
| `taxAmount` | Total pajak (dihitung otomatis) | Ya (auto) |
| `totalAmount` | Total akhir = subtotal + pajak (dihitung otomatis) | Ya (auto) |
| `status` | Status dokumen (lihat lifecycle di bawah) | Ya (auto) |
| `note` | Catatan tambahan | Tidak |

### Baris PO (Lines)

| Field | Keterangan | Wajib |
|-------|-----------|-------|
| `productId` | Produk yang dipesan | Ya |
| `quantity` | Jumlah yang dipesan (wajib > 0) | Ya |
| `uomId` | Satuan jumlah (misal: PCS, BOX) | Ya |
| `unitPrice` | Harga per satuan (wajib > 0) | Ya |
| `taxRate` | Persentase pajak (misal: 0.11 untuk PPN 11%) | Tidak (default: 0) |
| `lineSubtotal` | qty × unitPrice (dihitung otomatis) | Ya (auto) |
| `lineTax` | lineSubtotal × taxRate (dihitung otomatis) | Ya (auto) |
| `lineTotal` | lineSubtotal + lineTax (dihitung otomatis) | Ya (auto) |
| `prLineId` | Referensi ke baris PR asal (untuk STANDARD PO) | Tidak |
| `receivedQuantity` | Kuantitas yang sudah diterima (diupdate saat Goods Receipt) | Ya (auto, awal: 0) |
| `note` | Catatan per baris | Tidak |

## 3. Workflow & Aturan Bisnis (Business Rules)

### A. Status Lifecycle

```
DRAFT ──► SUBMITTED ──► APPROVED ──► SENT ──► PARTIALLY_RECEIVED ──► FULLY_RECEIVED ──► BILLED ──► CLOSED
                   │            │
                   └──► REJECTED └──► (tidak bisa ke SENT)
DRAFT/SUBMITTED ──► CANCELLED
```

| Status | Deskripsi | Aksi yang Tersedia |
|--------|-----------|-------------------|
| **DRAFT** | PO baru dibuat, belum diajukan | Edit, Hapus, Submit, Cancel |
| **SUBMITTED** | Sudah diajukan, menunggu persetujuan | Cancel, Approve/Reject (oleh approver) |
| **APPROVED** | Disetujui — siap dikirim ke supplier | Kirim ke Supplier (→ SENT) |
| **REJECTED** | Ditolak oleh approver | Tidak ada aksi lanjutan |
| **SENT** | PO sudah dikirim ke supplier | Penerimaan barang via Goods Receipt |
| **PARTIALLY_RECEIVED** | Sebagian barang sudah diterima | Terima sisa barang |
| **FULLY_RECEIVED** | Semua barang sudah diterima | Proses ke tagihan (AP) |
| **BILLED** | Tagihan vendor sudah dibuat | Proses pembayaran |
| **CLOSED** | Selesai — semua proses tuntas | Tidak ada aksi lanjutan |
| **CANCELLED** | Dibatalkan | Tidak ada aksi lanjutan |

### B. Aturan Tipe PO

**DIRECT PO:**
- Tidak memerlukan PR.
- Field `prId` dikosongkan (null).
- Dapat dibuat kapan saja selama user memiliki hak `PO_CREATE`.

**STANDARD PO:**
- **Wajib** merujuk ke PR yang berstatus `APPROVED`.
- Supplier PO **harus sama** dengan `suggestedSupplierId` yang ada di minimal satu baris PR yang dipilih.
- Sistem memvalidasi di backend — jika PR belum APPROVED atau supplier tidak cocok, sistem menolak penyimpanan.
- Setelah PO STANDARD dibuat, status PR yang dirujuk berubah ke `CONVERTED`.

### C. Perhitungan Total Otomatis
- `lineSubtotal = quantity × unitPrice`
- `lineTax = lineSubtotal × taxRate`
- `lineTotal = lineSubtotal + lineTax`
- `subtotal (header) = Σ lineSubtotal semua baris`
- `taxAmount (header) = Σ lineTax semua baris`
- `totalAmount (header) = subtotal + taxAmount`

Semua kalkulasi dilakukan di backend (domain layer) — tidak bergantung pada JavaScript client.

### D. Validasi Umum
1. **Exchange Rate**: Wajib > 0. Jika mata uang IDR, isi dengan `1`.
2. **Expected Date**: Tidak boleh lebih awal dari `orderDate`.
3. **Lines**: PO tidak dapat di-submit jika tidak memiliki minimal satu baris item.
4. **Edit & Hapus**: Hanya bisa dilakukan saat status **DRAFT**.
5. **Cancel**: Hanya bisa dilakukan saat status **DRAFT** atau **SUBMITTED**.

### E. Alur Approval
- Saat PO di-submit, sistem membuat `ApprovalRequest` secara otomatis.
- Approver dapat melihat status persetujuan dan riwayat keputusan di halaman **detail PO**.
- Setelah PO disetujui (APPROVED), bagian pengadaan dapat mengirimnya ke supplier dengan menekan tombol **Kirim ke Supplier** (→ status SENT).

## 4. Standar UI/UX

- **Tipe Toggle**: Dua radio button (**DIRECT** / **STANDARD**) di bagian atas form. Memilih STANDARD akan menampilkan field **Referensi PR** untuk memilih PR yang sudah disetujui.
- **PR Autocomplete**: Saat tipe STANDARD dipilih, field PR Reference menggunakan autocomplete yang hanya menampilkan PR berstatus APPROVED dari supplier yang sama. Endpoint: `GET /purchasing/purchase-requisitions/api/approved?supplierId={id}`.
- **Drawer Line Item**: Setiap baris item dimasukkan/diedit melalui offcanvas drawer untuk menghindari overflow tabel. Drawer menampilkan semua field baris termasuk kalkulasi otomatis.
- **Approval Sidebar**: Pada halaman detail PO, terdapat panel samping yang menampilkan status approval, nama approver saat ini, dan tombol aksi (jika user adalah approver aktif).
- **Approval History Drawer**: Klik **Riwayat Approval** untuk melihat rantai keputusan lengkap.
- **Tipe Badge**: Daftar PO menampilkan badge **Direct** (biru) atau **Standard** (hijau) di kolom Tipe.
- **Dynamic Recap**: Total subtotal, pajak, dan grand total ditampilkan secara real-time di bagian bawah form saat mengisi baris item.

## 5. Integrasi & Relasi Antar Modul

```
SPL ───────────────────────────────────────────────────────────►
                                                                  PO Form
                                                                  (unitPrice autofill)

PR (APPROVED) ─────────────────────────────────────────────────►
                                                                  STANDARD PO
                                                                  (referensi wajib)

PO (SENT/PARTIALLY_RECEIVED) ──────────────────────────────────►
                                                                  Goods Receipt (GR)
                                                                  Sprint 4

PO (FULLY_RECEIVED) ───────────────────────────────────────────►
                                                                  Vendor Bill (AP)
                                                                  Sprint 5
```

| Dari | Ke | Keterangan |
|------|----|-----------|
| SPL | PO (semua tipe) | Harga unit dapat di-autofill dari SPL aktif saat memilih produk di drawer |
| PR (APPROVED) | PO STANDARD | PR yang sudah disetujui menjadi referensi wajib PO STANDARD |
| PO | PR | Setelah PO STANDARD dibuat, status PR dirujuk berubah ke CONVERTED |
| PO (SENT) | Goods Receipt | GR dibuat berdasarkan PO yang sudah dikirim ke supplier *(Sprint 4)* |
| PO (FULLY_RECEIVED) | Vendor Bill | Tagihan AP dibuat berdasarkan PO yang sudah fully received *(Sprint 5)* |

## 6. Keamanan (Security)

Fitur ini dilindungi oleh otoritas berikut:

| Otoritas | Akses yang Diberikan |
|----------|---------------------|
| `PO_READ` | Melihat daftar dan detail PO |
| `PO_CREATE` | Membuat PO baru |
| `PO_UPDATE` | Mengubah PO berstatus DRAFT |
| `PO_DELETE` | Menghapus PO berstatus DRAFT |
| `PO_SUBMIT` | Mengajukan PO ke approval |
| `PO_SEND` | Mengirim PO ke supplier (APPROVED → SENT) |
| `PO_UPDATE` | Juga digunakan untuk aksi **Cancel** pada PO |
| `LOOKUP_INVENTORY` | Mencari produk via autocomplete |
| `LOOKUP_PURCHASING` | Mencari supplier dan PR via autocomplete |

## 7. Skenario Input Data

### Skenario 1: Membuat DIRECT PO (Tanpa PR)

**Konteks:** Bagian umum membutuhkan toner printer secara mendesak. Pembelian dilakukan langsung tanpa PR karena sudah ada persetujuan verbal dari manager.

**Langkah-langkah:**

1. Buka menu **Pengadaan → Purchase Order**, klik **+ Tambah Baru**.
2. Pilih tipe **DIRECT** (default).
3. Isi header PO:

   | Field | Nilai |
   |-------|-------|
   | Tipe PO | DIRECT |
   | Tanggal PO | 10/04/2026 |
   | Tanggal Estimasi Terima | 14/04/2026 |
   | Supplier | PT Sumber Tinta |
   | Gudang Penerima | Gudang Utama Jakarta |
   | Mata Uang | IDR |
   | Kurs | 1 |
   | Jangka Waktu Bayar | 30 hari |
   | Catatan | Urgent — stok toner habis |

4. Klik **+ Tambah Item** di tabel baris, isi drawer:

   | Field | Nilai |
   |-------|-------|
   | Produk | Toner Printer HP LaserJet 85A |
   | Jumlah | 3 |
   | Satuan | PCS |
   | Harga per Satuan | 285.000 |
   | Pajak | 11% (PPN) |
   | Catatan | Kompatibel dengan HP P1102 |

   Kalkulasi otomatis:
   - Subtotal: Rp 855.000
   - Pajak: Rp 94.050
   - Total: Rp 949.050

5. Klik **Simpan Item**, lalu klik **Simpan** di form utama.

   **Hasil:** PO tersimpan dengan status **DRAFT** dan kode otomatis (misal: `PO-2604-00001`).

6. Klik **Submit untuk Persetujuan**.

   **Hasil:** Status berubah ke **SUBMITTED**, masuk ke antrian approval.

---

### Skenario 2: Membuat STANDARD PO Berdasarkan PR yang Disetujui

**Konteks:** PR `PR-2604-00002` untuk laptop sudah disetujui manager (lihat [Skenario 3 di purchase-requisition.md](./purchase-requisition.md#skenario-3-pr-disetujui-dan-siap-dijadikan-referensi-po)). Bagian pengadaan kini membuat PO resmi ke PT Techno Nusantara.

**Prasyarat:** PR `PR-2604-00002` berstatus **APPROVED**, supplier **PT Techno Nusantara**.

**Langkah-langkah:**

1. Buka menu **Pengadaan → Purchase Order**, klik **+ Tambah Baru**.
2. Pilih tipe **STANDARD**. Field **Referensi PR** muncul.
3. Isi header PO:

   | Field | Nilai |
   |-------|-------|
   | Tipe PO | STANDARD |
   | Referensi PR | PR-2604-00002 (Laptop — IT Dept) |
   | Tanggal PO | 12/04/2026 |
   | Tanggal Estimasi Terima | 30/04/2026 |
   | Supplier | PT Techno Nusantara *(otomatis terisi dari PR)* |
   | Gudang Penerima | Gudang Utama Jakarta |
   | Mata Uang | IDR |
   | Kurs | 1 |
   | Jangka Waktu Bayar | 45 hari |

4. Klik **+ Tambah Item**, isi drawer:

   | Field | Nilai |
   |-------|-------|
   | Produk | Laptop 14 inch Core i5 |
   | Jumlah | 5 |
   | Satuan | PCS |
   | Harga per Satuan | 7.400.000 *(sesuai estimasi PR)* |
   | Pajak | 11% (PPN) |

   Kalkulasi otomatis:
   - Subtotal: Rp 37.000.000
   - Pajak: Rp 4.070.000
   - Total: Rp 41.070.000

5. Klik **Simpan Item**, lalu klik **Simpan**.

   **Hasil:** PO `PO-2604-00002` tersimpan (DRAFT). Status PR `PR-2604-00002` berubah ke **CONVERTED** secara otomatis.

6. Submit PO untuk persetujuan.

---

### Skenario 3: PO Disetujui dan Dikirim ke Supplier

**Konteks:** Manager menyetujui PO laptop `PO-2604-00002`. Bagian pengadaan mengirim PO ke PT Techno Nusantara.

**Langkah-langkah (sudut pandang approver):**

1. Buka PO `PO-2604-00002` di halaman detail.
2. Di panel **Informasi Approval** sebelah kanan, klik **Setujui**.
3. Isi catatan: *"Disetujui sesuai budget IT Q2."*
4. Klik **Konfirmasi Setujui**.

   **Hasil:** Status PO berubah ke **APPROVED**.

**Langkah-langkah (sudut pandang bagian pengadaan):**

5. Buka kembali PO `PO-2604-00002` (status: APPROVED).
6. Klik tombol **Kirim ke Supplier**.
7. Konfirmasi dialog yang muncul.

   **Hasil:** Status berubah ke **SENT**. PO siap dicetak dan dikirim ke PT Techno Nusantara. Proses selanjutnya adalah penerimaan barang melalui **Goods Receipt** (Sprint 4).
