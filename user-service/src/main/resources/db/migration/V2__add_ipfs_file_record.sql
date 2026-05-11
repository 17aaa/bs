-- IPFS 文件记录表
-- 用于追踪和管理上传到 IPFS 的文件

CREATE TABLE IF NOT EXISTS ipfs_file_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    cid VARCHAR(100) NOT NULL COMMENT 'IPFS Content Identifier',
    filename VARCHAR(255) COMMENT '文件名',
    file_type VARCHAR(50) COMMENT '文件类型 (image, json, video, audio, other)',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    mime_type VARCHAR(100) COMMENT 'MIME 类型',
    pinned BOOLEAN DEFAULT FALSE COMMENT '是否已固定',
    pinned_at TIMESTAMP COMMENT '固定时间',
    user_id BIGINT COMMENT '上传者用户ID',
    owner_address VARCHAR(42) COMMENT '上传者钱包地址',
    nft_asset_id BIGINT COMMENT '关联的 NFT ID',
    description TEXT COMMENT '文件描述',
    tags VARCHAR(500) COMMENT '标签，逗号分隔',
    access_count INT DEFAULT 0 COMMENT '访问次数',
    last_accessed_at TIMESTAMP COMMENT '最后访问时间',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active(正常), deleted(已删除), expired(已过期)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除（逻辑删除）',
    UNIQUE KEY uk_cid (cid),
    KEY idx_user_id (user_id),
    KEY idx_owner_address (owner_address),
    KEY idx_file_type (file_type),
    KEY idx_nft_asset_id (nft_asset_id),
    KEY idx_status (status),
    KEY idx_pinned (pinned),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IPFS 文件记录表';
