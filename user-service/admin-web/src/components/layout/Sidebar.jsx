import styled from 'styled-components';
import { NavLink } from 'react-router-dom';

const SidebarContainer = styled.aside`
  width: 240px;
  height: 100vh;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  color: white;
  position: fixed;
  left: 0;
  top: 0;
  overflow-y: auto;
`;

const Logo = styled.div`
  padding: 20px 24px;
  font-size: 20px;
  font-weight: 700;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
`;

const NavSection = styled.div`
  padding: 16px 0;
`;

const NavTitle = styled.div`
  padding: 8px 24px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #64748b;
`;

const NavItem = styled(NavLink)`
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  color: #94a3b8;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.05);
    color: white;
  }

  &.active {
    background: linear-gradient(90deg, rgba(102, 126, 234, 0.2) 0%, transparent 100%);
    color: white;
    border-left: 3px solid #667eea;
    padding-left: 21px;
  }

  svg {
    width: 20px;
    height: 20px;
  }
`;

function Sidebar({ userRole }) {
  const menuItems = [
    {
      title: '概览',
      items: [
        { path: '/dashboard', label: '仪表盘', icon: '📊' },
      ],
    },
    {
      title: '管理',
      items: [
        { path: '/users', label: '用户管理', icon: '👥' },
        { path: '/nfts', label: 'NFT 管理', icon: '🎨' },
        { path: '/orders', label: '交易管理', icon: '💰' },
        { path: '/fan-tokens', label: '粉丝代币', icon: '🪙' },
      ],
    },
    {
      title: '系统',
      items: [
        { path: '/settings/admins', label: '管理员', icon: '⚙️', superAdminOnly: true },
      ],
    },
  ];

  return (
    <SidebarContainer>
      <Logo>NFT 管理后台</Logo>

      {menuItems.map((section, index) => (
        <NavSection key={index}>
          <NavTitle>{section.title}</NavTitle>
          {section.items
            .filter((item) => !item.superAdminOnly || userRole === 'super_admin')
            .map((item, itemIndex) => (
              <NavItem key={itemIndex} to={item.path}>
                <span>{item.icon}</span>
                {item.label}
              </NavItem>
            ))}
        </NavSection>
      ))}
    </SidebarContainer>
  );
}

export default Sidebar;