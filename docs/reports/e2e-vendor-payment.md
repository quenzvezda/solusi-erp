# E2E Vendor Payment Implementation Report

## Task 1: Seed E2E data for Vendor Payment

Status: Complete

Changes:
- Added E2E bank, FX loss, and FX gain chart-of-account rows.
- Refreshed `VENDOR_PAYMENT` schema lines in V9000 because V62 runs before E2E-only COA rows exist.
- Added E2E bank account `E2E-BA-001` for IDR payments.
- Added Vendor Payment list and create URLs to shared warmup configuration.

Findings:
- The plan's COA ID mapping was stale after Vendor Bill E2E implementation. Existing `9403` and `9404` are Accounts Payable and Input VAT, so Vendor Payment bank/FX accounts use `9405-9407`.
- `V16__Master_Bank_Account.sql` was not the final table shape. `V17__Refactor_Audit_Columns_To_User_FK.sql` replaces `created_by` with `created_by_user_id`, so the E2E bank account seed uses final audit columns.
- `ID-CITY-JKT` is removed by the Indonesia geographic migration, so the bank account seed selects the first available `CITY_MUNICIPALITY`.

Validation:
- `scripts/check-migration-parity.sh` passed.
- `.\mvnw.cmd -B -Pe2e -DskipTests package` passed.
- E2E jar started with `--spring.profiles.active=e2e`; authenticated admin requests to `/accounts-payable/vendor-payments` and `/accounts-payable/vendor-payments/create` returned 200.
