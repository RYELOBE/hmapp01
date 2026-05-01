# Bug 审计验证检查清单

## P0 - 编译级错误

- [x] ReviewService 中已添加 getReviewQueuePaged 方法，OpsReviewController 可正常编译
- [x] ReviewService 中已添加 approve 方法，OpsReviewController 可正常编译
- [x] ReviewService 中已添加 reject 方法，OpsReviewController 可正常编译
- [x] ReviewService 中 UserAccountRepository 引用已替换为 UserRepository
- [x] 后端项目可成功编译（mvn compile 无错误）

## P1 - 前端数据解析

- [x] Cart.vue 中购物车数据正确显示（不再为空数组）
- [x] ItemDetail.vue 中收藏状态正确显示
- [x] SellerStats.vue 中统计数据正确显示
- [x] Favorites.vue 中收藏列表正确显示
- [x] MiniCart.vue 中购物车数量和列表正确显示
- [x] OrderMonitor.vue 中订单数据正确显示

## P1 - API 路径

- [x] 前端上传图片接口路径与后端一致，上传功能正常

## P1 - 常量一致性

- [x] 首页分类筛选值与 common 包一致，后端可正确识别
- [x] 发布商品页分类选项值与 common 包一致
- [x] 发布商品页成色选项值与 common 包一致

## P1 - 安全

- [x] 用户登录使用 BCrypt 校验密码
- [x] 运营登录使用 BCrypt 校验密码
- [x] 用户注册密码 BCrypt 加密存储
- [x] 数据库中密码不以明文存储

## P1 - 路由与 Token

- [x] 401 错误时正确跳转到 /login 而非 /#/login
- [x] ops 退出登录清除的是 ops token 而非用户 token
- [x] ops 子应用在微前端模式下路由 base 为 /ops
- [x] ops 应用调用非 /ops/ 前缀 API 时使用 ops token

## P2 - 业务逻辑

- [x] 审核详情页商品成色只显示一次
- [x] 购物车多商品结算功能正常
- [x] 订单确认页商品图片正确显示
- [x] 半星评分时评价文字正确显示
- [x] 双角色用户查看订单时买家和卖家订单都显示

## P2 - 代码质量

- [x] ops api.js 中无重复定义的函数
- [x] CartRepository 批量更新选中状态功能正常
