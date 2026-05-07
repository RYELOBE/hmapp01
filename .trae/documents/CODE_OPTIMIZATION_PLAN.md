# 校园二手交易平台代码优化与功能测试计划

## 一、问题分析

### 1.1 组件目录问题
- **重复文件**：`components/` 根目录下存在与 `components/data/` 重复的文件：
  - `AddressCard.vue`
  - `AiAssistant.vue`
  - `MiniCart.vue`
  - `OrderCard.vue`
  - `ProductCard.vue`
- **shared-components 冗余**：`shared-components/` 目录与 `components/` 目录高度重复
- **ops-components 缺失**：该目录不存在

### 1.2 服务目录问题
- **缺少子模块**：`services/` 目录缺少 `items/`, `ops/`, `orders/`, `users/`, `auth/` 子目录
- **api.js 过于庞大**：所有API函数集中在一个文件中，维护困难
- **ops-services 重复**：`ops-services/api.js` 与 `services/api.js` 存在重复的运营端API

### 1.3 样式文件问题
- **重复定义**：`style.css` 和 `styles/main.scss` 存在大量重复定义：
  - 全局重置样式
  - 滚动条美化
  - 选择文本样式
  - 全局链接样式

## 二、优化计划

### 2.1 组件目录优化

| 任务 | 描述 | 优先级 |
|------|------|--------|
| C1 | 删除 `components/` 根目录下重复文件（保留 `components/data/` 版本） | 高 |
| C2 | 合并 `shared-components/` 到 `components/`，删除重复内容 | 高 |
| C3 | 创建 `ops-components/` 目录，整理运营端专用组件 | 中 |

### 2.2 服务目录模块化

| 任务 | 描述 | 优先级 |
|------|------|--------|
| S1 | 创建 `services/items/` 子目录，拆分商品相关API | 高 |
| S2 | 创建 `services/orders/` 子目录，拆分订单相关API | 高 |
| S3 | 创建 `services/users/` 子目录，拆分用户相关API | 高 |
| S4 | 创建 `services/ops/` 子目录，拆分运营端API | 高 |
| S5 | 创建 `services/auth/` 子目录，拆分认证相关API | 高 |
| S6 | 更新 `services/api.js` 作为统一出口 | 高 |
| S7 | 删除 `ops-services/` 目录（已整合到 `services/ops/`） | 中 |

### 2.3 样式文件整合

| 任务 | 描述 | 优先级 |
|------|------|--------|
| ST1 | 清理 `style.css`，保留必要的全局样式 | 高 |
| ST2 | 确保 `styles/main.scss` 作为主样式入口 | 高 |

### 2.4 功能测试

| 任务 | 描述 | 优先级 |
|------|------|--------|
| T1 | 前端组件功能测试 | 高 |
| T2 | 页面数据渲染验证 | 高 |
| T3 | 角色区分测试（买家/卖家/运营） | 高 |
| T4 | 全流程业务测试 | 高 |

## 三、执行步骤

### 阶段一：组件目录清理（预计 30 分钟）
1. 删除 `components/` 根目录下重复文件
2. 检查并合并 `shared-components/` 到 `components/`

### 阶段二：服务模块化重构（预计 60 分钟）
1. 创建各子目录结构
2. 拆分 `api.js` 到各子模块
3. 更新导出入口

### 阶段三：样式整合（预计 20 分钟）
1. 分析重复内容
2. 清理 `style.css`

### 阶段四：功能测试（预计 60 分钟）
1. 组件功能验证
2. 页面渲染测试
3. 角色权限验证

## 四、风险与依赖

### 4.1 风险评估
| 风险 | 描述 | 应对措施 |
|------|------|----------|
| 模块拆分后引用路径错误 | 可能导致编译失败 | 全局搜索并更新引用 |
| 样式冲突 | 删除 `style.css` 可能影响页面 | 逐步删除，测试验证 |
| 组件重复删除遗漏 | 可能导致运行时错误 | 删除前确认引用 |

### 4.2 依赖关系
- 所有视图文件依赖 `services/api.js`
- 所有组件依赖 `components/index.js`
- 样式文件通过 `main.js` 引入

## 五、交付物

1. 优化后的组件目录结构
2. 模块化的服务目录结构
3. 整合后的样式文件
4. 测试验证报告