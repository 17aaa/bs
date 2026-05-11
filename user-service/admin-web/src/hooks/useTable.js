import { useState, useCallback, useEffect } from 'react';

function useTable(fetchFn, defaultPageSize = 10) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: defaultPageSize,
    total: 0,
  });
  const [filters, setFilters] = useState({});

  const fetchData = useCallback(async (params = {}) => {
    setLoading(true);
    try {
      const response = await fetchFn({
        pageNum: params.pageNum || pagination.current,
        pageSize: params.pageSize || pagination.pageSize,
        ...filters,
        ...params,
      });

      if (response.status === 'success' && response.data) {
        setData(response.data.records || []);
        setPagination((prev) => ({
          ...prev,
          current: params.pageNum || prev.current,
          pageSize: params.pageSize || prev.pageSize,
          total: response.data.total || 0,
        }));
      }
    } catch (error) {
      console.error('获取数据失败:', error);
    } finally {
      setLoading(false);
    }
  }, [fetchFn, filters, pagination.current, pagination.pageSize]);

  // 组件挂载时自动加载数据
  useEffect(() => {
    fetchData();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const changePage = useCallback((page) => {
    fetchData({ pageNum: page });
  }, [fetchData]);

  const changePageSize = useCallback((size) => {
    fetchData({ pageNum: 1, pageSize: size });
  }, [fetchData]);

  const setFilter = useCallback((newFilters) => {
    setFilters((prev) => ({ ...prev, ...newFilters }));
    fetchData({ pageNum: 1, ...newFilters });
  }, [fetchData]);

  const refresh = useCallback(() => {
    fetchData();
  }, [fetchData]);

  return {
    data,
    loading,
    pagination,
    filters,
    fetchData,
    changePage,
    changePageSize,
    setFilter,
    refresh,
  };
}

export default useTable;