/**
 * 性能监控工具
 */

class PerformanceMonitor {
  constructor() {
    this.metrics = new Map();
    this.enabled = process.env.NODE_ENV === 'development';
  }

  /**
   * 开始计时
   */
  start(label) {
    if (!this.enabled) return;
    this.metrics.set(label, {
      start: performance.now(),
      end: null,
      duration: null,
    });
  }

  /**
   * 结束计时
   */
  end(label) {
    if (!this.enabled) return;

    const metric = this.metrics.get(label);
    if (!metric) {
      console.warn(`[Performance] No metric found for: ${label}`);
      return;
    }

    metric.end = performance.now();
    metric.duration = metric.end - metric.start;

    console.log(`[Performance] ${label}: ${metric.duration.toFixed(2)}ms`);
    return metric.duration;
  }

  /**
   * 测量异步函数执行时间
   */
  async measure(label, fn) {
    this.start(label);
    try {
      const result = await fn();
      this.end(label);
      return result;
    } catch (error) {
      this.end(label);
      throw error;
    }
  }

  /**
   * 获取页面加载性能指标
   */
  getPageMetrics() {
    if (typeof window === 'undefined' || !window.performance) {
      return null;
    }

    const timing = performance.timing;
    const metrics = {
      // DNS查询时间
      dns: timing.domainLookupEnd - timing.domainLookupStart,
      // TCP连接时间
      tcp: timing.connectEnd - timing.connectStart,
      // 请求响应时间
      request: timing.responseEnd - timing.requestStart,
      // DOM解析时间
      domParse: timing.domInteractive - timing.responseEnd,
      // DOM渲染时间
      domRender: timing.domComplete - timing.domInteractive,
      // 页面加载总时间
      total: timing.loadEventEnd - timing.navigationStart,
    };

    return metrics;
  }

  /**
   * 获取内存使用情况
   */
  getMemoryUsage() {
    if (typeof window === 'undefined' || !window.performance?.memory) {
      return null;
    }

    const memory = performance.memory;
    return {
      usedJSHeapSize: (memory.usedJSHeapSize / 1024 / 1024).toFixed(2) + ' MB',
      totalJSHeapSize: (memory.totalJSHeapSize / 1024 / 1024).toFixed(2) + ' MB',
      jsHeapSizeLimit: (memory.jsHeapSizeLimit / 1024 / 1024).toFixed(2) + ' MB',
    };
  }

  /**
   * 获取Web Vitals指标
   */
  getWebVitals() {
    return new Promise((resolve) => {
      const vitals = {};

      // First Contentful Paint (FCP)
      const fcpEntry = performance.getEntriesByName('first-contentful-paint')[0];
      if (fcpEntry) {
        vitals.fcp = fcpEntry.startTime;
      }

      // Largest Contentful Paint (LCP)
      if ('PerformanceObserver' in window) {
        try {
          const lcpObserver = new PerformanceObserver((list) => {
            const entries = list.getEntries();
            const lastEntry = entries[entries.length - 1];
            vitals.lcp = lastEntry.startTime;
          });
          lcpObserver.observe({ type: 'largest-contentful-paint', buffered: true });
        } catch (e) {
          // LCP not supported
        }
      }

      // First Input Delay (FID)
      if ('PerformanceObserver' in window) {
        try {
          const fidObserver = new PerformanceObserver((list) => {
            const entries = list.getEntries();
            entries.forEach((entry) => {
              vitals.fid = entry.processingStart - entry.startTime;
            });
          });
          fidObserver.observe({ type: 'first-input', buffered: true });
        } catch (e) {
          // FID not supported
        }
      }

      resolve(vitals);
    });
  }

  /**
   * 打印性能报告
   */
  async printReport() {
    if (!this.enabled) return;

    console.group('📊 Performance Report');

    // 页面加载指标
    const pageMetrics = this.getPageMetrics();
    if (pageMetrics) {
      console.group('Page Load Metrics');
      console.table(pageMetrics);
      console.groupEnd();
    }

    // 内存使用
    const memory = this.getMemoryUsage();
    if (memory) {
      console.group('Memory Usage');
      console.table(memory);
      console.groupEnd();
    }

    // Web Vitals
    const vitals = await this.getWebVitals();
    if (Object.keys(vitals).length > 0) {
      console.group('Web Vitals');
      console.table(vitals);
      console.groupEnd();
    }

    // 自定义指标
    if (this.metrics.size > 0) {
      console.group('Custom Metrics');
      const customMetrics = {};
      this.metrics.forEach((value, key) => {
        if (value.duration !== null) {
          customMetrics[key] = value.duration.toFixed(2) + 'ms';
        }
      });
      console.table(customMetrics);
      console.groupEnd();
    }

    console.groupEnd();
  }
}

// 单例实例
const perfMonitor = new PerformanceMonitor();

export default perfMonitor;

// 便捷方法
export const measurePerformance = (label, fn) => perfMonitor.measure(label, fn);
export const startMeasure = (label) => perfMonitor.start(label);
export const endMeasure = (label) => perfMonitor.end(label);
export const printPerformanceReport = () => perfMonitor.printReport();