-- 插入运营账号数据
INSERT IGNORE INTO ops_account (id, username, password, nickname, roles, role_level, status) VALUES
  (2001, 'ops_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '运营管理员', 'OPS_ADMIN', 'OPS_ADMIN', 'ACTIVE'),
  (2002, 'ops_super', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 'OPS_ADMIN,OPS_SUPER', 'OPS_SUPER', 'ACTIVE');

-- 插入用户数据
INSERT IGNORE INTO user_account (id, username, password, nickname, roles, campus, phone) VALUES
  (1001, 'buyer', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '小明同学', 'BUYER', '东校区', '13800138001'),
  (1002, 'seller', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '小红学姐', 'SELLER', '西校区', '13800138002'),
  (1003, 'ops', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '运营同学', 'OPS', '主校区', '13800138003'),
  (1004, 'student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '小李学长', 'BUYER,SELLER', '东校区', '13800138004'),
  (1005, 'student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '小张学妹', 'BUYER,SELLER', '西校区', '13800138005'),
  (1006, 'student3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '小王同学', 'BUYER,SELLER', '主校区', '13800138006');

-- 插入商品数据 - 电子产品
INSERT IGNORE INTO item (id, title, price, description, seller_id, seller_name, category, condition_level, campus, review_status, reject_reason, image_urls) VALUES
  (3001, 'Apple AirPods Pro 2代', 1299, '使用半年，功能完好，配件齐全含充电盒', 1002, '小红学姐', 'ELECTRONICS', 'LIKE_NEW', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=Apple%20AirPods%20Pro%202%20wireless%20earbuds%20white%20charging%20case%20product%20photo%20white%20background&image_size=square\"]'),
  (3002, 'MacBook Pro 14寸 M2 Pro', 9999, '2023款，16GB+512GB，电池健康95%，带原装充电器', 1004, '小李学长', 'ELECTRONICS', 'LIKE_NEW', '东校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=MacBook%20Pro%2014%20inch%20laptop%20silver%20apple%20product%20photo%20clean%20desk&image_size=landscape_16_9\"]'),
  (3003, 'iPhone 14 Pro 256GB', 5999, '深空黑色，屏幕完美，无磕碰，电池健康90%', 1002, '小红学姐', 'ELECTRONICS', 'EXCELLENT', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=iPhone%2014%20Pro%20smartphone%20black%20color%20back%20view%20product%20photo&image_size=square\"]'),
  (3004, '小米无线充电宝 20000mAh', 129, '支持65W快充，轻薄便携，几乎全新', 1005, '小张学妹', 'ELECTRONICS', 'LIKE_NEW', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=xiaomi%20power%20bank%2020000mah%20white%20portable%20charger%20product%20photo&image_size=square\"]'),
  (3005, '罗技MX Master 3S鼠标', 399, '无线蓝牙双模，办公神器，手感极佳', 1006, '小王同学', 'ELECTRONICS', 'EXCELLENT', '主校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=logitech%20mx%20master%203s%20wireless%20mouse%20black%20ergonomic%20product%20photo&image_size=square\"]'),
  (3006, '索尼WH-1000XM5耳机', 1899, '旗舰降噪耳机，音质顶级，使用一年', 1004, '小李学长', 'ELECTRONICS', 'EXCELLENT', '东校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=sony%20wh1000xm5%20wireless%20headphones%20black%20over%20ear%20product%20photo&image_size=square\"]'),
  (3007, '华为MatePad Pro 12.6', 2999, '2022款，8GB+256GB，带原装笔和键盘', 1005, '小张学妹', 'ELECTRONICS', 'GOOD', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=huawei%20matepad%20pro%20tablet%20with%20keyboard%20and%20pen%20silver%20color&image_size=landscape_16_9\"]'),
  (3008, '机械键盘 Cherry轴', 299, 'RGB背光，青轴，9成新，手感超好', 1006, '小王同学', 'ELECTRONICS', 'EXCELLENT', '主校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=mechanical%20keyboard%20rgb%20lighting%20black%20cherry%20switches%20gaming&image_size=landscape_16_9\"]');

-- 插入商品数据 - 图书教材
INSERT IGNORE INTO item (id, title, price, description, seller_id, seller_name, category, condition_level, campus, review_status, reject_reason, image_urls) VALUES
  (3009, '高等数学同济第七版 上下册', 45, '考研必备，有少量笔记标注，不影响阅读', 1001, '小明同学', 'BOOKS', 'GOOD', '东校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=chinese%20higher%20mathematics%20textbook%20two%20volumes%20blue%20cover&image_size=square\"]'),
  (3010, '线性代数 第五版', 28, '清华大学出版社，九成新，无笔记', 1002, '小红学姐', 'BOOKS', 'EXCELLENT', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=linear%20algebra%20textbook%20chinese%20purple%20cover%20academic&image_size=square\"]'),
  (3011, 'Python编程从入门到实践', 35, '第二版，适合入门，有少量划线', 1004, '小李学长', 'BOOKS', 'GOOD', '东校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=python%20programming%20book%20yellow%20cover%20beginner%20guide&image_size=square\"]'),
  (3012, '数据结构与算法分析', 55, '经典教材，英文版第三版，保存完好', 1005, '小张学妹', 'BOOKS', 'LIKE_NEW', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=data%20structures%20algorithms%20textbook%20blue%20cover%20english%20version&image_size=square\"]'),
  (3013, '考研英语真题解析 2020-2024', 68, '全套真题，答案详解，标记重点', 1006, '小王同学', 'BOOKS', 'GOOD', '主校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=chinese%20graduate%20entrance%20exam%20english%20past%20papers%20study%20book&image_size=square\"]'),
  (3014, '现代汉语词典 第七版', 85, '正版全新，塑封刚拆，适合语文学习', 1001, '小明同学', 'BOOKS', 'LIKE_NEW', '东校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=chinese%20modern%20dictionary%20red%20cover%20thick%20book&image_size=square\"]');

-- 插入商品数据 - 生活用品
INSERT IGNORE INTO item (id, title, price, description, seller_id, seller_name, category, condition_level, campus, review_status, reject_reason, image_urls) VALUES
  (3015, '宜家书架 白色', 120, '四层书架，可拆卸，毕业转让', 1002, '小红学姐', 'DAILY', 'GOOD', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=ikea%20white%20bookshelf%20four%20tiers%20minimalist%20design&image_size=square\"]'),
  (3016, '台灯 LED护眼', 45, '可调节亮度色温，USB充电', 1004, '小李学长', 'DAILY', 'EXCELLENT', '东校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=led%20desk%20lamp%20modern%20design%20white%20eye%20protection&image_size=square\"]'),
  (3017, '收纳箱套装 3个', 35, '大号收纳箱，透明可视，搬家必备', 1005, '小张学妹', 'DAILY', 'LIKE_NEW', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=transparent%20storage%20boxes%20set%20of%20three%20plastic%20organizer&image_size=square\"]'),
  (3018, '床上书桌', 55, '可折叠，带抽屉，稳固耐用', 1006, '小王同学', 'DAILY', 'GOOD', '主校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=bed%20desk%20foldable%20wooden%20laptop%20stand%20student%20dorm&image_size=square\"]'),
  (3019, '电风扇 静音', 75, '台式小风扇，三档风速，夏天必备', 1001, '小明同学', 'DAILY', 'EXCELLENT', '东校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=small%20desk%20fan%20white%20quiet%20portable%20three%20speeds&image_size=square\"]'),
  (3020, '保温杯 500ml', 28, '316不锈钢，保温12小时', 1002, '小红学姐', 'DAILY', 'LIKE_NEW', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=stainless%20steel%20thermos%20bottle%20500ml%20minimalist%20design&image_size=square\"]');

-- 插入商品数据 - 运动器材
INSERT IGNORE INTO item (id, title, price, description, seller_id, seller_name, category, condition_level, campus, review_status, reject_reason, image_urls) VALUES
  (3021, '篮球 斯伯丁', 85, '室外用球，手感好，八成新', 1004, '小李学长', 'SPORTS', 'GOOD', '东校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=spalding%20basketball%20orange%20outdoor%20ball%20product%20photo&image_size=square\"]'),
  (3022, '羽毛球拍 尤尼克斯', 189, '入门级球拍，送球和手胶', 1005, '小张学妹', 'SPORTS', 'EXCELLENT', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=yonex%20badminton%20racket%20red%20professional%20sports%20equipment&image_size=square\"]'),
  (3023, '瑜伽垫 加厚', 35, '6mm厚度，防滑耐用，几乎全新', 1006, '小王同学', 'SPORTS', 'LIKE_NEW', '主校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=yoga%20mat%20purple%20thick%20non%20slip%20fitness%20exercise&image_size=square\"]'),
  (3024, '跑步鞋 耐克', 299, 'Air Zoom系列，42码，穿过几次', 1001, '小明同学', 'SPORTS', 'EXCELLENT', '东校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=nike%20running%20shoes%20black%20white%20athletic%20sneakers&image_size=square\"]'),
  (3025, '乒乓球拍 红双喜', 65, '四星拍，双面反胶，入门首选', 1002, '小红学姐', 'SPORTS', 'GOOD', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=red%20double%20happiness%20ping%20pong%20paddle%20table%20tennis%20racket&image_size=square\"]');

-- 插入商品数据 - 服饰鞋包
INSERT IGNORE INTO item (id, title, price, description, seller_id, seller_name, category, condition_level, campus, review_status, reject_reason, image_urls) VALUES
  (3026, '羽绒服 波司登', 399, '女士M码，白色，保暖性好', 1002, '小红学姐', 'CLOTHING', 'GOOD', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=bosideng%20down%20jacket%20white%20women%20winter%20coat&image_size=square\"]'),
  (3027, '牛仔裤 Levi\'s', 159, '男士30码，经典直筒款', 1004, '小李学长', 'CLOTHING', 'EXCELLENT', '东校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=levis%20jeans%20blue%20denim%20classic%20straight%20leg&image_size=square\"]'),
  (3028, '双肩背包 小米', 89, '大容量电脑包，防水材质', 1005, '小张学妹', 'CLOTHING', 'LIKE_NEW', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=xiaomi%20backpack%20black%20laptop%20bag%20minimalist%20design&image_size=square\"]'),
  (3029, '帆布鞋 匡威', 199, '经典款黑色，38码', 1006, '小王同学', 'CLOTHING', 'EXCELLENT', '主校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=converse%20canvas%20shoes%20black%20classic%20sneakers&image_size=square\"]');

-- 插入商品数据 - 美妆护肤
INSERT IGNORE INTO item (id, title, price, description, seller_id, seller_name, category, condition_level, campus, review_status, reject_reason, image_urls) VALUES
  (3030, 'SK-II神仙水 230ml', 799, '正品，用了一半，有效期到2026', 1002, '小红学姐', 'BEAUTY', 'GOOD', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=skii%20facial%20treatment%20essence%20bottle%20230ml%20skincare&image_size=square\"]'),
  (3031, 'MAC口红 Chili', 89, '热门色号，只用过几次', 1005, '小张学妹', 'BEAUTY', 'EXCELLENT', '西校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=mac%20lipstick%20chili%20color%20red%20tube%20cosmetics&image_size=square\"]'),
  (3032, '雅诗兰黛眼霜', 299, '小棕瓶眼霜，剩余三分之二', 1001, '小明同学', 'BEAUTY', 'GOOD', '东校区', 'APPROVED', NULL, '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=estee%20lauder%20eye%20cream%20small%20brown%20bottle%20skincare&image_size=square\"]');

-- 插入待审核和驳回商品
INSERT IGNORE INTO item (id, title, price, description, seller_id, seller_name, category, condition_level, campus, review_status, reject_reason) VALUES
  (3033, '二手游戏主机 PS5', 3999, '数字版，带手柄和电源线', 1004, '小李学长', 'ELECTRONICS', 'EXCELLENT', '东校区', 'PENDING_REVIEW', NULL),
  (3034, '显示器支架', 89, '可调节高度角度', 1006, '小王同学', 'DAILY', 'LIKE_NEW', '主校区', 'PENDING_REVIEW', NULL),
  (3035, '旧手机 iPhone X', 500, '屏幕有裂痕，功能正常', 1001, '小明同学', 'ELECTRONICS', 'FAIR', '东校区', 'REJECTED', '商品描述不完整，请补充更多信息');

-- 插入订单数据
INSERT IGNORE INTO orders (id, order_no, item_id, item_title, item_image, buyer_id, seller_id, price, quantity, total_amount, status, buyer_name, seller_name, receiver_address, receiver_phone) VALUES
  (9001, 'ORD20250501001', 3001, 'Apple AirPods Pro 2代', NULL, 1001, 1002, 1299, 1, 1299, 'PENDING_PAYMENT', '小明同学', '小红学姐', '东校区3号楼', '13800138001'),
  (9002, 'ORD20250502002', 3009, '高等数学同济第七版 上下册', NULL, 1005, 1001, 45, 1, 45, 'COMPLETED', '小张学妹', '小明同学', '西校区5号楼', '13800138005'),
  (9003, 'ORD20250503003', 3015, '宜家书架 白色', NULL, 1006, 1002, 120, 1, 120, 'SHIPPING', '小王同学', '小红学姐', '主校区1号楼', '13800138006'),
  (9004, 'ORD20250504004', 3021, '篮球 斯伯丁', NULL, 1001, 1004, 85, 1, 85, 'PENDING_SHIPMENT', '小明同学', '小李学长', '东校区2号楼', '13800138001');

-- 插入校园圈子帖子数据
INSERT IGNORE INTO circle_post (id, user_id, user_name, content, images, likes, comments, create_time, campus) VALUES
  (4001, 1002, '小红学姐', '出闲置！毕业季清仓，有需要的同学可以看看，都是九成新的生活用品~', '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=student%20dorm%20room%20clean%20organized%20second%20hand%20items&image_size=landscape_16_9\"]', 23, 5, '2025-05-01 10:30:00', '西校区'),
  (4002, 1004, '小李学长', '求购二手笔记本电脑，预算5000以内，最好是MacBook或者ThinkPad', '[]', 12, 8, '2025-05-02 14:20:00', '东校区'),
  (4003, 1005, '小张学妹', '图书馆占座神器！这个坐垫真的太舒服了，久坐不累~', '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=comfortable%20cushion%20library%20study%20student%20seat%20pad&image_size=square\"]', 45, 12, '2025-05-03 09:15:00', '西校区'),
  (4004, 1006, '小王同学', '运动会快到了，有没有一起组队跑步的？每天晚上操场见！', '[]', 31, 15, '2025-05-04 16:45:00', '主校区'),
  (4005, 1001, '小明同学', '分享一下我的考研经验，数学从70到120的逆袭之路！', '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=study%20desk%20with%20books%20notebooks%20stationery%20exam%20preparation&image_size=landscape_16_9\"]', 89, 34, '2025-05-05 11:00:00', '东校区'),
  (4006, 1002, '小红学姐', '出一台iPad，考研用不上了，2021款128G，价格可议', '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=ipad%20tablet%20on%20desk%20with%20apple%20pencil%20notes&image_size=landscape_16_9\"]', 56, 21, '2025-05-06 13:30:00', '西校区'),
  (4007, 1004, '小李学长', '周末有没有一起去爬山的？天气这么好不要宅在宿舍！', '[]', 28, 19, '2025-05-07 08:00:00', '东校区'),
  (4008, 1005, '小张学妹', '推荐一家超好吃的食堂窗口！三食堂二楼的麻辣香锅绝了', '[\"https://neeko-copilot.bytedance.net/api/text_to_image?prompt=spicy%20chinese%20mala%20xiang%20guo%20food%20bowl%20campus%20canteen&image_size=square\"]', 67, 28, '2025-05-07 12:00:00', '西校区');

-- 插入圈子评论数据
INSERT IGNORE INTO circle_comment (id, post_id, user_id, user_name, content, create_time) VALUES
  (5001, 4001, 1001, '小明同学', '学姐，请问有台灯吗？', '2025-05-01 10:35:00'),
  (5002, 4001, 1002, '小红学姐', '有的，LED护眼台灯，45元出', '2025-05-01 10:38:00'),
  (5003, 4002, 1006, '小王同学', '我有一台MacBook Pro 14寸，去年买的，你感兴趣吗？', '2025-05-02 14:30:00'),
  (5004, 4003, 1001, '小明同学', '求链接！！', '2025-05-03 09:20:00'),
  (5005, 4005, 1005, '小张学妹', '学长太强了！求资料分享', '2025-05-05 11:15:00');

-- 插入圈子点赞数据
INSERT IGNORE INTO circle_like (id, post_id, user_id) VALUES
  (6001, 4001, 1001),
  (6002, 4001, 1004),
  (6003, 4002, 1005),
  (6004, 4003, 1001),
  (6005, 4003, 1004),
  (6006, 4003, 1006),
  (6007, 4004, 1001),
  (6008, 4005, 1002),
  (6009, 4005, 1004),
  (6010, 4005, 1005);

-- 插入消息数据
INSERT IGNORE INTO message (id, sender_id, sender_name, receiver_id, content, type, status, create_time) VALUES
  (7001, 1002, '小红学姐', 1001, '你好，AirPods还在吗？我想要', 'ORDER', 'UNREAD', '2025-05-01 10:00:00'),
  (7002, 1004, '小李学长', 1001, '高数书我想要，什么时候方便取？', 'ORDER', 'READ', '2025-05-02 11:30:00'),
  (7003, 10000, '系统', 1001, '您的订单 ORD20250502002 已完成', 'SYSTEM', 'READ', '2025-05-03 16:00:00'),
  (7004, 1006, '小王同学', 1001, '书架还在吗？我想要', 'ORDER', 'UNREAD', '2025-05-04 09:15:00'),
  (7005, 10000, '系统', 1002, '您的商品已通过审核', 'SYSTEM', 'READ', '2025-05-05 10:00:00');

-- 插入收藏数据
INSERT IGNORE INTO favorite (id, user_id, item_id) VALUES
  (8001, 1001, 3001),
  (8002, 1001, 3003),
  (8003, 1004, 3002),
  (8004, 1005, 3015),
  (8005, 1006, 3024);

-- 插入购物车数据
INSERT IGNORE INTO cart_item (id, user_id, item_id, quantity) VALUES
  (9001, 1001, 3001, 1),
  (9002, 1001, 3009, 1),
  (9003, 1004, 3021, 1);

-- 插入知识库数据
INSERT IGNORE INTO knowledge_chunk (source_type, title, content) VALUES
  ('rules', '平台规则', '卖家发布商品后必须经过运营审核通过才可被买家看到。禁止发布违法违规商品。'),
  ('workflow', '审核流程', '审核状态有 PENDING_REVIEW（待审核）、APPROVED（已通过）、REJECTED（已驳回）。驳回时需填写原因。'),
  ('faq', '交易FAQ', '买家下单后可在我的订单查看状态，卖家可在订单页查看待发货列表。支持校内自提和快递配送。'),
  ('faq', '支付方式', '支持微信支付和支付宝支付，交易完成后款项将打入卖家账户。'),
  ('rules', '售后政策', '商品售出后7天内如发现质量问题可申请退换，需提供相关证明。');

-- 插入地址数据
INSERT IGNORE INTO address (id, user_id, receiver_name, receiver_phone, province, city, district, detail_address, is_default) VALUES
  (1001, 1001, '小明同学', '13800138001', '北京市', '北京市', '海淀区', '东校区3号楼302室', 1),
  (1002, 1002, '小红学姐', '13800138002', '北京市', '北京市', '海淀区', '西校区5号楼201室', 1),
  (1003, 1004, '小李学长', '13800138004', '北京市', '北京市', '海淀区', '东校区2号楼503室', 1),
  (1004, 1005, '小张学妹', '13800138005', '北京市', '北京市', '海淀区', '西校区1号楼405室', 1),
  (1005, 1006, '小王同学', '13800138006', '北京市', '北京市', '海淀区', '主校区4号楼102室', 1);