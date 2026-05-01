# 校园二手交易平台 - 功能完善与UI优化任务清单

## 阶段一：后端业务功能实现

### 1.1 收货地址管理模块

- [ ] **Task 1.1.1**: 创建 Address 实体类和 AddressRepository 数据访问层
  - 定义 Address 实体，包含 id, userId, receiverName, receiverPhone, province, city, district, detailAddress, postalCode, isDefault, createTime, updateTime 字段
  - 实现基于 JdbcTemplate 的数据访问方法

- [ ] **Task 1.1.2**: 创建 AddressService 业务服务层
  - 实现地址的 CRUD 操作
  - 实现默认地址设置逻辑
  - 实现用户地址列表查询

- [ ] **Task 1.1.3**: 创建 AddressController REST 接口
  - POST /api/addresses - 新增地址
  - GET /api/addresses - 获取地址列表
  - GET /api/addresses/{id} - 获取地址详情
  - PUT /api/addresses/{id} - 更新地址
  - DELETE /api/addresses/{id} - 删除地址
  - PUT /api/addresses/{id}/default - 设为默认

- [ ] **Task 1.1.4**: 更新数据库脚本
  - 在 schema.sql 中添加 address 表定义
  - 在 data.sql 中添加初始化数据

### 1.2 购物车管理模块

- [ ] **Task 1.2.1**: 创建 Cart/CartItem 实体类和 CartRepository 数据访问层
  - 定义 CartItem 实体，包含 id, userId, itemId, quantity, selected, createTime, updateTime 字段
  - 实现购物车的增删改查方法

- [ ] **Task 1.2.2**: 创建 CartService 业务服务层
  - 实现添加商品到购物车
  - 实现更新购物车商品数量
  - 实现删除购物车商品
  - 实现清空购物车

- [ ] **Task 1.2.3**: 创建 CartController REST 接口
  - GET /api/cart - 获取购物车列表
  - POST /api/cart - 添加到购物车
  - PUT /api/cart/{id} - 更新数量
  - PUT /api/cart/select - 更新选中状态
  - DELETE /api/cart/{id} - 删除
  - DELETE /api/cart/clear - 清空

- [ ] **Task 1.2.4**: 更新数据库脚本
  - 在 schema.sql 中添加 cart 表定义

### 1.3 商品评价模块

- [ ] **Task 1.3.1**: 创建 Review 实体类和 ReviewRepository 数据访问层
  - 定义 Review 实体，包含 id, orderId, itemId, buyerId, rating, content, images, reply, replyTime, createTime 字段
  - 实现评价的增删改查方法

- [ ] **Task 1.3.2**: 创建 ReviewService 业务服务层
  - 实现提交评价
  - 实现获取商品评价列表
  - 实现获取订单评价
  - 实现卖家回复评价

- [ ] **Task 1.3.3**: 创建 ReviewController REST 接口
  - POST /api/reviews - 提交评价
  - GET /api/reviews/item/{itemId} - 商品评价列表
  - GET /api/reviews/order/{orderId} - 订单评价
  - POST /api/reviews/{id}/reply - 卖家回复

- [ ] **Task 1.3.4**: 更新数据库脚本
  - 在 schema.sql 中添加 review 表定义

### 1.4 卖家统计模块

- [ ] **Task 1.4.1**: 创建 SellerStatsService 业务服务层
  - 实现销售概览统计（今日、本月、累计）
  - 实现销售趋势数据（最近7天/30天）
  - 实现商品销售排行

- [ ] **Task 1.4.2**: 创建 SellerStatsController REST 接口
  - GET /api/seller/stats/overview - 销售概览
  - GET /api/seller/stats/trend - 销售趋势
  - GET /api/seller/stats/ranking - 商品排行

### 1.5 订单流程完善

- [ ] **Task 1.5.1**: 完善 OrderService 订单评价状态检查
  - 订单完成后才能评价
  - 一个订单只能评价一次
  - 更新 OrderService.createOrder 支持收货地址

- [ ] **Task 1.5.2**: 完善 OrderController 接口
  - GET /api/orders/{id} - 获取订单详情（包含收货地址）

## 阶段二：前端页面实现

### 2.1 项目配置

- [ ] **Task 2.1.1**: 安装 Arco Design Vue 依赖
  - pnpm add @arco-design/web-vue

- [ ] **Task 2.1.2**: 配置 Arco Design Vue 全局设置
  - 在 main.js 中引入和注册组件库
  - 配置全局样式变量
  - 设置组件尺寸为 medium

- [ ] **Task 2.1.3**: 更新全局样式
  - 定义设计系统颜色变量
  - 定义字体和字号规范
  - 添加通用工具类

### 2.2 收货地址管理页面

- [ ] **Task 2.2.1**: 创建 AddressList.vue 地址列表页面
  - 使用 Arco List 组件展示地址列表
  - 实现新增、编辑、删除地址功能
  - 实现设置默认地址功能
  - 响应式卡片布局

- [ ] **Task 2.2.2**: 创建 AddressForm.vue 地址编辑表单组件
  - 使用 Arco Form 组件
  - 实现省市区三级联动选择
  - 表单验证规则

### 2.3 购物车页面

- [ ] **Task 2.3.1**: 创建 Cart.vue 购物车页面
  - 按卖家分组展示购物车商品
  - 实现数量修改功能
  - 实现选择/取消选择功能
  - 实现删除和移到收藏功能
  - 实时计算总价

- [ ] **Task 2.3.2**: 创建 MiniCart.vue 迷你购物车组件
  - 悬浮在页面右下角
  - 显示购物车商品数量
  - 点击展开快速查看
  - 支持快捷结算入口

### 2.4 订单确认页优化

- [ ] **Task 2.4.1**: 优化 OrderConfirm.vue 订单确认页面
  - 添加收货地址选择功能
  - 集成 AddressList 组件
  - 支持新增收货地址

### 2.5 买家中心优化

- [ ] **Task 2.5.1**: 创建 ReviewSubmit.vue 评价提交页面
  - 商品评分选择（1-5星）
  - 评价内容输入
  - 图片上传功能
  - 提交成功后跳转

- [ ] **Task 2.5.2**: 创建 ReviewList.vue 评价列表页面
  - 商品评价展示
  - 评价卡片组件复用

- [ ] **Task 2.5.3**: 优化 MyOrders.vue 订单列表页面
  - 新增评价入口（仅已完成订单）
  - 优化状态标签显示

### 2.6 卖家中心优化

- [ ] **Task 2.6.1**: 创建 SellerStats.vue 销售统计页面
  - 销售数据卡片展示
  - 销售趋势图表
  - 商品销售排行列表

- [ ] **Task 2.6.2**: 优化 MyItems.vue 商品管理页面
  - 添加统计数据卡片
  - 优化操作按钮布局

### 2.7 商品详情页优化

- [ ] **Task 2.7.1**: 优化 ItemDetail.vue 商品详情页
  - 添加收藏按钮功能
  - 添加加入购物车功能
  - 优化图片画廊展示
  - 优化商品信息布局

### 2.8 共享组件优化

- [ ] **Task 2.8.1**: 优化 mf-shared 中的 ItemCard.vue
  - 添加收藏按钮
  - 优化卡片样式和动画
  - 添加 Arco 组件样式

- [ ] **Task 2.8.2**: 优化 mf-shared 中的 ImageGallery.vue
  - 添加 Arco 样式支持
  - 优化图片切换动画

### 2.9 路由和服务

- [ ] **Task 2.9.1**: 更新 portal/src/router/index.js
  - 添加新页面的路由配置
  - 配置路由守卫

- [ ] **Task 2.9.2**: 更新 portal/src/services/api.js
  - 添加地址相关 API 方法
  - 添加购物车相关 API 方法
  - 添加评价相关 API 方法
  - 添加卖家统计相关 API 方法

## 阶段三：技术文档更新

- [ ] **Task 3.1**: 更新 TECHNICAL_IMPLEMENTATION.md
  - 添加收货地址模块实现说明
  - 添加购物车模块实现说明
  - 添加评价模块实现说明
  - 添加卖家统计模块实现说明
  - 添加 Arco Design Vue 集成说明

- [ ] **Task 3.2**: 更新 SPEC.md
  - 添加收货地址功能描述
  - 添加购物车功能描述
  - 添加评价功能描述
  - 更新 UI 组件库说明

## 任务依赖关系

```
Task 1.1.1 → Task 1.1.2 → Task 1.1.3 → Task 1.1.4
Task 1.2.1 → Task 1.2.2 → Task 1.2.3 → Task 1.2.4
Task 1.3.1 → Task 1.3.2 → Task 1.3.3 → Task 1.3.4
Task 1.4.1 → Task 1.4.2
Task 1.5.1 → Task 1.5.2

Task 2.1.1 → Task 2.1.2 → Task 2.1.3
Task 2.2.1 → Task 2.2.2
Task 2.3.1 → Task 2.3.2
Task 2.4.1 (depends on Task 2.2.1)
Task 2.5.1 → Task 2.5.2 → Task 2.5.3
Task 2.6.1 → Task 2.6.2
Task 2.7.1 (depends on Task 2.3.1)
Task 2.8.1 → Task 2.8.2
Task 2.9.1 → Task 2.9.2

Task 3.1 → Task 3.2 (after all implementation)
```

## 优先级排序

**第一优先级（核心业务流程阻塞）**
1. 收货地址管理（订单创建依赖）
2. 订单确认页收货地址选择
3. API 服务层配置

**第二优先级（核心功能）**
1. 购物车基础功能
2. 商品评价功能
3. 卖家统计功能

**第三优先级（用户体验优化）**
1. UI 样式优化
2. 迷你购物车组件
3. 共享组件优化

**第四优先级（文档）**
1. 技术文档更新
2. 规格文档更新
