const hre = require("hardhat");
const { ethers } = require("hardhat");

/**
 * 合约功能测试脚本
 * 测试 NFT 铸造、更新元数据等功能
 */
async function main() {
  console.log("🧪 Starting contract testing...\n");

  const [deployer, user1, user2] = await hre.ethers.getSigners();
  console.log("Using accounts:");
  console.log("  Deployer:", deployer.address);
  console.log("  User1:   ", user1.address);
  console.log("  User2:   ", user2.address, "\n");

  // 加载已部署的 NFT Asset 合约
  const nftAssetAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  const NFTAsset = await hre.ethers.getContractFactory("NFTAsset");
  const nftAsset = NFTAsset.attach(nftAssetAddress);

  console.log("NFT Asset Contract loaded at:", nftAssetAddress);

  // 测试 1: 铸造 NFT
  console.log("\n--- Test 1: Mint NFT ---");
  const metadataHash1 = ethers.keccak256(ethers.toUtf8Bytes("Test NFT Metadata 1"));
  const tx1 = await nftAsset.mint(user1.address, metadataHash1);
  await tx1.wait();
  console.log("✅ NFT minted for User1");

  // 获取 Token ID
  const tokenId1 = 0; // 第一个铸造的 Token ID 为 0
  const owner1 = await nftAsset.ownerOf(tokenId1);
  console.log("   Token ID:", tokenId1);
  console.log("   Owner:   ", owner1);
  console.log("   URI:     ", await nftAsset.tokenURI(tokenId1));

  // 测试 2: 再次铸造
  console.log("\n--- Test 2: Mint Another NFT ---");
  const metadataHash2 = ethers.keccak256(ethers.toUtf8Bytes("Test NFT Metadata 2"));
  const tx2 = await nftAsset.mint(user2.address, metadataHash2);
  await tx2.wait();
  console.log("✅ NFT minted for User2");

  const tokenId2 = 1;
  const owner2 = await nftAsset.ownerOf(tokenId2);
  console.log("   Token ID:", tokenId2);
  console.log("   Owner:   ", owner2);

  // 测试 3: 更新元数据
  console.log("\n--- Test 3: Update Metadata ---");
  const newMetadataHash = ethers.keccak256(ethers.toUtf8Bytes("Updated Metadata"));
  const tx3 = await nftAsset.updateMetadata(tokenId1, newMetadataHash);
  await tx3.wait();
  console.log("✅ Metadata updated for Token #1");

  // 获取版本历史
  const versionCount = await nftAsset.getCurrentVersion(tokenId1);
  console.log("   Current Version:", versionCount.toString());

  // 测试 4: 设置版税
  console.log("\n--- Test 4: Set Royalty ---");
  const tx4 = await nftAsset.setRoyalty(tokenId1, user1.address, 750); // 7.5%
  await tx4.wait();
  console.log("✅ Royalty set for Token #1");

  const royaltyInfo = await nftAsset.getRoyaltyInfo(tokenId1);
  console.log("   Recipient:", royaltyInfo[0]);
  console.log("   Fee:      ", royaltyInfo[1].toString(), "(basis points)");

  // 测试 5: 查询余额
  console.log("\n--- Test 5: Check Balances ---");
  const balance1 = await nftAsset.balanceOf(user1.address);
  const balance2 = await nftAsset.balanceOf(user2.address);
  console.log("   User1 NFT count:", balance1.toString());
  console.log("   User2 NFT count:", balance2.toString());

  // 测试 6: 获取版本历史
  console.log("\n--- Test 6: Version History ---");
  const history0 = await nftAsset.versionHistory(tokenId1, 0);
  console.log("   Token #1 Version 0:");
  console.log("     - Version:      ", history0[0].toString());
  console.log("     - Metadata Hash:", history0[1]);
  console.log("     - Updater:      ", history0[4]);

  const history1 = await nftAsset.versionHistory(tokenId1, 1);
  console.log("   Token #1 Version 1:");
  console.log("     - Version:      ", history1[0].toString());
  console.log("     - Metadata Hash:", history1[1]);
  console.log("     - Updater:      ", history1[4]);

  console.log("\n===========================================");
  console.log("✅ All tests passed!");
  console.log("===========================================\n");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("Test failed:", error);
    process.exit(1);
  });