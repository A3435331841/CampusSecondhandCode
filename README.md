# 校园二手交易平台

基于 Uni-app、Vue 3 和 Spring Boot 的校园二手交易平台。项目面向校内闲置物品流转场景，提供用户端小程序、后台管理端和后端服务，支持实名发布、商品浏览、收藏评论、即时聊天、订单流转、双方评价、信用分和后台治理等能力。

## 一、项目功能

### 1. 用户端

用户端位于 `frontend-app/`，主要面向校园用户使用。

- 微信小程序登录
- 学号实名校验
- 首页商品浏览与推荐
- 商品分类筛选
- 商品发布与图片上传
- 腾讯地图面交点选择，地图结果为空时提供校园常用面交点兜底
- 商品详情、多图展示、面交点展示
- 商品收藏与评论
- 买卖双方 WebSocket 实时聊天
- 我发布的商品
- 我买到的订单
- 我卖出的订单
- 取消订单、确认收货
- 买家评价卖家、卖家评价买家
- 用户信用分与评分维护

### 2. 管理端

管理端位于 `frontend-admin/`，主要面向平台管理员使用。

- 管理员登录
- 用户管理
- 商品审核
- 分类管理
- 实名状态查看
- 评论管理
- 数据看板
- 用户增长和商品发布趋势图

### 3. 后端服务

后端位于 `backend/`，提供统一业务接口和数据支撑。

- Token 鉴权与 Redis 登录态
- 微信小程序登录服务
- 学号实名服务
- 商品、分类、订单、收藏、评论接口
- WebSocket 聊天服务
- 聊天会话与历史消息
- 腾讯地图地点搜索与逆地址解析，支持校园常用地点兜底
- 订单评价与信用分更新
- 用户行为记录与轻量推荐
- SpringDoc / Swagger 接口文档
- 后端单元测试

## 二、技术栈

### 后端

- JDK 17
- Spring Boot 3.2.3
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis
- WebSocket
- SpringDoc OpenAPI
- JUnit 5 / Mockito

### 用户端

- Uni-app
- Vue 3
- uview-plus
- 微信小程序

### 管理端

- Vue 3
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts

## 三、目录结构

```text
.
├── backend/                 后端 Spring Boot 服务
├── frontend-app/            Uni-app 用户端（小程序）
├── frontend-web/            Vue 用户 Web 端
├── frontend-admin/          Vue 管理端
├── 资料/                    项目文档、阶段总结、验证说明
├── 阶段报告/                阶段汇报材料
├── .env.example             环境变量示例
├── .gitignore               Git 忽略规则
└── README.md                项目说明
```

## 四、环境要求

请先准备以下环境：

- JDK 17
- Maven 3.9.x
- MySQL 8.0
- Redis
- Node.js
- npm
- 微信开发者工具

如果只运行管理端和后端，不需要微信开发者工具；如果运行用户端小程序，需要使用微信开发者工具打开编译后的微信小程序工程。

## 五、配置说明

项目通过环境变量读取数据库、Redis、微信和腾讯地图配置。可参考 `.env.example`：

```text
WECHAT_MINI_APP_ID=
WECHAT_MINI_APP_SECRET=
TENCENT_MAP_KEY=

DB_HOST=localhost
DB_PORT=3306
DB_NAME=campus_secondhand
DB_USER=root
DB_PASSWORD=

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0
```

后端配置文件位于：

```text
backend/src/main/resources/application.yml
```

注意：

- 不要把真实 `AppSecret`、腾讯地图 Key、数据库密码提交到仓库。
- 微信登录和腾讯地图属于真实第三方接入，需要在对应平台申请并配置密钥。
- 学号实名当前使用本地 `biz_student_roster` 名单模拟学校权威数据源，后续可替换为学校官方接口。

## 六、数据库初始化

先在 MySQL 中创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS campus_secondhand
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

然后依次执行：

```text
backend/src/main/resources/database.sql
backend/src/main/resources/data.sql
```

其中：

- `database.sql`：数据库表结构
- `data.sql`：中文校园场景验收数据，包含 320 个用户、3000 件商品、1200 笔订单，以及聊天、收藏、评论、评价和推荐行为数据

`data.sql` 由脚本生成。如需调整演示数据，修改脚本后重新生成：

```powershell
python tools\generate_demo_data.py
```

内置演示账号：

- 管理端：`admin / 123456`
- 本地接口/演示数据：`user001 / 123456`、`user002 / 123456`、`user004 / 123456`
- 禁用账号验证：`user003 / 123456`

## 七、启动项目

### 1. 启动 Redis 和 MySQL

先确认 MySQL 和 Redis 已正常启动，并且数据库脚本已经执行完成。

### 2. 启动后端

```powershell
cd backend
mvn spring-boot:run
```

后端默认端口：

```text
http://localhost:8080
```

接口文档地址：

```text
http://localhost:8080/swagger-ui/index.html
```

### 3. 启动管理端

```powershell
cd frontend-admin
npm install
npm run dev
```

启动后根据终端输出访问管理端地址，通常为：

```text
http://localhost:5174
```
### 4. 启动用户 Web 端

```powershell
cd frontend-web
npm install
npm run dev
```

启动后访问：

```text
http://localhost:5176
```

使用 `user001 / 123456` 登录。

### 5. 启动用户端小程序

```powershell
cd frontend-app
npm install
npm run dev:mp-weixin
```

编译完成后，使用微信开发者工具打开生成的小程序工程。

微信登录调试注意事项：

- 打开目录应为 `frontend-app/dist/dev/mp-weixin`，不是 `frontend-app/src`。
- 微信开发者工具顶部应显示已登录状态，并且项目 AppID 应为 `.env` 中的 `WECHAT_MINI_APP_ID`。
- 如果控制台出现“游客模式”“无 AppID 关联”或 `wx.login` 返回工具模拟结果，请先在微信开发者工具中登录有该 AppID 开发权限的微信号，再重新编译或重新打开项目。
- 如果需要使用微信开发者工具 CLI，先在“设置 -> 安全设置”中开启服务端口。

如果需要 H5 调试，也可以运行：

```powershell
npm run dev:h5
```

## 八、测试

后端单元测试：

```powershell
cd backend
mvn test
```

也可以只运行某个测试类：

```powershell
mvn -Dtest=ProductServiceImplTest test
```

当前测试覆盖的主要方向包括：

- 商品发布与实名门禁
- 用户、商品后台接口权限边界
- 微信登录
- 学号实名
- 分类服务
- 腾讯地图服务与校园地点兜底
- 收藏服务
- 评论服务
- 订单评价与信用分
- 订单自购、未上架商品等下单边界
- 推荐服务
- 前台/后台中文界面文案与演示数据一致性

## 九、主要业务流程

### 1. 用户交易流程

```text
微信登录 -> 学号实名 -> 发布商品 -> 管理端审核 -> 商品展示 -> 聊天沟通 -> 下单 -> 确认收货 -> 双方评价
```

### 2. 商品发布流程

```text
填写商品信息 -> 上传图片 -> 选择分类 -> 选择面交点 -> 提交发布 -> 等待审核
```

### 3. 管理端治理流程

```text
管理员登录 -> 商品审核 -> 分类维护 -> 实名状态查看 -> 评论治理 -> 查看数据看板
```

## 十、项目文档

主要文档位于 `资料/` 和 `阶段报告/`：

- `资料/需求规格说明书.md`
- `资料/系统设计说明书_V2.1.md`
- `资料/环境配置与下载指南.md`
- `资料/B范围实现说明.md`
- `资料/B范围完整验证手册.md`
- `资料/B范围平台完善设计说明.md`（规划文档）
- `资料/B范围平台完善实施计划.md`（规划文档）
- `资料/GitHub提交整理说明.md`
- `阶段报告/项目进度汇报_第一周.md`
- `阶段报告/项目进度汇报_第二次（第二周至当前）.md`
- `阶段报告/第三阶段工作小结.md`

## 十一、提交说明

提交 GitHub 前请确认：

- 没有提交 `.env`、真实密钥、数据库密码等敏感配置。
- 没有提交 `target/`、`node_modules/`、`dist/`、`unpackage/` 等构建产物。
- 没有提交个人草稿、内部计划、临时文件或本地工具包。
- 如需公开文档，优先保留中文正式文档。

`.gitignore` 已对常见构建产物、本地配置和内部过程文件进行排除。

## 十二、当前版本边界

已真实接入：

- 微信小程序登录
- 腾讯地图 WebService

已实现但属于可替换模拟：

- 学校实名系统：当前基于本地学籍名单 `biz_student_roster` 校验

暂未包含：

- 学校官方认证接口直连
- 真实线上支付
- 机器学习推荐
- 生产环境部署脚本
