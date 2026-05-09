# 校园二手交易平台系统模块设计与实现文档生成 - 规格

## Why
需要基于项目实际代码（非产品文档），生成面向毕设论文的系统基础模块设计与实现文档和系统核心模块设计与实现文档，用于修改为校园二手平台毕设论文。

## What Changes
- 生成系统基础模块设计与实现文档，覆盖认证授权、文件上传、数据字典、通知消息等基础支撑模块
- 生成系统核心模块设计与实现文档，覆盖商品管理、订单交易、购物车、收藏、评价、校园圈子、AI助手等核心业务模块
- 文档内容基于代码实际实现，包含业务逻辑分析、产品逻辑分析、技术实现分析、UI规范分析

## Impact
- Affected specs: 全部功能模块
- Affected code: 前后端全部代码

---

## ADDED Requirements

### Requirement: 系统基础模块设计与实现文档
系统 SHALL 生成一份完整的系统基础模块设计与实现文档，包含以下模块的详细分析：

#### 模块1：认证授权模块
- **业务逻辑**：用户注册/登录/登出、运营账号独立认证、JWT令牌生成与验证、角色权限控制（BUYER/SELLER/OPS三种角色）
- **产品逻辑**：普通用户与运营人员分离登录入口、角色决定可见功能和页面、JWT无状态认证保证前后端分离架构下的安全性
- **技术实现**：
  - 后端：Spring Security + JWT（JJWT 0.12.6）+ BCrypt密码加密 + JwtAuthenticationFilter过滤器 + SecurityConfig安全配置
  - 前端：Pinia状态管理（auth store）+ JWT工具类（解析/验证/过期检测）+ 路由守卫（角色权限校验）+ Axios请求拦截器（Token注入）
  - 数据库：user_account表、ops_account表（双表隔离）
- **API端点**：
  - POST /api/auth/login - 用户登录
  - POST /api/auth/register - 用户注册
  - POST /api/auth/logout - 用户登出
  - GET /api/auth/me - 获取当前用户信息
  - PUT /api/auth/profile - 更新用户资料
  - PUT /api/auth/avatar - 更新用户头像
  - PUT /api/auth/password - 修改密码
  - POST /api/auth/ops/login - 运营后台登录

#### 模块2：文件上传模块
- **业务逻辑**：图片上传、Base64存储、文件类型校验、文件大小限制
- **产品逻辑**：支持商品图片上传、富文本编辑器图片插入、运营后台图片管理
- **技术实现**：
  - 后端：UploadController + UploadService + UploadRepository，图片以Base64格式存储在uploaded_file表中
  - 前端：ImageUploader组件 + RichEditor集成图片上传
- **API端点**：
  - POST /api/upload - 文件上传
  - GET /api/upload/{id} - 获取上传文件信息

#### 模块3：数据字典模块
- **业务逻辑**：提供系统枚举数据（用户状态、角色、审核状态、商品状态、订单状态、评价状态、成色选项等）
- **产品逻辑**：前端通过字典接口动态获取筛选选项，实现配置化的状态管理
- **技术实现**：
  - 后端：DictController，从数据库查询枚举数据返回给前端
  - 前端：enums.js动态加载枚举 + constants.js静态常量定义
- **API端点**：
  - GET /api/dict/options - 获取字典选项

#### 模块4：通知消息模块
- **业务逻辑**：站内通知发送、未读计数、标记已读、批量已读、删除通知
- **产品逻辑**：系统通知、交易消息、审核消息、互动消息四种类型，支持消息中心展示和Header角标提示
- **技术实现**：
  - 后端：NotificationController + NotificationService/NotificationServiceImpl + NotificationRepository + Notification实体 + NotificationDTO/NotificationQueryDTO
  - 前端：MessageCenter组件 + MessageList页面 + Header未读角标
- **API端点**：
  - GET /api/notifications - 通知列表
  - GET /api/notifications/unread-count - 未读数量
  - GET /api/notifications/recent - 最近通知
  - POST /api/notifications/{id}/read - 标记已读
  - POST /api/notifications/batch-read - 批量标记已读
  - POST /api/notifications/read-all - 全部标记已读
  - DELETE /api/notifications/{id} - 删除通知
  - POST /api/notifications/send - 发送通知

#### 模块5：地址管理模块
- **业务逻辑**：收货地址CRUD、默认地址设置、地址列表查询
- **产品逻辑**：买家可管理多个收货地址，下单时选择收货地址
- **技术实现**：
  - 后端：AddressController + AddressService + AddressRepository
  - 前端：AddressList页面 + AddressCard组件 + EditAddressModal组件
- **API端点**：
  - GET /api/addresses - 地址列表
  - POST /api/addresses - 新增地址
  - PUT /api/addresses/{id} - 更新地址
  - DELETE /api/addresses/{id} - 删除地址
  - PUT /api/addresses/{id}/default - 设为默认地址

#### 模块6：UI设计规范系统
- **色彩系统**：主色调#165DFF（10级色阶）、功能色（成功#00B42A/警告#FF7D00/危险#F53F3F/信息#165DFF）、中性色6级灰阶、背景色体系
- **字体规范**：系统字体栈（-apple-system/BlinkMacSystemFont/Segoe UI/Roboto等）、9级字号（12px-30px）、3级行高（1.25/1.5/1.75）、4级字重（400/500/600/700）
- **间距规范**：基于4px基准网格，8级间距（4px/8px/12px/16px/24px/32px/48px/64px）
- **圆角规范**：5级圆角（4px/8px/12px/16px/9999px）
- **阴影规范**：4级阴影（sm/md/lg/xl），随Z轴深度递增
- **运营后台主题**：深蓝白色调设计系统，包含独立CSS变量体系、5个Mixin（ops-card/ops-page-container/ops-toolbar/ops-table-actions/ops-status-tag）、4级响应式断点（sm:767px/md:1023px/lg:1279px/xl:1535px）
- **组件样式**：card/btn-primary/input-field基础组件样式

#### Scenario: 基础模块文档完整性
- **WHEN** 查看系统基础模块设计与实现文档
- **THEN** 每个模块包含业务逻辑、产品逻辑、技术实现、API端点、数据库设计、前端组件映射的完整描述

---

### Requirement: 系统核心模块设计与实现文档
系统 SHALL 生成一份完整的系统核心模块设计与实现文档，包含以下模块的详细分析：

#### 模块1：商品管理模块
- **业务逻辑**：
  - 商品创建（标题/价格/描述/图片/分类/成色/校区）
  - 商品列表查询（分页/关键词搜索/分类筛选/成色筛选/校区筛选/排序）
  - 商品详情查看
  - 商品审核（待审核→已上架/已驳回）
  - 商品上下架
  - 商品编辑和删除
  - 商品统计（浏览量/点击量/收藏量/热度评分）
  - 热门商品推荐
- **产品逻辑**：
  - 卖家发布商品需审核通过后才可在前台展示
  - 买家可浏览、搜索、筛选商品
  - 商品卡片展示核心信息（图片/标题/价格/成色/校区）
  - 商品详情页展示完整信息和评价
  - 运营后台可管理所有商品和审核流程
- **技术实现**：
  - 后端：ItemController + ItemService + ItemRepository + ItemStatsService + ItemStatsRepository
  - 前端：PublishItem页面 + MyItems页面 + Items列表页 + ItemDetail详情页 + ItemCard组件 + MasonryLayout瀑布流 + SearchFilter筛选器 + ImageGallery图片展示
  - 数据库：item表 + item_stats表 + review_log表
  - 商品状态机：PENDING_REVIEW → APPROVED/REJECTED，APPROVED → OFF_SHELF
- **API端点**：
  - POST /api/items - 创建商品
  - GET /api/items - 商品列表
  - GET /api/items/{id} - 商品详情
  - PUT /api/items/{id} - 更新商品
  - DELETE /api/items/{id} - 删除商品
  - PUT /api/items/{id}/off-shelf - 下架商品
  - GET /api/items/hot - 热门商品
  - POST /api/items/{id}/track-view - 记录浏览
  - POST /api/items/{id}/track-click - 记录点击
  - GET /api/items/stats - 商品统计
  - GET /api/items/{id}/reviews - 商品评价列表
  - POST /api/items/{id}/reviews - 创建商品评论

#### 模块2：订单交易模块
- **业务逻辑**：
  - 订单创建（买家下单/选择收货地址/确认商品信息）
  - 订单状态流转：PENDING_PAYMENT → PAID → SHIPPED → COMPLETED
  - 退款流程：PAID/SHIPPED → REFUNDING → REFUNDED/PAID（拒绝退款回退）
  - 取消订单：PENDING_PAYMENT/PAID → CANCELLED
  - 模拟支付
  - 卖家发货（填写快递公司+快递单号）
  - 买家确认收货
  - 订单分页查询（按角色/状态筛选）
  - 运营端订单管理
  - 订单统计（今日订单数/今日完成数/今日金额/总金额）
- **产品逻辑**：
  - 买家和卖家均可查看各自订单
  - 同时具有买家和卖家角色的用户可查看全部订单
  - 订单状态决定可执行的操作（按钮显隐）
  - 运营后台可查看所有订单和统计数据
- **技术实现**：
  - 后端：OrderController + OrderService + OrderRepository
  - 前端：Orders页面 + OrderConfirm页面 + OrderCard组件 + OrderActions组件 + OrderGoods组件
  - 数据库：orders表（包含buyer_id/seller_id/item_id/status/收货信息/快递信息）
  - 状态机：STATUS_TRANSITIONS Map定义合法状态转换
- **API端点**：
  - POST /api/orders - 创建订单
  - GET /api/orders/mine - 我的订单
  - GET /api/orders/{id} - 订单详情
  - POST /api/orders/{id}/pay - 模拟支付
  - POST /api/orders/{id}/ship - 发货
  - POST /api/orders/{id}/confirm - 确认收货
  - POST /api/orders/{id}/cancel - 取消订单
  - POST /api/orders/{id}/refund - 申请退款
  - POST /api/orders/{id}/refund/approve - 同意退款
  - POST /api/orders/{id}/refund/reject - 拒绝退款

#### 模块3：购物车模块
- **业务逻辑**：
  - 添加商品到购物车
  - 购物车列表查询
  - 修改商品数量
  - 选中/取消选中商品
  - 批量更新选中状态
  - 删除购物车商品
  - 清空购物车
  - 购物车商品数量统计
- **产品逻辑**：
  - 买家可将感兴趣的商品加入购物车
  - 购物车支持批量选择和结算
  - Header显示购物车商品数量角标
- **技术实现**：
  - 后端：CartController + CartService + CartRepository
  - 前端：Cart页面 + MiniCart组件
  - 数据库：cart表（user_id + item_id联合唯一索引）
- **API端点**：
  - GET /api/cart - 购物车列表
  - GET /api/cart/count - 购物车数量
  - POST /api/cart - 添加商品
  - PUT /api/cart/{id}/quantity - 更新数量
  - PUT /api/cart/{id}/selected - 更新选中状态
  - PUT /api/cart/selected/batch - 批量更新选中
  - DELETE /api/cart/{id} - 删除商品
  - DELETE /api/cart/item/{itemId} - 按商品删除
  - DELETE /api/cart/clear - 清空购物车

#### 模块4：收藏模块
- **业务逻辑**：
  - 添加/取消收藏
  - 收藏列表查询
  - 收藏数量统计
  - 检查商品是否已收藏
- **产品逻辑**：
  - 买家可收藏感兴趣的商品
  - 商品详情页显示收藏状态
  - 收藏夹页面展示所有收藏商品
- **技术实现**：
  - 后端：FavoriteController + FavoriteService + FavoriteRepository
  - 前端：Favorites页面
  - 数据库：favorite表（user_id + item_id联合唯一索引）
- **API端点**：
  - GET /api/favorites - 收藏列表
  - GET /api/favorites/count - 收藏数量
  - GET /api/favorites/check/{itemId} - 检查是否收藏
  - POST /api/favorites/{itemId} - 添加收藏
  - DELETE /api/favorites/{itemId} - 取消收藏

#### 模块5：评价模块
- **业务逻辑**：
  - 买家提交评价（1-5星评分 + 富文本内容 + 图片）
  - 评价审核（待审核→已通过/已驳回）
  - 卖家回复评价
  - 商品评价列表展示
  - 我的评价查询
  - 评价统计
- **产品逻辑**：
  - 买家在订单完成后可提交评价
  - 评价需审核通过后才展示在商品详情页
  - 卖家可回复买家的评价
  - 运营后台可管理所有评价和审核
- **技术实现**：
  - 后端：ReviewController + ReviewService + ReviewRepository + OpsReviewController
  - 前端：ReviewSubmit页面 + MyReviews页面 + ReviewManage页面 + ReviewAudit页面
  - 数据库：review表（order_id/item_id/buyer_id/seller_id/rating/content/images/reply/status）
  - 评价状态：PENDING → APPROVED/REJECTED
- **API端点**：
  - GET /api/reviews/item/{itemId} - 商品评价列表
  - GET /api/reviews/item/{itemId}/stats - 评价统计
  - GET /api/reviews/order/{orderId} - 订单评价
  - GET /api/reviews/{id} - 评价详情
  - POST /api/reviews - 创建评价
  - GET /api/reviews/my - 我的评价
  - GET /api/reviews/pending - 待审核评价
  - GET /api/reviews/all - 所有评价
  - POST /api/reviews/{id}/approve - 审核通过
  - POST /api/reviews/{id}/reject - 审核拒绝
  - DELETE /api/reviews/{id} - 删除评价
  - POST /api/reviews/{id}/reply - 回复评价

#### 模块6：校园圈子模块
- **业务逻辑**：
  - 帖子发布（标题 + 富文本内容 + 图片 + 标签）
  - 帖子审核（待审核→已通过/已驳回）
  - 帖子点赞/取消点赞
  - 帖子评论（无需审核，实时显示）
  - 帖子列表分页查询
  - 帖子详情查看
  - 我的帖子查询
  - 评论审核管理
- **产品逻辑**：
  - 校园圈子是社区互动功能，用户可发布图文动态
  - 帖子需审核通过后展示，评论无需审核
  - 支持点赞和评论互动
  - 运营后台可管理帖子和评论审核
- **技术实现**：
  - 后端：CircleController + CircleService + CirclePostRepository + CircleCommentRepository + CircleLikeRepository
  - 前端：CircleHome页面 + CircleDetail页面 + CirclePublish页面 + CircleManage页面 + CircleReview页面
  - 数据库：circle_post表 + circle_comment表 + circle_like表（post_id + user_id联合唯一索引）
  - 帖子状态：PENDING → APPROVED/REJECTED
- **API端点**：
  - GET /api/circle/posts - 帖子列表
  - GET /api/circle/posts/{id} - 帖子详情
  - POST /api/circle/posts - 发布帖子
  - DELETE /api/circle/posts/{id} - 删除帖子
  - POST /api/circle/posts/{id}/like - 点赞/取消点赞
  - GET /api/circle/posts/{id}/comments - 评论列表
  - POST /api/circle/posts/{id}/comments - 发表评论
  - GET /api/circle/my - 我的帖子
  - GET /api/circle/pending - 待审核帖子
  - GET /api/circle/posts/list - 帖子列表（管理）
  - GET /api/circle/comments/pending - 待审核评论
  - GET /api/circle/comments/approved - 已审核评论
  - POST /api/circle/posts/{id}/approve - 审核通过帖子
  - POST /api/circle/posts/{id}/reject - 审核拒绝帖子
  - POST /api/circle/comments/{id}/approve - 审核通过评论
  - POST /api/circle/comments/{id}/reject - 审核拒绝评论

#### 模块7：AI助手模块
- **业务逻辑**：
  - AI对话会话管理
  - 消息发送与接收
  - 知识库检索增强
- **产品逻辑**：
  - 为用户提供智能客服和商品推荐
  - 基于知识库的RAG增强回答质量
- **技术实现**：
  - 后端：AIController + AIService + AISessionRepository + AIMessageRepository + KnowledgeRepository
  - 前端：AiAssistant组件
  - 数据库：ai_session表 + ai_message表 + knowledge_chunk表
  - AI集成：智谱AI GLM-4-Flash模型

#### 模块8：运营后台模块
- **业务逻辑**：
  - 数据统计仪表盘（用户/商品/订单/圈子/评价统计）
  - 用户管理（卖家管理/买家管理/用户管理）
  - 商品管理与审核
  - 订单管理与审核
  - 圈子管理与审核
  - 评价管理与审核
  - 消息中心
  - 审批工作台（统一审核入口）
  - 订单趋势/分类统计/状态分布图表
  - 最近活动记录
- **产品逻辑**：
  - 运营人员通过独立后台管理系统
  - 深色侧边栏导航，现代化界面设计
  - 数据可视化图表展示
  - 统一审批工作台整合多类型审核
- **技术实现**：
  - 后端：OpsController + OpsReviewController + OpsAuthService + OpsAccountRepository + StatsService + SellerStatsService
  - 前端：OpsLayout布局 + EnhancedDashboard仪表盘 + 各管理页面 + ApprovalWorkspace审批工作台 + 图表组件（OpsBarChart/OpsDonutChart/OpsLineChart）
  - 数据库：ops_account表（独立于user_account）
  - 权限：OPS角色独立认证体系
- **API端点**（核心）：
  - GET /api/ops/stats - 运营统计
  - GET /api/ops/stats/brief - 简要统计
  - GET /api/ops/stats/full - 完整统计
  - GET /api/ops/stats/circle - 圈子统计
  - GET /api/ops/orders/trend - 订单趋势
  - GET /api/ops/items/category-stats - 分类统计
  - GET /api/ops/orders/status-distribution - 状态分布
  - GET /api/ops/recent-activities - 最近活动
  - GET /api/ops/orders - 订单分页
  - GET /api/ops/vendors - 卖家列表
  - GET /api/ops/buyers - 买家列表
  - GET /api/ops/users - 用户列表
  - GET /api/ops/users/{id} - 用户详情
  - PUT /api/ops/users/{id}/role - 更新角色
  - PUT /api/ops/users/{id}/status - 更新状态
  - GET /api/ops/items/pending - 待审核商品
  - GET /api/ops/items/pending-count - 待审核数量
  - POST /api/ops/items/{id}/approve - 审核通过
  - POST /api/ops/items/{id}/reject - 审核拒绝

#### 模块9：卖家统计模块
- **业务逻辑**：
  - 卖家商品发布统计
  - 卖家订单交易统计
  - 卖家收入统计
- **产品逻辑**：
  - 卖家可查看自己的销售数据和业绩概览
  - 支持趋势分析和数据可视化
- **技术实现**：
  - 后端：SellerStatsController + SellerStatsService
  - 前端：SellerStats页面

#### Scenario: 核心模块文档完整性
- **WHEN** 查看系统核心模块设计与实现文档
- **THEN** 每个模块包含业务逻辑、产品逻辑、技术实现、API端点、数据库设计、前端组件映射、状态流转的完整描述

---

### Requirement: 技术架构总览
文档 SHALL 包含系统整体技术架构分析：

#### 前端技术栈
- Vue 3.5.x + Vite 6.x（构建工具）
- Arco Design Vue 2.56.x（UI组件库）
- Pinia 2.2.x（状态管理）
- Vue Router 4.4.x（路由管理）
- Tiptap 3.x（富文本编辑器）
- ECharts 6.x + vue-echarts（数据可视化）
- Axios 1.7.x（HTTP客户端）
- SCSS/Less（样式预处理）

#### 后端技术栈
- Spring Boot 3.3.5（应用框架）
- Java 17（运行环境）
- Spring Security（安全框架）
- Spring Data JPA（数据访问，实际使用JdbcTemplate）
- MySQL 8.x（数据库）
- JJWT 0.12.6（JWT库）
- BCrypt（密码加密）

#### 系统架构模式
- 前后端分离架构
- RESTful API设计
- JWT无状态认证
- 基于角色的访问控制（RBAC）
- 运营系统与用户系统双轨隔离

#### 数据库设计
- 17张核心业务表
- utf8mb4字符集
- 合理的索引设计（单列索引+联合索引+唯一索引）
- 审计字段（created_at/updated_at自动维护）

#### Scenario: 技术架构文档完整性
- **WHEN** 查看技术架构总览
- **THEN** 包含完整的技术栈说明、架构模式、数据库设计、安全机制的描述

---

## MODIFIED Requirements
无修改的需求项。

## REMOVED Requirements
无移除的需求项。

---

## 文档输出格式要求

### 系统基础模块设计与实现文档结构
1. 概述（模块定位与整体架构）
2. 认证授权模块设计与实现
3. 文件上传模块设计与实现
4. 数据字典模块设计与实现
5. 通知消息模块设计与实现
6. 地址管理模块设计与实现
7. UI设计规范系统设计与实现
8. 基础模块间协作关系

### 系统核心模块设计与实现文档结构
1. 概述（核心业务架构与模块关系）
2. 商品管理模块设计与实现
3. 订单交易模块设计与实现
4. 购物车模块设计与实现
5. 收藏模块设计与实现
6. 评价模块设计与实现
7. 校园圈子模块设计与实现
8. AI助手模块设计与实现
9. 运营后台模块设计与实现
10. 卖家统计模块设计与实现
11. 核心模块间协作关系

### 每个模块的文档结构
1. 模块概述
2. 业务逻辑分析
3. 产品逻辑分析
4. 技术实现分析
   - 后端实现（Controller/Service/Repository/Entity）
   - 前端实现（页面/组件/服务/状态）
   - 数据库设计（表结构/索引/关系）
5. API接口设计
6. 状态流转图（如有）
7. 关键代码实现说明
