# UI Standard: Input Components (Golden Standard)

## Overview
To ensure visual consistency across the Solusi Program ERP, all input elements must follow a unified height, padding, and styling pattern. The **Stock Adjustment** page serves as the "Golden Standard" for these implementations.

## Standard CSS Classes

### 1. Standard Form Inputs (`32px` height)
Used for header fields and standard forms.
- `.erp-input`: Applied to `<input>`, `<select>`, and `<textarea>`.
- `.erp-input-ts`: Applied to TomSelect **wrappers** (automatically managed by `initLookup`).
- `.erp-number-decimal`: AutoNumeric formatting for decimals (2 decimal places).
- `.erp-number-integer`: AutoNumeric formatting for whole numbers.

### 2. Table/Dense Inputs (`28px` height)
Used for inline editing inside tables (e.g., line items).
- `.erp-input-sm`: Applied to small `<input>` and `<select>`.
- `.erp-input-ts-sm`: Applied to small TomSelect **wrappers**.

## Thymeleaf Fragments
Always prefer using the standardized fragments in `templates/fragments/inputs.html`.

### Usage Examples:

#### Standard Text & Numeric Input
```html
<div th:replace="~{fragments/inputs :: text(field='name', label='Full Name')}"></div>
<div th:replace="~{fragments/inputs :: decimal(field='price', label='Price')}"></div>
```

#### HTMX Form Submission
Setiap form wajib mendukung submit asinkron untuk menjaga UI state:
```html
<form th:action="@{...}" method="post" hx-post hx-target="#alert-container" hx-swap="innerHTML">
    <div id="alert-container">
        <div th:replace="~{fragments/alerts :: success}"></div>
        <div th:replace="~{fragments/alerts :: error}"></div>
    </div>
    ...
</form>
```

## Best Practices & JavaScript Initialization

### 1. Global Auto-Initialization
Sistem secara otomatis menginisialisasi komponen berikut tanpa perlu script manual di setiap halaman:
- **Numeric**: Elemen dengan class `.erp-number-*`.
- **Autocomplete**: Elemen `.erp-input-ts` yang memiliki atribut `data-lookup-path`.

**Aturan Wajib: Trinity Data (ID, Name, SubText)**
Untuk mencegah dropdown terlihat kosong saat mode Edit atau setelah error validasi, setiap implementasi Autocomplete **WAJIB** menyertakan:
1.  **ID (Value)**: Disimpan ke database.
2.  **Name (Text)**: Label utama yang terlihat.
3.  **SubText (Code)**: Informasi sekunder (kode) di bawah nama.

Developer wajib memastikan Request DTO memiliki field penampung untuk Name dan SubText tersebut (contoh: `brandName`, `brandCode`).

### 2. The Global `initLookup` Function
Jika butuh inisialisasi manual (misal: cascading), gunakan:
```javascript
const ts = initLookup(element, 'module/path', parentProvider);
```
- `lookupPath`: String path API (contoh: `'inventory/products'`).
- `parentProvider`: Callback function untuk filter data berdasarkan field lain.

### 2. Height Consistency
The `initLookup` function automatically detects if an element is inside a `.line-row` table and applies `.erp-input-ts-sm`, otherwise it applies `.erp-input-ts`. This ensures that the TomSelect component matches the `32px` or `28px` height of adjacent `.erp-input` fields.

### 3. SSR Synchronization
The `initLookup` function reads the `data-subtext` attribute from the initial `<option>` rendered by Thymeleaf. This ensures that the Code/Subtext is visible immediately upon page load (Edit Mode).

### 4. Validation
Always include `th:errorclass="is-invalid"` (included by default in fragments) for server-side validation feedback.
