# 校园二手交易平台UI升级 - The Implementation Plan (Decomposed and Prioritized Task List)

## [ ] Task 1: 全局设计系统CSS变量和主题配置
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在`packages/common/src/theme.js`中更新主题配置（主色#165DFF）
  - 在各应用的`style.css`中定义完整的CSS变量体系
  - 包括色彩系统、字体系统、间距系统、圆角系统、阴影系统、动效规范
- **Acceptance Criteria Addressed**: [AC-1]
- **Test Requirements**:
  - `programmatic` TR-1.1: 检查theme.js中primaryColor是否为"#165DFF"
  - `programmatic` TR-1.2: 检查style.css中是否定义了所有所需的CSS变量
  - `human-judgement` TR-1.3: 视觉检查设计变量在基础组件上的应用效果
- **Notes**: 此任务是所有其他UI升级的基础

## [ ] Task 2: 共享组件ItemCard UI升级
- **Priority**: P0
- **Depends On**: [Task 1]
- **Description**: 
  - 升级`packages/apps/mf-shared/src/exposes/ItemCard/ItemCard.vue`
  - 应用新的设计规范：8px圆角、hover上移4px、阴影效果
  - 优化商品信息的排版和间距
  - 添加卖家信息和收藏按钮
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-8]
- **Test Requirements**:
  - `human-judgement` TR-2.1: 检查ItemCard在不同页面上的视觉效果
  - `human-judgement` TR-2.2: 验证hover状态的阴影和位移效果
  - `programmatic` TR-2.3: 检查ItemCard的props和业务逻辑未改变
- **Notes**: ItemCard在首页、列表页、收藏页等多个地方使用

## [ ] Task 3: 首页UI升级
- **Priority**: P0
- **Depends On**: [Task 1, Task 2]
- **Description**: 
  - 升级`packages/apps/portal/src/views/home/index.vue`
  - 优化轮播横幅：高度280px、12px圆角、阴影、指示器样式
  - 升级分类导航标签样式
  - 优化商品网格布局和间距
  - 添加加载状态和空状态
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-9]
- **Test Requirements**:
  - `human-judgement` TR-3.1: 视觉检查首页布局符合设计规范
  - `human-judgement` TR-3.2: 验证轮播图的动效和交互
  - `programmatic` TR-3.3: 检查首页业务功能（搜索、筛选、排序）正常工作
- **Notes**: 首页是用户访问量最大的页面，需重点关注

## [ ] Task 4: 商品详情页UI升级
- **Priority**: P0
- **Depends On**: [Task 1]
- **Description**: 
  - 升级`packages/apps/portal/src/views/item/index.vue`
  - 实现左右分栏布局（左侧60%，右侧40%）
  - 优化图片画廊展示（主图+缩略图）
  - 升级购买操作区域的按钮和表单样式
  - 添加AI问答悬浮按钮（48px圆形）
- **Acceptance Criteria Addressed**: [AC-1, AC-3]
- **Test Requirements**:
  - `human-judgement` TR-4.1: 检查商品详情页布局符合设计规范
  - `human-judgement` TR-4.2: 验证图片查看功能
  - `programmatic` TR-4.3: 检查购买、收藏等业务功能正常工作
- **Notes**: 商品详情页是转化率最高的页面，UI细节很重要

## [ ] Task 5: 登录注册页面UI升级
- **Priority**: P0
- **Depends On**: [Task 1]
- **Description**: 
  - 升级`packages/apps/portal/src/views/Login.vue`
  - 升级`packages/apps/portal/src/views/Register.vue`
  - 实现左右分栏布局（左侧品牌展示区，右侧表单区）
  - 添加品牌渐变背景（#165DFF至#4080FF）
  - 优化表单样式（输入框、按钮、验证提示）
- **Acceptance Criteria Addressed**: [AC-1, AC-7]
- **Test Requirements**:
  - `human-judgement` TR-5.1: 检查登录/注册页视觉效果
  - `programmatic` TR-5.2: 验证登录/注册业务功能正常
- **Notes**: 登录页是用户的第一印象，需重点优化

## [ ] Task 6: 买家中心 - 订单列表和购物车UI升级
- **Priority**: P1
- **Depends On**: [Task 1, Task 2]
- **Description**: 
  - 升级`packages/apps/portal/src/views/MyOrders.vue`
  - 升级`packages/apps/portal/src/views/Cart.vue`
  - 实现左侧导航（宽度240px）
  - 优化订单卡片和表格样式
  - 添加筛选标签和状态标签（彩色）
- **Acceptance Criteria Addressed**: [AC-1, AC-4]
- **Test Requirements**:
  - `human-judgement` TR-6.1: 视觉检查买家中心各页面
  - `programmatic` TR-6.2: 验证订单查看、取消、支付等功能正常
  - `programmatic` TR-6.3: 验证购物车功能正常
- **Notes**: 买家中心是用户使用频率较高的区域

## [ ] Task 7: 卖家中心 - 商品管理和发布UI升级
- **Priority**: P1
- **Depends On**: [Task 1]
- **Description**: 
  - 升级`packages/apps/portal/src/views/seller/MyItems.vue`
  - 升级`packages/apps/portal/src/views/seller/PublishItem.vue`
  - 优化商品管理表格（审核状态彩色标签）
  - 升级发布商品的分步表单设计
- **Acceptance Criteria Addressed**: [AC-1, AC-5]
- **Test Requirements**:
  - `human-judgement` TR-7.1: 视觉检查卖家中心各页面
  - `programmatic` TR-7.2: 验证商品发布、编辑功能正常
- **Notes**: 卖家中心的表单设计需特别关注用户体验

## [ ] Task 8: 运营后台UI升级
- **Priority**: P1
- **Depends On**: [Task 1]
- **Description**: 
  - 升级`packages/apps/ops/src/views/OpsDashboard.vue`
  - 升级`packages/apps/ops/src/views/ReviewList.vue`
  - 升级`packages/apps/ops/src/views/Layout.vue`（侧边栏）
  - 实现深色侧边栏（#1D2129背景）
  - 优化统计卡片样式
  - 升级表格和筛选区域样式
- **Acceptance Criteria Addressed**: [AC-1, AC-6]
- **Test Requirements**:
  - `human-judgement` TR-8.1: 视觉检查运营后台各页面
  - `programmatic` TR-8.2: 验证审核、用户管理等业务功能正常
- **Notes**: 运营后台的效率和清晰度很重要

## [ ] Task 9: 统一状态设计（加载/空状态/错误状态）
- **Priority**: P2
- **Depends On**: [Task 1]
- **Description**: 
  - 在各应用中实现统一的骨架屏组件
  - 设计并实现空状态插图和文案
  - 优化错误状态的提示样式
  - 应用于各页面
- **Acceptance Criteria Addressed**: [AC-1, AC-9]
- **Test Requirements**:
  - `human-judgement` TR-9.1: 检查各状态在不同页面上的一致性
  - `programmatic` TR-9.2: 验证状态显示逻辑正确

## [ ] Task 10: 响应式布局和动效优化
- **Priority**: P2
- **Depends On**: [Task 1, Task 3, Task 4, Task 5, Task 6, Task 7, Task 8]
- **Description**: 
  - 为各页面添加响应式断点适配
  - 优化移动端、平板端的布局
  - 统一各交互的动效（hover、active、页面切换）
  - 确保过渡时长符合规范
- **Acceptance Criteria Addressed**: [AC-1, AC-10, AC-11]
- **Test Requirements**:
  - `programmatic` TR-10.1: 在各断点尺寸下测试布局适配
  - `human-judgement` TR-10.2: 主观评估动效流畅度
  - `programmatic` TR-10.3: 验证所有业务功能在响应式布局下正常工作
- **Notes**: 这是最终的收尾优化工作
