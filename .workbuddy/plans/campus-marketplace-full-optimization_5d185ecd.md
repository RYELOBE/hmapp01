---
name: campus-marketplace-full-optimization
overview: 校园二手平台全面优化，门户（买家/卖家前台）与运营后台完全分离，门户参考真实校园二手平台设计，运营后台参考 CommonEngine-web/dtp_web/operation-portal 模式
design:
  architecture:
    framework: vue
  styleKeywords:
    - Arco Design
    - 校园集市
    - 暖橙运营
    - 卡片瀑布流
    - 企业后台
  fontSystem:
    fontFamily: PingFang SC
    heading:
      size: 20px
      weight: 700
    subheading:
      size: 16px
      weight: 600
    body:
      size: 14px
      weight: 400
  colorSystem:
    primary:
      - "#0FC6C2"
      - "#F0A838"
      - "#D88C1F"
      - "#336AD8"
    background:
      - "#F5F5F5"
      - "#FAFAFA"
      - "#FFFFFF"
    text:
      - "#1D2129"
      - "#4E5969"
      - "#86909C"
    functional:
      - "#00B42A"
      - "#F53F3F"
      - "#FF7D00"
      - "#165DFF"
todos:
  - id: http-upgrade
    content: 增强 mf-shared http.js 请求封装（createHttp 工厂函数、网络检测、节流提示、isHideErrorMsg、CancelToken、状态码映射）
    status: completed
  - id: shared-hooks-components
    content: 新增 useSearchTable Hook、SearchFilter、ItemCard、ImageGallery 组件到 mf-shared 并配置 exposes
    status: completed
    dependencies:
      - http-upgrade
  - id: portal-layout-home
    content: 重做 Portal 门户 Layout（顶部导航集市风格）+ 首页（商品瀑布流/搜索/分类/Banner）
    status: completed
    dependencies:
      - shared-hooks-components
  - id: portal-detail-seller
    content: 新增商品详情页（大图+下单）+ 卖家中心（发布商品含图片上传/我的商品管理）
    status: completed
    dependencies:
      - portal-layout-home
  - id: portal-orders
    content: 新增订单中心页面（买家/卖家双视角、Tab 状态筛选）
    status: completed
    dependencies:
      - portal-layout-home
  - id: ops-theme-restructure
    content: 建立 ops CSS 主题系统 + 重构现有页面为 index.vue+components/+const.js 架构
    status: completed
    dependencies:
      - shared-hooks-components
  - id: ops-new-pages
    content: 新增 ops Dashboard/供方管理/需方管理页面，更新路由和菜单
    status: completed
    dependencies:
      - ops-theme-restructure
  - id: backend-api
    content: 后端：扩展 item 表字段 + 实现分页搜索/统计/供方/需方/订单分页接口
    status: completed
---

## 产品概述

校园二手交易平台前端全面优化，核心将**门户（portal）**与**运营后台（ops）**彻底区分。门户面向买家/卖家用户，参考闲鱼/转转等校园二手平台，打造集市风格的浏览与交易体验；运营后台面向管理员，保持后台管理风格但优化请求封装、页面架构、样式和业务逻辑。

## 核心功能

### Portal 门户（面向用户）

- **首页**：商品瀑布流/卡片展示、搜索栏、分类筛选、轮播 Banner，类似闲鱼首页体验
- **买家浏览**：商品卡片带图片、价格、卖家信息；搜索+筛选；商品详情页含图片、描述、卖家信息、下单按钮
- **卖家中心**：发布商品（含图片上传、分类、成色）、我的商品管理（状态标签）、编辑/下架
- **订单中心**：买家看已购订单、卖家看已售订单、订单状态流转

### Ops 运营后台（面向管理员）

- **请求封装升级**：参考 CommonEngine-web baseRequest.js，增强网络检测、节流提示、CancelToken
- **页面架构重构**：index.vue + components/ + const.js 模式，useSearchTable Hook 复用
- **样式体系提升**：统一 CSS 变量主题、优化 Layout 视觉
- **业务逻辑完善**：Dashboard 真实统计、订单监控、供方管理、需方管理、审核增强

### 后端接口配套

- 新增门户首页分页查询、分类筛选、搜索接口
- 新增运营统计、供方/需方管理、订单分页接口
- 扩展 item 表支持图片、分类、成色等字段

## 技术栈

- 前端框架：Vue3 + JavaScript + Arco Design + Vite + Pinia + Vue Router
- 微前端：qiankun + Module Federation (mf-shared)
- 门户样式：SCSS + CSS 变量主题 + 卡片瀑布流布局
- 后台样式：SCSS + CSS 变量主题 + 后台管理布局
- 后端：Spring Boot + Sa-Token + MySQL + JDBC
- 包管理：pnpm workspace monorepo

## 实现方案

### 一、共享层优化（mf-shared）

#### 1. http.js 请求封装升级

参考 CommonEngine-web baseRequest.js，重构为工厂函数模式：

- 新增 `createHttp(options)` 工厂函数，支持 `isGlobalShowErrorMsg`、`gotoLoginCallback`、`requestCallback`、`responseCallback` 钩子
- 请求拦截：网络断开检测（navigator.onLine）、GET 请求防缓存时间戳、isHideErrorMsg/isTokenNotAuth 按请求级别控制
- 响应拦截：节流错误提示（throttle 300ms）、完整 HTTP 状态码映射、401 处理改为钩子回调
- 导出 CancelToken 支持请求取消
- 保持现有 `http` 实例和 `get/post/put/_delete` 导出向后兼容

#### 2. useSearchTable Hook

参考 dtp_web commonmodule/hooks/search-table.js：

- 封装 searchForm、tableData、loading、pagination、selectedKeys 状态
- 提供 pageChange、pageSizeChange、sorterChange、search、reset 方法
- 接受 getData 函数和 config 配置
- 放置在 mf-shared/src/exposes/hooks/ 下

#### 3. 新增共享组件

- SearchFilter：筛选栏组件，支持 v-model 绑定、search/reset 事件
- ItemCard：商品卡片组件（图片+标题+价格+卖家），门户和运营后台复用
- ImageGallery：图片展示组件，支持多图轮播

### 二、Portal 门户重构（彻底重做）

#### 设计理念

从后台管理风格改为面向用户的电商集市风格，参考闲鱼/转转：

- 去掉左侧菜单栏，改为顶部导航栏（首页/我要买/我要卖/我的订单）
- 首页为商品瀑布流/卡片布局，而非后台列表
- 商品详情页为大图+信息+下单流程
- 卖家发布页含图片上传、分类选择、成色选择

#### Layout 重构

- 顶部导航：白色背景，Logo "校园集市" + 搜索框 + 导航链接 + 用户头像/下拉
- 内容区：全宽，无侧边栏，background: #f5f5f5
- 底部：简单 footer（可选）
- 买家/卖家角色切换：通过顶部导航切换视角

#### 页面结构

```
portal/src/views/
├── home/                    # 首页（买家视角入口）
│   ├── index.vue            # 瀑布流商品展示 + 搜索 + 分类 + Banner
│   ├── const.js             # 分类等常量
│   └── components/
│       ├── Banner.vue       # 首页轮播
│       ├── CategoryBar.vue  # 分类导航栏
│       └── ItemGrid.vue     # 商品瀑布流/卡片网格
├── item/                    # 商品详情页
│   ├── index.vue            # 大图+详情+下单
│   └── components/
│       └── ImageGallery.vue # 图片轮播展示
├── seller/                  # 卖家中心
│   ├── PublishItem.vue      # 发布商品（含图片上传、分类、成色）
│   ├── MyItems.vue          # 我的商品管理
│   └── components/
│       └── ItemEditModal.vue # 编辑商品弹窗
├── orders/                  # 订单中心
│   ├── index.vue            # 订单列表（买家/卖家双视角）
│   └── const.js             # 订单状态配置
├── Layout.vue               # 门户布局（顶部导航，无侧边栏）
├── Login.vue                # 保持不变
└── ForbiddenView.vue        # 保持不变
```

#### 关键交互

- 首页商品卡片 hover 放大效果、点击进入详情
- 搜索支持关键词+分类+价格区间
- 商品详情页：图片轮播 → 商品信息 → 卖家信息 → "我想要" 下单按钮
- 发布商品：图片拖拽上传 + 分类下拉 + 成色选择 + 价格输入
- 订单列表：Tab 切换（全部/待付款/待发货/已完成）

### 三、Ops 运营后台优化

#### 页面架构重构

每个业务页面采用统一架构：index.vue + components/ + const.js

- reviews/ → 带 Tab 状态筛选的审核列表 + RejectModal
- orders/ → 完整订单表格+筛选+分页 + OrderDetailModal
- dashboard/ → 真实统计卡片 + StatCard 组件
- vendor/ → 供方管理列表 + VendorDetailModal
- buyer/ → 需方管理列表 + BuyerDetailModal
- app-register/ → 重构自 AppRegisterView + AppFormModal
- portal-design/ → 重构自 PortalDesign
- role-manage/ → 重构自 RoleManage

#### Layout 样式优化

- 顶部导航保持暖橙渐变
- 侧边栏：白色背景，选中项橙色左边框 + 浅橙背景
- 内容区：#fafafa 背景
- 统一页面标题栏为 tab-bar 模式

### 四、后端接口配套

#### 数据库变更

- item 表增加字段：image_urls TEXT、category VARCHAR(32)、condition_level VARCHAR(16)、created_at DATETIME、updated_at DATETIME
- orders 表增加字段：created_at DATETIME

#### 新增/修改接口

- `GET /api/items` 扩展：增加 keyword/category/pageNo/pageSize 参数支持分页和搜索
- `POST /api/items` 扩展：增加 imageUrls/category/conditionLevel 字段
- `GET /api/items/{id}` 扩展：返回图片、分类、成色
- `GET /api/ops/statistics` — 运营统计数据聚合
- `GET /api/ops/orders` — 订单分页查询（支持 status/keyword/pageNo/pageSize）
- `GET /api/ops/vendors` — 供方列表查询
- `GET /api/ops/buyers` — 需方列表查询
- `GET /api/ops/reviews` 扩展 — 增加 status 筛选和分页参数

## 实施注意事项

- http.js 改造必须保持向后兼容，现有 `import { http } from "commonprovide/http"` 调用不能中断
- mf-shared 新增 exposes 后需同步更新 vite.config.js 的 exposes 配置
- portal Layout 彻底重做，从后台侧边栏模式改为顶部导航集市模式
- ops 路由新增 vendor/buyer 后需同步更新 Layout.vue 的 menuConfig
- 后端新增接口遵循现有 Controller → Service → Repository 三层架构
- item 表新增字段需编写 ALTER TABLE 迁移 SQL，保持与现有数据兼容
- portal 和 ops 作为两个独立 qiankun 子应用，通过 shell 主应用调度，互不影响

## 目录结构

```
packages/apps/mf-shared/src/exposes/
├── http.js                          # [MODIFY] 增强请求封装，添加 createHttp 工厂函数
├── hooks/                           # [NEW] 通用 Hooks
│   └── useSearchTable.js            # [NEW] 分页搜索 Hook
├── SearchFilter/                    # [NEW] 筛选组件
│   ├── SearchFilter.vue             # [NEW] 筛选栏组件
│   └── index.js                     # [NEW] 导出入口
├── ItemCard/                        # [NEW] 商品卡片组件（门户/运营复用）
│   ├── ItemCard.vue                 # [NEW] 商品卡片
│   └── index.js                     # [NEW] 导出入口
├── ImageGallery/                    # [NEW] 图片展示组件
│   ├── ImageGallery.vue             # [NEW] 图片轮播
│   └── index.js                     # [NEW] 导出入口
└── ...                              # 现有模块不变

packages/apps/portal/src/
├── style.css                        # [MODIFY] 门户主题变量
├── services/
│   └── api.js                       # [MODIFY] 新增分类/搜索/分页/收藏等 API
├── router/
│   └── index.js                     # [MODIFY] 新路由结构
├── views/
│   ├── Layout.vue                   # [MODIFY→重写] 顶部导航集市布局
│   ├── home/                        # [NEW] 首页
│   │   ├── index.vue                # [NEW] 瀑布流+搜索+分类+Banner
│   │   ├── const.js                 # [NEW] 分类常量
│   │   └── components/
│   │       ├── Banner.vue           # [NEW] 轮播 Banner
│   │       ├── CategoryBar.vue      # [NEW] 分类导航
│   │       └── ItemGrid.vue         # [NEW] 商品卡片网格
│   ├── item/                        # [NEW] 商品详情
│   │   ├── index.vue                # [NEW] 大图+详情+下单
│   │   └── components/
│   │       └── ImageGallery.vue     # [NEW] 本地图片轮播（或复用共享）
│   ├── seller/                      # [NEW] 卖家中心
│   │   ├── PublishItem.vue          # [NEW] 发布商品（含图片上传）
│   │   ├── MyItems.vue              # [NEW] 我的商品管理
│   │   └── components/
│   │       └── ItemEditModal.vue    # [NEW] 编辑弹窗
│   ├── orders/                      # [NEW] 订单中心
│   │   ├── index.vue                # [NEW] 订单列表
│   │   └── const.js                 # [NEW] 状态常量
│   ├── Login.vue                    # [KEEP]
│   ├── ForbiddenView.vue            # [KEEP]
│   ├── BuyerHome.vue                # [DELETE] → home/index.vue
│   ├── ItemList.vue                 # [DELETE] → home/index.vue
│   ├── ItemDetail.vue               # [DELETE] → item/index.vue
│   ├── SellerCenter.vue             # [DELETE] → seller/ 目录
│   └── MyOrders.vue                 # [DELETE] → orders/index.vue

packages/apps/ops/src/
├── style.css                        # [MODIFY] 添加 CSS 变量主题系统
├── services/
│   └── api.js                       # [MODIFY] 新增统计/供方/需方/订单分页 API
├── router/
│   └── index.js                     # [MODIFY] 新增 vendor/buyer 路由，改懒加载
├── views/
│   ├── Layout.vue                   # [MODIFY] 优化样式、更新 menuConfig
│   ├── dashboard/                   # [NEW] 仪表盘
│   │   ├── index.vue                # [NEW]
│   │   ├── const.js                 # [NEW]
│   │   └── components/
│   │       └── StatCard.vue         # [NEW]
│   ├── reviews/                     # [NEW] 审核管理
│   │   ├── index.vue                # [NEW]
│   │   ├── const.js                 # [NEW]
│   │   └── components/
│   │       └── RejectModal.vue      # [NEW]
│   ├── orders/                      # [NEW] 订单监控
│   │   ├── index.vue                # [NEW]
│   │   ├── const.js                 # [NEW]
│   │   └── components/
│   │       └── OrderDetailModal.vue # [NEW]
│   ├── vendor/                      # [NEW] 供方管理
│   │   ├── index.vue                # [NEW]
│   │   ├── const.js                 # [NEW]
│   │   └── components/
│   │       └── VendorDetailModal.vue# [NEW]
│   ├── buyer/                       # [NEW] 需方管理
│   │   ├── index.vue                # [NEW]
│   │   ├── const.js                 # [NEW]
│   │   └── components/
│   │       └── BuyerDetailModal.vue # [NEW]
│   ├── app-register/                # [NEW] 重构自 AppRegisterView
│   ├── portal-design/               # [NEW] 重构自 PortalDesign
│   ├── role-manage/                 # [NEW] 重构自 RoleManage
│   ├── Login.vue                    # [KEEP]
│   ├── ForbiddenView.vue            # [KEEP]
│   └── [旧文件 DELETE]             # ReviewList/OrderMonitor/OpsDashboard/AppRegisterView/PortalDesign/PortalDesigner/RoleManage

backend/src/main/java/com/campus/marketplace/
├── controller/
│   ├── ItemController.java          # [MODIFY] 扩展分页/搜索/分类
│   ├── OrderController.java         # [MODIFY] 增加分页查询
│   ├── OpsController.java           # [NEW] 统计/供方/需方查询
│   └── OpsReviewController.java     # [MODIFY] 增加状态筛选和分页
├── service/
│   ├── ItemService.java             # [MODIFY] 扩展查询方法
│   ├── OrderService.java            # [MODIFY] 增加分页
│   ├── OpsService.java              # [NEW] 统计/供方/需方逻辑
│   └── ReviewService.java           # [MODIFY] 增加筛选
├── dto/
│   ├── PageResult.java              # [NEW] 通用分页结果
│   ├── OpsStatisticsDTO.java        # [NEW]
│   ├── VendorDTO.java               # [NEW]
│   └── BuyerDTO.java                # [NEW]
└── resources/
    └── schema.sql                   # [MODIFY] item 表增加字段
```

## 架构设计

```mermaid
graph TD
    Shell[shell 主应用<br/>qiankun 调度] --> Portal[portal 门户子应用<br/>集市风格]
    Shell --> Ops[ops 运营后台子应用<br/>管理风格]
    
    Portal --> MF[mf-shared 共享层]
    Ops --> MF
    
    MF --> HTTP[http.js 增强请求]
    MF --> Hook[useSearchTable Hook]
    MF --> SF[SearchFilter 筛选]
    MF --> IC[ItemCard 商品卡片]
    MF --> IG[ImageGallery 图片]
    MF --> Existing[现有: auth-sdk/Login/StatusTag/PageHeader/...]
    
    Portal --> PH[首页 瀑布流]
    Portal --> PD[商品详情 大图+下单]
    Portal --> PS[卖家中心 发布+管理]
    Portal --> PO[订单中心 买卖双视角]
    
    Ops --> OD[Dashboard 统计]
    Ops --> OR[审核管理 Tab筛选]
    Ops --> OO[订单监控 分页]
    Ops --> OV[供方管理]
    Ops --> OB[需方管理]
    Ops --> OAR[子应用管理]
    Ops --> OPD[门户设计]
    Ops --> ORM[角色管理]
    
    Portal --> BE[Spring Boot 后端]
    Ops --> BE
    BE --> API1[/api/items 分页+搜索]
    BE --> API2[/api/ops/statistics]
    BE --> API3[/api/ops/orders]
    BE --> API4[/api/ops/vendors]
    BE --> API5[/api/ops/buyers]
```

## 设计理念

Portal 门户与 Ops 运营后台采用**截然不同**的设计风格，视觉上明确区分用户前台和管理员后台。

### Portal 门户 — 校园集市风格

参考闲鱼/转转等校园二手交易平台，打造轻快、活泼、面向用户的集市体验：

- **顶部导航栏**：白色背景，左侧 Logo "校园集市" + 搜索框居中 + 右侧导航链接（首页/我要卖/我的订单）+ 用户头像下拉
- **首页**：全宽卡片网格布局（3-4列），商品卡片含图片、标题、价格、成色标签、卖家头像；顶部 Banner 轮播 + 分类快捷入口
- **商品详情页**：大图轮播区 + 右侧信息区（标题/价格/成色/描述/卖家卡片/"我想要"下单按钮）
- **卖家发布页**：图片拖拽上传区 + 分类下拉 + 成色选择 + 表单
- **色彩**：主色青绿 #0fc6c2 活力感、辅助橙 #f0a838、背景 #f5f5f5 浅灰、卡片 #fff 白色
- **字体**：PingFang SC，标题 20px/700、副标题 16px/600、正文 14px/400
- **交互**：卡片 hover 轻微上浮 + 阴影、搜索实时反馈、下单按钮渐变动画

### Ops 运营后台 — 企业管理风格

保持后台管理系统风格，以暖橙色调为运营中心主色，专业清晰：

- **顶部导航栏**：48px 高，暖橙渐变背景（#d88c1f → #f0a838），白色文字
- **左侧菜单栏**：220px 宽，白色背景，选中项橙色左边框 + 浅橙背景
- **内容区**：#fafafa 浅灰背景
- **页面通用结构**：标题栏（48px 加粗标题 + 操作按钮）+ 筛选区 + 数据表格 + 操作弹窗
- **Dashboard**：统计卡片行（图标 + 数字 + 趋势）
- **色彩**：主色橙 #d88c1f/#f0a838、供方蓝 #336ad8、需方绿 #0fc6c2、背景 #fafafa、卡片 #fff

## Skills

- **Impeccable（前端设计工具集）**
- Purpose: 设计门户首页商品卡片瀑布流布局、详情页大图交互、卖家发布页图片上传等面向用户的高品质 UI
- Expected outcome: Portal 门户页面视觉达到闲鱼/转转级别的集市风格体验

- **前端开发**
- Purpose: 实现 useSearchTable Hook、SearchFilter 组件、门户瀑布流布局、图片上传组件等前端核心功能
- Expected outcome: 通用组件和 Hook 可复用、门户瀑布流和图片上传交互流畅

- **全栈开发**
- Purpose: 指导后端 Spring Boot 接口实现，确保 Controller→Service→Repository 分层正确、分页查询模式统一
- Expected outcome: 后端新增接口遵循三层架构，分页/筛选/错误处理规范统一

## SubAgent

- **code-explorer**
- Purpose: 在实现过程中快速定位现有代码模式、共享组件用法、路由配置等
- Expected outcome: 准确找到代码位置，避免重复造轮子