package com.campus.marketplace.service;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.marketplace.repository.OpsAccountRepository;
import com.campus.marketplace.repository.OpsAccountRepository.OpsAccount;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OpsAuthService {
  private final OpsAccountRepository opsAccountRepository;

  public OpsAuthService(OpsAccountRepository opsAccountRepository) {
    this.opsAccountRepository = opsAccountRepository;
  }

  public Map<String, Object> opsLogin(String username, String password) {
    var accountOpt = opsAccountRepository.findByUsername(username);
    if (accountOpt.isEmpty()) {
      throw new IllegalArgumentException("运营账号不存在");
    }
    var account = accountOpt.get();
    if (!"ACTIVE".equals(account.getStatus())) {
      throw new IllegalArgumentException("账号已被禁用");
    }
    if (!account.getPassword().equals(password)) {
      throw new IllegalArgumentException("密码错误");
    }

    Long opsId = account.getId();
    StpUtil.login(opsId, "ops");

    Map<String, Object> response = new HashMap<>();
    response.put("token", StpUtil.getTokenValue());
    response.put("tokenType", "ops");

    Map<String, Object> safeAccount = new HashMap<>();
    safeAccount.put("id", opsId);
    safeAccount.put("username", account.getUsername());
    safeAccount.put("nickname", account.getNickname());
    safeAccount.put("roles", account.getRolesList());
    safeAccount.put("roleLevel", account.getRoleLevel());
    response.put("user", safeAccount);
    return response;
  }

  public void logout() {
    try {
      if (StpUtil.isLogin()) {
        StpUtil.logout();
      }
    } catch (Exception ignored) {}
  }

  public boolean isOpsLogin() {
    try {
      return StpUtil.isLogin() && "ops".equals(StpUtil.getTokenName());
    } catch (Exception e) {
      return false;
    }
  }

  public Map<String, Object> getCurrentUser() {
    if (!isOpsLogin()) {
      return Map.of("code", 401, "message", "未登录");
    }
    Long opsId = Long.parseLong(String.valueOf(StpUtil.getLoginId()));
    var accountOpt = opsAccountRepository.findById(opsId);
    if (accountOpt.isEmpty()) {
      return Map.of("code", 404, "message", "账号不存在");
    }
    var account = accountOpt.get();
    Map<String, Object> safeAccount = new HashMap<>();
    safeAccount.put("id", account.getId());
    safeAccount.put("username", account.getUsername());
    safeAccount.put("nickname", account.getNickname());
    safeAccount.put("roles", account.getRolesList());
    safeAccount.put("roleLevel", account.getRoleLevel());
    return Map.of("code", 200, "data", safeAccount);
  }
}
