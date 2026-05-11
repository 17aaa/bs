# CreativeNFT Market - 部署指南

## 部署流程概述

```
1. 环境准备 → 2. 数据库初始化 → 3. 合约部署 → 4. 后端配置 → 5. 前端打包 → 6. 服务部署
```

---

## 一、环境准备

### 1.1 系统要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 18+ | Java 运行环境 |
| Node.js | 18+ | 前端和合约开发环境 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存（可选） |
| Maven | 3.8+ | Java 构建工具 |

### 1.2 安装检查

```bash
# 检查 Java 版本
java -version

# 检查 Node.js 版本
node -v

# 检查 Maven 版本
mvn -version
```

---

## 二、数据库初始化

### 2.1 创建数据库

```sql
CREATE DATABASE creativenft_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE creativenft_db;
```

### 2.2 执行初始化脚本

```bash
# 方式 1: 使用 MySQL 命令行
mysql -u root -p creativenft_db < src/main/resources/V1__init.sql

# 方式 2: 在 MySQL 客户端中执行
source /path/to/V1__init.sql;
```

### 2.3 验证表结构

```sql
SHOW TABLES;
-- 应该显示：users, nft_assets, nft_versions, market_orders, fan_tokens, stake_records, wallets
```

---

## 三、智能合约部署

### 3.1 本地测试网络（开发环境）

```bash
# 1. 编译合约
cd /Users/zhm/Desktop/user-service
npx hardhat compile

# 2. 部署到本地 Hardhat 网络
npx hardhat run scripts/deploy-local.js --network hardhat

# 3. 运行合约测试
npx hardhat run scripts/simple-test.js --network hardhat
```

部署成功后会输出合约地址，保存到 `deployments/hardhat.json`

### 3.2 Polygon Amoy 测试网（测试环境）

```bash
# 1. 复制环境变量文件
cp .env.example .env

# 2. 编辑 .env，填入你的配置
vi .env

# 必填项：
# PRIVATE_KEY=你的部署者私钥
# POLYGONSCAN_API_KEY=你的 Polygonscan API Key
# BLOCKCHAIN_PLATFORM_PRIVATE_KEY=平台钱包私钥

# 3. 获取测试网 MATIC
# 访问：https://faucet.polygon.technology/
# 输入你的钱包地址，领取测试用 MATIC

# 4. 部署合约
npm run deploy:amoy

# 5. 验证合约（可选，需要 POLYGONSCAN_API_KEY）
npm run verify:amoy <CONTRACT_ADDRESS>
```

部署成功后记录合约地址：
- NFT Asset Contract
- Micro Market Contract
- Fan Token Contract

### 3.3 Polygon 主网（生产环境）

```bash
# 1. 确保 .env 中配置了主网私钥和 RPC

# 2. 部署合约（需要足够的 MATIC 支付 Gas）
npm run deploy:polygon

# 3. 验证合约
npm run verify:polygon <CONTRACT_ADDRESS>
```

---

## 四、后端配置

### 4.1 修改 application.yml

编辑 `src/main/resources/application.yml`：

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:shardingsphere:classpath:sharding.yaml
    driver-class-name: org.apache.shardingsphere.driver.ShardingSphereDriver

# 区块链配置
blockchain:
  rpc-url: https://rpc-amoy.polygon.technology/  # 或主网 RPC
  chain-id: 80002  # 主网为 137
  gas-price: 30000000000
  gas-limit: 500000
  platform-private-key: ${BLOCKCHAIN_PLATFORM_PRIVATE_KEY}
  contract:
    nft-asset-address: ${NFT_ASSET_CONTRACT_ADDRESS}
    micro-market-address: ${MICRO_MARKET_CONTRACT_ADDRESS}
    fan-token-address: ${FAN_TOKEN_CONTRACT_ADDRESS}

# Redis 配置（可选）

    redis:
      host: localhost
      port: 6379
```

### 4.2 配置 ShardingSphere

编辑 `src/main/resources/sharding.yaml`，确保数据源配置正确：

```yaml
dataSources:
  ds0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/creativenft_db?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: YOUR_DB_PASSWORD
    connectionTimeout: 30000
    maximumPoolSize: 15
```

### 4.3 构建后端

```bash
# 编译打包
mvn clean package -DskipTests

# 生成的 jar 包位置
# target/user-service-0.0.1-SNAPSHOT.jar
```

### 4.4 启动后端

```bash
# 方式 1: 直接运行
java -jar target/user-service-0.0.1-SNAPSHOT.jar

# 方式 2: 使用 Maven
mvn spring-boot:run

# 方式 3: 后台运行（生产环境）
nohup java -jar target/user-service-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

验证启动：
```bash
curl http://localhost:8085/api/nft/1
```

---

## 五、前端部署

### 5.1 安装依赖

```bash
cd frontend
npm install
```

### 5.2 开发环境

```bash
# 启动开发服务器（带热重载）
npm run dev

# 访问 http://localhost:3000
```

### 5.3 生产环境打包

```bash
# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

构建产物在 `frontend/dist` 目录

### 5.4 部署到 Nginx

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api {
        proxy_pass http://localhost:8085;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

## 六、服务部署选项

### 6.1 Docker 部署（推荐）

创建 `Dockerfile`：

```dockerfile
FROM maven:3.9-eclipse-temurin-18 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:18-jre-alpine
WORKDIR /app
COPY --from=build /app/target/user-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建和运行：

```bash
# 构建镜像
docker build -t creative-nft-backend .

# 运行容器
docker run -d \
  -p 8085:8085 \
  -e BLOCKCHAIN_PLATFORM_PRIVATE_KEY=your_key \
  -e NFT_ASSET_CONTRACT_ADDRESS=0x... \
  --name creative-nft \
  creative-nft-backend
```

### 6.2 Docker Compose

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: creativenft_db
    volumes:
      - mysql_/var/lib/mysql
      - ./src/main/resources/V1__init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: .
    environment:
      DATABASE_URL: jdbc:mysql://mysql:3306/creativenft_db
      REDIS_HOST: redis
      BLOCKCHAIN_PLATFORM_PRIVATE_KEY: ${BLOCKCHAIN_PLATFORM_PRIVATE_KEY}
      NFT_ASSET_CONTRACT_ADDRESS: ${NFT_ASSET_CONTRACT_ADDRESS}
    ports:
      - "8085:8085"
    depends_on:
      - mysql
      - redis

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_
```

运行：
```bash
docker-compose up -d
```

---

## 七、验证清单

### 7.1 后端验证

```bash
# 1. 健康检查（需要添加 actuator）
curl http://localhost:8085/actuator/health

# 2. 测试 NFT API
curl http://localhost:8085/api/nft/1

# 3. 测试钱包 API
curl http://localhost:8085/api/wallet/address/1
```

### 7.2 前端验证

```bash
# 访问首页
curl http://localhost:3000 | grep "CreativeNFT"

# 检查静态资源
curl http://localhost:3000/assets/*.js | head -5
```

### 7.3 区块链连接验证

```bash
# 检查 RPC 连接
curl -X POST https://rpc-amoy.polygon.technology/ \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}'
```

### 7.4 合约验证

在区块浏览器中验证：
- Amoy 测试网：https://amoly.polygonscan.com/
- Polygon 主网：https://polygonscan.com/

输入合约地址，查看合约代码和交易记录。

---

## 八、故障排查

### 8.1 常见问题

**问题 1: 无法连接数据库**
```
解决方案：
1. 检查 MySQL 是否运行
2. 验证数据库用户权限
3. 检查 sharding.yaml 配置
```

**问题 2: 合约调用失败**
```
解决方案：
1. 确认合约地址正确
2. 检查 RPC URL 是否可访问
3. 验证平台钱包私钥配置
```

**问题 3: 前端无法连接后端**
```
解决方案：
1. 检查 Vite 代理配置
2. 确认后端服务端口
3. 检查 CORS 配置
```

### 8.2 日志查看

```bash
# 后端日志
tail -f app.log

# Docker 日志
docker logs -f creative-nft

# Nginx 日志
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

---

## 九、生产环境建议

### 9.1 安全配置

1. **私钥管理**
   - 使用环境变量或密钥管理服务
   - 不要将私钥提交到版本控制
   - 定期轮换密钥

2. **数据库安全**
   - 使用强密码
   - 限制数据库用户权限
   - 启用 SSL 连接

3. **API 安全**
   - 启用 HTTPS
   - 配置速率限制
   - 实施 JWT 认证

### 9.2 性能优化

1. **缓存策略**
   - 启用 Redis 缓存
   - 配置 CDN 静态资源
   - 实施数据库查询缓存

2. **负载均衡**
   - 使用 Nginx 反向代理
   - 配置多实例部署
   - 实施会话粘性

3. **监控告警**
   - 部署 Prometheus + Grafana
   - 配置日志聚合（ELK）
   - 设置告警规则

---

## 十、联系支持

如有问题，请参考：
- 项目文档：PROJECT.md
- 智能合约文档：contracts/README.md
- API 文档：待补充