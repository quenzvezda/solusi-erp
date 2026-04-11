# Sprint 2: Supplier Price List & Purchase Requisition Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement Supplier Price List CRUD and Purchase Requisition with approval workflow for the Solusi ERP procurement module.

**Architecture:** Clean Architecture + DDD + CQRS monolith. Each feature is a vertical slice: domain → application → infrastructure → web. TDD with JaCoCo coverage targets.

**Tech Stack:** Java 21, Spring Boot 4.0.3, MariaDB, Flyway, MapStruct, JUnit 5, Mockito, Thymeleaf + HTMX

---

## File Structure

### New Files Created

**Flyway Migration:**
- `src/main/resources/db/migration/V46__Add_Purchasing_Module.sql`

**i18n:**
- `src/main/resources/messages.properties` (append purchasing keys)
- `src/main/resources/messages_id.properties` (append purchasing keys)

**Supplier Price List — Domain:**
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/domain/model/SupplierPriceList.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/domain/repository/SupplierPriceListRepository.java`

**Supplier Price List — Application:**
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/CreateSupplierPriceListUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/CreateSupplierPriceListUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/UpdateSupplierPriceListUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/UpdateSupplierPriceListUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/DeleteSupplierPriceListUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/DeleteSupplierPriceListUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/FindSupplierPriceListsUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/FindSupplierPriceListsUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/GetSupplierPriceListEditViewUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/GetSupplierPriceListEditViewUseCaseImpl.java`

**Supplier Price List — Infrastructure:**
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/infrastructure/persistence/SupplierPriceListEntity.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/infrastructure/persistence/SupplierPriceListJpaRepository.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/infrastructure/persistence/SupplierPriceListPersistenceMapper.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/infrastructure/adapter/SupplierPriceListRepositoryImpl.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/infrastructure/config/SupplierPriceListConfig.java`

**Supplier Price List — Web:**
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/dto/SupplierPriceListSaveRequest.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/dto/SupplierPriceListSummaryResponse.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/dto/SupplierPriceListDetailResponse.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/mapper/SupplierPriceListWebMapper.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/controller/SupplierPriceListController.java`
- `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/controller/SupplierPriceListLookupController.java`
- `src/main/resources/templates/purchasing/supplier-price-lists/list.html`
- `src/main/resources/templates/purchasing/supplier-price-lists/form.html`

**Purchase Requisition — Domain:**
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisition.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisitionLine.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisitionStatus.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisitionPriority.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/repository/PurchaseRequisitionRepository.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/port/PurchaseRequisitionEventPublisher.java`

**Purchase Requisition — Application:**
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/CreatePurchaseRequisitionUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/CreatePurchaseRequisitionUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/UpdatePurchaseRequisitionUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/UpdatePurchaseRequisitionUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/DeletePurchaseRequisitionUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/DeletePurchaseRequisitionUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/SubmitPurchaseRequisitionUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/SubmitPurchaseRequisitionUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/CancelPurchaseRequisitionUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/CancelPurchaseRequisitionUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/query/FindPurchaseRequisitionsUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/query/FindPurchaseRequisitionsUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/query/GetPurchaseRequisitionEditViewUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/query/GetPurchaseRequisitionEditViewUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/query/GetPurchaseRequisitionDetailUseCase.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/query/GetPurchaseRequisitionDetailUseCaseImpl.java`

**Purchase Requisition — Infrastructure:**
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/persistence/PurchaseRequisitionEntity.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/persistence/PurchaseRequisitionLineEntity.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/persistence/PurchaseRequisitionJpaRepository.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/persistence/PurchaseRequisitionLineJpaRepository.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/persistence/PurchaseRequisitionPersistenceMapper.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/adapter/PurchaseRequisitionRepositoryImpl.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/adapter/PurchaseRequisitionEventPublisherImpl.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/listener/OnPurchaseRequisitionApprovedListener.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/config/PurchaseRequisitionConfig.java`

**Purchase Requisition — Web:**
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/web/dto/PurchaseRequisitionSaveRequest.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/web/dto/PurchaseRequisitionLineSaveRequest.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/web/dto/PurchaseRequisitionSummaryResponse.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/web/dto/PurchaseRequisitionDetailResponse.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/web/dto/PurchaseRequisitionLineResponse.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/web/mapper/PurchaseRequisitionWebMapper.java`
- `src/main/java/com/solusi/erp/purchasing/purchaserequisition/web/controller/PurchaseRequisitionController.java`
- `src/main/resources/templates/purchasing/purchase-requisitions/list.html`
- `src/main/resources/templates/purchasing/purchase-requisitions/form.html`
- `src/main/resources/templates/purchasing/purchase-requisitions/detail.html`

**Tests:**
- `src/test/java/com/solusi/erp/purchasing/supplierpricelist/domain/model/SupplierPriceListTest.java`
- `src/test/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/CreateSupplierPriceListUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/UpdateSupplierPriceListUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/DeleteSupplierPriceListUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/FindSupplierPriceListsUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/GetSupplierPriceListEditViewUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/supplierpricelist/web/controller/SupplierPriceListControllerTest.java`
- `src/test/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisitionTest.java`
- `src/test/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/CreatePurchaseRequisitionUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/UpdatePurchaseRequisitionUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/DeletePurchaseRequisitionUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/SubmitPurchaseRequisitionUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/command/CancelPurchaseRequisitionUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/query/FindPurchaseRequisitionsUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/query/GetPurchaseRequisitionEditViewUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/purchaserequisition/application/usecase/query/GetPurchaseRequisitionDetailUseCaseTest.java`
- `src/test/java/com/solusi/erp/purchasing/purchaserequisition/web/controller/PurchaseRequisitionControllerTest.java`

---
### Task 1: Flyway Migration V46 — Purchasing Module DDL + Permissions + Seed Data

**Goal:** Create database tables, sequences, permission groups, and permissions for the Purchasing module.

- [ ] Step 1: Create the migration file

**File:** `src/main/resources/db/migration/V46__Add_Purchasing_Module.sql`

```sql
-- V46: Purchasing Module — Supplier Price List, Purchase Requisition, Permissions

-- ============================================================
-- 0. ALTER: Add is_pkp to parties table
-- ============================================================
ALTER TABLE parties ADD COLUMN is_pkp BOOLEAN NOT NULL DEFAULT FALSE AFTER is_active;

-- ============================================================
-- 1. DDL: Supplier Price List
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_supplier_price_lists (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    code             VARCHAR(30)   NOT NULL,
    supplier_id      BIGINT        NOT NULL,
    product_id       BIGINT        NOT NULL,
    uom_id           BIGINT        NOT NULL,
    currency_id      BIGINT        NOT NULL,
    unit_price       DECIMAL(19,4) NOT NULL,
    min_quantity     DECIMAL(19,4) NOT NULL DEFAULT 1,
    effective_from   DATE          NOT NULL,
    effective_to     DATE          NULL,
    note             TEXT          NULL,
    is_active        BOOLEAN       NOT NULL DEFAULT TRUE,
    version          INT           NOT NULL DEFAULT 0,
    created_by_user_id BIGINT     NULL,
    created_date     DATETIME      NULL,
    updated_by_user_id BIGINT     NULL,
    updated_date     DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_spl_code (code),
    UNIQUE KEY uk_spl_combo (supplier_id, product_id, uom_id, currency_id, effective_from),
    CONSTRAINT fk_spl_supplier    FOREIGN KEY (supplier_id)      REFERENCES parties(id),
    CONSTRAINT fk_spl_product     FOREIGN KEY (product_id)       REFERENCES inv_products(id),
    CONSTRAINT fk_spl_uom         FOREIGN KEY (uom_id)           REFERENCES inv_uoms(id),
    CONSTRAINT fk_spl_currency    FOREIGN KEY (currency_id)      REFERENCES currencies(id),
    CONSTRAINT fk_spl_created_by  FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_spl_updated_by  FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. DDL: Purchase Requisition (Header)
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_purchase_requisitions (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    code             VARCHAR(30)   NOT NULL,
    request_date     DATE          NOT NULL,
    requester_id     BIGINT        NULL,
    facility_id      BIGINT        NULL,
    department       VARCHAR(50)   NULL,
    priority         VARCHAR(10)   NOT NULL COMMENT 'LOW | NORMAL | HIGH | URGENT',
    status           VARCHAR(20)   NOT NULL COMMENT 'DRAFT | SUBMITTED | APPROVED | CONVERTED | CANCELLED | REJECTED',
    note             TEXT          NULL,
    is_active        BOOLEAN       NOT NULL DEFAULT TRUE,
    version          INT           NOT NULL DEFAULT 0,
    created_by_user_id BIGINT     NULL,
    created_date     DATETIME      NULL,
    updated_by_user_id BIGINT     NULL,
    updated_date     DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pr_code (code),
    CONSTRAINT fk_pr_requester    FOREIGN KEY (requester_id)       REFERENCES users(id),
    CONSTRAINT fk_pr_facility     FOREIGN KEY (facility_id)        REFERENCES inv_facilities(id),
    CONSTRAINT fk_pr_created_by   FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_pr_updated_by   FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. DDL: Purchase Requisition Lines
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_purchase_requisition_lines (
    id                     BIGINT        NOT NULL AUTO_INCREMENT,
    header_id              BIGINT        NOT NULL,
    product_id             BIGINT        NOT NULL,
    quantity               DECIMAL(19,4) NOT NULL,
    uom_id                 BIGINT        NOT NULL,
    required_date          DATE          NULL,
    estimated_unit_price   DECIMAL(19,4) NULL,
    suggested_supplier_id  BIGINT        NULL,
    converted_po_line_id   BIGINT        NULL,
    note                   TEXT          NULL,
    version                INT           NOT NULL DEFAULT 0,
    created_by_user_id     BIGINT        NULL,
    created_date           DATETIME      NULL,
    updated_by_user_id     BIGINT        NULL,
    updated_date           DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_prl_header     FOREIGN KEY (header_id)            REFERENCES pur_purchase_requisitions(id),
    CONSTRAINT fk_prl_product    FOREIGN KEY (product_id)           REFERENCES inv_products(id),
    CONSTRAINT fk_prl_uom        FOREIGN KEY (uom_id)               REFERENCES inv_uoms(id),
    CONSTRAINT fk_prl_supplier   FOREIGN KEY (suggested_supplier_id) REFERENCES parties(id),
    CONSTRAINT fk_prl_created_by FOREIGN KEY (created_by_user_id)   REFERENCES users(id),
    CONSTRAINT fk_prl_updated_by FOREIGN KEY (updated_by_user_id)   REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. Sequence Registration
-- ============================================================
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by, updated_date)
VALUES ('SPL', 'SPL-{date:yyMM}-{seq}', 5, 'MONTHLY', 'SYSTEM', NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by, updated_date)
VALUES ('PR', 'PR-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 'SYSTEM', NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

-- ============================================================
-- 5. Permission Groups (Menu Entries)
-- ============================================================
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('PUR-01', 'Daftar Harga Supplier', 'Supplier Price List',
 'Pengadaan > Daftar Harga Supplier', 'Procurement (Purchase) > Supplier Price List',
 '/purchasing/supplier-price-lists', 'ti-receipt',
 'Kelola daftar harga supplier', 'Manage supplier price lists',
 200, 1, NOW()),

('PUR-02', 'Permintaan Pembelian', 'Purchase Requisition',
 'Pengadaan > Permintaan Pembelian', 'Procurement (Purchase) > Purchase Requisition',
 '/purchasing/purchase-requisitions', 'ti-file-text',
 'Kelola permintaan pembelian (PR)', 'Manage purchase requisitions (PR)',
 201, 1, NOW());

-- ============================================================
-- 6. Permissions
-- ============================================================
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
-- Supplier Price List permissions
('SPL_READ',   'Melihat daftar harga supplier',        1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-01')),
('SPL_CREATE', 'Membuat daftar harga supplier baru',    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-01')),
('SPL_UPDATE', 'Mengubah daftar harga supplier',        1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-01')),
('SPL_DELETE', 'Menghapus/nonaktifkan daftar harga supplier', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-01')),
('LOOKUP_SUPPLIER-PRICE-LIST', 'Lookup daftar harga supplier untuk autocomplete', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-01')),

-- Purchase Requisition permissions
('PR_READ',   'Melihat daftar permintaan pembelian',   1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02')),
('PR_CREATE', 'Membuat permintaan pembelian baru',      1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02')),
('PR_UPDATE', 'Mengubah permintaan pembelian',          1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02')),
('PR_DELETE', 'Menghapus permintaan pembelian',         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02')),
('PR_SUBMIT', 'Mengajukan permintaan pembelian untuk persetujuan', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02')),
('LOOKUP_PR', 'Lookup permintaan pembelian untuk autocomplete', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02'));

-- ============================================================
-- 7. Grant all purchasing permissions to ROLE_ADMIN
-- ============================================================
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('SPL_READ', 'SPL_CREATE', 'SPL_UPDATE', 'SPL_DELETE', 'LOOKUP_SUPPLIER-PRICE-LIST');

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('PR_READ', 'PR_CREATE', 'PR_UPDATE', 'PR_DELETE', 'PR_SUBMIT', 'LOOKUP_PR');
```

- [ ] Step 2: Run the migration

```bash
mvn flyway:migrate -Dflyway.configFiles=flyway.conf
```

- [ ] Step 3: Commit

```
feat(purchasing): add V46 migration for purchasing module DDL and permissions
```

---

### Task 2: i18n Messages for Purchasing Module

**Goal:** Add all English and Indonesian message labels for SPL and PR features.

- [ ] Step 1: Append English messages to `messages.properties`

**File:** `src/main/resources/messages.properties` (append to end)

```properties
# ============================================================
# Purchasing Module — Supplier Price List
# ============================================================
label.spl.title=Supplier Price List
label.spl.subtitle=Manage supplier product price agreements.
label.spl.add=Add Price List
label.spl.edit=Edit Price List
label.spl.column.code=Code
label.spl.column.supplier=Supplier
label.spl.column.product=Product
label.spl.column.uom=UoM
label.spl.column.currency=Currency
label.spl.column.unitPrice=Unit Price
label.spl.column.minQuantity=Min Qty
label.spl.column.effectiveFrom=Effective From
label.spl.column.effectiveTo=Effective To
label.spl.column.note=Note
label.spl.column.isActive=Active
label.spl.empty=No supplier price list data found.
label.spl.code=Code
label.spl.supplier=Supplier
label.spl.product=Product
label.spl.uom=Unit of Measure
label.spl.currency=Currency
label.spl.unitPrice=Unit Price
label.spl.minQuantity=Min Quantity
label.spl.effectiveFrom=Effective From
label.spl.effectiveTo=Effective To
label.spl.note=Note
label.spl.isActive=Active
placeholder.spl.note=Enter notes...
placeholder.spl.unitPrice=0.0000
placeholder.spl.minQuantity=1.0000
msg.error.spl.notfound=Supplier price list not found.
msg.error.spl.price.positive=Unit price must be greater than zero.
msg.error.spl.date.range=Effective from date must be on or before effective to date.
msg.error.spl.overlap=An overlapping price list exists for the same supplier, product, UoM, currency, and date range.

# ============================================================
# Purchasing Module — Purchase Requisition
# ============================================================
label.pr.title=Purchase Requisition
label.pr.subtitle=Manage purchase requisitions and approval workflow.
label.pr.add=New Requisition
label.pr.edit=Edit Requisition
label.pr.detail=Requisition Detail
label.pr.column.code=Code
label.pr.column.requestDate=Request Date
label.pr.column.requester=Requester
label.pr.column.facility=Facility
label.pr.column.department=Department
label.pr.column.priority=Priority
label.pr.column.status=Status
label.pr.column.note=Note
label.pr.empty=No purchase requisition data found.
label.pr.code=Code
label.pr.requestDate=Request Date
label.pr.requester=Requester
label.pr.facility=Facility
label.pr.department=Department
label.pr.priority=Priority
label.pr.status=Status
label.pr.note=Note
label.pr.isActive=Active
placeholder.pr.note=Enter notes...
placeholder.pr.department=e.g. IT, Operations, Finance

label.pr.line.title=Requisition Lines
label.pr.line.add=Add Line
label.pr.line.product=Product
label.pr.line.quantity=Quantity
label.pr.line.uom=UoM
label.pr.line.requiredDate=Required Date
label.pr.line.estimatedUnitPrice=Est. Unit Price
label.pr.line.suggestedSupplier=Suggested Supplier
label.pr.line.note=Note
label.pr.line.empty=No lines added yet.

label.pr.priority.LOW=Low
label.pr.priority.NORMAL=Normal
label.pr.priority.HIGH=High
label.pr.priority.URGENT=Urgent

label.pr.status.DRAFT=Draft
label.pr.status.SUBMITTED=Submitted
label.pr.status.APPROVED=Approved
label.pr.status.CONVERTED=Converted
label.pr.status.CANCELLED=Cancelled
label.pr.status.REJECTED=Rejected

label.pr.action.submit=Submit for Approval
label.pr.action.cancel=Cancel Requisition
label.pr.action.view=View Detail

msg.error.pr.notfound=Purchase requisition not found.
msg.error.pr.line.notfound=Purchase requisition line not found.
msg.error.pr.submit.no.lines=Cannot submit: requisition must have at least one line.
msg.error.pr.submit.invalid.status=Cannot submit: requisition is not in DRAFT status.
msg.error.pr.cancel.invalid.status=Cannot cancel: only DRAFT, SUBMITTED, or APPROVED requisitions can be cancelled.
msg.error.pr.update.not.draft=Cannot update: requisition is not in DRAFT status.
msg.error.pr.delete.not.draft=Cannot delete: only DRAFT requisitions can be deleted.
msg.error.pr.line.quantity.positive=Line quantity must be greater than zero.
msg.success.pr.submitted=Purchase requisition submitted for approval.
msg.success.pr.cancelled=Purchase requisition cancelled.
```

- [ ] Step 2: Append Indonesian messages to `messages_id.properties`

**File:** `src/main/resources/messages_id.properties` (append to end)

```properties
# ============================================================
# Modul Pengadaan — Daftar Harga Supplier
# ============================================================
label.spl.title=Daftar Harga Supplier
label.spl.subtitle=Kelola kesepakatan harga produk supplier.
label.spl.add=Tambah Daftar Harga
label.spl.edit=Ubah Daftar Harga
label.spl.column.code=Kode
label.spl.column.supplier=Supplier
label.spl.column.product=Produk
label.spl.column.uom=Satuan
label.spl.column.currency=Mata Uang
label.spl.column.unitPrice=Harga Satuan
label.spl.column.minQuantity=Qty Min
label.spl.column.effectiveFrom=Berlaku Dari
label.spl.column.effectiveTo=Berlaku Sampai
label.spl.column.note=Catatan
label.spl.column.isActive=Aktif
label.spl.empty=Tidak ada data daftar harga supplier.
label.spl.code=Kode
label.spl.supplier=Supplier
label.spl.product=Produk
label.spl.uom=Satuan
label.spl.currency=Mata Uang
label.spl.unitPrice=Harga Satuan
label.spl.minQuantity=Kuantitas Minimum
label.spl.effectiveFrom=Berlaku Dari
label.spl.effectiveTo=Berlaku Sampai
label.spl.note=Catatan
label.spl.isActive=Aktif
placeholder.spl.note=Masukkan catatan...
placeholder.spl.unitPrice=0,0000
placeholder.spl.minQuantity=1,0000
msg.error.spl.notfound=Daftar harga supplier tidak ditemukan.
msg.error.spl.price.positive=Harga satuan harus lebih dari nol.
msg.error.spl.date.range=Tanggal berlaku dari harus sama atau sebelum tanggal berlaku sampai.
msg.error.spl.overlap=Terdapat daftar harga yang tumpang tindih untuk kombinasi supplier, produk, satuan, mata uang, dan rentang tanggal yang sama.

# ============================================================
# Modul Pengadaan — Permintaan Pembelian
# ============================================================
label.pr.title=Permintaan Pembelian
label.pr.subtitle=Kelola permintaan pembelian dan alur persetujuan.
label.pr.add=Buat Permintaan Baru
label.pr.edit=Ubah Permintaan
label.pr.detail=Detail Permintaan
label.pr.column.code=Kode
label.pr.column.requestDate=Tanggal Permintaan
label.pr.column.requester=Pemohon
label.pr.column.facility=Fasilitas
label.pr.column.department=Departemen
label.pr.column.priority=Prioritas
label.pr.column.status=Status
label.pr.column.note=Catatan
label.pr.empty=Tidak ada data permintaan pembelian.
label.pr.code=Kode
label.pr.requestDate=Tanggal Permintaan
label.pr.requester=Pemohon
label.pr.facility=Fasilitas
label.pr.department=Departemen
label.pr.priority=Prioritas
label.pr.status=Status
label.pr.note=Catatan
label.pr.isActive=Aktif
placeholder.pr.note=Masukkan catatan...
placeholder.pr.department=contoh: IT, Operasional, Keuangan

label.pr.line.title=Baris Permintaan
label.pr.line.add=Tambah Baris
label.pr.line.product=Produk
label.pr.line.quantity=Kuantitas
label.pr.line.uom=Satuan
label.pr.line.requiredDate=Tanggal Dibutuhkan
label.pr.line.estimatedUnitPrice=Est. Harga Satuan
label.pr.line.suggestedSupplier=Supplier yang Disarankan
label.pr.line.note=Catatan
label.pr.line.empty=Belum ada baris yang ditambahkan.

label.pr.priority.LOW=Rendah
label.pr.priority.NORMAL=Normal
label.pr.priority.HIGH=Tinggi
label.pr.priority.URGENT=Mendesak

label.pr.status.DRAFT=Draf
label.pr.status.SUBMITTED=Diajukan
label.pr.status.APPROVED=Disetujui
label.pr.status.CONVERTED=Dikonversi
label.pr.status.CANCELLED=Dibatalkan
label.pr.status.REJECTED=Ditolak

label.pr.action.submit=Ajukan Persetujuan
label.pr.action.cancel=Batalkan Permintaan
label.pr.action.view=Lihat Detail

msg.error.pr.notfound=Permintaan pembelian tidak ditemukan.
msg.error.pr.line.notfound=Baris permintaan pembelian tidak ditemukan.
msg.error.pr.submit.no.lines=Tidak dapat mengajukan: permintaan harus memiliki minimal satu baris.
msg.error.pr.submit.invalid.status=Tidak dapat mengajukan: permintaan tidak berstatus DRAF.
msg.error.pr.cancel.invalid.status=Tidak dapat membatalkan: hanya permintaan berstatus DRAF, DIAJUKAN, atau DISETUJUI yang dapat dibatalkan.
msg.error.pr.update.not.draft=Tidak dapat mengubah: permintaan tidak berstatus DRAF.
msg.error.pr.delete.not.draft=Tidak dapat menghapus: hanya permintaan berstatus DRAF yang dapat dihapus.
msg.error.pr.line.quantity.positive=Kuantitas baris harus lebih dari nol.
msg.success.pr.submitted=Permintaan pembelian telah diajukan untuk persetujuan.
msg.success.pr.cancelled=Permintaan pembelian telah dibatalkan.
```

- [ ] Step 3: Commit

```
feat(purchasing): add i18n messages for supplier price list and purchase requisition
```

---

### Task 3: Supplier Price List — Domain Layer (with tests)

**Goal:** Create the SupplierPriceList domain model and repository interface with comprehensive domain tests.

- [ ] Step 1: Write domain model test (RED)

**File:** `src/test/java/com/solusi/erp/purchasing/supplierpricelist/domain/model/SupplierPriceListTest.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SupplierPriceList Domain Model Tests")
class SupplierPriceListTest {

    @Nested
    @DisplayName("createNew factory method")
    class CreateNew {

        @Test
        @DisplayName("creates new price list with valid parameters and empty metadata")
        void createNew_withValidParams_setsFieldsAndEmptyMetadata() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-2607-00001",
                1L, 2L, 3L, 4L,
                new BigDecimal("150.0000"),
                new BigDecimal("10.0000"),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 12, 31),
                "Bulk discount",
                true
            );

            assertThat(spl.getId()).isNull();
            assertThat(spl.getCode()).isEqualTo("SPL-2607-00001");
            assertThat(spl.getSupplierId()).isEqualTo(1L);
            assertThat(spl.getProductId()).isEqualTo(2L);
            assertThat(spl.getUomId()).isEqualTo(3L);
            assertThat(spl.getCurrencyId()).isEqualTo(4L);
            assertThat(spl.getUnitPrice()).isEqualByComparingTo("150.0000");
            assertThat(spl.getMinQuantity()).isEqualByComparingTo("10.0000");
            assertThat(spl.getEffectiveFrom()).isEqualTo(LocalDate.of(2026, 7, 1));
            assertThat(spl.getEffectiveTo()).isEqualTo(LocalDate.of(2026, 12, 31));
            assertThat(spl.getNote()).isEqualTo("Bulk discount");
            assertThat(spl.isActive()).isTrue();
        }

        @Test
        @DisplayName("creates new price list with null effective_to (open-ended)")
        void createNew_withNullEffectiveTo_succeeds() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-2607-00002",
                1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null,
                null,
                true
            );

            assertThat(spl.getEffectiveTo()).isNull();
            assertThat(spl.getNote()).isNull();
        }

        @Test
        @DisplayName("throws DomainException when unit price is zero")
        void createNew_withZeroPrice_throwsDomainException() {
            assertThatThrownBy(() -> SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                BigDecimal.ZERO,
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.spl.price.positive");
        }

        @Test
        @DisplayName("throws DomainException when unit price is negative")
        void createNew_withNegativePrice_throwsDomainException() {
            assertThatThrownBy(() -> SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("-5.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.spl.price.positive");
        }

        @Test
        @DisplayName("throws DomainException when effective_from is after effective_to")
        void createNew_withInvalidDateRange_throwsDomainException() {
            assertThatThrownBy(() -> SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1),
                null, true
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.spl.date.range");
        }

        @Test
        @DisplayName("succeeds when effective_from equals effective_to")
        void createNew_withSameDates_succeeds() {
            LocalDate sameDate = LocalDate.of(2026, 7, 15);
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                sameDate, sameDate,
                null, true
            );

            assertThat(spl.getEffectiveFrom()).isEqualTo(sameDate);
            assertThat(spl.getEffectiveTo()).isEqualTo(sameDate);
        }
    }

    @Nested
    @DisplayName("full constructor")
    class FullConstructor {

        @Test
        @DisplayName("preserves all fields including metadata")
        void constructor_preservesAllFieldsIncludingMetadata() {
            AuditMetadata metadata = new AuditMetadata(99L, 2L, null, null, null, null);
            SupplierPriceList spl = new SupplierPriceList(
                metadata,
                "SPL-2607-00001",
                10L, 20L, 30L, 40L,
                new BigDecimal("200.5000"),
                new BigDecimal("5.0000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30),
                "Note",
                true
            );

            assertThat(spl.getId()).isEqualTo(99L);
            assertThat(spl.getMetadata()).isEqualTo(metadata);
            assertThat(spl.getCode()).isEqualTo("SPL-2607-00001");
            assertThat(spl.getSupplierId()).isEqualTo(10L);
            assertThat(spl.getProductId()).isEqualTo(20L);
            assertThat(spl.getUomId()).isEqualTo(30L);
            assertThat(spl.getCurrencyId()).isEqualTo(40L);
            assertThat(spl.getUnitPrice()).isEqualByComparingTo("200.5000");
            assertThat(spl.getMinQuantity()).isEqualByComparingTo("5.0000");
            assertThat(spl.getEffectiveFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(spl.getEffectiveTo()).isEqualTo(LocalDate.of(2026, 6, 30));
            assertThat(spl.getNote()).isEqualTo("Note");
            assertThat(spl.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("update method")
    class Update {

        @Test
        @DisplayName("updates mutable fields; code and supplierId remain unchanged")
        void update_changesMutableFields() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 12, 31),
                "Old note", true
            );

            spl.update(
                5L, 6L, 7L,
                new BigDecimal("250.0000"),
                new BigDecimal("20.0000"),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2027, 1, 31),
                "New note",
                false
            );

            assertThat(spl.getCode()).isEqualTo("SPL-001");
            assertThat(spl.getSupplierId()).isEqualTo(1L);
            assertThat(spl.getProductId()).isEqualTo(5L);
            assertThat(spl.getUomId()).isEqualTo(6L);
            assertThat(spl.getCurrencyId()).isEqualTo(7L);
            assertThat(spl.getUnitPrice()).isEqualByComparingTo("250.0000");
            assertThat(spl.getMinQuantity()).isEqualByComparingTo("20.0000");
            assertThat(spl.getEffectiveFrom()).isEqualTo(LocalDate.of(2026, 8, 1));
            assertThat(spl.getEffectiveTo()).isEqualTo(LocalDate.of(2027, 1, 31));
            assertThat(spl.getNote()).isEqualTo("New note");
            assertThat(spl.isActive()).isFalse();
        }

        @Test
        @DisplayName("throws DomainException when update price is zero")
        void update_withZeroPrice_throwsDomainException() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            );

            assertThatThrownBy(() -> spl.update(
                2L, 3L, 4L,
                BigDecimal.ZERO,
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.spl.price.positive");
        }

        @Test
        @DisplayName("throws DomainException when update date range is invalid")
        void update_withInvalidDateRange_throwsDomainException() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            );

            assertThatThrownBy(() -> spl.update(
                2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2027, 1, 1),
                LocalDate.of(2026, 6, 1),
                null, true
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.spl.date.range");
        }
    }

    @Nested
    @DisplayName("deactivate method")
    class Deactivate {

        @Test
        @DisplayName("sets isActive to false")
        void deactivate_setsIsActiveToFalse() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            );

            spl.deactivate();

            assertThat(spl.isActive()).isFalse();
        }
    }
}
```

- [ ] Step 2: Run test — expect FAIL (class does not exist yet)

```bash
mvn test -pl . -Dtest="com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceListTest" -Dsurefire.failIfNoSpecifiedTests=false
```

- [ ] Step 3: Create the SupplierPriceList domain model

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/domain/model/SupplierPriceList.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SupplierPriceList {

    private final AuditMetadata metadata;
    private final String code;
    private final Long supplierId;
    private Long productId;
    private Long uomId;
    private Long currencyId;
    private BigDecimal unitPrice;
    private BigDecimal minQuantity;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String note;
    private boolean active;

    public SupplierPriceList(AuditMetadata metadata, String code, Long supplierId,
                             Long productId, Long uomId, Long currencyId,
                             BigDecimal unitPrice, BigDecimal minQuantity,
                             LocalDate effectiveFrom, LocalDate effectiveTo,
                             String note, boolean active) {
        this.metadata = metadata;
        this.code = code;
        this.supplierId = supplierId;
        this.productId = productId;
        this.uomId = uomId;
        this.currencyId = currencyId;
        this.unitPrice = unitPrice;
        this.minQuantity = minQuantity;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.note = note;
        this.active = active;
    }

    public static SupplierPriceList createNew(String code, Long supplierId,
                                               Long productId, Long uomId, Long currencyId,
                                               BigDecimal unitPrice, BigDecimal minQuantity,
                                               LocalDate effectiveFrom, LocalDate effectiveTo,
                                               String note, boolean active) {
        validateUnitPrice(unitPrice);
        validateDateRange(effectiveFrom, effectiveTo);
        return new SupplierPriceList(AuditMetadata.empty(), code, supplierId,
            productId, uomId, currencyId, unitPrice, minQuantity,
            effectiveFrom, effectiveTo, note, active);
    }

    public void update(Long productId, Long uomId, Long currencyId,
                       BigDecimal unitPrice, BigDecimal minQuantity,
                       LocalDate effectiveFrom, LocalDate effectiveTo,
                       String note, boolean active) {
        validateUnitPrice(unitPrice);
        validateDateRange(effectiveFrom, effectiveTo);
        this.productId = productId;
        this.uomId = uomId;
        this.currencyId = currencyId;
        this.unitPrice = unitPrice;
        this.minQuantity = minQuantity;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.note = note;
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }

    private static void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.spl.price.positive");
        }
    }

    private static void validateDateRange(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new DomainException("msg.error.spl.date.range");
        }
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public Long getSupplierId() { return supplierId; }
    public Long getProductId() { return productId; }
    public Long getUomId() { return uomId; }
    public Long getCurrencyId() { return currencyId; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getMinQuantity() { return minQuantity; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public String getNote() { return note; }
    public boolean isActive() { return active; }
}
```

- [ ] Step 4: Create the SupplierPriceListRepository interface

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/domain/repository/SupplierPriceListRepository.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SupplierPriceListRepository {

    SupplierPriceList save(SupplierPriceList supplierPriceList);

    Optional<SupplierPriceList> findById(Long id);

    Page<SupplierPriceList> findAll(String keyword, Pageable pageable);

    List<SupplierPriceList> search(String keyword, int limit);

    void delete(Long id);

    boolean existsOverlapping(Long supplierId, Long productId, Long uomId, Long currencyId,
                               LocalDate effectiveFrom, LocalDate effectiveTo, Long excludeId);
}
```

- [ ] Step 5: Run test — expect PASS

```bash
mvn test -pl . -Dtest="com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceListTest"
```

- [ ] Step 6: Commit

```
feat(purchasing): add supplier price list domain model and repository interface
```

---

### Task 4: Supplier Price List — Application Layer (with tests)

**Goal:** Create all use case interfaces and implementations for Supplier Price List CRUD with comprehensive tests.

- [ ] Step 1: Write CreateSupplierPriceListUseCase test (RED)

**File:** `src/test/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/CreateSupplierPriceListUseCaseTest.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateSupplierPriceListUseCase Tests")
class CreateSupplierPriceListUseCaseTest {

    @Mock
    private SupplierPriceListRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CreateSupplierPriceListUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateSupplierPriceListUseCaseImpl(repository, sequenceGeneratorService);
    }

    @Test
    @DisplayName("execute generates code from sequence and saves price list")
    void execute_generatesCodeAndSaves() {
        when(sequenceGeneratorService.generate("SPL")).thenReturn("SPL-2607-00001");
        when(repository.existsOverlapping(eq(1L), eq(2L), eq(3L), eq(4L),
            eq(LocalDate.of(2026, 7, 1)), isNull(), isNull())).thenReturn(false);
        when(repository.save(any(SupplierPriceList.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierPriceList result = useCase.execute(
            1L, 2L, 3L, 4L,
            new BigDecimal("150.0000"),
            new BigDecimal("10.0000"),
            LocalDate.of(2026, 7, 1),
            null, "Test note", true
        );

        assertThat(result.getCode()).isEqualTo("SPL-2607-00001");
        assertThat(result.getSupplierId()).isEqualTo(1L);
        assertThat(result.getUnitPrice()).isEqualByComparingTo("150.0000");
        verify(repository).save(any(SupplierPriceList.class));
    }

    @Test
    @DisplayName("execute returns persisted result with id from repository")
    void execute_returnsPersisted() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        SupplierPriceList persisted = new SupplierPriceList(metadata, "SPL-2607-00001",
            1L, 2L, 3L, 4L, new BigDecimal("150.0000"), new BigDecimal("10.0000"),
            LocalDate.of(2026, 7, 1), null, "note", true);

        when(sequenceGeneratorService.generate("SPL")).thenReturn("SPL-2607-00001");
        when(repository.existsOverlapping(any(), any(), any(), any(), any(), any(), any())).thenReturn(false);
        when(repository.save(any(SupplierPriceList.class))).thenReturn(persisted);

        SupplierPriceList result = useCase.execute(
            1L, 2L, 3L, 4L,
            new BigDecimal("150.0000"),
            new BigDecimal("10.0000"),
            LocalDate.of(2026, 7, 1),
            null, "note", true
        );

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("execute throws DomainException when overlapping price list exists")
    void execute_throwsWhenOverlapping() {
        when(sequenceGeneratorService.generate("SPL")).thenReturn("SPL-2607-00001");
        when(repository.existsOverlapping(eq(1L), eq(2L), eq(3L), eq(4L),
            eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 12, 31)), isNull())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(
            1L, 2L, 3L, 4L,
            new BigDecimal("150.0000"),
            new BigDecimal("10.0000"),
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 12, 31),
            null, true
        ))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.spl.overlap");
    }
}
```

- [ ] Step 2: Write UpdateSupplierPriceListUseCase test (RED)

**File:** `src/test/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/UpdateSupplierPriceListUseCaseTest.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSupplierPriceListUseCase Tests")
class UpdateSupplierPriceListUseCaseTest {

    @Mock
    private SupplierPriceListRepository repository;

    private UpdateSupplierPriceListUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateSupplierPriceListUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates price list fields successfully")
    void execute_updatesSuccessfully() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList existing = new SupplierPriceList(metadata, "SPL-001",
            10L, 20L, 30L, 40L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), "Old", true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsOverlapping(eq(10L), eq(25L), eq(35L), eq(45L),
            eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 12, 31)), eq(1L))).thenReturn(false);
        when(repository.save(any(SupplierPriceList.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierPriceList result = useCase.execute(1L,
            25L, 35L, 45L,
            new BigDecimal("200.0000"), new BigDecimal("5.0000"),
            LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31),
            "Updated", true
        );

        assertThat(result.getProductId()).isEqualTo(25L);
        assertThat(result.getUnitPrice()).isEqualByComparingTo("200.0000");
        assertThat(result.getNote()).isEqualTo("Updated");
        verify(repository).save(any(SupplierPriceList.class));
    }

    @Test
    @DisplayName("execute throws DomainException when not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L,
            2L, 3L, 4L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, null, true
        ))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.spl.notfound");
    }

    @Test
    @DisplayName("execute throws DomainException when overlapping after update")
    void execute_throwsWhenOverlapping() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList existing = new SupplierPriceList(metadata, "SPL-001",
            10L, 20L, 30L, 40L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 1, 1), null, null, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsOverlapping(eq(10L), eq(20L), eq(30L), eq(40L),
            eq(LocalDate.of(2026, 7, 1)), eq(LocalDate.of(2026, 12, 31)), eq(1L))).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L,
            20L, 30L, 40L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31), null, true
        ))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.spl.overlap");
    }
}
```

- [ ] Step 3: Write DeleteSupplierPriceListUseCase test (RED)

**File:** `src/test/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/DeleteSupplierPriceListUseCaseTest.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteSupplierPriceListUseCase Tests")
class DeleteSupplierPriceListUseCaseTest {

    @Mock
    private SupplierPriceListRepository repository;

    private DeleteSupplierPriceListUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteSupplierPriceListUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute deactivates existing price list (soft delete)")
    void execute_deactivatesExistingPriceList() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList existing = new SupplierPriceList(metadata, "SPL-001",
            1L, 2L, 3L, 4L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, null, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(SupplierPriceList.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L);

        verify(repository).save(any(SupplierPriceList.class));
    }

    @Test
    @DisplayName("execute throws DomainException when not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.spl.notfound");
    }
}
```

- [ ] Step 4: Write FindSupplierPriceListsUseCase test (RED)

**File:** `src/test/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/FindSupplierPriceListsUseCaseTest.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindSupplierPriceListsUseCase Tests")
class FindSupplierPriceListsUseCaseTest {

    @Mock
    private SupplierPriceListRepository repository;

    private FindSupplierPriceListsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindSupplierPriceListsUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates keyword and pageable to repository")
    void execute_delegatesToRepository() {
        Pageable pageable = Pageable.of(0, 20);
        Page<SupplierPriceList> expected = new Page<>(List.of(), 0, 20, 0L);
        when(repository.findAll("test", pageable)).thenReturn(expected);

        Page<SupplierPriceList> result = useCase.execute("test", pageable);

        assertThat(result).isEqualTo(expected);
        verify(repository).findAll("test", pageable);
    }
}
```

- [ ] Step 5: Write GetSupplierPriceListEditViewUseCase test (RED)

**File:** `src/test/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/GetSupplierPriceListEditViewUseCaseTest.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSupplierPriceListEditViewUseCase Tests")
class GetSupplierPriceListEditViewUseCaseTest {

    @Mock
    private SupplierPriceListRepository repository;

    private GetSupplierPriceListEditViewUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetSupplierPriceListEditViewUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute returns price list when found")
    void execute_returnsPriceListWhenFound() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList spl = new SupplierPriceList(metadata, "SPL-001",
            1L, 2L, 3L, 4L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, null, true);

        when(repository.findById(1L)).thenReturn(Optional.of(spl));

        Optional<SupplierPriceList> result = useCase.execute(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("SPL-001");
    }

    @Test
    @DisplayName("execute returns empty when not found")
    void execute_returnsEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<SupplierPriceList> result = useCase.execute(999L);

        assertThat(result).isEmpty();
    }
}
```

- [ ] Step 6: Run all tests — expect FAIL (use case classes do not exist yet)

```bash
mvn test -pl . -Dtest="com.solusi.erp.purchasing.supplierpricelist.application.**" -Dsurefire.failIfNoSpecifiedTests=false
```

- [ ] Step 7: Create use case interfaces and implementations

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/CreateSupplierPriceListUseCase.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;

import java.math.BigDecimal;
import java.time.LocalDate;

@FunctionalInterface
public interface CreateSupplierPriceListUseCase {
    SupplierPriceList execute(Long supplierId, Long productId, Long uomId, Long currencyId,
                              BigDecimal unitPrice, BigDecimal minQuantity,
                              LocalDate effectiveFrom, LocalDate effectiveTo,
                              String note, boolean active);
}
```

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/CreateSupplierPriceListUseCaseImpl.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateSupplierPriceListUseCaseImpl implements CreateSupplierPriceListUseCase {

    private final SupplierPriceListRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateSupplierPriceListUseCaseImpl(SupplierPriceListRepository repository,
                                               SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public SupplierPriceList execute(Long supplierId, Long productId, Long uomId, Long currencyId,
                                      BigDecimal unitPrice, BigDecimal minQuantity,
                                      LocalDate effectiveFrom, LocalDate effectiveTo,
                                      String note, boolean active) {
        String code = sequenceGeneratorService.generate("SPL");

        if (repository.existsOverlapping(supplierId, productId, uomId, currencyId,
                effectiveFrom, effectiveTo, null)) {
            throw new DomainException("msg.error.spl.overlap");
        }

        SupplierPriceList spl = SupplierPriceList.createNew(
            code, supplierId, productId, uomId, currencyId,
            unitPrice, minQuantity, effectiveFrom, effectiveTo, note, active
        );
        return repository.save(spl);
    }
}
```

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/UpdateSupplierPriceListUseCase.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;

import java.math.BigDecimal;
import java.time.LocalDate;

@FunctionalInterface
public interface UpdateSupplierPriceListUseCase {
    SupplierPriceList execute(Long id, Long productId, Long uomId, Long currencyId,
                              BigDecimal unitPrice, BigDecimal minQuantity,
                              LocalDate effectiveFrom, LocalDate effectiveTo,
                              String note, boolean active);
}
```

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/UpdateSupplierPriceListUseCaseImpl.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateSupplierPriceListUseCaseImpl implements UpdateSupplierPriceListUseCase {

    private final SupplierPriceListRepository repository;

    public UpdateSupplierPriceListUseCaseImpl(SupplierPriceListRepository repository) {
        this.repository = repository;
    }

    @Override
    public SupplierPriceList execute(Long id, Long productId, Long uomId, Long currencyId,
                                      BigDecimal unitPrice, BigDecimal minQuantity,
                                      LocalDate effectiveFrom, LocalDate effectiveTo,
                                      String note, boolean active) {
        SupplierPriceList spl = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.spl.notfound"));

        if (repository.existsOverlapping(spl.getSupplierId(), productId, uomId, currencyId,
                effectiveFrom, effectiveTo, id)) {
            throw new DomainException("msg.error.spl.overlap");
        }

        spl.update(productId, uomId, currencyId, unitPrice, minQuantity,
            effectiveFrom, effectiveTo, note, active);
        return repository.save(spl);
    }
}
```

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/DeleteSupplierPriceListUseCase.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

@FunctionalInterface
public interface DeleteSupplierPriceListUseCase {
    void execute(Long id);
}
```

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/command/DeleteSupplierPriceListUseCaseImpl.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;

public class DeleteSupplierPriceListUseCaseImpl implements DeleteSupplierPriceListUseCase {

    private final SupplierPriceListRepository repository;

    public DeleteSupplierPriceListUseCaseImpl(SupplierPriceListRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        SupplierPriceList spl = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.spl.notfound"));
        spl.deactivate();
        repository.save(spl);
    }
}
```

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/FindSupplierPriceListsUseCase.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;

@FunctionalInterface
public interface FindSupplierPriceListsUseCase {
    Page<SupplierPriceList> execute(String keyword, Pageable pageable);
}
```

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/FindSupplierPriceListsUseCaseImpl.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;

public class FindSupplierPriceListsUseCaseImpl implements FindSupplierPriceListsUseCase {

    private final SupplierPriceListRepository repository;

    public FindSupplierPriceListsUseCaseImpl(SupplierPriceListRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<SupplierPriceList> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
```

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/GetSupplierPriceListEditViewUseCase.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;

import java.util.Optional;

@FunctionalInterface
public interface GetSupplierPriceListEditViewUseCase {
    Optional<SupplierPriceList> execute(Long id);
}
```

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/application/usecase/query/GetSupplierPriceListEditViewUseCaseImpl.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;

import java.util.Optional;

public class GetSupplierPriceListEditViewUseCaseImpl implements GetSupplierPriceListEditViewUseCase {

    private final SupplierPriceListRepository repository;

    public GetSupplierPriceListEditViewUseCaseImpl(SupplierPriceListRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SupplierPriceList> execute(Long id) {
        return repository.findById(id);
    }
}
```

- [ ] Step 8: Run all tests — expect PASS

```bash
mvn test -pl . -Dtest="com.solusi.erp.purchasing.supplierpricelist.**"
```

- [ ] Step 9: Commit

```
feat(purchasing): add supplier price list use cases with tests
```

---

### Task 5: Supplier Price List — Infrastructure Layer

**Goal:** Create JPA entity, repository, MapStruct mapper, repository implementation, and Composition Root configuration.

- [ ] Step 1: Create JPA Entity

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/infrastructure/persistence/SupplierPriceListEntity.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pur_supplier_price_lists")
@Getter
@Setter
@NoArgsConstructor
public class SupplierPriceListEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "min_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal minQuantity;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
```

- [ ] Step 2: Create JPA Repository

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/infrastructure/persistence/SupplierPriceListJpaRepository.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SupplierPriceListJpaRepository extends JpaRepository<SupplierPriceListEntity, Long> {

    @Query("SELECT s FROM SupplierPriceListEntity s WHERE " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(s.unitPrice AS string) LIKE CONCAT('%', :keyword, '%')")
    Page<SupplierPriceListEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(s) > 0 FROM SupplierPriceListEntity s WHERE " +
           "s.supplierId = :supplierId AND s.productId = :productId AND " +
           "s.uomId = :uomId AND s.currencyId = :currencyId AND " +
           "s.active = true AND " +
           "(:excludeId IS NULL OR s.id <> :excludeId) AND " +
           "s.effectiveFrom <= COALESCE(:effectiveTo, s.effectiveFrom) AND " +
           "(s.effectiveTo IS NULL OR s.effectiveTo >= :effectiveFrom)")
    boolean existsOverlapping(@Param("supplierId") Long supplierId,
                               @Param("productId") Long productId,
                               @Param("uomId") Long uomId,
                               @Param("currencyId") Long currencyId,
                               @Param("effectiveFrom") LocalDate effectiveFrom,
                               @Param("effectiveTo") LocalDate effectiveTo,
                               @Param("excludeId") Long excludeId);
}
```

- [ ] Step 3: Create Persistence Mapper

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/infrastructure/persistence/SupplierPriceListPersistenceMapper.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierPriceListPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    SupplierPriceList toDomain(SupplierPriceListEntity entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    SupplierPriceListEntity toEntity(SupplierPriceList domain);

    default AuditMetadata toAuditMetadata(SupplierPriceListEntity entity) {
        return new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
    }
}
```

- [ ] Step 4: Create Repository Implementation

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/infrastructure/adapter/SupplierPriceListRepositoryImpl.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListEntity;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListJpaRepository;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SupplierPriceListRepositoryImpl implements SupplierPriceListRepository {

    private final SupplierPriceListJpaRepository jpaRepository;
    private final SupplierPriceListPersistenceMapper mapper;

    public SupplierPriceListRepositoryImpl(SupplierPriceListJpaRepository jpaRepository,
                                            SupplierPriceListPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SupplierPriceList save(SupplierPriceList domain) {
        SupplierPriceListEntity entity = mapper.toEntity(domain);
        SupplierPriceListEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SupplierPriceList> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<SupplierPriceList> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<SupplierPriceListEntity> springPage =
            (keyword != null && !keyword.isBlank())
                ? jpaRepository.search(keyword, springPageable)
                : jpaRepository.findAll(springPageable);
        return new Page<>(
            springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements()
        );
    }

    @Override
    public List<SupplierPriceList> search(String keyword, int limit) {
        return jpaRepository.search(
                keyword != null ? keyword : "",
                PageRequest.of(0, limit)
            ).getContent().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsOverlapping(Long supplierId, Long productId, Long uomId, Long currencyId,
                                      LocalDate effectiveFrom, LocalDate effectiveTo, Long excludeId) {
        return jpaRepository.existsOverlapping(supplierId, productId, uomId, currencyId,
            effectiveFrom, effectiveTo, excludeId);
    }
}
```

- [ ] Step 5: Create Composition Root Config

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/infrastructure/config/SupplierPriceListConfig.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.infrastructure.config;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.command.*;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.query.*;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.adapter.SupplierPriceListRepositoryImpl;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListJpaRepository;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class SupplierPriceListConfig {

    @Bean
    public SupplierPriceListRepository supplierPriceListDomainRepository(
            SupplierPriceListJpaRepository jpaRepository,
            SupplierPriceListPersistenceMapper mapper) {
        return new SupplierPriceListRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public CreateSupplierPriceListUseCase createSupplierPriceListUseCase(
            SupplierPriceListRepository supplierPriceListDomainRepository,
            SequenceGeneratorService sequenceGeneratorService,
            PlatformTransactionManager txManager) {
        CreateSupplierPriceListUseCase pure = new CreateSupplierPriceListUseCaseImpl(
            supplierPriceListDomainRepository, sequenceGeneratorService);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (supplierId, productId, uomId, currencyId, unitPrice, minQuantity,
                effectiveFrom, effectiveTo, note, active) ->
            tx.execute(status -> pure.execute(supplierId, productId, uomId, currencyId,
                unitPrice, minQuantity, effectiveFrom, effectiveTo, note, active));
    }

    @Bean
    public UpdateSupplierPriceListUseCase updateSupplierPriceListUseCase(
            SupplierPriceListRepository supplierPriceListDomainRepository,
            PlatformTransactionManager txManager) {
        UpdateSupplierPriceListUseCase pure = new UpdateSupplierPriceListUseCaseImpl(
            supplierPriceListDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, productId, uomId, currencyId, unitPrice, minQuantity,
                effectiveFrom, effectiveTo, note, active) ->
            tx.execute(status -> pure.execute(id, productId, uomId, currencyId,
                unitPrice, minQuantity, effectiveFrom, effectiveTo, note, active));
    }

    @Bean
    public DeleteSupplierPriceListUseCase deleteSupplierPriceListUseCase(
            SupplierPriceListRepository supplierPriceListDomainRepository,
            PlatformTransactionManager txManager) {
        DeleteSupplierPriceListUseCase pure = new DeleteSupplierPriceListUseCaseImpl(
            supplierPriceListDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id) -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public FindSupplierPriceListsUseCase findSupplierPriceListsUseCase(
            SupplierPriceListRepository supplierPriceListDomainRepository,
            PlatformTransactionManager txManager) {
        FindSupplierPriceListsUseCase pure = new FindSupplierPriceListsUseCaseImpl(
            supplierPriceListDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public GetSupplierPriceListEditViewUseCase getSupplierPriceListEditViewUseCase(
            SupplierPriceListRepository supplierPriceListDomainRepository,
            PlatformTransactionManager txManager) {
        GetSupplierPriceListEditViewUseCase pure = new GetSupplierPriceListEditViewUseCaseImpl(
            supplierPriceListDomainRepository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (id) -> tx.execute(status -> pure.execute(id));
    }
}
```

- [ ] Step 6: Commit

```
feat(purchasing): add supplier price list infrastructure layer
```

---

### Task 6: Supplier Price List — Web Layer (with tests)

**Goal:** Create DTOs, web mapper, controller, lookup controller, controller tests, and Thymeleaf templates.

- [ ] Step 1: Write controller test (RED)

**File:** `src/test/java/com/solusi/erp/purchasing/supplierpricelist/web/controller/SupplierPriceListControllerTest.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.web.controller;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.command.*;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.query.*;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListSummaryResponse;
import com.solusi.erp.purchasing.supplierpricelist.web.mapper.SupplierPriceListWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SupplierPriceListController Tests")
public class SupplierPriceListControllerTest {

    @Test
    @DisplayName("list returns list view with page model")
    public void listShouldReturnListViewAndModel() {
        CreateSupplierPriceListUseCase createUc = mock(CreateSupplierPriceListUseCase.class);
        UpdateSupplierPriceListUseCase updateUc = mock(UpdateSupplierPriceListUseCase.class);
        DeleteSupplierPriceListUseCase deleteUc = mock(DeleteSupplierPriceListUseCase.class);
        FindSupplierPriceListsUseCase findUc = mock(FindSupplierPriceListsUseCase.class);
        GetSupplierPriceListEditViewUseCase editViewUc = mock(GetSupplierPriceListEditViewUseCase.class);
        SupplierPriceListWebMapper webMapper = mock(SupplierPriceListWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        SupplierPriceListController controller = new SupplierPriceListController(
            createUc, updateUc, deleteUc, findUc, editViewUc, webMapper, messageSource
        );

        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        SupplierPriceList domainSpl = new SupplierPriceList(metadata, "SPL-001",
            1L, 2L, 3L, 4L,
            new BigDecimal("100.0000"), new BigDecimal("1.0000"),
            LocalDate.of(2026, 7, 1), null, null, true);

        com.solusi.erp.core.domain.model.Page<SupplierPriceList> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(domainSpl), 0, 20, 1L);
        when(findUc.execute(any(), any())).thenReturn(domainPage);

        SupplierPriceListSummaryResponse summary = new SupplierPriceListSummaryResponse();
        summary.setId(1L);
        summary.setCode("SPL-001");
        when(webMapper.toSummaryResponse(any(SupplierPriceList.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable =
            org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("purchasing/supplier-price-lists/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(SupplierPriceListSummaryResponse.class);
        assertEquals("SPL-001", ((SupplierPriceListSummaryResponse) first).getCode());
    }

    @Test
    @DisplayName("showCreateForm returns form view with empty request")
    public void showCreateFormShouldReturnFormView() {
        CreateSupplierPriceListUseCase createUc = mock(CreateSupplierPriceListUseCase.class);
        UpdateSupplierPriceListUseCase updateUc = mock(UpdateSupplierPriceListUseCase.class);
        DeleteSupplierPriceListUseCase deleteUc = mock(DeleteSupplierPriceListUseCase.class);
        FindSupplierPriceListsUseCase findUc = mock(FindSupplierPriceListsUseCase.class);
        GetSupplierPriceListEditViewUseCase editViewUc = mock(GetSupplierPriceListEditViewUseCase.class);
        SupplierPriceListWebMapper webMapper = mock(SupplierPriceListWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        SupplierPriceListController controller = new SupplierPriceListController(
            createUc, updateUc, deleteUc, findUc, editViewUc, webMapper, messageSource
        );

        Model model = new ExtendedModelMap();
        String view = controller.showCreateForm(model);

        assertEquals("purchasing/supplier-price-lists/form", view);
        assertThat(model.getAttribute("splRequest")).isNotNull();
    }
}
```

- [ ] Step 2: Run test — expect FAIL (classes do not exist yet)

```bash
mvn test -pl . -Dtest="com.solusi.erp.purchasing.supplierpricelist.web.controller.SupplierPriceListControllerTest" -Dsurefire.failIfNoSpecifiedTests=false
```

- [ ] Step 3: Create DTOs

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/dto/SupplierPriceListSaveRequest.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SupplierPriceListSaveRequest extends BaseAuditResponse {

    @Size(max = 30, message = "{label.spl.code} {validation.size.suffix}")
    private String code;

    @NotNull(message = "{label.spl.supplier} {validation.notblank.suffix}")
    private Long supplierId;

    @NotNull(message = "{label.spl.product} {validation.notblank.suffix}")
    private Long productId;

    @NotNull(message = "{label.spl.uom} {validation.notblank.suffix}")
    private Long uomId;

    @NotNull(message = "{label.spl.currency} {validation.notblank.suffix}")
    private Long currencyId;

    @NotNull(message = "{label.spl.unitPrice} {validation.notblank.suffix}")
    @DecimalMin(value = "0.0001", message = "{msg.error.spl.price.positive}")
    private BigDecimal unitPrice;

    @NotNull(message = "{label.spl.minQuantity} {validation.notblank.suffix}")
    private BigDecimal minQuantity;

    @NotNull(message = "{label.spl.effectiveFrom} {validation.notblank.suffix}")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private String note;

    private boolean active = true;

    private String supplierName;
    private String productName;
    private String uomName;
    private String currencyName;
}
```

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/dto/SupplierPriceListSummaryResponse.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SupplierPriceListSummaryResponse extends BaseAuditResponse {
    private String code;
    private Long supplierId;
    private String supplierName;
    private Long productId;
    private String productName;
    private Long uomId;
    private String uomName;
    private Long currencyId;
    private String currencyName;
    private BigDecimal unitPrice;
    private BigDecimal minQuantity;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String note;
    private boolean active;
}
```

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/dto/SupplierPriceListDetailResponse.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SupplierPriceListDetailResponse extends BaseAuditResponse {
    private String code;
    private Long supplierId;
    private String supplierName;
    private Long productId;
    private String productName;
    private Long uomId;
    private String uomName;
    private Long currencyId;
    private String currencyName;
    private BigDecimal unitPrice;
    private BigDecimal minQuantity;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String note;
    private boolean active;
}
```

- [ ] Step 4: Create Web Mapper

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/mapper/SupplierPriceListWebMapper.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListDetailResponse;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListSaveRequest;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SupplierPriceListWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    public abstract SupplierPriceListSummaryResponse toSummaryResponse(SupplierPriceList domain);

    public abstract SupplierPriceListDetailResponse toDetailResponse(SupplierPriceList domain);

    public abstract SupplierPriceListSaveRequest toSaveRequest(SupplierPriceList domain);

    @AfterMapping
    protected void mapAuditFields(SupplierPriceList domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }
}
```

- [ ] Step 5: Create Controller

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/controller/SupplierPriceListController.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.command.*;
import com.solusi.erp.purchasing.supplierpricelist.application.usecase.query.*;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.*;
import com.solusi.erp.purchasing.supplierpricelist.web.mapper.SupplierPriceListWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/purchasing/supplier-price-lists")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class SupplierPriceListController {

    private final CreateSupplierPriceListUseCase createSupplierPriceListUseCase;
    private final UpdateSupplierPriceListUseCase updateSupplierPriceListUseCase;
    private final DeleteSupplierPriceListUseCase deleteSupplierPriceListUseCase;
    private final FindSupplierPriceListsUseCase findSupplierPriceListsUseCase;
    private final GetSupplierPriceListEditViewUseCase getSupplierPriceListEditViewUseCase;
    private final SupplierPriceListWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('SPL_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<SupplierPriceList> domainPage =
            findSupplierPriceListsUseCase.execute(keyword, domainPageable);

        List<SupplierPriceListSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<SupplierPriceListSummaryResponse> springPage =
            new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "purchasing/supplier-price-lists/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('SPL_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("splRequest", new SupplierPriceListSaveRequest());
        return "purchasing/supplier-price-lists/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('SPL_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<SupplierPriceListDetailResponse>> create(
            @Valid @RequestBody SupplierPriceListSaveRequest request) {
        SupplierPriceList domain = createSupplierPriceListUseCase.execute(
            request.getSupplierId(), request.getProductId(),
            request.getUomId(), request.getCurrencyId(),
            request.getUnitPrice(), request.getMinQuantity(),
            request.getEffectiveFrom(), request.getEffectiveTo(),
            request.getNote(), request.isActive()
        );
        SupplierPriceListDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('SPL_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        SupplierPriceList domain = getSupplierPriceListEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Supplier price list not found"));
        model.addAttribute("splRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        return "purchasing/supplier-price-lists/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('SPL_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<SupplierPriceListDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierPriceListSaveRequest request) {
        SupplierPriceList domain = updateSupplierPriceListUseCase.execute(
            id, request.getProductId(), request.getUomId(), request.getCurrencyId(),
            request.getUnitPrice(), request.getMinQuantity(),
            request.getEffectiveFrom(), request.getEffectiveTo(),
            request.getNote(), request.isActive()
        );
        SupplierPriceListDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SPL_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteSupplierPriceListUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
```

- [ ] Step 6: Create Lookup Controller

**File:** `src/main/java/com/solusi/erp/purchasing/supplierpricelist/web/controller/SupplierPriceListLookupController.java`

```java
package com.solusi.erp.purchasing.supplierpricelist.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lookup/purchasing/supplier-price-lists")
@RequiredArgsConstructor
public class SupplierPriceListLookupController {

    private final SupplierPriceListRepository supplierPriceListDomainRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('LOOKUP_SUPPLIER-PRICE-LIST')")
    public List<LookupDto> search(@RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "10") int limit) {
        return supplierPriceListDomainRepository.search(keyword, limit).stream()
            .map(spl -> new LookupDto(
                spl.getId(),
                spl.getCode(),
                "Price: " + spl.getUnitPrice().toPlainString(),
                Map.of(
                    "unitPrice", spl.getUnitPrice(),
                    "supplierId", spl.getSupplierId(),
                    "productId", spl.getProductId(),
                    "uomId", spl.getUomId(),
                    "currencyId", spl.getCurrencyId()
                )
            ))
            .collect(Collectors.toList());
    }
}
```

- [ ] Step 7: Create Thymeleaf templates

**File:** `src/main/resources/templates/purchasing/supplier-price-lists/list.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<head th:replace="~{layout/master :: head(#{label.spl.title})}">
</head>

<body th:replace="~{layout/master :: layout(~{:: .spl-list-content}, ~{:: #page-specific-scripts})}">
    <div class="spl-list-content">

        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title" th:text="#{label.spl.title}">Supplier Price List</h2>
                        <div class="text-secondary mt-1" th:text="#{label.spl.subtitle}">
                            Manage supplier product price agreements.
                        </div>
                    </div>
                    <div class="col-auto ms-auto d-print-none" sec:authorize="hasAuthority('SPL_CREATE')">
                        <div class="btn-list">
                            <a th:href="@{/purchasing/supplier-price-lists/create}" class="btn btn-primary d-none d-sm-inline-block">
                                <i class="ti ti-plus icon"></i>
                                <span th:text="#{label.spl.add}">Add Price List</span>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">

                <div th:replace="~{fragments/alerts :: success}"></div>
                <div th:replace="~{fragments/alerts :: error}"></div>

                <div class="card">
                    <div class="card-body border-bottom py-3">
                        <div class="d-flex">
                            <div class="ms-auto text-secondary">
                                <form th:action="@{/purchasing/supplier-price-lists}" method="get" class="input-icon"
                                      hx-get="/purchasing/supplier-price-lists"
                                      hx-target="#spl-table-container"
                                      hx-trigger="keyup changed delay:500ms from:#search-input"
                                      hx-push-url="true">
                                    <input type="text" id="search-input" name="keyword" th:value="${keyword}"
                                           class="form-control form-control-sm"
                                           th:placeholder="#{label.search} + '...'">
                                    <span class="input-icon-addon">
                                        <i class="ti ti-search"></i>
                                    </span>
                                </form>
                            </div>
                        </div>
                    </div>

                    <div id="spl-table-container" th:fragment="spl-table-container"
                         hx-get="/purchasing/supplier-price-lists"
                         hx-trigger="refresh-table from:body delay:500ms"
                         hx-include="[name='keyword'], [name='page']">
                        <input type="hidden" name="page" th:value="${page.number}">

                        <div class="table-responsive">
                            <table class="table card-table table-vcenter text-nowrap datatable">
                                <thead>
                                    <tr>
                                        <th th:replace="~{fragments/table :: sortable('code', #{label.spl.column.code})}">Code</th>
                                        <th th:text="#{label.spl.column.supplier}">Supplier</th>
                                        <th th:text="#{label.spl.column.product}">Product</th>
                                        <th th:text="#{label.spl.column.unitPrice}">Unit Price</th>
                                        <th th:text="#{label.spl.column.effectiveFrom}">From</th>
                                        <th th:text="#{label.spl.column.effectiveTo}">To</th>
                                        <th th:text="#{label.spl.column.isActive}">Active</th>
                                        <th class="text-end" th:text="#{label.actions}">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr th:each="item : ${page.content}" th:id="'row-' + ${item.id}">
                                        <td><span class="font-weight-medium" th:text="${item.code}">SPL-001</span></td>
                                        <td th:text="${item.supplierName}">Supplier Name</td>
                                        <td th:text="${item.productName}">Product Name</td>
                                        <td th:text="${#numbers.formatDecimal(item.unitPrice, 1, 4)}">0.0000</td>
                                        <td th:text="${#temporals.format(item.effectiveFrom, 'dd MMM yyyy')}">01 Jul 2026</td>
                                        <td th:text="${item.effectiveTo != null ? #temporals.format(item.effectiveTo, 'dd MMM yyyy') : '-'}">-</td>
                                        <td>
                                            <span th:if="${item.active}" class="badge bg-success-lt">Active</span>
                                            <span th:unless="${item.active}" class="badge bg-secondary-lt">Inactive</span>
                                        </td>
                                        <td>
                                            <div class="btn-list flex-nowrap justify-content-end">
                                                <a th:href="@{/purchasing/supplier-price-lists/edit/{id}(id=${item.id})}"
                                                   class="btn btn-white btn-sm" sec:authorize="hasAuthority('SPL_UPDATE')">
                                                    <i class="ti ti-edit me-1"></i>
                                                    <span th:text="#{label.edit}">Edit</span>
                                                </a>
                                                <button type="button" class="btn btn-white btn-sm text-danger"
                                                        sec:authorize="hasAuthority('SPL_DELETE')"
                                                        data-bs-toggle="modal"
                                                        th:data-bs-target="'#modal-delete-' + ${item.id}">
                                                    <i class="ti ti-trash me-1"></i>
                                                    <span th:text="#{label.delete}">Delete</span>
                                                </button>
                                            </div>
                                            <div th:replace="~{fragments/modals :: delete-confirm(
                                                id='modal-delete-' + ${item.id},
                                                title=#{label.delete.confirm.title},
                                                message=#{msg.delete.confirm(${item.code})},
                                                actionUrl='/purchasing/supplier-price-lists/' + ${item.id},
                                                targetId='#row-' + ${item.id}
                                            )}"></div>
                                        </td>
                                    </tr>
                                    <tr th:if="${page.empty}">
                                        <td colspan="8" class="text-center py-4 text-secondary" th:text="#{label.spl.empty}">
                                            No supplier price list data found.
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                        <div th:replace="~{fragments/table :: pagination(${page})}"></div>
                    </div>
                </div>
            </div>
        </div>

    </div>

    <div id="page-specific-scripts" th:fragment="pageScripts">
    </div>
</body>

</html>
```

**File:** `src/main/resources/templates/purchasing/supplier-price-lists/form.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head th:replace="~{layout/master :: head(${splRequest.id == null
    ? #messages.msg('label.spl.add')
    : #messages.msg('label.spl.edit')})}">
</head>

<body th:replace="~{layout/master :: layout(~{:: .spl-form-content}, ~{:: #page-specific-scripts})}">
    <div class="spl-form-content" id="spl-form-content" th:fragment="spl-form-content">

        <div class="page-header d-print-none">
            <div class="container-xl">
                <div class="row g-2 align-items-center">
                    <div class="col">
                        <h2 class="page-title" th:text="${splRequest.id == null
                            ? #messages.msg('label.spl.add')
                            : #messages.msg('label.spl.edit')}">
                            Add Price List
                        </h2>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-body">
            <div class="container-xl">

                <form th:action="${splRequest.id == null
                        ? '/purchasing/supplier-price-lists/create'
                        : '/purchasing/supplier-price-lists/edit/' + splRequest.id}"
                    th:object="${splRequest}"
                    method="post"
                    class="card"
                    id="spl-form"
                    data-ajax-form="true"
                    data-redirect-on-success="/purchasing/supplier-price-lists">

                    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />

                    <div class="card-body">
                        <div class="alert-container"></div>

                        <div class="row">
                            <div class="col-md-6">
                                <div th:replace="~{fragments/inputs :: text(field='code', label=#{label.spl.code}, isReadonly=true, placeholder=${splRequest.id == null ? '[ Auto Generated ]' : ''})}"></div>

                                <div th:replace="~{fragments/inputs :: lookup(field='supplierId', label=#{label.spl.supplier}, required=true, lookupUrl='/api/lookup/master/parties', displayField='supplierName')}"></div>

                                <div th:replace="~{fragments/inputs :: lookup(field='productId', label=#{label.spl.product}, required=true, lookupUrl='/api/lookup/inventory/products', displayField='productName')}"></div>

                                <div th:replace="~{fragments/inputs :: lookup(field='uomId', label=#{label.spl.uom}, required=true, lookupUrl='/api/lookup/inventory/uoms', displayField='uomName')}"></div>

                                <div th:replace="~{fragments/inputs :: lookup(field='currencyId', label=#{label.spl.currency}, required=true, lookupUrl='/api/lookup/master/currencies', displayField='currencyName')}"></div>
                            </div>

                            <div class="col-md-6">
                                <div th:replace="~{fragments/inputs :: number(field='unitPrice', label=#{label.spl.unitPrice}, required=true, step='0.0001', placeholder=#{placeholder.spl.unitPrice})}"></div>

                                <div th:replace="~{fragments/inputs :: number(field='minQuantity', label=#{label.spl.minQuantity}, required=true, step='0.0001', placeholder=#{placeholder.spl.minQuantity})}"></div>

                                <div th:replace="~{fragments/inputs :: date(field='effectiveFrom', label=#{label.spl.effectiveFrom}, required=true)}"></div>

                                <div th:replace="~{fragments/inputs :: date(field='effectiveTo', label=#{label.spl.effectiveTo})}"></div>

                                <div class="mb-3">
                                    <label class="form-label" th:text="#{label.spl.note}">Note</label>
                                    <textarea th:field="*{note}" class="form-control erp-input" rows="3" th:placeholder="#{placeholder.spl.note}"></textarea>
                                </div>
                            </div>
                        </div>

                        <div th:if="${auditInfo != null and auditInfo.id != null}" th:replace="~{fragments/audit-info :: audit-info(${auditInfo})}"></div>
                    </div>

                    <div class="card-footer text-end">
                        <div class="d-flex align-items-center">
                            <div id="loading-indicator" class="spinner-border spinner-border-sm text-primary me-2" role="status" style="display: none;"></div>
                            <a th:href="@{/purchasing/supplier-price-lists}" class="btn btn-link link-secondary" th:text="#{label.cancel}">Cancel</a>
                            <button type="submit" class="btn btn-primary ms-auto">
                                <i class="ti ti-device-floppy"></i>
                                <span th:text="#{label.save}">Save</span>
                            </button>
                        </div>
                    </div>

                </form>
            </div>
        </div>

    </div>

    <div id="page-specific-scripts" th:fragment="pageScripts">
    </div>
</body>

</html>
```

- [ ] Step 8: Run tests — expect PASS

```bash
mvn test -pl . -Dtest="com.solusi.erp.purchasing.supplierpricelist.**"
```

- [ ] Step 9: Commit

```
feat(purchasing): add supplier price list web layer with controller, DTOs, templates
```

---

### Task 7: Purchase Requisition — Domain Layer (with tests)

**Goal:** Create PR domain model (aggregate root with status state machine), line entity, enums, repository interface, and comprehensive domain tests.

- [ ] Step 1: Write domain model tests (RED)

**File:** `src/test/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisitionTest.java`

```java
package com.solusi.erp.purchasing.purchaserequisition.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PurchaseRequisition Domain Model Tests")
class PurchaseRequisitionTest {

    private PurchaseRequisitionLine createValidLine() {
        return new PurchaseRequisitionLine(
            AuditMetadata.empty(), null,
            1L, new BigDecimal("10.0000"), 1L,
            LocalDate.of(2026, 8, 1),
            new BigDecimal("50.0000"), null, null, "Line note"
        );
    }

    @Nested
    @DisplayName("createNew factory method")
    class CreateNew {

        @Test
        @DisplayName("creates draft PR with valid parameters")
        void createNew_withValidParams_createsDraftPR() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-202607-00001",
                LocalDate.of(2026, 7, 14),
                1L, 2L, "IT",
                PurchaseRequisitionPriority.NORMAL,
                "Initial request",
                new ArrayList<>()
            );

            assertThat(pr.getId()).isNull();
            assertThat(pr.getCode()).isEqualTo("PR-202607-00001");
            assertThat(pr.getRequestDate()).isEqualTo(LocalDate.of(2026, 7, 14));
            assertThat(pr.getRequesterId()).isEqualTo(1L);
            assertThat(pr.getFacilityId()).isEqualTo(2L);
            assertThat(pr.getDepartment()).isEqualTo("IT");
            assertThat(pr.getPriority()).isEqualTo(PurchaseRequisitionPriority.NORMAL);
            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.DRAFT);
            assertThat(pr.getNote()).isEqualTo("Initial request");
            assertThat(pr.isActive()).isTrue();
        }

        @Test
        @DisplayName("creates PR with null optional fields")
        void createNew_withNullOptionals_succeeds() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-202607-00002",
                LocalDate.of(2026, 7, 14),
                1L, null, null,
                PurchaseRequisitionPriority.LOW,
                null,
                new ArrayList<>()
            );

            assertThat(pr.getFacilityId()).isNull();
            assertThat(pr.getDepartment()).isNull();
            assertThat(pr.getNote()).isNull();
        }
    }

    @Nested
    @DisplayName("full constructor")
    class FullConstructor {

        @Test
        @DisplayName("preserves all fields including metadata and status")
        void constructor_preservesAllFields() {
            AuditMetadata metadata = new AuditMetadata(5L, 3L, null, null, null, null);
            List<PurchaseRequisitionLine> lines = List.of(createValidLine());

            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-202607-00001",
                LocalDate.of(2026, 7, 14),
                1L, 2L, "Finance",
                PurchaseRequisitionPriority.HIGH,
                PurchaseRequisitionStatus.SUBMITTED,
                "Urgent need",
                true,
                lines
            );

            assertThat(pr.getId()).isEqualTo(5L);
            assertThat(pr.getMetadata()).isEqualTo(metadata);
            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.SUBMITTED);
            assertThat(pr.getLines()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update method")
    class Update {

        @Test
        @DisplayName("updates mutable fields when status is DRAFT")
        void update_whenDraft_updatesMutableFields() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL, "Old note", new ArrayList<>()
            );

            PurchaseRequisitionLine newLine = createValidLine();
            pr.update(
                LocalDate.of(2026, 7, 20),
                3L, "Operations",
                PurchaseRequisitionPriority.HIGH,
                "Updated note",
                List.of(newLine)
            );

            assertThat(pr.getRequestDate()).isEqualTo(LocalDate.of(2026, 7, 20));
            assertThat(pr.getFacilityId()).isEqualTo(3L);
            assertThat(pr.getDepartment()).isEqualTo("Operations");
            assertThat(pr.getPriority()).isEqualTo(PurchaseRequisitionPriority.HIGH);
            assertThat(pr.getNote()).isEqualTo("Updated note");
            assertThat(pr.getLines()).hasSize(1);
        }

        @Test
        @DisplayName("throws DomainException when status is not DRAFT")
        void update_whenNotDraft_throwsDomainException() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.SUBMITTED, "note", true, new ArrayList<>()
            );

            assertThatThrownBy(() -> pr.update(
                LocalDate.of(2026, 7, 20), 3L, "Ops",
                PurchaseRequisitionPriority.HIGH, "New", List.of()
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.update.not.draft");
        }
    }

    @Nested
    @DisplayName("submit method")
    class Submit {

        @Test
        @DisplayName("changes status from DRAFT to SUBMITTED with valid lines")
        void submit_fromDraft_changesStatusToSubmitted() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL, null,
                new ArrayList<>(List.of(createValidLine()))
            );

            pr.submit();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.SUBMITTED);
        }

        @Test
        @DisplayName("throws DomainException when no lines")
        void submit_withNoLines_throwsDomainException() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL, null, new ArrayList<>()
            );

            assertThatThrownBy(() -> pr.submit())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.submit.no.lines");
        }

        @Test
        @DisplayName("throws DomainException when status is not DRAFT")
        void submit_whenNotDraft_throwsDomainException() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.APPROVED, null, true,
                new ArrayList<>(List.of(createValidLine()))
            );

            assertThatThrownBy(() -> pr.submit())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.submit.invalid.status");
        }
    }

    @Nested
    @DisplayName("approve method")
    class Approve {

        @Test
        @DisplayName("changes status from SUBMITTED to APPROVED")
        void approve_fromSubmitted_changesStatusToApproved() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.SUBMITTED, null, true,
                new ArrayList<>(List.of(createValidLine()))
            );

            pr.approve();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.APPROVED);
        }
    }

    @Nested
    @DisplayName("reject method")
    class Reject {

        @Test
        @DisplayName("changes status from SUBMITTED to REJECTED")
        void reject_fromSubmitted_changesStatusToRejected() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.SUBMITTED, null, true,
                new ArrayList<>(List.of(createValidLine()))
            );

            pr.reject();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.REJECTED);
        }
    }

    @Nested
    @DisplayName("cancel method")
    class Cancel {

        @Test
        @DisplayName("cancels from DRAFT status")
        void cancel_fromDraft_changesStatusToCancelled() {
            PurchaseRequisition pr = PurchaseRequisition.createNew(
                "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL, null, new ArrayList<>()
            );

            pr.cancel();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.CANCELLED);
        }

        @Test
        @DisplayName("cancels from SUBMITTED status")
        void cancel_fromSubmitted_changesStatusToCancelled() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.SUBMITTED, null, true, new ArrayList<>()
            );

            pr.cancel();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.CANCELLED);
        }

        @Test
        @DisplayName("cancels from APPROVED status")
        void cancel_fromApproved_changesStatusToCancelled() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.APPROVED, null, true, new ArrayList<>()
            );

            pr.cancel();

            assertThat(pr.getStatus()).isEqualTo(PurchaseRequisitionStatus.CANCELLED);
        }

        @Test
        @DisplayName("throws DomainException when status is CONVERTED")
        void cancel_fromConverted_throwsDomainException() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.CONVERTED, null, true, new ArrayList<>()
            );

            assertThatThrownBy(() -> pr.cancel())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.cancel.invalid.status");
        }

        @Test
        @DisplayName("throws DomainException when already CANCELLED")
        void cancel_fromCancelled_throwsDomainException() {
            AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
            PurchaseRequisition pr = new PurchaseRequisition(
                metadata, "PR-001", LocalDate.of(2026, 7, 14),
                1L, 2L, "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.CANCELLED, null, true, new ArrayList<>()
            );

            assertThatThrownBy(() -> pr.cancel())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.cancel.invalid.status");
        }
    }

    @Nested
    @DisplayName("PurchaseRequisitionLine validation")
    class LineValidation {

        @Test
        @DisplayName("line with positive quantity is valid")
        void line_withPositiveQuantity_isValid() {
            PurchaseRequisitionLine line = new PurchaseRequisitionLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("5.0000"), 1L,
                null, null, null, null, null
            );

            assertThat(line.getQuantity()).isEqualByComparingTo("5.0000");
        }

        @Test
        @DisplayName("line with zero quantity throws DomainException")
        void line_withZeroQuantity_throwsDomainException() {
            assertThatThrownBy(() -> new PurchaseRequisitionLine(
                AuditMetadata.empty(), null,
                1L, BigDecimal.ZERO, 1L,
                null, null, null, null, null
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.line.quantity.positive");
        }

        @Test
        @DisplayName("line with negative quantity throws DomainException")
        void line_withNegativeQuantity_throwsDomainException() {
            assertThatThrownBy(() -> new PurchaseRequisitionLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("-1.0000"), 1L,
                null, null, null, null, null
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.line.quantity.positive");
        }
    }
}
```

- [ ] Step 2: Run tests — expect FAIL (classes do not exist yet)

```bash
mvn test -pl . -Dtest="com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionTest" -Dsurefire.failIfNoSpecifiedTests=false
```

- [ ] Step 3: Create PurchaseRequisitionPriority enum

**File:** `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisitionPriority.java`

```java
package com.solusi.erp.purchasing.purchaserequisition.domain.model;

public enum PurchaseRequisitionPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}
```

- [ ] Step 4: Create PurchaseRequisitionStatus enum

**File:** `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisitionStatus.java`

```java
package com.solusi.erp.purchasing.purchaserequisition.domain.model;

import java.util.Set;

public enum PurchaseRequisitionStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    CONVERTED,
    CANCELLED,
    REJECTED;

    private static final Set<PurchaseRequisitionStatus> CANCELLABLE =
        Set.of(DRAFT, SUBMITTED, APPROVED);

    public boolean canCancel() {
        return CANCELLABLE.contains(this);
    }

    public boolean canSubmit() {
        return this == DRAFT;
    }

    public boolean canUpdate() {
        return this == DRAFT;
    }

    public boolean canDelete() {
        return this == DRAFT;
    }
}
```

- [ ] Step 5: Create PurchaseRequisitionLine entity

**File:** `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisitionLine.java`

```java
package com.solusi.erp.purchasing.purchaserequisition.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PurchaseRequisitionLine {

    private final AuditMetadata metadata;
    private Long headerId;
    private final Long productId;
    private final BigDecimal quantity;
    private final Long uomId;
    private final LocalDate requiredDate;
    private final BigDecimal estimatedUnitPrice;
    private final Long suggestedSupplierId;
    private final Long convertedPoLineId;
    private final String note;

    public PurchaseRequisitionLine(AuditMetadata metadata, Long headerId,
                                    Long productId, BigDecimal quantity, Long uomId,
                                    LocalDate requiredDate, BigDecimal estimatedUnitPrice,
                                    Long suggestedSupplierId, Long convertedPoLineId,
                                    String note) {
        validateQuantity(quantity);
        this.metadata = metadata;
        this.headerId = headerId;
        this.productId = productId;
        this.quantity = quantity;
        this.uomId = uomId;
        this.requiredDate = requiredDate;
        this.estimatedUnitPrice = estimatedUnitPrice;
        this.suggestedSupplierId = suggestedSupplierId;
        this.convertedPoLineId = convertedPoLineId;
        this.note = note;
    }

    private static void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.pr.line.quantity.positive");
        }
    }

    public void setHeaderId(Long headerId) {
        this.headerId = headerId;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public Long getHeaderId() { return headerId; }
    public Long getProductId() { return productId; }
    public BigDecimal getQuantity() { return quantity; }
    public Long getUomId() { return uomId; }
    public LocalDate getRequiredDate() { return requiredDate; }
    public BigDecimal getEstimatedUnitPrice() { return estimatedUnitPrice; }
    public Long getSuggestedSupplierId() { return suggestedSupplierId; }
    public Long getConvertedPoLineId() { return convertedPoLineId; }
    public String getNote() { return note; }
}
```

- [ ] Step 6: Create PurchaseRequisition aggregate root

**File:** `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisition.java`

```java
package com.solusi.erp.purchasing.purchaserequisition.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PurchaseRequisition {

    private final AuditMetadata metadata;
    private final String code;
    private LocalDate requestDate;
    private final Long requesterId;
    private Long facilityId;
    private String department;
    private PurchaseRequisitionPriority priority;
    private PurchaseRequisitionStatus status;
    private String note;
    private boolean active;
    private List<PurchaseRequisitionLine> lines;

    public PurchaseRequisition(AuditMetadata metadata, String code,
                                LocalDate requestDate, Long requesterId,
                                Long facilityId, String department,
                                PurchaseRequisitionPriority priority,
                                PurchaseRequisitionStatus status,
                                String note, boolean active,
                                List<PurchaseRequisitionLine> lines) {
        this.metadata = metadata;
        this.code = code;
        this.requestDate = requestDate;
        this.requesterId = requesterId;
        this.facilityId = facilityId;
        this.department = department;
        this.priority = priority;
        this.status = status;
        this.note = note;
        this.active = active;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public static PurchaseRequisition createNew(String code, LocalDate requestDate,
                                                 Long requesterId, Long facilityId,
                                                 String department,
                                                 PurchaseRequisitionPriority priority,
                                                 String note,
                                                 List<PurchaseRequisitionLine> lines) {
        return new PurchaseRequisition(
            AuditMetadata.empty(), code, requestDate, requesterId,
            facilityId, department, priority,
            PurchaseRequisitionStatus.DRAFT,
            note, true, lines
        );
    }

    public void update(LocalDate requestDate, Long facilityId,
                       String department, PurchaseRequisitionPriority priority,
                       String note, List<PurchaseRequisitionLine> lines) {
        if (!status.canUpdate()) {
            throw new DomainException("msg.error.pr.update.not.draft");
        }
        this.requestDate = requestDate;
        this.facilityId = facilityId;
        this.department = department;
        this.priority = priority;
        this.note = note;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public void submit() {
        if (!status.canSubmit()) {
            throw new DomainException("msg.error.pr.submit.invalid.status");
        }
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.error.pr.submit.no.lines");
        }
        this.status = PurchaseRequisitionStatus.SUBMITTED;
    }

    public void approve() {
        this.status = PurchaseRequisitionStatus.APPROVED;
    }

    public void reject() {
        this.status = PurchaseRequisitionStatus.REJECTED;
    }

    public void cancel() {
        if (!status.canCancel()) {
            throw new DomainException("msg.error.pr.cancel.invalid.status");
        }
        this.status = PurchaseRequisitionStatus.CANCELLED;
    }

    public void deactivate() {
        if (!status.canDelete()) {
            throw new DomainException("msg.error.pr.delete.not.draft");
        }
        this.active = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public LocalDate getRequestDate() { return requestDate; }
    public Long getRequesterId() { return requesterId; }
    public Long getFacilityId() { return facilityId; }
    public String getDepartment() { return department; }
    public PurchaseRequisitionPriority getPriority() { return priority; }
    public PurchaseRequisitionStatus getStatus() { return status; }
    public String getNote() { return note; }
    public boolean isActive() { return active; }
    public List<PurchaseRequisitionLine> getLines() { return Collections.unmodifiableList(lines); }
}
```

- [ ] Step 7: Create PurchaseRequisitionRepository interface

**File:** `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/repository/PurchaseRequisitionRepository.java`

```java
package com.solusi.erp.purchasing.purchaserequisition.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;

import java.util.Optional;

public interface PurchaseRequisitionRepository {

    PurchaseRequisition save(PurchaseRequisition purchaseRequisition);

    Optional<PurchaseRequisition> findById(Long id);

    Page<PurchaseRequisition> findAll(String keyword, Pageable pageable);

    void deleteById(Long id);
}
```

- [ ] Step 8: Create PurchaseRequisitionEventPublisher port

**File:** `src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/port/PurchaseRequisitionEventPublisher.java`

```java
package com.solusi.erp.purchasing.purchaserequisition.domain.port;

public interface PurchaseRequisitionEventPublisher {
    void publishApprovalRequested(Long prId, String prCode, Long requesterId, Long approverId);
}
```

- [ ] Step 9: Run tests — expect PASS

```bash
mvn test -pl . -Dtest="com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionTest"
```

- [ ] Step 10: Commit

```
feat(purchasing): add purchase requisition domain model with status state machine
```

---
