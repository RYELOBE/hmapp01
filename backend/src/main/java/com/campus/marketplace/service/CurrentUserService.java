package com.campus.marketplace.service;

import com.campus.marketplace.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

  private final UserRepository userRepository;

  public CurrentUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Long userId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new RuntimeException("未登录");
    }

    // 从 authentication details 中获取 userId (在 JwtAuthenticationFilter 中设置)
    Object details = authentication.getDetails();
    if (details instanceof Number) {
      return ((Number) details).longValue();
    }

    // 备选方案：通过用户名查找
    String username = authentication.getName();
    var userOpt = userRepository.findByUsername(username);
    return userOpt.map(user -> ((Number) user.get("id")).longValue()).orElse(null);
  }

  public Map<String, Object> currentUser() {
    return userRepository.findById(userId()).orElse(null);
  }

  public List<String> roles() {
    return userRepository
      .findById(userId())
      .map(user -> UserRepository.parseRoles(user.get("roles")))
      .orElse(List.of());
  }
}
