package com.campus.marketplace.service;

import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.util.JwtUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
  }

  public Map<String, Object> login(String username, String password) {
    System.out.println("========================================");
    System.out.println("🔐 用户尝试登录: " + username);
    System.out.println("========================================");

    var userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      throw new IllegalArgumentException("账号不存在");
    }
    var user = userOpt.get();
    if (!passwordEncoder.matches(password, String.valueOf(user.get("password")))) {
      throw new IllegalArgumentException("密码错误");
    }

    Long userId = ((Number) user.get("id")).longValue();
    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) user.get("roles");

    // 🔥 使用 JwtUtil 生成真实的 JWT Token
    String token = jwtUtil.generateToken(username, roles, userId);

    Map<String, Object> response = new HashMap<>();
    response.put("token", token);
    response.put("tokenType", "Bearer");

    Map<String, Object> safeUser = new HashMap<>();
    safeUser.put("id", userId);
    safeUser.put("username", user.get("username"));
    safeUser.put("nickname", user.get("nickname"));
    safeUser.put("roles", roles);
    safeUser.put("avatar", user.get("avatar"));
    safeUser.put(
      "phone",
      user.get("phone") != null ? user.get("phone").toString().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") : null
    );
    response.put("user", safeUser);

    return response;
  }

  public void logout() {
    System.out.println("👋 用户已登出 (JWT无状态，无需服务端处理)");
  }

  public Map<String, Object> getCurrentUser(Long userId) {
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

    // 🔥 注册成功后也生成 JWT
    String token = jwtUtil.generateToken(username, roles, userId);

    Map<String, Object> response = new HashMap<>();
    response.put("token", token);
    response.put("tokenType", "Bearer");

    Map<String, Object> safeUser = new HashMap<>();
    safeUser.put("id", userId);
    safeUser.put("username", user.get("username"));
    safeUser.put("nickname", user.get("nickname"));
    safeUser.put("roles", user.get("roles"));
    response.put("user", safeUser);

    return response;
  }
}
