# Copilot Instructions — solusi-program-erp

Monolithic ERP system (SSR, no SPA). **Before starting any task, read the mandatory docs below.**

---

## Agent Behavior Rules

- **Check available skills first.** At the start of every session/prompt, inspect which skills are available and invoke the relevant one before proceeding.
- **Clarify before acting.** If a prompt is ambiguous or you are unsure about scope/intent, use the `ask_user` tool to ask before writing any code.
- **Never close a session unilaterally.** Use `ask_user` to confirm with the user before treating a session as complete.
- **Commit messages must not include a `Co-authored-by` trailer.** Write clean Conventional Commits with no auto-appended co-author lines.

---

## Mandatory Reading

Read these **before** exploring any source code:

| Document | Purpose |
|---|---|
| [`docs/AGENTS.md`](../docs/AGENTS.md) | **Primary guide** — tech stack, architecture, coding standards, RBAC, i18n, versioning, commit protocol. Read this first. |
| [`docs/index.md`](../docs/index.md) | Navigation map for all technical and functional documentation. |
| [`docs/spec/index.md`](../docs/spec/index.md) | Index of all horizontal/shared technical specs (pagination, sorting, i18n, autocomplete, numeric input, date/time, etc.). |

---

## Documentation Map

Use these folders when a task touches a specific concern:

| Folder | When to read |
|---|---|
| [`docs/architecture/`](../docs/architecture/) | Clean Architecture layers, DDD/CQRS standard, BaseModel pattern, Smart Delete, JaCoCo coverage |
| [`docs/spec/`](../docs/spec/) | Any UI component or shared technical pattern (autocomplete, HTMX, pagination, sorting, numeric input, date input, layout, i18n, form submission, error handling) |
| [`docs/modules/`](../docs/modules/) | Business rules for a specific module (Accounting, Procurement, Inventory, etc.) |
| [`docs/tests/`](../docs/tests/) | Web-layer testing guide, Playwright smoke test guide (TomSelect / Flatpickr / AutoNumeric / Line Items interaction dictionary) |
| [`docs/deployment/`](../docs/deployment/) | Local dev setup (Windows/Linux), VPS deployment, Docker/MinIO setup |

---

## Build & Test (Quick Reference)

```bash
# Start dependencies
docker compose up -d

# Run app
.\mvnw.cmd spring-boot:run          # Windows
./mvnw spring-boot:run              # Linux/macOS

# Fast tests (unit + static — excludes @Tag("integration-template"))
./mvnw test -DexcludedGroups=integration-template

# Full test suite
./mvnw test

# Single test class
./mvnw test -Dtest=TaxCommandUseCaseTest

# Single test method
./mvnw test -Dtest=TaxCommandUseCaseTest#shouldCreateTax
```

---

## Reference Implementations

When creating a new module, use these as templates (details in `docs/architecture/clean-ddd-cqrs-standard.md`):

| Module | Package | Use for |
|---|---|---|
| Tax | `master.tax` | Simplest CRUD baseline |
| Brand | `inventory.brand` | Golden reference for web-layer tests |
| Currency | `master.currency` | Business logic in domain |
| Role | `security.role` | Many-to-many via reference ID |
| User | `security.user` | Cross-module reference + security integration |
