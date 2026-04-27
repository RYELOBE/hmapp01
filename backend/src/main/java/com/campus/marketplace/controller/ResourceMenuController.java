package com.campus.marketplace.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.ResourceMenuService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resource")
@Validated
public class ResourceMenuController {
  private final ResourceMenuService resourceMenuService;
  private final CurrentUserService currentUserService;

  public ResourceMenuController(ResourceMenuService resourceMenuService,
      CurrentUserService currentUserService) {
    this.resourceMenuService = resourceMenuService;
    this.currentUserService = currentUserService;
  }

  /** 获取所有资源菜单（OPS 用） */
  @GetMapping("/menus")
  @SaCheckLogin
  @SaCheckRole("OPS")
  public Map<String, Object> getAllMenus() {
    return Map.of("menus", resourceMenuService.getAllMenus());
  }

  /** 获取资源菜单树 */
  @GetMapping("/menu-tree")
  @SaCheckLogin
  @SaCheckRole("OPS")
  public Map<String, Object> getMenuTree() {
    return Map.of("tree", resourceMenuService.getMenuTree());
  }

  /** 获取当前用户有权限的菜单树（登录后） */
  @GetMapping("/my-menus")
  @SaCheckLogin
  public Map<String, Object> getMyMenuTree() {
    return Map.of("tree", resourceMenuService.getMenuTreeByRoles(currentUserService.roles()));
  }

  /** [OPS] 保存资源菜单 */
  @PostMapping("/menus")
  @SaCheckLogin
  @SaCheckRole("OPS")
  public Map<String, Object> saveMenu(@RequestBody MenuSaveRequest req) {
    var menu = resourceMenuService.saveMenu(
        req.menuCode(), req.menuName(), req.menuType(),
        req.appCode(), req.parentId(), req.path(), req.icon(), req.sortOrder());
    return Map.of("menu", menu, "message", "资源菜单已保存");
  }

  /** [OPS] 删除资源菜单 */
  @DeleteMapping("/menus/{id}")
  @SaCheckLogin
  @SaCheckRole("OPS")
  public Map<String, Object> deleteMenu(@PathVariable Long id) {
    resourceMenuService.deleteMenu(id);
    return Map.of("message", "资源菜单已删除");
  }

  /** 获取所有角色 */
  @GetMapping("/roles")
  @SaCheckLogin
  @SaCheckRole("OPS")
  public Map<String, Object> getAllRoles() {
    return Map.of("roles", resourceMenuService.getAllRoles());
  }

  /** 获取角色已授权的资源 ID 列表 */
  @GetMapping("/roles/{roleCode}/resources")
  @SaCheckLogin
  @SaCheckRole("OPS")
  public Map<String, Object> getRoleResources(@PathVariable String roleCode) {
    return Map.of("resourceIds", resourceMenuService.getRoleResourceIds(roleCode));
  }

  /** [OPS] 设置角色的资源权限 */
  @PostMapping("/roles/{roleCode}/resources")
  @SaCheckLogin
  @SaCheckRole("OPS")
  public Map<String, Object> saveRoleResources(
      @PathVariable String roleCode,
      @RequestBody RoleResourceRequest req) {
    resourceMenuService.saveRoleResources(roleCode, req.resourceIds());
    return Map.of("message", "角色资源权限已更新");
  }

  public record MenuSaveRequest(
      @NotBlank String menuCode,
      @NotBlank String menuName,
      String menuType,
      String appCode,
      Long parentId,
      String path,
      String icon,
      int sortOrder
  ) {}

  public record RoleResourceRequest(
      @NotNull List<Long> resourceIds
  ) {}
}
