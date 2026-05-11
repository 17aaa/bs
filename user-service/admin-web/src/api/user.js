import request from './request';

// 获取用户列表
export const getUserList = async (params) => {
  const response = await request.get('/admin/users', { params });
  return response.data;
};

// 获取用户详情
export const getUserDetail = async (userId) => {
  const response = await request.get(`/admin/users/${userId}`);
  return response.data;
};

// 更新用户信息
export const updateUser = async (userId, data) => {
  const response = await request.put(`/admin/users/${userId}`, data);
  return response.data;
};

// 重置用户密码
export const resetPassword = async (userId, newPassword) => {
  const response = await request.post(`/admin/users/${userId}/reset-password`, null, {
    params: { newPassword },
  });
  return response.data;
};

// 封禁用户
export const banUser = async (userId, reason) => {
  const response = await request.post(`/admin/users/${userId}/ban`, { reason });
  return response.data;
};

// 解封用户
export const unbanUser = async (userId) => {
  const response = await request.post(`/admin/users/${userId}/unban`);
  return response.data;
};

// 升级为管理员
export const upgradeToAdmin = async (userId) => {
  const response = await request.post(`/admin/users/${userId}/upgrade`);
  return response.data;
};

// 降级为普通用户
export const downgradeToUser = async (userId) => {
  const response = await request.post(`/admin/users/${userId}/downgrade`);
  return response.data;
};