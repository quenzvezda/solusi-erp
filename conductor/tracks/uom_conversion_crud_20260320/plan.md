# Implementation Plan: Product UoM Conversion CRUD

## Phase 1: Database & Security Setup
- [ ] Task: Create Flyway migration to insert `UOM-CONVERSION` permissions (`_READ`, `_CREATE`, `_UPDATE`, `_DELETE`) into the `permissions` table.
- [ ] Task: Conductor - User Manual Verification 'Database & Security Setup' (Protocol in workflow.md)

## Phase 2: Backend Implementation (DTOs & Repository)
- [ ] Task: Create `ProductUomConversionRequest` and `ProductUomConversionResponse` DTOs (extending `BaseAuditResponse`).
- [ ] Task: Update `ProductUomConversionRepository` to add method for pagination/search (e.g., searching by product name) and checking for duplicate conversions (`existsByProductIdAndFromUomId`).
- [ ] Task: Create MapStruct mapper interface `ProductUomConversionMapper`.
- [ ] Task: Conductor - User Manual Verification 'Backend Implementation (DTOs & Repository)' (Protocol in workflow.md)

## Phase 3: Backend Implementation (Service & Controller)
- [ ] Task: Create `ProductUomConversionService` (interface and implementation) handling CRUD operations and enforcing validation rules (Positive Factor, No Duplicates, No Self-Conversion).
- [ ] Task: Create `ProductUomConversionController` with standard endpoints (`/inventory/uom-conversions`, `/create`, `/{id}/edit`, `/delete`).
- [ ] Task: Conductor - User Manual Verification 'Backend Implementation (Service & Controller)' (Protocol in workflow.md)

## Phase 4: Frontend Implementation (UI)
- [ ] Task: Create Thymeleaf view `templates/inventory/uom-conversions/list.html` for displaying the paginated table.
- [ ] Task: Create Thymeleaf view `templates/inventory/uom-conversions/form.html` for Create/Edit, integrating TomSelect for Product and UoM dropdowns.
- [ ] Task: Update `fragments/sidebar.html` to include the new "UoM Conversions" menu item securely wrapped with `sec:authorize`.
- [ ] Task: Conductor - User Manual Verification 'Frontend Implementation (UI)' (Protocol in workflow.md)