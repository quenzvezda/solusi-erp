# Implementation Report: E2E Purchase Requisition + Approval Flow

> Plan: docs/plans/e2e-pr-approval.md
> Source: Conversation on 2026-05-19 about expanding Playwright E2E to transactional modules
> Created: 2026-05-19

## Findings

## Task 1: D011 dev-seeder for role permissions

- **Status:** findings
- **Summary:** Created `docs/database/dev-seeder/D011__role_permissions.sql` granting ROLE_APPROVER and ROLE_EMPLOYEE the procurement permissions they need on fresh databases.

### Finding: by-role-type endpoint reuses LOOKUP_PARTY
- **Type:** decision
- **Severity:** info
- **Detail:** Plan listed `LOOKUP_PARTY-ROLE-TYPE` as a separate permission for the submit-for-approval modal's approver picker. Verified at `PartyLookupController.searchByRoleType()`: the `/api/lookup/parties/by-role-type` endpoint is gated by the class-level `@PreAuthorize("hasAuthority('LOOKUP_PARTY')")`, not a dedicated permission. Plan was inaccurate.
- **Action taken:** Granted `LOOKUP_PARTY` to ROLE_EMPLOYEE (already in ROLE_APPROVER from D010). No new permission needed.
- **Ref:** src/main/java/com/solusi/erp/master/party/web/controller/PartyLookupController.java:L24, L39-L48

### Finding: LOOKUP_PURCHASING permission does not exist
- **Type:** gap
- **Severity:** info
- **Detail:** Plan called for `LOOKUP_PURCHASING` permission. Searched all migrations — no such permission is defined. Purchase-related lookups use `LOOKUP_PR`, `LOOKUP_PO`, `LOOKUP_SUPPLIER-PRICE-LIST` instead.
- **Action taken:** Replaced `LOOKUP_PURCHASING` with `LOOKUP_PR` and `LOOKUP_PO` in the seeder (and dropped `LOOKUP_PARTY-ROLE-TYPE`).
- **Ref:** src/main/resources/db/migration/V46__Add_Purchasing_Module.sql:L207-L228 — canonical purchasing permissions

