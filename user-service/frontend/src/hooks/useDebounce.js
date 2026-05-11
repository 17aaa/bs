import { useState, useEffect } from 'react';

/**
 * 防抖 Hook
 * @param {any} value 需要防抖的值
 * @param {number} delay 延迟时间（毫秒）
 * @returns {any} 防抖后的值
 */
export function useDebounce(value, delay = 300) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(timer);
    };
  }, [value, delay]);

  return debouncedValue;
}

/**
 * 分页 Hook
 * @param {number} initialLimit 初始每页数量
 * @param {number} initialOffset 初始偏移量
 * @returns {Object} 分页状态和方法
 */
export function usePagination(initialLimit = 12, initialOffset = 0) {
  const [offset, setOffset] = useState(initialOffset);
  const [limit, setLimit] = useState(initialLimit);
  const [hasMore, setHasMore] = useState(true);

  const reset = () => {
    setOffset(initialOffset);
    setHasMore(true);
  };

  const loadMore = () => {
    setOffset((prev) => prev + limit);
  };

  const updateHasMore = (dataLength) => {
    setHasMore(dataLength === limit);
  };

  return {
    offset,
    limit,
    hasMore,
    reset,
    loadMore,
    updateHasMore,
    setOffset,
    setLimit,
    setHasMore,
  };
}

export default useDebounce;