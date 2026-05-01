# 校园二手交易平台 - 功能完善与UI优化检查清单

## 一、后端业务功能检查

### 1.1 收货地址管理模块

- [ ] Address 实体类字段完整，包含所有必需属性
- [ ] AddressRepository 数据访问方法正确实现
- [ ] AddressService 业务逻辑包含默认地址设置规则
- [ ] AddressController 接口路径遵循 RESTful 规范
- [ ] 接口权限注解正确配置（需要 BUYER 角色）
- [ ] 数据库 schema.sql 包含 address 表定义
- [ ] 初始数据 data.sql 包含测试地址数据

**验证方法**
```bash
# 启动后端服务后测试以下接口
curl -X POST http://localhost:8080/api/addresses \
  -H "Content-Type: application/json" \
  -H "Authorization: {token}" \
  -d '{"receiverName":"张三","receiverPhone":"13800138000","province":"北京","city":"北京市","district":"海淀区","detailAddress":"中关村大街1号"}'
```

### 1.2 购物车管理模块

- [ ] CartItem 实体类字段完整
- [ ] CartRepository 数据访问方法正确实现
- [ ] CartService 业务逻辑包含重复商品处理
- [ ] CartController 接口正确实现
- [ ] 数据库 schema.sql 包含 cart 表定义
- [ ] 购物车数量限制验证（每个商品最多99件）

**验证方法**
```bash
# 添加商品到购物车
curl -X POST http://localhost:8080/api/cart \
  -H "Content-Type: application/json" \
  -H "Authorization: {token}" \
  -d '{"itemId":1,"quantity":2}'

# 获取购物车列表
curl -X GET http://localhost:8080/api/cart \
  -H "Authorization: {token}"
```

### 1.3 商品评价模块

- [ ] Review 实体类字段完整，包含评分、内容、图片、回复等
- [ ] ReviewRepository 数据访问方法正确实现
- [ ] ReviewService 业务逻辑包含订单状态验证
- [ ] ReviewController 接口正确实现
- [ ] 数据库 schema.sql 包含 review 表定义
- [ ] 评价状态检查：订单必须为已完成状态
- [ ] 重复评价检查：一个订单只能评价一次
- [ ] 卖家回复功能正确实现

**验证方法**
```bash
# 提交评价
curl -X POST http://localhost:8080/api/reviews \
  -H "Content-Type: application/json" \
  -H "Authorization: {token}" \
  -d '{"orderId":1,"itemId":1,"rating":5,"content":"商品很好，满意！"}'

# 获取商品评价列表
curl -X GET http://localhost:8080/api/reviews/item/1

# 卖家回复评价
curl -X POST http://localhost:8080/api/reviews/1/reply \
  -H "Content-Type: application/json" \
  -H "Authorization: {token}" \
  -d '{"content":"感谢您的好评！"}'
```

### 1.4 卖家统计模块

- [ ] SellerStatsService 统计方法正确实现
- [ ] SellerStatsController 接口正确实现
- [ ] 销售概览包含今日、本月、累计数据
- [ ] 销售趋势数据格式正确
- [ ] 商品排行按销量排序

**验证方法**
```bash
# 获取销售概览
curl -X GET http://localhost:8080/api/seller/stats/overview \
  -H "Authorization: {token}"

# 获取销售趋势
curl -X GET "http://localhost:8080/api/seller/stats/trend?days=7" \
  -H "Authorization: {token}"

# 获取商品排行
curl -X GET "http://localhost:8080/api/seller/stats/ranking?limit=10" \
  -H "Authorization: {token}"
```

### 1.5 订单流程完善

- [ ] OrderService.createOrder 支持收货地址参数
- [ ] 订单评价状态检查逻辑正确
- [ ] OrderController 订单详情包含收货地址信息

**验证方法**
```bash
# 创建订单时传入收货地址
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: {token}" \
  -d '{"itemId":1,"quantity":1,"receiverName":"张三","receiverPhone":"13800138000","receiverAddress":"北京市海淀区中关村大街1号"}'
```

## 二、前端页面检查

### 2.1 项目配置

- [ ] package.json 包含 @arco-design/web-vue 依赖
- [ ] main.js 正确引入和配置 Arco Vue
- [ ] 全局样式变量正确定义
- [ ] 组件尺寸配置为 medium

### 2.2 收货地址管理页面

- [ ] AddressList.vue 页面正常加载
- [ ] 地址列表展示完整信息
- [ ] 新增地址表单验证通过
- [ ] 编辑地址功能正常
- [ ] 删除地址确认提示正常
- [ ] 设置默认地址功能正常
- [ ] 空状态页面友好展示
- [ ] 响应式布局适配移动端

**验证方法**
1. 登录买家账号
2. 进入"我的" -> "收货地址"页面
3. 测试新增、编辑、删除、设为默认功能

### 2.3 购物车页面

- [ ] Cart.vue 页面正常加载
- [ ] 购物车商品按卖家分组展示
- [ ] 商品数量修改功能正常
- [ ] 选择/取消选择功能正常
- [ ] 删除商品功能正常
- [ ] 总价实时计算正确
- [ ] 结算按钮跳转正确

**验证方法**
1. 添加多个商品到购物车
2. 进入购物车页面
3. 测试数量修改、选择、删除功能
4. 验证总价计算

### 2.4 MiniCart 组件

- [ ] MiniCart.vue 组件正常显示
- [ ] 悬浮位置正确（右下角）
- [ ] 商品数量徽章显示正确
- [ ] 点击展开功能正常
- [ ] 快捷结算入口功能正常

### 2.5 订单确认页优化

- [ ] OrderConfirm.vue 页面正常加载
- [ ] 收货地址选择功能正常
- [ ] 新增收货地址功能正常
- [ ] 地址回退功能正常
- [ ] 订单摘要计算正确

**验证方法**
1. 进入商品详情页
2. 点击"立即购买"
3. 在确认订单页选择或新增收货地址
4. 提交订单

### 2.6 评价功能页面

- [ ] ReviewSubmit.vue 页面正常加载
- [ ] 评分选择器（星星）功能正常
- [ ] 评价内容输入正常
- [ ] 图片上传功能正常
- [ ] 表单验证正常
- [ ] 提交成功跳转正确

- [ ] ReviewList.vue 评价列表正常加载
- [ ] 评价卡片展示正确
- [ ] 卖家回复展示正确
- [ ] 分页功能正常

**验证方法**
1. 完成一笔订单（确认收货）
2. 进入"我的订单"
3. 点击"去评价"按钮
4. 填写评价内容并提交

### 2.7 卖家统计页面

- [ ] SellerStats.vue 页面正常加载
- [ ] 数据卡片展示正确
- [ ] 销售趋势图显示正确
- [ ] 商品排行列表正确
- [ ] 数据刷新功能正常

### 2.8 商品详情页优化

- [ ] ItemDetail.vue 页面正常加载
- [ ] 收藏按钮功能正常
- [ ] 加入购物车功能正常
- [ ] 图片画廊展示正确
- [ ] 页面加载动画正常

### 2.9 路由配置

- [ ] router/index.js 包含所有新页面路由
- [ ] 路由守卫正确配置
- [ ] 懒加载配置正确

### 2.10 API 服务

- [ ] api.js 包含所有新接口方法
- [ ] 接口路径正确
- [ ] 请求参数正确
- [ ] 响应处理正确

## 三、UI 设计规范检查

### 3.1 色彩系统

- [ ] 主色调正确使用 #7C3AED
- [ ] 渐变背景正确应用
- [ ] 功能色（success/warning/danger/info）正确使用
- [ ] 中性色层级分明

### 3.2 组件使用

- [ ] 按钮组件使用 Arco Button
- [ ] 表单组件使用 Arco Form
- [ ] 卡片组件使用 Arco Card
- [ ] 列表组件使用 Arco List
- [ ] 弹窗组件使用 Arco Modal
- [ ] 消息提示使用 Arco Message

### 3.3 布局规范

- [ ] 页面容器使用统一间距（24px）
- [ ] 卡片使用统一圆角（12px）
- [ ] 响应式断点正确使用
- [ ] 栅格布局正确使用

### 3.4 动画效果

- [ ] 页面切换动画正常
- [ ] 卡片悬停效果正常
- [ ] 按钮点击反馈正常
- [ ] 加载状态显示正确

## 四、技术规范检查

### 4.1 代码规范

- [ ] Vue 组件使用组合式 API（<script setup>）
- [ ] 组件命名遵循 PascalCase
- [ ] 方法命名遵循 camelCase
- [ ] CSS 类名使用 kebab-case
- [ ] 代码注释完整

### 4.2 错误处理

- [ ] 表单验证错误提示友好
- [ ] API 请求错误正确捕获
- [ ] 空状态页面友好展示
- [ ] 加载状态显示正常

### 4.3 性能优化

- [ ] 图片懒加载配置
- [ ] 路由懒加载配置
- [ ] 组件按需引入

## 五、端到端测试

### 5.1 收货地址流程

- [ ] 用户注册并登录
- [ ] 进入收货地址管理
- [ ] 新增收货地址
- [ ] 设置默认地址
- [ ] 编辑收货地址
- [ ] 删除收货地址

### 5.2 购物车流程

- [ ] 浏览商品列表
- [ ] 点击"加入购物车"
- [ ] 进入购物车页面
- [ ] 修改商品数量
- [ ] 选择商品结算
- [ ] 进入订单确认页

### 5.3 订单评价流程

- [ ] 完成商品购买（创建订单 -> 支付 -> 卖家发货 -> 确认收货）
- [ ] 进入"我的订单"
- [ ] 点击"去评价"
- [ ] 填写评价内容
- [ ] 提交评价
- [ ] 查看评价列表

### 5.4 卖家统计流程

- [ ] 卖家登录
- [ ] 进入"卖家中心"
- [ ] 查看销售概览
- [ ] 查看销售趋势
- [ ] 查看商品排行

## 六、兼容性检查

- [ ] Chrome 浏览器测试通过
- [ ] Firefox 浏览器测试通过
- [ ] Safari 浏览器测试通过
- [ ] Edge 浏览器测试通过
- [ ] 移动端浏览器测试通过
- [ ] 响应式布局在各种屏幕尺寸下正常

## 七、部署检查

- [ ] 后端构建成功（mvn clean package）
- [ ] 前端构建成功（pnpm run build）
- [ ] 环境变量配置正确
- [ ] API 地址配置正确
- [ ] 数据库连接配置正确
