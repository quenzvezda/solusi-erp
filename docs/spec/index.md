# Technical Specifications Index

Folder ini berisi standar teknis horizontal yang berlaku di seluruh aplikasi ERP. Developer dan AI Agent **WAJIB** mengikuti standar ini saat membuat fitur baru.

## UI & Component Standards
- **[ui-standards.md](ui-standards.md)**: Standar visual, tinggi input (32px/28px), dan class CSS ERP.
- **[autocomplete-generic.md](autocomplete-generic.md)**: Cara kerja TomSelect asinkron dan pola cascading lookup.
- **[numeric-standards.md](numeric-standards.md)**: Standar input angka dengan pemisah ribuan (AutoNumeric), helper `ErpNumeric`, Spring Formatter, dan konsistensi precision (termasuk line dinamis/modal selector).
- **[datetime-standards.md](datetime-standards.md)**: Standar input tanggal & waktu (Flatpickr), format display vs backend (ISO 8601), `@DateTimeFormat` annotation, dan pre-fill dari database.
- **[header-lines-form.md](header-lines-form.md)**: Standar pola form Header-Lines (PO, SO, Inventory), termasuk mode line turunan dokumen vs line manual.
- **[form-submission.md](form-submission.md)**: Pola pengiriman form (Hybrid Approach: HTMX vs AJAX), urutan validasi client-side, redirect sukses, dan guard beforeunload.
- **[action-buttons.md](action-buttons.md)**: Standar tombol aksi dokumen (confirm yes/no, `ErpForm.postAction`, redirect, error handling, UX consistency).
- **[layout-standard.md](layout-standard.md)**: Standar layout utama dengan slot untuk JavaScript per-halaman.
- **[modal-selector.md](modal-selector.md)**: Pola reusable selector berbasis Bootstrap modal + HTMX untuk single-select/multi-select, termasuk exclusion query-level dan kontrak payload apply.
- **[currency-exchange-rate.md](currency-exchange-rate.md)**: Pola auto-lock exchange rate ke 1 saat default currency dipilih, termasuk backend payload dan reusable JS utility.
- **[page-specific-scripts.md](page-specific-scripts.md)**: Panduan memilih antara script inline di template dan file JavaScript khusus per halaman/fitur.

## Developer Guides (Implementation)
- **[../architecture/form-guide.md](../architecture/form-guide.md)**: Panduan praktis implementasi form dinamis (`ErpLineManager`, `ErpInventory`).

## Data & Logic Standards
- **[api-response.md](api-response.md)**: Standar respons API (`ApiResponse`) dan pembungkus data form (`FormViewDto`).
- **[auditing.md](auditing.md)**: Implementasi audit trail (siapa, kapan) dan penggunaan class `BaseAuditResponse`.
- **[sequence-generator.md](sequence-generator.md)**: Aturan penomoran otomatis untuk dokumen transaksi dan master data.
- **[pagination.md](pagination.md)**: Standar paginasi dinamis berdasarkan preferensi user.
- **[sorting.md](sorting.md)**: Pola pengurutan kolom tabel otomatis terintegrasi dengan JPA.
- **[error-handling.md](error-handling.md)**: Standar penanganan error dan navigasi otomatis (Smart Redirect).

## Localization & Navigation
- **[i18n-guide.md](i18n-guide.md)**: Konvensi penamaan kunci pesan (message keys) untuk multibahasa.
- **[menu-structure.md](menu-structure.md)**: Hirarki menu, breadcrumb, dan manajemen ikon sidebar.
- **[search-menu.md](search-menu.md)**: Cara kerja fitur pencarian menu global (Security-Aware Search).
