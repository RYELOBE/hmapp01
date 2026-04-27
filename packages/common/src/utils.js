/**
 * 通用工具函数
 */

/** 安全 JSON 解析 */
export function safeJsonParse(raw, fallback = null) {
  if (!raw) return fallback;
  try {
    return JSON.parse(raw);
  } catch {
    return fallback;
  }
}

/** 格式化价格 */
export function formatPrice(price) {
  if (price == null) return "¥0.00";
  return `¥${Number(price).toFixed(2)}`;
}

/** 生成简易唯一 ID（非 UUID，用于本地临时场景） */
export function simpleId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
}

/**
 * 防抖函数
 * @param {Function} fn 目标函数
 * @param {number} delay 延迟毫秒
 * @returns {Function}
 */
export function debounce(fn, delay = 300) {
  let timer = null;
  return function (...args) {
    clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}

/**
 * 节流函数
 * @param {Function} fn 目标函数
 * @param {number} interval 最小间隔毫秒
 * @returns {Function}
 */
export function throttle(fn, interval = 300) {
  let last = 0;
  return function (...args) {
    const now = Date.now();
    if (now - last >= interval) {
      last = now;
      fn.apply(this, args);
    }
  };
}

/**
 * 日期格式化
 * @param {Date|string|number} time 日期对象/时间戳/日期字符串
 * @param {string} format 格式模板，默认 '{y}-{m}-{d} {h}:{i}:{s}'
 * @returns {string}
 */
export function formatDate(time, format = '{y}-{m}-{d} {h}:{i}:{s}') {
  if (!time) return '';
  let date;
  if (typeof time === 'object') {
    date = time;
  } else {
    let t = typeof time === 'string' && /^-?\d+$/.test(time) ? parseInt(time, 10) : time;
    if (typeof t === 'number' && t.toString().length === 10) t *= 1000;
    date = new Date(t);
  }
  const formatObj = {
    y: date.getFullYear(),
    m: String(date.getMonth() + 1).padStart(2, '0'),
    d: String(date.getDate()).padStart(2, '0'),
    h: String(date.getHours()).padStart(2, '0'),
    i: String(date.getMinutes()).padStart(2, '0'),
    s: String(date.getSeconds()).padStart(2, '0'),
    a: ['日', '一', '二', '三', '四', '五', '六'][date.getDay()],
  };
  return format.replace(/{([ymdhisa])}/g, (_, key) => formatObj[key] || '');
}

/**
 * 深拷贝（简单版）
 * @param {*} source 源对象
 * @returns {*}
 */
export function deepClone(source) {
  if (source === null || typeof source !== 'object') return source;
  const target = Array.isArray(source) ? [] : {};
  Object.keys(source).forEach((key) => {
    target[key] =
      typeof source[key] === 'object' && source[key] !== null
        ? deepClone(source[key])
        : source[key];
  });
  return target;
}

/**
 * 文件大小格式化
 * @param {number} bytes 字节数
 * @returns {string}
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i];
}

/** 手机号验证（中国大陆） */
export function validatePhone(phone) {
  return /^1[3-9]\d{9}$/.test(String(phone));
}

/** 邮箱验证 */
export function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(email));
}

/**
 * 千分位格式化
 * @param {number|string} num
 * @returns {string}
 */
export function toThousands(num) {
  return String(num).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}
