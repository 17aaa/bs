import { useEffect, useState } from 'react';
import styled from 'styled-components';

const NotificationContainer = styled.div`
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  pointer-events: none;
`;

const NotificationItem = styled.div`
  pointer-events: auto;
  min-width: 300px;
  max-width: 500px;
  padding: 16px 20px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 12px;
  animation: slideIn 0.3s ease;
  border-left: 4px solid ${(props) => {
    switch (props.type) {
      case 'success':
        return '#4CAF50';
      case 'error':
        return '#f44336';
      case 'warning':
        return '#ff9800';
      default:
        return '#2196F3';
    }
  }};

  @keyframes slideIn {
    from {
      transform: translateX(100%);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }
`;

const Icon = styled.span`
  font-size: 20px;
`;

const Message = styled.span`
  flex: 1;
  font-size: 14px;
  color: #333;
`;

const CloseButton = styled.button`
  background: none;
  border: none;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;

  &:hover {
    color: #333;
  }
`;

// 通知类型图标
const icons = {
  success: '✓',
  error: '✕',
  warning: '⚠',
  info: 'ℹ',
};

function Notification({ message, type = 'info', onClose }) {
  useEffect(() => {
    const timer = setTimeout(() => {
      onClose();
    }, 5000);
    return () => clearTimeout(timer);
  }, [onClose]);

  return (
    <NotificationItem type={type}>
      <Icon>{icons[type]}</Icon>
      <Message>{message}</Message>
      <CloseButton onClick={onClose}>×</CloseButton>
    </NotificationItem>
  );
}

// 通知管理器
let notificationCallback = null;

export const notify = {
  success: (message) => {
    notificationCallback?.({ message, type: 'success' });
  },
  error: (message) => {
    notificationCallback?.({ message, type: 'error' });
  },
  warning: (message) => {
    notificationCallback?.({ message, type: 'warning' });
  },
  info: (message) => {
    notificationCallback?.({ message, type: 'info' });
  },
};

export function NotificationManager() {
  const [notification, setNotification] = useState(null);

  useEffect(() => {
    notificationCallback = (notif) => {
      setNotification(notif);
    };
  }, []);

  const handleClose = () => {
    setNotification(null);
  };

  if (!notification) return null;

  return (
    <NotificationContainer>
      <Notification
        message={notification.message}
        type={notification.type}
        onClose={handleClose}
      />
    </NotificationContainer>
  );
}

export default Notification;