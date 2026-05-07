# Spring Boot + JWT 完整配置指南

## 📦 1. Maven 依赖 (pom.xml)

```xml
<!-- JWT 支持 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>

<!-- Spring Security（如果使用） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## 🔧 2. JWT 工具类

```java
package com.campus.market.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret:your-256-bit-secret-key-for-jwt-token-generation-must-be-at-least-256-bits}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认24小时
    private long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String generateToken(String username, List<String> roles, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("roles", List.class);
    }

    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = parseToken(token).getExpiration();
        return expiration.before(new Date());
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT Token已过期", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("JWT Token格式无效", e);
        } catch (SignatureException e) {
            throw new RuntimeException("JWT签名无效", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT Token为空", e);
        }
    }
}
```

## 🔐 3. 登录接口返回 JWT

```java
package com.campus.market.controller;

import com.campus.market.dto.LoginRequest;
import com.campus.market.dto.LoginResponse;
import com.campus.market.entity.User;
import com.campus.market.service.UserService;
import com.campus.market.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 1. 验证用户名密码
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        
        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 401);
            error.put("message", "用户名或密码错误");
            return ResponseEntity.status(401).body(error);
        }

        // 2. 生成 JWT Token
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        String jwtToken = jwtUtil.generateToken(
                user.getUsername(),
                roles,
                user.getId()
        );

        // 3. 构建响应（关键：返回 JWT 格式的 Token）
        LoginResponse response = LoginResponse.builder()
                .token(jwtToken)                    // ← 这里是真正的 JWT！
                .tokenType("Bearer")               // 标记为 Bearer 类型
                .user(UserDTO.fromEntity(user))
                .build();

        System.out.println("✅ 生成的JWT Token: " + jwtToken.substring(0, 50) + "...");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // 注册逻辑...
        User newUser = userService.register(request);
        
        // 注册成功后自动生成 JWT
        String jwtToken = jwtUtil.generateToken(
                newUser.getUsername(),
                newUser.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toList()),
                newUser.getId()
        );

        LoginResponse response = LoginResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .user(UserDTO.fromEntity(newUser))
                .build();

        return ResponseEntity.ok(response);
    }
}
```

## 📝 4. DTO 类

```java
package com.campus.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;           // JWT Token (格式: eyJhbGci...)
    private String tokenType;       // "Bearer"
    private UserDTO user;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private List<String> roles;
    private String avatar;
    private String phone;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roles(user.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toList()))
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .build();
    }
}

@Data
public class LoginRequest {
    private String username;
    private String password;
}
```

## ⚙️ 5. Spring Security 配置（如果使用）

```java
package com.campus.market.config;

import com.campus.market.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（前后端分离不需要）
            .csrf(csrf -> csrf.disable())

            // 配置 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 设置 Session 为无状态（JWT 不需要 Session）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 公开接口（无需认证）
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()

                // 静态资源
                .requestMatchers("/",
                        "/index.html",
                        "/favicon.ico",
                        "/assets/**",
                        "/**/*.js",
                        "/**/*.css",
                        "/**/*.html"
                ).permitAll()

                // 其他所有接口都需要认证
                .anyRequest().authenticated()
            )

            // 添加 JWT 过滤器
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
            );

        return http;
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

## 🔍 6. JWT 认证过滤器

```java
package com.campus.market.config;

import com.campus.market.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 从请求头获取 Token
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7); // 去掉 "Bearer " 前缀

        try {
            // 2. 验证并解析 Token
            String username = jwtUtil.getUsernameFromToken(token);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 3. 从 Token 获取用户信息
                Long userId = jwtUtil.getUserIdFromToken(token);
                List<String> roles = jwtUtil.getRolesFromToken(token);

                // 4. 创建认证对象
                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        );

                authentication.setDetails(userId); // 存储用户ID

                // 5. 设置到安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            logger.error("JWT验证失败: " + e.getMessage());
            // Token 无效，不设置认证信息，后续会返回401
        }

        filterChain.doFilter(request, response);
    }
}
```

## 📌 7. application.yml 配置

```yaml
# JWT 配置
jwt:
  secret: your-super-secret-key-that-is-at-least-256-bits-long-change-this-in-production!!!
  expiration: 86400000 # 24小时（毫秒）

# Server 配置
server:
  port: 8080
  servlet:
    context-path: /api

# 数据库配置（示例）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_market?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## ✅ 8. 测试验证

启动后端后，测试登录接口：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"buyer","password":"123456"}'
```

**预期响应**：
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1001,
    "username": "buyer",
    "nickname": "买家同学",
    "roles": ["BUYER"],
    "avatar": null,
    "phone": "138****8888"
  }
}
```

**关键检查点**：
- ✅ `token` 字段应该是 `eyJhbGci...` 开头（JWT 格式）
- ❌ 不应该是 `711cacab-2771-...` （UUID 格式）

---

## 🎯 下一步操作

1. **复制上面的代码**到你的 Spring Boot 项目
2. **修改 `application.yml`** 中的 `jwt.secret` 为你自己的密钥
3. **重启后端服务**
4. **重新登录测试**

前端已经完全支持 JWT 格式了！一旦后端返回正确的 JWT，所有问题都会自动解决！🚀
