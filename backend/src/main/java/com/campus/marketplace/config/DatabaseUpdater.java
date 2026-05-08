package com.campus.marketplace.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseUpdater {

  private final JdbcTemplate jdbcTemplate;

  public DatabaseUpdater(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @PostConstruct
  public void updateDatabase() {
    try {
      updateReviewTable();
      updateUserTable();
      System.out.println("✅ 数据库结构更新完成");
    } catch (Exception e) {
      System.err.println("❌ 数据库更新失败: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void updateReviewTable() {
    try {
      jdbcTemplate.execute("ALTER TABLE review MODIFY COLUMN order_id BIGINT NULL");
      System.out.println("  ✓ review.order_id 改为可空");
    } catch (Exception e) {
      System.out.println("  - review.order_id 已是可空");
    }

    try {
      jdbcTemplate.execute("ALTER TABLE review ADD COLUMN seller_id BIGINT NULL AFTER order_id");
      System.out.println("  ✓ 新增 review.seller_id 列");
    } catch (Exception e) {
      System.out.println("  - review.seller_id 已存在");
    }

    try {
      jdbcTemplate.execute("ALTER TABLE review ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' AFTER images");
      System.out.println("  ✓ 新增 review.status 列 (默认 PENDING)");
    } catch (Exception e) {
      System.out.println("  - review.status 已存在");
    }

    try {
      jdbcTemplate.execute("ALTER TABLE review ADD INDEX idx_seller_id (seller_id)");
      System.out.println("  ✓ 新增 idx_seller_id 索引");
    } catch (Exception e) {
      System.out.println("  - idx_seller_id 已存在");
    }

    try {
      jdbcTemplate.execute("ALTER TABLE review ADD INDEX idx_status (status)");
      System.out.println("  ✓ 新增 idx_status 索引");
    } catch (Exception e) {
      System.out.println("  - idx_status 已存在");
    }
  }

  private void updateUserTable() {
    try {
      jdbcTemplate.execute("ALTER TABLE user_account ADD COLUMN avatar VARCHAR(512) DEFAULT '' AFTER campus");
      System.out.println("  ✓ 新增 user_account.avatar 列");
    } catch (Exception e) {
      System.out.println("  - user_account.avatar 已存在");
    }

    try {
      jdbcTemplate.execute("ALTER TABLE user_account ADD COLUMN email VARCHAR(128) DEFAULT '' AFTER avatar");
      System.out.println("  ✓ 新增 user_account.email 列");
    } catch (Exception e) {
      System.out.println("  - user_account.email 已存在");
    }

    try {
      jdbcTemplate.execute("ALTER TABLE user_account ADD COLUMN bio TEXT NULL AFTER email");
      System.out.println("  ✓ 新增 user_account.bio 列");
    } catch (Exception e) {
      System.out.println("  - user_account.bio 已存在");
    }
  }
}
