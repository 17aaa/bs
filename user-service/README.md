# CreativeNFT Market - 学生创意成果 NFT 确权与交易市场

## 项目概述

CreativeNFT Market 是一个基于 Polygon 区块链的学生创意成果 NFT 确权与交易平台，提供以下核心功能：

- **链上确权**：将学生作品哈希上链（Polygon 低成本公链），生成唯一 NFT 证书
- **版本控制**：每次更新自动记录版本历史，展示创意演进过程
- **微交易市场**：支持作品使用权/授权权的微额交易（0.1 元起）
- **创意融资**：项目方可发行粉丝代币，投资自己看好的创意项目

## 技术栈

| 组件 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.5.0 |
| 区块链 | Polygon + Web3j + Solidity |
| 数据库 | MySQL + ShardingSphere |
| 缓存 | Redis |
| 消息队列 | RocketMQ |
| 服务发现 | Nacos |
| 智能合约 | Hardhat + OpenZeppelin |
| 存储 | IPFS |

## 项目结构

```
user-service/
├── src/main/java/com/zhm/springboot/userservice/
│   ├── config/                    # 配置类
│   │   ├── Web3jConfig.java       # Web3j 配置
│   │   └── RedisConfig.java       # Redis 配置
│   ├── blockchain/                # 区块链模块
│   │   ├── config/                # 区块链配置
│   │   ├── security/              # 钱包安全
│   │   ├── service/               # 区块链服务
│   │   └── listener/              # 事件监听
│   ├── nft/                       # NFT 模块
│   │   ├── entity/
│   │   ├── mapper/
│   │   ├── service/
│   │   └── controller/
│   ├── market/                    # 市场交易模块
│   │   ├── entity/
│   │   ├── mapper/
│   │   ├── service/
│   │   └── controller/
│   └── wallet/                    # 钱包模块
│       ├── entity/
│       ├── mapper/
│       ├── service/
│       └── controller/
├── contracts/                     # Solidity 智能合约
│   ├── NFTAsset.sol               # NFT 合约
│   ├── MicroMarket.sol            # 市场合约
│   └── FanToken.sol               # 粉丝代币合约
├── scripts/                       # 部署脚本
│   └── deploy.js
└── resources/
    ├── application.yml            # 主配置
    ├── blockchain.yml             # 区块链配置
    └── db/migration/              # 数据库脚本
```

## 快速开始

### 1. 环境要求

- Java 18+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- MetaMask 钱包

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，配置以下变量
PRIVATE_KEY=your_private_key
POLYGONSCAN_API_KEY=your_api_key
```

### 3. 数据库初始化

```bash
# 创建数据库
mysql -u root -p < src/main/resources/db/migration/V1__init.sql
```

### 4. 启动 Redis

```bash
redis-server
```

### 5. 启动后端服务

```bash
./mvnw spring-boot:run
```

### 6. 部署智能合约

```bash
# 安装依赖
npm install

# 编译合约
npm run compile

# 部署到 Polygon Amoy 测试网
npm run deploy:amoy
```

## API 文档

### NFT 相关

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/nft/mint` | 铸造 NFT |
| POST | `/api/nft/{id}/update` | 更新 NFT 元数据 |
| GET | `/api/nft/{id}` | 获取 NFT 详情 |
| GET | `/api/nft/{id}/versions` | 获取版本历史 |
| GET | `/api/nft/owner/{address}` | 获取用户持有的 NFT |
| GET | `/api/nft/creator/{address}` | 获取用户创作的 NFT |

### 市场相关

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/market/list` | 创建固定价格订单 |
| POST | `/api/market/auction` | 创建荷兰拍卖 |
| POST | `/api/market/buy/{orderId}` | 购买 NFT |
| POST | `/api/market/cancel/{orderId}` | 取消订单 |
| GET | `/api/market/orders` | 获取订单列表 |

### 钱包相关

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/wallet/create` | 创建新钱包 |
| POST | `/api/wallet/sign` | 签名消息 |
| POST | `/api/wallet/verify` | 验证签名 |

## 智能合约

### 合约地址（测试网）

部署后合约地址将自动保存到 `src/main/resources/contracts/contract-addresses.properties`

### 合约验证

```bash
# 验证合约
npm run verify:amoy -- <CONTRACT_ADDRESS>
```

## 开发指南

### 添加新的区块链网络

1. 在 `hardhat.config.js` 中添加网络配置
2. 在 `PolygonNetworkProperties` 中添加网络枚举
3. 更新 `blockchain.yml` 配置

### 添加新的智能合约

1. 在 `contracts/` 目录下创建 `.sol` 文件
2. 运行 `npm run compile` 编译合约
3. 使用 Web3j 生成 Java 绑定类
4. 在 `ContractCallService` 中添加调用方法

### 生成 Web3j 绑定类

```bash
web3j generate solidity \
  --abi-path=./src/main/resources/contracts/NFTAsset.json \
  --bin-path=./src/main/resources/contracts/NFTAsset.bin \
  --output-dir=./src/main/java \
  --package=com.zhm.springboot.userservice.blockchain.contract
```

## 安全注意事项

1. **私钥管理**：永远不要将私钥提交到版本控制
2. **环境隔离**：测试网和生产环境使用不同的密钥
3. **合约审计**：生产环境部署前进行第三方安全审计
4. **数据备份**：定期备份数据库和 Keystore 文件

## 许可证

MIT License