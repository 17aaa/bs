import { Component } from 'react';
import styled from 'styled-components';

const Container = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: #f5f7fa;
`;

const ErrorIcon = styled.div`
  font-size: 64px;
  margin-bottom: 24px;
`;

const Title = styled.h1`
  font-size: 24px;
  color: #1e293b;
  margin: 0 0 12px 0;
`;

const Message = styled.p`
  font-size: 14px;
  color: #64748b;
  margin: 0 0 24px 0;
  text-align: center;
  max-width: 400px;
`;

const RetryButton = styled.button`
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
  }
`;

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: null });
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <Container>
          <ErrorIcon>⚠️</ErrorIcon>
          <Title>出错了</Title>
          <Message>
            页面遇到了一些问题，请尝试刷新页面。如果问题持续存在，请联系客服。
          </Message>
          <RetryButton onClick={this.handleRetry}>
            刷新页面
          </RetryButton>
        </Container>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;