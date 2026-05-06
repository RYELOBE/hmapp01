# API接口文档

## 概述

本系统采用 RESTful API 设计风格，所有接口统一返回 JSON 格式。

### 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: JWT Token（除公开接口外）
- **Content-Type**: `application/json`

### 响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 认证接口

### 用户登录

**请求**
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userId": 1,
    "username": "admin",
    "role": "admin"
  }
}
```

### 用户注册

**请求**
```http
POST /auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "test123456",
  "phone": "13800138000",
  "email": "test@example.com"
}
```

### 获取用户信息

**请求**
```http
GET /auth/userinfo
Authorization: Bearer {token}
```

---

## 用户接口

### 获取用户资料

**请求**
```http
GET /user/profile
Authorization: Bearer {token}
```

### 更新用户资料

**请求**
```http
PUT /user/profile
Authorization: Bearer {token}
Content-Type: application/json

{
  "phone": "13800138001",
  "email": "new@example.com",
  "avatar": "https://example.com/avatar.jpg"
}
```

### 校园认证

**请求**
```http
POST /user/verify
Authorization: Bearer {token}
Content-Type: application/json

{
  "school": "某某大学",
  "studentId": "2021001234"
}
```

### 获取收藏列表

**请求**
```http
GET /user/favorites
Authorization: Bearer {token}
```

### 添加收藏

**请求**
```http
POST /user/favorite
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 123
}
```

---

## 分类接口

### 获取所有分类

**请求**
```http
GET /category/list
```

**响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "📚 书籍教材",
      "icon": "book",
      "sortOrder": 1,
      "productCount": 120
    }
  ]
}
```

---

## 商品接口

### 获取商品列表

**请求**
```http
GET /product/list
```

**参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认12 |
| categoryId | long | 否 | 分类ID |
| keyword | string | 否 | 搜索关键词 |
| minPrice | decimal | 否 | 最低价格 |
| maxPrice | decimal | 否 | 最高价格 |
| condition | string | 否 | 成色 |
| sortBy | string | 否 | 排序字段 |
| sortOrder | string | 否 | 排序方向 |

**响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "iPad Air 4 256G",
        "price": 2999.00,
        "originalPrice": 4999.00,
        "images": ["https://..."],
        "condition": "95新",
        "viewCount": 156,
        "seller": {
          "id": 2,
          "username": "张三",
          "avatar": "https://...",
          "creditScore": 85
        }
      }
    ],
    "total": 100,
    "page": 1,
    "size": 12
  }
}
```

### 获取商品详情

**请求**
```http
GET /product/{id}
```

### 发布商品

**请求**
```http
POST /product
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "iPad Air 4 256G",
  "description": "2023年购买，使用不到一年，功能正常...",
  "price": 2999.00,
  "originalPrice": 4999.00,
  "categoryId": 2,
  "condition": "95新",
  "images": ["base64...", "base64..."],
  "deliveryType": "都可以"
}
```

### 更新商品

**请求**
```http
PUT /product/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "更新后的标题",
  "price": 2799.00
}
```

### 删除商品

**请求**
```http
DELETE /product/{id}
Authorization: Bearer {token}
```

### 获取推荐商品

**请求**
```http
GET /product/recommend
Authorization: Bearer {token}
```

### 搜索商品

**请求**
```http
GET /product/search?keyword=iPad
```

---

## 订单接口

### 创建订单

**请求**
```http
POST /order
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 123,
  "address": "某大学某宿舍楼",
  "receiverName": "张三",
  "receiverPhone": "13800138000"
}
```

**响应**
```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "orderId": 1001,
    "orderNo": "ORD20240101123456789"
  }
}
```

### 获取订单列表

**请求**
```http
GET /order/list?status=0
Authorization: Bearer {token}
```

**参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | int | 否 | 订单状态 |
| page | int | 否 | 页码 |
| size | int | 否 | 每页数量 |

**订单状态**
| 状态值 | 说明 |
|--------|------|
| 0 | 待付款 |
| 1 | 待发货 |
| 2 | 待收货 |
| 3 | 已完成 |
| 4 | 已取消 |
| 5 | 退款申请 |
| 6 | 已退款 |

### 订单详情

**请求**
```http
GET /order/{id}
Authorization: Bearer {token}
```

### 支付订单

**请求**
```http
PUT /order/{id}/pay
Authorization: Bearer {token}
```

### 确认收货

**请求**
```http
PUT /order/{id}/confirm
Authorization: Bearer {token}
```

### 取消订单

**请求**
```http
PUT /order/{id}/cancel
Authorization: Bearer {token}
```

---

## 购物车接口

### 获取购物车列表

**请求**
```http
GET /cart/list
Authorization: Bearer {token}
```

### 添加到购物车

**请求**
```http
POST /cart
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 123
}
```

### 更新数量

**请求**
```http
PUT /cart/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "quantity": 2
}
```

### 删除购物车项

**请求**
```http
DELETE /cart/{id}
Authorization: Bearer {token}
```

---

## AI接口

### AI对话

**请求**
```http
POST /ai/chat
Content-Type: application/json

{
  "message": "推荐一些电子产品"
}
```

**响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "type": "recommend",
    "message": "根据您的需求，为您推荐以下商品：",
    "products": [
      {
        "id": 1,
        "title": "iPad Air 4",
        "price": 2999.00,
        "image": "https://..."
      }
    ]
  }
}
```

### 智能推荐

**请求**
```http
GET /ai/recommend?category=电子产品&limit=6
```

---

## 管理员接口

### 获取仪表盘数据

**请求**
```http
GET /admin/dashboard
Authorization: Bearer {token} (Admin)
```

### 获取用户列表

**请求**
```http
GET /admin/user/list
Authorization: Bearer {token} (Admin)
```

### 审核商品

**请求**
```http
PUT /admin/product/{id}/audit
Authorization: Bearer {token} (Admin)
Content-Type: application/json

{
  "status": 1,
  "rejectReason": ""
}
```

### 获取分类列表

**请求**
```http
GET /admin/category/list
Authorization: Bearer {token} (Admin)
```

### 添加分类

**请求**
```http
POST /admin/category
Authorization: Bearer {token} (Admin)
Content-Type: application/json

{
  "name": "新增分类",
  "icon": "icon",
  "sortOrder": 10
}
```

---

## 错误码对照表

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户名已存在 |
| 1002 | 手机号已注册 |
| 1003 | 邮箱已注册 |
| 1004 | 用户名或密码错误 |
| 1005 | 账户已被禁用 |
| 2001 | 商品不存在 |
| 2002 | 商品已下架 |
| 2003 | 商品审核未通过 |
| 3001 | 订单不存在 |
| 3002 | 订单状态不允许此操作 |
| 4001 | 库存不足 |
| 5001 | 无权限访问 |
