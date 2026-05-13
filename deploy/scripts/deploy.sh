#!/usr/bin/env bash
# 在项目根目录中执行 ./deploy/scripts/deploy.sh

set -euo pipefail

PROJECT_VERSION="${1:-}"
REPO_DIR="${REPO_DIR:-$HOME/TimeTrack-Backend}"
APP_DIR="${APP_DIR:-$REPO_DIR/app}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"

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

mvn -B -pl timetrack-server -am package -DskipTests

APP_JAR="timetrack-server/target/timetrack-server-${PROJECT_VERSION}.jar"
if [ ! -f "$APP_JAR" ]; then
    echo "Expected jar not found: $APP_JAR"
    echo "Current target files:"
    ls -lah timetrack-server/target || true
    exit 1
fi

cd timetrack-ui
npm ci
npm run build
cd ..

mkdir -p "$APP_DIR/ui" "$APP_DIR/config"
cp "$APP_JAR" "$APP_DIR/app.jar"
rm -rf "$APP_DIR/ui"/*
cp -R timetrack-ui/dist/. "$APP_DIR/ui/"

if [ ! -f "$APP_DIR/config/application-${SPRING_PROFILE}.yaml" ]; then
    echo "Missing $APP_DIR/config/application-${SPRING_PROFILE}.yaml"
    echo "Create it from timetrack-server/src/main/resources/application-${SPRING_PROFILE}-example.yaml before deployment."
    exit 1
fi

cd "$APP_DIR"
nohup java -jar app.jar --spring.profiles.active="$SPRING_PROFILE" > app.log 2>&1 &
echo $! > app.pid
sleep 8
ps -p "$(cat app.pid)" -o comm= || (echo "Failed to start application" && tail -n 200 app.log && exit 1)
echo "Deployment successful"
