import { useState, useEffect } from 'react';
import styled from 'styled-components';
import { getOverview } from '../../api/dashboard';
import { getCategoryStats } from '../../api/nft';

const Container = styled.div``;

const StatsGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 28px;
`;

const StatCard = styled.div`
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
`;

const StatLabel = styled.div`
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
`;

const StatValue = styled.div`
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
`;

const StatChange = styled.div`
  font-size: 12px;
  margin-top: 8px;
  color: ${(props) => (props.$positive ? '#10b981' : '#ef4444')};
`;

const SectionTitle = styled.h2`
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
`;

const TwoCol = styled.div`
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 28px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
`;

const Panel = styled.div`
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
`;

const PanelTitle = styled.div`
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  border-bottom: 1px solid #f1f5f9;
`;

const ActivityItem = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid #f1f5f9;

  &:last-child {
    border-bottom: none;
  }
`;

const ActivityIcon = styled.span`
  font-size: 20px;
`;

const ActivityContent = styled.div`
  flex: 1;
`;

const ActivityDescription = styled.div`
  font-size: 14px;
  color: #1e293b;
`;

const ActivityTime = styled.div`
  font-size: 12px;
  color: #94a3b8;
`;

const CategoryBar = styled.div`
  padding: 12px 20px;
  border-bottom: 1px solid #f1f5f9;

  &:last-child {
    border-bottom: none;
  }
`;

const CategoryRow = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 14px;
`;

const BarTrack = styled.div`
  background: #f1f5f9;
  border-radius: 4px;
  height: 6px;
  overflow: hidden;
`;

const BarFill = styled.div`
  height: 100%;
  border-radius: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  width: ${(p) => p.pct}%;
  transition: width 0.6s ease;
`;

const EmptyMsg = styled.div`
  padding: 24px 20px;
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
`;

function DashboardPage() {
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [categoryStats, setCategoryStats] = useState([]);

  useEffect(() => {
    fetchOverview();
    fetchCategoryStats();
  }, []);

  const fetchOverview = async () => {
    try {
      const response = await getOverview();
      if (response.status === 'success') {
        setOverview(response.data);
      }
    } catch (error) {
      console.error('获取概览数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategoryStats = async () => {
    try {
      const res = await getCategoryStats();
      if (res.status === 'success' && Array.isArray(res.data)) {
        setCategoryStats(res.data);
      }
    } catch {
      // 静默失败
    }
  };

  const formatCredits = (volume) => {
    if (!volume) return '0';
    return (Number(BigInt(volume)) / 1e18).toFixed(2);
  };

  const maxCategory = Math.max(...categoryStats.map((c) => c.count || 0), 1);

  if (loading) {
    return <Container style={{ color: '#94a3b8', padding: 40, textAlign: 'center' }}>加载中...</Container>;
  }

  return (
    <Container>
      <StatsGrid>
        <StatCard>
          <StatLabel>总用户数</StatLabel>
          <StatValue>{overview?.totalUsers || 0}</StatValue>
          <StatChange $positive>今日新增 {overview?.todayNewUsers || 0}</StatChange>
        </StatCard>

        <StatCard>
          <StatLabel>NFT 总数</StatLabel>
          <StatValue>{overview?.totalNfts || 0}</StatValue>
          <StatChange $positive>今日新增 {overview?.todayNewNfts || 0}</StatChange>
        </StatCard>

        <StatCard>
          <StatLabel>总交易额（积分）</StatLabel>
          <StatValue style={{ fontSize: 22 }}>{formatCredits(overview?.totalTradeVolume)}</StatValue>
          <StatChange $positive>今日 {formatCredits(overview?.todayTradeVolume)}</StatChange>
        </StatCard>

        <StatCard>
          <StatLabel>总交易次数</StatLabel>
          <StatValue>{overview?.totalTrades || 0}</StatValue>
          <StatChange $positive>今日 {overview?.todayTrades || 0}</StatChange>
        </StatCard>

        <StatCard>
          <StatLabel>粉丝代币</StatLabel>
          <StatValue>{overview?.totalFanTokens || 0}</StatValue>
        </StatCard>
      </StatsGrid>

      <TwoCol>
        {/* 最近活动 */}
        <Panel>
          <PanelTitle>最近活动</PanelTitle>
          {overview?.recentActivities?.length > 0 ? (
            overview.recentActivities.map((activity, index) => (
              <ActivityItem key={index}>
                <ActivityIcon>
                  {activity.type === 'register' && '👤'}
                  {activity.type === 'mint' && '🎨'}
                  {activity.type === 'trade' && '💰'}
                  {!['register', 'mint', 'trade'].includes(activity.type) && '📌'}
                </ActivityIcon>
                <ActivityContent>
                  <ActivityDescription>{activity.description}</ActivityDescription>
                  {activity.username && (
                    <ActivityTime>用户: {activity.username}</ActivityTime>
                  )}
                </ActivityContent>
                <ActivityTime>{activity.time}</ActivityTime>
              </ActivityItem>
            ))
          ) : (
            <EmptyMsg>暂无最近活动</EmptyMsg>
          )}
        </Panel>

        {/* NFT 分类分布 */}
        <Panel>
          <PanelTitle>NFT 分类分布</PanelTitle>
          {categoryStats.length > 0 ? (
            categoryStats.map((cat) => (
              <CategoryBar key={cat.category || cat.name}>
                <CategoryRow>
                  <span style={{ color: '#1e293b', fontWeight: 500 }}>{cat.category || cat.name}</span>
                  <span style={{ color: '#64748b', fontSize: 13 }}>{cat.count}</span>
                </CategoryRow>
                <BarTrack>
                  <BarFill pct={Math.round((cat.count / maxCategory) * 100)} />
                </BarTrack>
              </CategoryBar>
            ))
          ) : (
            <EmptyMsg>暂无分类数据</EmptyMsg>
          )}
        </Panel>
      </TwoCol>
    </Container>
  );
}

export default DashboardPage;
