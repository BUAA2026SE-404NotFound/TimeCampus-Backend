# TimeCampus Alpha 交付说明

版本：`0.1.0-alpha`

日期：2026-05-10

## 1. 交付范围

本版本面向 Alpha 阶段的小规模校园试用，目标是完成时光航迹后端和管理端的最小可交付闭环。

已纳入本次交付：

- 用户端：微信登录、当前用户信息、POI 查询、地图首页、时间切换、收藏、UGC 上传、评论创建与查询。
- 管理端：管理员登录、POI 管理、官方内容导入、UGC 审核、评论审核、运营地图、审计日志查看。
- 地图服务：腾讯地图 WebService 签名、逆地理、地点搜索。
- 文件服务：上传文件校验、服务器本地 COS 挂载目录写入、绝对路径媒体读取、路径穿越防护。
- 部署：Ubuntu 24.04 LTS、Nginx、MySQL、Redis、服务器本地构建与 `prod` profile 运行。
- 测试：单元测试、控制器回归测试、第三方服务冒烟测试说明、压力测试报告。

## 2. 版本号

- Maven 根工程版本：`0.1.0-alpha`
- 管理端外部仓库 `timecampus-ui` 版本：`0.1.0-alpha`

子模块继承 Maven 根工程版本。

## 3. 交付前检查清单

- 后端 `mvn test` 通过。
- 管理端 `npm run build` 通过。
- `application-dev.yaml` 与 `application-prod.yaml` 不进入版本管理。
- 示例配置包含最小启动项：MySQL、Redis、微信、腾讯地图、存储目录。
- 生产存储目录默认指向 `/home/ubuntu/cos`。
- API 路径保持 `/api/v1/...`。
- 管理端静态资源由 Nginx 提供，`/api/v1/**` 反向代理到 Spring Boot。
- 敏感密钥仅保存在本地配置、服务器配置或 GitHub Secrets。

## 4. 说明

- Alpha 版本以校园小规模试用为目标。
- UGC 图片处理仅完成基础校验和元数据入库，尚未接入图片安全审核、压缩和缩略图流水线。
- 管理端 RBAC 仍为后续迭代方向，当前以管理员 token 控制 `/api/v1/admin/**`。
- 第三方真实冒烟测试需要显式配置环境变量后手动执行，默认 CI 不直接消耗腾讯地图和微信接口配额。

## 5. 发布建议

建议发布前按以下顺序确认：

1. 在本地执行 `mvn test`。
2. 在外部 `timecampus-ui` 仓库下执行 `npm run build`，并将 `dist` 发布到 Nginx 静态目录。
3. 在测试库执行 `schema.sql`，必要时执行 `dev-seed-buaa.sql`。
4. 在服务器 `app/config/application-prod.yaml` 中确认 MySQL、Redis、微信、腾讯地图和存储目录配置。
5. 通过 `curl http://127.0.0.1:8080/api/v1/health` 确认后端健康。
6. 通过浏览器访问管理端，完成登录、POI、内容、评论、UGC、运营地图和日志页面冒烟检查。
