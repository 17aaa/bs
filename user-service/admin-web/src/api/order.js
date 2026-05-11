import request from './request';

// 获取订单列表
export const getOrderList = async (params) => {
  const response = await request.get('/admin/orders', { params });
  return response.data;
};

// 获取订单详情
export const getOrderDetail = async (orderId) => {
  const response = await request.get(`/admin/orders/${orderId}`);
  return response.data;
};

// 获取订单统计
export const getStatistics = async () => {
  const response = await request.get('/admin/orders/statistics');
  return response.data;
};