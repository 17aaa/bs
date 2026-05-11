import { useState, useEffect } from 'react';
import styled from 'styled-components';
import { Table, Pagination, Button, Input, Modal } from '../../components/common';
import useTable from '../../hooks/useTable';
import * as orderApi from '../../api/order';

const Container = styled.div``;

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
        return `background: #dbeafe; color: #1e40af;`;
      case 2:
        return `background: #dcfce7; color: #166534;`;
      case 3:
        return `background: #fef2f2; color: #991b1b;`;
      case 4:
        return `background: #f1f5f9; color: #475569;`;
      default:
        return `background: #f1f5f9; color: #475569;`;
    }
  }}
`;

const OrderTypeBadge = styled.span`
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: #f1f5f9;
  color: #475569;
`;

const StatsGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
`;

const StatCard = styled.div`
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
`;

const StatLabel = styled.div`
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
`;

const StatValue = styled.div`
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
`;

const DetailRow = styled.div`
  display: flex;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;

  &:last-child {
    border-bottom: none;
  }
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

function OrdersPage() {
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [orderTypeFilter, setOrderTypeFilter] = useState('');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [detailData, setDetailData] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [stats, setStats] = useState(null);
  const [toast, setToast] = useState('');

  const showToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(''), 3000);
  };

  const { data, loading, pagination, fetchData, changePage } = useTable(
    (params) => orderApi.getOrderList(params),
    10
  );

  useEffect(() => {
    orderApi.getStatistics().then((response) => {
      if (response.status === 'success') {
        setStats(response.data);
      }
    }).catch(console.error);
  }, []);

  const handleSearch = () => {
    fetchData({ pageNum: 1, keyword, status: statusFilter || undefined, orderType: orderTypeFilter || undefined });
  };

  const showOrderDetail = async (order) => {
    setSelectedOrder(order);
    setDetailData(null);
    setModalOpen(true);
    try {
      const res = await orderApi.getOrderDetail(order.orderId || order.id);
      if (res.status === 'success') setDetailData(res.data);
    } catch {
      setDetailData(order);
    }
  };

  const formatCredits = (price) => {
    if (!price) return '-';
    return (Number(BigInt(price)) / 1e18).toFixed(4) + ' 积分';
  };

  const statusLabel = { 1: '活跃', 2: '已售', 3: '取消', 4: '过期' };
  const typeLabel = { 1: '固定价格', 2: '荷兰拍卖', 3: '报价' };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: '80px' },
    { title: 'NFT 名称', dataIndex: 'nftName' },
    {
      title: '类型',
      dataIndex: 'orderType',
      render: (type) => <OrderTypeBadge>{typeLabel[type] || '-'}</OrderTypeBadge>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      render: (status) => (
        <StatusBadge status={status}>{statusLabel[status] || '-'}</StatusBadge>
      ),
    },
    { title: '挂单价', dataIndex: 'price', render: formatCredits },
    { title: '成交价', dataIndex: 'finalPrice', render: formatCredits },
    {
      title: '时间',
      dataIndex: 'createdAt',
      render: (time) => time ? new Date(time).toLocaleDateString() : '-',
    },
    {
      title: '操作',
      render: (_, record) => (
        <Button variant="secondary" size="sm" onClick={() => showOrderDetail(record)}>
          详情
        </Button>
      ),
    },
  ];

  const detail = detailData || selectedOrder;

  return (
    <Container>
      {toast && <Toast>{toast}</Toast>}

      {stats && (
        <StatsGrid>
          <StatCard>
            <StatLabel>总订单数</StatLabel>
            <StatValue>{stats.totalOrders || 0}</StatValue>
          </StatCard>
          <StatCard>
            <StatLabel>已完成</StatLabel>
            <StatValue>{stats.completedOrders || 0}</StatValue>
          </StatCard>
          <StatCard>
            <StatLabel>活跃订单</StatLabel>
            <StatValue>{stats.activeOrders || 0}</StatValue>
          </StatCard>
          <StatCard>
            <StatLabel>总交易额</StatLabel>
            <StatValue style={{ fontSize: 18 }}>{formatCredits(stats.totalVolume)}</StatValue>
          </StatCard>
        </StatsGrid>
      )}

      <Toolbar>
        <SearchInput
          placeholder="搜索订单 ID..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
        />
        <FilterSelect value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="">全部状态</option>
          <option value="1">活跃</option>
          <option value="2">已售</option>
          <option value="3">取消</option>
          <option value="4">过期</option>
        </FilterSelect>
        <FilterSelect value={orderTypeFilter} onChange={(e) => setOrderTypeFilter(e.target.value)}>
          <option value="">全部类型</option>
          <option value="1">固定价格</option>
          <option value="2">荷兰拍卖</option>
          <option value="3">报价</option>
        </FilterSelect>
        <Button onClick={handleSearch}>搜索</Button>
      </Toolbar>

      <Table columns={columns} data={data} loading={loading} />

      <Pagination
        current={pagination.current}
        pageSize={pagination.pageSize}
        total={pagination.total}
        onChange={changePage}
      />

      <Modal
        open={modalOpen}
        title="订单详情"
        onClose={() => { setModalOpen(false); setDetailData(null); }}
      >
        {detail ? (
          <div>
            <DetailRow>
              <DetailLabel>订单 ID</DetailLabel>
              <DetailValue mono>{detail.orderId || detail.id}</DetailValue>
            </DetailRow>
            <DetailRow>
              <DetailLabel>NFT 名称</DetailLabel>
              <DetailValue>{detail.nftName || '-'}</DetailValue>
            </DetailRow>
            <DetailRow>
              <DetailLabel>类型</DetailLabel>
              <DetailValue>{typeLabel[detail.orderType] || '-'}</DetailValue>
            </DetailRow>
            <DetailRow>
              <DetailLabel>状态</DetailLabel>
              <DetailValue>
                <StatusBadge status={detail.status}>{statusLabel[detail.status] || '-'}</StatusBadge>
              </DetailValue>
            </DetailRow>
            <DetailRow>
              <DetailLabel>卖家地址</DetailLabel>
              <DetailValue mono>{detail.sellerAddress || '-'}</DetailValue>
            </DetailRow>
            <DetailRow>
              <DetailLabel>买家地址</DetailLabel>
              <DetailValue mono>{detail.buyerAddress || '-'}</DetailValue>
            </DetailRow>
            <DetailRow>
              <DetailLabel>挂单价</DetailLabel>
              <DetailValue>{formatCredits(detail.price)}</DetailValue>
            </DetailRow>
            {detail.orderType === 2 && (
              <DetailRow>
                <DetailLabel>底价</DetailLabel>
                <DetailValue>{formatCredits(detail.reservePrice)}</DetailValue>
              </DetailRow>
            )}
            <DetailRow>
              <DetailLabel>成交价</DetailLabel>
              <DetailValue>{formatCredits(detail.finalPrice)}</DetailValue>
            </DetailRow>
            {detail.txHash && (
              <DetailRow>
                <DetailLabel>交易哈希</DetailLabel>
                <DetailValue mono>{detail.txHash}</DetailValue>
              </DetailRow>
            )}
            <DetailRow>
              <DetailLabel>创建时间</DetailLabel>
              <DetailValue>{detail.createdAt ? new Date(detail.createdAt).toLocaleString() : '-'}</DetailValue>
            </DetailRow>
            {detail.updatedAt && (
              <DetailRow>
                <DetailLabel>更新时间</DetailLabel>
                <DetailValue>{new Date(detail.updatedAt).toLocaleString()}</DetailValue>
              </DetailRow>
            )}
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '20px', color: '#94a3b8' }}>加载中...</div>
        )}
      </Modal>
    </Container>
  );
}

export default OrdersPage;
