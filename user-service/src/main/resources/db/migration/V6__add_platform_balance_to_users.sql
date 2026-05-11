-- 为 users 表添加平台积分余额字段（注册赠送 100 积分，单位 Wei，100*10^18）
ALTER TABLE users
    ADD COLUMN platform_balance DECIMAL(65, 0) NOT NULL DEFAULT 0 COMMENT '平台积分余额（Wei单位）';

UPDATE users SET platform_balance = '100000000000000000000' WHERE platform_balance = 0;
