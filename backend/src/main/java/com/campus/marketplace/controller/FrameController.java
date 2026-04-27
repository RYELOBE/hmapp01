package com.campus.marketplace.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.FrameService;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/frame")
@Validated
public class FrameController {
  private final FrameService frameService;
  private final CurrentUserService currentUserService;

  public FrameController(FrameService frameService, CurrentUserService currentUserService) {
    this.frameService = frameService;
    this.currentUserService = currentUserService;
  }

  /** 获取所有子应用注册配置（qiankun 用）—— 公开接口，无需登录 */
  @GetMapping("/registers")
  public Map<String, Object> registers() {
    return Map.of("apps", frameService.getRegisters());
  }

  /** 获取当前用户有权访问的路由资源 —— 需要登录 */
  @GetMapping("/routes")
  @SaCheckLogin
  public Map<String, Object> routes() {
    return Map.of("resources", frameService.getAuthorizedRoutes(currentUserService.roles()));
  }

  /** [OPS] 动态新增/更新子应用配置 */
  @PostMapping("/registers")
  @SaCheckLogin
  @SaCheckRole("OPS")
  public Map<String, Object> addRegister(@RequestBody AppRegisterRequest req) {
    var app = frameService.saveRegister(
        req.appCode(), req.title(), req.entry(), req.pathPrefix(), req.roles(),
        req.hideShellMenu() != null && req.hideShellMenu());
    return Map.of("app", app, "message", "子应用配置保存成功");
  }

  /** [OPS] 删除子应用配置 */
  @DeleteMapping("/registers/{appCode}")
  @SaCheckLogin
  @SaCheckRole("OPS")
  public Map<String, Object> deleteRegister(@PathVariable String appCode) {
    frameService.deleteRegister(appCode);
    return Map.of("message", "子应用配置已删除");
  }

  public record AppRegisterRequest(
      @NotBlank String appCode,
      @NotBlank String title,
      @NotBlank String entry,
      @NotBlank String pathPrefix,
      List<String> roles,
      Boolean hideShellMenu
  ) {}
}
