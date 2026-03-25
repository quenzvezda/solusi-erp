# Pure Clean Architecture + DDD + CQRS Standard

Dokumen ini menjelaskan standar arsitektur terbaru yang diterapkan pada modul `news`. Standar ini bertujuan untuk mencapai **High Maintainability**, **Framework Independence**, dan **Testability**.

## 1. Layering & Struktur Package
Setiap modul (fitur) dibagi menjadi 4 layer utama:

```text
com.solusi.erp.[module]
├── domain              <-- 100% PURE JAVA
│   ├── model           (Entity & Value Objects)
│   ├── repository      (Interfaces)
│   └── service         (Domain Logic lintas Aggregate)
├── application         <-- 100% PURE JAVA (Logic Orchestrator)
│   └── usecase
│       ├── command     (Create, Update, Delete)
│       └── query       (Find, List, Search)
├── infrastructure      <-- FRAMEWORK DEPENDENT (Details)
│   ├── persistence     (JPA Entities, Spring Data Repositories)
│   ├── adapter         (Impl Domain Repository)
│   └── config          (Composition Root / Bean Registration)
└── web                 <-- FRAMEWORK DEPENDENT (Interface)
    ├── controller      (Spring Controllers)
    ├── dto             (Requests & Responses)
    └── mapper          (MapStruct)
```

## 2. Aturan Kemurnian (Purity Mandates)
1.  **Domain Layer**: DILARANG menggunakan anotasi Spring (`@Service`, `@Component`) atau library pihak ketiga seperti Lombok. Gunakan konstruktor manual Java.
2.  **Application Layer**: DILARANG menggunakan anotasi Spring atau `@Transactional`. Pengecualian hanya untuk interface Use Case jika sangat diperlukan (namun disarankan murni Java).
3.  **Dependencies**: Panah dependensi selalu mengarah ke **DALAM** (Domain). Domain tidak boleh tahu tentang Application, Infrastructure, atau Web.

## 3. CQRS (Command Query Responsibility Segregation)
Pemisahan tanggung jawab di Application Layer:
*   **Command**: Operasi yang merubah state. Menggunakan Domain Aggregate untuk validasi.
*   **Query**: Operasi baca data. Bisa langsung memanggil repository (Bypass Domain) untuk performa.

## 4. Composition Root (The Bridge)
Registrasi Bean dilakukan secara eksplisit di `infrastructure.config.[Module]Config`. Di sinilah framework "ditempelkan" ke kode murni:
*   Registrasi Bean manual.
*   Pembungkusan Use Case dengan `TransactionTemplate`.
*   Penanganan aspek silang (Cross-cutting concerns) lainnya.

## 5. Strategi Pengujian (Testing)
1.  **Unit Test (Domain/Application)**: Murni JUnit 5 + Mockito. Tanpa Spring Context. Sangat Cepat.
2.  **Surgical Integration Test**: Menggunakan `@ContextConfiguration` untuk memuat HANYA file Config modul terkait. Membuktikan registrasi Bean dan aspek framework (seperti transaksi) berjalan benar.

---
**Reference Implementation**: `com.solusi.erp.common.news`
