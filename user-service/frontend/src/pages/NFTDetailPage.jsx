import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import Header from '../components/Header';
import { nftApi, marketApi, walletApi } from '../services/api';
import { notify } from '../components/Notification';
import Loading from '../components/Loading';
import EmptyState from '../components/EmptyState';
import { ethers } from 'ethers';

const Container = styled.div`
  min-height: 100vh;
  background: #f5f7fa;
`;

const BackButton = styled.button`
  background: none;
  border: none;
  font-size: 16px;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;

  &:hover {
    color: #2196F3;
  }
`;

const TabContainer = styled.div`
  display: flex;
  gap: 8px;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 16px;
`;

const Tab = styled.button`
  padding: 12px 24px;
  background: none;
  border: none;
  border-bottom: 2px solid ${(props) => (props.$active ? '#667eea' : 'transparent')};
  color: ${(props) => (props.$active ? '#667eea' : '#666')};
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    color: #667eea;
  }
`;

const OwnerInfo = styled.div`
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  margin-top: 16px;
`;

const OwnerRow = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
`;

const AddressLink = styled.a`
  color: #667eea;
  text-decoration: none;
  font-family: monospace;
  font-size: 14px;

  &:hover {
    text-decoration: underline;
  }
`;

const StatusBadge = styled.span`
  padding: 4px 12px;
  border-radius: 4px;
  background: ${(props) => {
    switch (props.$status) {
      case 1: return '#e8f5e9';
      case 2: return '#fff3e0';
      default: return '#ffebee';
    }
  }};
  color: ${(props) => {
    switch (props.$status) {
      case 1: return '#2e7d32';
      case 2: return '#ef6c00';
      default: return '#c62828';
    }
  }};
  font-size: 12px;
  font-weight: 600;
`;

const Content = styled.div`
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
`;

const ImageSection = styled.div`
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 96px;
  overflow: hidden;
  position: relative;
`;

const NFTImage = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
`;

const InfoSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
`;

const Title = styled.h1`
  font-size: 32px;
  margin: 0;
  color: #333;
`;

const Description = styled.p`
  font-size: 16px;
  color: #666;
  line-height: 1.6;
  margin: 0;
`;

const MetaGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
`;

const MetaItem = styled.div`
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
`;

const MetaLabel = styled.span`
  font-size: 12px;
  color: #999;
  display: block;
  margin-bottom: 8px;
`;

const MetaValue = styled.span`
  font-size: 16px;
  color: #333;
  font-weight: 600;
`;

const Section = styled.div`
  margin-top: 24px;
`;

const SectionTitle = styled.h3`
  font-size: 18px;
  margin: 0 0 16px 0;
  color: #333;
`;

const VersionList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

const VersionItem = styled.div`
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
`;

const VersionHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
`;

const VersionBadge = styled.span`
  padding: 4px 12px;
  border-radius: 4px;
  background: #e3f2fd;
  color: #1976d2;
  font-size: 12px;
  font-weight: 600;
`;

const VersionTime = styled.span`
  font-size: 12px;
  color: #999;
`;

const ActionButton = styled.button`
  padding: 16px 32px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

const SecondaryButton = styled.button`
  padding: 16px 32px;
  border: 2px solid #667eea;
  border-radius: 12px;
  background: white;
  color: #667eea;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #f0f4ff;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

const ModalOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
`;

const ModalContent = styled.div`
  background: white;
  border-radius: 16px;
  padding: 32px;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
`;

const ModalTitle = styled.h2`
  margin: 0 0 24px 0;
  font-size: 24px;
  color: #333;
`;

const FormGroup = styled.div`
  margin-bottom: 20px;
`;

const Label = styled.label`
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
`;

const Input = styled.input`
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;

  &:focus {
    outline: none;
    border-color: #667eea;
  }
`;

const Select = styled.select`
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;

  &:focus {
    outline: none;
    border-color: #667eea;
  }
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: 12px;
  margin-top: 24px;
`;

const SaleTypeButton = styled.button`
  flex: 1;
  padding: 16px;
  border: 2px solid ${(props) => (props.$active ? '#667eea' : '#e0e0e0')};
  border-radius: 12px;
  background: ${(props) => (props.$active ? '#f0f4ff' : 'white')};
  color: ${(props) => (props.$active ? '#667eea' : '#666')};
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #667eea;
  }
`;

const PriceDisplay = styled.div`
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 16px;
`;

const PriceLabel = styled.div`
  font-size: 14px;
  color: #666;
`;

const PriceValue = styled.div`
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
  margin-top: 4px;
`;

function NFTDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [nft, setNft] = useState(null);
  const [versions, setVersions] = useState([]);
  const [account, setAccount] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('details'); // 'details' | 'history' | 'owners'
  const [owners, setOwners] = useState([]);
  const [ownersLoading, setOwnersLoading] = useState(false);
  const [activeOrder, setActiveOrder] = useState(null);
  const [buying, setBuying] = useState(false);

  // 报价功能状态
  const [showOfferModal, setShowOfferModal] = useState(false);
  const [offerPrice, setOfferPrice] = useState('');
  const [offerLoading, setOfferLoading] = useState(false);
  const [offers, setOffers] = useState([]);
  const [offersLoading, setOffersLoading] = useState(false);

  // 销售功能状态
  const [showSaleModal, setShowSaleModal] = useState(false);
  const [saleType, setSaleType] = useState('fixed'); // 'fixed' | 'dutch'
  const [price, setPrice] = useState('');
  const [startPrice, setStartPrice] = useState('');
  const [endPrice, setEndPrice] = useState('');
  const [duration, setDuration] = useState('24'); // 小时
  const [listingLoading, setListingLoading] = useState(false);

  useEffect(() => {
    loadNFTDetails();
  }, [id]);

  useEffect(() => {
    if (activeTab === 'owners' && nft) {
      loadTradeHistory();
    }
  }, [activeTab, nft]);

  const loadNFTDetails = async () => {
    try {
      const nftResponse = await nftApi.getNftAsset(id);
      if (nftResponse.status === 'success') {
        setNft(nftResponse.data);
      } else {
        notify.error('加载 NFT 信息失败');
      }

      const versionResponse = await nftApi.getVersionHistory(id);
      if (versionResponse.status === 'success') {
        setVersions(versionResponse.data || []);
      }

      if (nftResponse.status === 'success') {
        try {
          const orderRes = await marketApi.getOrderList({
            tokenId: nftResponse.data.tokenId,
            status: 1,
            size: 1,
          });
          if (orderRes.status === 'success' && orderRes.data?.length > 0) {
            setActiveOrder(orderRes.data[0]);
          }
        } catch (_) {
          // 无在售订单，静默忽略
        }
      }
    } catch (error) {
      console.error('加载 NFT 详情失败:', error);
      notify.error('加载 NFT 详情失败：' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const loadTradeHistory = async () => {
    setOwnersLoading(true);
    try {
      const res = await marketApi.getOrderList({ tokenId: nft.tokenId, status: 2, size: 50 });
      if (res.status === 'success') {
        setOwners(res.data || []);
      }
    } catch (e) {
      console.error('加载交易历史失败:', e);
    } finally {
      setOwnersLoading(false);
    }
  };

  const handleBuy = async () => {
    if (!account) {
      notify.warning('请先连接钱包');
      return;
    }
    if (!activeOrder) {
      notify.error('该 NFT 暂无在售订单');
      return;
    }
    setBuying(true);
    try {
      const response = await marketApi.buyNft(activeOrder.id.toString(), account);
      if (response.status === 'success') {
        notify.success('购买成功！交易哈希：' + response.data);
        setActiveOrder(null);
        loadNFTDetails();
      } else {
        notify.error(response.message || '购买失败');
      }
    } catch (error) {
      notify.error(error.message || '购买失败');
    } finally {
      setBuying(false);
    }
  };

  const loadOffers = async () => {
    if (!nft) return;
    setOffersLoading(true);
    try {
      const res = await marketApi.getOrderList({ tokenId: nft.tokenId, orderType: 3, status: 0, size: 50 });
      if (res.status === 'success') {
        setOffers(res.data || []);
      }
    } catch (e) {
      // 静默失败
    } finally {
      setOffersLoading(false);
    }
  };

  const handleSubmitOffer = async () => {
    if (!offerPrice || isNaN(Number(offerPrice)) || Number(offerPrice) <= 0) {
      notify.error('请输入有效的报价');
      return;
    }
    if (!account) {
      notify.error('请先连接钱包');
      return;
    }
    setOfferLoading(true);
    try {
      const priceWei = BigInt(Math.floor(parseFloat(offerPrice) * 1e18)).toString();
      const res = await marketApi.createOffer({
        buyerAddress: account,
        nftContract: nft.contractAddress,
        tokenId: nft.tokenId,
        price: priceWei,
        nftAssetId: nft.id,
      });
      if (res.status === 'success') {
        notify.success('报价提交成功！');
        setShowOfferModal(false);
        setOfferPrice('');
        loadOffers();
      } else {
        notify.error(res.message || '报价失败');
      }
    } catch (e) {
      notify.error(e.message || '报价提交失败');
    } finally {
      setOfferLoading(false);
    }
  };

  const handleAcceptOffer = async (offerId) => {
    if (!account) {
      notify.error('请先连接钱包');
      return;
    }
    try {
      const res = await marketApi.acceptOffer(offerId, account);
      if (res.status === 'success') {
        notify.success('已接受报价，交易完成！');
        loadOffers();
        loadNFTDetails();
      } else {
        notify.error(res.message || '接受报价失败');
      }
    } catch (e) {
      notify.error(e.message || '接受报价失败');
    }
  };

  const formatDate = (dateStr) => {
    return new Date(dateStr).toLocaleString('zh-CN');
  };

  const copyToClipboard = (text, message) => {
    navigator.clipboard.writeText(text).then(() => {
      notify.success(message || '已复制到剪贴板');
    }).catch(() => {
      notify.error('复制失败');
    });
  };

  const formatAddress = (address) => {
    if (!address) return 'N/A';
    return `${address.slice(0, 6)}...${address.slice(-4)}`;
  };

  // 检查当前用户是否为NFT所有者
  const isOwner = account && nft && account.toLowerCase() === nft.ownerAddress?.toLowerCase();

  // 计算荷兰拍卖的当前价格
  const calculateDutchPrice = () => {
    if (saleType !== 'dutch' || !startPrice || !endPrice || !duration) return null;
    const start = parseFloat(startPrice);
    const end = parseFloat(endPrice);
    const dur = parseFloat(duration);
    if (isNaN(start) || isNaN(end) || isNaN(dur) || dur <= 0) return null;
    // 简化计算：假设拍卖开始了一半时间
    const elapsed = dur / 2;
    const priceDrop = ((start - end) * elapsed) / dur;
    return (start - priceDrop).toFixed(4);
  };

  // 处理上架销售
  const handleListForSale = async () => {
    if (!price && !startPrice) {
      notify.error('请输入价格');
      return;
    }

    const priceInWei = ethers.parseEther(price || calculateDutchPrice()).toString();

    try {
      setListingLoading(true);

      let response;
      if (saleType === 'fixed') {
        response = await marketApi.createFixedPriceSale({
          sellerAddress: account,
          nftContract: nft.contractAddress || '0x0000000000000000000000000000000000000000',
          tokenId: nft.tokenId,
          price: priceInWei,
          nftAssetId: nft.id
        });
      } else {
        const startPriceInWei = ethers.parseEther(startPrice).toString();
        const endPriceInWei = ethers.parseEther(endPrice).toString();
        response = await marketApi.createDutchAuction({
          sellerAddress: account,
          nftContract: nft.contractAddress || '0x0000000000000000000000000000000000000000',
          tokenId: nft.tokenId,
          startPrice: startPriceInWei,
          reservePrice: endPriceInWei,
          startTime: Math.floor(Date.now() / 1000),
          duration: parseInt(duration) * 3600,
          nftAssetId: nft.id
        });
      }

      if (response.status === 'success') {
        notify.success('NFT 上架成功！');
        setShowSaleModal(false);
        loadNFTDetails(); // 刷新NFT信息
      } else {
        notify.error(response.message || '上架失败');
      }
    } catch (error) {
      console.error('上架失败:', error);
      notify.error('上架失败: ' + (error.message || '未知错误'));
    } finally {
      setListingLoading(false);
    }
  };

  if (loading) {
    return (
      <Container>
        <Header
          walletAddress={account}
          onWalletConnect={setAccount}
          currentPage="market"
          showLogout
        />
        <Loading fullScreen={false} />
      </Container>
    );
  }

  if (!nft) {
    return (
      <Container>
        <Header
          walletAddress={account}
          onWalletConnect={setAccount}
          currentPage="market"
          showLogout
        />
        <EmptyState
          icon="😕"
          title="NFT 不存在"
          subtitle="该 NFT 可能已被删除或下架"
          actionText="返回首页"
          onAction={() => navigate('/')}
        />
      </Container>
    );
  }

  const hasImage = nft.imageUrl || (nft.currentMetadataHash && nft.currentMetadataHash.match(/\.(jpg|jpeg|png|gif|webp)$/i));

  return (
    <Container>
      <Header
        walletAddress={account}
        onWalletConnect={setAccount}
        currentPage="market"
        showLogout
      />

      <Content>
        <div style={{ gridColumn: '1 / -1', marginBottom: '8px' }}>
          <BackButton onClick={() => navigate(-1)}>← 返回</BackButton>
        </div>
        <ImageSection>
          {hasImage && nft.imageUrl ? (
            <NFTImage src={nft.imageUrl} alt={nft.name} onError={(e) => e.target.style.display = 'none'} />
          ) : '🎨'}
        </ImageSection>

        <InfoSection>
          <Title>{nft.name}</Title>
          <Description>{nft.description}</Description>

          <MetaGrid>
            <MetaItem>
              <MetaLabel>分类</MetaLabel>
              <MetaValue>{nft.category || '艺术'}</MetaValue>
            </MetaItem>
            <MetaItem>
              <MetaLabel>创作者</MetaLabel>
              <MetaValue>{nft.creatorAddress?.slice(0, 10)}...</MetaValue>
            </MetaItem>
            <MetaItem>
              <MetaLabel>Token ID</MetaLabel>
              <MetaValue>{nft.tokenId}</MetaValue>
            </MetaItem>
            <MetaItem>
              <MetaLabel>版税</MetaLabel>
              <MetaValue>{(nft.royaltyFee / 100).toFixed(2)}%</MetaValue>
            </MetaItem>
            <MetaItem>
              <MetaLabel>当前版本</MetaLabel>
              <MetaValue>v{nft.currentVersion}</MetaValue>
            </MetaItem>
            <MetaItem>
              <MetaLabel>状态</MetaLabel>
              <MetaValue>
                {nft.status === 1 ? '正常' : nft.status === 2 ? '下架' : '冻结'}
              </MetaValue>
            </MetaItem>
          </MetaGrid>

          <TabContainer>
            <Tab $active={activeTab === 'details'} onClick={() => setActiveTab('details')}>详情</Tab>
            <Tab $active={activeTab === 'history'} onClick={() => setActiveTab('history')}>版本历史 ({versions.length})</Tab>
            <Tab $active={activeTab === 'owners'} onClick={() => setActiveTab('owners')}>持有记录</Tab>
          </TabContainer>

          {activeTab === 'details' && (
            <>
              <OwnerInfo>
                <SectionTitle style={{ margin: '0 0 12px 0', fontSize: '16px' }}>当前所有者</SectionTitle>
                <OwnerRow>
                  <span style={{ color: '#666', fontSize: '14px' }}>地址:</span>
                  <AddressLink href="#" onClick={(e) => {
                    e.preventDefault();
                    copyToClipboard(nft.ownerAddress, '所有者地址已复制');
                  }}>{formatAddress(nft.ownerAddress)}</AddressLink>
                </OwnerRow>
                <OwnerRow>
                  <span style={{ color: '#666', fontSize: '14px' }}>合约地址:</span>
                  <AddressLink href="#" onClick={(e) => {
                    e.preventDefault();
                    copyToClipboard(nft.contractAddress, '合约地址已复制');
                  }}>{formatAddress(nft.contractAddress)}</AddressLink>
                </OwnerRow>
                <OwnerRow>
                  <span style={{ color: '#666', fontSize: '14px' }}>创建时间:</span>
                  <span style={{ fontSize: '14px' }}>{formatDate(nft.createdAt)}</span>
                </OwnerRow>
              </OwnerInfo>

              <ButtonGroup>
                {isOwner ? (
                  <>
                    <SecondaryButton onClick={() => setShowSaleModal(true)}>
                      上架销售
                    </SecondaryButton>
                    <ActionButton onClick={() => navigate('/market')}>
                      在市场查看
                    </ActionButton>
                  </>
                ) : (
                  <>
                    <ActionButton onClick={handleBuy} disabled={buying || !activeOrder}>
                      {buying ? '购买中...' : activeOrder ? '购买此 NFT' : '暂无在售'}
                    </ActionButton>
                    <SecondaryButton onClick={() => { setShowOfferModal(true); loadOffers(); }}>
                      提交报价
                    </SecondaryButton>
                  </>
                )}
              </ButtonGroup>

              {/* 所有者查看收到的报价 */}
              {isOwner && (
                <div style={{ marginTop: 24 }}>
                  <div style={{ fontSize: 15, fontWeight: 600, color: '#1e293b', marginBottom: 12 }}
                       onClick={loadOffers}
                  >
                    收到的报价 {offers.length > 0 ? `(${offers.length})` : ''}
                    <span style={{ fontSize: 12, color: '#667eea', marginLeft: 8, cursor: 'pointer' }}
                          onClick={loadOffers}>刷新</span>
                  </div>
                  {offersLoading ? (
                    <div style={{ color: '#888', fontSize: 14 }}>加载中...</div>
                  ) : offers.length === 0 ? (
                    <div style={{ color: '#94a3b8', fontSize: 14 }}>暂无报价</div>
                  ) : (
                    offers.map((offer) => (
                      <div key={offer.orderId || offer.id} style={{
                        background: '#f8fafc', borderRadius: 10, padding: '12px 16px',
                        marginBottom: 8, border: '1px solid #e2e8f0',
                        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                      }}>
                        <div>
                          <div style={{ fontWeight: 600, color: '#1e293b' }}>
                            {offer.price ? (Number(offer.price) / 1e18).toFixed(4) : '-'} 积分
                          </div>
                          <div style={{ fontSize: 12, color: '#64748b', marginTop: 2 }}>
                            买家：{offer.buyerAddress?.slice(0, 10)}...
                          </div>
                        </div>
                        <SecondaryButton
                          style={{ padding: '6px 14px', fontSize: 13 }}
                          onClick={() => handleAcceptOffer(offer.orderId || offer.id)}
                        >
                          接受
                        </SecondaryButton>
                      </div>
                    ))
                  )}
                </div>
              )}
            </>
          )}

          {activeTab === 'history' && (
            <VersionList>
              {versions.length > 0 ? (
                versions.map((version) => (
                  <VersionItem key={version.id}>
                    <VersionHeader>
                      <VersionBadge>版本 {version.version}</VersionBadge>
                      <VersionTime>{formatDate(version.createdAt)}</VersionTime>
                    </VersionHeader>
                    <div style={{ fontSize: '14px', color: '#666' }}>
                      {version.changeDescription || '元数据更新'}
                    </div>
                    <div style={{ fontSize: '12px', color: '#999', marginTop: '8px' }}>
                      哈希：{version.metadataHash?.slice(0, 20)}...
                    </div>
                  </VersionItem>
                ))
              ) : (
                <EmptyState
                  icon="📝"
                  title="暂无版本历史"
                  subtitle="该 NFT 尚未有版本更新记录"
                />
              )}
            </VersionList>
          )}

          {activeTab === 'owners' && (
            <VersionList>
              {ownersLoading ? (
                <div style={{ padding: '40px', textAlign: 'center', color: '#888' }}>加载中...</div>
              ) : owners.length === 0 ? (
                <EmptyState icon="📊" title="暂无交易记录" subtitle="该 NFT 尚未发生成交" />
              ) : (
                owners.map((order) => (
                  <div key={order.orderId} style={{
                    background: 'white', borderRadius: 12, padding: '16px 20px',
                    marginBottom: 12, border: '1px solid #e0e0e0',
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                      <span style={{ fontWeight: 600, color: '#1e293b' }}>
                        {order.orderType === 2 ? '荷兰拍卖' : '固定价格'} 成交
                      </span>
                      <span style={{ color: '#667eea', fontWeight: 600 }}>
                        {order.finalPrice
                          ? (Number(order.finalPrice) / 1e18).toFixed(4) + ' 积分'
                          : '-'}
                      </span>
                    </div>
                    <div style={{ fontSize: 13, color: '#64748b', display: 'flex', flexDirection: 'column', gap: 4 }}>
                      <span>卖家：<code style={{ fontSize: 12 }}>{order.sellerAddress}</code></span>
                      <span>买家：<code style={{ fontSize: 12 }}>{order.buyerAddress || '-'}</code></span>
                      {order.txHash && (
                        <span>交易哈希：<code style={{ fontSize: 12 }}>{order.txHash.slice(0, 20)}...</code></span>
                      )}
                      <span>时间：{order.updatedAt ? formatDate(order.updatedAt) : '-'}</span>
                    </div>
                  </div>
                ))
              )}
            </VersionList>
          )}
        </InfoSection>
      </Content>

      {/* 报价弹窗 */}
      {showOfferModal && (
        <ModalOverlay onClick={() => setShowOfferModal(false)}>
          <ModalContent onClick={(e) => e.stopPropagation()}>
            <ModalTitle>提交报价</ModalTitle>

            <FormGroup>
              <Label>报价金额 (积分)</Label>
              <Input
                type="number"
                step="0.001"
                min="0"
                placeholder="输入您的报价"
                value={offerPrice}
                onChange={(e) => setOfferPrice(e.target.value)}
                autoFocus
              />
            </FormGroup>

            <FormGroup>
              <Label>说明</Label>
              <div style={{ fontSize: '14px', color: '#666', lineHeight: '1.6' }}>
                报价提交后，NFT 所有者可选择接受或忽略您的报价。接受后交易立即完成。
              </div>
            </FormGroup>

            <ButtonGroup>
              <SecondaryButton onClick={() => setShowOfferModal(false)} disabled={offerLoading}>
                取消
              </SecondaryButton>
              <ActionButton onClick={handleSubmitOffer} disabled={offerLoading}>
                {offerLoading ? '提交中...' : '确认报价'}
              </ActionButton>
            </ButtonGroup>
          </ModalContent>
        </ModalOverlay>
      )}

      {/* 销售弹窗 */}
      {showSaleModal && (
        <ModalOverlay onClick={() => setShowSaleModal(false)}>
          <ModalContent onClick={(e) => e.stopPropagation()}>
            <ModalTitle>上架销售</ModalTitle>

            <FormGroup>
              <Label>销售方式</Label>
              <div style={{ display: 'flex', gap: '12px' }}>
                <SaleTypeButton
                  $active={saleType === 'fixed'}
                  onClick={() => setSaleType('fixed')}
                >
                  固定价格
                </SaleTypeButton>
                <SaleTypeButton
                  $active={saleType === 'dutch'}
                  onClick={() => setSaleType('dutch')}
                >
                  荷兰拍卖
                </SaleTypeButton>
              </div>
            </FormGroup>

            {saleType === 'fixed' ? (
              <FormGroup>
                <Label>价格 (积分)</Label>
                <Input
                  type="number"
                  placeholder="请输入价格"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                />
              </FormGroup>
            ) : (
              <>
                <FormGroup>
                  <Label>起始价格 (积分)</Label>
                  <Input
                    type="number"
                    placeholder="拍卖开始的最高价格"
                    value={startPrice}
                    onChange={(e) => setStartPrice(e.target.value)}
                  />
                </FormGroup>
                <FormGroup>
                  <Label>结束价格 (积分)</Label>
                  <Input
                    type="number"
                    placeholder="拍卖结束的最低价格"
                    value={endPrice}
                    onChange={(e) => setEndPrice(e.target.value)}
                  />
                </FormGroup>
                <FormGroup>
                  <Label>拍卖时长 (小时)</Label>
                  <Select value={duration} onChange={(e) => setDuration(e.target.value)}>
                    <option value="1">1小时</option>
                    <option value="6">6小时</option>
                    <option value="12">12小时</option>
                    <option value="24">24小时</option>
                    <option value="48">48小时</option>
                    <option value="72">72小时</option>
                  </Select>
                </FormGroup>
                {calculateDutchPrice() && (
                  <PriceDisplay>
                    <PriceLabel>当前价格（估算）</PriceLabel>
                    <PriceValue>{calculateDutchPrice()} 积分</PriceValue>
                  </PriceDisplay>
                )}
              </>
            )}

            <FormGroup>
              <Label>说明</Label>
              <div style={{ fontSize: '14px', color: '#666', lineHeight: '1.6' }}>
                {saleType === 'fixed' ? (
                  <>买家将直接以固定价格购买您的NFT</>
                ) : (
                  <>荷兰拍卖是一种价格递减的拍卖方式。价格将随时间从起始价格线性下降至结束价格。最先接受的买家将获得NFT。</>
                )}
              </div>
            </FormGroup>

            <ButtonGroup>
              <SecondaryButton onClick={() => setShowSaleModal(false)} disabled={listingLoading}>
                取消
              </SecondaryButton>
              <ActionButton onClick={handleListForSale} disabled={listingLoading}>
                {listingLoading ? '处理中...' : '确认上架'}
              </ActionButton>
            </ButtonGroup>
          </ModalContent>
        </ModalOverlay>
      )}
    </Container>
  );
}

export default NFTDetailPage;