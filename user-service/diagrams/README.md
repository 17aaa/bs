# 设计图说明

本目录包含毕业设计论文所需的 PlantUML 图表源文件。

## 文件清单

| 文件 | 图名 | 类型 |
|------|------|------|
| 3-01-system-module.puml | 图3-1 系统功能模块图 | 组件图 |
| 3-02-user-auth-uc.puml | 图3-2 用户认证模块用例图 | 用例图 |
| 3-03-wallet-uc.puml | 图3-3 钱包管理模块用例图 | 用例图 |
| 3-04-ipfs-storage-uc.puml | 图3-4 IPFS存储模块用例图 | 用例图 |
| 3-05-nft-mint-uc.puml | 图3-5 NFT铸造模块用例图 | 用例图 |
| 3-06-nft-review-uc.puml | 图3-6 NFT审核模块用例图 | 用例图 |
| 3-07-market-trade-uc.puml | 图3-7 市场交易模块用例图 | 用例图 |
| 3-08-version-control-uc.puml | 图3-8 版本控制模块用例图 | 用例图 |
| 3-09-fan-token-uc.puml | 图3-9 粉丝代币模块用例图 | 用例图 |
| 3-10-notification-audit-uc.puml | 图3-10 通知与审计模块用例图 | 用例图 |
| 3-11-system-architecture.puml | 图3-11 系统架构图 | 分层架构图 |
| 3-12-tech-stack.puml | 图3-12 技术体系图 | 技术栈图 |
| 3-13-deployment.puml | 图3-13 系统部署图 | 部署图 |
| 3-14-wallet-auth-flow.puml | 图3-14 钱包认证模块流程图 | 活动图 |
| 3-15-ipfs-upload-flow.puml | 图3-15 IPFS文件上传流程图 | 活动图 |
| 3-16-nft-mint-flow.puml | 图3-16 NFT铸造流程图 | 活动图 |
| 3-17-nft-review-flow.puml | 图3-17 NFT审核流程图 | 活动图 |
| 3-18-nft-purchase-flow.puml | 图3-18 NFT购买流程图 | 活动图 |
| 3-19-er-diagram.puml | 图3-19 E-R图 | 类图 |
| 3-15-er-diagram.drawio | 图3-19 E-R图（drawio版） | drawio |
| 3-15-entity-attributes.drawio | 实体属性图（drawio版） | drawio |
| 4-01-wallet-auth-sequence.puml | 图4-1 钱包认证时序图 | 时序图 |
| 4-03-ipfs-dual-storage-sequence.puml | 图4-3 IPFS双存储时序图 | 时序图 |
| 4-05-nft-mint-sequence.puml | 图4-5 NFT铸造时序图 | 时序图 |
| 4-07-market-trade-sequence.puml | 图4-7 市场交易时序图 | 时序图 |
| 4-10-fantoken-stake-reward-flow.puml | 图4-10 粉丝代币质押奖励流程图 | 活动图 |

> 第四章界面截图（图4-2、4-4、4-6、4-8、4-9、4-11、4-12）需运行系统后手动截取。

## 渲染方式

### 方式一：VS Code 插件（推荐）

1. 安装 VS Code 插件：[PlantUML](https://marketplace.visualstudio.com/items?itemName=jebbs.plantuml)
2. 打开 `.puml` 文件，按 `Alt+D` 预览
3. 右键 → "Export Current Diagram" 导出为 PNG/SVG

### 方式二：命令行（PlantUML JAR）

```bash
# 安装（需 Java 运行时）
brew install plantuml  # macOS
# 或下载 jar: https://github.com/plantuml/plantuml/releases

# 批量导出为 PNG
plantuml -tpng diagrams/*.puml

# 批量导出为 SVG（矢量图，论文推荐）
plantuml -tsvg diagrams/*.puml
```

### 方式三：在线渲染

访问 [PlantUML Web Server](https://www.plantuml.com/plantuml/uml/)，粘贴 `.puml` 文件内容即可预览和导出。

### 方式四：IntelliJ IDEA 插件

安装 [PlantUML Integration](https://plugins.jetbrains.com/plugin/7017-plantuml-integration) 插件，打开 `.puml` 文件即可预览和导出。

## 论文使用建议

- 论文插图建议导出为 **SVG** 格式（矢量图，缩放不失真）
- 如果 Word 不支持 SVG，导出为 **PNG** 时指定 DPI：`plantuml -tpng -DPLANTUML_LIMIT_SIZE=8192 -dpi=300 diagrams/*.puml`
- 插入论文时按图号引用，如"如图3-12所示"