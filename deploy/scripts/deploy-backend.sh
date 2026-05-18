#!/usr/bin/env bash
# Deploy TimeCampus backend for the production server layout:
#   ~/TimeCampus-Backend  backend repository
#   ~/app                 backend runtime directory
#   ~/cos                 mounted or synced object storage directory
#   ~/TimeCampus-Portal   separate React portal, not touched by this script

set -Eeuo pipefail

REPO_DIR="${REPO_DIR:-$HOME/TimeCampus-Backend}"
APP_DIR="${APP_DIR:-$HOME/app}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
JAVA_BIN="${JAVA_BIN:-java}"
MAVEN_BIN="${MAVEN_BIN:-mvn}"
SKIP_GIT_PULL="${SKIP_GIT_PULL:-false}"
STOP_TIMEOUT_SECONDS="${STOP_TIMEOUT_SECONDS:-20}"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8080/api/v1/health}"
HEALTH_TIMEOUT_SECONDS="${HEALTH_TIMEOUT_SECONDS:-30}"

PID_FILE="${PID_FILE:-$APP_DIR/app.pid}"
LOG_FILE="${LOG_FILE:-$APP_DIR/app.log}"
APP_JAR="$APP_DIR/app.jar"
CONFIG_DIR="$APP_DIR/config"
PROFILE_CONFIG_FILE="$CONFIG_DIR/application-${SPRING_PROFILE}.yaml"
BASE_CONFIG_FILE="$CONFIG_DIR/application.yaml"
BACKUP_DIR="$APP_DIR/backup"
TARGET_DIR="$REPO_DIR/timecampus-server/target"

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

stop_app() {
    if [ ! -f "$PID_FILE" ]; then
        log "No pid file found; skip stop"
        return
    fi

    local pid
    pid="$(cat "$PID_FILE")"
    if [ -z "$pid" ] || ! kill -0 "$pid" 2>/dev/null; then
        log "Stale pid file; removing $PID_FILE"
        rm -f "$PID_FILE"
        return
    fi

    log "Stopping backend process pid=$pid"
    kill "$pid" || true

    local waited=0
    while kill -0 "$pid" 2>/dev/null; do
        if [ "$waited" -ge "$STOP_TIMEOUT_SECONDS" ]; then
            log "Process did not stop in ${STOP_TIMEOUT_SECONDS}s; sending SIGKILL"
            kill -9 "$pid" || true
            break
        fi
        sleep 1
        waited=$((waited + 1))
    done

    rm -f "$PID_FILE"
}

start_app() {
    log "Starting backend with profile=$SPRING_PROFILE"
    cd "$APP_DIR"
    nohup "$JAVA_BIN" -jar "$APP_JAR" \
        --spring.profiles.active="$SPRING_PROFILE" \
        > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"

    sleep 8
    if ! kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
        tail -n 120 "$LOG_FILE" || true
        rm -f "$PID_FILE"
        fail "Backend failed to start"
    fi

    log "Backend started pid=$(cat "$PID_FILE")"

    if command -v curl >/dev/null 2>&1; then
        log "Checking health endpoint: $HEALTH_URL"
        local waited=0
        until curl -fsS --max-time 3 "$HEALTH_URL" >/dev/null; do
            if [ "$waited" -ge "$HEALTH_TIMEOUT_SECONDS" ]; then
                tail -n 120 "$LOG_FILE" || true
                fail "Health check failed: $HEALTH_URL"
            fi
            sleep 1
            waited=$((waited + 1))
        done
        log "Health check passed"
    else
        log "curl not found; skipping HTTP health check"
    fi
}

require_command git
require_command "$MAVEN_BIN"
require_command "$JAVA_BIN"
require_command find
require_command sort
require_command awk

[ -d "$REPO_DIR" ] || fail "Backend repository not found: $REPO_DIR"
[ -f "$REPO_DIR/pom.xml" ] || fail "Missing Maven pom.xml in $REPO_DIR"

mkdir -p "$CONFIG_DIR" "$BACKUP_DIR"
if [ ! -f "$PROFILE_CONFIG_FILE" ] && [ ! -f "$BASE_CONFIG_FILE" ]; then
    fail "Missing config file: expected $PROFILE_CONFIG_FILE or $BASE_CONFIG_FILE"
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
[ -f "$latest_jar" ] || fail "Latest jar path is invalid: $latest_jar"

log "Latest jar: $latest_jar"

stop_app

if [ -f "$APP_JAR" ]; then
    backup_file="$BACKUP_DIR/app-$(date '+%Y%m%d%H%M%S').jar"
    log "Backing up current jar to $backup_file"
    cp "$APP_JAR" "$backup_file"
fi

log "Copying jar to $APP_JAR"
cp "$latest_jar" "$APP_JAR"

start_app

log "Deployment successful"
