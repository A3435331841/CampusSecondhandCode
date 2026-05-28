# B Scope 完整验证手册

## 1. 文档目的

本文档用于指导你对当前 `B` 范围版本的校园二手交易平台做一次完整、可复现、可留痕的验证工作。

验证目标不是“随便点一下能不能跑”，而是确认以下几个维度都成立：

- 环境可启动
- 配置可落地
- 数据可初始化
- 后端回归测试通过
- 用户端主链路闭环成立
- 管理端治理链路成立
- 微信登录与腾讯地图真实接入链路成立
- 本地实名名单校验链路成立
- 推荐、收藏、评论、评价、信用分等扩展能力成立
- 验证结果可截图、可记录、可用于答辩或验收

---

## 2. 当前版本边界

### 2.1 已真实接入

- 微信小程序登录
- 腾讯地图 WebService 检索与逆解析

### 2.2 已实现但为可替换模拟

- 学校实名系统：当前使用本地 `biz_student_roster` 学籍名单作为权威数据源

### 2.3 当前版本不包含

- 学校官方认证接口直连
- 真实线上支付
- 机器学习推荐

---

## 3. 验证前准备

### 3.1 代码位置

当前实现建议在隔离工作区验证：

- 项目路径：`C:\Users\34353\Desktop\计算机综合项目实践\CampusSecondhandCode-bscope`

### 3.2 基础软件

必须准备：

- JDK 17
- MySQL 8.0
- Redis
- Node.js
- 微信开发者工具

### 3.3 关键配置

当前实现依赖这些环境变量：

- `WECHAT_MINI_APP_ID`
- `WECHAT_MINI_APP_SECRET`
- `TENCENT_MAP_KEY`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_DB`

推荐本地验证值：

```powershell
$env:WECHAT_MINI_APP_ID='你的AppID'
$env:WECHAT_MINI_APP_SECRET='你的AppSecret'
$env:TENCENT_MAP_KEY='你的腾讯地图Key'
$env:DB_HOST='localhost'
$env:DB_PORT='3306'
$env:DB_NAME='campus_secondhand'
$env:DB_USER='root'
$env:DB_PASSWORD='你的MySQL密码'
$env:REDIS_HOST='localhost'
$env:REDIS_PORT='6379'
$env:REDIS_DB='0'
```

### 3.4 微信后台配置

你需要确认这些项已经完成：

- 已有有效小程序 `AppID`
- `AppSecret` 可用
- 小程序开发设置中的合法 request 域名已配置
- 开发时若使用本地接口，需要确认微信开发者工具对本地域名或代理方式允许访问

### 3.5 腾讯地图配置

你需要确认：

- 已创建 WebService API Key
- Key 具备地点搜索与逆地址解析权限

---

## 4. 数据初始化

### 4.1 创建数据库

当前应用不会在每次启动时自动覆盖数据库，你需要手工初始化。

先在 MySQL 中创建目标数据库，例如：

```sql
CREATE DATABASE IF NOT EXISTS campus_secondhand
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### 4.2 执行结构脚本

执行：

- `backend/src/main/resources/database.sql`

说明：

- 该脚本负责建表
- 不负责每次启动自动覆写数据

### 4.3 执行验收数据脚本

执行：

- `backend/src/main/resources/data.sql`

说明：

- 该脚本提供基础分类
- 该脚本提供实名名单测试数据
- 该脚本提供演示账号、商品、订单、聊天、收藏、评论、评价和推荐行为数据
- 该脚本由 `tools/generate_demo_data.py` 生成，调整演示数据后需重新执行生成脚本

### 4.4 初始化后人工检查

你至少要检查这些表是否存在：

- `sys_user`
- `biz_category`
- `biz_product`
- `biz_order`
- `biz_chat_session`
- `biz_chat_message`
- `biz_student_roster`
- `biz_favorite`
- `biz_product_comment`
- `biz_order_review`
- `biz_user_behavior`

你至少要检查这些验收数据是否存在：

- `biz_category` 有 6 条分类
- `biz_student_roster` 有 3 条实名名单
- `sys_user` 有 `admin`、`user001`、`user002`、`user003`、`user004`
- `biz_product` 有 12 条商品，覆盖待审核、在售、下架、售出状态
- `biz_order` 有 4 条订单，覆盖待交接、已完成、已取消状态
- `biz_chat_session` 有 4 条聊天会话
- `biz_chat_message` 有 9 条聊天消息
- `biz_favorite` 有 6 条收藏记录
- `biz_product_comment` 有 8 条评论记录
- `biz_order_review` 有 3 条评价记录
- `biz_user_behavior` 有 12 条推荐行为记录

内置账号：

- 管理端账号：`admin / 123456`
- 本地接口/演示数据账号：`user001 / 123456`、`user002 / 123456`、`user004 / 123456`
- 禁用账号：`user003 / 123456`

如需重新生成 `data.sql`：

```powershell
python tools\generate_demo_data.py
```

---

## 5. 启动顺序

推荐启动顺序如下。

### 5.1 启动 Redis

确认 Redis 正常运行。

### 5.2 启动 MySQL

确认数据库能连通，且你已执行初始化脚本。

### 5.3 启动后端

```powershell
Set-Location C:\Users\34353\Desktop\计算机综合项目实践\CampusSecondhandCode-bscope\backend
..\..\tools\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```

期望：

- 后端启动成功
- 默认端口 `8080`
- 无关键启动异常

### 5.4 启动管理端

```powershell
Set-Location C:\Users\34353\Desktop\计算机综合项目实践\CampusSecondhandCode-bscope\frontend-admin
npm install
npm run dev
```

期望：

- 管理端开发服务启动成功

### 5.5 启动用户端

```powershell
Set-Location C:\Users\34353\Desktop\计算机综合项目实践\CampusSecondhandCode-bscope\frontend-app
npm install
npm run dev:mp-weixin
```

然后在微信开发者工具中打开生成的小程序工程。

---

## 6. 后端自动化回归验证

### 6.1 执行命令

```powershell
Set-Location C:\Users\34353\Desktop\计算机综合项目实践\CampusSecondhandCode-bscope\backend
..\..\tools\apache-maven-3.9.6\bin\mvn.cmd test
```

### 6.2 通过标准

当前应满足：

- `BUILD SUCCESS`
- `24 tests, 0 failures, 0 errors`

### 6.3 当前测试覆盖点

- `ProductServiceImplTest`
  - 商品发布字段映射
  - 实名门禁
  - 配置和 schema 基线
- `WechatAuthServiceImplTest`
  - 微信登录新用户
  - 微信登录老用户
  - 缺失 code
  - 禁用用户
  - WeChat 上游异常
- `StudentVerificationServiceImplTest`
  - 实名精确匹配成功
  - 不匹配失败
  - 用户不存在失败
- `CategoryServiceImplTest`
  - 启用分类排序
  - 分类默认值保存
- `TencentMapServiceImplTest`
  - 逆解析成功
  - 地点建议成功
  - Key 缺失失败
  - 地图请求异常失败
- `FavoriteServiceImplTest`
  - 收藏幂等
  - 取消收藏
- `ProductCommentServiceImplTest`
  - 评论写入
  - 评论查询
- `ReviewServiceImplTest`
  - 评价成功
  - 重复评价拦截
- `RecommendationServiceImplTest`
  - 基于行为与分类偏好排序

---

## 7. 用户端完整验证

下面按真实业务主链路验证。

### 7.1 用户登录验证

步骤：

1. 打开小程序登录页
2. 点击 `Sign in with WeChat`
3. 观察后端是否收到 `/api/auth/wechat/login`
4. 检查小程序本地是否写入：
   - `user_token`
   - `user_info`

验证点：

- 登录成功后可进入首页
- `user_info` 中有：
  - `userId`
  - `nickname`
  - `avatar`
  - `role`
  - `verifyStatus`

失败排查：

- 如果失败，优先检查：
  - `AppID/AppSecret`
  - request 域名配置
  - 微信开发者工具网络面板
  - 后端异常日志

### 7.2 实名验证

步骤：

1. 进入 `Profile`
2. 点击 `Verify now`
3. 用 `data.sql` 中已有实名名单填写信息，例如：
   - `student_no`
   - `real_name`
   - `college`
   - `major`
   - `grade`
   - `id_card_suffix`
4. 提交

期望：

- 返回成功
- 本地 `user_info.verifyStatus` 变为 `VERIFIED`
- 返回上一页后 Profile 能显示已实名状态

负例：

1. 刻意输错一个字段
2. 提交实名

期望：

- 返回失败
- 用户资料不应被部分更新

### 7.3 发布商品验证

步骤：

1. 未实名时进入发布页尝试发布

期望：

- 后端应拒绝
- 提示用户未实名

然后：

1. 完成实名
2. 进入发布页
3. 填写标题、描述、价格、分类
4. 上传图片
5. 选择面交点
6. 提交

期望：

- 发布成功
- 商品状态初始为待审核

### 7.4 面交点验证

步骤：

1. 发布页点击 `Pickup`
2. 输入关键词，例如 `Library`
3. 选择一个返回结果
4. 返回发布页

期望：

- 发布页能回填：
  - `pickupPlaceName`
  - `pickupAddress`
  - `pickupLat`
  - `pickupLng`

### 7.5 首页分类与推荐验证

步骤：

1. 登录后进入首页
2. 检查分类区域是否从后端加载
3. 点击某分类，确认商品列表过滤
4. 做几次收藏/评论/下单行为
5. 回到首页

期望：

- 首页存在 `Recommended` 区块
- 推荐列表非空时应与用户行为偏好有一定相关性

### 7.6 收藏验证

步骤：

1. 进入商品详情
2. 点击 `Favorite`

期望：

- 收藏状态切换
- 再点一次应能取消收藏
- 后端收藏状态接口返回与页面一致

### 7.7 评论验证

步骤：

1. 进入商品详情
2. 输入评论文本
3. 点击发送

期望：

- 评论成功插入
- 评论列表刷新后能看到新评论
- 评论为空时应被拒绝

### 7.8 聊天验证

步骤：

1. 进入商品详情
2. 点击 `Chat`
3. 向卖家发送消息

期望：

- WebSocket 建连成功
- 当前会话消息实时发送
- 刷新/重进后能拉到历史消息

### 7.9 下单与收货验证

步骤：

1. 在详情页点击 `Buy now`
2. 进入 `My Purchases`
3. 查看新订单
4. 点击 `Confirm Receipt`

期望：

- 订单状态从待确认变成已完成
- 订单 `finishTime` 有值

### 7.10 买家评价卖家验证

步骤：

1. 在 `My Purchases` 中对已完成订单点击 `Review Seller`
2. 提交评价

期望：

- 评价只允许一次
- 重复提交应失败
- 卖家信用分和平均评分更新

### 7.11 卖家评价买家验证

步骤：

1. 进入 `My Sales`
2. 对已完成订单点击 `Review Buyer`
3. 提交评价

期望：

- 评价成功
- 卖家端状态更新为已评价
- 买家信用分或平均评分更新

---

## 8. 管理端完整验证

### 8.1 管理员登录

步骤：

1. 打开管理端
2. 用后台账号密码登录

期望：

- 进入后台首页
- 路由守卫生效

### 8.2 分类管理

步骤：

1. 进入 `Categories`
2. 新增一个分类
3. 修改已有分类
4. 切换分类启用状态

期望：

- 分类列表实时刷新
- 用户端首页与发布页能感知分类变化

### 8.3 商品审核

步骤：

1. 进入 `Products`
2. 查看待审核商品
3. 通过或驳回

期望：

- 审核后状态更新
- 审核结果影响用户端可见性

### 8.4 实名状态管理视图

步骤：

1. 进入 `Verification`
2. 查看实名后的用户数据

期望：

- 能看到：
  - `realName`
  - `studentNo`
  - `college`
  - `major`
  - `grade`
  - `verifyStatus`

### 8.5 评论管理

步骤：

1. 进入 `Comments`
2. 查看评论列表
3. 切换评论状态

期望：

- 评论状态修改后，用户端只显示启用评论

### 8.6 仪表盘

期望至少检查：

- 页面可正常加载
- 没有因新增接口导致报错

---

## 9. 推荐验证方法

推荐系统不是“必须肉眼看起来完美”，而是要验证它有没有按设计使用行为信号。

建议步骤：

1. 用 A 账号连续收藏/评论/购买某一分类商品
2. 返回首页查看推荐列表
3. 比较推荐结果是否优先出现同类高热度商品

验证标准：

- 不要求完全个性化
- 但应体现：
  - 分类偏好
  - 收藏/评论/下单行为
  - 商品热度

---

## 10. 验证证据留存

建议为答辩或验收准备这些证据。

### 10.1 截图证据

至少保留：

- 微信登录成功页
- 实名提交成功页
- 发布页面交点选择
- 商品详情页收藏和评论
- 聊天页面
- 我的购买页
- 我的卖出页
- 评价提交页
- 后台分类管理页
- 后台评论管理页
- 后台实名状态页

### 10.2 日志证据

建议保留：

- 后端启动日志
- `mvn test` 成功输出
- 微信登录失败/成功的关键日志片段
- 腾讯地图调用成功的关键日志片段

### 10.3 SQL 证据

建议抽样检查：

- `sys_user.verify_status`
- `biz_product.pickup_place_name`
- `biz_favorite`
- `biz_product_comment`
- `biz_order.finish_time`
- `biz_order_review`
- `sys_user.credit_score`
- `biz_user_behavior`

---

## 11. 缺陷记录模板

建议你在验证过程中按下面格式记录：

```markdown
### 缺陷标题
- 模块：
- 环境：
- 操作步骤：
1.
2.
3.
- 预期结果：
- 实际结果：
- 是否可复现：
- 严重程度：
- 附件：
```

严重程度建议：

- `P0`：主链路阻断，无法演示
- `P1`：核心功能异常，但有绕行
- `P2`：次要功能异常
- `P3`：文案/UI/体验问题

---

## 12. 最终验收清单

全部打勾，才建议你对外说“完整验证完成”。

- [ ] MySQL、Redis、后端、管理端、用户端都能启动
- [ ] 数据库结构脚本和验收数据脚本执行成功
- [ ] `mvn test` 全绿
- [ ] 微信登录真实打通
- [ ] 实名校验成功与失败两条路径都验证过
- [ ] 未实名禁止发布验证过
- [ ] 动态分类在用户端与后台都验证过
- [ ] 腾讯地图选点、搜索、详情展示验证过
- [ ] 收藏功能验证过
- [ ] 评论功能验证过
- [ ] 聊天功能验证过
- [ ] 下单、确认收货验证过
- [ ] 买家评价卖家验证过
- [ ] 卖家评价买家验证过
- [ ] 信用分更新验证过
- [ ] 推荐列表验证过
- [ ] 后台分类管理验证过
- [ ] 后台评论管理验证过
- [ ] 后台实名视图验证过
- [ ] 验证截图和日志证据已留存

---

## 13. 当前版本建议结论写法

如果你完成了上面的验证，可以这样表述：

> 当前 `B` 范围版本已完成环境验证、后端自动化回归验证、用户端主链路验证、管理端治理验证和关键扩展能力验证；微信登录与腾讯地图为真实接入，实名校验为基于本地权威名单的数据源模拟实现，系统已具备课程场景下的完整演示和验收条件。

如果你愿意，我下一步可以继续替你做两件事之一：

1. 把这份验证手册再整理成一版更正式的“课程验收文档”
2. 按这份手册给你生成一份“验证记录表模板”方便你逐项打勾和截图举证
