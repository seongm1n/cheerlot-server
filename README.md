<img width="2160" height="1350" alt="표지" src="https://github.com/user-attachments/assets/c0a16a77-561b-47d3-8556-22cb2794cb08" />

---

## ⚾️ 쳐랏! - cheerlot Backend API
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
<img src="https://img.shields.io/badge/spring_boot-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white" />
<img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" />
<img src="https://img.shields.io/badge/h2_database-1021FF?style=for-the-badge&logo=h2&logoColor=white" />
<img src="https://img.shields.io/badge/jsoup-43B02A?style=for-the-badge&logo=jsoup&logoColor=white" />
<img src="https://img.shields.io/badge/jackson-000000?style=for-the-badge&logo=json&logoColor=white" />
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
- Gradle 8.0 이상

### 로컬 실행
```bash
# 프로젝트 클론
git clone https://github.com/seongm1n/cheerlot.git
cd cheerlot

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
- **H2 Console**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:kbodb`
- **Username**: `sa`
- **Password**: (비어있음)

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
- **선수별 응원가**: 개별 선수 맞춤 응원가 제공
- **가사 및 음원**: 완전한 응원가 데이터 (가사 + 오디오 파일)
- **팀별 구성**: KBO 전 팀 선수 응원가 데이터베이스

### 🔄 스케줄링 서비스
- **자동화된 크롤링**: 스케줄 기반 자동 데이터 업데이트
- **비동기 처리**: 효율적인 데이터 수집 및 처리
- **에러 핸들링**: 견고한 재시도 로직 및 장애 처리

### 📱 API 서비스
- **RESTful API**: iOS 앱을 위한 표준화된 API 제공
- **팀별 라인업**: `/api/lineup/{teamCode}` - 팀 라인업 조회
- **선수 응원가**: `/api/cheersongs/{playerId}` - 선수별 응원가 조회
- **관리자 인터페이스**: `/admin/*` - 웹 기반 데이터 관리

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

## 🧱 프로젝트 구조

```
📁 src/main/java/com/example/cheerlot
├── 📁 controller
│   ├── 🎯 LineupController.java         # 라인업 API 엔드포인트
│   ├── 🎵 CheerSongController.java      # 응원가 API 엔드포인트
│   └── 🛠️ AdminController.java          # 관리자 인터페이스
├── 📁 service
│   ├── 🕷️ LineupCrawlerService.java     # 라인업 크롤링 서비스
│   ├── 📅 ScheduleCrawlerService.java   # 스케줄 크롤링 서비스
│   └── ⏰ CrawlerSchedulingService.java # 크롤링 스케줄링 관리
├── 📁 repository
│   ├── 👤 PlayerRepository.java         # 선수 데이터 저장소
│   ├── 🏟️ TeamRepository.java           # 팀 데이터 저장소
│   └── 🎵 CheerSongRepository.java      # 응원가 데이터 저장소
├── 📁 domain
│   ├── 👤 Player.java                   # 선수 도메인 모델
│   ├── 🏟️ Team.java                     # 팀 도메인 모델
│   ├── 🎯 Game.java                     # 경기 도메인 모델
│   └── 🎵 CheerSong.java                # 응원가 도메인 모델
└── 📁 dto
    ├── 📊 PlayerDto.java                # 선수 데이터 전송 객체
    ├── 🎯 LineupResponseDto.java        # 라인업 응답 DTO
    └── 🎵 CheerSongDto.java             # 응원가 응답 DTO

📁 src/main/resources
├── 🎵 cheersongs/audio/                 # 응원가 오디오 파일
├── 📄 application.properties            # 애플리케이션 설정
└── 📁 templates                         # Thymeleaf 관리자 템플릿

📁 logs
└── 📝 kbolineup.log                     # 애플리케이션 로그
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

### 라인업 API
```
GET /api/lineup/{teamCode}
- 설명: 팀 라인업 조회 (타순 포함)
- 응답: PlayerDto 리스트
```

### 응원가 API  
```
GET /api/cheersongs/{playerId}
- 설명: 선수별 응원가 조회
- 응답: CheerSongDto 리스트
```

### 관리자 API
```
GET /admin/*
- 설명: 웹 기반 선수/팀 관리 인터페이스
- 기술: Thymeleaf 템플릿
```

<br>

## 🔧 설정 및 특징

### 주요 설정
- **스케줄링**: `@EnableScheduling` - 자동화된 데이터 업데이트
- **비동기 처리**: `@EnableAsync` - 크롤러 비동기 작업
- **로깅**: `logs/kbolineup.log` - 애플리케이션 로그 관리
- **오디오 저장소**: `src/main/resources/cheersongs/audio/` - 응원가 파일

### 개발 패턴
- **JPA 리포지토리**: 팀 기반 선수 조회를 위한 커스텀 쿼리
- **REST 컨트롤러**: 도메인 엔티티 대신 DTO 반환
- **서비스 레이어**: 외부 API 통합 및 적절한 에러 핸들링
- **크롤러 서비스**: 재시도 로직과 우아한 장애 처리 구현