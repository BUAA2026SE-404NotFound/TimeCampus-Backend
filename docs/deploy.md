# 部署指南

配置：Ubuntu 24.04 LTS，2 核 4G 内存，5M 带宽。
部署方式为服务器本地 `git pull origin main` 后构建后端 Jar 与前端 `dist`，Nginx 对外提供管理端静态文件并反向代理 `/api/v1/**` 到 Spring Boot。

## 1. 安装基础软件

```bash
sudo apt update
sudo apt install -y git curl unzip nginx mysql-client redis-tools
sudo apt install -y openjdk-21-jdk maven

curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt install -y nodejs

java -version
mvn -version
node -v
npm -v
```

## 2. 准备仓库与目录

```bash
cd ~
git clone git@github.com:<owner>/<repo>.git TimeTrack-Backend
cd ~/TimeTrack-Backend
mkdir -p ~/TimeTrack-Backend/app/config
mkdir -p ~/TimeTrack-Backend/app/ui
mkdir -p /opt/timecampus/storage/uploads
```

如果 GitHub SSH 拉取尚未配置，需要先在服务器生成 SSH key，并把公钥加入 GitHub Deploy keys：

```bash
ssh-keygen -t ed25519 -C "timecampus-prod"
cat ~/.ssh/id_ed25519.pub
ssh -T git@github.com
```

## 3. 生产配置

生产环境不要提交真实 `application-prod.yaml`。在服务器创建：

```bash
cp ~/TimeTrack-Backend/timetrack-server/src/main/resources/application-prod-example.yaml \
   ~/TimeTrack-Backend/app/config/application-prod.yaml
nano ~/TimeTrack-Backend/app/config/application-prod.yaml
```

最小必填项：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/timetrack?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: your_mysql_user
    password: your_mysql_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: your_redis_password
      database: 0
      timeout: 3s

server:
  port: 8080

storage:
  local-root-dir: /opt/timecampus/storage/uploads
  max-file-size-mb: 10

wechat:
  appid: your_wechat_appid
  secret: your_wechat_secret

tencent-map:
  key: your_tencent_map_key
  sk: your_tencent_map_sk
```

注意：

- 本地默认 profile 是 `dev`，来自 `application.yaml` 的 `${SPRING_PROFILES_ACTIVE:dev}`。
- 服务器由 CD 脚本用 `java -jar app.jar --spring.profiles.active=prod` 启动。
- Spring Boot 会自动读取当前目录下的 `config/application-prod.yaml`，也就是 `~/TimeTrack-Backend/app/config/application-prod.yaml`。

## 4. 初始化数据库

在 MySQL 中创建数据库后执行 schema：

```bash
mysql -h 127.0.0.1 -u your_mysql_user -p timetrack < \
  ~/TimeTrack-Backend/timetrack-server/src/main/resources/sql/schema.sql
```

开发演示数据可选，不建议在生产直接执行：

```bash
mysql -h 127.0.0.1 -u your_mysql_user -p timetrack < \
  ~/TimeTrack-Backend/timetrack-server/src/main/resources/sql/dev-seed-buaa.sql
```

## 5. 配置 Nginx

仓库提供模板：

```text
deploy/nginx/timecampus.conf.template
```

一键安装并启用：

```bash
cd ~/TimeTrack-Backend
chmod +x deploy/scripts/install-nginx-timecampus.sh

# 无域名时使用 _
./deploy/scripts/install-nginx-timecampus.sh _ ubuntu

# 有域名时使用你的域名
./deploy/scripts/install-nginx-timecampus.sh example.com ubuntu
```

生成后的 Nginx 行为：

- `/`：服务 `~/TimeTrack-Backend/app/ui` 下的 Vue 管理端。
- `/api/v1/**`：反向代理到 `127.0.0.1:8080`。
- `/actuator/health`：反向代理到后端健康检查。
- `/swagger-ui/**` 与 `/v3/api-docs`：开发阶段可访问，生产不需要时可从模板中删除。

检查并重载：

```bash
sudo nginx -t
sudo systemctl reload nginx
sudo systemctl status nginx --no-pager
```

腾讯云安全组建议开放：

- TCP 22：SSH
- TCP 80：HTTP
- TCP 443：HTTPS，如后续配置证书

## 6. GitHub Actions CD 所需 Secrets

仓库 Settings -> Secrets and variables -> Actions 添加：

```text
DEPLOY_HOST       服务器公网 IP 或域名
DEPLOY_USERNAME   例如 ubuntu
DEPLOY_PASSWORD   服务器 SSH 密码
```

当前 CD 使用密码登录。如果改为 SSH key，建议后续把 workflow 切到 `key` 方式，并禁用服务器密码登录。

## 7. 手动部署命令

GitHub Actions CD 执行的核心逻辑如下，必要时可在服务器手动运行：

```bash
cd ~/TimeTrack-Backend
git fetch origin main
git checkout main
git pull --ff-only origin main

mvn -B -pl timetrack-server -am package -DskipTests

cd timetrack-ui
npm ci
npm run build
cd ..

mkdir -p ~/TimeTrack-Backend/app/ui
cp timetrack-server/target/*.jar ~/TimeTrack-Backend/app/app.jar
rm -rf ~/TimeTrack-Backend/app/ui/*
cp -R timetrack-ui/dist/. ~/TimeTrack-Backend/app/ui/

cd ~/TimeTrack-Backend/app
nohup java -jar app.jar --spring.profiles.active=prod > app.log 2>&1 &
echo $! > app.pid
```

## 8. 常用排查

后端日志：

```bash
tail -n 200 ~/TimeTrack-Backend/app/app.log
```

端口检查：

```bash
ss -lntp | grep -E ':80|:8080'
```

健康检查：

```bash
curl -i http://127.0.0.1:8080/api/v1/health
curl -i http://127.0.0.1/actuator/health
```

Nginx 日志：

```bash
sudo tail -n 100 /var/log/nginx/access.log
sudo tail -n 100 /var/log/nginx/error.log
```
