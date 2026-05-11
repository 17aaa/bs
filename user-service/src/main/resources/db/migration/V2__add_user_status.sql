-- 用户表增加状态字段
ALTER TABLE users ADD COLUMN status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用';
ALTER TABLE users ADD COLUMN ban_reason VARCHAR(500) COMMENT '封禁原因';
ALTER TABLE users ADD COLUMN banned_at TIMESTAMP NULL COMMENT '封禁时间';

-- 创建索引
ALTER TABLE users ADD INDEX idx_status (status);