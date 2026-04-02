# Pure Clean Architecture + DDD + CQRS Standard

Dokumen ini menjelaskan standar arsitektur terbaru yang diterapkan pada modul `news`. Standar ini bertujuan untuk mencapai **High Maintainability**, **Framework Independence**, dan **Testability**.

## 1. Layering & Struktur Package
Setiap modul (fitur) dibagi menjadi 4 layer utama:

```text
com.solusi.erp.[module]
├── domain              <-- 100% PURE JAVA
│   ├── model           (Entity & Value Objects)
│   ├── repository      (Interfaces — domain port ke persistence)
│   ├── port            (Interfaces — domain port ke slice LAIN, lihat §8)
│   └── service         (Domain Logic lintas Aggregate dalam 1 slice)
├── application         <-- 100% PURE JAVA (Logic Orchestrator)
│   └── usecase
│       ├── command     (Create, Update, Delete)
│       └── query       (Find, List, Search)
├── infrastructure      <-- FRAMEWORK DEPENDENT (Details)
│   ├── persistence     (JPA Entities, Spring Data Repositories)
│   ├── adapter         (Impl Domain Repository & Port)
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

---

## 8. Cross-Slice Communication Standard

Dalam arsitektur Vertical Slice, setiap slice adalah unit yang otonom. Ketika satu slice butuh data atau aksi dari slice lain, **dilarang** melakukan import langsung ke domain model atau JPA repository slice lain dari application/domain layer. Gunakan salah satu dari tiga pola berikut sesuai kebutuhan.

### Tabel Skenario

| Skenario | Contoh di Codebase | Pola yang Digunakan |
|----------|--------------------|---------------------|
| Slice A perlu **cek keberadaan** data di slice B | `brand` cek apakah ada `Product` yang pakai brand ini sebelum delete | **Query Port** (`XxxChecker`) |
| Slice A perlu **lookup nilai** dari slice B (nama, kode) | `product` tampilkan nama brand tanpa join entity | **Query Port** (`XxxLookupPort`) |
| Slice A perlu **tampilkan data referensi** dari slice B di autocomplete | `facility` tampilkan subText Party (kode + tipe) di form edit | **Lookup Provider Port** (`XxxLookupProvider`) |
| Slice A perlu **memicu aksi** di slice B | `adjustment` meminta `StockService` untuk kurangi stok | **Command Port** (inject Use Case interface) |
| Banyak slice perlu di-notify satu kejadian | Approval selesai → modul bisnis bereaksi | **Domain Event** (Spring `ApplicationEvent`) |
| Slice A & B selalu berubah bersama, coupling sangat tinggi | — | Pertimbangkan gabung jadi **1 slice** (salah boundary) |

> **Dilarang keras:** Import entity domain, JPA repository, atau Use Case *impl* dari slice lain di dalam **domain** atau **application** layer manapun.

---

### Pola 1 — Query Port (`XxxChecker` / `XxxQueryPort`)

Digunakan untuk **query read-only** dari slice A ke slice B tanpa slice A perlu mengetahui internal slice B.

**Struktur:**
```
slice-A/
  domain/port/SliceAInUseChecker.java       ← interface (pure Java, tanpa Spring)
  infrastructure/adapter/
    SliceAInUseCheckerImpl.java             ← boleh inject JpaRepository slice B
  infrastructure/config/SliceAConfig.java  ← wire bean checker
```

**Contoh — `brand` cek apakah dipakai `Product`:**
```java
// brand/domain/port/BrandInUseChecker.java  (domain, pure Java)
public interface BrandInUseChecker {
    boolean isUsedByAnyProduct(Long brandId);
}

// brand/infrastructure/adapter/BrandInUseCheckerImpl.java  (infrastructure)
public class BrandInUseCheckerImpl implements BrandInUseChecker {
    private final JpaProductRepository productJpaRepository;  // boleh di sini
    @Override
    public boolean isUsedByAnyProduct(Long brandId) {
        return productJpaRepository.existsByBrandId(brandId);
    }
}

// brand/application/usecase/command/DeleteBrandUseCaseImpl.java
public void execute(Long id) {
    repository.findById(id).orElseThrow(...);
    if (inUseChecker.isUsedByAnyProduct(id)) {
        throw new DomainException("msg.error.brand.in-use");
    }
    repository.delete(id);
}
```

**Wiring di Config:**
```java
@Bean
public BrandInUseChecker brandInUseChecker(JpaProductRepository jpaProductRepository) {
    return new BrandInUseCheckerImpl(jpaProductRepository);
}

@Bean
public DeleteBrandUseCase deleteBrandUseCase(
        BrandRepository repository,
        BrandInUseChecker brandInUseChecker,
        PlatformTransactionManager txManager) {
    var pure = new DeleteBrandUseCaseImpl(repository, brandInUseChecker);
    TransactionTemplate tx = new TransactionTemplate(txManager);
    return (id) -> tx.executeWithoutResult(s -> pure.execute(id));
}
```

**Referensi di Codebase:**
- `inventory.brand.domain.port.BrandInUseChecker`
- `inventory.productcategory.domain.port.ProductCategoryInUseChecker`

---

### Pola 2 — Command Port (inject Use Case Interface)

Digunakan ketika slice A perlu **memicu perubahan state** di slice B. Inject interface Use Case (bukan impl, bukan repository) dari slice B sebagai dependency slice A.

**Contoh — `adjustment` memicu perubahan stok:**
```java
// adjustment use case menerima StockService (port dari stock/domain/port/)
public class CreateStockAdjustmentUseCaseImpl {
    private final StockService stockService;   // port dari slice stock
    // ...
    public void execute(...) {
        // proses adjustment
        stockService.recordMovement(payload);  // delegate ke slice stock
    }
}
```

**Referensi di Codebase:**
- `inventory.stock.domain.port.StockService` — dipakai oleh `adjustment`
- `inventory.stock.domain.service.FifoValuationService` — domain service FIFO internal slice stock
- `inventory.uomconversion.domain.port.UomConversionService` — dipakai oleh `stock`

---

### Pola 3 — Domain Event

Digunakan ketika **banyak slice** perlu bereaksi atas satu kejadian, atau ketika coupling searah tidak jelas. Slice A publish event, slice-slice lain listen secara independen.

Lihat implementasi detail: [`docs/architecture/approval-arsitektur.md`](approval-arsitektur.md)

---

### Pola 4 — Lookup Provider Port (Cross-Slice Autocomplete)

Digunakan ketika slice consumer butuh **menampilkan data referensi** (nama + subText) dari slice provider secara konsisten di UI autocomplete. Pola ini menjamin **single source of truth** untuk format tampilan setiap entity.

**Masalah yang dipecahkan:**
- Controller di slice consumer perlu menampilkan subText (kode, tipe, dll) dari entity slice lain saat pre-fill form edit.
- Tanpa pola ini, setiap consumer harus query JPA repository provider langsung dan menduplikasi logika format subText.
- Jika format subText berubah (misal tambah field), semua consumer harus diubah.

**Struktur:**
```
provider-slice/                          (misal: master.party)
  domain/port/XxxLookupProvider.java     ← interface (pure Java)
  infrastructure/adapter/
    XxxLookupProviderImpl.java           ← impl: query JPA + format subText
  infrastructure/config/XxxConfig.java   ← @Bean registration

consumer-slice/                          (misal: inventory.facility)
  web/controller/XxxController.java      ← inject XxxLookupProvider (bukan JPA repo)
```

**Kontrak Interface:**
```java
// master/party/domain/port/PartyLookupProvider.java
public interface PartyLookupProvider {
    LookupDto resolve(Long partyId);
    // Returns: id, name (with salutation), subText ("PRT-001 - Organisasi / Perusahaan")
}
```

**Implementasi Adapter (single source of truth untuk format subText):**
```java
// master/party/infrastructure/adapter/PartyLookupProviderImpl.java
public class PartyLookupProviderImpl implements PartyLookupProvider {
    private final PartyJpaRepository partyJpaRepository;
    private final MessageSource messageSource;

    @Override
    public LookupDto resolve(Long partyId) {
        if (partyId == null) return null;
        return partyJpaRepository.findById(partyId)
                .map(this::toLookupDto).orElse(null);
    }

    private LookupDto toLookupDto(Party party) {
        String fullName = (hasText(party.getSalutation())
                ? party.getSalutation() + " " : "") + party.getName();
        String typeLabel = messageSource.getMessage(
                "label.party.type." + party.getType().name().toLowerCase(),
                null, LocaleContextHolder.getLocale());
        return new LookupDto(party.getId(), fullName,
                party.getCode() + " - " + typeLabel);
    }
}
```

**Consumer menggunakan port:**
```java
// inventory/facility/web/controller/FacilityController.java
private final PartyLookupProvider partyLookupProvider;  // inject port, bukan JPA repo

private Map<String, Object> buildFacilityUI(Facility domain) {
    LookupDto ownerLookup = partyLookupProvider.resolve(domain.getOwnerId());
    ui.put("ownerName", ownerLookup != null ? ownerLookup.name() : "");
    ui.put("ownerCode", ownerLookup != null ? ownerLookup.subText() : "");
}
```

**Kapan menggunakan pola ini:**

| Kondisi | Gunakan Lookup Provider? |
|---------|--------------------------|
| Entity provider dipakai oleh ≥ 2 consumer slice | ✅ Wajib — hindari duplikasi format |
| Entity provider hanya dipakai 1 consumer | ⚠️ Opsional — boleh query langsung, tapi provider port lebih future-proof |
| SubText memerlukan i18n / logika format kompleks | ✅ Wajib — pastikan konsistensi |
| Hanya butuh kode sederhana (misal `Geographic.code`) | ❌ Boleh query langsung dari JPA repo |

**Referensi di Codebase:**
- `master.party.domain.port.PartyLookupProvider` — dipakai oleh `inventory.facility`
- Format subText konsisten dengan `PartyLookupController.mapToLookupDto()` (lookup endpoint)

---

### Ringkasan Aturan

```
✅ domain/port/     → interface Java murni, boleh dipakai di application layer
✅ infrastructure/adapter/ → satu-satunya tempat yang boleh import JpaRepository slice lain
✅ Inject Use Case interface antar slice hanya melalui port / domain/port/
❌ Import domain model atau entity dari slice lain di application/domain layer
❌ Import JpaRepository slice lain di use case atau domain service
❌ @ManyToOne ke entity slice lain — gunakan Long referenceId
```

