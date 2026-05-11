# 学生创意成果NFT交易系统 - 系统图表集

使用 [Mermaid Live Editor](https://mermaid.live/) 在线预览这些图表

---

## 1. 系统用例图

```mermaid
graph TB
    subgraph 学生创作者
        C1[连接钱包]
        C2[上传创意成果]
        C3[铸造NFT]
        C4[版本更新]
        C5[管理作品]
        C6[设置销售价格]
        C7[查看收益]
    end

    subgraph 购买者
        B1[浏览市场]
        B2[搜索作品]
        B3[查看详情]
        B4[购买NFT]
        B5[查看持有资产]
        B6[转售NFT]
    end

    subgraph 平台管理员
        A1[用户管理]
        A2[内容审核]
        A3[交易监控]
        A4[数据统计]
        A5[系统配置]
    end

    subgraph NFT交易平台系统
        S1[钱包认证模块]
        S2[IPFS存储模块]
        S3[NFT铸造模块]
        S4[版本控制模块]
        S5[市场交易模块]
        S6[智能合约模块]
    end

    C1 --> S1
    C2 --> S2
    C3 --> S3
    C3 --> S6
    C4 --> S4
    C5 --> S3
    C6 --> S5
    C7 --> S5

    B1 --> S5
    B2 --> S5
    B3 --> S3
    B4 --> S5
    B4 --> S6
    B5 --> S3
    B6 --> S5

    A1 --> S1
    A2 --> S3
    A3 --> S5
    A4 --> S5
    A5 --> S6
```

---

## 2. NFT铸造时序图

```mermaid
sequenceDiagram
    actor 创作者
    participant 前端 as React前端
    participant 后端 as SpringBoot后端
    participant IPFS as IPFS网络
    participant 合约 as NFT智能合约
    participant 链 as Polygon区块链
    participant DB as MySQL数据库

    创作者->>前端: 1. 选择文件并填写信息
    前端->>后端: 2. POST /api/ipfs/upload
    后端->>IPFS: 3. 上传文件
    IPFS-->>后端: 4. 返回CID
    后端->>DB: 5. 保存IPFS记录
    后端-->>前端: 6. 返回文件URL

    创作者->>前端: 7. 确认铸造
    前端->>后端: 8. POST /api/nft/mint
    后端->>后端: 9. 生成元数据JSON
    后端->>IPFS: 10. 上传元数据
    IPFS-->>后端: 11. 返回元数据CID

    后端->>合约: 12. 调用mint()函数
    合约->>链: 13. 提交交易
    链-->>合约: 14. 交易确认
    合约-->>后端: 15. 返回Token ID

    后端->>DB: 16. 保存NFT资产记录
    后端->>DB: 17. 保存版本历史
    后端-->>前端: 18. 返回铸造结果
    前端-->>创作者: 19. 显示铸造成功
```

---

## 3. NFT购买时序图

```mermaid
sequenceDiagram
    actor 购买者
    participant 钱包 as MetaMask钱包
    participant 前端 as React前端
    participant 后端 as SpringBoot后端
    participant 合约 as 市场合约
    participant 链 as Polygon区块链
    participant DB as MySQL数据库

    购买者->>前端: 1. 浏览选择NFT
    前端->>后端: 2. GET /api/nft/{id}
    后端->>DB: 3. 查询NFT信息
    DB-->>后端: 4. 返回数据
    后端-->>前端: 5. 显示详情

    购买者->>前端: 6. 点击购买
    前端->>后端: 7. POST /api/market/buy
    后端->>合约: 8. 调用buy()函数
    合约->>钱包: 9. 请求签名交易
    购买者->>钱包: 10. 确认交易
    钱包->>链: 11. 提交交易
    链-->>合约: 12. 交易确认

    合约->>合约: 13. 计算版税
    合约->>链: 14. 转账给卖家
    合约->>链: 15. 转账版税给创作者
    合约->>链: 16. NFT转移给买家

    合约-->>后端: 17. 触发事件
    后端->>DB: 18. 更新订单状态
    后端->>DB: 19. 更新NFT所有者
    后端-->>前端: 20. 返回交易结果
    前端-->>购买者: 21. 显示购买成功
```

---

## 4. 系统架构图

```mermaid
graph TB
    subgraph 用户层
        U1[Web浏览器]
        U2[MetaMask钱包]
    end

    subgraph 前端层
        F1[React 19 + Vite 6]
        F2[ethers.js v6]
        F3[Styled Components]
        F4[React Router]
    end

    subgraph 网关层
        G1[Nginx反向代理]
        G2[负载均衡]
    end

    subgraph 服务层
        S1[Spring Boot 3.5]
        S2[用户服务]
        S3[NFT服务]
        S4[IPFS服务]
        S5[市场服务]
        S6[Web3j区块链服务]
    end

    subgraph 数据层
        D1[MySQL 8.0]
        D2[Redis缓存]
        D3[ShardingSphere分片]
    end

    subgraph 区块链层
        B1[Polygon Amoy测试网]
        B2[NFT合约ERC721]
        B3[市场合约]
        B4[IPFS网络]
    end

    U1 --> F1
    U2 --> F2
    F1 --> G1
    G1 --> S1
    S1 --> S2
    S1 --> S3
    S1 --> S4
    S1 --> S5
    S1 --> S6
    S2 --> D1
    S3 --> D1
    S4 --> B4
    S5 --> D1
    S6 --> B1
    S1 --> D2
    D1 --> D3
    B1 --> B2
    B1 --> B3
```

---

## 5. E-R图（实体关系图）

```mermaid
erDiagram
    用户 ||--o{ NFT资产 : 创建
    用户 ||--o{ NFT资产 : 拥有
    用户 ||--o{ 市场订单 : 购买
    用户 ||--o{ 市场订单 : 出售
    用户 ||--o{ IPFS文件记录 : 上传

    NFT资产 ||--o{ NFT版本历史 : 包含
    NFT资产 ||--o{ 市场订单 : 交易
    NFT资产 ||--o{ IPFS文件记录 : 关联

    用户 {
        bigint user_id PK "用户ID"
        string username "用户名"
        string password "密码"
        string email "邮箱"
        string phone "手机号"
        string avatar "头像URL"
        string school "学校"
        string major "专业"
        string bio "个人简介"
        timestamp gmt_create "创建时间"
    }

    NFT资产 {
        bigint id PK "主键ID"
        bigint token_id "链上Token ID"
        string contract_address "合约地址"
        string owner_address "所有者地址"
        string creator_address "创作者地址"
        string name "作品名称"
        string description "作品描述"
        string category "分类"
        string image_url "图片URL"
        string current_metadata_hash "当前元数据哈希"
        int current_version "当前版本号"
        int royalty_fee "版税比例"
        string royalty_recipient "版税接收地址"
        int status "状态"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }

    NFT版本历史 {
        bigint id PK "主键ID"
        bigint nft_asset_id FK "NFT资产ID"
        int version "版本号"
        string metadata_hash "元数据哈希"
        string ipfs_uri "IPFS URI"
        string change_description "变更描述"
        string updater_address "更新者地址"
        string tx_hash "交易哈希"
        bigint block_number "区块号"
        timestamp created_at "创建时间"
    }

    市场订单 {
        bigint id PK "主键ID"
        string order_id UK "订单ID"
        bigint sale_id "链上Sale ID"
        string seller_address FK "卖家地址"
        string buyer_address FK "买家地址"
        string nft_contract "NFT合约地址"
        bigint token_id "Token ID"
        int order_type "订单类型"
        int status "状态"
        bigint price "价格"
        bigint start_price "起始价"
        bigint reserve_price "保留价"
        string payment_token "支付代币"
        bigint end_time "结束时间"
        int royalty_fee "版税比例"
        string royalty_recipient "版税接收地址"
        bigint final_price "最终成交价"
        string tx_hash "交易哈希"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }

    IPFS文件记录 {
        bigint id PK "主键ID"
        string cid UK "IPFS CID"
        string filename "文件名"
        string file_type "文件类型"
        bigint file_size "文件大小"
        string mime_type "MIME类型"
        bigint user_id FK "上传用户ID"
        bigint nft_asset_id FK "关联NFT资产ID"
        boolean pinned "是否固定"
        int access_count "访问次数"
        string status "状态"
        timestamp created_at "创建时间"
    }
```

---

## 6. 类图

```mermaid
classDiagram
    class UserController {
        +login(LoginRequest)
        +register(RegisterRequest)
        +getUserInfo()
        +updateUserInfo()
        +bindWallet(String address)
    }

    class NftController {
        +mintNft(MintRequest)
        +getNftAsset(Long id)
        +getNftList(PageParam)
        +updateMetadata(Long id, UpdateRequest)
        +getVersionHistory(Long id)
    }

    class IpfsController {
        +uploadFile(MultipartFile)
        +uploadBatch(List~MultipartFile~)
        +downloadFile(String cid)
        +getFileInfo(String cid)
        +pinFile(String cid)
    }

    class MarketController {
        +createFixedPriceSale(SaleRequest)
        +createDutchAuction(AuctionRequest)
        +buyNft(Long orderId)
        +cancelOrder(Long orderId)
        +getOrderList(PageParam)
    }

    class NftService {
        -NftAssetMapper assetMapper
        -NftVersionMapper versionMapper
        -Web3j web3j
        +mintNft(String creator, String metadata)
        +updateMetadata(Long id, String newHash)
        +getNftByOwner(String address)
        +getNftByCreator(String address)
    }

    class IpfsServiceEnhanced {
        -IpfsProperties properties
        -IpfsFileRecordMapper recordMapper
        +uploadFile(File file)
        +uploadBytes(byte[] data)
        +downloadFile(String cid)
        +pinFile(String cid)
        +getGatewayUrl(String cid)
    }

    class MarketService {
        -MarketOrderMapper orderMapper
        -NftAssetMapper assetMapper
        -ContractCallService contractService
        +createFixedPriceSale(SaleRequest)
        +createDutchAuction(AuctionRequest)
        +buyNft(Long orderId, String buyer)
        +cancelOrder(Long orderId)
        +getOrderList()
    }

    class NFTAsset {
        <<Contract>>
        +mint(address to, string metadata)
        +transferFrom(address from, address to, uint256 tokenId)
        +updateMetadata(uint256 tokenId, string newHash)
        +getCreator(uint256 tokenId)
    }

    class MicroMarket {
        <<Contract>>
        +createFixedPriceSale(address nft, uint256 tokenId, uint256 price)
        +createDutchAuction(address nft, uint256 tokenId, uint256 startPrice, uint256 reservePrice, uint256 duration)
        +buyFixedPrice(uint256 saleId)
        +cancelSale(uint256 saleId)
    }

    UserController --> UserService
    NftController --> NftService
    IpfsController --> IpfsServiceEnhanced
    MarketController --> MarketService
    NftService --> NFTAsset
    MarketService --> MicroMarket
    IpfsServiceEnhanced --> IPFS
```

---

## 7. NFT铸造流程图

```mermaid
flowchart TD
    A[创作者准备作品] --> B[上传文件到IPFS]
    B --> C{上传成功?}
    C -->|否| D[显示错误信息]
    D --> B
    C -->|是| E[填写作品信息]
    E --> F[设置版税比例]
    F --> G[确认铸造]
    G --> H[调用智能合约]
    H --> I{交易确认?}
    I -->|否| J[显示交易失败]
    J --> G
    I -->|是| K[保存到数据库]
    K --> L[显示铸造成功]
    L --> M[NFT可在市场查看]

    style A fill:#e1f5fe
    style L fill:#c8e6c9
    style J fill:#ffcdd2
```

---

## 8. NFT购买流程图

```mermaid
flowchart TD
    A[购买者浏览市场] --> B[选择NFT作品]
    B --> C[查看作品详情]
    C --> D[确认购买]
    D --> E[检查钱包余额]
    E --> F{余额充足?}
    F -->|否| G[提示余额不足]
    F -->|是| H[调用合约购买]
    H --> I[钱包签名确认]
    I --> J{交易成功?}
    J -->|否| K[显示交易失败]
    J -->|是| L[更新订单状态]
    L --> M[转移NFT所有权]
    M --> N[分配版税给创作者]
    N --> O[显示购买成功]
    O --> P[可在我的资产查看]

    style A fill:#e1f5fe
    style O fill:#c8e6c9
    style K fill:#ffcdd2
```

---

## 9. 数据库表关系图

```mermaid
graph LR
    subgraph 用户相关
        U[users<br/>用户表]
        W[wallets<br/>钱包表]
    end

    subgraph NFT相关
        N[nft_assets<br/>NFT资产表]
        V[nft_versions<br/>版本历史表]
    end

    subgraph 交易相关
        M[market_orders<br/>市场订单表]
    end

    subgraph 存储相关
        I[ipfs_file_record<br/>IPFS文件记录表]
    end

    U -->|1:N| W
    U -->|1:N| N
    U -->|1:N| M
    U -->|1:N| I
    N -->|1:N| V
    N -->|1:N| M
    N -->|1:1| I

    style U fill:#bbdefb
    style N fill:#c8e6c9
    style M fill:#fff9c4
    style I fill:#ffccbc
```

---

## 10. 部署架构图

```mermaid
graph TB
    subgraph 客户端
        C1[浏览器Chrome/Firefox]
        C2[MetaMask插件]
    end

    subgraph 服务器层
        N1[Nginx<br/>端口80/443]

        subgraph Docker容器
            F[前端容器<br/>React应用]
            B[后端容器<br/>SpringBoot]
            M[MySQL容器<br/>端口3306]
            R[Redis容器<br/>端口6379]
        end
    end

    subgraph 外部服务
        P[Polygon网络<br/>Amoy测试网]
        I[IPFS网关<br/>ipfs.io]
    end

    C1 -->|HTTPS| N1
    N1 -->|/| F
    N1 -->|/api| B
    F -->|API调用| B
    B -->|JDBC| M
    B -->|Redis协议| R
    B -->|Web3j| P
    B -->|HTTP| I
    C2 -->|签名交易| P

    style N1 fill:#c8e6c9
    style P fill:#fff9c4
    style I fill:#ffccbc
```

---

## 使用说明

### 在 VS Code 中查看
1. 安装插件：Markdown Preview Mermaid Support
2. 打开本文档，按 Ctrl+Shift+V 预览

### 在 Typora 中查看
1. 偏好设置 → Markdown → 启用 Mermaid 图表
2. 文档会自动渲染图表

### 在线编辑
访问 https://mermaid.live/ 复制代码在线编辑和导出图片（PNG/SVG/PDF）