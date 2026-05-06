# 校园二手交易平台

## 项目简介

校园二手交易平台是一个面向大学生群体的闲置物品交易系统，提供商品发布、浏览、搜索、购买、评价等完整的二手交易功能，并集成AI智能助手提供商品咨询和智能推荐服务。

### 核心功能

- 🛒 **商品交易**：发布、浏览、搜索、购买二手商品
- 🔐 **校园认证**：学号/校园邮箱实名认证
- ⭐ **信用体系**：多维度信用评分系统
- 🤖 **AI助手**：智能商品推荐和交易咨询
- 📊 **运营管理**：完整后台管理系统
- 💬 **消息系统**：买卖双方即时沟通

## 技术栈

### 前端
- Vue 3 + JavaScript
- Arco Design Vue 组件库
- Vue Router 路由管理
- Pinia 状态管理
- Axios HTTP 客户端
- ECharts 数据可视化

### 后端
- Spring Boot 3.2
- Spring Security 安全框架
- MyBatis-Plus ORM框架
- MySQL 8.0 数据库
- Redis 7.0 缓存
- JWT Token 认证
- Swagger API 文档

## 项目结构

```
campus-second-hand/
├── frontend/                 # 前端项目
│   ├── src/
│   │   ├── api/             # API接口封装
│   │   ├── assets/          # 静态资源
│   │   ├── components/       # 公共组件
│   │   ├── router/          # 路由配置
│   │   ├── store/           # 状态管理
│   │   ├── utils/           # 工具函数
│   │   └── views/           # 页面组件
│   │       ├── admin/       # 运营端页面
│   │       ├── ai/          # AI助手页面
│   │       └── user/        # 用户端页面
│   ├── package.json
│   └── vite.config.js
│
├── backend/                  # 后端项目
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/campus/trade/
│   │       │       ├── config/      # 配置类
│   │       │       ├── controller/   # 控制器
│   │       │       ├── dto/         # 数据传输对象
│   │       │       ├── entity/       # 实体类
│   │       │       ├── mapper/       # 数据访问层
│   │       │       ├── service/      # 业务逻辑
│   │       │       ├── utils/        # 工具类
│   │       │       └── vo/          # 视图对象
│   │       └── resources/
│   │           └── application.yml   # 配置文件
│   └── pom.xml
│
├── sql/                      # 数据库脚本
│   └── init.sql
│
└── docs/                     # 项目文档
    ├── design/               # 设计文档
    ├── api/                  # API接口文档
    └── ui/                   # UI设计文档
```

## 快速开始

### 环境要求

- Node.js >= 16.0
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 1. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source /path/to/campus-second-hand/sql/init.sql
```

### 2. 后端启动

```bash
cd backend

# 导入Maven依赖
mvn clean install

# 启动应用
mvn spring-boot:run
```

后端服务将运行在 http://localhost:8080

### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将运行在 http://localhost:3000

### 4. 访问系统

- 用户端首页：http://localhost:3000/home
- 运营端首页：http://localhost:3000/admin/dashboard
- 默认管理员账号：`admin` / `admin123`

## 功能模块

### 用户端功能

| 模块 | 功能 |
|------|------|
| 首页 | 轮播图、分类导航、热门商品推荐、AI助手入口 |
| 商品浏览 | 分类筛选、价格筛选、关键词搜索、排序、分页 |
| 商品详情 | 图片展示、商品信息、卖家信息、收藏、购买 |
| 商品发布 | 图片上传、分类选择、定价、描述编辑 |
| 购物车 | 商品管理、数量调整、结算 |
| 订单管理 | 创建订单、支付、确认收货、评价 |
| 个人中心 | 个人信息、校园认证、收藏管理 |
| AI助手 | 商品咨询、智能推荐、交易指导 |

### 运营端功能

| 模块 | 功能 |
|------|------|
| 控制台 | 数据统计、图表分析、待审核商品 |
| 用户管理 | 用户列表、详情查看、账户管理 |
| 商品管理 | 商品审核、违规处理、详情查看 |
| 订单管理 | 订单列表、退款处理 |
| 分类管理 | 分类增删改、状态管理 |

## API接口

### 认证接口

```
POST /api/auth/register    - 用户注册
POST /api/auth/login       - 用户登录
POST /api/auth/logout      - 用户登出
GET  /api/auth/userinfo    - 获取用户信息
```

### 商品接口

```
GET  /api/product/list        - 商品列表
GET  /api/product/{id}        - 商品详情
POST /api/product             - 发布商品
PUT  /api/product/{id}        - 更新商品
DELETE /api/product/{id}      - 删除商品
GET  /api/product/recommend   - 推荐商品
GET  /api/product/search      - 搜索商品
```

### 订单接口

```
POST /api/order               - 创建订单
GET  /api/order/list          - 订单列表
GET  /api/order/{id}          - 订单详情
PUT  /api/order/{id}/pay      - 支付订单
PUT  /api/order/{id}/confirm  - 确认收货
PUT  /api/order/{id}/cancel   - 取消订单
```

### AI接口

```
POST /api/ai/chat             - AI对话
GET  /api/ai/recommend        - 智能推荐
```

详细API文档请参考 `/docs/api/` 目录

## 数据库设计

### 核心数据表

| 表名 | 说明 |
|------|------|
| user | 用户表 |
| category | 商品分类表 |
| product | 商品表 |
| order_info | 订单表 |
| cart | 购物车表 |
| favorite | 收藏表 |
| message | 消息表 |
| review | 评价表 |

详细表结构请参考 `/sql/init.sql`

## 配置说明

### 后端配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_trade
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379

jwt:
  secret: your_jwt_secret_key

campus:
  ai:
    api-key: your_api_key
    api-url: https://api.openai.com/v1/chat/completions
```

### 前端配置

编辑 `frontend/vite.config.js`：

```javascript
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 开发指南

### 前端开发

```bash
# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

### 后端开发

```bash
# 启动应用
mvn spring-boot:run

# 运行测试
mvn test

# 打包
mvn package
```

## 项目截图

### 用户端首页
![首页](docs/images/home.png)

### 商品详情页
![商品详情](docs/images/product-detail.png)

### AI助手
![AI助手](docs/images/ai-assistant.png)

### 运营端控制台
![控制台](docs/images/admin-dashboard.png)

## 技术亮点

1. **校园身份认证**：对接学校数据库，确保用户身份真实
2. **信用评分体系**：多维度评估用户信用，保障交易安全
3. **AI智能服务**：集成大语言模型，提供智能商品推荐和咨询
4. **完整交易流程**：从发布到评价，完整覆盖二手交易场景
5. **数据可视化**：ECharts图表，直观展示运营数据

## 适用场景

- 🏫 高校学生二手交易
- 📚 毕业季教材回收
- 💻 电子产品换代交易
- 🎓 校园创业实践项目

## 许可证

本项目仅供学习交流使用，禁止用于商业目的。

## 联系方式

如有问题，请提交 Issue 或联系开发者。
