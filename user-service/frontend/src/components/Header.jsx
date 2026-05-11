import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import styled from 'styled-components';
import WalletButton from './WalletButton';
import NotificationCenter from './NotificationCenter';
import { userApi } from '../services/api';

const HeaderContainer = styled.header`
  background: white;
  padding: 16px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
`;

const Logo = styled.h1`
  margin: 0;
  font-size: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  cursor: pointer;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.8;
  }
`;

const NavLinks = styled.nav`
  display: flex;
  gap: 24px;
  align-items: center;
`;

const NavLink = styled.span`
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: color 0.2s ease;
  position: relative;

  &:hover {
    color: #2196F3;
  }

  ${(props) =>
    props.$active &&
    `
    color: #667eea;
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      bottom: -4px;
      left: 0;
      right: 0;
      height: 2px;
      background: #667eea;
    }
  `}
`;

const UserInfo = styled.div`
  display: flex;
  align-items: center;
  gap: 16px;
`;

const BalanceBadge = styled.span`
  padding: 4px 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
`;

const LogoutBtn = styled.button`
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: white;
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #667eea;
    color: #667eea;
  }
`;

function Header({
  walletAddress,
  onWalletConnect,
  onLogout,
  currentPage = 'home',
  showLogout = false,
  showNav = true,
}) {
  const navigate = useNavigate();
  const [balance, setBalance] = useState(null);

  const fetchBalance = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) { setBalance(null); return; }
      const res = await userApi.getUserInfo();
      if (res.status === 'success' && res.data?.platformBalance != null) {
        setBalance((Number(BigInt(res.data.platformBalance)) / 1e18).toFixed(2));
      }
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    if (showLogout) fetchBalance();
    const handler = () => fetchBalance();
    window.addEventListener('balance-updated', handler);
    return () => window.removeEventListener('balance-updated', handler);
  }, [showLogout]);

  const handleLogout = () => {
    onLogout?.();
    navigate('/login');
  };

  return (
    <HeaderContainer>
      <Logo onClick={() => navigate('/')}>CreativeNFT</Logo>
      {showNav && (
        <NavLinks>
          <NavLink $active={currentPage ==='home'} onClick={() => navigate('/')}>
            首页
          </NavLink>
          <NavLink $active={currentPage ==='market'} onClick={() => navigate('/market')}>
            市场
          </NavLink>
          <NavLink $active={currentPage ==='create'} onClick={() => navigate('/create')}>
            创作
          </NavLink>
          <NavLink $active={currentPage ==='fantoken'} onClick={() => navigate('/fantoken')}>
            代币
          </NavLink>
          <NavLink $active={currentPage ==='profile'} onClick={() => navigate('/profile')}>
            我的
          </NavLink>
          <UserInfo>
            {showLogout && (
              <>
                {balance !== null && <BalanceBadge>💰 {balance} 积分</BalanceBadge>}
                <NotificationCenter />
                <LogoutBtn onClick={handleLogout}>退出</LogoutBtn>
              </>
            )}
            <WalletButton onConnect={onWalletConnect} />
          </UserInfo>
        </NavLinks>
      )}
    </HeaderContainer>
  );
}

export default Header;