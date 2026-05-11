-- 添加 nft_asset_id 字段到 market_orders 表
ALTER TABLE market_orders ADD COLUMN nft_asset_id BIGINT COMMENT 'NFT 资产 ID（关联本地 NFT 记录）' AFTER token_id;

-- 添加索引
ALTER TABLE market_orders ADD INDEX idx_nft_asset (nft_asset_id);