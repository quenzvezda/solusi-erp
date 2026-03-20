# Track: UI Input Standardization (Stock Adjustment Golden Standard)

## Phase 1: Research & Foundation
- [x] Task: Analyze existing Stock Adjustment inputs (`templates/inventory/adjustments/form.html`) to identify exact padding/height issues.
- [x] Task: Verify current `static/css/global.css` and `static/css/tomselect-custom.css` for existing input styles.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Research & Foundation' (Protocol in workflow.md)

## Phase 2: Global CSS & Utility Update
- [x] Task: Implement unified input height and padding classes in `global.css`.
- [x] Task: Ensure `tomselect-custom.css` aligns with the new unified input styling.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Global CSS & Utility Update' (Protocol in workflow.md)

## Phase 3: Component Extraction & Refactoring
- [x] Task: Create or update Thymeleaf fragments in `templates/fragments/` for common input types (Text, Select, Autocomplete, Date).
- [x] Task: Ensure fragments support custom widths while maintaining fixed height.
- [x] Task: Conductor - User Manual Verification 'Phase 3: Component Extraction & Refactoring' (Protocol in workflow.md)

## Phase 4: Implementation (Stock Adjustment)
- [x] Task: Refactor `templates/inventory/adjustments/form.html` header inputs to use the new fragments.
- [x] Task: Refactor `templates/inventory/adjustments/form.html` line item inputs to use the new fragments.
- [x] Task: Conductor - User Manual Verification 'Phase 4: Implementation (Stock Adjustment)' (Protocol in workflow.md)

## Phase 5: Final Review & Documentation
- [x] Task: Perform final visual check to ensure all inputs on the Stock Adjustment page are consistent.
- [x] Task: Update UI documentation/guide in `docs/spec/` to reflect the new "Golden Standard".
- [x] Task: Conductor - User Manual Verification 'Phase 5: Final Review & Documentation' (Protocol in workflow.md)
