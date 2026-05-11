import styled from 'styled-components';

const InputWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 4px;
`;

const Label = styled.label`
  font-size: 14px;
  font-weight: 500;
  color: #374151;
`;

const StyledInput = styled.input`
  padding: 10px 14px;
  font-size: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #1e293b;
  transition: all 0.2s ease;
  width: 100%;
  box-sizing: border-box;

  &:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }

  &::placeholder {
    color: #94a3b8;
  }

  &:disabled {
    background: #f8fafc;
    cursor: not-allowed;
  }

  ${(props) =>
    props.error &&
    `
    border-color: #ef4444;
    &:focus {
      border-color: #ef4444;
      box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
    }
  `}
`;

const ErrorMessage = styled.span`
  font-size: 12px;
  color: #ef4444;
`;

function Input({ label, error, ...props }) {
  return (
    <InputWrapper>
      {label && <Label>{label}</Label>}
      <StyledInput error={error} {...props} />
      {error && <ErrorMessage>{error}</ErrorMessage>}
    </InputWrapper>
  );
}

export default Input;