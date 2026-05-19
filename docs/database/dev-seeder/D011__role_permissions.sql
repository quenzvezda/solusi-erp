-- Development seeder role permissions for procurement & approval workflows.
-- Runs after D010 (roles) and BEFORE D030 (users). Closes the manual-QA gap
-- where ROLE_APPROVER and ROLE_EMPLOYEE had no PR / SPL / PO / lookup access
-- on a fresh database, forcing tickbox setup via admin UI before testing.

SET @role_approver_id = (SELECT id FROM roles WHERE name = 'ROLE_APPROVER');
SET @role_warehouse_id = (SELECT id FROM roles WHERE name = 'ROLE_WAREHOUSE');
SET @role_employee_id = (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE');

-- ROLE_APPROVER: Procurement review/approval (read everywhere, update for cancel)
-- Does NOT get PR_CREATE / PR_SUBMIT — approver reviews, requester submits.
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_approver_id, id FROM permissions WHERE name IN (
    -- Purchase Requisition
    'PR_READ', 'PR_UPDATE',
    'LOOKUP_PR',
    -- Purchase Order
    'PO_READ', 'PO_UPDATE',
    'LOOKUP_PO',
    -- Supplier Price List (read-only reference)
    'SPL_READ',
    'LOOKUP_SUPPLIER-PRICE-LIST',
    -- Inventory & master lookups needed to render PR/PO autocompletes
    'LOOKUP_INVENTORY',
    'LOOKUP_BRAND',
    'LOOKUP_PRODUCT-CATEGORY',
    'LOOKUP_FACILITY',
    'LOOKUP_UOM-CONVERSION'
);

-- ROLE_EMPLOYEE: Can request and submit PR, view supporting data
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_employee_id, id FROM permissions WHERE name IN (
    -- Purchase Requisition (full requester lifecycle)
    'PR_READ', 'PR_CREATE', 'PR_UPDATE', 'PR_DELETE', 'PR_SUBMIT',
    'LOOKUP_PR',
    -- Read references for PR creation
    'SPL_READ',
    'LOOKUP_SUPPLIER-PRICE-LIST',
    'LOOKUP_INVENTORY',
    'LOOKUP_BRAND',
    'LOOKUP_PRODUCT-CATEGORY',
    'LOOKUP_FACILITY',
    'LOOKUP_UOM-CONVERSION',
    -- Party lookup is shared (LOOKUP_PARTY also gates by-role-type endpoint)
    'LOOKUP_PARTY'
);
