# 钱包集成指南

## 功能概述

本系统实现了用户登录后自动连接钱包的功能，支持用户在进行 NFT 铸造、交易等操作时自动使用已绑定的钱包。

## 技术架构

### 后端
- **Spring Boot 3.5.0** - 主框架
- **Web3j 4.12.2** - 区块链集成
- **MyBatis-Plus** - 数据持久化
- **ShardingSphere** - 数据库分片

### 前端
- **React 18** - UI 框架
- **Ethers.js** - 钱包连接
- **Axios** - HTTP 请求

## API 接口

### 1. 用户登录

**接口**: `POST /user/login`

**参数**:
- `username` - 用户名
- `password` - 密码

**响应**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": 3,
  "walletConnected": true,
  "walletAddress": "0x152139e7cd2132e7cb8cddd8ee1ca84d7909b350"
}
```

**说明**:
- `walletConnected: false` 表示用户还没有钱包
- `walletConnected: true` 表示用户已绑定钱包，可直接进行交易操作

### 2. 获取或创建钱包

**接口**: `POST /user/wallet/get-or-create`

**参数**:
- `userId` - 用户 ID
- `password` - 钱包密码（用于加密 keystore）
- `Authorization` - JWT Token（Header）

**响应**:
```json
{
  "success": true,
  "walletAddress": "0x152139e7cd2132e7cb8cddd8ee1ca84d7909b350",
  "keystorePath": "/path/to/keystore.json"
}
```

## 使用流程

### 流程一：新用户首次使用

1. **用户登录**
   ```javascript
   const response = await axios.post('http://localhost:8085/user/login', null, {
     params: { username: 'admin', password: 'admin123' }
   });

   // 保存返回的信息
   localStorage.setItem('token', response.data.token);
   localStorage.setItem('userId', response.data.userId);
   localStorage.setItem('walletConnected', response.data.walletConnected);
   ```

2. **检查钱包状态**
   - 如果 `walletConnected === false`，引导用户创建钱包
   - 如果 `walletConnected === true`，自动连接钱包

3. **创建钱包（可选）**
   ```javascript
   const response = await axios.post(
     'http://localhost:8085/user/wallet/get-or-create',
     null,
     {
       params: { userId: 3, password: 'wallet_password' },
       headers: { Authorization: `Bearer ${token}` }
     }
   );
   ```

4. **自动连接钱包**
   ```javascript
   import web3 from './services/web3';

   // 页面加载时自动连接
   const savedWallet = web3.loadSavedWallet();
   if (savedWallet && savedWallet.walletConnected) {
     await web3.autoConnect(savedWallet.walletAddress);
   }
   ```

### 流程二：已绑定钱包用户

1. **用户登录** - 同流程一
2. **自动连接钱包** - 系统自动从 localStorage 读取钱包地址并连接
3. **进行交易/铸造** - 使用已连接的钱包

## 前端集成示例

### 登录页面 (LoginPage.jsx)

```jsx
const handleSubmit = async (e) => {
  e.preventDefault();

  const response = await axios.post('http://localhost:8085/user/login', null, {
    params: { username: formData.username, password: formData.password }
  });

  if (response.data && response.data.token) {
    // 保存登录信息
    localStorage.setItem('token', response.data.token);
    localStorage.setItem('userId', response.data.userId);
    localStorage.setItem('walletConnected', response.data.walletConnected);
    if (response.data.walletAddress) {
      localStorage.setItem('walletAddress', response.data.walletAddress);
    }

    // 跳转到首页
    navigate('/');
  }
};
```

### 自动连接钱包 (App.jsx)

```jsx
useEffect(() => {
  const checkWalletStatus = async () => {
    const savedWallet = web3.loadSavedWallet();
    if (savedWallet && savedWallet.walletConnected) {
      setIsWalletConnected(true);
      setWalletAddress(savedWallet.walletAddress);

      // 自动连接钱包
      try {
        await web3.autoConnect(savedWallet.walletAddress);
      } catch (error) {
        console.error('自动连接失败:', error);
      }
    }
  };

  checkWalletStatus();
}, []);
```

### 使用钱包进行交易

```jsx
import { nftApi } from './services/api';
import web3 from './services/web3';

const handleMintNFT = async () => {
  // 确保钱包已连接
  if (!web3.account) {
    alert('请先连接钱包');
    return;
  }

  try {
    // 调用后端 API 铸造 NFT
    const response = await nftApi.mintNft({
      ownerAddress: web3.account,
      metadataHash: 'QmXxx...'
    });

    console.log('NFT 铸造成功:', response);
  } catch (error) {
    console.error('铸造失败:', error);
  }
};
```

## 数据库表结构

### wallets 表

```sql
CREATE TABLE wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    wallet_address VARCHAR(42) NOT NULL,
    keystore_path VARCHAR(200),
    is_bound BOOLEAN DEFAULT FALSE,
    bound_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user (user_id),
    UNIQUE KEY uk_wallet (wallet_address)
);
```

## 安全注意事项

1. **私钥存储**
   - 私钥通过 Keystore 加密存储
   - 密码由用户自行保管
   - 不要将私钥提交到版本控制

2. **JWT Token**
   - Token 有过期时间
   - Token 存储在 localStorage
   - 敏感操作需要验证 Token

3. **钱包地址验证**
   - 使用正则验证地址格式：`^0x[a-fA-F0-9]{40}$`
   - 链上操作前验证地址有效性

## 测试账户

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | admin123 | 管理员账户 |
| test | test123 | 测试账户 |

## 常见问题

### Q: 登录后显示 "walletConnected: false" 怎么办？
A: 这是正常的，表示用户还没有绑定钱包。可以在个人中心点击"创建钱包"按钮创建。

### Q: 如何切换钱包？
A: 在个人中心可以点击"连接钱包"重新连接其他钱包地址。

### Q: 钱包密码忘记了怎么办？
A: 钱包密码用于加密本地 Keystore 文件，忘记密码需要重新创建钱包。

### Q: 支持哪些钱包？
A: 目前支持：
- MetaMask 浏览器插件
- 系统生成的 Keystore 钱包
- WalletConnect（待实现）

## 后续优化

- [ ] 支持 WalletConnect 协议
- [ ] 添加钱包签名登录功能
- [ ] 支持多钱包切换
- [ ] 添加交易历史记录
- [ ] 支持硬件钱包