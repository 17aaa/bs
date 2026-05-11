import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import Header from '../components/Header';
import NFTCard from '../components/NFTCard';
import Loading from '../components/Loading';
import EmptyState from '../components/EmptyState';
import { nftApi } from '../services/api';
import web3 from '../services/web3';

const Container = styled.div`
  min-height: 100vh;
  background: #f5f7fa;
`;

const Hero = styled.div`
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 80px 24px;
  text-align: center;
`;

const HeroTitle = styled.h1`
  font-size: 48px;
  margin: 0 0 16px 0;
`;

const HeroSubtitle = styled.p`
  font-size: 18px;
  margin: 0;
  opacity: 0.9;
`;

const MainContent = styled.main`
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 24px;
`;

const SectionTitle = styled.h2`
  font-size: 28px;
  margin: 0 0 24px 0;
  color: #333;
`;

const NFTGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }
`;

const RefreshButton = styled.button`
  position: fixed;
  top: 80px;
  right: 24px;
  padding: 10px 16px;
  border: none;
  border-radius: 20px;
  background: white;
  color: #667eea;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
  z-index: 10;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }

  &:active {
    transform: scale(0.95);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
`;

const AnimatedContainer = styled.div`
  @keyframes fadeInUp {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  & > * {
    animation: fadeInUp 0.4s ease-out forwards;
  }
`;

const CreateButton = styled.button`
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 32px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.1);
  }
`;

const FeaturesContainer = styled.div`
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 32px;
`;

const FeatureItem = styled.div`
  text-align: center;
`;

const FeatureIcon = styled.div`
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 4px;
`;

const FeatureLabel = styled.div`
  font-size: 14px;
  opacity: 0.9;
`;

function HomePage({ isWalletConnected, walletAddress, onWalletConnect }) {
  const navigate = useNavigate();
  const [nfts, setNfts] = useState([]);
  const [account, setAccount] = useState(walletAddress);
  const [userId, setUserId] = useState(localStorage.getItem('userId'));
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const loadNFTs = async (isRefresh = false) => {
    if (isRefresh) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }
    try {
      const response = await nftApi.getAllNfts(12);
      if (response.status === 'success') {
        setNfts(response.data || []);
      }
    } catch (error) {
      console.error('加载 NFT 失败:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleRefresh = () => {
    loadNFTs(true);
  };

  useEffect(() => {
    loadNFTs();
  }, []);

  useEffect(() => {
    if (walletAddress && !account) {
      setAccount(walletAddress);
      onWalletConnect?.(walletAddress);
    }
  }, [walletAddress]);

  const handleConnect = (acc) => {
    setAccount(acc);
    onWalletConnect?.(acc);
  };

  const handleLogout = () => {
    web3.clearSavedWallet();
    setAccount(null);
    setUserId(null);
    navigate('/login');
  };

  const handleViewDetails = (id) => {
    navigate(`/nft/${id}`);
  };

  return (
    <Container>
      <Header
        walletAddress={account}
        onWalletConnect={handleConnect}
        currentPage="home"
        showLogout
        onLogout={handleLogout}
      />

      <Hero>
        <HeroTitle>学生创意成果 NFT 交易平台</HeroTitle>
        <HeroSubtitle>将你的创意作品铸造为 NFT，实现链上确权与交易</HeroSubtitle>
        <FeaturesContainer>
          <FeatureItem>
            <FeatureIcon>🎨</FeatureIcon>
            <FeatureLabel>创意作品</FeatureLabel>
          </FeatureItem>
          <FeatureItem>
            <FeatureIcon>🔗</FeatureIcon>
            <FeatureLabel>区块链确权</FeatureLabel>
          </FeatureItem>
          <FeatureItem>
            <FeatureIcon>💰</FeatureIcon>
            <FeatureLabel>便捷交易</FeatureLabel>
          </FeatureItem>
        </FeaturesContainer>
      </Hero>

      <MainContent>
        <SectionTitle>最新 NFT</SectionTitle>

        {refreshing && (
          <RefreshButton onClick={handleRefresh} disabled={refreshing}>
            <span style={{ animation: refreshing ? 'spin 1s linear infinite' : 'none', display: 'inline-block' }}>🔄</span>
            刷新中...
          </RefreshButton>
        )}

        {!refreshing && (
          <RefreshButton onClick={handleRefresh}>
            🔄 刷新
          </RefreshButton>
        )}

        {loading ? (
          <Loading fullScreen={false} />
        ) : nfts.length > 0 ? (
          <AnimatedContainer>
            <NFTGrid>
              {nfts.map((nft, index) => (
                <NFTCard
                  key={nft.id}
                  nft={nft}
                  onViewDetails={handleViewDetails}
                  style={{ animationDelay: `${index * 0.05}s` }}
                />
              ))}
            </NFTGrid>
          </AnimatedContainer>
        ) : (
          <EmptyState
            icon="🎨"
            title="暂无 NFT 作品"
            subtitle="成为第一个创作者吧！"
            actionText="去创作"
            onAction={() => navigate('/create')}
          />
        )}
      </MainContent>

      <CreateButton onClick={() => navigate('/create')}>+</CreateButton>
    </Container>
  );
}

export default HomePage;