# 校园二手交易平台 UI设计规范文档

## 一、设计语言概述

### 1.1 设计理念
- **简洁现代**：采用简约设计风格，注重信息层次
- **用户友好**：直观的交互体验，降低学习成本
- **品牌统一**：保持一致的视觉语言和交互模式

### 1.2 设计原则
- **清晰的信息层级**：通过字体大小、颜色对比建立视觉层次
- **充足的留白**：提升可读性和呼吸感
- **响应式设计**：适配不同屏幕尺寸
- **微动效反馈**：提供即时的交互反馈

---

## 二、色彩系统

### 2.1 主色调
```scss
$primary-50: #E8F3FF;
$primary-100: #D0E7FF;
$primary-200: #A7D4FF;
$primary-300: #73B8FF;
$primary-400: #4080FF;
$primary-500: #165DFF;    // 主品牌色
$primary-600: #0E42D2;
$primary-700: #092DC2;
$primary-800: #072299;
$primary-900: #051A73;
```

### 2.2 功能色
```scss
// 成功
$success-500: #00B42A;
$success-bg: #E8FFEA;

// 警告
$warning-500: #FF7D00;
$warning-bg: #FFF7E8;

// 危险
$danger-500: #F53F3F;
$danger-bg: #FFECE8;

// 信息
$info-500: #165DFF;
$info-bg: #E8F3FF;
```

### 2.3 中性色
```scss
$gray-50: #F7F8FA;
$gray-100: #F2F3F5;
$gray-200: #E5E6EB;
$gray-300: #C9CDD4;
$gray-400: #86909C;
$gray-500: #4E5969;
$gray-600: #1D2129;
```

### 2.4 背景色
```scss
$bg-page: #F5F6F7;      // 页面背景
$bg-card: #FFFFFF;      // 卡片背景
$bg-hover: #F7F8FA;     // 悬停背景
```

---

## 三、字体规范

### 3.1 字体家族
```scss
$font-family-primary: "PingFang SC", "Microsoft YaHei", "Segoe UI", sans-serif;
$font-family-mono: "SF Mono", "Consolas", monospace;
```

### 3.2 字体大小
```scss
$font-size-xs: 11px;
$font-size-sm: 12px;
$font-size-base: 14px;
$font-size-lg: 16px;
$font-size-xl: 18px;
$font-size-2xl: 20px;
$font-size-3xl: 24px;
$font-size-4xl: 28px;
$font-size-5xl: 32px;
$font-size-6xl: 40px;
```

### 3.3 字体粗细
```scss
$font-weight-light: 300;
$font-weight-normal: 400;
$font-weight-medium: 500;
$font-weight-semibold: 600;
$font-weight-bold: 700;
$font-weight-extrabold: 800;
```

### 3.4 行高
```scss
$line-height-tight: 1.25;
$line-height-normal: 1.5;
$line-height-relaxed: 1.75;
```

### 3.5 字体样式应用

| 场景 | 字体大小 | 字体粗细 | 行高 | 颜色 |
|------|----------|----------|------|------|
| 页面标题 | 24-32px | 700-800 | 1.25 | $gray-600 |
| 卡片标题 | 16-18px | 600 | 1.4 | $gray-600 |
| 正文内容 | 14px | 400 | 1.6 | $gray-500 |
| 辅助文字 | 12px | 400 | 1.5 | $gray-400 |
| 小字提示 | 11px | 400 | 1.4 | $gray-400 |

---

## 四、间距标准

### 4.1 间距单位
```scss
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;
$spacing-2xl: 48px;
$spacing-3xl: 64px;
```

### 4.2 布局间距

| 场景 | 间距值 | 说明 |
|------|--------|------|
| 页面边距 | 16-24px | 移动端16px，桌面端24px |
| 卡片间距 | 16px | 卡片之间的间距 |
| 内容间距 | 8-16px | 元素之间的垂直间距 |
| 组件内边距 | 16px | 卡片、弹窗内部边距 |
| 按钮间距 | 8-12px | 按钮之间的间距 |

---

## 五、圆角规范

```scss
$radius-sm: 4px;
$radius-md: 8px;
$radius-lg: 12px;
$radius-xl: 16px;
$radius-full: 9999px;
```

| 组件 | 圆角大小 |
|------|----------|
| 按钮、输入框 | $radius-md |
| 卡片 | $radius-lg |
| 模态框 | $radius-xl |
| 圆形头像 | $radius-full |

---

## 六、阴影规范

```scss
$shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.04);
$shadow-md: 0 4px 12px rgba(0, 0, 0, 0.06);
$shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.08);
$shadow-xl: 0 12px 40px rgba(0, 0, 0, 0.12);
```

| 场景 | 阴影类型 |
|------|----------|
| 普通卡片 | $shadow-sm |
| 悬浮卡片 | $shadow-md |
| 模态框 | $shadow-lg |
| 下拉菜单 | $shadow-md |

---

## 七、组件库设计

### 7.1 按钮组件

| 类型 | 样式 | 适用场景 |
|------|------|----------|
| 主按钮 | 主色调背景，白色文字 | 主要操作 |
| 次按钮 | 白色背景，主色调边框和文字 | 次要操作 |
| 危险按钮 | 危险色背景，白色文字 | 删除、取消等危险操作 |
| 文字按钮 | 无背景，文字颜色 | 辅助操作 |

**按钮尺寸：**
- 小号：height 32px，padding 0 16px，font-size 13px
- 中号：height 40px，padding 0 20px，font-size 14px
- 大号：height 48px，padding 0 24px，font-size 16px

### 7.2 卡片组件

```scss
.card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md;
  box-shadow: $shadow-sm;
  border: 1px solid $gray-100;
  
  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }
}
```

### 7.3 输入框组件

```scss
.input {
  height: 40px;
  padding: 0 12px;
  border-radius: $radius-md;
  border: 1px solid $gray-200;
  font-size: $font-size-base;
  background: $bg-card;
  
  &:focus {
    border-color: $primary-500;
    box-shadow: 0 0 0 3px $primary-50;
    outline: none;
  }
}
```

### 7.4 标签组件

| 类型 | 颜色 |
|------|------|
| 默认 | $gray-200背景，$gray-500文字 |
| 成功 | $success-bg背景，$success-500文字 |
| 警告 | $warning-bg背景，$warning-500文字 |
| 危险 | $danger-bg背景，$danger-500文字 |
| 信息 | $info-bg背景，$info-500文字 |

---

## 八、页面布局规则

### 8.1 布局结构

```
┌─────────────────────────────────────────┐
│              顶部导航栏                  │
├─────────────────────────────────────────┤
│  ┌─────────┐  ┌─────────────────────┐  │
│  │  侧边栏  │  │                     │  │
│  │         │  │      主内容区        │  │
│  │         │  │                     │  │
│  └─────────┘  └─────────────────────┘  │
└─────────────────────────────────────────┘
```

### 8.2 响应式断点

| 断点 | 宽度 | 布局 |
|------|------|------|
| xs | < 576px | 移动端，单列布局 |
| sm | 576px - 768px | 平板端，双列布局 |
| md | 768px - 992px | 桌面端，三列布局 |
| lg | 992px - 1200px | 大屏幕，四列布局 |
| xl | > 1200px | 超大屏幕，五列布局 |

### 8.3 首页布局

```
┌─────────────────────────────────────────┐
│         Hero区域（搜索+Banner）          │
├─────────────────────────────────────────┤
│           分类快捷入口                   │
├─────────────────────────────────────────┤
│              商品瀑布流                  │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│  │     │ │     │ │     │ │     │ │     │
│  │ 商品│ │ 商品│ │ 商品│ │ 商品│ │ 商品│
│  │ 卡片│ │ 卡片│ │ 卡片│ │ 卡片│ │ 卡片│
│  │     │ │     │ │     │ │     │ │     │
│  └─────┘ └─────┘ └─────┘ └─────┘ └─────┘
└─────────────────────────────────────────┘
```

---

## 九、交互反馈机制

### 9.1 加载状态

| 场景 | 反馈方式 |
|------|----------|
| 页面加载 | 骨架屏 |
| 列表加载 | 加载动画 + 占位符 |
| 按钮点击 | 按钮loading状态 |
| 图片加载 | 渐进式加载 + 占位图 |

### 9.2 操作反馈

| 操作 | 反馈方式 |
|------|----------|
| 成功 | 绿色提示 + 图标 |
| 失败 | 红色提示 + 图标 |
| 警告 | 橙色提示 + 图标 |
| 信息 | 蓝色提示 + 图标 |

### 9.3 状态变化

| 状态 | 视觉反馈 |
|------|----------|
| 悬停 | 阴影加深 + 上移效果 |
| 点击 | 缩放效果 |
| 选中 | 高亮边框 + 背景色变化 |
| 禁用 | 透明度降低 + 光标禁用 |

---

## 十、运营后台设计规范

### 10.1 布局风格
- 深色侧边栏 + 浅色主内容区
- 固定顶部导航 + 可折叠侧边栏
- 卡片式数据展示

### 10.2 侧边栏设计

```scss
.sidebar {
  width: 200px;
  min-height: 100vh;
  background: linear-gradient(180deg, #1D2129 0%, #2A2F36 100%);
  color: #FFFFFF;
  padding: 20px 0;
  
  .sidebar-item {
    padding: 12px 20px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: rgba(255, 255, 255, 0.08);
    }
    
    &.active {
      background: $primary-500;
      border-left: 3px solid #FFFFFF;
    }
  }
}
```

### 10.3 数据统计卡片

```scss
.stats-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  
  .stats-icon {
    width: 48px;
    height: 48px;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
  }
  
  .stats-value {
    font-size: 28px;
    font-weight: 800;
    color: $gray-600;
  }
  
  .stats-label {
    font-size: 13px;
    color: $gray-400;
  }
}
```

---

## 十一、图标规范

### 11.1 图标库
使用 Arco Design Icons 图标库

### 11.2 图标尺寸

| 场景 | 尺寸 |
|------|------|
| 按钮图标 | 16-20px |
| 导航图标 | 18-24px |
| 数据卡片图标 | 24-32px |
| 页面图标 | 32-48px |

### 11.3 图标颜色

| 状态 | 颜色 |
|------|------|
| 默认 | $gray-400 |
| 悬停 | $primary-500 |
| 激活 | $primary-500 |
| 禁用 | $gray-300 |

---

## 十二、设计规范总结

### 12.1 规范文件结构
```
design/
├── colors.scss      // 色彩变量
├── typography.scss  // 字体变量
├── spacing.scss     // 间距变量
├── radius.scss      // 圆角变量
├── shadow.scss      // 阴影变量
└── components.scss  // 组件样式
```

### 12.2 使用规范
1. 所有颜色必须从色彩系统中选取
2. 字体大小必须使用预设变量
3. 间距必须使用预设变量
4. 组件样式必须遵循组件规范

---

## 附录：设计资源

- **Logo**: 使用平台专属Logo
- **图标**: Arco Design Icons
- **插图**: 可使用插画库
- **图片资源**: 商品图片使用用户上传图片