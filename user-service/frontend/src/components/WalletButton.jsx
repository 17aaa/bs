import { useState, useEffect } from 'react';
import styled from 'styled-components';
import web3 from '../services/web3';

const WalletContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
`;

const ConnectButton = styled.button`
  padding: 10px 20px;
  border-radius: 8px;
  border: 2px solid ${(props) => (props.connected ? '#4CAF50' : '#2196F3')};
  background: ${(props) => (props.connected ? '#4CAF50' : '#2196F3')};
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
  }
`;

const AddressSpan = styled.span`
  font-size: 14px;
  color: ${(props) => (props.$mock ? '#ff9800' : '#666')};
`;

const MockBadge = styled.span`
  padding: 2px 6px;
  border-radius: 3px;
  background: #ff9800;
  font-size: 10px;
  color: white;
  margin-right: 4px;
`;

const NetworkBadge = styled.span`
  padding: 4px 8px;
  border-radius: 4px;
  background: #f0f0f0;
  font-size: 12px;
  color: #666;
`;

function WalletButton({ onConnect }) {
  const [account, setAccount] = useState(null);
  const [chainId, setChainId] = useState(null);
  const [isConnecting, setIsConnecting] = useState(false);
  const [isMock, setIsMock] = useState(false);
  const [autoConnected, setAutoConnected] = useState(false);

  const formatAddress = (addr) => {
    if (!addr) return '';
    return `${addr.slice(0, 6)}...${addr.slice(-4)}`;
  };

  // 自动连接已保存的钱包
  useEffect(() => {
    const autoConnectWallet = async () => {
      const walletConnected = localStorage.getItem('walletConnected') === 'true';
      const walletAddress = localStorage.getItem('walletAddress');

      if (walletConnected && walletAddress && !autoConnected) {
        console.log('WalletButton: 自动连接后端创建的钱包:', walletAddress);
        try {
          const result = await web3.autoConnect(walletAddress);
          setAccount(walletAddress);
          setChainId(80002);
          setIsMock(result.isMock || false);
          onConnect?.(walletAddress, 80002);
          setAutoConnected(true);
          console.log('钱包连接成功:', {
            address: walletAddress,
            isMock: result.isMock,
            isServerWallet: result.isServerWallet
          });
        } catch (error) {
          console.error('自动连接失败:', error);
        }
      }
    };

    autoConnectWallet();
  }, [onConnect, autoConnected]);

  const getNetworkName = (id) => {
    const networks = {
      80002: 'Amoy',
      137: 'Polygon',
      1: 'Ethereum',
    };
    return networks[id] || `Chain ${id}`;
  };

  const connectWallet = async () => {
    setIsConnecting(true);
    try {
      const result = await web3.connect();
      setAccount(result.account);
      setChainId(result.chainId);
      setIsMock(!!result.isMock);
      onConnect?.(result.account, result.chainId);
    } catch (error) {
      alert(`连接失败：${error.message}`);
    } finally {
      setIsConnecting(false);
    }
  };

  const switchToAmoy = async () => {
    try {
      await web3.switchChain(80002);
      const { chainId } = await web3.connect();
      setChainId(chainId);
    } catch (error) {
      alert(`切换网络失败：${error.message}`);
    }
  };

  useEffect(() => {
    // 监听账户变化
    if (window.ethereum) {
      window.ethereum.on('accountsChanged', (accounts) => {
        if (accounts.length > 0) {
          setAccount(accounts[0]);
          onConnect?.(accounts[0], chainId);
        } else {
          setAccount(null);
          web3.disconnect();
        }
      });

      window.ethereum.on('chainChanged', () => {
        window.location.reload();
      });
    }

    return () => {
      if (window.ethereum) {
        window.ethereum.removeAllListeners();
      }
    };
  }, [chainId, onConnect]);

  return (
    <WalletContainer>
      {account ? (
        <>
          <NetworkBadge>{getNetworkName(chainId)}</NetworkBadge>
          {isMock && <MockBadge>测试模式</MockBadge>}
          <AddressSpan $mock={isMock}>{formatAddress(account)}</AddressSpan>
          {chainId !== 80002 && !isMock && (
            <button onClick={switchToAmoy} style={{ marginLeft: '8px', fontSize: '12px' }}>
              切换到 Amoy
            </button>
          )}
        </>
      ) : (
        <ConnectButton onClick={connectWallet} disabled={isConnecting}>
          {isConnecting ? '连接中...' : '连接钱包'}
        </ConnectButton>
      )}
    </WalletContainer>
  );
}

export default WalletButton;