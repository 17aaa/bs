import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import Header from '../components/Header';
import { Container, Content } from '../components/Layout';
import NFTCard from '../components/NFTCard';
import Loading from '../components/Loading';
import EmptyState from '../components/EmptyState';
import { nftApi, marketApi, userApi } from '../services/api';
import { useDebounce, usePagination } from '../hooks';
import { notify } from '../components/Notification';

// 出售弹窗样式
const ModalOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
`;

const Modal = styled.div`
  background: white;
  border-radius: 16px;
  padding: 32px;
  max-width: 420px;
  width: 90%;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.2);
`;

const ModalTitle = styled.h2`
  margin: 0 0 24px 0;
  font-size: 20px;
  color: #333;
  text-align: center;
`;

const ModalBody = styled.div`
  margin-bottom: 24px;
`;

const InputGroup = styled.div`
  margin-bottom: 16px;
`;

const InputLabel = styled.label`
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #555;
  margin-bottom: 8px;
`;

const ModalInput = styled.input`
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s ease;

  &:focus {
    outline: none;
    border-color: #667eea;
  }
`;

const ModalActions = styled.div`
  display: flex;
  gap: 12px;
`;

const ModalButton = styled.button`
  flex: 1;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-1px);
  }
`;

const PrimaryButton = styled(ModalButton)`
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  &:hover {
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
  }
`;

const SecondaryButton = styled(ModalButton)`
  background: #f5f5f5;
  color: #333;

  &:hover {
    background: #e8e8e8;
  }
`;

const Title = styled.h1`
  font-size: 32px;
  margin: 0 0 32px 0;
  color: #333;
`;

const FilterBar = styled.div`
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  align-items: center;
`;

const FilterButton = styled.button`
  padding: 8px 16px;
  border: 1px solid ${(props) => (props.$active ? '#667eea' : '#e0e0e0')};
  border-radius: 20px;
  background: ${(props) => (props.$active ? '#667eea' : 'white')};
  color: ${(props) => (props.$active ? 'white' : '#666')};
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #667eea;
  }
`;

const SearchInput = styled.input`
  flex: 1;
  min-width: 200px;
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 14px;

  &:focus {
    outline: none;
    border-color: #667eea;
  }
`;

const SortSelect = styled.select`
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  background: white;

  &:focus {
    outline: none;
    border-color: #667eea;
  }
`;

const NFTGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
`;

const LoadMoreContainer = styled.div`
  text-align: center;
  margin-top: 32px;
`;

const LoadMoreButton = styled.button`
  padding: 12px 32px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: ${(props) => (props.disabled ? 'not-allowed' : 'pointer')};
  opacity: ${(props) => (props.disabled ? 0.6 : 1)};
  transition: all 0.2s ease;

  &:hover:not(:disabled) {
    opacity: 0.9;
    transform: translateY(-1px);
  }
`;

const categories = {
  all: '全部',
  art: '艺术',
  music: '音乐',
  photography: '摄影',
  design: '设计',
  writing: '写作',
  video: '视频',
};

const categoryMap = {
  art: 'Art',
  music: 'Music',
  photography: 'Photography',
  design: 'Design',
  writing: 'Writing',
  video: 'Video',
};

function MarketPage() {
  const navigate = useNavigate();
  const [account, setAccount] = useState(null);
  const [nfts, setNfts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('newest');
  const [buyingId, setBuyingId] = useState(null);
  const [showSellModal, setShowSellModal] = useState(false);
  const [showBuyModal, setShowBuyModal] = useState(false);
  const [selectedNft, setSelectedNft] = useState(null);
  const [sellPrice, setSellPrice] = useState('');
  const [buyPrice, setBuyPrice] = useState('');
  const [listing, setListing] = useState(false);
  const [buying, setBuying] = useState(false);
  const [txHash, setTxHash] = useState('');
  const [txStatus, setTxStatus] = useState(''); // 'pending' | 'success' | 'error'
  const [buyerBalance, setBuyerBalance] = useState(null); // BigInt string from API
  const debouncedSearchTerm = useDebounce(searchTerm, 300);
  const pagination = usePagination(12, 0);

  // 重置分页当筛选或排序改变时
  useEffect(() => {
    pagination.reset();
    setNfts([]);
  }, [filter, sortBy]);

  // 加载 NFTs
  useEffect(() => {
    loadNFTs(false);
  }, [filter, sortBy, debouncedSearchTerm]);

  const loadNFTs = async (append = false) => {
    setLoading(true);
    try {
      // 从订单API获取已上架的NFT (status=1表示待售)
      const response = await marketApi.getOrderList({ status: 1, page: 1, size: 100 });
      if (response.status === 'success') {
        const orders = response.data || [];

        // 从订单中提取NFT信息，并关联NFT详情
        let data = orders.map(order => ({
          id: order.nftAssetId,
          tokenId: order.tokenId,
          name: order.nftName || `NFT #${order.tokenId}`,
          description: order.nftDescription || '',
          imageUrl: order.nftImageUrl,
          category: order.category || 'Art',
          ownerAddress: order.sellerAddress,
          creatorAddress: order.creatorAddress || order.sellerAddress,
          royaltyFee: order.royaltyFee,
          currentVersion: order.currentVersion,
          price: order.price,
          orderId: order.id,
          orderType: order.orderType,
          status: order.status,
        }));

        // 分类筛选
        if (filter !== 'all' && categoryMap[filter]) {
          data = data.filter((nft) => nft.category === categoryMap[filter]);
        }

        // 搜索过滤
        if (debouncedSearchTerm) {
          const term = debouncedSearchTerm.toLowerCase();
          data = data.filter(
            (nft) =>
              nft.name.toLowerCase().includes(term) ||
              nft.description?.toLowerCase().includes(term)
          );
        }

        // 排序
        if (sortBy === 'newest') {
          data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        } else if (sortBy === 'oldest') {
          data.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
        }

        if (append) {
          setNfts((prev) => [...prev, ...data]);
          pagination.setOffset((prev) => prev + pagination.limit);
        } else {
          setNfts(data);
          pagination.setOffset(pagination.limit);
        }
        pagination.updateHasMore(data.length);
      }
    } catch (error) {
      console.error('加载 NFT 失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleConnect = async (acc) => {
    setAccount(acc);
  };

  const handleViewDetails = (id) => {
    navigate(`/nft/${id}`);
  };

  const handleBuy = async () => {
    if (!account || !selectedNft) {
      notify.warning('请先连接钱包');
      return;
    }

    setBuying(true);
    setTxStatus('pending');
    setTxHash('');
    try {
      // 调用购买 API
      const response = await marketApi.buyNft(selectedNft.orderId.toString(), account);
      if (response.status === 'success') {
        setTxHash(response.data);
        setTxStatus('success');
        notify.success('购买成功！交易哈希：' + response.data);
        window.dispatchEvent(new CustomEvent('balance-updated'));
        loadNFTs(); // 刷新列表
        // 延迟关闭弹窗
        setTimeout(() => {
          setShowBuyModal(false);
          setSelectedNft(null);
          setBuyPrice('');
        }, 3000);
      } else {
        setTxStatus('error');
        notify.error(response.message || '购买失败');
      }
    } catch (error) {
      console.error('购买失败:', error);
      setTxStatus('error');
      notify.error(error.message || '购买失败');
    } finally {
      setBuying(false);
    }
  };

  // 打开购买弹窗
  const handleOpenBuyModal = async (nft) => {
    if (!account) {
      notify.warning('请先连接钱包');
      return;
    }
    setSelectedNft(nft);
    setBuyPrice(nft.price || '1000000000000000');
    setTxStatus('');
    setTxHash('');
    setBuyerBalance(null);
    setShowBuyModal(true);
    // 获取买家余额
    try {
      const res = await userApi.getUserInfo();
      if (res.status === 'success' && res.data?.platformBalance) {
        setBuyerBalance(res.data.platformBalance);
      }
    } catch (e) {
      // 忽略余额查询失败
    }
  };

  // 关闭购买弹窗
  const handleCloseBuyModal = () => {
    if (txStatus === 'pending') {
      notify.warning('交易处理中，请稍候');
      return;
    }
    setShowBuyModal(false);
    setSelectedNft(null);
    setBuyPrice('');
    setTxStatus('');
    setTxHash('');
  };

  // 打开出售弹窗
  const handleOpenSellModal = (nft) => {
    if (!account) {
      notify.warning('请先连接钱包');
      return;
    }
    setSelectedNft(nft);
    setSellPrice('1000000000000000'); // 默认 0.001 ETH
    setShowSellModal(true);
  };

  // 关闭出售弹窗
  const handleCloseSellModal = () => {
    setShowSellModal(false);
    setSelectedNft(null);
    setSellPrice('');
  };

  // 处理出售
  const handleSell = async () => {
    if (!selectedNft || !sellPrice) {
      notify.warning('请填写完整信息');
      return;
    }

    setListing(true);
    try {
      // 创建固定价格订单
      const response = await marketApi.createFixedPriceSale({
        sellerAddress: account,
        nftContract: selectedNft.contractAddress || '0x5FbDB2315678afecb367f032d93F642f64180aa3',
        tokenId: selectedNft.tokenId,
        price: sellPrice,
        nftAssetId: selectedNft.id, // 传递NFT资产ID
      });

      if (response.status === 'success') {
        notify.success('挂单成功！订单 ID: ' + response.data);
        handleCloseSellModal();
      } else {
        notify.error(response.message || '挂单失败');
      }
    } catch (error) {
      console.error('挂单失败:', error);
      notify.error(error.message || '挂单失败');
    } finally {
      setListing(false);
    }
  };

  const handleLoadMore = () => {
    if (!loading && pagination.hasMore) {
      loadNFTs(true);
    }
  };

  return (
    <Container>
      <Header
        walletAddress={account}
        onWalletConnect={handleConnect}
        currentPage="market"
        showLogout
      />

      <Content>
        {/* 市场说明 */}
        <div style={{
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          borderRadius: '12px',
          padding: '20px 24px',
          marginBottom: '24px',
          color: 'white'
        }}>
          <h3 style={{ margin: '0 0 8px 0', fontSize: '18px' }}>🎓 学生创意成果交易市场</h3>
          <p style={{ margin: 0, fontSize: '14px', opacity: 0.9, lineHeight: '1.6' }}>
            在这里可以购买和出售学生创作的NFT作品。<br/>
            <strong>注意：</strong>只有已上架销售的NFT才会出现在市场中。<br/>
            如果你是创作者，可以在"我的NFT"页面选择作品进行上架销售。
          </p>
        </div>

        <Title>探索 NFT</Title>

        <FilterBar>
          {Object.entries(categories).map(([key, label]) => (
            <FilterButton
              key={key}
              $active={filter === key}
              onClick={() => setFilter(key)}
            >
              {label}
            </FilterButton>
          ))}

          <SearchInput
            type="text"
            placeholder="搜索 NFT..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />

          <SortSelect
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
          >
            <option value="newest">最新发布</option>
            <option value="oldest">最早发布</option>
          </SortSelect>
        </FilterBar>

        {loading && nfts.length === 0 ? (
          <Loading fullScreen={false} />
        ) : nfts.length > 0 ? (
          <>
            <NFTGrid>
              {nfts.map((nft) => (
                <NFTCard
                  key={nft.orderId}
                  nft={nft}
                  onViewDetails={handleViewDetails}
                  onBuy={account && account.toLowerCase() !== nft.ownerAddress?.toLowerCase()
                    ? () => handleOpenBuyModal(nft)
                    : undefined}
                  onSell={account && account.toLowerCase() === nft.ownerAddress?.toLowerCase()
                    ? () => handleOpenSellModal(nft)
                    : undefined}
                  buying={buyingId === nft.id}
                />
              ))}
            </NFTGrid>
            {pagination.hasMore && (
              <LoadMoreContainer>
                <LoadMoreButton
                  onClick={handleLoadMore}
                  disabled={loading}
                >
                  {loading ? '加载中...' : '加载更多'}
                </LoadMoreButton>
              </LoadMoreContainer>
            )}
          </>
        ) : (
          <EmptyState
            icon="🔍"
            title="暂无符合条件的 NFT"
            subtitle="试试其他筛选条件或搜索关键词"
          />
        )}
      </Content>

      {/* 出售弹窗 */}
      {showSellModal && (
        <ModalOverlay onClick={handleCloseSellModal}>
          <Modal onClick={(e) => e.stopPropagation()}>
            <ModalTitle>出售 NFT</ModalTitle>
            <ModalBody>
              <div style={{ marginBottom: '16px' }}>
                <div style={{ fontWeight: 600, marginBottom: '8px', color: '#333' }}>
                  {selectedNft?.name}
                </div>
                <div style={{ fontSize: '13px', color: '#666' }}>
                  Token ID: {selectedNft?.tokenId}
                </div>
              </div>
              <InputGroup>
                <InputLabel>出售价格（积分）</InputLabel>
                <ModalInput
                  type="number"
                  value={sellPrice ? (Number(BigInt(sellPrice)) / 1e18) : ''}
                  onChange={(e) => {
                    const v = parseFloat(e.target.value);
                    if (!isNaN(v) && v >= 0) {
                      setSellPrice(BigInt(Math.round(v * 1e18)).toString());
                    } else {
                      setSellPrice('');
                    }
                  }}
                  placeholder="输入积分数量，如 10"
                  min="0"
                  step="0.01"
                />
                <div style={{ fontSize: '12px', color: '#999', marginTop: '4px' }}>
                  输入积分数量（买家将用平台积分支付）
                </div>
              </InputGroup>
            </ModalBody>
            <ModalActions>
              <SecondaryButton onClick={handleCloseSellModal}>取消</SecondaryButton>
              <PrimaryButton onClick={handleSell} disabled={listing || !sellPrice}>
                {listing ? '挂单中...' : '确认挂单'}
              </PrimaryButton>
            </ModalActions>
          </Modal>
        </ModalOverlay>
      )}

      {/* 购买弹窗 */}
      {showBuyModal && (
        <ModalOverlay onClick={handleCloseBuyModal}>
          <Modal onClick={(e) => e.stopPropagation()}>
            <ModalTitle>购买 NFT</ModalTitle>
            <ModalBody>
              <div style={{ marginBottom: '16px' }}>
                <div style={{ fontWeight: 600, marginBottom: '8px', color: '#333' }}>
                  {selectedNft?.name}
                </div>
                <div style={{ fontSize: '13px', color: '#666' }}>
                  Token ID: {selectedNft?.tokenId}
                </div>
                <div style={{ fontSize: '13px', color: '#666' }}>
                  卖家: {selectedNft?.ownerAddress?.slice(0, 10)}...{selectedNft?.ownerAddress?.slice(-4)}
                </div>
              </div>
              <InputGroup>
                <InputLabel>购买价格（平台积分）</InputLabel>
                <div style={{
                  padding: '12px 16px',
                  background: '#f8f9fa',
                  border: '2px solid #e0e0e0',
                  borderRadius: '8px',
                  fontSize: '18px',
                  fontWeight: '700',
                  color: '#667eea',
                }}>
                  {buyPrice ? (Number(BigInt(buyPrice)) / 1e18).toFixed(2) : '0'} 积分
                </div>
                {buyerBalance !== null && (
                  <div style={{
                    marginTop: '10px',
                    padding: '10px 14px',
                    borderRadius: '8px',
                    background: BigInt(buyerBalance) >= BigInt(buyPrice || '0') ? '#e8f5e9' : '#ffebee',
                    fontSize: '13px',
                    color: BigInt(buyerBalance) >= BigInt(buyPrice || '0') ? '#2e7d32' : '#c62828',
                    fontWeight: 500,
                  }}>
                    我的余额：{(Number(BigInt(buyerBalance)) / 1e18).toFixed(2)} 积分
                    {BigInt(buyerBalance) < BigInt(buyPrice || '0') && (
                      <span style={{ marginLeft: '8px' }}>⚠️ 余额不足</span>
                    )}
                  </div>
                )}
              </InputGroup>

              {/* 交易状态显示 */}
              {txStatus && (
                <div style={{
                  padding: '16px',
                  borderRadius: '8px',
                  marginTop: '16px',
                  background: txStatus === 'success' ? '#e8f5e9' : txStatus === 'error' ? '#ffebee' : '#fff3e0',
                }}>
                  {txStatus === 'pending' && (
                    <div style={{ color: '#ef6c00', textAlign: 'center' }}>
                      <div style={{ fontSize: '24px', marginBottom: '8px' }}>⏳</div>
                      <div>交易处理中...</div>
                    </div>
                  )}
                  {txStatus === 'success' && (() => {
                    const price = BigInt(buyPrice || '0');
                    const royaltyBps = selectedNft?.royaltyFee || 0;
                    const isSecondary = selectedNft?.creatorAddress && selectedNft?.sellerAddress
                      && selectedNft.creatorAddress.toLowerCase() !== selectedNft.sellerAddress?.toLowerCase();
                    const royalty = isSecondary ? price * BigInt(royaltyBps) / BigInt(10000) : BigInt(0);
                    const sellerReceives = price - royalty;
                    return (
                      <div style={{ color: '#2e7d32' }}>
                        <div style={{ textAlign: 'center', marginBottom: 12 }}>
                          <div style={{ fontSize: '24px', marginBottom: '8px' }}>✅</div>
                          <div style={{ fontWeight: 600 }}>购买成功！</div>
                        </div>
                        <div style={{ background: '#f0fdf4', borderRadius: 8, padding: '12px 14px', fontSize: 13 }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                            <span style={{ color: '#64748b' }}>成交价</span>
                            <span style={{ fontWeight: 600 }}>{(Number(price) / 1e18).toFixed(2)} 积分</span>
                          </div>
                          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                            <span style={{ color: '#64748b' }}>卖家到账</span>
                            <span style={{ fontWeight: 600 }}>{(Number(sellerReceives) / 1e18).toFixed(2)} 积分</span>
                          </div>
                          {royalty > BigInt(0) && (
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                              <span style={{ color: '#64748b' }}>创作者版税 ({(royaltyBps / 100).toFixed(2)}%)</span>
                              <span style={{ color: '#9333ea', fontWeight: 600 }}>{(Number(royalty) / 1e18).toFixed(2)} 积分</span>
                            </div>
                          )}
                        </div>
                        {txHash && (
                          <div style={{ fontSize: '11px', marginTop: 8, color: '#94a3b8', wordBreak: 'break-all', textAlign: 'center' }}>
                            哈希: {txHash.slice(0, 20)}...
                          </div>
                        )}
                      </div>
                    );
                  })()}
                  {txStatus === 'error' && (
                    <div style={{ color: '#c62828', textAlign: 'center' }}>
                      <div style={{ fontSize: '24px', marginBottom: '8px' }}>❌</div>
                      <div>购买失败</div>
                    </div>
                  )}
                </div>
              )}
            </ModalBody>
            <ModalActions>
              <SecondaryButton onClick={handleCloseBuyModal} disabled={txStatus === 'pending'}>
                {txStatus === 'success' ? '继续浏览' : '取消'}
              </SecondaryButton>
              {txStatus === 'success' && (
                <PrimaryButton onClick={() => { handleCloseBuyModal(); navigate('/profile?tab=owned'); }}>
                  查看我的 NFT
                </PrimaryButton>
              )}
              {txStatus !== 'success' && (
                <PrimaryButton
                  onClick={handleBuy}
                  disabled={buying || !buyPrice || (buyerBalance !== null && BigInt(buyerBalance) < BigInt(buyPrice || '0'))}
                >
                  {buying ? '处理中...' : '确认购买'}
                </PrimaryButton>
              )}
            </ModalActions>
          </Modal>
        </ModalOverlay>
      )}
    </Container>
  );
}

export default MarketPage;