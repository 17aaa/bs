# CreativeNFT User Service 部署指南

## 目录

- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [本地开发部署](#本地开发部署)
- [Docker 部署](#docker-部署)
- [生产环境部署](#生产环境部署)
- [常见问题](#常见问题)

## 环境要求

### 必需环境

- **Java**: 17 或更高版本
- **Maven**: 3.8 或更高版本
- **Node.js**: 18 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 6.0 或更高版本

### 可选环境

- **Docker**: 20.10 或更高版本
- **Docker Compose**: 2.0 或更高版本

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd user-service
```

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，填入实际的配置值
vi .env
```

### 3. 使用 Docker Compose 一键部署

```bash
# 赋予执行权限
chmod +x deploy.sh

# 部署开发环境
./deploy.sh dev

# 或部署生产环境
./deploy.sh prod
```

## 本地开发部署

### 后端服务

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS creativenft_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 构建项目
mvn clean package -DskipTests

# 3. 运行项目
mvn spring-boot:run

# 或使用 jar 包运行
java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

后端服务将在 http://localhost:8085 启动

### 前端服务

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 http://localhost:3000 启动

### 使用快速启动脚本

```bash
chmod +x start.sh
./start.sh
```

此脚本会自动构建并启动前后端服务。

## Docker 部署

### 开发环境

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 生产环境

```bash
# 配置生产环境变量
cp .env.example .env
# 编辑 .env 填入生产环境配置

# 使用生产环境配置部署
./deploy.sh prod

# 或手动部署
docker-compose -f docker-compose.prod.yml up -d
```

### 服务说明

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库服务 |
| Redis | 6379 | 缓存服务 |
| Backend | 8085 | 后端 API 服务 |
| Frontend | 80 | 前端 Web 服务 |

## 生产环境部署

### 1. 服务器准备

- 2 核 CPU 或更高
- 4GB 内存或更高
- 50GB 磁盘空间或更高
- 开放端口: 80, 443, 3306, 6379, 8085

### 2. 安全配置

```bash
# 修改默认密码
vi .env
# 设置强密码:
# MYSQL_ROOT_PASSWORD=your_strong_password
# MYSQL_PASSWORD=your_strong_password
# REDIS_PASSWORD=your_strong_password
# JWT_SECRET=your_random_secret_key
```

### 3. SSL/TLS 配置

使用 Nginx 或 Traefik 配置 HTTPS:

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 4. 监控和日志

```bash
# 查看应用日志
docker-compose -f docker-compose.prod.yml logs -f backend

# 查看系统指标
curl http://localhost:8085/actuator/metrics

# 查看健康状态
curl http://localhost:8085/actuator/health
```

## 常见问题

### 1. 数据库连接失败

**问题**: 应用启动时报告数据库连接错误

**解决**:
```bash
# 检查 MySQL 服务状态
docker-compose ps mysql

# 检查数据库是否创建
mysql -u root -p -e "SHOW DATABASES;"

# 检查用户权限
mysql -u root -p -e "SELECT user, host FROM mysql.user;"
```

### 2. 区块链交易失败

**问题**: NFT 铸造或交易失败

**解决**:
- 确保平台钱包有足够的 MATIC 代币
- 检查合约地址配置是否正确
- 查看区块链网络状态

### 3. IPFS 上传失败

**问题**: 文件上传到 IPFS 失败

**解决**:
- 检查网络连接
- 尝试使用备用 IPFS 网关
- 检查文件大小是否超过限制

### 4. 内存不足

**问题**: 应用运行缓慢或 OOM

**解决**:
```bash
# 调整 JVM 内存参数
export JAVA_OPTS="-Xms2g -Xmx4g"

# 或在 docker-compose.prod.yml 中调整
# deploy:
#   resources:
#     limits:
#       memory: 4G
```

## 更新部署

```bash
# 1. 拉取最新代码
git pull origin main

# 2. 重新部署
./deploy.sh prod

# 或手动更新
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d --build
```

## 备份和恢复

### 数据库备份

```bash
# 手动备份
mysqldump -u root -p creativenft_db > backup/creativenft_db_$(date +%Y%m%d_%H%M%S).sql

# 自动备份脚本
echo "0 2 * * * /usr/bin/mysqldump -u root -p'password' creativenft_db > /backup/creativenft_db_\$(date +\%Y\%m\%d).sql" | crontab -
```

### 数据恢复

```bash
mysql -u root -p creativenft_db < backup/creativenft_db_20240101_120000.sql
```

## 联系支持

如有问题，请通过以下方式联系:
- 提交 Issue: [GitHub Issues]
- 邮件支持: support@example.com
