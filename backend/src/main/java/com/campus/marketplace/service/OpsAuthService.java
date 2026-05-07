package com.campus.marketplace.service;

import com.campus.marketplace.repository.OpsAccountRepository;
import com.campus.marketplace.repository.OpsAccountRepository.OpsAccount;
import com.campus.marketplace.util.JwtUtil;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class OpsAuthService {

  private final OpsAccountRepository opsAccountRepository;
  private final JwtUtil jwtUtil;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public OpsAuthService(OpsAccountRepository opsAccountRepository, JwtUtil jwtUtil) {
    this.opsAccountRepository = opsAccountRepository;
    this.jwtUtil = jwtUtil;
  }

  public Map<String, Object> opsLogin(String username, String password) {
    System.out.println("========================================");
    System.out.println("🔐 运营账号尝试登录: " + username);
    System.out.println("========================================");

    var accountOpt = opsAccountRepository.findByUsername(username);
    if (accountOpt.isEmpty()) {
      throw new IllegalArgumentException("运营账号不存在");
    }
    var account = accountOpt.get();
    if (!"ACTIVE".equals(account.getStatus())) {
      throw new IllegalArgumentException("账号已被禁用");
    }
    if (!passwordEncoder.matches(password, account.getPassword())) {
      throw new IllegalArgumentException("密码错误");
    }

    Long opsId = account.getId();
    List<String> roles = account.getRolesList();

    // 🔥 使用 JwtUtil 生成真实的 JWT Token
    String token = jwtUtil.generateToken(username, roles, opsId);

    System.out.println("========================================");
    System.out.println("✅ 运营账号 JWT 已生成");
    System.out.println("👤 运营账号: " + username);
    System.out.println("🆔 账号ID: " + opsId);
    System.out.println("🎭 角色: " + roles);
    System.out.println("========================================");

    Map<String, Object> response = new HashMap<>();
    response.put("token", token);
    response.put("tokenType", "Bearer");

    Map<String, Object> safeAccount = new HashMap<>();
    safeAccount.put("id", opsId);
    safeAccount.put("username", account.getUsername());
    safeAccount.put("nickname", account.getNickname());
    safeAccount.put("roles", roles);
    safeAccount.put("roleLevel", account.getRoleLevel());
    response.put("user", safeAccount);
    return response;
  }

  public void logout() {
    System.out.println("👋 运营账号已登出 (JWT无状态，无需服务端处理)");
  }

  public boolean isOpsLogin() {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      return auth != null
          && auth.isAuthenticated()
          && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().startsWith("OPS"));
    } catch (Exception e) {
      return false;
    }
  }

  public Map<String, Object> getCurrentUser() {
    if (!isOpsLogin()) {
      return Map.of("code", 401, "message", "未登录");
    }

    Long opsId = userId();
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

  private Long userId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
      return null;
    }

    // 优先从 details 中获取 userId (在 JwtAuthenticationFilter 中设置)
    Object details = auth.getDetails();
    if (details instanceof Number) {
      return ((Number) details).longValue();
    }

    // 备选方案：通过用户名查找
    String username = auth.getName();
    return opsAccountRepository.findByUsername(username).map(OpsAccount::getId).orElse(null);
  }
}
