package com.campus.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.campus") // 加这一行！
public class MarketplaceApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(MarketplaceApiApplication.class, args);
  }
}
