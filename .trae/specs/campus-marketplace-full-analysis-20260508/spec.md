# 校园二手交易平台 - 完整项目功能规格说明书

## Why

本Spec基于对仓库代码的全面深入分析（后端14个Controller、18个Service、20个Repository、16张数据表；前端338行路由配置、30+页面组件、完整组件库），建立一份与实际代码100%一致的功能规格说明，用于指导毕业设计论文撰写和后续开发。

## 项目概述

**系统名称**：校园二手交易平台（Campus Marketplace）  
**技术架构**：前后端分离 B/S 架构  
**前端技术栈**：Vue 3 (Composition API) + Vite + Pinia + Vue Router + Arco Design  
**后端技术栈**：Spring Boot 3 + Spring Security + JWT (JJWT) + Spring JDBC + HikariCP + MySQL 8.x  
**构建工具**：pnpm Monorepo (workspace: packages/common + packages/apps/campus-app) / Maven  
**AI能力**：OpenAI兼容API接入，知识库检索增强生成(RAG)

## 用户角色

| 角色 | 标识 | 说明 |
|------|------|------|
| 买家 | BUYER | 浏览商品、购买、收藏、评价 |
| 卖家 | SELLER | 发布商品、管理订单、查看统计 |
| 运营 | OPS | 审核内容、管理用户/订单、数据看板 |

## 数据库表结构（16张表）

### 用户与认证
- **user_account** — 普通用户表（id, username, password, nickname, roles, campus, phone, status）
- **ops_account** — 运营账号表（id, username, password, nickname, roles, role_level, status）

### 商品相关
- **item** — 商品表（id, title, price, description, seller_id, seller_name, review_status, reject_reason, image_urls, category, condition_level, campus）
- **item_stats** — 商品统计表（id, item_id, view_count, click_count, favorite_count, hot_score）

### 交易流程
- **cart** / **cart_item** — 购物车表（user_id, item_id, quantity, selected）
- **orders** — 订单表（order_no, item_id, buyer_id, seller_id, price, quantity, total_amount, status, receiver_*, express_*）
- **favorite** — 收藏表（user_id, item_id）
- **address** — 收货地址表（receiver_name, receiver_phone, province, city, district, detail_address, is_default）
- **review** — 评价表（order_id, item_id, buyer_id, seller_id, rating, content, images, reply, status）
- **review_log** — 审核日志表（item_id, operator_id, action, reason）

### 社交与通知
- **circle_post** — 圈子帖子表（user_id, title, content, images, tags, likes, comments, status, like_count, comment_count, view_count）
- **circle_comment** — 圈子评论表（post_id, user_id, user_name, content）
- **circle_like** — 圈子点赞表（post_id, user_id）
- **message** — 消息表（sender_id, sender_name, receiver_id, content, type[SYSTEM/TRANSACTION/REVIEW/INTERACTION], status[UNREAD/READ]）

### AI与文件
- **ai_session** — AI会话表（session_id, user_id, title）
- **ai_message** — AI消息表（session_id, role[user/assistant], content, references_json）
- **knowledge_chunk** — 知识库片段表（source_type, title, content）
- **uploaded_file** — 上传文件表（original_name, stored_name, file_path, file_size, content_type, url, base64_data, uploader_id）

---

## 已实现功能模块详细规格

### 模块1：用户认证（AuthController / AuthService / JwtUtil）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| POST | /api/auth/register | 用户注册（用户名+密码+昵称+角色） | 公开 |
| POST | /api/auth/login | 用户登录（返回JWT令牌） | 公开 |
| GET | /api/auth/me | 获取当前登录用户信息 | 已认证 |
| PUT | /api/auth/profile | 更新个人资料（昵称/邮箱/简介/校区/手机） | 已认证 |
| PUT | /api/auth/password | 修改密码（旧密码验证） | 已认证 |
| POST | /api/auth/login | 运营端登录 | 公开 |

**核心实现：**
- BCrypt密码加密存储
- JWT令牌生成与验证（JwtUtil）
- JwtAuthenticationFilter拦截器自动解析Authorization头
- 前端路由守卫基于meta.roles做角色校验（hasAnyRole函数）
- Pinia auth store管理token和用户状态

**前端页面：**
- LoginView.vue — 登录页（含注册入口）
- Register.vue — 注册页
- Profile.vue — 个人资料编辑

---

### 模块2：商品管理（ItemController / ItemService / ItemRepository）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | /api/items | 商品列表（分页+搜索+分类筛选+排序） | 公开 |
| GET | /api/items/{id} | 商品详情（含浏览量记录） | 公开 |
| POST | /api/items | 发布商品 | SELLER |
| PUT | /api/items/{id} | 编辑商品信息 | SELLER(本人) |
| DELETE | /api/items/{id} | 下架/删除商品 | SELLER(本人) |
| GET | /api/items/my | 我的商品列表 | SELLER |
| POST | /api/items/{id}/track-click | 记录点击量 | 公开 |
| POST | /api/items/{id}/track-favorite | 记录收藏操作 | BUYER |

**核心实现：**
- 发布时review_status默认PENDING，需运营审核通过(APPROVED)才展示
- 热度算法：hot_score = view_count×0.2 + click_count×0.3 + favorite_count×0.5
- 支持按hot_score降序排列获取热门商品
- 分类(category)：数码/书籍/服装/生活/运动等
- 成色(condition_level)：全新/几乎全新/轻微使用痕迹/明显使用痕迹

**前端页面：**
- portal/home/index.vue — 首页（热门推荐+分类导航）
- portal/buyer/Items.vue — 全部商品列表（搜索/筛选/排序/瀑布流）
- portal/ItemDetail.vue — 商品详情页
- portal/seller/PublishItem.vue — 发布商品（含图片上传）
- portal/seller/MyItems.vue — 我的商品管理

**关键组件：**
- ItemCard.vue — 商品卡片组件
- MasonryLayout.vue — 瀑布流布局
- FilterBar.vue — 筛选栏组件
- ImageUploader.vue — 图片上传组件

---

### 模块3：商品审核（OpsReviewController / ReviewService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | /api/ops/reviews | 待审核商品队列（分页+状态/关键词/分类筛选） | OPS |
| GET | /api/ops/reviews/stats | 各状态审核数量统计 | OPS |
| POST | /api/ops/reviews/{itemId}/approve | 审核通过 | OPS |
| POST | /api/ops/reviews/{itemId}/reject | 审核驳回（需填写原因） | OPS |

**核心实现：**
- 审核操作写入review_log日志表
- 状态流转：PENDING → APPROVED / REJECTED
- 驳回必须填写reject_reason

**前端页面：**
- ops/review/ApprovalWorkspace.vue — 审核工作台
- ops/reviews/index.vue — 审核列表
- ops/ReviewDetail.vue — 审核详情

---

### 模块4：购物车（CartController / CartService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | /api/cart | 我的购物车列表 | BUYER |
| POST | /api/cart | 添加商品到购物车 | BUYER |
| PUT | /api/cart/{id} | 修改数量 | BUYER |
| PUT | /api/cart/{id}/select | 选择/取消选择 | BUYER |
| PUT | /api/cart/select-all | 全选/取消全选 | BUYER |
| DELETE | /api/cart/{id} | 删除购物车项 | BUYER |
| DELETE | /api/cart | 清空购物车 | BUYER |

**前端页面：**
- portal/Cart.vue — 购物车页面（含MiniCart组件）

---

### 模块5：订单管理（OrderController / OrderService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| POST | /api/orders | 创建订单（生成唯一order_no） | BUYER |
| GET | /api/orders | 订单列表（分页，按角色过滤） | BUYER/SELLER |
| GET | /api/orders/{id} | 订单详情 | 本人 |
| POST | /api/orders/{id}/pay | 模拟支付 | BUYER(本人) |
| POST | /api/orders/{id}/ship | 卖家发货（填快递信息） | SELLER(本人) |
| POST | /api/orders/{id}/confirm | 确认收货 | BUYER(本人) |
| POST | /api/orders/{id}/cancel | 取消订单 | BUYER(本人) |
| POST | /api/orders/{id}/refund | 申请退款 | BUYER(本人) |
| POST | /api/orders/{id}/refund/handle | 同意/拒绝退款 | SELLER(本人) |

**订单状态机：**
```
PENDING(待支付) → PAID(已支付) → SHIPPED(已发货) → COMPLETED(已完成)
                                      ↘ REFUNDING(退款中) → REFUNDED(已退款)
                                                              或 → PAID(拒绝退款)
PENDING → CANCELLED(已取消)
```

**前端页面：**
- portal/orders/index.vue — 订单列表（买家/卖家共用）
- portal/orders/OrderConfirm.vue — 订单确认页
- ops/orders/index.vue — 运营订单管理

**关键组件：**
- OrderCard.vue — 订单卡片
- OrderActions.vue — 订单操作按钮组
- OrderGoods.vue — 订单商品信息

---

### 模块6：收藏（FavoriteController / FavoriteService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| POST | /api/favorites/{itemId} | 添加收藏 | BUYER |
| DELETE | /api/favorites/{itemId} | 取消收藏 | BUYER |
| GET | /api/favorites | 收藏列表（分页） | BUYER |
| GET | /api/favorites/check/{itemId} | 检查是否已收藏 | BUYER |

**前端页面：**
- portal/Favorites.vue — 收藏列表页

---

### 模块7：收货地址（AddressController / AddressService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | /api/addresses | 地址列表 | BUYER/SELLER |
| POST | /api/addresses | 新增地址 | BUYER/SELLER |
| PUT | /api/addresses/{id} | 编辑地址 | BUYER/SELLER |
| DELETE | /api/addresses/{id} | 删除地址 | BUYER/SELLER |
| PUT | /api/addresses/{id}/default | 设为默认地址 | BUYER/SELLER |
| GET | /api/addresses/default | 获取默认地址 | BUYER/SELLER |

**前端页面：**
- portal/AddressList.vue — 地址管理页

---

### 模块8：评价（ReviewController / ReviewService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| POST | /api/reviews | 提交评价（评分1-5星+文字+图片） | BUYER |
| GET | /api/reviews/item/{itemId} | 商品评价列表（分页） | 公开 |
| POST | /api/reviews/{id}/reply | 卖家回复评价 | SELLER(本人) |
| GET | /api/ops/reviews | 评价审核列表 | OPS |
| POST | /api/ops/reviews/{id}/approve | 评价审核通过 | OPS |
| POST | /api/ops/reviews/{id}/reject | 评价审核驳回 | OPS |

**前端页面：**
- portal/MyReviews.vue — 我的评价
- portal/ReviewSubmit.vue — 提交评价
- ops/reviews/index.vue — 评价审核

---

### 模块9：校园圈子（CircleController / CircleService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | /api/circle/posts | 帖子列表（分页+标签筛选） | 公开 |
| GET | /api/circle/posts/{id} | 帖子详情 | 公开 |
| POST | /api/circle/posts | 发布帖子（标题+内容+图片+标签） | 已认证 |
| PUT | /api/circle/posts/{id} | 编辑帖子 | 作者 |
| DELETE | /api/circle/posts/{id} | 删除帖子 | 作者/OPS |
| POST | /api/circle/posts/{id}/like | 点赞/取消点赞（切换） | 已认证 |
| GET | /api/circle/posts/{id}/comments | 评论列表 | 公开 |
| POST | /api/circle/posts/{id}/comments | 发表评论 | 已认证 |
| GET | /api/circle/posts/my | 我的帖子 | 已认证 |
| GET | /api/ops/circle/posts | 运营帖子审核列表 | OPS |
| POST | /api/ops/circle/posts/{id}/approve | 帖子审核通过 | OPS |
| POST | /api/ops/circle/posts/{id}/reject | 帖子审核驳回 | OPS |

**前端页面：**
- portal/circle/CircleHome.vue — 圈子首页
- portal/circle/CirclePublish.vue — 发布帖子
- portal/circle/CircleDetail.vue — 帖子详情

---

### 模块10：消息通知（MessageController / MessageService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | /api/messages | 消息列表（分页+类型筛选） | BUYER/SELLER |
| GET | /api/messages/unread-count | 未读消息数量 | BUYER/SELLER |
| POST | /api/messages/{id}/read | 标记单条已读 | BUYER/SELLER |
| POST | /api/messages/read-all | 一键全部标记已读 | BUYER/SELLER |
| DELETE | /api/messages/{id} | 删除消息 | BUYER/SELLER |

**消息类型（type字段）：**
- SYSTEM — 系统通知（平台公告、维护通知）
- TRANSACTION — 交易消息（发货/收货/退款等）
- REVIEW — 审核消息（商品/帖子/评价审核结果）
- INTERACTION — 互动消息（圈子点赞/评论）

**MessageService内部方法（供其他Service调用）：**
- sendMessage() — 统一发送入口
- sendSystemNotification() — 发送系统通知
- sendTransactionMessage() — 发送交易消息
- sendReviewResultMessage() — 发送审核结果消息
- sendInteractionMessage() — 发送互动消息

**前端组件：**
- MessageCenter.vue — 导航栏下拉面板（角标未读数+最近5条预览+快速跳转）
- portal/MessageList.vue — 消息中心完整页面（分类Tab+批量操作+分页）

---

### 模块11：AI智能助手（AIController / AIService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| POST | /api/ai/chat | AI对话（发送消息+获取回复） | 已认证 |
| GET | /api/ai/presets | 预设问题列表（按角色返回） | 已认证 |
| GET | /api/ai/sessions/recent | 最近会话列表 | 已认证 |
| GET | /api/ai/sessions/{sessionId}/messages | 会话消息记录 | 已认证 |

**核心实现：**
- 接入OpenAI兼容API（可配置base_url和api_key）
- RAG模式：从knowledge_chunk知识库检索相关片段作为上下文
- 根据用户角色(BUYER/SELLER)返回不同的预设问题列表
- 会话管理：自动创建session_id，记录对话历史

**前端组件：**
- AiAssistant.vue — AI助手悬浮按钮+对话框组件

---

### 模块12：卖家数据统计（SellerStatsController / SellerStatsService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | /api/seller/stats/overview | 销售概览（总额/订单数/商品数） | SELLER |
| GET | /api/seller/stats/trend | 销售趋势（近N天每日销售额） | SELLER |
| GET | /api/seller/stats/ranking | 商品销量排名 | SELLER |

**前端页面：**
- portal/SellerStats.vue — 卖家统计页

---

### 模块13：运营管理后台（OpsController / OpsService / StatsService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | /api/ops/dashboard | 运营仪表盘数据聚合 | OPS |
| GET | /api/ops/users | 用户列表（分页+角色/状态筛选） | OPS |
| PUT | /api/ops/users/{id}/status | 更新用户状态 | OPS |
| PUT | /api/ops/users/{id}/roles | 更新用户角色 | OPS |
| GET | /api/ops/orders | 全部订单列表 | OPS |

**仪表盘数据包含：**
- 总用户数、总商品数、总订单数、交易总额
- 待审核商品数、待审核评价数、待审核帖子数
- 今日新增用户/商品/订单数

**前端页面（Ops Layout深色侧边栏）：**
- ops/dashboard/EnhancedDashboard.vue — 数据仪表盘（ECharts图表）
- ops/buyer/index.vue — 买家管理
- ops/vendor/index.vue — 卖家管理
- ops/UserManage.vue — 用户管理（角色分配/状态管理）
- ops/VendorManage.vue — 卖家管理
- ops/BuyerManage.vue — 买家管理
- ops/RoleManage.vue — 角色管理

**ECharts图表组件：**
- OpsBarChart.vue — 柱状图
- OpsDonutChart.vue — 环形图
- OpsLineChart.vue — 折线图
- StatsCard.vue — 统计卡片

---

### 模块14：文件上传（UploadController / UploadService）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| POST | /api/upload/image | 图片上传（Base64存储） | 已认证 |
| GET | /api/upload/list | 我的上传列表 | 已认证 |

**实现方式：**
- Base64编码存储在uploaded_file表的base64_data字段
- 同时保存原始文件名、存储名、URL路径等信息
- 支持jpg/png/gif/webp等图片格式

---

### 模块15：字典服务（DictController）

**后端API端点：**
| 方法 | 路径 | 功能 | 权限 |
|------|------|------|------|
| GET | /api/dict/{dictType} | 获取字典数据 | 公开 |

用于提供商品分类、成色等级等下拉选项数据。

---

### 模块16：全局异常处理

**GlobalExceptionHandler.java** 统一处理：
- 参数校验异常 (@Validated)
- 业务异常（自定义BusinessException）
- 认证授权异常
- 其他未预期异常

统一返回标准错误格式。

---

## 前端架构总结

### 路由结构（router/index.js）

```
/ → 重定向到 /portal/home
/login → 登录页
/forbidden → 403禁止访问

/portal/* （PortalLayout 白色导航栏）
├── home → 首页
├── buyer/items → 全部商品（买家）
├── buyer/orders → 买家订单
├── buyer/home → 买家首页重定向
├── item/:id → 商品详情
├── orders/confirm/:id → 订单确认
├── seller/publish → 发布商品 [SELLER]
├── seller/items → 我的商品 [SELLER]
├── seller/orders → 卖家订单 [SELLER]
├── orders → 订单列表 [BUYER/SELLER]
├── addresses → 地址管理 [BUYER/SELLER]
├── profile → 个人中心
├── cart → 购物车 [BUYER]
├── favorites → 收藏 [BUYER]
├── my-reviews → 我的评价 [BUYER]
├── review/:orderId → 提交评价 [BUYER]
├── seller/stats → 卖家统计 [SELLER]
├── circle → 校园圈子首页
├── circle/publish → 发布动态
├── circle/:id → 帖子详情
├── messages → 消息中心 [BUYER/SELLER]
└── about → 关于我们

/ops/* （OpsLayout 深色侧边栏）[OPS]
├── dashboard → 仪表盘
├── reviews → 评价审核列表
├── reviews/:id → 评价审核详情
├── review → 审核工作台
├── orders → 订单管理
├── vendor → 卖家管理
├── buyer → 买家管理
├── user-manage → 用户管理
├── vendor-manage → 卖家管理
├── buyer-manage → 买家管理
└── role-manage → 角色管理
```

### 关键组件清单

**布局组件：** PortalLayout, OpsLayout, PageContainer, PageHeader, Header, Footer, Login  
**业务组件：** ItemCard, OrderCard, OrderActions, OrderGoods, MiniCart, Cart, Favorite, AddressCard, EditAddressModal, PriceTag, ConditionTag, ProductCard  
**通用组件：** DataTable, DetailDrawer, ConfirmDialog, LoginPromptModal, MessageCenter, SearchFilter, StatusTag, RoleList, ImageGallery, RichEditor, ImageUploader, AiAssistant, CircleStylePagination  
**图表组件：** OpsBarChart, OpsDonutChart, OpsLineChart, StatsCard  
**主题配置：** ArcoConfigProvider, ThemeConfig

### 服务层（services/）

- http.js / core/http.js — Axios HTTP客户端（JWT拦截器）
- api.js — 统一API接口定义
- auth/ — 认证相关API
- items/ — 商品相关API
- orders/ — 订单相关API
- ops/ — 运营端API
- users/ — 用户API
- ai.js — AI助手API

### 状态管理（stores/）

- auth.js — 认证状态（token, user, roles, isLoggedIn, hydrate方法持久化恢复）

---

## Impact

- 本Spec完全基于代码实际实现，覆盖16个功能模块、60+个API端点、30+个前端页面
- 用于毕业论文第3章需求分析、第4-5章设计与实现的写作依据
- 论文中的功能描述、数据库设计、模块划分均应与此Spec保持一致
