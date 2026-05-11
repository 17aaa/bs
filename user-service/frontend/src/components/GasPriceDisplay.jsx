import { useState, useEffect } from 'react';
import styled from 'styled-components';
import axios from 'axios';

const Container = styled.div`
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
`;

const Title = styled.h4`
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 8px;
`;

const GasGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
`;

const GasCard = styled.div`
  background: white;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  border: 2px solid ${(props) => props.$active ? '#667eea' : 'transparent'};
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #667eea;
  }
`;

const GasLabel = styled.div`
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 4px;
`;

const GasValue = styled.div`
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
`;

const GasUnit = styled.span`
  font-size: 12px;
  color: #64748b;
  font-weight: normal;
`;

const GasTime = styled.div`
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
`;

const LoadingState = styled.div`
  text-align: center;
  color: #94a3b8;
  padding: 20px;
`;

const RefreshBtn = styled.button`
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;

  &:hover {
    background: #f0f4ff;
  }
`;

function GasPriceDisplay({ onSelect, selectedSpeed = 'standard' }) {
  const [gasPrice, setGasPrice] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchGasPrice();
    // 每30秒刷新一次
    const interval = setInterval(fetchGasPrice, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchGasPrice = async () => {
    try {
      const response = await axios.get('http://localhost:8085/api/blockchain/gas-price');
      if (response.data?.status === 'success') {
        setGasPrice(response.data.data);
      }
    } catch (error) {
      console.error('获取Gas价格失败:', error);
      // 使用模拟数据
      setGasPrice({
        slow: { price: 20, time: '~5分钟' },
        standard: { price: 30, time: '~2分钟' },
        fast: { price: 50, time: '~30秒' }
      });
    } finally {
      setLoading(false);
    }
  };

  const formatGwei = (wei) => {
    if (!wei) return '0';
    // Wei to Gwei (1 Gwei = 10^9 Wei)
    const gwei = Number(wei) / 1e9;
    return gwei.toFixed(1);
  };

  const handleSelect = (speed) => {
    onSelect?.(speed, gasPrice[speed]?.price);
  };

  if (loading) {
    return (
      <Container>
        <LoadingState>加载Gas价格...</LoadingState>
      </Container>
    );
  }

  if (!gasPrice) {
    return null;
  }

  return (
    <Container>
      <Title>
        ⛽ Gas费用估算
        <RefreshBtn onClick={fetchGasPrice}>刷新</RefreshBtn>
      </Title>
      <GasGrid>
        <GasCard
          $active={selectedSpeed === 'slow'}
          onClick={() => handleSelect('slow')}
        >
          <GasLabel>慢速</GasLabel>
          <GasValue>
            {formatGwei(gasPrice.slow?.price)}
            <GasUnit> Gwei</GasUnit>
          </GasValue>
          <GasTime>{gasPrice.slow?.time || '~5分钟'}</GasTime>
        </GasCard>

        <GasCard
          $active={selectedSpeed === 'standard'}
          onClick={() => handleSelect('standard')}
        >
          <GasLabel>标准</GasLabel>
          <GasValue>
            {formatGwei(gasPrice.standard?.price)}
            <GasUnit> Gwei</GasUnit>
          </GasValue>
          <GasTime>{gasPrice.standard?.time || '~2分钟'}</GasTime>
        </GasCard>

        <GasCard
          $active={selectedSpeed === 'fast'}
          onClick={() => handleSelect('fast')}
        >
          <GasLabel>快速</GasLabel>
          <GasValue>
            {formatGwei(gasPrice.fast?.price)}
            <GasUnit> Gwei</GasUnit>
          </GasValue>
          <GasTime>{gasPrice.fast?.time || '~30秒'}</GasTime>
        </GasCard>
      </GasGrid>
    </Container>
  );
}

export default GasPriceDisplay;