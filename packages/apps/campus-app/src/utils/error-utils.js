const ERROR_MESSAGES = {
  400: {
    default: '请求参数错误',
    specific: {
      '不能购买自己的商品': '您无法购买自己发布的商品',
      '商品不存在': '商品不存在或已下架',
      '商品未上架': '该商品尚未通过审核，暂时无法购买',
      '您已有该商品的进行中订单': '您已有该商品的进行中订单，请勿重复购买',
    }
  },
  401: {
    default: '登录已过期，请重新登录',
  },
  403: {
    default: '没有权限执行此操作',
    specific: {
      '没有权限访问该资源': '您没有权限访问该资源',
    }
  },
  500: {
    default: '服务器内部错误，请稍后重试',
  },
};

function extractErrorInfo(error) {
  if (!error) return { code: 500, message: '未知错误' };

  const response = error.response;
  if (!response) {
    return { code: 500, message: error.message || '网络连接失败' };
  }

  const status = response.status;
  const data = response.data;

  if (data && typeof data === 'object') {
    const code = data.code || status;
    const message = data.message || '';

    const errorConfig = ERROR_MESSAGES[code] || ERROR_MESSAGES[status];
    if (errorConfig) {
      if (errorConfig.specific && errorConfig.specific[message]) {
        return { code, message: errorConfig.specific[message], originalMessage: message };
      }
      return { code, message: message || errorConfig.default, originalMessage: message };
    }

    return { code, message: message || '操作失败', originalMessage: message };
  }

  return { code: status, message: error.message || `请求失败(${status})` };
}

function getErrorMessage(error) {
  const { message } = extractErrorInfo(error);
  return message;
}

function getErrorCode(error) {
  const { code } = extractErrorInfo(error);
  return code;
}

export { extractErrorInfo, getErrorMessage, getErrorCode, ERROR_MESSAGES };
