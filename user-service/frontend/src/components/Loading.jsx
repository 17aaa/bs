import styled from 'styled-components';

const LoadingOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
`;

const LoadingSpinner = styled.div`
  width: 60px;
  height: 60px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }
`;

const LoadingText = styled.p`
  color: white;
  margin-top: 16px;
  font-size: 16px;
`;

const LoadingContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
`;

function Loading({ fullScreen = true }) {
  if (fullScreen) {
    return (
      <LoadingOverlay>
        <LoadingContainer>
          <LoadingSpinner />
          <LoadingText>加载中...</LoadingText>
        </LoadingContainer>
      </LoadingOverlay>
    );
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
      <LoadingSpinner style={{ width: 24, height: 24, borderWidth: 2 }} />
      <span style={{ fontSize: 14, color: '#666' }}>加载中...</span>
    </div>
  );
}

export default Loading;