# 企业级项目管理系统 - 技术实现文档

## 文档信息
| 字段 | 内容 |
|---|---|
| 文档名称 | 企业级项目管理系统 - 技术实现文档 |
| 作者 | Tech Team |
| 创建时间 | 2026年5月 |
| 当前版本 | v1.0 |
| 适用范围 | 开发、测试团队 |

---

## 一、需求理解与分析

### 1.1 需求背景与目标
根据PRD文档，本系统需实现以下核心功能：
- 商品管理（发布、审核、展示）
- 评价功能（发布、审批、展示）
- 收藏功能
- 统一审批流管理
- 消息中心
- 校园圈子

### 1.2 核心业务流程

**商品审核流程：**
```
用户发布商品 → 系统保存草稿 → 提交审核 → 创建审批记录 → 运营审批 → 更新商品状态
```

**评价审核流程：**
```
用户提交评价 → 创建评价记录 → 创建审批记录 → 运营审批 → 更新评价状态 → 展示评价
```

**圈子内容流程：**
```
用户发布内容 → 创建帖子记录 → 创建审批记录 → 运营审批 → 更新状态 → 内容展示
```

### 1.3 需求约束与非功能要求
- **性能**：接口响应时间 ≤ 500ms，支持1000+并发用户
- **安全性**：数据加密传输，Token鉴权，防SQL注入和XSS攻击
- **数据量级**：预计日均新增商品1000+，评价5000+，消息100+

---

## 二、技术架构设计

### 2.1 技术栈选型

| 技术领域 | 技术选型 | 版本 | 说明 |
|---|---|---|---|
| 前端框架 | Vue.js | 3.4+ | 渐进式JavaScript框架 |
| UI组件库 | Element Plus | 2.6+ | 企业级UI组件库 |
| 富文本编辑器 | Quill | 2.0+ | 轻量级富文本编辑 |
| 后端框架 | Spring Boot | 3.2+ | Java后端框架 |
| 数据库 | MySQL | 8.0+ | 关系型数据库 |
| 缓存 | Redis | 7.0+ | 分布式缓存 |
| 消息队列 | RabbitMQ | 3.12+ | 异步消息处理 |
| 认证授权 | JWT | - | Token认证 |
| 文件存储 | MinIO | 2024+ | 对象存储 |

### 2.2 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端层 (Vue.js)                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │  用户端   │ │ 运营端   │ │ 管理端   │ │ 审批端   │          │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘          │
└───────┼────────────┼────────────┼────────────┼──────────────────┘
        │            │            │            │
        ▼            ▼            ▼            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway (Spring Cloud Gateway)           │
│                      [认证、限流、路由]                          │
└───────────────────────────┬─────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│   Product     │   │   Approval    │   │   Message     │
│    Service    │   │    Service    │   │    Service    │
└───────┬───────┘   └───────┬───────┘   └───────┬───────┘
        │                   │                   │
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│   Review      │   │   Favorite    │   │   Circle      │
│    Service    │   │    Service    │   │    Service    │
└───────┬───────┘   └───────┬───────┘   └───────┬───────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
                            ▼
                 ┌───────────────────┐
                 │     MySQL         │
                 │  (主从复制)        │
                 └───────────────────┘
                 ┌───────────────────┐
                 │     Redis         │
                 │  (缓存、会话)      │
                 └───────────────────┘
                 ┌───────────────────┐
                 │   RabbitMQ        │
                 │  (异步消息)        │
                 └───────────────────┘
                 ┌───────────────────┐
                 │    MinIO          │
                 │  (文件存储)        │
                 └───────────────────┘
```

### 2.3 分层架构设计

| 层级 | 职责 | 技术实现 |
|---|---|---|
| Controller层 | REST API入口，参数校验 | Spring MVC |
| Service层 | 业务逻辑处理 | Spring Service |
| Repository层 | 数据访问 | Spring Data JPA |
| Entity层 | 数据库实体映射 | JPA Entities |
| DTO层 | 数据传输对象 | POJO |

---

## 三、核心模块设计

### 3.1 商品管理模块

#### 3.1.1 核心功能
- 商品CRUD操作
- 商品状态流转
- 富文本内容存储

#### 3.1.2 数据模型

```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(columnDefinition = "JSON")
    private String images;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(nullable = false)
    private Integer status; // 0草稿 1待审核 2已发布 3已下架 4已驳回
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

#### 3.1.3 API接口设计

| API路径 | HTTP方法 | Controller文件 | 功能描述 |
|---|---|---|---|
| /api/v1/products | POST | ProductController.java | 创建商品 |
| /api/v1/products | GET | ProductController.java | 查询商品列表 |
| /api/v1/products/{id} | GET | ProductController.java | 查询商品详情 |
| /api/v1/products/{id} | PUT | ProductController.java | 更新商品 |
| /api/v1/products/{id} | DELETE | ProductController.java | 删除商品 |
| /api/v1/products/{id}/submit | POST | ProductController.java | 提交审核 |

### 3.2 评价管理模块

#### 3.2.1 核心功能
- 评价发布与展示
- 评价审批流程
- 评分统计

#### 3.2.2 API接口设计

| API路径 | HTTP方法 | Controller文件 | 功能描述 |
|---|---|---|---|
| /api/v1/reviews | POST | ReviewController.java | 创建评价 |
| /api/v1/reviews | GET | ReviewController.java | 查询评价列表 |
| /api/v1/reviews/{id} | GET | ReviewController.java | 查询评价详情 |
| /api/v1/products/{productId}/reviews | GET | ReviewController.java | 查询商品评价 |

### 3.3 收藏管理模块

#### 3.3.1 核心功能
- 添加/取消收藏
- 收藏列表查询
- 收藏统计

#### 3.3.2 API接口设计

| API路径 | HTTP方法 | Controller文件 | 功能描述 |
|---|---|---|---|
| /api/v1/favorites | POST | FavoriteController.java | 添加收藏 |
| /api/v1/favorites | GET | FavoriteController.java | 查询收藏列表 |
| /api/v1/favorites/{productId} | DELETE | FavoriteController.java | 取消收藏 |
| /api/v1/favorites/{productId}/exists | GET | FavoriteController.java | 检查是否已收藏 |

### 3.4 审批流模块

#### 3.4.1 核心功能
- 统一审批流程引擎
- 审批状态管理
- 审批记录追踪

#### 3.4.2 审批状态流转

```
待审核(PENDING) → 通过(APPROVED) → 已发布
待审核(PENDING) → 驳回(REJECTED) → 可重新提交
```

#### 3.4.3 API接口设计

| API路径 | HTTP方法 | Controller文件 | 功能描述 |
|---|---|---|---|
| /api/v1/approvals | GET | ApprovalController.java | 查询审批列表 |
| /api/v1/approvals/{id} | GET | ApprovalController.java | 查询审批详情 |
| /api/v1/approvals/{id}/approve | POST | ApprovalController.java | 审批通过 |
| /api/v1/approvals/{id}/reject | POST | ApprovalController.java | 审批驳回 |

### 3.5 消息中心模块

#### 3.5.1 核心功能
- 消息发布与审批
- 消息推送
- 消息阅读状态管理

#### 3.5.2 API接口设计

| API路径 | HTTP方法 | Controller文件 | 功能描述 |
|---|---|---|---|
| /api/v1/messages | POST | MessageController.java | 创建消息 |
| /api/v1/messages | GET | MessageController.java | 查询消息列表 |
| /api/v1/messages/{id} | GET | MessageController.java | 查询消息详情 |
| /api/v1/messages/{id}/read | POST | MessageController.java | 标记已读 |

### 3.6 校园圈子模块

#### 3.6.1 核心功能
- 帖子发布与审批
- 点赞/评论互动
- 话题标签管理

#### 3.6.2 API接口设计

| API路径 | HTTP方法 | Controller文件 | 功能描述 |
|---|---|---|---|
| /api/v1/circle/posts | POST | CircleController.java | 发布帖子 |
| /api/v1/circle/posts | GET | CircleController.java | 查询帖子列表 |
| /api/v1/circle/posts/{id} | GET | CircleController.java | 查询帖子详情 |
| /api/v1/circle/posts/{id}/like | POST | CircleController.java | 点赞 |
| /api/v1/circle/posts/{id}/comments | POST | CircleController.java | 评论 |

---

## 四、数据库设计

### 4.1 数据库表结构

#### 4.1.1 用户表（users）
| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | 用户ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password | VARCHAR(255) | NOT NULL | 加密密码 |
| email | VARCHAR(100) | UNIQUE | 邮箱 |
| phone | VARCHAR(20) | UNIQUE | 手机号 |
| role | INT | NOT NULL | 角色：0普通用户 1运营 2管理员 |
| status | INT | NOT NULL | 状态：0禁用 1启用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 4.1.2 商品分类表（categories）
| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | 分类ID |
| name | VARCHAR(50) | NOT NULL | 分类名称 |
| parent_id | BIGINT | | 父分类ID |
| sort_order | INT | DEFAULT 0 | 排序 |
| status | INT | NOT NULL | 状态：0禁用 1启用 |

#### 4.1.3 评论表（comments）
| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PRIMARY KEY | 评论ID |
| post_id | BIGINT | NOT NULL | 帖子ID |
| user_id | BIGINT | NOT NULL | 用户ID |
| content | TEXT | NOT NULL | 评论内容 |
| status | INT | DEFAULT 0 | 状态：0待审核 1已通过 2已驳回 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 4.2 索引设计

| 表名 | 索引字段 | 索引类型 | 说明 |
|---|---|---|---|
| products | status, created_at | 复合索引 | 优化审核列表查询 |
| reviews | product_id, status | 复合索引 | 优化商品评价查询 |
| approvals | target_type, status | 复合索引 | 优化审批列表查询 |
| favorites | user_id, product_id | 复合唯一索引 | 防止重复收藏 |
| circle_posts | status, created_at | 复合索引 | 优化圈子内容查询 |

---

## 五、API接口详细设计

### 5.1 接口规范

- **版本管理**：URL包含版本号 `/api/v1/xxx`
- **请求格式**：JSON
- **响应格式**：JSON
- **编码格式**：UTF-8
- **跨域支持**：CORS配置

### 5.2 错误码定义

| 错误码 | 含义 | 适用场景 |
|---|---|---|
| 400 | 请求参数错误 | 参数校验失败 |
| 401 | 未授权 | Token无效或过期 |
| 403 | 无权限 | 用户无操作权限 |
| 404 | 资源不存在 | 目标资源未找到 |
| 500 | 服务器错误 | 系统内部错误 |

### 5.3 响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": {},
    "timestamp": 1715088000000
}
```

---

## 六、安全设计

### 6.1 认证授权
- JWT Token认证
- RBAC角色权限控制
- 接口权限校验

### 6.2 数据安全
- 密码SHA-256加密存储
- 敏感数据传输采用HTTPS
- SQL注入防护（JPA预编译）
- XSS攻击防护（富文本过滤）

### 6.3 防刷机制
- 接口限流（每分钟最多100次请求）
- 验证码校验（敏感操作）

---

## 七、性能优化

### 7.1 缓存策略
- Redis缓存热门商品数据
- 缓存评价统计数据
- 缓存审批待办数量

### 7.2 数据库优化
- 读写分离
- 分库分表（未来扩展）
- 索引优化

### 7.3 异步处理
- 消息队列处理审核通知
- 异步发送消息推送

---

## 八、部署与集成

### 8.1 部署架构
- Docker容器化部署
- Kubernetes编排
- Nginx负载均衡

### 8.2 环境配置

| 环境 | 数据库 | 缓存 | 消息队列 |
|---|---|---|---|
| 开发 | MySQL | Redis | RabbitMQ |
| 测试 | MySQL | Redis | RabbitMQ |
| 生产 | MySQL集群 | Redis集群 | RabbitMQ集群 |

---

## 九、代码结构

```
backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/app/
│       │       ├── controller/     # REST API控制层
│       │       │   ├── ProductController.java
│       │       │   ├── ReviewController.java
│       │       │   ├── FavoriteController.java
│       │       │   ├── ApprovalController.java
│       │       │   ├── MessageController.java
│       │       │   └── CircleController.java
│       │       ├── service/        # 业务逻辑层
│       │       │   ├── ProductService.java
│       │       │   ├── ReviewService.java
│       │       │   ├── FavoriteService.java
│       │       │   ├── ApprovalService.java
│       │       │   ├── MessageService.java
│       │       │   └── CircleService.java
│       │       ├── repository/     # 数据访问层
│       │       │   ├── ProductRepository.java
│       │       │   ├── ReviewRepository.java
│       │       │   ├── FavoriteRepository.java
│       │       │   ├── ApprovalRepository.java
│       │       │   ├── MessageRepository.java
│       │       │   └── CircleRepository.java
│       │       ├── entity/         # 数据库实体
│       │       │   ├── Product.java
│       │       │   ├── Review.java
│       │       │   ├── Favorite.java
│       │       │   ├── Approval.java
│       │       │   ├── Message.java
│       │       │   └── CirclePost.java
│       │       ├── dto/            # 数据传输对象
│       │       │   ├── request/
│       │       │   └── response/
│       │       ├── config/         # 配置类
│       │       │   ├── SecurityConfig.java
│       │       │   ├── RedisConfig.java
│       │       │   └── RabbitMQConfig.java
│       │       ├── security/       # 安全相关
│       │       │   ├── JwtTokenProvider.java
│       │       │   └── CustomUserDetailsService.java
│       │       └── Application.java
│       └── resources/
│           ├── application.yml     # 应用配置
│           └── schema.sql          # 数据库初始化

```

---

## 十、版本历史

| 版本 | 日期 | 变更内容 | 作者 |
|---|---|---|---|
| v1.0 | 2026-05-07 | 初始版本 | Tech Team |