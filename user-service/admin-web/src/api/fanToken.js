import request from './request';

// 获取粉丝代币列表
export const getFanTokenList = async (params) => {
  const response = await request.get('/admin/fan-tokens', { params });
  return response.data;
};

// 获取粉丝代币详情
export const getFanTokenDetail = async (tokenId) => {
  const response = await request.get(`/admin/fan-tokens/${tokenId}`);
  return response.data;
};

// 激活公募
export const activatePublicSale = async (tokenId) => {
  const response = await request.post(`/admin/fan-tokens/${tokenId}/activate`);
  return response.data;
};

// 暂停公募
export const pausePublicSale = async (tokenId) => {
  const response = await request.post(`/admin/fan-tokens/${tokenId}/pause`);
  return response.data;
};

// 结束公募
export const endPublicSale = async (tokenId) => {
  const response = await request.post(`/admin/fan-tokens/${tokenId}/end`);
  return response.data;
};