#!/usr/bin/env bash
# 在项目根目录中执行 ./deploy/scripts/deploy.sh

set -euo pipefail

PROJECT_VERSION="${1:-}"
REPO_DIR="${REPO_DIR:-$HOME/TimeCampus-Backend}"
APP_DIR="${APP_DIR:-$REPO_DIR/app}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
UI_DIST_DIR="${UI_DIST_DIR:-}"

if [ -z "$PROJECT_VERSION" ]; then
    echo "Usage: ./deploy/scripts/deploy.sh <project-version>"
    echo "Example: ./deploy/scripts/deploy.sh 0.1.0-alpha"
    exit 1
fi

cd "$REPO_DIR"

if [ -f "$APP_DIR/app.pid" ] && kill -0 "$(cat "$APP_DIR/app.pid")" 2>/dev/null; then
    kill "$(cat "$APP_DIR/app.pid")" || true
    sleep 2
fi

mvn -B -pl timecampus-server -am package -DskipTests

APP_JAR="timecampus-server/target/timecampus-server-${PROJECT_VERSION}.jar"
if [ ! -f "$APP_JAR" ]; then
    echo "Expected jar not found: $APP_JAR"
    echo "Current target files:"
    ls -lah timecampus-server/target || true
    exit 1
fi

mkdir -p "$APP_DIR/ui" "$APP_DIR/config"
cp "$APP_JAR" "$APP_DIR/app.jar"
if [ -n "$UI_DIST_DIR" ]; then
    if [ ! -d "$UI_DIST_DIR" ]; then
        echo "UI_DIST_DIR does not exist: $UI_DIST_DIR"
        exit 1
    fi
    rm -rf "$APP_DIR/ui"/*
    cp -R "$UI_DIST_DIR"/. "$APP_DIR/ui/"
else
    echo "UI_DIST_DIR not set; keeping existing UI files in $APP_DIR/ui"
fi

if [ ! -f "$APP_DIR/config/application-${SPRING_PROFILE}.yaml" ]; then
    echo "Missing $APP_DIR/config/application-${SPRING_PROFILE}.yaml"
    echo "Create it from timecampus-server/src/main/resources/application-${SPRING_PROFILE}-example.yaml before deployment."
    exit 1
fi

cd "$APP_DIR"
nohup java -jar app.jar --spring.profiles.active="$SPRING_PROFILE" > app.log 2>&1 &
echo $! > app.pid
sleep 8
ps -p "$(cat app.pid)" -o comm= || (echo "Failed to start application" && tail -n 200 app.log && exit 1)
echo "Deployment successful"
