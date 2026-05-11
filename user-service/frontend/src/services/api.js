import axios from 'axios';

// 使用环境变量或默认值
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8085';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 自动解包 ApiResponse，401 时自动刷新 Token
let isRefreshing = false;
let pendingRequests = [];

const redirectToLogin = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('userId');
  window.location.href = '/login';
};

api.interceptors.response.use(
  (response) => {
    const { data } = response;
    if (data && data.status === 'error') {
      return Promise.reject(new Error(data.message || '请求失败'));
    }
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retried) {
      const refreshToken = localStorage.getItem('refreshToken');

      // 无 refreshToken 直接跳登录
      if (!refreshToken) {
        redirectToLogin();
        return Promise.reject(error);
      }

      // 已在刷新中，将请求加入等待队列
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingRequests.push({ resolve, reject });
        }).then((newToken) => {
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return api(originalRequest);
        });
      }

      originalRequest._retried = true;
      isRefreshing = true;

      try {
        const resp = await api.post('/user/token/refresh', null, {
          params: { refreshToken },
        });
        const newToken = resp.data?.data?.token;
        if (!newToken) throw new Error('刷新失败');

        localStorage.setItem('token', newToken);
        api.defaults.headers.common.Authorization = `Bearer ${newToken}`;

        // 释放等待队列
        pendingRequests.forEach(({ resolve }) => resolve(newToken));
        pendingRequests = [];

        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        pendingRequests.forEach(({ reject }) => reject(refreshError));
        pendingRequests = [];
        redirectToLogin();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    let errorMessage = '网络错误，请稍后重试';
    if (error.response) {
      switch (error.response.status) {
        case 403: errorMessage = '无权访问'; break;
        case 404: errorMessage = '请求的资源不存在'; break;
        case 400: errorMessage = error.response.data?.message || '请求参数错误'; break;
        case 500: errorMessage = '服务器错误，请稍后重试'; break;
        default: errorMessage = `请求失败：${error.response.status}`;
      }
    } else if (error.request) {
      errorMessage = '无法连接到服务器，请检查网络';
    } else {
      errorMessage = error.message || '请求错误';
    }

    console.error(errorMessage);
    return Promise.reject(new Error(errorMessage));
  }
);

// NFT API
export const nftApi = {
  // 铸造 NFT
  mintNft: async (params) => {
    const response = await api.post('/api/nft/mint', params);
    return response.data;
  },

  // 上传图片
  uploadImage: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/api/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // 获取 NFT 详情
  getNftAsset: async (id) => {
    const response = await api.get(`/api/nft/${id}`);
    return response.data;
  },

  // 获取版本历史
  getVersionHistory: async (id) => {
    const response = await api.get(`/api/nft/${id}/versions`);
    return response.data;
  },

  // 获取用户持有的 NFT
  getNftsByOwner: async (address) => {
    const response = await api.get(`/api/nft/owner/${address}`);
    return response.data;
  },

  // 获取创作者的 NFT
  getNftsByCreator: async (address) => {
    const response = await api.get(`/api/nft/creator/${address}`);
    return response.data;
  },

  // 获取所有 NFT（首页）
  getAllNfts: async (limit = 20, offset = 0) => {
    const response = await api.get('/api/nft/list', { params: { limit, offset } });
    return response.data;
  },

  // 更新 NFT 元数据
  updateMetadata: async (id, params) => {
    const response = await api.post(`/api/nft/${id}/update`, params);
    return response.data;
  },
};

// 市场 API
export const marketApi = {
  // 创建固定价格销售
  createFixedPriceSale: async ({ sellerAddress, nftContract, tokenId, price, nftAssetId, paymentToken, endTime }) => {
    const params = {
      sellerAddress,
      nftContract,
      tokenId,
      price,
      nftAssetId,
      paymentToken,
      endTime,
    };
    // 过滤掉 undefined 和 null 值
    Object.keys(params).forEach(key => params[key] === undefined && delete params[key]);
    const response = await api.post('/api/market/list', null, { params });
    return response.data;
  },

  // 创建荷兰拍卖
  createDutchAuction: async (params) => {
    const response = await api.post('/api/market/auction', null, { params });
    return response.data;
  },

  // 购买 NFT
  buyNft: async (orderId, buyerAddress) => {
    const response = await api.post(`/api/market/buy/${orderId}`, null, {
      params: { buyerAddress },
    });
    return response.data;
  },

  // 取消订单
  cancelOrder: async (orderId, sellerAddress) => {
    const response = await api.post(`/api/market/cancel/${orderId}`, null, {
      params: { sellerAddress },
    });
    return response.data;
  },

  // 创建报价
  createOffer: async (params) => {
    const response = await api.post('/api/market/offer', null, { params });
    return response.data;
  },

  // 接受报价
  acceptOffer: async (offerId, sellerAddress) => {
    const response = await api.post(`/api/market/offer/${offerId}/accept`, null, {
      params: { sellerAddress },
    });
    return response.data;
  },

  // 获取订单列表
  getOrderList: async (params) => {
    const response = await api.get('/api/market/orders', { params });
    return response.data;
  },

  // 获取订单详情
  getOrder: async (orderId) => {
    const response = await api.get(`/api/market/order/${orderId}`);
    return response.data;
  },

  // 获取用户交易历史（买家+卖家）
  getUserHistory: async (address, page = 1, size = 20) => {
    const response = await api.get('/api/market/history', { params: { address, page, size } });
    return response.data;
  },
};

// 上传 API
export const uploadApi = {
  // 上传图片
  uploadImage: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/api/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // 上传文件
  uploadFile: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/api/upload/file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // 获取文件列表
  listFiles: async () => {
    const response = await api.get('/api/upload/list');
    return response.data;
  },
};

// 粉丝代币 API
export const fanTokenApi = {
  // 创建粉丝代币
  createFanToken: async ({ projectId, creatorAddress, name, symbol, totalSupply }) => {
    const response = await api.post('/api/fan-token/create', null, {
      params: { projectId, creatorAddress, name, symbol, totalSupply },
    });
    return response.data;
  },

  // 获取创作者的粉丝代币列表
  getTokensByCreator: async (address) => {
    const response = await api.get(`/api/fan-token/creator/${address}`);
    return response.data;
  },

  // 参与公募（buy tokens）
  participateInSale: async ({ tokenAddress, buyerAddress, amount, paymentAmount }) => {
    const response = await api.post('/api/fan-token/participate', null, {
      params: { tokenAddress, buyerAddress, amount, paymentAmount },
    });
    return response.data;
  },

  // 质押代币
  stake: async ({ userAddress, tokenAddress, amount }) => {
    const response = await api.post('/api/fan-token/stake', null, {
      params: { userAddress, tokenAddress, amount },
    });
    return response.data;
  },

  // 解除质押
  unstake: async ({ userAddress, tokenAddress, amount }) => {
    const response = await api.post('/api/fan-token/unstake', null, {
      params: { userAddress, tokenAddress, amount },
    });
    return response.data;
  },

  // 领取奖励
  claimReward: async ({ userAddress, tokenAddress }) => {
    const response = await api.post('/api/fan-token/reward', null, {
      params: { userAddress, tokenAddress },
    });
    return response.data;
  },

  // 获取质押记录
  getStakeRecord: async (userAddress, tokenAddress) => {
    const response = await api.get('/api/fan-token/stake-record', {
      params: { userAddress, tokenAddress },
    });
    return response.data;
  },
};

// 用户 API
export const userApi = {
  // 登录
  login: async (username, password) => {
    const response = await api.post('/user/login', null, {
      params: { username, password },
    });
    return response.data;
  },

  // 注册
  register: async (userData) => {
    const response = await api.post('/user/register', userData);
    return response.data;
  },

  // 获取或创建钱包
  getOrCreateWallet: async (userId, password, token) => {
    const response = await api.post('/user/wallet/get-or-create', null, {
      params: { userId, password },
      headers: { Authorization: token },
    });
    return response.data;
  },

  // 注销
  logout: async () => {
    const response = await api.post('/user/logout');
    return response.data;
  },

  // 获取当前用户信息
  getUserInfo: async () => {
    const response = await api.get('/user/info');
    return response.data;
  },

  // 更新用户信息
  updateUserInfo: async (userData) => {
    const response = await api.put('/user/info', userData);
    return response.data;
  },

  // 更新头像
  updateAvatar: async (formData) => {
    const response = await api.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },
};

// 钱包 API
export const walletApi = {
  // 获取签名 challenge
  getChallenge: async (walletAddress) => {
    const response = await api.get('/api/wallet/challenge', {
      params: { address: walletAddress },
    });
    return response.data;
  },

  // 验证签名并登录
  verifySignature: async (walletAddress, signature, message) => {
    const response = await api.post('/api/wallet/verify', null, {
      params: { address: walletAddress, signature },
    });
    return response.data;
  },

  // 绑定钱包
  bindWallet: async (userId, walletAddress) => {
    const response = await api.post('/api/wallet/bind', null, {
      params: { userId, walletAddress },
    });
    return response.data;
  },

  // 获取钱包地址
  getWalletAddress: async (userId) => {
    const response = await api.get(`/api/wallet/address/${userId}`);
    return response.data;
  },

  // 创建钱包
  createWallet: async (password) => {
    const response = await api.post('/api/wallet/create', null, {
      params: { password },
    });
    return response.data;
  },
};

// IPFS API
export const ipfsApi = {
  // 上传单个文件到 IPFS（增强版）
  uploadFile: async (file, options = {}) => {
    const formData = new FormData();
    formData.append('file', file);
    if (options.description) formData.append('description', options.description);
    if (options.tags) formData.append('tags', options.tags);
    if (options.pin !== undefined) formData.append('pin', options.pin);

    const response = await api.post('/api/ipfs/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // 上传图片到 IPFS（兼容旧接口）
  uploadImage: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/api/ipfs/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // 批量上传文件
  uploadFiles: async (files, options = {}) => {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    if (options.description) formData.append('description', options.description);
    if (options.tags) formData.append('tags', options.tags);
    if (options.pin !== undefined) formData.append('pin', options.pin);

    const response = await api.post('/api/ipfs/upload/batch', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // 上传 JSON 元数据到 IPFS
  uploadMetadata: async (metadata) => {
    const response = await api.post('/api/ipfs/upload/metadata', metadata);
    return response.data;
  },

  // 创建完整的 NFT 元数据（包含图片）
  createNftMetadata: async (name, description, image, attributes) => {
    const formData = new FormData();
    formData.append('name', name);
    formData.append('description', description);
    formData.append('image', image);
    if (attributes) {
      formData.append('attributes', JSON.stringify(attributes));
    }
    const response = await api.post('/api/ipfs/nft/metadata', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // 使用已上传的图片创建 NFT 元数据
  createNftMetadataWithImage: async (name, description, imageCid, attributes) => {
    const response = await api.post('/api/ipfs/nft/metadata/with-image', {
      name,
      description,
      imageCid,
      attributes,
    });
    return response.data;
  },

  // 固定文件
  pinFile: async (cid) => {
    const response = await api.post(`/api/ipfs/pin/${cid}`);
    return response.data;
  },

  // 取消固定文件
  unpinFile: async (cid) => {
    const response = await api.post(`/api/ipfs/unpin/${cid}`);
    return response.data;
  },

  // 获取固定列表
  getPinnedFiles: async () => {
    const response = await api.get('/api/ipfs/pins');
    return response.data;
  },

  // 根据 CID 获取文件信息
  getFileByCid: async (cid) => {
    const response = await api.get(`/api/ipfs/file/${cid}`);
    return response.data;
  },

  // 查询文件列表
  queryFiles: async (params = {}) => {
    const response = await api.get('/api/ipfs/files', { params });
    return response.data;
  },

  // 获取用户的文件列表
  getUserFiles: async (userId) => {
    const response = await api.get(`/api/ipfs/files/user/${userId}`);
    return response.data;
  },

  // 获取钱包地址的文件列表
  getOwnerFiles: async (ownerAddress) => {
    const response = await api.get(`/api/ipfs/files/owner/${ownerAddress}`);
    return response.data;
  },

  // 从 IPFS 下载文件
  downloadFile: async (cid) => {
    const response = await api.get(`/api/ipfs/cat/${cid}`, {
      responseType: 'blob',
    });
    return response.data;
  },

  // 获取文件内容（自动检测类型）
  getContent: async (cid) => {
    const response = await api.get(`/api/ipfs/content/${cid}`, {
      responseType: 'blob',
    });
    return response.data;
  },

  // 获取 IPFS 网关 URL
  getGatewayUrl: async (cid) => {
    const response = await api.get(`/api/ipfs/gateway/${cid}`);
    return response.data;
  },

  // 获取 IPFS 统计信息
  getStatistics: async () => {
    const response = await api.get('/api/ipfs/statistics');
    return response.data;
  },

  // 更新文件信息
  updateFileInfo: async (cid, data) => {
    const response = await api.put(`/api/ipfs/file/${cid}`, null, { params: data });
    return response.data;
  },

  // 删除文件记录
  deleteFile: async (cid) => {
    const response = await api.delete(`/api/ipfs/file/${cid}`);
    return response.data;
  },
};

export default api;