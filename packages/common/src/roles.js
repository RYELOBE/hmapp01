export const roleLabels = {
  BUYER: "买家",
  SELLER: "卖家",
  OPS: "运营",
};

export const ALL_ROLES = Object.keys(roleLabels);

export function hasAnyRole(userRoles = [], requiredRoles = []) {
  const userRolesArray = normalizeToArray(userRoles);
  const requiredRolesArray = normalizeToArray(requiredRoles);
  return userRolesArray.some((role) => requiredRolesArray.includes(role));
}

function normalizeToArray(value) {
  if (Array.isArray(value)) return value;
  if (typeof value === 'string' && value.trim()) return [value.trim()];
  return [];
}

export function hasRole(userRoles = [], role) {
  return userRoles.includes(role);
}
