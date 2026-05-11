import request from './request';

// 登录
export const login = async (username, password) => {
  const response = await request.post('/user/login', null, {
    params: { username, password },
  });
  return response.data;
};

// 获取当前用户信息
export const getUserInfo = async () => {
  const response = await request.get('/user/info');
  return response.data;
};

// 注销
export const logout = async () => {
  const response = await request.post('/user/logout');
  return response.data;
};