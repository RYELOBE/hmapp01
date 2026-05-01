# 校园二手交易平台技术实现文档

## 一、技术架构概述

### 1.1 整体技术架构

校园二手交易平台采用微前端架构设计，将前端应用拆分为多个独立的子应用，通过主容器进行统一管理。后端服务采用Spring Boot框架，提供RESTful API接口。数据库使用MySQL存储业务数据。平台整体采用前后端分离的开发模式，前端通过HTTP协议与后端交互。

微前端架构的核心优势在于支持多团队并行开发、独立部署和按需加载。门户应用、运营后台应用和壳工程相互独立，各应用可以使用不同的技术栈（当前统一使用Vue 3），通过标准化的通信机制实现数据共享。

### 1.2 技术栈选型

前端技术选型方面，核心框架采用Vue 3，该版本全面支持组合式API，提供更好的TypeScript支持和性能优化。构建工具使用Vite 5，相比传统Webpack提供更快的开发启动速度和热更新体验。UI组件库在门户端采用Element Plus，运营后台采用自定义组件库，统一的设计语言保证用户体验一致性。

状态管理采用Pinia进行全局状态管理，相比Vuex更加轻量且支持组合式API风格。路由管理使用Vue Router 4，提供路由守卫和懒加载功能。微前端实现采用Qiankun框架，该框架基于Single-SPA进行了优化，提供更好的样式隔离和资源预加载能力。

后端技术选型方面，核心框架采用Spring Boot 3，最低支持Java 17版本。安全框架采用SaToken，该框架轻量且功能完整，支持多种登录模式和Token验证方式。持久层框架采用Spring Data JPA，简化数据库操作。数据库连接池采用HikariCP，提供优秀的性能表现。

### 1.3 项目目录结构

后端项目位于backend目录，采用标准Maven项目结构。核心源代码在src/main/java/com/campus/marketplace目录下，配置文件在src/main/resources目录下。主要包结构包括controller包存放控制器类，service包存放业务服务类，repository包存放数据访问接口，config包存放配置类。

前端项目采用Monorepo结构，在packages目录下管理多个应用。apps子目录下包含shell（壳工程）、portal（门户应用）、ops（运营后台）三个子应用。common子目录存放各应用共享的常量和工具函数。mf-shared目录存放跨应用共享的Vue组件。

## 二、前端微前端架构实现

### 2.1 壳工程结构

壳工程是整个前端应用的容器，负责管理子应用的注册、加载和生命周期。壳工程在packages/apps/shell目录下实现。

壳工程的核心入口文件为src/main.js，该文件初始化Vue应用实例并挂载到DOM节点。src/bootstrap.js文件在应用启动前执行，负责配置依赖注入和初始化全局状态。src/App.vue是根组件，提供子应用渲染的容器区域。

壳工程的路由配置在src/router/index.js中定义，路由分为登录路由和子应用路由两大类。登录路由指向壳工程内置的登录页面，子应用路由指向各微应用模块。这种路由设计确保用户访问受保护资源时先经过壳工程的路由守卫验证。

微应用渲染组件MicroView.vue负责动态加载子应用的资源并渲染到对应区域。组件接收子应用的配置信息，通过import-entry脚本动态加载子应用的入口文件和静态资源。加载完成后调用子应用导出的生命周期函数完成渲染。

### 2.2 子应用注册机制

子应用注册在壳工程的framePinia仓库中进行管理。framePinia定义了subAppConfig数组存储子应用配置信息，每个配置包含应用名称、挂载路径、入口文件地址、样式文件地址等属性。

子应用配置示例如下：portal应用的名称为portal-app，挂载路径为portal，入口文件地址指向portal应用构建后的index.html。ops应用的名称为ops-app，挂载路径为ops，入口文件地址指向ops应用构建后的index.html。

壳工程在初始化时遍历subAppConfig数组，依次加载各子应用的入口资源。加载过程采用Promise.all并行加载，提升整体加载速度。加载完成后根据子应用的activeRule规则判断何时激活子应用。

子应用切换时，壳工程负责卸载当前子应用并加载目标子应用。卸载过程调用子应用的unmount生命周期函数，清理组件实例和全局状态。加载过程重新执行初始化流程，确保子应用状态正确恢复。

### 2.3 门户应用实现

门户应用是面向普通用户的前端应用，提供商品浏览、购买、卖家中心等功能。门户应用在packages/apps/portal目录下实现。

门户应用的入口文件src/main.js初始化Vue应用并挂载到指定DOM节点。入口文件导入路由配置、Pinia状态管理和全局样式文件。应用启动后根据URL路径加载对应的页面组件。

路由配置在src/router/index.js中定义，采用history模式管理路由。路由表包含首页路由、商品列表路由、商品详情路由、买家中心路由、卖家中心路由、登录路由等。每个路由配置对应的组件路径，支持路由懒加载提升首屏加载速度。

API服务封装在src/services/api.js文件中，统一封装HTTP请求方法。请求拦截器自动添加认证Token，响应拦截器统一处理错误响应。API方法按照业务模块分组组织，包括用户认证API、商品API、订单API、AI问答API等。

### 2.4 运营后台实现

运营后台是面向平台运营人员的前端应用，提供商品审核、页面配置、用户管理等功能。运营后台在packages/apps/ops目录下实现。

运营后台的整体结构与门户应用类似，采用相同的目录组织方式和代码规范。路由配置在src/router/index.js中定义，包含工作台路由、审核管理路由、页面配置路由、用户管理路由、角色管理路由等。

运营后台的页面采用统一布局设计。Layout.vue组件提供页面整体框架，包括顶部导航栏、左侧菜单栏、右侧内容区域。内容区域根据当前路由动态渲染对应的页面组件。

运营后台的权限控制更加严格，每个路由都需要验证用户的运营权限。路由守卫检查用户是否拥有OPS_ADMIN或OPS_SUPER角色，未授权用户访问时重定向到403页面。

### 2.5 共享模块实现

mf-shared是跨应用共享的组件和SDK模块，在packages/apps/mf-shared目录下实现。该模块包含公共Vue组件、认证SDK、HTTP工具等共享资源。

公共组件按照功能分类组织在src/exposes目录下。Login组件提供登录表单功能，支持普通用户登录和运营用户登录两种模式。ItemCard组件提供商品卡片展示，包含商品图片、标题、价格等信息的展示。ImageUploader组件封装图片上传功能，提供预览和进度显示。

SDK封装在src目录下，包括auth-sdk.js（认证SDK）、http.js（HTTP请求工具）、login.js（登录工具）等。auth-sdk提供获取当前用户信息、检查角色权限、Token管理等方法。http.js封装axios实例，提供统一的请求配置和响应处理。

共享模块通过npm workspace方式被各子应用依赖。在子应用的package.json中声明对mf-shared的依赖，构建时自动从共享模块复制资源到子应用。

## 三、后端服务架构实现

### 3.1 后端项目结构

后端项目采用标准分层架构，包含Controller层、Service层、Repository层和Config层。Controller层负责接收HTTP请求并返回响应，Service层处理业务逻辑，Repository层负责数据持久化操作。

包结构按照业务模块划分。controller包下包含AuthController（认证）、ItemController（商品）、OrderController（订单）、OpsController（运营）、AIController（AI问答）等控制器类。每个控制器类对应一个业务领域，提供该领域的所有API接口。

service包下包含对应的服务类。服务类使用@Service注解标注，采用构造方法注入依赖。复杂的业务逻辑在服务类中实现，服务类之间可以相互调用完成组合业务。

repository包下包含数据访问接口。接口继承JpaRepository获得基本的增删改查能力，自定义查询方法遵循Spring Data JPA的命名规范自动实现。

config包下包含各类配置类。SaTokenConfig配置认证框架，WebConfig配置Web参数，CorsConfig配置跨域访问策略。

### 3.2 认证模块实现

认证模块使用SaToken框架实现，基于Token的身份验证机制。用户登录成功后生成Token并返回给客户端，客户端在后续请求中携带Token进行身份验证。

用户登录流程在AuthController中实现。接收用户名密码参数后调用AuthService验证凭证，验证成功则调用SaTokenHelper.login方法创建登录会话并返回Token。Token默认有效期24小时，支持配置刷新机制。

用户注册流程在AuthController中实现。接收用户信息和角色选择参数后，调用AuthService创建用户记录并分配角色。注册成功后自动创建登录会话并返回Token。

运营登录在独立的API路径/api/auth/ops/login下实现。运营登录验证独立的运营账号体系，登录成功后创建运营专用的Token。运营Token与用户Token使用不同的Token前缀标识，便于区分验证。

认证配置在SaTokenConfig中完成。配置使用Cookie方式存储Token，Cookie名称为satoken。配置登录超时时间为86400秒（24小时），二级登录校验不启用。

### 3.3 角色权限实现

角色权限基于SaToken的权限认证扩展实现。系统预置四种角色：BUYER（买家）、SELLER（卖家）、OPS_ADMIN（运营管理员）、OPS_SUPER（超级管理员）。

用户角色关联存储在user_role表中，每个用户可以拥有多个角色。SaTokenRoleProvider类实现框架的RoleProvider接口，提供根据用户ID查询角色列表的方法。

权限验证采用注解方式标记接口的访问要求。@SaCheckLogin注解验证登录状态，@SaCheckRole注解验证角色权限。注解参数指定需要的角色代码，多个角色使用OR关系验证。

业务层的权限验证在Service方法中实现。对于数据范围权限（如卖家只能操作自己的商品），Service方法接收当前用户ID并与数据所属用户ID进行比对。

### 3.4 商品模块实现

商品模块处理商品发布、编辑、查询等业务逻辑。ItemService是核心服务类，提供商品CRUD操作和状态管理。

商品发布流程：接收商品信息后保存到数据库，状态设为PENDING_REVIEW（待审核）。发布成功后触发审核通知（未来可扩展为消息通知）。

商品审核流程：运营人员调用审核接口，传入审核结果和备注。审核通过则更新状态为APPROVED，审核拒绝则更新状态为REJECTED并记录拒绝原因。

商品查询采用动态条件构建。ItemRepository继承JpaSpecificationExecutor支持Specification查询。查询条件包括分类、价格区间、关键词等，根据请求参数动态组装查询条件。

### 3.5 订单模块实现

订单模块处理订单创建、支付、发货、收货、退款等业务流程。OrderService是核心服务类，提供订单全生命周期管理。

订单创建采用事务保证数据一致性。创建订单时检查商品库存是否充足，检查通过则冻结库存并生成订单记录。订单编号使用时间戳加随机数生成，保证唯一性。

订单状态转换通过状态机模式控制。每个状态定义允许的后续状态，状态转换前验证转换条件的合法性。非法状态转换抛出BusinessException异常。

退款流程涉及多系统交互。买家发起退款申请后，订单状态更新为REFUNDING。运营人员审核通过后执行退款操作，更新状态为REFUNDED并释放冻结库存。

### 3.6 AI问答模块实现

AI问答模块提供智能客服功能。AI服务作为外部依赖，系统通过统一接口调用AI能力。

会话管理：每次对话创建一个会话记录，会话包含用户ID和会话编号。消息记录存储在ai_message表中，每条消息标记发送方角色。

AI调用：AIController接收用户消息后，调用AIService处理。Service首先保存用户消息到数据库，然后调用AI接口获取回复，最后保存AI回复到数据库并返回给前端。

AI服务支持流式响应和非流式响应两种模式。流式响应通过SSE（Server-Sent Events）技术实现，AI生成的内容实时推送到客户端。

## 四、数据库设计实现

### 4.1 核心表结构

user表是用户信息表，存储用户基本信息和账号状态。核心字段包括id（主键）、username（用户名）、password（密码加密存储）、nickname（昵称）、phone（电话）、email（邮箱）、avatar（头像）、status（状态）、create_time（创建时间）、update_time（更新时间）。

user_role表是用户角色关联表，存储用户与角色的多对多关系。字段包括id（主键）、user_id（用户ID）、role_code（角色代码）、create_time（创建时间）。通过user_id和role_code的联合唯一索引防止重复分配。

item表是商品信息表，存储商品完整信息。核心字段包括id（主键）、title（标题）、description（描述）、price（价格）、original_price（原价）、category（分类）、condition（成色）、images（图片JSON数组）、seller_id（卖家ID）、status（状态）、review_comment（审核备注）、view_count（浏览数）、favorite_count（收藏数）、create_time（创建时间）、update_time（更新时间）。

order表是订单信息表，存储订单完整信息。核心字段包括id（主键）、order_no（订单编号）、item_id（商品ID）、item_title（商品标题快照）、item_image（商品图片快照）、price（商品价格）、quantity（数量）、total_amount（总金额）、buyer_id（买家ID）、seller_id（卖家ID）、status（状态）、receiver_name（收货人）、receiver_phone（电话）、receiver_address（地址）、express_no（快递单号）、create_time（创建时间）、update_time（更新时间）。

favorite表是收藏信息表，存储用户收藏关系。字段包括id（主键）、user_id（用户ID）、item_id（商品ID）、create_time（创建时间）。通过user_id和item_id的唯一索引防止重复收藏。

ai_session表是AI会话表，存储用户与AI的对话会话。字段包括id（主键）、user_id（用户ID）、session_no（会话编号）、create_time（创建时间）。

ai_message表是AI消息表，存储会话中的消息记录。字段包括id（主键）、session_id（会话ID）、role（角色：USER或ASSISTANT）、content（消息内容）、create_time（创建时间）。

portal_config表是门户配置表，存储可配置的页面参数。字段包括id（主键）、config_key（配置键）、config_value（配置值JSON）、description（描述）、create_time（创建时间）、update_time（更新时间）。

### 4.2 索引设计

查询频繁的字段建立索引提升查询性能。user表的username字段建立唯一索引。item表的seller_id、status、category字段建立普通索引。order表的buyer_id、seller_id、status字段建立普通索引。favorite表的user_id、item_id字段建立联合索引。

### 4.3 初始数据

data.sql文件初始化系统的预设数据。包括四种角色的定义、初始分类数据、默认页面配置等。初始运营账号（admin/admin123）在data.sql中创建。

## 五、关键业务流程实现

### 5.1 用户注册流程

前端收集用户输入的用户名、密码、昵称和选择的角色，调用POST /api/auth/register接口。请求参数格式为JSON，包含username、password、nickname和roleCodes数组。

后端接收请求后执行以下步骤：首先验证用户名是否已存在，存在则返回错误；然后对密码进行BCrypt加密；接着创建用户记录并获取用户ID；最后为用户分配选择的角色。创建成功后返回Token和用户信息。

前端接收响应后，保存Token到本地存储，跳转到首页并显示用户信息。注册流程完成。

### 5.2 商品发布流程

卖家在发布页面填写商品信息，包括标题、描述、价格、分类、成色等。图片通过ImageUploader组件上传到后端的上传接口，返回图片URL后存入表单数据。

填写完成后点击发布按钮，前端调用POST /api/items接口提交商品信息。请求头携带用户Token，接口根据Token识别卖家身份。

后端接收请求后保存商品记录，状态设置为PENDING_REVIEW。发送消息通知运营人员有待审核商品（可通过日志记录实现）。返回商品ID和审核状态。

前端显示发布成功提示，跳转到商品详情页或卖家中心。卖家可以在卖家中心查看审核状态。

### 5.3 商品购买流程

买家在商品详情页点击购买按钮，进入订单确认页面。页面展示商品信息、收货地址选择、订单金额等信息。买家选择收货地址后点击提交订单。

前端调用POST /api/orders接口创建订单。请求体包含itemId、quantity、addressId等信息。

后端执行订单创建逻辑：检查商品状态是否为APPROVED，检查库存是否充足，创建订单记录并更新商品库存。订单状态设置为PENDING_PAYMENT。

由于是毕设简化版本，不接入真实支付，订单创建后状态直接推进为PAID。后续流程为卖家发货、买家确认收货。

### 5.4 运营审核流程

运营人员登录运营后台，进入商品审核页面。页面展示待审核商品列表，按发布时间倒序排列。

点击商品进入审核详情页，查看商品图片、信息和卖家历史记录。确认无误后点击通过按钮，系统调用审核通过接口更新状态为APPROVED。

如发现问题点击拒绝按钮，弹出拒绝原因输入框。填写原因后提交，系统更新状态为REJECTED并记录拒绝原因。

### 5.5 页面配置流程

运营人员在运营后台进入页面配置模块。模块分为路由配置和模块配置两个子页面。

路由配置页面展示当前生效的路由列表，支持新增、编辑、删除操作。编辑时填写路由路径、组件名称、显示顺序等信息。

配置保存后写入portal_config表。门户应用启动时从接口加载路由配置，动态注册到Vue Router中。

## 六、API接口规范

### 6.1 接口命名规范

接口命名遵循RESTful风格，使用HTTP动词表达操作语义。GET用于查询，POST用于创建，PUT用于更新，DELETE用于删除。

用户认证接口：/api/auth/login（登录）、/api/auth/register（注册）、/api/auth/logout（登出）、/api/auth/current（获取当前用户）。

商品接口：/api/items（列表GET、新增POST）、/api/items/{id}（详情GET、更新PUT、删除DELETE）、/api/items/my（我的商品）。

订单接口：/api/orders（列表GET、新增POST）、/api/orders/{id}（详情GET）、/api/orders/{id}/cancel（取消）、/api/orders/{id}/ship（发货）、/api/orders/{id}/confirm（确认收货）。

### 6.2 统一响应格式

所有接口返回统一的JSON格式，包含code、message、data三个字段。code为业务状态码，200表示成功，其他表示错误。message为描述信息，成功时通常为空。data为实际返回的数据。

分页查询的响应格式为：{code:200, message:"", data:{list:[], total:100, page:1, pageSize:20}}。客户端根据返回的分页信息渲染分页组件。

### 6.3 错误处理

后端使用全局异常处理器处理所有未捕获的异常。BusinessException类封装业务错误，抛出时携带错误码和描述。系统捕获异常后转换为统一响应格式返回。

前端在响应拦截器中处理错误响应。401错误跳转登录页，403错误显示无权限提示，其他错误显示错误信息并记录日志。

## 七、部署配置说明

### 7.1 后端部署

后端项目打包为可执行JAR文件。使用mvn clean package命令构建，构建产物在target目录下。

运行JAR包使用java -jar命令，指定Spring Profiles和环境变量。生产环境建议使用--server.port参数指定端口。

Docker部署需要编写Dockerfile和docker-compose.yml。Dockerfile基于openjdk:17镜像，复制JAR文件并设置启动命令。

### 7.2 前端部署

前端项目构建为静态资源。使用pnpm run build命令构建，portal应用构建产物在packages/apps/portal/dist目录，ops应用构建产物在packages/apps/ops/dist目录。

构建产物部署到Nginx服务器。Nginx配置需要设置SPA应用的history路由模式支持，避免刷新404问题。

### 7.3 环境变量

前端应用通过环境变量指定API接口地址、开发服务器地址等配置。开发环境使用.env.development文件，生产环境使用.env.production文件。

后端通过application.yml配置数据库连接、SaToken参数等。不同环境使用不同的配置文件，Spring自动加载对应环境的配置。

## 八、性能优化方案

### 8.1 前端优化

路由懒加载减少首屏加载时间。每个页面组件单独打包，用户访问时才加载对应资源。动态组件使用defineAsyncComponent实现懒加载。

图片资源优化：使用WebP格式减少体积，设置合理的图片尺寸避免加载过大图片。图片加载使用loading占位提升用户体验。

请求优化：接口合并减少请求次数，请求缓存避免重复调用。表单提交使用防抖防止重复提交。

### 8.2 后端优化

数据库查询优化：合理使用索引避免全表扫描，复杂查询使用explain分析执行计划。分页查询使用游标分页替代偏移分页提升大表查询性能。

缓存优化：热点数据使用Redis缓存，减少数据库访问。Token验证结果短暂缓存，提升验证性能。

连接池优化：合理配置HikariCP连接池参数，最大连接数与服务器资源匹配。

## 九、安全防护措施

### 9.1 认证安全

密码存储使用BCrypt加密，即使数据库泄露也无法还原明文密码。BCrypt的work factor设置为10，平衡安全性和性能。

Token使用随机字符串，足够长的位数防止暴力猜测。Token存储在HttpOnly Cookie中，防止XSS攻击窃取。

登录接口添加限流，同一IP短时间多次登录失败触发锁定。异常登录行为记录日志供安全审计。

### 9.2 权限安全

所有接口添加权限验证注解，未登录或权限不足返回403错误。权限注解在Controller层和方法层双重校验。

数据访问验证在Service层实现，即使绕过接口层权限验证也无法访问未授权数据。卖家操作验证数据归属，运营操作验证角色级别。

SQL注入防护使用参数化查询，MyBatis使用#{}语法避免拼接。XSS防护在响应输出时进行HTML转义。

### 9.3 接口安全

敏感接口添加CSRF Token验证。文件上传接口限制文件类型和大小，防止恶意文件上传。

接口调用频率限制，防止滥用。使用滑动窗口算法统计接口调用次数，超限返回429错误。

## 十、测试策略

### 10.1 后端测试

单元测试覆盖Service层的业务逻辑。使用JUnit 5和Mockito编写测试用例，模拟依赖对象的行为。测试用例覆盖正常流程和异常流程。

集成测试验证Controller到Repository的完整链路。使用H2内存数据库替代真实数据库，确保测试环境隔离。

接口测试使用SpringBootTest的WebMvcTest或RestTemplate进行端到端验证。测试用例覆盖所有接口的正常和异常输入。

### 10.2 前端测试

组件测试使用Vue Test Utils和Jest。测试组件的渲染输出和用户交互行为。重要业务组件需要编写测试用例。

E2E测试使用Playwright或Cypress。测试关键用户流程如注册登录、商品发布、订单创建等。

手动测试清单覆盖所有功能点。测试人员在测试环境执行测试，记录发现的缺陷。

## 十一、监控与日志

### 11.1 日志规范

日志使用SLF4J门面和Logback实现。日志级别分为ERROR、WARN、INFO、DEBUG四级，不同环境配置不同的日志级别。

日志内容包含时间戳、线程名、类名、日志级别、消息、异常堆栈等信息。关键业务操作记录INFO级别日志，异常情况记录ERROR级别日志。

日志输出到文件和控制台。生产环境日志文件按日期分割，保留30天。敏感信息如密码、Token不记录到日志。

### 11.2 异常监控

全局异常处理器记录所有未处理的异常。异常信息包含请求路径、请求参数、异常类型、堆栈信息。异常告警通知相关人员及时处理。

关键业务节点添加执行日志。记录业务操作的开始、结束、成功、失败等状态。失败日志包含错误原因和影响范围。

## 十二、文档维护

### 12.1 代码文档

所有类和方法添加Javadoc注释，说明功能、参数、返回值。复杂的业务逻辑添加行内注释说明实现思路。

接口参数和响应使用Swagger/OpenAPI注解生成API文档。文档包含接口说明、参数定义、响应示例等信息。

### 12.2 更新机制

需求变更同步更新本文档。每次发布版本时更新文档版本号和变更说明。

重大架构调整需要先更新文档再实施开发。文档评审通过后才能进入开发阶段。
