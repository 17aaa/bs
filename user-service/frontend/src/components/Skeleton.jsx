import styled, { keyframes } from 'styled-components';

const shimmer = keyframes`
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
`;

const BaseSkeleton = styled.div`
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: ${shimmer} 1.5s infinite;
  border-radius: ${(props) => props.$radius || '4px'};
`;

export const SkeletonText = styled(BaseSkeleton)`
  height: ${(props) => props.$height || '16px'};
  width: ${(props) => props.$width || '100%'};
  margin-bottom: ${(props) => props.$mb || '8px'};
`;

export const SkeletonCircle = styled(BaseSkeleton)`
  width: ${(props) => props.$size || '40px'};
  height: ${(props) => props.$size || '40px'};
  border-radius: 50%;
`;

export const SkeletonImage = styled(BaseSkeleton)`
  width: 100%;
  height: ${(props) => props.$height || '200px'};
  border-radius: ${(props) => props.$radius || '8px'};
`;

export const SkeletonCard = styled.div`
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
`;

export const NftCardSkeleton = () => (
  <SkeletonCard>
    <SkeletonImage $height="180px" $mb="16px" />
    <SkeletonText $width="70%" $height="18px" $mb="8px" />
    <SkeletonText $width="50%" $height="14px" $mb="12px" />
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <SkeletonText $width="30%" $height="20px" />
      <SkeletonCircle $size="32px" />
    </div>
  </SkeletonCard>
);

export const UserCardSkeleton = () => (
  <SkeletonCard>
    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
      <SkeletonCircle $size="48px" />
      <div style={{ flex: 1 }}>
        <SkeletonText $width="60%" $height="16px" $mb="4px" />
        <SkeletonText $width="40%" $height="12px" />
      </div>
    </div>
    <SkeletonText $width="100%" $height="14px" $mb="8px" />
    <SkeletonText $width="80%" $height="14px" />
  </SkeletonCard>
);

export const TableRowSkeleton = () => (
  <div style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '16px 0', borderBottom: '1px solid #f0f0f0' }}>
    <SkeletonText $width="60px" $height="16px" />
    <SkeletonText $width="120px" $height="16px" />
    <SkeletonText $width="150px" $height="16px" />
    <SkeletonText $width="80px" $height="16px" />
    <SkeletonText $width="100px" $height="16px" />
  </div>
);

export default BaseSkeleton;