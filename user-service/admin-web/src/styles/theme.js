// 主题配置
export const theme = {
  colors: {
    primary: '#667eea',
    primaryDark: '#5a67d8',
    primaryLight: '#8b5cf6',
    secondary: '#764ba2',
    success: '#10b981',
    warning: '#f59e0b',
    danger: '#ef4444',
    info: '#3b82f6',

    background: '#f8fafc',
    backgroundDark: '#0f172a',

    text: '#1e293b',
    textSecondary: '#64748b',
    textLight: '#94a3b8',

    border: '#e2e8f0',
    borderDark: '#334155',

    white: '#ffffff',
    black: '#000000',

    // 状态颜色
    status: {
      normal: '#10b981',
      disabled: '#ef4444',
      frozen: '#f59e0b',
      active: '#3b82f6',
      completed: '#10b981',
      cancelled: '#6b7280',
    },
  },

  spacing: {
    xs: '4px',
    sm: '8px',
    md: '16px',
    lg: '24px',
    xl: '32px',
  },

  borderRadius: {
    sm: '4px',
    md: '8px',
    lg: '12px',
    xl: '16px',
    full: '9999px',
  },

  shadows: {
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
    xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1)',
  },

  breakpoints: {
    sm: '640px',
    md: '768px',
    lg: '1024px',
    xl: '1280px',
  },
};

export default theme;