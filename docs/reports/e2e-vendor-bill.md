# E2E Vendor Bill Implementation Report

## Task 1: Seed E2E data for Vendor Bill

Status: Complete

Changes:
- Added E2E Accounts Payable and Input VAT chart-of-account rows to `V9000__e2e_seed_data.sql`.
- Added active `VENDOR_BILL` accounting schema with GRIR clearing, tax, and AP total lines.
- Added Vendor Bill list and reference-selection URLs to shared warmup configuration.

Validation:
- `scripts/check-migration-parity.sh` passed.
- `.\mvnw.cmd -B -Pe2e -DskipTests package` passed.
