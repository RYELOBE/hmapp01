# 校园二手交易平台技术实现文档

## 一、系统架构设计

### 1.1 整体架构
```
┌─────────────────────────────────────────────────────────────────┐
│                        前端层                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐    │
│  │  用户门户    │  │  运营后台    │  │    公共组件库       │    │
│  │   Portal    │  │    Ops      │  │  Shared Components │    │
│  └──────┬──────┘  └──────┬──────┘  └─────────────────────┘    │
│         │                │                                      │
│         └────────┬───────┘                                      │
│                  ▼                                              │
├─────────────────────────────────────────────────────────────────┤
│                        API网关层                                │
│                ┌─────────────────────┐                          │
│                │    Spring Gateway   │                          │
│                └──────────┬──────────┘                          │
│                           ▼                                     │
├─────────────────────────────────────────────────────────────────┤
│                        后端服务层                               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │ 用户服务 │ │ 商品服务 │ │ 订单服务 │ │ 审核服务 │          │
│  │  Auth    │ │  Item    │ │  Order   │ │ Review   │          │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘          │
│       │            │            │            │                  │
│       └────────────┼────────────┼────────────┘                  │
│                    ▼                                           │
├─────────────────────────────────────────────────────────────────┤
│                        数据层                                   │
│                ┌─────────────────────┐                          │
│                │      MySQL 8.x      │                          │
│                └─────────────────────┘                          │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 核心框架 |
| Vite | 5.x | 构建工具 |
| Arco Design Vue | 2.56.x | UI组件库 |
| Pinia | 2.2.x | 状态管理 |
| Vue Router | 4.4.x | 路由管理 |
| Axios | 1.7.x | HTTP客户端 |
| Tiptap | 2.x | 富文本编辑器 |
| Masonry Layout | - | 瀑布流布局 |

### 1.3 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.x | 核心框架 |
| Java | 17+ | 编程语言 |
| Spring Data JPA | 3.x | 数据访问 |
| MySQL | 8.x | 数据库 |
| Redis | 7.x | 缓存 |
| JWT | - | 认证授权 |

---

## 二、前端目录结构优化

### 2.1 优化后的目录结构

```
campus-app/
├── src/
│   ├── components/           # 通用组件
│   │   ├── layout/          # 布局组件
│   │   │   ├── Header.vue
│   │   │   ├── Sidebar.vue
│   │   │   └── Footer.vue
│   │   ├── common/           # 通用组件
│   │   │   ├── Button.vue
│   │   │   ├── Card.vue
│   │   │   └── Input.vue
│   │   ├── form/             # 表单组件
│   │   │   ├── RichEditor.vue
│   │   │   └── Uploader.vue
│   │   └── data/             # 数据展示组件
│   │       ├── Table.vue
│   │       ├── Chart.vue
│   │       └── StatsCard.vue
│   ├── views/                # 页面视图
│   │   ├── portal/           # 用户门户
│   │   │   ├── home/
│   │   │   ├── items/
│   │   │   ├── orders/
│   │   │   ├── profile/
│   │   │   └── circle/       # 校园圈子
│   │   ├── ops/              # 运营后台
│   │   │   ├── dashboard/
│   │   │   ├── review/       # 审批工作台
│   │   │   ├── users/
│   │   │   └── items/
│   │   └── shared/           # 共享视图
│   ├── stores/               # Pinia状态管理
│   │   ├── auth.js
│   │   ├── items.js
│   │   ├── orders.js
│   │   └── reviews.js
│   ├── services/             # API服务
│   │   ├── api.js
│   │   ├── auth.js
│   │   ├── items.js
│   │   └── reviews.js
│   ├── utils/                # 工具函数
│   │   ├── http.js
│   │   ├── format.js
│   │   └── storage.js
│   ├── router/               # 路由配置
│   │   └── index.js
│   ├── styles/               # 全局样式
│   │   ├── variables.scss
│   │   └── global.scss
│   ├── App.vue
│   ├── main.js
│   └── style.css
├── index.html
├── package.json
└── vite.config.js
```

### 2.2 目录职责说明

| 目录 | 职责 | 说明 |
|------|------|------|
| components/layout | 布局组件 | 页头、侧边栏、页脚等 |
| components/common | 通用组件 | 按钮、卡片、输入框等 |
| components/form | 表单组件 | 富文本编辑器、上传组件等 |
| components/data | 数据展示 | 表格、图表、统计卡片等 |
| views/portal | 用户门户 | 面向普通用户的页面 |
| views/ops | 运营后台 | 面向运营人员的页面 |
| stores | 状态管理 | Pinia store |
| services | API服务 | 后端接口调用 |
| utils | 工具函数 | 通用工具方法 |

---

## 三、数据库模型

### 3.1 核心实体关系图

```
User 1 ─── * UserRole * ─── 1 Role
  │
  ├─ * Item (卖家)
  ├─ * Order (买家/卖家)
  ├─ * Favorite
  ├─ * Review
  └─ * CirclePost

Item 1 ─── * OrderItem
  └─ * Review

Order 1 ─── * OrderItem
  └─ * Review

CirclePost 1 ─── * CircleComment
  └─ * CircleLike
```

### 3.2 核心表结构

#### user 用户表
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码(加密) |
| nickname | VARCHAR(50) | | 昵称 |
| phone | VARCHAR(20) | | 手机号 |
| email | VARCHAR(100) | | 邮箱 |
| avatar | VARCHAR(255) | | 头像URL |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | 状态 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

#### item 商品表
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 商品ID |
| title | VARCHAR(200) | NOT NULL | 标题 |
| description | TEXT | | 描述(富文本) |
| price | DECIMAL(10,2) | NOT NULL | 价格 |
| original_price | DECIMAL(10,2) | | 原价 |
| category | VARCHAR(50) | NOT NULL | 分类 |
| condition | VARCHAR(20) | NOT NULL | 成色 |
| images | TEXT | | 图片JSON |
| seller_id | BIGINT | FOREIGN KEY | 卖家ID |
| status | VARCHAR(20) | DEFAULT 'PENDING' | 状态 |
| view_count | INT | DEFAULT 0 | 浏览数 |
| favorite_count | INT | DEFAULT 0 | 收藏数 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

#### review 评价表
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 评价ID |
| order_id | BIGINT | FOREIGN KEY | 订单ID |
| item_id | BIGINT | FOREIGN KEY | 商品ID |
| buyer_id | BIGINT | FOREIGN KEY | 买家ID |
| seller_id | BIGINT | FOREIGN KEY | 卖家ID |
| rating | INT | NOT NULL | 评分(1-5) |
| content | TEXT | | 评价内容(富文本) |
| images | TEXT | | 图片JSON |
| status | VARCHAR(20) | DEFAULT 'PENDING' | 状态 |
| reply_content | TEXT | | 卖家回复 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

#### circle_post 圈子帖子表
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 帖子ID |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| title | VARCHAR(200) | NOT NULL | 标题 |
| content | TEXT | NOT NULL | 内容(富文本) |
| images | TEXT | | 图片JSON |
| tags | VARCHAR(200) | | 标签 |
| status | VARCHAR(20) | DEFAULT 'PENDING' | 状态 |
| like_count | INT | DEFAULT 0 | 点赞数 |
| comment_count | INT | DEFAULT 0 | 评论数 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

#### message 消息表
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 消息ID |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| type | VARCHAR(50) | NOT NULL | 消息类型 |
| title | VARCHAR(200) | NOT NULL | 标题 |
| content | TEXT | | 内容 |
| status | VARCHAR(20) | DEFAULT 'UNREAD' | 状态 |
| link | VARCHAR(500) | | 跳转链接 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

---

## 四、API接口规范

### 4.1 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

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

### 4.2 认证接口

| API路径 | HTTP方法 | 说明 |
|---------|----------|------|
| /api/auth/login | POST | 用户登录 |
| /api/auth/register | POST | 用户注册 |
| /api/auth/logout | POST | 用户登出 |
| /api/auth/current | GET | 获取当前用户 |
| /api/auth/ops/login | POST | 运营登录 |

### 4.3 商品接口

| API路径 | HTTP方法 | 说明 |
|---------|----------|------|
| /api/items | GET | 商品列表(支持分页、筛选) |
| /api/items/{id} | GET | 商品详情 |
| /api/items | POST | 发布商品 |
| /api/items/{id} | PUT | 编辑商品 |
| /api/items/{id} | DELETE | 删除商品 |
| /api/items/my | GET | 我的商品 |
| /api/items/{id}/favorite | POST | 收藏/取消收藏 |

### 4.4 评价接口

| API路径 | HTTP方法 | 说明 |
|---------|----------|------|
| /api/reviews | GET | 评价列表 |
| /api/reviews/{id} | GET | 评价详情 |
| /api/reviews | POST | 提交评价 |
| /api/reviews/{id}/reply | POST | 回复评价 |

### 4.5 圈子接口

| API路径 | HTTP方法 | 说明 |
|---------|----------|------|
| /api/circle/posts | GET | 圈子帖子列表 |
| /api/circle/posts/{id} | GET | 帖子详情 |
| /api/circle/posts | POST | 发布帖子 |
| /api/circle/posts/{id} | DELETE | 删除帖子 |
| /api/circle/posts/{id}/like | POST | 点赞/取消点赞 |
| /api/circle/posts/{id}/comments | GET | 评论列表 |
| /api/circle/posts/{id}/comments | POST | 发布评论 |

### 4.6 审核接口

| API路径 | HTTP方法 | 说明 |
|---------|----------|------|
| /api/reviews/pending | GET | 待审核评价列表 |
| /api/reviews/{id}/approve | POST | 审核通过评价 |
| /api/reviews/{id}/reject | POST | 拒绝评价 |
| /api/circle/pending | GET | 待审核帖子列表 |
| /api/circle/posts/{id}/approve | POST | 审核通过帖子 |
| /api/circle/posts/{id}/reject | POST | 拒绝帖子 |
| /api/items/pending | GET | 待审核商品列表 |
| /api/items/{id}/approve | POST | 审核通过商品 |
| /api/items/{id}/reject | POST | 拒绝商品 |

### 4.7 消息接口

| API路径 | HTTP方法 | 说明 |
|---------|----------|------|
| /api/messages | GET | 消息列表 |
| /api/messages/{id} | GET | 消息详情 |
| /api/messages/{id}/read | POST | 标记为已读 |
| /api/messages/read-all | POST | 全部标记为已读 |
| /api/messages/unread-count | GET | 未读消息数量 |

---

## 五、富文本编辑器集成方案

### 5.1 技术选型

使用 **Tiptap** 作为富文本编辑器，因其轻量级、可扩展、与Vue 3完美兼容。

### 5.2 安装依赖

```bash
pnpm add @tiptap/vue-3 @tiptap/pm @tiptap/starter-kit @tiptap/extension-image @tiptap/extension-link
```

### 5.3 组件实现

```vue
<template>
  <div class="rich-editor">
    <div class="editor-toolbar">
      <button @click="editor?.chain().focus().toggleBold().run()" :class="{ active: editor?.isActive('bold') }">
        B
      </button>
      <button @click="editor?.chain().focus().toggleItalic().run()" :class="{ active: editor?.isActive('italic') }">
        I
      </button>
      <button @click="editor?.chain().focus().toggleStrike().run()" :class="{ active: editor?.isActive('strike') }">
        S
      </button>
      <div class="toolbar-divider"></div>
      <button @click="editor?.chain().focus().toggleBulletList().run()" :class="{ active: editor?.isActive('bulletList') }">
        • List
      </button>
      <button @click="editor?.chain().focus().toggleOrderedList().run()" :class="{ active: editor?.isActive('orderedList') }">
        1. List
      </button>
      <div class="toolbar-divider"></div>
      <button @click="editor?.chain().focus().toggleHeading({ level: 1 }).run()" :class="{ active: editor?.isActive('heading', { level: 1 }) }">
        H1
      </button>
      <button @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()" :class="{ active: editor?.isActive('heading', { level: 2 }) }">
        H2
      </button>
      <div class="toolbar-divider"></div>
      <button @click="handleImageUpload">
        📷
      </button>
    </div>
    <editor-content :editor="editor" class="editor-content" />
  </div>
</template>

<script setup>
import { useEditor, EditorContent } from '@tiptap/vue-3';
import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import Link from '@tiptap/extension-link';

const props = defineProps({
  modelValue: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
});

const emit = defineEmits(['update:modelValue']);

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit,
    Image.configure({
      inline: true,
    }),
    Link.configure({
      openOnClick: false,
    }),
  ],
  onUpdate: ({ editor }) => {
    emit('update:modelValue', editor.getHTML());
  },
});

function handleImageUpload() {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.onchange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    
    // 上传图片到服务器
    const formData = new FormData();
    formData.append('file', file);
    
    try {
      const res = await fetch('/api/upload/image', {
        method: 'POST',
        body: formData,
      });
      const data = await res.json();
      
      // 插入图片到编辑器
      editor.value?.chain().focus().setImage({ src: data.url }).run();
    } catch (error) {
      console.error('Image upload failed:', error);
    }
  };
  input.click();
}
</script>

<style lang="scss" scoped>
.rich-editor {
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  overflow: hidden;
  
  .editor-toolbar {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 8px 12px;
    background: #f7f8fa;
    border-bottom: 1px solid #e5e6eb;
    
    button {
      width: 32px;
      height: 32px;
      border: none;
      background: transparent;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &:hover {
        background: #e5e6eb;
      }
      
      &.active {
        background: #165DFF;
        color: white;
      }
    }
    
    .toolbar-divider {
      width: 1px;
      height: 20px;
      background: #e5e6eb;
      margin: 0 4px;
    }
  }
  
  .editor-content {
    min-height: 200px;
    padding: 12px;
    
    :deep(.ProseMirror) {
      outline: none;
      min-height: 200px;
      
      img {
        max-width: 100%;
        height: auto;
        border-radius: 4px;
      }
      
      h1 {
        font-size: 20px;
        font-weight: 700;
        margin: 16px 0 8px;
      }
      
      h2 {
        font-size: 16px;
        font-weight: 600;
        margin: 14px 0 6px;
      }
      
      p {
        margin: 8px 0;
        line-height: 1.6;
      }
      
      ul, ol {
        padding-left: 24px;
        margin: 8px 0;
      }
      
      strong {
        font-weight: 600;
      }
      
      em {
        font-style: italic;
      }
      
      s {
        text-decoration: line-through;
      }
      
      a {
        color: #165DFF;
        text-decoration: underline;
      }
    }
  }
}
</style>
```

---

## 六、审批流技术实现

### 6.1 审批状态流转

```
待提交 → 待审核 → 审核通过
                     ↓
              审核拒绝 → 修改后重新提交
```

### 6.2 审核状态枚举

| 状态值 | 显示名称 | 说明 |
|--------|----------|------|
| DRAFT | 草稿 | 未提交 |
| PENDING | 待审核 | 已提交等待审核 |
| APPROVED | 已通过 | 审核通过 |
| REJECTED | 已拒绝 | 审核拒绝 |

### 6.3 审核操作API

#### 审核通过
```json
POST /api/reviews/{id}/approve
{}
```

#### 审核拒绝
```json
POST /api/reviews/{id}/reject
{
  "reason": "拒绝原因"
}
```

### 6.4 审批工作台页面结构

```vue
<template>
  <div class="review-workspace">
    <!-- 标签切换 -->
    <a-tabs v-model:active-key="activeTab" type="card">
      <a-tab-pane key="items" title="待审核商品">
        <review-list type="items" />
      </a-tab-pane>
      <a-tab-pane key="reviews" title="待审核评价">
        <review-list type="reviews" />
      </a-tab-pane>
      <a-tab-pane key="circle" title="待审核帖子">
        <review-list type="circle" />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>
```

---

## 七、瀑布流商品管理功能

### 7.1 技术方案

使用 **CSS Grid** 实现瀑布流布局，配合 **Intersection Observer API** 实现无限滚动加载。

### 7.2 实现代码

```vue
<template>
  <div class="masonry-container">
    <div class="masonry-grid" ref="gridRef">
      <div
        v-for="item in items"
        :key="item.id"
        class="masonry-item"
        :style="{ gridRowEnd: `span ${getSpanHeight(item)}` }"
        @click="handleItemClick(item)"
      >
        <a-card :bordered="false" hoverable class="item-card">
          <template #cover>
            <img :src="item.images?.[0]" :alt="item.title" class="item-image" />
          </template>
          <div class="item-content">
            <h3 class="item-title">{{ item.title }}</h3>
            <p class="item-price">¥{{ item.price.toFixed(2) }}</p>
            <span class="item-category">{{ item.category }}</span>
          </div>
        </a-card>
      </div>
    </div>
    
    <!-- 加载更多 -->
    <div v-if="hasMore" ref="loadMoreRef" class="load-more">
      <a-spin v-if="loading" />
      <a-button v-else type="primary" @click="loadMore">加载更多</a-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const gridRef = ref(null);
const loadMoreRef = ref(null);
const items = ref([]);
const loading = ref(false);
const hasMore = ref(true);
const page = ref(1);
const pageSize = 20;

// 根据商品图片高度计算跨度
function getSpanHeight(item) {
  // 根据图片比例估算高度跨度
  const baseHeight = 200;
  const aspectRatio = item.imageAspectRatio || 1;
  const height = baseHeight / aspectRatio;
  return Math.ceil(height / 100);
}

async function loadData(isAppend = false) {
  loading.value = true;
  try {
    const res = await fetch(`/api/items?page=${page.value}&size=${pageSize}`);
    const data = await res.json();
    
    if (isAppend) {
      items.value = [...items.value, ...data.list];
    } else {
      items.value = data.list;
    }
    
    hasMore.value = items.value.length < data.total;
    page.value++;
  } catch (error) {
    console.error('Load items failed:', error);
  } finally {
    loading.value = false;
  }
}

function handleItemClick(item) {
  // 跳转到商品详情
  router.push(`/items/${item.id}`);
}

function loadMore() {
  loadData(true);
}

// Intersection Observer实现自动加载
let observer = null;

onMounted(() => {
  loadData();
  
  observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting && !loading.value && hasMore.value) {
        loadMore();
      }
    });
  }, {
    rootMargin: '100px',
  });
  
  if (loadMoreRef.value) {
    observer.observe(loadMoreRef.value);
  }
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});
</script>

<style lang="scss" scoped>
.masonry-container {
  padding: 20px;
}

.masonry-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  grid-auto-rows: 100px;
  gap: 16px;
}

.masonry-item {
  break-inside: avoid;
}

.item-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  .item-image {
    width: 100%;
    object-fit: cover;
    aspect-ratio: 1;
  }
  
  .item-content {
    padding: 12px;
    
    .item-title {
      font-size: 14px;
      font-weight: 500;
      margin: 0 0 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .item-price {
      font-size: 18px;
      font-weight: 700;
      color: #F53F3F;
      margin: 0 0 8px;
    }
    
    .item-category {
      font-size: 12px;
      color: #86909C;
      background: #F2F3F5;
      padding: 2px 8px;
      border-radius: 4px;
    }
  }
}

.load-more {
  text-align: center;
  padding: 30px 0;
}
</style>
```

---

## 八、性能优化策略

### 8.1 前端优化

#### 图片懒加载
```vue
<img loading="lazy" :src="imageUrl" />
```

#### 路由懒加载
```javascript
const Home = () => import('./views/Home.vue');
```

#### 请求防抖节流
```javascript
function debounce(fn, delay = 300) {
  let timer = null;
  return function(...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}
```

#### 缓存策略
```javascript
// 本地缓存热门商品
const cache = new Map();

async function getItems(params) {
  const key = JSON.stringify(params);
  if (cache.has(key)) {
    return cache.get(key);
  }
  
  const res = await fetch('/api/items', {
    method: 'GET',
    params,
  });
  const data = await res.json();
  
  cache.set(key, data);
  setTimeout(() => cache.delete(key), 5 * 60 * 1000); // 5分钟过期
  
  return data;
}
```

### 8.2 后端优化

#### 数据库索引优化
```sql
-- 商品表索引
CREATE INDEX idx_item_category_status ON item(category, status);
CREATE INDEX idx_item_create_time ON item(create_time);
CREATE INDEX idx_item_seller_id ON item(seller_id);

-- 订单表索引
CREATE INDEX idx_order_buyer_id ON `order`(buyer_id);
CREATE INDEX idx_order_status ON `order`(status);

-- 评价表索引
CREATE INDEX idx_review_item_id ON review(item_id);
CREATE INDEX idx_review_status ON review(status);
```

#### 查询优化
```java
// 使用JPA Specification进行动态查询
public Page<Item> findItems(ItemQuery query) {
  Specification<Item> spec = buildSpecification(query);
  return itemRepository.findAll(spec, query.getPageable());
}
```

#### 连接池配置
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

## 九、部署与集成

### 9.1 前端构建

```bash
# 开发模式
pnpm dev

# 生产构建
pnpm build

# 预览构建结果
pnpm preview
```

### 9.2 后端部署

```bash
# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/campus-marketplace.jar

# 或使用Docker
docker build -t campus-marketplace .
docker run -p 8080:8080 campus-marketplace
```

### 9.3 Nginx配置

```nginx
server {
    listen 80;
    server_name campus-marketplace.com;
    
    # 前端静态资源
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
    # 图片上传
    location /uploads {
        alias /data/uploads;
    }
}
```

---

## 十、代码安全性

### 10.1 注意事项

| 风险点 | 风险等级 | 说明 |
|--------|----------|------|
| SQL注入 | 高 | 用户输入直接拼接SQL |
| XSS攻击 | 高 | 富文本内容未过滤 |
| CSRF攻击 | 中 | 跨站请求伪造 |
| 文件上传漏洞 | 高 | 恶意文件上传 |
| 敏感信息泄露 | 高 | 密码明文存储 |
| 越权访问 | 高 | 未验证数据权限 |

### 10.2 防护措施

#### SQL注入防护
```java
// 使用JPA预编译语句
@Query("SELECT u FROM User u WHERE u.username = :username")
User findByUsername(@Param("username") String username);
```

#### XSS防护
```javascript
// 富文本内容过滤
import DOMPurify from 'dompurify';

function sanitizeContent(html) {
  return DOMPurify.sanitize(html);
}
```

#### CSRF防护
```yaml
spring:
  security:
    csrf:
      enabled: true
      token-repository:
        cookie:
          http-only: true
          secure: true
```

#### 文件上传防护
```java
// 文件类型校验
@PostMapping("/upload")
public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
  String contentType = file.getContentType();
  List<String> allowedTypes = Arrays.asList("image/jpeg", "image/png", "image/webp");
  
  if (!allowedTypes.contains(contentType)) {
    throw new IllegalArgumentException("不支持的文件类型");
  }
  
  // 文件大小校验
  if (file.getSize() > 5 * 1024 * 1024) {
    throw new IllegalArgumentException("文件大小超过限制");
  }
  
  // 处理文件...
}
```

#### 敏感信息加密
```java
// BCrypt密码加密
@Autowired
private PasswordEncoder passwordEncoder;

public User register(UserRegisterDTO dto) {
  User user = new User();
  user.setPassword(passwordEncoder.encode(dto.getPassword()));
  // ...
  return userRepository.save(user);
}
```

#### 权限验证
```java
// 方法级权限控制
@PreAuthorize("hasRole('SELLER')")
public Item createItem(ItemCreateDTO dto) {
  // ...
}
```

---

## 附录：接口响应码

| 响应码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 未授权 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |