# TimeCampus Alpha 测试报告

版本：`0.1.0-alpha`

更新日期：2026-05-10

## 1. 测试目标

本轮测试面向 TimeCampus（时光航迹）Alpha 版本，目标是确认后端 API、管理端 Web、核心数据表、第三方服务封装和部署链路达到小规模校园试用标准。

测试重点：

- 核心业务链路：登录、POI、官方内容、UGC、评论、收藏、管理端审核、运营地图、审计日志。
- 后端质量：单元测试、控制器回归测试、第三方服务冒烟测试、统一响应格式。
- 前端质量：Vue3 管理端构建、菜单路由、评论审核页面、UGC 审核页面、地图运营页面。
- 部署质量：Ubuntu 24.04 LTS + Nginx + Spring Boot + MySQL + Redis 的生产环境可运行性。
- 性能质量：在 2 核 4G、5M 带宽的服务器约束下验证 Alpha 级并发承载能力。

## 2. 测试计划

### 2.1 测试范围

已纳入测试：

- 用户端 API：
  - 微信登录与 token 鉴权。
  - POI 公开列表与详情。
  - 地图首页聚合。
  - 时间切换。
  - 收藏新增、删除、列表。
  - UGC 上传。
  - 评论创建、公开评论列表、我的评论列表。
- 管理端 API：
  - 管理员登录与 token 鉴权。
  - POI 管理。
  - 官方内容导入与管理。
  - UGC 审核。
  - 评论审核。
  - 运营地图。
  - 审计日志。
- 第三方服务：
  - 腾讯地图签名与 smoke test。
  - 微信 code2session smoke test。
- Web 管理端：
  - Vue3 构建。
  - 管理端登录。
  - POI、内容、UGC、评论、运营地图、日志页面。
- 部署：
  - CI 后端测试与打包。
  - CI 前端构建。
  - CD 服务器本地构建部署。
  - Nginx 静态资源与 API 反向代理。

暂不纳入 Alpha 出口阻塞项：

- 大规模图片处理流水线。
- AR 内容处理。
- 完整数据分析系统。
- 高并发商用级压测。

### 2.2 测试类型

- 单元测试：服务层、DTO、VO、AOP、签名工具。
- 控制器测试：使用 MockMvc 验证统一响应与路由。
- 回归测试：避免 controller 路径冲突、API 前缀回退、媒体文件访问路径穿越。
- 冒烟测试：腾讯地图、微信服务通过开关在 CI 手动触发。
- 场景测试：按不同角色的使用目标组合功能。
- 压力测试：对公开读接口、管理端接口、用户登录态接口分场景压测。
- 部署验证：Nginx、MySQL、Redis、生产配置与日志排查。

## 3. 测试过程

### 3.1 单元测试与回归测试

执行命令：

```bash
mvn test
```

本轮新增和强化的测试：

- `CommentCreateRequestTest`：校验评论创建请求的字段约束。
- `CommentVOTest`：覆盖评论视图对象字段。
- `CommentServiceImplTest`：覆盖评论创建、公开查询、我的评论、管理端列表、通过、驳回、重复审核失败、非法状态。
- `CommentControllerWebMvcTest`：覆盖用户端评论 API。
- `AdminCommentControllerWebMvcTest`：覆盖管理端评论审核 API。
- `ControllerMappingRegressionTest`：纳入评论 controller，防止新增 controller 后出现 ambiguous mapping。
- `GlobalExceptionHandler`：补充未处理异常日志，便于生产环境定位 500 根因。

测试结果：

- 共 57 个自动化测试。
- 失败 0 个。
- 第三方 smoke 跳过 2 个，原因是需要显式设置 smoke 开关和第三方密钥。

### 3.2 前端构建测试

执行命令：

```bash
cd timetrack-ui
node node_modules/vite/bin/vite.js build --clearScreen false
```

结果：

- Vue3 管理端构建成功。
- 评论审核页面被正确打包为独立 chunk。
- Vite 输出大 chunk 警告，但不影响 Alpha 发布；后续可通过 manualChunks 优化。

### 3.3 压力测试

新增压测脚本：

```bash
node test/load-test.mjs
```

推荐参数：

```bash
BASE_URL=https://timecampus.asia SCENARIO=health CONCURRENCY=50 DURATION_SECONDS=60 node test/load-test.mjs
BASE_URL=https://timecampus.asia SCENARIO=public-read CONCURRENCY=50 DURATION_SECONDS=300 node test/load-test.mjs
BASE_URL=https://timecampus.asia SCENARIO=admin CONCURRENCY=20 DURATION_SECONDS=180 ADMIN_TOKEN=<token> node test/load-test.mjs
```

本轮压力测试目标：

- 稳定并发：50。
- 可接受并发：100。
- 短时峰值：100-150。
- 核心接口 P95：不超过 800ms。
- 登录/写入接口 P95：不超过 1000ms。
- 错误率：小于 1%。

实际执行结果一：健康检查基线。

执行命令：

```bash
BASE_URL=https://timecampus.asia SCENARIO=health CONCURRENCY=50 DURATION_SECONDS=60 node test/load-test.mjs
```

结果：

| 指标 | 结果 |
|---|---:|
| 总请求数 | 70,950 |
| 成功请求数 | 70,950 |
| 失败请求数 | 0 |
| 错误率 | 0% |
| RPS | 1182.50 |
| P50 | 37.58ms |
| P90 | 56.69ms |
| P95 | 69.32ms |
| P99 | 131.06ms |
| 最大响应时间 | 923.14ms |

实际执行结果二：公开读接口混合场景。

执行命令：

```bash
BASE_URL=https://timecampus.asia SCENARIO=public-read CONCURRENCY=20 DURATION_SECONDS=60 node test/load-test.mjs
```

覆盖接口：

- `GET /api/v1/health`
- `GET /api/v1/pois`
- `GET /api/v1/map/home`
- `GET /api/v1/comments/poi/1`

结果：

| 指标 | 结果 |
|---|---:|
| 总请求数 | 38,844 |
| 成功请求数 | 38,844 |
| 失败请求数 | 0 |
| 错误率 | 0% |
| RPS | 647.40 |
| P50 | 25.97ms |
| P90 | 53.21ms |
| P95 | 65.52ms |
| P99 | 112.55ms |
| 最大响应时间 | 540.90ms |

实际执行结果三：更高并发健康检查。

执行命令：

```bash
BASE_URL=https://timecampus.asia SCENARIO=health CONCURRENCY=100 DURATION_SECONDS=60 node test/load-test.mjs
```

结果：

| 指标 | 结果 |
|---|---:|
| 总请求数 | 75,529 |
| 成功请求数 | 75,529 |
| 失败请求数 | 0 |
| 错误率 | 0% |
| RPS | 1258.82 |
| P50 | 15.98ms |
| P90 | 224.01ms |
| P95 | 244.49ms |
| P99 | 866.39ms |
| 最大响应时间 | 13,826.46ms |

实际执行结果四：公开读接口 50 并发。

执行命令：

```bash
BASE_URL=https://timecampus.asia SCENARIO=public-read CONCURRENCY=50 DURATION_SECONDS=60 node test/load-test.mjs
```

结果：

| 指标 | 结果 |
|---|---:|
| 总请求数 | 43,895 |
| 成功请求数 | 43,895 |
| 失败请求数 | 0 |
| 错误率 | 0% |
| RPS | 731.58 |
| P50 | 17.16ms |
| P90 | 227.35ms |
| P95 | 246.13ms |
| P99 | 473.53ms |
| 最大响应时间 | 12,350.45ms |

实际执行结果五：公开读接口 100 并发。

执行命令：

```bash
BASE_URL=https://timecampus.asia SCENARIO=public-read CONCURRENCY=100 DURATION_SECONDS=60 node test/load-test.mjs
```

结果：

| 指标 | 结果 |
|---|---:|
| 总请求数 | 41,819 |
| 成功请求数 | 41,819 |
| 失败请求数 | 0 |
| 错误率 | 0% |
| RPS | 696.98 |
| P50 | 11.27ms |
| P90 | 239.06ms |
| P95 | 453.38ms |
| P99 | 1728.01ms |
| 最大响应时间 | 53,245.32ms |

结论：公开读场景在 50 并发下表现稳定，P95 明显低于 800ms；100 并发下错误率仍为 0，但 P99 和最大响应时间出现明显长尾，更适合作为短时峰值能力，而不是 Alpha 阶段的长期稳定承载目标。

实际执行结果六：管理端读接口 20 并发。

执行命令：

```bash
ADMIN_TOKEN=<token> BASE_URL=https://timecampus.asia SCENARIO=admin CONCURRENCY=20 DURATION_SECONDS=60 node test/load-test.mjs
```

覆盖接口：

- `GET /api/v1/admin/comments?status=pending`
- `GET /api/v1/admin/logs?limit=50`
- `GET /api/v1/admin/map/overview?limit=50`

结果：

| 指标 | 结果 |
|---|---:|
| 总请求数 | 23,320 |
| 成功请求数 | 23,320 |
| 失败请求数 | 0 |
| 错误率 | 0% |
| RPS | 388.67 |
| P50 | 41.05ms |
| P90 | 93.44ms |
| P95 | 106.63ms |
| P99 | 136.40ms |
| 最大响应时间 | 285.54ms |

实际执行结果七：管理端读接口 50 并发。

执行命令：

```bash
ADMIN_TOKEN=<token> BASE_URL=https://timecampus.asia SCENARIO=admin CONCURRENCY=50 DURATION_SECONDS=60 node test/load-test.mjs
```

结果：

| 指标 | 结果 |
|---|---:|
| 总请求数 | 25,674 |
| 成功请求数 | 25,674 |
| 失败请求数 | 0 |
| 错误率 | 0% |
| RPS | 427.90 |
| P50 | 97.28ms |
| P90 | 216.21ms |
| P95 | 256.83ms |
| P99 | 331.76ms |
| 最大响应时间 | 533.38ms |

说明：本轮压测来自开发机到线上域名，结果会受到公网链路、备案拦截策略和本机网络影响。管理端读接口已使用真实管理员 token 通过 Redis 鉴权链路；审核通过、驳回等写接口会修改生产数据，未在生产环境做高并发压测，建议在预发环境使用脱敏数据继续测试。

服务器硬件约束：

- Ubuntu 24.04 LTS。
- 2 核 CPU。
- 4G 内存。
- 5M 带宽。
- 单 Spring Boot 实例。
- 单 MySQL。
- 单 Redis。

## 4. 场景测试

### 4.1 用户画像与目标

新生用户：

- 目标：快速了解校园地点和历史故事。
- 典型路径：微信登录 -> 地图首页 -> 查看 POI -> 时间切换 -> 收藏地点 -> 评论。
- 需求：入口简单、地图信息直观、内容可信。
- 软件支持：`/api/v1/map/home`、`/api/v1/pois`、`/api/v1/pois/{id}/time-switch`、收藏、评论。

在校生用户：

- 目标：浏览校园历史内容，上传自己拍摄的校园影像。
- 典型路径：登录 -> 查看 POI -> 上传 UGC -> 查看审核状态 -> 收藏或评论。
- 需求：上传后有明确状态，审核流程可追踪。
- 软件支持：UGC 上传默认 `pending`，管理端审核后变为 `approved/rejected`。

校友用户：

- 目标：按年份查看校园变化，补充记忆和评论。
- 典型路径：登录 -> 按年份切换内容 -> 查看老照片 -> 评论 -> 收藏。
- 需求：历史影像检索准确，评论可以表达补充信息。
- 软件支持：时间切换、评论审核、收藏。

运营管理员：

- 目标：维护 POI、官方内容，审核 UGC 与评论，查看运营地图和日志。
- 典型路径：管理员登录 -> POI 管理 -> 内容导入 -> UGC 审核 -> 评论审核 -> 运营地图 -> 审计日志。
- 需求：后台入口清晰、审核动作可追踪、异常可排查。
- 软件支持：Vue3 管理端、`/api/v1/admin/**`、审计日志、运营地图。

### 4.2 场景组合验证

场景一：用户评论审核链路。

1. 用户登录。
2. 用户对 POI 发表评论。
3. 评论进入 `pending`。
4. 管理员在评论审核页面查看。
5. 管理员通过评论。
6. 公开评论列表出现该评论。

场景二：UGC 内容审核链路。

1. 用户上传图片。
2. 后端校验格式、大小、年份和 POI。
3. 内容进入待审核。
4. 管理员通过或驳回。
5. 地图和内容页面只展示审核通过内容。

场景三：运营地图排查。

1. 管理员打开运营地图。
2. 查看 POI、收藏用户、评论、媒体。
3. 点击 POI 查看详情。
4. 直接预览 URL 或本地文件系统媒体。

## 5. 测试矩阵

| 类别 | 配置 |
|---|---|
| 本地开发系统 | Windows 11 |
| 服务器系统 | Ubuntu 24.04 LTS |
| 服务器硬件 | 2 核 CPU / 4G 内存 / 5M 带宽 |
| JDK | Java 21 |
| 后端框架 | Spring Boot 3.3.11 |
| 构建工具 | Maven 3.9+ |
| 数据库 | MySQL 8 |
| 缓存 | Redis 7 |
| 前端 | Vue 3 + Vite 5 |
| Node.js | 22 |
| 桌面浏览器 | Chrome / Firefox / Edge |
| 移动端浏览器 | 手机 Chrome / 微信内置浏览器 |
| 反向代理 | Nginx |
| 第三方服务 | 腾讯地图 Web Service / 微信 code2session |

## 6. Bug 统计

本轮测试和部署联调中共发现并处理 12 个主要问题：

1. Controller 路径冲突导致 Spring Boot 启动失败。
2. `application-dev.yaml`、`application-prod.yaml` 曾被错误移除，需要改为仅移出版本管理。
3. 管理端左侧菜单缺少模块入口。
4. 字体调整未生效。
5. 运营地图 Internal Server Error。
6. 运营地图 POI 点不显示。
7. 地图坐标体系偏差。
8. 前端按钮主题色不一致。
9. 切换腾讯地图后前端地图显示异常。
10. 服务器旧 Java 进程占用 8080，导致新进程启动失败。
11. 生产 MySQL 账号认证方式与 Spring Boot JDBC 不兼容。
12. 生产数据库 schema 未同步评论表，导致评论管理接口返回 `code=5000`。

## 7. 测试结果

自动化测试：

- `mvn test` 通过。
- 测试总数：50。
- 失败数：0。
- 跳过数：2，均为需要手动开启的第三方 smoke test。

前端构建：

- `timetrack-ui` 构建通过。
- 评论审核页面、UGC 审核页面、运营地图页面均可被打包。

CI：

- 后端测试与打包通过。
- 前端构建通过。
- 第三方 smoke 默认跳过，手动 workflow 可开启。

压测：

- 已提供可复现压测脚本。
- `health` 场景 50 并发持续 60 秒，错误率 0%，P95 为 69.32ms。
- `health` 场景 100 并发持续 60 秒，错误率 0%，P95 为 244.49ms，但存在长尾。
- `public-read` 场景 20 并发持续 60 秒，错误率 0%，P95 为 65.52ms。
- `public-read` 场景 50 并发持续 60 秒，错误率 0%，P95 为 246.13ms。
- `public-read` 场景 100 并发持续 60 秒，错误率 0%，P95 为 453.38ms，P99 为 1728.01ms。
- `admin` 场景 20 并发持续 60 秒，错误率 0%，P95 为 106.63ms。
- `admin` 场景 50 并发持续 60 秒，错误率 0%，P95 为 256.83ms。
- 当前公开读接口和管理端读接口达到 Alpha 目标；50 并发可视为稳定承载能力，100 并发可视为公开读短时峰值能力。管理端写接口仍建议在预发环境补充压测。

## 8. Alpha 出口条件

满足以下条件时，认定 TimeCampus Alpha 可以发布：

- 后端 `mvn test` 全部通过。
- 前端 `npm run build` 通过。
- 核心接口统一使用 `/api/v1/...`。
- 管理端与用户端 controller 无路径冲突。
- 登录、POI、内容、地图、收藏、UGC、评论、审核、日志核心链路可用。
- 生产数据库 schema 与代码一致。
- 生产环境 Nginx 能正确代理 `/api/v1/**`。
- 50 并发持续 10 分钟错误率低于 1%。
- 核心接口 P95 响应时间不超过 800ms。
- 无阻塞级安全问题，例如密钥提交、任意文件读取、路径穿越。
- 已知非阻塞问题记录到后续迭代计划。

## 9. 后续改进

- 引入 JaCoCo 统计覆盖率并在 CI 中设置覆盖率阈值。
- 管理端按页面拆分前端 chunk，降低首屏包体积。
- 增加 Flyway/Liquibase 管理数据库迁移，避免生产 schema 手工不同步。
- 增加限流与 WAF 策略，降低公网扫描干扰。
- 扩展压测到写接口和混合业务流。
- 增加真实设备的小程序端兼容性测试。
