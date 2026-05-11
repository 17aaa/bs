# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

**CreativeNFT Market** — 学生创意成果 NFT 确权与交易平台，基于 Polygon 区块链。四个子项目在同一仓库：Spring Boot 后端、用户前端、管理后台、Solidity 智能合约。

## 常用命令

### 后端（Spring Boot / Maven · Java 17 · 端口 8085）

```bash
mvn spring-boot:run
mvn clean package -DskipTests
mvn test
mvn test -Dtest=SomeTest#methodName
```

> `/test` 目录当前为空，无 JUnit 测试文件，`mvn test` 会直接通过。

### 前端（React 19 / Vite 8）

```bash
cd frontend
npm run dev      # 用户界面，端口 3000，代理到 localhost:8085
npm run build
npm run lint

cd admin-web
npm run dev      # 管理后台，端口 5180，代理到 localhost:8085
npm run build
```

### 智能合约（Hardhat）

```bash
npm run compile          # 编译 Solidity，产物输出到 src/main/resources/contracts/
npm run test             # 合约测试
npm run deploy:local     # 本地 Hardhat 网络（Chain ID 31337）
npm run deploy:amoy      # Polygon Amoy 测试网（Chain ID 80002，需 .env 私钥）
npm run deploy:polygon   # Polygon 主网（Chain ID 137）
npm run verify:amoy <CONTRACT_ADDRESS>
```

### Docker Compose（全栈本地环境）

```bash
docker-compose up -d       # MySQL + Redis + MinIO + Backend + Frontend
docker-compose logs -f backend
docker-compose down -v     # 停止并清除卷数据
```

## 架构概览

```
前端 (React/Ethers.js) ──HTTP/REST──▶ Spring Boot 后端 ──Web3j RPC──▶ Polygon 区块链
                                              │
                          ┌───────────────────┼───────────────────┐
                       MySQL 8.0          Redis 7.0            MinIO / IPFS
                     (ShardingSphere)    (会话/缓存)          (文件/元数据)
                                              │
                                         RocketMQ
                                       (NFT 铸造队列)
```

### 后端模块（包路径：`com.zhm.springboot.userservice`）

| 模块 | 职责 | API 前缀 |
|------|------|----------|
| `blockchain` | Web3j 集成、合约调用（读/写）、IPFS、Gas 监控 | `/api/ipfs`, `/api/blockchain` |
| `nft` | NFT 铸造任务队列（RocketMQ 异步）、版本历史、审核流程 | `/api/nft`, `/api/nft/review`, `/api/nft/mint-task` |
| `market` | 交易订单（固定价格、荷兰拍卖） | `/api/market` |
| `wallet` | 用户钱包绑定、助记词恢复、挑战-验证签名登录 | `/api/wallet` |
| `fantoken` | ERC-20 粉丝代币、质押/奖励（Vesting） | `/api/fan-token` |
| `admin` | 后台管理：用户、NFT 审核、仪表板统计 | `/admin/*` |
| `notification` | 交易通知与链上事件推送 | `/api/notifications` |
| `audit` | 操作日志追踪 | `/api/audit-logs` |

共享基础设施：`config/`（全局配置）、`common/`（ApiResponse）、`filter/`（XSS 过滤）、`interceptor/`（限流 + Admin 鉴权）、`scheduled/`（定时任务）、`mq/`（RocketMQ 生产者/事务监听）。

#### blockchain 模块核心类

- `ContractCallService` — 只读合约调用（view/pure）
- `TransactionService` — 交易签名与广播
- `IpfsService` — IPFS 存储，带重试与备用网关
- `NftMetadataService` — 元数据生成与校验
- `GasPriceService` — 动态 Gas 价格监控
- `ContractLoader` — 合约 ABI 加载

合约 Java 包装类由 Web3j 生成，位于 `blockchain/contract/`（NFTAsset、MicroMarket、FanToken）。

#### 新增 Mapper 时的注意事项

`UserServiceApplication` 使用 `@MapperScan` 显式列出 8 个 Mapper 包。新增模块的 Mapper 必须手动添加到扫描列表，否则 Bean 无法注入：

```java
@MapperScan(basePackages = {
    "com.zhm.springboot.userservice.mapper",
    "com.zhm.springboot.userservice.nft.mapper",
    "com.zhm.springboot.userservice.market.mapper",
    "com.zhm.springboot.userservice.wallet.mapper",
    "com.zhm.springboot.userservice.fantoken.mapper",
    "com.zhm.springboot.userservice.blockchain.mapper",
    "com.zhm.springboot.userservice.notification.mapper",
    "com.zhm.springboot.userservice.audit.mapper"
})
```

### NFT 铸造异步流程

1. 用户上传文件 → MinIO 存储，创建 `MintTask` 入库
2. MintTask 通过 RocketMQ 异步消费（`MintTaskServiceImpl`）
3. 生成元数据 JSON → 上传 IPFS → 获取 CID
4. 调用 `NFTAsset.mint()` 上链，写入 `nft_versions` 初始记录
5. 本地开发无 MQ 服务时，铸造相关功能自动降级

### 前端双钱包模式

用户前端 `services/web3.js` 支持两种模式：
- **MetaMask 钱包**：浏览器扩展，完整交易签名能力
- **服务端钱包**：无 MetaMask 时自动降级，使用只读 JSON-RPC Provider，交易由后端 `TransactionService` 签名广播

### 智能合约（`/contracts`，Solidity 0.8.20）

- **NFTAsset.sol** — ERC-721，版本历史（TokenVersion 结构体）+ 版税（万分比），支持 `updateMetadata`
- **MicroMarket.sol** — 固定价格 / 荷兰拍卖 / Offer，自动版税分配，ReentrancyGuard
- **FanToken.sol** — ERC-20，线性 Vesting（创作者 4 年、团队 1 年）+ Staking

Hardhat 编译器启用 viaIR 优化，200 runs。

### 定时任务（`scheduled/SystemScheduledTasks`）

- 限流记录清理：每 5 分钟
- Gas 价格更新：每 30 秒
- 待审核 NFT 数量检查：每 1 小时
- 系统内存健康检查：每 5 分钟（>80% 告警）

### 数据库

ORM：MyBatis Plus 3.5.7，分页插件（MySQL 方言），自动填充 `createdAt`/`updatedAt`/`deleted`/`status`/`accessCount`。

ShardingSphere 5.5.2（`sharding.yaml`，当前单库 `ds0`，无分片规则）。**生产环境（`application-prod.yml`）不使用 ShardingSphere**，直接连接 MySQL。

迁移脚本位于 `src/main/resources/db/migration/`，当前版本 V1～V6。**注意**：存在两个 V2 迁移文件（`V2__add_user_status.sql` 和 `V2__add_ipfs_file_record.sql`），Flyway 会报版本冲突；`users` 表无独立建表迁移脚本。

核心表：`users`、`user_roles`、`wallets`、`nft_assets`、`nft_versions`、`market_orders`、`fan_tokens`、`stake_records`、`nft_reviews`、`mint_tasks`、`ipfs_file_record`、`transaction_notifications`、`audit_logs`。

## 关键配置

**配置文件**：

- `application.yml` — 主配置：数据库（ShardingSphere）、Redis、MinIO、RocketMQ、JWT、blockchain（含重复 blockchain 配置）、`debug: true`
- `application-prod.yml` — 生产配置：直连 MySQL（HikariCP 20 连接）、环境变量注入、日志 WARN 级别、Actuator 仅授权访问
- `blockchain.yml` — 区块链专用：含 WebSocket URL（事件监听）、交易重试/超时/nonce 缓存配置（application.yml 中缺失）
- `sharding.yaml` — ShardingSphere 分片规则（HikariCP 最大 15 连接）
- `bootstrap.yaml` — Nacos（`enabled: false`，可选微服务部署）
- `frontend/vite.config.js` — 端口 3000，代理 `/api`、`/actuator` → localhost:8085，`/minio` → localhost:9000
- `admin-web/vite.config.js` — 端口 5180，代理 `/admin`、`/user/` → localhost:8085

**必须配置的环境变量**（参考 `.env.example`）：

```
BLOCKCHAIN_PLATFORM_PRIVATE_KEY
NFT_ASSET_CONTRACT_ADDRESS
MICRO_MARKET_CONTRACT_ADDRESS
FAN_TOKEN_CONTRACT_ADDRESS
JWT_SECRET
MYSQL_USER / MYSQL_PASSWORD
```

Docker Compose 中 Backend 使用默认 Hardhat 测试账户私钥，仅供本地开发。

## 开发注意事项

- `allow-circular-references: true` 已开启，修改 Bean 依赖时注意循环依赖风险
- `@EnableFeignClients` + `@EnableScheduling` 已启用，存在 `PermissionClient`（Feign 远程调用）和定时任务
- 文件上传限制：单文件 50 MB / 请求 100 MB；IPFS 批量上传最多 10 文件
- Gas 参数（30 Gwei，buffer 1.2x）在 `blockchain.yml`，Polygon 主网部署前需重新评估
- JWT：HS512，access token 1 小时，refresh token 7 天
- Hardhat 编译产物路径为 `src/main/resources/contracts/`，修改合约后需重新编译并更新 Web3j 包装类
- 前端使用 ethers.js v6（API 与 v5 不兼容），钱包交互逻辑在 `frontend/src/services/`
- 前端 `api.js` 中部分端点（如 `/api/market/offer`、`/api/wallet/bind`）在后端 Controller 中不存在，调用会 404
- `application.yml` 与 `blockchain.yml` 存在区块链配置重复，修改时需同步更新
- 编码风格：Java 4 空格缩进，JS/React 2 空格缩进；Spring 命名 `*Controller`/`*Service`/`*ServiceImpl`/`*Mapper`/`*DTO`/`*VO`