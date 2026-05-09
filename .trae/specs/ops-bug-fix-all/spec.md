# Ops后台前后端Bug全面修复 Spec

## Why
运营后台存在大量前后端不匹配的API路径、缺失的后端端点、错误的SQL查询、安全漏洞、以及前端组件逻辑错误，导致多个页面无法正常使用，且存在严重的安全风险。

## What Changes
- 修复前端API调用路径与后端不匹配（9个高严重度bug）
- 添加后端缺失的API端点（circle/posts、orders审核、reviews删除等）
- 修复MessageRepository与message表结构不匹配
- 修复SecurityConfig遗漏ops/login放行
- 修复NotificationRepository HashMap参数顺序问题
- 修复StatsService评价总数计算逻辑
- 修复CircleService评论审核功能形同虚设
- 修复CirclePostRepository/CircleCommentRepository缺少user_name字段
- 修复ops/Layout.vue退出登录逻辑
- 修复ApprovalWorkspace.vue confirmReject重复定义
- 清理services/ops/index.js中已废弃的角色管理API
- **[新增]** 修复密码修改接口未验证旧密码且明文存储
- **[新增]** 修复注册接口允许选择OPS角色（权限提升漏洞）
- **[新增]** 修复圈子接口无认证保护
- **[新增]** 修复通知接口缺少用户身份校验
- **[新增]** 修复退款审批接口缺少权限控制
- **[新增]** 修复SellerStatsController路径不匹配
- **[新增]** 修复ItemController GET /items/mine应为POST
- **[新增]** 修复ReviewController评价详情路径
- **[新增]** 修复resource_function表被DROP但代码仍引用
- **[新增]** 修复AddressService创建地址设置默认逻辑错误
- **[新增]** 修复OrderService买卖家订单分页合并逻辑错误
- **[新增]** 修复ReviewService评价审核状态机校验缺失
- **[新增]** 修复GlobalExceptionHandler泄露内部错误信息
- **[新增]** 添加缺失的@Validated注解和输入验证

## Impact
- Affected code:
  - 前端: services/ops/index.js, CircleManage.vue, OrderReview.vue, orders/index.vue, ReviewManage.vue, ops/Layout.vue, ApprovalWorkspace.vue, EnhancedDashboard.vue, ReviewAudit.vue, services/api.js, services/items/index.js
  - 后端: OpsController, CircleController, ReviewController, OrderController, MessageRepository, NotificationRepository, StatsService, CircleService, CirclePostRepository, CircleCommentRepository, SecurityConfig, AuthController, AuthService, ItemController, SellerStatsController, AddressService, OrderService, ReviewService, GlobalExceptionHandler, FunctionRepository

## ADDED Requirements

### Requirement: 前后端API路径对齐
系统 SHALL 确保所有前端API调用路径与后端Controller端点完全匹配。

#### Scenario: 圈子管理页面加载
- **WHEN** 运营人员访问圈子管理页面
- **THEN** 前端调用`POST /ops/circle/posts`，后端存在对应端点并返回帖子列表

#### Scenario: 订单退款审核
- **WHEN** 运营人员审核退款订单
- **THEN** 前端调用正确的`POST /orders/{id}/refund/approve`和`POST /orders/{id}/refund/reject`

#### Scenario: 评价删除
- **WHEN** 运营人员删除评价
- **THEN** 后端提供`DELETE /api/reviews/{id}`端点

### Requirement: 运营端登录可用
系统 SHALL 允许未认证用户访问`POST /api/auth/ops/login`端点。

#### Scenario: 运营人员登录
- **WHEN** 运营人员提交登录表单
- **THEN** 系统返回JWT token，不返回403

### Requirement: 密码修改安全
系统 SHALL 在修改密码时验证旧密码，且新密码必须BCrypt加密后存储。

#### Scenario: 修改密码
- **WHEN** 用户提交密码修改请求
- **THEN** 系统验证旧密码正确后，将新密码BCrypt加密后存入数据库

### Requirement: 注册角色白名单
系统 SHALL 限制注册时可选的角色为BUYER和SELLER，不允许注册OPS相关角色。

#### Scenario: 尝试注册OPS角色
- **WHEN** 用户注册时选择OPS角色
- **THEN** 系统拒绝注册请求并返回错误信息

### Requirement: 圈子接口认证保护
系统 SHALL 对圈子写操作（发帖、评论、点赞、删帖）要求用户认证。

#### Scenario: 未登录用户发帖
- **WHEN** 未认证用户尝试发帖
- **THEN** 系统返回401 Unauthorized

### Requirement: 退款审批权限控制
系统 SHALL 限制退款审批操作仅卖家或运营人员可执行。

#### Scenario: 普通用户审批退款
- **WHEN** 非卖家非运营人员尝试审批退款
- **THEN** 系统返回403 Forbidden

### Requirement: 消息系统可用
系统 SHALL 确保MessageRepository的SQL查询与message表结构匹配。

#### Scenario: 查询消息列表
- **WHEN** 用户请求消息列表
- **THEN** 系统正确查询message表并返回结果，不抛出SQL异常

### Requirement: 通知查询参数正确
系统 SHALL 确保NotificationRepository查询参数与SQL占位符顺序一致。

#### Scenario: 条件查询通知
- **WHEN** 使用多个条件查询通知
- **THEN** 参数按正确顺序传递给SQL，返回正确的查询结果

### Requirement: 评价总数统计正确
系统 SHALL 正确计算评价总数。

#### Scenario: 仪表盘显示评价总数
- **WHEN** 运营人员查看仪表盘
- **THEN** 评价总数显示所有评价的数量，而非itemId=0和itemId=-1的数量

### Requirement: 圈子评论审核有效
系统 SHALL 确保评论审核操作实际更新评论状态。

#### Scenario: 审核通过评论
- **WHEN** 运营人员点击"通过"审核评论
- **THEN** 评论状态更新为APPROVED，不再出现在待审核列表中

### Requirement: ops/Layout.vue退出登录正确
系统 SHALL 使用authStore.logout()清除认证状态并跳转到/login。

#### Scenario: 运营人员退出登录
- **WHEN** 运营人员点击退出登录
- **THEN** 系统清除正确的token并跳转到/login页面

### Requirement: 错误信息安全
系统 SHALL 不在API响应中泄露内部错误信息（SQL语句、堆栈跟踪等）。

#### Scenario: 内部错误发生
- **WHEN** 后端发生RuntimeException
- **THEN** 返回通用错误信息"服务器内部错误"，不包含SQL语句或类路径

### Requirement: 卖家统计API路径
系统 SHALL 在`/api/seller/*`路径下提供卖家统计API。

#### Scenario: 卖家查看统计
- **WHEN** 卖家请求统计概览
- **THEN** 前端调用`/seller/overview`，后端正确响应

### Requirement: 评价审核状态机
系统 SHALL 只允许从PENDING状态转为APPROVED或REJECTED，不允许从REJECTED直接转为APPROVED。

#### Scenario: 审核已拒绝的评价
- **WHEN** 运营人员尝试通过一个已拒绝的评价
- **THEN** 系统拒绝操作并返回错误信息

## MODIFIED Requirements

### Requirement: CirclePostRepository.save()
INSERT语句 SHALL 包含user_name字段，与circle_post表的NOT NULL约束一致。

### Requirement: CircleCommentRepository.save()
INSERT语句 SHALL 包含user_name字段，与circle_comment表的NOT NULL约束一致。

### Requirement: SecurityConfig
permitAll列表 SHALL 包含`/api/auth/ops/login`路径，圈子写操作 SHALL 要求认证。

### Requirement: services/ops/index.js
移除所有角色管理API函数（getRoles, createRole, updateRole, deleteRole, updateRoleStatus, getRoleResources, updateRoleResources），因为后端不存在对应端点且角色管理功能已被删除。

### Requirement: AddressService.createAddress
创建地址时 SHALL 先保存地址获取ID，再设置默认。

### Requirement: OrderService.getMinePaged
买卖家订单合并分页 SHALL 确保返回的记录数不超过pageSize。

### Requirement: ReviewService.approveReview
评价审核 SHALL 验证当前状态为PENDING才允许转为APPROVED。

## REMOVED Requirements

### Requirement: 角色管理API
**Reason**: 角色管理功能已从系统中删除，ops_role表已DROP
**Migration**: 前端services/ops/index.js中删除相关函数，确保无页面引用

### Requirement: resource_function表
**Reason**: schema.sql中已DROP该表，FunctionRepository不再需要
**Migration**: 删除FunctionRepository相关代码
