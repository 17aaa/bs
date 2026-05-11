import { useState, useEffect } from 'react';
import styled from 'styled-components';
import Header from '../components/Header';
import { notify } from '../components/Notification';
import { fanTokenApi } from '../services/api';

const Container = styled.div`
  min-height: 100vh;
  background: #f5f7fa;
`;

const Content = styled.div`
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 20px;
`;

const PageTitle = styled.h1`
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
`;

const PageSub = styled.p`
  color: #64748b;
  font-size: 15px;
  margin: 0 0 32px 0;
`;

const TabBar = styled.div`
  display: flex;
  gap: 0;
  margin-bottom: 28px;
  border-bottom: 2px solid #e2e8f0;
`;

const Tab = styled.button`
  padding: 12px 24px;
  border: none;
  background: none;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  color: ${(p) => (p.$active ? '#667eea' : '#64748b')};
  border-bottom: 2px solid ${(p) => (p.$active ? '#667eea' : 'transparent')};
  margin-bottom: -2px;
  transition: all 0.2s;

  &:hover { color: #667eea; }
`;

const Card = styled.div`
  background: white;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  padding: 28px;
  margin-bottom: 20px;
`;

const CardTitle = styled.h2`
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 20px 0;
`;

const FormGrid = styled.div`
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
`;

const FormGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 6px;
`;

const Label = styled.label`
  font-size: 13px;
  font-weight: 500;
  color: #374151;
`;

const Input = styled.input`
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  color: #1e293b;
  background: white;
  transition: border-color 0.2s;

  &:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102,126,234,0.1);
  }

  &::placeholder { color: #94a3b8; }
`;

const Hint = styled.p`
  font-size: 12px;
  color: #94a3b8;
  margin: 4px 0 0 0;
`;

const SubmitBtn = styled.button`
  margin-top: 20px;
  padding: 14px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.2s;

  &:hover:not(:disabled) { opacity: 0.9; transform: translateY(-1px); }
  &:disabled { opacity: 0.5; cursor: not-allowed; }
`;

const TokenGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
`;

const TokenCard = styled.div`
  background: white;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  padding: 24px;
  border: 1px solid #f1f5f9;
  transition: box-shadow 0.2s;

  &:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.12); }
`;

const TokenHeader = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
`;

const TokenIcon = styled.div`
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
`;

const TokenName = styled.div`
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
`;

const TokenSymbol = styled.div`
  font-size: 13px;
  color: #667eea;
  font-weight: 500;
`;

const TokenStats = styled.div`
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
`;

const StatItem = styled.div`
  background: #f8fafc;
  border-radius: 8px;
  padding: 10px 12px;
`;

const StatLabel = styled.div`
  font-size: 11px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
`;

const StatValue = styled.div`
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
`;

const ActionRow = styled.div`
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
`;

const ActionBtn = styled.button`
  flex: 1;
  padding: 10px 16px;
  border: 1px solid ${(p) => p.primary ? '#667eea' : '#e2e8f0'};
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background: ${(p) => p.primary ? 'linear-gradient(135deg,#667eea,#764ba2)' : 'white'};
  color: ${(p) => p.primary ? 'white' : '#475569'};
  transition: all 0.2s;
  min-width: 0;

  &:hover:not(:disabled) {
    background: ${(p) => p.primary ? 'linear-gradient(135deg,#5a6fd8,#6a4394)' : '#f8fafc'};
    transform: translateY(-1px);
  }
  &:disabled { opacity: 0.5; cursor: not-allowed; }
`;

const EmptyState = styled.div`
  text-align: center;
  padding: 60px 20px;
  color: #94a3b8;
  font-size: 15px;
`;

const ModalOverlay = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
`;

const ModalBox = styled.div`
  background: white;
  border-radius: 16px;
  padding: 28px;
  width: 100%;
  max-width: 420px;
`;

const ModalTitle = styled.h3`
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 20px 0;
`;

const ModalActions = styled.div`
  display: flex;
  gap: 10px;
  margin-top: 20px;
  justify-content: flex-end;
`;

const CancelBtn = styled.button`
  padding: 10px 20px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #64748b;
  font-size: 14px;
  cursor: pointer;
`;

const ConfirmBtn = styled.button`
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  &:disabled { opacity: 0.5; cursor: not-allowed; }
`;

function FanTokenPage({ isWalletConnected, walletAddress, onWalletConnect }) {
  const [activeTab, setActiveTab] = useState('explore');
  const [tokens, setTokens] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalState, setModalState] = useState({ open: false, type: '', token: null });
  const [amount, setAmount] = useState('');
  const [processing, setProcessing] = useState(false);

  const [createForm, setCreateForm] = useState({
    name: '',
    symbol: '',
    projectId: '',
    totalSupply: '',
    creatorAddress: walletAddress || '',
  });

  useEffect(() => {
    if (walletAddress) {
      setCreateForm((f) => ({ ...f, creatorAddress: walletAddress }));
    }
  }, [walletAddress]);

  useEffect(() => {
    if (activeTab === 'explore') loadTokens();
  }, [activeTab, walletAddress]);

  const loadTokens = async () => {
    if (!walletAddress) { setTokens([]); return; }
    setLoading(true);
    try {
      const res = await fanTokenApi.getTokensByCreator(walletAddress);
      if (res.code === 200) setTokens(res.data || []);
    } catch (e) {
      notify.error('加载代币列表失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    if (!walletAddress) {
      notify.error('请先连接钱包');
      return;
    }
    setProcessing(true);
    try {
      const res = await fanTokenApi.createFanToken({
        projectId: createForm.projectId,
        name: createForm.name,
        symbol: createForm.symbol,
        totalSupply: createForm.totalSupply,
        creatorAddress: walletAddress,
      });
      if (res.code === 200) {
        notify.success('粉丝代币创建成功！');
        setCreateForm({ name: '', symbol: '', projectId: '', totalSupply: '', creatorAddress: walletAddress });
        setActiveTab('explore');
      } else {
        notify.error(res.message || '创建失败');
      }
    } catch (e) {
      notify.error(e.message || '创建失败，请稍后重试');
    } finally {
      setProcessing(false);
    }
  };

  const openModal = (type, token) => {
    setModalState({ open: true, type, token });
    setAmount('');
  };

  const closeModal = () => {
    setModalState({ open: false, type: '', token: null });
  };

  const handleModalConfirm = async () => {
    if (!amount || isNaN(Number(amount)) || Number(amount) <= 0) {
      notify.error('请输入有效数量');
      return;
    }
    if (!walletAddress) {
      notify.error('请先连接钱包');
      return;
    }
    setProcessing(true);
    try {
      const { type, token } = modalState;
      let res;
      if (type === 'buy') {
        const unitPrice = BigInt(token.salePrice || '0');
        const paymentAmount = unitPrice * BigInt(amount);
        res = await fanTokenApi.participateInSale({
          tokenAddress: token.tokenAddress,
          buyerAddress: walletAddress,
          amount,
          paymentAmount: paymentAmount.toString(),
        });
      } else if (type === 'stake') {
        res = await fanTokenApi.stake({
          tokenAddress: token.tokenAddress,
          userAddress: walletAddress,
          amount,
        });
      } else if (type === 'unstake') {
        res = await fanTokenApi.unstake({
          tokenAddress: token.tokenAddress,
          userAddress: walletAddress,
          amount,
        });
      } else if (type === 'claim') {
        res = await fanTokenApi.claimReward({
          tokenAddress: token.tokenAddress,
          userAddress: walletAddress,
        });
      }
      if (res?.code === 200) {
        notify.success('操作成功！');
        closeModal();
        loadTokens();
      } else {
        notify.error(res?.message || '操作失败');
      }
    } catch (e) {
      notify.error(e.message || '操作失败');
    } finally {
      setProcessing(false);
    }
  };

  const modalTitle = {
    buy: '参与公募',
    stake: '质押代币',
    unstake: '解除质押',
    claim: '领取奖励',
  }[modalState.type] || '';

  const needAmount = ['buy', 'stake', 'unstake'].includes(modalState.type);

  return (
    <Container>
      <Header
        currentPage="fantoken"
        walletAddress={walletAddress}
        onWalletConnect={onWalletConnect}
        showLogout
      />

      <Content>
        <PageTitle>粉丝代币</PageTitle>
        <PageSub>创作者发行专属代币，粉丝通过质押获得收益</PageSub>

        <TabBar>
          <Tab $active={activeTab === 'explore'} onClick={() => setActiveTab('explore')}>探索代币</Tab>
          <Tab $active={activeTab === 'create'} onClick={() => setActiveTab('create')}>发行代币</Tab>
        </TabBar>

        {activeTab === 'explore' && (
          <>
            {loading ? (
              <EmptyState>加载中...</EmptyState>
            ) : tokens.length === 0 ? (
              <EmptyState>
                <div style={{ fontSize: 48, marginBottom: 16 }}>🪙</div>
                <div>暂无粉丝代币</div>
                <div style={{ fontSize: 13, marginTop: 8 }}>成为第一个发行粉丝代币的创作者！</div>
              </EmptyState>
            ) : (
              <TokenGrid>
                {tokens.map((token) => (
                  <TokenCard key={token.id}>
                    <TokenHeader>
                      <TokenIcon>{token.symbol?.[0] || 'T'}</TokenIcon>
                      <div>
                        <TokenName>{token.name}</TokenName>
                        <TokenSymbol>${token.symbol}</TokenSymbol>
                      </div>
                    </TokenHeader>

                    <TokenStats>
                      <StatItem>
                        <StatLabel>总供应量</StatLabel>
                        <StatValue>{Number(token.totalSupply).toLocaleString()}</StatValue>
                      </StatItem>
                      <StatItem>
                        <StatLabel>公募价格</StatLabel>
                        <StatValue>{token.salePrice} ETH</StatValue>
                      </StatItem>
                      <StatItem>
                        <StatLabel>已质押</StatLabel>
                        <StatValue>{Number(token.totalStaked || 0).toLocaleString()}</StatValue>
                      </StatItem>
                      <StatItem>
                        <StatLabel>创作者</StatLabel>
                        <StatValue style={{ fontSize: 12 }}>{token.creatorAddress?.slice(0, 8)}...</StatValue>
                      </StatItem>
                    </TokenStats>

                    <ActionRow>
                      <ActionBtn primary onClick={() => openModal('buy', token)}>参与公募</ActionBtn>
                      <ActionBtn onClick={() => openModal('stake', token)}>质押</ActionBtn>
                      <ActionBtn onClick={() => openModal('unstake', token)}>解押</ActionBtn>
                      <ActionBtn onClick={() => openModal('claim', token)}>领奖</ActionBtn>
                    </ActionRow>
                  </TokenCard>
                ))}
              </TokenGrid>
            )}
          </>
        )}

        {activeTab === 'create' && (
          <Card>
            <CardTitle>发行粉丝代币</CardTitle>
            <form onSubmit={handleCreateSubmit}>
              <FormGrid>
                <FormGroup>
                  <Label>代币名称 *</Label>
                  <Input
                    required
                    placeholder="如：Creative Token"
                    value={createForm.name}
                    onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
                  />
                </FormGroup>
                <FormGroup>
                  <Label>代币符号 *</Label>
                  <Input
                    required
                    placeholder="如：CRT"
                    value={createForm.symbol}
                    onChange={(e) => setCreateForm({ ...createForm, symbol: e.target.value.toUpperCase() })}
                    maxLength={8}
                  />
                  <Hint>2-8 个大写字母</Hint>
                </FormGroup>
                <FormGroup>
                  <Label>关联 NFT ID *</Label>
                  <Input
                    required
                    type="number"
                    min="1"
                    placeholder="关联的 NFT 资产 ID"
                    value={createForm.projectId}
                    onChange={(e) => setCreateForm({ ...createForm, projectId: e.target.value })}
                  />
                  <Hint>与某个 NFT 作品绑定</Hint>
                </FormGroup>
                <FormGroup>
                  <Label>总供应量 *</Label>
                  <Input
                    required
                    type="number"
                    min="1"
                    placeholder="如：1000000"
                    value={createForm.totalSupply}
                    onChange={(e) => setCreateForm({ ...createForm, totalSupply: e.target.value })}
                  />
                </FormGroup>
              </FormGrid>

              <FormGroup style={{ marginTop: 16 }}>
                <Label>创作者地址</Label>
                <Input
                  value={createForm.creatorAddress}
                  readOnly
                  style={{ background: '#f8fafc', color: '#64748b' }}
                />
                {!walletAddress && <Hint style={{ color: '#ef4444' }}>请先连接钱包</Hint>}
              </FormGroup>

              <SubmitBtn type="submit" disabled={processing || !walletAddress}>
                {processing ? '发行中...' : '发行代币'}
              </SubmitBtn>
            </form>
          </Card>
        )}
      </Content>

      {modalState.open && (
        <ModalOverlay onClick={(e) => e.target === e.currentTarget && closeModal()}>
          <ModalBox>
            <ModalTitle>{modalTitle}</ModalTitle>

            <div style={{ fontSize: 14, color: '#475569', marginBottom: 16 }}>
              代币：<strong style={{ color: '#1e293b' }}>{modalState.token?.name} (${modalState.token?.symbol})</strong>
            </div>

            {needAmount && (
              <FormGroup>
                <Label>数量</Label>
                <Input
                  type="number"
                  min="1"
                  placeholder="输入数量"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  autoFocus
                />
              </FormGroup>
            )}

            {modalState.type === 'claim' && (
              <p style={{ fontSize: 14, color: '#64748b' }}>
                将领取您在该代币池中的所有质押奖励。
              </p>
            )}

            <ModalActions>
              <CancelBtn onClick={closeModal}>取消</CancelBtn>
              <ConfirmBtn onClick={handleModalConfirm} disabled={processing}>
                {processing ? '处理中...' : '确认'}
              </ConfirmBtn>
            </ModalActions>
          </ModalBox>
        </ModalOverlay>
      )}
    </Container>
  );
}

export default FanTokenPage;
