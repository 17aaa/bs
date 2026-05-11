/**
 * 简单的内存缓存工具
 * 用于缓存API响应，减少重复请求
 */

class CacheManager {
  constructor() {
    this.cache = new Map();
    this.defaultTTL = 5 * 60 * 1000; // 默认5分钟过期
  }

  /**
   * 设置缓存
   * @param {string} key 缓存键
   * @param {any} value 缓存值
   * @param {number} ttl 过期时间（毫秒）
   */
  set(key, value, ttl = this.defaultTTL) {
    this.cache.set(key, {
      value,
      expiry: Date.now() + ttl,
    });
  }

  /**
   * 获取缓存
   * @param {string} key 缓存键
   * @returns {any} 缓存值或null
   */
  get(key) {
    const item = this.cache.get(key);
    if (!item) return null;

    if (Date.now() > item.expiry) {
      this.cache.delete(key);
      return null;
    }

    return item.value;
  }

  /**
   * 删除缓存
   * @param {string} key 缓存键
   */
  delete(key) {
    this.cache.delete(key);
  }

  /**
   * 清空所有缓存
   */
  clear() {
    this.cache.clear();
  }

  /**
   * 清理过期缓存
   */
  cleanup() {
    const now = Date.now();
    for (const [key, item] of this.cache.entries()) {
      if (now > item.expiry) {
        this.cache.delete(key);
      }
    }
  }

  /**
   * 获取或设置缓存（如果不存在则调用fetcher获取）
   * @param {string} key 缓存键
   * @param {Function} fetcher 获取数据的函数
   * @param {number} ttl 过期时间
   */
  async getOrSet(key, fetcher, ttl = this.defaultTTL) {
    const cached = this.get(key);
    if (cached !== null) {
      return cached;
    }

    const value = await fetcher();
    this.set(key, value, ttl);
    return value;
  }
}

// 单例实例
const cache = new CacheManager();

// 定期清理过期缓存
setInterval(() => cache.cleanup(), 60 * 1000);

export default cache;

/**
 * 缓存装饰器
 * 用于缓存API请求
 */
export const withCache = (key, ttl) => {
  return (target, propertyKey, descriptor) => {
    const originalMethod = descriptor.value;
    descriptor.value = async function (...args) {
      const cacheKey = `${key}_${JSON.stringify(args)}`;
      return cache.getOrSet(cacheKey, () => originalMethod.apply(this, args), ttl);
    };
    return descriptor;
  };
};

/**
 * 常用缓存键
 */
export const CACHE_KEYS = {
  USER_INFO: 'user_info',
  NFT_LIST: 'nft_list',
  NFT_DETAIL: 'nft_detail',
  MARKET_ORDERS: 'market_orders',
  GAS_PRICE: 'gas_price',
  FAN_TOKENS: 'fan_tokens',
};

/**
 * 缓存时间常量
 */
export const CACHE_TTL = {
  SHORT: 30 * 1000,      // 30秒
  MEDIUM: 5 * 60 * 1000, // 5分钟
  LONG: 30 * 60 * 1000,  // 30分钟
};