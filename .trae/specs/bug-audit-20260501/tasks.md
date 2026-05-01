# Tasks

## P0 - 编译级错误（必须首先修复）

- [x] Task 1: 修复 ReviewService 缺失方法
  - [x] 在 ReviewService 中添加 `getReviewQueuePaged(String status, int pageNo, int pageSize)` 方法，调用 ReviewRepository 查询待审核商品分页列表
  - [x] 在 ReviewService 中添加 `approve(Long itemId, Long operatorId)` 方法，更新商品审核状态为 APPROVED 并记录审核日志
  - [x] 在 ReviewService 中添加 `reject(Long itemId, Long operatorId, String reason)` 方法，更新商品审核状态为 REJECTED 并记录审核日志

- [x] Task 2: 修复 UserAccountRepository 不存在的问题
  - [x] 将 ReviewService 中的 `UserAccountRepository` 引用改为 `UserRepository`（UserRepository 已有 findById 方法）
  - [x] 确认 UserRepository.findById 返回的 Map 中包含 nickname 字段

## P1 - 前端数据解析错误（影响核心功能）

- [x] Task 3: 修复前端响应数据多取一层 .data 的问题
  - [x] 修复 `portal/src/views/Cart.vue` L228: `res?.data || []` → `res || []`
  - [x] 修复 `portal/src/views/ItemDetail.vue` L201: `res?.data?.isFavorited` → `res?.isFavorited`
  - [x] 修复 `portal/src/views/SellerStats.vue` L179: `res?.data` → `res`
  - [x] 修复 `portal/src/views/SellerStats.vue` L190: `res?.data || []` → `res || []`
  - [x] 修复 `portal/src/views/SellerStats.vue` L200: `res?.data || []` → `res || []`
  - [x] 修复 `portal/src/views/Favorites.vue` L131: `res?.data || []` → `res || []`
  - [x] 修复 `portal/src/components/MiniCart.vue` L95: `countRes?.data?.total` → `countRes?.total`
  - [x] 修复 `portal/src/components/MiniCart.vue` L99: `listRes?.data || []` → `listRes || []`
  - [x] 修复 `ops/src/views/OrderMonitor.vue` L318: `result?.data || result` → `result`

## P1 - API 路径不匹配

- [x] Task 4: 修复上传接口路径不匹配
  - [x] 将 `portal/src/services/api.js` L190 的上传路径从 `/upload/image` 改为 `/upload`

## P1 - 常量不一致

- [x] Task 5: 统一分类常量
  - [x] 修改 `portal/src/views/home/const.js` 中的 CATEGORIES，使 value 与 `@campus/common/constants.js` 一致
  - [x] 修改 `portal/src/views/seller/PublishItem.vue` 中的 CATEGORY_OPTIONS，使 value 与 common 包一致

- [x] Task 6: 统一成色常量
  - [x] 修改 `portal/src/views/seller/PublishItem.vue` 中的 CONDITION_OPTIONS，使 value 与 `@campus/common/constants.js` 一致（brand_new/like_new/good/fair/poor）

## P1 - 安全漏洞

- [x] Task 7: 修复密码明文比对
  - [x] 在 AuthService.login 中使用 BCrypt 密码校验替代 String.equals
  - [x] 在 OpsAuthService.opsLogin 中使用 BCrypt 密码校验替代 String.equals
  - [x] 在 UserRepository.create 中注册时使用 BCrypt 加密存储密码
  - [x] 在 data.sql 中将初始密码改为 BCrypt 加密后的值

## P1 - 路由与 Token 问题

- [x] Task 8: 修复 401 跳转路由模式
  - [x] 修改 `mf-shared/src/exposes/http.js`，将 `window.location.hash` 改为 `window.location.href`

- [x] Task 9: 修复 ops 退出登录 Token 混用
  - [x] 修改 `ops/src/views/Layout.vue` 中的退出登录逻辑，使用 `opsLogout()` 替代 `logout()`

- [x] Task 10: 修复 ops 路由 base 未适配微前端
  - [x] 修改 `ops/src/router/index.js`，将 `createWebHistory("/")` 改为 `createWebHistory(poweredByQiankun ? "/ops" : "/")`

- [x] Task 11: 修复 ops API 中非 /ops/ 路径的 Token 选择问题
  - [x] 在 http.js 中添加 appType 配置参数，创建 opsHttp 实例
  - [x] ops api.js 改为导入 opsHttp

## P2 - 业务逻辑 Bug

- [x] Task 12: 修复 ReviewDetail 重复渲染
  - [x] 删除 `ops/src/views/ReviewDetail.vue` 中重复的 `conditionLevel` 渲染项

- [x] Task 13: 修复购物车结算只取第一个商品
  - [x] 修改 `portal/src/views/Cart.vue` 的 checkout 方法，支持多商品结算

- [x] Task 14: 修复订单确认页图片无法显示
  - [x] 修改 `portal/src/views/orders/OrderConfirm.vue`，解析 imageUrls JSON 字符串后取第一张图片 URL

- [x] Task 15: 修复半星评分导致 ratingText 显示 undefined
  - [x] 修改 `portal/src/views/ReviewSubmit.vue` 的 ratingText 计算属性，使用 `Math.floor(form.value.rating) - 1`

- [x] Task 16: 修复 OrderService.getMinePaged 双角色逻辑
  - [x] 修改 OrderService.getMinePaged 中 `isSeller && isBuyer` 分支，合并买家和卖家的订单查询结果

## P2 - 代码质量

- [x] Task 17: 删除 ops api.js 中重复定义的 API 函数
  - [x] 删除 `ops/src/services/api.js` 中第一次定义的函数，保留带 params 参数的版本

- [x] Task 18: 修复 CartRepository.updateSelectedByUserIdAndItemIds 参数传递
  - [x] 修改 CartRepository 的 JDBC 参数传递方式，确保 itemIds 数组正确展开为可变参数

# Task Dependencies
- Task 1, Task 2 无依赖，可并行执行
- Task 3-11 无依赖，可并行执行
- Task 12-18 无依赖，可并行执行
- 建议按 P0 → P1 → P2 顺序执行
