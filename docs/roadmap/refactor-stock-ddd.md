# Roadmap: Refactor Stock Slice ke Pure DDD

> **Tujuan:** Mengangkat slice `inventory.stock` dari pola _Infrastructure-Heavy Utility_
> ke standar **Pure Clean Architecture + DDD** yang sama dengan modul referensi
> (`brand`, `adjustment`). Business logic dipindah ke domain model pure Java.
>
> **Status:** ✅ Completed
> **Tests:** 640 passed (0 failures)
> **Verification:** Playwright smoke test — Stock On-Hand, Adjustments, Stock Card ✅
>
> **Scope:** Refactoring internal slice `stock/`. Tidak ada perubahan pada:
> - Contract `StockService.adjust(StockMovementPayload)` (cross-slice port)
> - JPA entity schema / database tables
> - Report module queries (CQRS read-side, bypass domain)
> - Consumer modules (`adjustment`, `report`)
>
> **Referensi Pattern:** `inventory.brand` (simple), `inventory.adjustment` (complex)
> **Referensi Arsitektur:** `docs/architecture/clean-ddd-cqrs-standard.md`

---

## Struktur Target

```
stock/
├── domain/                          ← 100% PURE JAVA
│   ├── model/
│   │   ├── StockBalance.java        ← NEW — Aggregate Root
│   │   ├── ValuationLayer.java      ← NEW — Entity
│   │   ├── CostAmount.java          ← NEW — Value Object
│   │   ├── MovementType.java        ← EXISTS
│   │   └── ReferenceType.java       ← EXISTS
│   ├── repository/
│   │   ├── StockBalanceRepository.java     ← NEW
│   │   └── ValuationLayerRepository.java   ← NEW
│   ├── port/
│   │   └── StockService.java        ← EXISTS (unchanged, cross-slice)
│   └── service/
│       └── FifoValuationService.java ← NEW — Domain Service (FIFO logic)
├── application/
│   └── dto/
│       └── StockMovementPayload.java ← EXISTS (unchanged)
├── infrastructure/
│   ├── persistence/
│   │   ├── StockBalanceEntity.java            ← EXISTS (unchanged)
│   │   ├── StockBalanceJpaRepository.java     ← EXISTS (unchanged)
│   │   ├── StockBalancePersistenceMapper.java ← NEW — MapStruct
│   │   ├── InventoryMovementEntity.java       ← EXISTS (unchanged)
│   │   ├── InventoryMovementJpaRepository.java ← EXISTS (unchanged)
│   │   ├── ValuationLayerEntity.java          ← EXISTS (unchanged)
│   │   ├── ValuationLayerJpaRepository.java   ← EXISTS (unchanged)
│   │   └── ValuationLayerPersistenceMapper.java ← NEW — MapStruct
│   ├── adapter/
│   │   ├── StockBalanceRepositoryImpl.java    ← NEW
│   │   ├── ValuationLayerRepositoryImpl.java  ← NEW
│   │   ├── StockBalanceContainerUsageChecker.java  ← EXISTS (unchanged)
│   │   └── InventoryMovementContainerUsageChecker.java ← EXISTS (unchanged)
│   ├── service/
│   │   └── StockServiceImpl.java    ← MODIFIED (delegates to domain)
│   └── config/
│       └── StockConfig.java         ← MODIFIED (wire new beans)
```

**File yang dihapus:**
- `infrastructure/service/ValuationServiceImpl.java` — logic diserap `FifoValuationService`
- `domain/port/ValuationService.java` — bukan cross-slice, internal domain service

---

## Phase 1 — Domain Models (Pure Java)

> Buat domain model murni Java tanpa anotasi framework. Ini fondasi seluruh refactor.

- [x] Buat `stock/domain/model/CostAmount.java` — Value Object (record)
  - Fields: `currencyId`, `exchangeRate`, `originalAmount`, `localAmount`
  - Factory method `of(currencyId, exchangeRate, originalAmount)` auto-hitung localAmount

- [x] Buat `stock/domain/model/StockBalance.java` — Aggregate Root
  - Fields: `metadata`, `productId`, `containerId`, `serialNumber`, `quantity`, `reservedQuantity`, `inTransitQuantity`
  - `static createNew(productId, containerId, serialNumber)`
  - `applyMovement(MovementType, BigDecimal)` — logic dari `StockServiceImpl.updateBalance()`
  - `validate()` — logic dari `StockServiceImpl.validateBalance()`
  - `getAvailableQuantity()` — derived field

- [x] Buat `stock/domain/model/ValuationLayer.java` — Entity
  - Fields: `metadata`, `productId`, `containerId`, `serialNumber`, `initialQuantity`, `remainingQuantity`, `unitCost` (CostAmount)
  - `static createNew(productId, containerId, serialNumber, quantity, unitCost)`
  - `consume(BigDecimal qty)` — returns `BigDecimal` local cost consumed

**Acceptance Criteria Phase 1:**
- [x] Ketiga model pure Java, tanpa import Spring/JPA/Lombok
- [x] Menggunakan `AuditMetadata` dari `core.domain.model`
- [x] Business logic `applyMovement` + `validate` ada di `StockBalance`

---

## Phase 2 — Domain Repository & Service

> Buat port persistence dan domain service FIFO. Semua pure Java.

- [x] Buat `stock/domain/repository/StockBalanceRepository.java` — interface
  - `StockBalance save(StockBalance)`
  - `Optional<StockBalance> findByProductContainerSerial(Long, Long, String)`

- [x] Buat `stock/domain/repository/ValuationLayerRepository.java` — interface
  - `void save(ValuationLayer)`
  - `List<ValuationLayer> findAvailableLayers(Long productId, Long containerId, BigDecimal minQty)`
  - `List<ValuationLayer> findAvailableLayersBySerial(Long productId, Long containerId, String sn, BigDecimal minQty)`

- [x] Buat `stock/domain/service/FifoValuationService.java` — Domain Service
  - Constructor: `FifoValuationService(ValuationLayerRepository)`
  - `addLayer(productId, containerId, serialNumber, qty, CostAmount)` — buat layer baru
  - `consumeLayers(productId, containerId, serialNumber, qty)` → `CostAmount` — FIFO logic dari `ValuationServiceImpl.consumeStock()`

**Acceptance Criteria Phase 2:**
- [x] Semua interface pure Java (tanpa Spring)
- [x] `FifoValuationService` pure Java, FIFO logic lengkap dengan error handling

---

## Phase 3 — Infrastructure Adapters

> Bridge domain ke framework. MapStruct mappers + repository impls.

- [x] Buat `stock/infrastructure/persistence/StockBalancePersistenceMapper.java` — MapStruct
  - `StockBalance toDomain(StockBalanceEntity)`
  - `StockBalanceEntity toEntity(StockBalance)`
  - `AuditMetadata toAuditMetadata(StockBalanceEntity)` default method

- [x] Buat `stock/infrastructure/persistence/ValuationLayerPersistenceMapper.java` — MapStruct
  - `ValuationLayer toDomain(ValuationLayerEntity)`
  - `ValuationLayerEntity toEntity(ValuationLayer)`
  - Mapping `CostAmount` ↔ `CurrencyAmount`

- [x] Buat `stock/infrastructure/adapter/StockBalanceRepositoryImpl.java`
  - Implements `StockBalanceRepository`
  - Delegates ke `StockBalanceJpaRepository` + mapper

- [x] Buat `stock/infrastructure/adapter/ValuationLayerRepositoryImpl.java`
  - Implements `ValuationLayerRepository`
  - Delegates ke `ValuationLayerJpaRepository` + mapper

**Acceptance Criteria Phase 3:**
- [x] MapStruct mapping kompilasi tanpa error
- [x] Repository impl follow pattern `BrandRepositoryImpl`

---

## Phase 4 — Refactor StockServiceImpl & Config

> Rewire implementasi untuk menggunakan domain model. Hapus file lama.

- [x] Refactor `StockServiceImpl.java`
  - Ganti dependency `StockBalanceJpaRepository` → `StockBalanceRepository` (domain)
  - Ganti dependency `ValuationService` → `FifoValuationService` (domain)
  - `adjust()`: load `StockBalance` domain → `applyMovement()` → `validate()` → save
  - Mapping `CostAmount` ↔ `CurrencyAmount` untuk `InventoryMovementEntity`

- [x] Hapus `ValuationServiceImpl.java` (infrastructure/service/)
- [x] Hapus `ValuationService.java` (domain/port/)

- [x] Update `StockConfig.java` — Composition Root
  - Wire `StockBalanceRepository` bean (adapter + mapper)
  - Wire `ValuationLayerRepository` bean (adapter + mapper)
  - Wire `FifoValuationService` bean (domain service)
  - Wire `StockService` bean (menggunakan `TransactionTemplate`)

**Acceptance Criteria Phase 4:**
- [x] `StockServiceImpl` tidak lagi manipulasi JPA entity langsung (kecuali `InventoryMovementEntity` untuk logging)
- [x] Contract `StockService.adjust(payload)` tidak berubah
- [x] Semua consumer (`ProcessStockAdjustmentUseCaseImpl`) tetap bekerja tanpa modifikasi

---

## Phase 5 — Tests & Verification

> Domain unit tests + update existing tests + full verification.

- [x] Buat `test/.../stock/domain/StockBalanceDomainTest.java`
  - Test `applyMovement` semua `MovementType`
  - Test `validate` positive + negative
  - Test `getAvailableQuantity`
  - Test `createNew`

- [x] Buat `test/.../stock/domain/FifoValuationServiceTest.java`
  - Test `addLayer` creates correct layer
  - Test `consumeLayers` FIFO order
  - Test `consumeLayers` insufficient stock error
  - Test serialized items consumption

- [x] Update `test/.../stock/infrastructure/service/StockServiceTest.java`
  - Ganti mock `StockBalanceJpaRepository` → mock `StockBalanceRepository`
  - Ganti mock `ValuationService` → mock `FifoValuationService`
  - Semua existing test case tetap pass

- [x] Jalankan `mvn clean test` — semua test harus pass
- [x] Smoke test Playwright: navigasi ke halaman inventory report (stock on-hand)
  untuk verifikasi UI masih berfungsi

**Acceptance Criteria Phase 5:**
- [x] Domain tests pass tanpa Spring context (pure JUnit 5 + Mockito/AssertJ)
- [x] Semua existing tests pass
- [x] `mvn clean test` clean (0 failures, 640 tests)
- [x] Playwright smoke test: halaman inventory report accessible dan data tampil
