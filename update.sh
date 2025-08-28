#!/bin/bash

echo "🔄 업데이트 시작..."

# 1. 기존 컨테이너 중지
docker-compose down

# 2. 최신 코드 가져오기
git pull origin dev

# 3. 빌드 및 백그라운드 실행
docker-compose up --build -d

echo "✅ 완료!"
echo "📋 상태 확인: docker-compose ps"
echo "📋 로그 보기: docker-compose logs -f app" 
