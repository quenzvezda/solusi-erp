CREATE TABLE `appr_signatures` (
    `id`             BIGINT NOT NULL AUTO_INCREMENT,
    `request_id`     BIGINT NOT NULL UNIQUE,
    `storage_key`    VARCHAR(500) NOT NULL,
    `bucket_name`    VARCHAR(100) NOT NULL,
    `stored_at`      TIMESTAMP(6) NOT NULL,
    `signer_user_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_sig_request` FOREIGN KEY (`request_id`) REFERENCES `appr_requests` (`id`)
);
