package com.campus.marketplace.config;

import com.campus.marketplace.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;

  public JwtAuthenticationFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String requestURI = request.getRequestURI();
    System.out.println("[JWT Filter] 处理请求: " + request.getMethod() + " " + requestURI);

    try {
      String token = extractTokenFromHeader(request);

      if (token != null && jwtUtil.validateToken(token)) {
        System.out.println("[JWT Filter] ✅ JWT验证成功");

        String username = jwtUtil.getUsernameFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<String> roles = jwtUtil.getRolesFromToken(token);

        var authorities = roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            username,
            null,
            authorities);
        authentication.setDetails(userId);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        System.out.println(
            "[JWT Filter] 👤 用户已认证: " + username + " (ID:" + userId + ", 角色:" + roles + ")");
      } else if (token != null) {
        System.err.println("[JWT Filter] ❌ JWT验证失败");
      }
    } catch (Exception e) {
      System.err.println("[JWT Filter] ❌ JWT处理异常: " + e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  private String extractTokenFromHeader(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      System.out
          .println("[JWT Filter] 🔑 从请求头提取到Token: " + (token.length() > 30 ? token.substring(0, 30) + "..." : token));
      return token;
    }
    return null;
  }
}
