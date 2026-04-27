package com.campus.marketplace.service;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.marketplace.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
  private final UserRepository userRepository;

  public CurrentUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Long userId() {
    return StpUtil.getLoginIdAsLong();
  }

  public Map<String, Object> currentUser() {
    return userRepository.findById(userId()).orElse(null);
  }

  public List<String> roles() {
    return userRepository.findById(userId())
        .map(user -> UserRepository.parseRoles(user.get("roles")))
        .orElse(List.of());
  }
}
