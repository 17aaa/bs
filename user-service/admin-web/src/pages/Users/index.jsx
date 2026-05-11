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
      default:
        return `background: #f1f5f9; color: #475569;`;
    }
  }}
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

const ActionButton = styled(Button)`
  padding: 4px 8px;
  font-size: 12px;
`;

const FormGroup = styled.div`
  margin-bottom: 14px;
`;

const FormLabel = styled.div`
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
`;

const DetailGrid = styled.div`
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 20px;
  margin-bottom: 16px;
`;

const DetailItem = styled.div`
  font-size: 14px;
`;

const DetailLabel = styled.div`
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 2px;
`;

const DetailValue = styled.div`
  color: #1e293b;
  font-weight: 500;
`;

const BalanceValue = styled.div`
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
  margin-top: 4px;
`;

const Divider = styled.div`
  border-top: 1px solid #f1f5f9;
  margin: 16px 0;
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

function UsersPage() {
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [roleFilter, setRoleFilter] = useState('');

  // 操作确认弹窗
  const [selectedUser, setSelectedUser] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState('');

  // 详情弹窗
  const [detailUser, setDetailUser] = useState(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);

  // 编辑弹窗
  const [editOpen, setEditOpen] = useState(false);
  const [editForm, setEditForm] = useState({ username: '', email: '', school: '', major: '', bio: '' });
  const [editUserId, setEditUserId] = useState(null);

  // 重置密码弹窗
  const [pwdOpen, setPwdOpen] = useState(false);
  const [pwdUserId, setPwdUserId] = useState(null);
  const [newPassword, setNewPassword] = useState('');

  const [processing, setProcessing] = useState(false);
  const [toast, setToast] = useState('');

  const showToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(''), 3000);
  };

  const { data, loading, pagination, fetchData, changePage } = useTable(
    (params) => userApi.getUserList(params),
    10
  );

  const handleSearch = () => {
    fetchData({ pageNum: 1, keyword, status: statusFilter || undefined, role: roleFilter || undefined });
  };

  const openModal = (type, user) => {
    setModalType(type);
    setSelectedUser(user);
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setSelectedUser(null);
    setModalType('');
  };

  const handleAction = async () => {
    setProcessing(true);
    try {
      switch (modalType) {
        case 'ban':
          await userApi.banUser(selectedUser.userId, '管理员封禁');
          showToast(`已封禁用户 ${selectedUser.username}`);
          break;
        case 'unban':
          await userApi.unbanUser(selectedUser.userId);
          showToast(`已解封用户 ${selectedUser.username}`);
          break;
        case 'upgrade':
          await userApi.upgradeToAdmin(selectedUser.userId);
          showToast(`已将 ${selectedUser.username} 升级为管理员`);
          break;
        case 'downgrade':
          await userApi.downgradeToUser(selectedUser.userId);
          showToast(`已将 ${selectedUser.username} 降级为普通用户`);
          break;
      }
      closeModal();
      fetchData();
    } catch (error) {
      showToast('操作失败：' + (error?.message || '未知错误'));
    } finally {
      setProcessing(false);
    }
  };

  const openDetail = async (user) => {
    setDetailOpen(true);
    setDetailUser(user);
    setDetailLoading(true);
    try {
      const res = await userApi.getUserDetail(user.userId);
      if (res.status === 'success') setDetailUser(res.data);
    } catch {
      // 使用列表数据
    } finally {
      setDetailLoading(false);
    }
  };

  const openEdit = (user) => {
    setEditUserId(user.userId);
    setEditForm({
      username: user.username || '',
      email: user.email || '',
      school: user.school || '',
      major: user.major || '',
      bio: user.bio || '',
    });
    setEditOpen(true);
  };

  const handleEdit = async () => {
    setProcessing(true);
    try {
      await userApi.updateUser(editUserId, editForm);
      showToast('用户信息已更新');
      setEditOpen(false);
      fetchData();
    } catch (e) {
      showToast('更新失败：' + (e?.message || '未知错误'));
    } finally {
      setProcessing(false);
    }
  };

  const openResetPwd = (user) => {
    setPwdUserId(user.userId);
    setNewPassword('');
    setPwdOpen(true);
  };

  const handleResetPwd = async () => {
    if (!newPassword || newPassword.length < 6) {
      showToast('密码至少需要 6 位');
      return;
    }
    setProcessing(true);
    try {
      await userApi.resetPassword(pwdUserId, newPassword);
      showToast('密码重置成功');
      setPwdOpen(false);
    } catch (e) {
      showToast('重置失败：' + (e?.message || '未知错误'));
    } finally {
      setProcessing(false);
    }
  };

  const formatBalance = (balance) => {
    if (!balance) return '0.00';
    return (Number(BigInt(balance)) / 1e18).toFixed(2);
  };

  const columns = [
    { title: 'ID', dataIndex: 'userId', width: '80px' },
    { title: '用户名', dataIndex: 'username' },
    { title: '邮箱', dataIndex: 'email' },
    { title: '学校', dataIndex: 'school' },
    {
      title: '状态',
      dataIndex: 'status',
      render: (status) => <StatusBadge status={status}>{status === 1 ? '正常' : '禁用'}</StatusBadge>,
    },
    {
      title: '角色',
      dataIndex: 'role',
      render: (role) => (
        <RoleBadge role={role}>
          {role === 'super_admin' ? '超级管理员' : role === 'admin' ? '管理员' : '用户'}
        </RoleBadge>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'gmtCreate',
      render: (time) => time ? new Date(time).toLocaleDateString() : '-',
    },
    {
      title: '操作',
      render: (_, record) => (
        <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
          <ActionButton variant="secondary" size="sm" onClick={() => openDetail(record)}>
            详情
          </ActionButton>
          <ActionButton variant="secondary" size="sm" onClick={() => openEdit(record)}>
            编辑
          </ActionButton>
          <ActionButton variant="secondary" size="sm" onClick={() => openResetPwd(record)}>
            重置密码
          </ActionButton>
          {record.status === 1 ? (
            <ActionButton variant="danger" size="sm" onClick={() => openModal('ban', record)}>
              封禁
            </ActionButton>
          ) : (
            <ActionButton variant="success" size="sm" onClick={() => openModal('unban', record)}>
              解封
            </ActionButton>
          )}
          {record.role === 'user' && (
            <ActionButton variant="primary" size="sm" onClick={() => openModal('upgrade', record)}>
              升级
            </ActionButton>
          )}
          {record.role === 'admin' && (
            <ActionButton variant="secondary" size="sm" onClick={() => openModal('downgrade', record)}>
              降级
            </ActionButton>
          )}
        </div>
      ),
    },
  ];

  return (
    <Container>
      {toast && <Toast>{toast}</Toast>}

      <Toolbar>
        <SearchInput
          placeholder="搜索用户名、邮箱..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
        />
        <FilterSelect value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="">全部状态</option>
          <option value="1">正常</option>
          <option value="2">禁用</option>
        </FilterSelect>
        <FilterSelect value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
          <option value="">全部角色</option>
          <option value="user">用户</option>
          <option value="admin">管理员</option>
          <option value="super_admin">超级管理员</option>
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

      {/* 操作确认弹窗 */}
      <Modal
        open={modalOpen}
        title="确认操作"
        onClose={closeModal}
        footer={
          <>
            <Button variant="secondary" onClick={closeModal}>取消</Button>
            <Button onClick={handleAction} disabled={processing}>
              {processing ? '处理中...' : '确定'}
            </Button>
          </>
        }
      >
        <p>
          {modalType === 'ban' && `确定要封禁用户 "${selectedUser?.username}" 吗？`}
          {modalType === 'unban' && `确定要解封用户 "${selectedUser?.username}" 吗？`}
          {modalType === 'upgrade' && `确定要将用户 "${selectedUser?.username}" 升级为管理员吗？`}
          {modalType === 'downgrade' && `确定要将用户 "${selectedUser?.username}" 降级为普通用户吗？`}
        </p>
      </Modal>

      {/* 用户详情弹窗 */}
      <Modal
        open={detailOpen}
        title="用户详情"
        onClose={() => setDetailOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => { setDetailOpen(false); openEdit(detailUser); }}>
              编辑信息
            </Button>
            <Button variant="outline" onClick={() => setDetailOpen(false)}>关闭</Button>
          </>
        }
      >
        {detailLoading ? (
          <div style={{ textAlign: 'center', color: '#94a3b8', padding: 20 }}>加载中...</div>
        ) : detailUser && (
          <div>
            <DetailGrid>
              <DetailItem>
                <DetailLabel>用户 ID</DetailLabel>
                <DetailValue>{detailUser.userId}</DetailValue>
              </DetailItem>
              <DetailItem>
                <DetailLabel>用户名</DetailLabel>
                <DetailValue>{detailUser.username}</DetailValue>
              </DetailItem>
              <DetailItem>
                <DetailLabel>邮箱</DetailLabel>
                <DetailValue>{detailUser.email || '-'}</DetailValue>
              </DetailItem>
              <DetailItem>
                <DetailLabel>学校</DetailLabel>
                <DetailValue>{detailUser.school || '-'}</DetailValue>
              </DetailItem>
              <DetailItem>
                <DetailLabel>专业</DetailLabel>
                <DetailValue>{detailUser.major || '-'}</DetailValue>
              </DetailItem>
              <DetailItem>
                <DetailLabel>角色</DetailLabel>
                <DetailValue>
                  <RoleBadge role={detailUser.role}>
                    {detailUser.role === 'super_admin' ? '超级管理员' : detailUser.role === 'admin' ? '管理员' : '用户'}
                  </RoleBadge>
                </DetailValue>
              </DetailItem>
              <DetailItem>
                <DetailLabel>状态</DetailLabel>
                <DetailValue>
                  <StatusBadge status={detailUser.status}>{detailUser.status === 1 ? '正常' : '禁用'}</StatusBadge>
                </DetailValue>
              </DetailItem>
              <DetailItem>
                <DetailLabel>注册时间</DetailLabel>
                <DetailValue>{detailUser.gmtCreate ? new Date(detailUser.gmtCreate).toLocaleString() : '-'}</DetailValue>
              </DetailItem>
            </DetailGrid>

            {detailUser.platformBalance != null && (
              <>
                <Divider />
                <DetailItem>
                  <DetailLabel>平台积分余额</DetailLabel>
                  <BalanceValue>{formatBalance(detailUser.platformBalance)} 积分</BalanceValue>
                </DetailItem>
              </>
            )}

            {detailUser.bio && (
              <>
                <Divider />
                <DetailItem>
                  <DetailLabel>个人简介</DetailLabel>
                  <DetailValue style={{ fontWeight: 400, marginTop: 4 }}>{detailUser.bio}</DetailValue>
                </DetailItem>
              </>
            )}
          </div>
        )}
      </Modal>

      {/* 编辑用户弹窗 */}
      <Modal
        open={editOpen}
        title="编辑用户信息"
        onClose={() => setEditOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setEditOpen(false)}>取消</Button>
            <Button variant="primary" onClick={handleEdit} disabled={processing}>
              {processing ? '保存中...' : '保存'}
            </Button>
          </>
        }
      >
        <FormGroup>
          <FormLabel>用户名</FormLabel>
          <Input
            value={editForm.username}
            onChange={(e) => setEditForm({ ...editForm, username: e.target.value })}
            placeholder="用户名"
          />
        </FormGroup>
        <FormGroup>
          <FormLabel>邮箱</FormLabel>
          <Input
            value={editForm.email}
            onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
            placeholder="邮箱"
          />
        </FormGroup>
        <FormGroup>
          <FormLabel>学校</FormLabel>
          <Input
            value={editForm.school}
            onChange={(e) => setEditForm({ ...editForm, school: e.target.value })}
            placeholder="学校"
          />
        </FormGroup>
        <FormGroup>
          <FormLabel>专业</FormLabel>
          <Input
            value={editForm.major}
            onChange={(e) => setEditForm({ ...editForm, major: e.target.value })}
            placeholder="专业"
          />
        </FormGroup>
      </Modal>

      {/* 重置密码弹窗 */}
      <Modal
        open={pwdOpen}
        title="重置用户密码"
        onClose={() => setPwdOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setPwdOpen(false)}>取消</Button>
            <Button variant="danger" onClick={handleResetPwd} disabled={processing}>
              {processing ? '处理中...' : '确认重置'}
            </Button>
          </>
        }
      >
        <FormGroup>
          <FormLabel>新密码（至少 6 位）</FormLabel>
          <Input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder="输入新密码"
          />
        </FormGroup>
        <p style={{ fontSize: 13, color: '#ef4444', margin: 0 }}>
          ⚠️ 重置后用户需使用新密码登录
        </p>
      </Modal>
    </Container>
  );
}

export default UsersPage;
