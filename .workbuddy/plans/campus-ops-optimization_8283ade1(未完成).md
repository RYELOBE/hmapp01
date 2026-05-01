---
name: campus-ops-optimization
overview: 参考 CommonEngine-web/dtp_web/operation-portal 优化 ops 子应用的请求封装、页面架构、样式和业务逻辑
design:
  architecture:
    framework: vue
    component: tdesign
  styleKeywords:
    - Arco Design
    - Enterprise Admin
    - Warm Orange
    - Clean Layout
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
      - "#D88C1F"
      - "#F0A838"
      - "#336AD8"
      - "#0FC6C2"
    background:
      - "#FAFAFA"
      - "#FFFFFF"
      - "#FFF5E6"
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
    content: 增强 mf-shared http.js 请求封装（getHttp 工厂函数、网络检测、节流提示、isHideErrorMsg、CancelToken、状态码映射）
    status: pending
  - id: shared-hooks-components
    content: 新增 useSearchTable Hook 和 SearchFilter 筛选组件到 mf-shared 并配置 exposes
    status: pending
    dependencies:
      - http-upgrade
  - id: ops-theme-style
    content: 建立 ops CSS 变量主题系统，优化 Layout.vue 样式
    status: pending
  - id: ops-page-restructure
    content: 重构现有页面为 index.vue+components/+const.js 架构（reviews/orders/app-register/portal-design/role-manage）
    status: pending
    dependencies:
      - shared-hooks-components
      - ops-theme-style
  - id: ops-new-pages
    content: 新增 dashboard（真实统计）、vendor（供方管理）、buyer（需方管理）页面，更新路由和菜单
    status: pending
    dependencies:
      - shared-hooks-components
      - ops-theme-style
  - id: backend-api
    content: 实现后端统计/供方/需方/订单分页接口（Controller+Service+DTO）
    status: pending
---

## 产品概述

校园二手交易平台前端优化与业务扩展，覆盖请求封装升级、页面架构重构、样式体系提升、业务逻辑完善四大维度，建立供方（卖方）、需方（买方）、运营三方完整的管理体系。

## 核心功能

- **请求封装升级**：参考 CommonEngine-web baseRequest.js，增强 http.js 支持网络检测、节流错误提示、isHideErrorMsg/isTokenNotAuth 按请求控制、CancelToken 取消请求、完整 HTTP 状态码映射、请求/响应钩子回调
- **通用 Hook 与组件**：实现 useSearchTable 分页搜索 Hook、SearchFilter 筛选组件，在 mf-shared 中共享
- **页面架构重构**：ops 子应用各页面采用 index.vue + components/ + const.js 架构，分离常量、抽取子组件
- **样式体系提升**：统一 CSS 变量主题系统，优化 ops Layout 和各页面视觉品质
- **业务逻辑完善**：OpsDashboard 接入真实统计接口、OrderMonitor 实现完整订单监控、新增供方管理和需方管理页面、审核流程增加状态 Tab 筛选
- **后端接口配套**：新增统计聚合接口、供方/需方管理接口、订单分页查询接口

## 技术栈

- 前端框架：Vue3 + JavaScript + Arco Design + Vite + Pinia + Vue Router
- 微前端：qiankun + Module Federation (mf-shared)
- 样式：SCSS + CSS 变量主题系统
- 后端：Spring Boot + Sa-Token + MySQL
- 包管理：pnpm workspace monorepo

## 实现方案

### 1. 请求封装升级（参考 CommonEngine-web baseRequest.js）

**策略**：重构 mf-shared/src/exposes/http.js，引入 getHttp 工厂函数模式

**关键决策**：

- 保持 `http` 实例和 `get/post/put/_delete` 导出兼容，不破坏现有调用
- 新增 `createHttp(options)` 工厂函数，支持 `isGlobalShowErrorMsg`、`gotoLoginCallback`、`requestCallback`、`responseCallback` 四个钩子
- 请求拦截增加：网络断开检测（`navigator.onLine`）、GET 请求防缓存时间戳、`isHideErrorMsg`/`isTokenNotAuth` 按请求级别控制
- 响应拦截增加：节流错误提示（throttle 300ms，避免批量报错刷屏）、完整 HTTP 状态码映射（400/401/403/404/408/500/501/502/503/504/505）、`isTokenNotAuth` 白名单跳过 token 校验
- 导出 `CancelToken` 支持请求取消
- 401 处理改为调用 `gotoLoginCallback` 钩子（默认跳转登录），而非硬编码 `window.location.href`

### 2. 通用 Hook 与组件

**useSearchTable Hook**（参考 dtp_web commonmodule/hooks/search-table.js）：

- 封装 searchForm、tableData、loading、pagination、selectedKeys 状态
- 提供 pageChange、pageSizeChange、sorterChange、search、reset 方法
- 接受 getData 函数和 config 配置作为参数
- 放置在 `mf-shared/src/exposes/hooks/` 下，通过 MF 暴露给各子应用

**SearchFilter 组件**（参考 dtp_web SearchCollapsable）：

- 简化版筛选栏组件，支持 v-model 绑定搜索表单
- 提供 search/reset 事件
- 放置在 `mf-shared/src/exposes/SearchFilter/` 下

### 3. 页面架构重构

每个业务页面采用统一架构：

```
views/
  SomePage/
    index.vue          # 主页面
    components/        # 子组件（Modal、Detail 等）
    const.js           # columns、options 等常量配置
```

**影响页面**：

- ReviewList → reviews/index.vue + components/RejectModal.vue + const.js
- OrderMonitor → orders/index.vue + components/OrderDetailModal.vue + const.js
- AppRegisterView → app-register/index.vue + components/AppFormModal.vue + const.js
- PortalDesign → portal-design/index.vue + components/ + const.js
- RoleManage → role-manage/index.vue + components/ + const.js

**新增页面**：

- 供方管理 → vendor/index.vue + components/ + const.js
- 需方管理 → buyer/index.vue + components/ + const.js

### 4. 样式体系提升

- 在 ops/src/style.css 中定义 CSS 变量主题系统（--ops-primary、--ops-bg、--ops-sider-bg 等）
- 优化 Layout.vue 内容区背景色、侧边栏样式
- 统一页面标题栏样式为 tab-bar 模式（参考 operation-portal）
- 各页面使用 PageHeader 组件（已有 mf-shared 暴露）统一页头

### 5. 业务逻辑完善

**OpsDashboard**：

- 后端新增 `/api/ops/statistics` 聚合接口，返回待审核数、今日通过数、今日驳回数、在线用户数、总商品数、总订单数
- 前端接入真实数据，卡片改为统计卡片 + 图标 + 趋势指示

**OrderMonitor**：

- 后端扩展 OrderController，增加分页查询接口 `/api/ops/orders`，支持状态筛选、时间范围、分页
- 前端实现完整订单表格 + 状态筛选 + 分页

**供方管理（VendorManage）**：

- 后端新增 `/api/ops/vendors` 接口，查询所有卖方用户及其商品统计
- 前端展示供方列表（名称、商品数、在售数、已售数、状态）

**需方管理（BuyerManage）**：

- 后端新增 `/api/ops/buyers` 接口，查询所有买方用户及其订单统计
- 前端展示需方列表（名称、订单数、已付款、已完成、状态）

**ReviewList 增强**：

- 增加 Tab 状态筛选（全部/待审核/已通过/已驳回）
- 驳回操作改为 Modal 弹窗（输入驳回原因）
- 分页支持

### 6. 后端接口配套

新增/修改的 Spring Boot 接口：

- `GET /api/ops/statistics` — 运营统计数据聚合
- `GET /api/ops/orders` — 订单分页查询（支持 status/keyword/pageNo/pageSize）
- `GET /api/ops/vendors` — 供方列表查询
- `GET /api/ops/buyers` — 需方列表查询
- 修改 `GET /api/ops/reviews` — 增加 status 筛选和分页参数

## 实施注意事项

- http.js 改造必须保持向后兼容，现有的 `import { http } from "commonprovide/http"` 调用不能中断
- mf-shared 新增 exposes 后需同步更新 vite.config.js 的 exposes 配置和 mf_config/configExposes.json
- ops 子应用路由新增 vendor/buyer 页面后，需同步更新 Layout.vue 的 menuConfig
- 后端新增接口需遵循现有 Controller 分层模式（Controller → Service → Repository）

## 目录结构

```
packages/apps/mf-shared/
├── src/exposes/
│   ├── http.js                    # [MODIFY] 增强请求封装，添加 getHttp 工厂函数
│   ├── hooks/                     # [NEW] 通用 Hooks 目录
│   │   └── useSearchTable.js      # [NEW] 分页搜索 Hook
│   ├── SearchFilter/              # [NEW] 筛选组件
│   │   ├── SearchFilter.vue       # [NEW] 筛选栏组件
│   │   └── index.js               # [NEW] 导出入口
│   └── ...                        # 现有模块不变
├── vite.config.js                 # [MODIFY] 新增 exposes 配置
└── mf_config/configExposes.json   # [MODIFY] 同步新增 exposes

packages/apps/ops/
├── src/
│   ├── style.css                  # [MODIFY] 添加 CSS 变量主题系统
│   ├── services/
│   │   └── api.js                 # [MODIFY] 新增统计/供方/需方/订单分页 API
│   ├── router/
│   │   └── index.js               # [MODIFY] 新增 vendor/buyer 路由
│   ├── views/
│   │   ├── Layout.vue             # [MODIFY] 优化样式、更新 menuConfig
│   │   ├── dashboard/             # [NEW] Dashboard 页面目录
│   │   │   ├── index.vue          # [NEW] 接入真实统计数据的仪表盘
│   │   │   ├── const.js           # [NEW] 统计卡片配置
│   │   │   └── components/        # [NEW] 子组件
│   │   │       └── StatCard.vue   # [NEW] 统计卡片组件
│   │   ├── reviews/               # [NEW] 审核管理页面目录（重构自 ReviewList）
│   │   │   ├── index.vue          # [NEW] 带 Tab 筛选的审核列表
│   │   │   ├── const.js           # [NEW] columns/statusOptions 配置
│   │   │   └── components/
│   │   │       └── RejectModal.vue # [NEW] 驳回原因弹窗
│   │   ├── orders/                # [NEW] 订单监控页面目录（重构自 OrderMonitor）
│   │   │   ├── index.vue          # [NEW] 完整订单表格+筛选+分页
│   │   │   ├── const.js           # [NEW] columns/statusOptions 配置
│   │   │   └── components/
│   │   │       └── OrderDetailModal.vue # [NEW] 订单详情弹窗
│   │   ├── vendor/                # [NEW] 供方管理页面
│   │   │   ├── index.vue          # [NEW] 供方列表+统计
│   │   │   ├── const.js           # [NEW] columns 配置
│   │   │   └── components/
│   │   │       └── VendorDetailModal.vue # [NEW] 供方详情弹窗
│   │   ├── buyer/                 # [NEW] 需方管理页面
│   │   │   ├── index.vue          # [NEW] 需方列表+统计
│   │   │   ├── const.js           # [NEW] columns 配置
│   │   │   └── components/
│   │   │       └── BuyerDetailModal.vue  # [NEW] 需方详情弹窗
│   │   ├── app-register/          # [NEW] 重构自 AppRegisterView
│   │   │   ├── index.vue          # [NEW] 主页面
│   │   │   ├── const.js           # [NEW] 常量
│   │   │   └── components/
│   │   │       └── AppFormModal.vue # [NEW] 表单弹窗子组件
│   │   ├── portal-design/         # [NEW] 重构自 PortalDesign
│   │   │   ├── index.vue          # [NEW] 主页面
│   │   │   └── components/
│   │   │       └── PortalDesigner.vue # [MOVED] 设计器子组件
│   │   ├── role-manage/           # [NEW] 重构自 RoleManage
│   │   │   ├── index.vue          # [NEW] 主页面
│   │   │   └── const.js           # [NEW] 常量
│   │   ├── Login.vue              # [KEEP] 保持不变
│   │   ├── ForbiddenView.vue      # [KEEP] 保持不变
│   │   ├── ReviewDetail.vue       # [KEEP] 暂保留，后续可并入 reviews/components/
│   │   ├── ReviewList.vue         # [DELETE] 迁移至 reviews/index.vue
│   │   ├── OrderMonitor.vue       # [DELETE] 迁移至 orders/index.vue
│   │   ├── OpsDashboard.vue       # [DELETE] 迁移至 dashboard/index.vue
│   │   ├── AppRegisterView.vue    # [DELETE] 迁移至 app-register/index.vue
│   │   ├── PortalDesign.vue       # [DELETE] 迁移至 portal-design/index.vue
│   │   ├── PortalDesigner.vue     # [DELETE] 迁移至 portal-design/components/
│   │   └── RoleManage.vue         # [DELETE] 迁移至 role-manage/index.vue
│   └── main.js                    # [MODIFY] 无需改动（路由懒加载后自动生效）

backend/src/main/java/com/campus/marketplace/
├── controller/
│   ├── OpsController.java         # [NEW] 运营统计+供方+需方查询接口
│   ├── OrderController.java       # [MODIFY] 增加分页查询接口
│   └── OpsReviewController.java   # [MODIFY] 增加状态筛选和分页
├── service/
│   ├── OpsService.java            # [NEW] 运营统计、供方/需方查询业务逻辑
│   ├── OrderService.java          # [MODIFY] 增加分页查询方法
│   └── ReviewService.java         # [MODIFY] 增加状态筛选方法
├── dto/
│   ├── OpsStatisticsDTO.java      # [NEW] 统计数据传输对象
│   ├── VendorDTO.java            # [NEW] 供方数据传输对象
│   ├── BuyerDTO.java             # [NEW] 需方数据传输对象
│   └── PageResult.java           # [NEW] 通用分页结果对象
└── mapper/ (如使用 MyBatis)
    └── ...                        # 对应的 Mapper 接口
```

## 架构设计

```mermaid
graph TD
    A[ops 子应用] --> B[mf-shared]
    B --> B1[http.js 增强请求封装]
    B --> B2[useSearchTable Hook]
    B --> B3[SearchFilter 组件]
    B --> B4[现有: auth-sdk / Login / StatusTag / PageHeader / PageContainer]
    
    A --> C[页面层]
    C --> C1[dashboard 仪表盘]
    C --> C2[reviews 审核管理]
    C --> C3[orders 订单监控]
    C --> C4[vendor 供方管理]
    C --> C5[buyer 需方管理]
    C --> C6[app-register 子应用管理]
    C --> C7[portal-design 门户设计]
    C --> C8[role-manage 角色管理]
    
    A --> D[Spring Boot 后端]
    D --> D1[/api/ops/statistics]
    D --> D2[/api/ops/reviews 分页+筛选]
    D --> D3[/api/ops/orders 分页+筛选]
    D --> D4[/api/ops/vendors]
    D --> D5[/api/ops/buyers]
```

## 设计风格

采用 Arco Design 企业级后台管理风格，以暖橙色调为运营中心主色，配合统一 CSS 变量主题系统，打造专业、清晰的运营管理界面。

## 设计内容

### 通用布局

- 顶部导航栏：48px 高，暖橙渐变背景（#d88c1f → #f0a838），白色文字
- 左侧菜单栏：220px 宽，白色背景，选中项橙色左边框 + 浅橙背景
- 内容区：浅灰白背景（#fafafa），统一 padding: 20px

### 页面通用结构（每个业务页）

- 标题栏：48px 高，左侧 20px 加粗标题 + 右侧操作按钮，底部 1px 边框分割
- 筛选区：SearchFilter 组件，支持关键词+下拉+日期范围筛选
- 数据表格：Arco a-table，统一分页器样式
- 操作弹窗：Arco a-modal，统一 560px 宽度

### Dashboard 仪表盘

- 顶部统计卡片行：3-4 个 StatCard，每个带图标、数字、趋势标签
- 下方可扩展区域：预留图表位置

### 色彩体系

- 运营主色：橙 #d88c1f / #f0a838
- 供方色：蓝 #336ad8 / #4a7feb
- 需方色：绿 #0fc6c2 / #2bd2b7
- 背景：#fafafa 内容区 / #fff 卡片
- 文字：#1d2129 主文字 / #4e5969 次文字 / #86909c 辅助文字

## Skills

- **Impeccable（前端设计工具集）**
- Purpose: 优化 ops 子应用的视觉设计品质，确保样式系统专业统一
- Expected outcome: 产出统一 CSS 变量主题、页面视觉规范、StatCard 等组件的高品质样式
- **全栈开发**
- Purpose: 指导后端 Spring Boot 接口实现，确保 Controller→Service→Repository 分层正确、分页查询模式统一
- Expected outcome: 后端新增接口遵循三层架构，分页/筛选/错误处理规范统一
- **前端开发**
- Purpose: 指导 useSearchTable Hook、SearchFilter 组件、页面组件化架构的实现
- Expected outcome: 通用 Hook 和组件可复用、页面架构清晰、代码质量高