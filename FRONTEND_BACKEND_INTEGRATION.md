# 校园二手交易平台 - 前后端联调测试文档

**版本**: 1.0
**创建日期**: 2026-05-07
**项目路径**:
- 前端: `packages/apps/campus-app`
- 后端: `backend`

---

## 目录

1. [测试环境准备](#1-测试环境准备)
2. [评价功能联调测试](#2-评价功能联调测试)
3. [圈子系统联调测试](#3-圈子系统联调测试)
4. [消息中心联调测试](#4-消息中心联调测试)
5. [审批工作台联调测试](#5-审批工作台联调测试)
6. [数据统计联调测试](#6-数据统计联调测试)
7. [核心业务流程联调](#7-核心业务流程联调)
8. [API接口清单](#8-api接口清单)
9. [测试结果记录模板](#9-测试结果记录模板)

---

## 1. 测试环境准备

### 1.1 环境要求

```yaml
前端环境:
  Node.js: >= 16.0.0
  pnpm: >= 7.0.0
  端口: 5173 (开发服务器)

后端环境:
  JDK: >= 11
  Maven: >= 3.6
  数据库: MySQL 8.0+
  端口: 8080 (应用服务器)

测试工具:
  - Chrome/Edge 最新版 (推荐)
  - Postman 或 Apifox (API测试)
  - Vue DevTools (可选)
```

### 1.2 启动步骤

```bash
# 1. 启动后端服务
cd backend
mvn spring-boot:run
# 验证: 访问 http://localhost:8080/api/health (如果有的话)

# 2. 启动前端开发服务器
cd packages/apps/campus-app
pnpm install
pnpm dev
# 验证: 浏览器访问 http://localhost:5173
```

### 1.3 测试账号

```yaml
管理员账号:
  username: admin
  password: admin123
  roles: [OPS_ADMIN, OPS_SUPER]

运营人员账号:
  username: ops01
  password: ops123
  roles: [OPS]

买家账号:
  username: buyer01
  password: buyer123
  roles: [BUYER]

卖家账号:
  username: seller01
  password: seller123
  roles: [SELLER]

买卖家双角色账号:
  username: user01
  password: user123
  roles: [BUYER, SELLER]
```

---

## 2. 评价功能联调测试

### 2.1 用户提交评价

```yaml
测试场景: 用户提交评价
优先级: P0 (高)
前置条件:
  - 用户已登录(买家角色)
  - 存在已完成的订单(orderId=1, itemId=10)
测试数据:
  orderId: 1
  itemId: 10
  rating: 5
  content: "商品质量很好，卖家发货很快，包装也很仔细！"
测试步骤:
  1. 使用买家账号登录系统
  2. 访问 /portal/review?orderId=1&itemId=10
  3. 页面应自动加载订单和商品信息
  4. 选择5星评分（点击第5颗星）
  5. 在富文本编辑器输入评价内容
  6. 可选：上传评价图片(最多3张)
  7. 点击"提交评价"按钮
预期结果:
  前端行为:
    - 显示加载状态（按钮loading）
    - POST /api/reviews 请求成功返回200
    - 显示成功提示Message.success("评价提交成功")
    - 2秒后自动跳转到 /portal/orders 或 /portal/item/{itemId}
  后端验证:
    - 数据库reviews表新增一条记录
    - status字段为 'PENDING'
    - user_id为当前登录用户ID
    - order_id和item_id正确关联
  API响应格式:
    success: true
    message: "评价提交成功"
    data:
      id: 100
      status: "PENDING"
异常场景:
  - 未登录: 跳转到登录页，带上redirect参数
  - 重复评价: 提示"您已经评价过该商品"
  - 内容为空: 表单校验提示"请输入评价内容"
  - 网络异常: 显示错误提示，不跳转
```

### 2.2 运营审核评价

```yaml
测试场景: 运营审核评价
优先级: P0 (高)
前置条件: 运营人员已登录，存在待审核评价
测试步骤:
  1. 使用运营账号(ops01)登录
  2. 访问 /ops/review
  3. 默认显示"待审核商品"标签页
  4. 切换到"待审核评价"子标签（如果有）
     或者访问专门的审核工作台 /ops/review?type=reviews
  5. 在列表中找到刚提交的评价（可通过关键词搜索）
  6. 点击"通过"或"拒绝"按钮
  7. 如果是拒绝：
     a. 弹出原因输入框
     b. 输入原因："评价内容包含不当信息"
     c. 点击确认
  8. 如果是通过：
     a. 弹出确认对话框："确定要通过该评价吗？"
     b. 点击"确认"
预期结果:
  通过操作:
    - POST /api/reviews/{id}/approve 返回200
    - 评价从列表消失（或移到"已通过"标签）
    - Tab角标数量减1（如果有角标的话）
    - 显示成功提示
    - 列表自动刷新
  拒绝操作:
    - POST /api/reviews/{id}/reject 返回200
    - request body: { reason: "评价内容包含不当信息" }
    - 评价从列表消失（或移到"已拒绝"标签）
    - 显示成功提示
  数据库验证:
    - reviews表对应记录的status更新为 APPROVED 或 REJECTED
    - 如果有reviewed_by字段，记录运营人员ID
    - reviewed_at字段更新为当前时间
批量操作:
  - 勾选多个评价
  - 点击"批量通过"按钮
  - 弹出确认对话框："确定要批量通过 N 条评价吗？"
  - 确认后所有选中评价状态更新
```

---

## 3. 圈子系统联调测试

### 3.1 发布圈子帖子

```yaml
测试场景: 发布圈子帖子
优先级: P0 (高)
前置条件: 用户已登录（买家或卖家角色）
测试数据:
  title: "测试帖子-自动化测试"
  content: "<p>这是一篇测试帖子的内容</p><p>支持<strong>富文本</strong>编辑</p>"
  tags: ["测试", "自动化"]
测试步骤:
  1. 登录系统（使用user01账号，具有BUYER或SELLER角色）
  2. 访问 /portal/circle/publish
  3. 页面加载发布表单组件
  4. 在标题输入框输入："测试帖子-自动化测试"
  5. 在富文本编辑器中输入内容（可使用工具栏格式化文本）
  6. 添加标签：
     a. 在标签输入框输入"#测试"
     b. 按Enter或点击添加
     c. 重复添加"#自动化"标签
  7. 可选：上传帖子图片（最多9张）
  8. 点击"发布"按钮
预期结果:
  前端行为:
    - 按钮显示loading状态
    - POST /api/circle/posts 请求成功返回201或200
    - Message.success("发布成功，等待审核")
    - 自动跳转到 /portal/circle（圈子首页）
  后端验证:
    - circle_posts表新增记录
    - status字段为 'PENDING'
    - user_id为当前用户
    - tags以JSON数组存储
    - content存储HTML格式的富文本
  API请求体:
    title: "测试帖子-自动化测试"
    content: "<p>这是一篇测试帖子的内容...</p>"
    tags: ["测试", "自动化"]
    images: []  // 图片URL数组
  API响应:
    success: true
    data:
      id: 50
      status: "PENDING"
      createdAt: "2026-05-07T10:30:00Z"
异常处理:
  - 标题为空: 校验提示"请输入标题"
  - 内容为空: 校验提示"请输入内容"
  - 标签超过限制: 提示"最多添加5个标签"
  - 未登录: 跳转登录页
```

### 3.2 点赞和评论帖子

```yaml
测试场景: 点赞和评论帖子
优先级: P1 (中)
前置条件: 存在已通过审核的帖子(postId=1, status='APPROVED')
测试数据:
  postId: 1
  commentContent: "很棒的分享！学习了！"
测试步骤 - 点赞:
  1. 访问 /portal/circle/1 （帖子详情页）
  2. 页面加载帖子详情、评论区、点赞数
  3. 找到点赞按钮（通常在帖子标题下方或底部）
  4. 记录当前点赞数（如：12）
  5. 点击点赞按钮
预期结果 - 点赞:
  - 按钮变为激活状态（红色/高亮）
  - POST /api/circle/posts/1/like 返回200
  - 点赞数从12变为13
  - 再次点击取消点赞，数字变回12
  - 数据库circle_post_likes表新增/删除记录

测试步骤 - 评论:
  1. 在页面底部的评论输入框输入："很棒的分享！学习了！"
  2. 点击"发送"按钮或按Enter键
预期结果 - 评论:
  - 输入框清空
  - POST /api/circle/posts/1/comments 返回200
  - 评论立即显示在评论区顶部（乐观更新或等待响应）
  - 评论数+1
  - 评论内容包括：用户头像、用户名、时间、内容
  - 数据库circle_comments表新增记录
  API请求体（评论）:
    content: "很棒的分享！学习了！"
    parentId: null  // 如果是回复评论，填父评论ID
  API响应:
    success: true
    data:
      id: 200
      content: "很棒的分享！学习了！"
      userName: "user01"
      createdAt: "2026-05-07T10:35:00Z"

测试步骤 - 回复评论:
  1. 找到某条评论下方的"回复"按钮
  2. 点击展开回复框
  3. 输入回复内容："同意楼主观点！"
  4. 发送回复
预期结果 - 回复:
  - 回复作为子评论嵌套在该评论下方
  - 显示"@原评论用户名"前缀
  - parentId正确设置
```

### 3.3 圈子首页浏览

```yaml
测试场景: 圈子首页浏览
优先级: P2 (低)
测试步骤:
  1. 访问 /portal/circle
  2. 验证页面元素：
     - 顶部导航栏（圈子标题、发布按钮）
     - 帖子列表（卡片形式或流式布局）
     - 每个帖子卡片包含：
       * 作者头像和昵称
       * 发布时间
       * 帖子标题（可点击进入详情）
       * 内容摘要（截断显示）
       * 标签（可点击筛选）
       * 点赞数、评论数统计
     - 分页控件或"加载更多"按钮
  3. 测试筛选功能：
     - 点击标签筛选（如"#学习"）
     - 验证URL参数变化 ?tag=学习
     - 验证只显示带该标签的帖子
  4. 测试排序切换（最新/最热）
预期结果:
  - GET /api/circle/posts 返回帖子列表
  - 支持分页参数：page, size
  - 支持筛选参数：tag, keyword, sort
  - 只显示status='APPROVED'的帖子
  - 滚动加载或分页正常工作
```

---

## 4. 消息中心联调测试

### 4.1 未读消息展示

```yaml
测试场景: 未读消息展示
优先级: P0 (高)
前置条件: 当前用户有未读消息（需预先创建测试数据）
测试步骤:
  1. 使用买家账号(buyer01)登录系统
  2. 观察顶部导航栏右侧的消息图标（铃铛图标）
预期结果:
  - GET /api/messages/unread-count 返回未读数N（如：3）
  - 消息图标右上角显示红色圆形角标
  - 角标内显示数字"N"
  - 如果N=0，不显示角标或隐藏
  - 角标样式：
    background-color: #F53F3F (红色)
    color: white
    font-size: 11px
    min-width: 16px
    height: 16px
    border-radius: 50%
  定时刷新:
  - 每30秒或60秒轮询一次 unread-count 接口
  - 新消息到达时角标数字更新（带动画效果）
```

### 4.2 查看和处理消息

```yaml
测试场景: 查看和处理消息
优先级: P0 (高)
测试步骤:
  1. 点击顶部导航栏的消息图标
  2. 展开下拉面板（Popover/Dropdown）
  3. 查看最近的消息列表（默认显示最近5条）
预期结果 - 消息列表:
  - GET /api/messages?pageSize=5&status=UNREAD
  - 每条消息包含：
    * 消息类型图标（订单/系统/评价等）
    * 消息标题（如："您的订单已发货"）
    * 消息摘要（如："卖家已为您发货，快递单号：..."）
    * 时间（相对时间格式，如"5分钟前"）
    * 未读标记（蓝色圆点或加粗文字）
  - 底部显示"查看全部消息"链接 → 跳转到 /portal/messages
  - 底部显示"全部标记已读"按钮

测试步骤 - 单条消息已读:
  1. 点击某条未读消息
  2. 可能的行为：
     a. 直接跳转到相关页面（如订单详情）
     b. 展开消息详情面板
预期结果:
  - POST /api/messages/{id}/read 返回200
  - 该消息的状态变为READ
  - 未读数角标数字减1
  - 消息列表中的未读标记消失

测试步骤 - 全部标记已读:
  1. 点击"全部标记已读"按钮
  2. 弹出确认对话框（可选）
预期结果:
  - POST /api/messages/read-all 返回200
  - 所有消息状态变为READ
  - 角标数字变为0并隐藏
  - 下拉面板中所有消息的未读标记消失
  - Message.success("已全部标记为已读")

消息类型及触发场景:
  订单类:
    - 订单创建成功: "您成功下单了《{商品名}》"
    - 卖家已发货: "您的订单已发货，快递单号：{expressNo}"
    - 买家已收货: "买家已确认收货，交易完成"
    - 退款申请: "买家申请退款，请及时处理"
  评价类:
    - 收到新评价: "您的商品《{商品名}》收到新评价"
    - 评价被审核: "您提交的评价已通过审核"
  系统类:
    - 商品审核通过: "您发布的《{商品名}》已通过审核"
    - 商品审核拒绝: "您发布的《{商品名}》未通过审核，原因：{reason}"
    - 系统公告: "{公告内容}"
```

### 4.3 消息中心完整页面

```yaml
测试场景: 消息中心完整页面浏览
优先级: P1 (中)
测试步骤:
  1. 访问 /portal/messages
  2. 验证页面结构：
     - 页面标题"消息中心"
     - 左侧消息分类标签（全部/订单/评价/系统）
     - 右侧消息列表区域
  3. 测试分类切换：
     - 点击"订单"标签
     - 验证只显示订单相关消息
     - URL参数变化 ?type=order
  4. 测试分页：
     - 滚动到底部或点击"加载更多"
     - 加载下一页消息
  5. 测试批量操作：
     - 勾选多条消息
     - 点击"批量删除"或"批量已读"
预期结果:
  - GET /api/messages?page=1&size=20&type=all
  - 分类筛选正常
  - 分页功能正常
  - 已读/未读状态视觉区分明显
  - 空状态提示友好（"暂无消息"）
```

---

## 5. 审批工作台联调测试

### 5.1 商品审核流程

```yaml
测试场景: 商品审核流程
优先级: P0 (高)
前置条件: 
  - 运营人员已登录
  - 存在待审核商品（需预先创建测试数据或使用真实数据）
测试步骤:
  1. 使用运营账号(ops01)登录
  2. 访问 /ops/review
  3. 验证默认显示"待审核商品"标签页
  4. 观察待审核商品列表：
     - 商品缩略图
     - 商品标题
     - 价格
     - 分类标签
     - 成色标签
     - 提交时间
     - 卖家信息
  5. 点击某个商品的"审核"或"查看"按钮
预期结果 - 审核详情抽屉/弹窗:
  - 右侧滑出Drawer（宽度720px）
  - 标题："商品审核详情"
  - 显示完整商品信息：
    * 商品图片轮播（支持多图查看）
    * 商品标题、价格、原价
    * 分类、成色、数量
    * 详细描述（HTML渲染）
    * 卖家信息（昵称、注册时间、信誉度）
    * 提交时间
  - 底部操作按钮区：
    * "通过"按钮（主按钮，蓝色）
    * "拒绝"按钮（次要按钮）

测试步骤 - 通过商品:
  1. 在审核详情中点击"通过"按钮
  2. Drawer footer显示确认按钮
  3. 点击确认
预期结果 - 通过:
  - POST /api/items/{id}/approve 返回200
  - Drawer关闭
  - 商品从待审核列表消失
  - Message.success("商品已通过审核")
  - 列表自动刷新
  - 统计数据更新（待审核数-1）
  - 数据库items表：
    review_status: 'APPROVED'
    approved_by: 当前运营ID
    approved_at: 当前时间

测试步骤 - 拒绝商品:
  1. 在审核详情中点击"拒绝"按钮
  2. 弹出原因输入Modal
  3. 输入拒绝原因："图片不清晰，无法看清商品细节"
  4. 字符限制：最少5字符，最多200字符
  5. 点击"确认拒绝"
预期结果 - 拒绝:
  - POST /api/items/{id}/reject 返回200
  - Request Body: { reason: "图片不清晰，无法看清商品细节" }
  - Modal关闭 + Drawer关闭
  - 商品从列表消失
  - Message.success("已拒绝该商品")
  - 数据库items表：
    review_status: 'REJECTED'
    reject_reason: "图片不清晰，无法看清商品细节"
    rejected_by: 当前运营ID
    rejected_at: 当前时间

批量审核:
  - 勾选多个商品（checkbox多选）
  - 显示批量操作栏："已选择 N 项"
  - 点击"批量通过"
  - 确认对话框："确定要批量通过 N 件商品吗？"
  - 全部处理成功后刷新列表
```

### 5.2 评价审核流程

```yaml
测试场景: 评价审核流程（在审批工作台中）
优先级: P0 (高)
测试步骤:
  1. 访问 /ops/review
  2. 切换到"待审核评价"标签
  3. 查看待审核评价列表
  4. 每条评价显示：
     - 评价者头像和昵称
     - 星级评分（可视化显示★ ★ ★ ★ ★）
     - 评价内容（HTML渲染，可能含图片）
     - 关联商品名称（可点击跳转）
     - 评价时间
  5. 点击"通过"或"拒绝"
预期结果:
  - 与商品审核类似的流程
  - POST /api/reviews/{id}/approve 或 /reject
  - 评价状态更新
  - 商品的平均评分重新计算（如果通过）
```

### 5.3 审批统计数据

```yaml
测试场景: 审批工作台统计数据
优先级: P2 (低)
测试步骤:
  1. 访问 /ops/review
  2. 观察页面顶部的统计卡片区域
预期结果:
  - 显示关键指标卡片（通常4个）：
    * 待审核商品数: 15
    * 今日已审核: 28
    * 通过率: 85%
    * 平均审核时长: 2.5小时
  - 数据来源: GET /api/ops/stats 或 /api/ops/review/stats
  - 数字带有动画效果（从0增长到实际值）
  - 卡片颜色区分（蓝/绿/橙/红）
  - 可点击跳转到对应筛选列表
```

---

## 6. 数据统计联调测试

### 6.1 Dashboard数据加载

```yaml
测试场景: Dashboard数据加载
优先级: P0 (高)
前置条件: 运营人员已登录
测试步骤:
  1. 使用运营账号登录
  2. 访问 /ops/dashboard
  3. 等待页面完全加载（观察loading状态）
  4. 验证页面布局：
     - 顶部：统计卡片行（通常8个或4个关键指标）
     - 中部左侧：图表区域（折线图/柱状图）
     - 中部右侧：排行榜或列表
     - 底部：近期活动动态
预期结果 - 统计卡片:
  - GET /api/ops/stats 返回统计数据
  - 8个统计卡片正确显示数值（示例）：
    1. 总用户数: 1,234
    2. 今日新增用户: 56
    3. 总商品数: 5,678
    4. 待审核商品: 23
    5. 总订单数: 9,012
    6. 今日订单数: 145
    7. 交易总额: ¥123,456.78
    8. 退款率: 2.3%
  - 数值格式正确：
    * 大数字使用千分位分隔符（1,234）
    * 金额保留2位小数（¥123,456.78）
    * 百分比保留1位小数（2.3%）
  - 卡片样式：
    * 不同指标不同主题色
    * hover效果（阴影提升+轻微上移）
    * 图标+标题+数值的垂直布局

预期结果 - 图表区域:
  - 近30天订单趋势折线图
    X轴：日期
    Y轴：订单数
    数据：GET /api/ops/stats/orders-trend?days=30
  - 商品分类分布饼图/环形图
    数据：GET /api/ops/stats/category-distribution
  - 图表交互：
    * 鼠标悬停显示tooltip（具体数值）
    * 图例可点击显示/隐藏系列
    * 支持缩放（如果数据点多）

预期结果 - 排行榜:
  - 热销商品TOP10
  - 活跃卖家TOP10
  - 数据表格形式，可点击跳转详情
性能要求:
  - 首屏加载时间 < 3秒
  - 所有API并行请求（Promise.all或类似）
  - 图表使用懒加载或虚拟滚动（数据量大时）
  - 错误降级：某个API失败不影响其他模块显示
```

### 6.2 各管理页面统计数据

```yaml
测试场景: 买家管理页面统计
优先级: P1 (中)
测试步骤:
  1. 访问 /ops/buyer-manage
  2. 观察页面顶部的统计卡片（3个）
预期结果:
  - 买家总数: 500
  - 今日新增: 12
  - 活跃买家（近30天）: 180
  - 数据来源: GET /api/ops/buyers?stats=true

测试场景: 卖家管理页面统计
优先级: P1 (中)
测试步骤:
  1. 访问 /ops/vendor-manage
  2. 观察统计卡片（4个）
预期结果:
  - 卖家总数: 120
  - 今日新增: 5
  - 在售商品数: 1,500
  - 待审核商品: 23

测试场景: 订单监控页面统计
优先级: P1 (中)
测试步骤:
  1. 访问 /ops/orders
  2. 观察统计卡片（4个）
预期结果:
  - 总订单数: 9,012
  - 今日新增: 145
  - 交易总额: ¥45,678.90
  - 退款订单数: 28
```

---

## 7. 核心业务流程联调

### 7.1 完整购买流程

```yaml
测试场景: 从浏览到购买的完整流程
优先级: P0 (高)
参与角色: 买家(buyer01)、卖家(seller01)
测试步骤:
  【买家操作】
  1. 买家登录系统
  2. 访问首页 /portal/home
  3. 浏览商品列表，找到感兴趣的商品
  4. 点击商品进入详情页 /portal/item/{itemId}
  5. 查看商品信息、图片、评价
  6. 点击"立即购买"按钮
  7. 跳转到订单确认页 /portal/orders/confirm/{itemId}
  8. 选择收货地址（如果没有则添加新地址）
  9. 确认订单信息（商品、金额、地址）
  10. 点击"提交订单"按钮
  11. 订单创建成功，跳转到支付页面（模拟支付）
  12. 点击"确认支付"
  13. 订单状态变为"已付款"

  【卖家操作】
  14. 卖家登录系统
  15. 进入"我的订单" /portal/orders?tab=PAID
  16. 找到刚才的订单
  17. 点击"发货"按钮
  18. 填写快递公司：顺丰速运
  19. 填写快递单号：SF1234567890
  20. 确认发货

  【买家操作】
  21. 买家回到订单列表
  22. 查看订单状态变为"已发货"
  23. 点击"确认收货"按钮
  24. 确认对话框："确认已收到商品吗？"
  25. 点击确认

  【买家操作 - 评价】
  26. 订单完成后，出现"去评价"按钮
  27. 点击进入评价页面
  28. 提交评价（参考2.1节）

  【运营操作】（可选）
  29. 运营人员在后台审核评价（参考2.2节）

预期结果 - 各阶段API调用:
  步骤10 - 创建订单:
    POST /api/orders
    body: { itemId, quantity, receiverName, receiverPhone, receiverAddress }
    response: { orderId, orderNo, status: "PENDING_PAYMENT" }

  步骤12 - 支付订单:
    POST /api/orders/{orderId}/pay
    response: { status: "PAID" }

  步骤20 - 发货:
    POST /api/orders/{orderId}/ship
    body: { expressCompany: "顺丰速运", expressNo: "SF1234567890" }
    response: { status: "SHIPPED" }

  步骤25 - 确认收货:
    POST /api/orders/{orderId}/confirm
    response: { status: "COMPLETED" }

数据一致性检查:
  - items表的sold_count增加
  - orders表状态流转正确
  - inventory库存减少（如果有库存管理）
  - 卖家的销售额统计更新
  - 买家的消费金额统计更新
```

### 7.2 商品发布审核流程

```yaml
测试场景: 从发布到上架的完整流程
优先级: P0 (高)
参与角色: 卖家(seller01)、运营(ops01)
测试步骤:
  【卖家操作】
  1. 卖家登录
  2. 访问 /portal/seller/publish
  3. 填写商品信息：
     - 标题: "iPhone 13 Pro Max 256G 蓝色"
     - 分类: 数码电子
     - 成色: 95新
     - 价格: 6999.00
     - 原价: 8999.00
     - 数量: 1
  4. 上传商品图片（3-5张）
  5. 填写详细描述
  6. 点击"提交审核"
  7. 跳转到"我的商品"页面
  8. 查看商品状态为"审核中"

  【运营操作】
  9. 运营登录
  10. 访问 /ops/review
  11. 在待审核列表找到该商品
  12. 点击"审核"查看详情
  13. 检查商品信息和图片
  14. 点击"通过"
  15. 确认通过

  【验证】
  16. 卖家刷新"我的商品"页面
  17. 商品状态变为"已通过"
  18. 在首页或商品列表可以搜到该商品
预期结果:
  - 商品状态流转: DRAFT → PENDING_REVIEW → APPROVED
  - 审核通过后商品在前台可见
  - 卖家收到系统通知（消息中心）
  - 商品可被买家搜索和购买
```

---

## 8. API接口清单

### 8.1 评价相关接口

```yaml
POST /api/reviews
  描述: 创建评价
  认证: 需要(BUYER角色)
  请求体:
    orderId: integer (required)
    itemId: integer (required)
    rating: integer (1-5, required)
    content: string (required, HTML格式)
    images: array<string> (optional, 最多3张)
  响应:
    success: boolean
    data:
      id: integer
      status: "PENDING"

GET /api/reviews?itemId={id}&status=APPROVED&page=1&size=5
  描述: 获取商品评价列表
  认证: 可选（公开接口）
  响应:
    success: boolean
    data:
      list: array<Review>
      total: integer

POST /api/reviews/{id}/approve
  描述: 通过评价
  认证: 需要(OPS角色)
  响应:
    success: boolean
    message: string

POST /api/reviews/{id}/reject
  描述: 拒绝评价
  认证: 需要(OPS角色)
  请求体:
    reason: string (required, 5-200字符)
  响应:
    success: boolean
    message: string
```

### 8.2 圈子相关接口

```yaml
POST /api/circle/posts
  描述: 发布帖子
  认证: 需要(BUYER或SELLER角色)
  请求体:
    title: string (required, max 60字符)
    content: string (required, HTML格式)
    tags: array<string> (optional, max 5个)
    images: array<string> (optional, max 9张)
  响应:
    success: boolean
    data:
      id: integer
      status: "PENDING"

GET /api/circle/posts?page=1&size=20&tag=&sort=&keyword=
  描述: 获取帖子列表（仅APPROVED状态）
  认证: 可选
  响应:
    success: boolean
    data:
      list: array<Post>
      total: integer

GET /api/circle/posts/:id
  描述: 获取帖子详情
  认证: 可选
  响应:
    success: boolean
    data: PostDetail

POST /api/circle/posts/:id/like
  描述: 点赞/取消点赞帖子
  认证: 需要
  响应:
    success: boolean
    data:
      liked: boolean
      likeCount: integer

POST /api/circle/posts/:id/comments
  描述: 发表评论
  认证: 需要
  请求体:
    content: string (required)
    parentId: integer (optional, 回复评论时填写)
  响应:
    success: boolean
    data: Comment
```

### 8.3 消息相关接口

```yaml
GET /api/messages/unread-count
  描述: 获取未读消息数量
  认证: 需要
  响应:
    success: boolean
    data:
      count: integer

GET /api/messages?page=1&size=20&type=&status=
  描述: 获取消息列表
  认证: 需要
  参数:
    type: string (all/order/review/system)
    status: string (ALL/UNREAD/READ)
  响应:
    success: boolean
    data:
      list: array<Message>
      total: integer

POST /api/messages/:id/read
  描述: 标记单条消息已读
  认证: 需要
  响应:
    success: boolean

POST /api/messages/read-all
  描述: 标记所有消息已读
  认证: 需要
  响应:
    success: boolean
    message: string
```

### 8.4 审批相关接口

```yaml
GET /api/items/pending?category=&keyword=&page=1&size=20
  描述: 获取待审核商品列表
  认证: 需要(OPS角色)
  响应:
    success: boolean
    data:
      list: array<Item>
      total: integer

POST /api/items/:id/approve
  描述: 通过商品审核
  认证: 需要(OPS角色)
  响应:
    success: boolean
    message: string

POST /api/items/:id/reject
  描述: 拒绝商品审核
  认证: 需要(OPS角色)
  请求体:
    reason: string (required)
  响应:
    success: boolean
    message: string

GET /api/ops/stats
  描述: 获取运营统计数据
  认证: 需要(OPS角色)
  响应:
    success: boolean
    data:
      totalUsers: integer
      todayNewUsers: integer
      totalItems: integer
      pendingItems: integer
      totalOrders: integer
      todayOrders: integer
      totalAmount: number
      refundRate: number
      # ... 更多统计维度
```

---

## 9. 测试结果记录模板

### 9.1 测试用例执行记录表

| 用例编号 | 测试场景 | 优先级 | 测试日期 | 执行人 | 结果 | Bug编号 | 备注 |
|---------|---------|--------|---------|--------|------|---------|------|
| TC-001 | 用户提交评价 | P0 | | | PASS/FAIL | | |
| TC-002 | 运营审核评价 | P0 | | | PASS/FAIL | | |
| TC-003 | 发布圈子帖子 | P0 | | | PASS/FAIL | | |
| TC-004 | 点赞帖子 | P1 | | | PASS/FAIL | | |
| TC-005 | 评论帖子 | P1 | | | PASS/FAIL | | |
| TC-006 | 未读消息展示 | P0 | | | PASS/FAIL | | |
| TC-007 | 消息已读 | P0 | | | PASS/FAIL | | |
| TC-008 | 全部标记已读 | P0 | | | PASS/FAIL | | |
| TC-009 | 商品审核-通过 | P0 | | | PASS/FAIL | | |
| TC-010 | 商品审核-拒绝 | P0 | | | PASS/FAIL | | |
| TC-011 | Dashboard数据加载 | P0 | | | PASS/FAIL | | |
| TC-012 | 完整购买流程 | P0 | | | PASS/FAIL | | |
| TC-013 | 商品发布审核流程 | P0 | | | PASS/FAIL | | |

### 9.2 Bug报告模板

```markdown
## Bug标题: [简短描述]

**Bug编号**: BUG-XXX  
**严重程度**: Critical/Major/Minor/Low  
**优先级**: P0/P1/P2/P3  
**发现日期**: 2026-05-07  
**发现人**: XXX  
**所属模块**: 评价/圈子/消息/审批  

### 复现步骤
1. 
2. 
3. 

### 预期结果


### 实际结果
[描述实际看到的现象，附截图]

### 环境信息
- 浏览器: Chrome XX.X.XXXX
- 操作系统: Windows 10/11
- 前端版本: commit xxxxxx
- 后端版本: commit xxxxxx

### 附加信息
- 控制台错误日志
- Network请求截图
- 相关日志片段
```

### 9.3 性能测试记录

| 页面/接口 | 首屏加载时间 | API响应时间(P50) | API响应时间(P99) | 是否达标 |
|----------|------------|----------------|----------------|---------|
| 首页 /portal/home | < 2s | < 200ms | < 500ms | □ |
| 商品详情 /portal/item/:id | < 2s | < 150ms | < 400ms | □ |
| 圈子首页 /portal/circle | < 2s | < 200ms | < 500ms | □ |
| 运营Dashboard /ops/dashboard | < 3s | < 300ms | < 800ms | □ |
| 审批工作台 /ops/review | < 2s | < 250ms | < 600ms | □ |

---

## 附录

### A. 常见问题排查

#### Q1: 前端页面白屏或报错
```bash
# 1. 检查浏览器控制台是否有错误
# 2. 检查Network面板是否有失败的请求（红色）
# 3. 检查后端服务是否正常运行
# 4. 检查CORS配置是否允许跨域
# 5. 清除浏览器缓存后重试
```

#### Q2: API请求401未授权
```bash
# 1. 检查Token是否过期（查看localStorage）
# 2. 检查请求头是否携带Authorization
# 3. 重新登录获取新Token
# 4. 检查后端JWT配置是否与前端一致
```

#### Q3: API请求403禁止访问
```bash
# 1. 检查当前用户角色是否满足要求
# 2. 检查路由meta.roles配置
# 3. 检查后端权限注解 @PreAuthorize
# 4. 使用正确的测试账号登录
```

#### Q4: 页面样式错乱
```bash
# 1. 检查style.css和main.scss是否正确导入
# 2. 检查SCSS变量是否正确定义
# 3. 清除浏览器缓存（Ctrl+Shift+R强制刷新）
# 4. 检查Arco Design CSS版本兼容性
```

### B. 测试数据SQL脚本

```sql
-- 创建测试评价数据（用于测试审核功能）
INSERT INTO reviews (id, order_id, item_id, user_id, rating, content, status, created_at)
VALUES (100, 1, 10, 2, 5, '<p>测试评价内容</p>', 'PENDING', NOW());

-- 创建测试圈子帖子
INSERT INTO circle_posts (id, user_id, title, content, tags, status, created_at)
VALUES (50, 2, '测试帖子', '<p>测试内容</p>', '["测试"]', 'PENDING', NOW());

-- 创建测试未读消息
INSERT INTO messages (id, user_id, type, title, content, status, created_at)
VALUES (200, 2, 'ORDER', '测试消息', '这是一条测试消息', 'UNREAD', NOW());
```

### C. 联系方式与支持

如有疑问或发现问题，请联系：
- **前端负责人**: [姓名] - [邮箱]
- **后端负责人**: [姓名] - [邮箱]
- **测试负责人**: [姓名] - [邮箱]
- **项目群聊**: [钉钉/微信群号]

---

**文档结束**

*最后更新: 2026-05-07*
*文档版本: 1.0*
