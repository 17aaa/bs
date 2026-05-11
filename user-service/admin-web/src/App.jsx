import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './hooks/index.js';
import { AdminLayout } from './components/layout';
import LoginPage from './pages/Login';
import DashboardPage from './pages/Dashboard';
import UsersPage from './pages/Users';
import NftsPage from './pages/Nfts';
import OrdersPage from './pages/Orders';
import FanTokensPage from './pages/FanTokens';
import AdminsPage from './pages/Settings/Admins';

// 受保护的路由
function ProtectedRoute({ children }) {
  const { userInfo, loading } = useAuth();

  if (loading) {
    return <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh' }}>加载中...</div>;
  }

  if (!userInfo) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

// 超级管理员专用路由
function SuperAdminRoute({ children }) {
  const { userInfo } = useAuth();

  if (userInfo?.role !== 'super_admin') {
    return <Navigate to="/403" replace />;
  }

  return children;
}

// 403 页面
function ForbiddenPage() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', background: '#f8fafc' }}>
      <h1 style={{ fontSize: '48px', color: '#ef4444', margin: '0 0 16px 0' }}>403</h1>
      <p style={{ color: '#64748b', margin: '0 0 24px 0' }}>无权访问此页面</p>
      <a href="/dashboard" style={{ color: '#667eea', textDecoration: 'none' }}>返回仪表盘</a>
    </div>
  );
}

// 主应用
function AppContent() {
  const { userInfo, logout } = useAuth();

  return (
    <Routes>
      {/* 公开路由 */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/403" element={<ForbiddenPage />} />

      {/* 受保护的管理路由 */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AdminLayout title="仪表盘" userInfo={userInfo} onLogout={logout} />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="nfts" element={<NftsPage />} />
        <Route path="orders" element={<OrdersPage />} />
        <Route path="fan-tokens" element={<FanTokensPage />} />
        <Route
          path="settings/admins"
          element={
            <SuperAdminRoute>
              <AdminsPage />
            </SuperAdminRoute>
          }
        />
      </Route>

      {/* 默认重定向 */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;