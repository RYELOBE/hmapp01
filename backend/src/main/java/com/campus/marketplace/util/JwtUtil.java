package com.campus.marketplace.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret:campus_market_jwt_secret_key_2024_must_be_at_least_256_bits_long_for_security}")
  private String secret;

  @Value("${jwt.expiration:86400000}")
  private long expiration;

  private SecretKey getSigningKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(String username, List<String> roles, Long userId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    String token = Jwts
      .builder()
      .subject(username)
      .claim("userId", userId)
      .claim("roles", roles)
      .issuedAt(now)
      .expiration(expiryDate)
      .signWith(getSigningKey())
      .compact();

    System.out.println("========================================");
    System.out.println("✅ JWT Token 已生成");
    System.out.println("📋 Token前50字符: " + (token.length() > 50 ? token.substring(0, 50) + "..." : token));
    System.out.println("📏 Token总长度: " + token.length());
    System.out.println("👤 用户名: " + username);
    System.out.println("🆔 用户ID: " + userId);
    System.out.println("🎭 角色: " + roles);
    System.out.println("⏰ 过期时间: " + expiryDate.toString());
    System.out.println("========================================");

    return token;
  }

  public Claims parseToken(String token) {
    try {
      return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    } catch (Exception e) {
      System.err.println("❌ JWT解析失败: " + e.getMessage());
      throw new RuntimeException("无效的JWT Token", e);
    }
  }

  public String getUsernameFromToken(String token) {
    return parseToken(token).getSubject();
  }

  public Long getUserIdFromToken(String token) {
    return parseToken(token).get("userId", Long.class);
  }

  @SuppressWarnings("unchecked")
  public List<String> getRolesFromToken(String token) {
    return parseToken(token).get("roles", List.class);
  }

  public boolean validateToken(String token) {
    try {
      parseToken(token);
      return true;
    } catch (Exception e) {
      System.err.println("❌ JWT验证失败: " + e.getMessage());
      return false;
    }
  }

  public boolean isTokenExpired(String token) {
    try {
      Date expiration = parseToken(token).getExpiration();
      return expiration.before(new Date());
    } catch (Exception e) {
      return true;
    }
  }
}
