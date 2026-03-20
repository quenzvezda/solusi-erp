# Architecture & UI Standardization Roadmap

Dokumen ini melacak inisiatif untuk menyederhanakan kode, meningkatkan User Experience (UX), dan menstandarisasi pola pengembangan di seluruh modul ERP. Tujuannya adalah untuk memberikan panduan yang jelas bagi AI Agent dan Developer di masa depan.

## 1. Decimal & Numeric Formatting Standardization
**Masalah saat ini:** Input angka masih manual menggunakan `type="number"`, tidak ada pemisah ribuan (thousand separator), dan format desimal tidak konsisten. Input `type="number"` bawaan browser juga tidak mendukung format lokal yang cantik (misal: `1,250,000.00`).

**Action Items (Target):**
- [x] **Library Integration (Frontend):**
    - Tambahkan `AutoNumeric` library (MIT License) via CDN di `master.html`.
    - Buat helper function global `initNumericInputs(container)` di `master.html` untuk menginisialisasi elemen berdasarkan class.
- [x] **Standardized Numeric Fragments:** Buat dua fragment baru di `fragments/inputs.html` yang menggunakan `<input type="text">`:
    - `integer`: Menggunakan class `.erp-number-integer`. Konfigurasi: 0 desimal, ribuan separator `,`.
    - `decimal`: Menggunakan class `.erp-number-decimal`. Konfigurasi: 2 desimal (default), ribuan separator `,`, desimal point `.`.
- [x] **Real-time Formatting Logic:**
    - Trigger: Event `input` (keyup/down/paste) ditangani secara internal oleh AutoNumeric untuk instant feedback tanpa merusak posisi kursor.
    - Dynamic Row Support: Pastikan input numeric pada tabel dinamis (seperti Stock Adjustment Lines) otomatis terinisialisasi saat baris baru ditambahkan.
- [x] **Global Spring Formatter (Backend):**
    - Implementasikan `WebMvcConfigurer` dengan `addFormatters`.
    - Daftarkan `BigDecimalFormatter` yang otomatis melakukan `replaceAll(",", "")` sebelum parsing, agar Spring tidak error saat menerima string `"1,250.00"`.
    - Daftarkan `IntegerFormatter` untuk menangani format string seperti `"1,250"`.
- [x] **DTO Scale Alignment:** Standardisasi inisialisasi `BigDecimal` di seluruh Request DTO agar menggunakan scale 2 (`new BigDecimal("0.00")`) demi konsistensi UI saat pertama kali load.

## 2. HTMX Form Submission & Error Handling
**Masalah saat ini:** Menggunakan Full Page Refresh saat validasi gagal menyebabkan UI state (seperti label Autocomplete/TomSelect) hilang. Penggunaan *Hidden Fields* sebagai solusi sementara membuat struktur HTML menjadi kotor.

**Action Items (Target):**
- [x] **Standardisasi HTMX Form:** Wajibkan semua form baru menggunakan atribut HTMX (`hx-post`, `hx-target="#alert-container"`, `hx-swap="innerHTML"`).
- [x] **Global Alert Fragment:** Buat fragment standar (misal di `fragments/alerts.html`) khusus untuk menerima response error dari HTMX.
- [x] **TomSelect Re-initialization:** Buat script event listener global (`htmx:afterSwap`) di `master.html` yang otomatis melakukan *re-init* TomSelect atau tooltips jika fragment yang diswap mengandung elemen tersebut.

## 3. Controller Boilerplate Reduction
**Masalah saat ini:** Controller kita memiliki kode yang redundan untuk mengecek `htmxRequest`, men-set `HX-Redirect`, dan menangani `BindingResult`.

**Action Items (Target):**
- [x] **HtmxResponseUtility / BaseController:** Buat sebuah kelas utility atau `BaseController` yang merangkum logika response.
    *   *Contoh method:* `return HtmxUtil.redirect(response, "/inventory/uom-conversions", successMessage);`
    *   *Contoh method:* `return HtmxUtil.validationError(model, bindingResult, "fragments/alerts :: form-error");`
- [x] **Refactoring Modul Eksisting:** Terapkan utility ini pada controller yang sudah menggunakan HTMX (seperti `ProductUomConversionController` dan `StockAdjustmentController`).

## Status & Prioritas
**SELURUH TARGET SELESAI (DONE)**
- Semua fitur utama (UoM Conversion, Stock Adjustment) sudah mengikuti standar baru ini.
- Arsitektur baru ini siap digunakan sebagai referensi untuk modul-modul ERP lainnya.
