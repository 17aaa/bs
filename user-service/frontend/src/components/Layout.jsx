import styled from 'styled-components';

const Container = styled.div`
  min-height: 100vh;
  background: #f5f7fa;
`;

const Content = styled.div`
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 24px;

  @media (max-width: 768px) {
    padding: 24px 16px;
  }
`;

// 默认导出一个 Layout 组件
function Layout({ children }) {
  return (
    <Container>
      <Content>{children}</Content>
    </Container>
  );
}

export { Container, Content };
export default Layout;