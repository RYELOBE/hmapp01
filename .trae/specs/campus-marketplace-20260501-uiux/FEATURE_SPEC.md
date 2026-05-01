# 校园二手交易平台 - 功能完善与UI优化规格说明书

## 一、项目背景与目标

### 1.1 现有项目状态

基于 campus-marketplace-20260501 规格文档的当前实现状态，项目已经完成了核心功能的初步实现。本次合并已将以下功能集成到 master 分支：

- 用户注册与登录（支持买家/卖家角色选择）
- 运营账号独立认证体系
- 完整的订单流程（创建、支付、发货、收货、退款）
- AI问答模块（支持模拟响应）
- 微前端架构优化
- Qiankun 子应用加载状态管理

### 1.2 本次完善目标

本次迭代聚焦于两个核心方向：

**业务功能完善**
- 实现收货地址管理功能
- 实现购物车功能
- 实现商品收藏功能
- 实现商品评价功能
- 完善卖家中心销售统计

**前端UI优化**
- 统一使用字节跳动 Arco Design Vue 组件库
- 优化所有页面的视觉设计
- 提升用户体验和交互流畅度

## 二、业务功能详细设计

### 2.1 收货地址管理

#### 2.1.1 功能描述

收货地址管理允许买家用户维护自己的收货地址列表，在下单时选择或新增收货地址。这是订单创建的必要前置功能。

#### 2.1.2 数据模型

```javascript
// 收货地址实体
{
  id: Long,           // 主键
  userId: Long,       // 所属用户ID
  receiverName: String,    // 收货人姓名
  receiverPhone: String,   // 联系电话
  province: String,        // 省份
  city: String,            // 城市
  district: String,         // 区/县
  detailAddress: String,    // 详细地址
  postalCode: String,       // 邮政编码（可选）
  isDefault: Boolean,       // 是否默认地址
  createTime: DateTime,
  updateTime: DateTime
}
```

#### 2.1.3 API接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/addresses | GET | 获取地址列表 | BUYER |
| /api/addresses | POST | 新增地址 | BUYER |
| /api/addresses/{id} | GET | 获取地址详情 | BUYER |
| /api/addresses/{id} | PUT | 更新地址 | BUYER |
| /api/addresses/{id} | DELETE | 删除地址 | BUYER |
| /api/addresses/{id}/default | PUT | 设为默认地址 | BUYER |

#### 2.1.4 前端页面

- **地址列表页**：展示用户所有收货地址，支持设为默认、编辑、删除操作
- **地址编辑弹窗**：表单编辑收货地址信息，支持省市区三级联动选择

### 2.2 购物车功能

#### 2.2.1 功能描述

购物车允许用户将多个商品加入购物车后统一结算，提升购物便利性。用户可以在购物车中管理待购买的商品。

#### 2.2.2 数据模型

```javascript
// 购物车项实体
{
  id: Long,           // 主键
  userId: Long,       // 所属用户ID
  itemId: Long,       // 商品ID
  quantity: Integer,   // 购买数量
  selected: Boolean,    // 是否选中结算
  createTime: DateTime,
  updateTime: DateTime
}
```

#### 2.2.3 API接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/cart | GET | 获取购物车列表 | BUYER |
| /api/cart | POST | 添加商品到购物车 | BUYER |
| /api/cart/{id} | PUT | 更新购物车项数量 | BUYER |
| /api/cart/{id} | DELETE | 从购物车删除 | BUYER |
| /api/cart/select | PUT | 更新选中状态 | BUYER |
| /api/cart/clear | DELETE | 清空购物车 | BUYER |

#### 2.2.4 前端页面

- **购物车页面**：展示购物车内所有商品，支持选择、修改数量、删除操作，提供结算入口
- **迷你购物车**：悬浮在页面右下角，显示购物车商品数量，点击展开快速查看

### 2.3 商品收藏功能

#### 2.3.1 功能描述

收藏功能允许用户将感兴趣的商品添加到收藏夹，方便后续查看和购买。

#### 2.3.2 API接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/favorites | GET | 获取收藏列表 | BUYER |
| /api/favorites | POST | 添加收藏 | BUYER |
| /api/favorites/{itemId} | DELETE | 取消收藏 | BUYER |
| /api/favorites/check/{itemId} | GET | 检查是否已收藏 | BUYER |

#### 2.3.3 前端组件

- **收藏按钮**：商品卡片和详情页显示，点击切换收藏状态
- **收藏列表页**：展示用户收藏的所有商品，支持一键购买

### 2.4 商品评价功能

#### 2.4.1 功能描述

买家确认收货后可以对商品进行评价，帮助其他买家了解商品质量。

#### 2.4.2 数据模型

```javascript
// 商品评价实体
{
  id: Long,           // 主键
  orderId: Long,      // 关联订单ID
  itemId: Long,       // 商品ID
  buyerId: Long,      // 买家ID
  rating: Integer,     // 评分 1-5
  content: String,     // 评价内容
  images: String[],   // 评价图片（可选）
  reply: String,      // 卖家回复（可选）
  replyTime: DateTime,// 回复时间
  createTime: DateTime
}

// 商品评分统计
{
  itemId: Long,
  averageRating: Decimal,  // 平均评分
  totalReviews: Integer,   // 评价总数
  ratingDistribution: {     // 评分分布
    "5": Integer,
    "4": Integer,
    "3": Integer,
    "2": Integer,
    "1": Integer
  }
}
```

#### 2.4.3 API接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/reviews | POST | 提交评价 | BUYER |
| /api/reviews/item/{itemId} | GET | 获取商品评价列表 | 公开 |
| /api/reviews/order/{orderId} | GET | 获取订单评价 | BUYER |
| /api/reviews/{id}/reply | POST | 卖家回复 | SELLER |

### 2.5 卖家销售统计

#### 2.5.1 功能描述

卖家中心提供销售统计数据，帮助卖家了解销售情况。

#### 2.5.2 数据指标

- **今日销售额/订单数**：当日成交金额和订单数量
- **本月销售额/订单数**：当月累计数据
- **累计销售额/订单数**：历史全部数据
- **销售趋势图**：最近7天/30天销售趋势
- **商品销售排行**：按销量排序的商品列表

#### 2.5.3 API接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/seller/stats/overview | GET | 销售概览 | SELLER |
| /api/seller/stats/trend | GET | 销售趋势 | SELLER |
| /api/seller/stats/ranking | GET | 商品排行 | SELLER |

## 三、前端UI设计规范

### 3.1 设计理念

采用字节跳动 Arco Design Vue 作为主组件库，打造现代、简洁、专业的校园二手交易平台界面。

**设计关键词**：年轻化、清新感、校园气息、可信赖

### 3.2 色彩系统

```css
:root {
  /* 主色调 - 渐变紫蓝色，体现年轻活力 */
  --color-primary: #7C3AED;
  --color-primary-light: #A78BFA;
  --color-primary-dark: #5B21B6;

  /* 渐变背景 */
  --gradient-primary: linear-gradient(135deg, #7C3AED 0%, #EC4899 100%);
  --gradient-bg: linear-gradient(180deg, #F5F3FF 0%, #FFFFFF 100%);

  /* 功能色 */
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-danger: #EF4444;
  --color-info: #3B82F6;

  /* 中性色 */
  --color-text-primary: #1F2937;
  --color-text-secondary: #6B7280;
  --color-text-tertiary: #9CA3AF;
  --color-border: #E5E7EB;
  --color-bg: #F9FAFB;

  /* 阴影 */
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  --shadow-card: 0 4px 20px rgba(124, 58, 237, 0.08);
}
```

### 3.3 字体系统

```css
/* 标题字体 */
font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;

/* 字号规范 */
--font-size-xs: 12px;
--font-size-sm: 14px;
--font-size-base: 16px;
--font-size-lg: 18px;
--font-size-xl: 20px;
--font-size-2xl: 24px;
--font-size-3xl: 30px;

/* 字重规范 */
--font-weight-normal: 400;
--font-weight-medium: 500;
--font-weight-semibold: 600;
--font-weight-bold: 700;
```

### 3.4 组件使用规范

#### 3.4.1 按钮

```vue
<!-- 主要按钮 -->
<a-button type="primary">主要操作</a-button>

<!-- 次要按钮 -->
<a-button>次要操作</a-button>

<!-- 文字按钮 -->
<a-button type="text">文字按钮</a-button>

<!-- 危险操作按钮 -->
<a-button type="primary" status="danger">删除</a-button>

<!-- 按钮尺寸 -->
<a-button size="small">小按钮</a-button>
<a-button size="medium">中按钮（默认）</a-button>
<a-button size="large">大按钮</a-button>
```

#### 3.4.2 卡片

```vue
<!-- 商品卡片 -->
<a-card hoverable class="item-card">
  <template #cover>
    <div class="item-image">
      <img :src="item.image" />
    </div>
  </template>
  <div class="item-info">
    <div class="item-title">{{ item.title }}</div>
    <div class="item-price">
      <span class="price-symbol">¥</span>
      <span class="price-value">{{ item.price }}</span>
    </div>
  </div>
</a-card>
```

#### 3.4.3 表单

```vue
<a-form :model="form" layout="vertical">
  <a-form-item label="收货人">
    <a-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
  </a-form-item>
  <a-form-item label="联系电话">
    <a-input v-model="form.phone" placeholder="请输入手机号" />
  </a-form-item>
  <a-form-item label="所在地区">
    <a-cascader v-model="form.region" :options="regionOptions" placeholder="请选择省市区" />
  </a-form-item>
  <a-form-item label="详细地址">
    <a-textarea v-model="form.detailAddress" :rows="3" placeholder="请输入详细地址" />
  </a-form-item>
</a-form>
```

#### 3.4.4 列表

```vue
<a-list :data="listData" :pagination="paginationConfig">
  <template #item="{ item }">
    <a-list-item>
      <div class="list-item-content">
        <!-- 内容 -->
      </div>
      <template #actions>
        <a-button type="text">编辑</a-button>
        <a-button type="text" status="danger">删除</a-button>
      </template>
    </a-list-item>
  </template>
</a-list>
```

### 3.5 页面布局规范

#### 3.5.1 页面容器

```vue
<div class="page-container">
  <div class="page-header">
    <h1 class="page-title">页面标题</h1>
    <div class="page-actions">
      <a-button type="primary">主要操作</a-button>
    </div>
  </div>
  <div class="page-content">
    <!-- 页面内容 -->
  </div>
</div>
```

```css
.page-container {
  padding: 24px;
  background: var(--color-bg);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}
```

#### 3.5.2 栅格布局

```vue
<a-row :gutter="[24, 24]">
  <a-col :span="6" :xs="24" :sm="12" :md="8" :lg="6">
    <!-- 卡片内容 -->
  </a-col>
</a-row>
```

### 3.6 动画与交互

#### 3.6.1 页面切换动画

```css
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
```

#### 3.6.2 卡片悬停效果

```css
.item-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
```

#### 3.6.3 按钮点击反馈

```css
.btn-interactive {
  transition: transform 0.1s ease, opacity 0.2s ease;
}

.btn-interactive:active {
  transform: scale(0.98);
}
```

## 四、核心页面设计

### 4.1 首页（Home）

**布局结构**
```
┌─────────────────────────────────────────────────────────────┐
│ [Header] Logo | 搜索框 | 导航菜单 | 登录/用户中心            │
├─────────────────────────────────────────────────────────────┤
│ [Banner] 轮播图区域（3-5张自动轮播）                         │
├─────────────────────────────────────────────────────────────┤
│ [Categories] 分类导航（横向滚动）                             │
├─────────────────────────────────────────────────────────────┤
│ [Hot Items] 热门商品（4列卡片网格）                          │
├─────────────────────────────────────────────────────────────┤
│ [New Items] 最新上架（无限滚动加载）                          │
├─────────────────────────────────────────────────────────────┤
│ [Footer] 版权信息 | 联系方式                                 │
└─────────────────────────────────────────────────────────────┘
```

**设计要点**
- Banner 使用渐变背景 + 商品图片，营造年轻活力氛围
- 商品卡片悬停放大1.02倍，增加阴影深度
- 分类导航使用图标 + 文字组合，支持横向滚动

### 4.2 商品列表页（ItemList）

**布局结构**
```
┌─────────────────────────────────────────────────────────────┐
│ [Header] 搜索栏 + 筛选条件                                   │
├────────────┬────────────────────────────────────────────────┤
│ [Filters]  │ [Item Grid]                                    │
│ - 分类      │ ┌────┐ ┌────┐ ┌────┐ ┌────┐                │
│ - 价格区间  │ │    │ │    │ │    │ │    │                │
│ - 成色      │ └────┘ └────┘ └────┘ └────┘                │
│ - 更多筛选  │ ┌────┐ ┌────┐ ┌────┐ ┌────┐                │
│            │ │    │ │    │ │    │ │    │                │
│            │ └────┘ └────┘ └────┘ └────┘                │
├────────────┴────────────────────────────────────────────────┤
│ [Pagination] 分页器                                          │
└─────────────────────────────────────────────────────────────┘
```

**设计要点**
- 左侧筛选面板固定，右侧商品列表可滚动
- 筛选条件折叠展示，点击展开更多选项
- 商品卡片显示：图片、标题、价格、卖家、成色标签

### 4.3 商品详情页（ItemDetail）

**布局结构**
```
┌─────────────────────────────────────────────────────────────┐
│ [Header] 返回按钮 | 商品标题 | 分享按钮                        │
├─────────────────────────────┬───────────────────────────────┤
│ [Image Gallery]            │ [Item Info]                  │
│ ┌─────────────────────────┐ │ 商品标题                      │
│ │                         │ │ ¥299 <del>¥599</del>        │
│ │    主图（大图展示）      │ │ 销量 128 | 收藏 56          │
│ │                         │ │─────────────────────────    │
│ └─────────────────────────┘ │ 选择规格（暂无）              │
│ [Thumbnails] 缩略图列表      │─────────────────────────    │
│                             │ 卖家：校园小铺               │
│                             │ 发货地：本部校区             │
├─────────────────────────────┴───────────────────────────┤
│ [Tabs] 商品详情 | 评价列表（3）                            │
├─────────────────────────────────────────────────────────────┤
│ [Actions]                                                    │
│ [收藏] [加入购物车] [立即购买]                               │
└─────────────────────────────────────────────────────────────┘
```

**设计要点**
- 图片画廊支持点击放大、左右切换、手势滑动
- 价格区域使用主色调突出显示，原价使用删除线
- 底部操作栏固定在页面底部，滚动时始终可见

### 4.4 订单确认页（OrderConfirm）

**布局结构**
```
┌─────────────────────────────────────────────────────────────┐
│ [Header] 返回 | 确认订单                                     │
├─────────────────────────────────────────────────────────────┤
│ [Receiver] 收货人信息                                       │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ 👤 张三  📱 138****8888                                 ││
│ │ 📍 北京市 海淀区 中关村大街1号 清华大学学生宿舍楼A座 501   ││
│ │                               [修改]                     ││
│ └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│ [Items] 商品信息                                            │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ [图片] 商品标题                                          ││
│ │           ¥299 × 1                                      ││
│ └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│ [Payment] 支付方式                                         │
│ ○ 模拟支付（仅供演示）                                      │
├─────────────────────────────────────────────────────────────┤
│ [Summary] 订单摘要                                          │
│ 商品金额：¥299                                              │
│ 运费：包邮                                                  │
│ ─────────────────                                          │
│ 合计：¥299                                                  │
├─────────────────────────────────────────────────────────────┤
│ [Submit] [提交订单 ¥299]                                    │
└─────────────────────────────────────────────────────────────┘
```

**设计要点**
- 收货地址卡片使用圆角边框，修改按钮右上角定位
- 商品列表紧凑排列，减少页面高度
- 底部提交栏固定，使用渐变背景突出按钮

### 4.5 买家中心 - 订单列表（MyOrders）

**布局结构**
```
┌─────────────────────────────────────────────────────────────┐
│ [Header] 我的订单                                            │
├─────────────────────────────────────────────────────────────┤
│ [Tabs] 全部 | 待付款 | 待发货 | 待收货 | 已完成 | 退款中      │
├─────────────────────────────────────────────────────────────┤
│ [Order Cards]                                               │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ 订单号：ORD20260501001           状态：[待付款]          ││
│ │─────────────────────────────────────────────────────    ││
│ │ [图片] 商品标题商品标题商品标题商品标题                    ││
│ │           ¥299 × 1                    [取消订单] [支付] ││
│ └─────────────────────────────────────────────────────────┘│
│ ┌─────────────────────────────────────────────────────────┐│
│ │ 订单号：ORD20260501002           状态：[待发货]          ││
│ │ ...                                                     ││
│ └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

**设计要点**
- Tab 切换使用胶囊样式，当前选中项高亮
- 订单卡片使用卡片阴影，点击整张卡片跳转详情
- 操作按钮根据订单状态动态显示

### 4.6 卖家中心 - 商品管理（MyItems）

**布局结构**
```
┌─────────────────────────────────────────────────────────────┐
│ [Header] 我的商品                           [+发布商品]     │
├─────────────────────────────────────────────────────────────┤
│ [Stats Cards]                                               │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐               │
│ │在售 12 │ │待审核 3 │ │已售出 45│ │收入 ¥12890│            │
│ └────────┘ └────────┘ └────────┘ └────────┘               │
├─────────────────────────────────────────────────────────────┤
│ [Tabs] 全部 | 审核中 | 已上架 | 已驳回 | 已下架              │
├─────────────────────────────────────────────────────────────┤
│ [Item Cards]                                                │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ [图片] 商品标题                    [编辑] [下架] [删除] ││
│ │         ¥299 | 销量 5 | 审核状态：通过                   ││
│ └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

**设计要点**
- 顶部数据卡片使用渐变背景展示
- 商品列表支持批量操作
- 审核状态使用彩色标签区分

### 4.7 收货地址管理页（AddressList）

**布局结构**
```
┌─────────────────────────────────────────────────────────────┐
│ [Header] 收货地址                           [+新增地址]     │
├─────────────────────────────────────────────────────────────┤
│ [Address Cards]                                             │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ 默认地址                                                 ││
│ │ 👤 李四  📱 139****9999                                 ││
│ │ 上海市 徐汇区 漕河泾开发区印象城商场3楼                   ││
│ │                                    [编辑] [删除] [取消默认]││
│ └─────────────────────────────────────────────────────────┘│
│ ┌─────────────────────────────────────────────────────────┐│
│ │ 👤 王五  📱 137****7777                                 ││
│ │ 北京市 海淀区 中关村大街1号                               ││
│ │                                    [编辑] [删除] [设为默认]││
│ └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

**设计要点**
- 默认地址使用特殊边框和标签标识
- 地址卡片紧凑展示，突出关键信息
- 操作按钮悬停时显示

### 4.8 购物车页（Cart）

**布局结构**
```
┌─────────────────────────────────────────────────────────────┐
│ [Header] 购物车 (共3件商品)                                 │
├─────────────────────────────────────────────────────────────┤
│ [Seller Sections]                                           │
│ ▼ 卖家：校园小铺                                            │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ [☑] [图片] 商品标题                                      ││
│ │             ¥299 × [-][2][+]     小计：¥598             ││
│ │                              [移到收藏] [删除]            ││
│ └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│ [Summary]                                                   │
│ 已选择 2 件商品                                             │
│ 商品总价：¥598                                              │
│ 运费：包邮                                                  │
│ ─────────────────                                          │
│ 合计：¥598                                                 │
├─────────────────────────────────────────────────────────────┤
│ [结算] [去结算 ¥598]                                        │
└─────────────────────────────────────────────────────────────┘
```

**设计要点**
- 按卖家分组展示购物车商品
- 数量输入框使用步进器组件
- 底部结算栏固定，实时更新选中商品总价

## 五、技术实现要点

### 5.1 Arco Design Vue 集成

**安装依赖**

```bash
pnpm add @arco-design/web-vue
```

**全局配置**

```javascript
// main.js
import ArcoVue from '@arco-design/web-vue';
import '@arco-design/web-vue/dist/arco.css';
import {
  // 消息提示
  Message,
  // 通知提醒
  Notification,
  // 加载状态
  Loading,
} from '@arco-design/web-vue';

const app = createApp(App);

app.use(ArcoVue, {
  // 组件尺寸
  componentSize: 'medium',
});

// 提供全局方法
app.provide('message', Message);
app.provide('notification', Notification);
app.provide('loading', Loading);
```

### 5.2 样式变量覆盖

```css
/* global.css */
:root {
  /* 主色 */
  --arcoblue-6: #7C3AED;

  /* 圆角 */
  --border-radius-small: 6px;
  --border-radius-medium: 8px;
  --border-radius-large: 12px;
  --border-radius-round: 9999px;

  /* 阴影 */
  --box-shadow-1: 0 4px 20px rgba(124, 58, 237, 0.08);
  --box-shadow-2: 0 10px 30px rgba(124, 58, 237, 0.12);
}
```

### 5.3 响应式断点

```javascript
// 使用 Arco 的响应式断点
// xs: 0px
// sm: 576px
// md: 768px
// lg: 992px
// xl: 1200px
// xxl: 1400px

// 组件中使用
<a-col :xs="24" :sm="12" :md="8" :lg="6">
```

## 六、文件变更清单

### 6.1 后端新增文件

| 文件路径 | 说明 |
|----------|------|
| `backend/.../controller/AddressController.java` | 收货地址控制器 |
| `backend/.../controller/CartController.java` | 购物车控制器 |
| `backend/.../controller/ReviewController.java` | 商品评价控制器 |
| `backend/.../controller/SellerStatsController.java` | 卖家统计控制器 |
| `backend/.../service/AddressService.java` | 收货地址服务 |
| `backend/.../service/CartService.java` | 购物车服务 |
| `backend/.../service/ReviewService.java` | 商品评价服务 |
| `backend/.../service/SellerStatsService.java` | 卖家统计服务 |
| `backend/.../repository/AddressRepository.java` | 收货地址数据访问 |
| `backend/.../repository/CartRepository.java` | 购物车数据访问 |
| `backend/.../repository/ReviewRepository.java` | 评价数据访问 |

### 6.2 后端修改文件

| 文件路径 | 变更说明 |
|----------|----------|
| `backend/.../controller/OrderController.java` | 新增评价相关接口 |
| `backend/.../service/OrderService.java` | 新增订单评价状态检查 |
| `backend/src/main/resources/schema.sql` | 新增 address、cart、review 表 |
| `backend/src/main/resources/data.sql` | 初始化地址、购物车数据 |

### 6.3 前端新增文件

| 文件路径 | 说明 |
|----------|------|
| `packages/apps/portal/src/views/AddressList.vue` | 收货地址列表页 |
| `packages/apps/portal/src/views/Cart.vue` | 购物车页面 |
| `packages/apps/portal/src/views/ReviewSubmit.vue` | 评价提交页 |
| `packages/apps/portal/src/views/SellerStats.vue` | 销售统计页 |
| `packages/apps/portal/src/views/ReviewList.vue` | 评价列表页 |
| `packages/apps/portal/src/components/MiniCart.vue` | 迷你购物车组件 |
| `packages/apps/portal/src/components/AddressForm.vue` | 地址编辑表单组件 |
| `packages/apps/portal/src/components/ReviewCard.vue` | 评价卡片组件 |

### 6.4 前端修改文件

| 文件路径 | 变更说明 |
|----------|----------|
| `packages/apps/portal/src/router/index.js` | 新增路由配置 |
| `packages/apps/portal/src/services/api.js` | 新增 API 方法 |
| `packages/apps/portal/src/views/ItemDetail.vue` | 新增收藏按钮、优化UI |
| `packages/apps/portal/src/views/OrderConfirm.vue` | 新增收货地址选择 |
| `packages/apps/portal/src/views/orders/index.vue` | 新增评价入口 |
| `packages/apps/portal/src/views/seller/MyItems.vue` | 新增统计数据卡片 |
| `packages/apps/portal/src/style.css` | 全局样式变量 |
| `packages/apps/portal/package.json` | 添加 Arco Design 依赖 |
| `packages/apps/mf-shared/src/exposes/ItemCard.vue` | 优化卡片组件样式 |
| `packages/common/src/constants.js` | 新增地址相关常量 |

## 七、验收标准

### 7.1 业务功能验收

- [ ] 用户可以添加、编辑、删除收货地址
- [ ] 用户可以设置默认收货地址
- [ ] 用户可以将商品加入购物车
- [ ] 用户可以在购物车修改数量、删除商品
- [ ] 用户可以提交订单时选择收货地址
- [ ] 用户可以对已完成的订单进行评价
- [ ] 卖家可以查看销售统计数据
- [ ] 卖家可以对评价进行回复

### 7.2 UI设计验收

- [ ] 所有页面使用 Arco Design Vue 组件
- [ ] 色彩系统符合设计规范
- [ ] 页面布局响应式适配
- [ ] 卡片悬停动画正常
- [ ] 按钮交互反馈正常
- [ ] 表单验证提示友好

### 7.3 技术验收

- [ ] 后端接口返回格式统一
- [ ] 前端错误处理完善
- [ ] 加载状态显示正常
- [ ] 空状态页面设计友好
