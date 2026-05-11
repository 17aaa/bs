import { useState, useEffect, useRef } from 'react';
import styled from 'styled-components';

const ImageContainer = styled.div`
  position: relative;
  width: 100%;
  height: ${(props) => props.$height || '200px'};
  background: #f5f7fa;
  border-radius: ${(props) => props.$radius || '8px'};
  overflow: hidden;
`;

const Placeholder = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  color: #94a3b8;
  font-size: 14px;
`;

const StyledImage = styled.img`
  width: 100%;
  height: 100%;
  object-fit: ${(props) => props.$fit || 'cover'};
  opacity: ${(props) => (props.$loaded ? 1 : 0)};
  transition: opacity 0.3s ease;
`;

const Skeleton = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;

  @keyframes shimmer {
    0% {
      background-position: 200% 0;
    }
    100% {
      background-position: -200% 0;
    }
  }
`;

function LazyImage({ src, alt, height, radius, fit, placeholder = '🖼️' }) {
  const [loaded, setLoaded] = useState(false);
  const [inView, setInView] = useState(false);
  const [error, setError] = useState(false);
  const imgRef = useRef(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setInView(true);
          observer.disconnect();
        }
      },
      { threshold: 0.1, rootMargin: '50px' }
    );

    if (imgRef.current) {
      observer.observe(imgRef.current);
    }

    return () => observer.disconnect();
  }, []);

  const handleLoad = () => {
    setLoaded(true);
  };

  const handleError = () => {
    setError(true);
    setLoaded(true);
  };

  return (
    <ImageContainer ref={imgRef} $height={height} $radius={radius}>
      {!inView && (
        <Skeleton />
      )}
      {inView && !loaded && !error && (
        <Skeleton />
      )}
      {inView && error && (
        <Placeholder>
          <span>{placeholder}</span>
        </Placeholder>
      )}
      {inView && (
        <StyledImage
          src={src}
          alt={alt}
          $fit={fit}
          $loaded={loaded && !error}
          onLoad={handleLoad}
          onError={handleError}
          loading="lazy"
        />
      )}
    </ImageContainer>
  );
}

export default LazyImage;