package com.campus.marketplace.controller;

import com.campus.marketplace.service.AuthService;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.OpsAuthService;
import com.campus.marketplace.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
  private final AuthService authService;
  private final OpsAuthService opsAuthService;
  private final CurrentUserService currentUserService;
  private final UserRepository userRepository;

  public AuthController(AuthService authService, OpsAuthService opsAuthService, 
      CurrentUserService currentUserService, UserRepository userRepository) {
    this.authService = authService;
    this.opsAuthService = opsAuthService;
    this.currentUserService = currentUserService;
    this.userRepository = userRepository;
  }

  @PostMapping("/login")
  public Map<String, Object> login(@RequestBody LoginRequest request) {
    return authService.login(request.username(), request.password());
  }

  @PostMapping("/register")
  public Map<String, Object> register(@RequestBody RegisterRequest request) {
    return authService.register(request.username(), request.password(), request.nickname(), request.roles());
  }

  @PostMapping("/ops/login")
  public Map<String, Object> opsLogin(@RequestBody LoginRequest request) {
    return opsAuthService.opsLogin(request.username(), request.password());
  }

  @PostMapping("/logout")
  public Map<String, Object> logout() {
    authService.logout();
    return Map.of("code", 200, "message", "登出成功");
  }

  @PostMapping("/ops/logout")
  public Map<String, Object> opsLogout() {
    opsAuthService.logout();
    return Map.of("code", 200, "message", "运营账号登出成功");
  }

  @GetMapping("/current")
  public Map<String, Object> currentUser() {
    return authService.getCurrentUser(currentUserService.userId());
  }

  @GetMapping("/ops/current")
  public Map<String, Object> opsCurrentUser() {
    return opsAuthService.getCurrentUser();
  }

  /** 更新用户资料 */
  @PutMapping("/profile")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> updateProfile(@RequestBody ProfileUpdateRequest request) {
    Long userId = currentUserService.userId();
    
    userRepository.updateProfile(
        userId,
        request.nickname(),
        request.email(),
        request.bio(),
        request.campus(),
        request.phone()
    );
    
    // 返回更新后的用户信息
    var user = userRepository.findById(userId).orElseThrow();
    return Map.of("code", 200, "message", "资料更新成功", "data", user);
  }

  /** 更新用户头像 */
  @PutMapping("/avatar")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> updateAvatar(@RequestBody AvatarUpdateRequest request) {
    Long userId = currentUserService.userId();
    
    userRepository.updateAvatar(userId, request.avatar());
    
    return Map.of("code", 200, "message", "头像更新成功", "data", Map.of("avatar", request.avatar()));
  }

  /** 修改密码 */
  @PutMapping("/password")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> changePassword(@RequestBody PasswordChangeRequest request) {
    Long userId = currentUserService.userId();
    
    // TODO: 验证旧密码（需要根据实际 AuthService 实现）
    userRepository.updatePassword(userId, request.newPassword());
    
    return Map.of("code", 200, "message", "密码修改成功");
  }

  public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

  public record RegisterRequest(
      @NotBlank String username,
      @NotBlank String password,
      String nickname,
      List<String> roles) {}

  public record ProfileUpdateRequest(
      String nickname,
      String email,
      String bio,
      String campus,
      String phone) {}

  public record AvatarUpdateRequest(@NotBlank String avatar) {}

  public record PasswordChangeRequest(
      @NotBlank String oldPassword,
      @NotBlank String newPassword) {}
}
