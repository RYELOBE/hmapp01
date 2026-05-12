package com.campus.marketplace.config;

import com.campus.marketplace.repository.NotificationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataMigrationRunner implements CommandLineRunner {

  private final NotificationRepository notificationRepository;

  public DataMigrationRunner(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @Override
  public void run(String... args) {
    // 启动时迁移通知类型
    try {
      notificationRepository.migrateNotificationTypes();
      System.out.println("✅ 通知类型迁移完成");
    } catch (Exception e) {
      System.out.println("⚠️ 通知类型迁移跳过: " + e.getMessage());
    }
  }
}
