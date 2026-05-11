import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';

const HeaderContainer = styled.header`
  height: 64px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: fixed;
  top: 0;
  left: 240px;
  right: 0;
  z-index: 100;
`;

const PageTitle = styled.h1`
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
`;

const UserInfo = styled.div`
  display: flex;
  align-items: center;
  gap: 16px;
`;

const RoleBadge = styled.span`
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;

  ${(props) => {
    switch (props.role) {
      case 'super_admin':
        return `
          background: #fef3c7;
          color: #92400e;
        `;
      case 'admin':
        return `
          background: #dbeafe;
          color: #1e40af;
        `;
      default:
        return `
          background: #f1f5f9;
          color: #475569;
        `;
    }
  }}
`;

const Username = styled.span`
  font-size: 14px;
  color: #475569;
`;

const LogoutButton = styled.button`
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #475569;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #ef4444;
    color: #ef4444;
  }
`;

function Header({ title, userInfo, onLogout }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    onLogout?.();
    navigate('/login');
  };

  const getRoleLabel = (role) => {
    switch (role) {
      case 'super_admin':
        return '超级管理员';
      case 'admin':
        return '管理员';
      default:
        return '用户';
    }
  };

  return (
    <HeaderContainer>
      <PageTitle>{title}</PageTitle>

      <UserInfo>
        {userInfo && (
          <>
            <RoleBadge role={userInfo.role}>{getRoleLabel(userInfo.role)}</RoleBadge>
            <Username>{userInfo.username}</Username>
          </>
        )}
        <LogoutButton onClick={handleLogout}>退出登录</LogoutButton>
      </UserInfo>
    </HeaderContainer>
  );
}

export default Header;