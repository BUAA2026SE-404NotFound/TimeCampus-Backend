#!/usr/bin/env bash
set -euo pipefail

SOURCE_JAR="${1:?jar path is required}"
APP_DIR="${APP_DIR:-$HOME/app}"
APP_JAR="$APP_DIR/app.jar"
BACKUP_DIR="$APP_DIR/backups"
SERVICE_NAME="${SERVICE_NAME:-timecampus-backend}"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8080/api/v1/health}"
BACKUP_JAR=""

case "$APP_DIR" in
  "$HOME/app") ;;
  *) echo "Refusing to deploy outside $HOME/app" >&2; exit 1 ;;
esac

mkdir -p "$APP_DIR" "$BACKUP_DIR"
if [ -f "$APP_JAR" ]; then
  BACKUP_JAR="$BACKUP_DIR/app-$(date +%Y%m%d%H%M%S).jar"
  cp "$APP_JAR" "$BACKUP_JAR"
fi
install -m 0644 "$SOURCE_JAR" "$APP_JAR.next"
mv -f "$APP_JAR.next" "$APP_JAR"

sudo systemctl restart "$SERVICE_NAME"
for _ in $(seq 1 60); do
  if curl -fsS "$HEALTH_URL" >/dev/null; then
    find "$BACKUP_DIR" -maxdepth 1 -type f -name 'app-*.jar' \
      -printf '%T@ %p\n' | sort -nr | tail -n +6 | cut -d' ' -f2- | xargs -r rm -f
    echo "Backend artifact is healthy"
    exit 0
  fi
  sleep 1
done

if [ -n "$BACKUP_JAR" ] && [ -f "$BACKUP_JAR" ]; then
  cp "$BACKUP_JAR" "$APP_JAR"
  sudo systemctl restart "$SERVICE_NAME"
fi
echo "Backend health check failed; previous jar restored" >&2
exit 1
