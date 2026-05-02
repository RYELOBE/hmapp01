# 校园二手交易平台UI升级 - Product Requirement Document

## Overview
- **Summary**: 根据UI设计规范文档，对校园二手交易平台进行全面的视觉和交互升级改造，保持原有的业务逻辑功能不变，仅进行UI层面的优化。
- **Purpose**: 提升平台的视觉品质和用户体验，打造专业、现代、统一的设计风格，参考湾数通平台的设计理念。
- **Target Users**: 平台的所有用户，包括普通买家、卖家、运营管理员。

## Goals
- 建立统一的设计语言体系（色彩、字体、间距、圆角、阴影、动效）
- 优化所有核心页面的UI呈现，包括首页、商品详情、买家中心、卖家中心、运营后台
- 使用Arco Design Vue组件库的统一设计语言
- 实现白色背景基调、蓝色(#165DFF)为主题色的视觉风格
- 添加微交互动效提升用户体验
- 确保响应式设计在各设备上的良好展示

## Non-Goals (Out of Scope)
- 不修改任何业务逻辑功能
- 不修改后端API接口
- 不调整数据库结构
- 不添加新的功能特性
- 不改变用户权限体系

## Background & Context
- 现有代码库基于Vue 3 + Vite + Arco Design Vue
- 采用微前端架构，包含shell、portal、ops、mf-shared四个应用
- 已有完整的业务功能实现
- 参考文档：`/workspace/.trae/specs/campus-marketplace-20260501/UI_DESIGN_SPECIFICATION.md`

## Functional Requirements
- **FR-1**: 全局CSS变量定义，建立完整的设计系统
- **FR-2**: 首页UI升级（轮播、分类导航、商品网格、商品卡片）
- **FR-3**: 商品详情页UI升级
- **FR-4**: 买家中心页面UI升级（订单列表、收藏、购物车、地址管理）
- **FR-5**: 卖家中心页面UI升级（商品管理、发布商品、订单管理）
- **FR-6**: 运营后台UI升级（工作台、商品审核、用户管理等）
- **FR-7**: 登录注册页面UI升级
- **FR-8**: 共享组件UI升级（ItemCard、PageHeader等）
- **FR-9**: 加载状态、空状态、错误状态的统一设计
- **FR-10**: 响应式布局适配优化

## Non-Functional Requirements
- **NFR-1**: 页面加载性能不降低，保持当前水平
- **NFR-2**: 所有动画过渡流畅，帧率保持在50fps以上
- **NFR-3**: 色彩对比度符合WCAG 2.1 AA级标准
- **NFR-4**: 键盘可访问性保持良好
- **NFR-5**: 浏览器兼容性保持不变（Chrome 90+、Firefox 88+、Safari 14+、Edge 90+）

## Constraints
- **Technical**: Vue 3、Vite、Arco Design Vue、微前端架构
- **Business**: 保持业务逻辑功能完全不变，仅UI升级
- **Dependencies**: Arco Design Vue组件库

## Assumptions
- 现有业务逻辑完整且稳定
- Arco Design Vue组件库提供足够的组件和配置灵活性
- 微前端架构各应用可以独立升级UI而不相互影响

## Acceptance Criteria

### AC-1: 全局设计系统实现
- **Given**: 项目代码库
- **When**: 应用新的CSS变量和主题配置
- **Then**: 所有页面和组件都能正确应用新的设计系统（色彩、字体、间距、圆角、阴影）
- **Verification**: `human-judgment`
- **Notes**: 需对照设计规范文档检查视觉一致性

### AC-2: 首页UI符合设计规范
- **Given**: 访问首页
- **When**: 查看页面布局、组件样式、动效
- **Then**: 首页完全按照设计规范呈现（轮播高度280px、分类标签样式、商品卡片8px圆角、hover效果等）
- **Verification**: `human-judgment`

### AC-3: 商品详情页UI符合设计规范
- **Given**: 访问任意商品详情页
- **When**: 查看页面布局和组件
- **Then**: 商品详情页呈现左右分栏布局、图片画廊、信息区域、操作区域符合设计规范
- **Verification**: `human-judgment`

### AC-4: 买家中心页面UI符合设计规范
- **Given**: 登录并访问买家中心各页面
- **When**: 查看订单列表、收藏、购物车、地址管理页面
- **Then**: 左侧导航宽度240px、筛选标签、订单卡片、表格样式等符合设计规范
- **Verification**: `human-judgment`

### AC-5: 卖家中心页面UI符合设计规范
- **Given**: 登录并访问卖家中心各页面
- **When**: 查看商品管理、发布商品、订单管理页面
- **Then**: 筛选区域、表格、分步表单等符合设计规范
- **Verification**: `human-judgment`

### AC-6: 运营后台UI符合设计规范
- **Given**: 登录运营后台
- **When**: 查看工作台、商品审核、用户管理等页面
- **Then**: 深色侧边栏、统计卡片、表格布局符合设计规范
- **Verification**: `human-judgment`

### AC-7: 登录注册页UI符合设计规范
- **Given**: 访问登录/注册页面
- **When**: 查看页面布局
- **Then**: 左右分栏布局、品牌展示区渐变背景、表单卡片宽度400px符合设计规范
- **Verification**: `human-judgment`

### AC-8: 共享组件UI升级完成
- **Given**: 使用共享组件的页面
- **When**: 查看ItemCard、PageHeader等组件
- **Then**: 组件样式、交互符合设计规范
- **Verification**: `human-judgment`

### AC-9: 状态设计统一实现
- **Given**: 浏览应用各页面
- **When**: 遇到加载、空数据、错误状态
- **Then**: 骨架屏、空状态插图、错误提示符合统一的设计风格
- **Verification**: `human-judgment`

### AC-10: 响应式设计正常工作
- **Given**: 在不同尺寸的设备上浏览
- **When**: 调整窗口大小
- **Then**: 页面布局在各断点下正确适配（移动端<768px、平板768-992px、笔记本992-1200px、桌面>1200px）
- **Verification**: `programmatic`

### AC-11: 动效过渡流畅
- **Given**: 操作应用各交互
- **When**: hover、点击、页面切换
- **Then**: 所有动效流畅自然，过渡时长符合规范（150ms-350ms）
- **Verification**: `human-judgment`

### AC-12: 业务功能完整可用
- **Given**: 使用升级后的应用
- **When**: 执行所有原有业务操作
- **Then**: 所有业务功能正常工作，没有因为UI升级导致功能损坏
- **Verification**: `programmatic`

## Open Questions
- [ ] 设计规范中提到的"真实商品场景图片"是否有现成的资源文件？
- [ ] 是否需要为移动端创建专门的底部导航？
- [ ] 运营后台的深色侧边栏是否需要与品牌风格保持更好的协调？
