# Journal Auto-Posting Engine — Architecture

> Dokumen ini menjelaskan arsitektur teknis sistem auto-posting jurnal akuntansi.  
> Untuk spesifikasi bisnis dan aturan jurnal per event, lihat [docs/modules/accounting/journal-entry.md](../modules/accounting/journal-entry.md).

---

## 1. Gambaran Umum

Journal Auto-Posting Engine adalah **framework generik** yang memungkinkan setiap modul operasional (GR, Vendor Bill, Payment, dll.) memposting jurnal akuntansi tanpa perlu mengetahui detail COA atau aturan debit/kredit. Aturan tersebut dikonfigurasi di **Accounting Schema** oleh administrator.

```
Modul Operasional                 Engine                      Accounting
(GR, Bill, Payment)               (Journal Module)            (Schema + COA)
        │                               │                           │
        │  JournalPostingCommand         │                           │
        │──────────────────────────────▶│                           │
        │  { eventType,                 │  findByEventType()        │
        │    sourceId,                  │──────────────────────────▶│
        │    values: Map<Var, Amount> } │                           │
        │                               │◀─────── AccountingSchema ─┤
        │                               │                           │
        │                               │  Build JournalEntry       │
        │                               │  Validate balance         │
        │                               │  Save                     │
        │◀──────────────────────────────│                           │
```

---

## 2. Package Structure

```
accounting/
├── journal/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── JournalEntry.java          ← Aggregate Root
│   │   │   ├── JournalLine.java           ← Value Object (Record)
│   │   │   ├── JournalStatus.java         ← Enum: DRAFT, POSTED
│   │   │   ├── JournalPosition.java       ← Enum: DEBIT, CREDIT
│   │   │   ├── JournalVariable.java       ← Enum: variable → event mapping
│   │   │   └── JournalEntryFilter.java    ← Search criteria (Record)
│   │   └── repository/
│   │       ├── JournalEntryRepository.java     ← Write port
│   │       └── JournalEntryQueryPort.java      ← Read port
│   ├── application/
│   │   └── usecase/
│   │       ├── command/
│   │       │   ├── JournalPostingCommand.java          ← Input DTO
│   │       │   ├── PostJournalForEventUseCase.java     ← Interface
│   │       │   └── PostJournalForEventUseCaseImpl.java ← Implementation
│   │       └── query/
│   │           ├── FindJournalEntriesUseCase(Impl).java
│   │           └── GetJournalEntryDetailUseCase(Impl).java
│   ├── infrastructure/
│   │   ├── persistence/
│   │   │   ├── JournalEntryEntity.java
│   │   │   ├── JournalLineEntity.java
│   │   │   ├── JournalEntryJpaRepository.java
│   │   │   └── JournalPersistenceMapper.java
│   │   ├── adapter/
│   │   │   ├── JournalEntryRepositoryImpl.java
│   │   │   └── JournalEntryQueryPortImpl.java
│   │   └── config/
│   │       └── JournalConfig.java
│   └── web/
│       ├── controller/JournalEntryController.java
│       ├── dto/
│       │   ├── JournalEntryListResponse.java
│       │   ├── JournalEntryDetailResponse.java
│       │   └── JournalLineResponse.java
│       └── mapper/JournalEntryWebMapper.java
└── schema/
    └── domain/
        ├── model/
        │   ├── AccountingSchema.java       ← Posting rules aggregate
        │   ├── AccountingSchemaLine.java   ← Value Object
        │   ├── SchemaEventType.java        ← Enum: all supported events
        │   └── JournalVariable.java        ← Shared enum (di journal domain)
        └── repository/
            └── SchemaRepository.java
```

---

## 3. Domain Model

### 3.1 JournalEntry (Aggregate Root)

```java
public class JournalEntry {
    AuditMetadata metadata;       // id, version, audit trail
    SchemaEventType eventType;    // GOODS_RECEIPT, VENDOR_BILL, ...
    String sourceType;            // "GOODS_RECEIPT"
    Long sourceId;                // FK ke dokumen asal
    String sourceCode;            // "GR-202605-00001"
    LocalDate journalDate;
    String description;
    JournalStatus status;         // selalu POSTED untuk auto-journal
    List<JournalLine> lines;      // immutable
}
```

**Factory method:**
```java
JournalEntry.createPosted(eventType, sourceType, sourceId, sourceCode,
                          postingDate, description, lines)
```

**Invariant:**
- `lines` tidak boleh kosong
- `validateBalanced()`: `Σ debit = Σ kredit` atau throw `DomainException`

### 3.2 JournalLine (Value Object — Record)

```java
public record JournalLine(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount) {
    static JournalLine debit(Long accountId, BigDecimal amount)
    static JournalLine credit(Long accountId, BigDecimal amount)
}
```

**Invariant:**
- Tidak boleh keduanya non-zero
- Tidak boleh keduanya zero
- Tidak boleh negative

### 3.3 JournalVariable (Enum)

Enum ini adalah **kontrak** antara modul operasional dan engine posting. Setiap variable dipetakan ke satu `SchemaEventType`:

```java
public enum JournalVariable {
    // GOODS_RECEIPT
    GR_INVENTORY_AMT(GOODS_RECEIPT),
    GR_TAX_AMT(GOODS_RECEIPT),
    GR_GRAND_TOTAL(GOODS_RECEIPT),

    // VENDOR_BILL
    VB_GRIR_CLEARING_AMT(VENDOR_BILL),
    VB_TAX_AMT(VENDOR_BILL),
    VB_FX_LOSS_AMT(VENDOR_BILL),
    VB_AP_TOTAL(VENDOR_BILL),
    VB_FX_GAIN_AMT(VENDOR_BILL),

    // VENDOR_PAYMENT
    VP_AP_AMT(VENDOR_PAYMENT),
    VP_BANK_OUT_AMT(VENDOR_PAYMENT),

    // ... dst.
    ;

    List<JournalVariable> getVariablesForEvent(SchemaEventType eventType)
}
```

---

## 4. Posting Algorithm

`PostJournalForEventUseCaseImpl.execute()`:

```
1. Idempotency check
   └─ journalEntryRepository.existsBySource(sourceType, sourceId)
   └─ Jika sudah ada → return (skip, no error)

2. Load active schema
   └─ schemaRepository.findByEventTypeAndIsActiveTrue(eventType)
   └─ Jika tidak ada → throw DomainException("msg.error.journal.schema.notfound")

3. Build journal lines dari schema lines
   └─ Untuk setiap AccountingSchemaLine:
       a. Ambil nilai: command.values().getOrDefault(schemaLine.var, ZERO)
       b. Skip jika nilai = null atau = 0
       c. Buat JournalLine.debit() atau .credit() sesuai position

4. Create JournalEntry
   └─ JournalEntry.createPosted(...)

5. Validate balance
   └─ entry.validateBalanced()
   └─ Jika tidak balance → throw DomainException("msg.error.journal.unbalanced")

6. Persist
   └─ journalEntryRepository.save(entry)
```

---

## 5. Integrasi dengan Modul Operasional

### Pola Integrasi

Modul operasional **tidak import** kelas dari journal domain secara langsung — cukup inject `PostJournalForEventUseCase` (interface) dan membangun `JournalPostingCommand`.

```java
// Di CompleteGoodsReceiptUseCaseImpl
postJournalForEventUseCase.execute(new JournalPostingCommand(
    SchemaEventType.GOODS_RECEIPT,
    "GOODS_RECEIPT",
    receipt.getId(),
    receipt.getCode(),
    receipt.getReceiptDate(),
    "Auto journal for goods receipt " + receipt.getCode(),
    Map.of(
        JournalVariable.GR_INVENTORY_AMT, inventoryTotal,
        JournalVariable.GR_TAX_AMT,       BigDecimal.ZERO,
        JournalVariable.GR_GRAND_TOTAL,   inventoryTotal
    )
));
```

### Integrasi yang Sudah Ada

| Modul | Use Case | Event Type |
|---|---|---|
| Goods Receipt | `CompleteGoodsReceiptUseCaseImpl` | `GOODS_RECEIPT` |
| Vendor Bill | `ConfirmVendorBillUseCaseImpl` | `VENDOR_BILL` |

### Cara Menambah Integrasi Baru

Untuk event baru berikutnya (contoh: Vendor Payment):

1. **Tambah `SchemaEventType`** (jika belum ada) — sudah ada semua di enum
2. **Tambah `JournalVariable`** (jika variable baru dibutuhkan)
3. **Konfigurasi Accounting Schema** via UI (admin input akun COA per variable)
4. **Inject** `PostJournalForEventUseCase` ke use case operasional baru
5. **Build** `JournalPostingCommand` dengan values yang sesuai
6. **Wire** di Config class modul yang bersangkutan

---

## 6. Transaksi & Atomicity

### Strategi: Synchronous In-Transaction Posting

```
┌─────────────────────────────────────────────┐
│  @Transactional                              │
│                                             │
│  CompleteGoodsReceiptUseCase / ConfirmVendorBillUseCase │
│    ├── validate period open                 │
│    ├── update operational aggregate         │
│    └── PostJournalForEventUseCase  ◄────────┤
│         └── save JournalEntry               │
│                                             │
│  COMMIT (semua atau tidak sama sekali)      │
└─────────────────────────────────────────────┘
```

**Implementasi di Config:**
```java
@Bean
public CompleteGoodsReceiptUseCase completeGoodsReceiptUseCase(
        ..., PostJournalForEventUseCase postJournalUseCase,
        PlatformTransactionManager txManager) {
    CompleteGoodsReceiptUseCase pure = new CompleteGoodsReceiptUseCaseImpl(
            ..., postJournalUseCase);
    TransactionTemplate tx = new TransactionTemplate(txManager);
    return id -> tx.execute(status -> { pure.execute(id); return null; });
}
```

**Trade-off yang diambil:**

| Aspek | Pilihan | Alternatif yang Ditolak |
|---|---|---|
| **Timing** | Synchronous | Async/event-driven |
| **Scope** | Same transaction | Separate transaction |
| **Failure** | Rollback transaksi induk | Retry queue |
| **Reasoning** | Atomic consistency MVP | Kompleks untuk MVP |

---

## 7. Database Schema

```sql
-- Header
CREATE TABLE acc_journal_entries (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type  VARCHAR(50) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id   BIGINT NOT NULL,
    source_code VARCHAR(60),
    posting_date DATE NOT NULL,
    description VARCHAR(255),
    status      VARCHAR(20) NOT NULL,
    -- audit columns ...
    CONSTRAINT uk_acc_journal_source UNIQUE (source_type, source_id)
);

-- Lines
CREATE TABLE acc_journal_lines (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    journal_entry_id BIGINT NOT NULL,
    line_no          INT NOT NULL,
    account_id       BIGINT NOT NULL,
    debit_amount     DECIMAL(19,4) NOT NULL DEFAULT 0,
    credit_amount    DECIMAL(19,4) NOT NULL DEFAULT 0,
    -- audit columns ...
    CONSTRAINT fk_acc_journal_lines_entry
        FOREIGN KEY (journal_entry_id) REFERENCES acc_journal_entries(id)
        ON DELETE CASCADE
);
```

**Key design decisions:**
- `UNIQUE (source_type, source_id)` → idempotency di level DB (backup dari aplikasi)
- `ON DELETE CASCADE` → lines ikut terhapus jika header dihapus (admin only)
- `DECIMAL(19,4)` → presisi tinggi untuk nilai moneter

---

## 8. CQRS: Read Path

Read path **terpisah** dari write path, menggunakan `JournalEntryQueryPort`:

```
Controller
    └─ FindJournalEntriesUseCase
        └─ JournalEntryQueryPort (interface)
            └─ JournalEntryQueryPortImpl
                └─ JpaSpecificationExecutor (dynamic filter)
                    └─ JournalEntryJpaRepository
```

**Filter yang didukung:**
- `eventType` — exact match
- `sourceCode` — LIKE search
- `journalCode` — parse ID dari "JNL-XXXXXX"
- `postingDateFrom` / `postingDateTo` — range

---

## 9. Error Messages (i18n Keys)

| Key | Kondisi |
|---|---|
| `msg.error.journal.schema.notfound` | Tidak ada active schema untuk event type |
| `msg.error.journal.unbalanced` | Total debit ≠ total kredit |
| `msg.error.journal.lines.required` | Journal entry tanpa lines |
| `msg.error.journal.invalid.amount` | Amount bernilai negatif |
| `msg.error.journal.invalid.line` | Line memiliki debit dan kredit sekaligus |

---

## 10. Test Coverage

| Layer | Test Class | Skenario |
|---|---|---|
| Domain | `JournalEntryTest` | Balance validation, factory methods, line constraints |
| Domain | `JournalVariableTest` | Variable filtering per event type |
| Application | `PostJournalForEventUseCaseTest` | Schema missing, happy path |
| Application | `JournalQueryUseCasesTest` | Delegation ke query port |
| Infrastructure | `JournalEntryQueryPortImplTest` | Filter + mapping |
| Web | `JournalEntryControllerTest` | View + model attributes |
| Web | `JournalEntryWebMapperTest` | DTO mapping + code formatting |
| Web | `JournalMessageBundleTest` | i18n key existence |
| Web | `JournalTemplateTest` | Template rendering |
| Integration | `JournalTemplateIntegrationTest` | End-to-end template |

---

## 11. Referensi Terkait

- [Journal Entry — Business Spec](../modules/accounting/journal-entry.md)
- [Accounting Schema](../modules/accounting/accounting-schema.md)
- [Clean DDD CQRS Standard](clean-ddd-cqrs-standard.md)
- [Accounting Foundation](accounting-foundation.md)
- [Sprint 5 — Vendor Bill Roadmap](../roadmap/sprint-5-vendor-bill.md)
