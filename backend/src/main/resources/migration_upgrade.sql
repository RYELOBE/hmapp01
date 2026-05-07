-- ── 1. 扩展 review 表（评价表）────────────────────
ALTER TABLE review 
ADD COLUMN IF NOT EXISTS content TEXT NULL COMMENT '评价内容(富文本HTML)' AFTER rating,
ADD COLUMN IF NOT EXISTS images TEXT NULL COMMENT '评价图片JSON数组' AFTER content,
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED' AFTER images,
ADD COLUMN IF NOT EXISTS seller_id BIGINT NOT NULL DEFAULT 0 COMMENT '卖家ID' AFTER buyer_id,
ADD COLUMN IF NOT EXISTS reply_content TEXT NULL COMMENT '卖家回复内容' AFTER status,
ADD COLUMN IF NOT EXISTS reply_time DATETIME NULL COMMENT '回复时间' AFTER reply_content;

CREATE INDEX IF NOT EXISTS idx_review_status ON review(status);
CREATE INDEX IF NOT EXISTS idx_review_item_id ON review(item_id);
CREATE INDEX IF NOT EXISTS idx_review_create_time ON review(create_time);

-- ── 2. 创建 circle_post 表（圈子帖子表）─────────────
CREATE TABLE IF NOT EXISTS circle_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  content TEXT NOT NULL COMMENT '内容(富文本)',
  images TEXT COMMENT '图片JSON数组',
  tags VARCHAR(200) COMMENT '标签，逗号分隔',
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: DRAFT/PENDING/APPROVED/REJECTED',
  like_count INT DEFAULT 0 COMMENT '点赞数',
  comment_count INT DEFAULT 0 COMMENT '评论数',
  view_count INT DEFAULT 0 COMMENT '浏览数',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='圈子帖子表';

CREATE INDEX IF NOT EXISTS idx_circle_post_status ON circle_post(status);
CREATE INDEX IF NOT EXISTS idx_circle_post_user_id ON circle_post(user_id);
CREATE INDEX IF NOT EXISTS idx_circle_post_create_time ON circle_post(create_time);

-- ── 3. 创建 circle_comment 表（圈子评论表）──────────
CREATE TABLE IF NOT EXISTS circle_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL COMMENT '帖子ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  content VARCHAR(1000) NOT NULL COMMENT '评论内容',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (post_id) REFERENCES circle_post(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='圈子评论表';

CREATE INDEX IF NOT EXISTS idx_circle_comment_post_id ON circle_comment(post_id);

-- ── 4. 创建 circle_like 表（圈子点赞表）──────────────
CREATE TABLE IF NOT EXISTS circle_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL COMMENT '帖子ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_user (post_id, user_id),
  FOREIGN KEY (post_id) REFERENCES circle_post(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='圈子点赞表';

-- ── 5. 创建 message 表（消息表）────────────────────
CREATE TABLE IF NOT EXISTS message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  type VARCHAR(50) NOT NULL COMMENT '类型: SYSTEM/TRANSACTION/REVIEW/INTERACTION',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  content TEXT COMMENT '内容',
  status VARCHAR(20) DEFAULT 'UNREAD' COMMENT '状态: UNREAD/READ',
  link VARCHAR(500) COMMENT '跳转链接',
  is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

CREATE INDEX IF NOT EXISTS idx_message_user_id ON message(user_id);
CREATE INDEX IF NOT EXISTS idx_message_status ON message(status);
CREATE INDEX IF NOT EXISTS idx_message_type ON message(type);
CREATE INDEX IF NOT EXISTS idx_message_create_time ON message(create_time);
