-- V72: Split Vendor Bill document lifecycle from settlement lifecycle.

ALTER TABLE ap_vendor_bills ADD COLUMN document_status VARCHAR(30) NULL;
ALTER TABLE ap_vendor_bills ADD COLUMN settlement_status VARCHAR(30) NULL;

UPDATE ap_vendor_bills
SET document_status = CASE status
        WHEN 'DRAFT' THEN 'DRAFT'
        WHEN 'CONFIRMED' THEN 'CONFIRMED'
        WHEN 'PARTIAL_PAID' THEN 'CONFIRMED'
        WHEN 'PAID' THEN 'CONFIRMED'
        WHEN 'CANCELLED' THEN 'CANCELLED'
        ELSE status
    END,
    settlement_status = CASE status
        WHEN 'CONFIRMED' THEN 'OPEN'
        WHEN 'PARTIAL_PAID' THEN 'PARTIALLY_SETTLED'
        WHEN 'PAID' THEN 'SETTLED'
        ELSE NULL
    END;

ALTER TABLE ap_vendor_bills ALTER COLUMN document_status SET NOT NULL;

ALTER TABLE ap_vendor_bills DROP COLUMN status;

CREATE INDEX idx_ap_vendor_bills_document_status
    ON ap_vendor_bills (document_status);

CREATE INDEX idx_ap_vendor_bills_settlement_status
    ON ap_vendor_bills (settlement_status);

CREATE INDEX idx_ap_vendor_bills_payable_lookup
    ON ap_vendor_bills (vendor_id, currency_id, document_status, settlement_status);
