# NFT 平台功能完善记录

## 本次更新内容

### 1. 修复 NFT 铸造 400 错误

**问题**: 前端发送 JSON 请求体，后端使用 `@RequestParam` 接收查询参数

**解决方案**:
- 前端 `api.js`: 改用 `api.post('/api/nft/mint', params)` 发送 JSON
- 后端 `NftController.java`: 改用 `@RequestBody Map<String, Object> request` 接收
- 添加参数验证和空值处理逻辑

### 2. 添加获取所有 NFT 接口

**后端**:
```java
@GetMapping("/list")
public ResponseEntity<Map<String, Object>> getAllNfts(
    @RequestParam(required = false) Integer limit,
    @RequestParam(required = false) Integer offset)
```

**Service**:
```java
public List<NftAsset> getAllNfts(Integer limit, Integer offset) {
    LambdaQueryWrapper<NftAsset> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(NftAsset::getStatus, 1);
    wrapper.orderByDesc(NftAsset::getCreatedAt);
    wrapper.last("LIMIT " + offset + ", " + limit);
    return nftAssetMapper.selectList(wrapper);
}
```

**前端 API**:
```javascript
getAllNfts: async (limit = 20, offset = 0) => {
    const response = await api.get('/api/nft/list', { params: { limit, offset } });
    return response.data;
}
```

### 3. 首页 NFT 列表展示

**修改 HomePage.jsx**:
- 调用 `nftApi.getAllNfts(20, 0)` 获取最新 NFT
- 使用 `NFTCard` 组件展示
- 空状态提示"暂无 NFT 作品"

### 4. 创建页面图片上传功能

**CreatePage.jsx 新增功能**:
- 图片上传区域（点击或拖拽）
- 图片预览
- 支持 JPG、PNG、GIF 格式
- 本地 URL 预览（实际部署时需上传到服务器/IPFS）

**UI 组件**:
- `UploadArea` - 上传区域样式
- `UploadIcon` - 上传图标
- `PreviewImage` - 预览图片样式

### 5. NFT 详情页

**已有功能**:
- NFT 基本信息展示
- 版本历史列表
- 购买按钮（待实现市场功能）
- 返回导航

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

### 2. 测试铸造功能
1. 访问 http://localhost:3000
2. 登录/注册账号
3. 点击"创作"或右下角"+"按钮
4. 上传图片（可选）
5. 填写作品名称、描述
6. 选择分类、设置版税
7. 点击"铸造 NFT"

### 3. 测试首页展示
1. 访问 http://localhost:3000
2. 查看"最新 NFT"区域
3. 点击 NFT 卡片查看详情

### 4. 测试详情页
1. 从首页或铸造成功后进入详情页
2. 查看 NFT 信息
3. 查看版本历史

## API 端点汇总

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | /api/nft/mint | 铸造 NFT（JSON） |
| GET | /api/nft/{id} | 获取 NFT 详情 |
| GET | /api/nft/{id}/versions | 获取版本历史 |
| GET | /api/nft/list | 获取所有 NFT |
| GET | /api/nft/owner/{address} | 获取用户持有的 NFT |
| GET | /api/nft/creator/{address} | 获取用户创作的 NFT |
| POST | /api/nft/{id}/update | 更新 NFT 元数据 |

## 待完善功能

- [ ] 市场交易功能（挂单、购买）
- [ ] 图片上传到服务器/IPFS
- [ ] 粉丝代币发行
- [ ] 用户个人中心
- [ ] 恢复链上铸造（需充值 MATIC）

## 注意事项

### 链上铸造
当前为测试方便，已临时跳过链上操作。要恢复链上铸造：

1. 获取测试 MATIC: https://faucet.polygon.technology/
2. 充值到平台账户：`0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266`
3. 修改 `NftService.java` 第 62 行：
   ```java
   // 从这行:
   String txHash = "0xpending";
   
   // 改回:
   String txHash = mintOnChain(creatorAddress, metadataHash);
   ```

### 图片存储
当前图片仅本地预览，实际部署需要：
1. 后端添加文件上传接口
2. 存储到服务器或对象存储
3. 返回 URL 保存到数据库
4. 或上传到 IPFS 去中心化存储
