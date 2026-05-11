import { useState } from 'react';
import styled from 'styled-components';

const Card = styled.div`
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  cursor: pointer;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
    border-color: #667eea;
  }
`;

const CardImage = styled.div`
  width: 100%;
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 48px;
  overflow: hidden;
  position: relative;

  &:hover {
    .card-emoji {
      transform: scale(1.1);
    }
  }
`;

const CardImageActual = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
  transition: transform 0.3s ease;

  ${Card}:hover & {
    transform: scale(1.05);
  }
`;

const ImageLoadingOverlay = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
`;

const ImageLoadingSpinner = styled.div`
  width: 32px;
  height: 32px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }
`;

const CardContent = styled.div`
  padding: 16px;
`;

const CardTitle = styled.h3`
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #333;
`;

const CardDescription = styled.p`
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
`;

const CardMeta = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
`;

const CardBadge = styled.span`
  padding: 4px 8px;
  border-radius: 4px;
  background: ${(props) => (props.$primary ? '#e3f2fd' : '#f5f5f5')};
  color: ${(props) => (props.$primary ? '#1976d2' : '#666')};
`;

const AuctionBadge = styled.span`
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  background: ${(p) => p.$dutch ? 'linear-gradient(135deg,#f59e0b,#ef4444)' : 'linear-gradient(135deg,#667eea,#764ba2)'};
  color: white;
  z-index: 2;
`;

const PriceRow = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
`;

const PriceLabel = styled.span`
  font-size: 11px;
  color: #94a3b8;
  text-transform: uppercase;
`;

const PriceValue = styled.span`
  font-size: 15px;
  font-weight: 700;
  color: #667eea;
`;

const CardAction = styled.button`
  width: 100%;
  margin-top: 12px;
  padding: 10px;
  border: none;
  border-radius: 8px;
  background: ${(props) => (props.$primary ? '#2196F3' : '#f5f5f5')};
  color: ${(props) => (props.$primary ? 'white' : '#333')};
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    opacity: 0.9;
  }
`;

function NFTCard({ nft, onViewDetails, onBuy, onSell, style, buying = false }) {
  const [imageLoaded, setImageLoaded] = useState(false);
  const [imageError, setImageError] = useState(false);

  const hasImage = nft.imageUrl || (nft.metadataUrl && nft.metadataUrl.match(/\.(jpg|jpeg|png|gif|webp)$/i));
  const showImage = hasImage && !imageError;

  // orderType: 1=固定价格, 2=荷兰拍卖
  const isDutch = nft.orderType === 2;
  const priceEth = nft.price ? (Number(nft.price) / 1e18).toFixed(4) : null;

  return (
    <Card style={style} onClick={() => onViewDetails?.(nft.id)}>
      <CardImage>
        {nft.orderType && (
          <AuctionBadge $dutch={isDutch}>
            {isDutch ? '荷兰拍卖' : '固定价格'}
          </AuctionBadge>
        )}
        {showImage ? (
          <>
            {!imageLoaded && (
              <ImageLoadingOverlay>
                <ImageLoadingSpinner />
              </ImageLoadingOverlay>
            )}
            <CardImageActual
              src={nft.imageUrl || nft.metadataUrl}
              alt={nft.name}
              onLoad={() => setImageLoaded(true)}
              onError={() => {
                setImageError(true);
                setImageLoaded(false);
              }}
            />
          </>
        ) : (
          <span className="card-emoji" style={{ transition: 'transform 0.3s ease' }}>🎨</span>
        )}
      </CardImage>
      <CardContent>
        <CardTitle>{nft.name}</CardTitle>
        <CardDescription>{nft.description}</CardDescription>
        {priceEth && (
          <PriceRow>
            <PriceLabel>{isDutch ? '当前价格' : '售价'}</PriceLabel>
            <PriceValue>{priceEth} 积分</PriceValue>
          </PriceRow>
        )}
        <CardMeta style={{ marginTop: '10px' }}>
          <CardBadge $primary>{nft.category || 'Art'}</CardBadge>
          <span>版本：{nft.currentVersion}</span>
        </CardMeta>
        <CardMeta style={{ marginTop: '8px' }}>
          <span>ID: {nft.tokenId}</span>
          <span>版税：{nft.royaltyFee != null ? (nft.royaltyFee / 100).toFixed(2) : '5.00'}%</span>
        </CardMeta>
        {onViewDetails && (
          <CardAction $primary onClick={(e) => {
            e.stopPropagation();
            onViewDetails(nft.id);
          }}>
            查看详情
          </CardAction>
        )}
        {onSell && (
          <CardAction
            onClick={(e) => {
              e.stopPropagation();
              onSell(nft);
            }}
          >
            出售
          </CardAction>
        )}
        {onBuy && (
          <CardAction
            onClick={(e) => {
              e.stopPropagation();
              onBuy();
            }}
            disabled={buying}
            style={{ opacity: buying ? 0.6 : 1 }}
          >
            {buying ? '购买中...' : '购买'}
          </CardAction>
        )}
      </CardContent>
    </Card>
  );
}

export default NFTCard;