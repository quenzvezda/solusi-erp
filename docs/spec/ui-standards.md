# UI Standard: Input Components (Golden Standard)

## Overview
To ensure visual consistency across the Solusi Program ERP, all input elements must follow a unified height, padding, and styling pattern. The **Stock Adjustment** page serves as the "Golden Standard" for these implementations.

## Standard CSS Classes

### 1. Standard Form Inputs (`32px` height)
Used for header fields and standard forms.
- `.erp-input`: Applied to `<input>`, `<select>`, and `<textarea>`.
- `.erp-input-ts`: Applied to TomSelect **wrappers** (automatically managed by `initLookup`).

### 2. Table/Dense Inputs (`28px` height)
Used for inline editing inside tables (e.g., line items).
- `.erp-input-sm`: Applied to small `<input>` and `<select>`.
- `.erp-input-ts-sm`: Applied to small TomSelect **wrappers** (automatically managed by `initLookup`).

## Thymeleaf Fragments
Always prefer using the standardized fragments in `templates/fragments/inputs.html` instead of writing raw HTML.

### Usage Examples:

#### Standard Text Input
```html
<div th:replace="~{fragments/inputs :: text(field=*{name}, label='Full Name', placeholder='e.g. John Doe')}"></div>
```

#### TomSelect Autocomplete
```html
<!-- fragment inside templates/fragments/inputs.html -->
<div th:replace="~{fragments/inputs :: autocomplete(field=*{facilityId}, label='Facility', id='header-facility', required=true)}"></div>
```

## Best Practices & JavaScript Initialization

### 1. The Global `initLookup` Function
All autocompletes **MUST** be initialized using the global `initLookup` function defined in `master.html`. **DILARANG** melakukan inisialisasi `new TomSelect()` secara manual untuk lookup standar.

**Example Implementation:**
```javascript
window.addEventListener('load', function() {
    const el = document.getElementById('header-facility');
    // Global function handles: wrapper classes, SSR sync, and debouncing
    const ts = initLookup(el, 'inventory/facilities');
    
    // Optional: Add custom event listeners
    if (ts) {
        ts.on('change', (val) => { ... });
    }
});
```

### 2. Height Consistency
The `initLookup` function automatically detects if an element is inside a `.line-row` table and applies `.erp-input-ts-sm`, otherwise it applies `.erp-input-ts`. This ensures that the TomSelect component matches the `32px` or `28px` height of adjacent `.erp-input` fields.

### 3. SSR Synchronization
The `initLookup` function reads the `data-subtext` attribute from the initial `<option>` rendered by Thymeleaf. This ensures that the Code/Subtext is visible immediately upon page load (Edit Mode).

### 4. Validation
Always include `th:errorclass="is-invalid"` (included by default in fragments) for server-side validation feedback.
