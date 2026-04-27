package com.campus.marketplace.service;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.marketplace.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;

  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Map<String, Object> login(String username, String password) {
    var userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      throw new IllegalArgumentException("账号不存在");
    }
    var user = userOpt.get();
    if (!String.valueOf(user.get("password")).equals(password)) {
      throw new IllegalArgumentException("密码错误");
    }

    Long userId = ((Number) user.get("id")).longValue();
    StpUtil.login(userId);

    Map<String, Object> response = new HashMap<>();
    response.put("token", StpUtil.getTokenValue());

    Map<String, Object> safeUser = new HashMap<>();
    safeUser.put("id", userId);
    safeUser.put("username", user.get("username"));
    safeUser.put("nickname", user.get("nickname"));
    safeUser.put("roles", user.get("roles"));
    response.put("user", safeUser);
    return response;
  }
}