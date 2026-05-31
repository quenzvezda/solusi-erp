# Implementation Plan: Manual Journal Entry

> Source: `docs/brainstorming/2026-05-31-manual-journal-entry.md`
> Created: 2026-05-31
> Sprint: 6 - Accounting Core
> Status: IN_PROGRESS
>
> **For agentic workers:** execute task-by-task. Explore references fresh before editing each task, update checkbox state incrementally, record deviations in `docs/reports/2026-05-31-manual-journal-entry.md`, and do not mark a task complete before its validation command passes.

## 1. Goal

Menambah jalur input jurnal manual ke modul `accounting.journal` yang saat ini read-only untuk hasil auto-posting. Hasil akhirnya mendukung simpan `DRAFT`, edit/delete selama draft, post ke periode terbuka, dan reversal satu kali untuk jurnal manual yang sudah `POSTED`.

Fitur memakai Clean Architecture + DDD + CQRS yang sudah berlaku di repo. Domain journal tetap menjadi pemilik lifecycle dan invariant double-entry; adapter lintas-slice hanya memvalidasi COA/currency; web layer hanya menginjeksi use case dan lookup provider.

## 2. Locked Decisions

- Lifecycle manual: `DRAFT -> POSTED`.
- Tidak memakai approval.
- Satu currency dan satu exchange rate per journal header.
- Input line adalah nominal transaction currency; base amount dihitung `transaction amount x exchangeRate`.
- Balance manual dicek pada transaction currency.
- Reversal hanya untuk jurnal manual original yang sudah `POSTED`, tanggal dipilih user, period wajib `OPEN`, currency/rate diwarisi, langsung `POSTED`, dan maksimal sekali.
- Auto-posted journal GR/VB tidak dapat direverse dari layar journal.
- Kode jurnal tetap berbasis ID: `JNL-%06d`.
- Memo per-line nullable.
- Period guard auto-posting GR/VB tidak diretrofit pada task ini.
- General Ledger View, Trial Balance, approval, per-line currency, dan reversal chain tidak termasuk scope.

## 3. Exploration Corrections

### 3.1 `eventType` harus menjadi `String`

`JournalEntry.eventType` saat ini bertipe `SchemaEventType`, sedangkan manual journal perlu menyimpan `event_type='MANUAL'`. `JournalPersistenceMapper.toDomain()` juga memanggil `SchemaEventType.valueOf(...)`, sehingga row manual tidak dapat dibaca sebelum refactor.

ref: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalEntry.java:L14-L26`
ref: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalPersistenceMapper.java:L66-L75`

### 3.2 Reference No perlu kolom terpisah

Brainstorm mengunci `source_code=NULL` untuk jurnal manual, tetapi UI juga meminta `Reference No` opsional. Jangan memakai `source_code` untuk reference manual karena field itu adalah identitas dokumen sumber auto-posting. Tambahkan `reference_no VARCHAR(100) NULL` pada header.

### 3.3 Mapper update tidak boleh membuat insert baru

`JournalPersistenceMapper.toEntity()` saat ini selalu membuat entity baru tanpa menyalin ID/version. Jalur update draft harus memakai pola `applyToEntity(domain, entity)` pada entity existing agar `JpaRepository.save()` melakukan update, line lama diganti terkontrol, dan optimistic locking tetap hidup.

ref: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalPersistenceMapper.java:L14-L41`
ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillPersistenceMapper.java:L36-L44`

### 3.4 UI lookup bukan validasi backend

Endpoint lookup COA sudah memfilter akun aktif dan non-header, tetapi request dapat dipalsukan. Tambahkan port backend khusus validasi posting account dan currency. Jangan inject `CoaJpaRepository` atau `CurrencyJpaRepository` langsung ke controller/use case journal.

ref: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/persistence/CoaJpaRepository.java:L46-L50`
ref: `docs/AGENTS.md` - batas dependency web layer dan cross-slice access

### 3.5 Double reversal perlu constraint database

Pengecekan `existsByReversalOfId()` diperlukan untuk pesan domain yang jelas, tetapi belum cukup terhadap request paralel. Tambahkan unique constraint nullable pada `reversal_of_id`; MariaDB dan H2 mengizinkan banyak nilai `NULL`.

## 4. Target File Map

### Database

- Create `src/main/resources/db/migration/V64__Add_Manual_Journal.sql`
- Create `src/main/resources/db/migration-h2/V64__Add_Manual_Journal.sql`

### Domain And Cross-Slice Ports

- Modify `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalEntry.java`
- Modify `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalLine.java`
- Modify `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalEntryFilter.java`
- Modify `src/main/java/com/solusi/erp/accounting/journal/domain/repository/JournalEntryRepository.java`
- Create `src/main/java/com/solusi/erp/accounting/coa/domain/port/CoaPostingValidator.java`
- Create `src/main/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaPostingValidatorImpl.java`
- Create `src/main/java/com/solusi/erp/master/currency/domain/port/CurrencyPostingValidator.java`
- Create `src/main/java/com/solusi/erp/master/currency/infrastructure/adapter/CurrencyPostingValidatorImpl.java`
- Modify `src/main/java/com/solusi/erp/accounting/coa/infrastructure/config/CoaConfig.java`
- Modify `src/main/java/com/solusi/erp/master/currency/infrastructure/config/CurrencyConfig.java`

### Journal Infrastructure And Application

- Modify journal persistence entity, mapper, JPA repository, adapter, query port, query adapter, and `JournalConfig`
- Create manual create/update/delete/post/reverse command use cases
- Create detail read model so the detail page can render `reversalOf` and `reversedBy`

### Web And UI

- Modify `JournalEntryController`, journal DTOs, and `JournalEntryWebMapper`
- Create `src/main/resources/templates/accounting/journal/journal-entry-form.html`
- Modify journal list/detail templates
- Create `src/main/resources/static/js/accounting/journal/journal-entry-form.js`
- Create `src/main/resources/static/js/accounting/journal/journal-entry-detail.js`
- Update `messages_id.properties`, `messages_en.properties`, and journal business docs

### Tests

- Extend journal domain/infrastructure/query/web/template tests
- Add use case and config tests
- Create `e2e-tests/tests/accounting/manual-journal-entry.spec.ts`
- Add journal routes to E2E warmup URLs

## 5. Task Order

### Task 1: Add MariaDB And H2 Migration V64 [x]

Tambah schema manual journal, permission baru, grant admin, dan mirror H2 yang benar-benar kompatibel.

**Depends on:** none

**Files:**
- Create: `src/main/resources/db/migration/V64__Add_Manual_Journal.sql`
- Create: `src/main/resources/db/migration-h2/V64__Add_Manual_Journal.sql`
- Test: `src/test/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalManualMigrationTest.java`

- [x] Buat migration MariaDB `V64__Add_Manual_Journal.sql`.

  Header:

  ```sql
  ALTER TABLE acc_journal_entries
    ADD COLUMN currency_id BIGINT NULL AFTER source_code,
    ADD COLUMN exchange_rate DECIMAL(19,6) NULL AFTER currency_id,
    ADD COLUMN reference_no VARCHAR(100) NULL AFTER exchange_rate,
    ADD COLUMN reversal_of_id BIGINT NULL AFTER reference_no,
    MODIFY COLUMN source_id BIGINT NULL,
    ADD CONSTRAINT fk_acc_journal_entries_currency
      FOREIGN KEY (currency_id) REFERENCES master_currencies(id),
    ADD CONSTRAINT fk_acc_journal_entries_reversal_of
      FOREIGN KEY (reversal_of_id) REFERENCES acc_journal_entries(id),
    ADD CONSTRAINT uk_acc_journal_entries_reversal_of UNIQUE (reversal_of_id);

  ALTER TABLE acc_journal_lines
    ADD COLUMN description VARCHAR(255) NULL AFTER original_credit_amount;
  ```

  ref: `src/main/resources/db/migration/V55__Add_Journal_Core.sql:L1-L30` - existing journal tables and source unique key
  ref: `src/main/resources/db/migration/V60__Journal_Multicurrency_And_VB_Exchange_Rate.sql:L1-L10` - MariaDB alter/FK precedent

- [x] Seed permissions ke group existing `ACC-05`: `JOURNAL-ENTRY_CREATE`, `JOURNAL-ENTRY_UPDATE`, `JOURNAL-ENTRY_DELETE`, `JOURNAL-ENTRY_POST`, `JOURNAL-ENTRY_REVERSE`. Grant semuanya ke `ROLE_ADMIN` dengan daftar eksplisit `IN (...)`, bukan wildcard `LIKE`.

  ref: `src/main/resources/db/migration/V55__Add_Journal_Core.sql:L32-L58` - existing journal permission group
  ref: `src/main/resources/db/migration/V58__Add_Vendor_Bill_Module.sql:L102-L121` - permission insert and admin grant pattern

- [x] Buat mirror H2 `db/migration-h2/V64__Add_Manual_Journal.sql`. Pecah setiap `ADD COLUMN`, `ALTER COLUMN source_id BIGINT NULL`, dan `ADD CONSTRAINT` menjadi statement terpisah. Jangan pakai `AFTER` atau `MODIFY COLUMN`.

  ref: `src/main/resources/db/migration-h2/V60__Journal_Multicurrency_And_VB_Exchange_Rate.sql:L1-L9` - H2 compatibility style
  ref: `docs/tests/playwright-e2e-guide.md:L57-L73` - mandatory 1:1 mirror and syntax differences

- [x] Tambahkan `JournalManualMigrationTest` berbasis `@SpringBootTest` + `@ActiveProfiles("e2e")` + `JdbcTemplate`. Assert:
  - `currency_id`, `exchange_rate`, `reference_no`, `reversal_of_id` ada pada `acc_journal_entries`.
  - `description` ada pada `acc_journal_lines`.
  - `source_id` menerima `NULL`.
  - Lima permission baru ada dan terhubung ke `ACC-05`.
  - Lima permission ter-grant ke `ROLE_ADMIN`.
  - Insert dua row dengan `reversal_of_id=NULL` valid, tetapi dua reversal terhadap original yang sama ditolak constraint.

**Simple verification:**

```powershell
.\mvnw.cmd -q -Pe2e -Dtest=JournalManualMigrationTest test
```

Expected: H2 Flyway menjalankan V64 dan test lulus.

**MariaDB follow-up gate:** saat profile dev tersedia, boot aplikasi atau jalankan Flyway terhadap MariaDB dan pastikan V64 applied tanpa syntax error.

### Task 2: Refactor Journal Domain For Manual Lifecycle [x]

Ubah aggregate agar mendukung manual draft, update, post, reversal, memo line, header currency, dan kompatibilitas auto-posting lama.

**Depends on:** Task 1

**Files:**
- Modify: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalEntry.java`
- Modify: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalLine.java`
- Test: `src/test/java/com/solusi/erp/accounting/journal/domain/model/JournalEntryTest.java`

- [x] Ubah `JournalEntry.eventType` dari `SchemaEventType` ke `String`. Pertahankan overload factory auto-posting yang menerima `SchemaEventType` lalu menyimpan `.name()` agar caller GR/VB tidak dipaksa berubah sekaligus.

  ref: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalEntry.java:L14-L52`

- [x] Tambahkan header fields:

  ```java
  private final Long currencyId;
  private final BigDecimal exchangeRate;
  private final String referenceNo;
  private final Long reversalOfId;
  private JournalStatus status;
  private List<JournalLine> lines;
  ```

  `currencyId/exchangeRate` nullable hanya untuk compatibility row auto-posting lama. Factory manual wajib mengisinya.

- [x] Tambahkan `JournalLine.description`. Pertahankan helper auto-posting lama dengan memo `null`. Tambahkan helper manual yang menerima transaction amount, currency, rate, dan memo lalu mengisi:
  - base debit/credit = transaction amount x rate
  - original currency ID = header currency ID
  - original debit/credit = transaction amount
  - exchange rate = header rate

  ref: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalLine.java:L7-L47`

- [x] Tambahkan factory/manual methods dengan kontrak eksplisit:

  ```java
  JournalEntry.createDraft(LocalDate postingDate, Long currencyId, BigDecimal exchangeRate,
                           String referenceNo, String description, List<JournalLine> lines)
  JournalEntry.updateDraft(LocalDate postingDate, Long currencyId, BigDecimal exchangeRate,
                           String referenceNo, String description, List<JournalLine> lines)
  void post()
  JournalEntry createReversal(LocalDate reversalDate, String reversalDescription)
  boolean isManual()
  boolean isReversal()
  ```

  Manual identity selalu `eventType="MANUAL"`, `sourceType="MANUAL"`, `sourceId=null`, `sourceCode=null`.

- [x] Pisahkan invariant manual dari compatibility auto:
  - factory/update manual menolak kurang dari dua line;
  - line menolak `accountId=null`;
  - line menolak debit dan credit sama-sama positif;
  - line menolak debit dan credit sama-sama nol;
  - line menolak nilai negatif;
  - manual currency wajib non-null;
  - manual exchange rate wajib `> 0`;
  - `validateBalancedInTransactionCurrency()` menjumlah `originalDebitAmount/originalCreditAmount`;
  - update/post hanya status `DRAFT`;
  - reversal hanya original `POSTED`, source manual, dan `reversalOfId == null`.

- [x] Pada `createReversal()`, swap debit/credit base dan original amounts, pertahankan account/memo/currency/rate, set `reversalOfId = original.id`, dan set status reversal langsung `POSTED`.

- [x] Extend `JournalEntryTest` dengan edge cases:
  - auto-posting existing factory masih menghasilkan `POSTED`;
  - create draft happy path;
  - draft menolak satu line dan zero line;
  - manual rate null/zero/negative ditolak;
  - manual balance dicek dengan original amount;
  - update draft mengganti header dan lines;
  - update posted ditolak;
  - post draft sukses; post ulang ditolak;
  - reversal swaps debit/credit serta original amounts;
  - reversal draft ditolak;
  - reversal auto-posted ditolak;
  - reversal-of-reversal ditolak;
  - per-line memo nullable diterima;
  - account null ditolak.

**Simple verification:**

```powershell
.\mvnw.cmd -q -Dtest=JournalEntryTest,JournalVariableTest test
```

Expected: domain tests lulus tanpa Spring context.

### Task 3: Add Backend Posting Reference Validators [x]

Validasi COA/currency harus terjadi di backend melalui provider slice, bukan hanya lewat TomSelect.

**Depends on:** Task 2

**Files:**
- Create: `src/main/java/com/solusi/erp/accounting/coa/domain/port/CoaPostingValidator.java`
- Create: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaPostingValidatorImpl.java`
- Modify: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/config/CoaConfig.java`
- Create: `src/main/java/com/solusi/erp/master/currency/domain/port/CurrencyPostingValidator.java`
- Create: `src/main/java/com/solusi/erp/master/currency/infrastructure/adapter/CurrencyPostingValidatorImpl.java`
- Modify: `src/main/java/com/solusi/erp/master/currency/infrastructure/config/CurrencyConfig.java`
- Test: `src/test/java/com/solusi/erp/accounting/coa/infrastructure/adapter/CoaPostingValidatorImplTest.java`
- Test: `src/test/java/com/solusi/erp/master/currency/infrastructure/adapter/CurrencyPostingValidatorImplTest.java`

- [x] Buat port COA:

  ```java
  public interface CoaPostingValidator {
      boolean isPostable(Long coaId);
  }
  ```

  Adapter hanya mengembalikan true untuk row existing dengan `isActive=true && isHeader=false`.

  ref: `src/main/java/com/solusi/erp/accounting/coa/infrastructure/persistence/CoaJpaRepository.java:L46-L50`

- [x] Buat port currency:

  ```java
  public interface CurrencyPostingValidator {
      CurrencyPostingInfo getPostingInfo(Long currencyId);
      record CurrencyPostingInfo(boolean active, boolean defaultCurrency) {}
  }
  ```

  Missing currency mengembalikan `null`; use case journal menerjemahkannya menjadi domain error. Default currency wajib rate `1`.

  ref: `src/main/java/com/solusi/erp/master/currency/infrastructure/adapter/CurrencyLookupProviderImpl.java:L25-L42`

- [x] Wire dua adapter di composition root pemilik slice masing-masing.

- [x] Tambahkan unit test edge cases:
  - COA active leaf diterima;
  - COA inactive ditolak;
  - COA header ditolak;
  - COA missing/null ditolak;
  - currency active default terdeteksi;
  - currency active non-default terdeteksi;
  - currency inactive dan missing ditolak oleh info yang dikembalikan.

**Simple verification:**

```powershell
.\mvnw.cmd -q -Dtest=CoaPostingValidatorImplTest,CurrencyPostingValidatorImplTest test
```

Expected: validator adapter tests lulus.

### Task 4: Extend Journal Persistence Safely [x]

Persist header currency/reference/reversal, line memo, update draft, delete draft, dan query reversal tanpa insert duplikat.

**Depends on:** Task 2

**Files:**
- Modify: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalEntryEntity.java`
- Modify: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalLineEntity.java`
- Modify: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalPersistenceMapper.java`
- Modify: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalEntryJpaRepository.java`
- Modify: `src/main/java/com/solusi/erp/accounting/journal/domain/repository/JournalEntryRepository.java`
- Modify: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/adapter/JournalEntryRepositoryImpl.java`
- Test: `src/test/java/com/solusi/erp/accounting/journal/infrastructure/adapter/JournalEntryRepositoryImplTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalPersistenceMapperTest.java`

- [x] Tambahkan entity fields sesuai V64. Ubah `sourceId` menjadi nullable. Tambahkan `description` pada `JournalLineEntity`.

  ref: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalEntryEntity.java:L18-L41`
  ref: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalLineEntity.java:L20-L42`

- [x] Refactor mapper:
  - `toDomain()` tidak lagi memanggil `SchemaEventType.valueOf`;
  - map header currency/rate/reference/reversal;
  - map line memo;
  - buat `toNewEntity(domain)` dan `applyToEntity(domain, entity)`;
  - `applyToEntity` mempertahankan ID/version/audit header existing, clear old child lines, dan add child lines baru dengan back-reference benar.

- [x] Extend repository domain port:

  ```java
  Optional<JournalEntry> findById(Long id);
  void deleteById(Long id);
  boolean existsReversalOf(Long originalJournalId);
  Optional<JournalEntry> findReversalOf(Long originalJournalId);
  ```

- [x] Extend JPA repository dengan `existsByReversalOfId(Long)` dan `findByReversalOfId(Long)`.

- [x] Ubah adapter `save()`:
  - ID null -> `toNewEntity`;
  - ID non-null -> load entity existing atau throw not-found, lalu `applyToEntity`;
  - save dan map kembali.

  Ini wajib agar update draft dan perubahan status post tidak menjadi insert baru.

- [x] Tambahkan mapper/repository tests:
  - round trip row auto lama dengan currency null tetap valid;
  - row manual `eventType=MANUAL` dapat dibaca;
  - line memo round trip;
  - update existing mempertahankan ID/version dan mengganti lines;
  - save domain ID non-null memakai lookup existing;
  - save domain ID non-null missing ditolak;
  - exists/find reversal delegate ke JPA repository;
  - delete delegate ke JPA repository.

**Simple verification:**

```powershell
.\mvnw.cmd -q -Dtest=JournalPersistenceMapperTest,JournalEntryRepositoryImplTest test
```

Expected: persistence mapping dan adapter tests lulus.

### Task 5: Extend Journal Read Path And Detail Read Model [x]

List filter harus dapat mencari `MANUAL`; detail view perlu relasi reversal tanpa mencampur query ke controller.

**Depends on:** Task 4

**Files:**
- Modify: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalEntryFilter.java`
- Modify: `src/main/java/com/solusi/erp/accounting/journal/domain/port/JournalEntryQueryPort.java`
- Modify: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/adapter/JournalEntryQueryPortImpl.java`
- Modify: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/query/GetJournalEntryDetailUseCase.java`
- Modify: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/query/GetJournalEntryDetailUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/query/JournalEntryDetailView.java`
- Test: `src/test/java/com/solusi/erp/accounting/journal/domain/model/JournalEntryFilterTest.java`
- Test: `src/test/java/com/solusi/erp/accounting/journal/infrastructure/adapter/JournalEntryQueryPortImplTest.java`
- Test: `src/test/java/com/solusi/erp/accounting/journal/application/usecase/query/JournalQueryUseCasesTest.java`

- [x] Ubah `JournalEntryFilter.sourceType` dari `SchemaEventType` menjadi `String`.

- [x] Pada specification query, bandingkan string langsung ke `eventType`. Pastikan filter `MANUAL` bekerja dan existing filter `GOODS_RECEIPT` tidak regress.

  ref: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/adapter/JournalEntryQueryPortImpl.java:L30-L50`

- [x] Tambahkan query port untuk mencari reversal berdasarkan original ID.

- [x] Buat read model:

  ```java
  public record JournalEntryDetailView(
      JournalEntry entry,
      Long reversedById
  ) {}
  ```

  `entry.getReversalOfId()` menjelaskan “Reversal of”; `reversedById` menjelaskan “Reversed by”.

- [x] Ubah `GetJournalEntryDetailUseCase` agar mengembalikan `Optional<JournalEntryDetailView>`, dengan lookup reversal dilakukan di query/application layer.

- [x] Extend tests:
  - manual string filter menghasilkan predicate event type manual;
  - unknown string filter tidak crash;
  - detail original mengisi `reversedById`;
  - detail reversal membawa `entry.reversalOfId`;
  - detail tanpa reversal tetap valid;
  - lazy lines tetap terbaca dalam read-only transaction.

**Simple verification:**

```powershell
.\mvnw.cmd -q -Dtest=JournalEntryFilterTest,JournalEntryQueryPortImplTest,JournalQueryUseCasesTest test
```

Expected: journal read path tests lulus.

### Task 6: Implement Draft Lifecycle Use Cases [x]

Create/update/delete manual journal memakai validator backend dan domain lifecycle.

**Depends on:** Task 3, Task 4

**Files:**
- Create: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ManualJournalLineCommand.java`
- Create: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ManualJournalCommand.java`
- Create: create/update/delete manual journal use case interfaces and implementations under `application/usecase/command`
- Test: create/update/delete use case tests under `src/test/java/com/solusi/erp/accounting/journal/application/usecase/command`

- [x] Buat command records:

  ```java
  ManualJournalLineCommand(Long accountId, BigDecimal debitAmount,
                           BigDecimal creditAmount, String description)
  ManualJournalCommand(LocalDate postingDate, Long currencyId,
                       BigDecimal exchangeRate, String referenceNo,
                       String description, List<ManualJournalLineCommand> lines)
  ```

- [x] Buat helper application private/shared untuk:
  - resolve currency via `CurrencyPostingValidator`;
  - reject missing/inactive currency;
  - reject default currency bila rate bukan `1`;
  - reject rate `<= 0`;
  - validate setiap account via `CoaPostingValidator`;
  - build `JournalLine` manual dari transaction amounts;
  - invoke transaction-currency balance validation.

- [x] Implement `CreateManualJournalUseCase`: validate references, build domain draft, save.

- [x] Implement `UpdateManualJournalUseCase`: find by ID, reject missing/non-manual/non-draft, validate references lagi, invoke `updateDraft`, save.

- [x] Implement `DeleteManualJournalUseCase`: find by ID, reject missing/non-manual/non-draft, delete by ID.

- [x] Tambahkan Mockito unit tests dengan edge cases:
  - create happy path;
  - create unbalanced ditolak dan repository tidak save;
  - create kurang dari dua line ditolak;
  - create null/inactive currency ditolak;
  - create default currency dengan rate bukan satu ditolak;
  - create non-default currency dengan rate positif diterima;
  - create invalid/header/inactive/missing COA ditolak;
  - update draft happy path;
  - update posted ditolak;
  - update auto journal ditolak;
  - update missing ID ditolak;
  - delete draft happy path;
  - delete posted, auto journal, dan missing ID ditolak.

  ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/command/CreateVendorBillUseCaseTest.java`
  ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/command/UpdateVendorBillUseCaseTest.java`

**Simple verification:**

```powershell
.\mvnw.cmd -q -Dtest=CreateManualJournalUseCaseTest,UpdateManualJournalUseCaseTest,DeleteManualJournalUseCaseTest test
```

Expected: draft lifecycle tests lulus.

### Task 7: Implement Post And Reverse Use Cases [x]

Post draft dan reversal original manual harus memakai period guard, revalidation, dan database race protection.

**Depends on:** Task 6

**Files:**
- Create: post/reverse manual journal use case interfaces and implementations under `application/usecase/command`
- Test: `src/test/java/com/solusi/erp/accounting/journal/application/usecase/command/PostManualJournalUseCaseTest.java`
- Test: `src/test/java/com/solusi/erp/accounting/journal/application/usecase/command/ReverseManualJournalUseCaseTest.java`

- [x] Implement `PostManualJournalUseCase.execute(Long id)`:
  - load journal;
  - reject missing/non-manual/non-draft;
  - revalidate currency dan accounts agar akun yang dinonaktifkan setelah draft tidak lolos;
  - validate balance;
  - call `EnsureOpenPeriodForDateUseCase.execute(entry.getJournalDate())`;
  - `entry.post()`;
  - save.

- [x] Implement `ReverseManualJournalUseCase.execute(Long id, LocalDate reversalDate)`:
  - load journal;
  - reject missing, non-manual, non-posted, dan journal reversal (`reversalOfId != null`);
  - reject bila `existsReversalOf(id)`;
  - validate reversal date non-null;
  - call period guard untuk reversal date;
  - buat reversal via domain;
  - save sebagai row baru.

- [x] Tangkap pelanggaran unique reversal constraint di boundary yang sesuai dan resolve ke message domain konsisten, misalnya `msg.error.journal.already.reversed`.

- [x] Tambahkan Mockito tests:
  - post happy path memanggil period guard sebelum save;
  - post draft dengan closed period mempropagasi error dan tidak save;
  - post posted/non-manual/missing ditolak;
  - post revalidates currency/account;
  - reverse happy path menghasilkan row `POSTED`, mewarisi currency/rate, dan swaps lines;
  - reverse tanggal closed period ditolak;
  - reverse auto journal ditolak;
  - reverse draft ditolak;
  - reverse journal yang merupakan reversal ditolak;
  - reverse original yang sudah punya reversal ditolak;
  - reverse missing ID dan null date ditolak.

  ref: `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/EnsureOpenPeriodForDateUseCaseImpl.java:L16-L20`
  ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/command/ConfirmVendorBillUseCaseTest.java:L86-L112`

**Simple verification:**

```powershell
.\mvnw.cmd -q -Dtest=PostManualJournalUseCaseTest,ReverseManualJournalUseCaseTest test
```

Expected: posting dan reversal tests lulus.

### Task 8: Wire Journal Composition Root [x]

Semua journal commands harus memiliki transaction boundary eksplisit.

**Depends on:** Task 5, Task 6, Task 7

**Files:**
- Modify: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/config/JournalConfig.java`
- Create: `src/test/java/com/solusi/erp/accounting/journal/infrastructure/config/JournalConfigTest.java`

- [x] Wire create/update/delete/post/reverse use cases di `JournalConfig`.
- [x] Wrap command use cases dengan `TransactionTemplate`.
- [x] Pertahankan query use case dalam read-only transaction.
- [x] Pertahankan `PostJournalForEventUseCase` existing agar auto-posting GR/VB tidak regress.
- [x] Tambahkan config context test dengan mocked JPA repository, validator ports, schema repository, period guard, dan transaction manager. Assert seluruh bean journal tersedia.

  ref: `src/main/java/com/solusi/erp/accounting/schema/infrastructure/config/SchemaConfig.java:L30-L75`
  ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/config/VendorBillConfigTest.java:L27-L95`

**Simple verification:**

```powershell
.\mvnw.cmd -q -Dtest=JournalConfigTest,PostJournalForEventUseCaseTest test
```

Expected: composition root valid dan auto-posting test existing tetap hijau.

### Task 9: Add Web DTOs, Mapper, And Controller Routes

Expose form, CRUD, post, reverse, list manual filter, currency prefill, dan detail reversal secara konsisten.

**Depends on:** Task 5, Task 8

**Files:**
- Create: journal save/line/reverse request DTOs under `src/main/java/com/solusi/erp/accounting/journal/web/dto`
- Modify: existing journal response DTOs
- Modify: `src/main/java/com/solusi/erp/accounting/journal/web/mapper/JournalEntryWebMapper.java`
- Modify: `src/main/java/com/solusi/erp/accounting/journal/web/controller/JournalEntryController.java`
- Test: `src/test/java/com/solusi/erp/accounting/journal/web/mapper/JournalEntryWebMapperTest.java`
- Test: `src/test/java/com/solusi/erp/accounting/journal/web/controller/JournalEntryControllerTest.java`
- Test: `src/test/java/com/solusi/erp/architecture/WebLayerDependencyGuardTest.java`

- [ ] Buat `JournalEntrySaveRequest extends BaseAuditResponse`:
  - `postingDate` dengan `@NotNull` dan `@DateTimeFormat(pattern="yyyy-MM-dd")`;
  - `currencyId`, `exchangeRate`, `referenceNo`, `description`;
  - `List<JournalLineSaveRequest> lines`;
  - line DTO: `accountId`, `accountName`, `accountCode`, `debitAmount`, `creditAmount`, `description`.

- [ ] Buat `ReverseJournalRequest` dengan `@NotNull @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate postingDate`.

- [ ] Extend response DTO:
  - header currency ID/name/code, exchangeRate, referenceNo;
  - reversalOfId/code dan reversedById/code;
  - line memo;
  - transaction totals dan base totals;
  - helper booleans `manual`, `reversal`, `reversed`, `multiCurrency`.

- [ ] Extend mapper:
  - map detail read model, bukan entity;
  - format journal code tetap `JNL-%06d`;
  - enrich currency/account label via lookup provider;
  - map manual form request untuk edit dengan trinity autocomplete fields;
  - map save request ke `ManualJournalCommand`;
  - pertahankan mapping auto journal lama.

  ref: `src/main/java/com/solusi/erp/accounting/journal/web/mapper/JournalEntryWebMapper.java:L20-L117`

- [ ] Tambahkan `@DefaultRedirectUrl` pada controller. Inject hanya use case, `GetDefaultCurrencyUseCase`, `CurrencyLookupProvider`, mapper, dan `MessageSource`; jangan inject JPA repository.

- [ ] Tambahkan routes:

  | Method | Route | Permission | Result |
  |---|---|---|---|
  | GET | `/accounting/journal-entries/create` | `JOURNAL-ENTRY_CREATE` | form draft baru, posting date hari ini, default currency/rate |
  | GET | `/accounting/journal-entries/edit/{id}` | `JOURNAL-ENTRY_UPDATE` | form draft existing |
  | POST | `/accounting/journal-entries` | `JOURNAL-ENTRY_CREATE` | JSON `ApiResponse`, HTTP 201 |
  | PUT | `/accounting/journal-entries/{id}` | `JOURNAL-ENTRY_UPDATE` | JSON `ApiResponse` |
  | DELETE | `/accounting/journal-entries/{id}` | `JOURNAL-ENTRY_DELETE` | refresh table response |
  | POST | `/accounting/journal-entries/{id}/post` | `JOURNAL-ENTRY_POST` | JSON `ApiResponse` |
  | POST | `/accounting/journal-entries/{id}/reverse` | `JOURNAL-ENTRY_REVERSE` | JSON body reversal date |

  ref: `src/main/java/com/solusi/erp/accountspayable/vendorpayment/web/controller/VendorPaymentController.java:L85-L180`
  ref: `docs/spec/form-submission.md:L116-L128`

- [ ] Pada list, expose filter values string: semua `SchemaEventType.name()` plus `"MANUAL"`.

- [ ] Extend controller/mapper tests:
  - create form prefills hari ini, default currency, rate one, dan lookup trinity;
  - edit form rejects missing journal melalui global handler path;
  - list exposes manual filter;
  - create/update mapping delegates command benar;
  - delete/post/reverse delegate use case benar;
  - setiap route memiliki `@PreAuthorize` tepat;
  - detail maps `reversalOf/reversedBy`;
  - web layer dependency guard tetap lulus.

**Simple verification:**

```powershell
.\mvnw.cmd -q -Dtest=JournalEntryControllerTest,JournalEntryWebMapperTest,WebLayerDependencyGuardTest test
```

Expected: controller, mapper, dan dependency guard tests lulus.

### Task 10: Build Thymeleaf Form And Extend List/Detail Templates

Buat UI manual journal dengan pola header-lines, security visibility, numeric/date/autocomplete standards, dan reversal modal.

**Depends on:** Task 9

**Files:**
- Create: `src/main/resources/templates/accounting/journal/journal-entry-form.html`
- Modify: `src/main/resources/templates/accounting/journal/journal-entry-list.html`
- Modify: `src/main/resources/templates/accounting/journal/journal-entry-detail.html`
- Modify: `src/test/java/com/solusi/erp/accounting/journal/web/template/JournalTemplateTest.java`
- Modify: `src/test/java/com/solusi/erp/accounting/journal/web/template/integration/JournalTemplateIntegrationTest.java`

- [ ] Sebelum edit, baca ulang spesifikasi frontend:
  - `docs/spec/autocomplete-generic.md`
  - `docs/spec/numeric-standards.md`
  - `docs/spec/datetime-standards.md`
  - `docs/spec/header-lines-form.md`
  - `docs/spec/form-submission.md`
  - `docs/spec/action-buttons.md`
  - `docs/spec/currency-exchange-rate.md`
  - `docs/spec/page-specific-scripts.md`

- [ ] Buat satu form create/edit:
  - `data-ajax-form="true"`;
  - action create/update sesuai ID;
  - hidden CSRF, ID, version;
  - kartu Detail berisi Reference No, Posting Date, Currency autocomplete, Exchange Rate decimal, Description;
  - date memakai `data-picker="date"`;
  - currency memakai fragment autocomplete dengan `initialValue`, `initialText`, `initialSubtext`, `initialPayloadIsDefault`;
  - exchange rate memakai fragment decimal dan readonly style saat default currency.

  ref: `src/main/resources/templates/accountspayable/vendor-payments/form.html:L24-L94`
  ref: `docs/spec/autocomplete-generic.md:L34-L48`

- [ ] Buat kartu Journal Lines:
  - `<tbody id="line-container">`;
  - row existing memakai class `.line-row`;
  - hidden template `<tbody id="row-template-source">`;
  - per row COA `<select data-lookup-path="accounting/coa">`;
  - render trinity data account ID/name/code pada mode edit;
  - debit/credit `.erp-number-decimal`;
  - memo text input;
  - add/remove buttons;
  - total transaction debit/credit;
  - base total;
  - badge `Balanced` / `Unbalanced`.

  ref: `docs/spec/header-lines-form.md:L18-L26`
  ref: `docs/spec/numeric-standards.md:L29-L39`

- [ ] Extend list:
  - tombol create guarded `JOURNAL-ENTRY_CREATE`;
  - manual filter option;
  - draft badge berbeda dari posted;
  - delete action hanya draft manual dan guarded `JOURNAL-ENTRY_DELETE`;
  - pertahankan pagination fragment existing.

- [ ] Extend detail:
  - tampilkan Reference No, Currency, Exchange Rate;
  - source type `MANUAL` memakai label manual tanpa link source;
  - line memo;
  - transaction amount dan base amount ketika multi-currency;
  - badges `Reversal of JNL-xxxxxx` dan `Reversed by JNL-xxxxxx` dengan link;
  - DRAFT: Edit/Delete/Post sesuai permission;
  - original POSTED manual belum reversed: Reverse sesuai permission;
  - auto-posted dan reversal row tidak menampilkan tombol reverse.

- [ ] Post button memakai `ErpForm.postAction` karena tidak membutuhkan payload. Reverse button membuka Bootstrap modal `#reverse-journal-modal` berisi date picker default hari ini karena reversal membutuhkan payload tanggal.

  ref: `docs/spec/action-buttons.md:L5-L49`

- [ ] Extend static/template render tests:
  - form memiliki AJAX attrs, row template, binding names, picker, autocomplete, numeric classes;
  - initial currency payload metadata dirender;
  - detail action visibility sesuai permission/status;
  - auto journal tidak mendapat reverse action;
  - reversal badges/link dirender;
  - list create/delete visibility sesuai permission;
  - format angka tetap dua desimal.

**Simple verification:**

```powershell
.\mvnw.cmd -q -Dtest=JournalTemplateTest,JournalTemplateIntegrationTest test
```

Expected: template static dan render tests lulus.

### Task 11: Add Page-Specific JavaScript

Wire dynamic rows, TomSelect, AutoNumeric, currency lock, balance recap, client guard, dan reversal modal.

**Depends on:** Task 10

**Files:**
- Create: `src/main/resources/static/js/accounting/journal/journal-entry-form.js`
- Create: `src/main/resources/static/js/accounting/journal/journal-entry-detail.js`
- Modify: `src/main/resources/templates/accounting/journal/journal-entry-form.html`
- Modify: `src/main/resources/templates/accounting/journal/journal-entry-detail.html`
- Modify: `src/test/java/com/solusi/erp/accounting/journal/web/template/JournalTemplateTest.java`

- [ ] Pada form JS:
  - instantiate `new ErpLineManager("line-container", "row-template-source", { onUpdate: calculateTotals })`;
  - init lookup existing dan row baru dengan `initLookup(select, "accounting/coa")`;
  - init numeric row baru melalui helper global;
  - add/remove line melalui `ErpLineManager`;
  - hitung transaction debit/credit menggunakan `ErpNumeric.get`;
  - hitung base totals dengan exchange rate;
  - update badge balanced/unbalanced real-time;
  - listen input/change pada debit, credit, rate;
  - init `ERP.CurrencyRateLock` untuk currency dan exchange rate;
  - capture submit sebelum handler global dan blok bila line < 2 atau total tidak balance.

  ref: `src/main/resources/static/js/shared/erp-common-handler.js:L238-L280`
  ref: `src/main/resources/static/js/shared/erp-common-handler.js:L303-L316`
  ref: `src/main/resources/static/js/accountspayable/vendor-payments/form.js:L235-L274`
  ref: `docs/spec/form-submission.md:L52-L57`

- [ ] Jangan mengandalkan TomSelect payload untuk COA edit row. Nama/kode initial harus berasal dari rendered `<option selected data-subtext="...">`.

  ref: `docs/spec/page-specific-scripts.md:L79-L90`

- [ ] Pada detail JS:
  - open reversal modal;
  - ambil ISO date dari input;
  - POST JSON ke `/{id}/reverse` dengan CSRF;
  - disable confirm untuk mencegah double-click;
  - on success simpan `erp_pending_success`, redirect ke detail reversal;
  - on error tampilkan `ErpModal.showError` dan re-enable button.

- [ ] Tambahkan static test bahwa kedua scripts di-include via page script slot, currency rate lock dimuat sebelum form JS, dan reverse modal mempunyai endpoint/date binding yang dibutuhkan.

**Simple verification:**

```powershell
.\mvnw.cmd -q -Dtest=JournalTemplateTest test
```

Expected: script contract static test lulus.

### Task 12: Add i18n Keys And Synchronize Journal Documentation

Tambahkan message keys ID/EN dan ubah business docs dari read-only menjadi lifecycle manual + auto.

**Depends on:** Task 9, Task 10, Task 11

**Files:**
- Modify: `src/main/resources/messages_id.properties`
- Modify: `src/main/resources/messages_en.properties`
- Modify: `docs/modules/accounting/journal-entry.md`
- Modify: `docs/architecture/journal-posting-engine.md`
- Modify: `src/test/java/com/solusi/erp/accounting/journal/web/template/JournalMessageBundleTest.java`

- [ ] Tambahkan keys ID dan EN untuk:
  - label manual journal, reference no, currency, exchange rate, memo, transaction amount, base amount;
  - create/edit/post/delete/reverse;
  - balanced/unbalanced;
  - reversal of/reversed by;
  - confirmation modal post/delete/reverse;
  - success create/update/delete/post/reverse;
  - errors lines minimum, invalid currency, default rate must one, invalid account, invalid status, auto journal immutable, already reversed, reversal-of-reversal forbidden.

- [ ] Update bundle test agar setiap key baru wajib ada di kedua locale. Jangan gunakan template render MessageSource untuk bundle existence karena test helper tidak memuat application MessageSource lengkap.

- [ ] Update business docs:
  - journal sekarang memiliki dua jalur: auto-posted immutable dan manual draft-post-reversal;
  - schema data/header currency/reference/reversal;
  - permission baru;
  - period guard scope manual;
  - deferred items tetap eksplisit.

  ref: `docs/modules/accounting/journal-entry.md:L1-L264`
  ref: `docs/architecture/journal-posting-engine.md:L1-L372`

**Simple verification:**

```powershell
.\mvnw.cmd -q -Dtest=JournalMessageBundleTest test
```

Expected: bundle ID/EN lengkap.

### Task 13: Add Playwright E2E Happy Path

Uji flow user nyata: create draft, edit, post, reverse, dan verifikasi linkage dari browser.

**Depends on:** Task 1 through Task 12

**Files:**
- Create: `e2e-tests/tests/accounting/manual-journal-entry.spec.ts`
- Modify: `e2e-tests/scripts/run-e2e.ps1`
- Modify: `e2e-tests/scripts/warmup-urls.txt`
- Reference only: `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql`

- [ ] Sebelum menulis spec, baca ulang `docs/tests/playwright-pitfalls.md`, journal list/detail/form templates, dan dua journal JS files. Jangan memakai helper `selectTomSelect()` karena signature load-nya known broken.

  ref: `docs/tests/playwright-pitfalls.md:L53-L87`

- [ ] Gunakan admin storage state. Tambahkan warmup:

  ```text
  /accounting/journal-entries
  /accounting/journal-entries/create
  ```

- [ ] Tambahkan parameter optional pada `run-e2e.ps1` agar task dapat menjalankan spec terarah tanpa mengubah default full-suite behavior:

  ```powershell
  param(
      [Parameter(ValueFromRemainingArguments = $true)]
      [string[]]$PlaywrightArgs
  )
  ...
  npx playwright test @PlaywrightArgs
  ```

  Tanpa argumen, PowerShell meneruskan array kosong dan runner tetap mengeksekusi seluruh Playwright suite.

  ref: `e2e-tests/scripts/run-e2e.ps1:L1-L88` - current runner always calls `npx playwright test`

- [ ] Buat helper lokal untuk add row dan set COA. Karena COA tidak membutuhkan payload turunan, `setTomSelectValue()` boleh dipakai setelah row dan TomSelect siap. Gunakan seeded accounts:
  - debit: `9401` / `E2E Inventory`
  - credit: `9405` / `E2E Bank`
  - default currency IDR dari seeder
  - open period date: `2026-05-20`

  ref: `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql:L219-L243`
  ref: `e2e-tests/helpers/tomselect.ts:L53-L83`

- [ ] Buat satu scenario serial `"manual journal create edit post and reverse happy path"`:
  1. Navigate `/accounting/journal-entries/create`.
  2. Assert default IDR terpilih, rate `1`, dan readonly.
  3. Set posting date `2026-05-20` dengan `setFlatpickrDate`.
  4. Isi reference unik `E2E-MJ-${Date.now()}`.
  5. Tambah dua line, pilih COA 9401 dan 9405.
  6. Isi debit/credit `125000` dengan `setAutoNumeric`.
  7. Assert badge balanced.
  8. Submit AJAX dan tunggu redirect list.
  9. Cari row reference unik, buka draft detail, assert badge `DRAFT`.
  10. Buka edit, ubah memo/reference, submit, assert perubahan terlihat.
  11. Klik Post, lalu click `#confirm-modal-btn-yes`, assert detail status `POSTED`.
  12. Klik Reverse, isi reversal date `2026-05-20`, confirm modal reversal.
  13. Assert redirect ke detail reversal row `POSTED`, badge `Reversal of JNL-*`, dan debit/credit swapped.
  14. Kembali ke original dan assert badge/link `Reversed by JNL-*`; tombol reverse sudah hilang.

  ref: `e2e-tests/helpers/autonumeric.ts:L11-L39`
  ref: `e2e-tests/helpers/flatpickr.ts:L20-L51`
  ref: `docs/tests/playwright-pitfalls.md:L91-L112` - Bootstrap modal confirmation

- [ ] Pada kegagalan pertama, baca screenshot/video/trace dari Playwright artifact sebelum mengubah selector. Jangan menandai task selesai hanya karena `tsc` dan `--list` lulus.

**Simple verification:**

```powershell
cd e2e-tests
npx tsc --noEmit
npx playwright test tests/accounting/manual-journal-entry.spec.ts --list
cd ..
.\e2e-tests\scripts\run-e2e.ps1 tests/accounting/manual-journal-entry.spec.ts
```

Expected: TypeScript clean, scenario terdaftar, lalu spec hidup lulus minimal satu kali.

### Task 14: Final Regression, SemVer, And Report

Jalankan gate menyeluruh, update version setelah feature lolos, dan isi report implementasi.

**Depends on:** Task 1 through Task 13

**Files:**
- Modify after successful implementation verification: `pom.xml`
- Populate: `docs/reports/2026-05-31-manual-journal-entry.md`

- [ ] Jalankan journal-focused suite:

  ```powershell
  .\mvnw.cmd -q -Dtest="*Journal*" test
  ```

- [ ] Jalankan full Java gate:

  ```powershell
  .\mvnw.cmd clean test
  ```

- [ ] Jalankan full E2E gate:

  ```powershell
  .\e2e-tests\scripts\run-e2e.ps1
  ```

- [ ] Setelah implementasi dan testing sukses, bump versi `pom.xml` dengan SemVer **MINOR** karena manual journal adalah fitur baru backward-compatible. Jangan bump version sebelum gate feature lolos.

  ref: `docs/AGENTS.md` section `9.A Semantic Versioning Automation`

- [ ] Setelah version bump, jalankan ulang:

  ```powershell
  .\mvnw.cmd clean test
  .\e2e-tests\scripts\run-e2e.ps1
  ```

- [ ] Isi report dengan:
  - task yang selesai;
  - file yang berubah;
  - keputusan/deviation dari plan;
  - hasil command verifikasi;
  - status MariaDB migration smoke;
  - risiko residual bila ada.

- [ ] Periksa `git status --short` dan pastikan tidak ada file generated artifact yang ikut staged.

**Final validation criteria:**

- Semua journal unit/integration tests hijau.
- `mvn clean test` hijau.
- E2E manual journal hidup hijau.
- Full Playwright E2E hijau setelah version bump.
- Migration MariaDB V64 diverifikasi pada profile dev atau dicatat eksplisit sebagai gate deployment yang belum dapat dijalankan.

## 6. Coverage Matrix

| Brainstorm Requirement | Covered By |
|---|---|
| Draft -> Posted lifecycle | Tasks 2, 6, 7, 9, 10, 13 |
| No approval | Tasks 6-9 |
| Header currency + exchange rate | Tasks 1, 2, 4, 9-11 |
| Rate 1 for base currency | Tasks 3, 6, 10, 11, 13 |
| Balance transaction currency | Tasks 2, 6, 11 |
| Reversal manual-only, once, open period | Tasks 1, 2, 7, 9-11, 13 |
| ID-based journal code | Tasks 9, 10 |
| Per-line memo | Tasks 1, 2, 4, 9-11 |
| Detail badges and actions | Tasks 5, 9-11 |
| Permissions | Tasks 1, 9, 10 |
| Backend postable COA guard | Tasks 3, 6, 7 |
| H2 migration mirror | Tasks 1, 13, 14 |
| Unit edge cases | Tasks 2-9, 12 |
| E2E happy path | Task 13 |
| Docs synchronization | Task 12 |

## 7. Explicitly Deferred

- Approval workflow.
- Per-line currency.
- Reverse auto-posted GR/VB journals.
- Retrofit period guard into existing auto-posting engine.
- General Ledger View.
- Trial Balance.
- Reversal-of-reversal chain UI.
- Any refactor outside journal, COA posting validator, and currency posting validator required by this feature.
