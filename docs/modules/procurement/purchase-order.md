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
| `taxId` | Referensi master tax yang dipilih di header PO | Tidak |
| `taxName` | Snapshot nama pajak pada saat PO disimpan | Tidak |
| `taxRate` | Snapshot tarif pajak dalam format persen (misal `11.00`) | Tidak |
| `taxCalculationMode` | Snapshot mode hitung `EXCLUSIVE` / `INCLUSIVE` | Tidak |
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
| `lineSubtotal` | Nilai DPP hasil kalkulasi header tax | Ya (auto) |
| `lineTax` | Nilai pajak hasil kalkulasi header tax | Ya (auto) |
| `lineTotal` | Nilai bruto baris setelah kalkulasi pajak | Ya (auto) |
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
- Pada **create / pre-add flow**, selector PR hanya menampilkan PR yang masih memiliki line dengan **remaining qty > 0**.
- Saat PR dipilih, field header **supplier**, **facility**, dan **currency** di-derive dari PR lalu di-lock di UI.
- Tombol **Add Line** untuk STANDARD tidak membuat row kosong; tombol ini membuka selector line PR dan hanya menampilkan line yang masih eligible.
- Sistem tetap memvalidasi di backend — request STANDARD tanpa PR atau dengan PR yang belum `APPROVED` tetap ditolak.

### C. Perhitungan Total Otomatis
- Pajak dipilih **sekali di header PO** dari master Tax.
- `taxRate` header disimpan dalam format persen (`11.00`) lalu dinormalisasi ke decimal (`0.11`) saat kalkulasi domain.
- Jika `taxCalculationMode = EXCLUSIVE`:
  - `lineSubtotal = quantity × unitPrice`
  - `lineTax = lineSubtotal × normalizedTaxRate`
  - `lineTotal = lineSubtotal + lineTax`
- Jika `taxCalculationMode = INCLUSIVE`:
  - `gross = quantity × unitPrice`
  - `lineSubtotal = gross / (1 + normalizedTaxRate)`
  - `lineTax = gross - lineSubtotal`
  - `lineTotal = gross`
- `subtotal (header) = Σ lineSubtotal semua baris`
- `taxAmount (header) = Σ lineTax semua baris`
- `totalAmount (header) = Σ lineTotal semua baris`

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

- **Tipe Toggle**: Dua radio button (**DIRECT** / **STANDARD**) di bagian atas form.
- **STANDARD PR Selector Modal**: Saat tipe STANDARD dipilih pada create flow, field **Referensi PR** tidak lagi memakai select biasa. User memilih PR melalui modal selector berbasis tabel yang mendukung search + pagination.
- **Derived Header Locking**: Setelah PR dipilih, `supplier`, `facility`, dan `currency` otomatis terisi dari PR dan dikunci di UI.
- **Header Tax Selector**: Form PO menyediakan satu autocomplete **Tax** di header yang mengambil data dari master Tax aktif. Pemilihan ini mengontrol seluruh perhitungan pajak setiap line.
- **STANDARD Line Selector Modal**: Tombol **Add Line** pada STANDARD membuka selector line PR multi-select. Sistem mengecualikan line yang sudah habis atau sudah dipilih di draft saat ini.
- **Edit Header Parity**: Pada edit DRAFT PO, kontrol **PO Type** dan **Referensi PR** memakai struktur visual yang sama dengan create flow, tetapi tetap non-interaktif/locked agar referensi STANDARD tidak berubah diam-diam.
- **STANDARD Edit Line Expansion**: Pada edit DRAFT STANDARD PO, tombol **Add Line** tetap membuka selector line PR dari referensi yang sama agar user bisa menambahkan sisa line PR yang belum dikonversi, bukan membuat line kosong manual.
- **DIRECT Line Entry**: Tombol **Add Line** pada DIRECT tetap membuat satu row kosong untuk input manual.
- **Inline Line Actions**: Tabel line item hanya menyisakan aksi hapus; tidak ada lagi tombol drawer/pensil atau input pajak manual per baris.
- **Approval Sidebar**: Pada halaman detail PO, terdapat panel samping yang menampilkan status approval, nama approver saat ini, dan tombol aksi (jika user adalah approver aktif).
- **Approval History Drawer**: Klik **Riwayat Approval** untuk melihat rantai keputusan lengkap.
- **Tipe Badge**: Daftar PO menampilkan badge **Direct** (biru) atau **Standard** (hijau) di kolom Tipe.

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
| SPL | PO (semua tipe) | Harga unit dapat di-autofill dari SPL aktif saat memilih produk di row line item |
| PR (APPROVED) | PO STANDARD | PR yang sudah disetujui menjadi referensi wajib PO STANDARD |
| PO | PR | PO STANDARD menyimpan referensi `prId` dan `prLineId` ke PR asal untuk pelacakan konversi parsial |
| PO (SENT) | Goods Receipt | GR dibuat berdasarkan PO yang sudah dikirim ke supplier *(Sprint 4)* |
| PO (FULLY_RECEIVED) | Vendor Bill | Tagihan AP dibuat berdasarkan PO yang sudah fully received *(Sprint 5)* |

## 5.1. Document Flow: Goods Receipt

Setelah PO berstatus **SENT** atau **PARTIALLY_RECEIVED**, bagian gudang dapat menerima barang melalui dokumen **Goods Receipt (GR)** untuk mencatat penerimaan fisik barang dari supplier.

**Alur Dasar:**
1. Buka detail PO dengan status SENT/PARTIALLY_RECEIVED
2. Pada halaman detail PO, tombol **"Create Goods Receipt"** muncul (bersyarat permission `GOODS-RECEIPT_CREATE`)
3. Klik tombol tersebut untuk membuka form GR pre-populated dengan data line item dari PO
4. Isi kuantitas barang yang diterima untuk setiap line item
5. Simpan GR dengan status **DRAFT**
6. Setelah verifikasi fisik selesai, tekan **Complete** untuk finalisasi GR (status → **COMPLETED**)
7. Sistem akan update status PO:
   - Jika semua line item fully received → PO status = **FULLY_RECEIVED**
   - Jika sebagian → PO status = **PARTIALLY_RECEIVED** (tetap)

**Detail Teknis:**
- Link dokumentasi: Lihat [docs/modules/inventory/goods-receipt.md](../inventory/goods-receipt.md)
- Domain entities: `GoodsReceipt`, `GoodsReceiptLine` (inventory module)
- Use cases: `CreateGoodsReceiptUseCase`, `CompleteGoodsReceiptUseCase`, `CountGoodsReceiptsByPoUseCase`
- Handling item serialized: GR mendukung tracking nomor seri/batch untuk item tertentu

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
   | Pajak | PPN 11% Exclusive |
   | Catatan | Urgent — stok toner habis |

4. Klik **+ Tambah Item** di tabel baris, isi form line item:

   | Field | Nilai |
   |-------|-------|
   | Produk | Toner Printer HP LaserJet 85A |
   | Jumlah | 3 |
   | Satuan | PCS |
   | Harga per Satuan | 285.000 |
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
2. Pilih tipe **STANDARD**. Klik tombol **Pilih PR** untuk membuka modal selector PR.
3. Isi header PO:

   | Field | Nilai |
   |-------|-------|
   | Tipe PO | STANDARD |
   | Referensi PR | PR-2604-00002 (dipilih dari modal selector) |
   | Tanggal PO | 12/04/2026 |
   | Tanggal Estimasi Terima | 30/04/2026 |
   | Supplier | PT Techno Nusantara *(otomatis terisi dan lock)* |
   | Gudang Penerima | Gudang Utama Jakarta *(otomatis terisi dan lock bila ada di PR)* |
   | Mata Uang | IDR *(otomatis terisi dan lock)* |
   | Kurs | 1 |
   | Jangka Waktu Bayar | 45 hari |
   | Pajak | PPN 11% Inclusive |

4. Klik **+ Tambah Item**. Sistem membuka modal selector **line PR**.

   | Field | Nilai |
   |-------|-------|
   | Produk | Laptop 14 inch Core i5 *(dipilih dari PR line, locked)* |
   | Jumlah | 5 *(default ke remaining qty, tetap bisa dikurangi untuk partial PO)* |
   | Satuan | PCS *(dipilih dari PR line, locked)* |
   | Harga per Satuan | 7.400.000 *(default dari estimasi PR, tetap editable)* |
     Kalkulasi otomatis:
   - Subtotal: Rp 37.000.000
   - Pajak: Rp 4.070.000
   - Total: Rp 41.070.000

5. Klik **Apply** di modal line selector, lalu klik **Simpan**.

   **Hasil:** PO `PO-2604-00002` tersimpan dengan status **DRAFT** dan seluruh line menyimpan referensi `prLineId` ke line PR asal.

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
