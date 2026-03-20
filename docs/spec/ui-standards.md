# UI Standard: Input Components (Golden Standard)

## Overview
To ensure visual consistency across the Solusi Program ERP, all input elements must follow a unified height, padding, and styling pattern. The **Stock Adjustment** page serves as the "Golden Standard" for these implementations.

## Standard CSS Classes

### 1. Standard Form Inputs (`32px` height)
Used for header fields and standard forms.
- `.erp-input`: Applied to `<input>`, `<select>`, and `<textarea>`.
- `.erp-input-ts`: Applied to TomSelect wrappers.

### 2. Table/Dense Inputs (`28px` height)
Used for inline editing inside tables (e.g., line items).
- `.erp-input-sm`: Applied to small `<input>` and `<select>`.
- `.erp-input-ts-sm`: Applied to small TomSelect wrappers.

## Thymeleaf Fragments
Always prefer using the standardized fragments in `templates/fragments/inputs.html` instead of writing raw HTML.

### Usage Examples:

#### Standard Text Input
```html
<div th:replace="~{fragments/inputs :: text(field=*{name}, label='Full Name', placeholder='e.g. John Doe')}"></div>
```

#### TomSelect Autocomplete
```html
<div th:replace="~{fragments/inputs :: autocomplete(field=*{facilityId}, label='Facility', id='header-facility', extraClass='required')}"></div>
```

#### Table Number Input
```html
<div th:replace="~{fragments/inputs :: table-number(name='lines[0].quantity', value='1.00', extraClass='input-qty')}"></div>
```

## Best Practices
1. **Consistency:** All inputs in the same row/container should use the same height class (e.g., don't mix `.erp-input` and standard `.form-control`).
2. **Autocomplete Initialization:** When using `autocomplete` fragments, ensure the corresponding JavaScript `initLookup` is called for the ID.
3. **Date Inputs:** Use the `.erp-input` class on date inputs to ensure they align with text inputs.
4. **Validation:** Always include `th:errorclass="is-invalid"` (included by default in fragments) for server-side validation feedback.
