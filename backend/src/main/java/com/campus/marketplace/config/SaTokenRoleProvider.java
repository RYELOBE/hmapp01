package com.campus.marketplace.config;

import cn.dev33.satoken.stp.StpInterface;
import com.campus.marketplace.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SaTokenRoleProvider implements StpInterface {
  private final UserRepository userRepository;

  public SaTokenRoleProvider(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public List<String> getPermissionList(Object loginId, String loginType) {
    return List.of();
  }

  @Override
  public List<String> getRoleList(Object loginId, String loginType) {
    Long userId = Long.parseLong(String.valueOf(loginId));
    return userRepository.findById(userId)
        .map(user -> UserRepository.parseRoles(user.get("roles")))
        .orElse(List.of());
  }
}
