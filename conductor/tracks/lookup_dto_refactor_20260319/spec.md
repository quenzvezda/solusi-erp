# Specification: Generic LookupDto Refactor

## 1. Overview
The goal of this track is to refactor the core `LookupDto` to support a generic payload (`Map<String, Object>`). This will allow the system to pass additional metadata (e.g., parent IDs, supplementary names, or specific flags) within the same standardized DTO, eliminating the need for specialized lookup DTOs like `InventoryLookupDto`.

## 2. Functional Requirements

### 2.1 Backend Refactoring
- **Core DTO Update**: Modify `com.solusi.erp.core.dto.LookupDto` (currently a record) to include a `Map<String, Object> payload` field.
- **DTO Replacement**: Remove `com.solusi.erp.inventory.dto.InventoryLookupDto` entirely.
- **Controller Update**: Refactor `com.solusi.erp.inventory.controller.InventoryLookupController` to return `LookupDto` instead of `InventoryLookupDto`.
- **Payload Population**: 
    - When fetching a **Container**, the payload should include `parentId` (Grid ID) and `parentName` (Grid Name).
    - When fetching a **Grid**, the payload should include `parentId` (Facility ID) and `parentName` (Facility Name).

### 2.2 Frontend (JavaScript) Refactoring
- **Generic Auto-Fill Helper**: Implement a reusable JavaScript function `lookupAutoFill(payload, mappings)` that:
    - Takes the payload object and a mapping object (e.g., `{ payloadKey: targetElement }`).
    - Automatically updates the target elements (TomSelect instances or standard inputs) with the values from the payload.
- **Form Integration**: Update `inventory/adjustments/form.html` to use the new `LookupDto.payload` and the `lookupAutoFill` helper for the cascading logic between Facility, Grid, and Container.

## 3. Non-Functional Requirements
- **Consistency**: All existing autocomplete fields must continue to work without regression.
- **Maintainability**: The generic payload should be flexible enough for future lookup requirements (e.g., product prices, UOM conversions).
- **Clean Code**: Adhere to the established Java and HTML/CSS styleguides.

## 4. Acceptance Criteria
- [ ] `InventoryLookupDto.java` is deleted.
- [ ] `LookupDto.java` has a `payload` field of type `Map<String, Object>`.
- [ ] `InventoryLookupController` returns `LookupDto` for all endpoints.
- [ ] In the Stock Adjustment form:
    - [ ] Selecting a **Container** automatically populates its parent **Grid** field if it's currently empty or different.
    - [ ] The logic uses the new `lookupAutoFill` helper.
- [ ] No regression in other autocomplete functionalities (Product, Facility, etc.).
