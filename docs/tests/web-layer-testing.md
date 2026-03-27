# Web-layer Testing Guidelines

This document describes the recommended standard for testing the web layer (Controller + Thymeleaf templates) in the Inventory module.

Goals:
- Fast, deterministic controller unit tests (no Spring context).
- Representative runtime template render tests (fragment-level) to ensure templates do not fail at render time.
- Static template checks to catch property typos early.
- Shared test utilities to reduce duplication.

Patterns

1. Controller Unit Tests (fast, isolated)
- Instantiate controller class directly and inject Mockito mocks for all dependencies (usecases, mappers, messageSource).
- Use `ExtendedModelMap` for Model and call controller methods directly.
- Assert view name and model attributes; verify mapping from domain to DTO via mocked mapper.

2. MockMvc / Surgical Integration (use sparingly)
- Use `MockMvc` standaloneSetup for tests that require HTTP pipeline (form binding, validation).
- Reserve `@WebMvcTest` for broader controller integration checks.

3. Template Tests (two layers)
- Static property scan: scan template file(s) for `${item.<prop>}` and ensure the DTO has the field or getter. Fast and catches typos.
- Runtime fragment render: use `testutils.TemplateTestUtils` to render specific template fragment (e.g. `inventory/brands/list :: brand-table-container`) with a sample `page` variable (use `TestPageBuilder` or simple `PageImpl`). Assert rendered output is non-empty and contains expected markers.

Test Utilities
- `com.solusi.erp.testutils.TemplateTestUtils` — Lightweight Thymeleaf TemplateEngine configured to load templates from classpath `templates/` and render fragments.
- `com.solusi.erp.testutils.TestDtoFactory` — Small factory for building representative DTOs (Brand/Product) used in render tests.
- `com.solusi.erp.testutils.TestPageBuilder` — Helper to create `org.springframework.data.domain.Page` objects for templates expecting `page` model variable.

CI / Test grouping
- Tag fast unit tests as `unit` (default). Run on every commit/PR.
- Tag runtime render tests as `integration` or `render` and run in a separate CI job (nightly or heavy PR checks).

Notes
- If templates depend heavily on Spring Dialects (Security, i18n messages), runtime render may need additional dialect configuration. Fallback: keep such tests in the slower integration suite that boots a Spring context.
