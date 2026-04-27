export const roleLabels = {
  BUYER: "买家",
  SELLER: "卖家",
  OPS: "运营",
};

export const ALL_ROLES = Object.keys(roleLabels);

export function hasAnyRole(userRoles = [], requiredRoles = []) {
  return userRoles.some((role) => requiredRoles.includes(role));
}

export function hasRole(userRoles = [], role) {
  return userRoles.includes(role);
}
