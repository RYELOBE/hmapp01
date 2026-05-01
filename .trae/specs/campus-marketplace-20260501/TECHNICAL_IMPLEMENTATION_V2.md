# 校园二手交易平台技术实现文档 v2.0

## 一、技术架构概述

### 1.1 整体技术架构

校园二手交易平台采用微前端架构设计，将前端应用拆分为多个独立的子应用，通过主容器进行统一管理。后端服务采用Spring Boot框架，提供RESTful API接口。数据库使用MySQL存储业务数据。平台整体采用前后端分离的开发模式，前端通过HTTP协议与后端交互。

**技术选型核心变更：**
- UI组件库统一采用 **Arco Design Vue**（字节跳动企业级组件库）
- 全部使用 **JavaScript**（不使用TypeScript）
- 保持Vue 3 + Vite技术栈

### 1.2 技术栈选型

**前端技术栈**
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 核心框架，组合式API |
| Vite | 5.x | 构建工具 |
| Arco Design Vue | 2.56.x | UI组件库（字节跳动） |
| Pinia | 2.2.x | 状态管理 |
| Vue Router | 4.4.x | 路由管理 |
| Axios | 1.7.x | HTTP客户端 |
| Qiankun | 1.0.x | 微前端框架 |

**后端技术栈**
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.x | 核心框架 |
| Java | 17+ | 编程语言 |
| SaToken | 1.37.x | 认证授权 |
| Spring Data JPA | 3.x | 数据访问 |
| HikariCP | 5.x | 连接池 |
| MySQL | 8.x | 数据库 |

### 1.3 项目目录结构

```
campus-marketplace/
├── backend/                           # 后端服务
│   └── src/main/java/com/campus/marketplace/
│       ├── config/                   # 配置类
│       ├── controller/               # 控制器
│       ├── service/                  # 业务服务
│       ├── repository/                # 数据访问
│       ├── entity/                   # 实体类
│       └── exception/                # 异常类
├── packages/
│   ├── apps/
│   │   ├── shell/                    # 壳工程（微前端容器）
│   │   ├── portal/                   # 门户应用（用户端）
│   │   └── ops/                      # 运营后台
│   ├── common/                       # 公共模块
│   └── mf-shared/                    # 共享组件库
```

## 二、前端微前端架构实现

### 2.1 壳工程结构

壳工程是整个前端应用的容器，负责管理子应用的注册、加载和生命周期。

**目录结构**
```
shell/
├── src/
│   ├── main.js              # 应用入口
│   ├── App.vue               # 根组件
│   ├── bootstrap.js          # 初始化脚本
│   ├── router/
│   │   └── index.js         # 路由配置
│   ├── stores/
│   │   └── auth.js          # 认证状态
│   ├── views/
│   │   ├── container/        # 容器视图
│   │   ├── LoginView.vue     # 登录视图
│   │   └── MicroView.vue     # 微应用视图
│   ├── minFrame/             # 微前端核心
│   │   ├── index.js
│   │   ├── framePinia.js     # 子应用配置
│   │   └── minFrameUtils.js  # 工具函数
│   └── components/
│       └── AiAssistant.vue   # AI助手
├── vite.config.js           # Vite配置
└── package.json
```

**微应用注册配置**
```javascript
// framePinia.js
export const subAppConfig = [
  {
    name: 'portal-app',
    entry: '//localhost:5174',
    container: '#portalContainer',
    activeRule: '/portal'
  },
  {
    name: 'ops-app', 
    entry: '//localhost:7102',
    container: '#opsContainer',
    activeRule: '/ops'
  }
];
```

### 2.2 子应用配置

**Portal应用配置**
```javascript
// vite.config.js
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import qiankun from 'vite-plugin-qiankun';
import path from 'path';

export default defineConfig({
  plugins: [
    vue(),
    qiankun('portal-app', {
      useDevMode: true
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5174,
    headers: {
      'Access-Control-Allow-Origin': '*'
    }
  }
});
```

**Ops应用配置**
```javascript
// vite.config.js
export default defineConfig({
  plugins: [
    vue(),
    qiankun('ops-app', {
      useDevMode: true
    })
  ],
  server: {
    port: 7102,
    headers: {
      'Access-Control-Allow-Origin': '*'
    }
  }
});
```

### 2.3 门户应用实现

门户应用面向普通用户，提供商品浏览、购买、卖家中心等功能。

**目录结构**
```
portal/
├── src/
│   ├── main.js                    # 应用入口
│   ├── App.vue                    # 根组件
│   ├── style.css                  # 全局样式
│   ├── router/
│   │   └── index.js              # 路由配置
│   ├── services/
│   │   └── api.js                # API服务
│   ├── views/
│   │   ├── home/                 # 首页模块
│   │   ├── item/                 # 商品模块
│   │   ├── orders/               # 订单模块
│   │   ├── seller/               # 卖家中心
│   │   ├── Layout.vue             # 布局组件
│   │   ├── Login.vue              # 登录页
│   │   ├── Register.vue           # 注册页
│   │   ├── Cart.vue               # 购物车
│   │   ├── Favorites.vue          # 收藏
│   │   ├── AddressList.vue        # 地址管理
│   │   ├── MyOrders.vue           # 我的订单
│   │   ├── ItemDetail.vue         # 商品详情
│   │   └── ReviewSubmit.vue       # 评价提交
│   └── components/
│       └── MiniCart.vue           # 迷你购物车
├── vite.config.js
└── package.json
```

### 2.4 运营后台实现

运营后台面向平台运营人员，提供商品审核、页面配置、用户管理等功能。

**目录结构**
```
ops/
├── src/
│   ├── main.js                    # 应用入口
│   ├── App.vue                    # 根组件
│   ├── style.css                  # 全局样式
│   ├── router/
│   │   └── index.js              # 路由配置
│   ├── services/
│   │   └── api.js                # API服务
│   ├── views/
│   │   ├── dashboard/             # 工作台模块
│   │   ├── reviews/              # 审核模块
│   │   ├── orders/               # 订单模块
│   │   ├── buyer/                # 需方管理
│   │   ├── vendor/               # 供方管理
│   │   ├── Layout.vue             # 布局组件
│   │   ├── Login.vue              # 登录页
│   │   ├── OpsDashboard.vue       # 运营工作台
│   │   ├── ReviewList.vue         # 审核列表
│   │   ├── ReviewDetail.vue       # 审核详情
│   │   ├── OrderMonitor.vue       # 订单监控
│   │   ├── UserManage.vue         # 用户管理
│   │   ├── VendorManage.vue       # 供方管理
│   │   ├── BuyerManage.vue        # 需方管理
│   │   ├── RoleManage.vue         # 角色管理
│   │   ├── PortalDesign.vue      # 门户设计
│   │   └── PortalDesigner.vue    # 门户设计器
│   └── views-old/                 # 旧视图兼容
├── vite.config.js
└── package.json
```

## 三、Arco Design Vue组件规范

### 3.1 组件使用规范

所有前端页面必须使用Arco Design Vue组件库，自定义组件仅在Arco组件不满足需求时使用。

**必须使用的Arco组件列表**

| 组件类型 | Arco组件 | 用途 |
|---------|---------|------|
| 按钮 | a-button | 所有按钮操作 |
| 卡片 | a-card | 信息容器 |
| 表格 | a-table | 数据列表 |
| 表单 | a-form, a-form-item | 表单输入 |
| 输入框 | a-input, a-input-number | 文本/数字输入 |
| 选择器 | a-select, a-cascader | 下拉选择 |
| 对话框 | a-modal | 模态窗口 |
| 抽屉 | a-drawer | 侧边面板 |
| 标签 | a-tag | 状态标签 |
| 徽标 | a-badge | 数量提示 |
| 消息 | Message | 操作反馈 |
| 加载 | a-spin, a-skeleton | 加载状态 |
| 空状态 | a-empty | 无数据 |
| 折叠 | a-collapse | 内容折叠 |
| 标签页 | a-tabs | 标签切换 |
| 评分 | a-rate | 评分组件 |
| 上传 | a-upload | 文件上传 |
| 头像 | a-avatar | 用户头像 |
| 分页 | a-pagination | 分页导航 |
| 面包屑 | a-breadcrumb | 导航路径 |
| 导航菜单 | a-menu | 侧边菜单 |
| 下拉菜单 | a-dropdown | 下拉菜单 |
| 工具提示 | a-tooltip | 提示信息 |
| 进度条 | a-progress | 进度展示 |
| 轮播 | a-carousel | 轮播图 |
| 栅格 | a-row, a-col | 布局 |
| 统计 | a-statistic | 数据统计 |
| 步进器 | a-input-number | 数量输入 |
| 复选框 | a-checkbox | 选择 |
| 单选框 | a-radio | 单选 |
| 开关 | a-switch | 开关 |
| 时间线 | a-timeline | 时间线 |
| 树形控件 | a-tree | 树形结构 |

### 3.2 组件使用示例

**按钮使用**
```vue
<a-button type="primary" @click="handleClick">
  主要按钮
</a-button>
<a-button type="secondary" @click="handleClick">
  次要按钮
</a-button>
<a-button status="danger" @click="handleDelete">
  删除
</a-button>
```

**表格使用**
```vue
<a-table
  :data="tableData"
  :pagination="pagination"
  :loading="loading"
  @page-change="handlePageChange"
>
  <template #columns>
    <a-table-column title="商品名称" data-index="title" />
    <a-table-column title="价格" data-index="price">
      <template #cell="{ record }">
        ¥{{ record.price }}
      </template>
    </a-table-column>
    <a-table-column title="操作">
      <template #cell="{ record }">
        <a-button size="small" @click="viewDetail(record)">
          查看
        </a-button>
      </template>
    </a-table-column>
  </template>
</a-table>
```

**表单使用**
```vue
<a-form :model="form" layout="vertical">
  <a-form-item label="商品标题" required>
    <a-input v-model="form.title" placeholder="请输入商品标题" />
  </a-form-item>
  <a-form-item label="商品价格" required>
    <a-input-number v-model="form.price" :min="0" :precision="2" />
  </a-form-item>
  <a-form-item>
    <a-button type="primary" @click="handleSubmit">提交</a-button>
  </a-form-item>
</a-form>
```

### 3.3 全局样式配置

```javascript
// main.js
import ArcoVue from '@arco-design/web-vue';
import '@arco-design/web-vue/dist/arco.css';

app.use(ArcoVue, {
  componentPrefix: 'a'  // 组件前缀
});
```

```css
/* style.css - 全局样式变量 */
:root {
  --color-primary: #165DFF;
  --color-success: #0FC6C2;
  --color-warning: #FFB026;
  --color-danger: #F53F3F;
  --border-radius: 8px;
  --spacing-unit: 16px;
}
```

## 四、后端服务架构

### 4.1 项目结构

```
backend/
├── src/main/java/com/campus/marketplace/
│   ├── MarketplaceApiApplication.java  # 启动类
│   ├── config/
│   │   ├── CorsConfig.java            # 跨域配置
│   │   ├── SaTokenConfig.java          # SaToken配置
│   │   ├── WebConfig.java              # Web配置
│   │   └── GlobalExceptionHandler.java # 全局异常处理
│   ├── controller/
│   │   ├── AuthController.java         # 认证
│   │   ├── ItemController.java         # 商品
│   │   ├── OrderController.java        # 订单
│   │   ├── AddressController.java      # 地址
│   │   ├── CartController.java         # 购物车
│   │   ├── FavoriteController.java     # 收藏
│   │   ├── ReviewController.java       # 评价
│   │   ├── OpsController.java          # 运营
│   │   ├── OpsReviewController.java    # 运营审核
│   │   ├── SellerStatsController.java  # 卖家统计
│   │   ├── AIController.java           # AI问答
│   │   ├── PortalConfigController.java # 门户配置
│   │   └── ResourceMenuController.java # 资源菜单
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── ItemService.java
│   │   ├── OrderService.java
│   │   ├── AddressService.java
│   │   ├── CartService.java
│   │   ├── FavoriteService.java
│   │   ├── ReviewService.java
│   │   ├── OpsService.java
│   │   ├── OpsAuthService.java
│   │   ├── SellerStatsService.java
│   │   ├── AIService.java
│   │   ├── PortalConfigService.java
│   │   ├── CurrentUserService.java
│   │   └── FrameService.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── ItemRepository.java
│   │   ├── OrderRepository.java
│   │   ├── AddressRepository.java
│   │   ├── CartRepository.java
│   │   ├── FavoriteRepository.java
│   │   ├── ReviewRepository.java
│   │   ├── OpsAccountRepository.java
│   │   ├── PortalConfigRepository.java
│   │   ├── ResourceMenuRepository.java
│   │   └── RoleResourceRepository.java
│   └── entity/
│       └── (JPA实体类)
├── src/main/resources/
│   ├── application.yml                  # 应用配置
│   ├── schema.sql                      # 数据库表结构
│   ├── data.sql                        # 初始数据
│   └── migration.sql                   # 数据迁移
└── pom.xml
```

### 4.2 API接口规范

**统一响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

**分页响应格式**
```json
{
  "code": 200,
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 10
  }
}
```

**认证接口**
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 用户登录 |
| /api/auth/register | POST | 用户注册 |
| /api/auth/logout | POST | 登出 |
| /api/auth/current | GET | 获取当前用户 |
| /api/auth/ops/login | POST | 运营登录 |

**商品接口**
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/items | GET | 商品列表 |
| /api/items/{id} | GET | 商品详情 |
| /api/items | POST | 发布商品 |
| /api/items/{id} | PUT | 编辑商品 |
| /api/items/{id} | DELETE | 删除商品 |
| /api/items/my | GET | 我的商品 |

**订单接口**
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/orders/mine | GET | 我的订单 |
| /api/orders/{id} | GET | 订单详情 |
| /api/orders | POST | 创建订单 |
| /api/orders/{id}/pay | POST | 模拟支付 |
| /api/orders/{id}/ship | POST | 卖家发货 |
| /api/orders/{id}/confirm | POST | 确认收货 |
| /api/orders/{id}/cancel | POST | 取消订单 |
| /api/orders/{id}/refund | POST | 申请退款 |

**运营接口**
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ops/statistics | GET | 运营统计 |
| /api/ops/orders | GET | 订单列表 |
| /api/ops/vendors | GET | 供方列表 |
| /api/ops/buyers | GET | 需方列表 |
| /api/ops/reviews | GET | 待审核列表 |
| /api/ops/reviews/{id}/approve | POST | 审核通过 |
| /api/ops/reviews/{id}/reject | POST | 审核拒绝 |

## 五、数据库设计

### 5.1 核心表结构

**user 用户表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(255) | 密码 |
| nickname | VARCHAR(50) | 昵称 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| avatar | VARCHAR(255) | 头像 |
| status | VARCHAR(20) | 状态 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**user_role 用户角色表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| role_code | VARCHAR(50) | 角色代码 |
| create_time | DATETIME | 创建时间 |

**item 商品表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(200) | 标题 |
| description | TEXT | 描述 |
| price | DECIMAL(10,2) | 价格 |
| original_price | DECIMAL(10,2) | 原价 |
| category | VARCHAR(50) | 分类 |
| condition | VARCHAR(20) | 成色 |
| images | TEXT | 图片JSON |
| seller_id | BIGINT | 卖家ID |
| status | VARCHAR(20) | 状态 |
| view_count | INT | 浏览数 |
| favorite_count | INT | 收藏数 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**order 订单表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_no | VARCHAR(50) | 订单编号 |
| item_id | BIGINT | 商品ID |
| item_title | VARCHAR(200) | 商品标题 |
| item_image | VARCHAR(255) | 商品图片 |
| price | DECIMAL(10,2) | 单价 |
| quantity | INT | 数量 |
| total_amount | DECIMAL(10,2) | 总金额 |
| buyer_id | BIGINT | 买家ID |
| seller_id | BIGINT | 卖家ID |
| status | VARCHAR(20) | 状态 |
| receiver_name | VARCHAR(50) | 收货人 |
| receiver_phone | VARCHAR(20) | 联系电话 |
| receiver_address | VARCHAR(255) | 地址 |
| express_company | VARCHAR(50) | 快递公司 |
| express_no | VARCHAR(50) | 快递单号 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 5.2 索引设计

```sql
-- 用户表索引
CREATE INDEX idx_user_username ON user(username);
CREATE INDEX idx_user_status ON user(status);

-- 角色表索引
CREATE UNIQUE INDEX idx_user_role_unique ON user_role(user_id, role_code);

-- 商品表索引
CREATE INDEX idx_item_seller ON item(seller_id);
CREATE INDEX idx_item_status ON item(status);
CREATE INDEX idx_item_category ON item(category);
CREATE INDEX idx_item_create_time ON item(create_time);

-- 订单表索引
CREATE INDEX idx_order_buyer ON order(buyer_id);
CREATE INDEX idx_order_seller ON order(seller_id);
CREATE INDEX idx_order_status ON order(status);
CREATE INDEX idx_order_create_time ON order(create_time);
```

## 六、性能优化

### 6.1 前端优化

**路由懒加载**
```javascript
// router/index.js
const routes = [
  {
    path: '/home',
    component: () => import('@/views/home/index.vue')
  }
];
```

**图片懒加载**
```vue
<a-image
  :src="imageUrl"
  loading="lazy"
  width="200"
  height="200"
/>
```

**请求优化**
```javascript
// 防抖
function debounce(fn, delay) {
  let timer = null;
  return function(...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}
```

### 6.2 后端优化

**数据库查询优化**
- 合理使用索引
- 避免SELECT *
- 使用分页查询
- 避免N+1查询

**连接池优化**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

## 七、部署配置

### 7.1 后端部署

```bash
cd backend
mvn clean package -DskipTests
java -jar target/marketplace-api.jar
```

### 7.2 前端部署

```bash
# 构建所有应用
pnpm run build

# 分别构建
cd packages/apps/portal && pnpm run build
cd packages/apps/ops && pnpm run build
```

### 7.3 Nginx配置

```nginx
server {
    listen 80;
    server_name localhost;
    
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://backend:8080;
    }
}
```

## 八、文档更新记录

| 版本 | 更新日期 | 更新内容 |
|------|----------|----------|
| v1.0 | 2025-05-01 | 初始版本 |
| v2.0 | 2025-05-01 | 更新技术栈：统一使用Arco Design Vue + JavaScript |
