# Roadmap: Implement Smart Delete Pattern (Master Module)

> **Tujuan:** Implementasi pola _Smart Delete_ generik di seluruh `module/master`:
> jika entity sedang digunakan → soft-delete (isActive=false), jika tidak → hard delete.
> Pattern dibuat reusable dan terdokumentasi agar dapat ditiru di fitur lain.
>
> **Status:** ✅ Completed
> **Tests:** 646 passed (0 failures)
>
> **Scope:** 6 fitur master (Tax, Currency, PartyRoleType, Geographic, Party, BankAccount)
> + core infrastructure (DeleteResult enum, HtmxResponseUtility, i18n, frontend event)
> + dokumentasi pattern
>
> **Referensi Pattern:** `inventory.brand` (inUse checker), `inventory.uom` (composite checker)
> **Referensi Arsitektur:** `docs/architecture/clean-ddd-cqrs-standard.md`

---

## Struktur Smart Delete Pattern

```
<feature>/
├── domain/
│   └── port/
│       └── XxxInUseChecker.java       ← Pure Java interface
├── application/
│   └── usecase/command/
│       └── DeleteXxxUseCaseImpl.java   ← Smart Delete logic
├── infrastructure/
│   ├── adapter/
│   │   └── XxxInUseCheckerImpl.java   ← JPA-based checker
│   └── config/
│       └── XxxConfig.java             ← Wire checker into delete use case
```

**Flow:** `findById → inUseChecker.isInUse(id) → inUse ? softDelete : hardDelete → return DeleteResult`

---

## Phase 1 — Core Infrastructure

> Buat komponen generic yang dipakai semua fitur Smart Delete.

- [x] Buat `core/domain/model/DeleteResult.java` — Pure Java enum (`HARD_DELETED`, `SOFT_DELETED`)
- [x] Tambah method `okWithRefreshTableAndWarning(message)` di `HtmxResponseUtility`
  - Event baru: `erp:show-warning` (kuning/amber, beda dari `erp:show-success` hijau)
- [x] Tambah listener `erp:show-warning` di `erp-form-handler.js`
  - Tampilkan alert kuning/amber (bukan hijau) dengan pesan dari server
- [x] Tambah i18n messages (EN + ID):
  - `msg.success.deactivated` — "Data has been deactivated because it is still in use."
  - `msg.success.delete` — sudah ada

**Acceptance Criteria Phase 1:**
- [x] `DeleteResult` enum pure Java, tidak ada import framework
- [x] `erp:show-warning` event tampil alert amber di UI
- [x] `HtmxResponseUtility` punya method baru yang trigger warning event

---

## Phase 2 — Smart Delete: Tax, Currency, PartyRoleType

> Fitur yang SUDAH punya `isActive` + `softDelete()`. Hanya perlu tambah inUse checker.

### Tax
- [x] Buat `master/tax/domain/port/TaxInUseChecker.java` — interface `boolean isInUse(Long id)`
- [x] Buat `master/tax/infrastructure/adapter/TaxInUseCheckerImpl.java` — cek penggunaan di module lain
- [x] Ubah `DeleteTaxUseCaseImpl` — return `DeleteResult`, panggil checker, smart delete
- [x] Ubah `DeleteTaxUseCase` interface — return type `void` → `DeleteResult`
- [x] Update `TaxConfig` — wire `TaxInUseChecker` ke delete use case
- [x] Update `TaxController` — baca `DeleteResult`, response berbeda (success/warning)
- [x] Tambah i18n: `msg.error.tax.in-use` (EN + ID)

### Currency
- [x] Buat `master/currency/domain/port/CurrencyInUseChecker.java`
- [x] Buat `master/currency/infrastructure/adapter/CurrencyInUseCheckerImpl.java`
- [x] Ubah `DeleteCurrencyUseCaseImpl` — smart delete pattern
- [x] Ubah `DeleteCurrencyUseCase` interface — return `DeleteResult`
- [x] Update `CurrencyConfig` — wire checker
- [x] Update `CurrencyController` — handle `DeleteResult`
- [x] Tambah i18n: `msg.error.currency.in-use` (EN + ID)

### PartyRoleType
- [x] Buat `master/partyroletype/domain/port/PartyRoleTypeInUseChecker.java`
- [x] Buat `master/partyroletype/infrastructure/adapter/PartyRoleTypeInUseCheckerImpl.java`
- [x] Ubah `DeletePartyRoleTypeUseCaseImpl` — smart delete pattern
- [x] Ubah `DeletePartyRoleTypeUseCase` interface — return `DeleteResult`
- [x] Update `PartyRoleTypeConfig` — wire checker
- [x] Update `PartyRoleTypeController` — handle `DeleteResult`
- [x] Tambah i18n: `msg.error.party-role-type.in-use` (EN + ID)

**Acceptance Criteria Phase 2:**
- [x] Semua inUse port adalah pure Java interface
- [x] Delete use case return `DeleteResult` bukan `void`
- [x] Jika inUse: soft-delete (isActive=false) + return `SOFT_DELETED`
- [x] Jika not inUse: hard-delete + return `HARD_DELETED`
- [x] Controller kirim toast hijau (hard delete) atau kuning (soft delete)

---

## Phase 3 — Smart Delete: Geographic, Party, BankAccount

> Fitur yang SUDAH punya `isActive` tapi delete-nya inkonsisten (hard-delete / mixed).
> Perlu tambah inUse checker + pastikan softDelete() ada di domain model.

### Geographic
- [x] Tambah `softDelete()` method di `Geographic` domain model (jika belum ada)
- [x] Buat `master/geographic/domain/port/GeographicInUseChecker.java`
- [x] Buat `master/geographic/infrastructure/adapter/GeographicInUseCheckerImpl.java`
  - Cek: PartyAddress.city_id, Geographic.parent_id, BankAccount.city_id
- [x] Ubah `DeleteGeographicUseCaseImpl` — smart delete pattern
- [x] Ubah `DeleteGeographicUseCase` interface — return `DeleteResult`
- [x] Update `GeographicConfig` — wire checker
- [x] Update `GeographicController` — handle `DeleteResult`
- [x] Tambah i18n: `msg.error.geographic.in-use` (EN + ID)

### Party
- [x] Tambah `softDelete()` method di `Party` domain model
- [x] Buat `master/party/domain/port/PartyInUseChecker.java`
- [x] Buat `master/party/infrastructure/adapter/PartyInUseCheckerImpl.java`
  - Cek: BankAccount.party_id
- [x] Ubah `DeletePartyUseCaseImpl` — smart delete pattern
- [x] Ubah `DeletePartyUseCase` interface — return `DeleteResult`
- [x] Update `PartyConfig` — wire checker
- [x] Update `PartyController` — handle `DeleteResult`
- [x] Tambah i18n: `msg.error.party.in-use` (EN + ID)

### BankAccount
- [x] Tambah `softDelete()` method di `BankAccount` domain model (jika belum ada)
- [x] Buat `master/bankaccount/domain/port/BankAccountInUseChecker.java`
- [x] Buat `master/bankaccount/infrastructure/adapter/BankAccountInUseCheckerImpl.java`
  - Cek: (saat ini belum ada consumer — placeholder impl return false)
- [x] Ubah `DeleteBankAccountUseCaseImpl` — smart delete pattern
- [x] Ubah `DeleteBankAccountUseCase` interface — return `DeleteResult`
- [x] Update `BankAccountConfig` — wire checker
- [x] Update `BankAccountController` — handle `DeleteResult`
- [x] Tambah i18n: `msg.error.bank-account.in-use` (EN + ID)

**Acceptance Criteria Phase 3:**
- [x] Semua domain model punya `softDelete()` method
- [x] Geographic checker cek 3 FK (PartyAddress, Geographic parent, BankAccount)
- [x] Party checker cek BankAccount FK
- [x] Controller kirim toast berbeda berdasarkan `DeleteResult`

---

## Phase 4 — Documentation & Tests

> Buat pattern guide, update existing docs, jalankan tests.

- [x] Buat `docs/architecture/smart-delete-pattern.md` — guide lengkap Smart Delete
  - Berisi: kapan pakai, anatomi pattern, contoh implementasi, checklist
- [x] Update `docs/index.md` — tambah referensi ke smart-delete-pattern.md
- [x] Jalankan `mvn clean compile` — pastikan semua kompilasi clean
- [x] Jalankan `mvn clean test` — semua test harus pass
- [x] Buat `commit.txt` di root project

**Acceptance Criteria Phase 4:**
- [x] Dokumentasi cukup detail untuk agentic workflow di masa depan
- [x] `mvn clean test` pass tanpa failure (646 tests, 0 failures)
- [x] `commit.txt` format conventional commit, bahasa Inggris
