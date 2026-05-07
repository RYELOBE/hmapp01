-- 插入运营账号数据
INSERT IGNORE INTO ops_account (id, username, password, nickname, roles, role_level, status) VALUES
  (2001, 'ops_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '运营管理员', 'OPS_ADMIN', 'OPS_ADMIN', 'ACTIVE'),
  (2002, 'ops_super', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 'OPS_ADMIN,OPS_SUPER', 'OPS_SUPER', 'ACTIVE');

-- 插入用户数据
INSERT IGNORE INTO user_account (id, username, password, nickname, roles) VALUES
  (1001, 'buyer', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '买家同学', 'BUYER'),
  (1002, 'seller', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '卖家同学', 'SELLER'),
  (1003, 'ops', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '运营同学', 'OPS');

-- 插入商品数据
INSERT IGNORE INTO item (id, title, price, description, seller_id, seller_name, review_status, reject_reason) VALUES
  (3001, '九成新机械键盘', 199, '蓝牙双模，配件齐全', 1002, '卖家同学', 'APPROVED', NULL),
  (3002, '宿舍显示器 24 寸', 399, '自提优先', 1002, '卖家同学', 'PENDING_REVIEW', NULL),
  (3003, '二手 iPad 64G', 1699, '仅限校内面交', 1002, '卖家同学', 'REJECTED', '描述信息不完整');

-- 插入订单数据
INSERT IGNORE INTO orders (id, order_no, item_id, item_title, item_image, buyer_id, seller_id, price, quantity, total_amount, status, buyer_name, seller_name) VALUES
  (9001, 'ORD20250501001', 3001, '九成新机械键盘', NULL, 1001, 1002, 199, 1, 199, 'PENDING_PAYMENT', '买家同学', '卖家同学');

-- 插入知识库数据
INSERT IGNORE INTO knowledge_chunk (source_type, title, content) VALUES
  ('rules', '平台规则', '卖家发布商品后必须经过运营审核通过才可被买家看到。'),
  ('workflow', '审核流程', '审核状态有 PENDING、APPROVED、REJECTED。驳回时需填写原因。'),
  ('faq', '交易FAQ', '买家下单后可在我的订单查看状态，卖家可在订单页查看待发货列表。');

