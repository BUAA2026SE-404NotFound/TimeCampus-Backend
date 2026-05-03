# 时光航迹后端与管理端

“时光航迹”（TimeCampus / Time Track）是面向校园历史影像浏览与共创的小程序后端项目，并配套提供 Web 管理端。当前实现聚焦 Alpha 阶段最小可交付内容：微信登录、POI 管理、官方内容导入、地图聚合、时间切换、收藏、UGC 上传与审核、审计日志、腾讯地图 WebService 封装、管理端web页面。
团队使用 ApiFox 进行 API 文档管理，后续会持续完善接口定义、示例请求与响应、错误码说明等内容。

## 当前状态

- 后端：Spring Boot 3.3.11，Java 21，多模块 Maven 工程。
- 管理端：Vue 3 + Vite + Element Plus。
- API 路径统一为 `/api/v1/...`。
- 响应格式统一为：

```json
{"code":0,"message":"ok","data":{}}
```

状态码与状态信息定义在 `timetrack-common` 模块的 `ResponseCode` 中

## 技术栈

- Java 21
- Spring Boot 3.3.11
- Maven 3.9+
- MyBatis
- MySQL 8
- Redis 7
- Vue 3
- Vite
- Element Plus
- 腾讯地图 WebService API
- 微信小程序 code2Session

## 项目结构

```text
time-track-backend
├─ timetrack-common       # 通用基础设施：统一响应、错误码、业务异常、全局异常、请求 ID
├─ timetrack-pojo         # DTO / Entity / VO / 模型常量
├─ timetrack-server       # Spring Boot 服务端：Controller / Service / Mapper / 配置
├─ timetrack-ui           # Vue3 管理端
├─ docs                   # 数据库与重构说明文档（暂无）
└─ .github/workflows      # CI/CD
```

后端 controller 已按使用端拆分：

```text
timetrack-server/src/main/java/com/notfound/timetrackserver/controller
├─ admin                  # 管理端接口
└─ user                   # 用户端/小程序接口
```

## 后端能力

### 用户端

- 微信小程序登录：`POST /api/v1/auth/wechat/login`
- 当前用户信息：`GET /api/v1/me`
- POI 公开查询：`GET /api/v1/pois`、`GET /api/v1/pois/{id}`
- 内容查询：`GET /api/v1/pois/{id}/contents`、`GET /api/v1/contents/{id}`
- 时间切换：`GET /api/v1/pois/{id}/time-switch?year=YYYY`
- 收藏：`POST /api/v1/favorites/{targetType}/{targetId}`、`DELETE /api/v1/favorites/{targetType}/{targetId}`、`GET /api/v1/favorites`
- UGC 上传：`POST /api/v1/ugc`
- 地图聚合与辅助：`GET /api/v1/map/home`、`GET /api/v1/map/reverse-geocode`、`GET /api/v1/map/poi-search`

### 管理端

- 管理员登录/登出：`POST /api/v1/admin/login`、`POST /api/v1/admin/logout`
- POI 管理：`/api/v1/admin/pois`
- 官方内容批量导入：`POST /api/v1/admin/contents/batch-import`
- UGC 审核：`GET /api/v1/admin/ugc`、`POST /api/v1/admin/ugc/{id}/approve`、`POST /api/v1/admin/ugc/{id}/reject`
- 审计日志查看：`GET /api/v1/admin/logs`

## 管理端能力

管理端位于 `timetrack-ui`，当前页面包括：

- 登录
- 运营首页
- POI 管理
- 运营地图：查看 POI 点位及收藏、评论、媒体、UGC 概览
- 官方内容列表与批量导入
- UGC 审核
- 腾讯地图辅助搜索
- 审计日志查看

管理端默认通过 Vite dev server 代理访问后端：

```text
/api/v1 -> http://localhost:8080/api/v1
```

前端代码中 Axios 的 baseURL 为 `/api/v1`。

## 数据库

建库脚本是数据库结构的真源：

- [`timetrack-server/src/main/resources/sql/schema.sql`](timetrack-server/src/main/resources/sql/schema.sql)

当前核心表：

- `user`
- `admin`
- `poi`
- `media`
- `favorite`
- `comment`
- `log`

为了本地开发和 CI 稳定，脚本中未使用物理外键约束，关联关系由应用层校验。

## 关键设计

### Token 鉴权

- 用户端使用 `Authorization: Bearer <token>`
- 管理端使用 `Authorization: Bearer <token>`
- token 当前存储在 Redis 中
- `/api/v1/admin/**` 需要管理员 token
- `/api/v1/me`、收藏、UGC 上传等用户行为接口需要用户 token

### UGC 审核流程

- 上传后：`review_status = pending`
- 审核通过：`review_status = approved`
- 审核驳回：`review_status = rejected`，必须填写驳回原因

### AOP 时间填充

`timetrack-server` 使用 `TimeFillAspect` 拦截 MyBatis mapper 的 `insert*` / `update*` 方法：

- 新增时自动填充 `createTime`、`updateTime`
- 修改时自动刷新 `updateTime`

Mapper XML 显式写入 `create_time` / `update_time`，避免依赖数据库隐式行为。

### 审计日志

关键写操作会写入 `log` 表：

- POI 新增、修改、删除
- 官方内容批量导入
- UGC 上传
- UGC 通过、驳回

管理端通过 `/api/v1/admin/logs` 查询。

### 腾讯地图 Sig

腾讯地图配置支持 Key + SK：

```yaml
tencent-map:
  key: ${TENCENT_MAP_KEY:}
  sk: ${TENCENT_MAP_SK:}
```

当 `sk` 为空时，后端使用普通 Key 请求；当 `sk` 存在时，后端会根据腾讯位置服务规则按参数名排序、拼接原始参数并追加 `sig`。签名逻辑位于 `TencentMapSignature`，并覆盖了单元测试。

### common / pojo 模块重构

`common` 和 `pojo` 已清理为纯库模块：

- 删除库模块中的 Spring Boot 启动类
- `common` 不再暴露未知异常原始消息
- `pojo` 新增模型常量与更严格的 DTO 校验
- 删除临时兼容访问器，如 `createdAt/updatedAt/imageUrl`

详见：

- [`docs/common-pojo-refactor.md`](docs/common-pojo-refactor.md)

## 配置项

主要配置文件：

- `timetrack-server/src/main/resources/application.yaml`
- `timetrack-server/src/main/resources/application-dev-example.yaml`
- `timetrack-server/src/main/resources/application-prod.yaml`

`application-dev.yaml` 包含本地数据库密码、Redis 密码、微信密钥、腾讯地图 SK 等敏感信息，已取消版本管理。首次本地运行时请从示例文件复制：

```shell
copy timetrack-server\src\main\resources\application-dev-example.yaml timetrack-server\src\main\resources\application-dev.yaml
```

Linux/macOS：

```shell
cp timetrack-server/src/main/resources/application-dev-example.yaml timetrack-server/src/main/resources/application-dev.yaml
```

然后在本机的 `application-dev.yaml` 或环境变量中填入真实值。不要提交 `application-dev.yaml`。

### 最小运行配置

后端启动所需最小配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/timetrack?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: your-db-password
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: your-redis-password
```

如需微信登录，需要配置：

```yaml
wechat:
  appid: your-wechat-appid
  secret: your-wechat-secret
```

如需腾讯地图接口，需要配置：

```yaml
tencent-map:
  key: your-tencent-key
  sk: your-tencent-sk
```

也可以通过环境变量注入敏感信息：

```text
DB_USERNAME
DB_PASSWORD
REDIS_HOST
REDIS_PORT
REDIS_DATABASE
REDIS_PASSWORD
WECHAT_APPID
WECHAT_SECRET
TENCENT_MAP_KEY
TENCENT_MAP_SK
```

本地开发需要 MySQL 和 Redis 可用。

## 快速启动

### 后端

```shell
mvn clean test
mvn -pl timetrack-server -am spring-boot:run
```

服务默认端口：

```text
http://localhost:8080
```

Swagger UI：

```text
http://localhost:8080/swagger-ui/index.html
```

### 管理端

```shell
cd timetrack-ui
npm install
npm run dev
```

默认地址：

```text
http://localhost:8081
```

构建：

```shell
npm run build
```

## 测试

默认测试：

```shell
mvn test
```

当前覆盖内容包括：

- common 统一响应、异常处理、请求 ID
- pojo DTO 校验与实体字段契约
- 用户登录服务
- 收藏逻辑
- UGC 上传与审核规则
- AOP 时间填充
- `/api/v1` 回归接口
- 管理端日志接口
- 腾讯地图 Sig 签名
- 微信 code2Session 文本响应兼容

第三方真实冒烟测试默认跳过，需显式开启。

腾讯地图冒烟测试：

```shell
set RUN_TENCENT_SMOKE=true
set TENCENT_MAP_KEY=your-key
set TENCENT_MAP_SK=your-sk
mvn -pl timetrack-server -am "-Dtest=com.notfound.timetrackserver.smoke.TencentMapSmokeTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

微信冒烟测试：

```shell
set RUN_WECHAT_SMOKE=true
set WECHAT_APPID=your-appid
set WECHAT_SECRET=your-secret
mvn -pl timetrack-server -am "-Dtest=com.notfound.timetrackserver.smoke.WechatAuthSmokeTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

## CI/CD

- CI：`.github/workflows/ci.yml`
  - 执行 `mvn clean test`
  - 打包服务模块
  - 上传构建产物
- CD：`.github/workflows/cd.yml`
  - 当前面向 `main` 分支部署

## 分支策略

- `main`：稳定分支
- `release`：预发布分支
- `dev`：日常集成分支
- `feature/*`、`fix/*`、`chore/*`：功能、修复、维护分支

## 未来迭代方向

### 认证与权限

- 将当前 Redis token 升级为更完整的 JWT + Refresh Token 或统一会话模型。
- 管理端增加 RBAC：角色、菜单、按钮级权限。
- 增加登录失败次数限制、验证码、管理员密码重置流程。

### 数据模型

- 将当前 `media` 演进为更贴近 Alpha 文档的 `content_item` / `content_review` 模型。
- 明确 `publish_status` 与 `review_status` 的职责边界。
- 增加数据库迁移工具，如 Flyway 或 Liquibase。
- 生产环境恢复物理外键或增加更完整的应用层一致性检查。

### 内容与 UGC

- 增加 UGC 上传频率限制，目前可基于 Redis 计数器实现。
- 增加图片安全审核、敏感内容检测、重复图片检测。
- 支持视频、音频、文档等更多内容类型。
- 增加内容版本管理和审核历史。

### 存储

- 当前文件上传支持本地存储回退，后续可完善 COS 挂载路径、访问 URL、清理策略。
- 增加缩略图生成、图片压缩、EXIF 清洗。
- 增加对象存储可用性冒烟测试。

### 地图能力

- 增加腾讯地图 API 响应缓存，降低配额消耗。
- 对腾讯地图 API status 做统一错误映射。
- 支持地点搜索结果一键转 POI 草稿。
- 增加地图坐标合法性和边界校验工具。

### 管理端

- 完善分页、排序、批量操作。
- 增加用户管理、评论审核、日志详情页。
- 接入 ECharts，做内容增长、审核效率、POI 热度等运营看板。
- 增加 OpenAPI 生成的前端 API 类型。

### 测试与质量

- 增加 Testcontainers，使用临时 MySQL/Redis 做 mapper 与集成测试。
- 增加 JaCoCo 覆盖率门禁。
- 将第三方冒烟测试纳入独立 CI job，按手动或定时触发。
- 增加管理端组件测试和 Playwright 冒烟测试。

### API 文档

- 使用 ApiFox 平台进行管理
- https://localhost:8080/swagger-ui/index.html 仅供开发阶段使用。
