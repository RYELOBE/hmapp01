const JWT_HEADER = 'Authorization';
const JWT_PREFIX = 'Bearer ';

export function isJWT(token) {
  if (!token) return false;

  const cleanToken = token.startsWith(JWT_PREFIX)
    ? token.substring(JWT_PREFIX.length)
    : token;

  const parts = cleanToken.split('.');
  return parts.length === 3 && parts.every(part => part.length > 0);
}

export function parseJWT(token) {
  if (!token) return null;

  try {
    let cleanToken = token;
    if (cleanToken.startsWith(JWT_PREFIX)) {
      cleanToken = cleanToken.substring(JWT_PREFIX.length);
    }

    if (!isJWT(cleanToken)) {
      console.warn('[JWT] ⚠️ Token不是有效的JWT格式:', cleanToken.substring(0, 30));
      return null;
    }

    const base64Url = cleanToken.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('[JWT] ❌ 解析JWT失败:', error);
    return null;
  }
}

export function getJWTPayload(token) {
  return parseJWT(token);
}

export function getJWTExpiration(token) {
  const payload = parseJWT(token);
  if (!payload || !payload.exp) return null;
  return new Date(payload.exp * 1000);
}

export function isJWTExpired(token) {
  const expiration = getJWTExpiration(token);
  if (!expiration) return true;
  return new Date() >= expiration;
}

export function isJWTExpiringSoon(token, thresholdSeconds = 300) {
  const expiration = getJWTExpiration(token);
  if (!expiration) return true;
  const now = new Date();
  const threshold = new Date(now.getTime() + thresholdSeconds * 1000);
  return expiration <= threshold;
}

export function getJWTSubject(token) {
  const payload = parseJWT(token);
  return payload?.sub || payload?.username || payload?.userId || null;
}

export function getJWTRoles(token) {
  const payload = parseJWT(token);
  const roles = payload?.roles || payload?.authorities || [];

  if (typeof roles === 'string') {
    try {
      return JSON.parse(roles);
    } catch {
      return [roles];
    }
  }

  if (Array.isArray(roles)) {
    return roles.map(role => {
      if (typeof role === 'string') return role;
      return role?.authority || role?.role || String(role);
    });
  }

  return [];
}

export function getJWTUserInfo(token) {
  const payload = parseJWT(token);
  if (!payload) return null;

  return {
    id: payload?.sub || payload?.userId || payload?.id,
    username: payload?.username || payload?.preferred_username || payload?.user_name,
    nickname: payload?.nickname || payload?.name || payload?.preferred_username,
    roles: getJWTRoles(token),
    email: payload?.email,
    avatar: payload?.picture || payload?.avatar,
    issuedAt: payload?.iat ? new Date(payload.iat * 1000) : null,
    expiresAt: payload?.exp ? new Date(payload.exp * 1000) : null,
  };
}

export function formatJWTToken(token) {
  if (!token) return '';
  if (token.startsWith(JWT_PREFIX)) return token;
  return `${JWT_PREFIX}${token}`;
}

export function extractJWTFromHeader(headerValue) {
  if (!headerValue) return null;
  if (headerValue.startsWith(JWT_PREFIX)) {
    return headerValue.substring(JWT_PREFIX.length);
  }
  return headerValue;
}

export function createAuthHeader(token) {
  if (!token) return {};
  const formattedToken = formatJWTToken(token);
  return { [JWT_HEADER]: formattedToken };
}

export function validateJWT(token) {
  if (!token) {
    return { valid: false, reason: 'Token不存在' };
  }

  if (!isJWT(token)) {
    return { valid: false, reason: 'Token格式无效' };
  }

  if (isJWTExpired(token)) {
    return { valid: false, reason: 'Token已过期' };
  }

  const userInfo = getJWTUserInfo(token);
  if (!userInfo?.id) {
    return { valid: false, reason: 'Token中缺少用户信息' };
  }

  return { valid: true, user: userInfo, expiresAt: getJWTExpiration(token) };
}

export function logJWTDebugInfo(token) {
  console.log('===== JWT Token 调试信息 =====');
  console.log('[JWT] 原始Token:', token ? token.substring(0, 50) + '...' : '❌ 不存在');
  console.log('[JWT] 是否为有效JWT格式:', isJWT(token));

  if (isJWT(token)) {
    const payload = parseJWT(token);
    console.log('[JWT] Payload内容:', payload);

    const userInfo = getJWTUserInfo(token);
    console.log('[JWT] 用户信息:', userInfo);

    const expiration = getJWTExpiration(token);
    console.log('[JWT] 过期时间:', expiration?.toLocaleString());
    console.log('[JWT] 是否已过期:', isJWTExpired(token));
    console.log('[JWT] 是否即将过期:', isJWTExpiringSoon(token));

    const validation = validateJWT(token);
    console.log('[JWT] 验证结果:', validation.valid ? '✅ 有效' : '❌ 无效', validation.reason || '');
  } else {
    console.warn('[JWT] ⚠️ 当前Token不是JWT格式，可能是UUID或其他格式');
  }
  console.log('=============================');
}
