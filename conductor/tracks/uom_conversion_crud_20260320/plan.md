# Implementation Plan: Product UoM Conversion CRUD

## Phase 1: Database & Security Setup
- [x] Task: Create Flyway migration to insert `UOM-CONVERSION` permissions (`_READ`, `_CREATE`, `_UPDATE`, `_DELETE`) into the `permissions` table.
- [x] Task: Conductor - User Manual Verification 'Database & Security Setup' (Protocol in workflow.md)

## Phase 2: Backend Implementation (DTOs & Repository)
- [x] Task: Create `ProductUomConversionRequest` and `ProductUomConversionResponse` DTOs (extending `BaseAuditResponse`).
- [x] Task: Update `ProductUomConversionRepository` to add method for pagination/search (e.g., searching by product name) and checking for duplicate conversions (`existsByProductIdAndFromUomId`).
- [x] Task: Create MapStruct mapper interface `ProductUomConversionMapper`.
- [x] Task: Conductor - User Manual Verification 'Backend Implementation (DTOs & Repository)' (Protocol in workflow.md)

## Phase 3: Backend Implementation (Service & Controller)
- [x] Task: Create `ProductUomConversionService` (interface and implementation) handling CRUD operations and enforcing validation rules (Positive Factor, No Duplicates, No Self-Conversion).
- [x] Task: Create `ProductUomConversionController` with standard endpoints (`/inventory/uom-conversions`, `/create`, `/{id}/edit`, `/delete`).
- [x] Task: Conductor - User Manual Verification 'Backend Implementation (Service & Controller)' (Protocol in workflow.md)

## Phase 4: Frontend Implementation (UI)
- [x] Task: Create Thymeleaf view `templates/inventory/uom-conversions/list.html` for displaying the paginated table.
- [x] Task: Create Thymeleaf view `templates/inventory/uom-conversions/form.html` for Create/Edit, integrating TomSelect for Product and UoM dropdowns.
- [x] Task: Update `fragments/sidebar.html` to include the new "UoM Conversions" menu item securely wrapped with `sec:authorize`.
- [x] Task: Conductor - User Manual Verification 'Frontend Implementation (UI)' (Protocol in workflow.md)