# 校园二手交易平台 Bug 审计报告

## Why
项目代码中存在大量具体 bug，包括后端编译错误、前端数据解析错误、API 不匹配、安全漏洞等问题，需要系统性地梳理和修复以确保项目可正常运行。

## What Changes
- 修复后端编译错误：缺失的 Repository 类、缺失的 Service 方法
- 修复前端响应数据解析错误：http 拦截器已解包但组件多取一层 .data
- 修复前后端 API 不匹配：上传接口路径、分类常量不一致
- 修复安全漏洞：密码明文比对、越权访问
- 修复微前端集成问题：路由模式不匹配、Token 混用
- 修复业务逻辑错误：购物车结算、图片解析、评分取整

## Impact
- Affected code: 后端全部 Controller/Service/Repository，前端 portal/ops/shell/mf-shared 四个子应用

---

## ADDED Requirements

### Requirement: 后端编译级 Bug 修复

#### Scenario: ReviewService 缺失方法导致编译失败
- **WHEN** 项目编译时
- **THEN** OpsReviewController 调用的 `reviewService.getReviewQueuePaged()`、`reviewService.approve()`、`reviewService.reject()` 方法在 ReviewService 中不存在，编译失败

#### Scenario: UserAccountRepository 不存在导致编译失败
- **WHEN** 项目编译时
- **THEN** ReviewService 引用了 `UserAccountRepository`，但该类在项目中不存在，编译失败

### Requirement: 前端响应数据解析修复

#### Scenario: http 拦截器已解包 data.data，组件不应再取 .data
- **WHEN** 前端组件调用 API 后处理响应数据
- **THEN** http.js 拦截器在 code===0/200 时已返回 `data.data`（解包后的数据），但多个组件仍多取一层 `.data`，导致实际数据为 undefined

受影响文件及行号：
1. `portal/src/views/Cart.vue` L228: `res?.data || []` → `res || []`
2. `portal/src/views/ItemDetail.vue` L201: `res?.data?.isFavorited` → `res?.isFavorited`
3. `portal/src/views/SellerStats.vue` L179: `res?.data` → `res`
4. `portal/src/views/SellerStats.vue` L190: `res?.data || []` → `res || []`
5. `portal/src/views/SellerStats.vue` L200: `res?.data || []` → `res || []`
6. `portal/src/views/Favorites.vue` L131: `res?.data || []` → `res || []`
7. `portal/src/components/MiniCart.vue` L95: `countRes?.data?.total` → `countRes?.total`
8. `portal/src/components/MiniCart.vue` L99: `listRes?.data || []` → `listRes || []`
9. `ops/src/views/OrderMonitor.vue` L318: `result?.data || result` → `result`

### Requirement: 上传接口路径不匹配修复

#### Scenario: 前端调用上传接口路径与后端不一致
- **WHEN** 前端调用 `uploadImage` 发送 POST `/upload/image`
- **THEN** 后端 UploadController 的路径是 POST `/api/upload`，前端路径多了 `/image` 后缀，导致 404

### Requirement: 分类常量不一致修复

#### Scenario: 前端分类值与 common 包定义不匹配
- **WHEN** 前端发布商品或筛选分类时
- **THEN** 首页 `home/const.js` 使用 `digital`/`book`/`sport`/`instrument`/`other`，而 `@campus/common/constants.js` 定义为 `electronics`/`textbooks`/`sports`/`furniture`/`transport`/`others`，PublishItem.vue 也使用 `digital`/`book` 等值，后端无法正确识别

### Requirement: 成色常量不一致修复

#### Scenario: 前端成色值与 common 包定义不匹配
- **WHEN** 前端发布商品选择成色时
- **THEN** PublishItem.vue 使用 `new`/`99`/`95`/`9`/`8`/`7`，而 common 包定义为 `brand_new`/`like_new`/`good`/`fair`/`poor`，后端无法正确识别

### Requirement: 密码明文比对修复

#### Scenario: 登录时密码未加密比对
- **WHEN** 用户登录时
- **THEN** AuthService L24 和 OpsAuthService L27 直接用 `String.equals()` 比对明文密码，未使用 BCrypt 等加密方式，存在安全隐患

### Requirement: 401 跳转路由模式修复

#### Scenario: Token 过期后跳转登录页使用 hash 路由
- **WHEN** 后端返回 401 时
- **THEN** http.js L102 和 L140 使用 `window.location.hash = "#/login"` 跳转，但项目使用 history 路由模式，导致跳转到错误 URL

### Requirement: ops 退出登录 Token 混用修复

#### Scenario: ops 退出登录清除了错误的 Token
- **WHEN** 运营人员退出登录时
- **THEN** ops Layout 调用 `logoutSdk()` 实际是 `logout()`，清除的是普通用户 token `campus_market_token`，而非运营 token `campus_ops_token`

### Requirement: ops 路由 base 未适配微前端修复

#### Scenario: ops 子应用路由 base 未根据 qiankun 动态调整
- **WHEN** ops 作为微前端子应用运行时
- **THEN** ops router 使用 `createWebHistory("/")` 而非 `createWebHistory(poweredByQiankun ? "/ops" : "/")`，导致子应用路由与主应用冲突

### Requirement: ReviewDetail 重复渲染修复

#### Scenario: 审核详情页商品成色重复显示
- **WHEN** 运营人员查看审核详情时
- **THEN** ReviewDetail.vue L46-51 中 `conditionLevel` 被渲染了两次

### Requirement: 购物车结算逻辑修复

#### Scenario: 购物车多商品结算只取第一个
- **WHEN** 用户在购物车选择多个商品点击结算时
- **THEN** Cart.vue L292-299 只取了第一个商品的 itemId 跳转，其余选中商品被忽略

### Requirement: 图片 URL 解析修复

#### Scenario: 订单确认页图片无法显示
- **WHEN** 用户在订单确认页查看商品图片时
- **THEN** OrderConfirm.vue L17 直接将 `imageUrls`（可能是 JSON 字符串如 `'["url1","url2"]'`）作为 img src，导致图片无法显示

### Requirement: 评分取整修复

#### Scenario: 半星评分导致文字显示 undefined
- **WHEN** 用户在评价页面选择半星评分（如 4.5 星）时
- **THEN** ReviewSubmit.vue L146-151 的 `ratingText` 使用 `texts[form.value.rating - 1]`，当评分为 4.5 时 `texts[3.5]` 为 undefined

### Requirement: ops API 中非 /ops/ 路径的 Token 选择修复

#### Scenario: ops 调用非 /ops/ 前缀的 API 时使用了错误的 Token
- **WHEN** ops 应用调用 `getItemDetail`（路径 `/items/${itemId}`）等非 `/ops/` 前缀的 API 时
- **THEN** http.js 的 `isOpsPath()` 判断基于 URL 是否包含 `/ops/`，导致这些请求使用了普通用户 Token 而非 ops Token

### Requirement: API 函数重复定义修复

#### Scenario: ops api.js 中函数被定义两次
- **WHEN** ops 应用导入 API 函数时
- **THEN** api.js L96-110 和 L142-156 中 `getResourceMenus`、`getResourceMenuTree`、`saveResourceMenu`、`deleteResourceMenu` 被重复定义，第二次覆盖第一次

### Requirement: OrderService.getMinePaged 双角色逻辑修复

#### Scenario: 同时拥有买家和卖家角色的用户查看订单
- **WHEN** 用户同时拥有 BUYER 和 SELLER 角色查看我的订单时
- **THEN** OrderService.getMinePaged L220-222 在 `isSeller && isBuyer` 时只查询了买家订单，未合并卖家订单，卖家作为卖方的订单不会显示

### Requirement: CartRepository.updateSelectedByUserIdAndItemIds 参数传递修复

#### Scenario: 批量更新购物车选中状态
- **WHEN** 前端调用批量选中/取消选中购物车项时
- **THEN** CartRepository L87 `jdbc.update(sql.toString(), selected, userId, itemIds.toArray())` 中 `itemIds.toArray()` 传入的是 Object 数组而非可变参数，JDBC 参数绑定可能不正确

## MODIFIED Requirements

### Requirement: ItemController.listItems 返回类型修复
原 `itemService.listItems()` 返回 `List<Map<String, Object>>`，但控制器方法返回 `Map<String, Object>`，已修复为包装成 `Map.of("code", 200, "data", ...)` 格式。

## REMOVED Requirements
无
