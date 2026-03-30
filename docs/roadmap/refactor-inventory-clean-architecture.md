# Refactor Inventory Module — Clean Architecture (Vertical Slicing)

> **Status:** Planning  
> **Referensi DDD:** `com.solusi.erp.common.news`  
> **Referensi Slice Bersih:** `com.solusi.erp.master.tax`

---

## Temuan Discovery

> Semua klaim diverifikasi dengan membaca file aktual di codebase.

### 1. Package Horizontal yang Tersisa

Ditemukan **9 package horizontal** dengan total **≈67 file** di bawah
`src/main/java/com/solusi/erp/inventory/`:

#### `inventory/controller/` — 1 file

| File | Fitur | Consumer | Ada di Slice? |
|------|-------|----------|---------------|
| `InventoryReportController.java` | report | — | ❌ (package `report/` sudah ada tapi tanpa web layer) |

#### `inventory/dto/` — 26 file

| File | Fitur | Consumer Utama | Duplikat di Slice? |
|------|-------|----------------|---------------------|
| `BrandRequest.java` | brand | `BrandMapper` | ✅ → `brand/web/dto/BrandSaveRequest.java` |
| `BrandResponse.java` | brand | `BrandMapper` | ✅ → `brand/web/dto/BrandDetailResponse.java` |
| `ContainerRequest.java` | container | `ContainerPersistenceMapper` | ✅ → `container/web/dto/ContainerSaveRequest.java` |
| `ContainerResponse.java` | container | `ContainerPersistenceMapper` | ✅ → `container/web/dto/ContainerDetailResponse.java` |
| `FacilityRequest.java` | facility | `FacilityPersistenceMapper` | ✅ → `facility/web/dto/FacilitySaveRequest.java` |
| `FacilityResponse.java` | facility | `FacilityPersistenceMapper` | ✅ → `facility/web/dto/FacilityDetailResponse.java` |
| `GridRequest.java` | grid | `GridPersistenceMapper` | ✅ → `grid/web/dto/GridSaveRequest.java` |
| `GridResponse.java` | grid | `GridPersistenceMapper` | ✅ → `grid/web/dto/GridDetailResponse.java` |
| `InventoryMovementResponse.java` | stock | `InventoryMovementMapper`, `InventoryReportController` | ❌ perlu pindah ke `stock/` |
| `LocationStockDetailResponse.java` | stock | `StockServiceImpl`, `InventoryReportController` | ❌ perlu pindah ke `report/` atau `stock/` |
| `ProductCategoryRequest.java` | productcategory | `ProductCategoryMapper` | ✅ → `productcategory/web/dto/ProductCategorySaveRequest.java` |
| `ProductCategoryResponse.java` | productcategory | `ProductCategoryMapper` | ✅ → `productcategory/web/dto/ProductCategoryDetailResponse.java` |
| `ProductRequest.java` | product | controller, validation | ✅ → `product/web/dto/ProductSaveRequest.java` |
| `ProductResponse.java` | product | controller | ✅ → `product/web/dto/ProductDetailResponse.java` |
| `ProductStockSummaryResponse.java` | stock | `InventoryReportController` | ❌ perlu pindah ke `report/` |
| `ProductUomConversionRequest.java` | uomconversion | controller | ✅ → `uomconversion/web/dto/` |
| `ProductUomConversionResponse.java` | uomconversion | controller | ✅ → `uomconversion/web/dto/` |
| `StockAdjustmentLineRequest.java` | adjustment | `StockAdjustmentController` | ✅ → `adjustment/web/dto/StockAdjustmentSaveLineRequest.java` |
| `StockAdjustmentLineResponse.java` | adjustment | `StockAdjustmentController` | ✅ → `adjustment/web/dto/StockAdjustmentLineDetailResponse.java` |
| `StockAdjustmentRequest.java` | adjustment | `StockAdjustmentController` | ✅ → `adjustment/web/dto/StockAdjustmentSaveRequest.java` |
| `StockAdjustmentResponse.java` | adjustment | `StockAdjustmentController` | ✅ → `adjustment/web/dto/StockAdjustmentDetailResponse.java` |
| `StockCardFilter.java` | report | `InventoryReportController`, `GetStockCardUseCaseImpl` | ❌ perlu pindah ke `report/web/dto/` |
| `StockMovementPayload.java` | stock | `StockServiceImpl`, `ProcessStockAdjustmentUseCaseImpl` | ❌ perlu pindah ke `stock/application/dto/` |
| `UnitOfMeasureRequest.java` | uom | controller | ✅ → `uom/web/dto/UomSaveRequest.java` |
| `UnitOfMeasureResponse.java` | uom | controller | ✅ → `uom/web/dto/UomDetailResponse.java` |
| `UomConversionLookupDto.java` | uomconversion | controller | ✅ → `uomconversion/web/dto/` |

#### `inventory/form/` — 3 file

| File | Fitur | Consumer | Ada di Slice? |
|------|-------|----------|---------------|
| `ContainerUIForm.java` | container | `ContainerController` | ❌ perlu pindah ke `container/web/dto/` |
| `FacilityUIForm.java` | facility | `FacilityController` | ❌ perlu pindah ke `facility/web/dto/` |
| `ProductUomUIForm.java` | uomconversion | `UomConversionController` | ❌ perlu pindah ke `uomconversion/web/dto/` |

#### `inventory/mapper/` — 3 file

| File | Fitur | Consumer | Status |
|------|-------|----------|--------|
| `BrandMapper.java` | brand | Spring DI (legacy) | ❌ redundan — digantikan `brand/web/mapper/BrandWebMapper.java` |
| `InventoryMovementMapper.java` | stock | `GetStockCardUseCaseImpl` | ❌ perlu pindah ke `stock/infrastructure/` |
| `ProductCategoryMapper.java` | productcategory | Spring DI (legacy) | ❌ redundan — digantikan `productcategory/web/mapper/ProductCategoryWebMapper.java` |

#### `inventory/model/` — 13 entity + 4 enum

| File | Fitur | JPA Table | Duplikat Domain di Slice? |
|------|-------|-----------|---------------------------|
| `Brand.java` | brand | `brands` | ✅ domain di `brand/domain/model/Brand.java` |
| `Container.java` | container | `inv_containers` | ✅ domain di `container/domain/model/Container.java` |
| `Dimensions.java` | shared embeddable | `@Embeddable` | ❌ dipakai Container + Product |
| `Facility.java` | facility | `inv_facilities` | ✅ domain di `facility/domain/model/Facility.java` |
| `Grid.java` | grid | `inv_grids` | ✅ domain di `grid/domain/model/Grid.java` |
| `InventoryMovement.java` | stock | `inv_movements` | ❌ belum ada domain di slice mana pun |
| `ProductCategory.java` | productcategory | `product_categories` | ✅ domain di `productcategory/domain/model/ProductCategory.java` |
| `ProductUomConversion.java` | uomconversion | `product_uom_conversions` | ✅ domain di `uomconversion/domain/model/` |
| `StockAdjustment.java` | adjustment | `inv_stock_adjustments` | ✅ domain di `adjustment/domain/model/StockAdjustment.java` |
| `StockAdjustmentLine.java` | adjustment | `inv_stock_adjustment_lines` | ✅ domain di `adjustment/domain/model/StockAdjustmentLineItem.java` |
| `StockBalance.java` | stock | `inv_stock_balances` | ❌ belum ada domain di slice mana pun |
| `UnitOfMeasure.java` | uom | `unit_of_measures` | ✅ domain di `uom/domain/model/UnitOfMeasure.java` |
| `ValuationLayer.java` | stock | `inv_valuation_layers` | ❌ belum ada domain di slice mana pun |
| `MovementType.java` | stock (enum) | — | ❌ belum ada di slice mana pun |
| `ReferenceType.java` | stock (enum) | — | ❌ belum ada di slice mana pun |
| `ProductCategoryType.java` | productcategory (enum) | — | ❌ belum ada di `productcategory/domain/model/` |
| `UomType.java` | uom (enum) | — | ❌ belum ada di `uom/domain/model/` |

#### `inventory/repository/` — 12 file (semua Spring Data JPA)

| File | Fitur | Consumer Utama |
|------|-------|----------------|
| `BrandRepository.java` | brand | `brand/infrastructure/adapter/BrandRepositoryImpl.java` |
| `ContainerRepository.java` | container | `container/infrastructure/adapter/ContainerRepositoryImpl.java`, `StockServiceImpl` |
| `FacilityRepository.java` | facility | `facility/infrastructure/adapter/FacilityRepositoryImpl.java` |
| `GridRepository.java` | grid | `grid/infrastructure/adapter/GridRepositoryImpl.java` |
| `InventoryMovementRepository.java` | stock | `StockServiceImpl`, `GetStockCardUseCaseImpl` |
| `ProductCategoryRepository.java` | productcategory | `productcategory/infrastructure/adapter/`, `product/web/mapper/ProductWebMapper.java` |
| `ProductUomConversionRepository.java` | uomconversion | `uomconversion/infrastructure/adapter/`, `UomConversionServiceImpl` |
| `StockAdjustmentLineRepository.java` | adjustment | `adjustment/infrastructure/adapter/StockAdjustmentRepositoryImpl.java` |
| `StockAdjustmentRepository.java` | adjustment | `adjustment/infrastructure/adapter/StockAdjustmentRepositoryImpl.java` |
| `StockBalanceRepository.java` | stock | `StockServiceImpl`, report use cases |
| `UnitOfMeasureRepository.java` | uom | `uom/infrastructure/adapter/`, `UomConversionServiceImpl`, `ProductWebMapper` |
| `ValuationLayerRepository.java` | stock | `ValuationServiceImpl` |

#### `inventory/service/` — 6 file

| File | Fitur | Consumer |
|------|-------|----------|
| `StockService.java` | stock | `ProcessStockAdjustmentUseCaseImpl`, `StockAdjustmentConfig` |
| `StockServiceImpl.java` | stock | Spring DI via `StockService` |
| `UomConversionService.java` | uomconversion | `StockServiceImpl`, `UomConversionConfig` |
| `UomConversionServiceImpl.java` | uomconversion | Spring DI via `UomConversionService` |
| `ValuationService.java` | stock | `StockServiceImpl` |
| `ValuationServiceImpl.java` | stock | Spring DI via `ValuationService` |

#### `inventory/util/` — 1 file

| File | Fitur | Consumer |
|------|-------|----------|
| `SerialNumberGenerator.java` | stock | `StockServiceImpl` |

#### `inventory/validation/` — 2 file

| File | Fitur | Consumer |
|------|-------|----------|
| `ValidUomMeasurement.java` | product | `ProductRequest` (horizontal dto) |
| `UomMeasurementValidator.java` | product | `ValidUomMeasurement` |

---

### 2. Vertical Slice — Status Tiap Layer

Semua 9 slice sudah punya struktur `domain/`, `application/`, `infrastructure/`, dan
`web/`, **tetapi** `infrastructure/persistence/` di semua slice (kecuali `product/`)
masih mendelegasikan ke package horizontal `inventory/model/` (JPA entity) dan
`inventory/repository/` (Spring Data JPA). Belum ada `[Feature]Entity.java` dan
`[Feature]JpaRepository.java` di dalam slice sendiri.

| Slice | Domain Model | Domain Repo Port | App Use Cases | JPA Entity di Slice? | Spring Data di Slice? | Web Layer | Import Horizontal? |
|-------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| `brand` | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ `inventory.model`, `inventory.repository` |
| `container` | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ `inventory.model`, `inventory.repository` |
| `facility` | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ + master JpaRepository langsung |
| `grid` | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ `inventory.model`, `inventory.repository` |
| `product` | ✅ | ✅ | ✅ | ✅ `ProductEntity.java` | ✅ `JpaProductRepository.java` | ✅ | ✅ `inventory.model.Brand`, `.ProductCategory`, `.UnitOfMeasure`; `inventory.repository.*` |
| `productcategory` | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ `inventory.model`, `inventory.repository` |
| `uom` | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ `inventory.model.UomType` (15 referensi) |
| `uomconversion` | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ 3 referensi ke horizontal |
| `adjustment` | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ `inventory.model.StockAdjustmentLine`, `.MovementType`, `.ReferenceType`; `inventory.service.StockService` |
| `report` | — | — | ✅ (partial) | — | — | ❌ `InventoryReportController` masih di horizontal `controller/` | ✅ `inventory.repository.StockBalanceRepository` |

---

### 3. Coupling Antar Domain yang Harus Diputus

#### Coupling Inventory → Master

| Lokasi | Import | Masalah | Solusi |
|--------|--------|---------|--------|
| `facility/infrastructure/persistence/FacilityPersistenceMapper.java` | `master.geographic.infrastructure.persistence.GeographicJpaRepository` | Infrastructure detail master bocor ke inventory | Buat `GeographicPort` di `facility/application/port/`, implementasi di `facility/infrastructure/adapter/` |
| `facility/infrastructure/persistence/FacilityPersistenceMapper.java` | `master.party.infrastructure.persistence.PartyJpaRepository` | Infrastructure detail master bocor ke inventory | Buat `PartyPort` di `facility/application/port/`, implementasi di `facility/infrastructure/adapter/` |
| `adjustment/web/controller/StockAdjustmentController.java` | `master.currency.application.usecase.query.FindActiveCurrenciesUseCase` | Boleh — gunakan use case bukan domain model | Pertahankan, tapi hapus impor `Currency.java` domain model dan `CurrencyWebMapper` |
| `adjustment/web/controller/StockAdjustmentController.java` | `master.currency.domain.model.Currency` | Domain model antar modul tidak boleh saling referensi | Ganti dengan `CurrencySummaryResponse` dari web DTO master atau buat local record |
| `adjustment/web/controller/StockAdjustmentController.java` | `master.currency.web.mapper.CurrencyWebMapper` | Web mapper modul lain tidak boleh diimpor | Pindah tanggung jawab mapping ke dalam `StockAdjustmentWebMapper` |
| `adjustment/application/usecase/command/CreateStockAdjustmentUseCaseImpl.java` | `master.currency.domain.repository.CurrencyRepository` | Repository domain modul lain tidak boleh diakses langsung | Buat port `CurrencyValidationPort` di `adjustment/application/port/` |
| `adjustment/application/usecase/command/UpdateStockAdjustmentUseCaseImpl.java` | `master.currency.domain.repository.CurrencyRepository` | Sama | Sama |
| `inventory/mapper/InventoryMovementMapper.java` | `master.currency.domain.repository.CurrencyRepository` | Sama | Gunakan `currencyId` + resolve alias via port di `stock/application/port/CurrencyAliasPort.java` |

#### Coupling Intra-Inventory: @ManyToOne yang Harus Diputus

| Entity (Horizontal Model) | Field | Target | Solusi |
|---------------------------|-------|--------|--------|
| `inventory/model/Grid.java` | `facility` | `Facility` | Ganti `@ManyToOne Facility facility` → `Long facilityId` + `String facilityName` di entity baru `GridEntity.java` |
| `inventory/model/Container.java` | `facility` (melalui Grid) | `Facility` | Ganti `@ManyToOne Grid grid` → `Long gridId` + `String gridCode`, `Long facilityId` di `ContainerEntity.java` |
| `inventory/model/ProductUomConversion.java` | `product`, `fromUom`, `toUom` | `Product`, `UnitOfMeasure` | Ganti dengan `productId`, `fromUomId`, `toUomId` (Long) |
| `inventory/model/StockAdjustmentLine.java` | `product`, `sourceFacility`, `destinationFacility`, `uom` | 4 entity | Ganti semua dengan Long ID references |
| `inventory/model/StockAdjustment.java` | `currency` | `Currency` (master) | Sudah ada `currencyId` di domain; entity baru tidak boleh punya `@ManyToOne Currency` |
| `inventory/model/InventoryMovement.java` | `product`, `location` | `Product`, `Container` | Ganti dengan `productId`, `containerId` (Long) |
| `inventory/model/StockBalance.java` | `product`, `location` | `Product`, `Container` | Ganti dengan `productId`, `containerId` (Long) |
| `inventory/model/ValuationLayer.java` | `product`, `location` | `Product`, `Container` | Ganti dengan `productId`, `containerId` (Long) |
| `product/infrastructure/persistence/ProductEntity.java` | `brand`, `productCategory`, `unitOfMeasure` | 3 inventory entity | Ganti dengan `brandId`, `productCategoryId`, `unitOfMeasureId` (Long) |

> **Catatan:** @ManyToOne yang tetap dipertahankan (intentional):
> - `StockAdjustmentLine` ↔ `StockAdjustment` (parent-child, satu aggregate, boleh)

---

### 4. Shared Concern yang Ditemukan

Berikut class/type yang dipakai **lintas slice** — perlu home yang tepat:

| Concern | Dipakai Oleh | Solusi |
|---------|-------------|--------|
| `MovementType` (enum) | `StockServiceImpl`, `ProcessStockAdjustmentUseCaseImpl`, `InventoryMovementMapper` | Pindah ke `stock/domain/model/MovementType.java` (slice baru) |
| `ReferenceType` (enum) | `StockServiceImpl`, `ProcessStockAdjustmentUseCaseImpl`, `StockMovementPayload` | Pindah ke `stock/domain/model/ReferenceType.java` |
| `UomType` (enum) | `UnitOfMeasure`, `UomSaveRequest`, `UomDetailResponse`, `UomRepository` (15 file) | Pindah ke `uom/domain/model/UomType.java` |
| `ProductCategoryType` (enum) | `ProductCategory`, `ProductCategoryRequest/Response`, `ProductCategoryMapper` | Pindah ke `productcategory/domain/model/ProductCategoryType.java` |
| `Dimensions` (embeddable) | `inventory/model/Container.java`, `inventory/model/Product.java` | Buat `inventory/shared/embeddable/Dimensions.java` |
| `SerialNumberGenerator` | `StockServiceImpl` | Pindah ke `stock/domain/util/SerialNumberGenerator.java` |
| `StockMovementPayload` | `StockServiceImpl`, `ProcessStockAdjustmentUseCaseImpl` | Pindah ke `stock/application/dto/StockMovementPayload.java` |
| `StockService` (interface) | `ProcessStockAdjustmentUseCaseImpl` | Dijadikan use case port: `AdjustStockUseCase` di `stock/application/usecase/command/` |

---

## Rekomendasi Utama ⭐

**Masalah inti:** Semua 9 slice sudah punya struktur domain/application/web yang relatif
bersih, tetapi `infrastructure/persistence/` di setiap slice **masih menunjuk ke package
horizontal** (`inventory/model/` untuk JPA entity dan `inventory/repository/` untuk
Spring Data JPA). Ini adalah satu-satunya gap teknis utama yang menghalangi penghapusan
package horizontal.

**Pendekatan yang direkomendasikan: Migrasi persistence per-slice, berurutan dari paling
tidak ada dependency ke paling banyak dependency.**

Untuk setiap slice, pola kerja yang sama diulang:
1. Buat `[Feature]Entity.java` (JPA entity baru) di `[slice]/infrastructure/persistence/`
2. Buat `[Feature]JpaRepository.java` di `[slice]/infrastructure/persistence/`
3. Update `[Feature]PersistenceMapper.java` agar mapping dari entity lokal (bukan horizontal)
4. Update `[Feature]RepositoryImpl.java` (adapter) agar inject `[Feature]JpaRepository` lokal
5. Hapus entry dari `inventory/model/` dan `inventory/repository/` setelah tidak ada consumer

Tambahan:
- Buat **`inventory/stock/`** sebagai slice baru untuk `InventoryMovement`, `StockBalance`,
  `ValuationLayer`, `StockService`, `ValuationService` — karena ketiganya bukan milik satu
  fitur existing melainkan cross-cutting stock operation concern.
- Buat **`inventory/shared/`** untuk `Dimensions` embeddable dan typedef yang benar-benar
  lintas batas.
- Putus coupling ke master module menggunakan **port interface** di application layer —
  mengikuti pola `PartyReference` yang sudah ada di `master/party/`.

## Alternatif

**Alternatif A: Biarkan `@ManyToOne` intra-inventory di persistence layer.**  
Lebih cepat dikerjakan. Tradeoff: coupling tetap ada di level JPA entity, bukan di domain
model. Dapat diterima jika tim menganggap intra-inventory coupling sebagai intentional.
Tidak direkomendasikan karena menyulitkan testing per-slice.

**Alternatif B: Gabungkan `stock/` ke dalam `adjustment/` slice.**  
Karena StockService hanya dipanggil dari adjustment saat ini. Tradeoff: jika nantinya ada
fitur Goods Receipt, Delivery Order, dll. yang juga butuh stock adjustment, slice adjustment
akan terlalu besar. Tidak direkomendasikan.

**Alternatif C: Pertahankan `InventoryMovementMapper` di horizontal `mapper/`.**  
Karena complexity-nya tinggi (CurrencyRepository + nested path mapping). Tradeoff: delay
phase cleanup. Dapat dijadikan opsi jika waktu terbatas, dengan catatan harus diselesaikan
sebelum phase 13.

---

## Phase 1 — Foundation: Enum Migration + `inventory/shared/`

> **Dependency:** Tidak ada — harus dikerjakan pertama karena hampir semua phase berikutnya
> bergantung pada lokasi enum yang benar.

### Enum Relocation

- [ ] Buat `src/main/java/com/solusi/erp/inventory/uom/domain/model/UomType.java`  
  Salin isi dari `inventory/model/UomType.java`
- [ ] Update semua 15 file yang mengimport `com.solusi.erp.inventory.model.UomType` → ubah ke  
  `com.solusi.erp.inventory.uom.domain.model.UomType`  
  File-file tersebut: `UomSaveRequest.java`, `UomDetailResponse.java`, `UomRepository.java` (port),
  `UomSaveRequest.java`, semua file di `uom/` slice yang menggunakan `UomType`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/productcategory/domain/model/ProductCategoryType.java`  
  Salin isi dari `inventory/model/ProductCategoryType.java`
- [ ] Update semua file yang mengimport `com.solusi.erp.inventory.model.ProductCategoryType`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/UomType.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/ProductCategoryType.java`

### Buat `inventory/shared/`

- [ ] Buat `src/main/java/com/solusi/erp/inventory/shared/embeddable/Dimensions.java`  
  Pindahkan `@Embeddable Dimensions` dari `inventory/model/Dimensions.java` ke sini
- [ ] Update import `com.solusi.erp.inventory.model.Dimensions` → `com.solusi.erp.inventory.shared.embeddable.Dimensions`  
  di: `inventory/model/Container.java`, `product/infrastructure/persistence/ProductEntity.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/Dimensions.java`

**Acceptance Criteria Phase 1:**
- [ ] `rg "import com.solusi.erp.inventory.model.UomType" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.model.ProductCategoryType" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.model.Dimensions" src` → 0 hasil
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 2 — UOM Slice: Persistence Migration

> **Dependency:** Phase 1 (UomType sudah di `uom/domain/model/`)

- [ ] Buat `src/main/java/com/solusi/erp/inventory/uom/infrastructure/persistence/UomEntity.java`  
  JPA entity (`@Entity @Table(name="unit_of_measures")`), fields: `id`, `code`, `name`,
  `type` (UomType), extends `BaseModel`; TIDAK ada `@ManyToOne`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/uom/infrastructure/persistence/UomJpaRepository.java`  
  Interface extends `JpaRepository<UomEntity, Long>` dengan query methods:
  `search(keyword, pageable)`, `findByType(UomType)`, `existsByCode()`,
  `existsByCodeAndIdNot()`
- [ ] Update `src/main/java/com/solusi/erp/inventory/uom/infrastructure/persistence/UomPersistenceMapper.java`  
  Ubah mapping dari `inventory.model.UnitOfMeasure` → `UomEntity` (lokal)
- [ ] Update `src/main/java/com/solusi/erp/inventory/uom/infrastructure/adapter/UomRepositoryImpl.java`  
  Inject `UomJpaRepository` (lokal) gantikan `inventory.repository.UnitOfMeasureRepository`
- [ ] Update `src/main/java/com/solusi/erp/inventory/uom/infrastructure/config/UomConfig.java`  
  Pastikan bean `UomJpaRepository` diinjeksi ke adapter
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/UnitOfMeasure.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/UnitOfMeasureRepository.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/UnitOfMeasureRequest.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/UnitOfMeasureResponse.java`

**Acceptance Criteria Phase 2:**
- [ ] `rg "import com.solusi.erp.inventory.model.UnitOfMeasure" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.repository.UnitOfMeasureRepository" src` → 0 hasil
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 3 — Brand Slice: Persistence Migration

> **Dependency:** Tidak ada dependency ke slice lain

- [ ] Buat `src/main/java/com/solusi/erp/inventory/brand/infrastructure/persistence/BrandEntity.java`  
  JPA entity (`@Entity @Table(name="brands")`), extends `BaseModel`, TIDAK ada `@ManyToOne`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/brand/infrastructure/persistence/BrandJpaRepository.java`  
  Interface extends `JpaRepository<BrandEntity, Long>` dengan query methods:
  `search(keyword, pageable)`, `existsByCode()`, `existsByCodeAndIdNot()`
- [ ] Update `src/main/java/com/solusi/erp/inventory/brand/infrastructure/persistence/BrandPersistenceMapper.java`  
  Ubah dari mapping `inventory.model.Brand` → `BrandEntity` (lokal)
- [ ] Update `src/main/java/com/solusi/erp/inventory/brand/infrastructure/adapter/BrandRepositoryImpl.java`  
  Inject `BrandJpaRepository` lokal, hapus `inventory.repository.BrandRepository`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/Brand.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/BrandRepository.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/BrandRequest.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/BrandResponse.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/mapper/BrandMapper.java`  
  (redundan, sudah digantikan `brand/web/mapper/BrandWebMapper.java`)

**Acceptance Criteria Phase 3:**
- [ ] `rg "import com.solusi.erp.inventory.model.Brand" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.repository.BrandRepository" src` → 0 hasil
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 4 — ProductCategory Slice: Persistence Migration

> **Dependency:** Phase 1 (ProductCategoryType sudah di `productcategory/domain/model/`)

- [ ] Buat `src/main/java/com/solusi/erp/inventory/productcategory/infrastructure/persistence/ProductCategoryEntity.java`  
  JPA entity (`@Entity @Table(name="product_categories")`), fields: `id`, `code`, `name`,
  `type` (ProductCategoryType), `note`, extends `BaseModel`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/productcategory/infrastructure/persistence/ProductCategoryJpaRepository.java`  
  Query methods: `search()`, `existsByCode()`, `existsByCodeAndIdNot()`
- [ ] Update `src/main/java/com/solusi/erp/inventory/productcategory/infrastructure/persistence/ProductCategoryPersistenceMapper.java`  
  Mapping dari `ProductCategoryEntity` lokal (bukan `inventory.model.ProductCategory`)
- [ ] Update `src/main/java/com/solusi/erp/inventory/productcategory/infrastructure/adapter/ProductCategoryRepositoryImpl.java`  
  Inject `ProductCategoryJpaRepository` lokal
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/ProductCategory.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/ProductCategoryRepository.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/ProductCategoryRequest.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/ProductCategoryResponse.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/mapper/ProductCategoryMapper.java`  
  (redundan, sudah digantikan `productcategory/web/mapper/ProductCategoryWebMapper.java`)

**Acceptance Criteria Phase 4:**
- [ ] `rg "import com.solusi.erp.inventory.model.ProductCategory" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.repository.ProductCategoryRepository" src` → 0 hasil
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 5 — Facility Slice: Persistence + Master Decoupling

> **Dependency:** Tidak ada dependency ke slice inventory lain

### Putus Coupling ke Master Infrastructure

- [ ] Buat `src/main/java/com/solusi/erp/inventory/facility/application/port/PartyNamePort.java`  
  Interface: `String findNameById(Long partyId)`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/facility/application/port/CityNamePort.java`  
  Interface: `String findNameById(Long cityId)`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/facility/infrastructure/adapter/PartyNameAdapter.java`  
  Implements `PartyNamePort`, inject `master.party.infrastructure.persistence.PartyJpaRepository`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/facility/infrastructure/adapter/CityNameAdapter.java`  
  Implements `CityNamePort`, inject `master.geographic.infrastructure.persistence.GeographicJpaRepository`
- [ ] Update `src/main/java/com/solusi/erp/inventory/facility/infrastructure/persistence/FacilityPersistenceMapper.java`  
  Inject `PartyNamePort` + `CityNamePort` (bukan JpaRepository langsung)
- [ ] Update `src/main/java/com/solusi/erp/inventory/facility/infrastructure/config/FacilityConfig.java`  
  Daftarkan bean `PartyNameAdapter` dan `CityNameAdapter`

### Persistence Migration

- [ ] Buat `src/main/java/com/solusi/erp/inventory/facility/infrastructure/persistence/FacilityEntity.java`  
  JPA entity (`@Entity @Table(name="inv_facilities")`), fields: `id`, `code`, `name`,
  `ownerId` (Long), `isActive`, `@Embedded Address`; extends `BaseModel`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/facility/infrastructure/persistence/FacilityJpaRepository.java`  
  Query methods: `search(keyword, pageable)`, `existsByCode()`, `existsByCodeAndIdNot()`
- [ ] Update `FacilityPersistenceMapper.java` — mapping dari `FacilityEntity` lokal
- [ ] Update `src/main/java/com/solusi/erp/inventory/facility/infrastructure/adapter/FacilityRepositoryImpl.java`  
  Inject `FacilityJpaRepository` lokal
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/Facility.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/FacilityRepository.java`
- [ ] Pindahkan `FacilityUIForm.java` → `src/main/java/com/solusi/erp/inventory/facility/web/dto/FacilityUIForm.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/FacilityRequest.java` (duplikat slice)
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/FacilityResponse.java` (duplikat slice)
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/form/FacilityUIForm.java`

**Acceptance Criteria Phase 5:**
- [ ] `rg "import com.solusi.erp.inventory.model.Facility" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.repository.FacilityRepository" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository" src/main/java/com/solusi/erp/inventory` → 0 hasil
- [ ] `rg "import com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository" src/main/java/com/solusi/erp/inventory` → 0 hasil
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 6 — Grid Slice: Persistence Migration

> **Dependency:** Phase 5 (Facility sudah clean — tidak ada @ManyToOne Facility di GridEntity baru)

- [ ] Buat `src/main/java/com/solusi/erp/inventory/grid/infrastructure/persistence/GridEntity.java`  
  JPA entity (`@Entity @Table(name="inv_grids")`), fields: `id`, `facilityId` (Long),
  `code`, `name`, `isActive`; extends `BaseModel`  
  **Perhatian:** TIDAK ada `@ManyToOne Facility` — gunakan `facilityId: Long`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/grid/infrastructure/persistence/GridJpaRepository.java`  
  Query methods: `search(facilityId, keyword, pageable)`, `findByFacilityId(Long)`,
  `existsByFacilityIdAndCode()`, `existsByFacilityIdAndCodeAndIdNot()`
- [ ] Update `src/main/java/com/solusi/erp/inventory/grid/infrastructure/persistence/GridPersistenceMapper.java`  
  Resolve `facilityName` via `FacilityRepository` port (bukan langsung dari @ManyToOne)
- [ ] Update `src/main/java/com/solusi/erp/inventory/grid/infrastructure/adapter/GridRepositoryImpl.java`  
  Inject `GridJpaRepository` lokal
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/Grid.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/GridRepository.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/GridRequest.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/GridResponse.java`

**Acceptance Criteria Phase 6:**
- [ ] `rg "import com.solusi.erp.inventory.model.Grid" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.repository.GridRepository" src` → 0 hasil
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 7 — Container Slice: Persistence Migration

> **Dependency:** Phase 6 (Grid clean), Phase 1 (Dimensions sudah di `inventory/shared/`)

- [ ] Buat `src/main/java/com/solusi/erp/inventory/container/infrastructure/persistence/ContainerEntity.java`  
  JPA entity (`@Entity @Table(name="inv_containers")`), fields: `id`, `gridId` (Long),
  `facilityId` (Long — denormalized untuk query), `code`, `name`, `barcode`,
  `@Embedded Dimensions`, `maxWeight`, `isActive`; extends `BaseModel`  
  **Perhatian:** TIDAK ada `@ManyToOne Grid`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/container/infrastructure/persistence/ContainerJpaRepository.java`  
  Query methods: `search()`, `searchByGrid()`, `searchByFacility()`, `findByGridId()`,
  `existsByGridIdAndCode()`, `existsByGridIdAndCodeAndIdNot()`,
  `existsByBarcode()`, `existsByBarcodeAndIdNot()`
- [ ] Update `src/main/java/com/solusi/erp/inventory/container/infrastructure/persistence/ContainerPersistenceMapper.java`  
  Mapping dari `ContainerEntity` lokal; resolve `gridName`, `facilityName` via domain query
- [ ] Update `src/main/java/com/solusi/erp/inventory/container/infrastructure/adapter/ContainerRepositoryImpl.java`  
  Inject `ContainerJpaRepository` lokal
- [ ] Pindahkan `ContainerUIForm.java` → `src/main/java/com/solusi/erp/inventory/container/web/dto/ContainerUIForm.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/Container.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/ContainerRepository.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/ContainerRequest.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/ContainerResponse.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/form/ContainerUIForm.java`

**Acceptance Criteria Phase 7:**
- [ ] `rg "import com.solusi.erp.inventory.model.Container" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.repository.ContainerRepository" src/main/java/com/solusi/erp/inventory` → 0 hasil  
  (masih ada referensi dari `StockServiceImpl` yang diselesaikan di Phase 10)
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 8 — Product Slice: Decouple dari Intra-Inventory + Validasi

> **Dependency:** Phase 2 (UOM), Phase 3 (Brand), Phase 4 (ProductCategory) — karena
> `ProductEntity.java` akan menggunakan ID references ke tiga entity tersebut

### Decouple @ManyToOne di ProductEntity

- [ ] Update `src/main/java/com/solusi/erp/inventory/product/infrastructure/persistence/ProductEntity.java`  
  Ganti:
  - `@ManyToOne Brand brand` → `Long brandId`
  - `@ManyToOne ProductCategory productCategory` → `Long productCategoryId`
  - `@ManyToOne UnitOfMeasure unitOfMeasure` → `Long unitOfMeasureId`
  - `@ManyToOne Company company` → `Long companyId` (jika ada)
  - `@ManyToOne Facility warehouse` → `Long warehouseId` (jika ada)

### Decouple ProductPersistenceMapper dari Horizontal Repositories

- [ ] Update `src/main/java/com/solusi/erp/inventory/product/infrastructure/persistence/ProductPersistenceMapper.java`  
  Hapus inject `inventory.repository.BrandRepository`,
  `inventory.repository.ProductCategoryRepository`,
  `inventory.repository.UnitOfMeasureRepository`  
  Resolve nama brand/category/uom via lookup use case atau domain port

### Decouple ProductWebMapper dari Horizontal Repositories

- [ ] Update `src/main/java/com/solusi/erp/inventory/product/web/mapper/ProductWebMapper.java`  
  Hapus inject `inventory.repository.BrandRepository`,
  `inventory.repository.ProductCategoryRepository`,
  `inventory.repository.UnitOfMeasureRepository`  
  Gunakan `GetBrandLookupUseCase`, `GetProductCategoryLookupUseCase`, `GetUomLookupUseCase`
  (sudah ada di masing-masing slice)

### Pindahkan Validation ke Slice Product

- [ ] Pindahkan `src/main/java/com/solusi/erp/inventory/validation/ValidUomMeasurement.java`  
  → `src/main/java/com/solusi/erp/inventory/product/web/validation/ValidUomMeasurement.java`
- [ ] Pindahkan `src/main/java/com/solusi/erp/inventory/validation/UomMeasurementValidator.java`  
  → `src/main/java/com/solusi/erp/inventory/product/web/validation/UomMeasurementValidator.java`
- [ ] Update import di `ProductSaveRequest.java` (slice web dto)
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/validation/ValidUomMeasurement.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/validation/UomMeasurementValidator.java`

### Hapus Horizontal Product DTOs (sudah ada duplikat di slice)

- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/ProductRequest.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/ProductResponse.java`

**Acceptance Criteria Phase 8:**
- [ ] `rg "import com.solusi.erp.inventory.model\.(Brand|ProductCategory|UnitOfMeasure)" src/main/java/com/solusi/erp/inventory/product` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.repository\.(Brand|ProductCategory|UnitOfMeasure)Repository" src/main/java/com/solusi/erp/inventory/product` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.validation" src` → 0 hasil
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 9 — UomConversion Slice: Service + Persistence Migration

> **Dependency:** Phase 2 (UOM), Phase 8 (Product)

### Pindahkan UomConversionService ke Slice

- [ ] Pindahkan `src/main/java/com/solusi/erp/inventory/service/UomConversionService.java`  
  → `src/main/java/com/solusi/erp/inventory/uomconversion/domain/service/UomConversionService.java`
- [ ] Pindahkan `src/main/java/com/solusi/erp/inventory/service/impl/UomConversionServiceImpl.java`  
  → `src/main/java/com/solusi/erp/inventory/uomconversion/domain/service/UomConversionServiceImpl.java`  
  Ubah agar inject `UomConversionRepository` (domain port) + `UomRepository` (domain port),
  bukan `ProductUomConversionRepository` + `UnitOfMeasureRepository` (horizontal)
- [ ] Update `src/main/java/com/solusi/erp/inventory/uomconversion/infrastructure/config/UomConversionConfig.java`  
  Daftarkan `UomConversionServiceImpl` sebagai bean

### Persistence Migration

- [ ] Buat `src/main/java/com/solusi/erp/inventory/uomconversion/infrastructure/persistence/UomConversionEntity.java`  
  JPA entity (`@Entity @Table(name="product_uom_conversions")`), fields: `id`,
  `productId` (Long), `fromUomId` (Long), `toUomId` (Long), `conversionFactor`; extends `BaseModel`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/uomconversion/infrastructure/persistence/UomConversionJpaRepository.java`  
  Query methods dari `inventory/repository/ProductUomConversionRepository.java`
- [ ] Update `src/main/java/com/solusi/erp/inventory/uomconversion/infrastructure/persistence/UomConversionPersistenceMapper.java`  
  Mapping dari `UomConversionEntity` lokal
- [ ] Update `src/main/java/com/solusi/erp/inventory/uomconversion/infrastructure/adapter/UomConversionRepositoryImpl.java`  
  Inject `UomConversionJpaRepository` lokal
- [ ] Pindahkan `ProductUomUIForm.java` → `src/main/java/com/solusi/erp/inventory/uomconversion/web/dto/ProductUomUIForm.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/ProductUomConversion.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/ProductUomConversionRepository.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/ProductUomConversionRequest.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/ProductUomConversionResponse.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/UomConversionLookupDto.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/form/ProductUomUIForm.java`

**Acceptance Criteria Phase 9:**
- [ ] `rg "import com.solusi.erp.inventory.model.ProductUomConversion" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.repository.ProductUomConversionRepository" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.service.UomConversionService" src` → 0 hasil
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 10 — Slice Baru: `inventory/stock/`

> **Dependency:** Phase 7 (Container clean), Phase 8 (Product clean), Phase 9 (UomConversion clean)

Buat slice baru `inventory/stock/` untuk menampung shared stock operations yang belum
punya home di slice manapun: `InventoryMovement`, `StockBalance`, `ValuationLayer`,
`StockService`, `ValuationService`, `SerialNumberGenerator`.

### Domain Layer

- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/domain/model/MovementType.java`  
  Pindahkan dari `inventory/model/MovementType.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/domain/model/ReferenceType.java`  
  Pindahkan dari `inventory/model/ReferenceType.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/domain/model/StockBalance.java`  
  Pure domain model (no JPA), fields: `id`, `productId`, `containerId`, `serialNumber`,
  `onHand`, `reserved`, `available`, `inTransit`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/domain/model/InventoryMovement.java`  
  Pure domain model, fields: `id`, `productId`, `containerId`, `serialNumber`, `quantity`,
  `movementType` (MovementType), `referenceType` (ReferenceType), `referenceId`,
  `referenceCode`, `currencyId`, `unitCost`, `localUnitCost`, `transactionDate`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/domain/model/ValuationLayer.java`  
  Pure domain model, fields: `id`, `productId`, `containerId`, `serialNumber`,
  `initialQuantity`, `remainingQuantity`, `unitCost` (CurrencyAmount), `localUnitCost`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/domain/repository/StockBalanceRepository.java`  
  Port interface: `findByProductIdAndContainerIdAndSerialNumber()`, `save()`,
  `getOnHandSummary()`, `getOnHandDetail()`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/domain/repository/InventoryMovementRepository.java`  
  Port interface: `save()`, `search(productId, containerId, startDate, endDate, pageable)`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/domain/repository/ValuationLayerRepository.java`  
  Port interface: `save()`, `findLayersByProductContainerSerial()`, `findRemainingLayers()`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/domain/util/SerialNumberGenerator.java`  
  Pindahkan dari `inventory/util/SerialNumberGenerator.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/domain/service/ValuationDomainService.java`  
  Pure Java (no Spring), extrak logika FIFO dari `ValuationServiceImpl`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/domain/service/StockDomainService.java`  
  Pure Java (no Spring), extrak core logic dari `StockServiceImpl`

### Application Layer

- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/application/port/UomConversionPort.java`  
  Interface: `BigDecimal convertToBaseUom(Long productId, Long sourceUomId, BigDecimal qty)`  
  (Implementasi di `uomconversion/infrastructure/adapter/UomConversionPortAdapter.java`)
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/application/port/CurrencyAliasPort.java`  
  Interface: `String findAlias(Long currencyId)`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/application/dto/StockMovementPayload.java`  
  Pindahkan dari `inventory/dto/StockMovementPayload.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/application/usecase/command/AdjustStockUseCase.java`  
  Interface: `void adjust(StockMovementPayload payload)`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/application/usecase/command/AdjustStockUseCaseImpl.java`  
  Implementasi, extrak dari `StockServiceImpl` — inject domain repositories + services + ports

### Infrastructure Layer

- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/StockBalanceEntity.java`  
  JPA entity (`@Entity @Table(name="inv_stock_balances")`), gunakan `productId` + `containerId` (Long)
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/StockBalanceJpaRepository.java`  
  Query methods dari `inventory/repository/StockBalanceRepository.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/StockBalancePersistenceMapper.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/adapter/StockBalanceRepositoryImpl.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementEntity.java`  
  JPA entity (`@Entity @Table(name="inv_movements")`), gunakan `productId` + `containerId` (Long),
  `@Embedded CurrencyAmount` untuk unit cost
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementJpaRepository.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementPersistenceMapper.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/adapter/InventoryMovementRepositoryImpl.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/ValuationLayerEntity.java`  
  JPA entity (`@Entity @Table(name="inv_valuation_layers")`)
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/ValuationLayerJpaRepository.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/ValuationLayerPersistenceMapper.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/adapter/ValuationLayerRepositoryImpl.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/adapter/CurrencyAliasAdapter.java`  
  Implements `CurrencyAliasPort`, inject `master.currency.domain.repository.CurrencyRepository`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/uomconversion/infrastructure/adapter/UomConversionPortAdapter.java`  
  Implements `stock/application/port/UomConversionPort.java` menggunakan `UomConversionService`
- [ ] Update/pindahkan `inventory/mapper/InventoryMovementMapper.java`  
  → `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementPersistenceMapper.java`  
  Ganti inject `CurrencyRepository` langsung → inject `CurrencyAliasPort`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/stock/infrastructure/config/StockConfig.java`  
  Composition root: wiring semua bean stock slice

### Hapus File Lama

- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/InventoryMovement.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/StockBalance.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/ValuationLayer.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/MovementType.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/ReferenceType.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/InventoryMovementRepository.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/StockBalanceRepository.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/ValuationLayerRepository.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/service/StockService.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/service/impl/StockServiceImpl.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/service/ValuationService.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/service/impl/ValuationServiceImpl.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/util/SerialNumberGenerator.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/StockMovementPayload.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/mapper/InventoryMovementMapper.java`

**Acceptance Criteria Phase 10:**
- [ ] `rg "import com.solusi.erp.inventory.model\.(InventoryMovement|StockBalance|ValuationLayer|MovementType|ReferenceType)" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.service\.(StockService|ValuationService)" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.repository\.(StockBalance|InventoryMovement|ValuationLayer)Repository" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.util.SerialNumberGenerator" src` → 0 hasil
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 11 — Adjustment Slice: Full Cleanup + Currency Decoupling

> **Dependency:** Phase 5 (Facility), Phase 8 (Product), Phase 10 (stock/ slice)

### Decouple dari master.currency di Use Cases

- [ ] Buat `src/main/java/com/solusi/erp/inventory/adjustment/application/port/CurrencyValidationPort.java`  
  Interface: `boolean existsById(Long currencyId)`, `boolean isActive(Long currencyId)`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/adjustment/infrastructure/adapter/CurrencyValidationAdapter.java`  
  Implements `CurrencyValidationPort`, inject `master.currency.domain.repository.CurrencyRepository`
- [ ] Update `src/main/java/com/solusi/erp/inventory/adjustment/application/usecase/command/CreateStockAdjustmentUseCaseImpl.java`  
  Ganti inject `master.currency.domain.repository.CurrencyRepository` → inject `CurrencyValidationPort`
- [ ] Update `src/main/java/com/solusi/erp/inventory/adjustment/application/usecase/command/UpdateStockAdjustmentUseCaseImpl.java`  
  Sama
- [ ] Daftarkan `CurrencyValidationAdapter` di `StockAdjustmentConfig.java`

### Decouple StockAdjustmentController dari master.currency web/domain

- [ ] Update `src/main/java/com/solusi/erp/inventory/adjustment/web/controller/StockAdjustmentController.java`  
  - Hapus `import master.currency.domain.model.Currency`
  - Hapus `import master.currency.web.mapper.CurrencyWebMapper`
  - Pertahankan `import master.currency.application.usecase.query.FindActiveCurrenciesUseCase`
    dan `GetDefaultCurrencyUseCase` (gunakan use case, bukan domain model)
  - Gunakan `CurrencySummaryResponse` dari master.currency.web.dto (boleh — web DTO bukan domain model)

### Persistence Migration

- [ ] Buat `src/main/java/com/solusi/erp/inventory/adjustment/infrastructure/persistence/StockAdjustmentEntity.java`  
  JPA entity (`@Entity @Table(name="inv_stock_adjustments")`), fields: `id`, `code`,
  `transactionDate`, `facilityId` (Long), `currencyId` (Long),
  `@Embedded CurrencyAmount totalAmount`, `status` (String/Enum),
  `@OneToMany(cascade=ALL, orphanRemoval=true) List<StockAdjustmentLineEntity> lines`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/adjustment/infrastructure/persistence/StockAdjustmentLineEntity.java`  
  JPA entity (`@Entity @Table(name="inv_stock_adjustment_lines")`), fields: `id`,
  `@ManyToOne StockAdjustmentEntity header`, `productId` (Long), `gridId` (Long),
  `containerId` (Long), `uomId` (Long), `quantity`, `unitCost`, `serialNumber`  
  **Catatan:** `@ManyToOne StockAdjustmentEntity` dipertahankan — parent-child satu aggregate
- [ ] Buat `src/main/java/com/solusi/erp/inventory/adjustment/infrastructure/persistence/StockAdjustmentJpaRepository.java`  
  Query methods dari `inventory/repository/StockAdjustmentRepository.java`
- [ ] Buat `src/main/java/com/solusi/erp/inventory/adjustment/infrastructure/persistence/StockAdjustmentLineJpaRepository.java`  
  Query methods dari `inventory/repository/StockAdjustmentLineRepository.java`
- [ ] Update `src/main/java/com/solusi/erp/inventory/adjustment/infrastructure/persistence/StockAdjustmentPersistenceMapper.java`  
  Mapping dari `StockAdjustmentEntity` lokal (bukan `inventory.model.StockAdjustment`);
  hapus inject `master.currency.domain.repository.CurrencyRepository`
- [ ] Update `src/main/java/com/solusi/erp/inventory/adjustment/infrastructure/adapter/StockAdjustmentRepositoryImpl.java`  
  Inject `StockAdjustmentJpaRepository` + `StockAdjustmentLineJpaRepository` lokal
- [ ] Update `src/main/java/com/solusi/erp/inventory/adjustment/application/usecase/command/ProcessStockAdjustmentUseCaseImpl.java`  
  Ganti inject `inventory.service.StockService` → inject `stock.application.usecase.command.AdjustStockUseCase`  
  Ganti import `inventory.model.StockAdjustmentLine` → gunakan domain model lokal  
  Ganti import `inventory.model.MovementType` → `stock.domain.model.MovementType`  
  Ganti import `inventory.model.ReferenceType` → `stock.domain.model.ReferenceType`
- [ ] Update `src/main/java/com/solusi/erp/inventory/adjustment/infrastructure/config/StockAdjustmentConfig.java`  
  Tambah inject `AdjustStockUseCase` (dari stock/ slice), `CurrencyValidationAdapter`

### Hapus File Lama

- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/StockAdjustment.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/model/StockAdjustmentLine.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/StockAdjustmentRepository.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/repository/StockAdjustmentLineRepository.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/StockAdjustmentRequest.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/StockAdjustmentResponse.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/StockAdjustmentLineRequest.java`
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/dto/StockAdjustmentLineResponse.java`

**Acceptance Criteria Phase 11:**
- [ ] `rg "import com.solusi.erp.inventory.model\.(StockAdjustment|StockAdjustmentLine)" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.inventory.repository\.(StockAdjustment|StockAdjustmentLine)Repository" src` → 0 hasil
- [ ] `rg "import com.solusi.erp.master.currency.domain" src/main/java/com/solusi/erp/inventory` → 0 hasil
- [ ] `rg "import com.solusi.erp.master.currency.web.mapper" src/main/java/com/solusi/erp/inventory` → 0 hasil
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 12 — Report Slice: Web Layer Completion

> **Dependency:** Phase 10 (stock/ slice dengan StockBalanceRepository, InventoryMovementRepository)

### Verifikasi Struktur Report Slice

- [ ] Verifikasi isi `src/main/java/com/solusi/erp/inventory/report/` — identifikasi file yang sudah ada
- [ ] Pastikan `GetOnHandSummaryUseCase`, `GetOnHandDetailUseCase`, `GetStockCardUseCase`
  sudah ada di `report/application/usecase/query/`
- [ ] Jika use cases belum ada: buat dengan inject `StockBalanceRepository` dan
  `InventoryMovementRepository` dari `stock/domain/repository/` sebagai port

### Pindahkan Controller + DTOs

- [ ] Pindahkan `src/main/java/com/solusi/erp/inventory/controller/InventoryReportController.java`  
  → `src/main/java/com/solusi/erp/inventory/report/web/controller/InventoryReportController.java`
- [ ] Pindahkan `src/main/java/com/solusi/erp/inventory/dto/StockCardFilter.java`  
  → `src/main/java/com/solusi/erp/inventory/report/web/dto/StockCardFilter.java`
- [ ] Pindahkan `src/main/java/com/solusi/erp/inventory/dto/LocationStockDetailResponse.java`  
  → `src/main/java/com/solusi/erp/inventory/report/web/dto/LocationStockDetailResponse.java`
- [ ] Pindahkan `src/main/java/com/solusi/erp/inventory/dto/ProductStockSummaryResponse.java`  
  → `src/main/java/com/solusi/erp/inventory/report/web/dto/ProductStockSummaryResponse.java`
- [ ] Pindahkan `src/main/java/com/solusi/erp/inventory/dto/InventoryMovementResponse.java`  
  → `src/main/java/com/solusi/erp/inventory/stock/web/dto/InventoryMovementResponse.java`  
  (atau `report/web/dto/` jika hanya dipakai di report)
- [ ] Pastikan `report/` punya `infrastructure/config/ReportConfig.java` — composition root
- [ ] Hapus `src/main/java/com/solusi/erp/inventory/controller/InventoryReportController.java`

**Acceptance Criteria Phase 12:**
- [ ] `rg "import com.solusi.erp.inventory.controller" src` → 0 hasil
- [ ] `.\mvnw.cmd clean test -q` → pass

---

## Phase 13 — Hapus Package Horizontal

> **Dependency:** Semua phase 1–12 selesai

Hapus semua package horizontal yang tersisa setelah semua consumer dimigrasikan:

- [ ] Verifikasi: `rg "import com.solusi.erp.inventory\.model\." src` → 0 hasil
- [ ] Verifikasi: `rg "import com.solusi.erp.inventory\.repository\." src` → 0 hasil
- [ ] Verifikasi: `rg "import com.solusi.erp.inventory\.service\." src` → 0 hasil
- [ ] Verifikasi: `rg "import com.solusi.erp.inventory\.dto\." src` → 0 hasil
- [ ] Verifikasi: `rg "import com.solusi.erp.inventory\.form\." src` → 0 hasil
- [ ] Verifikasi: `rg "import com.solusi.erp.inventory\.mapper\." src` → 0 hasil
- [ ] Verifikasi: `rg "import com.solusi.erp.inventory\.controller\." src` → 0 hasil
- [ ] Verifikasi: `rg "import com.solusi.erp.inventory\.validation\." src` → 0 hasil
- [ ] Verifikasi: `rg "import com.solusi.erp.inventory\.util\." src` → 0 hasil
- [ ] Hapus direktori `src/main/java/com/solusi/erp/inventory/model/`
- [ ] Hapus direktori `src/main/java/com/solusi/erp/inventory/repository/`
- [ ] Hapus direktori `src/main/java/com/solusi/erp/inventory/service/`
- [ ] Hapus direktori `src/main/java/com/solusi/erp/inventory/dto/`
- [ ] Hapus direktori `src/main/java/com/solusi/erp/inventory/form/`
- [ ] Hapus direktori `src/main/java/com/solusi/erp/inventory/mapper/`
- [ ] Hapus direktori `src/main/java/com/solusi/erp/inventory/controller/`
- [ ] Hapus direktori `src/main/java/com/solusi/erp/inventory/validation/`
- [ ] Hapus direktori `src/main/java/com/solusi/erp/inventory/util/`

**Acceptance Criteria Phase 13:**
- [ ] Semua direktori horizontal di atas tidak ada
- [ ] `.\mvnw.cmd clean test -q` → pass
- [ ] Tidak ada `@ManyToOne` lintas modul di seluruh `inventory/` (kecuali `StockAdjustmentLine → StockAdjustmentEntity`)

---

## Final Acceptance Criteria

- [ ] `.\mvnw.cmd clean test -q` → semua test pass, tidak ada test yang dinonaktifkan
- [ ] `rg "import com.solusi.erp.inventory\.(controller|dto|form|mapper|model|service|repository|util|validation)" src` → **0 hasil**
- [ ] `rg "@ManyToOne" src/main/java/com/solusi/erp/inventory --include="*.java"` → **hanya** `StockAdjustmentLineEntity.java` (parent-child, intentional)
- [ ] `rg "import com.solusi.erp.master\.(.*?)\.infrastructure\.persistence\.(.*?)JpaRepository" src/main/java/com/solusi/erp/inventory --include="*.java"` → **0 hasil** (infrastruktur master tidak bocor ke inventory)
- [ ] `rg "import com.solusi.erp.master\.currency\.domain" src/main/java/com/solusi/erp/inventory --include="*.java"` → **0 hasil**
- [ ] Smoke test Playwright:
  - [ ] Form Product: autocomplete brand dan productcategory berfungsi, validation weight/dimension berjalan
  - [ ] Form Stock Adjustment: add line item, autocomplete product/grid/container berfungsi, currency default terisi
  - [ ] Form Grid: autocomplete facility berfungsi
  - [ ] Form Container: autocomplete grid dan facility berfungsi
  - [ ] Form UOM Conversion: autocomplete product, from-uom, to-uom berfungsi
  - [ ] List view semua fitur inventory render tanpa error (brand, container, facility, grid, product, productcategory, uom, uomconversion, adjustment)
  - [ ] Sorting dan pagination berfungsi di semua list inventory
  - [ ] Report On-Hand Summary: filter dan render berfungsi
  - [ ] Report Stock Card: filter by product dan date range berfungsi

---

## Dependency Tree (Urutan Phase)

```
Phase 1 (Enum + Shared)
  └─► Phase 2 (UOM)
  └─► Phase 4 (ProductCategory)
  └─► Phase 7 (Container — butuh Dimensions di shared)

Phase 3 (Brand) ─────────────────────────────────────────────┐
Phase 2 (UOM) ───────────────────────────────────────────────┤
Phase 4 (ProductCategory) ───────────────────────────────────┤──► Phase 8 (Product)
                                                             │
Phase 5 (Facility)                                          │
  └─► Phase 6 (Grid)                                        │
        └─► Phase 7 (Container)                              │
                                                             │
Phase 8 (Product) ───────────────────────────────────────────┤
Phase 2 (UOM) ───────────────────────────────────────────────┤──► Phase 9 (UomConversion)
                                                             │
Phase 7, 8, 9 ───────────────────────────────────────────────┤──► Phase 10 (stock/ slice)
                                                             │
Phase 5, 8, 10 ──────────────────────────────────────────────┴──► Phase 11 (Adjustment)

Phase 10 ──────────────────────────────────────────────────────► Phase 12 (Report)

Phases 1–12 ───────────────────────────────────────────────────► Phase 13 (Delete horizontal)
```
