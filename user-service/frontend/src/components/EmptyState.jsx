import styled from 'styled-components';

const EmptyContainer = styled.div`
  text-align: center;
  padding: ${({ padding }) => padding || '80px 24px'};
  color: #999;
`;

const EmptyIcon = styled.div`
  font-size: ${({ iconSize }) => iconSize || '64px'};
  margin-bottom: 16px;
  opacity: 0.5;
`;

const EmptyTitle = styled.p`
  font-size: 16px;
  color: #666;
  margin: 0 0 8px 0;
`;

const EmptySubtitle = styled.p`
  font-size: 14px;
  color: #999;
  margin: 0;
`;

const ActionButton = styled.button`
  margin-top: 24px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
  }
`;

function EmptyState({
  icon = '📭',
  title = '暂无数据',
  subtitle = '',
  actionText,
  onAction,
  padding,
  iconSize,
}) {
  return (
    <EmptyContainer padding={padding}>
      <EmptyIcon iconSize={iconSize}>{icon}</EmptyIcon>
      {title && <EmptyTitle>{title}</EmptyTitle>}
      {subtitle && <EmptySubtitle>{subtitle}</EmptySubtitle>}
      {actionText && onAction && (
        <ActionButton onClick={onAction}>{actionText}</ActionButton>
      )}
    </EmptyContainer>
  );
}

export default EmptyState;