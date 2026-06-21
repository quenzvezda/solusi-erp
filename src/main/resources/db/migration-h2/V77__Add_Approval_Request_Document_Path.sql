-- V77: Store a relative document path for generic approval notification links.

ALTER TABLE appr_requests
    ADD COLUMN document_path VARCHAR(500) NULL;
