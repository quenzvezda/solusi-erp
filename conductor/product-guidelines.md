# Product Guidelines: Solusi Program ERP

## Prose & Communication
- **Style:** Technical & Precise. Use clear, concise language that prioritizes accuracy and technical depth.
- **Tone:** Professional and objective, suitable for developers and ERP power users.
- **System Messages:** Error messages and notifications should be informative, providing clear reasons and potential actions.

## UI/UX Principles
- **Hybrid Experience Pattern:** 
  - Use **HTMX** for passive updates (filtering, sorting, paging) to minimize server overhead.
  - Use **AJAX (JSON)** for active data entry (Forms) to protect client-side component state (TomSelect, AutoNumeric).
- **Minimalist & Focused:** Keep the user interface clean, removing unnecessary clutter and focusing on the task at hand.
- **Standardized ERP Workflows:** Follow established patterns for ERP operations (e.g., list-view to detail-view, standard form layouts).
- **Reusable & Generic Components:** 
  - Prioritize reusing existing UI elements. 
  - When new UI is required, design it to be generic and reusable across different modules. Avoid "one-time use" components.
- **Consistency:** Maintain a unified look and feel across all screens, using standard navigation and interaction patterns.
- **Numeric Display Standard:** All numeric values in the UI MUST be displayed with exactly 2 decimal places (e.g., `1,250.50`) for consistency.

## Naming & Domain Concepts
- **Domain-driven Terms:** Use precise business and accounting terminology (e.g., *Journal Entry*, *COGS*, *UOM*, *Facility*, *Container*).
- **Object Naming:** Classes, variables, and database tables should reflect their real-world ERP counterparts.

## Documentation Standards
- **Rationale-focused:** Document the "why" behind architectural decisions (e.g., why AJAX over HTMX for forms).
- **Accessibility:** Keep documentation well-organized and easily accessible within the project structure.

## Technical Specifications
All implementations MUST strictly adhere to the following technical standards:
- **API Response & Envelopes:** [../docs/spec/api-response.md](../docs/spec/api-response.md)
- **Form Submission (Hybrid):** [../docs/spec/form-submission.md](../docs/spec/form-submission.md)
- **Auditing & Trace:** [../docs/spec/auditing.md](../docs/spec/auditing.md)
- **Autocomplete Logic:** [../docs/spec/autocomplete-generic.md](../docs/spec/autocomplete-generic.md)
- **Internationalization (i18n):** [../docs/spec/i18n-guide.md](../docs/spec/i18n-guide.md)
- **Menu & Breadcrumbs:** [../docs/spec/menu-structure.md](../docs/spec/menu-structure.md)
- **Pagination Strategy:** [../docs/spec/pagination.md](../docs/spec/pagination.md)
- **Global Search:** [../docs/spec/search-menu.md](../docs/spec/search-menu.md)
- Code Generation (Sequence): [../docs/spec/sequence-generator.md](../docs/spec/sequence-generator.md)
- Sorting Logic: [../docs/spec/sorting.md](../docs/spec/sorting.md)
- Standardized UI: [../docs/spec/ui-standards.md](../docs/spec/ui-standards.md)
- Layout Standard (Scripts): [../docs/spec/layout-standard.md](../docs/spec/layout-standard.md)
