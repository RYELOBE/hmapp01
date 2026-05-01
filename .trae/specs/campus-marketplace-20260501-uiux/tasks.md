# 校园二手交易平台 - 功能完善与UI优化任务清单

## 阶段一：后端业务功能实现

### 1.1 收货地址管理模块

- [x] **Task 1.1.1**: 创建 Address 实体类和 AddressRepository 数据访问层
  - 定义 Address 实体，包含 id, userId, receiverName, receiverPhone, province, city, district, detailAddress, postalCode, isDefault, createTime, updateTime 字段
  - 实现基于 JdbcTemplate 的数据访问方法

- [x] **Task 1.1.2**: 创建 AddressService 业务服务层
  - 实现地址的 CRUD 操作
  - 实现默认地址设置逻辑
  - 实现用户地址列表查询

- [x] **Task 1.1.3**: 创建 AddressController REST 接口
  - POST /api/addresses - 新增地址
  - GET /api/addresses - 获取地址列表
  - GET /api/addresses/{id} - 获取地址详情
  - PUT /api/addresses/{id} - 更新地址
  - DELETE /api/addresses/{id} - 删除地址
  - PUT /api/addresses/{id}/default - 设为默认

- [x] **Task 1.1.4**: 更新数据库脚本
  - 在 schema.sql 中添加 address 表定义
  - 在 data.sql 中添加初始化数据

### 1.2 购物车管理模块

- [x] **Task 1.2.1**: 创建 Cart/CartItem 实体类和 CartRepository 数据访问层
  - 定义 CartItem 实体，包含 id, userId, itemId, quantity, selected, createTime, updateTime 字段
  - 实现购物车的增删改查方法

- [x] **Task 1.2.2**: 创建 CartService 业务服务层
  - 实现添加商品到购物车
  - 实现更新购物车商品数量
  - 实现删除购物车商品
  - 实现清空购物车

- [x] **Task 1.2.3**: 创建 CartController REST 接口
  - GET /api/cart - 获取购物车列表
  - POST /api/cart - 添加到购物车
  - PUT /api/cart/{id} - 更新数量
  - PUT /api/cart/select - 更新选中状态
  - DELETE /api/cart/{id} - 删除
  - DELETE /api/cart/clear - 清空

- [x] **Task 1.2.4**: 更新数据库脚本
  - 在 schema.sql 中添加 cart 表定义

### 1.3 商品评价模块

- [x] **Task 1.3.1**: 创建 Review 实体类和 ReviewRepository 数据访问层
  - 定义 Review 实体，包含 id, orderId, itemId, buyerId, rating, content, images, reply, replyTime, createTime 字段
  - 实现评价的增删改查方法

- [x] **Task 1.3.2**: 创建 ReviewService 业务服务层
  - 实现提交评价
  - 实现获取商品评价列表
  - 实现获取订单评价
  - 实现卖家回复评价

- [x] **Task 1.3.3**: 创建 ReviewController REST 接口
  - POST /api/reviews - 提交评价
  - GET /api/reviews/item/{itemId} - 商品评价列表
  - GET /api/reviews/order/{orderId} - 订单评价
  - POST /api/reviews/{id}/reply - 卖家回复

- [x] **Task 1.3.4**: 更新数据库脚本
  - 在 schema.sql 中添加 review 表定义

### 1.4 收藏功能模块

- [x] **Task 1.4.1**: 创建 FavoriteRepository 数据访问层
  - 实现收藏的增删改查方法

- [x] **Task 1.4.2**: 创建 FavoriteService 业务服务层
  - 实现添加收藏
  - 实现取消收藏
  - 实现收藏列表查询

- [x] **Task 1.4.3**: 创建 FavoriteController REST 接口
  - GET /api/favorites - 获取收藏列表
  - POST /api/favorites/{itemId} - 添加收藏
  - DELETE /api/favorites/{itemId} - 取消收藏
  - GET /api/favorites/check/{itemId} - 检查收藏状态

### 1.5 卖家统计模块

- [x] **Task 1.5.1**: 创建 SellerStatsService 业务服务层
  - 实现销售概览统计（今日、本月、累计）
  - 实现销售趋势数据（最近7天/30天）
  - 实现商品销售排行

- [x] **Task 1.5.2**: 创建 SellerStatsController REST 接口
  - GET /api/seller/stats/overview - 销售概览
  - GET /api/seller/stats/trend - 销售趋势
  - GET /api/seller/stats/ranking - 商品排行

## 阶段二：前端页面实现

### 2.1 项目配置

- [x] **Task 2.1.1**: Arco Design Vue 依赖已安装
  - package.json 中包含 @arco-design/web-vue 依赖

- [x] **Task 2.1.2**: 更新全局样式
  - 定义设计系统颜色变量
  - 定义字体和字号规范
  - 添加通用工具类

### 2.2 收货地址管理页面

- [x] **Task 2.2.1**: 创建 AddressList.vue 地址列表页面
  - 使用 Arco Card 组件展示地址卡片
  - 实现新增、编辑、删除地址功能
  - 实现设置默认地址功能
  - 响应式卡片布局

### 2.3 购物车页面

- [x] **Task 2.3.1**: 创建 Cart.vue 购物车页面
  - 按卖家分组展示购物车商品
  - 实现数量修改功能
  - 实现选择/取消选择功能
  - 实现删除功能
  - 实时计算总价

- [x] **Task 2.3.2**: 创建 MiniCart.vue 迷你购物车组件
  - 悬浮在页面右下角
  - 显示购物车商品数量
  - 点击展开快速查看
  - 支持快捷结算入口

### 2.4 收藏功能页面

- [x] **Task 2.4.1**: 创建 Favorites.vue 收藏列表页面
  - 展示用户收藏的所有商品
  - 支持取消收藏功能
  - 支持快速购买功能

### 2.5 评价功能页面

- [x] **Task 2.5.1**: 创建 ReviewSubmit.vue 评价提交页面
  - 商品评分选择（1-5星）
  - 评价内容输入
  - 图片上传功能
  - 提交成功后跳转

### 2.6 卖家中心优化

- [x] **Task 2.6.1**: 创建 SellerStats.vue 销售统计页面
  - 销售数据卡片展示
  - 销售趋势图表
  - 商品销售排行列表

### 2.7 商品详情页优化

- [x] **Task 2.7.1**: 优化 ItemDetail.vue 商品详情页
  - 添加收藏按钮功能
  - 添加加入购物车功能
  - 优化图片画廊展示
  - 优化商品信息布局

### 2.8 路由和服务

- [x] **Task 2.8.1**: 更新 portal/src/router/index.js
  - 添加新页面的路由配置
  - 配置路由守卫

- [x] **Task 2.8.2**: 更新 portal/src/services/api.js
  - 添加地址相关 API 方法
  - 添加购物车相关 API 方法
  - 添加收藏相关 API 方法
  - 添加评价相关 API 方法
  - 添加卖家统计相关 API 方法

### 2.9 布局优化

- [x] **Task 2.9.1**: 优化 Layout.vue 布局组件
  - 添加收货地址入口
  - 添加我的收藏入口
  - 添加销售统计入口
  - 集成 MiniCart 组件
  - 更新导航链接

## 已完成文件清单

### 后端新增文件
- `backend/.../controller/AddressController.java`
- `backend/.../controller/CartController.java`
- `backend/.../controller/FavoriteController.java`
- `backend/.../controller/ReviewController.java`
- `backend/.../controller/SellerStatsController.java`
- `backend/.../service/AddressService.java`
- `backend/.../service/CartService.java`
- `backend/.../service/FavoriteService.java`
- `backend/.../service/ReviewService.java`
- `backend/.../service/SellerStatsService.java`
- `backend/.../repository/AddressRepository.java`
- `backend/.../repository/CartRepository.java`
- `backend/.../repository/FavoriteRepository.java`
- `backend/.../repository/ReviewRepository.java`

### 后端修改文件
- `backend/.../repository/OrderRepository.java` - 添加统计查询方法
- `backend/src/main/resources/schema.sql` - 添加 address, cart, favorite, review 表

### 前端新增文件
- `packages/apps/portal/src/views/AddressList.vue`
- `packages/apps/portal/src/views/Cart.vue`
- `packages/apps/portal/src/views/Favorites.vue`
- `packages/apps/portal/src/views/ReviewSubmit.vue`
- `packages/apps/portal/src/views/SellerStats.vue`
- `packages/apps/portal/src/components/MiniCart.vue`

### 前端修改文件
- `packages/apps/portal/src/views/ItemDetail.vue` - 添加收藏和加入购物车功能
- `packages/apps/portal/src/views/Layout.vue` - 添加导航入口和 MiniCart 组件
- `packages/apps/portal/src/router/index.js` - 添加新页面路由
- `packages/apps/portal/src/services/api.js` - 添加所有新接口方法
