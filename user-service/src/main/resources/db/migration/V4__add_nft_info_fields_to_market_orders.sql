-- 添加 NFT 信息冗余字段到 market_orders 表（用于展示）
ALTER TABLE market_orders
ADD COLUMN nft_name VARCHAR(255) COMMENT 'NFT 名称（冗余字段）' AFTER nft_asset_id,
ADD COLUMN nft_description TEXT COMMENT 'NFT 描述（冗余字段）' AFTER nft_name,
ADD COLUMN nft_image_url VARCHAR(500) COMMENT 'NFT 图片 URL（冗余字段）' AFTER nft_description,
ADD COLUMN category VARCHAR(50) COMMENT 'NFT 分类（冗余字段）' AFTER nft_image_url,
ADD COLUMN creator_address VARCHAR(42) COMMENT 'NFT 创作者地址（冗余字段）' AFTER category;