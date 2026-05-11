import request from './request';

// 获取仪表盘概览
export const getOverview = async () => {
  const response = await request.get('/admin/dashboard/overview');
  return response.data;
};