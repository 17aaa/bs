# CreativeNFT Market - 学生创意成果 NFT 确权与交易市场

## 项目简介

一个基于 Spring Boot + React + Polygon 区块链的 NFT 交易平台，支持学生创意作品的铸造、展示和交易。

### 核心功能

- **NFT 铸造**：将创意作品哈希上链，生成唯一 NFT 证书
- **版本控制**：每次更新自动记录版本历史
- **微交易市场**：支持固定价格销售和荷兰拍卖
- **粉丝代币**：项目方可发行粉丝代币进行创意融资

---

## 技术栈

### 后端
- **框架**: Spring Boot 3.5.0
- **区块链**: Web3j 4.12.2 + Polygon (Amoy 测试网)
- **数据库**: MySQL + ShardingSphere 5.5.2
- **缓存**: Redis (Lettuce 连接池)
- **ORM**: MyBatis-Plus 3.5.7
- **消息队列**: RocketMQ

### 前端
- **框架**: React 18 + Vite 6
- **路由**: React Router DOM
- **HTTP**: Axios
- **Web3**: Ethers.js
- **样式**: Styled Components

### 智能合约
- **语言**: Solidity 0.8.20
- **标准**: ERC-721 (NFT), ERC-20 (Fan Token)
- **框架**: Hardhat + OpenZeppelin Contracts v4.9.0
- **部署目标**: Polygon Amoy 测试网 (Chain ID: 80002)

---

## 快速开始

### 1. 环境准备

```bash
# 要求
- JDK 18+
- Node.js 18+
- MySQL 8.0+
- Redis (可选)
```

### 2. 数据库初始化

```sql
CREATE DATABASE creativenft_db;
USE creativenft_db;
-- 运行 src/main/resources/V1__init.sql
```

### 3. 后端启动

```bash
cd /Users/zhm/Desktop/user-service

# 安装依赖
mvn clean install

# 启动服务
mvn spring-boot:run
```

访问：http://localhost:8085

### 4. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问：http://localhost:3000

### 5. 智能合约部署

#### 本地测试网络

```bash
# 编译合约
npx hardhat compile

# 部署到本地 Hardhat 网络
npx hardhat run scripts/deploy-local.js --network hardhat

# 运行测试
npx hardhat run scripts/simple-test.js --network hardhat
```

#### Polygon Amoy 测试网

```bash
# 1. 复制 .env.example 为 .env
cp .env.example .env

# 2. 编辑 .env，填入你的私钥

# 3. 获取测试网 MATIC
# https://faucet.polygon.technology/

# 4. 部署合约
npm run deploy:amoy

# 5. 验证合约（可选）
npm run verify:amoy <CONTRACT_ADDRESS>
```

---

## 项目结构

```
user-service/
├── src/main/java/com/zhm/springboot/userservice/
│   ├── config/                 # 配置类
│   ├── blockchain/             # 区块链模块
│   │   ├── config/            # 区块链配置
│   │   ├── contract/          # 合约包装类
│   │   ├── security/          # 钱包安全管理
│   │   └── service/           # 区块链服务
│   ├── nft/                    # NFT 模块
│   │   ├── entity/            # 实体类
│   │   ├── mapper/            # MyBatis Mapper
│   │   ├── service/           # 业务服务
│   │   └── controller/        # REST API
│   ├── market/                 # 市场交易模块
│   └── fantoken/               # 粉丝代币模块
├── contracts/                  # Solidity 合约
│   ├── NFTAsset.sol           # NFT 合约
│   ├── MicroMarket.sol        # 市场合约
│   └── FanToken.sol           # 粉丝代币合约
├── scripts/                    # 部署脚本
│   ├── deploy.js              # 测试网部署
│   ├── deploy-local.js        # 本地部署
│   └── test-contract.js       # 合约测试
├── frontend/                   # React 前端
│   ├── src/
│   │   ├── components/        # 组件
│   │   ├── pages/             # 页面
│   │   └── services/          # API 服务
│   └── package.json
└── pom.xml                     # Maven 配置
```

---

## API 端点

### NFT 相关

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/nft/mint` | 铸造 NFT |
| GET  | `/api/nft/{id}` | 获取 NFT 详情 |
| GET  | `/api/nft/{id}/versions` | 版本历史 |
| GET  | `/api/nft/owner/{address}` | 用户持有的 NFT |
| GET  | `/api/nft/creator/{address}` | 用户创作的 NFT |
| POST | `/api/nft/{id}/update` | 更新元数据 |

### 市场相关

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/market/list` | 创建销售订单 |
| POST | `/api/market/buy/{orderId}` | 购买 NFT |
| POST | `/api/market/cancel/{orderId}` | 取消订单 |
| GET  | `/api/market/orders` | 订单列表 |
| GET  | `/api/market/order/{id}` | 订单详情 |

### 钱包相关

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/wallet/bind` | 绑定钱包 |
| GET  | `/api/wallet/address/{userId}` | 获取钱包地址 |

---

## 配置说明

### application.yml

```yaml
# 区块链配置
blockchain:
  rpc-url: https://rpc-amoy.polygon.technology/
  chain-id: 80002
  gas-price: 30000000000  # 30 Gwei
  gas-limit: 500000
  platform-private-key: ${BLOCKCHAIN_PLATFORM_PRIVATE_KEY}
  contract:
    nft-asset-address: ${NFT_ASSET_CONTRACT_ADDRESS}
    micro-market-address: ${MICRO_MARKET_CONTRACT_ADDRESS}
    fan-token-address: ${FAN_TOKEN_CONTRACT_ADDRESS}
```

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `BLOCKCHAIN_PLATFORM_PRIVATE_KEY` | 平台钱包私钥 | Hardhat 默认私钥 |
| `NFT_ASSET_CONTRACT_ADDRESS` | NFT 合约地址 | 本地部署地址 |
| `MICRO_MARKET_CONTRACT_ADDRESS` | 市场合约地址 | - |
| `FAN_TOKEN_CONTRACT_ADDRESS` | 粉丝代币合约地址 | - |

---

## 测试

### 单元测试

```bash
mvn test
```

### 合约测试

```bash
# 本地网络测试
npx hardhat run scripts/simple-test.js --network hardhat

# 完整功能测试
npx hardhat run scripts/test-contract.js --network hardhat
```

---

## 开发进度

### 已完成
- ✅ 基础环境搭建（Spring Boot + Web3j + Hardhat）
- ✅ 智能合约开发（NFTAsset, MicroMarket, FanToken）
- ✅ 后端服务实现（NftService, MarketService）
- ✅ 前端页面开发（首页、详情、创作、市场、个人中心）
- ✅ 钱包管理服务（创建、助记词、Keystore）
- ✅ 合约调用集成（ContractCallService）
- ✅ 本地部署脚本和测试脚本

### 待完成
- ⏳ 部署到 Polygon Amoy 测试网
- ⏳ Redis 缓存集成
- ⏳ 完整的事件监听
- ⏳ IPFS 元数据存储
- ⏳ 端到端集成测试

---

## 常见问题

### 1. 无法连接 Nacos

如果在本地开发，已在 `application.yml` 中禁用 Nacos：
```yaml
spring:
  cloud:
    nacos:
      config:
        enabled: false
      discovery:
        enabled: false
```

### 2. ShardingSphere 表不存在

确保所有表已在 `sharding.yaml` 中配置：
```yaml
tables:
  users:
    actualDataNodes: ds0.users
  nft_assets:
    actualDataNodes: ds0.nft_assets
  # ... 其他表
```

### 3. 合约部署失败

检查：
- 私钥是否正确
- 账户是否有足够的测试网 MATIC
- RPC URL 是否可访问

---

## 许可证

MIT