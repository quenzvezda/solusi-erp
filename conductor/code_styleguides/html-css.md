# HTML & CSS Style Guide: Solusi Program ERP

## General Principles
- **Semantic HTML:** Use semantic tags (`<nav>`, `<header>`, `<footer>`, `<main>`, `<section>`, etc.) for accessibility and clarity.
- **Thymeleaf Patterns:** Use Thymeleaf fragments for common UI components (e.g., layouts, tables, forms).

## Thymeleaf & Templates
- **Fragments:** Define reusable fragments in `templates/fragments/` and layout templates in `templates/layout/`.
- **Insertion Pattern:** 
  - Use `th:insert` instead of `th:replace` for input fragments (like Autocomplete) to preserve the wrapper `div` and its `id`.
  - Prefer `th:block` inside fragments to avoid redundant nested tags.
- **Expression Language:** Use Thymeleaf expressions (`${...}`, `*{...}`, `#{...}`, `@{...}`) correctly for data, variables, i18n, and links.
- **Security:** Use `sec:authorize` to control visibility based on user roles and permissions.

## AJAX & Form Submission (Standard CRUD)
- **Attribute-Driven Logic:** Use `data-ajax-form="true"` on form tags to enable automatic AJAX submission via `erp-form-handler.js`.
- **Redirects:** Specify the success redirect path using `data-redirect-on-success="/module/path"`.
- **State Persistence:** Do not use HTMX for forms with complex JavaScript components (TomSelect, AutoNumeric) to prevent state destruction on validation errors. Use AJAX instead.

## CSS & Styling
- **Utility Classes:** Use utility-first CSS principles (e.g., Bootstrap-like utilities) for layout and spacing.
- **Consistency:** Follow established design patterns (e.g., standard margins, colors, and typography).
- **Responsive Design:** Ensure layouts work across different screen sizes using responsive utility classes.
- **Dense Data Entry Tables:** 
  - Use `table-layout: fixed; width: 100%;` for forms with many input columns to prevent horizontal scrollbars.
  - Apply tight padding (e.g., `padding: 0.4rem 0.2rem`) and slightly smaller font sizes (e.g., `0.85rem`) for table content.
  - Force TomSelect width with `.ts-wrapper { width: 100% !important; }`.

## Component Standards
- **TomSelect (Autocomplete):**
  - **Preload:** Use `preload: 'focus'` to trigger lookups immediately upon interaction.
  - **Dropdown Parent:** Use `dropdownParent: 'body'` to prevent clipping issues.
  - **Debounce:** Always implement a minimum of 150ms debounce for remote AJAX lookups.
  - **Initialization Signals:** Use the `erp:lookup-initialized` event to attach custom listeners to TomSelect instances safely.

## Best Practices
- **Clean Structure:** Keep templates well-organized and modular.
- **Avoid Inline Styles:** Use CSS classes instead of inline styles.
- **Accessibility:** Use proper ARIA attributes where needed.
