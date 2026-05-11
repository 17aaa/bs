import { useState, useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import styled from 'styled-components';
import Header from '../components/Header';
import { Container, Content } from '../components/Layout';
import NFTCard from '../components/NFTCard';
import WalletButton from '../components/WalletButton';
import EmptyState from '../components/EmptyState';
import { nftApi, userApi, marketApi } from '../services/api';
import { notify } from '../components/Notification';
import web3 from '../services/web3';

const Title = styled.h1`
  font-size: 32px;
  margin: 0 0 32px 0;
  color: #333;
`;

const UserProfileCard = styled.div`
  background: white;
  padding: 32px;
  border-radius: 16px;
  margin-bottom: 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 24px;

  @media (max-width: 768px) {
    flex-direction: column;
    text-align: center;
  }
`;

const Avatar = styled.div`
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: white;
  font-weight: bold;
  flex-shrink: 0;
`;

const UserDetails = styled.div`
  flex: 1;
`;

const UserName = styled.h2`
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #333;
`;

const UserMeta = styled.div`
  display: flex;
  gap: 24px;
  color: #666;
  font-size: 14px;

  @media (max-width: 768px) {
    justify-content: center;
    gap: 16px;
  }
`;

const MetaItem = styled.div`
  display: flex;
  align-items: center;
  gap: 6px;
`;

const Tabs = styled.div`
  display: flex;
  gap: 8px;
  margin-bottom: 32px;
  border-bottom: 2px solid #e0e0e0;
`;

const Tab = styled.button`
  padding: 12px 24px;
  border: none;
  background: none;
  font-size: 16px;
  color: ${(props) => (props.$active ? '#667eea' : '#666')};
  cursor: pointer;
  border-bottom: 2px solid ${(props) => (props.$active ? '#667eea' : 'transparent')};
  margin-bottom: -2px;
  transition: all 0.2s ease;

  &:hover {
    color: #667eea;
  }
`;

const NFTGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
`;

const ConnectPrompt = styled.div`
  text-align: center;
  padding: 80px 24px;
`;

const ConnectPromptText = styled.p`
  font-size: 18px;
  color: #666;
  margin-bottom: 24px;
`;

const StatsGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 32px;
`;

const StatCard = styled.div`
  background: white;
  padding: 24px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
`;

const StatValue = styled.div`
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8px;
`;

const StatLabel = styled.div`
  font-size: 14px;
  color: #999;
`;

const WalletInfo = styled.div`
  background: white;
  padding: 16px 24px;
  border-radius: 8px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const WalletAddress = styled.span`
  font-family: monospace;
  color: #666;
  font-size: 14px;
`;

const CreateWalletBtn = styled.button`
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
  }
`;

const WalletModal = styled.div`
  margin-top: 24px;
  padding: 24px;
  background: white;
  border-radius: 8px;
  max-width: 400px;
  margin: 24px auto;
`;

const PasswordInput = styled.input`
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  margin-bottom: 16px;
  box-sizing: border-box;

  &:focus {
    outline: none;
    border-color: #667eea;
  }
`;

const ModalButtons = styled.div`
  display: flex;
  gap: 12px;
`;

const PrimaryButton = styled.button`
  flex: 1;
  padding: 12px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;

  &:hover {
    opacity: 0.9;
  }
`;

const SecondaryButton = styled.button`
  flex: 1;
  padding: 12px;
  background: #f0f0f0;
  color: #666;
  border: none;
  border-radius: 6px;
  cursor: pointer;

  &:hover {
    background: #e0e0e0;
  }
`;

const ModalContent = styled.div`
  h3 {
    margin: 0 0 16px 0;
  }

  p {
    font-size: 12px;
    color: #999;
    margin: 16px 0 0 0;
  }
`;

const IconWrapper = styled.div`
  font-size: 64px;
  margin-bottom: 16px;
`;

const EditButton = styled.button`
  padding: 8px 16px;
  border: 1px solid #667eea;
  border-radius: 6px;
  background: white;
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #f0f4ff;
  }
`;

const EditModalOverlay = styled.div`
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

const EditModalContent = styled.div`
  background: white;
  border-radius: 16px;
  padding: 32px;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
`;

const EditModalTitle = styled.h2`
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

const AvatarUpload = styled.div`
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
`;

const AvatarPreview = styled.div`
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: ${(props) => props.src ? `url(${props.src})` : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'};
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
  font-weight: bold;
`;

const UploadButton = styled.button`
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: white;
  color: #666;
  font-size: 14px;
  cursor: pointer;

  &:hover {
    border-color: #667eea;
    color: #667eea;
  }
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: 12px;
  margin-top: 24px;
`;

const SaveButton = styled.button`
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;

  &:hover {
    opacity: 0.9;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

const CancelButton = styled.button`
  flex: 1;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  color: #666;
  font-size: 16px;
  cursor: pointer;

  &:hover {
    background: #f5f5f5;
  }
`;

function ProfilePage({ isWalletConnected, walletAddress, onWalletConnect, onLogout }) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [account, setAccount] = useState(walletAddress);
  const [activeTab, setActiveTab] = useState(searchParams.get('tab') || 'created');
  const [nfts, setNfts] = useState([]);
  const [txHistory, setTxHistory] = useState([]);
  const [stats, setStats] = useState({
    created: 0,
    owned: 0,
    volume: 0,
  });
  const [showCreateWallet, setShowCreateWallet] = useState(false);
  const [createWalletPassword, setCreateWalletPassword] = useState('');
  const [userInfo, setUserInfo] = useState(null);
  const userId = localStorage.getItem('userId');

  // 编辑个人信息状态
  const [showEditModal, setShowEditModal] = useState(false);
  const [editForm, setEditForm] = useState({
    username: '',
    school: '',
    major: '',
    bio: '',
  });
  const [avatarFile, setAvatarFile] = useState(null);
  const [avatarPreview, setAvatarPreview] = useState(null);
  const [saving, setSaving] = useState(false);
  const fileInputRef = useRef(null);

  // 出售相关状态
  const [showSellModal, setShowSellModal] = useState(false);
  const [selectedNftForSale, setSelectedNftForSale] = useState(null);
  const [sellPrice, setSellPrice] = useState('');
  const [listing, setListing] = useState(false);

  // 加载用户信息
  useEffect(() => {
    const loadUserInfo = async () => {
      try {
        const response = await userApi.getUserInfo();
        if (response.status === 'success') {
          setUserInfo(response.data);
        }
      } catch (error) {
        console.error('加载用户信息失败:', error);
      }
    };
    loadUserInfo();
  }, []);

  const handleConnect = async (acc) => {
    setAccount(acc);
    onWalletConnect?.(acc);
  };

  // 打开编辑弹窗
  const handleOpenEdit = () => {
    setEditForm({
      username: userInfo?.username || '',
      school: userInfo?.school || '',
      major: userInfo?.major || '',
      bio: userInfo?.bio || '',
    });
    setAvatarPreview(userInfo?.avatar || null);
    setShowEditModal(true);
  };

  // 处理头像选择
  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setAvatarFile(file);
      const reader = new FileReader();
      reader.onload = (e) => {
        setAvatarPreview(e.target.result);
      };
      reader.readAsDataURL(file);
    }
  };

  // 保存个人信息
  const handleSaveProfile = async () => {
    try {
      setSaving(true);

      // 如果有新头像，先上传头像
      let avatarUrl = userInfo?.avatar;
      if (avatarFile) {
        const formData = new FormData();
        formData.append('file', avatarFile);
        const avatarResponse = await userApi.updateAvatar(formData);
        if (avatarResponse.status === 'success') {
          avatarUrl = avatarResponse.data?.url || avatarUrl;
        }
      }

      // 更新用户信息
      const response = await userApi.updateUserInfo({
        ...editForm,
        avatar: avatarUrl,
      });

      if (response.status === 'success') {
        notify.success('个人信息更新成功');
        setUserInfo(response.data);
        setShowEditModal(false);
      } else {
        notify.error(response.message || '更新失败');
      }
    } catch (error) {
      console.error('更新个人信息失败:', error);
      notify.error('更新失败: ' + (error.message || '未知错误'));
    } finally {
      setSaving(false);
    }
  };

  // 打开出售弹窗
  const handleOpenSellModal = (nft) => {
    if (!account) {
      notify.warning('请先连接钱包');
      return;
    }
    setSelectedNftForSale(nft);
    setSellPrice('1000000000000000'); // 默认 0.001 ETH
    setShowSellModal(true);
  };

  // 关闭出售弹窗
  const handleCloseSellModal = () => {
    setShowSellModal(false);
    setSelectedNftForSale(null);
    setSellPrice('');
  };

  // 处理出售
  const handleSell = async () => {
    if (!selectedNftForSale || !sellPrice) {
      notify.warning('请填写完整信息');
      return;
    }

    setListing(true);
    try {
      const response = await marketApi.createFixedPriceSale({
        sellerAddress: account,
        nftContract: selectedNftForSale.contractAddress || '0x5FbDB2315678afecb367f032d93F642f64180aa3',
        tokenId: selectedNftForSale.tokenId,
        price: sellPrice,
        nftAssetId: selectedNftForSale.id,
      });

      if (response.status === 'success') {
        notify.success('挂单成功！订单 ID: ' + response.data);
        handleCloseSellModal();
        loadNFTs(); // 刷新列表
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

  useEffect(() => {
    if (account) {
      if (activeTab === 'history') {
        loadHistory();
      } else {
        loadNFTs();
      }
    }
  }, [account, activeTab]);

  useEffect(() => {
    if (account) {
      loadAllStats();
    }
  }, [account]);

  const loadAllStats = async () => {
    try {
      const [createdRes, ownedRes] = await Promise.all([
        nftApi.getNftsByCreator(account),
        nftApi.getNftsByOwner(account),
      ]);
      setStats(prev => ({
        ...prev,
        created: createdRes.status === 'success' ? (createdRes.data || []).length : prev.created,
        owned: ownedRes.status === 'success' ? (ownedRes.data || []).length : prev.owned,
      }));
    } catch {
      // 静默失败，不影响主流程
    }
  };

  useEffect(() => {
    // 如果已有钱包地址，自动连接
    if (walletAddress && !account) {
      setAccount(walletAddress);
      onWalletConnect?.(walletAddress);
    }
  }, [walletAddress]);

  const loadNFTs = async () => {
    try {
      let response;
      if (activeTab === 'created') {
        response = await nftApi.getNftsByCreator(account);
      } else {
        response = await nftApi.getNftsByOwner(account);
      }

      if (response.status === 'success') {
        const data = response.data || [];
        setNfts(data);
        setStats(prev => ({
          ...prev,
          created: activeTab === 'created' ? data.length : prev.created,
          owned: activeTab === 'owned' ? data.length : prev.owned,
        }));
      }
    } catch (error) {
      console.error('加载 NFT 失败:', error);
    }
  };

  const loadHistory = async () => {
    try {
      const response = await marketApi.getUserHistory(account);
      if (response.status === 'success') {
        setTxHistory(response.data || []);
      }
    } catch (error) {
      console.error('加载交易记录失败:', error);
    }
  };

  const handleViewDetails = (id) => {
    navigate(`/nft/${id}`);
  };

  const handleLogout = () => {
    onLogout?.();
    navigate('/login');
  };

  const handleCreateWallet = async () => {
    if (!createWalletPassword || createWalletPassword.length < 6) {
      notify.warning('密码至少需要 6 位');
      return;
    }

    try {
      const response = await web3.createWallet(createWalletPassword);
      if (response) {
        notify.success('钱包创建成功！请安全保存助记词和私钥。');
        setShowCreateWallet(false);
        setAccount(response.address);
        onWalletConnect?.(response.address);
      }
    } catch (error) {
      console.error('创建钱包失败:', error);
      notify.error('创建钱包失败：' + error.message);
    }
  };

  if (!account) {
    return (
      <Container>
        <Header
          walletAddress={null}
          onWalletConnect={handleConnect}
          currentPage="profile"
          showLogout
          onLogout={handleLogout}
        />

        <Content>
          <ConnectPrompt>
            <IconWrapper>👛</IconWrapper>
            <ConnectPromptText>请连接钱包查看您的个人中心</ConnectPromptText>
            <WalletButton onConnect={handleConnect} />

            {userId && (
              <>
                <p style={{ color: '#999', margin: '24px 0' }}>或者</p>
                <CreateWalletBtn onClick={() => setShowCreateWallet(true)}>
                  创建链上钱包
                </CreateWalletBtn>

                {showCreateWallet && (
                  <WalletModal>
                    <ModalContent>
                      <h3>创建钱包</h3>
                      <PasswordInput
                        type="password"
                        placeholder="设置钱包密码（至少 6 位）"
                        value={createWalletPassword}
                        onChange={(e) => setCreateWalletPassword(e.target.value)}
                      />
                      <ModalButtons>
                        <PrimaryButton onClick={handleCreateWallet}>
                          创建钱包
                        </PrimaryButton>
                        <SecondaryButton onClick={() => setShowCreateWallet(false)}>
                          取消
                        </SecondaryButton>
                      </ModalButtons>
                      <p>⚠️ 请安全保存助记词和私钥，丢失后无法找回</p>
                    </ModalContent>
                  </WalletModal>
                )}
              </>
            )}
          </ConnectPrompt>
        </Content>
      </Container>
    );
  }

  return (
    <Container>
      <Header
        walletAddress={account}
        onWalletConnect={handleConnect}
        currentPage="profile"
        showLogout
        onLogout={handleLogout}
      />

      <Content>
        {/* 用户信息卡片 */}
        {userInfo && (
          <UserProfileCard>
            <Avatar style={userInfo?.avatar ? { backgroundImage: `url(${userInfo.avatar})`, backgroundSize: 'cover' } : {}}>
              {!userInfo?.avatar && (userInfo?.username?.charAt(0).toUpperCase() || 'U')}
            </Avatar>
            <UserDetails>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                <UserName>{userInfo.username || '匿名用户'}</UserName>
                <EditButton onClick={handleOpenEdit}>编辑资料</EditButton>
              </div>
              <UserMeta>
                {userInfo.school && (
                  <MetaItem>
                    <span>🏫</span>
                    <span>{userInfo.school}</span>
                  </MetaItem>
                )}
                {userInfo.major && (
                  <MetaItem>
                    <span>📚</span>
                    <span>{userInfo.major}</span>
                  </MetaItem>
                )}
                <MetaItem>
                  <span>👛</span>
                  <span>{userInfo.walletConnected ? '已连接钱包' : '未连接钱包'}</span>
                </MetaItem>
                <MetaItem>
                  <span>💰</span>
                  <span>平台积分：{userInfo.platformBalance
                    ? (parseFloat(BigInt(userInfo.platformBalance)) / 1e18).toFixed(2)
                    : '0.00'} 积分</span>
                </MetaItem>
              </UserMeta>
              {userInfo.bio && (
                <div style={{ marginTop: '12px', fontSize: '14px', color: '#666' }}>
                  {userInfo.bio}
                </div>
              )}
            </UserDetails>
          </UserProfileCard>
        )}

        <Title>我的 NFT</Title>

        <WalletInfo>
          <div>
            <div style={{ fontSize: '12px', color: '#999', marginBottom: '4px' }}>钱包地址</div>
            <WalletAddress>{account}</WalletAddress>
          </div>
          <WalletButton onConnect={handleConnect} />
        </WalletInfo>

        <StatsGrid>
          <StatCard>
            <StatValue>{stats.created}</StatValue>
            <StatLabel>已创作</StatLabel>
          </StatCard>
          <StatCard>
            <StatValue>{stats.owned}</StatValue>
            <StatLabel>持有中</StatLabel>
          </StatCard>
          <StatCard>
            <StatValue>{stats.volume}</StatValue>
            <StatLabel>交易额 (积分)</StatLabel>
          </StatCard>
        </StatsGrid>

        <Tabs>
          <Tab
            $active={activeTab === 'created'}
            onClick={() => setActiveTab('created')}
          >
            我创作的
          </Tab>
          <Tab
            $active={activeTab === 'owned'}
            onClick={() => setActiveTab('owned')}
          >
            我持有的
          </Tab>
          <Tab
            $active={activeTab === 'history'}
            onClick={() => setActiveTab('history')}
          >
            交易记录
          </Tab>
        </Tabs>

        {activeTab === 'history' ? (
          txHistory.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {txHistory.map((order) => {
                const isSeller = order.sellerAddress === account;
                const statusMap = { 1: '挂单中', 2: '已成交', 3: '已取消' };
                const priceVal = order.finalPrice || order.price || order.startPrice;
                return (
                  <div key={order.id} style={{
                    background: 'white', borderRadius: 12, padding: '16px 20px',
                    boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
                    display: 'flex', alignItems: 'center', gap: 16
                  }}>
                    <div style={{ width: 48, height: 48, borderRadius: 8, overflow: 'hidden', flexShrink: 0, background: '#f0f0f0', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      {order.nftImageUrl ? (
                        <img src={order.nftImageUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      ) : <span style={{ fontSize: 24 }}>🎨</span>}
                    </div>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ fontWeight: 600, fontSize: 15, color: '#1e293b', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {order.nftName || `Order #${order.id}`}
                      </div>
                      <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 2 }}>
                        {isSeller ? '我出售' : '我购买'} · {statusMap[order.status] || '未知'} · {order.updatedAt?.slice(0, 10)}
                      </div>
                    </div>
                    <div style={{ textAlign: 'right', flexShrink: 0 }}>
                      <div style={{ fontWeight: 700, color: isSeller ? '#16a34a' : '#dc2626', fontSize: 15 }}>
                        {isSeller ? '+' : '-'}{priceVal ? (Number(BigInt(priceVal)) / 1e18).toFixed(2) : '?'} 积分
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <EmptyState icon="📋" title="暂无交易记录" />
          )
        ) : nfts.length > 0 ? (
          <NFTGrid>
            {nfts.map((nft) => (
              <NFTCard
                key={nft.id}
                nft={nft}
                onViewDetails={handleViewDetails}
                onSell={handleOpenSellModal}
              />
            ))}
          </NFTGrid>
        ) : (
          <EmptyState
            icon={activeTab === 'created' ? '🎨' : '👛'}
            title={activeTab === 'created' ? '你还没有创作任何 NFT' : '你还没有持有任何 NFT'}
            actionText={activeTab === 'created' ? '去创作' : undefined}
            onAction={activeTab === 'created' ? () => navigate('/create') : undefined}
          />
        )}
      </Content>

      {/* 编辑个人信息弹窗 */}
      {showEditModal && (
        <EditModalOverlay onClick={() => setShowEditModal(false)}>
          <EditModalContent onClick={(e) => e.stopPropagation()}>
            <EditModalTitle>编辑个人信息</EditModalTitle>

            <AvatarUpload>
              <AvatarPreview src={avatarPreview}>
                {!avatarPreview && editForm.username?.charAt(0).toUpperCase()}
              </AvatarPreview>
              <div>
                <UploadButton onClick={() => fileInputRef.current?.click()}>
                  更换头像
                </UploadButton>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  style={{ display: 'none' }}
                  onChange={handleAvatarChange}
                />
              </div>
            </AvatarUpload>

            <FormGroup>
              <Label>昵称</Label>
              <Input
                type="text"
                placeholder="请输入昵称"
                value={editForm.username}
                onChange={(e) => setEditForm({ ...editForm, username: e.target.value })}
              />
            </FormGroup>

            <FormGroup>
              <Label>学校</Label>
              <Input
                type="text"
                placeholder="请输入学校名称"
                value={editForm.school}
                onChange={(e) => setEditForm({ ...editForm, school: e.target.value })}
              />
            </FormGroup>

            <FormGroup>
              <Label>专业</Label>
              <Input
                type="text"
                placeholder="请输入专业"
                value={editForm.major}
                onChange={(e) => setEditForm({ ...editForm, major: e.target.value })}
              />
            </FormGroup>

            <FormGroup>
              <Label>个人简介</Label>
              <Input
                as="textarea"
                rows="3"
                placeholder="请输入个人简介"
                value={editForm.bio}
                onChange={(e) => setEditForm({ ...editForm, bio: e.target.value })}
                style={{ resize: 'vertical', minHeight: '80px' }}
              />
            </FormGroup>

            <ButtonGroup>
              <CancelButton onClick={() => setShowEditModal(false)}>取消</CancelButton>
              <SaveButton onClick={handleSaveProfile} disabled={saving}>
                {saving ? '保存中...' : '保存'}
              </SaveButton>
            </ButtonGroup>
          </EditModalContent>
        </EditModalOverlay>
      )}

      {/* 出售弹窗 */}
      {showSellModal && (
        <EditModalOverlay onClick={handleCloseSellModal}>
          <EditModalContent onClick={(e) => e.stopPropagation()}>
            <EditModalTitle>出售 NFT</EditModalTitle>

            <div style={{ marginBottom: '16px' }}>
              <div style={{ fontWeight: 600, marginBottom: '8px', color: '#333' }}>
                {selectedNftForSale?.name}
              </div>
              <div style={{ fontSize: '13px', color: '#666' }}>
                Token ID: {selectedNftForSale?.tokenId}
              </div>
            </div>

            <FormGroup>
              <Label>出售价格（Wei）</Label>
              <Input
                type="number"
                value={sellPrice}
                onChange={(e) => setSellPrice(e.target.value)}
                placeholder="输入价格，如 1000000000000000 = 0.001 ETH"
              />
              <div style={{ fontSize: '12px', color: '#999', marginTop: '4px' }}>
                1 ETH = 1000000000000000000 Wei | 0.001 ETH = 1000000000000000 Wei
              </div>
            </FormGroup>

            <ButtonGroup>
              <CancelButton onClick={handleCloseSellModal}>取消</CancelButton>
              <SaveButton onClick={handleSell} disabled={listing || !sellPrice}>
                {listing ? '挂单中...' : '确认挂单'}
              </SaveButton>
            </ButtonGroup>
          </EditModalContent>
        </EditModalOverlay>
      )}
    </Container>
  );
}

export default ProfilePage;