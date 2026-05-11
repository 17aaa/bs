const hre = require("hardhat");
const fs = require("fs");
const path = require("path");

async function main() {
  console.log("🚀 Starting deployment...");

  const [deployer] = await hre.ethers.getSigners();
  console.log("Deploying contracts with account:", deployer.address);

  const balance = await hre.ethers.provider.getBalance(deployer.address);
  console.log("Account balance:", hre.ethers.formatEther(balance), "MATIC");

  // 部署 NFT Asset 合约
  console.log("\n📦 Deploying NFTAsset contract...");
  const NFTAsset = await hre.ethers.getContractFactory("NFTAsset");
  const nftAsset = await NFTAsset.deploy();
  await nftAsset.waitForDeployment();
  const nftAssetAddress = await nftAsset.getAddress();
  console.log("NFTAsset deployed to:", nftAssetAddress);

  // 部署 MicroMarket 合约
  console.log("\n📦 Deploying MicroMarket contract...");
  const MicroMarket = await hre.ethers.getContractFactory("MicroMarket");
  const microMarket = await MicroMarket.deploy();
  await microMarket.waitForDeployment();
  const microMarketAddress = await microMarket.getAddress();
  console.log("MicroMarket deployed to:", microMarketAddress);

  // 部署 FanToken 合约
  console.log("\n📦 Deploying FanToken contract...");
  const FanToken = await hre.ethers.getContractFactory("FanToken");

  // 代币分配配置
  const allocation = {
    publicSale: 40,  // 40% 公募
    creator: 30,     // 30% 创作者（4 年解锁）
    team: 20,        // 20% 团队（1 年解锁）
    staking: 10      // 10% 质押奖励
  };

  const totalSupply = 1000000; // 100 万代币

  const fanToken = await FanToken.deploy(
    "Creative Fan Token",      // 名称
    "CFT",                      // 符号
    deployer.address,           // 创作者地址
    totalSupply,                // 总供应量
    allocation                  // 分配比例
  );
  await fanToken.waitForDeployment();
  const fanTokenAddress = await fanToken.getAddress();
  console.log("FanToken deployed to:", fanTokenAddress);

  // 输出合约地址到配置文件
  const configContent = `# 合约地址（自动生成 - ${new Date().toISOString()}）
blockchain.contract.nft-asset-address=${nftAssetAddress}
blockchain.contract.micro-market-address=${microMarketAddress}
blockchain.contract.fan-token-address=${fanTokenAddress}
`;

  const configPath = path.join(__dirname, "..", "src", "main", "resources", "contracts", "contract-addresses.properties");

  // 确保目录存在
  const configDir = path.dirname(configPath);
  if (!fs.existsSync(configDir)) {
    fs.mkdirSync(configDir, { recursive: true });
  }

  fs.writeFileSync(configPath, configContent);
  console.log("\n✅ Contract addresses saved to:", configPath);

  // 验证合约（仅在测试网/主网执行）
  const networkName = hre.network.name;
  if (networkName !== "hardhat") {
    console.log("\n⏳ Waiting for block explorers to index contracts...");
    await sleep(30000); // 等待 30 秒

    console.log("\n🔍 Verifying contracts on block explorer...");

    try {
      await hre.run("verify:verify", {
        address: nftAssetAddress,
        constructorArguments: []
      });
      console.log("NFTAsset verified!");
    } catch (e) {
      console.log("NFTAsset verification may have failed:", e.message);
    }

    try {
      await hre.run("verify:verify", {
        address: microMarketAddress,
        constructorArguments: []
      });
      console.log("MicroMarket verified!");
    } catch (e) {
      console.log("MicroMarket verification may have failed:", e.message);
    }

    try {
      await hre.run("verify:verify", {
        address: fanTokenAddress,
        constructorArguments: [
          "Creative Fan Token",
          "CFT",
          deployer.address,
          totalSupply,
          allocation
        ]
      });
      console.log("FanToken verified!");
    } catch (e) {
      console.log("FanToken verification may have failed:", e.message);
    }
  }

  console.log("\n✅ Deployment completed!");
  console.log("\n===========================================");
  console.log("Network:", networkName);
  console.log("Deployer:", deployer.address);
  console.log("===========================================");
  console.log("NFT Asset Contract:", nftAssetAddress);
  console.log("Micro Market Contract:", microMarketAddress);
  console.log("Fan Token Contract:", fanTokenAddress);
  console.log("===========================================");

  // 输出区块浏览器链接
  let explorerUrl;
  if (networkName === "amoy") {
    explorerUrl = "https://amoy.polygonscan.com";
  } else if (networkName === "polygon") {
    explorerUrl = "https://polygonscan.com";
  }

  if (explorerUrl) {
    console.log("\n🔗 Block Explorer Links:");
    console.log("NFT Asset:", `${explorerUrl}/address/${nftAssetAddress}`);
    console.log("Micro Market:", `${explorerUrl}/address/${microMarketAddress}`);
    console.log("Fan Token:", `${explorerUrl}/address/${fanTokenAddress}`);
  }
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });