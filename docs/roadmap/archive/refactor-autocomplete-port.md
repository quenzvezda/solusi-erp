# Roadmap: Refactor Autocomplete ke Pola Port (LookupProvider)

> **Tujuan:** Setiap autocomplete cross-domain/cross-slice di seluruh halaman harus
> mengambil data pre-edit melalui sebuah LookupProvider Port, bukan direct query ke
> infrastruktur domain lain. Referensi: `PartyLookupProvider` (sudah selesai).
>
> **Scope:** Mapping, planning, dan implementasi refactor. Tidak ada perubahan
> behaviour user-facing — semua halaman tetap berfungsi sama.
>
> **Bukan scope:** adjustment _line items_ (productId, gridId, containerId di baris
> tabel) — data ini adalah snapshot historis yang sengaja di-denormalisasi saat
> transaksi dibuat, analog dengan baris invoice. Tidak perlu Port.

---

## Temuan: Mapping Autocomplete di Seluruh Project

### Referensi Implementasi yang Sudah Selesai

**`PartyLookupProvider`** adalah satu-satunya LookupProvider Port yang sudah
terimplementasi penuh:

| File | Package |
|------|---------|
| `PartyLookupProvider.java` | `master.party.domain.port` |
| `PartyLookupProviderImpl.java` | `master.party.infrastructure.adapter` |
| Bean di `PartyConfig.java` | `master.party.infrastructure.config` |

Pattern: `resolve(Long id) → LookupDto(id, name, subText)` — dikonsumsi oleh
`FacilityController` untuk pre-edit field `ownerId`.

**Catatan khusus `PartyReferenceGateway`** (`security.user.application.port`):
Sudah merupakan pola Port untuk konsumsi data Party oleh slice User, namun
mengembalikan custom record `PartyReferenceData(id, code, name)`, bukan `LookupDto`.
Status: ✅ Sudah Port (kontrak berbeda, tapi prinsip sama).

---

### Tabel Mapping Lengkap

| Halaman | Field | Lookup Path (API) | Domain Sumber | Consumer Domain | Status |
|---------|-------|-------------------|---------------|-----------------|--------|
| `/master/geographic` | `parentId` | `geographics/countries` atau `geographics/provinces` | Geographic | Geographic (self) | — Same domain, skip |
| `/master/bank-accounts` | `cityId` | `geographics/cities` | Geographic | BankAccount | ❌ Belum Port + BUG: `${selectedCity}` tidak di-set controller |
| `/master/bank-accounts` | `partyId` | `parties` | Party | BankAccount | ❌ Belum Port + BUG: `${selectedParty}` tidak di-set controller |
| `/inventory/facilities` | `ownerId` | `parties` | Party | Facility | ✅ Sudah Port (`PartyLookupProvider`) |
| `/inventory/facilities` | `cityId` | `geographics/cities` | Geographic | Facility | ❌ Belum Port (direct `GeographicJpaRepository`) |
| `/inventory/grids` | `facilityId` | `inventory/facilities` | Facility | Grid | ❌ Belum Port (stored `facilityName`, subtext kosong) |
| `/inventory/containers` | `gridId` | `inventory/grids` | Grid | Container | ❌ Belum Port (stored `gridName`, `gridCode` = `""`) |
| `/inventory/products` | `categoryId` | `inventory/product-categories` | ProductCategory | Product | ❌ Belum Port (stored `categoryName/categoryCode`) |
| `/inventory/products` | `brandId` | `inventory/brands` | Brand | Product | ❌ Belum Port (stored `brandName/brandCode`) |
| `/inventory/uom-conversions` | `productId` | `inventory/products` | Product | UomConversion | ❌ Belum Port (stored `productName/productCode`) |
| `/inventory/adjustments` | `facilityId` (header) | `inventory/facilities` | Facility | Adjustment | ❌ Belum Port (stored `facilityName/facilityCode`) |
| `/inventory/adjustments` | `productId` (lines) | `inventory/products` | Product | Adjustment | — Snapshot historis, skip |
| `/inventory/adjustments` | `gridId` (lines) | `inventory/grids` | Grid | Adjustment | — Snapshot historis, skip |
| `/inventory/adjustments` | `containerId` (lines) | `inventory/containers` | Container | Adjustment | — Snapshot historis, skip |
| `/security/users` | `roleId` | `security/roles` | Role | User | ❌ Belum Port (direct `FindRolesUseCase`, load all) |
| `/security/users` | `partyId` | `parties/available-for-user` | Party | User | ✅ Sudah Port (`PartyReferenceGateway`) |

---

## Dependency Graph Antar Phase

```
Phase 1: GeographicLookupProvider
    └─► dikonsumsi Phase 2 (BankAccount/cityId) & Phase 3 (Facility/cityId)

Phase 2: Wire PartyLookupProvider ke BankAccount   ← tidak butuh port baru
    └─► PartyLookupProvider sudah ada (Phase 0, done)

Phase 3: Wire GeographicLookupProvider ke FacilityController
    └─► bergantung pada Phase 1

Phase 4: FacilityLookupProvider
    └─► dikonsumsi Phase 5 (Grid/facilityId) & Phase 6 (Adjustment/facilityId)

Phase 5: GridLookupProvider
    └─► dikonsumsi Phase 7 (Container/gridId)

Phase 6: Wire FacilityLookupProvider ke StockAdjustmentController
    └─► bergantung pada Phase 4

Phase 7: Wire GridLookupProvider ke ContainerController
    └─► bergantung pada Phase 5

Phase 8: ProductLookupProvider
    └─► dikonsumsi Phase 9 (UomConversion/productId)

Phase 9: Wire ProductLookupProvider ke UomConversionController
    └─► bergantung pada Phase 8

Phase 10: ProductCategoryLookupProvider
    └─► dikonsumsi Phase 12 (Product/categoryId)

Phase 11: BrandLookupProvider
    └─► dikonsumsi Phase 12 (Product/brandId)

Phase 12: Wire ProductCategory & Brand LookupProvider ke ProductController
    └─► bergantung pada Phase 10 & 11

Phase 13: RoleLookupProvider
    └─► dikonsumsi Phase 14 (User/roleId)

Phase 14: Wire RoleLookupProvider ke UserServiceImpl
    └─► bergantung pada Phase 13
```

---

### Phase 1 — GeographicLookupProvider

> **Prioritas tertinggi:** Geographic dikonsumsi oleh dua slice berbeda (Facility dan
> BankAccount). Saat ini Facility mengakses `GeographicJpaRepository` langsung
> (pelanggaran batas domain), dan BankAccount bahkan tidak set `selectedCity` sama
> sekali (bug pre-edit).

#### Buat Port

- [x] Buat interface `com.solusi.erp.master.geographic.domain.port.GeographicLookupProvider`
  dengan method `LookupDto resolve(Long geographicId)`
  di file `src/main/java/com/solusi/erp/master/geographic/domain/port/GeographicLookupProvider.java`

#### Buat Implementation

- [x] Buat class `com.solusi.erp.master.geographic.infrastructure.adapter.GeographicLookupProviderImpl`
  implements `GeographicLookupProvider`
  di file `src/main/java/com/solusi/erp/master/geographic/infrastructure/adapter/GeographicLookupProviderImpl.java`
  - Query `GeographicJpaRepository.findById(id)`
  - Format `name` = nama kota/provinsi/negara
  - Format `subText` = kode geographic (contoh: `"JKT - Kota"`)

#### Daftarkan Bean

- [x] Tambah `@Bean GeographicLookupProvider geographicLookupProvider(...)` di
  `com.solusi.erp.master.geographic.infrastructure.config.GeographicConfig`
  (buat file jika belum ada:
  `src/main/java/com/solusi/erp/master/geographic/infrastructure/config/GeographicConfig.java`)

**Acceptance Criteria Phase 1:**
- [x] `GeographicLookupProvider` terimplementasi sebagai Port dengan Impl dan Bean
- [x] `resolve(null)` mengembalikan `null` (tidak throw exception)
- [x] `mvn clean test` pass sebelum lanjut ke Phase 2

---

### Phase 2 — Wire PartyLookupProvider ke BankAccountController

> Port `PartyLookupProvider` sudah ada. BankAccountController tidak inject port ini
> sama sekali, sehingga field `partyId` pre-edit di halaman edit tidak menampilkan
> nama party. Bug nyata, bukan hanya code smell.

#### Wire ke Controller

- [x] Inject `PartyLookupProvider partyLookupProvider` di constructor
  `com.solusi.erp.master.bankaccount.web.controller.BankAccountController`
  di file `src/main/java/com/solusi/erp/master/bankaccount/web/controller/BankAccountController.java`

- [x] Di method `showEditForm(Long id, Model model)` pada `BankAccountController`:
  - Panggil `partyLookupProvider.resolve(domain.getPartyId())`
  - Set `model.addAttribute("selectedParty", lookup != null ? lookup.name() : domain.getPartyName())`
  - (Opsional) set subtext untuk kode party jika template mendukung

- [x] Update template `src/main/resources/templates/master/bank-accounts/form.html`
  bila `initialSubtext` untuk `partyId` perlu diisi (saat ini kosong `''`)

**Acceptance Criteria Phase 2:**
- [x] Halaman edit Bank Account menampilkan nama Party yang sudah terpilih dengan benar
- [x] `mvn clean test` pass sebelum lanjut ke Phase 3

---

### Phase 3 — Wire GeographicLookupProvider ke FacilityController dan BankAccountController

> FacilityController saat ini mengakses `GeographicJpaRepository` secara langsung
> untuk mengisi `cityName` pre-edit (pelanggaran batas domain).
> BankAccountController tidak set `selectedCity` sama sekali (bug pre-edit).

#### Wire ke FacilityController

- [x] Inject `GeographicLookupProvider geographicLookupProvider` di constructor
  `com.solusi.erp.inventory.facility.web.controller.FacilityController`
  di file `src/main/java/com/solusi/erp/inventory/facility/web/controller/FacilityController.java`

- [x] Di method `buildFacilityUI(Facility domain)` pada `FacilityController`:
  - Ganti `GeographicJpaRepository` call dengan `geographicLookupProvider.resolve(domain.getCityId())`
  - Hapus injection `GeographicJpaRepository` dari controller (tidak perlu lagi setelah ini)

#### Wire ke BankAccountController

- [x] Inject `GeographicLookupProvider geographicLookupProvider` di constructor
  `com.solusi.erp.master.bankaccount.web.controller.BankAccountController`
  di file `src/main/java/com/solusi/erp/master/bankaccount/web/controller/BankAccountController.java`

- [x] Di method `showEditForm(Long id, Model model)` pada `BankAccountController`:
  - Panggil `geographicLookupProvider.resolve(domain.getCityId())`
  - Set `model.addAttribute("selectedCity", lookup != null ? lookup.name() : domain.getCityName())`

**Acceptance Criteria Phase 3:**
- [x] `FacilityController` tidak lagi mengimport atau menggunakan `GeographicJpaRepository`
- [x] Halaman edit Facility menampilkan nama kota yang benar via Port
- [x] Halaman edit Bank Account menampilkan nama kota yang sudah terpilih dengan benar
- [x] `mvn clean test` pass sebelum lanjut ke Phase 4

---

### Phase 4 — FacilityLookupProvider

> Facility dikonsumsi oleh Grid (pre-edit `facilityId`) dan StockAdjustment
> (pre-edit `facilityId` di header form). Saat ini keduanya menggunakan nama
> yang tersimpan di domain (`facilityName`) tanpa subtext yang konsisten.

#### Buat Port

- [x] Buat interface `com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider`
  dengan method `LookupDto resolve(Long facilityId)`
  di file `src/main/java/com/solusi/erp/inventory/facility/domain/port/FacilityLookupProvider.java`

#### Buat Implementation

- [x] Buat class `com.solusi.erp.inventory.facility.infrastructure.adapter.FacilityLookupProviderImpl`
  implements `FacilityLookupProvider`
  di file `src/main/java/com/solusi/erp/inventory/facility/infrastructure/adapter/FacilityLookupProviderImpl.java`
  - Query `FacilityJpaRepository.findById(id)`
  - Format `name` = nama facility
  - Format `subText` = kode facility (contoh: `"FAC-001"`)

#### Daftarkan Bean

- [x] Tambah `@Bean FacilityLookupProvider facilityLookupProvider(...)` di
  `com.solusi.erp.inventory.facility.infrastructure.config.FacilityConfig`
  di file `src/main/java/com/solusi/erp/inventory/facility/infrastructure/config/FacilityConfig.java`

**Acceptance Criteria Phase 4:**
- [x] `FacilityLookupProvider` terimplementasi sebagai Port dengan Impl dan Bean
- [x] `mvn clean test` pass sebelum lanjut ke Phase 5

---

### Phase 5 — GridLookupProvider

> Grid dikonsumsi oleh Container (pre-edit `gridId`). Saat ini `ContainerController`
> menggunakan `domain.getGridName()` (stored name) dan `gridCode = ""` (kosong) —
> subtext tidak muncul di pre-edit.

#### Buat Port

- [x] Buat interface `com.solusi.erp.inventory.grid.domain.port.GridLookupProvider`
  dengan method `LookupDto resolve(Long gridId)`
  di file `src/main/java/com/solusi/erp/inventory/grid/domain/port/GridLookupProvider.java`

#### Buat Implementation

- [x] Buat class `com.solusi.erp.inventory.grid.infrastructure.adapter.GridLookupProviderImpl`
  implements `GridLookupProvider`
  di file `src/main/java/com/solusi/erp/inventory/grid/infrastructure/adapter/GridLookupProviderImpl.java`
  - Query `GridJpaRepository.findById(id)`
  - Format `name` = nama grid
  - Format `subText` = kode grid (contoh: `"GRD-001"`)

#### Daftarkan Bean

- [x] Tambah `@Bean GridLookupProvider gridLookupProvider(...)` di
  `com.solusi.erp.inventory.grid.infrastructure.config.GridConfig`
  di file `src/main/java/com/solusi/erp/inventory/grid/infrastructure/config/GridConfig.java`

**Acceptance Criteria Phase 5:**
- [x] `GridLookupProvider` terimplementasi sebagai Port dengan Impl dan Bean
- [x] `mvn clean test` pass sebelum lanjut ke Phase 6

---

### Phase 6 — Wire FacilityLookupProvider ke GridController dan StockAdjustmentController

#### Wire ke GridController

- [x] Inject `FacilityLookupProvider facilityLookupProvider` di constructor
  `com.solusi.erp.inventory.grid.web.controller.GridController`
  di file `src/main/java/com/solusi/erp/inventory/grid/web/controller/GridController.java`

- [x] Di method `showEditForm(Long id, Model model)` pada `GridController`:
  - Panggil `facilityLookupProvider.resolve(domain.getFacilityId())`
  - Set `gridRequest.facilityName` dan `gridRequest.facilityCode` dari `LookupDto`
    (atau tambah `gridUI` map ke model jika tidak ingin modifikasi DTO)

#### Wire ke StockAdjustmentController

- [x] Inject `FacilityLookupProvider facilityLookupProvider` di constructor
  `com.solusi.erp.inventory.adjustment.web.controller.StockAdjustmentController`
  di file `src/main/java/com/solusi/erp/inventory/adjustment/web/controller/StockAdjustmentController.java`

- [x] Di method `editForm(Long id, Model model)` pada `StockAdjustmentController`:
  - Panggil `facilityLookupProvider.resolve(domain.getFacilityId())`
  - Timpa `stockAdjustment.facilityName` dan `facilityCode` dengan data dari Port
    agar menampilkan nama facility terkini (bukan snapshot lama)

**Acceptance Criteria Phase 6:**
- [x] Halaman edit Grid menampilkan nama dan kode Facility yang benar via Port
- [x] Halaman edit Stock Adjustment menampilkan nama Facility yang benar via Port
- [x] `mvn clean test` pass sebelum lanjut ke Phase 7

---

### Phase 7 — Wire GridLookupProvider ke ContainerController

#### Wire ke ContainerController

- [x] Inject `GridLookupProvider gridLookupProvider` di constructor
  `com.solusi.erp.inventory.container.web.controller.ContainerController`
  di file `src/main/java/com/solusi/erp/inventory/container/web/controller/ContainerController.java`

- [x] Di method `buildContainerUI(Container domain)` pada `ContainerController`:
  - Ganti `ui.put("gridName", domain.getGridName())` dan `ui.put("gridCode", "")`
    dengan `gridLookupProvider.resolve(domain.getGridId())`
  - Set `gridName` dan `gridCode` dari `LookupDto` (saat ini `gridCode` selalu `""` — bug)

**Acceptance Criteria Phase 7:**
- [x] Halaman edit Container menampilkan nama dan kode Grid yang benar via Port
- [x] `gridCode` tidak lagi kosong string
- [x] `mvn clean test` pass sebelum lanjut ke Phase 8

---

### Phase 8 — ProductLookupProvider

> Product dikonsumsi oleh UomConversion (pre-edit `productId`). Saat ini
> `UomConversionController` menggunakan `domain.getProductName()` dan
> `domain.getProductCode()` dari stored data.

#### Buat Port

- [x] Buat interface `com.solusi.erp.inventory.product.domain.port.ProductLookupProvider`
  dengan method `LookupDto resolve(Long productId)`
  di file `src/main/java/com/solusi/erp/inventory/product/domain/port/ProductLookupProvider.java`

#### Buat Implementation

- [x] Buat class `com.solusi.erp.inventory.product.infrastructure.adapter.ProductLookupProviderImpl`
  implements `ProductLookupProvider`
  di file `src/main/java/com/solusi/erp/inventory/product/infrastructure/adapter/ProductLookupProviderImpl.java`
  - Query `ProductJpaRepository.findById(id)`
  - Format `name` = nama produk
  - Format `subText` = kode produk (contoh: `"PRD-001"`)

#### Daftarkan Bean

- [x] Tambah `@Bean ProductLookupProvider productLookupProvider(...)` di
  `com.solusi.erp.inventory.product.infrastructure.config.ProductConfig`
  di file `src/main/java/com/solusi/erp/inventory/product/infrastructure/config/ProductConfig.java`

**Acceptance Criteria Phase 8:**
- [x] `ProductLookupProvider` terimplementasi sebagai Port dengan Impl dan Bean
- [x] `mvn clean test` pass sebelum lanjut ke Phase 9

---

### Phase 9 — Wire ProductLookupProvider ke UomConversionController

#### Wire ke UomConversionController

- [x] Inject `ProductLookupProvider productLookupProvider` di constructor
  `com.solusi.erp.inventory.uomconversion.web.controller.UomConversionController`
  di file `src/main/java/com/solusi/erp/inventory/uomconversion/web/controller/UomConversionController.java`

- [x] Di method `showEditForm(Long id, Model model)` pada `UomConversionController`:
  - Panggil `productLookupProvider.resolve(domain.getProductId())`
  - Ganti konstruksi `UomConversionUIInfo` dengan nama dan kode dari `LookupDto`

**Acceptance Criteria Phase 9:**
- [x] Halaman edit UOM Conversion menampilkan nama dan kode Product yang benar via Port
- [x] `mvn clean test` pass sebelum lanjut ke Phase 10

---

### Phase 10 — ProductCategoryLookupProvider

> ProductCategory dikonsumsi oleh Product (pre-edit `categoryId`). Saat ini
> `ProductController` menggunakan `productRequest.categoryName/categoryCode`
> dari stored domain data.

#### Buat Port

- [x] Buat interface `com.solusi.erp.inventory.productcategory.domain.port.ProductCategoryLookupProvider`
  dengan method `LookupDto resolve(Long categoryId)`
  di file `src/main/java/com/solusi/erp/inventory/productcategory/domain/port/ProductCategoryLookupProvider.java`

#### Buat Implementation

- [x] Buat class `com.solusi.erp.inventory.productcategory.infrastructure.adapter.ProductCategoryLookupProviderImpl`
  implements `ProductCategoryLookupProvider`
  di file `src/main/java/com/solusi/erp/inventory/productcategory/infrastructure/adapter/ProductCategoryLookupProviderImpl.java`
  - Query `ProductCategoryJpaRepository.findById(id)`
  - Format `name` = nama kategori
  - Format `subText` = kode kategori

#### Daftarkan Bean

- [x] Tambah `@Bean ProductCategoryLookupProvider productCategoryLookupProvider(...)` di
  `com.solusi.erp.inventory.productcategory.infrastructure.config.ProductCategoryConfig`
  di file `src/main/java/com/solusi/erp/inventory/productcategory/infrastructure/config/ProductCategoryConfig.java`

**Acceptance Criteria Phase 10:**
- [x] `ProductCategoryLookupProvider` terimplementasi sebagai Port dengan Impl dan Bean
- [x] `mvn clean test` pass sebelum lanjut ke Phase 11

---

### Phase 11 — BrandLookupProvider

> Brand dikonsumsi oleh Product (pre-edit `brandId`). Saat ini `ProductController`
> menggunakan `productRequest.brandName/brandCode` dari stored domain data.

#### Buat Port

- [x] Buat interface `com.solusi.erp.inventory.brand.domain.port.BrandLookupProvider`
  dengan method `LookupDto resolve(Long brandId)`
  di file `src/main/java/com/solusi/erp/inventory/brand/domain/port/BrandLookupProvider.java`

#### Buat Implementation

- [x] Buat class `com.solusi.erp.inventory.brand.infrastructure.adapter.BrandLookupProviderImpl`
  implements `BrandLookupProvider`
  di file `src/main/java/com/solusi/erp/inventory/brand/infrastructure/adapter/BrandLookupProviderImpl.java`
  - Query `BrandJpaRepository.findById(id)`
  - Format `name` = nama brand
  - Format `subText` = kode brand

#### Daftarkan Bean

- [x] Tambah `@Bean BrandLookupProvider brandLookupProvider(...)` di
  `com.solusi.erp.inventory.brand.infrastructure.config.BrandConfig`
  di file `src/main/java/com/solusi/erp/inventory/brand/infrastructure/config/BrandConfig.java`

**Acceptance Criteria Phase 11:**
- [x] `BrandLookupProvider` terimplementasi sebagai Port dengan Impl dan Bean
- [x] `mvn clean test` pass sebelum lanjut ke Phase 12

---

### Phase 12 — Wire ProductCategory dan Brand LookupProvider ke ProductController

#### Wire ke ProductController

- [x] Inject `ProductCategoryLookupProvider productCategoryLookupProvider` di constructor
  `com.solusi.erp.inventory.product.web.controller.ProductController`
  di file `src/main/java/com/solusi/erp/inventory/product/web/controller/ProductController.java`

- [x] Inject `BrandLookupProvider brandLookupProvider` di constructor
  `com.solusi.erp.inventory.product.web.controller.ProductController`

- [x] Di method `showEditForm(Long id, Model model)` pada `ProductController`:
  - Panggil `productCategoryLookupProvider.resolve(domain.getCategoryId())`
  - Set `productRequest.setCategoryName(...)` dan `productRequest.setCategoryCode(...)`
    dari `LookupDto`, atau tambah `productUI` map ke model

  - Panggil `brandLookupProvider.resolve(domain.getBrandId())`
  - Set `productRequest.setBrandName(...)` dan `productRequest.setBrandCode(...)`
    dari `LookupDto`

**Acceptance Criteria Phase 12:**
- [x] Halaman edit Product menampilkan nama dan kode ProductCategory yang benar via Port
- [x] Halaman edit Product menampilkan nama dan kode Brand yang benar via Port
- [x] `mvn clean test` pass sebelum lanjut ke Phase 13

---

### Phase 13 — RoleLookupProvider

> Role dikonsumsi oleh User (pre-edit `roleId`). Saat ini `UserServiceImpl.toUserWithRole()`
> memanggil `findRolesUseCase.execute()` yang me-load semua Role lalu filter client-side —
> tidak efisien dan tidak menggunakan Port.

#### Buat Port

- [x] Buat interface `com.solusi.erp.security.role.domain.port.RoleLookupProvider`
  dengan method `LookupDto resolve(Long roleId)`
  di file `src/main/java/com/solusi/erp/security/role/domain/port/RoleLookupProvider.java`

#### Buat Implementation

- [x] Buat class `com.solusi.erp.security.role.infrastructure.adapter.RoleLookupProviderImpl`
  implements `RoleLookupProvider`
  di file `src/main/java/com/solusi/erp/security/role/infrastructure/adapter/RoleLookupProviderImpl.java`
  - Query `RoleJpaRepository.findById(id)`
  - Format `name` = nama role
  - Format `subText` = deskripsi role (atau kode jika ada)

#### Daftarkan Bean

- [x] Tambah `@Bean RoleLookupProvider roleLookupProvider(...)` di
  `com.solusi.erp.security.role.infrastructure.config.RoleConfig`
  di file `src/main/java/com/solusi/erp/security/role/infrastructure/config/RoleConfig.java`

**Acceptance Criteria Phase 13:**
- [x] `RoleLookupProvider` terimplementasi sebagai Port dengan Impl dan Bean
- [x] `mvn clean test` pass sebelum lanjut ke Phase 14

---

### Phase 14 — Wire RoleLookupProvider ke UserServiceImpl

#### Wire ke UserServiceImpl

- [x] Inject `RoleLookupProvider roleLookupProvider` di constructor
  `com.solusi.erp.security.user.service.impl.UserServiceImpl`
  di file `src/main/java/com/solusi/erp/security/user/service/impl/UserServiceImpl.java`

- [x] Di method `getUserEditView(Long id)` pada `UserServiceImpl`:
  - Panggil `roleLookupProvider.resolve(user.getRoleId())`
  - Bangun `UserUiForm` dengan `roleName` dan `roleDescription` dari `LookupDto`
  - Hapus penggunaan `findRolesUseCase` untuk keperluan pre-edit (tetap boleh digunakan
    untuk dropdown list di form, bukan untuk resolve single role by ID)

- [x] Update bean definition di
  `com.solusi.erp.security.user.infrastructure.config.UserConfig`
  di file `src/main/java/com/solusi/erp/security/user/infrastructure/config/UserConfig.java`
  untuk inject `RoleLookupProvider` ke `UserServiceImpl`

**Acceptance Criteria Phase 14:**
- [x] Halaman edit User menampilkan nama Role yang benar via Port
- [x] `UserServiceImpl` tidak lagi load semua Role hanya untuk resolve satu role by ID
- [x] `mvn clean test` pass

---

## Final Acceptance Criteria

- [x] `mvn clean test` pass (semua test suite green)
- [x] Semua autocomplete di seluruh halaman berfungsi — tidak ada 404 pada endpoint lookup
- [x] Tidak ada direct cross-domain query untuk kebutuhan lookup/pre-edit:
  - `FacilityController` tidak inject `GeographicJpaRepository`
  - `BankAccountController` tidak inject repo domain lain
  - `GridController` tidak inject repo Facility langsung
  - `ContainerController` tidak inject repo Grid langsung
  - `UomConversionController` tidak inject repo Product langsung
  - `ProductController` tidak inject repo ProductCategory atau Brand langsung
  - `UserServiceImpl` tidak load semua Role hanya untuk resolve satu ID
- [x] Smoke test Playwright — semua halaman berikut dapat dibuka dan autocomplete berfungsi:
  - [x] `/master/bank-accounts/create` — dropdown city dan party bisa diisi
  - [x] `/master/bank-accounts/edit/{id}` — city dan party tampil pre-populated
  - [x] `/inventory/facilities/edit/{id}` — owner (party) dan city tampil pre-populated
  - [x] `/inventory/grids/edit/{id}` — facility tampil pre-populated dengan subtext kode
  - [x] `/inventory/containers/edit/{id}` — grid tampil pre-populated dengan subtext kode
  - [x] `/inventory/products/edit/{id}` — category dan brand tampil pre-populated
  - [x] `/inventory/uom-conversions/edit/{id}` — product tampil pre-populated
  - [x] `/inventory/adjustments/edit/{id}` — facility header tampil pre-populated
  - [x] `/security/users/edit/{id}` — role tampil pre-populated

---

## Ringkasan Port yang Perlu Dibuat

| # | Port Interface | Package | Impl | Config Bean |
|---|----------------|---------|------|-------------|
| 1 | `GeographicLookupProvider` | `master.geographic.domain.port` | `GeographicLookupProviderImpl` | `GeographicConfig` |
| 2 | `FacilityLookupProvider` | `inventory.facility.domain.port` | `FacilityLookupProviderImpl` | `FacilityConfig` |
| 3 | `GridLookupProvider` | `inventory.grid.domain.port` | `GridLookupProviderImpl` | `GridConfig` |
| 4 | `ProductLookupProvider` | `inventory.product.domain.port` | `ProductLookupProviderImpl` | `ProductConfig` |
| 5 | `ProductCategoryLookupProvider` | `inventory.productcategory.domain.port` | `ProductCategoryLookupProviderImpl` | `ProductCategoryConfig` |
| 6 | `BrandLookupProvider` | `inventory.brand.domain.port` | `BrandLookupProviderImpl` | `BrandConfig` |
| 7 | `RoleLookupProvider` | `security.role.domain.port` | `RoleLookupProviderImpl` | `RoleConfig` |

Port yang sudah ada (referensi / tidak perlu dibuat ulang):

| Port | Package | Status |
|------|---------|--------|
| `PartyLookupProvider` | `master.party.domain.port` | ✅ Done |
| `PartyReferenceGateway` | `security.user.application.port` | ✅ Done (kontrak berbeda) |

---

## Appendix: Additional Anti-Pattern Cleanup (Post-Roadmap)

Setelah semua Phase di roadmap ini selesai, ditemukan beberapa anti-pattern tambahan yang diselesaikan:

### 1. Approval Read Abstraction (News, PO, PR)

**Masalah:** Controller News, Purchase Order, dan Purchase Request menggunakan direct query ke repository untuk membaca data approval yang sudah dibuat (`ApprovalJpaRepository`), melanggar prinsip dependency inversion.

**Solusi:**
- Dibuat `ApprovalReadPort` di `common.approval.domain.port` dengan method `findByRelatedId(String relatedId) → Optional<Approval>`
- Dibuat `ApprovalReadAdapter` di `common.approval.infrastructure.adapter`
- Wire adapter ke controller News, PO, dan PR melalui config bean masing-masing (`NewsConfig`, `PurchaseOrderConfig`, `PurchaseRequestConfig`)

**Dampak:** Controller tidak lagi inject `ApprovalJpaRepository` secara langsung.

---

### 2. Inventory Report Mapper menggunakan CurrencyLookupProvider

**Masalah:** `InventoryReportMapper` menggunakan direct `CurrencyJpaRepository` untuk resolve currency saat mapping `StockAdjustment` ke DTO report.

**Solusi:**
- Inject `CurrencyLookupProvider` (port yang sudah ada di `master.currency.domain.port`) ke `InventoryReportMapper`
- Ganti `currencyRepository.findById(...)` dengan `currencyLookupProvider.resolve(...)`
- Update `InventoryReportMapperImpl` (infrastructure adapter) untuk menerima `CurrencyLookupProvider`

**Dampak:** Mapper tidak lagi bergantung pada infrastructure layer secara langsung.

---

### 3. Purchase Order Trinity Data/LookupProvider Completion

**Masalah:** `PurchaseOrderController` sudah menggunakan `SupplierLookupProvider`, `CurrencyLookupProvider`, dan `PaymentMethodLookupProvider` untuk pre-edit, tetapi implementasi Trinity Data (kombinasi ketiga provider dalam satu bean) belum lengkap.

**Solusi:**
- Dibuat `PurchaseOrderTrinityData` record di `purchasing.purchaseorder.application.dto` dengan 3 field: `supplierLookup`, `currencyLookup`, `paymentMethodLookup`
- Update `PurchaseOrderServiceImpl` untuk menerima `PurchaseOrderTrinityData` di constructor
- Update bean `PurchaseOrderServiceImpl` di `PurchaseOrderConfig` untuk inject `PurchaseOrderTrinityData`
- Refactor method `getPurchaseOrderEditView` untuk menggunakan trinity data

**Dampak:** Controller tidak lagi inject 3 provider secara terpisah; lebih clean dan sesuai dengan pola Trinity Data.

---

### 4. Purchase Request Trinity Data/LookupProvider Completion

**Masalah:** Sama seperti PO — `PurchaseRequestController` sudah menggunakan `SupplierLookupProvider` dan `CurrencyLookupProvider`, tetapi belum dibuat Trinity Data pattern.

**Solusi:**
- Dibuat `PurchaseRequestTrinityData` record di `purchasing.purchaserequest.application.dto` dengan 2 field: `supplierLookup`, `currencyLookup`
- Update `PurchaseRequestServiceImpl` untuk menerima `PurchaseRequestTrinityData` di constructor
- Update bean `PurchaseRequestServiceImpl` di `PurchaseRequestConfig` untuk inject `PurchaseRequestTrinityData`
- Refactor method `getPurchaseRequestEditView` untuk menggunakan trinity data

**Dampak:** Controller lebih clean dan konsisten dengan pola di PO.

---

### 5. Geographic Parent `initialSubtext` Cleanup

**Masalah:** Halaman Geographic (Province/City/District/Village) menampilkan `initialSubtext=''` (kosong) untuk field `parentId`, padahal seharusnya menampilkan kode parent geographic (country code, province code, dll.) sesuai dengan pola LookupDto.

**Solusi:**
- Update `GeographicServiceImpl.getProvinceEditView()` untuk set `initialSubtext` = country code
- Update `GeographicServiceImpl.getCityEditView()` untuk set `initialSubtext` = province code
- Update `GeographicServiceImpl.getDistrictEditView()` untuk set `initialSubtext` = city code
- Update `GeographicServiceImpl.getVillageEditView()` untuk set `initialSubtext` = district code

**Dampak:** Autocomplete parent geographic sekarang menampilkan subtext (kode) dengan benar di form edit, konsisten dengan autocomplete lainnya.

---

### Ringkasan Cleanup

| Item | Status |
|------|--------|
| Approval read abstraction (News/PO/PR) | ✅ Done |
| Inventory report mapper using CurrencyLookupProvider | ✅ Done |
| PO Trinity Data/LookupProvider completion | ✅ Done |
| PR Trinity Data/LookupProvider completion | ✅ Done |
| Geographic parent `initialSubtext` cleanup | ✅ Done |

**Verifikasi:** `mvn clean test` pass — semua unit test green.
