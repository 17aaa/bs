/**
 * 安全工具函数
 * 防止XSS攻击、输入验证等
 */

/**
 * HTML实体编码
 * 防止XSS攻击
 */
export const escapeHtml = (str) => {
  if (typeof str !== 'string') return str;

  const htmlEntities = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  };

  return str.replace(/[&<>"']/g, (char) => htmlEntities[char]);
};

/**
 * 解码HTML实体
 */
export const unescapeHtml = (str) => {
  if (typeof str !== 'string') return str;

  const htmlEntities = {
    '&amp;': '&',
    '&lt;': '<',
    '&gt;': '>',
    '&quot;': '"',
    '&#39;': "'",
  };

  return str.replace(/&(amp|lt|gt|quot|#39);/g, (entity) => htmlEntities[entity]);
};

/**
 * 清理HTML标签
 * 移除所有HTML标签，只保留纯文本
 */
export const stripHtml = (str) => {
  if (typeof str !== 'string') return str;
  return str.replace(/<[^>]*>/g, '');
};

/**
 * 验证以太坊地址
 */
export const isValidEthAddress = (address) => {
  if (!address) return false;
  return /^0x[a-fA-F0-9]{40}$/.test(address);
};

/**
 * 验证交易哈希
 */
export const isValidTxHash = (hash) => {
  if (!hash) return false;
  return /^0x[a-fA-F0-9]{64}$/.test(hash);
};

/**
 * 验证URL
 */
export const isValidUrl = (url) => {
  if (!url) return false;
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

/**
 * 验证邮箱
 */
export const isValidEmail = (email) => {
  if (!email) return false;
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

/**
 * 验证用户名
 * 只允许字母、数字、下划线，长度3-20
 */
export const isValidUsername = (username) => {
  if (!username) return false;
  return /^[a-zA-Z0-9_]{3,20}$/.test(username);
};

/**
 * 验证密码强度
 * 至少8位，包含大小写字母和数字
 */
export const isStrongPassword = (password) => {
  if (!password) return false;
  return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/.test(password);
};

/**
 * 获取密码强度等级
 */
export const getPasswordStrength = (password) => {
  if (!password) return { level: 0, label: '无', color: '#94a3b8' };

  let score = 0;
  if (password.length >= 8) score++;
  if (password.length >= 12) score++;
  if (/[a-z]/.test(password)) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/\d/.test(password)) score++;
  if (/[@$!%*?&]/.test(password)) score++;

  if (score <= 2) return { level: 1, label: '弱', color: '#ef4444' };
  if (score <= 4) return { level: 2, label: '中', color: '#f59e0b' };
  return { level: 3, label: '强', color: '#10b981' };
};

/**
 * 防抖函数
 */
export const debounce = (func, wait) => {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
};

/**
 * 节流函数
 */
export const throttle = (func, limit) => {
  let inThrottle;
  return function executedFunction(...args) {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), limit);
    }
  };
};

/**
 * 格式化地址（隐藏中间部分）
 */
export const formatAddress = (address, start = 6, end = 4) => {
  if (!address) return '';
  if (address.length <= start + end) return address;
  return `${address.slice(0, start)}...${address.slice(-end)}`;
};

/**
 * 格式化哈希（隐藏中间部分）
 */
export const formatHash = (hash, start = 10, end = 8) => {
  if (!hash) return '';
  if (hash.length <= start + end) return hash;
  return `${hash.slice(0, start)}...${hash.slice(-end)}`;
};

/**
 * 复制到剪贴板
 */
export const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    // 降级方案
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    const success = document.execCommand('copy');
    document.body.removeChild(textarea);
    return success;
  }
};