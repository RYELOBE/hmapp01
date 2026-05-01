---
name: fix-local-qiankun-white-screen
overview: 定位并修复本地运行时 Vue3/Vite/Qiankun 微前端白屏问题，确保 shell、portal、ops、mf-shared 与 Spring Boot 后端能在本地协同启动。
todos:
  - id: explore-confirm
    content: 使用 [subagent:code-explorer] 复核白屏触发链路与关键报错
    status: completed
  - id: fix-local-entries
    content: 修正后端 data.sql 子应用本地入口端口
    status: completed
    dependencies:
      - explore-confirm
  - id: stabilize-remote-start
    content: 调整 commonprovide 与根 dev:front 启动顺序
    status: completed
    dependencies:
      - explore-confirm
  - id: align-routes
    content: 对齐 portal 与 ops 子应用路由匹配
    status: completed
    dependencies:
      - fix-local-entries
  - id: improve-error-state
    content: 使用 [skill:前端用户体验工程] 增强加载失败提示
    status: completed
    dependencies:
      - align-routes
  - id: verify-local-run
    content: 使用 [skill:全栈开发伴侣] 验证本地全栈启动
    status: completed
    dependencies:
      - stabilize-remote-start
      - align-routes
      - improve-error-state
---

## User Requirements

- 排查并修复本地运行时前端页面白屏问题。
- 保证主入口、门户页面、运营中心页面在本地能正常打开和跳转。
- 保证登录后按角色进入对应页面，菜单路径与实际页面一致。
- 仅处理本地开发运行链路，不涉及线上部署或新增业务功能。
- 修复后页面不应出现纯白屏；加载失败时需要有明确提示或可回退页面。

## Product Overview

这是一个校园交易平台本地开发项目，包含主入口、买卖双方门户、运营中心和后端接口。本次重点是让本地开发环境稳定启动，并让各入口页面可正常显示。

## Core Features

- 本地前后端启动链路可用。
- 主入口能加载门户和运营中心。
- 子页面菜单跳转路径正确。
- 登录后能进入对应角色页面。
- 微应用加载失败时显示可理解的错误反馈。

## Tech Stack Selection

- 前端沿用现有技术栈：pnpm workspace、Vue 3、Vite、Vue Router、Pinia、Arco Design、qiankun、vite-plugin-qiankun、@originjs/vite-plugin-federation。
- 后端沿用现有技术栈：Spring Boot 3.3.5、Java 17、Spring JDBC/JdbcTemplate、MySQL、Sa-Token。
- 本次不引入新框架、不重构架构，只修正本地运行配置、微前端注册入口、子应用路由匹配和白屏兜底。

## Implementation Approach

本次采用“小范围定点修复”的方式，优先解决最可能导致白屏的本地运行链路问题：后端返回的子应用入口端口、共享远程模块启动方式、子应用路由 base 与菜单路径不一致。整体保持现有 qiankun 主应用动态注册模式，由 shell 从后端获取子应用注册配置，再挂载 portal 和 ops。

关键技术决策：

1. **修正后端本地子应用入口端口**

- 已确认实际本地端口为 portal 7101、ops 7102，但 `backend/src/main/resources/data.sql` 目前写入 7107、7108。
- shell 会通过 `GET /api/frame/registers` 获取这些 entry，端口错误会导致 qiankun 加载子应用失败。
- 因 `application.yml` 中 `spring.sql.init.mode: always`，更新 `data.sql` 的 INSERT 和 UPDATE 能覆盖本地已有错误配置。

2. **稳定 commonprovide 远程模块启动**

- shell、portal、ops 都依赖 `http://localhost:7199/assets/remoteEntry.js`。
- 当前 `packages/apps/mf-shared/package.json` 的 `dev` 直接执行 preview；如果没有 dist，会导致 remoteEntry 404，进而前端白屏。
- 计划让根 `dev:front` 先 build commonprovide，再并发启动 commonprovide preview、shell、portal、ops，降低启动竞态和远程模块缺失风险。

3. **对齐子应用路由**

- portal 在 qiankun 下使用 `/portal` 作为 history base，后端菜单提供 `/portal/buyer/home`、`/portal/buyer/orders`、`/portal/seller/orders`，但 portal 内部缺少 `buyer/home`、`buyer/orders`、`seller/orders` 路由，需要补齐别名或等价路由。
- ops 当前在 qiankun 下使用 `/ops` 作为 history base，但路由本身也带 `ops/` 前缀，存在路径被剥离后不匹配或双前缀风险。优先通过调整 history base 或路由匹配策略解决，不批量改动所有页面跳转。

4. **补强白屏兜底**

- 保留现有 shell 的 qiankun 注册流程。
- 在 `packages/apps/shell/src/minFrame/index.js` 中增强微应用加载异常处理，使失败时跳转到已有错误页或展示明确提示。
- 复用现有 `ForbiddenView.vue` 或类似错误结果页，不新增复杂 UI。

## Implementation Notes

- 当前仓库已有大量未提交修改，实施时必须避免格式化整文件和无关重构。
- 优先修改配置和路由文件，不触碰业务服务、订单、商品、AI 等非白屏相关逻辑。
- commonprovide 的预构建会增加本地启动时间，但可避免远程模块 404；运行时没有额外复杂度。
- 路由别名或路由 base 修复为常量级匹配，不引入性能瓶颈。
- 错误日志仅记录微应用加载失败的必要信息，避免输出 token、用户信息、请求大对象。
- 验证时需覆盖未登录打开 shell、buyer 登录、seller 登录、ops 登录，以及直接访问子应用本地端口。

## Architecture Design

现有本地链路保持不变：

1. 开发者启动后端和前端。
2. shell 打开后请求后端 `/api/frame/registers` 获取子应用注册配置。
3. shell 使用 qiankun 注册 portal 和 ops。
4. shell、portal、ops 通过 Module Federation 加载 commonprovide 共享模块。
5. 登录后 shell 请求 `/api/frame/routes` 获取角色可访问菜单。
6. 菜单路径触发 qiankun activeRule，挂载对应子应用。

本次修复集中在三个连接点：

- 后端注册数据：保证 shell 拿到正确 entry。
- 共享 remote：保证 commonprovide 的 remoteEntry 在 shell 启动前可访问。
- 子应用路由：保证 shell 菜单路径进入子应用后能命中真实页面。

## Directory Structure

```text
c:/Users/Administrator/Documents/New project/
├── package.json
│   # [MODIFY] 根工作区脚本。调整 dev:front/dev:all，使 commonprovide 先 build，再启动 preview 与各微应用，避免 remoteEntry 缺失和启动竞态。
│
├── README.md
│   # [MODIFY] 更新本地启动说明、端口说明和白屏排查提示，明确应使用 dev:front 或 start-all.ps1。
│
├── scripts/
│   └── start-all.ps1
│       # [AFFECTED] 已调用 pnpm dev:front。确认根脚本调整后该一键启动脚本仍能启动前端和后端。
│
├── backend/
│   └── src/main/resources/
│       ├── application.yml
│       │   # [AFFECTED] 已配置 sql.init.mode=always，确认 data.sql 修复会在本地启动时生效。
│       └── data.sql
│           # [MODIFY] 将 app_register 中 portal/ops entry 从 7107/7108 修正为 7101/7102，并保持 UPDATE 覆盖已有本地数据。
│
└── packages/apps/
    ├── mf-shared/
    │   └── package.json
    │       # [MODIFY] 调整 commonprovide dev 脚本，确保 preview 前已生成 dist/assets/remoteEntry.js。
    │
    ├── portal/
    │   └── src/router/index.js
    │       # [MODIFY] 补齐 buyer/home、buyer/orders、seller/orders 等与后端菜单一致的路由或别名，保持 standalone 路由兼容。
    │
    ├── ops/
    │   └── src/router/index.js
    │       # [MODIFY] 修正 qiankun 模式下 history base 与 /ops 前缀路由的匹配关系，避免无匹配导致空页面。
    │
    └── shell/
        └── src/
            ├── minFrame/index.js
            │   # [MODIFY] 增强 qiankun 微应用加载失败处理，输出可诊断错误并跳转到可见错误页。
            └── views/ForbiddenView.vue
                # [MODIFY] 复用现有错误页展示微应用加载失败原因和返回入口，避免纯白屏。
```

## Key Code Structures

无需新增复杂接口或数据结构。核心变更为脚本、SQL seed 数据、Vue Router 配置和已有错误处理逻辑。

## Agent Extensions

### SubAgent

- **code-explorer**
- Purpose: 继续复核白屏触发链路、关键文件和实际调用关系，避免遗漏 qiankun 注册、路由和远程模块加载问题。
- Expected outcome: 输出确认后的根因清单和需要修改的最小文件范围。

### Skill

- **全栈开发伴侣**
- Purpose: 按全栈项目流程验证前后端本地启动、接口连通和登录后页面链路。
- Expected outcome: shell、portal、ops、backend 在本地端口稳定运行，核心页面可访问。

- **前端用户体验工程**
- Purpose: 修复白屏兜底体验，确保微应用加载失败时有明确提示和可操作回退。
- Expected outcome: 页面不再纯白屏，用户能看到错误说明并返回首页或重试。