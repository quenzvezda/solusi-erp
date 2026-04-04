ALTER TABLE `appr_histories`
    ADD COLUMN `signature_key` VARCHAR(500) NULL COMMENT 'Storage key of the digital signature image, if captured during this action';
