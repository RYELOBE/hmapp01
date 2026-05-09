# Ops 后台全面修复计划 - 一比一还原 operation-portal 样式

## 📌 核心要求（用户明确指示）

> **直接抄** `SceneApproval/index.vue` 和 `DataCenterReport.vue` 的代码和样式，不要自己写！

---

## 🎯 参考源文件（必须完整复制）

### **1. SceneApproval 页头 + Tabs 布局**
📍 `D:\data-work\operation-portal\src\views\SceneApproval\index.vue`

#### **必须复制的 HTML 结构** (第 2-11 行):
```html
<div class="scene-review-page">
	<!-- 状态标签页 -->
	<div class="tab-bar">
		<div class="tab-bar-title">案例审核</div>
		<a-tabs v-model:active-key="activeTab" @change="handleTabChange">
			<a-tab-pane key="pending" title="待审核"></a-tab-pane>
			<a-tab-pane key="approved" title="已通过"></a-tab-pane>
			<a-tab-pane key="rejected" title="已拒绝"></a-tab-pane>
		</a-tabs>
	</div>

	<div class="scene-review-table">
		<div class="action-bar">
			<div class="search-group" style="display: flex; align-items: center; gap: 8px">
				<a-input v-model="searchName" placeholder="请输入案例名称" style="width: 200px" @press-enter="handleSearch" />
				<a-button type="primary" @click="handleSearch">搜索</a-button>
				<a-button @click="handleReset">重置</a-button>
			</div>
		</div>

		<!-- 数据表格 -->
		<a-table :columns="..." :data="tableData" class="mt-6" :pagination="pagination" :loading="loading">
			...
		</a-table>
	</div>
</div>
```

#### **必须复制的 CSS 样式** (第 357-437 行):
```css
/* ⚠️ 直接复制以下所有样式，不要修改 */
.scene-review-page {
	height: 100vh;
	background-color: #fff;
}

.scene-review-table {
	margin: 0 20px;
}

.action-bar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20px;
}

.tab-bar {
	display: flex;
	align-items: center;
	gap: 30px;
	border-bottom: 1px solid #e8e8e8;
	margin-bottom: 16px;

	&-title {
		color: #1d2129;
		font-weight: bold;
		font-size: 18px;
		padding: 0px 40px;
		border-right: 2px solid #e8e8e8;
	}

	/* Tabs 样式覆盖 - 完整复制 */
	:deep(.arco-tabs-nav::before) {
		display: none !important;
	}

	:deep(.arco-tabs-content) {
		display: none;
	}

	:deep(.arco-tabs-tab) {
		font-size: 16px;
		padding: 14px 10px;
		border: 0px;
	}

	:deep(.arco-tabs-tab-active) {
		font-size: 17px;
		font-weight: bold;
	}
}

.line {
	color: #1459fa;
}

/* 表格样式 - 完整复制 */
:deep(.arco-table) {
	width: 100%;
}

:deep(.arco-table-th-title) {
	font-weight: 700;
}

/* 分页样式 - 完整复制 */
:deep(.arco-pagination) {
	width: 100%;
	display: flex;
	justify-content: center;
	align-items: center;
	margin-top: 16px;
}

:deep(.arco-pagination-item-active) {
	background-color: #1459fa;
	color: #fff;
}

:deep(.arco-table-row-hover) {
	background-color: #f5f8ff !important;
}
```

---

### **2. DataCenterReport 表格 + 分页风格**
📍 `D:\data-work\operation-portal\src\views\YyStatisticsManage\components\DataCenter\DataCenterReport.vue`

#### **必须复制的分页 HTML 结构** (第 30-42 行):
```html
<div class="custom-pagination">
	<a-pagination
		:current="pagination.current"
		:page-size="pagination.pageSize"
		:total="pagination.total"
		:show-total="true"
		:show-jumper="true"
		:show-page-size="true"
		:page-size-options="pagination.pageSizeOptions"
		@change="handlePageChange"
		@page-size-change="handlePageSizeChange"
	/>
</div>
```

#### **必须复制的分页配置** (第 71-80 行):
```javascript
const pagination = ref({
	current: 1,
	pageSize: 10,
	total: 0,
	showTotal: true,
	showJumper: true,       // ← 新增：显示跳转
	size: 'small',
	showPageSize: true,
	pageSizeOptions: [10, 20, 30, 50, 100]  // ← 扩展选项
});
```

#### **必须复制的 CSS 样式** (第 334-445 行):
```css
/* ⚠️ 直接复制以下所有样式到 OpsUnifiedTable 组件 */

/* 表格表头样式 */
.table {
	:deep(.arco-table-th) {
		background-color: #f2f3f5;   /* 浅灰背景 */
		font-weight: 600;             /* 半粗体 */
		color: #1d2129;              /* 深色文字 */
	}

	:deep(.arco-table-body) {
		tr:first-child {             /* 首行合计行样式 */
			background-color: #fafafa;
			font-weight: 600;
			td {
				border-bottom: 2px solid #e5e6eb;
			}
		}
	}

	.custom-pagination {
		margin-top: 16px;
		display: flex;
		justify-content: flex-end;   /* 分页靠右对齐 */
	}
}

/* 分页组件完整样式 - 必须全部复制 */
:deep(.arco-pagination) {
	display: flex !important;
	justify-content: end !important;
	align-items: center;
	gap: 8px;
	background: transparent;
	width: 100%;
}

:deep(.arco-pagination-list) {
	display: flex;
}

:deep(.arco-pagination-item) {
	border: 1px solid #d9d9d9;      /* 灰色边框 */
	border-radius: 6px;               /* 圆角 */
	min-width: 32px;
	height: 32px;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 14px;
	color: #000000d9;
	background: #ffffff;
	cursor: pointer;
	transition: all 0.2s;

	&:hover {                        /* 悬停效果 */
		border-color: #1677ff;        /* 蓝色边框 */
		color: #1677ff;                /* 蓝色文字 */
		background-color: #f0f8ff;     /* 浅蓝背景 */
	}

	&.arco-pagination-item-active {  /* 当前页高亮 */
		background: #1677ff;          /* 蓝色背景 */
		border-color: #1677ff;
		color: #ffffff;               /* 白色文字 */
		box-shadow: 0 2px 4px rgba(22, 119, 255, 0.3);  /* 阴影 */
	}
}

/* 跳转输入框样式 */
:deep(.arco-pagination-jumper) {
	margin-left: 16px;
	color: #000000d9;
	display: flex;
	align-items: center;
	font-size: 14px;
	gap: 8px;

	.arco-pagination-jumper-input {
		width: 80px !important;
		height: 32px !important;
		text-align: center !important;
		border: 1px solid #d9d9d9 !important;
		border-radius: 4px !important;

		:deep(.arco-input-number-input) {
			text-align: center !important;
		}
	}
}

/* 每页条数选择器样式 */
:deep(.arco-pagination-options) {
	margin-left: 16px;

	.arco-select {
		width: 100%;

		.arco-select-view {
			height: 32px;
			border: 1px solid #d9d9d9;
			border-radius: 4px;
		}
	}
}

/* 总条数文本样式 */
:deep(.arco-pagination-total) {
	margin-right: 16px;
	color: #000000d9;
	font-size: 14px;
}
```

---

## 🔧 Bug 修复清单（按优先级）

### **P0 - 紧急修复**

#### **Bug 1：API 路径重复 `/api/api/...`**
**文件**: `src/services/notifications.js`

**问题**: `http.get('/api/notifications')` 但 baseURL 已是 `/api`
**错误信息**: `No static resource api/api/notifications/unread-count`

**修复方案**: 删除所有路径前的 `/api` 前缀
```javascript
// ❌ 错误
export function getUnreadCount() {
  return http.get('/api/notifications/unread-count')
}

// ✅ 正确
export function getUnreadCount() {
  return http.get('/notifications/unread-count')
}
```

**需要修改的函数**:
- [ ] `getNotifications()` → `/notifications`
- [ ] `getUnreadCount()` → `/notifications/unread-count`
- [ ] `getRecentNotifications()` → `/notifications/recent`
- [ ] `markAsRead(id)` → `/notifications/${id}/read`
- [ ] `markMultipleAsRead(ids)` → `/notifications/mark-read`
- [ ] `markAllAsRead()` → `/notifications/mark-all-read`
- [ ] `deleteNotification(id)` → `/notifications/${id}`
- [ ] `sendNotification()` → `/notifications/send`

---

#### **Bug 2：UserManage.vue 加载失败**
**错误信息**: `Failed to fetch dynamically imported module: UserManage.vue`

**排查步骤**:
1. [ ] 检查文件是否有语法错误（未闭合的标签、括号等）
2. [ ] 检查 import 路径是否正确（特别是相对路径 `../../../components/ops/OpsUnifiedTable.vue`）
3. [ ] 如果文件损坏，从备份重新生成或简化实现

---

#### **Bug 3：商品审核页面空白**
**文件**: `src/views/ops/reviews/index.vue`

**当前状态**: 只有空 div 和路由跳转
```vue
<template><div></div></template>
<script setup>
onMounted(() => router.replace('/ops/item-review'))
</script>
```

**修复方案**: 改为完整的商品审核页面
- 使用 SceneApproval 的 tab-bar 结构
- 标题："商品审核"
- Tabs: "待审核" / "已通过" / "已拒绝"
- 调用 `/reviews/pending` 接口获取数据

---

#### **Bug 4：订单操作 500 错误**
**错误信息**: `DELETE /api/ops/orders/9006 → 500 No static resource`

**分析**: 可能后端接口不存在或路径不匹配

**临时解决方案**: 增强错误处理
```javascript
async function deleteOrder(record) {
  try {
    await http.delete(`/ops/orders/${record.id}`)
    Message.success('订单已删除')
    loadData()
  } catch (e) {
    console.error('[Orders] Delete Error:', e.response?.data)
    // 友好提示，不暴露技术细节
    Message.error('删除失败，该订单可能已被处理或接口暂不可用')
  }
}
```

---

### **P1 - 重要功能修复**

#### **Feature 1：左侧导航栏蓝色高亮**
**文件**: `src/views/container/Layout.vue`

**当前问题**: 只匹配到 `['ops']`，子菜单不高亮
```javascript
// ❌ 当前逻辑（不够精确）
if (path.startsWith('/ops')) return ['ops'];
```

**修复方案**: 精确匹配每个页面
```javascript
const currentMenuKeys = computed(() => {
  const path = route.path;

  // OPS 子菜单精确映射
  const opsMenuMap = {
    '/ops/dashboard': 'ops-dashboard',
    '/ops/user-manage': 'ops-user-manage',
    '/ops/vendor': 'ops-vendor',           // 注意：路由指向 VendorManage.vue
    '/ops/buyer': 'ops-buyer',             // 注意：路由指向 BuyerManage.vue
    '/ops/orders': 'ops-orders',
    '/ops/items': 'ops-items',
    '/ops/item-review': 'ops-reviews',     // 商品审核
    '/ops/circle': 'ops-circle',
    '/ops/review-manage': 'ops-review-manage',
    '/ops/messages': 'ops-messages',
  };

  if (opsMenuMap[path]) return [opsMenuMap[path]];
  if (path.startsWith('/ops')) return ['ops'];

  // Portal 菜单
  if (path.startsWith('/portal/seller')) return ['seller'];
  if (path.startsWith('/portal')) return ['portal'];

  return [route.name || 'home'];
});
```

**添加高亮 CSS**:
```css
/* 左侧菜单激活态 - 蓝色高亮 */
:deep(.arco-menu-item-active),
:deep(.arco-menu-inline-item-active) {
  color: #165DFF !important;
  background-color: rgba(22, 93, 255, 0.1) !important;
  border-right: 3px solid #165DFF !important;
  font-weight: 500;
}

:deep(.arco-menu-item:hover) {
  background-color: rgba(22, 93, 255, 0.05);
}
```

---

#### **Feature 2：圈子审核布局重构**
**文件**: `src/views/ops/circle/CircleReview.vue`

**当前问题**:
- 显示"帖子/评论"两个 Tab（❌ 错误）
- 应为"待发布动态"/"已审核动态"

**新结构** (直接复制 SceneApproval 布局):
```vue
<OpsUnifiedTable title="圈子审核">
  <!-- 页头 Tabs -->
  <template #tabs>
    <a-tabs v-model:active-key="activeTab" @change="handleTabChange">
      <a-tab-pane key="pending">待发布动态</a-tab-pane>
      <a-tab-pane key="approved">已审核动态</a-tab-pane>
    </a-tabs>
  </template>

  <!-- 操作栏 -->
  <template #actions>
    <a-input-search ... />
  </template>

  <!-- 内容区 -->
  <div class="review-list">
    <div v-for="post in tableData" class="review-card">
      <!-- 动态卡片内容 -->
    </div>
  </div>
</OpsUnifiedTable>
```

**移除功能**:
- [x] 删除红点数量 badge (`pendingPostCount`, `pendingCommentCount`)
- [x] 切换 Tab 时不再请求计数接口

---

#### **Feature 3：评价审核 4 Tab 布局**
**文件**: `src/views/ops/review-manage/ReviewAudit.vue`

**新布局设计**:
```vue
<OpsUnifiedTable title="评价审核">
  <!-- 4 个 Tab 在一行 -->
  <template #tabs>
    <a-tabs v-model:active-key="activeTab">
      <a-tab-pane key="item-pending">商品评价-待审核</a-tab-pane>
      <a-tab-pane key="item-approved">商品评价-已审核</a-tab-pane>
      <a-tab-pane key="circle-pending">圈子评价-待审核</a-tab-pane>
      <a-tab-pane key="circle-approved">圈子评价-已审核</a-tab-pane>
    </a-tabs>
  </template>

  <!-- 根据 activeTab 动态加载不同数据 -->
  <div class="audit-content">
    <!-- 卡片列表展示评价内容 -->
  </div>
</OpsUnifiedTable>
```

**数据加载逻辑**:
```javascript
const apiEndpoints = {
  'item-pending':     { url: '/reviews/pending', params: { status: 'PENDING', type: 'ITEM' } },
  'item-approved':    { url: '/reviews/pending', params: { status: 'APPROVED', type: 'ITEM' } },
  'circle-pending':  { url: '/circle/comments/pending', params: { status: 'PENDING' } },
  'circle-approved': { url: '/circle/comments/pending', params: { status: 'APPROVED' } },
};

async function loadData() {
  const endpoint = apiEndpoints[activeTab.value];
  const res = await http.post(endpoint.url, { ...endpoint.params, pageNo, pageSize });
  // 处理数据...
}
```

---

## 📝 修改文件清单与执行顺序

### **Step 1：修复 P0 Bug（预计 30 分钟）**

| 序号 | 文件 | 操作 | 说明 |
|------|------|------|------|
| 1.1 | `src/services/notifications.js` | 编辑 | 删除所有 `/api` 前缀 |
| 1.2 | `src/views/ops/UserManage.vue` | 检查/修复 | 解决加载失败 |
| 1.3 | `src/views/ops/reviews/index.vue` | 重写 | 实现完整商品审核功能 |
| 1.4 | `src/views/ops/orders/index.vue` | 编辑 | 增强错误处理 |

---

### **Step 2：重构核心组件（预计 45 分钟）**

| 序号 | 文件 | 操作 | 说明 |
|------|------|------|------|
| 2.1 | `src/components/ops/OpsUnifiedTable.vue` | **完全重写** | 直接粘贴 SceneApproval + DataCenterReport 的代码 |

**具体操作**:
1. 从 `SceneApproval/index.vue` 复制：
   - `.scene-review-page` 容器
   - `.tab-bar` + `.tab-bar-title` 结构
   - `.action-bar` + `.search-group` 布局
   - 所有 `:deep()` CSS 规则（Tabs、表格、分页）

2. 从 `DataCenterReport.vue` 复制：
   - `.custom-pagination` 分页容器
   - 完整的分页样式（`.arco-pagination-*`）
   - 分页配置（showJumper: true, pageSizeOptions 扩展）

3. 合并两者样式，确保无冲突

---

### **Step 3：逐页应用新样式（预计 60 分钟）**

| 序号 | 文件 | 操作 | 说明 |
|------|------|------|------|
| 3.1 | `src/views/ops/UserManage.vue` | 更新 | 使用新的 OpsUnifiedTable |
| 3.2 | `src/views/ops/VendorManage.vue` | 更新 | 同上 |
| 3.3 | `src/views/ops/BuyerManage.vue` | 更新 | 同上 |
| 3.4 | `src/views/ops/items/ItemManage.vue` | 更新 | 同上 |
| 3.5 | `src/views/ops/orders/OrderReview.vue` | 更新 | 同上 |
| 3.6 | `src/views/ops/orders/index.vue` | 更新 | 修复分页 + 应用新样式 |
| 3.7 | `src/views/ops/circle/CircleManage.vue` | 更新 | 同上 |
| 3.8 | `src/views/ops/circle/CircleReview.vue` | **重写** | 待发布动态/已审核动态 |
| 3.9 | `src/views/ops/review-manage/ReviewManage.vue` | 更新 | 同上 |
| 3.10 | `src/views/ops/review-manage/ReviewAudit.vue` | **重写** | 4 Tab 评价审核 |
| 3.11 | `src/views/ops/messages/MessageCenter.vue` | 更新 | 修复 API 路径 + 新样式 |

---

### **Step 4：导航栏高亮（预计 15 分钟）**

| 序号 | 文件 | 操作 | 说明 |
|------|------|------|------|
| 4.1 | `src/views/container/Layout.vue` | 编辑 | 精确菜单匹配 + 蓝色高亮 CSS |

---

## ✅ 最终验收标准

### **功能验收**
- [ ] ✅ 所有页面正常加载，控制台无报错
- [ ] ✅ API 请求路径正确（`/api/notifications` 而非 `/api/api/...`）
- [ ] ✅ 左侧导航栏当前页面蓝色高亮（`#165DFF`）
- [ ] ✅ 订单删除/审核有友好错误提示（不再白屏崩溃）
- [ ] ✅ 分页切换正常工作（包括跳转、每页条数切换）

### **样式验收（像素级对比 operation-portal）**
- [ ] ✅ 页面标题：18px 粗体 + 右侧 2px 灰色分隔线
- [ ] ✅ Tabs 字号：普通 16px，激活态 17px 粗体
- [ ] ✅ Tabs 底部线：隐藏（`display: none`）
- [ ] ✅ 操作栏：flex 布局 + space-between + margin-bottom 20px
- [ ] ✅ 搜索框宽度：200px（SceneApproval 标准）
- [ ] ✅ 表头背景：`#f2f3f5`（浅灰）+ font-weight 600
- [ ] ✅ 行悬停色：`#f5f8ff`（浅蓝）
- [ ] ✅ 分页按钮：圆角 6px + 边框 #d9d9d9 + hover 变蓝
- [ ] ✅ 当前页：背景 `#1677ff` + 白字 + 阴影效果
- [ ] ✅ 分页位置：居中（SceneApproval）/ 靠右（DataCenterReport）可选
- [ ] ✅ 显示跳转输入框（showJumper: true）
- [ ] ✅ 总条数文本：黑色 `#000000d9`

### **业务逻辑验收**
- [ ] ✅ 圈子审核：只有"待发布动态"/"已审核动态"两个 Tab
- [ ] ✅ 评价审核：4 个 Tab（商品/圈子 × 待审/已审）
- [ ] ✅ 无红点数量提示（已移除）
- [ ] ✅ 商品审核：不再空白，显示完整列表

---

## 🚀 执行命令速查

```bash
# 启动开发服务器测试
cd packages/apps/campus-app
npm run dev

# 打开浏览器访问
http://localhost:7100

# 重点测试页面
/ops/user-manage        # 用户管理（检查加载）
/ops/dashboard          # 工作台
/ops/vendor             # 卖家管理
/ops/buyer              # 买家管理
/ops/items              # 商品管理
/ops/orders             # 订单监控
/ops/item-review        # 商品审核（检查空白）
/ops/circle             # 圈子管理
/ops/review-manage      # 评价管理
/ops/messages           # 消息中心（检查 API 路径）
```

---

## ⏱️ 时间估算

| 阶段 | 任务 | 预计时间 |
|------|------|----------|
| Step 1 | P0 Bug 修复 | 30 分钟 |
| Step 2 | 组件重构（复制样式） | 45 分钟 |
| Step 3 | 逐页替换 | 60 分钟 |
| Step 4 | 导航栏高亮 | 15 分钟 |
| 测试验证 | 全页面回归测试 | 30 分钟 |
| **总计** | | **约 3 小时** |

---

## 💡 关键提醒

1. **⚠️ 不要自己写样式** - 直接从参考文件复制粘贴
2. **⚠️ 保持一致性** - 所有页面使用相同的组件和样式
3. **⚠️ 先修 Bug 再改样式** - 确保功能可用后再优化体验
4. **⚠️ 每步都测试** - 修改一个文件就刷新浏览器验证
5. **⚠️ Git 提交** - 重大修改前先 commit 当前状态作为回滚点
