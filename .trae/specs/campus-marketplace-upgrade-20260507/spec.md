# 校园二手交易平台系统全面优化升级 - 实施规范

## Why
现有平台UI设计陈旧、缺少评价/圈子功能、审核流程不完善、运营后台需要现代化改造，且后端需要实现新功能的API接口以支持前端所有功能需求。

## What Changes

### 前端升级内容
- **首页现代化设计**：采用全新视觉风格，包含Hero区域、分类导航、瀑布流商品展示
- **商品瀑布流布局**：响应式自适应布局，支持无限滚动加载
- **富文本编辑器集成**：基于Tiptap实现，支持评价和圈子内容发布
- **评价功能**：完整评价提交、审核、展示流程
- **校园圈子功能**：圈子内容发布、审核、互动功能
- **独立审批工作台**：分离审核与管理功能，支持商品/评价/帖子审核
- **运营后台现代化**：深色侧边栏、数据统计卡片、现代化界面
- **关于我们页面**：平台介绍页面
- **消息中心**：站内消息通知功能
- **全局UI规范统一**：色彩系统、字体规范、间距标准

### 后端新增功能
- **评价API接口**：评价CRUD、审核状态管理
- **圈子API接口**：帖子发布、审核、点赞评论
- **消息API接口**：消息推送、状态管理、未读计数
- **审批工作台API**：统一审核接口、待审核列表查询
- **文件上传增强**：富文本图片上传支持
- **数据统计API**：运营后台统计数据接口

### **BREAKING Changes**
- 无破坏性变更，保持向后兼容

## Impact
- Affected specs: 全部功能模块（FR-1至FR-9）
- Affected code:
  - 前端：views/portal/*, views/ops/*, components/*, services/*
  - 后端：controller/*, service/*, repository/*

---

## ADDED Requirements

### Requirement: 首页现代化设计 (FR-1)
The system SHALL provide a modernized homepage with:
- Hero区域：搜索框 + 轮播Banner
- 分类快捷入口：图标+文字形式
- 瀑布流商品展示区
- 响应式布局适配移动端和桌面端

#### Scenario: 首页加载成功
- **WHEN** 用户访问平台首页
- **THEN** 展示现代化设计的首页，包含轮播图、分类导航、热门商品瀑布流
- **AND** 页面加载时间 < 2秒

### Requirement: 商品瀑布流布局 (FR-2)
The system SHALL implement masonry layout for product listing:
- CSS Grid实现瀑布流效果
- 响应式列数：移动端2列、平板3列、桌面4-5列
- Intersection Observer API实现无限滚动
- 图片懒加载优化性能

#### Scenario: 瀑布流正常显示
- **WHEN** 用户进入商品列表页
- **THEN** 商品按高度自适应排列成瀑布流
- **AND** 滚动到底部自动加载更多商品

### Requirement: 富文本编辑器 (FR-5)
The system SHALL integrate Tiptap rich text editor:
- 支持加粗、斜体、删除线、标题、列表
- 支持图片上传插入
- 支持链接插入
- 编辑器样式与整体UI风格统一

#### Scenario: 富文本编辑器使用
- **WHEN** 用户在评价或圈子页面打开编辑器
- **THEN** 显示格式化工具栏和编辑区域
- **AND** 用户可输入富文本内容并插入图片

### Requirement: 评价功能 (FR-3)
The system SHALL implement complete review functionality:
- 买家可在订单完成后15天内提交评价
- 评价包含1-5星评分和富文本内容
- 评价需经过审核才能展示
- 审核通过的评价显示在商品详情页
- 卖家可回复评价

#### Scenario: 评价提交流程
- **WHEN** 用户完成订单后进入评价页面
- **THEN** 可使用富文本编辑器撰写评价并选择评分
- **AND** 提交后评价状态为"待审核"

#### Scenario: 评价审核通过
- **WHEN** 运营人员审核通过评价
- **THEN** 评价状态变为"已通过"
- **AND** 评价展示在商品详情页

### Requirement: 校园圈子功能 (FR-4)
The system SHALL implement campus circle feature:
- 用户可发布图文帖子
- 帖子需经过审核才能展示
- 支持点赞、评论互动
- 评论实时显示无需审核

#### Scenario: 圈子内容发布
- **WHEN** 用户登录后进入圈子页面点击发布
- **THEN** 可输入标题和富文本内容
- **AND** 提交后内容状态为"待审核"

#### Scenario: 圈子内容展示
- **WHEN** 圈子帖子审核通过
- **THEN** 帖子显示在圈子首页列表
- **AND** 其他用户可点赞和评论

### Requirement: 独立审批工作台 (FR-6)
The system SHALL provide independent approval workspace:
- 统一的审批工作台界面
- 标签页切换：商品/评价/帖子
- 待审核列表展示
- 支持批量审核操作
- 审核记录和原因填写

#### Scenario: 运营人员审核内容
- **WHEN** 运营人员进入审批工作台
- **THEN** 可看到三个标签页的待审核内容数量
- **AND** 点击查看详情并进行通过/拒绝操作

### Requirement: 运营后台现代化 (FR-7)
The system SHALL modernize operations backend:
- 深色渐变侧边栏导航
- 数据统计仪表盘卡片
- 现代化表格和筛选器
- 响应式布局适配
- 统一的设计语言

#### Scenario: 运营后台访问
- **WHEN** 运营人员登录后台
- **THEN** 展示现代化的管理界面
- **AND** 包含数据统计概览和快捷操作入口

### Requirement: 关于我们页面 (FR-8)
The system SHALL provide "About Us" page:
- 平台介绍和愿景
- 团队信息展示
- 联系方式
- 符合整体设计风格

#### Scenario: 访问关于我们页面
- **WHEN** 用户点击"关于我们"链接
- **THEN** 展示平台介绍页面
- **AND** 内容完整、排版美观

### Requirement: 消息中心 (FR-9)
The system SHALL implement message center:
- 顶部导航显示未读消息数
- 消息类型：系统通知、交易消息、审核消息、互动消息
- 消息列表展示
- 支持标记已读和全部已读
- 点击消息跳转相关页面

#### Scenario: 新消息通知
- **WHEN** 用户有新消息时登录
- **THEN** 消息图标显示未读数量角标
- **AND** 点击展开消息下拉列表

---

## MODIFIED Requirements

### Requirement: 商品管理功能增强
原有商品管理功能增加以下能力：
- 商品描述字段支持富文本存储
- 商品列表页采用瀑布流布局
- 商品图片懒加载优化
- 商品卡片悬停动效

### Requirement: 认证授权增强
原有认证系统增加：
- 运营账号与普通账号完全隔离
- JWT Token自动刷新机制
- 角色权限细粒度控制

### Requirement: 文件上传功能增强
原有上传功能增加：
- 富文本编辑器图片上传接口
- 图片压缩和格式转换
- 上传进度反馈
- 文件大小和类型校验

---

## REMOVED Requirements

无移除的需求项。

---

## Technical Constraints

### 前端技术栈
- Vue 3.5.x + Vite 5.x
- Arco Design Vue 2.56.x
- Pinia 2.2.x 状态管理
- Tiptap 2.x 富文本编辑器
- JavaScript（不使用TypeScript）

### 后端技术栈
- Spring Boot 3.x
- Java 17+
- Spring Data JPA
- MySQL 8.x
- Redis 7.x（缓存）

### 设计规范
- 主色调：#165DFF（蓝色）
- 字体：PingFang SC / Microsoft YaHei
- 圆角：4px/8px/12px/16px
- 阴影：多层阴影系统
- 间距：4px/8px/16px/24px/32px/48px/64px

---

## Acceptance Criteria Summary

| ID | 功能 | 验证方式 |
|----|------|----------|
| AC-1 | 首页现代化 | human-judgment + programmatic |
| AC-2 | 商品瀑布流 | programmatic |
| AC-3 | 评价功能 | programmatic |
| AC-4 | 校园圈子 | programmatic |
| AC-5 | 审批工作台 | programmatic |
| AC-6 | 关于我们 | human-judgment |
| AC-7 | 消息中心 | programmatic |
| AC-8 | 运营后台 | human-judgment + programmatic |
| AC-9 | Bug修复 | programmatic |

---

## Implementation Phases

### Phase 1: 基础架构搭建
- 目录结构优化
- UI设计规范建立
- 公共组件库创建

### Phase 2: 核心功能实现
- 首页现代化
- 商品瀑布流
- 富文本编辑器

### Phase 3: 业务功能实现
- 评价功能
- 校园圈子
- 消息中心

### Phase 4: 管理功能实现
- 审批工作台
- 运营后台改造
- 关于我们页面

### Phase 5: 后端API实现
- 评价API
- 圈子API
- 消息API
- 审核API
- 统计API

### Phase 6: 测试与优化
- 功能测试
- 性能优化
- Bug修复
- 兼容性测试
