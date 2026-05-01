# 校园二手交易平台 产品规格说明书

## 一、项目概述

### 1.1 项目背景与目标

校园二手交易平台是一个面向高校师生的在线二手物品交易系统，旨在为校园用户提供便捷、安全的二手物品发布、浏览、购买服务。平台采用微前端架构，支持用户端门户和运营管理后台的独立部署与配置。

### 1.2 核心价值

- **便捷性**：用户无需登录即可浏览商品，降低使用门槛
- **安全性**：商品需经运营审核后上架，保障交易安全
- **灵活性**：支持买家/卖家双角色，用户可根据需求自由切换
- **可配置性**：运营后台可动态配置前端页面路由和功能模块

## 二、系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端访问层                          │
├──────────────────┬──────────────────┬───────────────────────┤
│   门户应用(Portal) │   运营后台(OPS)   │   壳工程(Shell)        │
│   端口:5174       │   端口:5175       │   端口:5173           │
│   mf-shared      │   mf-shared      │   mf-shared          │
└────────┬─────────┴────────┬─────────┴─────────┬─────────────┘
         │                  │                   │
         └──────────────────┴───────────────────┘
                            │
                    ┌───────┴───────┐
                    │  微前端容器     │
                    │  (Qiankun)    │
                    └───────┬───────┘
                            │
┌───────────────────────────┴─────────────────────────────────┐
│                        后端服务层                            │
├─────────────────────────────────────────────────────────────┤
│  Spring Boot 后端 (端口:8080)                                │
│  ├── 用户认证模块 (SaToken)                                   │
│  ├── 商品管理模块                                             │
│  ├── 订单管理模块                                             │
│  ├── AI问答模块                                               │
│  ├── 运营管理模块                                             │
│  └── 门户配置模块                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 技术栈

| 层级 | 技术选型 | 说明 |
|------|---------|------|
| 前端框架 | Vue 3 + Vite | 组合式API |
| UI组件库 | Element Plus / shadcn-vue | 企业级组件 |
| 状态管理 | Pinia | 全局状态 |
| 微前端 | Qiankun | 子应用隔离 |
| 后端框架 | Spring Boot 3 | Java 17 |
| 安全框架 | SaToken | 认证授权 |
| 数据库 | MySQL 8 | 关系型存储 |
| AI服务 | 第三方AI API | 智能问答 |

### 2.3 端口分配

| 应用 | 端口 | 访问地址 | 说明 |
|------|------|----------|------|
| Shell (壳工程) | 5173 | http://localhost:5173 | 微前端主容器 |
| Portal (门户) | 5174 | /portal/* | 用户端应用 |
| OPS (运营后台) | 5175 | /ops/* | 管理员应用 |
| Backend (后端) | 8080 | http://localhost:8080 | API服务 |
| mf-shared (共享模块) | - | 共享依赖 | 公共组件/SDK |

## 三、业务角色

### 3.1 角色定义

| 角色代码 | 角色名称 | 权限范围 | 登录入口 |
|----------|----------|----------|----------|
| BUYER | 买家 | 浏览商品、购买、下单、查看订单 | 用户登录 |
| SELLER | 卖家 | 发布商品、管理商品、查看订单 | 用户登录 |
| OPS_ADMIN | 运营管理员 | 审核商品、配置页面、管理用户 | 运营登录 |
| OPS_SUPER | 超级管理员 | 所有运营权限、角色管理 | 运营登录 |

### 3.2 角色权限矩阵

| 功能 | 买家 | 卖家 | 运营管理员 | 超级管理员 |
|------|------|------|------------|------------|
| 浏览门户 | ✓ | ✓ | ✓ | ✓ |
| 浏览商品详情 | ✓ | ✓ | ✓ | ✓ |
| 发布商品 | ✗ | ✓ | ✓ | ✓ |
| 编辑商品 | ✗ | 仅自己 | ✓ | ✓ |
| 删除商品 | ✗ | 仅自己 | ✓ | ✓ |
| 审核商品 | ✗ | ✗ | ✓ | ✓ |
| 创建订单 | ✓ | ✗ | ✗ | ✗ |
| 查看我的订单 | ✓ | ✓ | ✓ | ✓ |
| AI问答 | ✓ | ✓ | ✓ | ✓ |
| 页面配置 | ✗ | ✗ | ✓ | ✓ |
| 用户管理 | ✗ | ✗ | ✓ | ✓ |
| 角色管理 | ✗ | ✗ | ✗ | ✓ |

## 四、用户端功能模块

### 4.1 门户模块 (Portal)

#### 4.1.1 首页

- 商品推荐展示（最新、热门）
- 分类导航
- 搜索功能
- 公告轮播

#### 4.1.2 商品列表页

- 分类筛选
- 价格区间筛选
- 排序方式（最新、价格、销量）
- 分页展示
- 快速收藏

#### 4.1.3 商品详情页

- 商品图片展示
- 商品信息（标题、描述、价格、成色）
- 卖家信息
- AI问答入口
- 立即购买/加入购物车

#### 4.1.4 登录/注册

- 用户名密码登录
- 注册时选择初始角色（买家/卖家）
- 注册后可申请开通其他角色

### 4.2 买家中心 (Buyer Center)

#### 4.2.1 我的订单

| 订单状态 | 说明 |
|----------|------|
| PENDING_PAYMENT | 待付款 |
| PAID | 已付款 |
| SHIPPED | 已发货 |
| COMPLETED | 已完成 |
| CANCELLED | 已取消 |
| REFUNDING | 退款中 |
| REFUNDED | 已退款 |

- 订单列表展示
- 订单详情查看
- 取消订单
- 确认收货
- 申请退款

#### 4.2.2 我的收藏

- 收藏商品列表
- 取消收藏
- 一键购买

### 4.3 卖家中心 (Seller Center)

#### 4.3.1 商品管理

| 商品状态 | 说明 |
|----------|------|
| DRAFT | 草稿 |
| PENDING_REVIEW | 待审核 |
| APPROVED | 审核通过 |
| REJECTED | 审核拒绝 |
| OFFLINE | 已下架 |

- 发布商品
- 编辑商品
- 删除商品
- 上架/下架商品
- 查看审核结果

#### 4.3.2 订单管理

- 待发货订单
- 已发货订单
- 已完成订单
- 订单发货操作

### 4.4 AI问答模块

- 智能客服入口
- 商品相关问题咨询
- 交易流程咨询
- 常见问题解答

## 五、运营后台功能模块

### 5.1 运营登录

- 独立的运营登录页面
- 运营账号与用户账号隔离
- 运营账号由超级管理员创建

### 5.2 运营工作台

- 数据概览（用户数、商品数、订单数）
- 待审核商品列表
- 最新订单动态
- 系统公告

### 5.3 商品审核

- 待审核商品列表
- 审核详情查看
- 通过/拒绝操作
- 拒绝理由填写

### 5.4 页面配置

#### 5.4.1 路由配置

```javascript
// 可配置的路由示例
{
  "route": "/portal/home",
  "name": "首页",
  "component": "HomeView",
  "visible": true,
  "order": 1
}
```

#### 5.4.2 功能模块配置

| 模块标识 | 模块名称 | 可配置项 |
|----------|----------|----------|
| banner | 轮播图 | 图片、链接、显示顺序 |
| category | 分类导航 | 分类列表、图标 |
| hot_items | 热门商品 | 显示数量、排序规则 |
| recommend | 推荐商品 | 显示数量、刷新频率 |

### 5.5 用户管理

- 用户列表
- 用户详情查看
- 角色分配
- 禁用/启用用户

### 5.6 角色权限管理

- 角色列表
- 创建角色
- 编辑角色权限
- 删除角色

## 六、数据模型

### 6.1 核心实体

#### 用户 (user)

```javascript
{
  id: Long,
  username: String,
  password: String,
  nickname: String,
  phone: String,
  email: String,
  avatar: String,
  status: Enum(ACTIVE, DISABLED),
  createTime: DateTime,
  updateTime: DateTime
}
```

#### 用户角色 (user_role)

```javascript
{
  id: Long,
  userId: Long,
  roleCode: String,
  createTime: DateTime
}
```

#### 商品 (item)

```javascript
{
  id: Long,
  title: String,
  description: String,
  price: Decimal,
  originalPrice: Decimal,
  category: String,
  condition: Enum(NEW, LIKE_NEW, GOOD, FAIR),
  images: String[], // JSON数组
  sellerId: Long,
  status: Enum(DRAFT, PENDING_REVIEW, APPROVED, REJECTED, OFFLINE),
  reviewComment: String,
  viewCount: Integer,
  favoriteCount: Integer,
  createTime: DateTime,
  updateTime: DateTime
}
```

#### 订单 (order)

```javascript
{
  id: Long,
  orderNo: String,
  itemId: Long,
  itemTitle: String,
  itemImage: String,
  price: Decimal,
  quantity: Integer,
  totalAmount: Decimal,
  buyerId: Long,
  sellerId: Long,
  status: Enum(PENDING_PAYMENT, PAID, SHIPPED, COMPLETED, CANCELLED, REFUNDING, REFUNDED),
  receiverName: String,
  receiverPhone: String,
  receiverAddress: String,
  expressNo: String,
  createTime: DateTime,
  updateTime: DateTime
}
```

#### 收藏 (favorite)

```javascript
{
  id: Long,
  userId: Long,
  itemId: Long,
  createTime: DateTime
}
```

#### 门户配置 (portal_config)

```javascript
{
  id: Long,
  configKey: String,
  configValue: JSON,
  description: String,
  createTime: DateTime,
  updateTime: DateTime
}
```

#### AI会话 (ai_session)

```javascript
{
  id: Long,
  userId: Long,
  sessionNo: String,
  createTime: DateTime
}
```

#### AI消息 (ai_message)

```javascript
{
  id: Long,
  sessionId: Long,
  role: Enum(USER, ASSISTANT),
  content: String,
  createTime: DateTime
}
```

## 七、API接口设计

### 7.1 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 用户登录 |
| /api/auth/register | POST | 用户注册 |
| /api/auth/logout | POST | 登出 |
| /api/auth/current | GET | 获取当前用户 |
| /api/auth/ops/login | POST | 运营登录 |
| /api/auth/ops/logout | POST | 运营登出 |

### 7.2 商品接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/items | GET | 商品列表 | 公开 |
| /api/items/{id} | GET | 商品详情 | 公开 |
| /api/items | POST | 发布商品 | SELLER |
| /api/items/{id} | PUT | 编辑商品 | SELLER |
| /api/items/{id} | DELETE | 删除商品 | SELLER |
| /api/items/my | GET | 我的商品 | SELLER |

### 7.3 订单接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/orders | GET | 订单列表 | 登录 |
| /api/orders/{id} | GET | 订单详情 | 登录 |
| /api/orders | POST | 创建订单 | BUYER |
| /api/orders/{id}/cancel | POST | 取消订单 | BUYER |
| /api/orders/{id}/ship | POST | 订单发货 | SELLER |
| /api/orders/{id}/confirm | POST | 确认收货 | BUYER |
| /api/orders/{id}/refund | POST | 申请退款 | BUYER |

### 7.4 运营接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/ops/reviews | GET | 待审核列表 | OPS_ADMIN |
| /api/ops/reviews/{id} | GET | 审核详情 | OPS_ADMIN |
| /api/ops/reviews/{id}/approve | POST | 审核通过 | OPS_ADMIN |
| /api/ops/reviews/{id}/reject | POST | 审核拒绝 | OPS_ADMIN |
| /api/ops/configs | GET | 获取配置 | OPS_ADMIN |
| /api/ops/configs | PUT | 更新配置 | OPS_ADMIN |
| /api/ops/users | GET | 用户列表 | OPS_ADMIN |
| /api/ops/users/{id}/roles | PUT | 分配角色 | OPS_ADMIN |
| /api/ops/roles | GET | 角色列表 | OPS_SUPER |
| /api/ops/roles | POST | 创建角色 | OPS_SUPER |
| /api/ops/roles/{id} | PUT | 更新角色 | OPS_SUPER |
| /api/ops/roles/{id} | DELETE | 删除角色 | OPS_SUPER |

### 7.5 AI接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/ai/sessions | GET | 会话列表 | 登录 |
| /api/ai/sessions | POST | 创建会话 | 登录 |
| /api/ai/messages | GET | 消息列表 | 登录 |
| /api/ai/chat | POST | 发送消息 | 登录 |

## 八、业务流程

### 8.1 用户注册流程

```
用户注册 → 选择角色(买家/卖家) → 填写信息 → 注册成功 → 开通角色权限
                ↓
         可同时选择多个角色
```

### 8.2 商品发布流程

```
卖家发布商品 → 保存草稿 → 提交审核 → 运营审核 → 通过(上架) / 拒绝(修改)
                                   ↓
                            审核通过后商品可见于门户
```

### 8.3 商品购买流程

```
买家浏览商品 → 加入购物车/立即购买 → 确认订单 → 提交订单 → 卖家发货 → 买家确认收货 → 订单完成
                                                    ↓
                                           可申请退款
```

### 8.4 页面配置流程

```
运营登录 → 页面配置 → 添加/修改路由 → 保存配置 → 刷新页面 → 新配置生效
```

## 九、安全设计

### 9.1 认证机制

- 使用 SaToken 进行身份认证
- Token 有效期：24小时
- 支持 Token 刷新
- 运营账号与用户账号隔离

### 9.2 权限控制

- 基于角色的访问控制 (RBAC)
- 接口级别权限校验
- 前端路由权限拦截
- 数据级别权限隔离

### 9.3 数据安全

- 密码加密存储
- 敏感信息脱敏
- SQL注入防护
- XSS攻击防护

## 十、非功能性需求

### 10.1 性能需求

- 页面加载时间 < 3秒
- 接口响应时间 < 500ms
- 支持100并发用户

### 10.2 可用性需求

- 系统可用性 > 99.9%
- 支持7x24小时运行
- 故障恢复时间 < 30分钟

### 10.3 可维护性需求

- 代码覆盖率 > 70%
- 完整的日志记录
- 统一的错误处理

## 十一、项目结构

```
campus-marketplace/
├── backend/                    # 后端服务
│   └── src/main/java/com/campus/marketplace/
│       ├── config/            # 配置类
│       ├── controller/        # 控制器
│       ├── service/          # 业务服务
│       ├── repository/       # 数据访问
│       └── model/            # 数据模型
├── packages/
│   ├── apps/
│   │   ├── shell/           # 壳工程（微前端容器）
│   │   ├── portal/          # 用户端门户
│   │   └── ops/             # 运营后台
│   └── common/              # 公共模块
│       └── src/
│           ├── constants.js # 常量定义
│           ├── roles.js     # 角色定义
│           ├── theme.js     # 主题配置
│           └── utils.js     # 工具函数
└── packages/apps/mf-shared/ # 共享组件库
    └── src/
        ├── components/      # Vue组件
        └── sdk/             # SDK封装
```

## 十二、部署方案

### 12.1 开发环境

```bash
# 启动后端
cd backend
./mvnw spring-boot:run

# 启动前端（并行）
pnpm run dev
```

### 12.2 生产环境

- 后端：Docker容器部署
- 前端：Nginx静态部署
- 数据库：云数据库服务

## 十三、版本规划

| 版本 | 功能 | 优先级 |
|------|------|--------|
| v1.0 | 基础功能（用户、登录、商品、订单） | P0 |
| v1.1 | 运营审核、页面配置 | P0 |
| v1.2 | AI问答功能 | P1 |
| v1.3 | 高级搜索、推荐系统 | P2 |
| v1.4 | 消息通知系统 | P2 |

## 十四、风险与挑战

### 14.1 技术风险

- 微前端架构复杂度高
- 多子应用状态共享困难
- AI服务响应延迟

### 14.2 业务风险

- 用户信任度建立需要时间
- 交易安全需要持续关注
- 审核效率影响用户体验

### 14.3 缓解措施

- 加强代码审查
- 完善的日志和监控
- 用户教育和使用指引
- 快速响应用户反馈
