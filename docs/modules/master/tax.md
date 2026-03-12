# Spesifikasi Fungsional: Tax (Pajak)

## 1. Ringkasan Modul
Modul Tax merupakan master data yang digunakan untuk menyimpan dan mengelola berbagai jenis pajak (seperti PPN, PPh) yang akan diterapkan pada transaksi-transaksi dalam sistem ERP (misal: Pembelian, Penjualan). 

## 2. Fitur Utama
*   **Manual Tax Code**: Berbeda dengan mayoritas modul lain, kode pajak (Tax Code) diinput secara manual oleh *user* (bukan *auto-generated*) untuk menyesuaikan dengan kode standar akuntansi atau regulasi pemerintah (contoh: `PPN11`).
*   **Subtracting Flag (`isSubtract`)**: Mendukung *flag* khusus untuk menentukan apakah suatu pajak sifatnya memotong/mengurangi total nilai transaksi (seperti PPh) atau menambah total nilai transaksi (seperti PPN).
*   **Active/Inactive Toggle**: Mendukung aktivasi/deaktivasi data pajak secara dinamis dari form *create* maupun *edit* tanpa harus menghapus data.
*   **Soft Delete**: Mendukung penghapusan halus (`isActive = false`) untuk mempertahankan integritas data historis pada transaksi lama yang menggunakan pajak tersebut.
*   **Global Visibility**: Semua daftar pajak (aktif maupun tidak aktif) akan ditampilkan di halaman master data (*List View*) agar admin dapat memonitor atau mengaktifkan kembali pajak yang lama. Namun, pada *dropdown* transaksi, hanya pajak yang aktif (`isActive = true`) yang boleh dimunculkan.

## 3. Aturan Bisnis (Business Rules)
1.  **Kode Pajak (Code)**: Wajib diisi, harus unik (tidak boleh duplikat di *database*), dan *read-only* saat mode *edit*.
2.  **Nama Pajak (Name)**: Wajib diisi.
3.  **Nilai Persentase (Rate)**: Wajib diisi dalam bentuk desimal (contoh: `11.00` untuk 11%). Tidak boleh bernilai negatif.
4.  **Hapus Data**: DILARANG keras menggunakan *Hard Delete*. Sistem menggunakan *Soft Delete* (flag `isActive = false`).
5.  **Security/Permissions**:
    *   `TAX_READ`: Akses daftar dan detail pajak.
    *   `TAX_CREATE`: Akses menambah data pajak baru.
    *   `TAX_UPDATE`: Akses mengubah data pajak yang sudah ada.
    *   `TAX_DELETE`: Akses menghapus (soft-delete) data pajak.

## 4. Panduan Implementasi UI
### Toggle Checkbox Handling (Spring Boot)
Checkbox HTML (seperti `isActive` dan `isSubtract`) yang **tidak dicentang** tidak akan mengirimkan *parameter* apapun di dalam *payload request*. Oleh karena itu, Spring akan melakukan *binding* nilai ke `null`. Pada *layer Service* (`TaxServiceImpl`), *value* `null` pada *checkbox* INI **WAJIB** secara manual dikonversi menjadi `false` (baik saat *create* maupun *update*) agar Hibernate tidak melempar *exception* `Column cannot be null`. Saran implementasi:

```java
if (tax.getIsActive() == null) {
    tax.setIsActive(false);
}
```

### UI Status Label Standard
Pada halaman *List View*, status Aktif/Tidak Aktif menggunakan standar desain *badge-outline* dengan *badge-dot* yang diwajibkan dalam proyek (lihat `docs/spec/pagination.md` bagian 6).

```html
<span class="badge badge-outline text-green" th:if="${item.isActive}">
    <span class="badge-dot bg-success me-1"></span>
    <span th:text="#{label.active}">Aktif</span>
</span>
<span class="badge badge-outline text-red" th:unless="${item.isActive}">
    <span class="badge-dot bg-danger me-1"></span>
    <span th:text="#{label.inactive}">Tidak Aktif</span>
</span>
```
