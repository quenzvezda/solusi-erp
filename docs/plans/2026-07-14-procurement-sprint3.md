# Sprint 3 — Purchase Order Module: Exhaustive Implementation Plan

> **Date:** 2026-07-14
> **Module:** Procurement (Purchase Order)
> **Sprint:** 3 of Purchasing Track
> **Architecture:** Clean Architecture + DDD + CQRS
> **Framework:** Spring Boot 3 + Thymeleaf + HTMX

---

## Agentic Worker Instructions

You are an autonomous AI developer executing this plan. Follow every step exactly as written.
Each task contains complete, copy-paste-ready code. Do NOT improvise or deviate.

**Rules:**
1. Follow TDD: write the failing test FIRST, then the implementation.
2. Use conventional English commit messages. Do NOT add Co-authored-by trailers.
3. All domain and application code is pure Java — no Spring annotations.
4. Spring annotations live ONLY in infrastructure and web layers.
5. Transactions are managed via `TransactionTemplate` in the Config (Composition Root), not via `@Transactional`.
6. Use AssertJ assertions (`assertThat`, `assertThatThrownBy`) in all tests.
7. Use `@ExtendWith(MockitoExtension.class)` for unit tests — no Spring context.
8. Run `mvn clean test` after each task to verify.

---

## Goal

Implement the **Purchase Order (PO)** module end-to-end:
- Flyway migration for tables, sequences, permissions
- Domain model with status state machine and tax calculation
- Application use cases (CQRS) with comprehensive tests
- Infrastructure (JPA, MapStruct mappers, event listeners)
- Web layer (Controller, DTOs, Thymeleaf templates)
- Smoke test with Playwright

---

## Architecture Overview

```
purchasing/purchaseorder/
    domain/
        model/          -- PurchaseOrder, PurchaseOrderLine, PurchaseOrderStatus
        repository/     -- PurchaseOrderRepository interface
        port/           -- SupplierInfoProvider, AccountingPeriodChecker
    application/
        usecase/
            command/    -- Create, Update, Delete, AddLine, RemoveLine, UpdateLine,
                           Submit, Approve, Reject, Send, Cancel, ConvertPrToPo
            query/      -- FindPurchaseOrders, GetPurchaseOrderDetail, GetPurchaseOrderEditView
    infrastructure/
        persistence/    -- JPA entities, JPA repositories, MapStruct mappers
        adapter/        -- PurchaseOrderRepositoryImpl, SupplierInfoProviderImpl,
                           AccountingPeriodCheckerImpl
        config/         -- PurchaseOrderConfig (Composition Root)
        listener/       -- OnPurchaseOrderApprovedListener
    web/
        controller/     -- PurchaseOrderController
        dto/            -- Request/Response DTOs
        mapper/         -- PurchaseOrderWebMapper
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Build | Maven |
| Database | MySQL 8 (Flyway migrations) |
| ORM | JPA / Hibernate |
| Mapping | MapStruct 1.5+ |
| View | Thymeleaf + HTMX |
| Security | Spring Security (method-level @PreAuthorize) |
| Testing | JUnit 5 + Mockito + AssertJ |
| E2E | Playwright |

---

## Sprint 2 Artifacts (Already Exist — Can Reference)

| Artifact | Package |
|----------|---------|
| `SupplierPriceList` domain model | `com.solusi.erp.purchasing.supplierpricelist.domain.model` |
| `SupplierPriceListRepository` interface | `com.solusi.erp.purchasing.supplierpricelist.domain.repository` |
| `AuditMetadata` record | `com.solusi.erp.core.domain.model` |
| `Page<T>` record | `com.solusi.erp.core.domain.model` |
| `Pageable` class | `com.solusi.erp.core.domain.model` |
| `DomainException` | `com.solusi.erp.core.exception` |
| `BaseModel` JPA entity | `com.solusi.erp.core.model` |
| `BaseAuditResponse` DTO | `com.solusi.erp.core.dto` |
| `ApiResponse<T>` DTO | `com.solusi.erp.core.dto` |
| `LookupDto` record | `com.solusi.erp.core.dto` |
| `SequenceGeneratorService` | `com.solusi.erp.core.infrastructure.sequence` |
| `PageableMapper` utility | `com.solusi.erp.core.infrastructure.util` |
| `AuditMapperHelper` | `com.solusi.erp.core.mapper` |
| `HtmxResponseUtility` | `com.solusi.erp.util` |
| `ApprovalRequestedEvent` | `com.solusi.erp.core.event` |
| `ApprovalCompletedEvent` | `com.solusi.erp.core.event` |
| `Party` JPA entity (with is_pkp) | `com.solusi.erp.master.party.infrastructure.persistence` |
| `PartyJpaRepository` | `com.solusi.erp.master.party.infrastructure.persistence` |
| `AccountingPeriod` domain model | `com.solusi.erp.accounting.period.domain.model` |
| `AccountingPeriodJpaRepository` | `com.solusi.erp.accounting.period.infrastructure.persistence` |
| V46 migration (PR, SPL tables) | `src/main/resources/db/migration/V46__Add_Purchasing_Module.sql` |

---

## Complete File Structure

```
src/
  main/
    java/com/solusi/erp/purchasing/purchaseorder/
      domain/
        model/
          PurchaseOrderStatus.java
          PurchaseOrderLine.java
          PurchaseOrder.java
        repository/
          PurchaseOrderRepository.java
        port/
          SupplierInfoProvider.java
          AccountingPeriodChecker.java
      application/
        usecase/
          command/
            CreatePurchaseOrderUseCase.java
            CreatePurchaseOrderUseCaseImpl.java
            UpdatePurchaseOrderUseCase.java
            UpdatePurchaseOrderUseCaseImpl.java
            DeletePurchaseOrderUseCase.java
            DeletePurchaseOrderUseCaseImpl.java
            AddPurchaseOrderLineUseCase.java
            AddPurchaseOrderLineUseCaseImpl.java
            UpdatePurchaseOrderLineUseCase.java
            UpdatePurchaseOrderLineUseCaseImpl.java
            RemovePurchaseOrderLineUseCase.java
            RemovePurchaseOrderLineUseCaseImpl.java
            SubmitPurchaseOrderUseCase.java
            SubmitPurchaseOrderUseCaseImpl.java
            ApprovePurchaseOrderUseCase.java
            ApprovePurchaseOrderUseCaseImpl.java
            RejectPurchaseOrderUseCase.java
            RejectPurchaseOrderUseCaseImpl.java
            SendPurchaseOrderUseCase.java
            SendPurchaseOrderUseCaseImpl.java
            CancelPurchaseOrderUseCase.java
            CancelPurchaseOrderUseCaseImpl.java
            ConvertPrToPurchaseOrderUseCase.java
            ConvertPrToPurchaseOrderUseCaseImpl.java
          query/
            FindPurchaseOrdersUseCase.java
            FindPurchaseOrdersUseCaseImpl.java
            GetPurchaseOrderDetailUseCase.java
            GetPurchaseOrderDetailUseCaseImpl.java
            GetPurchaseOrderEditViewUseCase.java
            GetPurchaseOrderEditViewUseCaseImpl.java
      infrastructure/
        persistence/
          PurchaseOrderEntity.java
          PurchaseOrderLineEntity.java
          PurchaseOrderJpaRepository.java
          PurchaseOrderLineJpaRepository.java
          PurchaseOrderPersistenceMapper.java
        adapter/
          PurchaseOrderRepositoryImpl.java
          SupplierInfoProviderImpl.java
          AccountingPeriodCheckerImpl.java
        config/
          PurchaseOrderConfig.java
        listener/
          OnPurchaseOrderApprovedListener.java
      web/
        controller/
          PurchaseOrderController.java
        dto/
          PurchaseOrderSaveRequest.java
          PurchaseOrderLineSaveRequest.java
          PurchaseOrderSummaryResponse.java
          PurchaseOrderDetailResponse.java
          PurchaseOrderLineResponse.java
          SubmitApprovalRequest.java
        mapper/
          PurchaseOrderWebMapper.java
    resources/
      db/migration/
        V47__Add_Purchase_Order_Module.sql
      templates/purchasing/purchase-orders/
        list.html
        form.html
        detail.html
  test/
    java/com/solusi/erp/purchasing/purchaseorder/
      domain/model/
        PurchaseOrderStatusTest.java
        PurchaseOrderLineTest.java
        PurchaseOrderTest.java
      application/usecase/command/
        CreatePurchaseOrderUseCaseTest.java
        UpdatePurchaseOrderUseCaseTest.java
        DeletePurchaseOrderUseCaseTest.java
        AddPurchaseOrderLineUseCaseTest.java
        UpdatePurchaseOrderLineUseCaseTest.java
        RemovePurchaseOrderLineUseCaseTest.java
        SubmitPurchaseOrderUseCaseTest.java
        ApprovePurchaseOrderUseCaseTest.java
        RejectPurchaseOrderUseCaseTest.java
        SendPurchaseOrderUseCaseTest.java
        CancelPurchaseOrderUseCaseTest.java
        ConvertPrToPurchaseOrderUseCaseTest.java
      application/usecase/query/
        FindPurchaseOrdersUseCaseTest.java
        GetPurchaseOrderDetailUseCaseTest.java
        GetPurchaseOrderEditViewUseCaseTest.java
      web/controller/
        PurchaseOrderControllerTest.java
```

---

## Task 1: Flyway Migration V47 — Purchase Order Tables, Sequence, Permissions

### 1.1 Overview

Create `V47__Add_Purchase_Order_Module.sql` with:
- `pur_purchase_orders` table
- `pur_purchase_order_lines` table
- PO sequence registration
- Permission group PUR-03
- Permissions: PO_READ, PO_CREATE, PO_UPDATE, PO_DELETE, PO_SUBMIT, PO_SEND, LOOKUP_PO
- Grant all to ROLE_ADMIN

### 1.2 Migration File

- [ ] Create `src/main/resources/db/migration/V47__Add_Purchase_Order_Module.sql`

```sql
-- V47: Purchase Order Module — Tables, Sequence, Permissions

-- ============================================================
-- 1. DDL: Purchase Order (Header)
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_purchase_orders (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    code                VARCHAR(30)   NOT NULL,
    order_date          DATE          NOT NULL,
    expected_date       DATE          NOT NULL,
    supplier_id         BIGINT        NOT NULL,
    facility_id         BIGINT        NULL,
    currency_id         BIGINT        NOT NULL,
    exchange_rate       DECIMAL(19,4) NOT NULL DEFAULT 1,
    payment_term_days   INT           NOT NULL DEFAULT 30,
    pr_id               BIGINT        NULL COMMENT 'Source Purchase Requisition if converted from PR',
    status              VARCHAR(25)   NOT NULL COMMENT 'DRAFT | SUBMITTED | APPROVED | REJECTED | SENT | PARTIALLY_RECEIVED | FULLY_RECEIVED | BILLED | CLOSED | CANCELLED',
    subtotal            DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_tax           DECIMAL(19,4) NOT NULL DEFAULT 0,
    grand_total         DECIMAL(19,4) NOT NULL DEFAULT 0,
    note                TEXT          NULL,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    version             INT           NOT NULL DEFAULT 0,
    created_by_user_id  BIGINT        NULL,
    created_date        DATETIME      NULL,
    updated_by_user_id  BIGINT        NULL,
    updated_date        DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_po_code (code),
    CONSTRAINT fk_po_supplier    FOREIGN KEY (supplier_id)       REFERENCES parties(id),
    CONSTRAINT fk_po_facility    FOREIGN KEY (facility_id)       REFERENCES inv_facilities(id),
    CONSTRAINT fk_po_currency    FOREIGN KEY (currency_id)       REFERENCES currencies(id),
    CONSTRAINT fk_po_pr          FOREIGN KEY (pr_id)             REFERENCES pur_purchase_requisitions(id),
    CONSTRAINT fk_po_created_by  FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_po_updated_by  FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. DDL: Purchase Order Lines
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_purchase_order_lines (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    header_id           BIGINT        NOT NULL,
    product_id          BIGINT        NOT NULL,
    quantity            DECIMAL(19,4) NOT NULL,
    uom_id              BIGINT        NOT NULL,
    unit_price          DECIMAL(19,4) NOT NULL,
    tax_rate            DECIMAL(5,4)  NOT NULL DEFAULT 0,
    line_subtotal       DECIMAL(19,4) NOT NULL DEFAULT 0,
    line_tax            DECIMAL(19,4) NOT NULL DEFAULT 0,
    line_total          DECIMAL(19,4) NOT NULL DEFAULT 0,
    pr_line_id          BIGINT        NULL COMMENT 'Source PR line if converted from PR',
    note                TEXT          NULL,
    version             INT           NOT NULL DEFAULT 0,
    created_by_user_id  BIGINT        NULL,
    created_date        DATETIME      NULL,
    updated_by_user_id  BIGINT        NULL,
    updated_date        DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pol_header     FOREIGN KEY (header_id)          REFERENCES pur_purchase_orders(id),
    CONSTRAINT fk_pol_product    FOREIGN KEY (product_id)         REFERENCES inv_products(id),
    CONSTRAINT fk_pol_uom        FOREIGN KEY (uom_id)             REFERENCES inv_uoms(id),
    CONSTRAINT fk_pol_pr_line    FOREIGN KEY (pr_line_id)         REFERENCES pur_purchase_requisition_lines(id),
    CONSTRAINT fk_pol_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_pol_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. Sequence Registration
-- ============================================================
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by, updated_date)
VALUES ('PO', 'PO-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 'SYSTEM', NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

-- ============================================================
-- 4. Permission Group (Menu Entry)
-- ============================================================
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('PUR-03', 'Purchase Order', 'Purchase Order',
 'Pengadaan > Purchase Order', 'Procurement (Purchase) > Purchase Order',
 '/purchasing/purchase-orders', 'ti-shopping-cart',
 'Kelola purchase order (PO)', 'Manage purchase orders (PO)',
 202, 1, NOW());

-- ============================================================
-- 5. Permissions
-- ============================================================
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('PO_READ',   'Melihat daftar purchase order',                    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_CREATE', 'Membuat purchase order baru',                       1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_UPDATE', 'Mengubah purchase order',                           1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_DELETE', 'Menghapus purchase order',                          1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_SUBMIT', 'Mengajukan purchase order untuk persetujuan',       1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_SEND',   'Mengirim purchase order ke supplier',               1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('LOOKUP_PO', 'Lookup purchase order untuk autocomplete',          1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03'));

-- ============================================================
-- 6. Grant all PO permissions to ROLE_ADMIN
-- ============================================================
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('PO_READ', 'PO_CREATE', 'PO_UPDATE', 'PO_DELETE', 'PO_SUBMIT', 'PO_SEND', 'LOOKUP_PO');
```

### 1.3 Verification

- [ ] Run `mvn flyway:migrate` or start the application to verify migration applies cleanly
- [ ] Verify tables exist: `SHOW TABLES LIKE 'pur_purchase_order%';`
- [ ] Verify sequence: `SELECT * FROM system_sequences WHERE module_code = 'PO';`
- [ ] Verify permissions: `SELECT * FROM permissions WHERE name LIKE 'PO_%' OR name = 'LOOKUP_PO';`

### 1.4 Commit

```
feat(purchasing): add V47 migration for purchase order tables and permissions
```

---

## Task 2: i18n Messages for Purchase Order Module

### 2.1 Overview

Add internationalized message keys for the Purchase Order module in both English and Indonesian.

### 2.2 English Messages

- [ ] Append to `src/main/resources/messages.properties`

```properties
# ============================================================
# Purchase Order
# ============================================================
label.po=Purchase Order
label.po.title=Purchase Order Management
label.po.subtitle=Manage purchase orders for procurement.
label.po.add=Create New Purchase Order
label.po.edit=Edit Purchase Order
label.po.detail=Purchase Order Detail
label.po.code=PO Code
label.po.orderDate=Order Date
label.po.expectedDate=Expected Date
label.po.supplier=Supplier
label.po.facility=Facility
label.po.currency=Currency
label.po.exchangeRate=Exchange Rate
label.po.paymentTermDays=Payment Terms (Days)
label.po.pr=Source PR
label.po.status=Status
label.po.subtotal=Subtotal
label.po.totalTax=Total Tax
label.po.grandTotal=Grand Total
label.po.note=Note
label.po.lines=Order Lines
label.po.lines.add=Add Line
label.po.lines.empty=No order lines added yet.
label.po.line.product=Product
label.po.line.quantity=Quantity
label.po.line.uom=UOM
label.po.line.unitPrice=Unit Price
label.po.line.taxRate=Tax Rate
label.po.line.lineSubtotal=Subtotal
label.po.line.lineTax=Tax
label.po.line.lineTotal=Total
label.po.line.prLine=PR Line
label.po.line.note=Note

label.po.column.code=Code
label.po.column.orderDate=Order Date
label.po.column.supplier=Supplier
label.po.column.status=Status
label.po.column.grandTotal=Grand Total

label.po.empty=No purchase order data found.
label.po.delete.confirm.title=Confirm Delete
label.po.delete.confirm.text=Are you sure you want to delete purchase order {0}?

label.po.action.submit=Submit for Approval
label.po.action.approve=Approve
label.po.action.reject=Reject
label.po.action.send=Send to Supplier
label.po.action.cancel=Cancel Order
label.po.action.convertFromPr=Convert from PR

label.po.status.DRAFT=Draft
label.po.status.SUBMITTED=Submitted
label.po.status.APPROVED=Approved
label.po.status.REJECTED=Rejected
label.po.status.SENT=Sent
label.po.status.PARTIALLY_RECEIVED=Partially Received
label.po.status.FULLY_RECEIVED=Fully Received
label.po.status.BILLED=Billed
label.po.status.CLOSED=Closed
label.po.status.CANCELLED=Cancelled

msg.success.po.created=Purchase order {0} created successfully.
msg.success.po.updated=Purchase order {0} updated successfully.
msg.success.po.deleted=Purchase order {0} deleted successfully.
msg.success.po.submitted=Purchase order {0} submitted for approval.
msg.success.po.approved=Purchase order {0} approved.
msg.success.po.rejected=Purchase order {0} rejected.
msg.success.po.sent=Purchase order {0} sent to supplier.
msg.success.po.cancelled=Purchase order {0} cancelled.
msg.success.po.line.added=Line added to purchase order.
msg.success.po.line.updated=Purchase order line updated.
msg.success.po.line.removed=Line removed from purchase order.
msg.success.po.converted=Purchase order {0} created from PR {1}.

msg.error.po.not.found=Purchase order not found.
msg.error.po.not.editable=Purchase order can only be edited in DRAFT status.
msg.error.po.not.deletable=Purchase order can only be deleted in DRAFT status.
msg.error.po.expectedDate.before.orderDate=Expected date must be on or after order date.
msg.error.po.exchangeRate.invalid=Exchange rate must be greater than zero.
msg.error.po.submit.no.lines=Purchase order must have at least one line to submit.
msg.error.po.status.invalid.transition=Cannot transition from {0} to {1}.
msg.error.po.supplier.inactive=Supplier is not active.
msg.error.po.period.not.open=Order date is not within an open accounting period.
msg.error.po.line.not.found=Purchase order line not found.
msg.error.po.pr.not.found=Purchase requisition not found.
msg.error.po.pr.not.approved=Purchase requisition must be in APPROVED status.
msg.error.po.pr.no.matching.lines=No matching PR lines found for the selected supplier.
```

### 2.3 Indonesian Messages

- [ ] Append to `src/main/resources/messages_id.properties`

```properties
# ============================================================
# Purchase Order
# ============================================================
label.po=Purchase Order
label.po.title=Manajemen Purchase Order
label.po.subtitle=Kelola purchase order untuk pengadaan.
label.po.add=Buat Purchase Order Baru
label.po.edit=Ubah Purchase Order
label.po.detail=Detail Purchase Order
label.po.code=Kode PO
label.po.orderDate=Tanggal Order
label.po.expectedDate=Tanggal Diharapkan
label.po.supplier=Supplier
label.po.facility=Fasilitas
label.po.currency=Mata Uang
label.po.exchangeRate=Kurs
label.po.paymentTermDays=Jangka Waktu Pembayaran (Hari)
label.po.pr=PR Asal
label.po.status=Status
label.po.subtotal=Subtotal
label.po.totalTax=Total Pajak
label.po.grandTotal=Grand Total
label.po.note=Catatan
label.po.lines=Baris Pesanan
label.po.lines.add=Tambah Baris
label.po.lines.empty=Belum ada baris pesanan.
label.po.line.product=Produk
label.po.line.quantity=Jumlah
label.po.line.uom=Satuan
label.po.line.unitPrice=Harga Satuan
label.po.line.taxRate=Tarif Pajak
label.po.line.lineSubtotal=Subtotal
label.po.line.lineTax=Pajak
label.po.line.lineTotal=Total
label.po.line.prLine=Baris PR
label.po.line.note=Catatan

label.po.column.code=Kode
label.po.column.orderDate=Tanggal Order
label.po.column.supplier=Supplier
label.po.column.status=Status
label.po.column.grandTotal=Grand Total

label.po.empty=Data purchase order tidak ditemukan.
label.po.delete.confirm.title=Konfirmasi Hapus
label.po.delete.confirm.text=Apakah Anda yakin ingin menghapus purchase order {0}?

label.po.action.submit=Ajukan Persetujuan
label.po.action.approve=Setujui
label.po.action.reject=Tolak
label.po.action.send=Kirim ke Supplier
label.po.action.cancel=Batalkan Order
label.po.action.convertFromPr=Konversi dari PR

label.po.status.DRAFT=Draf
label.po.status.SUBMITTED=Diajukan
label.po.status.APPROVED=Disetujui
label.po.status.REJECTED=Ditolak
label.po.status.SENT=Terkirim
label.po.status.PARTIALLY_RECEIVED=Diterima Sebagian
label.po.status.FULLY_RECEIVED=Diterima Seluruhnya
label.po.status.BILLED=Ditagih
label.po.status.CLOSED=Ditutup
label.po.status.CANCELLED=Dibatalkan

msg.success.po.created=Purchase order {0} berhasil dibuat.
msg.success.po.updated=Purchase order {0} berhasil diperbarui.
msg.success.po.deleted=Purchase order {0} berhasil dihapus.
msg.success.po.submitted=Purchase order {0} diajukan untuk persetujuan.
msg.success.po.approved=Purchase order {0} disetujui.
msg.success.po.rejected=Purchase order {0} ditolak.
msg.success.po.sent=Purchase order {0} terkirim ke supplier.
msg.success.po.cancelled=Purchase order {0} dibatalkan.
msg.success.po.line.added=Baris ditambahkan ke purchase order.
msg.success.po.line.updated=Baris purchase order diperbarui.
msg.success.po.line.removed=Baris dihapus dari purchase order.
msg.success.po.converted=Purchase order {0} dibuat dari PR {1}.

msg.error.po.not.found=Purchase order tidak ditemukan.
msg.error.po.not.editable=Purchase order hanya dapat diubah dalam status DRAFT.
msg.error.po.not.deletable=Purchase order hanya dapat dihapus dalam status DRAFT.
msg.error.po.expectedDate.before.orderDate=Tanggal diharapkan harus sama atau setelah tanggal order.
msg.error.po.exchangeRate.invalid=Kurs harus lebih besar dari nol.
msg.error.po.submit.no.lines=Purchase order harus memiliki minimal satu baris untuk diajukan.
msg.error.po.status.invalid.transition=Tidak dapat mengubah status dari {0} ke {1}.
msg.error.po.supplier.inactive=Supplier tidak aktif.
msg.error.po.period.not.open=Tanggal order tidak dalam periode akuntansi yang terbuka.
msg.error.po.line.not.found=Baris purchase order tidak ditemukan.
msg.error.po.pr.not.found=Permintaan pembelian tidak ditemukan.
msg.error.po.pr.not.approved=Permintaan pembelian harus dalam status DISETUJUI.
msg.error.po.pr.no.matching.lines=Tidak ada baris PR yang sesuai untuk supplier yang dipilih.
```

### 2.4 Commit

```
feat(purchasing): add i18n messages for purchase order module
```

---

## Task 3: Domain Layer — Models, Repository Interface, Ports, and Tests

### 3.1 PurchaseOrderStatus Enum

This enum implements a state machine with explicit allowed transitions.

#### 3.1.1 Test First

- [ ] Create `src/test/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderStatusTest.java`

```java
package com.solusi.erp.purchasing.purchaseorder.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PurchaseOrderStatus Tests")
class PurchaseOrderStatusTest {

    @Nested
    @DisplayName("canTransitionTo")
    class CanTransitionTo {

        @Test
        @DisplayName("DRAFT can transition to SUBMITTED")
        void draft_canTransitionToSubmitted() {
            assertThat(PurchaseOrderStatus.DRAFT.canTransitionTo(PurchaseOrderStatus.SUBMITTED)).isTrue();
        }

        @Test
        @DisplayName("DRAFT can transition to CANCELLED")
        void draft_canTransitionToCancelled() {
            assertThat(PurchaseOrderStatus.DRAFT.canTransitionTo(PurchaseOrderStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("DRAFT cannot transition to APPROVED")
        void draft_cannotTransitionToApproved() {
            assertThat(PurchaseOrderStatus.DRAFT.canTransitionTo(PurchaseOrderStatus.APPROVED)).isFalse();
        }

        @Test
        @DisplayName("DRAFT cannot transition to SENT")
        void draft_cannotTransitionToSent() {
            assertThat(PurchaseOrderStatus.DRAFT.canTransitionTo(PurchaseOrderStatus.SENT)).isFalse();
        }

        @Test
        @DisplayName("DRAFT cannot transition to REJECTED")
        void draft_cannotTransitionToRejected() {
            assertThat(PurchaseOrderStatus.DRAFT.canTransitionTo(PurchaseOrderStatus.REJECTED)).isFalse();
        }

        @Test
        @DisplayName("SUBMITTED can transition to APPROVED")
        void submitted_canTransitionToApproved() {
            assertThat(PurchaseOrderStatus.SUBMITTED.canTransitionTo(PurchaseOrderStatus.APPROVED)).isTrue();
        }

        @Test
        @DisplayName("SUBMITTED can transition to REJECTED")
        void submitted_canTransitionToRejected() {
            assertThat(PurchaseOrderStatus.SUBMITTED.canTransitionTo(PurchaseOrderStatus.REJECTED)).isTrue();
        }

        @Test
        @DisplayName("SUBMITTED can transition to CANCELLED")
        void submitted_canTransitionToCancelled() {
            assertThat(PurchaseOrderStatus.SUBMITTED.canTransitionTo(PurchaseOrderStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("SUBMITTED cannot transition to SENT")
        void submitted_cannotTransitionToSent() {
            assertThat(PurchaseOrderStatus.SUBMITTED.canTransitionTo(PurchaseOrderStatus.SENT)).isFalse();
        }

        @Test
        @DisplayName("APPROVED can transition to SENT")
        void approved_canTransitionToSent() {
            assertThat(PurchaseOrderStatus.APPROVED.canTransitionTo(PurchaseOrderStatus.SENT)).isTrue();
        }

        @Test
        @DisplayName("APPROVED cannot transition to DRAFT")
        void approved_cannotTransitionToDraft() {
            assertThat(PurchaseOrderStatus.APPROVED.canTransitionTo(PurchaseOrderStatus.DRAFT)).isFalse();
        }

        @Test
        @DisplayName("SENT can transition to PARTIALLY_RECEIVED")
        void sent_canTransitionToPartiallyReceived() {
            assertThat(PurchaseOrderStatus.SENT.canTransitionTo(PurchaseOrderStatus.PARTIALLY_RECEIVED)).isTrue();
        }

        @Test
        @DisplayName("SENT can transition to FULLY_RECEIVED")
        void sent_canTransitionToFullyReceived() {
            assertThat(PurchaseOrderStatus.SENT.canTransitionTo(PurchaseOrderStatus.FULLY_RECEIVED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_RECEIVED can transition to FULLY_RECEIVED")
        void partiallyReceived_canTransitionToFullyReceived() {
            assertThat(PurchaseOrderStatus.PARTIALLY_RECEIVED.canTransitionTo(PurchaseOrderStatus.FULLY_RECEIVED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_RECEIVED can transition to CLOSED")
        void partiallyReceived_canTransitionToClosed() {
            assertThat(PurchaseOrderStatus.PARTIALLY_RECEIVED.canTransitionTo(PurchaseOrderStatus.CLOSED)).isTrue();
        }

        @Test
        @DisplayName("FULLY_RECEIVED can transition to BILLED")
        void fullyReceived_canTransitionToBilled() {
            assertThat(PurchaseOrderStatus.FULLY_RECEIVED.canTransitionTo(PurchaseOrderStatus.BILLED)).isTrue();
        }

        @Test
        @DisplayName("FULLY_RECEIVED can transition to CLOSED")
        void fullyReceived_canTransitionToClosed() {
            assertThat(PurchaseOrderStatus.FULLY_RECEIVED.canTransitionTo(PurchaseOrderStatus.CLOSED)).isTrue();
        }

        @Test
        @DisplayName("BILLED can transition to CLOSED")
        void billed_canTransitionToClosed() {
            assertThat(PurchaseOrderStatus.BILLED.canTransitionTo(PurchaseOrderStatus.CLOSED)).isTrue();
        }

        @Test
        @DisplayName("CLOSED cannot transition to anything")
        void closed_cannotTransitionToAnything() {
            for (PurchaseOrderStatus target : PurchaseOrderStatus.values()) {
                assertThat(PurchaseOrderStatus.CLOSED.canTransitionTo(target)).isFalse();
            }
        }

        @Test
        @DisplayName("CANCELLED cannot transition to anything")
        void cancelled_cannotTransitionToAnything() {
            for (PurchaseOrderStatus target : PurchaseOrderStatus.values()) {
                assertThat(PurchaseOrderStatus.CANCELLED.canTransitionTo(target)).isFalse();
            }
        }

        @Test
        @DisplayName("REJECTED cannot transition to anything")
        void rejected_cannotTransitionToAnything() {
            for (PurchaseOrderStatus target : PurchaseOrderStatus.values()) {
                assertThat(PurchaseOrderStatus.REJECTED.canTransitionTo(target)).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("isEditable")
    class IsEditable {

        @Test
        @DisplayName("DRAFT is editable")
        void draft_isEditable() {
            assertThat(PurchaseOrderStatus.DRAFT.isEditable()).isTrue();
        }

        @Test
        @DisplayName("SUBMITTED is not editable")
        void submitted_isNotEditable() {
            assertThat(PurchaseOrderStatus.SUBMITTED.isEditable()).isFalse();
        }

        @Test
        @DisplayName("APPROVED is not editable")
        void approved_isNotEditable() {
            assertThat(PurchaseOrderStatus.APPROVED.isEditable()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = PurchaseOrderStatus.class, names = {"SENT", "PARTIALLY_RECEIVED", "FULLY_RECEIVED", "BILLED", "CLOSED", "CANCELLED", "REJECTED"})
        @DisplayName("non-draft statuses are not editable")
        void nonDraft_isNotEditable(PurchaseOrderStatus status) {
            assertThat(status.isEditable()).isFalse();
        }
    }

    @Nested
    @DisplayName("isCancellable")
    class IsCancellable {

        @Test
        @DisplayName("DRAFT is cancellable")
        void draft_isCancellable() {
            assertThat(PurchaseOrderStatus.DRAFT.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("SUBMITTED is cancellable")
        void submitted_isCancellable() {
            assertThat(PurchaseOrderStatus.SUBMITTED.isCancellable()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = PurchaseOrderStatus.class, names = {"APPROVED", "SENT", "PARTIALLY_RECEIVED", "FULLY_RECEIVED", "BILLED", "CLOSED", "CANCELLED", "REJECTED"})
        @DisplayName("other statuses are not cancellable")
        void others_areNotCancellable(PurchaseOrderStatus status) {
            assertThat(status.isCancellable()).isFalse();
        }
    }
}
```

#### 3.1.2 Implementation

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderStatus.java`

```java
package com.solusi.erp.purchasing.purchaseorder.domain.model;

import java.util.EnumSet;
import java.util.Set;

public enum PurchaseOrderStatus {

    DRAFT(EnumSet.of(Lazy.SUBMITTED, Lazy.CANCELLED)),
    SUBMITTED(EnumSet.of(Lazy.APPROVED, Lazy.REJECTED, Lazy.CANCELLED)),
    APPROVED(EnumSet.of(Lazy.SENT)),
    REJECTED(EnumSet.noneOf(PurchaseOrderStatus.class)),
    SENT(EnumSet.of(Lazy.PARTIALLY_RECEIVED, Lazy.FULLY_RECEIVED)),
    PARTIALLY_RECEIVED(EnumSet.of(Lazy.FULLY_RECEIVED, Lazy.CLOSED)),
    FULLY_RECEIVED(EnumSet.of(Lazy.BILLED, Lazy.CLOSED)),
    BILLED(EnumSet.of(Lazy.CLOSED)),
    CLOSED(EnumSet.noneOf(PurchaseOrderStatus.class)),
    CANCELLED(EnumSet.noneOf(PurchaseOrderStatus.class));

    private final Set<PurchaseOrderStatus> allowedTransitions;

    PurchaseOrderStatus(Set<PurchaseOrderStatus> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransitionTo(PurchaseOrderStatus target) {
        return allowedTransitions.contains(target);
    }

    public boolean isEditable() {
        return this == DRAFT;
    }

    public boolean isCancellable() {
        return this == DRAFT || this == SUBMITTED;
    }

    /**
     * Lazy holder to work around forward-reference limitation in enum constructors.
     */
    private static final class Lazy {
        static final PurchaseOrderStatus SUBMITTED = PurchaseOrderStatus.SUBMITTED;
        static final PurchaseOrderStatus APPROVED = PurchaseOrderStatus.APPROVED;
        static final PurchaseOrderStatus REJECTED = PurchaseOrderStatus.REJECTED;
        static final PurchaseOrderStatus SENT = PurchaseOrderStatus.SENT;
        static final PurchaseOrderStatus PARTIALLY_RECEIVED = PurchaseOrderStatus.PARTIALLY_RECEIVED;
        static final PurchaseOrderStatus FULLY_RECEIVED = PurchaseOrderStatus.FULLY_RECEIVED;
        static final PurchaseOrderStatus BILLED = PurchaseOrderStatus.BILLED;
        static final PurchaseOrderStatus CLOSED = PurchaseOrderStatus.CLOSED;
        static final PurchaseOrderStatus CANCELLED = PurchaseOrderStatus.CANCELLED;
    }
}
```

**Important:** The `Lazy` inner class solves the forward-reference problem in Java enums. Enum constants are initialized top-to-bottom, so `DRAFT` cannot reference `SUBMITTED` directly in its constructor. The `Lazy` holder defers resolution until after all constants are initialized.

### 3.2 PurchaseOrderLine Value Object

#### 3.2.1 Test First

- [ ] Create `src/test/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderLineTest.java`

```java
package com.solusi.erp.purchasing.purchaseorder.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PurchaseOrderLine Tests")
class PurchaseOrderLineTest {

    private static final Long PRODUCT_ID = 10L;
    private static final Long UOM_ID = 5L;
    private static final Long PR_LINE_ID = 99L;

    @Nested
    @DisplayName("createNew — PKP supplier (11% tax)")
    class CreateNewPkp {

        @Test
        @DisplayName("calculates lineSubtotal = qty * unitPrice")
        void calculatesLineSubtotal() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("10"), UOM_ID,
                new BigDecimal("1000"), true, PR_LINE_ID, "Test note"
            );
            assertThat(line.getLineSubtotal()).isEqualByComparingTo(new BigDecimal("10000"));
        }

        @Test
        @DisplayName("calculates lineTax = lineSubtotal * 0.11 for PKP")
        void calculatesLineTaxPkp() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("10"), UOM_ID,
                new BigDecimal("1000"), true, PR_LINE_ID, "Test note"
            );
            assertThat(line.getTaxRate()).isEqualByComparingTo(new BigDecimal("0.11"));
            assertThat(line.getLineTax()).isEqualByComparingTo(new BigDecimal("1100"));
        }

        @Test
        @DisplayName("calculates lineTotal = lineSubtotal + lineTax for PKP")
        void calculatesLineTotalPkp() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("10"), UOM_ID,
                new BigDecimal("1000"), true, PR_LINE_ID, "Test note"
            );
            assertThat(line.getLineTotal()).isEqualByComparingTo(new BigDecimal("11100"));
        }

        @Test
        @DisplayName("stores productId, uomId, prLineId, note correctly")
        void storesFieldsCorrectly() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("5"), UOM_ID,
                new BigDecimal("200"), true, PR_LINE_ID, "My note"
            );
            assertThat(line.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(line.getUomId()).isEqualTo(UOM_ID);
            assertThat(line.getPrLineId()).isEqualTo(PR_LINE_ID);
            assertThat(line.getNote()).isEqualTo("My note");
        }
    }

    @Nested
    @DisplayName("createNew — Non-PKP supplier (0% tax)")
    class CreateNewNonPkp {

        @Test
        @DisplayName("taxRate is 0 for non-PKP supplier")
        void taxRateZero() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("10"), UOM_ID,
                new BigDecimal("1000"), false, null, null
            );
            assertThat(line.getTaxRate()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("lineTax is 0 for non-PKP supplier")
        void lineTaxZero() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("10"), UOM_ID,
                new BigDecimal("1000"), false, null, null
            );
            assertThat(line.getLineTax()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("lineTotal equals lineSubtotal for non-PKP supplier")
        void lineTotalEqualsSubtotal() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("10"), UOM_ID,
                new BigDecimal("1000"), false, null, null
            );
            assertThat(line.getLineTotal()).isEqualByComparingTo(new BigDecimal("10000"));
        }

        @Test
        @DisplayName("prLineId can be null")
        void prLineIdNullable() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("1"), UOM_ID,
                new BigDecimal("500"), false, null, null
            );
            assertThat(line.getPrLineId()).isNull();
        }
    }

    @Nested
    @DisplayName("createNew — edge cases")
    class CreateNewEdgeCases {

        @Test
        @DisplayName("quantity of 1 with unit price 1 produces correct totals")
        void minimalValues() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, BigDecimal.ONE, UOM_ID,
                BigDecimal.ONE, false, null, null
            );
            assertThat(line.getLineSubtotal()).isEqualByComparingTo(BigDecimal.ONE);
            assertThat(line.getLineTax()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(line.getLineTotal()).isEqualByComparingTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("large quantity and price produce correct totals")
        void largeValues() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("99999.9999"), UOM_ID,
                new BigDecimal("99999.9999"), true, null, null
            );
            BigDecimal expectedSubtotal = new BigDecimal("99999.9999").multiply(new BigDecimal("99999.9999"));
            BigDecimal expectedTax = expectedSubtotal.multiply(new BigDecimal("0.11"));
            BigDecimal expectedTotal = expectedSubtotal.add(expectedTax);
            assertThat(line.getLineSubtotal()).isEqualByComparingTo(expectedSubtotal);
            assertThat(line.getLineTax()).isEqualByComparingTo(expectedTax);
            assertThat(line.getLineTotal()).isEqualByComparingTo(expectedTotal);
        }

        @Test
        @DisplayName("fractional quantity calculates correctly")
        void fractionalQuantity() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("2.5"), UOM_ID,
                new BigDecimal("100"), true, null, null
            );
            assertThat(line.getLineSubtotal()).isEqualByComparingTo(new BigDecimal("250"));
            assertThat(line.getLineTax()).isEqualByComparingTo(new BigDecimal("27.5"));
            assertThat(line.getLineTotal()).isEqualByComparingTo(new BigDecimal("277.5"));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("update recalculates totals with new values")
        void updateRecalculates() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("10"), UOM_ID,
                new BigDecimal("1000"), true, PR_LINE_ID, "Old note"
            );
            line.update(new BigDecimal("20"), new BigDecimal("500"), true, "New note");
            assertThat(line.getQuantity()).isEqualByComparingTo(new BigDecimal("20"));
            assertThat(line.getUnitPrice()).isEqualByComparingTo(new BigDecimal("500"));
            assertThat(line.getLineSubtotal()).isEqualByComparingTo(new BigDecimal("10000"));
            assertThat(line.getLineTax()).isEqualByComparingTo(new BigDecimal("1100"));
            assertThat(line.getLineTotal()).isEqualByComparingTo(new BigDecimal("11100"));
            assertThat(line.getNote()).isEqualTo("New note");
        }

        @Test
        @DisplayName("update from PKP to non-PKP sets tax to zero")
        void updatePkpToNonPkp() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("10"), UOM_ID,
                new BigDecimal("1000"), true, null, null
            );
            assertThat(line.getTaxRate()).isEqualByComparingTo(new BigDecimal("0.11"));
            line.update(new BigDecimal("10"), new BigDecimal("1000"), false, null);
            assertThat(line.getTaxRate()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(line.getLineTax()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(line.getLineTotal()).isEqualByComparingTo(new BigDecimal("10000"));
        }

        @Test
        @DisplayName("update from non-PKP to PKP adds tax")
        void updateNonPkpToPkp() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("10"), UOM_ID,
                new BigDecimal("1000"), false, null, null
            );
            assertThat(line.getTaxRate()).isEqualByComparingTo(BigDecimal.ZERO);
            line.update(new BigDecimal("10"), new BigDecimal("1000"), true, null);
            assertThat(line.getTaxRate()).isEqualByComparingTo(new BigDecimal("0.11"));
            assertThat(line.getLineTax()).isEqualByComparingTo(new BigDecimal("1100"));
            assertThat(line.getLineTotal()).isEqualByComparingTo(new BigDecimal("11100"));
        }
    }

    @Nested
    @DisplayName("recalculate")
    class Recalculate {

        @Test
        @DisplayName("recalculate updates totals from current values")
        void recalculateUpdates() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, new BigDecimal("5"), UOM_ID,
                new BigDecimal("200"), true, null, null
            );
            assertThat(line.getLineSubtotal()).isEqualByComparingTo(new BigDecimal("1000"));
            assertThat(line.getLineTax()).isEqualByComparingTo(new BigDecimal("110"));
            assertThat(line.getLineTotal()).isEqualByComparingTo(new BigDecimal("1110"));
            line.recalculate();
            assertThat(line.getLineSubtotal()).isEqualByComparingTo(new BigDecimal("1000"));
            assertThat(line.getLineTax()).isEqualByComparingTo(new BigDecimal("110"));
            assertThat(line.getLineTotal()).isEqualByComparingTo(new BigDecimal("1110"));
        }
    }

    @Nested
    @DisplayName("getId / getMetadata")
    class MetadataAccess {

        @Test
        @DisplayName("new line has null id")
        void newLineHasNullId() {
            PurchaseOrderLine line = PurchaseOrderLine.createNew(
                PRODUCT_ID, BigDecimal.ONE, UOM_ID,
                BigDecimal.TEN, false, null, null
            );
            assertThat(line.getId()).isNull();
        }
    }
}
```

#### 3.2.2 Implementation

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderLine.java`

```java
package com.solusi.erp.purchasing.purchaseorder.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.math.BigDecimal;

public class PurchaseOrderLine {

    private static final BigDecimal PKP_TAX_RATE = new BigDecimal("0.11");

    private final AuditMetadata metadata;
    private Long productId;
    private BigDecimal quantity;
    private Long uomId;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal lineSubtotal;
    private BigDecimal lineTax;
    private BigDecimal lineTotal;
    private Long prLineId;
    private String note;

    public PurchaseOrderLine(AuditMetadata metadata, Long productId, BigDecimal quantity,
                             Long uomId, BigDecimal unitPrice, BigDecimal taxRate,
                             BigDecimal lineSubtotal, BigDecimal lineTax, BigDecimal lineTotal,
                             Long prLineId, String note) {
        this.metadata = metadata;
        this.productId = productId;
        this.quantity = quantity;
        this.uomId = uomId;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate;
        this.lineSubtotal = lineSubtotal;
        this.lineTax = lineTax;
        this.lineTotal = lineTotal;
        this.prLineId = prLineId;
        this.note = note;
    }

    public static PurchaseOrderLine createNew(Long productId, BigDecimal quantity, Long uomId,
                                               BigDecimal unitPrice, boolean supplierIsPkp,
                                               Long prLineId, String note) {
        BigDecimal taxRate = supplierIsPkp ? PKP_TAX_RATE : BigDecimal.ZERO;
        BigDecimal sub = quantity.multiply(unitPrice);
        BigDecimal tax = sub.multiply(taxRate);
        BigDecimal total = sub.add(tax);
        return new PurchaseOrderLine(AuditMetadata.empty(), productId, quantity, uomId,
            unitPrice, taxRate, sub, tax, total, prLineId, note);
    }

    public void update(BigDecimal quantity, BigDecimal unitPrice, boolean supplierIsPkp, String note) {
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.taxRate = supplierIsPkp ? PKP_TAX_RATE : BigDecimal.ZERO;
        this.note = note;
        recalculate();
    }

    public void recalculate() {
        this.lineSubtotal = quantity.multiply(unitPrice);
        this.lineTax = lineSubtotal.multiply(taxRate);
        this.lineTotal = lineSubtotal.add(lineTax);
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public Long getProductId() { return productId; }
    public BigDecimal getQuantity() { return quantity; }
    public Long getUomId() { return uomId; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTaxRate() { return taxRate; }
    public BigDecimal getLineSubtotal() { return lineSubtotal; }
    public BigDecimal getLineTax() { return lineTax; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public Long getPrLineId() { return prLineId; }
    public String getNote() { return note; }
}
```

### 3.3 PurchaseOrder Aggregate Root

#### 3.3.1 Test First

- [ ] Create `src/test/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderTest.java`

```java
package com.solusi.erp.purchasing.purchaseorder.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PurchaseOrder Tests")
class PurchaseOrderTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 14);
    private static final LocalDate TOMORROW = LocalDate.of(2026, 7, 15);
    private static final LocalDate YESTERDAY = LocalDate.of(2026, 7, 13);

    private PurchaseOrder createDraftPo() {
        return PurchaseOrder.createNew(
            "PO-202607-00001", TODAY, TOMORROW, 1L, 2L, 3L,
            new BigDecimal("1"), 30, null, "Test note"
        );
    }

    private PurchaseOrderLine createLine(BigDecimal qty, BigDecimal price, boolean pkp) {
        return PurchaseOrderLine.createNew(10L, qty, 5L, price, pkp, null, null);
    }

    @Nested
    @DisplayName("createNew")
    class CreateNew {

        @Test
        @DisplayName("creates PO in DRAFT status")
        void createsInDraft() {
            PurchaseOrder po = createDraftPo();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
        }

        @Test
        @DisplayName("sets isActive to true")
        void setsIsActive() {
            PurchaseOrder po = createDraftPo();
            assertThat(po.isActive()).isTrue();
        }

        @Test
        @DisplayName("sets code, orderDate, expectedDate, supplierId, facilityId, currencyId")
        void setsAllFields() {
            PurchaseOrder po = createDraftPo();
            assertThat(po.getCode()).isEqualTo("PO-202607-00001");
            assertThat(po.getOrderDate()).isEqualTo(TODAY);
            assertThat(po.getExpectedDate()).isEqualTo(TOMORROW);
            assertThat(po.getSupplierId()).isEqualTo(1L);
            assertThat(po.getFacilityId()).isEqualTo(2L);
            assertThat(po.getCurrencyId()).isEqualTo(3L);
            assertThat(po.getExchangeRate()).isEqualByComparingTo(BigDecimal.ONE);
            assertThat(po.getPaymentTermDays()).isEqualTo(30);
            assertThat(po.getNote()).isEqualTo("Test note");
        }

        @Test
        @DisplayName("initializes totals to zero")
        void initializesTotalsToZero() {
            PurchaseOrder po = createDraftPo();
            assertThat(po.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(po.getTotalTax()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(po.getGrandTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("initializes empty lines list")
        void initializesEmptyLines() {
            PurchaseOrder po = createDraftPo();
            assertThat(po.getLines()).isEmpty();
        }

        @Test
        @DisplayName("sets null id for new PO")
        void nullId() {
            PurchaseOrder po = createDraftPo();
            assertThat(po.getId()).isNull();
        }

        @Test
        @DisplayName("expectedDate equals orderDate is valid")
        void expectedDateEqualsOrderDate() {
            PurchaseOrder po = PurchaseOrder.createNew(
                "PO-001", TODAY, TODAY, 1L, 2L, 3L,
                BigDecimal.ONE, 30, null, null
            );
            assertThat(po.getExpectedDate()).isEqualTo(TODAY);
        }

        @Test
        @DisplayName("throws when expectedDate is before orderDate")
        void throwsWhenExpectedBeforeOrder() {
            assertThatThrownBy(() -> PurchaseOrder.createNew(
                "PO-001", TODAY, YESTERDAY, 1L, 2L, 3L,
                BigDecimal.ONE, 30, null, null
            )).isInstanceOf(DomainException.class)
              .hasMessage("msg.error.po.expectedDate.before.orderDate");
        }

        @Test
        @DisplayName("throws when exchangeRate is zero")
        void throwsWhenExchangeRateZero() {
            assertThatThrownBy(() -> PurchaseOrder.createNew(
                "PO-001", TODAY, TOMORROW, 1L, 2L, 3L,
                BigDecimal.ZERO, 30, null, null
            )).isInstanceOf(DomainException.class)
              .hasMessage("msg.error.po.exchangeRate.invalid");
        }

        @Test
        @DisplayName("throws when exchangeRate is negative")
        void throwsWhenExchangeRateNegative() {
            assertThatThrownBy(() -> PurchaseOrder.createNew(
                "PO-001", TODAY, TOMORROW, 1L, 2L, 3L,
                new BigDecimal("-1"), 30, null, null
            )).isInstanceOf(DomainException.class)
              .hasMessage("msg.error.po.exchangeRate.invalid");
        }

        @Test
        @DisplayName("prId can be null")
        void prIdNullable() {
            PurchaseOrder po = createDraftPo();
            assertThat(po.getPrId()).isNull();
        }

        @Test
        @DisplayName("prId set when provided")
        void prIdSet() {
            PurchaseOrder po = PurchaseOrder.createNew(
                "PO-001", TODAY, TOMORROW, 1L, 2L, 3L,
                BigDecimal.ONE, 30, 42L, null
            );
            assertThat(po.getPrId()).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("updateHeader")
    class UpdateHeader {

        @Test
        @DisplayName("updates header fields in DRAFT")
        void updatesInDraft() {
            PurchaseOrder po = createDraftPo();
            LocalDate newDate = LocalDate.of(2026, 8, 1);
            LocalDate newExpected = LocalDate.of(2026, 8, 15);
            po.updateHeader(newDate, newExpected, 4L, new BigDecimal("15000"), 45, "Updated note");
            assertThat(po.getOrderDate()).isEqualTo(newDate);
            assertThat(po.getExpectedDate()).isEqualTo(newExpected);
            assertThat(po.getFacilityId()).isEqualTo(4L);
            assertThat(po.getExchangeRate()).isEqualByComparingTo(new BigDecimal("15000"));
            assertThat(po.getPaymentTermDays()).isEqualTo(45);
            assertThat(po.getNote()).isEqualTo("Updated note");
        }

        @Test
        @DisplayName("throws when not in DRAFT")
        void throwsWhenNotDraft() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(BigDecimal.ONE, BigDecimal.TEN, false));
            po.submit();
            assertThatThrownBy(() -> po.updateHeader(TODAY, TOMORROW, 4L, BigDecimal.ONE, 30, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.not.editable");
        }

        @Test
        @DisplayName("throws when expectedDate before orderDate")
        void throwsWhenExpectedBeforeOrder() {
            PurchaseOrder po = createDraftPo();
            assertThatThrownBy(() -> po.updateHeader(TODAY, YESTERDAY, 4L, BigDecimal.ONE, 30, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.expectedDate.before.orderDate");
        }

        @Test
        @DisplayName("throws when exchangeRate is zero")
        void throwsWhenExchangeRateZero() {
            PurchaseOrder po = createDraftPo();
            assertThatThrownBy(() -> po.updateHeader(TODAY, TOMORROW, 4L, BigDecimal.ZERO, 30, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.exchangeRate.invalid");
        }
    }

    @Nested
    @DisplayName("addLine / removeLine")
    class LineManagement {

        @Test
        @DisplayName("addLine adds line and recalculates totals")
        void addLineRecalculates() {
            PurchaseOrder po = createDraftPo();
            PurchaseOrderLine line = createLine(new BigDecimal("10"), new BigDecimal("100"), false);
            po.addLine(line);
            assertThat(po.getLines()).hasSize(1);
            assertThat(po.getSubtotal()).isEqualByComparingTo(new BigDecimal("1000"));
            assertThat(po.getTotalTax()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(po.getGrandTotal()).isEqualByComparingTo(new BigDecimal("1000"));
        }

        @Test
        @DisplayName("addLine with PKP tax recalculates correctly")
        void addLinePkpRecalculates() {
            PurchaseOrder po = createDraftPo();
            PurchaseOrderLine line = createLine(new BigDecimal("10"), new BigDecimal("100"), true);
            po.addLine(line);
            assertThat(po.getSubtotal()).isEqualByComparingTo(new BigDecimal("1000"));
            assertThat(po.getTotalTax()).isEqualByComparingTo(new BigDecimal("110"));
            assertThat(po.getGrandTotal()).isEqualByComparingTo(new BigDecimal("1110"));
        }

        @Test
        @DisplayName("multiple addLine sums totals")
        void multipleAddLineSumsTotals() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(new BigDecimal("10"), new BigDecimal("100"), false));
            po.addLine(createLine(new BigDecimal("5"), new BigDecimal("200"), false));
            assertThat(po.getLines()).hasSize(2);
            assertThat(po.getSubtotal()).isEqualByComparingTo(new BigDecimal("2000"));
            assertThat(po.getGrandTotal()).isEqualByComparingTo(new BigDecimal("2000"));
        }

        @Test
        @DisplayName("addLine throws when not in DRAFT")
        void addLineThrowsWhenNotDraft() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(BigDecimal.ONE, BigDecimal.TEN, false));
            po.submit();
            assertThatThrownBy(() -> po.addLine(createLine(BigDecimal.ONE, BigDecimal.TEN, false)))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.not.editable");
        }

        @Test
        @DisplayName("removeLine removes by id and recalculates")
        void removeLineRecalculates() {
            AuditMetadata meta1 = new AuditMetadata(1L, 1L, LocalDateTime.now(), 1L, LocalDateTime.now(), 1L);
            PurchaseOrderLine line1 = new PurchaseOrderLine(meta1, 10L, new BigDecimal("10"), 5L,
                new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("1000"),
                BigDecimal.ZERO, new BigDecimal("1000"), null, null);

            AuditMetadata meta2 = new AuditMetadata(2L, 1L, LocalDateTime.now(), 1L, LocalDateTime.now(), 1L);
            PurchaseOrderLine line2 = new PurchaseOrderLine(meta2, 11L, new BigDecimal("5"), 5L,
                new BigDecimal("200"), BigDecimal.ZERO, new BigDecimal("1000"),
                BigDecimal.ZERO, new BigDecimal("1000"), null, null);

            PurchaseOrder po = createDraftPo();
            po.addLine(line1);
            po.addLine(line2);
            assertThat(po.getLines()).hasSize(2);
            assertThat(po.getSubtotal()).isEqualByComparingTo(new BigDecimal("2000"));

            po.removeLine(1L);
            assertThat(po.getLines()).hasSize(1);
            assertThat(po.getSubtotal()).isEqualByComparingTo(new BigDecimal("1000"));
        }

        @Test
        @DisplayName("removeLine throws when not in DRAFT")
        void removeLineThrowsWhenNotDraft() {
            AuditMetadata meta = new AuditMetadata(1L, 1L, LocalDateTime.now(), 1L, LocalDateTime.now(), 1L);
            PurchaseOrderLine line = new PurchaseOrderLine(meta, 10L, new BigDecimal("10"), 5L,
                new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("1000"),
                BigDecimal.ZERO, new BigDecimal("1000"), null, null);
            PurchaseOrder po = createDraftPo();
            po.addLine(line);
            po.submit();
            assertThatThrownBy(() -> po.removeLine(1L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.not.editable");
        }

        @Test
        @DisplayName("removeLine throws when line not found")
        void removeLineThrowsWhenNotFound() {
            PurchaseOrder po = createDraftPo();
            assertThatThrownBy(() -> po.removeLine(999L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.line.not.found");
        }
    }

    @Nested
    @DisplayName("submit")
    class Submit {

        @Test
        @DisplayName("transitions from DRAFT to SUBMITTED")
        void draftToSubmitted() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(BigDecimal.ONE, BigDecimal.TEN, false));
            po.submit();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.SUBMITTED);
        }

        @Test
        @DisplayName("throws when no lines")
        void throwsWhenNoLines() {
            PurchaseOrder po = createDraftPo();
            assertThatThrownBy(po::submit)
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.submit.no.lines");
        }

        @Test
        @DisplayName("throws when not in DRAFT")
        void throwsWhenNotDraft() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(BigDecimal.ONE, BigDecimal.TEN, false));
            po.submit();
            assertThatThrownBy(po::submit)
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.status.invalid.transition");
        }
    }

    @Nested
    @DisplayName("approve")
    class Approve {

        @Test
        @DisplayName("transitions from SUBMITTED to APPROVED")
        void submittedToApproved() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(BigDecimal.ONE, BigDecimal.TEN, false));
            po.submit();
            po.approve();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.APPROVED);
        }

        @Test
        @DisplayName("throws when in DRAFT")
        void throwsWhenDraft() {
            PurchaseOrder po = createDraftPo();
            assertThatThrownBy(po::approve)
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.status.invalid.transition");
        }
    }

    @Nested
    @DisplayName("reject")
    class Reject {

        @Test
        @DisplayName("transitions from SUBMITTED to REJECTED")
        void submittedToRejected() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(BigDecimal.ONE, BigDecimal.TEN, false));
            po.submit();
            po.reject();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.REJECTED);
        }

        @Test
        @DisplayName("throws when in DRAFT")
        void throwsWhenDraft() {
            PurchaseOrder po = createDraftPo();
            assertThatThrownBy(po::reject)
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.status.invalid.transition");
        }
    }

    @Nested
    @DisplayName("send")
    class Send {

        @Test
        @DisplayName("transitions from APPROVED to SENT")
        void approvedToSent() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(BigDecimal.ONE, BigDecimal.TEN, false));
            po.submit();
            po.approve();
            po.send();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.SENT);
        }

        @Test
        @DisplayName("throws when in DRAFT")
        void throwsWhenDraft() {
            PurchaseOrder po = createDraftPo();
            assertThatThrownBy(po::send)
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.status.invalid.transition");
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("cancels from DRAFT")
        void cancelsFromDraft() {
            PurchaseOrder po = createDraftPo();
            po.cancel();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
            assertThat(po.isActive()).isFalse();
        }

        @Test
        @DisplayName("cancels from SUBMITTED")
        void cancelsFromSubmitted() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(BigDecimal.ONE, BigDecimal.TEN, false));
            po.submit();
            po.cancel();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
            assertThat(po.isActive()).isFalse();
        }

        @Test
        @DisplayName("throws when in APPROVED")
        void throwsWhenApproved() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(BigDecimal.ONE, BigDecimal.TEN, false));
            po.submit();
            po.approve();
            assertThatThrownBy(po::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.status.invalid.transition");
        }
    }

    @Nested
    @DisplayName("markPartiallyReceived / markFullyReceived / markBilled / close")
    class ReceivingAndClosing {

        private PurchaseOrder sentPo() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(BigDecimal.ONE, BigDecimal.TEN, false));
            po.submit();
            po.approve();
            po.send();
            return po;
        }

        @Test
        @DisplayName("markPartiallyReceived from SENT")
        void partialFromSent() {
            PurchaseOrder po = sentPo();
            po.markPartiallyReceived();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }

        @Test
        @DisplayName("markFullyReceived from SENT")
        void fullyFromSent() {
            PurchaseOrder po = sentPo();
            po.markFullyReceived();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.FULLY_RECEIVED);
        }

        @Test
        @DisplayName("markFullyReceived from PARTIALLY_RECEIVED")
        void fullyFromPartial() {
            PurchaseOrder po = sentPo();
            po.markPartiallyReceived();
            po.markFullyReceived();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.FULLY_RECEIVED);
        }

        @Test
        @DisplayName("markBilled from FULLY_RECEIVED")
        void billedFromFullyReceived() {
            PurchaseOrder po = sentPo();
            po.markFullyReceived();
            po.markBilled();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.BILLED);
        }

        @Test
        @DisplayName("close from BILLED")
        void closeFromBilled() {
            PurchaseOrder po = sentPo();
            po.markFullyReceived();
            po.markBilled();
            po.close();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.CLOSED);
        }

        @Test
        @DisplayName("close from PARTIALLY_RECEIVED (force close)")
        void closeFromPartiallyReceived() {
            PurchaseOrder po = sentPo();
            po.markPartiallyReceived();
            po.close();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.CLOSED);
        }

        @Test
        @DisplayName("close from FULLY_RECEIVED")
        void closeFromFullyReceived() {
            PurchaseOrder po = sentPo();
            po.markFullyReceived();
            po.close();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.CLOSED);
        }

        @Test
        @DisplayName("markPartiallyReceived throws when DRAFT")
        void partialThrowsFromDraft() {
            PurchaseOrder po = createDraftPo();
            assertThatThrownBy(po::markPartiallyReceived)
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.status.invalid.transition");
        }

        @Test
        @DisplayName("markBilled throws when SENT")
        void billedThrowsFromSent() {
            PurchaseOrder po = sentPo();
            assertThatThrownBy(po::markBilled)
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.status.invalid.transition");
        }
    }

    @Nested
    @DisplayName("recalculateTotals")
    class RecalculateTotals {

        @Test
        @DisplayName("sums line subtotals, taxes, and totals")
        void sumsLineTotals() {
            PurchaseOrder po = createDraftPo();
            po.addLine(createLine(new BigDecimal("10"), new BigDecimal("100"), true));
            po.addLine(createLine(new BigDecimal("5"), new BigDecimal("200"), false));

            assertThat(po.getSubtotal()).isEqualByComparingTo(new BigDecimal("2000"));
            assertThat(po.getTotalTax()).isEqualByComparingTo(new BigDecimal("110"));
            assertThat(po.getGrandTotal()).isEqualByComparingTo(new BigDecimal("2110"));
        }

        @Test
        @DisplayName("zero lines produce zero totals")
        void zeroLinesZeroTotals() {
            PurchaseOrder po = createDraftPo();
            assertThat(po.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(po.getTotalTax()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(po.getGrandTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
```

#### 3.3.2 Implementation

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrder.java`

```java
package com.solusi.erp.purchasing.purchaseorder.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrder {

    private final AuditMetadata metadata;
    private String code;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private Long supplierId;
    private Long facilityId;
    private Long currencyId;
    private BigDecimal exchangeRate;
    private int paymentTermDays;
    private Long prId;
    private PurchaseOrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;
    private String note;
    private boolean active;
    private List<PurchaseOrderLine> lines;

    public PurchaseOrder(AuditMetadata metadata, String code, LocalDate orderDate,
                         LocalDate expectedDate, Long supplierId, Long facilityId,
                         Long currencyId, BigDecimal exchangeRate, int paymentTermDays,
                         Long prId, PurchaseOrderStatus status, BigDecimal subtotal,
                         BigDecimal totalTax, BigDecimal grandTotal, String note,
                         boolean active, List<PurchaseOrderLine> lines) {
        this.metadata = metadata;
        this.code = code;
        this.orderDate = orderDate;
        this.expectedDate = expectedDate;
        this.supplierId = supplierId;
        this.facilityId = facilityId;
        this.currencyId = currencyId;
        this.exchangeRate = exchangeRate;
        this.paymentTermDays = paymentTermDays;
        this.prId = prId;
        this.status = status;
        this.subtotal = subtotal;
        this.totalTax = totalTax;
        this.grandTotal = grandTotal;
        this.note = note;
        this.active = active;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public static PurchaseOrder createNew(String code, LocalDate orderDate, LocalDate expectedDate,
                                           Long supplierId, Long facilityId, Long currencyId,
                                           BigDecimal exchangeRate, int paymentTermDays,
                                           Long prId, String note) {
        validateDates(orderDate, expectedDate);
        validateExchangeRate(exchangeRate);
        return new PurchaseOrder(AuditMetadata.empty(), code, orderDate, expectedDate,
            supplierId, facilityId, currencyId, exchangeRate, paymentTermDays, prId,
            PurchaseOrderStatus.DRAFT, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            note, true, new ArrayList<>());
    }

    public void updateHeader(LocalDate orderDate, LocalDate expectedDate, Long facilityId,
                              BigDecimal exchangeRate, int paymentTermDays, String note) {
        requireEditable();
        validateDates(orderDate, expectedDate);
        validateExchangeRate(exchangeRate);
        this.orderDate = orderDate;
        this.expectedDate = expectedDate;
        this.facilityId = facilityId;
        this.exchangeRate = exchangeRate;
        this.paymentTermDays = paymentTermDays;
        this.note = note;
    }

    public void addLine(PurchaseOrderLine line) {
        requireEditable();
        this.lines.add(line);
        recalculateTotals();
    }

    public void removeLine(Long lineId) {
        requireEditable();
        boolean removed = this.lines.removeIf(l -> l.getId() != null && l.getId().equals(lineId));
        if (!removed) {
            throw new DomainException("msg.error.po.line.not.found");
        }
        recalculateTotals();
    }

    public void submit() {
        if (lines.isEmpty()) {
            throw new DomainException("msg.error.po.submit.no.lines");
        }
        transitionTo(PurchaseOrderStatus.SUBMITTED);
    }

    public void approve() {
        transitionTo(PurchaseOrderStatus.APPROVED);
    }

    public void reject() {
        transitionTo(PurchaseOrderStatus.REJECTED);
    }

    public void send() {
        transitionTo(PurchaseOrderStatus.SENT);
    }

    public void cancel() {
        transitionTo(PurchaseOrderStatus.CANCELLED);
        this.active = false;
    }

    public void markPartiallyReceived() {
        transitionTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);
    }

    public void markFullyReceived() {
        transitionTo(PurchaseOrderStatus.FULLY_RECEIVED);
    }

    public void markBilled() {
        transitionTo(PurchaseOrderStatus.BILLED);
    }

    public void close() {
        transitionTo(PurchaseOrderStatus.CLOSED);
    }

    public void recalculateTotals() {
        this.subtotal = lines.stream()
            .map(PurchaseOrderLine::getLineSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalTax = lines.stream()
            .map(PurchaseOrderLine::getLineTax)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.grandTotal = lines.stream()
            .map(PurchaseOrderLine::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void transitionTo(PurchaseOrderStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new DomainException("msg.error.po.status.invalid.transition", status.name(), target.name());
        }
        this.status = target;
    }

    private void requireEditable() {
        if (!status.isEditable()) {
            throw new DomainException("msg.error.po.not.editable");
        }
    }

    private static void validateDates(LocalDate orderDate, LocalDate expectedDate) {
        if (expectedDate.isBefore(orderDate)) {
            throw new DomainException("msg.error.po.expectedDate.before.orderDate");
        }
    }

    private static void validateExchangeRate(BigDecimal exchangeRate) {
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.po.exchangeRate.invalid");
        }
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public LocalDate getOrderDate() { return orderDate; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public Long getSupplierId() { return supplierId; }
    public Long getFacilityId() { return facilityId; }
    public Long getCurrencyId() { return currencyId; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public int getPaymentTermDays() { return paymentTermDays; }
    public Long getPrId() { return prId; }
    public PurchaseOrderStatus getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTotalTax() { return totalTax; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public String getNote() { return note; }
    public boolean isActive() { return active; }
    public List<PurchaseOrderLine> getLines() { return List.copyOf(lines); }
}
```

### 3.4 PurchaseOrderRepository Interface

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/repository/PurchaseOrderRepository.java`

```java
package com.solusi.erp.purchasing.purchaseorder.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository {

    PurchaseOrder save(PurchaseOrder purchaseOrder);

    Optional<PurchaseOrder> findById(Long id);

    Optional<PurchaseOrder> findByIdWithLines(Long id);

    Page<PurchaseOrder> findAll(String keyword, PurchaseOrderStatus status, Pageable pageable);

    List<PurchaseOrder> search(String keyword, int limit);

    void delete(Long id);
}
```

### 3.5 SupplierInfoProvider Port

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/port/SupplierInfoProvider.java`

```java
package com.solusi.erp.purchasing.purchaseorder.domain.port;

public interface SupplierInfoProvider {

    boolean isActive(Long supplierId);

    boolean isPkp(Long supplierId);

    String getSupplierName(Long supplierId);
}
```

### 3.6 AccountingPeriodChecker Port

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/port/AccountingPeriodChecker.java`

```java
package com.solusi.erp.purchasing.purchaseorder.domain.port;

import java.time.LocalDate;

public interface AccountingPeriodChecker {

    boolean isDateInOpenPeriod(LocalDate date);
}
```

### 3.7 Commit

```
feat(purchasing): add purchase order domain layer with status state machine and tax calculation
```

---

## Task 4: Application Layer — Use Cases and Tests

Each use case follows the pattern: functional interface + pure Java implementation.
All dependencies are injected via constructor. No Spring annotations.

### 4.1 CreatePurchaseOrderUseCase

#### 4.1.1 Interface

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/application/usecase/command/CreatePurchaseOrderUseCase.java`

```java
package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;

import java.math.BigDecimal;
import java.time.LocalDate;

@FunctionalInterface
public interface CreatePurchaseOrderUseCase {

    PurchaseOrder execute(LocalDate orderDate, LocalDate expectedDate, Long supplierId,
                          Long facilityId, Long currencyId, BigDecimal exchangeRate,
                          int paymentTermDays, Long prId, String note);
}
```

#### 4.1.2 Test First

- [ ] Create `src/test/java/com/solusi/erp/purchasing/purchaseorder/application/usecase/command/CreatePurchaseOrderUseCaseTest.java`

```java
package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.domain.port.AccountingPeriodChecker;
import com.solusi.erp.purchasing.purchaseorder.domain.port.SupplierInfoProvider;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePurchaseOrderUseCase Tests")
class CreatePurchaseOrderUseCaseTest {

    @Mock private PurchaseOrderRepository repository;
    @Mock private SequenceGeneratorService sequenceGeneratorService;
    @Mock private SupplierInfoProvider supplierInfoProvider;
    @Mock private AccountingPeriodChecker accountingPeriodChecker;

    private CreatePurchaseOrderUseCaseImpl useCase;

    private static final LocalDate ORDER_DATE = LocalDate.of(2026, 7, 14);
    private static final LocalDate EXPECTED_DATE = LocalDate.of(2026, 7, 28);

    @BeforeEach
    void setUp() {
        useCase = new CreatePurchaseOrderUseCaseImpl(
            repository, sequenceGeneratorService, supplierInfoProvider, accountingPeriodChecker
        );
    }

    @Test
    @DisplayName("creates purchase order with generated code and saves")
    void execute_createsAndSaves() {
        when(supplierInfoProvider.isActive(1L)).thenReturn(true);
        when(accountingPeriodChecker.isDateInOpenPeriod(ORDER_DATE)).thenReturn(true);
        when(sequenceGeneratorService.generate("PO")).thenReturn("PO-202607-00001");
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder result = useCase.execute(ORDER_DATE, EXPECTED_DATE, 1L, 2L, 3L,
            BigDecimal.ONE, 30, null, "Test");

        assertThat(result.getCode()).isEqualTo("PO-202607-00001");
        assertThat(result.getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
        assertThat(result.getOrderDate()).isEqualTo(ORDER_DATE);
        assertThat(result.getExpectedDate()).isEqualTo(EXPECTED_DATE);
        assertThat(result.getSupplierId()).isEqualTo(1L);
        assertThat(result.getFacilityId()).isEqualTo(2L);
        assertThat(result.getCurrencyId()).isEqualTo(3L);
        assertThat(result.getPaymentTermDays()).isEqualTo(30);
        verify(repository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("throws when supplier is not active")
    void execute_throwsWhenSupplierInactive() {
        when(supplierInfoProvider.isActive(1L)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(ORDER_DATE, EXPECTED_DATE, 1L, 2L, 3L,
            BigDecimal.ONE, 30, null, null))
            .isInstanceOf(DomainException.class)
            .hasMessage("msg.error.po.supplier.inactive");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("throws when order date is not in open period")
    void execute_throwsWhenPeriodNotOpen() {
        when(supplierInfoProvider.isActive(1L)).thenReturn(true);
        when(accountingPeriodChecker.isDateInOpenPeriod(ORDER_DATE)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(ORDER_DATE, EXPECTED_DATE, 1L, 2L, 3L,
            BigDecimal.ONE, 30, null, null))
            .isInstanceOf(DomainException.class)
            .hasMessage("msg.error.po.period.not.open");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("passes prId through to domain")
    void execute_passesPrId() {
        when(supplierInfoProvider.isActive(1L)).thenReturn(true);
        when(accountingPeriodChecker.isDateInOpenPeriod(ORDER_DATE)).thenReturn(true);
        when(sequenceGeneratorService.generate("PO")).thenReturn("PO-202607-00002");
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder result = useCase.execute(ORDER_DATE, EXPECTED_DATE, 1L, 2L, 3L,
            BigDecimal.ONE, 30, 42L, null);

        assertThat(result.getPrId()).isEqualTo(42L);
    }
}
```

#### 4.1.3 Implementation

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/application/usecase/command/CreatePurchaseOrderUseCaseImpl.java`

```java
package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.port.AccountingPeriodChecker;
import com.solusi.erp.purchasing.purchaseorder.domain.port.SupplierInfoProvider;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreatePurchaseOrderUseCaseImpl implements CreatePurchaseOrderUseCase {

    private final PurchaseOrderRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final SupplierInfoProvider supplierInfoProvider;
    private final AccountingPeriodChecker accountingPeriodChecker;

    public CreatePurchaseOrderUseCaseImpl(PurchaseOrderRepository repository,
                                           SequenceGeneratorService sequenceGeneratorService,
                                           SupplierInfoProvider supplierInfoProvider,
                                           AccountingPeriodChecker accountingPeriodChecker) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.supplierInfoProvider = supplierInfoProvider;
        this.accountingPeriodChecker = accountingPeriodChecker;
    }

    @Override
    public PurchaseOrder execute(LocalDate orderDate, LocalDate expectedDate, Long supplierId,
                                  Long facilityId, Long currencyId, BigDecimal exchangeRate,
                                  int paymentTermDays, Long prId, String note) {
        if (!supplierInfoProvider.isActive(supplierId)) {
            throw new DomainException("msg.error.po.supplier.inactive");
        }
        if (!accountingPeriodChecker.isDateInOpenPeriod(orderDate)) {
            throw new DomainException("msg.error.po.period.not.open");
        }
        String code = sequenceGeneratorService.generate("PO");
        PurchaseOrder po = PurchaseOrder.createNew(code, orderDate, expectedDate, supplierId,
            facilityId, currencyId, exchangeRate, paymentTermDays, prId, note);
        return repository.save(po);
    }
}
```

### 4.2 UpdatePurchaseOrderUseCase

#### 4.2.1 Interface

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/application/usecase/command/UpdatePurchaseOrderUseCase.java`

```java
package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import java.math.BigDecimal;
import java.time.LocalDate;

@FunctionalInterface
public interface UpdatePurchaseOrderUseCase {

    void execute(Long id, LocalDate orderDate, LocalDate expectedDate, Long facilityId,
                 BigDecimal exchangeRate, int paymentTermDays, String note);
}
```

#### 4.2.2 Test First

- [ ] Create `src/test/java/com/solusi/erp/purchasing/purchaseorder/application/usecase/command/UpdatePurchaseOrderUseCaseTest.java`

```java
package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.domain.port.AccountingPeriodChecker;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePurchaseOrderUseCase Tests")
class UpdatePurchaseOrderUseCaseTest {

    @Mock private PurchaseOrderRepository repository;
    @Mock private AccountingPeriodChecker accountingPeriodChecker;

    private UpdatePurchaseOrderUseCaseImpl useCase;

    private static final LocalDate ORDER_DATE = LocalDate.of(2026, 7, 14);
    private static final LocalDate EXPECTED_DATE = LocalDate.of(2026, 7, 28);

    @BeforeEach
    void setUp() {
        useCase = new UpdatePurchaseOrderUseCaseImpl(repository, accountingPeriodChecker);
    }

    private PurchaseOrder draftPo() {
        return new PurchaseOrder(
            new AuditMetadata(1L, 1L, null, null, null, null),
            "PO-202607-00001", ORDER_DATE, EXPECTED_DATE, 1L, 2L, 3L,
            BigDecimal.ONE, 30, null, PurchaseOrderStatus.DRAFT,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "note", true, new ArrayList<>()
        );
    }

    @Test
    @DisplayName("updates header fields of DRAFT PO")
    void execute_updatesHeader() {
        PurchaseOrder po = draftPo();
        when(repository.findById(1L)).thenReturn(Optional.of(po));
        when(accountingPeriodChecker.isDateInOpenPeriod(ORDER_DATE)).thenReturn(true);

        useCase.execute(1L, ORDER_DATE, EXPECTED_DATE, 5L, new BigDecimal("15000"), 45, "Updated");

        verify(repository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("throws when PO not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, ORDER_DATE, EXPECTED_DATE, 5L,
            BigDecimal.ONE, 30, null))
            .isInstanceOf(DomainException.class)
            .hasMessage("msg.error.po.not.found");
    }

    @Test
    @DisplayName("throws when period not open")
    void execute_throwsWhenPeriodClosed() {
        PurchaseOrder po = draftPo();
        when(repository.findById(1L)).thenReturn(Optional.of(po));
        when(accountingPeriodChecker.isDateInOpenPeriod(ORDER_DATE)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(1L, ORDER_DATE, EXPECTED_DATE, 5L,
            BigDecimal.ONE, 30, null))
            .isInstanceOf(DomainException.class)
            .hasMessage("msg.error.po.period.not.open");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("throws when PO is not in DRAFT status")
    void execute_throwsWhenNotDraft() {
        PurchaseOrder po = new PurchaseOrder(
            new AuditMetadata(1L, 1L, null, null, null, null),
            "PO-202607-00001", ORDER_DATE, EXPECTED_DATE, 1L, 2L, 3L,
            BigDecimal.ONE, 30, null, PurchaseOrderStatus.SUBMITTED,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, true, new ArrayList<>()
        );
        when(repository.findById(1L)).thenReturn(Optional.of(po));
        when(accountingPeriodChecker.isDateInOpenPeriod(ORDER_DATE)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L, ORDER_DATE, EXPECTED_DATE, 5L,
            BigDecimal.ONE, 30, null))
            .isInstanceOf(DomainException.class)
            .hasMessage("msg.error.po.not.editable");
    }
}
```

#### 4.2.3 Implementation

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/application/usecase/command/UpdatePurchaseOrderUseCaseImpl.java`

```java
package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.port.AccountingPeriodChecker;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdatePurchaseOrderUseCaseImpl implements UpdatePurchaseOrderUseCase {

    private final PurchaseOrderRepository repository;
    private final AccountingPeriodChecker accountingPeriodChecker;

    public UpdatePurchaseOrderUseCaseImpl(PurchaseOrderRepository repository,
                                           AccountingPeriodChecker accountingPeriodChecker) {
        this.repository = repository;
        this.accountingPeriodChecker = accountingPeriodChecker;
    }

    @Override
    public void execute(Long id, LocalDate orderDate, LocalDate expectedDate, Long facilityId,
                        BigDecimal exchangeRate, int paymentTermDays, String note) {
        PurchaseOrder po = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.po.not.found"));
        if (!accountingPeriodChecker.isDateInOpenPeriod(orderDate)) {
            throw new DomainException("msg.error.po.period.not.open");
        }
        po.updateHeader(orderDate, expectedDate, facilityId, exchangeRate, paymentTermDays, note);
        repository.save(po);
    }
}
```

### 4.3 DeletePurchaseOrderUseCase

#### 4.3.1 Interface

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/application/usecase/command/DeletePurchaseOrderUseCase.java`

```java
package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

@FunctionalInterface
public interface DeletePurchaseOrderUseCase {

    void execute(Long id);
}
```

#### 4.3.2 Test First

- [ ] Create `src/test/java/com/solusi/erp/purchasing/purchaseorder/application/usecase/command/DeletePurchaseOrderUseCaseTest.java`

```java
package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeletePurchaseOrderUseCase Tests")
class DeletePurchaseOrderUseCaseTest {

    @Mock private PurchaseOrderRepository repository;

    private DeletePurchaseOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeletePurchaseOrderUseCaseImpl(repository);
    }

    private PurchaseOrder poWithStatus(PurchaseOrderStatus status) {
        return new PurchaseOrder(
            new AuditMetadata(1L, 1L, null, null, null, null),
            "PO-001", LocalDate.now(), LocalDate.now().plusDays(7), 1L, 2L, 3L,
            BigDecimal.ONE, 30, null, status,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, true, new ArrayList<>()
        );
    }

    @Test
    @DisplayName("deletes DRAFT PO successfully")
    void execute_deletesDraft() {
        when(repository.findById(1L)).thenReturn(Optional.of(poWithStatus(PurchaseOrderStatus.DRAFT)));

        useCase.execute(1L);

        verify(repository).delete(1L);
    }

    @Test
    @DisplayName("throws when PO not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
            .isInstanceOf(DomainException.class)
            .hasMessage("msg.error.po.not.found");
    }

    @Test
    @DisplayName("throws when PO is not in DRAFT status")
    void execute_throwsWhenNotDraft() {
        when(repository.findById(1L)).thenReturn(Optional.of(poWithStatus(PurchaseOrderStatus.SUBMITTED)));

        assertThatThrownBy(() -> useCase.execute(1L))
            .isInstanceOf(DomainException.class)
            .hasMessage("msg.error.po.not.deletable");
    }

    @Test
    @DisplayName("throws when PO is APPROVED")
    void execute_throwsWhenApproved() {
        when(repository.findById(1L)).thenReturn(Optional.of(poWithStatus(PurchaseOrderStatus.APPROVED)));

        assertThatThrownBy(() -> useCase.execute(1L))
            .isInstanceOf(DomainException.class)
            .hasMessage("msg.error.po.not.deletable");
    }
}
```

#### 4.3.3 Implementation

- [ ] Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/application/usecase/command/DeletePurchaseOrderUseCaseImpl.java`

```java
package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

public class DeletePurchaseOrderUseCaseImpl implements DeletePurchaseOrderUseCase {

    private final PurchaseOrderRepository repository;

    public DeletePurchaseOrderUseCaseImpl(PurchaseOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        PurchaseOrder po = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.po.not.found"));
        if (!po.getStatus().isEditable()) {
            throw new DomainException("msg.error.po.not.deletable");
        }
        repository.delete(id);
    }
}
```

