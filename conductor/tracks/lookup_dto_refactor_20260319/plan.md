# Implementation Plan - Generic LookupDto Refactor

## Phase 1: Backend Refactoring
- [ ] Task: Update `LookupDto.java` core record.
    - [ ] Add `Map<String, Object> payload` field.
    - [ ] Provide a secondary constructor for backward compatibility (optional but recommended if used elsewhere).
- [ ] Task: Refactor `InventoryLookupController.java`.
    - [ ] Change return types from `InventoryLookupDto` to `LookupDto`.
    - [ ] Populate `payload` with `parentId` and `parentName` for Grid and Container lookups.
- [ ] Task: Cleanup.
    - [ ] Delete `src/main/java/com/solusi/erp/inventory/dto/InventoryLookupDto.java`.
    - [ ] Fix any compilation errors in other files that might be using `InventoryLookupDto`.
- [ ] Task: Conductor - User Manual Verification 'Backend Refactoring' (Protocol in workflow.md)

## Phase 2: Frontend Refactoring
- [ ] Task: Implement JS Utility in `form.html`.
    - [ ] Create `lookupAutoFill(payload, mappings)` helper function.
- [ ] Task: Update Autocomplete logic in `form.html`.
    - [ ] Modify `tsContainer.on('change', ...)` to use the new `data.payload`.
    - [ ] Integrate `lookupAutoFill` for the cascading logic.
- [ ] Task: Conductor - User Manual Verification 'Frontend Refactoring' (Protocol in workflow.md)
