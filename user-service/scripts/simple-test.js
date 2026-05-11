const hre = require("hardhat");
const { ethers } = require("hardhat");

async function main() {
  console.log("🧪 Simple Contract Test\n");

  const [deployer, user1] = await hre.ethers.getSigners();
  console.log("Deployer:", deployer.address);
  console.log("User1:   ", user1.address, "\n");

  // 部署新合约
  console.log("Deploying NFTAsset contract...");
  const NFTAsset = await hre.ethers.getContractFactory("NFTAsset");
  const nftAsset = await NFTAsset.deploy();
  await nftAsset.waitForDeployment();

  const address = await nftAsset.getAddress();
  console.log("Deployed to:", address, "\n");

  // 测试 mint
  console.log("Minting NFT for user1...");
  const tx = await nftAsset.mint(user1.address, "ipfs://test123");
  await tx.wait();
  console.log("✅ Minted!\n");

  // 测试 ownerOf
  console.log("Testing ownerOf...");
  try {
    const owner = await nftAsset.ownerOf(0);
    console.log("✅ Owner of Token #0:", owner);
  } catch (error) {
    console.error("❌ ownerOf failed:", error.message);
  }

  // 测试 balanceOf
  console.log("\nTesting balanceOf...");
  try {
    const balance = await nftAsset.balanceOf(user1.address);
    console.log("✅ User1 balance:", balance.toString());
  } catch (error) {
    console.error("❌ balanceOf failed:", error.message);
  }

  // 测试 tokenURI
  console.log("\nTesting tokenURI...");
  try {
    const uri = await nftAsset.tokenURI(0);
    console.log("✅ Token URI:", uri);
  } catch (error) {
    console.error("❌ tokenURI failed:", error.message);
  }

  console.log("\n✅ Test completed!");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("Test failed:", error);
    process.exit(1);
  });