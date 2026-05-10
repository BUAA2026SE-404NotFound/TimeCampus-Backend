# 在项目根目录中执行 ./deploy/scripts/deploy.sh

sudo ss -ltnp | grep :8080
if [ $? -eq 0 ]; then
    pid=$(sudo ss -ltnp | grep :8080 | awk '{print $7}' | cut -d',' -f2)
    kill -9 $pid
fi

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