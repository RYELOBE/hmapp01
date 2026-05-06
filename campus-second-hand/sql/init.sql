CREATE DATABASE IF NOT EXISTS campus_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_trade;

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色: user/admin',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0正常/1禁用',
  `school` VARCHAR(100) DEFAULT NULL COMMENT '学校',
  `student_id` VARCHAR(50) DEFAULT NULL COMMENT '学号',
  `is_verified` TINYINT DEFAULT 0 COMMENT '校园认证: 0未认证/1已认证',
  `credit_score` INT DEFAULT 60 COMMENT '信用分',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_school` (`school`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `product_count` INT DEFAULT 0 COMMENT '商品数量',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0禁用/1启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `title` VARCHAR(200) NOT NULL COMMENT '商品标题',
  `description` TEXT COMMENT '商品描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `images` JSON COMMENT '图片JSON数组',
  `condition` VARCHAR(20) DEFAULT NULL COMMENT '成色',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0待审核/1已通过/2已拒绝/3已下架',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `favorite_count` INT DEFAULT 0 COMMENT '收藏数',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
  `delivery_type` VARCHAR(20) DEFAULT '都可以' COMMENT '交易方式',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_seller` (`seller_id`),
  KEY `idx_status` (`status`),
  KEY `idx_price` (`price`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE `order_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0待付款/1待发货/2待收货/3已完成/4已取消/5退款申请/6已退款',
  `address` VARCHAR(500) DEFAULT NULL COMMENT '收货地址',
  `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人',
  `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货电话',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `confirm_time` DATETIME DEFAULT NULL COMMENT '确认收货时间',
  `refund_reason` VARCHAR(500) DEFAULT NULL COMMENT '退款原因',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer` (`buyer_id`),
  KEY `idx_seller` (`seller_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `quantity` INT DEFAULT 1 COMMENT '数量',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

CREATE TABLE `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

CREATE TABLE `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_user_id` BIGINT DEFAULT NULL COMMENT '发送者ID',
  `to_user_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `type` TINYINT DEFAULT 0 COMMENT '类型: 0系统消息/1用户消息',
  `is_read` TINYINT DEFAULT 0 COMMENT '已读: 0未读/1已读',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_from_user` (`from_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

CREATE TABLE `review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `from_user_id` BIGINT NOT NULL COMMENT '评价者ID',
  `to_user_id` BIGINT NOT NULL COMMENT '被评价者ID',
  `rating` TINYINT NOT NULL COMMENT '评分1-5',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

INSERT INTO `category` (`name`, `icon`, `sort_order`) VALUES
('📚 书籍教材', 'book', 1),
('💻 电子产品', 'laptop', 2),
('🏠 生活用品', 'home', 3),
('👗 服饰鞋包', 'shirt', 4),
('⚽ 运动健身', 'sport', 5),
('🎫 票券卡类', 'ticket', 6),
('🔌 数码配件', 'phone', 7),
('📦 其他分类', 'box', 8);

INSERT INTO `user` (`username`, `password`, `phone`, `role`, `is_verified`, `credit_score`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '13800138000', 'admin', 1, 100);
