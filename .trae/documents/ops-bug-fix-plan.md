# Ops后台 Bug修复计划

## 问题概述
根据用户反馈，当前存在以下关键问题：
1. 导航栏高亮失效
2. UserManage.vue动态导入失败
3. 页头样式问题（分割线未延伸到导航栏边缘）
4. 圈子审核页面仍显示红点数量
5. 评价审核页面布局错误
6. SQL语法错误（通知服务）
7. 权限访问错误

## 修复步骤

### Step 1: 修复 Layout.vue 菜单高亮映射
**文件**: `packages/apps/campus-app/src/views/container/Layout.vue`

**问题**: 当前菜单高亮映射使用2级路径，但路由是3级路径结构

**修复方案**:
```javascript
// 当前错误映射
if (path.startsWith('/ops')) return ['ops'];

// 修正为精确映射
const opsMenuMap = {
  '/ops/dashboard': 'ops-dashboard',
  '/ops/users/vendor-manage': 'ops-vendor',
  '/ops/users/buyer-manage': 'ops-buyer',
  '/ops/users/user-manage': 'ops-user-manage',
  '/ops/orders/list': 'ops-orders',
  '/ops/orders/review': 'ops-order-review',
  '/ops/items/list': 'ops-items',
  '/ops/items/review': 'ops-reviews',
  '/ops/circle/list': 'ops-circle',
  '/ops/circle/review': 'ops-circle-review',
  '/ops/review-manage/review': 'ops-review-manage',
  '/ops/review-manage/audit': 'ops-review-audit',
  '/ops/messages': 'ops-messages',
};

const getActiveMenu = (path) => {
  return opsMenuMap[path] || 'ops-dashboard';
};
```

### Step 2: 重写 UserManage.vue 解决动态导入失败
**文件**: `packages/apps/campus-app/src/views/ops/UserManage.vue`

**问题**: 组件语法错误导致动态导入失败

**修复方案**: 完全重写组件，采用与ItemManage.vue相同的结构：
- 使用正确的Vue 3 Composition API语法
- 确保所有导入路径正确
- 简化组件结构，避免复杂嵌套
- 使用OpsUnifiedTable统一表格组件

### Step 3: 修复 OpsUnifiedTable 页头样式
**文件**: `packages/apps/campus-app/src/components/ops/OpsUnifiedTable.vue`

**问题**: 页头分割线未延伸到导航栏边缘

**修复方案**:
```css
/* 页头容器 */
.tab-bar {
  margin-left: -20px;
  margin-right: -20px;
  padding-left: 20px;
  padding-right: 20px;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 16px;
  background-color: #fff;
}

/* 页头标题（可点击导航） */
.tab-bar-title {
  cursor: pointer;
  &:hover {
    color: #165DFF;
  }
}
```

### Step 4: 重构 CircleReview.vue 删除红点
**文件**: `packages/apps/campus-app/src/views/ops/CircleReview.vue`

**问题**: 圈子审核页面仍显示红点数量

**修复方案**:
1. 删除所有badge相关组件和逻辑
2. 修改Tab文本为"待发布动态"/"已审核动态"
3. 移除pendingCount状态和获取函数
4. 简化组件结构

### Step 5: 重构 ReviewAudit.vue 4 Tab 布局
**文件**: `packages/apps/campus-app/src/views/ops/ReviewAudit.vue`

**问题**: 评价审核页面布局错误

**修复方案**:
1. 移除页面标题和badge数量
2. 实现4个Tab布局：
   - 商品评价-待审核
   - 商品评价-已审核
   - 圈子评价-待审核
   - 圈子评价-已审核
3. 使用统一的OpsUnifiedTable组件
4. 保持与operation-portal一致的样式

### Step 6: 修复SQL语法错误
**文件**: `packages/apps/campus-app/src/services/notifications.js`

**问题**: 通知查询SQL语法错误

**修复方案**:
1. 检查API路径是否正确
2. 添加错误处理逻辑
3. 捕获500错误并提供友好提示

### Step 7: 验证所有修复
1. 启动开发服务器
2. 逐一测试所有页面功能
3. 验证导航高亮正常工作
4. 确认样式与operation-portal一致
5. 测试所有API调用无错误

## 实施顺序
按照上述步骤顺序实施，每完成一步立即验证效果，确保不影响其他功能。