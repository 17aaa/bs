import { useState } from 'react';
import styled from 'styled-components';
import { Table, Pagination, Button, Input, Modal } from '../../components/common';
import useTable from '../../hooks/useTable';
import * as userApi from '../../api/user';

const Container = styled.div``;

const Toolbar = styled.div`
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
`;

const SearchInput = styled(Input)`
  width: 240px;
`;

const RoleBadge = styled.span`
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;

  ${(props) => {
    switch (props.role) {
      case 'super_admin':
        return `background: #fef3c7; color: #92400e;`;
      case 'admin':
        return `background: #dbeafe; color: #1e40af;`;
      default:
        return `background: #f1f5f9; color: #475569;`;
    }
  }}
`;

const ActionGroup = styled.div`
  display: flex;
  gap: 8px;
`;

const FormGroup = styled.div`
  margin-bottom: 16px;
`;

const FormLabel = styled.div`
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
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

const UserResultCard = styled.div`
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px 16px;
  margin-top: 12px;
  font-size: 14px;
`;

function AdminsPage() {
  const [keyword, setKeyword] = useState('');
  const [confirmModal, setConfirmModal] = useState({ open: false, userId: null, action: null, username: '' });
  const [processing, setProcessing] = useState(false);
  const [toast, setToast] = useState('');

  // 添加管理员相关状态
  const [addModal, setAddModal] = useState(false);
  const [addKeyword, setAddKeyword] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [selectedCandidate, setSelectedCandidate] = useState(null);

  const showToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(''), 3000);
  };

  const { data, loading, pagination, fetchData, changePage } = useTable(
    (params) => userApi.getUserList({ ...params, role: 'admin', keyword }),
    10
  );

  const handleSearch = () => fetchData({ page: 1, keyword });

  const openConfirm = (userId, username, action) => {
    setConfirmModal({ open: true, userId, action, username });
  };

  const handleConfirm = async () => {
    const { userId, action } = confirmModal;
    setProcessing(true);
    try {
      if (action === 'downgrade') {
        await userApi.downgradeToUser(userId);
        showToast('已移除管理员权限');
      } else if (action === 'upgrade') {
        await userApi.upgradeToAdmin(userId);
        showToast('已升级为管理员');
      }
      setConfirmModal({ open: false, userId: null, action: null, username: '' });
      fetchData({ page: pagination.current });
    } catch (e) {
      showToast('操作失败：' + (e?.message || '未知错误'));
    } finally {
      setProcessing(false);
    }
  };

  const searchUsers = async () => {
    if (!addKeyword.trim()) return;
    setSearchLoading(true);
    setSelectedCandidate(null);
    try {
      const res = await userApi.getUserList({ keyword: addKeyword, role: 'user', pageSize: 5 });
      if (res.status === 'success') {
        setSearchResults(res.data?.records || res.data?.list || []);
      }
    } catch {
      showToast('搜索失败');
    } finally {
      setSearchLoading(false);
    }
  };

  const handleAddAdmin = async () => {
    if (!selectedCandidate) return;
    setProcessing(true);
    try {
      await userApi.upgradeToAdmin(selectedCandidate.userId);
      showToast(`已将 ${selectedCandidate.username} 升级为管理员`);
      setAddModal(false);
      setAddKeyword('');
      setSearchResults([]);
      setSelectedCandidate(null);
      fetchData({ page: 1 });
    } catch (e) {
      showToast('操作失败：' + (e?.message || '未知错误'));
    } finally {
      setProcessing(false);
    }
  };

  const roleLabel = (role) => {
    if (role === 'super_admin') return '超级管理员';
    if (role === 'admin') return '管理员';
    return '普通用户';
  };

  const columns = [
    { dataIndex: 'userId', title: 'ID', width: 80 },
    { dataIndex: 'username', title: '用户名' },
    { dataIndex: 'email', title: '邮箱' },
    { dataIndex: 'school', title: '学校' },
    {
      dataIndex: 'role',
      title: '角色',
      render: (value) => <RoleBadge role={value}>{roleLabel(value)}</RoleBadge>,
    },
    { dataIndex: 'createdAt', title: '创建时间', render: (value) => value?.slice(0, 10) },
    {
      title: '操作',
      render: (_, row) => (
        <ActionGroup>
          {row.role === 'admin' && (
            <Button
              variant="danger"
              size="sm"
              onClick={() => openConfirm(row.userId, row.username, 'downgrade')}
            >
              移除权限
            </Button>
          )}
        </ActionGroup>
      ),
    },
  ];

  return (
    <Container>
      {toast && <Toast>{toast}</Toast>}

      <Toolbar>
        <SearchInput
          placeholder="搜索用户名 / 邮箱"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
        />
        <Button onClick={handleSearch}>搜索管理员</Button>
        <Button variant="primary" onClick={() => { setAddModal(true); setAddKeyword(''); setSearchResults([]); setSelectedCandidate(null); }}>
          + 添加管理员
        </Button>
      </Toolbar>

      <Table columns={columns} data={data} loading={loading} />

      <Pagination
        current={pagination.current}
        total={pagination.total}
        pageSize={pagination.pageSize}
        onChange={changePage}
      />

      {/* 权限变更确认弹窗 */}
      <Modal
        open={confirmModal.open}
        onClose={() => setConfirmModal({ ...confirmModal, open: false })}
        title={confirmModal.action === 'downgrade' ? '移除管理员权限' : '升级为管理员'}
        footer={
          <>
            <Button variant="outline" onClick={() => setConfirmModal({ ...confirmModal, open: false })}>
              取消
            </Button>
            <Button variant="danger" onClick={handleConfirm} disabled={processing}>
              {processing ? '处理中...' : '确认'}
            </Button>
          </>
        }
      >
        确认{confirmModal.action === 'downgrade' ? '移除' : '升级'}用户{' '}
        <strong>{confirmModal.username}</strong> 的管理员权限？此操作不可撤销。
      </Modal>

      {/* 添加管理员弹窗 */}
      <Modal
        open={addModal}
        onClose={() => setAddModal(false)}
        title="添加管理员"
        footer={
          <>
            <Button variant="outline" onClick={() => setAddModal(false)}>取消</Button>
            <Button variant="primary" onClick={handleAddAdmin} disabled={!selectedCandidate || processing}>
              {processing ? '处理中...' : '确认添加'}
            </Button>
          </>
        }
      >
        <FormGroup>
          <FormLabel>搜索普通用户</FormLabel>
          <div style={{ display: 'flex', gap: 8 }}>
            <Input
              placeholder="输入用户名或邮箱"
              value={addKeyword}
              onChange={(e) => setAddKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && searchUsers()}
              style={{ flex: 1 }}
            />
            <Button onClick={searchUsers} disabled={searchLoading}>
              {searchLoading ? '搜索中...' : '搜索'}
            </Button>
          </div>
        </FormGroup>

        {searchResults.length > 0 && (
          <div>
            <FormLabel>选择要升级的用户：</FormLabel>
            {searchResults.map((user) => (
              <UserResultCard
                key={user.userId}
                style={{
                  cursor: 'pointer',
                  borderColor: selectedCandidate?.userId === user.userId ? '#667eea' : '#e2e8f0',
                  background: selectedCandidate?.userId === user.userId ? '#eff6ff' : '#f8fafc',
                }}
                onClick={() => setSelectedCandidate(user)}
              >
                <div style={{ fontWeight: 600 }}>{user.username}</div>
                <div style={{ fontSize: 12, color: '#64748b', marginTop: 2 }}>
                  {user.email} · {user.school || '未填写学校'}
                </div>
              </UserResultCard>
            ))}
          </div>
        )}

        {searchResults.length === 0 && addKeyword && !searchLoading && (
          <div style={{ color: '#94a3b8', fontSize: 14, textAlign: 'center', padding: '12px 0' }}>
            未找到匹配的普通用户
          </div>
        )}
      </Modal>
    </Container>
  );
}

export default AdminsPage;
