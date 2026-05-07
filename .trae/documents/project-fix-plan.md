# 项目全面修复计划

## 问题概述
根据用户反馈和代码检查，发现以下主要问题需要解决：

1. **Token存储混乱** - localStorage中有多个token相关key
2. **路由结构问题** - 根路径"/"使用旧布局
3. **首页升级问题** - 用户看不到升级后的首页
4. **导航栏问题** - 存在两套Header实现
5. **服务文件重复** - auth.js和auth/index.js重复
6. **运营后台显示问题**
7. **图标问题** - 需要验证所有图标正确
8. **组件重复** - StatsCard等组件有重复版本

---

## 详细修复计划

### 1. 路由结构优化
**问题**：根路径"/"使用旧的container/Layout.vue，新首页在/portal/home
**方案**：
- 修改根路径"/"直接重定向到/portal/home
- 保留运营后台的/ops路由结构
- 更新router/index.js

### 2. 统一导航栏实现
**问题**：存在两套Header实现
**方案**：
- 保留views/portal/Layout.vue中的内联Header（功能更完整）
- 清理components/layout/Header/Header.vue（避免混淆）
- 确保所有门户页面使用一致的导航

### 3. Token存储清理与统一
**问题**：localStorage中有campus_market_token、token、mock_token等多个key
**方案**：
- 确保只使用STORAGE_KEYS.TOKEN（来自@campus/common）
- 清理auth store中的冗余代码
- 统一token读取和写入逻辑

### 4. 重复文件清理
**问题**：services/auth.js和services/auth/index.js重复
**方案**：
- 保留services/auth/index.js（模块化结构）
- 删除services/auth.js
- 更新所有引用路径
- 同理处理其他重复文件（如StatsCard）

### 5. 运营后台问题修复
**问题**：运营后台显示异常
**方案**：
- 检查ops路由配置
- 验证ops布局组件
- 确保菜单导航正常

### 6. 图标全面检查与修复
**方案**：
- 遍历所有Vue文件
- 验证所有从@arco-design/web-vue/es/icon导入的图标都存在
- 使用正确的图标名称

### 7. 首页升级确认
**方案**：
- 确保/portal/home显示的是升级后的页面
- 验证Hero区域、分类、商品列表等功能正常

---

## 执行步骤

### 第一步：路由修复
1. 修改router/index.js，根路径"/"重定向到/portal/home
2. 优化路由守卫逻辑
3. 测试路由跳转

### 第二步：清理重复文件
1. 删除services/auth.js
2. 删除重复的StatsCard组件
3. 更新所有引用路径

### 第三步：Token逻辑优化
1. 简化auth store
2. 确保token读取/写入统一
3. 测试登录/登出流程

### 第四步：导航栏统一
1. 确认使用views/portal/Layout.vue中的Header
2. 清理components/layout/Header/Header.vue
3. 测试导航功能

### 第五步：运营后台修复
1. 检查ops路由
2. 验证ops布局
3. 测试运营功能

### 第六步：图标检查
1. 扫描所有Vue文件中的图标使用
2. 修复不存在的图标引用

### 第七步：构建测试
1. 运行构建，确保无错误
2. 启动开发服务器，全面测试

---

## 文件修改清单

### 需要修改的文件
- `packages/apps/campus-app/src/router/index.js` - 路由重定向
- `packages/apps/campus-app/src/stores/auth.js` - 简化逻辑
- `packages/apps/campus-app/src/views/portal/Layout.vue` - 确认使用
- `packages/apps/campus-app/src/views/ops/dashboard/index.vue` - 检查图标
- 其他文件...

### 需要删除的文件
- `packages/apps/campus-app/src/services/auth.js`
- `packages/apps/campus-app/src/components/layout/Header/Header.vue`（或保留作为备用）
- `packages/apps/campus-app/src/components/data/StatsCard.vue`（保留子目录版本）

### 需要检查的文件
- 所有使用图标的Vue文件
- 所有引用services/auth的文件
