import { useState } from 'react';
import styled from 'styled-components';
import { Table, Pagination, Button, Input, Modal } from '../../components/common';
import useTable from '../../hooks/useTable';
import * as fanTokenApi from '../../api/fanToken';

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
        return `
          background: #dcfce7;
          color: #166534;
        `;
      case 2:
        return `
          background: #fef3c7;
          color: #92400e;
        `;
      case 3:
        return `
          background: #f1f5f9;
          color: #475569;
        `;
      default:
        return `
          background: #f1f5f9;
          color: #475569;
        `;
    }
  }}
`;

const SaleBadge = styled.span`
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;

  ${(props) => props.$active ? `
    background: #dbeafe;
    color: #1e40af;
  ` : `
    background: #f1f5f9;
    color: #64748b;
  `}
`;

const ActionButton = styled(Button)`
  padding: 4px 8px;
  font-size: 12px;
`;

function FanTokensPage() {
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [selectedToken, setSelectedToken] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState('');

  const { data, loading, pagination, fetchData, changePage } = useTable(
    (params) => fanTokenApi.getFanTokenList(params),
    10
  );

  const handleSearch = () => {
    fetchData({ pageNum: 1, keyword, status: statusFilter || undefined });
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const openModal = (type, token) => {
    setModalType(type);
    setSelectedToken(token);
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setSelectedToken(null);
    setModalType('');
  };

  const handleAction = async () => {
    try {
      switch (modalType) {
        case 'activate':
          await fanTokenApi.activatePublicSale(selectedToken.id);
          break;
        case 'pause':
          await fanTokenApi.pausePublicSale(selectedToken.id);
          break;
        case 'end':
          await fanTokenApi.endPublicSale(selectedToken.id);
          break;
      }
      closeModal();
      fetchData();
    } catch (error) {
      console.error('操作失败:', error);
    }
  };

  const formatSupply = (supply) => {
    if (!supply) return '0';
    return Number(supply).toLocaleString();
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: '80px' },
    { title: '名称', dataIndex: 'name' },
    { title: '符号', dataIndex: 'symbol' },
    { title: '总供应量', dataIndex: 'totalSupply', render: formatSupply },
    { title: '公募剩余', dataIndex: 'publicSaleRemaining', render: formatSupply },
    {
      title: '公募状态',
      dataIndex: 'publicSaleActive',
      render: (active) => <SaleBadge $active={active}>{active ? '进行中' : '未开始'}</SaleBadge>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      render: (status) => (
        <StatusBadge status={status}>
          {status === 1 ? '正常' : status === 2 ? '暂停' : '结束'}
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
          {!record.publicSaleActive && record.status !== 3 && (
            <ActionButton variant="success" size="sm" onClick={() => openModal('activate', record)}>
              激活公募
            </ActionButton>
          )}
          {record.publicSaleActive && (
            <ActionButton variant="warning" size="sm" onClick={() => openModal('pause', record)}>
              暂停公募
            </ActionButton>
          )}
          {record.status !== 3 && (
            <ActionButton variant="danger" size="sm" onClick={() => openModal('end', record)}>
              结束公募
            </ActionButton>
          )}
        </div>
      ),
    },
  ];

  const getModalContent = () => {
    switch (modalType) {
      case 'activate':
        return `确定要激活 "${selectedToken?.name}" 的公募吗？`;
      case 'pause':
        return `确定要暂停 "${selectedToken?.name}" 的公募吗？`;
      case 'end':
        return `确定要结束 "${selectedToken?.name}" 的公募吗？此操作不可逆。`;
      default:
        return '';
    }
  };

  return (
    <Container>
      <Toolbar>
        <SearchInput
          placeholder="搜索代币名称..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyPress={handleKeyPress}
        />
        <FilterSelect value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="">全部状态</option>
          <option value="1">正常</option>
          <option value="2">暂停</option>
          <option value="3">结束</option>
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
        title="确认操作"
        onClose={closeModal}
        footer={
          <>
            <Button variant="secondary" onClick={closeModal}>
              取消
            </Button>
            <Button onClick={handleAction}>确定</Button>
          </>
        }
      >
        <p>{getModalContent()}</p>
      </Modal>
    </Container>
  );
}

export default FanTokensPage;