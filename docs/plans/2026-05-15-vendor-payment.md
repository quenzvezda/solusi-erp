# Implementation Plan: Vendor Payment

> Source: docs/brainstorming/2026-05-15-vendor-payment.md
> Created: 2026-05-15
> Sprint: 5 (Accounts Payable)
> Status: IN_PROGRESS

## Summary

Vendor Payment adalah dokumen pembayaran ke supplier yang mengalokasikan dana dari Bank Account ke satu atau lebih Vendor Bill yang masih outstanding. Plan ini mencakup: refactor Bank Account (tambah currencyId, coaId), enhancement Journal Engine (accountOverrides), domain model VP, use cases, web layer + templates, dan update status VB setelah payment confirm.

## Tasks

### Task 1: Bank Account Refactor [x]
Tambah field `currencyId`, `coaId`, refactor `accountType` ke enum `PaymentType` pada domain model, entity, migration, dan seeder.

**Depends on:** (none)
**Reference module:** `master.bankaccount`

Steps:
- [ ] Flyway migration `V61__Bank_Account_Refactor_For_Payment.sql`: ALTER TABLE `bank_accounts` ADD `currency_id` BIGINT, ADD `coa_id` BIGINT, ADD FK constraints
      ref: src/main/resources/db/migration/V60__Journal_Multicurrency_And_VB_Exchange_Rate.sql — naming convention dan format terakhir
- [ ] Update seeder data: BA-DEMO-01 → currencyId=IDR, coaId=1120; BA-DEMO-02 → currencyId=IDR, coaId=1130; BA-DEMO-03 → currencyId=IDR, coaId=1110; BA-DEMO-04 → currencyId=IDR, coaId=NULL
      ref: src/main/resources/db/migration/V16__Master_Bank_Account.sql — original seeder structure
- [ ] Create enum `PaymentType` (CASH, BANK_TRANSFER, GIRO, CREDIT_CARD) di `master.shared.model`
      ref: src/main/java/com/solusi/erp/master/shared/model/AccountType.java:L1-L7 — existing enum pattern
- [ ] Update domain model `BankAccount.java`: tambah field `currencyId` (Long), `coaId` (Long), ubah `accountType` dari String ke `PaymentType` enum
      ref: src/main/java/com/solusi/erp/master/bankaccount/domain/model/BankAccount.java:L1-L83 — current domain model
- [ ] Update JPA entity `BankAccount.java` (persistence): tambah `currency_id`, `coa_id` columns, ubah `account_type` mapping
      ref: src/main/java/com/solusi/erp/master/bankaccount/infrastructure/persistence/BankAccount.java:L1-L49 — current entity
- [ ] Update `BankAccountPersistenceMapper` untuk map field baru
- [ ] Update `BankAccountSaveRequest`, `BankAccountDetailResponse`, `BankAccountSummaryResponse` DTOs
- [ ] Update `BankAccountWebMapper` untuk handle field baru
- [ ] Update `BankAccountController` dan form template jika perlu (currency autocomplete, COA autocomplete)
- [ ] Update existing use case tests yang terpengaruh perubahan constructor
      ref: src/test/java/com/solusi/erp/master/bankaccount/application/usecase/command/CreateBankAccountUseCaseTest.java — existing test

**Validation criteria:**
- `mvn compile -q` passes
- Existing bank account tests pass: `mvn test -Dtest="*BankAccount*"`
- Application starts tanpa error (seeder data valid)

---

### Task 2: Journal Engine Enhancement (Account Overrides) [x]
Tambah field `accountOverrides` di `JournalPostingCommand` dan logic override di `PostJournalForEventUseCaseImpl`.

**Depends on:** (none)
**Reference module:** `accounting.journal`

Steps:
- [ ] Tambah field `Map<JournalVariable, Long> accountOverrides` di record `JournalPostingCommand`
      ref: src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/JournalPostingCommand.java:L1-L31 — current record
- [ ] Tambah convenience constructor tanpa accountOverrides (backward compatible, default null)
- [ ] Update `PostJournalForEventUseCaseImpl`: resolve accountOverrides dulu, fallback ke schema accountId
      ref: src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/PostJournalForEventUseCaseImpl.java — posting logic
- [ ] **TEST:** Update `PostJournalForEventUseCaseTest` — tambah test case dengan accountOverrides (override satu variable, verify account yang dipakai)
      ref: src/test/java/com/solusi/erp/accounting/journal/application/usecase/command/PostJournalForEventUseCaseTest.java — existing test
- [ ] **TEST:** Verify existing tests tetap pass (backward compatible, null override = pakai schema)

**Validation criteria:**
- `mvn test -Dtest="PostJournalForEventUseCaseTest"` passes
- Existing journal tests tidak break
- Override logic: jika accountOverrides != null dan contains variable → pakai override; else → pakai schema

---

### Task 3: JournalVariable & Accounting Schema Seeder [x]
Tambah `VP_FX_LOSS_AMT`, `VP_FX_GAIN_AMT` ke enum dan seed schema lines untuk event VENDOR_PAYMENT.

**Depends on:** Task 2
**Reference module:** `accounting.journal`, `accounting.schema`

Steps:
- [ ] Tambah enum values `VP_FX_LOSS_AMT(SchemaEventType.VENDOR_PAYMENT)` dan `VP_FX_GAIN_AMT(SchemaEventType.VENDOR_PAYMENT)` di `JournalVariable.java`
      ref: src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalVariable.java:L16-L17 — existing VP variables
- [ ] Flyway migration `V62__Vendor_Payment_Accounting_Schema.sql`: INSERT schema lines untuk VENDOR_PAYMENT event (VP_AP_AMT→2110 DEBIT, VP_BANK_OUT_AMT→1120 CREDIT, VP_FX_LOSS_AMT→5140 DEBIT, VP_FX_GAIN_AMT→4240 CREDIT)
      ref: src/main/resources/db/migration/V56__Refactor_Schema_To_Dynamic_Lines.sql — schema seeder format

**Validation criteria:**
- `mvn compile -q` passes
- Application starts, schema UI shows VENDOR_PAYMENT event with 4 variables
- (no test — seeder SQL + enum addition only)

---

### Task 4: Flyway Migration — Vendor Payment Tables [x]
DDL untuk tabel `vendor_payments` dan `vendor_payment_lines`.

**Depends on:** (none)
**Reference module:** `accountspayable.vendorbill`

Steps:
- [ ] Flyway migration `V63__Add_Vendor_Payment_Module.sql`:
  - CREATE TABLE `vendor_payments` (id, code, vendor_id FK, currency_id FK, bank_account_id FK, payment_date, exchange_rate, payment_amount, status ENUM, reference, notes, audit fields)
  - CREATE TABLE `vendor_payment_lines` (id, vendor_payment_id FK, vendor_bill_id FK, bill_code, outstanding_amount, paid_amount)
  - INSERT permissions: VENDOR-PAYMENT_READ, _CREATE, _UPDATE, _DELETE
  - INSERT permission_group entry
  - INSERT menu entry under "Finance & Accounting > Account Payable > Vendor Payment"
  - INSERT role_permissions for admin role
      ref: src/main/resources/db/migration/V58__Add_Vendor_Bill_Module.sql — similar module migration pattern

**Validation criteria:**
- Application starts tanpa Flyway error
- Permission muncul di role management UI
- Menu muncul di sidebar
- (no test — pure DDL/seed)

---

### Task 5: Domain Model [x]
`VendorPayment` aggregate root, `VendorPaymentLine` value object, `VendorPaymentStatus` enum, repository port, dan cross-slice ports.

**Depends on:** Task 4
**Reference module:** `accountspayable.vendorbill`

Steps:
- [ ] Create enum `VendorPaymentStatus` (DRAFT, CONFIRMED, CANCELLED)
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillStatus.java — enum pattern
- [ ] Create `VendorPaymentLine` record/class: vendorBillId, billCode, outstandingAmount, paidAmount
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillLine.java — child entity pattern
- [ ] Create `VendorPayment` aggregate root: all fields from brainstorm section 2A, status lifecycle (confirm/cancel), validation rules (section 4)
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBill.java:L1-L50 — aggregate root with status + lines
- [ ] Create `VendorPaymentRepository` interface (save, findById, findAll, deleteById)
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/repository/VendorBillRepository.java — repository port pattern
- [ ] Create `PayableVendorBillQueryPort` interface: query VB CONFIRMED/PARTIAL_PAID with outstanding > 0 per vendor+currency
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/port/BillableGrQueryPort.java — cross-slice query port
- [ ] Create `VendorBillPaymentUpdatePort` interface: update VB status (PARTIAL_PAID/PAID) after payment confirm
- [ ] **TEST:** Write `VendorPaymentTest.java` — test confirm (sets status, validates rules), cancel (only from DRAFT), validation (amount > 0, lines required, sum mismatch)
      ref: src/test/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillTest.java — domain test pattern

**Validation criteria:**
- `mvn test -Dtest="VendorPaymentTest"` passes
- Domain model is pure Java (no Spring/Lombok/JPA annotations)
- All validation rules from brainstorm section 4 have corresponding test cases

---

### Task 6: Infrastructure Layer [x]
JPA entities, persistence mapper, repository impl, cross-slice adapters, config.

**Depends on:** Task 5
**Reference module:** `accountspayable.vendorbill`

Steps:
- [ ] Create `VendorPaymentEntity` JPA entity (maps to `vendor_payments` table)
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillEntity.java — entity with lines pattern
- [ ] Create `VendorPaymentLineEntity` JPA entity (maps to `vendor_payment_lines`)
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillLineEntity.java — child entity
- [ ] Create `VendorPaymentJpaRepository` (Spring Data JPA interface)
- [ ] Create `VendorPaymentPersistenceMapper` (domain ↔ entity mapping)
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillPersistenceMapper.java — mapper pattern
- [ ] Create `VendorPaymentRepositoryImpl` implements `VendorPaymentRepository`
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillRepositoryImpl.java — repo impl pattern
- [ ] Create `PayableVendorBillQueryAdapter` implements `PayableVendorBillQueryPort` (query VB with outstanding > 0)
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/BillableGrQueryAdapter.java — cross-slice adapter
- [ ] Create `VendorBillPaymentUpdateAdapter` implements `VendorBillPaymentUpdatePort` (update VB status)
- [ ] Create `VendorPaymentConfig` — register all beans, wrap use cases with TransactionTemplate
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/config/VendorBillConfig.java — config pattern
- [ ] **TEST:** Write `VendorPaymentConfigTest.java` — verify all beans wire correctly
      ref: src/test/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/config/VendorBillConfigTest.java — config integration test

**Validation criteria:**
- `mvn test -Dtest="VendorPaymentConfigTest"` passes
- All beans registered and non-null

---

### Task 7: Application Use Cases [x]
Command use cases (create, update, confirm, cancel, delete) dan query use cases (list, detail, payable bills).

**Depends on:** Task 6
**Reference module:** `accountspayable.vendorbill`

Steps:
- [ ] Create `CreateVendorPaymentUseCase` interface + impl (generate code via SequenceGeneratorService, validate, save as DRAFT)
- [ ] Create `UpdateVendorPaymentUseCase` interface + impl (only DRAFT editable, validate, save)
- [ ] Create `ConfirmVendorPaymentUseCase` interface + impl:
  - Validate sum of paidAmount = paymentAmount (unapplied must be 0)
  - Validate bank account has coaId and currency matches
  - Calculate FX per-line (brainstorm section 13)
  - Post journal via `PostJournalForEventUseCase` with accountOverrides for VP_BANK_OUT_AMT
  - Update VB status via `VendorBillPaymentUpdatePort`
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/command/ConfirmVendorBillUseCaseImpl.java — confirm + journal posting pattern
- [ ] Create `CancelVendorPaymentUseCase` interface + impl (only DRAFT → CANCELLED)
- [ ] Create `DeleteVendorPaymentUseCase` interface + impl (only DRAFT, hard delete)
- [ ] Create `GetVendorPaymentListUseCase` interface + impl (paginated, filtered by keyword/vendor/status)
- [ ] Create `GetVendorPaymentDetailUseCase` interface + impl
- [ ] Create `GetPayableVendorBillsUseCase` interface + impl (load outstanding VBs for vendor+currency)
- [ ] Register semua use case beans di `VendorPaymentConfig`
- [ ] **TEST:** Write `CreateVendorPaymentUseCaseTest` (happy path, no lines, amount <= 0)
      ref: src/test/java/com/solusi/erp/purchasing/purchaseorder/application/usecase/command/CreatePurchaseOrderUseCaseTest.java — Mockito use case test
- [ ] **TEST:** Write `ConfirmVendorPaymentUseCaseTest` (journal posting args, FX calculation, VB status update, validation failures)
- [ ] **TEST:** Write `CancelVendorPaymentUseCaseTest` (only from DRAFT, fail from CONFIRMED)
- [ ] **TEST:** Write `GetPayableVendorBillsUseCaseTest` (returns only CONFIRMED/PARTIAL_PAID with outstanding > 0)

**Validation criteria:**
- `mvn test -Dtest="*VendorPayment*UseCaseTest"` passes
- FX calculation matches brainstorm section 13 formula
- Journal posting uses accountOverrides for dynamic bank COA

---

### Task 8: Web Layer [x]
Controller, DTOs, web mapper.

**Depends on:** Task 7
**Reference module:** `accountspayable.vendorbill`

Steps:
- [ ] Create `VendorPaymentSaveRequest` DTO (extends BaseAuditResponse pattern for edit)
- [ ] Create `VendorPaymentLineRequest` DTO
- [ ] Create `VendorPaymentSummaryResponse` DTO (for list view)
- [ ] Create `VendorPaymentDetailResponse` DTO (for detail/view page)
- [ ] Create `PayableVendorBillResponse` DTO (for outstanding VB list in form)
- [ ] Create `VendorPaymentWebMapper` (MapStruct or manual)
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/web/mapper/VendorBillWebMapper.java — web mapper pattern
- [ ] Create `VendorPaymentController`:
  - GET list (paginated, filtered)
  - GET create form
  - GET edit form (DRAFT only)
  - GET detail (readonly for CONFIRMED)
  - POST create (AJAX/JSON)
  - PUT update (AJAX/JSON)
  - POST confirm (action button)
  - POST cancel (action button)
  - DELETE (from list, DRAFT only)
  - GET payable-bills endpoint (load outstanding VBs for vendor+currency, AJAX)
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/web/controller/VendorBillController.java — controller pattern
- [ ] **TEST:** Write `VendorPaymentControllerTest` (view names, model attrs, @PreAuthorize annotations, AJAX responses)
      ref: src/test/java/com/solusi/erp/accountspayable/vendorbill/web/controller/VendorBillControllerTest.java — controller test pattern

**Validation criteria:**
- `mvn test -Dtest="VendorPaymentControllerTest"` passes
- All endpoints have correct `@PreAuthorize` annotations
- AJAX endpoints return `ApiResponse<T>` wrapper

---

### Task 9: Thymeleaf Templates & JavaScript
List view, create/edit form, detail view, bank account modal selector.

**Depends on:** Task 8
**Reference module:** `accountspayable.vendorbill`

Steps:
- [ ] Create `templates/accountspayable/vendor-payments/list.html` (table with code, vendor, date, bank, amount, status badge, delete action)
      ref: src/main/resources/templates/accountspayable/vendor-bills/list.html — list template pattern
- [ ] Create `templates/accountspayable/vendor-payments/form.html` (header fields + allocation lines table + recap section)
      ref: src/main/resources/templates/accountspayable/vendor-bills/form.html — form with lines pattern
- [ ] Create `templates/accountspayable/vendor-payments/detail.html` (readonly, VB links, journal link)
      ref: src/main/resources/templates/accountspayable/vendor-bills/detail.html — detail view pattern
- [ ] Create bank account modal selector fragment (filtered by isActive + currencyId match + coaId NOT NULL)
      ref: docs/spec/modal-selector.md — modal selector spec
- [ ] Create `static/js/accountspayable/vendor-payments/form.js`:
  - Vendor autocomplete → on change, reload payable bills
  - Currency autocomplete → on change, reload payable bills + clear lines
  - Bank account modal selector → auto-fill payment type readonly
  - Payment amount input (AutoNumeric)
  - Allocation lines: paidAmount inputs (AutoNumeric), real-time recap calculation
  - Recap section: Payment Amount / Applied / Unapplied (live JS update)
      ref: src/main/resources/static/js/accountspayable/vendor-bills/form.js — form JS pattern
      ref: docs/spec/numeric-standards.md — AutoNumeric pattern
- [ ] Add i18n keys untuk vendor payment (messages_en.properties, messages_id.properties)
      ref: docs/spec/i18n-guide.md — i18n naming convention
- [ ] **TEST:** Write `VendorPaymentTemplateTest` (sec:authorize visibility, fragment IDs, DTO property binding, form action URLs)
      ref: src/test/java/com/solusi/erp/accountspayable/vendorbill/web/template/VendorBillTemplateTest.java — template test pattern

**Validation criteria:**
- `mvn test -Dtest="VendorPaymentTemplateTest"` passes
- Form recap updates real-time saat input paidAmount berubah
- Bank account modal hanya menampilkan yang punya coaId dan currency match

---

### Task 10: VB Status Update & Final Integration
Implement `VendorBillPaymentUpdatePort` logic dan pastikan seluruh flow end-to-end bekerja.

**Depends on:** Task 7, Task 9
**Reference module:** `accountspayable.vendorbill`

Steps:
- [ ] Implement logic di `VendorBillPaymentUpdateAdapter`:
  - Query semua CONFIRMED VendorPaymentLine yang merujuk VB
  - Sum paidAmount → total_paid
  - Update VB status: total_paid = 0 → CONFIRMED, 0 < total_paid < totalAmount → PARTIAL_PAID, total_paid = totalAmount → PAID
      ref: docs/brainstorming/2026-05-15-vendor-payment.md — section 5 (VB Status Update)
- [ ] Tambah method `updatePaymentStatus(VendorBillStatus)` di VendorBill domain model jika belum ada
- [ ] Verify VendorBillStatus enum sudah punya PARTIAL_PAID dan PAID values
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillStatus.java — check existing enum
- [ ] **TEST:** Write integration scenario di `ConfirmVendorPaymentUseCaseTest` — verify VB status updated correctly after payment confirm
- [ ] **TEST:** Verify partial payment scenario (pay 50% → PARTIAL_PAID, pay remaining 50% → PAID)
- [ ] End-to-end smoke test: create VP → allocate to VB → confirm → check journal posted + VB status updated

**Validation criteria:**
- `mvn test -Dtest="*VendorPayment*"` all pass
- VB status correctly transitions: CONFIRMED → PARTIAL_PAID → PAID
- Journal entry created with correct DR/CR amounts and dynamic bank COA
