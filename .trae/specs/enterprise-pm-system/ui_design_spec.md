# 企业级项目管理系统 - UI设计规范

## 文档信息
| 字段 | 内容 |
|---|---|
| 文档名称 | 企业级项目管理系统 - UI设计规范 |
| 作者 | Design Team |
| 创建时间 | 2026年5月 |
| 当前版本 | v1.0 |
| 适用范围 | UI设计师、前端开发团队 |

---

## 一、设计原则

### 1.1 设计理念
- **现代简约**：采用扁平化设计，减少视觉噪声
- **清晰层次**：通过色彩、间距、字体大小建立明确的信息层级
- **一致性**：统一的设计语言贯穿整个系统
- **响应式**：适配不同屏幕尺寸

### 1.2 核心价值观
- 高效：让用户快速完成任务
- 可靠：稳定的交互体验
- 专业：企业级的视觉品质
- 友好：亲切的交互反馈

---

## 二、色彩系统

### 2.1 主色调

| 颜色名称 | HEX | RGB | 用途 |
|---|---|---|---|
| 主色蓝 | #1E88E5 | rgb(30, 136, 229) | 品牌色、主按钮、强调元素 |
| 主色蓝-亮 | #42A5F5 | rgb(66, 165, 245) | 悬停状态、次要强调 |
| 主色蓝-暗 | #1565C0 | rgb(21, 101, 192) | 按下状态、深色背景 |

### 2.2 辅助色

| 颜色名称 | HEX | RGB | 用途 |
|---|---|---|---|
| 成功绿 | #43A047 | rgb(67, 160, 71) | 成功状态、确认按钮 |
| 警告橙 | #FB8C00 | rgb(251, 140, 0) | 警告提示、待处理状态 |
| 错误红 | #E53935 | rgb(229, 57, 53) | 错误提示、删除操作 |
| 信息蓝 | #1E88E5 | rgb(30, 136, 229) | 信息提示、说明文字 |

### 2.3 中性色

| 颜色名称 | HEX | RGB | 用途 |
|---|---|---|---|
| 背景白 | #FFFFFF | rgb(255, 255, 255) | 页面背景、卡片背景 |
| 背景灰 | #F5F7FA | rgb(245, 247, 250) | 次要背景、分隔区域 |
| 边框灰 | #E4E7ED | rgb(228, 231, 237) | 边框线、分割线 |
| 文本主色 | #303133 | rgb(48, 49, 51) | 主要文字、标题 |
| 文本次色 | #606266 | rgb(96, 98, 102) | 次要文字、描述 |
| 文本浅色 | #909399 | rgb(144, 147, 153) | 辅助文字、占位符 |
| 文本禁用 | #C0C4CC | rgb(192, 196, 204) | 禁用状态文字 |

### 2.4 状态色

| 状态 | 颜色 | HEX | 说明 |
|---|---|---|---|
| 待审核 | 警告橙 | #FB8C00 | 等待处理的内容 |
| 已通过 | 成功绿 | #43A047 | 审批通过的内容 |
| 已驳回 | 错误红 | #E53935 | 审批驳回的内容 |
| 已发布 | 主色蓝 | #1E88E5 | 已发布的内容 |

---

## 三、字体系统

### 3.1 字体选择

| 类型 | 字体名称 | 备用字体 | 说明 |
|---|---|---|---|
| 中文字体 | 思源黑体 | Microsoft YaHei, sans-serif | 主要中文字体 |
| 英文字体 | Roboto | Arial, sans-serif | 主要英文字体 |

### 3.2 字体大小层级

| 层级 | 字号 | 字重 | 行高 | 用途 |
|---|---|---|---|---|
| H1 | 24px | 600 | 32px | 页面主标题 |
| H2 | 20px | 600 | 28px | 模块标题 |
| H3 | 16px | 500 | 24px | 卡片标题、表单标题 |
| 正文 | 14px | 400 | 22px | 主要内容、描述文字 |
| 小正文 | 12px | 400 | 18px | 辅助说明、表格内容 |
| 微文字 | 11px | 400 | 16px | 标签、状态文字 |

### 3.3 字体样式

```css
/* 标题样式 */
.heading-1 {
  font-size: 24px;
  font-weight: 600;
  line-height: 32px;
  color: #303133;
}

.heading-2 {
  font-size: 20px;
  font-weight: 600;
  line-height: 28px;
  color: #303133;
}

.heading-3 {
  font-size: 16px;
  font-weight: 500;
  line-height: 24px;
  color: #303133;
}

/* 正文样式 */
.body {
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  color: #606266;
}

.small {
  font-size: 12px;
  font-weight: 400;
  line-height: 18px;
  color: #909399;
}

.micro {
  font-size: 11px;
  font-weight: 400;
  line-height: 16px;
  color: #C0C4CC;
}
```

---

## 四、间距系统

### 4.1 间距单位
使用 `px` 作为基础单位，建立8px倍数的间距系统。

### 4.2 间距层级

| 间距名称 | 数值 | 用途 |
|---|---|---|
| xxxs | 4px | 紧凑元素间距 |
| xxs | 8px | 内边距、小元素间距 |
| xs | 12px | 表单元素间距 |
| sm | 16px | 卡片内边距、按钮间距 |
| md | 24px | 模块间距、卡片间距 |
| lg | 32px | 页面区块间距 |
| xl | 48px | 大区域间距 |
| xxl | 64px | 页面边距 |

### 4.3 布局间距

```css
/* 通用间距类 */
.mt-xxxs { margin-top: 4px; }
.mt-xxs { margin-top: 8px; }
.mt-xs { margin-top: 12px; }
.mt-sm { margin-top: 16px; }
.mt-md { margin-top: 24px; }
.mt-lg { margin-top: 32px; }
.mt-xl { margin-top: 48px; }

.ml-sm { margin-left: 16px; }
.mr-sm { margin-right: 16px; }
.mb-sm { margin-bottom: 16px; }

.p-xxs { padding: 8px; }
.p-xs { padding: 12px; }
.p-sm { padding: 16px; }
.p-md { padding: 24px; }
```

---

## 五、组件规范

### 5.1 按钮

#### 5.1.1 按钮类型

| 类型 | 样式 | 用途 |
|---|---|---|
| 主按钮 | 实心背景，白色文字 | 主要操作、确认提交 |
| 次按钮 | 白色背景，边框，主色文字 | 次要操作、取消 |
| 文字按钮 | 无边框，主色文字 | 辅助操作、链接 |

#### 5.1.2 按钮尺寸

| 尺寸 | 高度 | 左右内边距 | 字号 |
|---|---|---|---|
| 大按钮 | 44px | 24px | 14px |
| 默认按钮 | 36px | 16px | 14px |
| 小按钮 | 28px | 12px | 12px |
| 迷你按钮 | 22px | 8px | 12px |

#### 5.1.3 按钮状态

| 状态 | 主按钮样式 | 次按钮样式 |
|---|---|---|
| 默认 | 主色蓝背景，白色文字 | 白色背景，主色蓝边框，主色蓝文字 |
| 悬停 | 主色蓝-亮背景 | 主色蓝边框变亮，文字变亮 |
| 按下 | 主色蓝-暗背景 | 边框变暗，文字变暗 |
| 禁用 | 灰色背景，浅色文字 | 灰色边框，灰色文字 |

```css
/* 主按钮 */
.btn-primary {
  background: #1E88E5;
  color: #FFFFFF;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  background: #42A5F5;
}

.btn-primary:active {
  background: #1565C0;
}

.btn-primary:disabled {
  background: #E4E7ED;
  color: #C0C4CC;
  cursor: not-allowed;
}

/* 次按钮 */
.btn-secondary {
  background: #FFFFFF;
  color: #1E88E5;
  border: 1px solid #1E88E5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  border-color: #42A5F5;
  color: #42A5F5;
}
```

### 5.2 输入框

#### 5.2.1 输入框样式

| 状态 | 样式 |
|---|---|
| 默认 | 灰色边框，浅灰背景 |
| 聚焦 | 主色蓝边框，浅蓝背景 |
| 禁用 | 浅灰背景，灰色边框，浅色文字 |
| 错误 | 红色边框，浅红背景 |

#### 5.2.2 输入框尺寸

| 尺寸 | 高度 | 内边距 | 字号 |
|---|---|---|---|
| 默认 | 36px | 0 12px | 14px |
| 小 | 28px | 0 8px | 12px |

```css
.input {
  height: 36px;
  padding: 0 12px;
  border: 1px solid #E4E7ED;
  border-radius: 4px;
  font-size: 14px;
  background: #FFFFFF;
  transition: all 0.2s ease;
}

.input:focus {
  outline: none;
  border-color: #1E88E5;
  background: #F5F9FF;
}

.input:disabled {
  background: #F5F7FA;
  border-color: #E4E7ED;
  color: #C0C4CC;
}

.input.error {
  border-color: #E53935;
  background: #FFF5F5;
}
```

### 5.3 卡片

#### 5.3.1 卡片样式

| 属性 | 值 |
|---|---|
| 背景色 | #FFFFFF |
| 边框 | 1px solid #E4E7ED |
| 圆角 | 8px |
| 阴影 | 0 2px 12px rgba(0, 0, 0, 0.08) |
| 内边距 | 16px |

#### 5.3.2 卡片类型

| 类型 | 用途 |
|---|---|
| 普通卡片 | 信息展示、列表项 |
| 悬浮卡片 | 下拉菜单、弹窗内容 |
| 统计卡片 | 数据统计展示 |

```css
.card {
  background: #FFFFFF;
  border: 1px solid #E4E7ED;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 16px;
}

.card-hover:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}
```

### 5.4 表格

#### 5.4.1 表格样式

| 属性 | 值 |
|---|---|
| 边框 | 1px solid #E4E7ED |
| 表头背景 | #F5F7FA |
| 表头文字 | #606266，14px，500 |
| 单元格内边距 | 12px |
| 行高 | 22px |

#### 5.4.2 表格状态

| 状态 | 样式 |
|---|---|
| 悬停行 | 浅蓝背景 |
| 选中行 | 主色蓝背景，白色文字 |
| 斑马纹 | 交替行浅灰背景 |

```css
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.table th {
  background: #F5F7FA;
  color: #606266;
  font-weight: 500;
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #E4E7ED;
}

.table td {
  padding: 12px;
  border-bottom: 1px solid #E4E7ED;
  color: #303133;
}

.table tr:hover {
  background: #F5F9FF;
}

.table-striped tr:nth-child(even) {
  background: #FAFBFC;
}
```

### 5.5 标签

#### 5.5.1 标签样式

| 类型 | 背景色 | 文字色 | 用途 |
|---|---|---|---|
| 主标签 | 主色蓝 | 白色 | 主要标识 |
| 成功标签 | 成功绿 | 白色 | 成功状态 |
| 警告标签 | 警告橙 | 白色 | 警告状态 |
| 错误标签 | 错误红 | 白色 | 错误状态 |
| 中性标签 | 背景灰 | 文本次色 | 普通标识 |

#### 5.5.2 标签尺寸

| 尺寸 | 高度 | 左右内边距 | 字号 |
|---|---|---|---|
| 默认 | 22px | 8px | 12px |
| 小 | 18px | 6px | 11px |

```css
.tag {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 400;
}

.tag-primary {
  background: #1E88E5;
  color: #FFFFFF;
}

.tag-success {
  background: #43A047;
  color: #FFFFFF;
}

.tag-warning {
  background: #FB8C00;
  color: #FFFFFF;
}

.tag-danger {
  background: #E53935;
  color: #FFFFFF;
}

.tag-default {
  background: #F5F7FA;
  color: #606266;
}
```

### 5.6 进度条

#### 5.6.1 进度条样式

| 属性 | 值 |
|---|---|
| 高度 | 6px |
| 背景色 | #E4E7ED |
| 圆角 | 3px |

#### 5.6.2 进度条颜色

| 类型 | 颜色 |
|---|---|
| 主进度 | 主色蓝 |
| 成功进度 | 成功绿 |
| 警告进度 | 警告橙 |

```css
.progress-bar {
  height: 6px;
  background: #E4E7ED;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar-inner {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-bar-inner.primary {
  background: #1E88E5;
}

.progress-bar-inner.success {
  background: #43A047;
}

.progress-bar-inner.warning {
  background: #FB8C00;
}
```

### 5.7 弹窗

#### 5.7.1 弹窗样式

| 属性 | 值 |
|---|---|
| 背景色 | #FFFFFF |
| 圆角 | 8px |
| 阴影 | 0 4px 24px rgba(0, 0, 0, 0.15) |
| 最小宽度 | 400px |

#### 5.7.2 弹窗结构

| 区域 | 样式 |
|---|---|
| 头部 | 16px内边距，底部边框 |
| 内容区 | 24px内边距 |
| 底部 | 16px内边距，背景浅灰 |

```css
.modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
  min-width: 400px;
  z-index: 1000;
}

.modal-header {
  padding: 16px;
  border-bottom: 1px solid #E4E7ED;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  padding: 16px;
  background: #F5F7FA;
  border-radius: 0 0 8px 8px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
```

### 5.8 导航栏

#### 5.8.1 顶部导航栏

| 属性 | 值 |
|---|---|
| 高度 | 64px |
| 背景色 | #FFFFFF |
| 阴影 | 0 2px 8px rgba(0, 0, 0, 0.06) |
| 内边距 | 0 24px |

#### 5.8.2 侧边导航栏

| 属性 | 值 |
|---|---|
| 宽度 | 200px |
| 背景色 | #FFFFFF |
| 边框 | 右侧1px solid #E4E7ED |

#### 5.8.3 导航项样式

| 状态 | 样式 |
|---|---|
| 默认 | 白色背景，文本次色 |
| 激活 | 主色蓝背景，白色文字 |
| 悬停 | 浅蓝背景 |

```css
/* 顶部导航 */
.navbar-top {
  height: 64px;
  background: #FFFFFF;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 侧边导航 */
.navbar-side {
  width: 200px;
  background: #FFFFFF;
  border-right: 1px solid #E4E7ED;
  min-height: calc(100vh - 64px);
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background: #F5F9FF;
}

.nav-item.active {
  background: #1E88E5;
  color: #FFFFFF;
}
```

### 5.9 徽章

#### 5.9.1 徽章样式

| 属性 | 值 |
|---|---|
| 最小宽度 | 20px |
| 高度 | 20px |
| 圆角 | 10px |
| 背景色 | 错误红 |
| 文字色 | 白色 |
| 字号 | 12px |

```css
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background: #E53935;
  color: #FFFFFF;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.badge-warning {
  background: #FB8C00;
}

.badge-success {
  background: #43A047;
}
```

---

## 六、布局规范

### 6.1 页面布局

```
┌─────────────────────────────────────────────────────────────┐
│  顶部导航栏 (64px)                                          │
│  ┌───────┬──────────────────┬─────────────────────────┐     │
│  │ Logo  │      搜索框       │   消息通知  │ 用户头像   │     │
│  └───────┴──────────────────┴─────────────────────────┘     │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐ ┌───────────────────────────────────┐   │
│  │   侧边导航栏     │ │           主内容区                 │   │
│  │    (200px)      │ │                                   │   │
│  │                 │ │                                   │   │
│  │  - 商品管理     │ │                                   │   │
│  │  - 评价管理     │ │                                   │   │
│  │  - 审批中心     │ │                                   │   │
│  │  - 消息中心     │ │                                   │   │
│  │  - 校园圈子     │ │                                   │   │
│  └─────────────────┘ └───────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 响应式断点

| 断点 | 屏幕宽度 | 布局调整 |
|---|---|---|
| 桌面端 | ≥ 1200px | 完整双栏布局 |
| 平板端 | 768px - 1199px | 侧边栏折叠为图标 |
| 移动端 | < 768px | 侧边栏隐藏，使用抽屉菜单 |

```css
/* 响应式断点 */
@media (max-width: 1199px) {
  .navbar-side {
    width: 64px;
  }
  
  .nav-item span {
    display: none;
  }
}

@media (max-width: 767px) {
  .navbar-side {
    position: fixed;
    left: -200px;
    z-index: 999;
  }
  
  .navbar-side.open {
    left: 0;
  }
}
```

---

## 七、图标规范

### 7.1 图标类型

| 类型 | 风格 | 尺寸 |
|---|---|---|
| 线性图标 | 2px描边，圆角2px | 16px, 20px, 24px |
| 填充图标 | 实心填充 | 16px, 20px, 24px |

### 7.2 图标命名规范

```
icon-<模块>-<功能>
示例：icon-product-add, icon-review-star, icon-approval-check
```

### 7.3 常用图标

| 图标名称 | 用途 |
|---|---|
| icon-product | 商品管理 |
| icon-review | 评价管理 |
| icon-approval | 审批中心 |
| icon-message | 消息中心 |
| icon-circle | 校园圈子 |
| icon-search | 搜索 |
| icon-add | 添加 |
| icon-edit | 编辑 |
| icon-delete | 删除 |
| icon-check | 确认 |
| icon-close | 关闭 |
| icon-star | 星星/评分 |
| icon-heart | 收藏 |
| icon-like | 点赞 |
| icon-comment | 评论 |

---

## 八、交互规范

### 8.1 按钮交互

| 状态 | 反馈 |
|---|---|
| 悬停 | 背景色变亮，光标变为手型 |
| 按下 | 背景色变暗，按钮轻微缩小 |
| 禁用 | 光标变为禁止，无交互反馈 |

### 8.2 表单交互

| 状态 | 反馈 |
|---|---|
| 聚焦 | 输入框边框变为主色蓝 |
| 输入错误 | 边框变红，显示错误提示 |
| 验证成功 | 边框变绿（可选） |

### 8.3 列表交互

| 状态 | 反馈 |
|---|---|
| 悬停 | 背景色变浅，显示操作按钮 |
| 选中 | 高亮背景 |
| 点击 | 进入详情页或展开详情 |

### 8.4 加载状态

| 场景 | 加载方式 |
|---|---|
| 页面加载 | 全屏骨架屏 |
| 列表加载 | 列表骨架屏 |
| 按钮加载 | 按钮内loading动画 |
| 局部刷新 | 旋转loading图标 |

### 8.5 提示反馈

| 类型 | 样式 | 显示位置 |
|---|---|---|
| 成功提示 | 绿色背景，对勾图标 | 顶部居中 |
| 错误提示 | 红色背景，叉号图标 | 顶部居中 |
| 警告提示 | 橙色背景，警告图标 | 顶部居中 |
| 信息提示 | 蓝色背景，信息图标 | 顶部居中 |
| 表单提示 | 红色文字 | 输入框下方 |

---

## 九、富文本编辑器规范

### 9.1 编辑器工具栏

| 工具 | 功能 |
|---|---|
| 粗体 | 文字加粗 |
| 斜体 | 文字斜体 |
| 下划线 | 文字下划线 |
| 标题 | H1-H3标题 |
| 列表 | 有序列表、无序列表 |
| 引用 | 引用块 |
| 链接 | 插入链接 |
| 图片 | 插入图片 |
| 撤销 | 撤销操作 |
| 重做 | 重做操作 |

### 9.2 编辑器样式

| 属性 | 值 |
|---|---|
| 最小高度 | 200px |
| 内边距 | 16px |
| 边框 | 1px solid #E4E7ED |
| 圆角 | 4px |
| 字号 | 14px |
| 行高 | 1.8 |

```css
.editor {
  min-height: 200px;
  padding: 16px;
  border: 1px solid #E4E7ED;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.8;
}

.editor:focus {
  outline: none;
  border-color: #1E88E5;
}
```

---

## 十、版本历史

| 版本 | 日期 | 变更内容 | 作者 |
|---|---|---|---|
| v1.0 | 2026-05-07 | 初始版本 | Design Team |