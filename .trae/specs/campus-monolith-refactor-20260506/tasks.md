# 校园交易平台 - 微前端转单体架构重构 - 实现计划

## [ ] Task 1: 创建单体应用基础结构
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 packages/apps 下创建新的单体应用目录 `campus-app`
  - 创建基础文件结构（src, index.html, package.json, vite.config.js）
  - 配置基础依赖和路径别名
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: 目录结构正确创建
  - `programmatic` TR-1.2: vite.config.js 配置正确，包含路径别名
- **Notes**: 参考现有的 shell 应用结构

## [ ] Task 2: 合并路由配置
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 创建统一的路由配置文件
  - 合并 shell、portal、ops 的路由
  - 统一路由守卫和权限验证逻辑
- **Acceptance Criteria Addressed**: AC-1, AC-4
- **Test Requirements**:
  - `programmatic` TR-2.1: 所有原有路由路径可访问
  - `human-judgment` TR-2.2: 路由结构清晰，权限验证统一
- **Notes**: 保留原有路径结构 /portal/* 和 /ops/*

## [ ] Task 3: 整合状态管理
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 整合 authStore 和 framePinia
  - 移除微前端相关的状态管理逻辑
  - 创建统一的 Pinia store
- **Acceptance Criteria Addressed**: AC-1, AC-2
- **Test Requirements**:
  - `programmatic` TR-3.1: 登录状态正确管理
  - `human-judgment` TR-3.2: store 结构清晰，无冗余代码
- **Notes**: 保留 authStore 的核心功能，移除 framePinia 中的微前端逻辑

## [ ] Task 4: 迁移组件和页面
- **Priority**: P0
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 迁移 shell 的组件和页面
  - 迁移 portal 的组件和页面
  - 迁移 ops 的组件和页面
  - 整合 mf-shared 中的共享组件
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgment` TR-4.1: 组件目录结构清晰
  - `human-judgment` TR-4.2: 所有页面组件正确迁移
- **Notes**: 注意组件命名冲突和路径引用更新

## [ ] Task 5: 更新配置文件
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 更新根目录 package.json，移除微前端相关依赖
  - 更新 vite.config.js，移除 qiankun 和 federation 插件
  - 更新主入口文件 main.js
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-5.1: package.json 无 qiankun 和 vite-plugin-federation
  - `programmatic` TR-5.2: 构建成功
- **Notes**: 需要更新多个 vite.config.js 文件

## [ ] Task 6: 移除微前端相关代码
- **Priority**: P1
- **Depends On**: Task 1-5
- **Description**: 
  - 移除 shell 中的 minFrame 目录
  - 移除所有 qiankun 生命周期钩子
  - 移除 Module Federation 相关配置
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-6.1: grep 'qiankun' 返回空
  - `programmatic` TR-6.2: grep 'federation' 返回空
- **Notes**: 仔细检查所有文件中的微前端相关代码

## [ ] Task 7: Bug 排查和修复
- **Priority**: P1
- **Depends On**: Task 1-6
- **Description**: 
  - 审查代码，识别潜在 bug
  - 修复已发现的问题
  - 运行测试验证修复
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - `programmatic` TR-7.1: 测试用例通过
  - `human-judgment` TR-7.2: 代码审查无明显问题
- **Notes**: 重点关注路由、状态管理和组件通信

## [ ] Task 8: 构建和测试验证
- **Priority**: P0
- **Depends On**: Task 1-7
- **Description**: 
  - 运行 pnpm build 验证构建
  - 启动开发服务器验证功能
  - 检查所有页面是否正常渲染
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-5
- **Test Requirements**:
  - `programmatic` TR-8.1: 构建成功无错误
  - `programmatic` TR-8.2: 开发服务器正常启动
  - `human-judgment` TR-8.3: 所有页面可正常访问
- **Notes**: 需要验证所有关键路径