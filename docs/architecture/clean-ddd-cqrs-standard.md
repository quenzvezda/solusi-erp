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

## 6. Intent-Based Naming (DTO Standard)
Untuk menghindari kekakuan nama (seperti `ApprovalRequestRequest`) dan meningkatkan keterbacaan, setiap DTO di layer `web` wajib menggunakan penamaan berbasis intensi (Intent-Based Naming):

*   **Request (Write/Command)**: Gunakan kata kerja atau aksi.
    *   `[Action][Entity]Request`
    *   Contoh: `NewsSaveRequest` (untuk Create/Update), `ApprovalDecisionRequest` (untuk Approve/Reject).
*   **Response (Read/Query)**: Gunakan deskripsi output.
    *   `[Entity][Type]Response`
    *   Contoh: `NewsDetailResponse`, `NewsSummaryResponse` (untuk list view).

Keuntungan:
1.  **Menghindari Naming Stuttering**: Tidak ada lagi `RequestRequest`.
2.  **Validasi Spesifik**: Setiap DTO hanya berisi field yang diperlukan untuk aksi tersebut.
3.  **UI Alignment**: Nama DTO mencerminkan tugas (task) yang sedang dikerjakan user di layar.

---
## 7. Reference Implementations

Berikut modul-modul yang bisa dijadikan referensi implementasi Clean Architecture + DDD:

| Modul | Package | Kompleksitas | Cocok untuk mempelajari |
|-------|---------|-------------|------------------------|
| **Tax** | `master.tax` | Sederhana | CRUD dasar, use case command/query, MapStruct mapper |
| **Brand** | `inventory.brand` | Sederhana | CRUD + lookup use case, golden reference untuk web-layer test |
| **Currency** | `master.currency` | Sedang | Business logic di domain (default currency), aggregate method |
| **PermissionGroup** | `security.permissiongroup` | Sedang | Localized fields, integration dengan search menu |
| **Role** | `security.role` | Sedang | Many-to-many relation via reference ID, lookup use case |
| **User** | `security.user` | Kompleks | Cross-module reference (party, role), profile management, security integration |
| **News** | `common.news` | Sederhana | Prototype awal — masih valid sebagai contoh minimal |
| **Approval** | `common.approval` | Kompleks | Event-driven, polymorphic reference, domain service |

> **Catatan:** Untuk modul baru, mulailah dari `master.tax` atau `inventory.brand` sebagai template, lalu lihat `security.role` untuk pola many-to-many.
