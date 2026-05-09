# Header-Lines Form Pattern

Dokumen ini mendefinisikan standar teknis untuk pembuatan form yang memiliki struktur **Header** (Informasi Utama) dan **Lines** (Item Detail).

---

## 1. Komponen Utama

Setiap form Header-Lines wajib mengikuti struktur berikut:

1.  **Header Section**: Berisi field metadata (Tanggal, Kode, Facility, Currency).
2.  **Summary Card (Dynamic Recap)**: Panel kanan yang menampilkan total perhitungan secara real-time.
3.  **Lines Table**: Tabel input dinamis yang mendukung penambahan/penghapusan baris.
4.  **Edit Drawer**: Side panel untuk penginputan detail tambahan per baris (misal: UoM, Serial Number).

---

## 2. Manajemen Baris Dinamis (`ErpLineManager`)

Pengelolaan baris diatur oleh class `ErpLineManager`. Developer dilarang melakukan manipulasi DOM manual untuk penomoran baris.

### Cara Kerja:
- **Template**: Baris baru diambil dari elemen `<tbody>` tersembunyi dengan ID `#row-template-source`.
- **Indexing**: Setiap kali baris ditambah/dihapus, helper akan mengupdate atribut `name` (misal: `lines[0].qty` -> `lines[1].qty`) agar kompatibel dengan Spring MVC List Binding.
- **Auto-Initialization**: Baris baru otomatis mendapatkan inisialisasi AutoNumeric dan lookup TomSelect.

---

## 3. Inventory Integration (`ErpInventory`)

Untuk transaksi yang melibatkan pergerakan barang, wajib menggunakan `ErpInventory.setupUomLogic`.

### Fitur:
- **Multi-UoM Support**: Menghitung kuantitas base secara otomatis berdasarkan faktor konversi produk.
- **Serial Number Sync**: Menyediakan input Serial Number dinamis sesuai jumlah kuantitas yang dimasukkan.
- **Validation**: Mencegah penyimpanan jika data di dalam drawer belum valid.

---

## 4. Pola Form Hybrid (Add & Edit)

Untuk efisiensi, satu file HTML harus menangani mode pembuatan (*Add*) dan perubahan (*Edit*).

- **Conditional Titles**: Gunakan `th:text="${object.id == null ? 'Add' : 'Edit'}"`.
- **Locking Logic**: Jika dokumen sudah diproses (Status COMPLETED), seluruh input (button, select, input) harus otomatis di-disable secara global via JavaScript.

### 4.1 Line Turunan Dokumen vs Line Manual

Pada form yang bisa bersumber dari dokumen lain (contoh: GR dari PO), line harus dibedakan tegas:

- **Line turunan dokumen** (`referenceLineId` terisi): field identitas line seperti `product`/`uom` diperlakukan sebagai snapshot referensi dan harus di-lock.
- **Line manual** (tanpa referensi): field identitas line boleh editable sesuai rule domain.
- Tombol **Add Line** pada mode turunan dokumen membuka **modal selector** agar line baru tetap memiliki referensi valid, bukan row kosong bebas.

### 4.2 Mandatory Marker + Backend Validation

- Field wajib pada tabel line wajib menampilkan indikator visual konsisten (misalnya asterisk merah pada header kolom).
- Validasi UI hanya sebagai guard cepat; backend tetap menjadi sumber kebenaran (`@Valid`/constraint DTO) untuk mencegah data invalid tersimpan.

---

## 5. Sinkronisasi Data

- **Header to Line**: Gunakan event listener pada field header (seperti Facility) untuk memicu aksi pada line (misal: hapus semua line jika gudang berubah).
- **Line to Summary**: Gunakan event `input` pada tabel untuk memicu fungsi `calculateTotals()` yang mengupdate Summary Card.
