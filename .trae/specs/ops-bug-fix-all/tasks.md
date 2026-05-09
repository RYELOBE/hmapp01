# Tasks

## Phase 1: 后端安全漏洞修复（CRITICAL）

- [x] Task 1: 修复 SecurityConfig 放行 ops/login + 圈子接口认证
  - [x] 1.1 在 permitAll 列表中添加 `/api/auth/ops/login`
  - [x] 1.2 将 `/api/circle/**` 从 permitAll 移除，改为仅 GET 请求 permitAll
  - [x] 1.3 在 CircleController 写操作方法上添加 `@PreAuthorize("isAuthenticated()")`

- [x] Task 2: 修复密码修改接口安全
  - [x] 2.1 在 AuthController.changePassword 中验证旧密码
  - [x] 2.2 使用 BCrypt 加密新密码后再存入数据库
  - [x] 2.3 验证修改密码后可以正常登录

- [x] Task 3: 修复注册接口角色白名单
  - [x] 3.1 在 AuthService.register 中限制可选角色为 BUYER 和 SELLER
  - [x] 3.2 拒绝包含 OPS、OPS_ADMIN、OPS_SUPER 角色的注册请求

- [x] Task 4: 修复退款审批权限控制
  - [x] 4.1 在 OrderController.approveRefund/rejectRefund 添加卖家或OPS角色校验

- [x] Task 5: 修复 GlobalExceptionHandler 信息泄露
  - [x] 5.1 RuntimeException 和 Exception 处理返回通用错误信息
  - [x] 5.2 使用 SLF4J Logger 替代 ex.printStackTrace()

## Phase 2: 后端数据层Bug修复（HIGH）

- [x] Task 6: 修复 MessageRepository 与 message 表结构不匹配
  - [x] 6.1 修改 ROW_MAPPER 使用 message 表实际列名
  - [x] 6.2 修改所有 SQL 查询语句
  - [x] 6.3 验证消息列表查询正常

- [x] Task 7: 修复 NotificationRepository HashMap 参数顺序
  - [x] 7.1 将 HashMap 改为 LinkedHashMap 或按条件分别构建参数数组

- [x] Task 8: 修复 StatsService 评价总数计算
  - [x] 8.1 改为 `reviewRepository.countAll()`

- [x] Task 9: 修复 CirclePostRepository.save() 缺少 user_name
  - [x] 9.1 INSERT 语句添加 user_name 列和参数

- [x] Task 10: 修复 CircleCommentRepository.save() 缺少 user_name
  - [x] 10.1 INSERT 语句添加 user_name 列和参数

- [x] Task 11: 修复 CircleService 评论审核功能
  - [x] 11.1 给 circle_comment 表添加 status 列（DatabaseUpdater）
  - [x] 11.2 修改 getPendingComments 只查询 status=PENDING
  - [x] 11.3 修改 getReviewedComments 只查询 status!=PENDING
  - [x] 11.4 修改 approveComment 更新 status 为 APPROVED
  - [x] 11.5 修改 rejectComment 更新 status 为 REJECTED

- [x] Task 12: 修复 AddressService 创建地址设置默认逻辑
  - [x] 12.1 先保存地址获取 ID，再调用 setDefault(newId, userId)

- [x] Task 13: 修复 OrderService 买卖家订单分页合并
  - [x] 13.1 合并后截取 pageSize 条记录
  - [x] 13.2 正确计算 total

- [x] Task 14: 修复 ReviewService 评价审核状态机
  - [x] 14.1 approveReview 只允许从 PENDING 转为 APPROVED
  - [x] 14.2 rejectReview 只允许从 PENDING 转为 REJECTED

- [x] Task 15: 修复 NotificationController 分页 total
  - [x] 15.1 返回符合条件的总记录数

## Phase 3: 后端缺失API端点添加

- [x] Task 16: 添加圈子管理列表端点 POST /ops/circle/posts
  - [x] 16.1 支持分页和状态筛选

- [x] Task 17: 添加订单退款审核端点
  - [x] 17.1 POST /api/orders/{id}/refund/approve
  - [x] 17.2 POST /api/orders/{id}/refund/reject

- [x] Task 18: 添加评价删除端点
  - [x] 18.1 DELETE /api/reviews/{id}

- [x] Task 19: 修复 SellerStatsController 路径
  - [x] 19.1 将 @RequestMapping 从 "/api/seller/stats" 改为 "/api/seller"

- [x] Task 20: 添加 POST /api/items/mine 端点
  - [x] 20.1 在 ItemController 添加 POST 版本的 /mine

- [x] Task 21: 添加 GET /api/reviews/{id} 端点
  - [x] 21.1 按 reviewId 获取评价详情

- [x] Task 22: 修复 ItemController 添加 @Validated
  - [x] 22.1 类级别添加 @Validated
  - [x] 22.2 @RequestBody 参数添加 @Validated

## Phase 4: 前端Bug修复

- [x] Task 23: 修复 CircleManage.vue API 路径
  - [x] 23.1 将 `/ops/circle/posts` 改为正确的后端端点

- [x] Task 24: 修复 OrderReview.vue 退款审核路径
  - [x] 24.1 改为 `/orders/${id}/refund/approve` 和 `/orders/${id}/refund/reject`

- [x] Task 25: 修复 orders/index.vue 删除订单
  - [x] 25.1 移除或修改 DELETE /ops/orders/{id} 调用

- [x] Task 26: 修复 ReviewManage.vue API 路径
  - [x] 26.1 评价列表改为 `/reviews/list`
  - [x] 26.2 删除评价改为正确端点

- [x] Task 27: 修复 ops/Layout.vue 退出登录
  - [x] 27.1 使用 authStore.logout()
  - [x] 27.2 跳转到 /login
  - [x] 27.3 修复个人信息跳转路径

- [x] Task 28: 修复 ApprovalWorkspace.vue confirmReject
  - [x] 28.1 删除重复定义
  - [x] 28.2 移除 window 挂载

- [x] Task 29: 修复 EnhancedDashboard.vue featuresGrid
  - [x] 29.1 过滤 RouteView 类型和旧路径重定向项

- [x] Task 30: 修复前端 updateUserRole API
  - [x] 30.1 路径改为 `/ops/users/${id}/roles`
  - [x] 30.2 请求体改为 `{ roles: [role] }`

- [x] Task 31: 清理 services/ops/index.js
  - [x] 31.1 删除角色管理函数
  - [x] 31.2 删除 deleteItem（与 items/index.js 重复）

- [x] Task 32: 修复 ReviewAudit.vue 筛选参数
  - [x] 32.1 后端 /reviews/list 支持 keyword/type 筛选，或前端不传不支持的参数

- [x] Task 33: 修复 services/items/index.js getMyItems
  - [x] 33.1 将 POST /items/mine 改为 GET /items/mine（或等后端添加 POST 端点）

- [x] Task 34: 修复 services/api.js 评价详情路径
  - [x] 34.1 将 `/reviews/${orderId}` 改为 `/reviews/order/${orderId}`

## Phase 5: 验证

- [x] Task 35: 全页面功能验证
  - [x] 35.1 运营端登录正常
  - [x] 35.2 仪表盘数据正确
  - [x] 35.3 用户管理页面正常
  - [x] 35.4 商品管理/审核页面正常
  - [x] 35.5 订单管理/审核页面正常
  - [x] 35.6 圈子管理/审核页面正常
  - [x] 35.7 评价管理/审核页面正常
  - [x] 35.8 消息中心正常
  - [x] 35.9 退出登录正常
  - [x] 35.10 卖家统计页面正常
  - [x] 35.11 密码修改正常
  - [x] 35.12 注册不允许选择OPS角色

# Task Dependencies
- [Task 16] depends on [Task 11] (圈子评论审核需要status列)
- [Task 23] depends on [Task 16]
- [Task 24] depends on [Task 17]
- [Task 26] depends on [Task 18]
- [Task 33] depends on [Task 20]
- [Task 35] depends on [All Tasks 1-34]
