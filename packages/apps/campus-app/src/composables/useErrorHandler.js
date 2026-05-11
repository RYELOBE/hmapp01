import { Message } from "@arco-design/web-vue";
import { getErrorMessage, getErrorCode, extractErrorInfo, ERROR_MESSAGES } from "../utils/error-utils";

/**
 * 全局错误处理 composable
 * 统一处理 API 错误和业务错误
 */
export function useErrorHandler() {
  /**
   * 处理错误并显示提示
   * @param {Error} error - 错误对象
   * @param {Object} options - 配置选项
   * @param {string} options.defaultMessage - 默认错误消息
   * @param {boolean} options.showToast - 是否显示提示（默认 true）
   * @returns {Object} 错误信息 { code, message, originalMessage }
   */
  const handleError = (error, options = {}) => {
    const { defaultMessage = "操作失败", showToast = true } = options;
    
    const errorInfo = extractErrorInfo(error);
    const message = errorInfo.message || defaultMessage;
    
    if (showToast) {
      // 根据错误码选择提示类型
      const code = errorInfo.code;
      if (code >= 500) {
        Message.error(message);
      } else if (code >= 400) {
        Message.warning(message);
      } else {
        Message.error(message);
      }
    }
    
    console.error("[ErrorHandler]", {
      code: errorInfo.code,
      message: errorInfo.message,
      originalMessage: errorInfo.originalMessage,
      error,
    });
    
    return errorInfo;
  };

  /**
   * 异步操作包装器，自动处理错误
   * @param {Function} asyncFn - 异步函数
   * @param {Object} options - 配置选项
   * @returns {Function} 包装后的函数
   */
  const withErrorHandler = (asyncFn, options = {}) => {
    return async (...args) => {
      try {
        return await asyncFn(...args);
      } catch (error) {
        handleError(error, options);
        throw error;
      }
    };
  };

  /**
   * 批量操作错误处理
   * @param {Array} errors - 错误数组 [{ item, error }]
   * @param {Object} options - 配置选项
   */
  const handleBatchErrors = (errors, options = {}) => {
    const { showAll = false, maxShow = 3 } = options;
    
    if (!errors || errors.length === 0) return;
    
    if (errors.length === 1) {
      Message.error(errors[0].error);
      return;
    }
    
    if (showAll && errors.length <= maxShow) {
      errors.forEach((e, index) => {
        setTimeout(() => {
          Message.error(`${e.item}: ${e.error}`);
        }, index * 200);
      });
    } else {
      Message.warning(`${errors.length} 个操作失败，首个错误：${errors[0].error}`);
    }
  };

  return {
    handleError,
    withErrorHandler,
    handleBatchErrors,
    getErrorMessage,
    getErrorCode,
    ERROR_MESSAGES,
  };
}

// 导出单例方法，方便直接调用
export const showError = (error, defaultMessage = "操作失败") => {
  const message = getErrorMessage(error) || defaultMessage;
  Message.error(message);
  return message;
};

export const showWarning = (error, defaultMessage = "操作失败") => {
  const message = getErrorMessage(error) || defaultMessage;
  Message.warning(message);
  return message;
};
