import { useState, useEffect } from 'react';
import styled from 'styled-components';
import { Table, Pagination, Button, Input, Modal } from '../../components/common';
import useTable from '../../hooks/useTable';
import * as nftApi from '../../api/nft';

const Container = styled.div``;

const TabBar = styled.div`
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  border-bottom: 2px solid #e2e8f0;
`;

const Tab = styled.button`
  padding: 10px 20px;
  border: none;
  background: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  color: ${(p) => (p.$active ? '#667eea' : '#64748b')};
  border-bottom: 2px solid ${(p) => (p.$active ? '#667eea' : 'transparent')};
  margin-bottom: -2px;
  transition: all 0.2s;

  &:hover {
    color: #667eea;
  }
`;

const PendingBadge = styled.span`
  display: inline-block;
  background: #ef4444;
  color: white;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 10px;
  margin-left: 6px;
`;

const Toolbar = styled.div`
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
`;

const SearchInput = styled(Input)`
  width: 240px;
`;

const FilterSelect = styled.select`
  padding: 10px 14px;
  font-size: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #1e293b;
  cursor: pointer;

  &:focus {
    outline: none;
    border-color: #667eea;
  }
`;

const StatusBadge = styled.span`
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;

  ${(props) => {
    switch (props.status) {
      case 1:
        return `background: #dcfce7; color: #166534;`;
      case 2:
        return `background: #fef2f2; color: #991b1b;`;
      case 3:
        return `background: #fef3c7; color: #92400e;`;
      default:
        return `background: #f1f5f9; color: #475569;`;
    }
  }}
`;

const ReviewStatusBadge = styled.span`
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;

  ${(props) => {
    switch (props.status) {
      case 'approved':
        return `background: #dcfce7; color: #166534;`;
      case 'rejected':
        return `background: #fef2f2; color: #991b1b;`;
      default:
        return `background: #fef3c7; color: #92400e;`;
    }
  }}
`;

const NftImage = styled.img`
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
`;

const NftThumb = styled.div`
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  background: #f1f5f9;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
`;

const NftInfo = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
`;

const NftMeta = styled.div`
  font-size: 13px;
  color: #475569;
  margin-top: 2px;
`;

const Textarea = styled.textarea`
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  min-height: 80px;
  box-sizing: border-box;
  font-family: inherit;
  color: #1e293b;

  &:focus {
    outline: none;
    border-color: #667eea;
  }
`;

const FormLabel = styled.div`
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
`;

const FormGroup = styled.div`
  margin-bottom: 16px;
`;

const AddrText = styled.span`
  font-size: 12px;
  color: #94a3b8;
  font-family: monospace;
`;

const DetailRow = styled.div`
  display: flex;
  gap: 8px;
  padding: 9px 0;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
  &:last-child { border-bottom: none; }
`;

const DetailLabel = styled.span`
  color: #64748b;
  min-width: 90px;
  flex-shrink: 0;
`;

const DetailValue = styled.span`
  color: #1e293b;
  word-break: break-all;
  font-family: ${(p) => p.mono ? 'monospace' : 'inherit'};
  font-size: ${(p) => p.mono ? '12px' : '14px'};
`;

const VersionItem = styled.div`
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
  &:last-child { border-bottom: none; }
`;

const Toast = styled.div`
  position: fixed;
  top: 20px;
  right: 20px;
  background: #1e293b;
  color: white;
  padding: 12px 20px;
  border-radius: 8px;
  z-index: 9999;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
`;

function NftsPage() {
  const [activeTab, setActiveTab] = useState('list');
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [selectedNft, setSelectedNft] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState('');

  // 审核相关状态
  const [pendingList, setPendingList] = useState([]);
  const [pendingLoading, setPendingLoading] = useState(false);
  const [pendingPage, setPendingPage] = useState(1);
  const [pendingTotal, setPendingTotal] = useState(0);
  const [pendingCount, setPendingCount] = useState(0);
  const [reviewModal, setReviewModal] = useState({ open: false, nft: null, action: '' });
  const [rejectReason, setRejectReason] = useState('');
  const [reviewComment, setReviewComment] = useState('');
  const [reviewProcessing, setReviewProcessing] = useState(false);
  const [toast, setToast] = useState('');

  // 详情弹窗
  const [nftDetail, setNftDetail] = useState(null);
  const [nftVersions, setNftVersions] = useState([]);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);

  const showToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(''), 3000);
  };

  const { data, loading, pagination, fetchData, changePage } = useTable(
    (params) => nftApi.getNftList(params),
    10
  );

  // 加载待审核数量（用于红点）
  useEffect(() => {
    loadPendingCount();
  }, []);

  const loadPendingCount = async () => {
    try {
      const res = await nftApi.getPendingReviews(1, 1);
      if (res.status === 'success') {
        // 从列表推断 total，或用 count 接口
        setPendingCount(res.data?.total || (res.data?.list?.length > 0 ? 1 : 0));
      }
    } catch (e) {
      // 静默失败
    }
  };

  const loadPendingReviews = async (page = 1) => {
    setPendingLoading(true);
    try {
      const res = await nftApi.getPendingReviews(page, 10);
      if (res.status === 'success') {
        setPendingList(res.data?.list || res.data || []);
        setPendingTotal(res.data?.total || (res.data?.length ?? 0));
        setPendingPage(page);
      }
    } catch (e) {
      showToast('加载待审核列表失败');
    } finally {
      setPendingLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'review') {
      loadPendingReviews(1);
    }
  }, [activeTab]);

  const handleSearch = () => {
    fetchData({ pageNum: 1, keyword, status: statusFilter || undefined });
  };

  const openModal = (type, nft) => {
    setModalType(type);
    setSelectedNft(nft);
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setSelectedNft(null);
    setModalType('');
  };

  const openDetail = async (nft) => {
    setDetailOpen(true);
    setNftDetail(nft);
    setNftVersions([]);
    setDetailLoading(true);
    try {
      const [detailRes, versionsRes] = await Promise.all([
        nftApi.getNftDetail(nft.id),
        nftApi.getNftVersions(nft.id),
      ]);
      if (detailRes.status === 'success') setNftDetail(detailRes.data);
      if (versionsRes.status === 'success') setNftVersions(versionsRes.data || []);
    } catch {
      // 静默失败，使用列表数据
    } finally {
      setDetailLoading(false);
    }
  };

  const handleAction = async () => {
    try {
      switch (modalType) {
        case 'delist':
          await nftApi.delistNft(selectedNft.id);
          showToast(`NFT "${selectedNft.name}" 已下架`);
          break;
        case 'freeze':
          await nftApi.freezeNft(selectedNft.id);
          showToast(`NFT "${selectedNft.name}" 已冻结`);
          break;
        case 'unfreeze':
          await nftApi.unfreezeNft(selectedNft.id);
          showToast(`NFT "${selectedNft.name}" 已解冻`);
          break;
      }
      closeModal();
      fetchData();
    } catch (error) {
      showToast('操作失败：' + (error?.message || '未知错误'));
    }
  };

  const openReviewModal = (nft, action) => {
    setReviewModal({ open: true, nft, action });
    setRejectReason('');
    setReviewComment('');
  };

  const closeReviewModal = () => {
    setReviewModal({ open: false, nft: null, action: '' });
  };

  const handleReview = async () => {
    const { nft, action } = reviewModal;
    if (action === 'reject' && !rejectReason.trim()) {
      showToast('请填写拒绝原因');
      return;
    }
    setReviewProcessing(true);
    try {
      if (action === 'approve') {
        await nftApi.approveNft(nft.nftAssetId, reviewComment || undefined);
        showToast('审核已通过');
      } else {
        await nftApi.rejectNft(nft.nftAssetId, rejectReason, reviewComment || undefined);
        showToast('已拒绝该 NFT');
      }
      closeReviewModal();
      loadPendingReviews(pendingPage);
      loadPendingCount();
    } catch (e) {
      showToast('操作失败：' + (e?.message || '未知错误'));
    } finally {
      setReviewProcessing(false);
    }
  };

  const listColumns = [
    {
      title: '图片',
      dataIndex: 'imageUrl',
      render: (url) => url ? <NftImage src={url} alt="NFT" /> : '-',
    },
    { title: 'ID', dataIndex: 'id', width: '80px' },
    { title: '名称', dataIndex: 'name' },
    { title: '分类', dataIndex: 'category' },
    { title: '版本', dataIndex: 'currentVersion' },
    {
      title: '状态',
      dataIndex: 'status',
      render: (status) => (
        <StatusBadge status={status}>
          {status === 1 ? '正常' : status === 2 ? '下架' : '冻结'}
        </StatusBadge>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      render: (time) => time ? new Date(time).toLocaleDateString() : '-',
    },
    {
      title: '操作',
      render: (_, record) => (
        <div style={{ display: 'flex', gap: '8px' }}>
          <Button variant="secondary" size="sm" onClick={() => openDetail(record)}>
            详情
          </Button>
          {record.status === 1 && (
            <>
              <Button variant="warning" size="sm" onClick={() => openModal('delist', record)}>
                下架
              </Button>
              <Button variant="danger" size="sm" onClick={() => openModal('freeze', record)}>
                冻结
              </Button>
            </>
          )}
          {record.status === 3 && (
            <Button variant="success" size="sm" onClick={() => openModal('unfreeze', record)}>
              解冻
            </Button>
          )}
        </div>
      ),
    },
  ];

  const reviewColumns = [
    {
      title: 'NFT',
      render: (_, row) => (
        <NftInfo>
          <NftThumb>
            {row.imageUrl ? <img src={row.imageUrl} alt="" /> : null}
          </NftThumb>
          <div>
            <div style={{ fontWeight: 500, fontSize: 14 }}>{row.nftName || row.name || '-'}</div>
            <NftMeta>{row.category}</NftMeta>
          </div>
        </NftInfo>
      ),
    },
    {
      title: '提交人',
      render: (_, row) => (
        <div>
          <div style={{ fontSize: 13 }}>{row.creatorUsername || '-'}</div>
          <AddrText>{row.creatorAddress?.slice(0, 10)}...</AddrText>
        </div>
      ),
    },
    {
      title: '审核状态',
      dataIndex: 'status',
      render: (status) => (
        <ReviewStatusBadge status={status || 'pending'}>
          {status === 'approved' ? '已通过' : status === 'rejected' ? '已拒绝' : '待审核'}
        </ReviewStatusBadge>
      ),
    },
    {
      title: '提交时间',
      dataIndex: 'createdAt',
      render: (t) => t ? new Date(t).toLocaleString() : '-',
    },
    {
      title: '操作',
      render: (_, row) => (
        <div style={{ display: 'flex', gap: 8 }}>
          <Button variant="success" size="sm" onClick={() => openReviewModal(row, 'approve')}>
            通过
          </Button>
          <Button variant="danger" size="sm" onClick={() => openReviewModal(row, 'reject')}>
            拒绝
          </Button>
        </div>
      ),
    },
  ];

  return (
    <Container>
      {toast && (
        <Toast>{toast}</Toast>
      )}

      <TabBar>
        <Tab $active={activeTab === 'list'} onClick={() => setActiveTab('list')}>
          NFT 列表
        </Tab>
        <Tab $active={activeTab === 'review'} onClick={() => setActiveTab('review')}>
          内容审核
          {pendingCount > 0 && <PendingBadge>{pendingCount > 99 ? '99+' : pendingCount}</PendingBadge>}
        </Tab>
      </TabBar>

      {activeTab === 'list' && (
        <>
          <Toolbar>
            <SearchInput
              placeholder="搜索 NFT 名称..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            />
            <FilterSelect value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="">全部状态</option>
              <option value="1">正常</option>
              <option value="2">下架</option>
              <option value="3">冻结</option>
            </FilterSelect>
            <Button onClick={handleSearch}>搜索</Button>
          </Toolbar>

          <Table columns={listColumns} data={data} loading={loading} />

          <Pagination
            current={pagination.current}
            pageSize={pagination.pageSize}
            total={pagination.total}
            onChange={changePage}
          />
        </>
      )}

      {activeTab === 'review' && (
        <>
          <Table columns={reviewColumns} data={pendingList} loading={pendingLoading} />
          <Pagination
            current={pendingPage}
            pageSize={10}
            total={pendingTotal}
            onChange={(page) => loadPendingReviews(page)}
          />
        </>
      )}

      {/* NFT 详情弹窗 */}
      <Modal
        open={detailOpen}
        title="NFT 详情"
        onClose={() => setDetailOpen(false)}
        footer={<Button variant="outline" onClick={() => setDetailOpen(false)}>关闭</Button>}
      >
        {detailLoading ? (
          <div style={{ textAlign: 'center', color: '#94a3b8', padding: 20 }}>加载中...</div>
        ) : nftDetail && (
          <div>
            {nftDetail.imageUrl && (
              <img src={nftDetail.imageUrl} alt={nftDetail.name} style={{ width: '100%', maxHeight: 200, objectFit: 'cover', borderRadius: 8, marginBottom: 16 }} />
            )}
            <DetailRow><DetailLabel>ID</DetailLabel><DetailValue>{nftDetail.id}</DetailValue></DetailRow>
            <DetailRow><DetailLabel>名称</DetailLabel><DetailValue>{nftDetail.name}</DetailValue></DetailRow>
            <DetailRow><DetailLabel>分类</DetailLabel><DetailValue>{nftDetail.category || '-'}</DetailValue></DetailRow>
            <DetailRow><DetailLabel>版本</DetailLabel><DetailValue>{nftDetail.currentVersion}</DetailValue></DetailRow>
            <DetailRow>
              <DetailLabel>版税</DetailLabel>
              <DetailValue>{nftDetail.royaltyFee != null ? (nftDetail.royaltyFee / 100).toFixed(2) + '%' : '-'}</DetailValue>
            </DetailRow>
            <DetailRow>
              <DetailLabel>创作者</DetailLabel>
              <DetailValue mono>{nftDetail.creatorAddress}</DetailValue>
            </DetailRow>
            <DetailRow>
              <DetailLabel>持有者</DetailLabel>
              <DetailValue mono>{nftDetail.ownerAddress}</DetailValue>
            </DetailRow>
            {nftDetail.description && (
              <DetailRow>
                <DetailLabel>描述</DetailLabel>
                <DetailValue>{nftDetail.description}</DetailValue>
              </DetailRow>
            )}
            <DetailRow>
              <DetailLabel>创建时间</DetailLabel>
              <DetailValue>{nftDetail.createdAt ? new Date(nftDetail.createdAt).toLocaleString() : '-'}</DetailValue>
            </DetailRow>

            {nftVersions.length > 0 && (
              <div style={{ marginTop: 16 }}>
                <div style={{ fontSize: 14, fontWeight: 600, color: '#1e293b', marginBottom: 8 }}>版本历史</div>
                {nftVersions.map((v, i) => (
                  <VersionItem key={v.id || i}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13 }}>
                      <span style={{ fontWeight: 600 }}>v{v.version}</span>
                      <span style={{ color: '#94a3b8' }}>{v.createdAt ? new Date(v.createdAt).toLocaleString() : '-'}</span>
                    </div>
                    {v.ipfsCid && (
                      <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 2, fontFamily: 'monospace' }}>
                        CID: {v.ipfsCid}
                      </div>
                    )}
                    {v.changeDescription && (
                      <div style={{ fontSize: 13, color: '#475569', marginTop: 4 }}>{v.changeDescription}</div>
                    )}
                  </VersionItem>
                ))}
              </div>
            )}
          </div>
        )}
      </Modal>

      {/* NFT 管理操作确认弹窗 */}
      <Modal
        open={modalOpen}
        title="确认操作"
        onClose={closeModal}
        footer={
          <>
            <Button variant="secondary" onClick={closeModal}>取消</Button>
            <Button onClick={handleAction}>确定</Button>
          </>
        }
      >
        <p>
          {modalType === 'delist' && `确定要下架 NFT "${selectedNft?.name}" 吗？`}
          {modalType === 'freeze' && `确定要冻结 NFT "${selectedNft?.name}" 吗？`}
          {modalType === 'unfreeze' && `确定要解冻 NFT "${selectedNft?.name}" 吗？`}
        </p>
      </Modal>

      {/* 审核弹窗 */}
      <Modal
        open={reviewModal.open}
        title={reviewModal.action === 'approve' ? '审核通过确认' : '审核拒绝'}
        onClose={closeReviewModal}
        footer={
          <>
            <Button variant="outline" onClick={closeReviewModal}>取消</Button>
            <Button
              variant={reviewModal.action === 'approve' ? 'success' : 'danger'}
              onClick={handleReview}
              disabled={reviewProcessing}
            >
              {reviewProcessing ? '处理中...' : reviewModal.action === 'approve' ? '确认通过' : '确认拒绝'}
            </Button>
          </>
        }
      >
        <div>
          <div style={{ marginBottom: 16, fontSize: 14, color: '#475569' }}>
            NFT：<strong style={{ color: '#1e293b' }}>{reviewModal.nft?.nftName || reviewModal.nft?.name}</strong>
          </div>

          {reviewModal.action === 'reject' && (
            <FormGroup>
              <FormLabel>拒绝原因 *</FormLabel>
              <Textarea
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="请填写拒绝原因，将通知给提交人"
              />
            </FormGroup>
          )}

          <FormGroup>
            <FormLabel>审核意见（可选）</FormLabel>
            <Textarea
              value={reviewComment}
              onChange={(e) => setReviewComment(e.target.value)}
              placeholder="附加说明..."
            />
          </FormGroup>
        </div>
      </Modal>
    </Container>
  );
}

export default NftsPage;
