# 校园二手交易平台

Vue3 + Java 全栈校园二手交易市场，包含买家/卖家门户及运营管理功能。

## 技术栈

- 前端：Vue 3, JavaScript, Arco Design, Vite, Pinia, Vue Router
- 后端：Spring Boot, Sa-Token, MySQL
- AI：OpenAI 兼容聊天 API

## 项目结构

```
/workspace
├── backend/              # Java 后端
├── packages/
│   ├── common/           # 共享常量和工具
│   └── apps/
│       └── campus-app/   # 单应用前端
```

## 本地环境要求

1. Node.js 20+
2. pnpm 9+
3. JDK 17+
4. Maven 3.9+
5. MySQL 8.x

## 前端安装运行

```bash
pnpm install
pnpm dev
```

前端访问地址：`http://localhost:5173`

## 后端运行

```bash
cd backend
mvn spring-boot:run
```

后端 API：`http://localhost:8080/api`

## 演示账号

- 买家：buyer / 123456
- 卖家：seller / 123456
- 运营：ops / 123456

## 主要功能

### 买家端
- 浏览商品列表
- 查看商品详情
- 加入购物车
- 下单购买
- 收藏商品
- 收货地址管理
- 我的订单

### 卖家端
- 发布商品
- 管理我的商品
- 上下架商品
- 订单处理
- 卖家数据统计

### 运营端
- 用户管理
- 商品审核
- 订单监控
- 评价管理

## 注意事项

- 默认使用内存存储快速演示
- 生产环境可使用 `schema.sql` 和 `data.sql` 迁移至 MySQL
