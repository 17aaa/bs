# 快速测试指南

## 启动后端

```bash
cd /Users/zhm/Desktop/user-service
mvn spring-boot:run
```

## 测试 API

### 1. 注册新用户

```bash
curl -X POST 'http://localhost:8085/user/register' \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"pass123"}'
```

**响应示例**:
```json
{
  "userId": 1234567890,
  "username": "testuser",
  "password": "$2a$12$...",
  "email": null,
  "phone": null,
  "gmtCreate": "2026-03-20T09:21:13.974+00:00"
}
```

### 2. 用户登录

```bash
curl -X POST 'http://localhost:8085/user/login?username=testuser&password=pass123'
```

**响应示例**:
```json
{
  "userId": 1234567890,
  "walletConnected": false,
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 3. 创建钱包

```bash
TOKEN="your_jwt_token_here"
curl -X POST 'http://localhost:8085/user/wallet/get-or-create?userId=1234567890&password=wallet123' \
  -H "Authorization: $TOKEN"
```

**响应示例**:
```json
{
  "success": true,
  "walletAddress": "0x152139e7cd2132e7cb8cddd8ee1ca84d7909b350",
  "keystorePath": "/path/to/keystore.json"
}
```

### 4. 再次登录（查看钱包已连接）

```bash
curl -X POST 'http://localhost:8085/user/login?username=testuser&password=pass123'
```

**响应示例**:
```json
{
  "userId": 1234567890,
  "walletConnected": true,
  "walletAddress": "0x152139e7cd2132e7cb8cddd8ee1ca84d7909b350",
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

## 测试前端

### 1. 启动前端开发服务器

```bash
cd /Users/zhm/Desktop/user-service/frontend
npm run dev
```

访问：http://localhost:5173

### 2. 测试流程

1. **访问首页** → 自动跳转到 `/login`
2. **点击"去注册"** → 注册新用户
   - 用户名：任意（至少 3 字符）
   - 密码：至少 6 个字符
3. **注册成功** → 自动切换到登录
4. **登录** → 输入用户名和密码
5. **登录成功** → 跳转到首页，可以看到：
   - 右上角显示用户头像
   - 点击"我的"进入个人中心
6. **创建钱包**（可选）
   - 在个人中心点击"创建钱包"
   - 输入钱包密码（至少 6 位）
   - 创建成功后显示钱包地址
7. **退出登录** → 点击右上角"退出"

## 测试用户

如果不想注册，可以使用已有的测试账户：

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | admin123 | 管理员账户 |
| test | test123 | 测试账户 |

```bash
# 登录测试
curl -X POST 'http://localhost:8085/user/login?username=admin&password=admin123'
```

## 常见问题

### Q: 注册失败，提示"密码必须包含..."
A: 密码要求已降低为至少 6 个字符。如果还是看到老提示，请清理浏览器缓存。

### Q: 登录后显示 "walletConnected: false"
A: 这是正常的，表示用户还没有钱包。可以在个人中心创建钱包。

### Q: Nacos 连接错误
A: 如果不使用 Nacos 服务发现，可以忽略这些错误日志。核心功能不受影响。

### Q: PermissionClient 错误
A: 如果 permission-service 未启动，注册时会打印警告但不影响注册成功。

## 数据库检查

```bash
# 查看用户
mysql -u root creativenft_db -e "SELECT * FROM users ORDER BY user_id DESC LIMIT 5;"

# 查看钱包
mysql -u root creativenft_db -e "SELECT * FROM wallets ORDER BY id DESC LIMIT 5;"
```

## 清理测试数据

```bash
mysql -u root creativenft_db -e "DELETE FROM wallets WHERE user_id > 100; DELETE FROM users WHERE user_id > 100;"
```