## USER 🧑‍💻

<session_context>
This is the Gemini CLI. We are setting up the context for our chat.
Today's date is Tuesday, March 24, 2026 (formatted according to the user's locale).
My operating system is: win32
The project's temporary directory is: C:\Users\quenz\.gemini\tmp\solusi-program-erp
- **Workspace Directories:**
  - F:\solusi-program-erp
  - F:\solusi-program-erp\conductor
- **Directory Structure:**

Showing up to 200 items (files + folders). Folders or files indicated with ... contain more items not shown, were ignored, or the display limit (200 items) was reached.

F:\solusi-program-erp\
├───.env.example
├───.gitattributes
├───.gitignore
├───commit.txt
├───mvnw
├───mvnw.cmd
├───pom.xml
├───pr.txt
├───refactor-plan.md
├───theme-standardization-plan.md
├───.git\...
├───.github\
│   └───workflows\
│       └───deploy.yml
├───.idea\...
├───.mvn\
│   └───wrapper\
│       └───maven-wrapper.properties
├───.run\
│   ├───Alpha.run.xml
│   ├───Dev.run.xml
│   ├───flyway_clean (dev).run.xml
│   ├───mvn clean compile.run.xml
│   └───mvn clean test.run.xml
├───.vscode\
│   ├───launch.json.example
│   └───tasks.json
├───${project.build.directory}\
│   └───classes\
│       ├───db\
│       │   └───migration\
│       ├───static\
│       │   ├───css\
│       │   ├───img\
│       │   └───js\
│       └───templates\
│           ├───dashboard\
│           ├───error\
│           ├───fragments\
│           ├───inventory\
│           │   ├───adjustments\
│           │   ├───brands\
│           │   ├───containers\
│           │   ├───facilities\
│           │   ├───grids\
│           │   ├───product-categories\
│           │   ├───products\
│           │   ├───reports\
│           │   │   ├───on-hand\
│           │   │   └───stock-card\
│           │   ├───unit-of-measures\
│           │   └───uom-conversions\
│           ├───layout\
│           ├───master\
│           │   ├───bank-accounts\
│           │   ├───currency\
│           │   ├───geographic\
│           │   ├───parties\
│           │   ├───party-role-types\
│           │   └───tax\
│           └───security\
│               ├───permission-groups\
│               ├───permissions\
│               ├───profile\
│               ├───roles\
│               └───users\
├───conductor\
│   ├───index.md
│   ├───product-guidelines.md
│   ├───product.md
│   ├───tech-stack.md
│   ├───tracks.md
│   ├───workflow.md
│   └───code_styleguides\
│       ├───html-css.md
│       └───java.md
├───docs\
│   ├───AGENTS.md
│   ├───index.md
│   ├───architecture\
│   │   ├───base-model-pattern.md
│   │   ├───form-guide.md
│   │   ├───party-class-diagram.md
│   │   └───security-class-diagram.md
│   ├───database\
│   │   ├───dev-seeder.sql
│   │   ├───inventory-erd.md
│   │   ├───party-erd.md
│   │   └───security-erd.md
│   ├───modules\
│   │   ├───inventory\
│   │   │   ├───stock-adjustment.md
│   │   │   ├───stock-utility.md
│   │   │   ├───valuation-and-uom.md
│   │   │   └───warehouse-hierarchy.md
│   │   ├───master\
│   │   │   ├───bankaccount.md
│   │   │   ├───business-partner.md
│   │   │   ├───currency.md
│   │   │   ├───geographic.md
│   │   │   └───tax.md
│   │   └───security\
│   │       └───permission-groups.md
│   ├───roadmap\
│   │   ├───accounting-module.md
│   │   ├───inventory-module.md
│   │   ├───procurement-module.md
│   │   └───sales-module.md
│   └───spec\
│       ├───api-response.md
│       ├───auditing.md
│       ├───autocomplete-generic.md
│       ├───error-handling.md
│       ├───form-submission.md
│       ├───header-lines-form.md
│       ├───i18n-guide.md
│       ├───index.md
│       ├───layout-standard.md
│       ├───menu-structure.md
│       ├───numeric-standards.md
│       ├───pagination.md
│       ├───search-menu.md
│       ├───sequence-generator.md
│       ├───sorting.md
│       └───ui-standards.md
├───src\
│   ├───main\
│   │   ├───java\
│   │   │   └───com\
│   │   │       └───solusi\
│   │   │           └───erp\
│   │   └───resources\
│   │       ├───application.yaml
│   │       ├───messages_id.properties
│   │       ├───messages.properties
│   │       ├───rebel.xml
│   │       ├───db\
│   │       │   └───migration\
│   │       │       ├───V1__Initial_Security_Schema.sql
│   │       │       ├───V10__Master_Tax.sql
│   │       │       ├───V11__Master_Currency.sql
│   │       │       ├───V12__Master_Geographic.sql
│   │       │       ├───V13__Refactor_Party_Address_Geographic.sql
│   │       │       ├───V14__LOOKUP_GEOGRAPHIC_Permission.sql
│   │       │       ├───V15__Master_Geographic_Indonesia_Data.sql
│   │       │       ├───V16__Master_Bank_Account.sql
│   │       │       ├───V17__Refactor_Audit_Columns_To_User_FK.sql
│   │       │       ├───V18__Update_System_Sequences_Audit.sql
│   │       │       ├───V19__Add_Localized_Permission_Groups.sql
│   │       │       ├───V2__Seed_Security_Data.sql
│   │       │       ├───V20__Add_Icon_To_Permission_Groups.sql
│   │       │       ├───V21__Inventory_Warehouse_Hierarchy.sql
│   │       │       ├───V22__Add_Warehouse_Hierarchy_Permissions.sql
│   │       │       ├───V23__Inventory_Core_Valuation_And_Movements.sql
│   │       │       ├───V24__Inventory_Stock_Adjustment_And_Menu.sql
│   │       │       ├───V25__Add_Product_Uom_Conversion_Permissions.sql
│   │       │       ├───V26__Add_Uom_And_Factor_To_Adjustment_Line.sql
│   │       │       ├───V27__add_party_to_user.sql
│   │       │       ├───V3__Inventory_Product_Category.sql
│   │       │       ├───V4__System_Sequence_Generator.sql
│   │       │       ├───V5__Inventory_Unit_Of_Measure.sql
│   │       │       ├───V6__Inventory_Brand.sql
│   │       │       ├───V7__Inventory_Product.sql
│   │       │       ├───V8__Master_Business_Partner.sql
│   │       │       └───V9__Party_Redesign.sql
│   │       ├───static\
│   │       │   ├───favicon.ico
│   │       │   ├───css\
│   │       │   │   ├───global.css
│   │       │   │   └───tomselect-custom.css
│   │       │   ├───img\
│   │       │   │   ├───logo-only.png
│   │       │   │   ├───logo.png
│   │       │   │   ├───avatars\
│   │       │   │   └───illustrations\
│   │       │   ├───js\
│   │       │   │   ├───erp-common-handler.js
│   │       │   │   └───erp-form-handler.js
│   │       │   └───libs\
│   │       └───templates\
│   │           ├───error.html
│   │           ├───home.html
│   │           ├───dashboard\
│   │           ├───error\
│   │           ├───fragments\
│   │           ├───inventory\
│   │           ├───layout\
│   │           ├───master\
│   │           └───security\
│   └───test\
│       └───java\
│           └───com\
│               └───solusi\
├───target\
│   ├───classes\...
│   ├───generated-sources\...
│   ├───generated-test-sources\...
│   ├───maven-status\...
│   └───test-classes\...
└───tmp\
Showing up to 200 items (files + folders).

F:\solusi-program-erp\conductor\
├───index.md
├───product-guidelines.md
├───product.md
├───tech-stack.md
├───tracks.md
├───workflow.md
└───code_styleguides\
    ├───html-css.md
    └───java.md


</session_context>

---

## USER 🧑‍💻

Here is the user's editor context as a JSON object. This is for your information only.
```json
{
  "activeFile": {
    "path": "F:/solusi-program-erp/commit.txt",
    "cursor": {
      "line": 8,
      "character": 0
    }
  },
  "otherOpenFiles": [
    "F:/solusi-program-erp/src/main/resources/static/js/erp-common-handler.js",
    "F:/solusi-program-erp/docs/spec/index.md",
    "F:/solusi-program-erp/src/main/resources/messages.properties",
    "F:/solusi-program-erp/src/main/resources/messages_id.properties",
    "F:/solusi-program-erp/src/main/resources/templates/security/users/form.html"
  ]
}
```

---

## USER 🧑‍💻

@docs/AGENTS.md @docs/index.md @docs/spec/index.md 

anda bisa pelajari dulu project project saya untuk menjawab pertanyaan saya soal sistem Approver/Approval

Jadi saya mau implementasi sistem approval yang generic. Maksudnya generic itu modelnya bisa di implementasikan di model manapun dengan mudah tanpa terlalu banyak refactor sistem yang sudah ada.

misalnya contoh. kan kita sudah ada fitur @docs/modules/inventory/stock-adjustment.md 
kalau sekarang karena belum ada approver caranya adalah dengan membuat tombol Proces Adjustment, ketika di proses maka stock masuk ke gudang alias di verifikasi. nah nanti jika sistem approvernya sudah ada kita bisa ganti Proces Adjustment ini menjadi approval dengan mudah di sisi Backend maupun FrontEnd (UInya juga harus generic disetiap halaman, agar developer dan user tidak bingung setiap halaman beda-beda).

Untuk detail dari sistem approvalnya itu sendiri saya kepikiran ada atribut Party sebagai orang yang harus melakukan approve, tanggal approve (dengan jam), Log history dan setiap historynya itu ada tipe actionnya apakah REQUESTED (ini ketika approval pertama kali dibuat) APPROVE AND FINISH (ini ketika proses approval disetujui dan diselesaikan), FOWARD (ini untuk memindahkan proses approval ke Party lain tapi), REJECTED (permitaan ditolak), APPROVE AND FORWARD ini disetujui oleh approver tapi tidak finish dilempar lagi ke orang approver (party lain).

saya baru kebayang sistemnya kayak begini. nah saya butuh teman diskusi agar sistem approvalnya fleksible, kaya akan fitur (cukup untuk standard ERP profesional), dan mudah di maintain dan implementasi pada existing feature
--- Content from referenced files ---
Content from @docs/AGENTS.md:
# AGENTS.md / Project Architecture & AI Guidelines

> **PENTING**: Jika Anda baru atau sedang melakukan troubleshooting, harap baca **[Documentation Index](index.md)** untuk melihat peta lengkap dokumentasi teknis dan fungsional proyek ini.

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
* **Package Structure:** Gunakan **Package by Feature/Module** (Contoh: `com.solusi.erp.inventory`, `com.solusi.erp.sales`, `com.solusi.erp.security`). Di dalam setiap modul tersebut baru terdapat `controller`, `service`, `repository`, `entity`, dan `dto`.
* **Data Transfer Object (DTO):** 
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

## 7. Internationalization (i18n)
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

## 8. Agent Instructions (How to Assist)
Saat menghasilkan kode:
1.  **Fokus pada Backend & Integrasi Thymeleaf:** Tulis kode Java yang bersih dan berikan contoh HTML Thymeleaf yang mengimplementasikan class Bootstrap/Tabler secara langsung.
2.  **Berikan Kode Lengkap:** Jika membuat sebuah DTO atau Controller, sertakan seluruh import, anotasi, dan field yang diperlukan secara utuh.
3.  **Versioning & Commit Protocol:**
    *   Jika User meminta Agent untuk melakukan `commit`, Agent **WAJIB** memeriksa apakah ada perubahan versi di `pom.xml` dibandingkan dengan *commit* terakhir.
    *   Jika versi belum naik, Agent **HARUS** mengingatkan User untuk menaikkan versi terlebih dahulu dan menyarankan kenaikan berdasarkan prinsip **Semantic Versioning (SemVer)**:
        *   **Patch (0.0.x):** Untuk perbaikan bug kecil atau optimasi tanpa fitur baru.
        *   **Minor (0.x.0):** Untuk penambahan fitur baru yang tidak merusak kompatibilitas (misal: modul baru).
        *   **Major (x.0.0):** Untuk perubahan besar/arsitektural yang tidak kompatibel dengan versi sebelumnya.
4.  **Strategic replace Tool Usage:** The replace tool requires an exact literal match for `old_string` and is highly sensitive to whitespace. Avoid replacing large, complex blocks of code. Prefer smaller, more targeted replacements. Always re-read the target file immediately before executing a replace command to ensure the `old_string` is based on the file's current content.
5.  **Wajib Membaca Referensi & Contoh Eksisting:** DILARANG keras berasumsi tentang komponen UI, spesifikasi teknis, atau fitur bisnis yang sudah ada. Jika tugas berkaitan dengan modul baru/lama, AI **WAJIB** membaca dokumen spesifikasi teknis di direktori `docs/spec/` dan dokumentasi proses bisnis di `docs/modules/`. AI **DISARANKAN KUAT** untuk memeriksa *source code* serupa yang sudah stabil (seperti `Product` atau `Tax`) sebagai template *best practice* sebelum membuat kode.

## 9. Cold Start Strategy (Initial Setup)
Untuk menjamin keamanan dan sinkronisasi enkripsi:
*   **Seeder SQL**: Menggunakan placeholder `INITIAL_PASSWORD_SETUP` untuk password admin pertama.
*   **SystemInitializer (Java)**: Sebuah `CommandLineRunner` yang mendeteksi placeholder tersebut dan menggantinya dengan hash BCrypt yang valid untuk password **`admin123`** saat aplikasi pertama kali dijalankan.
*   **Force Reset**: Semua user baru (termasuk admin) wajib memiliki flag `password_change_required = true` di database.

## 10. Understanding Documentation Structure
Sistem ini menggunakan folder `docs/` terstruktur agar AI dan Developer dapat menemukan konteks secara mandiri. AI diharapkan inisiatif membuka dan membaca direktori ini jika kekurangan konteks:
*   **`docs/architecture/`**: Visualisasi dan arsitektur klasifikasi (Class Diagram, Usecases, dll).
*   **`docs/database/`**: Dokumentasi ERD dan definisi relasi antar identitas database ERP yang kompleks.
*   **`docs/modules/`**: **[SANGAT PENTING UNTUK AI]** Penjelasan setiap Modul/Fitur bisnis spesifik (misal: `modules/master/tax.md`, `modules/master/currency.md`, `modules/security/permission-groups.md`). Jelajahi riwayat aturan modul melalui file ini.
*   **`docs/spec/`**: Spesifikasi teknis horizontal/bersama yang dipakai seluruh fitur (misal: Tata cara standar `pagination.md`, `sequence-generator.md`, `search-menu.md`, atau `menu-structure.md`).
*   **`docs/roadmap/`**: Dokumen perencanaan masa depan ERP atau fitur yang masih tertunda.

## 11. Security & Role Permissions
Aplikasi ini memiliki UI dinamis untuk Manajemen Role (Grouped Permissions) yang secara otomatis akan mengelompokkan daftar _permission_ ke dalam sebuah Folder berdasarkan **kata pertama sebelum underscore (`_`)**. Oleh sebab itu, konvensi penamaan permission sangatlah penting:
1. **Modul Utama (CRUD)**: Gunakan format `[NAMA_MODUL]_[AKSI]`. 
   Contoh: `GEOGRAPHIC_READ`, `PRODUCT_CREATE`. Ini akan mengelompokkan mereka ke folder `GEOGRAPHIC` dan `PRODUCT`.
2. **Fitur Lintas Modul (Shared Features)**: Gunakan *Prefix* jenis fiturnya, contohnya `LOOKUP_` untuk autocomplete popup, dan `POPUP_` untuk fitur modal/popup lainnya (misal: Popup selector item di transaksi).
   Contoh: `LOOKUP_GEOGRAPHIC`, `LOOKUP_PRODUCT`, `POPUP_PARTNER`. Ini akan membuat folder `LOOKUP` dan `POPUP` yang bersih dan mudah diatur oleh Administrator di UI tanpa mencampuri izin akses CRUD reguler.

### Global Search Menu & Permission Groups
Selain pengelompokan visual di UI Role, sistem memiliki fitur **Global Search Menu** yang menggunakan entitas `PermissionGroup`.
- Setiap `Permission` **WAJIB** dikaitkan dengan satu `PermissionGroup` agar modul tersebut dapat muncul di hasil pencarian navbar (jika user punya akses).
- Detail teknis silakan merujuk ke [docs/spec/search-menu.md](spec/search-menu.md) dan [docs/modules/security/permission-groups.md](modules/security/permission-groups.md).
Content from @docs/index.md:
# Solusi ERP Documentation Index

Selamat datang di pusat dokumentasi sistem Solusi ERP. Gunakan dokumen ini sebagai peta navigasi untuk memahami arsitektur, standar teknis, dan modul bisnis yang ada.

## 1. Core Guidelines
- **[AGENTS.md](AGENTS.md)**: Panduan utama untuk AI Assistant dan pengembang. Berisi tech stack, standar coding, dan instruksi fundamental. **WAJIB BACA PERTAMA KALI.**

## 2. Documentation Folders
Dokumentasi dikelompokkan ke dalam folder berikut berdasarkan konteksnya:

- **[/architecture](architecture/)**: Berisi Class Diagram dan pola desain tingkat tinggi (seperti `BaseModel`).
- **[/database](database/)**: Berisi ERD (Entity Relationship Diagram) dan script dummy data (Seeder).
- **[/modules](modules/)**: Penjelasan fungsional dan aturan bisnis untuk setiap fitur spesifik (Inventory, Master Data, dll).
- **[/spec](spec/)**: **[PENTING]** Spesifikasi teknis horizontal/shared yang digunakan di seluruh aplikasi. Lihat **[Spec Index](spec/index.md)**.
- **[/roadmap](roadmap/)**: Rencana pengembangan masa depan dan pelacakan standardisasi arsitektur.

## 3. How to Use
Jika Anda baru bergabung dengan proyek ini atau sedang melakukan troubleshooting:
1. Baca **AGENTS.md** untuk memahami aturan main.
2. Jika masalah berkaitan dengan komponen UI atau pola coding tertentu (misal: cara kerja Autocomplete atau HTMX), carilah dokumen yang relevan di folder **[/spec](spec/index.md)**.
3. Jika masalah berkaitan dengan aturan bisnis modul, cari di folder **[/modules](modules/)**.
Content from @docs/modules/inventory/stock-adjustment.md:
# Stock Adjustment

Dokumen ini menjelaskan spesifikasi fungsional dan teknis untuk fitur **Stock Adjustment** (Penyesuaian Stok) di dalam modul Inventori.

## 1. Ikhtisar (Overview)
Stock Adjustment digunakan untuk melakukan koreksi jumlah stok fisik secara manual di dalam gudang. Fitur ini biasanya digunakan setelah kegiatan *Stock Opname* atau untuk memperbaiki selisih stok yang ditemukan secara tidak sengaja.

## 2. Model Data & Atribut Utama
- **Header**:
    - `code`: Nomor dokumen otomatis (Format: `ADJ-yyMM-XXXXX`).
    - `transactionDate`: Tanggal efektif penyesuaian stok.
    - `facility`: Gudang utama tempat penyesuaian dilakukan.
    - `totalCost`: Total nilai penyesuaian dalam mata uang asli dan lokal (IDR).
    - `status`: **DRAFT** atau **COMPLETED**.
- **Lines**:
    - `product`: Barang yang disesuaikan.
    - `grid`: Area spesifik di gudang.
    - `container`: Bin/rak spesifik.
    - `quantity`: Jumlah barang (positif menambah stok, negatif mengurangi).
    - `unitCost`: HPP per unit saat penyesuaian dilakukan.

## 3. Workflow & Aturan Bisnis (Business Rules)

### A. Status Lifecycle
1.  **DRAFT**: 
    - Penyesuaian baru tersimpan di database.
    - **Belum ada efek** terhadap jumlah stok fisik maupun nilai aset.
    - Data masih dapat diubah (Edit) atau dihapus.
2.  **COMPLETED**:
    - Dipicu dengan menekan tombol **"Process to Inventory"**.
    - Status berubah menjadi permanen dan **tidak dapat diubah lagi**.
    - Memicu mutasi stok fisik di tabel `inv_stock_balances`.
    - Memicu pencatatan sejarah di `inv_movements`.
    - Memicu penambahan/pengurangan di `inv_valuation_layers` (FIFO).

### B. Aturan Valuasi & UoM
- Semua kuantitas yang diinput akan otomatis dikonversi ke **Base UoM** produk sebelum disimpan ke saldo stok.
- Penyesuaian stok positif akan membuat **Valuation Layer** baru dengan harga `unitCost` yang diinput.
- Penyesuaian stok negatif akan mengonsumsi stok menggunakan logika **FIFO** (First-In, First-Out).

### C. Integritas Lokasi (Gudang)
- **Facility Change**: Jika user mengubah *Facility* (Gudang) saat item sudah ada di tabel, sistem akan menampilkan konfirmasi dan **menghapus seluruh item** jika disetujui. Hal ini dilakukan karena Grid dan Container bergantung pada Facility yang dipilih.

## 4. Standar UI/UX (Technical Standard)
- **Generic Helpers**: Menggunakan arsitektur `erp-common-handler.js` untuk konsistensi antar modul:
    - `ErpLineManager`: Otomasi penambahan/penghapusan baris dan penataan index `lines[n]`.
    - `ErpNumeric`: Penanganan input angka ribuan dan desimal yang aman.
    - `ErpInventory`: Mesin konversi UoM (Unit of Measure) dan Serial Number yang terintegrasi dengan drawer.
- **Autocomplete**: Menggunakan standar `Autocomplete Generic` dengan cascading Facility -> Grid -> Container.
- **Dynamic Recap**: Menampilkan ringkasan total nilai dokumen secara real-time di sisi kanan atas form.
- **Fixed Table Layout**: Tabel item menggunakan layout tetap untuk mencegah horizontal scrollbar pada input data yang padat.

## 5. Keamanan (Security)
Fitur ini dilindungi oleh otoritas berikut:
- `STOCK-ADJUSTMENT_READ`: Melihat daftar dan detail adjustment.
- `STOCK-ADJUSTMENT_CREATE`: Menambah adjustment baru.
- `STOCK-ADJUSTMENT_UPDATE`: Mengubah data adjustment berstatus DRAFT.
- `STOCK-ADJUSTMENT_DELETE`: Menghapus adjustment berstatus DRAFT.
- `STOCK-ADJUSTMENT_PROCESS`: Memproses adjustment dari DRAFT ke COMPLETED.
- `LOOKUP_INVENTORY`: Melakukan pencarian autocomplete produk dan lokasi.
Content from @docs/spec/index.md:
# Technical Specifications Index

Folder ini berisi standar teknis horizontal yang berlaku di seluruh aplikasi ERP. Developer dan AI Agent **WAJIB** mengikuti standar ini saat membuat fitur baru.

## UI & Component Standards
- **[ui-standards.md](ui-standards.md)**: Standar visual, tinggi input (32px/28px), dan class CSS ERP.
- **[autocomplete-generic.md](autocomplete-generic.md)**: Cara kerja TomSelect asinkron dan pola cascading lookup.
- **[numeric-standards.md](numeric-standards.md)**: Standar input angka dengan pemisah ribuan (AutoNumeric), helper `ErpNumeric`, dan Spring Formatter.
- **[header-lines-form.md](header-lines-form.md)**: Standar pola form Header-Lines (PO, SO, Inventory).
- **[form-submission.md](form-submission.md)**: Pola pengiriman form (Hybrid Approach: HTMX vs AJAX).
- **[layout-standard.md](layout-standard.md)**: Standar layout utama dengan slot untuk JavaScript per-halaman.

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
--- End of content ---

---

## MODEL ✨

Halo! Ini adalah topik yang sangat menarik dan krusial untuk sebuah sistem ERP. Membangun sistem *Approval* yang *generic* (bisa dipasang ke modul mana saja) memang membutuhkan desain arsitektur yang matang di awal agar kedepannya tidak menyulitkan *maintenance* dan tidak terjadi *tight-coupling* (ketergantungan antar modul yang terlalu erat).

Berdasarkan deskripsi Anda, saya sudah menyusun **Rancangan Arsitektur Generic Approval System**. Mari kita diskusikan rancangan ini menjadi 4 bagian utama: **Desain Database**, **Strategi Integrasi Backend (Event-Driven)**, **Desain UI/UX Frontend**, dan **Flow Implementasi**.

### 1. Desain Database (Entity Model)

Agar sistem ini *generic*, modul Approval tidak boleh memiliki relasi (*Foreign Key*) langsung ke entitas transaksi (seperti `StockAdjustment`). Kita akan menggunakan pendekatan **Polymorphic Relation** menggunakan `referenceType` dan `referenceId`.

Kita bisa membuat 2 tabel utama:

**A. `appr_requests` (Approval Request)**
Menyimpan status *current* dari proses persetujuan sebuah dokumen.
*   `id`: UUID / Long
*   `referenceType`: String (Contoh: `"STOCK_ADJUSTMENT"`, `"PURCHASE_ORDER"`) -> Ini yang membuatnya generic.
*   `referenceId`: String / Long (ID dari dokumen transaksi).
*   `status`: Enum (`PENDING`, `COMPLETED`, `REJECTED`, `CANCELLED`).
*   `currentApproverId`: Relasi ke tabel `Party` (Siapa yang giliran memproses saat ini).
*   *Audit fields dari `BaseModel`* (`createdBy`, `createdDate`, dll).

**B. `appr_histories` (Approval History / Log)**
Menyimpan jejak langkah (log) sesuai permintaan Anda.
*   `id`: UUID / Long
*   `requestId`: FK ke `appr_requests`.
*   `action`: Enum (`REQUESTED`, `APPROVE_AND_FINISH`, `FORWARD`, `REJECTED`, `APPROVE_AND_FORWARD`).
*   `actorId`: Relasi ke `Party` (Siapa yang melakukan aksi).
*   `targetApproverId`: Relasi ke `Party` (Opsional, diisi jika action-nya `FORWARD` atau `APPROVE_AND_FORWARD`).
*   `notes`: Text (Alasan reject atau catatan forward).
*   `actionDate`: LocalDateTime (Otomatis dari `createdDate` BaseModel).

### 2. Strategi Integrasi Backend (Event-Driven)

Bagaimana cara modul `StockAdjustment` tahu bahwa dokumennya sudah di-approve dan harus menambah stok ke gudang, tanpa membuat modul Approval tahu tentang gudang? 

**Jawabannya: Menggunakan Spring Application Events (`ApplicationEventPublisher`).**

1.  **Saat Request Dibuat:** User klik "Request Approval" di halaman Stock Adjustment. Controller memanggil `ApprovalService.createRequest("STOCK_ADJUSTMENT", id, firstApproverPartyId)`. Status Stock Adjustment berubah dari `DRAFT` menjadi `WAITING_APPROVAL`.
2.  **Saat Proses Approve Berjalan:** Approver membuka halaman, melihat komponen approval, lalu klik "Approve & Finish".
3.  **Event Diterbitkan:** `ApprovalService` mencatat history, mengubah status request menjadi `COMPLETED`, lalu mem-publish event secara internal:
    ```java
    applicationEventPublisher.publishEvent(new ApprovalCompletedEvent("STOCK_ADJUSTMENT", id));
    ```
4.  **Modul Transaksi Bereaksi:** Di modul Inventory, kita buat sebuah *Listener*:
    ```java
    @EventListener(condition = "#event.referenceType == 'STOCK_ADJUSTMENT'")
    public void onStockAdjustmentApproved(ApprovalCompletedEvent event) {
        // Panggil service eksisting yang sebelumnya di-trigger oleh tombol "Process"
        stockAdjustmentService.processAdjustmentToInventory(event.getReferenceId());
    }
    ```
Dengan cara ini, Modul Approval murni hanya mengurus "Siapa setuju apa", sedangkan eksekusi bisnis (tambah stok, potong saldo, dll) tetap berada di modul masing-masing.

### 3. Desain UI/UX Frontend (Generic Fragment)

Karena proyek Anda menggunakan Thymeleaf dan HTMX, kita bisa membuat **Satu Buah Fragment Universal** yang bisa di-*include* di halaman detail (View) modul apapun.

File: `templates/fragments/approval.html`
```html
<!-- Fragment ini hanya butuh 2 parameter: tipe dan id dokumen -->
<div th:fragment="timeline(refType, refId)">
    <div class="card">
        <div class="card-header">
            <h3 class="card-title">Approval Workflow</h3>
        </div>
        
        <!-- Bagian ini di-load via HTMX agar asinkron dan tidak memberatkan loading awal halaman -->
        <div class="card-body" 
             th:attr="hx-get=@{/api/approvals/history(refType=${refType}, refId=${refId})}" 
             hx-trigger="load">
             <div class="spinner-border"></div> Loading history...
        </div>

        <!-- Tombol Aksi (Hanya muncul via HTMX jika Party yang login == currentApprover) -->
        <!-- Menampilkan tombol Approve & Finish, Forward, Reject dengan modal untuk input Notes -->
    </div>
</div>
```

**Implementasi di Halaman Stock Adjustment:**
Anda hanya perlu menambahkan satu baris ini di halaman HTML `StockAdjustment`:
```html
<div th:replace="~{fragments/approval :: timeline('STOCK_ADJUSTMENT', ${adjustment.id})}"></div>
```

### 4. Peningkatan Fitur untuk Standard ERP Profesional

Dari ide awal Anda, ini sudah sangat bagus. Untuk membuatnya berstandar *Enterprise*, kita bisa mendiskusikan penambahan ini (bisa untuk fase 2, tidak harus sekarang):

1.  **Role/Jabatan vs Person (Party):** 
    Saat mem-forward, apakah dilempar ke "Budi" (Spesifik Party) atau ke "Kepala Gudang" (Party Role)? Biasanya di ERP, dokumen diapprove oleh *Jabatan*, bukan orang spesifik, agar jika Budi cuti, penggantinya (yang punya jabatan Kepala Gudang) bisa melakukan approve.
2.  **Approval Template / Matrix:**
    Untuk saat ini, `REQUESTED` mungkin mengharuskan user memilih siapa yang akan meng-approve. Di masa depan, ERP memiliki tabel *Matrix*, contoh: "Jika Stock Adjustment di atas Rp 10.000.000, maka Approver-nya otomatis Manager Finance. Jika di bawah itu, cukup Kepala Gudang".
3.  **Notification System:** 
    Perlu dipikirkan apakah sistem akan mengirim notifikasi (via UI lonceng atau Email) kepada `targetApprover` ketika sebuah action `FORWARD` atau `REQUESTED` terjadi.

Bagaimana menurut Anda tentang pendekatan **Polymorphic (referenceType & referenceId)** dan penggunaan **Spring Event Listener** ini? Jika Anda setuju dengan arah arsitekturnya, kita bisa mulai merancang entitas (`BaseModel`) dan kerangka kerjanya di *source code* Anda.