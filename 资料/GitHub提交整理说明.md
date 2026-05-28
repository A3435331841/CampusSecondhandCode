# GitHub 提交整理说明

## 一、整理目标

本项目建议以 `CampusSecondhandCode` 作为对外提交仓库，公开内容应聚焦“校园二手交易平台”的源码、必要配置说明、数据库脚本和中文项目文档。个人草稿、内部计划、真实密钥、构建产物和本地工具包不建议上传。

## 二、建议保留的仓库结构

```text
CampusSecondhandCode/
├── backend/                 后端 Spring Boot 源码
├── frontend-app/            Uni-app 用户端源码
├── frontend-admin/          Vue 管理端源码
├── 资料/                    中文项目文档
├── README.md                项目总说明
└── .gitignore               Git 忽略规则
```

## 三、中文文档建议

公开仓库中的文档建议全部使用中文，命名清晰、用途明确。推荐保留以下文档：

- `README.md`：项目简介、技术栈、目录结构、启动方式
- `资料/需求规格说明书.md`：需求范围和功能模块
- `资料/系统设计说明书_V2.1.md`：系统架构、数据库设计、接口设计
- `资料/环境配置与下载指南.md`：JDK、MySQL、Redis、Node.js、微信开发者工具配置
- `资料/第三阶段工作小结.md`：小组阶段总结
- `资料/肖家鑫_第三阶段工作小结.md`：个人阶段总结
- `资料/B_scope_实现说明.md`：如果需要说明当前 B 范围能力，可整理成中文版本后保留
- `资料/B_scope_完整验证手册.md`：如果需要验收复现，可整理成中文版本后保留

`docs/superpowers/` 下的英文设计和执行计划更像内部过程文件，不建议作为公开项目文档直接上传。

## 四、敏感信息处理规则

公开仓库中不应出现以下信息：

- 微信小程序真实 `AppSecret`
- 腾讯地图真实 Key
- 数据库真实账号和密码
- Redis 真实密码或公网连接地址
- 个人手机号、身份证、真实学生隐私数据
- 服务器公网 IP、远程登录账号和密钥

推荐写法：

```yaml
wechat:
  mini-app:
    app-id: ${WECHAT_MINI_APP_ID:}
    app-secret: ${WECHAT_MINI_APP_SECRET:}

tencent:
  map:
    key: ${TENCENT_MAP_KEY:}
```

## 五、不建议上传的文件类型

- `target/`
- `node_modules/`
- `dist/`
- `unpackage/`
- `.idea/`
- `.vscode/`
- `.env`
- `*.zip`
- `*.rar`
- `*.log`
- 个人汇报草稿
- 内部 AI 执行计划
- 本地工具包

这些内容已经在 `.gitignore` 中进行规避。

## 六、提交前建议命令

提交前建议先检查暂存区：

```powershell
git status --short
```

如果发现不应上传的文件，先取消暂存或移动到 `待处理_不上传文件/`：

```powershell
git restore --staged <文件路径>
```

确认无敏感内容后再提交：

```powershell
git add .
git status --short
git commit -m "整理项目文档与提交配置"
```

## 七、后续建议

1. 将英文内部计划文档整理为中文实现说明或移入 `待处理_不上传文件/`。
2. 检查 `application.yml`、SQL 脚本和 README，确保没有真实密钥。
3. 删除或忽略构建产物和依赖目录。
4. 保留能支撑答辩和验收的中文文档，避免仓库里混入过多过程草稿。
