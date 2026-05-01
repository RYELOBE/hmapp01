package com.campus.marketplace.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.campus.marketplace.repository.OpsAccountRepository;
import com.campus.marketplace.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SaTokenRoleProvider implements StpInterface {
  private final UserRepository userRepository;
  private final OpsAccountRepository opsAccountRepository;

  public SaTokenRoleProvider(UserRepository userRepository, OpsAccountRepository opsAccountRepository) {
    this.userRepository = userRepository;
    this.opsAccountRepository = opsAccountRepository;
  }

  @Override
  public List<String> getPermissionList(Object loginId, String loginType) {
    return List.of();
  }

  @Override
  public List<String> getRoleList(Object loginId, String loginType) {
    String device = StpUtil.getLoginDevice();
    if ("ops".equals(device)) {
      Long opsId = Long.parseLong(String.valueOf(loginId));
      var accountOpt = opsAccountRepository.findById(opsId);
      if (accountOpt.isPresent()) {
        return accountOpt.get().getRolesList();
      }
      return List.of();
    }
    Long userId = Long.parseLong(String.valueOf(loginId));
    return userRepository.findById(userId)
        .map(user -> UserRepository.parseRoles(user.get("roles")))
        .orElse(List.of());
  }
}
