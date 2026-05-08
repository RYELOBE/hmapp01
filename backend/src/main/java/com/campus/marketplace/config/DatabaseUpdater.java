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
    } catch (Exception e) {
      System.err.println("数据库更新失败: " + e.getMessage());
    }
  }

  private void updateReviewTable() {
    try {
      jdbcTemplate.execute("ALTER TABLE review MODIFY COLUMN order_id BIGINT NULL");
    } catch (Exception e) {
      System.out.println("order_id 列已经是 NULL 或者不需要修改: " + e.getMessage());
    }

    try {
      jdbcTemplate.execute("ALTER TABLE review ADD COLUMN seller_id BIGINT NULL AFTER order_id");
    } catch (Exception e) {
      System.out.println("seller_id 列已存在或者不需要添加: " + e.getMessage());
    }

    try {
      jdbcTemplate.execute("ALTER TABLE review ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' AFTER images");
    } catch (Exception e) {
      System.out.println("status 列已存在或者不需要添加: " + e.getMessage());
    }

    try {
      jdbcTemplate.execute("ALTER TABLE review ADD INDEX idx_seller_id (seller_id)");
    } catch (Exception e) {
      System.out.println("idx_seller_id 索引已存在或者不需要添加: " + e.getMessage());
    }

    try {
      jdbcTemplate.execute("ALTER TABLE review ADD INDEX idx_status (status)");
    } catch (Exception e) {
      System.out.println("idx_status 索引已存在或者不需要添加: " + e.getMessage());
    }

    System.out.println("数据库 review 表更新完成");
  }
}
