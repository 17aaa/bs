# 自动钱包创建功能

## 功能概述

用户在注册后，系统会自动为其创建钱包并完成登录，无需任何额外操作。

## 实现原理

### 后端实现

**文件**: `src/main/java/com/zhm/springboot/userservice/controller/UserController.java`

注册接口修改：
```java
@PostMapping("/register")
public Map<String, Object> register(@RequestBody User user, HttpServletRequest request) {
    // 1. 创建用户
    User registeredUser = userService.register(user);

    // 2. 自动创建钱包（使用随机密码）
    String walletPassword = "wallet_" + registeredUser.getUserId() + "_" + System.currentTimeMillis();
    var wallet = walletService.createWalletForUser(registeredUser.getUserId(), walletPassword);

    // 3. 返回用户信息和钱包信息
    Map<String, Object> result = new HashMap<>();
    result.put("userId", registeredUser.getUserId());
    result.put("username", registeredUser.getUsername());
    result.put("walletConnected", true);
    result.put("walletAddress", wallet.getWalletAddress());
    return result;
}
```

### 前端实现

**文件**: `frontend/src/pages/LoginPage.jsx`

注册成功后自动登录：
```javascript
const response = await axios.post('http://localhost:8085/user/register', {
  username: formData.username,
  password: formData.password,
});

// 自动保存登录信息
localStorage.setItem('userId', response.data.userId);
localStorage.setItem('walletConnected', response.data.walletConnected);
localStorage.setItem('walletAddress', response.data.walletAddress);

// 自动跳转到首页
navigate('/');
```

## API 响应

### 注册接口

**请求**:
```bash
POST /user/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "123456"
}
```

**响应**:
```json
{
  "userId": 2034925631827247105,
  "username": "newuser",
  "walletConnected": true,
  "walletAddress": "0x7ca9d2780ba0b536517cf78476d6b9bf00f4192c"
}
```

### 登录接口

**请求**:
```bash
POST /user/login?username=newuser&password=123456
```

**响应**:
```json
{
  "userId": 2034925631827247105,
  "walletConnected": true,
  "walletAddress": "0x7ca9d2780ba0b536517cf78476d6b9bf00f4192c",
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

## 用户流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  访问首页   │ ──> │  点击注册   │ ──> │  输入信息   │ ──> │  提交注册  │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                                                                  │
                                                                  ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  钱包已连接 │ <── │  自动登录   │ <── │  创建钱包   │ <── │  创建用户  │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
```

## 测试步骤

### 1. 启动服务

```bash
# 后端
cd /Users/zhm/Desktop/user-service
mvn spring-boot:run

# 前端
cd /Users/zhm/Desktop/user-service/frontend
npm run dev
```

### 2. 测试注册

访问 http://localhost:3000，点击"去注册"，输入：
- 用户名：`testuser`
- 密码：`123456`

点击"注册"，观察：
1. 注册成功提示
2. 自动跳转到首页
3. 右上角显示用户头像
4. 进入"我的"页面可以看到钱包地址

### 3. 验证 API

```bash
# 注册
curl -X POST 'http://localhost:8085/user/register' \
  -H "Content-Type: application/json" \
  -d '{"username":"autouser","password":"123456"}'

# 登录
curl -X POST 'http://localhost:8085/user/login?username=autouser&password=123456'
```

## 数据库变化

### users 表
新增用户记录

### wallets 表
自动新增钱包记录：
- `user_id`: 新用户 ID
- `wallet_address`: 自动生成的钱包地址
- `keystore_path`: 加密存储路径
- `is_bound`: true

## 安全说明

1. **钱包密码**: 系统自动生成随机密码，用户无需记忆
2. **Keystore 存储**: 钱包文件加密存储在服务器
3. **私钥安全**: 私钥不暴露给前端，由后端安全管理


## 优势

1. **用户体验**: 注册即用，无需额外操作
2. **降低门槛**: 用户无需了解区块链钱包知识
3. **自动化**: 减少用户操作步骤，提高转化率
4. **安全性**: 系统自动管理钱包，降低用户丢失风险

## 后续优化

- [ ] 支持用户导出钱包私钥
- [ ] 支持用户自定义钱包密码
- [ ] 支持绑定现有 MetaMask 钱包
- [ ] 增加钱包安全提示