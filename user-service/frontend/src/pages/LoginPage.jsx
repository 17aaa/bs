import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import axios from 'axios';
import { notify } from '../components/Notification';
import Header from '../components/Header';
import { walletApi } from '../services/api';

// 使用画眉AI生成的背景图
import generatedBg from '../assets/huamei_1774260545518.jpg';
const BACKGROUND_IMAGE = generatedBg;

const Container = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: url(${BACKGROUND_IMAGE}) no-repeat center center;
  background-size: cover;
  padding-top: 80px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(
      135deg,
      rgba(15, 23, 42, 0.85) 0%,
      rgba(30, 41, 59, 0.75) 50%,
      rgba(15, 23, 42, 0.85) 100%
    );
    z-index: 1;
  }
`;

const LoginBox = styled.div`
  position: relative;
  z-index: 2;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(24px) saturate(200%);
  -webkit-backdrop-filter: blur(24px) saturate(200%);
  padding: 48px;
  border-radius: 24px;
  box-shadow:
    0 25px 50px -12px rgba(0, 0, 0, 0.5),
    0 0 0 1px rgba(255, 255, 255, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  width: 100%;
  max-width: 420px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-8px);
    box-shadow:
      0 35px 60px -15px rgba(0, 0, 0, 0.6),
      0 0 0 1px rgba(255, 255, 255, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.15);
  }

  @media (max-width: 480px) {
    padding: 32px 24px;
    margin: 16px;
  }
`;

const Logo = styled.div`
  text-align: center;
  margin-bottom: 32px;

  .logo-icon {
    font-size: 48px;
    margin-bottom: 12px;
    display: block;
    animation: float 3s ease-in-out infinite;
  }

  @keyframes float {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-8px); }
  }
`;

const Title = styled.h1`
  margin: 0 0 8px 0;
  font-size: 28px;
  text-align: center;
  color: white;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
`;

const Subtitle = styled.p`
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  margin: 0 0 32px 0;
`;

const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 20px;
`;

const InputGroup = styled.div`
  position: relative;
`;

const Input = styled.input`
  width: 100%;
  padding: 16px 20px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 14px;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.08);
  color: white;
  transition: all 0.3s ease;
  box-sizing: border-box;

  &:focus {
    outline: none;
    border-color: #8b5cf6;
    background: rgba(255, 255, 255, 0.12);
    box-shadow:
      0 0 0 4px rgba(139, 92, 246, 0.2),
      0 4px 12px rgba(0, 0, 0, 0.2);
  }

  &::placeholder {
    color: rgba(255, 255, 255, 0.4);
  }
`;

const Button = styled.button`
  padding: 18px 24px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 50%, #3b82f6 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  box-shadow:
    0 4px 16px rgba(139, 92, 246, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  margin-top: 8px;
  letter-spacing: 1px;
  text-transform: uppercase;

  &:hover {
    opacity: 0.95;
    transform: translateY(-2px);
    box-shadow:
      0 8px 24px rgba(139, 92, 246, 0.5),
      0 0 0 1px rgba(255, 255, 255, 0.2);
  }

  &:active {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    transform: none;
  }
`;

const Divider = styled.div`
  display: flex;
  align-items: center;
  margin: 24px 0;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  }

  span {
    padding: 0 16px;
    color: rgba(255, 255, 255, 0.4);
    font-size: 12px;
  }
`;

const WalletButton = styled.button`
  width: 100%;
  padding: 16px 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.05);
  color: white;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.3);
    transform: translateY(-2px);
  }

  .wallet-icon {
    font-size: 20px;
  }
`;

const SwitchLink = styled.p`
  text-align: center;
  margin: 24px 0 0 0;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;

  span {
    color: #a78bfa;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.2s ease;

    &:hover {
      color: #c4b5fd;
      text-decoration: underline;
    }
  }
`;

const PasswordHint = styled.p`
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  margin: 8px 0 0 0;
  padding-left: 4px;
`;

function LoginPage() {
  const navigate = useNavigate();
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
  });
  const [loading, setLoading] = useState(false);
  const [walletLoading, setWalletLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      if (isLogin) {
        // 登录
        const response = await axios.post('http://localhost:8085/user/login', null, {
          params: {
            username: formData.username,
            password: formData.password,
          },
        });

        // 后端返回格式：{status: "success",  {token, userId, ...}}
        if (response.data && response.data.status === 'success' && response.data.data) {
          const loginData = response.data.data;
          // 保存 token 和钱包信息
          localStorage.setItem('token', loginData.token);
          if (loginData.refreshToken) {
            localStorage.setItem('refreshToken', loginData.refreshToken);
          }
          localStorage.setItem('userId', loginData.userId);
          localStorage.setItem('walletConnected', loginData.walletConnected);
          if (loginData.walletAddress) {
            localStorage.setItem('walletAddress', loginData.walletAddress);
          }

          notify.success('登录成功！');
          setTimeout(() => {
            navigate('/');
          }, 500);
        } else {
          notify.error('登录失败，请检查用户名和密码');
        }
      } else {
        // 注册
        if (formData.password !== formData.confirmPassword) {
          notify.error('两次输入的密码不一致');
          setLoading(false);
          return;
        }

        const response = await axios.post('http://localhost:8085/user/register', {
          username: formData.username,
          password: formData.password,
        });

        // 后端返回格式：{status: "success",  {userId, token, ...}}
        if (response.data && response.data.status === 'success' && response.data.data) {
          const registerData = response.data.data;
          // 注册成功，自动登录
          if (registerData.token) {
            localStorage.setItem('token', registerData.token);
          }
          if (registerData.refreshToken) {
            localStorage.setItem('refreshToken', registerData.refreshToken);
          }
          localStorage.setItem('userId', registerData.userId);
          localStorage.setItem('walletConnected', registerData.walletConnected || false);
          if (registerData.walletAddress) {
            localStorage.setItem('walletAddress', registerData.walletAddress);
          }

          notify.success('注册成功，自动登录中...');
          setTimeout(() => {
            navigate('/');
          }, 500);
        } else {
          notify.error('注册失败');
        }
      }
    } catch (error) {
      console.error('请求失败:', error);
      notify.error(error.response?.data?.message || '请求失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const handleMetaMaskLogin = async () => {
    if (!window.ethereum) {
      notify.error('未检测到 MetaMask，请先安装 MetaMask 扩展');
      return;
    }
    setWalletLoading(true);
    try {
      // 1. 请求用户连接钱包
      const accounts = await window.ethereum.request({ method: 'eth_requestAccounts' });
      const address = accounts[0];

      // 2. 获取 challenge 消息
      const challengeRes = await walletApi.getChallenge(address);
      if (challengeRes.status !== 'success') {
        notify.error('获取签名消息失败');
        return;
      }
      const message = challengeRes.data.message;

      // 3. 调用 MetaMask 签名
      const signature = await window.ethereum.request({
        method: 'personal_sign',
        params: [message, address],
      });

      // 4. 调用后端钱包登录接口
      const loginRes = await axios.post('http://localhost:8085/api/wallet/wallet-login', null, {
        params: { address, signature },
      });
      const loginData = loginRes.data;
      if (loginData.status === 'success' && loginData.data) {
        localStorage.setItem('token', loginData.data.token);
        if (loginData.data.refreshToken) {
          localStorage.setItem('refreshToken', loginData.data.refreshToken);
        }
        localStorage.setItem('userId', loginData.data.userId);
        localStorage.setItem('walletAddress', address);
        localStorage.setItem('walletConnected', 'true');
        notify.success('钱包登录成功！');
        setTimeout(() => navigate('/'), 500);
      } else {
        notify.error(loginData.message || '钱包登录失败');
      }
    } catch (error) {
      if (error.code === 4001) {
        notify.error('用户拒绝了签名请求');
      } else {
        notify.error(error.response?.data?.message || '钱包登录失败：' + error.message);
      }
    } finally {
      setWalletLoading(false);
    }
  };

  return (
    <Container>
      <Header currentPage="login" showLogout={false} showNav={false} />

      <LoginBox>
        <Logo>
          <span className="logo-icon">🎨</span>
        </Logo>
        <Title>{isLogin ? '欢迎回来' : '创建账号'}</Title>
        <Subtitle>学生创意成果 NFT 交易平台</Subtitle>

        <Form onSubmit={handleSubmit}>
          <InputGroup>
            <Input
              type="text"
              name="username"
              placeholder="用户名"
              value={formData.username}
              onChange={handleChange}
              required
            />
          </InputGroup>
          <InputGroup>
            <Input
              type="password"
              name="password"
              placeholder="密码"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </InputGroup>
          {!isLogin && (
            <>
              <InputGroup>
                <Input
                  type="password"
                  name="confirmPassword"
                  placeholder="确认密码"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                />
              </InputGroup>
              <PasswordHint>密码要求：至少 6 个字符</PasswordHint>
            </>
          )}

          <Button type="submit" disabled={loading}>
            {loading ? '处理中...' : isLogin ? '登录' : '立即注册'}
          </Button>
        </Form>

        <Divider>
          <span>或</span>
        </Divider>

        <WalletButton onClick={handleMetaMaskLogin} disabled={walletLoading}>
          <span className="wallet-icon">🔗</span>
          {walletLoading ? '连接中...' : '使用 MetaMask 登录'}
        </WalletButton>

        <SwitchLink>
          {isLogin ? (
            <>
              还没有账号？<span onClick={() => setIsLogin(false)}>立即注册</span>
            </>
          ) : (
            <>
              已有账号？<span onClick={() => setIsLogin(true)}>立即登录</span>
            </>
          )}
        </SwitchLink>
      </LoginBox>
    </Container>
  );
}

export default LoginPage;