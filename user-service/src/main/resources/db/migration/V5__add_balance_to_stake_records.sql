-- 为 stake_records 表添加 balance 字段（可用余额，未质押部分）
ALTER TABLE stake_records
    ADD COLUMN balance BIGINT NOT NULL DEFAULT 0 COMMENT '可用余额（未质押部分）' AFTER reward_earned;
