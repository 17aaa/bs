import { ethers } from 'ethers';

/**
 * Web3 钱包服务
 * 提供 MetaMask 钱包连接、签名等功能
 */
class Web3Service {
  constructor() {
    this.provider = null;
    this.signer = null;
    this.account = null;
    this.chainId = null;
  }

  // 检查是否安装 MetaMask
  isMetaMaskInstalled() {
    return typeof window !== 'undefined' && typeof window.ethereum !== 'undefined';
  }

  // 连接钱包
  async connect() {
    // 如果没有 MetaMask，使用测试账户（仅用于开发测试）
    if (!this.isMetaMaskInstalled()) {
      console.warn('MetaMask 未安装，使用测试账户模式');
      this.account = '0x70997970C51812dc3A010C7d01b50e0d17dc79C8';
      this.chainId = 31337; // Hardhat 本地网络
      return {
        account: this.account,
        chainId: this.chainId,
        isMock: true,
      };
    }

    try {
      this.provider = new ethers.BrowserProvider(window.ethereum);
      const accounts = await this.provider.send('eth_requestAccounts', []);
      this.account = accounts[0];
      this.signer = await this.provider.getSigner();
      this.chainId = await this.getChainId();

      return {
        account: this.account,
        chainId: this.chainId,
      };
    } catch (error) {
      console.error('连接钱包失败:', error);
      throw error;
    }
  }

  // 获取链 ID
  async getChainId() {
    if (!this.provider) {
      throw new Error('钱包未连接');
    }
    const network = await this.provider.getNetwork();
    return Number(network.chainId);
  }

  // 切换到指定网络
  async switchChain(chainId) {
    if (!this.isMetaMaskInstalled()) {
      throw new Error('请安装 MetaMask 钱包');
    }

    try {
      await window.ethereum.request({
        method: 'wallet_switchEthereumChain',
        params: [{ chainId: `0x${chainId.toString(16)}` }],
      });
      return true;
    } catch (error) {
      // 如果网络不存在，尝试添加
      if (error.code === 4902) {
        return this.addChain(chainId);
      }
      throw error;
    }
  }

  // 添加 Polygon Amoy 测试网
  async addChain(chainId) {
    const networks = {
      80002: {
        chainId: '0x13882',
        chainName: 'Polygon Amoy Testnet',
        nativeCurrency: {
          name: 'MATIC',
          symbol: 'MATIC',
          decimals: 18,
        },
        rpcUrls: ['https://rpc-amoy.polygon.technology/'],
        blockExplorerUrls: ['https://amoy.polygonscan.com/'],
      },
      137: {
        chainId: '0x89',
        chainName: 'Polygon Mainnet',
        nativeCurrency: {
          name: 'MATIC',
          symbol: 'MATIC',
          decimals: 18,
        },
        rpcUrls: ['https://polygon-rpc.com/'],
        blockExplorerUrls: ['https://polygonscan.com/'],
      },
    };

    const network = networks[chainId];
    if (!network) {
      throw new Error('不支持的网络');
    }

    try {
      await window.ethereum.request({
        method: 'wallet_addEthereumChain',
        params: [network],
      });
      return true;
    } catch (error) {
      console.error('添加网络失败:', error);
      throw error;
    }
  }

  // 签名消息
  async signMessage(message) {
    if (!this.signer) {
      throw new Error('钱包未连接');
    }
    return await this.signer.signMessage(message);
  }

  // 获取余额
  async getBalance(address) {
    if (!this.provider) {
      throw new Error('钱包未连接');
    }
    const balance = await this.provider.getBalance(address);
    return ethers.formatEther(balance);
  }

  // 发送交易
  async sendTransaction(to, value, data) {
    if (!this.signer) {
      throw new Error('钱包未连接');
    }

    const tx = {
      to,
      value: ethers.parseEther(value.toString()),
      data,
    };

    const txResponse = await this.signer.sendTransaction(tx);
    return await txResponse.wait();
  }

  // 调用合约读取方法
  async callContract(contractAddress, abi, functionName, args = []) {
    if (!this.provider) {
      throw new Error('钱包未连接');
    }

    const contract = new ethers.Contract(contractAddress, abi, this.provider);
    return await contract[functionName](...args);
  }

  // 调用合约写入方法
  async writeContract(contractAddress, abi, functionName, args = []) {
    if (!this.signer) {
      throw new Error('钱包未连接');
    }

    const contract = new ethers.Contract(contractAddress, abi, this.signer);
    const txResponse = await contract[functionName](...args);
    return await txResponse.wait();
  }

  // 断开连接
  disconnect() {
    this.provider = null;
    this.signer = null;
    this.account = null;
    this.chainId = null;
  }

  // 获取当前账户
  getAccount() {
    return this.account;
  }

  // 获取签名器
  getSigner() {
    return this.signer;
  }

  // 自动连接钱包（登录后使用）
  async autoConnect(savedWalletAddress) {
    // 如果保存了钱包地址，说明是后端创建的真正钱包
    if (savedWalletAddress) {
      console.log('使用后端创建的钱包地址自动连接:', savedWalletAddress);
      this.account = savedWalletAddress;
      this.chainId = 80002; // Polygon Amoy 测试网
      this.provider = new ethers.JsonRpcProvider('https://rpc-amoy.polygon.technology/');
      return {
        account: this.account,
        chainId: this.chainId,
        isMock: false,  // 真正的钱包，不是测试模式
        isAutoConnect: true,
        isServerWallet: true,  // 标记为后端创建的钱包
      };
    }

    // 否则尝试连接 MetaMask
    return await this.connect();
  }

  // 从本地存储加载钱包信息
  loadSavedWallet() {
    const walletConnected = localStorage.getItem('walletConnected');
    const walletAddress = localStorage.getItem('walletAddress');
    const token = localStorage.getItem('token');

    if (walletConnected === 'true' && walletAddress) {
      return {
        walletConnected: true,
        walletAddress: walletAddress,
        token: token,
      };
    }

    return null;
  }

  // 清除保存的钱包信息
  clearSavedWallet() {
    localStorage.removeItem('walletConnected');
    localStorage.removeItem('walletAddress');
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    this.disconnect();
  }
}

export default new Web3Service();