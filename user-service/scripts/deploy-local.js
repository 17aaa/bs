const hre = require("hardhat");
const fs = require("fs");
const path = require("path");

/**
 * 本地 Hardhat 网络部署脚本
 * 用于快速测试，无需测试网 MATIC
 */
async function main() {
  console.log("🚀 Starting local deployment on Hardhat network...");

  const [deployer] = await hre.ethers.getSigners();
  console.log("Deploying contracts with account:", deployer.address);

  const balance = await hre.ethers.provider.getBalance(deployer.address);
  console.log("Account balance:", hre.ethers.formatEther(balance), "ETH");

  // 部署 NFT Asset 合约
  console.log("\n📦 Deploying NFTAsset contract...");
  const NFTAsset = await hre.ethers.getContractFactory("NFTAsset");
  const nftAsset = await NFTAsset.deploy();
  await nftAsset.waitForDeployment();
  const nftAssetAddress = await nftAsset.getAddress();
  console.log("✅ NFTAsset deployed to:", nftAssetAddress);

  // 部署 MicroMarket 合约
  console.log("\n📦 Deploying MicroMarket contract...");
  const MicroMarket = await hre.ethers.getContractFactory("MicroMarket");
  const microMarket = await MicroMarket.deploy();
  await microMarket.waitForDeployment();
  const microMarketAddress = await microMarket.getAddress();
  console.log("✅ MicroMarket deployed to:", microMarketAddress);

  // 部署 FanToken 合约
  console.log("\n📦 Deploying FanToken contract...");
  const FanToken = await hre.ethers.getContractFactory("FanToken");

  const allocation = {
    publicSale: 40,
    creator: 30,
    team: 20,
    staking: 10
  };

  const totalSupply = 1000000;

  const fanToken = await FanToken.deploy(
    "Creative Fan Token",
    "CFT",
    deployer.address,
    totalSupply,
    allocation
  );
  await fanToken.waitForDeployment();
  const fanTokenAddress = await fanToken.getAddress();
  console.log("✅ FanToken deployed to:", fanTokenAddress);

  // 保存合约地址到本地文件
  const addresses = {
    network: "hardhat",
    deployer: deployer.address,
    contracts: {
      nftAsset: nftAssetAddress,
      microMarket: microMarketAddress,
      fanToken: fanTokenAddress
    },
    deployedAt: new Date().toISOString()
  };

  const outputPath = path.join(__dirname, "..", "deployments", "hardhat.json");
  const deployDir = path.dirname(outputPath);

  if (!fs.existsSync(deployDir)) {
    fs.mkdirSync(deployDir, { recursive: true });
  }

  fs.writeFileSync(outputPath, JSON.stringify(addresses, null, 2));
  console.log("\n✅ Contract addresses saved to:", outputPath);

  // 输出摘要
  console.log("\n===========================================");
  console.log("✅ Local Deployment Completed!");
  console.log("===========================================");
  console.log("Network: Hardhat (Local)");
  console.log("Deployer:", deployer.address);
  console.log("===========================================");
  console.log("NFT Asset Contract:    ", nftAssetAddress);
  console.log("Micro Market Contract: ", microMarketAddress);
  console.log("Fan Token Contract:    ", fanTokenAddress);
  console.log("===========================================");

  // 生成后端配置
  console.log("\n📝 Backend configuration (for testing):");
  console.log("-------------------------------------------");
  console.log("# 在 application.yml 或环境变量中设置：");
  console.log(`BLOCKCHAIN_PLATFORM_PRIVATE_KEY=0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80`);
  console.log(`NFT_ASSET_CONTRACT_ADDRESS=${nftAssetAddress}`);
  console.log(`MICRO_MARKET_CONTRACT_ADDRESS=${microMarketAddress}`);
  console.log(`FAN_TOKEN_CONTRACT_ADDRESS=${fanTokenAddress}`);
  console.log("-------------------------------------------");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("Deployment failed:", error);
    process.exit(1);
  });