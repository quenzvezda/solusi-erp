-- Allow multiple signatures per approval request (for multi-step approvals like APPROVE_AND_FORWARD + APPROVE_AND_FINISH)
-- Must drop FK before dropping the implicit unique index, then re-add FK as non-unique
ALTER TABLE `appr_signatures` DROP FOREIGN KEY `fk_sig_request`;
ALTER TABLE `appr_signatures` DROP INDEX `request_id`;
ALTER TABLE `appr_signatures` ADD CONSTRAINT `fk_sig_request` FOREIGN KEY (`request_id`) REFERENCES `appr_requests` (`id`);
