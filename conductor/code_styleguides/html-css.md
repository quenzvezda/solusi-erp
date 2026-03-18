# HTML & CSS Style Guide: Solusi Program ERP

## General Principles
- **Semantic HTML:** Use semantic tags (`<nav>`, `<header>`, `<footer>`, `<main>`, `<section>`, etc.) for accessibility and clarity.
- **Thymeleaf Patterns:** Use Thymeleaf fragments for common UI components (e.g., layouts, tables, forms).

## Thymeleaf & Templates
- **Fragments:** Define reusable fragments in `templates/fragments/` and layout templates in `templates/layout/`.
- **Expression Language:** Use Thymeleaf expressions (`${...}`, `*{...}`, `#{...}`, `@{...}`) correctly for data, variables, i18n, and links.
- **Security:** Use `sec:authorize` to control visibility based on user roles and permissions.

## CSS & Styling
- **Utility Classes:** Use utility-first CSS principles (e.g., Bootstrap-like utilities) for layout and spacing.
- **Consistency:** Follow established design patterns (e.g., standard margins, colors, and typography).
- **Responsive Design:** Ensure layouts work across different screen sizes using responsive utility classes.

## Best Practices
- **Clean Structure:** Keep templates well-organized and modular.
- **Avoid Inline Styles:** Use CSS classes instead of inline styles.
- **Accessibility:** Use proper ARIA attributes where needed.
