# ERP Menu Structure & Hierarchy

Dokumen ini mendefinisikan standar hirarki menu (Breadcrumb) yang digunakan di seluruh sistem Solusi ERP. Hirarki ini secara dinamis dirender di Sidebar dan Global Search.

---

## 1. Konvensi Penulisan
- **Pemisah**: Gunakan ` > ` (spasi-lebihdari-spasi) sebagai pemisah level.
- **Level**:
    - **Level 1 (Parent)**: Kategori besar (misal: Company Admin, Operations).
    - **Level 2 (Child)**: Pengelompokan logis/sub-modul (misal: Security, Master Data).
    - **Level 3 (Grandchild)**: Halaman fitur/transaksi (misal: Produk, Pajak).

---

## 2. Peta Menu (Current & Future)

### A. Company Admin (Sistem & Konfigurasi Dasar)
- **Security**
    - Pengguna (`SEC-01`)
    - Peran (`SEC-02`)
    - Otoritas (`SEC-03`)
    - Grup Menu (`SEC-04`)
- **Master Data**
    - Business Partner (`MST-01`)
    - Tipe Peran Partner (`MST-06`)
    - Data Geografis (`MST-02`)
- **Finance Settings**
    - Pajak (`MST-04`)
    - Mata Uang (`MST-05`)
    - Rekening Bank (`MST-03`)

### B. Operations (Inti Bisnis)
- **Inventory Setup**
    - Produk (`INV-01`)
    - Kategori Produk (`INV-02`)
    - Brand (`INV-03`)
    - Satuan Ukur (`INV-04`)
    - Fasilitas (`INV-05`)
    - Grid (`INV-06`)
    - Kontainer (`INV-07`)
- **Inventory Transactions**
    - Penyesuaian Stok (`INV-08`)
    - *Future: Goods Receipt, Goods Issue, Internal Transfer*
- **Inventory Reports**
    - Kartu Stok (`INV-09`)
    - Stok On-Hand (`INV-10`)
- **Sales & Distribution**
    - *Future: Sales Quotation, Sales Order, Delivery Planning, Delivery Order, Sales Return, Sales Report*
- **Procurement (Purchase)**
    - *Future: Purchase Requisition, Purchase Order, Purchase Return, Purchase Report*

### C. Finance & Accounting (Keuangan)
- **Account Receivable (Sales Billing)**
    - *Future: Billing, Receipt*
- **Account Payable (Purchase Invoice)**
    - *Future: Invoice, Payment*
- **General Ledger**
    - *Future: Journal Entry, Accounting Schema, Accounting Period*
- **Adjustments**
    - *Future: Debit Memo, Credit Memo, Manual Billing/Invoice*

### D. Production (Riset/Upcoming)
- *Future: To be defined*

---

## 3. Cara Update Menu
Untuk mengubah struktur menu, lakukan update pada kolom `breadcrumb_id` dan `breadcrumb_en` di tabel `permission_groups`. Sidebar akan otomatis menyesuaikan hirarkinya.

---

## 4. Multi-Language Rendering Mechanism
Sidebar dan Global Search saat ini mendukung **Dynamic Dual-Language** (ID dan EN) yang dapat berubah secara instan ketika user mengganti bahasa tanpa perlu memuat ulang data dari database.

### Arsitektur Render Menu
1. **Pembangunan Hirarki (Saat Login)**:
    - Ketika user login, `CustomAuthenticationSuccessHandler` memanggil `PermissionGroupServiceImpl.buildMenuTree()`.
    - Sistem mengambil `permission_groups` dari database, lalu melakukan *split* pada `breadcrumb_id` dan `breadcrumb_en` secara bersamaan.
    - DTO `MenuNodeResponse` yang dihasilkan akan menyimpan **kedua bahasa tersebut** (`nameId` dan `nameEn`) sekaligus.
    - *Tree* DTO utuh ini kemudian disimpan ke dalam HTTP Session atribut `userMenu`.

2. **Resolusi Bahasa Dinamis (Saat Akses Halaman)**:
    - `MenuNodeResponse` memiliki method getter kustom: `public String getName()`.
    - Di dalam view Thymeleaf (`sidebar.html`), saat merender `<span th:text="${node.name}">`, Thymeleaf secara transparan mengeksekusi method `getName()` via Spring Expression Language (SpEL).
    - Di dalam `getName()`, backend mengecek `LocaleContextHolder.getLocale().getLanguage()`.
    - Jika sesi bahasa saat itu di-set ke `"en"`, ia mereturn `nameEn`; jika tidak, `nameId`.
    
**Kesimpulan**: 
Caching dilakukan pada level *Dual-Language Object*, dan penentuan bahasa dievaluasi pada runtime (saat HTML selesai dikompilasi). Hal ini memberikan UX penggantian bahasa secara instan (0 ping ke Database) sembari mempertahankan keunggulan performa Caching di dalam memori Sesi.

---

## 5. Parent Menu Icons (Folder Icons)
Menu utama tingkat atas (seperti *Company Admin, Operations, Security*) tidak benar-benar eksis sebagai entri terpisah (baris) di tabel `permission_groups`. Mereka di-generate secara *on-the-fly* dari hasil pemotongan (*split*) string `breadcrumb`.

Karena menu parent tidak tersimpan di database, mereka tidak memiliki kolom `icon_class`. Untuk menyelesaikan isu ini tanpa membuat relasi database *Parent-Child Recursive* yang memperberat query:
1. Kita me-mapping *Ikon kustom* secara Hardcode lewat sebuah struktur Map **`PARENT_ICONS`** di dalam `PermissionGroupServiceImpl.java`.
2. Map Kamus (*Dictionary*) ini menggunakan **English Breadcrumb Name** (cth: "Company Admin", "Security") sebagai Key (*Kata Kunci Pencarian*), dan nama kelas Tabler Icon memanjang (cth: "ti-building-skyscraper") sebagai Nilai (*Value*).
3. Penggunaan *English Name* sebagai Key **DIWAJIBKAN** karena hal tersebut menjamin bahwa nama rujukan bersifat absolut/statis, tanpa peduli apakah sesi browser user saat itu sedang berbahasa Indonesia atau Inggris.
4. **Agentic/Developer Instruction**: Jika di masa depan Modul baru ditambahkan (Misal: "Human Resources"), developer atau AI Assistant **DIWAJIBKAN** membuka `PermissionGroupServiceImpl.java` dan menambahkan mapping ikon baru pada `PARENT_ICONS` map. Jika tidak, folder parent baru tersebut akan otomatis ber-ikon generik `ti-folder`.
