<img width="2160" height="1350" alt="표지" src="https://github.com/user-attachments/assets/c0a16a77-561b-47d3-8556-22cb2794cb08" />

---

## ⚾️ CheerLot Backend API
KBO 팀 라인업 정보와 응원가를 제공하는 Spring Boot 백엔드 서버
<br>
iOS 앱을 위한 실시간 라인업 데이터와 선수별 응원가 API 서비스

### 🔗 관련 서비스
- **iOS 앱**: [App Store에서 다운로드](https://apps.apple.com/app/%EC%B3%90%EB%9E%8F/id6748527115)
- **Backend API**: 현재 저장소 (라인업 & 응원가 데이터 제공)

<br>

## 📆 프로젝트 기간
- 전체 기간: `2025.05.08 ~ 진행중`
- 개발 기간: `2025.05.27 ~ 진행중`

<br>

## 🛠 기술 스택

### Environment
<div align="left">
<img src="https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white" />
<img src="https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white" />
<img src="https://img.shields.io/badge/gradle-%2302303A.svg?style=for-the-badge&logo=gradle&logoColor=white" />
</div>

### Backend Development
<div align="left">
<img src="https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellijidea&logoColor=white" />
<img src="https://img.shields.io/badge/spring_boot_3.5.0-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white" />
<img src="https://img.shields.io/badge/java_17-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" />
<img src="https://img.shields.io/badge/redis-%23DC382D.svg?style=for-the-badge&logo=redis&logoColor=white" />
<img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white" />
<img src="https://img.shields.io/badge/thymeleaf-%23005F0F.svg?style=for-the-badge&logo=thymeleaf&logoColor=white" />
<img src="https://img.shields.io/badge/jsoup-43B02A?style=for-the-badge&logo=jsoup&logoColor=white" />
</div>

### Communication
<div align="left">
<img src="https://img.shields.io/badge/Miro-FFFC00.svg?style=for-the-badge&logo=Miro&logoColor=050038" />
<img src="https://img.shields.io/badge/Notion-white.svg?style=for-the-badge&logo=Notion&logoColor=000000" />
<img src="https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white" />
</div>

<br>

## 🚀 시작하기

### 개발 환경 요구사항
- Java 17 이상
- Gradle 8.14
- Docker & Docker Compose (Redis용)

### 로컬 실행
```bash
# 프로젝트 클론
git clone https://github.com/seongm1n/cheerlot-server.git
cd cheerlot-server

# Redis 실행 (Docker Compose)
docker-compose up -d

# 의존성 설치 및 빌드 (테스트 제외)
./gradlew build -x test

# 애플리케이션 실행
./gradlew bootRun

# 테스트 실행
./gradlew test

# 클린 빌드
./gradlew clean build
```

### 데이터베이스 접근
- **Redis**: localhost:6379
- **관리자 인터페이스**: http://localhost:8080/admin
- **API 문서**: http://localhost:8080/admin/api-docs

### 배포
```bash
# 프로덕션 업데이트 및 배포
./update.sh
```

<br>

## 🌟 주요 기능

### 📊 실시간 라인업 관리
- **자동 라인업 크롤링**: Naver Sports API를 통한 일일 라인업 업데이트
- **타순 동기화**: 선수별 타순 정보 실시간 반영
- **선수 교체 추적**: 경기 중 선수 교체 정보 업데이트

### 🎵 응원가 서비스
- **선수별 응원가**: 개별 선수 맞춤 응원가 제공 (250+ 파일)
- **가사 및 음원**: 완전한 응원가 데이터 (가사 + 오디오 파일)
- **팀별 구성**: KBO 전 팀 선수 응원가 데이터베이스
- **실시간 제공**: REST API를 통한 스트리밍 서비스

### 🔄 스케줄링 서비스
- **자동화된 크롤링**: 스케줄 기반 자동 데이터 업데이트
- **비동기 처리**: 효율적인 데이터 수집 및 처리
- **에러 핸들링**: 견고한 재시도 로직 및 장애 처리

### 📱 API 서비스
- **RESTful API**: iOS 앱을 위한 표준화된 API 제공
- **팀별 라인업**: `/api/lineups/{teamCode}` - 팀 라인업 조회
- **선수별 정보**: `/api/players/{teamCode}` - 팀 선수 조회
- **응원가 서비스**: `/api/cheersongs/{code}` - 응원가 오디오 제공
- **버전 관리**: `/api/version/{type}/{teamCode}/number` - 버전 정보
- **관리자 인터페이스**: `/admin/*` - 웹 기반 데이터 관리
- **API 문서**: `/admin/api-docs` - 실시간 API 명세서

<br>

## 🏗 아키텍처 개요

### 핵심 도메인 모델
- **Player**: KBO 선수 정보 (라인업 포지션, 타순, 응원가)
- **Team**: KBO 팀 정보 (선수 로스터, 메타데이터)
- **Game**: 라인업 크롤링을 위한 경기 정보
- **CheerSong**: 선수별 응원가 (가사, 오디오 파일)

### 주요 서비스
- **LineupCrawlerService**: 타순 동기화를 통한 일일 라인업 업데이트
- **ScheduleCrawlerService**: 경기 스케줄 데이터 수집
- **CrawlerSchedulingService**: 자동화된 크롤링 작업 관리

### 데이터 플로우
1. 스케줄된 크롤러가 Naver Sports API에서 경기 데이터 수집
2. 경기별 선수 타순 초기화 및 업데이트
3. `/resources/cheersongs/audio/`에서 응원가 사전 로드
4. REST 엔드포인트를 통해 iOS 앱에 구조화된 데이터 제공

<br>

## 🧑‍💻 팀 소개
| 지구 | 윤 | 제나 | 테오 | 이안 | 아이비 |
|:-:|:-:|:-:|:-:|:-:|:-:|
| <img src="https://github.com/user-attachments/assets/732e7948-c050-43c5-ab51-d6cdae9c758d" style="width:140px; height:auto;" /> | <img src="https://github.com/user-attachments/assets/70c4d70e-42a7-4d1d-92a1-fd83940c19d3" style="width:140px; height:auto;" /> | <img src="https://github.com/user-attachments/assets/4542e591-53c5-468b-a9de-5ba5425353b8" style="width:140px; height:auto;" /> | <img src="https://github.com/user-attachments/assets/1d95c442-4481-4b71-8b0a-b1369884a46c" style="width:140px; height:auto;" /> | <img src="https://github.com/user-attachments/assets/9a000595-2e4c-413f-a484-4bb88a0136b8" style="width:140px; height:auto;" /> | <img src="https://github.com/user-attachments/assets/676f5076-75d3-47a7-a7c6-4d7eda80e898" style="width:140px; height:auto;" /> |
|[@Zigu](https://github.com/991218t)|[@Yoon](https://github.com/choiy109)|[@Jenna](https://github.com/ParkJihee-jenna)|[@Theo](https://github.com/seongm1n)|[@Ian](https://github.com/SeungEEE)|[@Ivy](https://github.com/dlguszoo)|

<br>

## 🧱 프로젝트 구조 (Package by Feature)

```
📁 src/main/java/academy/cheerlot/
├── 🚀 CheerlotApplication.java          # 메인 애플리케이션
├── 📁 config/                           # 설정 클래스
│   ├── 🔧 RedisConfig.java              # Redis 설정
│   ├── 📊 RedisDataLoader.java          # 초기 데이터 로더
│   └── 🌐 RestTemplateConfig.java       # HTTP 클라이언트 설정
├── 📁 player/                           # 선수 관리 모듈
│   ├── 👤 Player.java                   # 선수 엔티티 (Redis)
│   ├── 📊 PlayerRepository.java         # 선수 데이터 저장소
│   ├── 🎯 PlayerController.java         # 선수 API 엔드포인트
│   └── 📋 PlayerResponse.java           # 선수 응답 DTO (Record)
├── 📁 team/                            # 팀 관리 모듈
│   ├── 🏟️ Team.java                     # 팀 엔티티 (Redis)
│   └── 📊 TeamRepository.java           # 팀 데이터 저장소
├── 📁 cheersong/                       # 응원가 관리 모듈
│   ├── 🎵 CheerSong.java                # 응원가 엔티티
│   ├── 📊 CheerSongRepository.java      # 응원가 저장소
│   ├── 🎯 CheerSongController.java      # 응원가 API 엔드포인트
│   └── 📋 CheerSongResponse.java        # 응원가 응답 DTO (Record)
├── 📁 lineup/                          # 라인업 관리 모듈
│   ├── 🎯 LineupController.java         # 라인업 API 엔드포인트
│   ├── 🕷️ LineupCrawlerService.java     # 라인업 크롤링 서비스
│   ├── 📅 ScheduleCrawlerService.java   # 스케줄 크롤링 서비스
│   ├── ⏰ CrawlerSchedulingService.java # 크롤링 스케줄링
│   └── 📋 LineupResponse.java           # 라인업 응답 DTO (Record)
├── 📁 game/                            # 경기 관리 모듈
│   ├── 🎯 Game.java                     # 경기 엔티티
│   └── 📊 GameRepository.java           # 경기 데이터 저장소
├── 📁 version/                         # 버전 관리 모듈
│   ├── 📊 Version.java                  # 버전 엔티티
│   ├── 📊 VersionRepository.java        # 버전 저장소
│   ├── 🎯 VersionController.java        # 버전 API 엔드포인트
│   ├── 🔧 VersionService.java           # 버전 관리 서비스
│   └── 🔧 RosterVersionService.java     # 로스터 버전 서비스
└── 📁 admin/                           # 관리자 인터페이스 모듈
    └── 🛠️ AdminController.java          # 관리자 웹 인터페이스

📁 src/main/resources/
├── 🎵 cheersongs/audio/                 # 응원가 오디오 파일 (250+ 파일)
├── 📄 application.properties            # 애플리케이션 설정
└── 📁 templates/admin/                  # Thymeleaf 관리자 템플릿
    ├── 📊 dashboard.html                # 관리자 대시보드
    ├── 👤 players.html                  # 선수 관리
    ├── 🎯 lineup.html                   # 라인업 관리
    ├── 🎵 cheersongs.html               # 응원가 관리
    ├── 📊 versions.html                 # 버전 관리
    ├── 📖 api-docs.html                 # API 문서 페이지
    └── 🎨 layout.html                   # 공통 레이아웃

📁 docker/
├── 🐳 docker-compose.yml               # Redis 서비스 구성
└── 📁 redis/                           # Redis 설정 파일
```

<br>

## 🔖 브랜치 전략
- `main`: 배포 가능한 안정 버전
- `dev`: 통합 개발 브랜치
- `feature/*`: 기능 개발 브랜치  
- `bugfix/*`: 버그 수정 브랜치
- `hotfix/*`: 긴급 수정 브랜치

<br>

## 🌀 커밋 메시지 컨벤션
- `[#이슈번호] Feat: 새로운 기능 추가`
- `[#이슈번호] Fix: 버그 수정`
- `[#이슈번호] Refactor: 코드 리팩토링`
- `[#이슈번호] Docs: 문서 수정`
- `[#이슈번호] Style: 코드 스타일 변경`
- `[#이슈번호] Test: 테스트 코드 추가/수정`
- `[#이슈번호] Chore: 빌드 프로세스 또는 보조 도구 변경`

<br>

## 📡 API 명세

### 📊 선수 관리 API
```
GET /api/players
- 설명: 전체 선수 정보 조회
- 응답: PlayerResponse 리스트 (Record DTO)

GET /api/players/{teamCode}
- 설명: 팀별 선수 정보 조회 (타순순)
- 응답: PlayerResponse 리스트
```

### 🎯 라인업 관리 API
```
GET /api/lineups/{teamCode}
- 설명: 팀 라인업 조회 (타순이 있는 선수만)
- 응답: LineupResponse (Record DTO)

GET /api/crawl
- 설명: 수동 라인업 크롤링 실행
- 응답: 크롤링 완료 메시지
```

### 🎵 응원가 API  
```
GET /api/cheersongs/{code}
- 설명: 응원가 오디오 파일 제공
- 파라미터: code (팀코드+백넘버+확장자, 예: lg10.mp3)
- 응답: 오디오 파일 (audio/mpeg)
```

### 📊 버전 관리 API
```
GET /api/version/roster/{teamCode}/number
- 설명: 로스터 버전 번호 조회
- 응답: Long (버전 번호)

GET /api/version/lineup/{teamCode}/number
- 설명: 라인업 버전 번호 조회
- 응답: Long (버전 번호)
```

### 🛠️ 관리자 Web API
```
GET /admin
- 설명: 관리자 대시보드
- 기술: Thymeleaf 템플릿

GET /admin/players
- 설명: 선수 관리 페이지

GET /admin/lineup
- 설명: 라인업 관리 페이지

GET /admin/cheersongs
- 설명: 응원가 관리 페이지

GET /admin/versions
- 설명: 버전 관리 페이지

GET /admin/api-docs
- 설명: 실시간 API 문서 페이지

POST /admin/player/{id}/batting-order
- 설명: 선수 타순 수정

POST /admin/version/{teamCode}/{type}/update
- 설명: 버전 수동 업데이트

POST /admin/version/{teamCode}/{type}/increment
- 설명: 버전 자동 증가
```

<br>

## 🔧 설정 및 특징

### 주요 설정
- **Redis 구성**: Spring Data Redis + Docker Compose 연동
- **스케줄링**: `@EnableScheduling` - 자동화된 데이터 업데이트
- **비동기 처리**: `@EnableAsync` - 크롤러 비동기 작업
- **로깅**: `logs/kbolineup.log` - 애플리케이션 로그 관리
- **오디오 저장소**: `src/main/resources/cheersongs/audio/` - 응원가 파일 (250+)
- **관리자 UI**: Thymeleaf + Bootstrap 5 - 반응형 웹 인터페이스

### 개발 패턴 및 아키텍처
- **Package by Feature**: 기능별 패키지 구조로 모듈화
- **Redis Repository 패턴**: 인덱싱을 통한 효율적 데이터 조회
- **Record DTO**: 불변 데이터 전송 객체 (Java 17)
- **REST 컨트롤러**: Clean API 계약과 적절한 HTTP 상태 코드
- **서비스 레이어**: 외부 API 통합 및 비즈니스 로직 캡슐화
- **크롤러 서비스**: 재시도 로직과 우아한 장애 처리
- **버전 관리**: 데이터 변경 추적을 통한 클라이언트 동기화
- **에러 핸들링**: 포괄적인 예외 처리 및 복구 전략