#!/bin/bash

echo "🔄 업데이트 시작..."

# 1. 기존 프로세스 종료
pkill -f "cheerlot" && echo "기존 프로세스 종료 완료" || echo "실행중인 프로세스 없음"

# 2. 최신 코드 가져오기
git pull origin dev

# 3. 빌드
./gradlew build -x test

# 4. 백그라운드 실행
nohup java -jar build/libs/cheerlot-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

echo "✅ 완료! PID: $(pgrep -f cheerlot)"
echo "📋 로그 보기: tail -f app.log" 