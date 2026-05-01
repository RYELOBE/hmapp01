package com.campus.marketplace.service;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.marketplace.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Map<String, Object> login(String username, String password) {
    var userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      throw new IllegalArgumentException("账号不存在");
    }
    var user = userOpt.get();
    if (!passwordEncoder.matches(password, String.valueOf(user.get("password")))) {
      throw new IllegalArgumentException("密码错误");
    }

    Long userId = ((Number) user.get("id")).longValue();
    StpUtil.login(userId);

    Map<String, Object> response = new HashMap<>();
    response.put("token", StpUtil.getTokenValue());
    response.put("tokenType", "user");

    Map<String, Object> safeUser = new HashMap<>();
    safeUser.put("id", userId);
    safeUser.put("username", user.get("username"));
    safeUser.put("nickname", user.get("nickname"));
    safeUser.put("roles", user.get("roles"));
    response.put("user", safeUser);
    return response;
  }

  public void logout() {
    if (StpUtil.isLogin()) {
      StpUtil.logout();
    }
  }

  public Map<String, Object> getCurrentUser() {
    if (!StpUtil.isLogin()) {
      return Map.of("code", 401, "message", "未登录");
    }
    Long userId = Long.parseLong(String.valueOf(StpUtil.getLoginId()));
    var userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      return Map.of("code", 404, "message", "用户不存在");
    }
    var user = userOpt.get();
    Map<String, Object> safeUser = new HashMap<>();
    safeUser.put("id", ((Number) user.get("id")).longValue());
    safeUser.put("username", user.get("username"));
    safeUser.put("nickname", user.get("nickname"));
    safeUser.put("roles", user.get("roles"));
    return Map.of("code", 200, "data", safeUser);
  }

  public Map<String, Object> register(String username, String password, String nickname, List<String> roles) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("用户名不能为空");
    }
    if (password == null || password.length() < 6) {
      throw new IllegalArgumentException("密码长度不能少于6位");
    }
    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("至少需要选择一个角色");
    }

    var existingUser = userRepository.findByUsername(username);
    if (existingUser.isPresent()) {
      throw new IllegalArgumentException("用户名已存在");
    }

    var user = userRepository.create(username, passwordEncoder.encode(password), nickname, roles);

    Long userId = ((Number) user.get("id")).longValue();
    StpUtil.login(userId);

    Map<String, Object> response = new HashMap<>();
    response.put("token", StpUtil.getTokenValue());
    response.put("tokenType", "user");

    Map<String, Object> safeUser = new HashMap<>();
    safeUser.put("id", userId);
    safeUser.put("username", user.get("username"));
    safeUser.put("nickname", user.get("nickname"));
    safeUser.put("roles", user.get("roles"));
    response.put("user", safeUser);
    return response;
  }
}