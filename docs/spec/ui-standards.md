# UI Standard: Input Components (Golden Standard)

## Overview
To ensure visual consistency across the Solusi Program ERP, all input elements must follow a unified height, padding, and styling pattern. The **Stock Adjustment** page serves as the "Golden Standard" for these implementations.

## UI Layout Standards

### 1. Standard List Page
Setiap halaman daftar data (List) wajib mengikuti tata letak berikut untuk konsistensi:
- **Search Bar**: Harus diletakkan di sisi kanan menggunakan utility `ms-auto`.
- **Search Bar Structure**: Gunakan pembungkus `div.input-icon` di dalam form untuk memisahkan input teks dari elemen `input type="hidden"` agar tidak merusak padding CSS.
- **Search Bar Sizing**: Gunakan class `.form-control-sm` pada input pencarian untuk tampilan yang lebih compact dan seragam di seluruh modul.
- **Input Icon**: Gunakan `.ti-search` sebagai dekorator di dalam `.input-icon-addon`.
- **Asynchronous Navigation**: Header tabel (sorting) dan paginasi wajib menggunakan `hx-boost="true"` (sudah termasuk dalam fragment standard).
- **Action Buttons**: 
  - Gunakan class `.btn-white.btn-sm` untuk tombol aksi di dalam tabel.
  - Bungkus dalam `.btn-list.flex-nowrap.justify-content-end` agar rapi.
  - Selalu gunakan icon (Tabler Icons) di samping teks label.

### 2. Operational List Table Density
Halaman daftar operasional seperti Purchase Order, Vendor Bill, Debit Memo, Payment, Goods Receipt, dan dokumen transaksi lain harus mudah di-scan tanpa menggeser halaman.

- **No Page-Level Horizontal Overflow**: Tabel list tidak boleh membuat body halaman melebar keluar viewport desktop umum (1366px ke atas). Jika tabel tampak membutuhkan horizontal scrollbar, evaluasi ulang kolomnya terlebih dahulu.
- **Horizontal Scroll Is Last Resort**: Scroll horizontal hanya boleh dipakai untuk report, matrix, atau tabel analitik dengan banyak dimensi. Untuk list operasional, prefer compact table.
- **Combine Derived Columns**: Nilai turunan yang satu konteks harus digabung dalam satu cell stack, bukan dijadikan kolom terpisah.
  - Contoh amount stack: `Gross`, `Settled`, `Remaining`.
  - Contoh invoice date stack: `Bill Date` dan `Due Date`.
  - Contoh status stack: `Document Status` dan `Settlement Status`.
- **Keep Primary Identifiers Separate**: Kolom utama seperti code, date, party/vendor/customer, source document, status, amount summary, dan actions boleh tetap terpisah karena dipakai untuk scan cepat.
- **Use Fixed Table Layout When Needed**: Untuk list dengan data panjang, gunakan `table-layout: fixed; width: 100%;` dan `text-truncate d-block` pada content yang panjang agar layout tidak melebar.
- **Currency Placement**: Currency tidak perlu menjadi kolom sendiri di list jika mayoritas dokumen memakai currency yang sama. Gabungkan di bawah amount summary atau tampilkan di halaman detail.
- **Action Column**:
  - Action utama seperti `View` harus tetap cukup besar dan jelas. Gunakan `.btn.btn-white.btn-sm` dengan icon + teks.
  - Icon-only action hanya boleh dipakai untuk secondary/destructive action yang konteksnya jelas, dan tetap wajib punya `title`/`aria-label`.
  - Lebar kolom action harus cukup untuk label tombol; jangan membuat tombol menjadi terlalu kecil hanya untuk menghemat kolom.

Contoh compact amount cell:
```html
<td class="text-end">
    <div class="d-flex justify-content-between gap-2 small">
        <span class="text-secondary" th:text="#{label.debit-memo.gross}">Gross</span>
        <span th:text="${#numbers.formatDecimal(item.grossAmountOriginal, 1, 'COMMA', 2, 'POINT')}">0.00</span>
    </div>
    <div class="d-flex justify-content-between gap-2 small">
        <span class="text-secondary" th:text="#{label.debit-memo.remaining}">Remaining</span>
        <span class="fw-medium" th:text="${#numbers.formatDecimal(item.remainingAmount, 1, 'COMMA', 2, 'POINT')}">0.00</span>
    </div>
</td>
```

### 3. Standard Delete Confirmation
Untuk menjaga keamanan data, setiap aksi penghapusan (Delete) wajib menggunakan **Modal Confirmation** (bukan `window.confirm` bawaan browser). 

**Komponen Modal:**
- **ID Modal**: Gunakan format `modal-delete-${item.id}`.
- **Warna Aksen**: Gunakan class `.modal-status.bg-danger` untuk memberikan indikator visual bahaya.
- **Ikon**: Gunakan `.ti-alert-triangle.text-danger` berukuran besar (`.icon-lg`).
- **Teks Konfirmasi**: Harus menyebutkan nama atau kode data yang akan dihapus menggunakan i18n (misal: `label.delete.confirm.text(${item.code})`).
- **Tombol Aksi**: Tombol "Hapus" harus berwarna merah (`.btn-danger`) dan diletakkan di sisi kanan bawah.

**Implementasi Fragment (Mandatory):**
Gunakan fragment `fragments/modals :: delete-confirm` untuk menjaga konsistensi dan kebersihan kode. 

Contoh pemanggilan:
```html
<div th:replace="~{fragments/modals :: delete-confirm(
    id='modal-delete-' + ${item.id},
    title=#{label.delete.confirm.title},
    message=#{label.my.module.delete.confirm(${item.name})},
    actionUrl='/my-module/' + ${item.id},
    targetId='#row-' + ${item.id}
)}"></div>
```
*Catatan: Parameter `targetId` adalah selector CSS untuk baris tabel yang akan di-swap/dihapus oleh HTMX.*

---

## Standard CSS Classes

### 1. Standard Form Inputs (`32px` height)
Used for header fields and standard forms.
- `.erp-input`: Applied to `<input>`, `<select>`, and `<textarea>`.
- `.erp-input-ts`: Applied to TomSelect **wrappers** (automatically managed by `initLookup`).
- `.erp-number-decimal`: AutoNumeric formatting for decimals (2 decimal places).
- `.erp-number-integer`: AutoNumeric formatting for whole numbers.

### 2. Table/Dense Inputs (`28px` height)
Used for inline editing inside tables (e.g., line items).
- `.erp-input-sm`: Applied to small `<input>` and `.erp-input-sm`.
- `.erp-input-ts-sm`: Applied to small TomSelect **wrappers**.

## Visual Themes & Branding

Sistem mendukung tema visual dinamis yang dapat diatur per-user melalui profil. Tema ini mengontrol atmosfer aplikasi tanpa merusak kegunaan (usability).

### 1. Mekanisme Penerapan Tema
Tema diterapkan pada tag `<body>` di `master.html` menggunakan atribut `data-bs-theme`. Atribut ini diisi secara dinamis dari objek `userProfile` yang disuntikkan oleh `GlobalModelAttributeAdvice`.

### 2. Daftar Tema Standar
- **`light` (Default)**: Tampilan putih bersih standar Tabler/Bootstrap.
- **`dark`**: Mode gelap penuh untuk kenyamanan mata di lingkungan minim cahaya.
- **`warm`**: Menggunakan rona *Cream* halus pada latar belakang dan aksen *Amber* untuk memberikan kesan hangat dan rileks.
- **`green`**: Menggunakan rona *Mint* tipis pada latar belakang dan aksen *Emerald* untuk kesan segar dan modern.

### 3. Sidebar Persistence
Terlepas dari tema yang dipilih (Light/Warm/Green), komponen **Sidebar** tetap menggunakan `data-bs-theme="dark"` secara permanen untuk menjaga kontras tinggi dan identitas brand ERP.

### 4. Dark Mode & Theme Compatibility (CRITICAL)
Untuk memastikan UI tetap terbaca dan profesional di semua tema (Dark, Warm, Green), Developer **WAJIB** mengikuti aturan class berikut:

1.  **Avoid Fixed Backgrounds**: 
    - **DILARANG** menggunakan class `bg-light` atau `bg-white` secara eksplisit pada elemen input, kartu, atau header tabel karena warna ini tidak akan berubah di Mode Gelap.
    - **GUNAKAN** class `bg-body-tertiary` untuk latar belakang abu-abu halus yang adaptif, atau `bg-secondary-lt` untuk area rekap/highlight yang tetap kontras di semua tema.
    
2.  **Adaptive Text Colors**:
    - **DILARANG** menggunakan `text-dark` untuk teks konten utama (seperti angka total atau label) karena akan menjadi tidak terbaca di Mode Gelap.
    - **GUNAKAN** `text-body` (default) atau `text-reset` agar warna teks otomatis menyesuaikan dengan tema yang aktif.
    
3.  **Readonly Inputs**:
    - Untuk input yang bersifat `readonly` atau `disabled`, biarkan browser/Bootstrap menanganinya atau gunakan `isReadonly=true` pada fragment tanpa menambahkan `bg-light` manual.
    
4.  **Sticky Headers**:
    - Saat membuat tabel dengan `sticky-top`, pastikan **TIDAK** menambahkan `bg-white`. Gunakan `bg-body` atau biarkan transparan jika pembungkusnya sudah memiliki warna latar belakang yang tepat.

---

## Global Programmatic Modals (ErpModal)
Untuk menghindari ketergantungan langsung pada objek `bootstrap` di level JavaScript halaman (yang seringkali tidak terdefinisi karena masalah loading), sistem menyediakan helper global **`ErpModal`**.

### 1. Cara Pemanggilan
DILARANG menggunakan `new bootstrap.Modal()` secara langsung. Gunakan fungsi berikut:
- **Error**: `ErpModal.showError(message, optionalTitle)`
- **Warning**: `ErpModal.showWarning(message, optionalTitle)`
- **Confirm**: `ErpModal.confirm(message, callbackFunction, optionalTitle)`

Contoh Penggunaan:
```javascript
if (!facilityId) {
    ErpModal.showWarning("Harap pilih gudang terlebih dahulu!");
    return;
}

ErpModal.confirm("Yakin ingin memproses data?", function() {
    // Logika jika user klik 'Ya'
    form.submit();
});
```

## Thymeleaf Fragments
Always prefer using the standardized fragments in `templates/fragments/inputs.html`.

### Usage Examples:

#### Standard Text & Numeric Input
```html
<div th:replace="~{fragments/inputs :: text(field='name', label='Full Name')}"></div>
<div th:replace="~{fragments/inputs :: decimal(field='price', label='Price')}"></div>
```

#### Hybrid Form Submission
Setiap form transaksi atau master data kompleks wajib menggunakan pola AJAX untuk menjaga UI state:
```html
<form id="product-form"
      th:action="@{...}"
      method="post"
      data-ajax-form="true"
      data-redirect-on-success="/inventory/products">
    ...
</form>
```
Gunakan HTMX hanya untuk filter pencarian atau interaksi sederhana yang tidak merusak komponen JavaScript. Lihat **[form-submission.md](form-submission.md)** untuk panduan lengkap.

## Best Practices & JavaScript Initialization

### 1. Global Auto-Initialization
Sistem secara otomatis menginisialisasi komponen berikut melalui **`shared/erp-common-handler.js`**:
- **Numeric**: Elemen dengan class `.erp-number-*`.
- **Autocomplete**: Elemen `.erp-input-ts` yang memiliki atribut `data-lookup-path`.

**Aturan Wajib: Trinity Data (ID, Name, SubText)**
Untuk mencegah dropdown terlihat kosong saat mode Edit atau setelah error validasi, setiap implementasi Autocomplete **WAJIB** menyertakan:
1.  **ID (Value)**: Disimpan ke database.
2.  **Name (Text)**: Label utama yang terlihat.
3.  **SubText (Code)**: Informasi sekunder (kode) di bawah nama.

Developer wajib memastikan Request DTO memiliki field penampung untuk Name dan SubText tersebut (contoh: `brandName`, `brandCode`).

### 2. The Global `initLookup` Function
Jika butuh inisialisasi manual (misal: cascading), gunakan fungsi yang tersedia di `shared/erp-common-handler.js`:
```javascript
const ts = initLookup(element, 'module/path', parentProvider);
```
- `lookupPath`: String path API tanpa `/api/lookup/` (contoh: `'inventory/products'`).
- `parentProvider`: Callback function untuk filter data berdasarkan field lain.

### 2. Height Consistency
The `initLookup` function automatically detects if an element is inside a `.line-row` table and applies `.erp-input-ts-sm`, otherwise it applies `.erp-input-ts`. This ensures that the TomSelect component matches the `32px` or `28px` height of adjacent `.erp-input` fields.

### 3. SSR Synchronization
The `initLookup` function reads the `data-subtext` attribute from the initial `<option>` rendered by Thymeleaf. This ensures that the Code/Subtext is visible immediately upon page load (Edit Mode).

### 4. Validation
Always include `th:errorclass="is-invalid"` (included by default in fragments) for server-side validation feedback.

**Clean Validation Standard:**
Untuk menjaga tampilan tetap profesional dan bersih, kita menonaktifkan ikon validasi bawaan (seperti ikon "X" merah) di seluruh jenis input.
- **Visual**: Hanya menggunakan **Border Merah** yang tegas (`#d63939`).
- **TomSelect**: Menggunakan class `.is-invalid-ts` pada wrapper untuk memberikan efek border merah yang identik dengan input standar.
- **Feedback**: Pesan error ditampilkan dalam class `.invalid-feedback` di bawah elemen input.

