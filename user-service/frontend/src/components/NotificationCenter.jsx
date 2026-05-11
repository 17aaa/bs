import { useState, useEffect } from 'react';
import styled from 'styled-components';
import axios from 'axios';

const Container = styled.div`
  position: relative;
`;

const BellButton = styled.button`
  background: none;
  border: none;
  cursor: pointer;
  position: relative;
  padding: 8px;
  font-size: 20px;
  color: #666;
  transition: color 0.2s;

  &:hover {
    color: #667eea;
  }
`;

const Badge = styled.span`
  position: absolute;
  top: 2px;
  right: 2px;
  background: #ef4444;
  color: white;
  font-size: 10px;
  font-weight: bold;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
`;

const Dropdown = styled.div`
  position: absolute;
  top: 100%;
  right: 0;
  width: 360px;
  max-height: 480px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
  display: ${(props) => (props.$show ? 'block' : 'none')};
`;

const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
`;

const Title = styled.h3`
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
`;

const MarkAllBtn = styled.button`
  background: none;
  border: none;
  color: #667eea;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;

  &:hover {
    background: #f0f4ff;
  }
`;

const List = styled.div`
  max-height: 380px;
  overflow-y: auto;
`;

const EmptyState = styled.div`
  padding: 40px 20px;
  text-align: center;
  color: #94a3b8;
`;

const Item = styled.div`
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  background: ${(props) => (props.$unread ? '#f8faff' : 'white')};
  transition: background 0.2s;

  &:hover {
    background: #f5f7fa;
  }

  &:last-child {
    border-bottom: none;
  }
`;

const ItemHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
`;

const ItemType = styled.span`
  font-size: 13px;
  font-weight: 600;
  color: ${(props) => {
    switch (props.$type) {
      case 'purchase': return '#10b981';
      case 'sale': return '#667eea';
      case 'royalty': return '#f59e0b';
      case 'bid': return '#3b82f6';
      default: return '#64748b';
    }
  }};
`;

const ItemTime = styled.span`
  font-size: 11px;
  color: #94a3b8;
`;

const ItemContent = styled.p`
  margin: 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
`;

const ItemAmount = styled.span`
  font-weight: 600;
  color: #1e293b;
`;

const LoadingState = styled.div`
  padding: 40px 20px;
  text-align: center;
  color: #94a3b8;
`;

function NotificationCenter() {
  const [show, setShow] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchNotifications();
    fetchUnreadCount();
  }, []);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get('http://localhost:8085/api/notifications', {
        headers: { Authorization: `Bearer ${token}` },
        params: { page: 1, size: 20 }
      });
      if (response.data?.status === 'success') {
        setNotifications(response.data.data || []);
      }
    } catch (error) {
      console.error('获取通知失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchUnreadCount = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get('http://localhost:8085/api/notifications/unread-count', {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (response.data?.status === 'success') {
        setUnreadCount(response.data.data || 0);
      }
    } catch (error) {
      console.error('获取未读数量失败:', error);
    }
  };

  const markAllAsRead = async () => {
    try {
      const token = localStorage.getItem('token');
      await axios.post('http://localhost:8085/api/notifications/read-all', {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setUnreadCount(0);
      setNotifications(notifications.map(n => ({ ...n, isRead: true })));
    } catch (error) {
      console.error('标记已读失败:', error);
    }
  };

  const getTypeLabel = (type) => {
    switch (type) {
      case 'purchase': return '购买成功';
      case 'sale': return '出售成功';
      case 'royalty': return '版税收入';
      case 'bid': return '新出价';
      case 'transfer': return 'NFT转移';
      default: return '通知';
    }
  };

  const getTypeIcon = (type) => {
    switch (type) {
      case 'purchase': return '🛒';
      case 'sale': return '💰';
      case 'royalty': return '💎';
      case 'bid': return '📈';
      case 'transfer': return '🎁';
      default: return '🔔';
    }
  };

  return (
    <Container>
      <BellButton onClick={() => setShow(!show)}>
        🔔
        {unreadCount > 0 && <Badge>{unreadCount > 99 ? '99+' : unreadCount}</Badge>}
      </BellButton>

      <Dropdown $show={show}>
        <Header>
          <Title>通知中心</Title>
          {unreadCount > 0 && (
            <MarkAllBtn onClick={markAllAsRead}>全部已读</MarkAllBtn>
          )}
        </Header>

        <List>
          {loading ? (
            <LoadingState>加载中...</LoadingState>
          ) : notifications.length === 0 ? (
            <EmptyState>暂无通知</EmptyState>
          ) : (
            notifications.map((notification) => (
              <Item key={notification.id} $unread={!notification.isRead}>
                <ItemHeader>
                  <ItemType $type={notification.type}>
                    {getTypeIcon(notification.type)} {getTypeLabel(notification.type)}
                  </ItemType>
                  <ItemTime>{notification.timeAgo}</ItemTime>
                </ItemHeader>
                <ItemContent>
                  {notification.nftName && `${notification.nftName} - `}
                  {notification.amountFormatted && (
                    <ItemAmount>{notification.amountFormatted}</ItemAmount>
                  )}
                </ItemContent>
              </Item>
            ))
          )}
        </List>
      </Dropdown>
    </Container>
  );
}

export default NotificationCenter;