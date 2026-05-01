# 校园二手交易平台实施任务清单

## 一、项目概述与实施目标

本实施任务清单基于已完成的《产品规格说明书》《产品业务逻辑文档》《技术实现文档》《Bug分析与修复方案》四份核心文档编制。任务清单将文档中定义的各项需求和已识别的问题转化为可执行的开发任务，按照依赖关系排序，确保开发工作有序推进。

实施目标包括：完善双登录入口体系，实现用户端与运营端完全分离；构建完整的角色权限机制，支持买家、卖家双身份；打通商品发布与审核流程，实现状态可控流转；优化前端微前端架构，解决白屏和状态同步问题；完善后端接口规范和数据校验；修复已识别的安全漏洞和业务逻辑缺陷。

## 二、任务分解与详细说明

### 任务一：认证授权体系重构

**任务描述**：重构现有认证授权体系，实现用户登录与运营登录的完全分离，完善基于角色的权限控制机制。

**涉及文件**：

- backend/src/main/java/com/campus/marketplace/controller/AuthController.java
- backend/src/main/java/com/campus/marketplace/config/SaTokenConfig.java
- backend/src/main/java/com/campus/marketplace/service/AuthService.java
- packages/apps/mf-shared/src/sdk/auth-sdk.js
- packages/apps/portal/src/views/Login.vue
- packages/apps/ops/src/views/Login.vue

**具体步骤**：

首先，需要在AuthController中新增运营登录接口。新接口路径为/api/auth/ops/login，与现有的用户登录接口/api/auth/login区分开来。运营登录方法需要验证独立的运营账号体系，登录成功后在Token中添加运营标识前缀。实现时需要参考现有登录逻辑，但使用不同的账号表或账号类型字段进行区分。

其次，配置SaToken框架支持多类型Token。SaTokenConfig中需要设置运营Token的存储策略，包括Token有效期、Cookie名称、刷新机制等参数。运营Token与用户Token使用不同的前缀标识，便于在权限校验时快速区分。

再次，完善权限注解配置。在所有需要权限校验的Controller方法上添加@SaCheckRole注解。发布商品相关接口要求SELLER角色，审核管理相关接口要求OPS_ADMIN角色，用户管理相关接口要求OPS_ADMIN或OPS_SUPER角色。超级管理员专属操作如角色管理需要OPS_SUPER角色。

然后，完善Service层的数据归属校验。卖家操作商品时需要验证商品是否属于当前卖家，买家操作订单时需要验证订单是否属于当前买家，运营操作时验证操作对象的合法性和权限范围。

最后，统一前端Token存储机制。用户Token和运营Token分别存储，前端请求拦截器根据访问路径自动携带对应Token。Token过期处理时分别跳转到对应的登录页面。

**前置条件**：数据库user_role表结构确认，角色枚举值定义完成。

**验收标准**：用户登录后无法访问运营接口，运营登录后无法操作用户业务，同一用户可以同时拥有人户端和运营端登录状态。

### 任务二：用户注册与角色选择功能

**任务描述**：完善用户注册流程，实现注册时的角色选择，支持多角色身份机制。

**涉及文件**：

- backend/src/main/java/com/campus/marketplace/controller/AuthController.java
- backend/src/main/java/com/campus/marketplace/service/AuthService.java
- backend/src/main/java/com/campus/marketplace/repository/UserRepository.java
- packages/apps/portal/src/views/Register.vue
- packages/apps/shell/src/stores/auth.js

**具体步骤**：

首先，修改注册接口支持角色选择参数。注册接口接收username、password、nickname和roleCodes数组参数。注册时验证用户名唯一性，对密码进行BCrypt加密，创建用户记录后批量插入用户角色关联记录。

其次，实现前端注册页面。注册页面包含用户名输入框、密码输入框、确认密码输入框、昵称输入框、角色选择复选框组。角色选择至少提供买家和卖家两个选项，必选至少一项。表单需要前端校验，验证密码一致性和必填项。

再次，实现角色切换机制。用户登录后系统判断用户拥有的角色数量。如果只有一个角色，自动设为当前活跃角色。如果有多个角色，提供角色切换入口允许用户选择当前活跃角色。角色切换后更新全局状态，刷新页面功能菜单。

然后，实现角色申请功能。用户可以申请开通新角色，申请记录存入申请表中等待运营人员审核。运营后台添加角色申请审核功能。

**前置条件**：认证授权体系重构任务完成，角色枚举值定义完成。

**验收标准**：新用户注册时必须选择至少一个角色，注册成功后自动获得对应角色权限，用户可以切换当前活跃角色，用户可以申请开通新角色。

### 任务三：商品发布与审核流程

**任务描述**：完善商品发布流程和审核机制，实现状态可控流转。

**涉及文件**：

- backend/src/main/java/com/campus/marketplace/controller/ItemController.java
- backend/src/main/java/com/campus/marketplace/controller/OpsReviewController.java
- backend/src/main/java/com/campus/marketplace/service/ItemService.java
- backend/src/main/java/com/campus/marketplace/service/ReviewService.java
- backend/src/main/java/com/campus/marketplace/repository/ItemRepository.java
- packages/apps/portal/src/views/seller/PublishItem.vue
- packages/apps/portal/src/views/seller/MyItems.vue
- packages/apps/ops/src/views/ReviewList.vue
- packages/apps/ops/src/views/ReviewDetail.vue

**具体步骤**：

首先，修正商品发布时的状态设置逻辑。卖家发布商品时保存后状态设为PENDING_REVIEW（待审核），非APPROVED或直接上架。这一修改主要在ItemService.publish方法中完成。

其次，添加按状态查询商品的方法。在ItemRepository中添加findByStatus方法，支持查询指定状态的商品列表。添加根据sellerId和status联合查询的方法，支持卖家查询自己各状态的商品。

再次，完善运营审核接口。OpsReviewController提供待审核列表查询接口、审核详情接口、通过接口、拒绝接口。审核拒绝时要求填写拒绝原因，系统记录审核日志。

然后，完善卖家商品管理页面。卖家中心展示自己的所有商品列表，支持按状态筛选。展示审核状态标签：待审核黄色、已通过绿色、已拒绝红色。拒绝商品展示拒绝原因，卖家可修改后重新提交。

最后，完善运营审核页面。审核列表页展示待审核商品，提供通过和拒绝按钮。审核详情页展示商品完整信息，运营人员可以查看商品图片、文字描述，核实是否符合平台规范。

**前置条件**：角色权限控制完成，卖家角色校验生效。

**验收标准**：卖家发布商品后进入待审核状态，运营后台可见待审核商品列表，运营审核通过后商品上架展示，审核拒绝后卖家可修改重新提交。

### 任务四：订单管理核心功能

**任务描述**：完善订单创建、状态流转、支付模拟等核心功能。

**涉及文件**：

- backend/src/main/java/com/campus/marketplace/controller/OrderController.java
- backend/src/main/java/com/campus/marketplace/service/OrderService.java
- backend/src/main/java/com/campus/marketplace/service/ItemService.java
- backend/src/main/java/com/campus/marketplace/repository/OrderRepository.java
- packages/apps/portal/src/views/orders/index.vue
- packages/apps/portal/src/views/orders/OrderConfirm.vue

**具体步骤**：

首先，完善订单创建的业务校验。OrderService.createOrder方法中添加商品可售状态检查、库存充足性检查、重复订单检查。校验不通过时抛出明确的业务异常。

其次，实现订单状态机控制。定义订单状态枚举和允许的流转规则。待付款可流转为已取消或已付款，已付款可流转为已发货或退款中，已发货可流转为已完成或退款中，已完成和已取消为终态。

再次，添加模拟支付功能。由于毕设不接入真实支付，添加模拟支付接口。买家在待付款订单页面点击支付按钮调用模拟接口，接口将订单状态更新为已付款并记录支付时间。

然后，完善卖家订单处理功能。卖家中心展示销售订单列表。待发货订单提供发货按钮，点击后填写快递公司和快递单号。发货后订单状态更新为已发货。

最后，完善买家订单确认功能。买家在已发货订单中点击确认收货按钮。确认收货后订单状态更新为已完成，交易完成。

**前置条件**：商品发布审核流程完成，商品库存机制已建立。

**验收标准**：订单创建前进行完整业务校验，订单状态按照定义规则流转，模拟支付功能可用，卖家可发货，买家可确认收货。

### 任务五：前端微前端稳定性修复

**任务描述**：修复子应用加载白屏、路由状态不同步、跨应用状态共享等问题。

**涉及文件**：

- packages/apps/shell/src/router/index.js
- packages/apps/shell/src/minFrame/framePinia.js
- packages/apps/shell/src/views/MicroView.vue
- packages/apps/portal/vite.config.js
- packages/apps/ops/vite.config.js
- packages/apps/mf-shared/vite.config.js

**具体步骤**：

首先，修复子应用入口配置。检查portal和ops子应用的vite.config.js，确保output配置正确。确保html入口文件中的挂载点id与应用bootstrap.js中的容器id一致。

其次，修复生命周期函数导出。子应用的bootstrap.js需要正确导出bootstrap、mount、unmount三个生命周期函数。mount函数接收props参数，包括rootContainer容器引用。

再次，配置路由同步机制。shell应用使用history模式，配置Qiankun的prefetch策略。子应用使用空基座的history模式或memory模式，避免与主应用路由冲突。

然后，实现跨应用状态共享。使用Cookie存储登录Token，前端封装统一的Token读写方法。在shell应用中实现全局状态仓库，各子应用通过props或init GlobalState获取状态。

最后，优化加载性能和错误处理。配置资源预加载策略，避免资源加载顺序问题。添加加载超时处理和友好错误提示。实现加载失败后的重试机制。

**前置条件**：后端接口稳定可用，前端构建配置正确。

**验收标准**：首次访问无白屏，页面切换流畅无闪烁，登录状态在各子应用间同步，刷新页面保持当前状态。

### 任务六：AI问答功能集成

**任务描述**：集成AI问答功能，提供智能客服能力。

**涉及文件**：

- backend/src/main/java/com/campus/marketplace/controller/AIController.java
- backend/src/main/java/com/campus/marketplace/service/AIService.java
- backend/src/main/java/com/campus/marketplace/repository/AISessionRepository.java
- backend/src/main/java/com/campus/marketplace/repository/AIMessageRepository.java
- packages/apps/mf-shared/src/components/AiAssistant.vue
- packages/apps/portal/src/views/ItemDetail.vue

**具体步骤**：

首先，扩展AI对话后端接口。现有的AIController需要完善，实现会话管理、消息存储、AI调用逻辑。接口包括创建会话、发送消息、获取历史消息等。

其次，设计会话和消息存储结构。会话表存储用户ID和会话标识，消息表存储会话ID、角色、内容、创建时间。消息角色区分用户消息和AI回复。

再次，开发AI助手前端组件。在mf-shared中开发AiAssistant.vue组件，提供对话界面、消息列表、输入框等元素。组件支持展开收起、最小化、关闭等交互。

然后，将AI助手集成到商品详情页。页面右下角显示AI助手入口按钮，点击展开AI助手浮窗。浮窗中展示对话历史，用户可以输入问题并获取回答。

最后，配置AI服务调用。根据项目实际情况对接第三方AI服务或使用模拟数据。确保AI回复格式良好，支持Markdown渲染。

**前置条件**：后端认证接口稳定，AI服务API可用。

**验收标准**：用户可以与AI助手对话，对话历史正确保存，AI回复内容合理。

## 三、任务依赖关系

任务之间存在以下依赖关系，需要按照顺序执行。

认证授权体系重构任务为基础任务，其他所有功能任务依赖此任务完成后才能正确进行权限校验。

用户注册与角色选择功能依赖认证授权体系重构任务完成后进行。

商品发布与审核流程依赖认证授权体系重构和用户注册功能完成后进行。

订单管理核心功能依赖商品发布功能完成后进行，因为订单需要关联有效的商品信息。

前端微前端稳定性修复任务可以与后端任务并行执行，但需要在整体开发阶段后期进行联调验证。

AI问答功能集成可以独立进行，但建议在其他核心功能稳定后再集成测试。

## 四、验收与测试要求

每个任务完成后需要通过以下测试验证。

功能测试验证任务定义的所有功能点可正常使用，符合业务逻辑文档中的定义。

权限测试验证未授权用户无法访问受限功能，不同角色用户的权限边界正确。

接口测试使用Postman或类似工具验证所有接口的请求和响应符合规范。

集成测试验证前后端联调正常，数据流转正确，异常处理完善。

安全测试验证敏感信息保护、越权访问防护、输入校验有效性。

## 五、实施注意事项

开发过程中需要保持与文档的一致性，如需调整及时更新文档。代码提交遵循规范，提交信息清晰描述变更内容。每个任务完成后进行自测，不得将明显缺陷带到下一阶段。遇到阻塞问题及时沟通协调解决。
