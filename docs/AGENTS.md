# AGENTS.md / Project Architecture & AI Guidelines

> **PENTING**: Sebelum task apa pun, baca **[Documentation Index](index.md)** untuk melihat peta lengkap dokumentasi teknis dan fungsional proyek ini.

## 1. Project Overview
Proyek ini adalah sistem Enterprise Resource Planning (ERP) Monolitik yang dibangun untuk tujuan pembelajaran dan MVP. Aplikasi dirender sepenuhnya di sisi server (Server-Side Rendering/SSR) tanpa memisahkan frontend sebagai Single Page Application (SPA).

## 2. Tech Stack & Versions (Strictly Enforced)
AI Assistant WAJIB mematuhi versi dan teknologi berikut berdasarkan `pom.xml` utama. DILARANG menyarankan alternatif di luar *stack* ini:
* **Language:** Java 21 (LTS)
* **Framework:** Spring Boot 4.0.3 (Gunakan sintaks dan standar terbaru yang relevan dengan versi 4.x)
* **Build Tool:** Maven
* **Database:** MariaDB (`mariadb-java-client`)
* **ORM:** Spring Data JPA (Hibernate)
* **Database Migration:** Flyway (`flyway-mysql`). DILARANG keras menggunakan `spring.jpa.hibernate.ddl-auto=update` di *production*.
* **Security:** Spring Security 6+ (Stateful / Session-based). DILARANG menggunakan JWT.
* **Frontend Template Engine:** Thymeleaf (Gunakan **Native Thymeleaf Fragments** `th:fragment`, `th:replace`). DILARANG menggunakan `thymeleaf-layout-dialect` karena masalah stabilitas dengan Spring Boot 4.
* **UI/CSS Framework:** Bootstrap 5
* **Admin Template:** Tabler (MIT License) - Gunakan kelas dan struktur HTML bawaan Tabler.
* **AJAX & Fragments:** Hybrid Approach (**AJAX/JSON** untuk Form CRUD, **HTMX** untuk Search/Filter/Pagination).
* **Magic Routing:** HtmxViewInterceptor (Otomatis menangani fragmen berdasarkan `HX-Target`).
* **Numeric Formatting:** AutoNumeric (Untuk ribuan separator & desimal).
* **Boilerplate Reduction:** Lombok & ApiResponse.
* **Mapping:** MapStruct (dengan aturan mengabaikan `id` pada update).
* **Validation:** Hibernate Validator (`spring-boot-starter-validation`).
* **Reporting:** Apache POI (Excel) & JasperReports (PDF).

## 3. Architecture & Coding Standards
*   **Package Structure:** Gunakan **Package by Feature/Module**. 
*   **Advanced Architecture (Standard):** **Seluruh modul utama** menggunakan **Pure Clean Architecture + DDD + CQRS** dengan pendekatan vertical slicing per feature. Ini adalah standar wajib untuk semua pengembangan baru. Lihat detailnya di [docs/architecture/clean-ddd-cqrs-standard.md](docs/architecture/clean-ddd-cqrs-standard.md). Modul referensi yang stabil dan lengkap: `master.tax`, `master.currency`, `security.permissiongroup`, `security.role`, `inventory.brand`.
*   **DTO (Data Transfer Object):** 
    * JANGAN PERNAH mengirimkan JPA Entity secara langsung ke Thymeleaf (Controller to View).
    * JANGAN PERNAH menerima form submission langsung ke JPA Entity.
    * Selalu gunakan DTO untuk *request* (form) dan *response* (view), dan gunakan MapStruct untuk *mapping*.
    * **Standard DTO Inheritance**: Semua Request dan Response DTO **WAJIB** meng-extend `BaseAuditResponse` agar metadata ID, Version, dan Audit Trace otomatis terkelola dan tampil di UI secara seragam.
* **UI Performance Standards:**
    * **CDN usage:** Gunakan JSDelivr (@latest) untuk semua library eksternal (Tabler Core, Icons, ApexCharts) untuk memaksimalkan caching.
    * **Compression:** Pastikan Gzip compression aktif di `application.yaml` untuk tipe file text, css, js, json, dan woff2.
    * **Optimization:** Gunakan `<link rel="dns-prefetch">` dan `preconnect` untuk `cdn.jsdelivr.net` dan `rsms.me`.
* **UI/UX Design Patterns:**
    * **Grouping & Collapsible:** Untuk daftar yang sangat panjang (seperti Permission pada Role Form atau List Permission), WAJIB dikelompokkan berdasarkan modul/fitur menggunakan kartu yang dapat ditutup-buka (*collapsible*).
    * **Visual Feedback:** Gunakan perubahan warna header (misal: biru solid saat aktif) dan counter real-time (misal: 2/4 selected) untuk memberikan konteks pada user.
* **Frontend Approach:**
    * Gunakan **Native Thymeleaf Fragments**. Buat satu file master (misal: `layout/master.html`) yang mendefinisikan fragmen kerangka utama.
    * Halaman spesifik memanggil fragmen tersebut menggunakan `th:replace="~{layout/master :: layout(~{:: .content})}"`.
    * Pastikan setiap halaman modular dan hanya mengirimkan fragmen konten ke dalam master.

## 4. Data Modeling & Auditing (BaseModel)
* **BaseModel:** Semua entitas bisnis WAJIB *extends* sebuah kelas abstrak `BaseModel` (menggunakan `@MappedSuperclass`).
* **Audit Fields:** `BaseModel` harus memiliki field:
    * `createdBy` (String/Long) dengan anotasi `@CreatedBy`
    * `createdDate` (LocalDateTime) dengan anotasi `@CreatedDate`
    * `updatedBy` (String/Long) dengan anotasi `@LastModifiedBy`
    * `updatedDate` (LocalDateTime) dengan anotasi `@LastModifiedDate`
    * `version` (Integer/Long) dengan anotasi `@Version` untuk *optimistic locking* (default 1).
* **Data Retention (Soft Delete):** DILARANG menggunakan *Hard Delete* untuk data master. Selalu gunakan flag `isActive` (boolean/tinyint) untuk mengatur status aktif/tidak aktif. Hindari menggunakan `@Where` global (Hibernate) jika masih perlu melihat data historis, filter `isActive = true` secara eksplisit di level Repository.
* AI WAJIB memastikan Spring Data JPA Auditing aktif (`@EnableJpaAuditing` dan bean `AuditorAware` terkonfigurasi).

## 5. Business Module Standards
Setiap modul bisnis baru (Inventory, Sales, Purchasing, dll) WAJIB mengikuti pola berikut:
*   **Pagination (Mandatory)**: Selalu gunakan `Page<T>` dari Spring Data JPA pada level Service dan Controller.
    *   **Automated Resolver**: Gunakan parameter `Pageable pageable` langsung di method Controller. 
    *   Sistem secara otomatis akan meresolve `pageSize` berdasarkan preferensi `UserProfile.defaultPageSize`.
    *   Detail teknis silakan merujuk ke [docs/spec/pagination.md](spec/pagination.md).
    *   **Frontend UI (Thymeleaf)**: Tampilkan pagination menggunakan *generic fragment* `<div th:replace="~{fragments/table :: pagination(${page})}"></div>`. Fragment ini otomatis menangani *page windowing*, navigasi lengkap (First/Last), dan *retention* parameter URL (termasuk *search* dan *sorting*).
    *   DILARANG keras menulis struktur HTML `<ul class="pagination">` secara manual di setiap halaman list.
*   **Sorting (Mandatory)**: Setiap list view WAJIB mendukung pengurutan kolom menggunakan `TableSortingAdvice`.
    *   Gunakan fragment generic `th:replace="~{fragments/table :: sortable('fieldName', #{label})}"`.
    *   Sistem secara otomatis menangani *state* pengurutan dan indikator visual (icons).
*   **Search (Mandatory)**: Setiap list view WAJIB memiliki fitur pencarian minimal pada 1-2 kolom utama (misal: Code, Name).
*   **Code Auto-Generation**: Field `code` (95% modul) WAJIB di-generate oleh `SequenceGeneratorService`.
    *   Detail teknis dan pattern silakan merujuk ke [docs/spec/sequence-generator.md](spec/sequence-generator.md).
    *   DILARANG menginput kode manual di form `create`.
    *   UI field `code` WAJIB diset `readonly` dan `bg-light`.
*   **Collection Validation**: Jika entitas memiliki *nested collection* (seperti `contacts`, `addresses`) yang dilengkapi flag `isDefault`, pastikan membuat validasi backend (toleransi maksimal 1 data default) dan validasi frontend (menggunakan *radio button*).
* **i18n Implementation**: 
    *   **Tool Usage (CRITICAL)**: DILARANG menggunakan `echo` untuk menambah entry i18n. Gunakan `replace` tool dengan mengikuti protokol di [docs/spec/i18n-guide.md](spec/i18n-guide.md#7-ai-guidelines-for-updating-i18n-files-critical).
    *   Semua pesan error di Service (yang dilempar via `RuntimeException`) WAJIB di-resolve menggunakan `MessageSource` agar mendukung multi-bahasa.
    *   Gunakan helper method `private String getMessage(String key)` di setiap Service Implementation.
*   **Global Error Handling & Navigation**:
    *   Gunakan anotasi `@DefaultRedirectUrl` pada level class Controller untuk mengaktifkan fitur Smart Redirect.
    *   Fitur ini akan secara otomatis mengalihkan pengguna kembali ke halaman daftar jika terjadi error pada request `GET` di halaman form.
    *   DILARANG menulis blok `try-catch` manual di Controller hanya untuk melakukan redirect navigasi; percayakan pada `GlobalExceptionHandler`.

## 6. UI/UX Lookup Standardization
Untuk menjaga estetika dan konsistensi tampilan pada elemen autocomplete (TomSelect), AI dan Developer WAJIB mengikuti standar berikut:
*   **Field `name` (Primary Text)**: Selalu gunakan **Nama** saja (contoh: `p.getName()`). DILARANG menggabungkan Code ke dalam field name (seperti `Code - Name`) karena akan membuat teks terlalu panjang.
*   **Field `subText` (Secondary Text)**: Gunakan **Code** (contoh: `p.getCode()`). Field ini akan tampil otomatis di bawah nama pada dropdown pencarian.
*   **Display Label Persistence**: Setiap Request DTO yang digunakan untuk form Edit **WAJIB** memiliki field tambahan untuk menampung `Name` dan `Code` (contoh: `brandName`, `brandCode`) agar saat halaman di-load ulang (HTMX), label pada Autocomplete tidak hilang.
*   **Thymeleaf Fragments**: Selalu gunakan parameter `initialValue`, `initialText`, dan `initialSubtext` saat memanggil fragment `autocomplete`.

## 7. Security & RBAC (Role-Based Access Control)
Sistem otorisasi menggunakan model **Fine-Grained Authority (Privilege-Based)**.
* **Database Entities:** Harus terdiri dari `User`, `Role`, dan `Permission` (Authority).
* **Mapping:** 1 User memiliki 1 Role. 1 Role memiliki banyak Permission (Many-to-Many).
* **Naming Convention:** 
    * Nama modul multi-kata menggunakan **Dash** (`-`). Contoh: `SALES-ORDER`.
    * Pemisah Modul dan Aksi menggunakan **Underscore** (`_`). Contoh: `READ`, `CREATE`, `UPDATE`, `DELETE`.
    * Format Lengkap: `[MODUL-NAME]_[ACTION]`. Contoh: `SALES-ORDER_READ`.
* **Smart Sidebar Logic:** Menu induk (parent) DILARANG menggunakan permission tunggal (seperti `SECURITY_READ`). Gunakan `sec:authorize="hasAnyAuthority('CHILD_1_READ', 'CHILD_2_READ')"` agar menu induk otomatis muncul jika user punya akses ke salah satu anaknya.
* **Backend Guard:** Gunakan anotasi `@PreAuthorize("hasAuthority('NAMA_PERMISSION')")` di setiap *method* Controller. DILARANG menggunakan `hasRole()`.
* **Frontend Guard:** Gunakan `sec:authorize="hasAuthority('...')"` dari library `thymeleaf-extras-springsecurity6`.
* **SecurityUser Implementation:** Gunakan class `SecurityUser` yang mengimplementasikan `UserDetails` dan **WAJIB** melakukan *pre-calculate* authorities di constructor untuk menghindari `LazyInitializationException` atau *detachment* saat UI merender izin.
* **Permission Batching:** Gunakan fitur generator untuk mempercepat pembuatan set standar (READ, CREATE, UPDATE, DELETE) untuk setiap modul baru.
* **SQL Wildcard Safety (CRITICAL):** Saat melakukan *seeding* permission di Flyway (terutama saat `INSERT INTO role_permissions`), gunakan `ESCAPE` jika nama modul merupakan awalan dari modul lain (contoh: `PRODUCT` dan `PRODUCT-CATEGORY`).
    * SALAH: `LIKE 'PRODUCT_%'` (akan mencocokkan `PRODUCT-CATEGORY` karena `-` dianggap satu karakter oleh `_`).
    * BENAR: `LIKE 'PRODUCT\_%' ESCAPE '\\'`.

## 8. Internationalization (i18n)
Sistem ini menggunakan mekanisme internasionalisasi dinamis untuk mendukung multi-bahasa (default: `id`, `en`).
* **Central Configuration (CRITICAL FOR AI):** SELURUH konfigurasi bahasa utama (LocaleResolver, LocaleChangeInterceptor) SUDAH TERPUSAT di `com.solusi.erp.config.I18nConfig.java`. Integrasi validasi Spring (`LocalValidatorFactoryBean`) ada di `WebMvcConfig.java`. 
    *   **DILARANG KERAS** membuat Bean `localeResolver` baru atau menggandakannya di file konfigurasi lain (seperti `WebMvcConfig.java`). Ini akan memicu `BeanDefinitionOverrideException`.
* **Storage:** Menggunakan **Cookie-based Locale Resolver** (cookie name: `lang`) agar preferensi bertahan selama 30 hari di browser meskipun session berakhir.
* **Smart Synchronization:** Saat login sukses, `CustomAuthenticationSuccessHandler` melakukan sinkronisasi dua arah:
    1. Jika user sudah memilih bahasa di landing page (Cookie ada), maka database (`UserProfile`) otomatis diupdate mengikuti pilihan browser.
    2. Jika Cookie kosong/default, maka preferensi dari database disetel ke browser.
* **Thymeleaf Implementation:** DILARANG melakukan hardcoding teks statis. Selalu gunakan operator `#{key.pesan}`.
* **Naming Convention:** Ikuti panduan penamaan kunci di [docs/spec/i18n-guide.md](spec/i18n-guide.md) untuk menjaga konsistensi.

## 7. Exception Handling & Error Pages
* **Global Handler:** Gunakan `@ControllerAdvice` untuk menangkap *exception* (seperti 403 Forbidden, 404 Not Found, 500 Internal Server Error, dan `MethodArgumentNotValidException` untuk validasi form).
* **Custom Error Views:** Arahkan *error* tersebut ke halaman khusus Thymeleaf (misal: `error/404.html`, `error/403.html`) yang sudah di-styling menggunakan UI Tabler agar menyatu dengan tema ERP. Jangan gunakan *Whitelabel Error Page* bawaan Spring Boot.

## 9. Agent Instructions (How to Assist)
Saat menghasilkan kode:
1.  **Fokus pada Backend & Integrasi Thymeleaf:** Tulis kode Java yang bersih dan berikan contoh HTML Thymeleaf yang mengimplementasikan class Bootstrap/Tabler secara langsung.
2.  **Berikan Kode Lengkap:** Jika membuat sebuah DTO atau Controller, sertakan seluruh import, anotasi, dan field yang diperlukan secara utuh.
3.  **Form Input Standards (Critical):** 
    *   **Numeric Inputs**: Ikuti [docs/spec/numeric-standards.md](docs/spec/numeric-standards.md) untuk input dengan pemisah ribuan.
    *   **Date/Time Inputs**: Ikuti [docs/spec/datetime-standards.md](docs/spec/datetime-standards.md) untuk input tanggal dan waktu. Wajib: (a) tambahkan `data-picker="datetime|date|time"` pada HTML input, (b) tambahkan `@DateTimeFormat(pattern = "...")` pada DTO field.
4.  **Versioning & Commit Protocol:**
    *   Jika User meminta Agent untuk melakukan `commit`, Agent **WAJIB** memeriksa apakah ada perubahan versi di `pom.xml` dibandingkan dengan *commit* terakhir.
    *   Jika versi belum naik, Agent **HARUS** mengingatkan User untuk menaikkan versi terlebih dahulu dan menyarankan kenaikan berdasarkan prinsip **Semantic Versioning (SemVer)**:
        *   **Patch (0.0.x):** Untuk perbaikan bug kecil atau optimasi tanpa fitur baru.
        *   **Minor (0.x.0):** Untuk penambahan fitur baru yang tidak merusak kompatibilitas (misal: modul baru).
        *   **Major (x.0.0):** Untuk perubahan besar/arsitektural yang tidak kompatibel dengan versi sebelumnya.
5.  **Strategic replace Tool Usage:** The replace tool requires an exact literal match for `old_string` and is highly sensitive to whitespace. Avoid replacing large, complex blocks of code. Prefer smaller, more targeted replacements. Always re-read the target file immediately before executing a replace command to ensure the `old_string` is based on the file's current content.
6.  **Wajib Membaca Referensi & Contoh Eksisting:** DILARANG keras berasumsi tentang komponen UI, spesifikasi teknis, atau fitur bisnis yang sudah ada. Jika tugas berkaitan dengan modul baru/lama, AI **WAJIB** membaca dokumen spesifikasi teknis di direktori `docs/spec/` dan dokumentasi proses bisnis di `docs/modules/`. AI **DISARANKAN KUAT** untuk memeriksa *source code* serupa yang sudah stabil (seperti `Product` atau `Tax`) sebagai template *best practice* sebelum membuat kode.

## 9.A Semantic Versioning Automation (WAJIB DITERAPKAN)
Untuk menjaga konsistensi versioning, setiap agent **WAJIB** mengikuti protokol berikut:

1. **Analisis konteks perubahan** untuk menentukan jenis bump:
   * **MAJOR (x.0.0)**: Breaking API changes, perubahan arsitektural fundamental, atau perubahan flow bisnis yang signifikan
   * **MINOR (0.x.0)**: Penambahan fitur baru, modul baru, atau enhancement yang kompatibel backward
   * **PATCH (0.0.x)**: Bug fixes, optimasi performa, refactoring kecil, atau perbaikan dokumentasi
2. **Update `pom.xml` version** hanya setelah task diterima:
   * **Feature / refactor / optimize / test**: setelah implementasi + testing sukses
   * **Bug fix**: setelah user manual verification lolos
3. **Version format** tetap mengikuti **Semantic Versioning 2.0.0**:
   * `MAJOR.MINOR.PATCH`
   * Contoh: `1.0.0` → `1.1.0` → `1.1.1` → `2.0.0`

## 9.B Git Workflow & Commit Suggestions
- Commit timing dikendalikan oleh generator prompt helper.
- Jika commit suggestion diminta, gunakan **Conventional Commits** dalam bahasa Inggris dan jangan tambahkan trailer `Co-authored-by`.
- Jangan melakukan `git push` atau membuat PR kecuali diminta secara eksplisit.
- Jangan memakai destructive git commands seperti `reset --hard`, `checkout --`, atau `force push`.

## 10. Playwright Smoke Test & Frontend Debugging

Untuk smoke test end-to-end atau debugging frontend, gunakan MCP Playwright. AI **WAJIB** menjalankan Spring Boot server secara mandiri — **DILARANG** meminta user untuk menjalankan server.

> **Panduan lengkap:** Baca **[docs/tests/playwright-smoke-test-guide.md](tests/playwright-smoke-test-guide.md)** untuk cara start server (termasuk Windows PowerShell), kredensial dev, kamus interaksi komponen (TomSelect, Flatpickr, AutoNumeric, Line Items, Serial Number Drawer), dan troubleshooting.

## 11. Cold Start Strategy (Initial Setup)
Untuk menjamin keamanan dan sinkronisasi enkripsi:
*   **Seeder SQL**: Menggunakan placeholder `INITIAL_PASSWORD_SETUP` untuk password admin pertama.
*   **SystemInitializer (Java)**: Sebuah `CommandLineRunner` yang mendeteksi placeholder tersebut dan menggantinya dengan hash BCrypt yang valid untuk password **`admin123`** saat aplikasi pertama kali dijalankan.
*   **Force Reset**: Semua user baru (termasuk admin) wajib memiliki flag `password_change_required = true` di database.

## 12. Security & Role Permissions
Aplikasi ini memiliki UI dinamis untuk Manajemen Role (Grouped Permissions) yang secara otomatis akan mengelompokkan daftar _permission_ ke dalam sebuah Folder berdasarkan **kata pertama sebelum underscore (`_`)**. Oleh sebab itu, konvensi penamaan permission sangatlah penting:
1. **Modul Utama (CRUD)**: Gunakan format `[NAMA_MODUL]_[AKSI]`. 
   Contoh: `GEOGRAPHIC_READ`, `PRODUCT_CREATE`. Ini akan mengelompokkan mereka ke folder `GEOGRAPHIC` dan `PRODUCT`.
2. **Fitur Lintas Modul (Shared Features)**: Gunakan *Prefix* jenis fiturnya, contohnya `LOOKUP_` untuk autocomplete popup, dan `POPUP_` untuk fitur modal/popup lainnya (misal: Popup selector item di transaksi).
   Contoh: `LOOKUP_GEOGRAPHIC`, `LOOKUP_PRODUCT`, `POPUP_PARTNER`. Ini akan membuat folder `LOOKUP` dan `POPUP` yang bersih dan mudah diatur oleh Administrator di UI tanpa mencampuri izin akses CRUD reguler.

## 13. Global Search Menu & Permission Groups
Selain pengelompokan visual di UI Role, sistem memiliki fitur **Global Search Menu** yang menggunakan entitas `PermissionGroup`.
- Setiap `Permission` **WAJIB** dikaitkan dengan satu `PermissionGroup` agar modul tersebut dapat muncul di hasil pencarian navbar (jika user punya akses).
- Detail teknis silakan merujuk ke [docs/spec/search-menu.md](spec/search-menu.md) dan [docs/modules/security/permission-groups.md](modules/security/permission-groups.md).
