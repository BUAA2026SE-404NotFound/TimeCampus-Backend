#!/usr/bin/env bash
set -euo pipefail

# ====================== 参数校验 ======================
if [ $# -lt 1 ]; then
    echo "用法：$0 <域名> [部署用户]"
    echo "示例：$0 api.timetrack.com root"
    exit 1
fi

SERVER_NAME="$1"
DEPLOY_USER="${2:-${SUDO_USER:-$USER}}"
REPO_DIR="/home/${DEPLOY_USER}/TimeTrack-Backend"
TEMPLATE_PATH="${REPO_DIR}/deploy/nginx/timetrack.conf.template"
NGINX_CONF="/etc/nginx/conf.d/timetrack.conf"

# ====================== 检查必要文件 ======================
if [ ! -f "$TEMPLATE_PATH" ]; then
    echo "错误：模板文件不存在：$TEMPLATE_PATH"
    exit 1
fi

if [ ! -d "$REPO_DIR" ]; then
    echo "错误：仓库目录不存在：$REPO_DIR"
    exit 1
fi

# ====================== 安装 Nginx ======================
echo "更新软件源并安装 Nginx..."
sudo apt-get update -qq
sudo apt-get install -y -qq nginx

# ====================== 备份旧配置 ======================
if [ -f "$NGINX_CONF" ]; then
    echo "备份旧 Nginx 配置..."
    sudo cp "$NGINX_CONF" "${NGINX_CONF}.backup.$(date +%Y%m%d%H%M%S)"
fi

# ====================== 生成配置（关键：sed 分隔符改为 #） ======================
echo "生成 Nginx 配置文件..."
sudo sed \
  -e "s#__SERVER_NAME__#${SERVER_NAME}#g" \
  -e "s#__DEPLOY_USER__#${DEPLOY_USER}#g" \
  "$TEMPLATE_PATH" | sudo tee "$NGINX_CONF" >/dev/null

# ====================== 检查配置是否生成成功 ======================
if [ ! -f "$NGINX_CONF" ]; then
    echo "错误：Nginx 配置生成失败！"
    exit 1
fi

# ====================== 禁用默认站点（避免冲突） ======================
echo "禁用 Nginx 默认站点..."
sudo rm -f /etc/nginx/sites-enabled/default

# ====================== 测试配置 ======================
echo "测试 Nginx 配置..."
sudo nginx -t

# ====================== 启动并重载 ======================
echo "启动并重启 Nginx..."
sudo systemctl enable nginx
sudo systemctl start nginx
sudo systemctl reload nginx

# ====================== 完成 ======================
echo -e "\n✅ 部署成功！"
echo "域名：${SERVER_NAME}"
echo "部署用户：${DEPLOY_USER}"
echo "Nginx 配置：${NGINX_CONF}"