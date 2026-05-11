-- CreativeNFT Market 数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS creativenft_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE creativenft_db;

-- NFT 资产表
CREATE TABLE IF NOT EXISTS nft_assets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    token_id BIGINT NOT NULL COMMENT 'Token ID（链上）',
    contract_address VARCHAR(42) NOT NULL COMMENT '合约地址',
    owner_address VARCHAR(42) NOT NULL COMMENT '所有者地址',
    creator_address VARCHAR(42) NOT NULL COMMENT '创作者地址',
    name VARCHAR(200) NOT NULL COMMENT '作品名称',
    description TEXT COMMENT '作品描述',
    category VARCHAR(50) COMMENT '分类',
    current_metadata_hash VARCHAR(100) COMMENT '当前元数据哈希（IPFS CID）',
    current_version INT DEFAULT 1 COMMENT '当前版本号',
    royalty_fee INT DEFAULT 500 COMMENT '版税比例（万分比）',
    royalty_recipient VARCHAR(42) COMMENT '版税接收地址',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-下架 3-冻结',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_token_contract (token_id, contract_address),
    KEY idx_owner (owner_address),
    KEY idx_creator (creator_address),
    KEY idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NFT 资产表';

-- NFT 版本历史表
CREATE TABLE IF NOT EXISTS nft_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    nft_asset_id BIGINT NOT NULL COMMENT 'NFT 资产 ID',
    version INT NOT NULL COMMENT '版本号',
    metadata_hash VARCHAR(100) NOT NULL COMMENT '元数据哈希（IPFS CID）',
    ipfs_uri VARCHAR(200) COMMENT 'IPFS URI',
    change_description TEXT COMMENT '变更描述',
    updater_address VARCHAR(42) NOT NULL COMMENT '更新者地址',
    tx_hash VARCHAR(66) COMMENT '交易哈希',
    block_number BIGINT COMMENT '区块号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_nft_version (nft_asset_id, version),
    KEY idx_updater (updater_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NFT 版本历史表';

-- 市场订单表
CREATE TABLE IF NOT EXISTS market_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    order_id VARCHAR(64) NOT NULL COMMENT '订单 ID（业务唯一标识）',
    sale_id BIGINT COMMENT '链上 Sale ID',
    seller_address VARCHAR(42) NOT NULL COMMENT '卖家地址',
    buyer_address VARCHAR(42) COMMENT '买家地址',
    nft_contract VARCHAR(42) NOT NULL COMMENT 'NFT 合约地址',
    token_id BIGINT NOT NULL COMMENT 'Token ID',
    order_type TINYINT NOT NULL COMMENT '订单类型：1-固定价格 2-荷兰拍卖 3-报价',
    status TINYINT DEFAULT 1 COMMENT '状态：1-活跃 2-已售 3-取消 4-过期',
    price BIGINT NOT NULL COMMENT '价格（Wei 单位）',
    start_price BIGINT COMMENT '拍卖起始价',
    reserve_price BIGINT COMMENT '拍卖保留价',
    payment_token VARCHAR(42) COMMENT '支付代币地址（0x0 = MATIC）',
    end_time BIGINT COMMENT '结束时间（时间戳）',
    royalty_fee INT COMMENT '版税比例（万分比）',
    royalty_recipient VARCHAR(42) COMMENT '版税接收地址',
    final_price BIGINT COMMENT '最终成交价',
    tx_hash VARCHAR(66) COMMENT '交易哈希',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_id (order_id),
    KEY idx_seller (seller_address),
    KEY idx_nft (nft_contract, token_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='市场订单表';

-- 粉丝代币表
CREATE TABLE IF NOT EXISTS fan_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    token_address VARCHAR(42) NOT NULL COMMENT '代币地址',
    project_id BIGINT NOT NULL COMMENT '项目 ID',
    creator_address VARCHAR(42) NOT NULL COMMENT '创作者地址',
    name VARCHAR(100) NOT NULL COMMENT '代币名称',
    symbol VARCHAR(20) NOT NULL COMMENT '代币符号',
    total_supply BIGINT NOT NULL COMMENT '总供应量',
    public_sale_remaining BIGINT COMMENT '公募剩余量',
    public_sale_price BIGINT COMMENT '公募价格（Wei）',
    public_sale_active BOOLEAN DEFAULT FALSE COMMENT '公募是否激活',
    contract_address VARCHAR(42) COMMENT '粉丝代币合约地址',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-暂停 3-结束',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_token_address (token_address),
    KEY idx_project (project_id),
    KEY idx_creator (creator_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='粉丝代币表';

-- 质押记录表
CREATE TABLE IF NOT EXISTS stake_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    user_address VARCHAR(42) NOT NULL COMMENT '用户地址',
    token_address VARCHAR(42) NOT NULL COMMENT '代币地址',
    staked_amount BIGINT NOT NULL COMMENT '质押数量',
    reward_earned BIGINT DEFAULT 0 COMMENT '累计奖励',
    last_update_time BIGINT COMMENT '最后更新时间（时间戳）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_token (user_address, token_address),
    KEY idx_user (user_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质押记录表';

-- 用户钱包表
CREATE TABLE IF NOT EXISTS wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    wallet_address VARCHAR(42) NOT NULL COMMENT '钱包地址',
    keystore_path VARCHAR(200) COMMENT 'Keystore 路径',
    is_bound BOOLEAN DEFAULT FALSE COMMENT '是否已绑定',
    bound_at TIMESTAMP COMMENT '绑定时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user (user_id),
    UNIQUE KEY uk_wallet (wallet_address),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包表';