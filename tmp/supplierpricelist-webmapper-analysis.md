# SupplierPriceList Web Mapper - Architecture Analysis

## Context & Problem Statement

Di SPL feature slice, `SupplierPriceListWebMapper` langsung inject JPA repositories dari bounded context lain
(Party, Product, Uom, Currency) untuk resolve display name pada form pre-edit. Ini bermasalah karena:

1. **DDD violation**: Web layer langsung depend ke infrastructure/persistence layer bounded context lain
2. **Data kurang**: Product hanya kirim `name`, tidak kirim `code` sebagai subText di autocomplete
3. **Tidak konsisten**: Resolusi nama dilakukan berbeda di tiap WebMapper, tidak ada single source of truth
4. **Duplikasi logic**: Cara resolve supplier name (salutation + name) ditulis ulang di WebMapper, padahal
   sudah ada logic sama di `PartyLookupProviderImpl`

## Current State

### SupplierPriceListWebMapper.java (MASALAH)

```java
@Autowired
protected PartyJpaRepository partyRepository;       // <-- langsung inject infra layer!
@Autowired
protected JpaProductRepository productRepository;   // <-- langsung inject infra layer!
@Autowired
protected UomJpaRepository uomRepository;           // <-- langsung inject infra layer!
@Autowired
protected CurrencyJpaRepository currencyRepository; // <-- langsung inject infra layer!

@Named("getProductName")
protected String getProductName(Long id) {
    return productRepository.findById(id).map(p -> p.getName()).orElse(null);
    // Hanya getName() -- TIDAK ADA code!
}
```

### SupplierPriceListSaveRequest.java

Fields untuk display di form:
```java
private String supplierName;   // hanya name
private String productName;    // hanya name, TIDAK ada productCode
private String uomName;        // hanya name
private String currencyName;   // hanya name
```

### form.html

Autocomplete Product hanya pass initialText (name), TIDAK ada initialSubtext (code):
```html
<div th:replace="~{fragments/inputs :: autocomplete(field='productId', ...,
    initialValue=${splRequest.productId},
    initialText=${splRequest.productName})}">
</div>
```

### Autocomplete Fragment (inputs.html)

SUDAH SUPPORT `initialSubtext` dan `initialPayload`:
```html
<option th:if="${initialValue != null}"
        th:value="${initialValue}"
        th:text="${initialText}"
        th:attr="data-subtext=${initialSubtext}"     <!-- SUDAH ADA! -->
        th:data-payload-type="${initialPayload}"
        selected></option>
```

### TomSelect initLookup (erp-common-handler.js)

SUDAH SUPPORT subText rendering:
```javascript
const subText = initialOption.getAttribute('data-subtext');
if (subText) this.options[val].subText = subText;
// ... renders as <small> below the name
```

### Existing LookupProvider Pattern (SUDAH ADA DI CODEBASE)

**Port interface** (domain layer - bersih dari infra):
```java
public interface ProductLookupProvider {
    LookupDto resolve(Long productId);
}
```

**Infrastructure adapter**:
```java
public class ProductLookupProviderImpl implements ProductLookupProvider {
    @Override
    public LookupDto resolve(Long productId) {
        return jpaProductRepository.findById(productId)
                .map(e -> new LookupDto(e.getId(), e.getName(), e.getCode()))
                .orElse(null);
    }
}
```

**LookupDto contract** (core):
```java
public record LookupDto(
    Long id,           // ID entity
    String name,       // primary label (nama product, nama supplier, dll)
    String subText,    // secondary info (code, type, symbol, dll)
    Map<String, Object> payload  // extra metadata (isSerialized, uomId, dll)
) { ... }
```

### Contoh Penggunaan LookupProvider di WebMapper Lain (SUDAH JALAN)

**SchemaWebMapper.java** (accounting/schema):
```java
@Autowired
protected CoaLookupProvider coaLookupProvider;  // inject port, bukan JPA!

@AfterMapping
protected void resolveAccountNames(AccountingSchema domain, @MappingTarget SchemaSummaryResponse target) {
    LookupDto debit = coaLookupProvider.resolve(domain.getDebitAccountId());
    target.setDebitAccountName(debit != null ? debit.subText() + " - " + debit.name() : null);
}
```

**ApprovalWebMapper.java** (common/approval):
```java
private final PartyLookupProvider partyLookupProvider;  // inject port!

private String resolveActorName(Long actorId) {
    LookupDto lookup = partyLookupProvider.resolve(actorId);
    return lookup != null ? lookup.name() : null;
}
```

## Existing LookupProvider Ports Status

| Entity    | LookupProvider Port | LookupProviderImpl | LookupController |
|-----------|--------------------|--------------------|-----------------|
| Product   | YA                 | YA                 | YA              |
| Party     | YA                 | YA                 | YA              |
| COA       | YA                 | YA                 | ?               |
| Facility  | YA                 | YA                 | YA              |
| Grid      | YA                 | YA                 | YA              |
| Brand     | YA                 | YA                 | YA              |
| Category  | YA                 | YA                 | YA              |
| Role      | YA                 | YA                 | YA              |
| Uom       | **BELUM**          | **BELUM**          | YA (via UseCase)|
| Currency  | **BELUM**          | **BELUM**          | YA (via JPA)    |
| Geographic| YA                 | YA                 | YA              |

## Opsi Perbaikan

---

### Opsi A: Inject LookupProvider Ports di WebMapper (RECOMMENDED)

Replace JPA repository injections dengan LookupProvider ports. Ini sudah pola yang established
di codebase (SchemaWebMapper, ApprovalWebMapper).

**Kelebihan:**
- Konsisten dengan existing pattern di codebase
- Domain port = clean DDD boundary
- LookupDto sudah include `subText` (code) -- solve masalah data kurang
- Consumer tidak perlu tahu bagaimana entity di-query
- Single source of truth: format display diatur di LookupProviderImpl

**Kekurangan:**
- Perlu buat UomLookupProvider + CurrencyLookupProvider port (belum ada)
- Tapi ini investasi yang berguna untuk semua future consumers

**Implementasi:**

1. Buat `UomLookupProvider` port + impl (mirip ProductLookupProvider)
2. Buat `CurrencyLookupProvider` port + impl (mirip ProductLookupProvider)
3. Register di BeanConfig masing-masing bounded context
4. Refactor SupplierPriceListWebMapper:

```java
@Autowired
protected PartyLookupProvider partyLookupProvider;
@Autowired
protected ProductLookupProvider productLookupProvider;
@Autowired
protected UomLookupProvider uomLookupProvider;
@Autowired
protected CurrencyLookupProvider currencyLookupProvider;
```

5. Update DTO untuk include subText fields:
```java
// SupplierPriceListSaveRequest.java
private String supplierName;
private String supplierSubText;   // BARU: code + type
private String productName;
private String productSubText;    // BARU: product code
private String uomName;
private String uomSubText;        // BARU: uom code
private String currencyName;
private String currencySubText;   // BARU: symbol + alias
```

6. Update form.html untuk pass initialSubtext:
```html
<div th:replace="~{fragments/inputs :: autocomplete(field='productId', ...,
    initialValue=${splRequest.productId},
    initialText=${splRequest.productName},
    initialSubtext=${splRequest.productSubText})}">
</div>
```

---

### Opsi B: DI via BeanConfig (Manual Wiring)

Inject LookupProvider ke WebMapper via BeanConfig per slice, bukan @Autowired.

**Kelebihan:**
- Explicit dependency declaration
- Tidak rely pada component scanning

**Kekurangan:**
- Tidak konsisten dengan existing pattern (SchemaWebMapper, ApprovalWebMapper pakai @Autowired)
- Lebih verbose, tidak ada benefit signifikan dibanding @Autowired
- MapStruct + componentModel="spring" sudah handle injection otomatis

**Catatan:** Karena WebMapper adalah MapStruct abstract class dengan `componentModel = "spring"`,
MapStruct auto-generate implementation yang pakai @Autowired. Manual BeanConfig untuk WebMapper
tidak umum di codebase ini.

---

### Opsi C: Centralized LookupResolver Helper

Buat helper class `LookupResolver` yang inject semua LookupProvider dan punya method standard:
```java
@Component
public class LookupResolver {
    public String resolveName(Long id, SomeLookupProvider provider) { ... }
    public String resolveSubText(Long id, SomeLookupProvider provider) { ... }
    public LookupDto resolve(Long id, SomeLookupProvider provider) { ... }
}
```

**Kelebihan:**
- DRY: satu tempat untuk resolve logic

**Kekurangan:**
- Over-abstraction untuk kasus yang sederhana
- LookupProvider.sendiri sudah cukup simple (1 method)
- Tidak ada di existing codebase, jadi tidak konsisten

---

## Rekomendasi

**Opsi A** adalah yang paling cocok karena:

1. **Sudah established pattern** di codebase (SchemaWebMapper, ApprovalWebMapper)
2. **Menyelesaikan root cause**: WebMapper tidak depend ke infra layer lain
3. **Solve masalah data kurang**: LookupDto.subText sudah include code
4. **Investasi berguna**: UomLookupProvider dan CurrencyLookupProvider bisa dipakai semua consumers
5. **Minimal perubahan**: autocomplete fragment DAN JS handler SUDAH support subText

## Implementation Checklist (Opsi A)

### Phase 1: Buat Missing LookupProvider Ports

- [ ] `UomLookupProvider` (domain port) di inventory/uom/domain/port/
- [ ] `UomLookupProviderImpl` (infra adapter) di inventory/uom/infrastructure/adapter/
- [ ] Register di Uom BeanConfig
- [ ] `CurrencyLookupProvider` (domain port) di master/currency/domain/port/
- [ ] `CurrencyLookupProviderImpl` (infra adapter) di master/currency/infrastructure/adapter/
- [ ] Register di Currency BeanConfig

### Phase 2: Refactor SupplierPriceListWebMapper

- [ ] Replace `PartyJpaRepository` -> `PartyLookupProvider`
- [ ] Replace `JpaProductRepository` -> `ProductLookupProvider`
- [ ] Replace `UomJpaRepository` -> `UomLookupProvider`
- [ ] Replace `CurrencyJpaRepository` -> `CurrencyLookupProvider`
- [ ] Refactor mapping methods to use LookupDto

### Phase 3: Update DTOs

- [ ] Add `productSubText`, `supplierSubText`, `uomSubText`, `currencySubText` to `SupplierPriceListSaveRequest`
- [ ] Add same to `SupplierPriceListDetailResponse` (optional, for consistency)
- [ ] Add same to `SupplierPriceListSummaryResponse` (optional)

### Phase 4: Update Templates

- [ ] Pass `initialSubtext` in all autocomplete fields in form.html
- [ ] Verify subText renders correctly in TomSelect on edit

### Phase 5: Verify

- [ ] Create new SPL -> form loads correctly
- [ ] Edit existing SPL -> autocomplete shows name + code (subText)
- [ ] Autocomplete search still works
- [ ] Cascading UOM auto-selection still works (product -> UOM)
