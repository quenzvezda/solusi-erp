# Product Guidelines: Solusi Program ERP

## Prose & Communication
- **Style:** Technical & Precise. Use clear, concise language that prioritizes accuracy and technical depth.
- **Tone:** Professional and objective, suitable for developers and ERP power users.
- **System Messages:** Error messages and notifications should be informative, providing clear reasons and potential actions.

## UI/UX Principles
- **Minimalist & Focused:** Keep the user interface clean, removing unnecessary clutter and focusing on the task at hand.
- **Standardized ERP Workflows:** Follow established patterns for ERP operations (e.g., list-view to detail-view, standard form layouts).
- **Reusable & Generic Components:** 
  - Prioritize reusing existing UI elements. 
  - When new UI is required, design it to be generic and reusable across different modules. Avoid "one-time use" components.
- **Consistency:** Maintain a unified look and feel across all screens, using standard navigation and interaction patterns.

## Naming & Domain Concepts
- **Domain-driven Terms:** Use precise business and accounting terminology (e.g., *Journal Entry*, *COGS*, *UOM*, *Facility*, *Container*).
- **Object Naming:** Classes, variables, and database tables should reflect their real-world ERP counterparts.

## Documentation Standards
- **Rationale-focused:** Document the "why" behind architectural and design decisions, not just the "how".
- **Example-driven:** Provide clear, functional code examples for common patterns, particularly for complex logic like COGS calculation or journal entry generation.
- **Accessibility:** Keep documentation well-organized and easily accessible within the project structure.
