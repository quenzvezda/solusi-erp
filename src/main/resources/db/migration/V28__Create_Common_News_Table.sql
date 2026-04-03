CREATE TABLE `common_news` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT NOT NULL,
    `status` VARCHAR(20) NOT NULL,
    `publish_date` DATETIME(6),
    `expiry_date` DATETIME(6),
    `author` VARCHAR(100),
    `version` BIGINT NOT NULL DEFAULT 1,
    `created_by_user_id` BIGINT,
    `created_date` DATETIME(6),
    `updated_by_user_id` BIGINT,
    `updated_date` DATETIME(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_news_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
