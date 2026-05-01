-- 插入运营账号数据
INSERT IGNORE INTO ops_account (id, username, password, nickname, roles, role_level, status) VALUES
  (2001, 'ops_admin', '123456', '运营管理员', 'OPS_ADMIN', 'OPS_ADMIN', 'ACTIVE'),
  (2002, 'ops_super', '123456', '超级管理员', 'OPS_ADMIN,OPS_SUPER', 'OPS_SUPER', 'ACTIVE');

-- 插入用户数据
INSERT IGNORE INTO user_account (id, username, password, nickname, roles) VALUES
  (1001, 'buyer', '123456', '买家同学', 'BUYER'),
  (1002, 'seller', '123456', '卖家同学', 'SELLER'),
  (1003, 'ops', '123456', '运营同学', 'OPS');

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

-- 插入预置子应用注册配置
INSERT IGNORE INTO app_register (app_code, title, entry, path_prefix, roles) VALUES
  ('portal', '门户（买家/卖家）', 'http://localhost:7101', '/portal', 'BUYER,SELLER'),
  ('ops', '运营中心', 'http://localhost:7102', '/ops', 'OPS');

-- 更新子应用入口URL
UPDATE app_register SET entry = 'http://localhost:7101' WHERE app_code = 'portal';
UPDATE app_register SET entry = 'http://localhost:7102' WHERE app_code = 'ops';

-- 插入预置门户配置
INSERT IGNORE INTO portal_config (portal_code, portal_name, template_type, config_json) VALUES
  ('portal', '门户（买家/卖家）', 'backstage', '{"theme":{"background":"linear-gradient(135deg, #d88c1f 0%, #f0a838 100%)","textTheme":"light"},"logoConfig":{"systemName":"Campus Trade","showLogoTitle":true},"showLeftMenu":true,"showQuickEntry":true}'),
  ('ops', '运营中心', 'backstage', '{"theme":{"background":"linear-gradient(135deg, #336ad8 0%, #6d9fff 100%)","textTheme":"light"},"logoConfig":{"systemName":"运营中心","showLogoTitle":true},"showLeftMenu":true,"showQuickEntry":false}');

-- 插入预置资源菜单
INSERT IGNORE INTO resource_menu (id, parent_id, menu_code, menu_name, menu_type, app_code, path, icon, sort_order) VALUES
  (1, 0, 'buyer_home', '买家首页', 'MENU', 'portal', '/portal/home', 'icon-home', 1),
  (2, 0, 'buyer_items', '商品浏览', 'MENU', 'portal', '/portal/buyer/items', 'icon-apps', 2),
  (3, 0, 'buyer_orders', '我的订单', 'MENU', 'portal', '/portal/orders', 'icon-list', 3),
  (4, 0, 'seller_items', '我的商品', 'MENU', 'portal', '/portal/seller/items', 'icon-storage', 4),
  (5, 0, 'seller_orders', '卖家订单', 'MENU', 'portal', '/portal/orders', 'icon-file', 5),
  (10, 0, 'ops_dashboard', '数据概览', 'MENU', 'ops', '/ops/dashboard', 'icon-dashboard', 1),
  (11, 0, 'ops_reviews', '商品审核', 'MENU', 'ops', '/ops/reviews', 'icon-check-circle', 2),
  (12, 0, 'ops_orders', '订单监控', 'MENU', 'ops', '/ops/orders', 'icon-order', 3),
  (13, 0, 'ops_app_register', '子应用管理', 'MENU', 'ops', '/ops/app-register', 'icon-settings', 4),
  (14, 0, 'ops_portal_design', '门户设计', 'MENU', 'ops', '/ops/portal-design', 'icon-palette', 5),
  (15, 0, 'ops_role_manage', '角色管理', 'MENU', 'ops', '/ops/role-manage', 'icon-user-group', 6),
  (16, 0, 'ops_resource_manage', '资源管理', 'MENU', 'ops', '/ops/resource-manage', 'icon-menu', 7);

-- 插入预置角色-资源关联
INSERT IGNORE INTO role_resource (role_code, resource_id) VALUES
  ('BUYER', 1), ('BUYER', 2), ('BUYER', 3),
  ('SELLER', 4), ('SELLER', 5),
  ('OPS', 10), ('OPS', 11), ('OPS', 12), ('OPS', 13), ('OPS', 14), ('OPS', 15), ('OPS', 16);

