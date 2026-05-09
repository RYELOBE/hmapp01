-- 消息通知表
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    title VARCHAR(255) NOT NULL COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    type VARCHAR(50) NOT NULL COMMENT '消息类型：SYSTEM-系统通知, REVIEW-审核通知, ORDER-订单通知, USER-用户通知, BUSINESS-业务通知',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 插入示例数据
INSERT INTO notification (title, content, type, receiver_id, sender_id, sender_name, priority) VALUES
('新的商品审核待处理', '您有3个商品待审核，请及时处理', 'REVIEW', NULL, 1, '系统', 'HIGH'),
('用户投诉需要处理', '用户投诉商品质量问题，需要您处理', 'USER', NULL, 1, '系统', 'MEDIUM'),
('系统维护通知', '系统将于今晚22:00进行维护，预计持续2小时', 'SYSTEM', NULL, 1, '系统', 'MEDIUM'),
('订单退款申请', '订单#12345申请退款，请审核', 'ORDER', NULL, 1, '系统', 'HIGH'),
('评价审核提醒', '有新的评价待审核', 'REVIEW', NULL, 1, '系统', 'MEDIUM');
