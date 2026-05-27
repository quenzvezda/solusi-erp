CREATE TABLE `appr_requests` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `reference_type` VARCHAR(50) NOT NULL,
    `reference_id` BIGINT NOT NULL,
    `status` VARCHAR(20) NOT NULL,
    `current_approver_id` BIGINT,
    `version` BIGINT NOT NULL DEFAULT 1,
    `created_by_user_id` BIGINT,
    `created_date` TIMESTAMP(6),
    `updated_by_user_id` BIGINT,
    `updated_date` TIMESTAMP(6),
    PRIMARY KEY (`id`),
    INDEX `idx_appr_reference` (`reference_type`, `reference_id`)
);

CREATE TABLE `appr_histories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `request_id` BIGINT NOT NULL,
    `action` VARCHAR(30) NOT NULL,
    `actor_id` BIGINT NOT NULL,
    `target_approver_id` BIGINT,
    `notes` TEXT,
    `action_date` TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_history_request` FOREIGN KEY (`request_id`) REFERENCES `appr_requests` (`id`)
);
