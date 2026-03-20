# Technical Specifications Index

Folder ini berisi standar teknis horizontal yang berlaku di seluruh aplikasi ERP. Developer dan AI Agent **WAJIB** mengikuti standar ini saat membuat fitur baru.

## UI & Component Standards
- **[ui-standards.md](ui-standards.md)**: Standar visual, tinggi input (32px/28px), dan class CSS ERP.
- **[autocomplete-generic.md](autocomplete-generic.md)**: Cara kerja TomSelect asinkron dan pola cascading lookup.
- **[numeric-standards.md](numeric-standards.md)**: Standar input angka dengan pemisah ribuan (AutoNumeric) dan Spring Formatter.
- **[htmx-standard.md](htmx-standard.md)**: Pola form submission AJAX menggunakan HTMX untuk menjaga state UI.

## Data & Logic Standards
- **[auditing.md](auditing.md)**: Implementasi audit trail (siapa, kapan) dan penggunaan class `BaseAuditResponse`.
- **[sequence-generator.md](sequence-generator.md)**: Aturan penomoran otomatis untuk dokumen transaksi dan master data.
- **[pagination.md](pagination.md)**: Standar paginasi dinamis berdasarkan preferensi user.
- **[sorting.md](sorting.md)**: Pola pengurutan kolom tabel otomatis terintegrasi dengan JPA.

## Localization & Navigation
- **[i18n-guide.md](i18n-guide.md)**: Konvensi penamaan kunci pesan (message keys) untuk multibahasa.
- **[menu-structure.md](menu-structure.md)**: Hirarki menu, breadcrumb, dan manajemen ikon sidebar.
- **[search-menu.md](search-menu.md)**: Cara kerja fitur pencarian menu global (Security-Aware Search).
