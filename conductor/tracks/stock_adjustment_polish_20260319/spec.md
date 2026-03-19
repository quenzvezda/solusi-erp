# Specification: Stock Adjustment UI Polish & Autocomplete

## Overview
This track enhances the **Stock Adjustment** feature by introducing intelligent filtering and professional-grade autocomplete using **TomSelect**. It ensures that users select a **Facility** at the header level before adding items, and provides hierarchical filtering (**Facility > Grid > Container**) to streamline the bin selection process.

## Functional Requirements

### 1. Data Model Enhancements
- **Header Update:** Add `facility_id` to the `StockAdjustment` entity. This dictates the facility for all adjustment lines.
- **Line Update:** Add `grid_id` to `StockAdjustmentLine` as a helper lookup field.
- **Logic:** 
  - If a **Container** is selected, the **Grid** must be automatically populated.
  - If a **Grid** is selected, the **Container** list must be filtered to only show bins within that grid.

### 2. Autocomplete Integration (TomSelect)
Implement AJAX-based lookup for the following entities following `docs/spec/autocomplete-generic.md`:
- **Product:** Search by name/code.
- **Facility:** Search by name/code.
- **Grid:** Filtered by selected Facility.
- **Container:** Filtered by selected Grid.

### 3. UX Validations & i18n Reuse
- **Facility Enforcement:** Show an alert if "Add Line" is clicked without selecting a Facility first.
- **Reusable i18n Message:** Implement the "trick" for reusable alerts:
  - `msg.error.please_select_first = Please select {0} first!` (or equivalent in Indonesian).
  - This allows the message to be reused for any model (Facility, Product, etc.).

### 4. Backend Lookup API
Create a new `LookupController` (or extend existing ones) to provide:
- Generic `LookupDto` responses.
- Permission-protected endpoints using the `LOOKUP_` prefix (e.g., `LOOKUP_INVENTORY`).

## Non-Functional Requirements
- **Security:** All lookup endpoints MUST require appropriate authority.
- **Performance:** All TomSelect lookups must be **debounced** (min 100ms) and **limited** to top 10 results.
- **Maintainability:** Use standard `LookupDto` record for consistency.

## Acceptance Criteria
- Stock Adjustment header requires a Facility.
- Users cannot add lines without a selected Facility.
- Line items support autocomplete for Product, Grid, and Container.
- Selecting a Grid dynamically filters the available Containers.
- Existing adjustments (Edit Mode) correctly perform reverse lookups to display names instead of IDs.
