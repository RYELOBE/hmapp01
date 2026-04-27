package com.campus.marketplace.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.campus.marketplace.service.PortalConfigService;
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
@RequestMapping("/api/portal")
@Validated
public class PortalConfigController {
  private final PortalConfigService portalConfigService;

  public PortalConfigController(PortalConfigService portalConfigService) {
    this.portalConfigService = portalConfigService;
  }

  /** 获取所有门户配置（公开） */
  @GetMapping("/configs")
  public Map<String, Object> listPortals() {
    return Map.of("portals", portalConfigService.getAllPortals());
  }

  /** 获取单个门户配置（公开） */
  @GetMapping("/configs/{portalCode}")
  public Map<String, Object> getPortal(@PathVariable String portalCode) {
    return Map.of("portal", portalConfigService.getByPortalCode(portalCode));
  }

  /** [OPS] 保存门户配置 */
  @PostMapping("/configs")
  @SaCheckLogin
  @SaCheckRole("OPS")
  public Map<String, Object> savePortal(@RequestBody PortalSaveRequest req) {
    var portal = portalConfigService.savePortal(
        req.portalCode(), req.portalName(), req.templateType(),
        req.configJson(), req.updatedBy());
    return Map.of("portal", portal, "message", "门户配置已保存");
  }

  /** [OPS] 删除门户配置 */
  @DeleteMapping("/configs/{portalCode}")
  @SaCheckLogin
  @SaCheckRole("OPS")
  public Map<String, Object> deletePortal(@PathVariable String portalCode) {
    portalConfigService.deletePortal(portalCode);
    return Map.of("message", "门户配置已删除");
  }

  public record PortalSaveRequest(
      @NotBlank String portalCode,
      @NotBlank String portalName,
      String templateType,
      String configJson,
      String updatedBy
  ) {}
}
