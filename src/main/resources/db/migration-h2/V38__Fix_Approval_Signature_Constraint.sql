-- Allow multiple signatures per approval request (for multi-step approvals like APPROVE_AND_FORWARD + APPROVE_AND_FINISH)
-- H2: DROP CONSTRAINT instead of DROP FOREIGN KEY / DROP INDEX
ALTER TABLE appr_signatures DROP CONSTRAINT IF EXISTS fk_sig_request;
ALTER TABLE appr_signatures DROP CONSTRAINT IF EXISTS request_id;
ALTER TABLE appr_signatures ADD CONSTRAINT fk_sig_request FOREIGN KEY (request_id) REFERENCES appr_requests (id);
