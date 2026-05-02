package com.campus.marketplace.service;

import com.campus.marketplace.repository.FunctionRepository;
import com.campus.marketplace.repository.ResourceMenuRepository;
import com.campus.marketplace.repository.RoleResourceRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ResourceMenuService {
  private final ResourceMenuRepository resourceMenuRepository;
  private final RoleResourceRepository roleResourceRepository;
  private final FunctionRepository functionRepository;

  public ResourceMenuService(ResourceMenuRepository resourceMenuRepository,
      RoleResourceRepository roleResourceRepository,
      FunctionRepository functionRepository) {
    this.resourceMenuRepository = resourceMenuRepository;
    this.roleResourceRepository = roleResourceRepository;
    this.functionRepository = functionRepository;
  }

  /** 获取所有资源菜单（平铺列表） */
  public List<Map<String, Object>> getAllMenus() {
    return resourceMenuRepository.findAll();
  }

  /** 按 appCode 过滤资源菜单 */
  public List<Map<String, Object>> getMenusByAppCode(String appCode) {
    return resourceMenuRepository.findByAppCode(appCode);
  }

  /** 获取资源菜单树（前端展示用） */
  public List<Map<String, Object>> getMenuTree() {
    List<Map<String, Object>> allMenus = resourceMenuRepository.findAll();
    return buildTree(allMenus, 0L);
  }

  /** 获取某个角色有权限的菜单树 */
  public List<Map<String, Object>> getMenuTreeByRole(String roleCode) {
    List<Long> authorizedIds = roleResourceRepository.findResourceIdsByRole(roleCode);
    if (authorizedIds.isEmpty()) {
      return List.of();
    }
    List<Map<String, Object>> menus = resourceMenuRepository.findByIds(authorizedIds);
    return buildTree(menus, 0L);
  }

  /** 获取当前用户角色有权限的菜单树（多角色合并） */
  public List<Map<String, Object>> getMenuTreeByRoles(List<String> roleCodes) {
    List<Long> allIds = new ArrayList<>();
    for (String role : roleCodes) {
      allIds.addAll(roleResourceRepository.findResourceIdsByRole(role));
    }
    if (allIds.isEmpty()) {
      return List.of();
    }
    List<Map<String, Object>> menus = resourceMenuRepository.findByIds(allIds);
    return buildTree(menus, 0L);
  }

  /** 保存/更新资源菜单 */
  public Map<String, Object> saveMenu(String menuCode, String menuName, String menuType,
      String appCode, Long parentId, String path, String icon, int sortOrder,
      String urlPath, String componentPath, String resourceCode, Integer status, Integer visible) {
    return resourceMenuRepository.save(menuCode, menuName, menuType, appCode, parentId, path, icon, sortOrder,
        urlPath, componentPath, resourceCode, status, visible);
  }

  /** 删除资源菜单 */
  public void deleteMenu(Long id) {
    boolean removed = resourceMenuRepository.delete(id);
    if (!removed) {
      throw new IllegalArgumentException("资源菜单不存在");
    }
  }

  /** 设置角色的资源权限（先删后增） */
  public void saveRoleResources(String roleCode, List<Long> resourceIds) {
    roleResourceRepository.saveRoleResources(roleCode, resourceIds);
  }

  /** 获取所有角色列表 */
  public List<String> getAllRoles() {
    return roleResourceRepository.findAllRoles();
  }

  /** 获取角色已授权的资源 ID 列表 */
  public List<Long> getRoleResourceIds(String roleCode) {
    return roleResourceRepository.findResourceIdsByRole(roleCode);
  }

  /** 获取资源已关联的角色列表 */
  public List<String> getResourceRoles(Long resourceId) {
    return roleResourceRepository.findRolesByResourceId(resourceId);
  }

  public Map<String, Object> createRole(String roleCode, String roleName, String description) {
    return roleResourceRepository.createRole(roleCode, roleName, description);
  }

  public void updateRole(String roleCode, String roleName, String description) {
    roleResourceRepository.updateRole(roleCode, roleName, description);
  }

  public void deleteRole(String roleCode) {
    roleResourceRepository.deleteRole(roleCode);
  }

  public void updateRoleStatus(String roleCode, String status) {
    roleResourceRepository.updateRoleStatus(roleCode, status);
  }

  /** 获取某菜单下的功能按钮列表 */
  public List<Map<String, Object>> getFunctionsByMenuId(Long menuId) {
    return functionRepository.findByMenuId(menuId);
  }

  /** 创建/更新功能按钮 */
  public Map<String, Object> saveFunction(Map<String, Object> func) {
    return functionRepository.save(func);
  }

  /** 删除功能按钮 */
  public void deleteFunction(Long id) {
    boolean deleted = functionRepository.delete(id);
    if (!deleted) {
      throw new IllegalArgumentException("功能按钮不存在");
    }
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> buildTree(List<Map<String, Object>> menus, Long parentId) {
    List<Map<String, Object>> tree = new ArrayList<>();
    for (Map<String, Object> menu : menus) {
      Long pid = ((Number) menu.get("parentId")).longValue();
      if (pid.equals(parentId)) {
        Map<String, Object> node = new HashMap<>(menu);
        node.put("children", buildTree(menus, ((Number) menu.get("id")).longValue()));
        tree.add(node);
      }
    }
    return tree;
  }
}
