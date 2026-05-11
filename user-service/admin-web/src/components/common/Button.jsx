import styled from 'styled-components';

const StyledButton = styled.button`
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: ${(props) => {
    switch (props.size) {
      case 'sm': return '6px 12px';
      case 'lg': return '12px 24px';
      default: return '8px 16px';
    }
  }};
  font-size: ${(props) => {
    switch (props.size) {
      case 'sm': return '12px';
      case 'lg': return '16px';
      default: return '14px';
    }
  }};
  font-weight: 500;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;

  ${(props) => {
    switch (props.variant) {
      case 'secondary':
        return `
          background: #f1f5f9;
          color: #475569;
          &:hover:not(:disabled) {
            background: #e2e8f0;
          }
        `;
      case 'danger':
        return `
          background: #ef4444;
          color: white;
          &:hover:not(:disabled) {
            background: #dc2626;
          }
        `;
      case 'success':
        return `
          background: #10b981;
          color: white;
          &:hover:not(:disabled) {
            background: #059669;
          }
        `;
      case 'warning':
        return `
          background: #f59e0b;
          color: white;
          &:hover:not(:disabled) {
            background: #d97706;
          }
        `;
      case 'outline':
        return `
          background: white;
          color: #667eea;
          border: 1px solid #667eea;
          &:hover:not(:disabled) {
            background: #f5f3ff;
          }
        `;
      default:
        return `
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          &:hover:not(:disabled) {
            opacity: 0.9;
            transform: translateY(-1px);
          }
        `;
    }
  }}

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

function Button({ children, variant = 'primary', size = 'md', disabled = false, onClick, type = 'button', ...props }) {
  return (
    <StyledButton
      variant={variant}
      size={size}
      disabled={disabled}
      onClick={onClick}
      type={type}
      {...props}
    >
      {children}
    </StyledButton>
  );
}

export default Button;