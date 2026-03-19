# Implementation Plan - Generic LookupDto Refactor

## Phase 1: Backend Refactoring
- [x] Task: Update `LookupDto.java` core record.
    - [x] Add `Map<String, Object> payload` field.
    - [x] Provide a secondary constructor for backward compatibility (optional but recommended if used elsewhere).
- [x] Task: Refactor `InventoryLookupController.java`.
    - [x] Change return types from `InventoryLookupDto` to `LookupDto`.
    - [x] Populate `payload` with `parentId` and `parentName` for Grid and Container lookups.
- [x] Task: Cleanup.
    - [x] Delete `src/main/java/com/solusi/erp/inventory/dto/InventoryLookupDto.java`.
    - [x] Fix any compilation errors in other files that might be using `InventoryLookupDto`.
- [x] Task: Conductor - User Manual Verification 'Backend Refactoring' (Protocol in workflow.md)

## Phase 2: Frontend Refactoring
- [x] Task: Implement JS Utility in `form.html`.
    - [x] Create `lookupAutoFill(payload, mappings)` helper function.
- [x] Task: Update Autocomplete logic in `form.html`.
    - [x] Modify `tsContainer.on('change', ...)` to use the new `data.payload`.
    - [x] Integrate `lookupAutoFill` for the cascading logic.
- [x] Task: Conductor - User Manual Verification 'Frontend Refactoring' (Protocol in workflow.md)
