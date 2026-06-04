ALTER TABLE inv_goods_issues
    ADD COLUMN cancelled_date DATE NULL AFTER status,
    ADD COLUMN cancel_reason TEXT NULL AFTER cancelled_date;
