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
      createNotificationTable();
      System.out.println("✅ 数据库结构更新完成");
    } catch (Exception e) {
      System.err.println("❌ 数据库更新失败: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void createNotificationTable() {
    try {
      String createTableSql = """
          CREATE TABLE IF NOT EXISTS notification (
              id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
              title VARCHAR(255) NOT NULL COMMENT '消息标题',
              content TEXT NOT NULL COMMENT '消息内容',
              type VARCHAR(50) NOT NULL COMMENT '消息类型：REVIEW-审核通知, ORDER-订单通知, ITEM-商品通知, CIRCLE-圈子通知',
              is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
              receiver_id BIGINT COMMENT '接收用户ID，为空表示全体用户',
              sender_id BIGINT COMMENT '发送用户ID',
              sender_name VARCHAR(100) COMMENT '发送用户名称',
              business_id VARCHAR(100) COMMENT '关联业务ID',
              business_type VARCHAR(50) COMMENT '关联业务类型',
              priority VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '优先级：LOW-低, MEDIUM-中, HIGH-高, URGENT-紧急',
              status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '消息状态：ACTIVE-有效, DELETED-已删除',
              created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
              read_at DATETIME COMMENT '阅读时间',
              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
              deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
              INDEX idx_receiver_id (receiver_id),
              INDEX idx_type (type),
              INDEX idx_is_read (is_read),
              INDEX idx_created_at (created_at),
              INDEX idx_status (status)
          ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表'
          """;
      jdbcTemplate.execute(createTableSql);
      System.out.println("  ✓ 创建 notification 表");
    } catch (Exception e) {
      System.out.println("  - notification 表已存在");
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
      jdbcTemplate.execute("ALTER TABLE circle_comment ADD COLUMN status VARCHAR(20) DEFAULT 'PENDING'");
      System.out.println("  ✓ 新增 circle_comment.status 列");
    } catch (Exception e) {
      System.out.println("  - circle_comment.status 已存在");
    }

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
