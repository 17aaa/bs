# CreativeNFT Market - 学生创意成果 NFT 交易平台

## 项目简介

一个基于 React + Spring Boot 的 NFT 交易平台，支持学生创意作品的铸造、展示和交易。

## 技术栈

### 前端
- React 18
- Vite 6
- React Router DOM
- Axios
- Ethers.js (Web3)
- Styled Components

### 后端
- Spring Boot 3.5.0
- MyBatis-Plus
- ShardingSphere
- MySQL
- Redis

### 区块链
- Solidity 0.8.20
- Polygon (Amoy 测试网)
- ERC-721 (NFT)
- ERC-20 (Fan Token)

## 快速开始

### 前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:3000

### 后端

```bash
# 确保 MySQL 运行
mvn spring-boot:run
```

API 端点：http://localhost:8085

## 功能特性

### NFT 管理
- 铸造 NFT
- 版本控制
- 元数据管理
- 创作者确权

### 市场交易
- 固定价格销售
- 荷兰拍卖
- 报价系统

### 粉丝代币
- 代币发行
- 公募销售
- 质押挖矿

## 页面结构

```
frontend/src/
├── components/
│   ├── NFTCard.jsx
│   └── WalletButton.jsx
├── pages/
│   ├── HomePage.jsx        # 首页
│   ├── NFTDetailPage.jsx   # NFT 详情
│   ├── CreatePage.jsx      # 创作 NFT
│   ├── MarketPage.jsx      # 市场
│   └── ProfilePage.jsx     # 个人中心
├── services/
│   ├── api.js              # API 调用
│   └── web3.js             # Web3 钱包
└── App.jsx
```

## API 端点

### NFT
- POST `/api/nft/mint` - 铸造 NFT
- GET `/api/nft/{id}` - 获取 NFT 详情
- GET `/api/nft/{id}/versions` - 版本历史
- GET `/api/nft/owner/{address}` - 用户持有的 NFT
- GET `/api/nft/creator/{address}` - 用户创作的 NFT
- POST `/api/nft/{id}/update` - 更新元数据

### 市场
- POST `/api/market/list` - 创建订单
- POST `/api/market/buy/{orderId}` - 购买
- POST `/api/market/cancel/{orderId}` - 取消订单
- GET `/api/market/orders` - 订单列表
- GET `/api/market/order/{id}` - 订单详情

## 钱包连接

支持 MetaMask 钱包连接，切换到 Polygon Amoy 测试网 (Chain ID: 80002)。

## 开发

```bash
# 前端
npm run dev      # 开发模式
npm run build    # 生产构建
npm run preview  # 预览构建

# 后端
mvn clean install
mvn spring-boot:run
```

## 许可证

MIT