package com.campus.marketplace.service;

import com.campus.marketplace.repository.AppRegisterRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FrameService {
  private final AppRegisterRepository appRegisterRepository;

  public FrameService(AppRegisterRepository appRegisterRepository) {
    this.appRegisterRepository = appRegisterRepository;
  }

  /**
   * 返回所有已注册子应用配置（供 qiankun registerMicroApps 使用）。
   * 字段: appCode, title, entry, pathPrefix, roles
   */
  public List<Map<String, Object>> getRegisters() {
    return appRegisterRepository.findAll();
  }

  /**
   * 返回当前用户有权访问的路由资源。
   * 字段: appCode, pathPrefix, roles, title
   */
  public List<Map<String, Object>> getAuthorizedRoutes(List<String> userRoles) {
    return appRegisterRepository.findAll().stream()
        .filter(app -> {
          @SuppressWarnings("unchecked")
          List<String> allowed = (List<String>) app.getOrDefault("roles", List.of());
          if (allowed.isEmpty()) {
            return true;
          }
          return userRoles.stream().anyMatch(allowed::contains);
        })
        .toList();
  }

  /**
   * 动态新增或更新子应用配置。
   */
  public Map<String, Object> saveRegister(String appCode, String title, String entry,
      String pathPrefix, List<String> roles, boolean hideShellMenu) {
    return appRegisterRepository.save(appCode, title, entry, pathPrefix, roles, hideShellMenu);
  }

  /**
   * 删除子应用配置。
   */
  public void deleteRegister(String appCode) {
    boolean removed = appRegisterRepository.delete(appCode);
    if (!removed) {
      throw new IllegalArgumentException("子应用 [" + appCode + "] 不存在");
    }
  }
}
