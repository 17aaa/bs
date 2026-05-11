import request from './request';

// 获取 NFT 列表
export const getNftList = async (params) => {
  const response = await request.get('/admin/nfts', { params });
  return response.data;
};

// 获取 NFT 详情
export const getNftDetail = async (nftId) => {
  const response = await request.get(`/admin/nfts/${nftId}`);
  return response.data;
};

// 获取 NFT 版本历史
export const getNftVersions = async (nftId) => {
  const response = await request.get(`/admin/nfts/${nftId}/versions`);
  return response.data;
};

// 下架 NFT
export const delistNft = async (nftId) => {
  const response = await request.post(`/admin/nfts/${nftId}/delist`);
  return response.data;
};

// 冻结 NFT
export const freezeNft = async (nftId, reason) => {
  const response = await request.post(`/admin/nfts/${nftId}/freeze`, null, {
    params: { reason },
  });
  return response.data;
};

// 解冻 NFT
export const unfreezeNft = async (nftId) => {
  const response = await request.post(`/admin/nfts/${nftId}/unfreeze`);
  return response.data;
};

// 获取分类统计
export const getCategoryStats = async () => {
  const response = await request.get('/admin/nfts/category-stats');
  return response.data;
};

// 获取待审核 NFT 列表
export const getPendingReviews = async (page = 1, size = 20) => {
  const response = await request.get('/api/nft/review/pending', { params: { page, size } });
  return response.data;
};

// 审核通过
export const approveNft = async (nftAssetId, comment) => {
  const response = await request.post(`/api/nft/review/approve/${nftAssetId}`, null, {
    params: comment ? { comment } : {},
  });
  return response.data;
};

// 审核拒绝
export const rejectNft = async (nftAssetId, rejectReason, comment) => {
  const response = await request.post(`/api/nft/review/reject/${nftAssetId}`, null, {
    params: { rejectReason, ...(comment ? { comment } : {}) },
  });
  return response.data;
};