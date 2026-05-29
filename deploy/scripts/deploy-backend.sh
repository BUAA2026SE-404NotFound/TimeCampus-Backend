#!/usr/bin/env bash
# Deploy TimeCampus backend on the production server layout:
#   ~/TimeCampus-Backend  backend repository
#   ~/app                 runtime directory: app.jar, config/, logs, backups
#
# The script is intended to be run directly on the server.

set -Eeuo pipefail

REPO_DIR="${REPO_DIR:-$HOME/TimeCampus-Backend}"
APP_DIR="${APP_DIR:-$HOME/app}"
SERVICE_NAME="${SERVICE_NAME:-timecampus-backend}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
MAVEN_BIN="${MAVEN_BIN:-mvn}"
JAVA_BIN="${JAVA_BIN:-/usr/bin/java}"
SKIP_GIT_PULL="${SKIP_GIT_PULL:-false}"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8080/api/v1/health}"
HEALTH_TIMEOUT_SECONDS="${HEALTH_TIMEOUT_SECONDS:-45}"

APP_JAR="$APP_DIR/app.jar"
CONFIG_DIR="$APP_DIR/config"
BACKUP_DIR="$APP_DIR/backup"
TARGET_DIR="$REPO_DIR/timecampus-server/target"
SYSTEMD_UNIT="/etc/systemd/system/${SERVICE_NAME}.service"

log() {
    printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

fail() {
    printf 'ERROR: %s\n' "$*" >&2
    exit 1
}

require_command() {
    command -v "$1" >/dev/null 2>&1 || fail "Required command not found: $1"
}

find_latest_jar() {
    find "$TARGET_DIR" -maxdepth 1 -type f \
        -name 'timecampus-server-*.jar' \
        ! -name '*.original' \
        -printf '%T@ %p\n' |
        sort -nr |
        awk 'NR == 1 { sub(/^[^ ]+ /, ""); print }'
}

ensure_systemd_unit() {
    if [ -f "$SYSTEMD_UNIT" ]; then
        return
    fi

    log "Creating systemd unit: $SYSTEMD_UNIT"
    sudo tee "$SYSTEMD_UNIT" >/dev/null <<UNIT
[Unit]
Description=TimeCampus Spring Boot Backend
After=network-online.target redis-server.service
Wants=network-online.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$APP_DIR
EnvironmentFile=-$HOME/TimeCampus/.env
Environment=SPRING_PROFILES_ACTIVE=$SPRING_PROFILE
Environment=SERVER_PORT=8080
Environment=SERVER_ADDRESS=127.0.0.1
Environment=TIMECAMPUS_MAX_FILE_SIZE_MB=20
Environment="JAVA_TOOL_OPTIONS=-Xms256m -Xmx768m"
ExecStart=$JAVA_BIN -jar $APP_JAR --server.address=127.0.0.1 --spring.config.additional-location=file:$CONFIG_DIR/
Restart=always
RestartSec=5
SuccessExitStatus=143
StandardOutput=append:$APP_DIR/app.log
StandardError=append:$APP_DIR/app.log

[Install]
WantedBy=multi-user.target
UNIT
    sudo systemctl daemon-reload
    sudo systemctl enable "$SERVICE_NAME"
}

wait_for_health() {
    require_command curl
    log "Checking health endpoint: $HEALTH_URL"

    local waited=0
    until curl -fsS --max-time 3 "$HEALTH_URL" >/dev/null; do
        if [ "$waited" -ge "$HEALTH_TIMEOUT_SECONDS" ]; then
            sudo journalctl -u "$SERVICE_NAME" -n 120 --no-pager || true
            fail "Health check failed: $HEALTH_URL"
        fi
        sleep 1
        waited=$((waited + 1))
    done
    log "Health check passed"
}

require_command git
require_command "$MAVEN_BIN"
require_command "$JAVA_BIN"
require_command find
require_command sort
require_command awk
require_command sudo

[ -d "$REPO_DIR" ] || fail "Backend repository not found: $REPO_DIR"
[ -f "$REPO_DIR/pom.xml" ] || fail "Missing Maven pom.xml in $REPO_DIR"

mkdir -p "$CONFIG_DIR" "$BACKUP_DIR"
if [ ! -f "$CONFIG_DIR/application.yaml" ] && [ ! -f "$CONFIG_DIR/application-${SPRING_PROFILE}.yaml" ]; then
    fail "Missing config file: expected $CONFIG_DIR/application.yaml or application-${SPRING_PROFILE}.yaml"
fi

cd "$REPO_DIR"

if [ "$SKIP_GIT_PULL" != "true" ]; then
    log "Updating backend repository"
    git fetch origin
    current_branch="$(git rev-parse --abbrev-ref HEAD)"
    git pull --ff-only origin "$current_branch"
else
    log "SKIP_GIT_PULL=true; using existing repository contents"
fi

log "Building backend jar"
"$MAVEN_BIN" -B -pl timecampus-server -am clean package -DskipTests

latest_jar="$(find_latest_jar)"
[ -n "$latest_jar" ] || fail "No jar found under $TARGET_DIR"
log "Latest jar: $latest_jar"

if [ -f "$APP_JAR" ]; then
    backup_file="$BACKUP_DIR/app-$(date '+%Y%m%d%H%M%S').jar"
    log "Backing up current jar to $backup_file"
    cp "$APP_JAR" "$backup_file"
fi

log "Copying jar to $APP_JAR"
cp "$latest_jar" "$APP_JAR"

ensure_systemd_unit

log "Restarting $SERVICE_NAME"
sudo systemctl daemon-reload
sudo systemctl restart "$SERVICE_NAME"
wait_for_health

log "Deployment successful"
