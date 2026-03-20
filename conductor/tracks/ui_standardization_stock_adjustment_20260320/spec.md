# Track Spec: UI Input Standardization (Stock Adjustment Golden Standard)

## Overview
This track aims to standardize the visual appearance and behavior of input elements on the **Stock Adjustment** page, making it the project's "Golden Standard" for UI. The focus is on ensuring consistency in element height, padding, and general styling across different input types, including Autocomplete (TomSelect), DatePickers, Select dropdowns, and text inputs, particularly within line item tables.

## Functional Requirements
- **Unified Input Styling:** Ensure all input elements (text, select, autocomplete, date) have a consistent height and padding.
- **Table Inline Inputs:** Standardize the styling of inputs within the Stock Adjustment line items table.
- **Responsive Widths:** Allow input widths to vary based on layout requirements while maintaining consistent height.
- **Visual Alignment:** Align the visual style of TomSelect (autocomplete) and standard HTML/Thymeleaf selects.

## Non-Functional Requirements
- **Reusable Fragments:** Use Thymeleaf fragments for input components to ensure future maintainability and consistency.
- **Global CSS Utility:** Update `global.css` or `tomselect-custom.css` to provide consistent styling classes.
- **Code Style:** Adhere to the project's HTML/CSS and Java style guides.

## Acceptance Criteria
- [ ] Stock Adjustment page inputs (header and line items) have uniform height.
- [ ] Autocomplete (TomSelect) visually matches standard text inputs and selects in terms of padding and borders.
- [ ] DatePicker elements follow the same styling pattern.
- [ ] All input-related logic on the Stock Adjustment page remains functional.

## Out of Scope
- Major layout/structure changes to other pages (only Stock Adjustment is the target).
- Backend logic changes (unless required for data binding to new fragments).
- Redesigning the action bars or search/filter areas.
