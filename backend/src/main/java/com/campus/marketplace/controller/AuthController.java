package com.campus.marketplace.controller;

import com.campus.marketplace.service.AuthService;
import com.campus.marketplace.service.CurrentUserService;
import com.campus.marketplace.service.OpsAuthService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

  public AuthController(AuthService authService, OpsAuthService opsAuthService, CurrentUserService currentUserService) {
    this.authService = authService;
    this.opsAuthService = opsAuthService;
    this.currentUserService = currentUserService;
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

  public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

  public record RegisterRequest(
      @NotBlank String username,
      @NotBlank String password,
      String nickname,
      List<String> roles) {}
}
