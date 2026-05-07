# 校园二手交易平台全面优化升级 - 实施任务列表

## Phase 1: 基础架构与设计规范

### Task 1: 前端目录结构优化
- **Priority**: P0
- **Depends On**: None
- **Description**:
  - [ ] 1.1 优化前端项目目录结构，按功能模块重新组织
  - [ ] 1.2 创建标准化的components目录（layout/common/form/data）
  - [ ] 1.3 创建views/portal和views/ops视图目录
  - [ ] 1.4 创建stores状态管理目录
  - [ ] 1.5 创建services API服务目录
  - [ ] 1.6 统一组件命名规范
- **Acceptance Criteria Addressed**: AC-1, AC-7, AC-8
- **Test Requirements**:
  - `programmatic` TR-1.1: 目录结构符合Vue 3最佳实践
  - `human-judgement` TR-1.2: 目录结构清晰，便于维护

---

### Task 2: UI设计规范系统建立
- **Priority**: P0
- **Depends On**: Task 1
- **Description**:
  - [ ] 2.1 创建styles/variables.scss色彩变量文件
  - [ ] 2.2 创建styles/typography.scss字体规范文件
  - [ ] 2.3 创建styles/spacing.scss间距标准文件
  - [ ] 2.4 创建styles/components.scss组件样式基础
  - [ ] 2.5 配置全局样式导入
- **Acceptance Criteria Addressed**: AC-1, AC-7, AC-8
- **Test Requirements**:
  - `human-judgement` TR-2.1: 设计规范完整且符合文档要求
  - `programmatic` TR-2.2: SCSS变量可在全局使用

---

### Task 3: 公共组件库创建
- **Priority**: P0
- **Depends On**: Task 2
- **Description**:
  - [ ] 3.1 创建Header.vue全局导航组件（含消息中心入口）
  - [ ] 3.2 创建Footer.vue页脚组件
  - [ ] 3.3 创建StatsCard.vue统计卡片组件
  - [ ] 3.4 创建PageContainer.vue页面容器组件
  - [ ] 3.5 创建StatusTag.vue状态标签组件
- **Acceptance Criteria Addressed**: AC-1, AC-7, AC-8
- **Test Requirements**:
  - `programmatic` TR-3.1: 所有公共组件可正常导入使用
  - `programmatic` TR-3.2: 组件符合设计规范样式

---

## Phase 2: 核心前端功能实现

### Task 4: 首页现代化设计
- **Priority**: P1
- **Depends On**: Task 3
- **Description**:
  - [ ] 4.1 重新设计Hero区域（搜索框+轮播Banner）
  - [ ] 4.2 实现分类快捷入口模块
  - [ ] 4.3 集成商品瀑布流展示区
  - [ ] 4.4 添加响应式布局适配
  - [ ] 4.5 实现页面加载动画效果
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgement` TR-4.1: 首页视觉效果现代化美观
  - `programmatic` TR-4.2: 页面加载时间 < 2秒
  - `programmatic` TR-4.3: 响应式布局正常工作

---

### Task 5: 商品瀑布流布局实现
- **Priority**: P0
- **Depends On**: Task 2
- **Description**:
  - [ ] 5.1 使用CSS Grid实现MasonryLayout组件
  - [ ] 5.2 实现响应式列数自适应（移动端2列、桌面端4-5列）
  - [ ] 5.3 集成Intersection Observer实现无限滚动
  - [ ] 5.4 实现图片懒加载优化
  - [ ] 5.5 添加商品卡片悬停动效
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-5.1: 瀑布流布局正确显示
  - `programmatic` TR-5.2: 无限滚动自动加载正常
  - `programmatic` TR-5.3: 图片懒加载生效

---

### Task 6: 富文本编辑器集成
- **Priority**: P0
- **Depends On**: Task 1
- **Description**:
  - [ ] 6.1 安装Tiptap依赖包
  - [ ] 6.2 创建RichEditor.vue可复用组件
  - [ ] 6.3 实现工具栏（加粗/斜体/标题/列表/图片/链接）
  - [ ] 6.4 集成图片上传功能
  - [ ] 6.5 统一编辑器样式与整体UI风格
- **Acceptance Criteria Addressed**: AC-3, AC-4, AC-5
- **Test Requirements**:
  - `programmatic` TR-6.1: 编辑器正常加载显示
  - `programmatic` TR-6.2: 格式设置功能正常工作
  - `programmatic` TR-6.3: 图片上传插入成功

---

## Phase 3: 业务功能前端实现

### Task 7: 评价功能前端实现
- **Priority**: P1
- **Depends On**: Task 6
- **Description**:
  - [ ] 7.1 创建评价提交页面ReviewSubmit.vue（使用富文本编辑器）
  - [ ] 7.2 实现1-5星评分选择组件
  - [ ] 7.3 在商品详情页添加评价展示区域
  - [ ] 7.4 实现评价状态显示（待审核/已通过）
  - [ ] 7.5 实现卖家回复评价UI
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `programmatic` TR-7.1: 用户可提交评价（评分+富文本）
  - `programmatic` TR-7.2: 评价状态正确显示
  - `programmatic` TR-7.3: 已通过评价在商品详情展示

---

### Task 8: 校园圈子功能前端实现
- **Priority**: P1
- **Depends On**: Task 6
- **Description**:
  - [ ] 8.1 创建圈子首页CircleHome.vue
  - [ ] 8.2 实现圈子内容发布页面CirclePublish.vue
  - [ ] 8.3 实现帖子详情页CircleDetail.vue
  - [ ] 8.4 实现点赞和评论交互功能
  - [ ] 8.5 实现帖子列表瀑布流展示
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - `programmatic` TR-8.1: 用户可发布圈子内容
  - `programmatic` TR-8.2: 内容状态正确显示
  - `programmatic` TR-8.3: 点赞评论功能正常

---

### Task 9: 消息中心前端实现
- **Priority**: P1
- **Depends On**: Task 3
- **Description**:
  - [ ] 9.1 创建MessageCenter.vue消息中心组件
  - [ ] 9.2 在Header组件集成未读消息数角标
  - [ ] 9.3 实现消息下拉列表展示
  - [ ] 9.4 创建消息列表页面MessageList.vue
  - [ ] 9.5 实现标记已读和全部已读功能
- **Acceptance Criteria Addressed**: AC-7 (AC-9 in spec)
- **Test Requirements**:
  - `programmatic` TR-9.1: 消息图标显示未读数量
  - `programmatic` TR-9.2: 消息列表正确展示
  - `programmatic` TR-9.3: 消息状态更新正常

---

## Phase 4: 运营后台前端实现

### Task 10: 独立审批工作台界面
- **Priority**: P0
- **Depends On**: Task 3
- **Description**:
  - [ ] 10.1 创建ApprovalWorkspace.vue审批工作台页面
  - [ ] 10.2 实现标签页切换（商品/评价/帖子）
  - [ ] 10.3 实现待审核列表展示组件
  - [ ] 10.4 实现审核通过/拒绝操作弹窗
  - [ ] 10.5 显示待审核数量统计
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - `programmatic` TR-10.1: 审批工作台展示待审核列表
  - `programmatic` TR-10.2: 支持审核通过和拒绝操作
  - `programmatic` TR-10.3: 审核记录正确更新

---

### Task 11: 运营后台现代化改造
- **Priority**: P1
- **Depends On**: Task 3, Task 10
- **Description**:
  - [ ] 11.1 重新设计OpsLayout.vue深色侧边栏
  - [ ] 11.2 升级Dashboard仪表盘页面
  - [ ] 11.3 优化数据统计卡片展示
  - [ ] 11.4 现代化用户管理页面UserManage.vue
  - [ ] 11.5 现代化商品管理页面VendorManage.vue
  - [ ] 11.6 现代化订单管理页面Orders/index.vue
  - [ ] 11.7 现代化买家管理页面BuyerManage.vue
  - [ ] 11.8 现代化评价管理页面Reviews/index.vue
  - [ ] 11.9 优化侧边栏导航响应式适配
- **Acceptance Criteria Addressed**: AC-7 (AC-8 in spec)
- **Test Requirements**:
  - `human-judgement` TR-11.1: 运营后台视觉效果现代化
  - `programmatic` TR-11.2: 侧边栏导航响应式适配
  - `programmatic` TR-11.3: 数据统计卡片正确展示

---

### Task 12: 关于我们页面创建
- **Priority**: P2
- **Depends On**: Task 2
- **Description**:
  - [ ] 12.1 创建AboutUs.vue关于我们页面
  - [ ] 12.2 实现平台介绍内容区
  - [ ] 12.3 实现团队信息展示区
  - [ ] 12.4 实现联系方式展示区
  - [ ] 12.5 配置路由和导航链接
- **Acceptance Criteria Addressed**: AC-6
- **Test Requirements**:
  - `human-judgement` TR-12.1: 页面内容完整、设计美观
  - `programmatic` TR-12.2: 路由配置正确

---

## Phase 5: 后端API实现

### Task 13: 评价API后端实现
- **Priority**: P0
- **Depends On**: None
- **Description**:
  - [ ] 13.1 创建Review实体类（扩展字段：content富文本、images、status、reply_content）
  - [ ] 13.2 创建或扩展ReviewRepository
  - [ ] 13.3 实现ReviewService业务逻辑（提交评价、查询评价、审核管理）
  - [ ] 13.4 扩展ReviewController API接口：
    - POST /api/reviews - 提交评价
    - GET /api/reviews/item/{itemId} - 商品评价列表
    - GET /api/reviews/my - 我的评价
    - GET /api/reviews/pending - 待审核评价列表
    - POST /api/reviews/{id}/approve - 审核通过
    - POST /api/reviews/{id}/reject - 审核拒绝
    - POST /api/reviews/{id}/reply - 卖家回复
  - [ ] 13.5 数据库schema升级（review表增加字段）
- **Acceptance Criteria Addressed**: AC-3, AC-5
- **Test Requirements**:
  - `programmatic` TR-13.1: 评价CRUD接口正常工作
  - `programmatic` TR-13.2: 审核状态流转正确
  - `programmatic` TR-13.3: 接口权限控制正确

---

### Task 14: 圈子API后端实现
- **Priority**: P0
- **Depends On**: None
- **Description**:
  - [ ] 14.1 创建CirclePost实体类（id, userId, title, content, images, tags, status, likeCount, commentCount）
  - [ ] 14.2 创建CircleComment实体类
  - [ ] 14.3 创建CircleLike实体类
  - [ ] 14.4 创建CirclePostRepository、CircleCommentRepository、CircleLikeRepository
  - [ ] 14.5 实现CircleService业务逻辑：
    - 发布帖子、查询帖子列表、审核帖子
    - 点赞/取消点赞、发表评论、查询评论
  - [ ] 14.6 创建CircleController API接口：
    - GET /api/circle/posts - 帖子列表（分页）
    - GET /api/circle/posts/{id} - 帖子详情
    - POST /api/circle/posts - 发布帖子
    - DELETE /api/circle/posts/{id} - 删除帖子
    - POST /api/circle/posts/{id}/like - 点赞/取消点赞
    - GET /api/circle/posts/{id}/comments - 评论列表
    - POST /api/circle/posts/{id}/comments - 发表评论
    - GET /api/circle/pending - 待审核帖子
    - POST /api/circle/posts/{id}/approve - 审核通过
    - POST /api/circle/posts/{id}/reject - 审核拒绝
  - [ ] 14.7 数据库schema升级（circle_post、circle_comment、circle_like表）
- **Acceptance Criteria Addressed**: AC-4, AC-5
- **Test Requirements**:
  - `programmatic` TR-14.1: 圈子CRUD接口正常工作
  - `programmatic` TR-14.2: 点赞评论功能正常
  - `programmatic` TR-14.3: 审核流程正确

---

### Task 15: 消息API后端实现
- **Priority**: P1
- **Depends On**: None
- **Description**:
  - [ ] 15.1 创建Message实体类（userId, type, title, content, status, link）
  - [ ] 15.2 创建MessageRepository
  - [ ] 15.3 实现MessageService业务逻辑：
    - 发送消息（系统通知、交易消息、审核消息、互动消息）
    - 查询消息列表、标记已读、全部已读
    - 未读消息计数
  - [ ] 15.4 创建MessageController API接口：
    - GET /api/messages - 消息列表（分页）
    - GET /api/messages/{id} - 消息详情
    - POST /api/messages/{id}/read - 标记已读
    - POST /api/messages/read-all - 全部标记已读
    - GET /api/messages/unread-count - 未读数量
  - [ ] 15.6 数据库schema升级（message表）
  - [ ] 15.7 实现消息自动发送机制（订单状态变更、审核结果等事件触发）
- **Acceptance Criteria Addressed**: AC-9 (消息中心)
- **Test Requirements**:
  - `programmatic` TR-15.1: 消息CRUD接口正常工作
  - `programmatic` TR-15.2: 未读计数准确
  - `programmatic` TR-15.3: 消息自动发送触发正确

---

### Task 16: 文件上传增强
- **Priority**: P1
- **Depends On**: None
- **Description**:
  - [ ] 16.1 扩展UploadController支持富文本图片上传
  - [ ] 16.2 实现图片压缩功能
  - [ ] 16.3 增强文件类型校验（支持jpg/png/webp）
  - [ ] 16.4 增加上传大小限制（单张最大5MB）
  - [ ] 16.5 返回统一的URL格式供富文本编辑器使用
- **Acceptance Criteria Addressed**: AC-3, AC-4, AC-5
- **Test Requirements**:
  - `programmatic` TR-16.1: 图片上传接口正常工作
  - `programmatic` TR-16.2: 文件校验规则生效
  - `programmatic` TR-16.3: 返回URL可访问

---

### Task 17: 数据统计API实现
- **Priority**: P1
- **Depends On**: Task 13, Task 14
- **Description**:
  - [ ] 17.1 创建StatsService统计服务
  - [ ] 17.2 实现运营统计数据接口：
    - 总用户数、今日新增用户
    - 总商品数、待审核商品数
    - 总订单数、今日订单数
    - 总评价数、待审核评价数
    - 总帖子数、待审核帖子数
  - [ ] 17.3 创建或扩展OpsController统计接口
  - [ ] 17.4 实现缓存策略提升查询性能
- **Acceptance Criteria Addressed**: AC-8 (运营后台)
- **Test Requirements**:
  - `programmatic` TR-17.1: 统计数据接口返回正确
  - `programmatic` TR-17.2: 缓存策略有效
  - `programmatic` TR-17.3: 权限验证正确

---

### Task 18: 数据库Schema升级
- **Priority**: P0
- **Depends On**: None
- **Description**:
  - [ ] 18.1 编写migration.sql数据库升级脚本
  - [ ] 18.2 review表增加字段：content(TEXT), images(TEXT), status(VARCHAR), reply_content(TEXT)
  - [ ] 18.3 创建circle_post表
  - [ ] 18.4 创建circle_comment表
  - [ ] 18.5 创建circle_like表
  - [ ] 18.6 创建message表
  - [ ] 18.7 创建必要索引优化查询性能
- **Acceptance Criteria Addressed**: All ACs
- **Test Requirements**:
  - `programmatic` TR-18.1: SQL脚本可正确执行
  - `programmatic` TR-18.2: 表结构符合设计文档
  - `programmatic` TR-18.3: 索引创建成功

---

## Phase 6: 测试与优化

### Task 19: 功能Bug修复与优化
- **Priority**: P1
- **Depends On**: All frontend tasks
- **Description**:
  - [ ] 19.1 修复已知的功能bug
  - [ ] 19.2 优化页面加载性能（目标 < 2秒）
  - [ ] 19.3 修复跨浏览器兼容性问题
  - [ ] 19.4 优化移动端适配问题
  - [ ] 19.5 修复路由跳转问题
- **Acceptance Criteria Addressed**: All ACs
- **Test Requirements**:
  - `programmatic` TR-19.1: 已知bug修复验证通过
  - `programmatic` TR-19.2: 页面加载时间达标
  - `human-judgement` TR-19.3: 各浏览器兼容性良好

---

### Task 20: 前后端联调测试
- **Priority**: P0
- **Depends On**: Task 13-19
- **Description**:
  - [ ] 20.1 评价功能前后端联调
  - [ ] 20.2 圈子功能前后端联调
  - [ ] 20.3 消息中心前后端联调
  - [ ] 20.4 审批工作台前后端联调
  - [ ] 20.5 运营后台数据联调
  - [ ] 20.6 文件上传功能联调
- **Acceptance Criteria Addressed**: All ACs
- **Test Requirements**:
  - `programmatic` TR-20.1: 所有API接口调用正常
  - `programmatic` TR-20.2: 数据传输正确无误
  - `programmatic` TR-20.3: 错误处理完善

---

## Task Dependencies

```
Phase 1 (基础架构):
Task 1 → Task 2 → Task 3

Phase 2 (核心功能):
Task 2 → Task 5 (并行)
Task 3 → Task 4
Task 1 → Task 6 (并行)

Phase 3 (业务功能):
Task 6 → Task 7 (并行)
Task 6 → Task 8 (并行)
Task 3 → Task 9 (并行)

Phase 4 (管理功能):
Task 3 → Task 10
Task 3 + Task 10 → Task 11
Task 2 → Task 12 (并行)

Phase 5 (后端API):
Task 13, 14, 15, 16, 17, 18 (可并行实施)

Phase 6 (测试优化):
All Tasks → Task 19 → Task 20
```

## Priority Summary

| Priority | Task IDs | Description |
|----------|---------|-------------|
| P0 | 1, 2, 3, 5, 6, 10, 13, 14, 18, 20 | 核心功能和关键路径 |
| P1 | 4, 7, 8, 9, 11, 15, 16, 17, 19 | 重要功能增强 |
| P2 | 12 | 辅助功能页面 |
