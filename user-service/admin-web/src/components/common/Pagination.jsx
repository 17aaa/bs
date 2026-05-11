import styled from 'styled-components';

const PaginationWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 0;
`;

const PageButton = styled.button`
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  font-size: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover:not(:disabled) {
    border-color: #667eea;
    color: #667eea;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  ${(props) =>
    props.$active &&
    `
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-color: transparent;
    color: white;
  `}
`;

const PageInfo = styled.span`
  font-size: 14px;
  color: #64748b;
  margin: 0 8px;
`;

function Pagination({ current, pageSize, total, onChange }) {
  const totalPages = Math.ceil(total / pageSize);

  const getPageNumbers = () => {
    const pages = [];
    const maxVisible = 5;

    if (totalPages <= maxVisible) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (current <= 3) {
        for (let i = 1; i <= 4; i++) {
          pages.push(i);
        }
        pages.push('...');
        pages.push(totalPages);
      } else if (current >= totalPages - 2) {
        pages.push(1);
        pages.push('...');
        for (let i = totalPages - 3; i <= totalPages; i++) {
          pages.push(i);
        }
      } else {
        pages.push(1);
        pages.push('...');
        pages.push(current - 1);
        pages.push(current);
        pages.push(current + 1);
        pages.push('...');
        pages.push(totalPages);
      }
    }

    return pages;
  };

  if (totalPages <= 1) return null;

  return (
    <PaginationWrapper>
      <PageButton disabled={current === 1} onClick={() => onChange(current - 1)}>
        上一页
      </PageButton>

      {getPageNumbers().map((page, index) => (
        <PageButton
          key={index}
          $active={page === current}
          disabled={page === '...'}
          onClick={() => typeof page === 'number' && onChange(page)}
        >
          {page}
        </PageButton>
      ))}

      <PageButton disabled={current === totalPages} onClick={() => onChange(current + 1)}>
        下一页
      </PageButton>

      <PageInfo>
        共 {total} 条
      </PageInfo>
    </PaginationWrapper>
  );
}

export default Pagination;