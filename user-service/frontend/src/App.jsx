import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect, Suspense, lazy } from 'react';
import { NotificationManager } from './components/Notification';
import web3 from './services/web3';

// 路由懒加载
const HomePage = lazy(() => import('./pages/HomePage'));
const NFTDetailPage = lazy(() => import('./pages/NFTDetailPage'));
const CreatePage = lazy(() => import('./pages/CreatePage'));
const MarketPage = lazy(() => import('./pages/MarketPage'));
const ProfilePage = lazy(() => import('./pages/ProfilePage'));
const LoginPage = lazy(() => import('./pages/LoginPage'));
const FanTokenPage = lazy(() => import('./pages/FanTokenPage'));

// 加载中组件
const LoadingSpinner = () => (
  <div style={{
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: '#f5f7fa'
  }}>
    <div style={{
      width: '40px',
      height: '40px',
      border: '3px solid #e0e0e0',
      borderTopColor: '#667eea',
      borderRadius: '50%',
      animation: 'spin 1s linear infinite'
    }}>
      <style>{`
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  </div>
);

// 受保护的路由组件
function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token');

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

function App() {
  const [isWalletConnected, setIsWalletConnected] = useState(false);
  const [walletAddress, setWalletAddress] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 检查登录状态和钱包连接
    const checkWalletStatus = async () => {
      const token = localStorage.getItem('token');
      const walletConnected = localStorage.getItem('walletConnected') === 'true';
      let walletAddress = localStorage.getItem('walletAddress');

      // 如果有token但没有walletAddress，尝试从后端获取
      if (token && !walletAddress) {
        try {
          const response = await fetch('http://localhost:8085/user/info', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          const data = await response.json();
          if (data.status === 'success' && data.data?.walletAddress) {
            walletAddress = data.data.walletAddress;
            localStorage.setItem('walletAddress', walletAddress);
          }
        } catch (error) {
          console.error('获取用户钱包地址失败:', error);
        }
      }

      if (token && walletConnected && walletAddress) {
        setIsWalletConnected(true);
        setWalletAddress(walletAddress);

        // 自动连接钱包
        try {
          await web3.autoConnect(walletAddress);
        } catch (error) {
          console.error('自动连接失败:', error);
        }
      }
      setIsLoading(false);
    };

    checkWalletStatus();
  }, []);

  const handleWalletConnect = (address) => {
    setIsWalletConnected(true);
    setWalletAddress(address);
  };

  const handleLogout = () => {
    web3.clearSavedWallet();
    setIsWalletConnected(false);
    setWalletAddress(null);
  };

  if (isLoading) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }}>
        <div style={{
          width: '60px',
          height: '60px',
          border: '4px solid rgba(255, 255, 255, 0.3)',
          borderTopColor: 'white',
          borderRadius: '50%',
          animation: 'spin 1s linear infinite'
        }}>
          <style>{`
            @keyframes spin {
              to { transform: rotate(360deg); }
            }
          `}</style>
        </div>
      </div>
    );
  }

  return (
    <BrowserRouter>
      <NotificationManager />
      <Suspense fallback={<LoadingSpinner />}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <HomePage
                  isWalletConnected={isWalletConnected}
                  walletAddress={walletAddress}
                  onWalletConnect={handleWalletConnect}
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/nft/:id"
            element={
              <ProtectedRoute>
                <NFTDetailPage
                  isWalletConnected={isWalletConnected}
                  walletAddress={walletAddress}
                  onWalletConnect={handleWalletConnect}
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/create"
            element={
              <ProtectedRoute>
                <CreatePage
                  isWalletConnected={isWalletConnected}
                  walletAddress={walletAddress}
                  onWalletConnect={handleWalletConnect}
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/market"
            element={
              <ProtectedRoute>
                <MarketPage
                  isWalletConnected={isWalletConnected}
                  walletAddress={walletAddress}
                  onWalletConnect={handleWalletConnect}
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <ProfilePage
                  isWalletConnected={isWalletConnected}
                  walletAddress={walletAddress}
                  onWalletConnect={handleWalletConnect}
                  onLogout={handleLogout}
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/fantoken"
            element={
              <ProtectedRoute>
                <FanTokenPage
                  isWalletConnected={isWalletConnected}
                  walletAddress={walletAddress}
                  onWalletConnect={handleWalletConnect}
                />
              </ProtectedRoute>
            }
          />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}

export default App;