# Implementation Plan: Vendor Bill Payment Status and Unpaid Amount

> Source: docs/modules/accountspayable/vendor-bill.md and direct user brief on 2026-05-17
> Created: 2026-05-17
> Sprint: 5
> Status: IN_PROGRESS

## Summary

Expose Vendor Bill payment visibility in list and detail pages so users can immediately see whether a bill is unpaid, partially paid, or fully paid, and how much remains payable. The data already exists indirectly through confirmed Vendor Payment lines and Vendor Bill payment status updates; this plan adds a read-side payment summary path and surfaces it in the existing VB web UI without violating web-layer dependency rules.

## Scope

- Add paid and unpaid/outstanding amount data to Vendor Bill list and detail read models.
- Compute payment summary from confirmed Vendor Payment lines only.
- Show unpaid amount as a new Vendor Bill list column.
- Show payment status, paid amount, and unpaid amount on Vendor Bill detail.
- Update tests and module documentation for `PARTIAL_PAID` / `PAID` lifecycle visibility.

## Out of Scope

- Changing Vendor Payment confirmation or allocation behavior.
- Changing accounting journal posting for Vendor Bill or Vendor Payment.
- Adding payment history drill-down/table; this plan only shows summary amounts.
- Adding new payment workflows from the Vendor Bill page.

## Tasks

### Task 1: Payment Summary Read Port and Adapter [x]
Add an application/domain read path that calculates paid and outstanding amounts per Vendor Bill from confirmed Vendor Payment lines.

**Depends on:** (none)
**Reference module:** `accountspayable.vendorpayment`, `accountspayable.vendorbill`

Steps:
- [x] Create a Vendor Bill payment summary read port with methods for single bill and batch bill ids.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorpayment/domain/port/PayableVendorBillQueryPort.java:L1-L18 — existing paid/outstanding projection shape for payable VB selector.
- [x] Define a small immutable projection containing `vendorBillId`, `paidAmount`, and `outstandingAmount`.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorpayment/domain/port/PayableVendorBillQueryPort.java:L10-L17 — current record style for payment amount projections.
- [x] Implement the adapter in the Vendor Bill infrastructure slice using SQL over `ap_vendor_bills`, `ap_vendor_payment_lines`, and confirmed `ap_vendor_payments`.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/PayableVendorBillQueryAdapter.java:L1-L80 — SQL pattern calculating `paid_amount` and `outstanding_amount` from confirmed payments.
- [x] Ensure the query does not filter out fully paid bills for list/detail use; unlike payable selector, no `HAVING outstanding > 0` should be used.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/PayableVendorBillQueryAdapter.java:L40-L52 — payable selector uses filter/HAVING that must not be copied for VB display.
- [x] Treat missing payment rows as paid `0` and outstanding equal to `total_amount`.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/VendorBillPaymentUpdateAdapter.java:L1-L80 — status updater uses `COALESCE(SUM(...), 0)` semantics.
- [x] Register the adapter in Vendor Bill infrastructure configuration if explicit bean wiring is used.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/config/VendorBillConfig.java — inspect current config before editing; prior read attempt was inconclusive.
- [x] **TEST:** Add adapter/config coverage following existing Vendor Bill infrastructure test style; if config changes, add/update config integration test.
      ref: src/test/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/ — inspect existing infrastructure/config test pattern before implementation.

**Validation criteria:**
- Payment summary query returns correct paid/unpaid for unpaid, partial, and fully paid bills.
- Fully paid bills still return in summary for list/detail display.
- `mvn compile -q -pl .` passes.

---

### Task 2: Enrich Vendor Bill Query Use Cases [x]
Add payment summary amounts to Vendor Bill list/detail application views and populate them through the read port.

**Depends on:** Task 1
**Reference module:** `accountspayable.vendorbill`

Steps:
- [x] Add `paidAmount` and `outstandingAmount` fields to `VendorBillSummaryView`.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/VendorBillSummaryView.java:L1-L18 — current list projection contains status and total only.
- [x] Add `paidAmount` and `outstandingAmount` fields to `VendorBillDetailView`.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/VendorBillDetailView.java:L1-L24 — current detail projection contains invoice totals only.
- [x] Inject the payment summary port into `FindVendorBillsUseCaseImpl` and batch-load summaries for the page content ids to avoid per-row queries.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/FindVendorBillsUseCaseImpl.java:L1-L80 — current `toSummary()` maps directly from aggregate only.
- [x] Inject the payment summary port into `GetVendorBillDetailUseCaseImpl` and load one summary for the requested bill.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/GetVendorBillDetailUseCaseImpl.java:L1-L90 — current `toDetail()` maps directly from aggregate only.
- [x] Default missing summary values defensively to paid `0` and outstanding `bill.totalAmount()`.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/VendorBillPaymentUpdateAdapter.java:L1-L80 — payment status calculation treats no confirmed payments as unpaid.
- [x] **TEST:** Update `FindVendorBillsUseCaseTest` or add equivalent query test to verify summary values are attached to each list row.
      ref: src/test/java/com/solusi/erp/accountspayable/vendorbill/application/ — inspect current use case test package before implementation.
- [x] **TEST:** Update `GetVendorBillDetailUseCaseTest` or add equivalent query test to verify paid/outstanding values on detail.
      ref: src/test/java/com/solusi/erp/accountspayable/vendorbill/application/ — use Mockito query use case test pattern.

**Validation criteria:**
- List/detail use case tests cover unpaid, partial, and fully paid values where practical.
- No web-layer class computes payment amounts directly.
- `mvn test -q -pl . -Dtest="*VendorBill*UseCaseTest" -DfailIfNoTests=false` passes.

---

### Task 3: Web DTO, Mapper, and Controller Contract [x]
Expose payment summary values through Vendor Bill web responses while keeping controllers repository-free.

**Depends on:** Task 2
**Reference module:** `accountspayable.vendorbill`

Steps:
- [x] Add `paidAmount` and `outstandingAmount` to `VendorBillSummaryResponse`.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/web/dto/VendorBillSummaryResponse.java:L1-L40 — current DTO has status and total only.
- [x] Add `paidAmount` and `outstandingAmount` to `VendorBillDetailResponse`.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/web/dto/VendorBillDetailResponse.java:L1-L50 — current DTO has subtotal/tax/total only.
- [x] Map the new fields in `VendorBillWebMapper.toSummaryResponse()` and `toDetailResponse()`.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/web/mapper/VendorBillWebMapper.java:L1-L120 — mapper currently copies invoice totals but no payment totals.
- [x] Keep `VendorBillController` unchanged unless tests reveal model contract needs adjustment; it should continue using use cases and mapper only.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/web/controller/VendorBillController.java:L1-L120 — list/detail actions already depend on application use cases, not repositories.
- [x] **TEST:** Update web mapper tests if present, or controller tests if they assert response fields/model contract.
      ref: src/test/java/com/solusi/erp/accountspayable/vendorbill/web/ — inspect current Vendor Bill web test patterns.
- [x] **TEST:** Run dependency guard to ensure no repository dependency was introduced in web layer.
      ref: src/test/java/com/solusi/erp/architecture/WebLayerDependencyGuardTest.java:L1-L120 — web controller/mapper repository dependency guard.

**Validation criteria:**
- New DTO fields are populated from application views.
- `VendorBillController` has no repository imports/fields.
- `mvn test -q -pl . -Dtest="*VendorBill*ControllerTest,*VendorBill*MapperTest,WebLayerDependencyGuardTest" -DfailIfNoTests=false` passes.

---

### Task 4: Vendor Bill List Unpaid Column [x]
Add an unpaid/outstanding amount column to the Vendor Bill list table.

**Depends on:** Task 3
**Reference module:** `accountspayable.vendorbill`

Steps:
- [x] Add a list table header for unpaid/outstanding amount after the Total column.
      ref: src/main/resources/templates/accountspayable/vendor-bills/list.html:L1-L140 — current table has Total then Actions.
- [x] Render `item.outstandingAmount` with the same decimal formatting as `item.totalAmount`.
      ref: src/main/resources/templates/accountspayable/vendor-bills/list.html:L80-L120 — existing total amount formatting pattern.
- [x] Add i18n labels in English and Indonesian messages files.
      ref: src/main/resources/messages.properties — existing `label.vb.*` and amount labels.
      ref: src/main/resources/messages_id.properties — Indonesian label counterpart.
- [x] Ensure the new column does not break action-column colspan or empty-state row if the list template has one.
      ref: src/main/resources/templates/accountspayable/vendor-bills/list.html:L1-L140 — verify table body/empty state structure.
- [x] **TEST:** Update Vendor Bill template tests to assert the new unpaid column key and `outstandingAmount` binding exist.
      ref: src/test/java/com/solusi/erp/accountspayable/vendorbill/web/template/ — inspect current template test naming and static assertion style.

**Validation criteria:**
- Vendor Bill list shows unpaid amount for each row.
- Static template test covers the new column and binding.
- `mvn test -q -pl . -Dtest="*VendorBill*TemplateTest" -DfailIfNoTests=false` passes.

---

### Task 5: Vendor Bill Detail Payment Summary [ ]
Add payment visibility to Vendor Bill detail so users can see status, paid amount, and remaining amount.

**Depends on:** Task 3
**Reference module:** `accountspayable.vendorbill`, `accountspayable.vendorpayment`

Steps:
- [ ] Add a payment summary area to `vendor-bills/detail.html` near the header/recap section.
      ref: src/main/resources/templates/accountspayable/vendor-bills/detail.html:L1-L180 — current detail shows invoice metadata, lines, and total invoice recap.
- [ ] Display current bill status prominently; existing `PARTIAL_PAID` / `PAID` status should be visible as payment state.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillStatus.java:L1-L10 — status enum already contains payment states.
- [ ] Display paid amount from `bill.paidAmount` and unpaid/outstanding amount from `bill.outstandingAmount` with standard decimal formatting.
      ref: src/main/resources/templates/accountspayable/vendor-bills/detail.html:L120-L180 — existing subtotal/tax/total formatting pattern.
- [ ] Use existing badge color conventions for statuses; ensure `PARTIAL_PAID` and `PAID` have sensible visual distinction.
      ref: src/main/resources/templates/accountspayable/vendor-payments/detail.html:L13-L18 — status badge class pattern for payment detail.
- [ ] Add i18n labels for paid amount, unpaid amount, and payment summary/status as needed.
      ref: src/main/resources/messages.properties — English labels.
      ref: src/main/resources/messages_id.properties — Indonesian labels.
- [ ] **TEST:** Update Vendor Bill template tests to assert detail binds `paidAmount`, `outstandingAmount`, and payment/status labels.
      ref: src/test/java/com/solusi/erp/accountspayable/vendorbill/web/template/ — static template assertion pattern.

**Validation criteria:**
- Vendor Bill id 4 / `VB-202605-00004` can visually show partially paid status and remaining amount.
- Detail template test covers payment summary bindings.
- `mvn test -q -pl . -Dtest="*VendorBill*TemplateTest" -DfailIfNoTests=false` passes.

---

### Task 6: Documentation and Verification [ ]
Update the Vendor Bill module documentation and run focused regression checks.

**Depends on:** Task 1, Task 2, Task 3, Task 4, Task 5
**Reference module:** project documentation and architecture tests

Steps:
- [ ] Update `docs/modules/accountspayable/vendor-bill.md` lifecycle to include `PARTIAL_PAID` and `PAID` after payment confirmation.
      ref: docs/modules/accountspayable/vendor-bill.md:L1-L120 — current lifecycle text only documents `DRAFT -> CONFIRMED` and `DRAFT -> CANCELLED`.
- [ ] Document that list/detail payment amounts are derived from confirmed Vendor Payment lines.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/PayableVendorBillQueryAdapter.java:L1-L80 — source query pattern for confirmed payment totals.
- [ ] Run compile check.
      ref: docs/modules/accountspayable/vendor-bill.md:L120-L180 — module docs mention clean architecture and testing expectations.
- [ ] Run focused Vendor Bill tests.
      ref: src/test/java/com/solusi/erp/accountspayable/vendorbill/ — Vendor Bill domain/application/web test area.
- [ ] Run `WebLayerDependencyGuardTest`.
      ref: src/test/java/com/solusi/erp/architecture/WebLayerDependencyGuardTest.java:L1-L120 — prevents repository dependency regression in web layer.
- [ ] Manually verify `/accounts-payable/vendor-bills` shows Unpaid column.
- [ ] Manually verify `/accounts-payable/vendor-bills/4` shows status/payment summary and remaining amount for `VB-202605-00004`.

**Validation criteria:**
- `mvn compile -q -pl .` passes.
- `mvn test -q -pl . -Dtest="*VendorBill*,WebLayerDependencyGuardTest" -DfailIfNoTests=false` passes.
- Manual list/detail browser checks confirm unpaid/payment summary is visible.

## Risk Notes

- Do not inject Vendor Payment repositories or JPA repositories into `VendorBillController` or `VendorBillWebMapper`.
- Do not reuse payable-selector filtering for list/detail summaries; fully paid bills still need display values.
- Only confirmed Vendor Payments should contribute to paid amount.
- Keep amount naming consistent: domain/application may use `outstandingAmount`; UI label can say “Unpaid”.
- Treat `VendorBillStatus` as the source of payment state; do not create a separate status enum unless implementation reveals a real need.
