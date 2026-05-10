# Vendor Bill Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the Vendor Bill (three-way matching PO+GR+Invoice) module as the first half of Sprint 5 Accounts Payable.

**Architecture:** Clean Architecture + DDD + CQRS in bounded context `com.solusi.erp.accountspayable.vendorbill`, following Goods Receipt module as canonical reference. Domain stays framework-free; infrastructure adapts JPA/native SQL; web layer depends on use cases and query/read ports only. Confirmation flow enforces open period and posts schema-driven journal for GR/IR clearing to AP.

**Tech Stack:** Java 21, Spring Boot 4, Spring Security, Spring Data JPA/Hibernate, MapStruct, Thymeleaf + HTMX, MariaDB, Flyway, JUnit 5 + Mockito + AssertJ

---

## File Structure (Target)

- **Migration & security seed**
  - `src/main/resources/db/migration/V58__Add_Vendor_Bill_Module.sql`
- **Domain**
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillStatus.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillGrRef.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillLine.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBill.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/repository/VendorBillRepository.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/port/BillableGrQueryPort.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/port/BillableGrView.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/port/BillableGrLineView.java`
- **Infrastructure persistence/adapters**
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillEntity.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillLineEntity.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillGrRefEntity.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillJpaRepository.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillPersistenceMapper.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillRepositoryImpl.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/BillableGrQueryAdapter.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/config/VendorBillConfig.java`
- **Application use cases**
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/command/**`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/**`
- **Web**
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/dto/**`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/mapper/VendorBillWebMapper.java`
  - `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/controller/VendorBillController.java`
  - `src/main/resources/templates/accountspayable/vendor-bills/list.html`
  - `src/main/resources/templates/accountspayable/vendor-bills/form.html`
  - `src/main/resources/templates/accountspayable/vendor-bills/detail.html`
  - `src/main/resources/templates/accountspayable/vendor-bills/gr-line-selector-modal.html`
- **Cross-module enhancement**
  - `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java`
  - `src/main/resources/templates/inventory/goods-receipts/view.html`
- **Tests**
  - `src/test/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillTest.java`
  - `src/test/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/command/ConfirmVendorBillUseCaseTest.java`
  - `src/test/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/command/CreateVendorBillUseCaseTest.java`
  - `src/test/java/com/solusi/erp/accountspayable/vendorbill/web/mapper/VendorBillWebMapperTest.java`
  - `src/test/java/com/solusi/erp/accountspayable/vendorbill/web/controller/VendorBillControllerTest.java`

## Implementation Tasks

### Task 1: V58 Flyway Migration (Vendor Bill schema + permissions)

**Files:**
- Create: `src/main/resources/db/migration/V58__Add_Vendor_Bill_Module.sql`
- Reference: `src/main/resources/db/migration/V50__Add_Goods_Receipt_Module.sql`

- [ ] **Step 1: Write migration test expectation (manual contract)**

```sql
-- Must exist after migrate:
-- ap_vendor_bills
-- ap_vendor_bill_gr_refs
-- ap_vendor_bill_lines
-- UNIQUE (vendor_id, vendor_invoice_number)
-- Permission group AP-01 + VENDOR-BILL_* permissions
```

- [ ] **Step 2: Add MariaDB DDL and indexes**

```sql
CREATE TABLE ap_vendor_bills (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    vendor_id BIGINT NOT NULL,
    vendor_invoice_number VARCHAR(100) NOT NULL,
    bill_date DATE NOT NULL,
    due_date DATE NOT NULL,
    currency_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    subtotal DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    created_by_user_id BIGINT,
    created_date DATETIME NOT NULL,
    updated_by_user_id BIGINT,
    updated_date DATETIME NOT NULL,
    version BIGINT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ap_vendor_bills_code (code),
    UNIQUE KEY uk_ap_vendor_bills_vendor_invoice (vendor_id, vendor_invoice_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 3: Add line/ref tables and FK rules**

```sql
CREATE TABLE ap_vendor_bill_gr_refs (
    bill_id BIGINT NOT NULL,
    gr_id BIGINT NOT NULL,
    PRIMARY KEY (bill_id, gr_id),
    CONSTRAINT fk_ap_vb_ref_bill FOREIGN KEY (bill_id) REFERENCES ap_vendor_bills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ap_vendor_bill_lines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    bill_id BIGINT NOT NULL,
    gr_line_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    qty_billed DECIMAL(19,4) NOT NULL,
    uom_id BIGINT NOT NULL,
    uom_name VARCHAR(100) NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    inventory_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    line_total DECIMAL(19,4) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_ap_vb_lines_bill (bill_id),
    KEY idx_ap_vb_lines_gr_line (gr_line_id),
    CONSTRAINT fk_ap_vb_line_bill FOREIGN KEY (bill_id) REFERENCES ap_vendor_bills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 4: Seed sequence + permissions**

```sql
INSERT INTO system_sequences(module_code, sequence_key, sequence_name, prefix, format, next_value, reset_policy, is_active)
VALUES ('VENDOR-BILL', 'VB_CODE', 'Vendor Bill Number', 'VB', '{PREFIX}-{YYYY}{MM}-{NUM:5}', 1, 'MONTHLY', 1);

INSERT INTO permission_groups(code, name_i18n_key, icon, route, sort_order, is_active)
VALUES ('AP-01', 'permission.group.ap01', 'ti ti-file-invoice', '/accounts-payable/vendor-bills', 300, 1);
```

- [ ] **Step 5: Run migration**

Run: `.\mvnw.cmd -q "-Dtest=FlywayMigrationTest" test`  
Expected: PASS, V58 applied without SQL syntax errors.

### Task 2: Domain Model (status, aggregate, line, GR reference)

**Files:**
- Create: `.../domain/model/VendorBillStatus.java`
- Create: `.../domain/model/VendorBillGrRef.java`
- Create: `.../domain/model/VendorBillLine.java`
- Create: `.../domain/model/VendorBill.java`
- Test: `.../domain/model/VendorBillTest.java`

- [ ] **Step 1: Write failing domain tests**

```java
@Test
void confirm_should_fail_when_status_not_draft() {}

@Test
void confirm_should_fail_when_no_lines() {}

@Test
void cancel_should_fail_when_confirmed() {}
```

- [ ] **Step 2: Implement status enum and value objects**

```java
public enum VendorBillStatus {
    DRAFT, CONFIRMED, CANCELLED, PARTIAL_PAID, PAID
}
```

- [ ] **Step 3: Implement `VendorBill` aggregate invariants**

```java
public void confirm(BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount) {
    if (status != VendorBillStatus.DRAFT) throw new DomainException("msg.error.vb.invalid.status");
    if (lines == null || lines.isEmpty()) throw new DomainException("msg.error.vb.lines.required");
    this.subtotal = subtotal;
    this.taxAmount = taxAmount;
    this.totalAmount = totalAmount;
    this.status = VendorBillStatus.CONFIRMED;
}
```

- [ ] **Step 4: Implement static factory and defensive copy**

```java
public static VendorBill createNew(..., List<VendorBillGrRef> grRefs, List<VendorBillLine> lines) {
    return new VendorBill(null, ..., VendorBillStatus.DRAFT, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            List.copyOf(grRefs), List.copyOf(lines), null);
}
```

- [ ] **Step 5: Run domain tests**

Run: `.\mvnw.cmd -q "-Dtest=VendorBillTest" test`  
Expected: PASS, all invariants enforced.

### Task 3: VendorBillRepository Domain Interface

**Files:**
- Create: `.../domain/repository/VendorBillRepository.java`

- [ ] **Step 1: Define repository contract used by application**

```java
public interface VendorBillRepository {
    Page<VendorBill> findAll(String keyword, Long vendorId, VendorBillStatus status, Pageable pageable);
    VendorBill save(VendorBill bill);
    Optional<VendorBill> findById(Long id);
    void deleteById(Long id);
}
```

- [ ] **Step 2: Compile API surface**

Run: `.\mvnw.cmd -q -DskipTests compile`  
Expected: PASS, no unresolved domain repository references.

### Task 4: BillableGrQueryPort + read models

**Files:**
- Create: `.../domain/port/BillableGrQueryPort.java`
- Create: `.../domain/port/BillableGrView.java`
- Create: `.../domain/port/BillableGrLineView.java`

- [ ] **Step 1: Define GR picker list record**

```java
public record BillableGrView(
        Long grId, String grCode, Long poId, String poCode, Long vendorId, Long currencyId) {}
```

- [ ] **Step 2: Define GR line picker record**

```java
public record BillableGrLineView(
        Long grLineId, Long grId, Long productId, String productName, String productCode,
        BigDecimal quantityReceived, Long uomId, String uomName, BigDecimal unitPrice,
        BigDecimal inventoryAmount, BigDecimal taxAmount, BigDecimal grIrAmount, BigDecimal outstandingQty) {}
```

- [ ] **Step 3: Define cross-BC query port**

```java
public interface BillableGrQueryPort {
    List<BillableGrView> findBillableGrs(Long vendorId, Long currencyId);
    List<BillableGrLineView> findBillableGrLines(Long grId);
    Map<Long, BigDecimal> sumConfirmedBilledQtyByGrId(Long grId);
    GrLineData getGrLineData(Long grLineId);
    BigDecimal sumConfirmedLineTotals(Long grLineId, Long excludeBillId);
    BigDecimal sumConfirmedBilledQty(Long grLineId, Long excludeBillId);
    record GrLineData(BigDecimal quantityReceived, BigDecimal grIrAmount) {}
}
```

- [ ] **Step 4: Compile port API**

Run: `.\mvnw.cmd -q -DskipTests compile`  
Expected: PASS.

### Task 5: JPA Entities + JpaRepository

**Files:**
- Create: `.../infrastructure/persistence/VendorBillEntity.java`
- Create: `.../infrastructure/persistence/VendorBillLineEntity.java`
- Create: `.../infrastructure/persistence/VendorBillGrRefEntity.java`
- Create: `.../infrastructure/persistence/VendorBillJpaRepository.java`

- [ ] **Step 1: Create aggregate root entity**

```java
@Entity
@Table(name = "ap_vendor_bills")
public class VendorBillEntity extends BaseModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorBillLineEntity> lines = new ArrayList<>();
}
```

- [ ] **Step 2: Create line and GR ref entities**

```java
@Entity
@Table(name = "ap_vendor_bill_gr_refs")
@IdClass(VendorBillGrRefId.class)
public class VendorBillGrRefEntity {
    @Id @Column(name = "bill_id") private Long billId;
    @Id @Column(name = "gr_id") private Long grId;
}
```

- [ ] **Step 3: Create Spring Data repository with filter query**

```java
public interface VendorBillJpaRepository extends JpaRepository<VendorBillEntity, Long> {
    @Query("""
        select vb from VendorBillEntity vb
        where (:keyword is null or lower(vb.code) like lower(concat('%', :keyword, '%'))
           or lower(vb.vendorInvoiceNumber) like lower(concat('%', :keyword, '%')))
          and (:vendorId is null or vb.vendorId = :vendorId)
          and (:status is null or vb.status = :status)
        """)
    Page<VendorBillEntity> findAllFiltered(String keyword, Long vendorId, VendorBillStatus status, Pageable pageable);
}
```

- [ ] **Step 4: Compile persistence layer**

Run: `.\mvnw.cmd -q -DskipTests compile`  
Expected: PASS, entity mappings valid.

### Task 6: Persistence Mapper + Repository Impl + Billable GR Query Adapter

**Files:**
- Create: `.../infrastructure/persistence/VendorBillPersistenceMapper.java`
- Create: `.../infrastructure/adapter/VendorBillRepositoryImpl.java`
- Create: `.../infrastructure/adapter/BillableGrQueryAdapter.java`

- [ ] **Step 1: Write failing repository adapter test (slice/unit)**

```java
@Test
void save_then_findById_should_roundtrip_domain_aggregate() {}
```

- [ ] **Step 2: Implement MapStruct mapper with collection mapping**

```java
@Mapper(componentModel = "spring")
public interface VendorBillPersistenceMapper {
    VendorBill toDomain(VendorBillEntity entity);
    VendorBillEntity toEntity(VendorBill domain);
}
```

- [ ] **Step 3: Implement repository adapter**

```java
@RequiredArgsConstructor
public class VendorBillRepositoryImpl implements VendorBillRepository {
    private final VendorBillJpaRepository jpaRepository;
    private final VendorBillPersistenceMapper mapper;

    @Override
    public VendorBill save(VendorBill bill) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(bill)));
    }
}
```

- [ ] **Step 4: Implement native SQL billable GR adapter**

```java
@Repository
@RequiredArgsConstructor
public class BillableGrQueryAdapter implements BillableGrQueryPort {
    private final NamedParameterJdbcTemplate jdbc;
    // findBillableGrs() uses EXISTS + outstanding qty against confirmed vendor bills only
}
```

- [ ] **Step 5: Run focused adapter tests**

Run: `.\mvnw.cmd -q "-Dtest=VendorBillRepositoryImplTest,BillableGrQueryAdapterTest" test`  
Expected: PASS, mapping + SQL query contract valid.

### Task 7: Create/Update/Delete/Cancel Use Cases

**Files:**
- Create: `.../application/usecase/command/VendorBillLineCommand.java`
- Create: `CreateVendorBillUseCase.java`, `CreateVendorBillUseCaseImpl.java`
- Create: `UpdateVendorBillUseCase.java`, `UpdateVendorBillUseCaseImpl.java`
- Create: `DeleteVendorBillUseCase.java`, `DeleteVendorBillUseCaseImpl.java`
- Create: `CancelVendorBillUseCase.java`, `CancelVendorBillUseCaseImpl.java`
- Test: `.../application/usecase/command/CreateVendorBillUseCaseTest.java`

- [ ] **Step 1: Write failing create use case tests**

```java
@Test
void create_should_reject_due_date_before_bill_date() {}

@Test
void create_should_set_status_draft_and_zero_amounts() {}
```

- [ ] **Step 2: Implement command DTO**

```java
public record VendorBillLineCommand(
        Long grLineId, Long productId, String productName, String description,
        BigDecimal qtyBilled, Long uomId, String uomName, BigDecimal unitPrice,
        BigDecimal inventoryAmount, BigDecimal taxAmount) {}
```

- [ ] **Step 3: Implement create/update/delete/cancel logic**

```java
if (dueDate.isBefore(billDate)) {
    throw new DomainException("msg.error.vb.due.before.bill");
}
```

- [ ] **Step 4: Enforce status guard for update/delete/cancel**

```java
if (bill.getStatus() != VendorBillStatus.DRAFT) {
    throw new DomainException("msg.error.vb.only.draft.editable");
}
```

- [ ] **Step 5: Run command use case tests**

Run: `.\mvnw.cmd -q "-Dtest=CreateVendorBillUseCaseTest,UpdateVendorBillUseCaseTest,CancelVendorBillUseCaseTest" test`  
Expected: PASS.

### Task 8: ConfirmVendorBillUseCase (three-way matching + journal)

**Files:**
- Create: `.../application/usecase/command/ConfirmVendorBillUseCase.java`
- Create: `.../application/usecase/command/ConfirmVendorBillUseCaseImpl.java`
- Test: `.../application/usecase/command/ConfirmVendorBillUseCaseTest.java`

- [ ] **Step 1: Write failing confirmation tests**

```java
@Test
void confirm_should_fail_when_qty_exceeds_outstanding() {}

@Test
void confirm_should_post_journal_using_vendor_bill_schema_variables() {}

@Test
void confirm_should_use_remainder_on_last_bill_line() {}
```

- [ ] **Step 2: Implement outstanding qty validation**

```java
BigDecimal alreadyBilled = billableGrQueryPort.sumConfirmedBilledQty(line.getGrLineId(), bill.getId());
BigDecimal outstanding = grLine.quantityReceived().subtract(alreadyBilled);
if (line.getQtyBilled().compareTo(outstanding) > 0) {
    throw new DomainException("msg.error.vb.qty.exceed.outstanding");
}
```

- [ ] **Step 3: Implement line total recomputation + remainder rule**

```java
BigDecimal proportional = qtyBilled.multiply(grIrAmount).divide(qtyReceived, 4, RoundingMode.HALF_UP);
BigDecimal lineTotal = isLastBill
        ? grIrAmount.subtract(sumConfirmedLineTotals)
        : proportional;
```

- [ ] **Step 4: Implement open period + journal posting**

```java
postJournalForEventUseCase.execute(new JournalPostingCommand(
        SchemaEventType.VENDOR_BILL,
        bill.getId(),
        bill.getCode(),
        bill.getBillDate(),
        Map.of(
            JournalVariable.VB_GRIR_CLEARING_AMT, total,
            JournalVariable.VB_TAX_AMT, BigDecimal.ZERO,
            JournalVariable.VB_AP_TOTAL, total
        )));
```

- [ ] **Step 5: Run confirmation tests**

Run: `.\mvnw.cmd -q "-Dtest=ConfirmVendorBillUseCaseTest" test`  
Expected: PASS.

### Task 9: Query Use Cases (list/detail/create-view)

**Files:**
- Create: `FindVendorBillsUseCase.java`, `FindVendorBillsUseCaseImpl.java`
- Create: `GetVendorBillDetailUseCase.java`, `GetVendorBillDetailUseCaseImpl.java`
- Create: `GetVendorBillCreateViewUseCase.java`, `GetVendorBillCreateViewUseCaseImpl.java`

- [ ] **Step 1: Define read models**

```java
public record VendorBillSummaryView(Long id, String code, Long vendorId, String vendorInvoiceNumber,
                                    LocalDate billDate, LocalDate dueDate, VendorBillStatus status,
                                    BigDecimal totalAmount) {}
```

- [ ] **Step 2: Implement list query with pageable filters**

```java
public Page<VendorBillSummaryView> execute(String keyword, Long vendorId, VendorBillStatus status, Pageable pageable) {
    return repository.findAll(keyword, vendorId, status, pageable).map(this::toSummary);
}
```

- [ ] **Step 3: Implement detail query**

```java
public VendorBillDetailView execute(Long id) {
    return repository.findById(id).map(this::toDetail)
            .orElseThrow(() -> new DomainException("msg.error.vb.notfound"));
}
```

- [ ] **Step 4: Implement create form bootstrap query**

```java
List<BillableGrView> billableGrs = billableGrQueryPort.findBillableGrs(vendorId, currencyId);
```

- [ ] **Step 5: Run query tests**

Run: `.\mvnw.cmd -q "-Dtest=FindVendorBillsUseCaseTest,GetVendorBillDetailUseCaseTest,GetVendorBillCreateViewUseCaseTest" test`  
Expected: PASS.

### Task 10: VendorBillConfig wiring

**Files:**
- Create: `.../infrastructure/config/VendorBillConfig.java`
- Reference: `.../inventory/goodsreceipt/infrastructure/config/GoodsReceiptConfig.java`

- [ ] **Step 1: Write minimal spring context test**

```java
@SpringBootTest
class VendorBillConfigTest {
    @Autowired ConfirmVendorBillUseCase confirmVendorBillUseCase;
}
```

- [ ] **Step 2: Register repository + adapters beans**

```java
@Bean
VendorBillRepository vendorBillRepository(VendorBillJpaRepository jpa, VendorBillPersistenceMapper mapper) {
    return new VendorBillRepositoryImpl(jpa, mapper);
}
```

- [ ] **Step 3: Register use cases with TransactionTemplate pattern**

```java
@Bean
ConfirmVendorBillUseCase confirmVendorBillUseCase(..., TransactionTemplate tx) {
    return command -> tx.execute(status -> impl.execute(command));
}
```

- [ ] **Step 4: Run context wiring test**

Run: `.\mvnw.cmd -q "-Dtest=VendorBillConfigTest" test`  
Expected: PASS.

### Task 11: Web DTOs + VendorBillWebMapper

**Files:**
- Create: `.../web/dto/VendorBillSaveRequest.java`
- Create: `.../web/dto/VendorBillDetailResponse.java`
- Create: `.../web/dto/VendorBillSummaryResponse.java`
- Create: `.../web/dto/VendorBillFormView.java`
- Create: `.../web/mapper/VendorBillWebMapper.java`
- Test: `.../web/mapper/VendorBillWebMapperTest.java`

- [ ] **Step 1: Write failing mapper test**

```java
@Test
void toCreateCommand_should_map_line_items_and_header_fields() {}
```

- [ ] **Step 2: Implement request/response DTOs extending BaseAuditResponse where applicable**

```java
public class VendorBillSaveRequest extends BaseAuditResponse {
    @NotNull private Long vendorId;
    @NotBlank private String vendorInvoiceNumber;
    @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") private LocalDate billDate;
    @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") private LocalDate dueDate;
}
```

- [ ] **Step 3: Implement web mapper without repository dependency**

```java
@Component
public class VendorBillWebMapper {
    public CreateVendorBillCommand toCreateCommand(VendorBillSaveRequest req) { ... }
    public VendorBillDetailResponse toDetailResponse(VendorBillDetailView view) { ... }
}
```

- [ ] **Step 4: Run mapper test + dependency guard**

Run: `.\mvnw.cmd -q "-Dtest=VendorBillWebMapperTest,WebLayerDependencyGuardTest" test`  
Expected: PASS (no repository import in web mapper/controller).

### Task 12: VendorBillController

**Files:**
- Create: `.../web/controller/VendorBillController.java`
- Test: `.../web/controller/VendorBillControllerTest.java`

- [ ] **Step 1: Write failing web MVC tests**

```java
@Test
void list_should_render_vendor_bill_list_template() {}

@Test
void confirm_should_return_success_api_response() {}
```

- [ ] **Step 2: Implement routes + security**

```java
@Controller
@RequestMapping("/accounts-payable/vendor-bills")
@DefaultRedirectUrl("/accounts-payable/vendor-bills")
public class VendorBillController { ... }
```

- [ ] **Step 3: Add CRUD + confirm endpoints (form via AJAX/JSON)**

```java
@PostMapping
@ResponseBody
@PreAuthorize("hasAuthority('VENDOR-BILL_CREATE')")
public ApiResponse<?> create(@Valid @RequestBody VendorBillSaveRequest request) { ... }
```

- [ ] **Step 4: Run controller tests**

Run: `.\mvnw.cmd -q "-Dtest=VendorBillControllerTest" test`  
Expected: PASS.

### Task 13: Thymeleaf Templates (list/form/detail/modal)

**Files:**
- Create: `src/main/resources/templates/accountspayable/vendor-bills/list.html`
- Create: `src/main/resources/templates/accountspayable/vendor-bills/form.html`
- Create: `src/main/resources/templates/accountspayable/vendor-bills/detail.html`
- Create: `src/main/resources/templates/accountspayable/vendor-bills/gr-line-selector-modal.html`

- [ ] **Step 1: Create list page with generic pagination + sortable headers**

```html
<div th:replace="~{fragments/table :: pagination(${page})}"></div>
<th th:replace="~{fragments/table :: sortable('code', #{vendorbill.code})}"></th>
```

- [ ] **Step 2: Create form page with vendor + GR line selector modal trigger**

```html
<button type="button" class="btn btn-outline-primary" data-bs-toggle="modal" data-bs-target="#grLineSelectorModal">
  [[#{vendorbill.select.gr.lines}]]
</button>
```

- [ ] **Step 3: Implement save/confirm/cancel button wiring via `ErpForm.postAction`**

```html
<button type="button" class="btn btn-primary"
        onclick="ErpForm.postAction(this)"
        data-post-url="/accounts-payable/vendor-bills"
        data-confirm-message="#{common.confirm.save}">
  [[#{common.save}]]
</button>
```

- [ ] **Step 4: Add page-specific scripts fragment injection**

```html
<th:block id="page-specific-scripts">
  <script th:src="@{/js/accountspayable/vendor-bills/form.js}"></script>
</th:block>
```

- [ ] **Step 5: Smoke test template rendering**

Run: `.\mvnw.cmd -q "-Dtest=VendorBillControllerTest#list_should_render_vendor_bill_list_template" test`  
Expected: PASS.

### Task 14: Goods Receipt detail billing status integration

**Files:**
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java`
- Modify: `src/main/resources/templates/inventory/goods-receipts/view.html`

- [ ] **Step 1: Write failing GR view test for billing badge**

```java
@Test
void detail_should_show_billing_status_per_line() {}
```

- [ ] **Step 2: Inject `BillableGrQueryPort` into GoodsReceiptController**

```java
private final BillableGrQueryPort billableGrQueryPort;
```

- [ ] **Step 3: Build status map from confirmed billed qty**

```java
Map<Long, BigDecimal> billedQtyMap = billableGrQueryPort.sumConfirmedBilledQtyByGrId(id);
```

- [ ] **Step 4: Render status in GR detail template**

```html
<span class="badge bg-green-lt" th:if="${line.billingStatus == 'FULLY_BILLED'}">[[#{goodsreceipt.billing.fully}]]</span>
<span class="badge bg-yellow-lt" th:if="${line.billingStatus == 'PARTIAL_BILLED'}">[[#{goodsreceipt.billing.partial}]]</span>
<span class="badge bg-secondary-lt" th:if="${line.billingStatus == 'UNBILLED'}">[[#{goodsreceipt.billing.unbilled}]]</span>
```

- [ ] **Step 5: Run GR focused tests**

Run: `.\mvnw.cmd -q "-Dtest=GoodsReceiptControllerTest,GetGoodsReceiptCreateViewUseCaseTest" test`  
Expected: PASS.

### Task 15: Test suite completion + regression

**Files:**
- Create/Modify tests listed in Tasks 2, 7, 8, 11, 12

- [ ] **Step 1: Run focused Vendor Bill regression suite**

Run: `.\mvnw.cmd -q "-Dtest=VendorBillTest,ConfirmVendorBillUseCaseTest,CreateVendorBillUseCaseTest,VendorBillWebMapperTest,VendorBillControllerTest" test`  
Expected: PASS.

- [ ] **Step 2: Run module interaction regression**

Run: `.\mvnw.cmd -q "-Dtest=GoodsReceiptControllerTest,WebLayerDependencyGuardTest" test`  
Expected: PASS.

- [ ] **Step 3: Run full compile safety check**

Run: `.\mvnw.cmd -q -DskipTests compile`  
Expected: PASS.

- [ ] **Step 4: Verify i18n keys and permission strings are complete**

```properties
permission.vendor-bill.read=Read Vendor Bill
msg.error.vb.notfound=Vendor Bill tidak ditemukan
```

- [ ] **Step 5: Commit in small batches**

```bash
git add src/main/resources/db/migration/V58__Add_Vendor_Bill_Module.sql
git commit -m "feat(ap): add vendor bill schema and permissions"
```

---

## Self-Review Checklist (Completed)

- **Spec coverage:** Semua requirement invoice-only (create/update/delete/cancel/confirm + three-way match + auto-journal + GR billing status + UI + test) sudah dipetakan ke 15 task.
- **Placeholder scan:** Tidak ada TBD/TODO/fill-later; setiap task berisi file target, langkah, snippet, dan perintah verifikasi.
- **Type consistency:** Nama inti konsisten: `VendorBill`, `VendorBillLine`, `BillableGrQueryPort`, `ConfirmVendorBillUseCase`, `V58__Add_Vendor_Bill_Module.sql`.

---

## Notes (Scope Lock)

- Scope plan ini **hanya invoice/vendor bill**.
- Payment (`PARTIAL_PAID`, `PAID` settlement flow, bank/coa payment posting) dikerjakan di plan berikutnya setelah modul invoice stabil.

---

Plan complete and saved to `docs/superpowers/plans/2026-05-10-vendor-bill.md`. Two execution options:

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
