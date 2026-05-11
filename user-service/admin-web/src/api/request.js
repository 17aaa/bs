import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token');
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

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data } = response;
    if (data && data.status === 'error') {
      return Promise.reject(new Error(data.message || '请求失败'));
    }
    return response;
  },
  (error) => {
    let errorMessage = '网络错误，请稍后重试';

    if (error.response) {
      switch (error.response.status) {
        case 401:
          errorMessage = '未授权，请重新登录';
          localStorage.removeItem('admin_token');
          localStorage.removeItem('admin_userInfo');
          window.location.href = '/login';
          break;
        case 403:
          errorMessage = '无权访问';
          break;
        case 404:
          errorMessage = '请求的资源不存在';
          break;
        case 500:
          errorMessage = '服务器错误，请稍后重试';
          break;
        default:
          errorMessage = `请求失败：${error.response.status}`;
      }
    }

    return Promise.reject(new Error(errorMessage));
  }
);

export default request;