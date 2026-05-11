import styled from 'styled-components';

const TableWrapper = styled.div`
  overflow-x: auto;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
`;

const StyledTable = styled.table`
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
`;

const Thead = styled.thead`
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
`;

const Th = styled.th`
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #475569;
  white-space: nowrap;
`;

const Tbody = styled.tbody``;

const Tr = styled.tr`
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.2s ease;

  &:hover {
    background: #f8fafc;
  }

  &:last-child {
    border-bottom: none;
  }
`;

const Td = styled.td`
  padding: 12px 16px;
  color: #1e293b;
`;

const EmptyRow = styled.tr`
  td {
    text-align: center;
    color: #94a3b8;
    padding: 40px;
  }
`;

function Table({ columns, data, onRowClick, emptyText = '暂无数据' }) {
  return (
    <TableWrapper>
      <StyledTable>
        <Thead>
          <tr>
            {columns.map((col, index) => (
              <Th key={index} style={{ width: col.width }}>
                {col.title}
              </Th>
            ))}
          </tr>
        </Thead>
        <Tbody>
          {data.length === 0 ? (
            <EmptyRow>
              <Td colSpan={columns.length}>{emptyText}</Td>
            </EmptyRow>
          ) : (
            data.map((row, rowIndex) => (
              <Tr
                key={rowIndex}
                onClick={() => onRowClick?.(row)}
                style={{ cursor: onRowClick ? 'pointer' : 'default' }}
              >
                {columns.map((col, colIndex) => (
                  <Td key={colIndex}>
                    {col.render ? col.render(row[col.dataIndex], row, rowIndex) : row[col.dataIndex]}
                  </Td>
                ))}
              </Tr>
            ))
          )}
        </Tbody>
      </StyledTable>
    </TableWrapper>
  );
}

export default Table;