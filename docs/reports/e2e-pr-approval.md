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

## Task 2: Mirror D011 + dev users + transactional master data into V9000

- **Status:** findings
- **Summary:** Expanded `V9000__e2e_seed_data.sql` from 30 lines to a full mirror of D010 + D011 + D020 (subset) + D030, plus transactional master (facility, products, supplier price list) needed by PR flow. App starts cleanly, login works for approver1 and employee1, employee1 can hit `/purchasing/purchase-requisitions` (HTTP 200).

### Finding: products table uses created_by_user_id not created_by
- **Type:** deviation
- **Severity:** info
- **Detail:** Initial V9000 INSERT for products used `created_by VARCHAR='SYSTEM'` matching the V7 schema. App failed to start with `Column "created_by" not found` because V17 (Refactor_Audit_Columns_To_User_FK) renamed it to `created_by_user_id BIGINT` and dropped the old column. Plan steps were not specific about column names.
- **Action taken:** Updated V9000 INSERT to use `created_by_user_id, created_date, version` matching post-V17 schema. Other tables (unit_of_measures, brands, product_categories, parties, master_currencies) were already correct because the existing V9000 used the new column names.
- **Ref:** src/main/resources/db/migration/V17__Refactor_Audit_Columns_To_User_FK.sql:L102-L114

### Finding: full D020 mirror skipped (party addresses, identifications, contacts)
- **Type:** decision
- **Severity:** info
- **Detail:** D020 seeds ~200 lines including party_addresses, party_identifications, party_contacts, party_address_types, party_role types per party. PR flow does not require any of these — only `parties.id`, `party_role_types`, and `party_roles` are referenced.
- **Action taken:** Mirrored only the minimum (5 parties, party_role_types APPROVER, party_roles links). Future tests that need address/identification can extend V9000 incrementally.

### Finding: ROLE_ADMIN-only roles in production migrations
- **Type:** decision
- **Severity:** info
- **Detail:** Production migrations only insert ROLE_ADMIN. ROLE_APPROVER, ROLE_WAREHOUSE, ROLE_EMPLOYEE come from dev seeder D010. Without mirroring D010 to V9000, the user FK to role_id would fail.
- **Action taken:** Added a "ROLES (mirror D010)" section to V9000 before users insert. Also mirrored D010's role_permissions for the three new roles to keep behavior identical to manual QA.

### Finding: facility owner_id must reference an existing party
- **Type:** decision
- **Severity:** info
- **Detail:** `inv_facilities.owner_id` is NOT NULL with FK to parties. Plan was vague about who should own the facility.
- **Action taken:** Used BP-DEV-SUP01 as the facility owner. Not realistic but works for E2E. Can be revisited if a "company internal" party is added later.
- **Ref:** src/main/resources/db/migration/V21__Inventory_Warehouse_Hierarchy.sql:L24

## Task 3: Validate seed change does not break existing E2E specs

- **Status:** clean
- **Summary:** Full Playwright suite ran green: 18/18 passed in 2m43s on first attempt with 0 retries used. No regression from V9000 expansion. Existing specs use `uniqueName()` for create flows and stable seed ids 9001-9002 for TomSelect autofill, both unaffected by the new seed.
