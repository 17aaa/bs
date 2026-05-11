import styled from 'styled-components';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';

const LayoutContainer = styled.div`
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
`;

const MainContent = styled.main`
  flex: 1;
  margin-left: 240px;
  margin-top: 64px;
  padding: 24px;
  min-height: calc(100vh - 64px);
`;

function AdminLayout({ title, userInfo, onLogout }) {
  return (
    <LayoutContainer>
      <Sidebar userRole={userInfo?.role} />
      <Header title={title} userInfo={userInfo} onLogout={onLogout} />
      <MainContent>
        <Outlet />
      </MainContent>
    </LayoutContainer>
  );
}

export default AdminLayout;