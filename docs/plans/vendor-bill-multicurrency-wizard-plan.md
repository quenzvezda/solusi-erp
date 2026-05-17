# Implementation Plan: Vendor Bill Multi-Currency & 2-Step Wizard

> Referensi: [Brainstorming Follow-up](../brainstorming/2026-05-11-vendor-bill-multicurrency-ui-followup.md)  
> Next Flyway migration: **V60**  
> Status: **DRAFT — menunggu approval**

---

## Ringkasan Perubahan

1. **Journal multi-currency foundation** — tambah kolom original currency/rate/amount di `acc_journal_lines`
2. **Vendor Bill exchange rate** — tambah `exchange_rate` di `ap_vendor_bills`
3. **FX variance journal variables** — tambah `VB_FX_LOSS_AMT` dan `VB_FX_GAIN_AMT`
4. **BillableApReference abstraction** — generic read model untuk wizard step 1
5. **Vendor Bill 2-step wizard UI** — replace form lama dengan wizard
6. **Journal Entry detail UI upgrade** — tampilkan original currency (minimal)
7. **i18n & seeder update**

---

## Task 1: Flyway Migration — Journal Multi-Currency + VB Exchange Rate

**File:** `V60__Journal_Multicurrency_And_VB_Exchange_Rate.sql`

### 1a. Tambah kolom multi-currency di `acc_journal_lines`

```sql
ALTER TABLE acc_journal_lines
    ADD COLUMN original_currency_id BIGINT NULL
        AFTER credit_amount,
    ADD COLUMN exchange_rate DECIMAL(19,6) NULL
        AFTER original_currency_id,
    ADD COLUMN original_debit_amount DECIMAL(19,4) NULL DEFAULT NULL
        AFTER exchange_rate,
    ADD COLUMN original_credit_amount DECIMAL(19,4) NULL DEFAULT NULL
        AFTER original_debit_amount,
    ADD CONSTRAINT fk_acc_jl_currency
        FOREIGN KEY (original_currency_id) REFERENCES master_currencies(id);
```

Semantic: `NULL` = transaksi base currency (backward compatible, tidak perlu backfill).

### 1b. Tambah `exchange_rate` di `ap_vendor_bills`

```sql
ALTER TABLE ap_vendor_bills
    ADD COLUMN exchange_rate DECIMAL(19,6) NOT NULL DEFAULT 1.000000
        AFTER currency_id;
```

### 1c. Tambah `VB_FX_LOSS_AMT` dan `VB_FX_GAIN_AMT` ke seeder accounting schema

```sql
-- Seed schema lines untuk VB FX variance (akun COA harus sudah ada)
-- Admin perlu set akun COA yang benar via UI Accounting Schema setelah migration.
-- Migration ini hanya memastikan variable enum dikenali oleh kode Java.
-- Tidak ada INSERT ke acc_schema_lines di sini — konfigurasi via UI.
```

> **Catatan:** Tidak perlu seed `acc_schema_lines` karena pola existing = konfigurasi via UI.
> Yang perlu dilakukan: tambah enum values di Java (Task 3).

### File yang diubah/dibuat:
- `src/main/resources/db/migration/V60__Journal_Multicurrency_And_VB_Exchange_Rate.sql`

---

## Task 2: Journal Domain & Infrastructure — Multi-Currency Support

### 2a. Update `JournalLine` record

**File:** `accounting/journal/domain/model/JournalLine.java`

Tambah field opsional untuk multi-currency:

```java
public record JournalLine(
    Long accountId,
    BigDecimal debitAmount,          // base currency (existing)
    BigDecimal creditAmount,         // base currency (existing)
    Long originalCurrencyId,         // nullable — null = base currency
    BigDecimal exchangeRate,         // nullable
    BigDecimal originalDebitAmount,  // nullable — original currency
    BigDecimal originalCreditAmount  // nullable — original currency
) {
    // Backward-compatible factory methods (existing, untuk GR dll):
    public static JournalLine debit(Long accountId, BigDecimal amount) { ... }
    public static JournalLine credit(Long accountId, BigDecimal amount) { ... }

    // New factory methods untuk multi-currency:
    public static JournalLine debitWithOriginal(Long accountId, BigDecimal baseAmount,
            Long currencyId, BigDecimal rate, BigDecimal originalAmount) { ... }
    public static JournalLine creditWithOriginal(Long accountId, BigDecimal baseAmount,
            Long currencyId, BigDecimal rate, BigDecimal originalAmount) { ... }
}
```

**Penting:** Factory method lama `debit()` dan `credit()` tetap ada dan meneruskan `null` untuk field multi-currency → backward compatible, GR posting tidak berubah.

### 2b. Update `JournalLineEntity`

**File:** `accounting/journal/infrastructure/persistence/JournalLineEntity.java`

Tambah kolom:

```java
@Column(name = "original_currency_id")
private Long originalCurrencyId;

@Column(name = "exchange_rate", precision = 19, scale = 6)
private BigDecimal exchangeRate;

@Column(name = "original_debit_amount", precision = 19, scale = 4)
private BigDecimal originalDebitAmount;

@Column(name = "original_credit_amount", precision = 19, scale = 4)
private BigDecimal originalCreditAmount;
```

### 2c. Update `JournalPersistenceMapper`

**File:** `accounting/journal/infrastructure/persistence/JournalPersistenceMapper.java`

- `toEntity()`: map 4 field baru dari domain → entity
- `toDomain()`: map 4 field baru dari entity → domain record

### 2d. Update `JournalPostingCommand`

**File:** `accounting/journal/application/usecase/command/JournalPostingCommand.java`

Tambah field opsional multi-currency context:

```java
public record JournalPostingCommand(
    SchemaEventType eventType,
    String sourceType,
    Long sourceId,
    String sourceCode,
    LocalDate postingDate,
    String description,
    Map<JournalVariable, BigDecimal> values,
    // --- new (nullable untuk backward compat) ---
    Long originalCurrencyId,
    BigDecimal exchangeRate,
    Map<JournalVariable, BigDecimal> originalValues  // original currency amounts
) {
    // Backward-compatible constructor (untuk GR dll):
    public JournalPostingCommand(SchemaEventType eventType, String sourceType,
            Long sourceId, String sourceCode, LocalDate postingDate,
            String description, Map<JournalVariable, BigDecimal> values) {
        this(eventType, sourceType, sourceId, sourceCode, postingDate,
             description, values, null, null, null);
    }
}
```

### 2e. Update `PostJournalForEventUseCaseImpl`

**File:** `accounting/journal/application/usecase/command/PostJournalForEventUseCaseImpl.java`

Logika perubahan:
- Jika `command.originalCurrencyId() != null` → buat `JournalLine` via `debitWithOriginal()` / `creditWithOriginal()`
- Jika `null` → tetap pakai `debit()` / `credit()` seperti sekarang
- `originalValues` dipakai untuk lookup original amount per variable

```java
// Pseudocode perubahan di dalam .map(schemaLine -> {...}):
BigDecimal baseValue = command.values().getOrDefault(schemaLine.getVar(), BigDecimal.ZERO);
if (baseValue == null || baseValue.signum() == 0) return null;

if (command.originalCurrencyId() != null && command.originalValues() != null) {
    BigDecimal origValue = command.originalValues().getOrDefault(schemaLine.getVar(), baseValue);
    return schemaLine.getPosition() == DEBIT
        ? JournalLine.debitWithOriginal(schemaLine.getAccountId(), baseValue,
              command.originalCurrencyId(), command.exchangeRate(), origValue)
        : JournalLine.creditWithOriginal(schemaLine.getAccountId(), baseValue,
              command.originalCurrencyId(), command.exchangeRate(), origValue);
} else {
    // existing path
    return schemaLine.getPosition() == DEBIT
        ? JournalLine.debit(schemaLine.getAccountId(), baseValue)
        : JournalLine.credit(schemaLine.getAccountId(), baseValue);
}
```

**Balancing tetap berdasarkan `debitAmount`/`creditAmount` (base currency).** Tidak berubah.

### File yang diubah:
- `JournalLine.java` — tambah 4 field + 2 factory method baru
- `JournalLineEntity.java` — tambah 4 kolom
- `JournalPersistenceMapper.java` — map field baru
- `JournalPostingCommand.java` — tambah 3 field opsional + backward-compat constructor
- `PostJournalForEventUseCaseImpl.java` — conditional multi-currency line creation

---

## Task 3: FX Variance Journal Variables

### 3a. Tambah enum values di `JournalVariable`

**File:** `accounting/journal/domain/model/JournalVariable.java`

```java
// Tambah setelah VB_AP_TOTAL:
VB_FX_LOSS_AMT(SchemaEventType.VENDOR_BILL),
VB_FX_GAIN_AMT(SchemaEventType.VENDOR_BILL),
```

### 3b. Tidak perlu migrasi DB

Variable enum hanya hidup di Java. Schema lines di-konfigurasi via UI Accounting Schema setelah deploy — admin set COA untuk `VB_FX_LOSS_AMT` (Debit → akun FX Loss, misal 8210) dan `VB_FX_GAIN_AMT` (Credit → akun FX Gain, misal 7210).

### File yang diubah:
- `JournalVariable.java` — tambah 2 enum values

---

## Task 4: Vendor Bill Domain & Infrastructure — Exchange Rate

### 4a. Update `VendorBill` domain model

**File:** `accountspayable/vendorbill/domain/model/VendorBill.java`

Tambah field `exchangeRate`:

```java
private final BigDecimal exchangeRate;
```

- Update constructor, `createNew()`, dan semua getter
- `createNew()` menerima `exchangeRate` parameter

### 4b. Update `VendorBillEntity`

**File:** `accountspayable/vendorbill/infrastructure/persistence/VendorBillEntity.java`

```java
@Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
private BigDecimal exchangeRate = BigDecimal.ONE;
```

### 4c. Update `VendorBillPersistenceMapper`

**File:** `accountspayable/vendorbill/infrastructure/persistence/VendorBillPersistenceMapper.java`

Map `exchangeRate` di `toEntity()` dan `toDomain()`.

### 4d. Update DTOs

**File-file di `vendorbill/web/dto/`:**

- `VendorBillSaveRequest` — tambah `@NotNull BigDecimal exchangeRate`
- `VendorBillSaveCommand` — tambah `BigDecimal exchangeRate`
- `VendorBillDetailResponse` — tambah `BigDecimal exchangeRate`
- `VendorBillSummaryResponse` — tambah `BigDecimal exchangeRate` (opsional, untuk list view)

### 4e. Update `VendorBillWebMapper`

Map `exchangeRate` di semua mapping method.

### 4f. Update Use Cases

- `CreateVendorBillUseCaseImpl` — terima `exchangeRate` dari command, pass ke `VendorBill.createNew()`
- `UpdateVendorBillUseCaseImpl` — terima `exchangeRate`, rebuild domain object dengan rate baru

### File yang diubah:
- `VendorBill.java`
- `VendorBillEntity.java`
- `VendorBillPersistenceMapper.java`
- `VendorBillSaveRequest.java`
- `VendorBillSaveCommand.java` (record)
- `VendorBillDetailResponse.java`
- `VendorBillWebMapper.java`
- `CreateVendorBillUseCaseImpl.java`
- `UpdateVendorBillUseCaseImpl.java`

---

## Task 5: Update `ConfirmVendorBillUseCase` — FX Variance + Multi-Currency Journal

### 5a. Kalkulasi FX variance

**File:** `accountspayable/vendorbill/application/usecase/command/ConfirmVendorBillUseCaseImpl.java`

Logika baru setelah recalculate line totals:

```java
// 1. Hitung total original (line_total sudah dalam original currency)
BigDecimal originalTotal = ...; // Σ line.lineTotal (original currency)

// 2. Hitung base amount
BigDecimal billRate = confirmedBill.getExchangeRate();
BigDecimal apBaseTotal = originalTotal.multiply(billRate).setScale(4, HALF_UP);

// 3. Hitung GR/IR clearing base amount
//    = Σ per line: line.lineTotal (yang sudah gross proportional dari grIrAmount)
//    GR sudah posting grIrAmount * grRate sebagai base currency
//    Jadi kita perlu lookup grRate per GR untuk hitung base GR/IR
BigDecimal grirBaseTotal = BigDecimal.ZERO;
for (VendorBillLine line : confirmedBill.getLines()) {
    GrLineData grData = billableGrQueryPort.getGrLineData(line.getGrLineId());
    BigDecimal grRate = getGrExchangeRate(line.getGrLineId()); // lookup GR header rate
    BigDecimal lineGrirBase = line.getLineTotal().multiply(grRate).setScale(4, HALF_UP);
    grirBaseTotal = grirBaseTotal.add(lineGrirBase);
}

// 4. FX variance
BigDecimal fxVariance = apBaseTotal.subtract(grirBaseTotal);
BigDecimal fxLoss = fxVariance.compareTo(BigDecimal.ZERO) > 0 ? fxVariance : BigDecimal.ZERO;
BigDecimal fxGain = fxVariance.compareTo(BigDecimal.ZERO) < 0 ? fxVariance.abs() : BigDecimal.ZERO;

// 5. Post journal dengan multi-currency context
Map<JournalVariable, BigDecimal> baseValues = Map.of(
    VB_GRIR_CLEARING_AMT, grirBaseTotal,
    VB_TAX_AMT, BigDecimal.ZERO,
    VB_AP_TOTAL, apBaseTotal,
    VB_FX_LOSS_AMT, fxLoss,
    VB_FX_GAIN_AMT, fxGain
);

Map<JournalVariable, BigDecimal> originalValues = Map.of(
    VB_GRIR_CLEARING_AMT, originalTotal,  // dalam original currency
    VB_TAX_AMT, BigDecimal.ZERO,
    VB_AP_TOTAL, originalTotal,
    VB_FX_LOSS_AMT, fxLoss,  // FX variance selalu dalam base currency
    VB_FX_GAIN_AMT, fxGain
);

postJournalForEventUseCase.execute(new JournalPostingCommand(
    SchemaEventType.VENDOR_BILL,
    "VENDOR_BILL",
    confirmedBill.getId(),
    confirmedBill.getCode(),
    confirmedBill.getBillDate(),
    "Auto journal for vendor bill " + confirmedBill.getCode(),
    baseValues,
    confirmedBill.getCurrencyId(),
    billRate,
    originalValues
));
```

### 5b. Tambah method lookup GR exchange rate

Perlu tambah method di `BillableGrQueryPort`:

```java
BigDecimal getGrExchangeRate(Long grLineId);
```

Implementasi di `BillableGrQueryAdapter`: query `SELECT gr.exchange_rate FROM pur_goods_receipts gr JOIN pur_goods_receipt_lines grl ON grl.header_id = gr.id WHERE grl.id = :grLineId`.

### 5c. Edge case: currency = default currency

Jika `bill.currencyId` = default currency (IDR), maka `exchangeRate = 1`, semua amount original = base, FX variance = 0. Flow tetap sama, hanya semua FX variable = 0 → line di-skip oleh journal engine.

### File yang diubah:
- `ConfirmVendorBillUseCaseImpl.java` — FX variance calc + multi-currency posting
- `BillableGrQueryPort.java` — tambah `getGrExchangeRate(Long grLineId)`
- `BillableGrQueryAdapter.java` — implementasi query

---

## Task 6: BillableApReference Abstraction

### 6a. Buat read model generic

**File baru:** `accountspayable/vendorbill/application/port/BillableApReference.java`

```java
public record BillableApReference(
    String sourceType,        // "GOODS_RECEIPT"
    Long sourceId,
    String sourceCode,
    LocalDate sourceDate,
    Long vendorId,
    String vendorName,
    Long currencyId,
    String currencyCode,
    BigDecimal exchangeRate,
    BigDecimal outstandingAmount,
    int outstandingLineCount,
    String status
) {}
```

### 6b. Buat provider interface

**File baru:** `accountspayable/vendorbill/application/port/BillableApReferenceProvider.java`

```java
public interface BillableApReferenceProvider {
    String getSourceType();
    List<BillableApReference> findBillableReferences(Long vendorId, Long currencyId);
    // vendorId dan currencyId nullable = tampilkan semua
}
```

### 6c. Implementasi GR provider

**File baru:** `accountspayable/vendorbill/infrastructure/adapter/GoodsReceiptBillableReferenceProvider.java`

- Delegate ke `BillableGrQueryAdapter` (reuse existing SQL) + enrich dengan vendor name dan currency code via lookup
- Atau tulis SQL baru yang langsung join ke `parties` dan `master_currencies` untuk mendapatkan nama

### 6d. Buat use case baru: `FindBillableReferencesUseCase`

**File baru:** `accountspayable/vendorbill/application/usecase/query/FindBillableReferencesUseCase.java`

```java
@FunctionalInterface
public interface FindBillableReferencesUseCase {
    List<BillableApReference> execute(Long vendorId, Long currencyId);
}
```

**Impl:** inject `List<BillableApReferenceProvider>`, aggregate results dari semua providers (saat ini hanya GR).

### 6e. Update `VendorBillConfig`

Register bean `GoodsReceiptBillableReferenceProvider` dan `FindBillableReferencesUseCase`.

### File baru:
- `BillableApReference.java` (record)
- `BillableApReferenceProvider.java` (interface)
- `GoodsReceiptBillableReferenceProvider.java` (impl)
- `FindBillableReferencesUseCase.java` (interface)
- `FindBillableReferencesUseCaseImpl.java` (impl)

### File yang diubah:
- `VendorBillConfig.java` — register bean baru

---

## Task 7: Vendor Bill 2-Step Wizard — Controller & Templates

### 7a. Update `VendorBillController` — endpoint baru untuk step 1

```java
// Step 1: Billable references list (halaman baru)
@GetMapping("/select-references")
@PreAuthorize("hasAuthority('VENDOR-BILL_CREATE')")
public String selectReferences(Model model) {
    // Load semua billable references (tanpa filter awal)
    List<BillableApReference> refs = findBillableReferencesUseCase.execute(null, null);
    model.addAttribute("references", refs);
    return "accountspayable/vendor-bills/select-references";
}

// Step 1 → Step 2: POST selected reference IDs, redirect ke create form
@PostMapping("/create-from-references")
@PreAuthorize("hasAuthority('VENDOR-BILL_CREATE')")
public String createFromReferences(
        @RequestParam List<Long> selectedGrIds,
        RedirectAttributes redirectAttributes) {
    // Validate: semua GR harus same vendor + same currency
    // Simpan selected IDs di flash attribute atau session
    redirectAttributes.addFlashAttribute("selectedGrIds", selectedGrIds);
    return "redirect:/accounts-payable/vendor-bills/create";
}
```

### 7b. Update endpoint `GET /create`

Menerima `selectedGrIds` dari flash attribute:

```java
@GetMapping("/create")
@PreAuthorize("hasAuthority('VENDOR-BILL_CREATE')")
public String createForm(@ModelAttribute("selectedGrIds") List<Long> selectedGrIds, Model model) {
    // Load GR data + lines berdasarkan selectedGrIds
    // Pre-populate: vendor (locked), currency (locked), exchangeRate (computed)
    // Lines auto-populate dari GR lines dengan outstandingQty
    // ...
    return "accountspayable/vendor-bills/form";
}
```

### 7c. Template Step 1: `select-references.html`

**File baru:** `templates/accountspayable/vendor-bills/select-references.html`

Layout:
- Page title: "Select Billable References" / "Pilih Referensi Tagihan"
- Table dengan checkbox per row:
  - Source Type (badge: "Goods Receipt")
  - Document Code (link ke detail GR)
  - Date
  - Vendor Name
  - Currency Code
  - Exchange Rate
  - Outstanding Amount
  - Outstanding Lines
  - Status
- Client-side validation: semua selected harus same vendor + same currency
- Tombol "Continue" / "Lanjutkan" → POST ke `/create-from-references`
- Client-side JS: track selections, validate constraints, show warning jika mixed vendor/currency

### 7d. Update Template: `form.html`

**File:** `templates/accountspayable/vendor-bills/form.html`

Perubahan major:
- **Hapus**: input angka `vendorId`, input angka `currencyId`, tombol "Load GRs"
- **Tambah**: Vendor field read-only (display name, hidden input ID)
- **Tambah**: Currency field read-only (display code, hidden input ID)
- **Tambah**: Exchange Rate field — editable, `data-autonumeric` untuk formatting, default dari source
- **Tambah**: Warning banner jika mixed rate dari sources → "Exchange rate berbeda antar referensi, silakan tentukan rate."
- Lines sudah ter-populate dari server (bukan load via AJAX lagi)
- **Hapus/simplify**: GR line selector modal (tidak perlu lagi — lines sudah ditentukan di step 1)

### 7e. Update `form.js`

**File:** `static/js/accountspayable/vendor-bills/form.js`

- Hapus logika "Load GRs" button
- Hapus logika GR line selector modal AJAX
- Tambah logika exchange rate change → recalculate display amounts (opsional, bisa juga server-side saja)
- Pertahankan logika qty_billed edit + line remove

### 7f. Template step 1 JS

**File baru:** `static/js/accountspayable/vendor-bills/select-references.js`

- Track selected checkboxes
- Validate same vendor + same currency constraint di client
- Show/hide warning messages
- Enable/disable "Continue" button

### File baru:
- `templates/accountspayable/vendor-bills/select-references.html`
- `static/js/accountspayable/vendor-bills/select-references.js`

### File yang diubah:
- `VendorBillController.java` — endpoint baru + update create flow
- `templates/accountspayable/vendor-bills/form.html` — redesign
- `static/js/accountspayable/vendor-bills/form.js` — simplify
- `templates/accountspayable/vendor-bills/list.html` — update "Create" button URL ke `/select-references`

---

## Task 8: Journal Entry Detail UI — Multi-Currency Display

### 8a. Update `JournalLineResponse`

**File:** `accounting/journal/web/dto/JournalLineResponse.java`

Tambah:

```java
private String originalCurrencyCode;  // nullable
private BigDecimal exchangeRate;       // nullable
private BigDecimal originalDebitAmount;  // nullable
private BigDecimal originalCreditAmount; // nullable
```

### 8b. Update `JournalEntryDetailResponse`

Tambah flag:

```java
private boolean hasMultiCurrencyLines;  // true jika ada line dengan originalCurrencyId != null
```

### 8c. Update Journal web mapper

Enrich `originalCurrencyCode` via `CurrencyLookupProvider` (sudah ada port-nya).

### 8d. Update `journal-entry-detail.html`

**File:** `templates/accounting/journal/journal-entry-detail.html`

Conditional kolom tambahan di tabel lines:

```html
<!-- Header row: tambah kolom jika hasMultiCurrencyLines -->
<th th:if="${detail.hasMultiCurrencyLines}" th:text="#{label.journal.currency}">Currency</th>
<th th:if="${detail.hasMultiCurrencyLines}" th:text="#{label.journal.rate}">Rate</th>
<th th:if="${detail.hasMultiCurrencyLines}" th:text="#{label.journal.originalDebit}">Orig. Debit</th>
<th th:if="${detail.hasMultiCurrencyLines}" th:text="#{label.journal.originalCredit}">Orig. Credit</th>

<!-- Data row -->
<td th:if="${detail.hasMultiCurrencyLines}"
    th:text="${line.originalCurrencyCode != null ? line.originalCurrencyCode : '—'}">—</td>
<!-- ... dst -->
```

### 8e. Tambah link VENDOR_BILL di journal detail

Update conditional link di `sourceCode` display agar handle `VENDOR_BILL`:

```html
<a th:if="${detail.sourceType == 'VENDOR_BILL'}"
   th:href="@{'/accounts-payable/vendor-bills/' + ${detail.sourceId}}"
   th:text="${detail.sourceCode}">VB-...</a>
```

### File yang diubah:
- `JournalLineResponse.java` — tambah 4 field
- `JournalEntryDetailResponse.java` — tambah flag
- Journal web mapper — enrich currency code
- `journal-entry-detail.html` — conditional columns + VB link

---

## Task 9: i18n Message Keys

### 9a. Keys baru yang perlu ditambahkan

**`messages.properties` (EN):**

```properties
# Step 1 wizard
label.vb.selectReferences=Select Billable References
label.vb.selectReferences.subtitle=Choose goods receipts to create a vendor bill.
label.vb.sourceType=Source Type
label.vb.sourceCode=Document Code
label.vb.sourceDate=Date
label.vb.vendor=Vendor
label.vb.currency=Currency
label.vb.exchangeRate=Exchange Rate
label.vb.outstandingAmount=Outstanding Amount
label.vb.outstandingLines=Outstanding Lines
label.vb.continue=Continue
label.vb.mixedRate.warning=Selected references have different exchange rates. Please enter the bill exchange rate manually.
label.vb.mixedVendor.error=All selected references must have the same vendor.
label.vb.mixedCurrency.error=All selected references must have the same currency.
label.vb.noSelection.error=Please select at least one reference.

# Exchange rate
label.vb.exchangeRate.inherited=Rate inherited from source reference.
label.vb.exchangeRate.manual=Please enter exchange rate manually.

# Journal multi-currency
label.journal.currency=Currency
label.journal.rate=Rate
label.journal.originalDebit=Original Debit
label.journal.originalCredit=Original Credit
```

**`messages_id.properties` (ID):** terjemahan Indonesia untuk semua key di atas.

### File yang diubah:
- `messages.properties`
- `messages_id.properties`

---

## Task 10: Update Existing BillableGrQueryPort & Adapter

### 10a. Tambah exchange rate ke `BillableGrView`

**File:** `vendorbill/domain/port/BillableGrQueryPort.java`

```java
// Update record:
record BillableGrView(
    Long grId, String grCode, Long poId, String poCode,
    Long vendorId, Long currencyId,
    BigDecimal exchangeRate  // BARU
) {}
```

### 10b. Update SQL di `BillableGrQueryAdapter`

**File:** `vendorbill/infrastructure/adapter/BillableGrQueryAdapter.java`

- `SQL_FIND_BILLABLE_GRS`: tambah `gr.exchange_rate` ke SELECT
- `findBillableGrs()`: map `exchangeRate` ke `BillableGrView`
- Tambah method `getGrExchangeRate(Long grLineId)`:

```sql
SELECT gr.exchange_rate
FROM pur_goods_receipts gr
JOIN pur_goods_receipt_lines grl ON grl.header_id = gr.id
WHERE grl.id = :grLineId
```

### File yang diubah:
- `BillableGrQueryPort.java` — update record + tambah method
- `BillableGrQueryAdapter.java` — update SQL + tambah method

---

## Urutan Implementasi (Dependency Order)

```
Task 1 (Migration V60)
  ↓
Task 2 (Journal domain multi-currency)
  ↓
Task 3 (FX variance enum)      ← bisa paralel dengan Task 4
  ↓
Task 4 (VB domain exchange rate) ← bisa paralel dengan Task 3
  ↓
Task 10 (Update BillableGrQuery) ← depends on Task 4
  ↓
Task 5 (Confirm use case FX)    ← depends on Task 2, 3, 4, 10
  ↓
Task 6 (BillableApReference)    ← depends on Task 10
  ↓
Task 7 (Wizard UI)              ← depends on Task 4, 6
  ↓
Task 8 (Journal detail UI)      ← depends on Task 2
  ↓
Task 9 (i18n)                   ← bisa paralel, tapi sebaiknya setelah Task 7
```

Ringkasan: **Task 1 → 2 → (3 || 4) → 10 → 5 → 6 → 7 → 8 → 9**

---

## Checklist Verifikasi Akhir

- [ ] GR posting (`CompleteGoodsReceiptUseCaseImpl`) **tidak berubah** — tetap pakai constructor lama `JournalPostingCommand` tanpa multi-currency
- [ ] Journal balancing tetap berdasarkan base currency (`debitAmount`/`creditAmount`)
- [ ] VB confirm dengan currency = IDR (default) → FX variance = 0 → `VB_FX_LOSS_AMT` dan `VB_FX_GAIN_AMT` di-skip
- [ ] VB confirm dengan currency = USD, rate berbeda dari GR → FX variance dipost ke journal
- [ ] Journal detail menampilkan kolom multi-currency hanya jika ada line dengan `originalCurrencyId != null`
- [ ] Wizard step 1 → step 2 mempertahankan selected GR IDs
- [ ] Exchange rate inheritance rule (5 rules) diterapkan di step 2 form
- [ ] Remainder method masih berjalan di `ConfirmVendorBillUseCaseImpl` untuk rounding
- [ ] List page "Create" button mengarah ke `/select-references` bukan `/create`
- [ ] Backward compatible: existing data VB tanpa `exchange_rate` → default 1.0
