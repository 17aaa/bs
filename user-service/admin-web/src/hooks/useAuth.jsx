import { useState, useEffect, createContext, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import * as authApi from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [userInfo, setUserInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    const token = localStorage.getItem('admin_token');
    if (!token) {
      setLoading(false);
      return;
    }

    try {
      const response = await authApi.getUserInfo();
      if (response.status === 'success' && response.data) {
        const role = response.data.role || 'user';
        // 只有 admin 或 super_admin 才允许访问管理端
        if (role !== 'admin' && role !== 'super_admin') {
          localStorage.removeItem('admin_token');
          setLoading(false);
          return;
        }
        setUserInfo({
          userId: response.data.userId,
          username: response.data.username,
          role,
        });
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
      localStorage.removeItem('admin_token');
    } finally {
      setLoading(false);
    }
  };

  const login = async (username, password) => {
    const response = await authApi.login(username, password);
    if (response.status === 'success' && response.data) {
      localStorage.setItem('admin_token', response.data.token);
      // 登录后立即获取真实角色
      let role = 'user';
      try {
        const infoRes = await authApi.getUserInfo();
        role = infoRes?.data?.role || 'user';
      } catch (_) {}
      if (role !== 'admin' && role !== 'super_admin') {
        localStorage.removeItem('admin_token');
        throw new Error('权限不足，请使用管理员账号登录');
      }
      setUserInfo({
        userId: response.data.userId,
        username: username,
        role,
      });
      return response.data;
    }
    throw new Error('登录失败');
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch (error) {
      console.error('注销失败:', error);
    }
    localStorage.removeItem('admin_token');
    setUserInfo(null);
  };

  return (
    <AuthContext.Provider value={{ userInfo, loading, login, logout, checkAuth }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}