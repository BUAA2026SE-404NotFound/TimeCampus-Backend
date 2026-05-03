#!/usr/bin/env bash
set -euo pipefail

SERVER_NAME="${1:-_}"
DEPLOY_USER="${2:-${SUDO_USER:-$USER}}"
REPO_DIR="/home/${DEPLOY_USER}/TimeTrack-Backend"
TEMPLATE_PATH="${REPO_DIR}/deploy/nginx/timecampus.conf.template"
NGINX_CONF="/etc/nginx/sites-available/timecampus.conf"

if [ ! -f "$TEMPLATE_PATH" ]; then
  echo "Template not found: $TEMPLATE_PATH"
  echo "Run this script after cloning the repository to $REPO_DIR."
  exit 1
fi

sudo apt-get update
sudo apt-get install -y nginx

sudo sed \
  -e "s/__SERVER_NAME__/${SERVER_NAME}/g" \
  -e "s/__DEPLOY_USER__/${DEPLOY_USER}/g" \
  "$TEMPLATE_PATH" | sudo tee "$NGINX_CONF" >/dev/null

sudo ln -sf "$NGINX_CONF" /etc/nginx/sites-enabled/timecampus.conf
sudo rm -f /etc/nginx/sites-enabled/default

sudo nginx -t
sudo systemctl enable nginx
sudo systemctl reload nginx

echo "Nginx configured for server_name=${SERVER_NAME}, deploy_user=${DEPLOY_USER}"
