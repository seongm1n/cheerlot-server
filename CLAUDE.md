# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Building and Running
```bash
# Build the project (skipping tests for faster build)
./gradlew build -x test

# Clean build (full rebuild)
./gradlew clean build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Check for outdated dependencies
./gradlew dependencyUpdates

# Docker development (Redis required)
docker-compose up -d redis  # Start Redis only
docker-compose up           # Start full stack
```

### Data Access
- **Redis**: Port 6379 (configured via docker-compose.yml)
- **Admin Dashboard**: http://localhost:8080/admin
- **API Documentation**: http://localhost:8080/admin/api-docs

### Deployment
```bash
# Production deployment script
./update.sh
```

## Architecture Overview

### Redis-Based Spring Boot Application
This is a Spring Boot application that provides KBO (Korean Baseball Organization) lineup and cheer song data via REST API for iOS app consumption. The application has been migrated from H2 database to Redis for improved performance and scalability, following a Package by Feature architecture.

**Current Technology Stack**:
- **Framework**: Spring Boot 3.5.0
- **Java**: 17
- **Database**: Redis 7 (migrated from H2)
- **Cache**: Redis-based caching
- **Web Crawling**: Jsoup 1.15.3 
- **API Documentation**: SpringDoc OpenAPI 2.3.0
- **Template Engine**: Thymeleaf (admin interface)
- **Build Tool**: Gradle 8.x

**Package Structure**:
```
src/main/java/academy/cheerlot/
├── CheerlotApplication.java         # Main application with scheduling enabled
├── config/                          # Configuration classes
│   ├── RedisConfig.java            # Redis configuration and serialization
│   ├── RedisDataLoader.java        # Initial data loading to Redis
│   └── RestTemplateConfig.java     # REST client configuration
├── player/                          # Player feature module
│   ├── Player.java                  # Redis entity (@RedisHash)
│   ├── PlayerRepository.java       # Redis repository
│   ├── PlayerController.java       # REST API endpoints
│   └── PlayerResponse.java         # Response DTO (record)
├── team/                           # Team feature module
│   ├── Team.java                   # Redis entity
│   └── TeamRepository.java        # Redis repository
├── cheersong/                      # CheerSong feature module
│   ├── CheerSong.java              # Redis entity
│   ├── CheerSongRepository.java    # Redis repository
│   ├── CheerSongController.java    # Audio file serving
│   └── CheerSongResponse.java      # Response DTO
├── game/                           # Game feature module
│   ├── Game.java                   # Redis entity
│   └── GameRepository.java        # Redis repository
├── lineup/                         # Lineup and Crawler feature module
│   ├── LineupController.java       # Lineup API and manual crawling
│   ├── LineupCrawlerService.java   # Naver Sports API integration
│   ├── ScheduleCrawlerService.java # Game schedule collection
│   ├── CrawlerSchedulingService.java # Automated scheduling (@Scheduled)
│   └── LineupResponse.java         # Response DTO (record)
├── version/                        # Version management feature module
│   ├── Version.java                # Redis entity
│   ├── VersionRepository.java      # Redis repository
│   ├── VersionController.java      # Version API endpoints
│   └── VersionService.java         # Version management logic
└── admin/                          # Admin interface feature module
    └── AdminController.java        # Thymeleaf-based web interface
```

### Core Domain Model

**Redis Entity Structure** (using @RedisHash):
- `Player`: KBO player information stored as Redis hash with composite key (teamCode:backNumber)
  - Fields: playerId, name, backNumber, position, batsThrows, batsOrder, teamCode
  - Indexed on: name, teamCode for efficient queries
- `Team`: KBO team information with roster metadata and last update tracking
  - Fields: teamCode, name, lastUpdated, lastOpponent, playerCount
  - Indexed on: name
- `Game`: Game information for lineup crawling with scheduling data
- `CheerSong`: Individual player cheer songs with audio file references
  - Fields: id, title, lyrics, audioFileName, playerId
  - Indexed on: audioFileName, playerId
- `Version`: Version tracking for roster and lineup changes by team and type
  - Support for ROSTER and LINEUP version types per team

**Key Relationships** (Redis-based):
- Player identified by composite key (teamCode:backNumber) 
- CheerSong linked to Player via playerId string reference
- Team tracks player count and last update metadata
- Game entities drive the crawling schedule
- Version entities track changes by teamCode and VersionType enum

### Feature Modules

**Player Module** (`academy.cheerlot.player`):
- **Entity**: `Player` - Redis-based KBO player with composite key (teamCode:backNumber)
- **Repository**: `PlayerRepository` - Redis repository with team-based queries (findByTeamCodeOrderByBatsOrder)
- **Controller**: `PlayerController` - REST API endpoints with CheerSong integration
- **DTO**: `PlayerResponse` - Record-based response DTO with embedded cheer songs

**Team Module** (`academy.cheerlot.team`):
- **Entity**: `Team` - Redis entity with roster metadata and update tracking
- **Repository**: `TeamRepository` - Redis repository for team data management

**CheerSong Module** (`academy.cheerlot.cheersong`):
- **Entity**: `CheerSong` - Redis entity for player-specific cheer songs
- **Repository**: `CheerSongRepository` - Redis repository with playerId-based queries
- **Controller**: `CheerSongController` - Audio file serving from classpath resources
- **DTO**: `CheerSongResponse` - Record-based response DTO

**Game Module** (`academy.cheerlot.game`):
- **Entity**: `Game` - Redis entity for game information and crawling metadata
- **Repository**: `GameRepository` - Redis repository for game schedule storage

**Lineup Module** (`academy.cheerlot.lineup`):
- **Services**:
  - `LineupCrawlerService` - Naver Sports API integration with proper headers and error handling
  - `ScheduleCrawlerService` - Game schedule data collection
  - `CrawlerSchedulingService` - Automated crawling orchestration with @Scheduled annotations
- **Controller**: `LineupController` - Lineup API endpoints and manual crawling triggers
- **DTO**: `LineupResponse` - Record-based response with team metadata and player lineup

**Version Module** (`academy.cheerlot.version`):
- **Entity**: `Version` - Redis entity for tracking roster and lineup version changes
- **Repository**: `VersionRepository` - Redis repository with team and type-based queries
- **Service**: `VersionService` - Version increment and management logic
- **Controller**: `VersionController` - Version number API endpoints

**Admin Module** (`academy.cheerlot.admin`):
- **Controller**: `AdminController` - Comprehensive Thymeleaf-based web interface
  - Dashboard with team statistics and cheer song metrics
  - Player management with batting order updates
  - Lineup visualization and management
  - CheerSong file management and validation
  - Version management with manual increment/update capabilities
  - **NEW**: API documentation page at `/admin/api-docs` with interactive examples

### Data Flow
1. **Application Startup**: `RedisDataLoader` loads initial team, player, and cheer song data into Redis
2. **Scheduled Operations**: `ScheduleCrawlerService` collects game schedule data from Naver Sports API
3. **Lineup Updates**: `LineupCrawlerService` processes games to update existing player batting orders (does not create new players)
4. **Cheer Song Management**: Audio files stored in `/resources/cheersongs/audio/` served via `CheerSongController`
5. **API Responses**: REST endpoints serve structured data to iOS app via record-based DTOs (`PlayerResponse`, `LineupResponse`)
6. **Admin Management**: Comprehensive web interface for data management, version control, and API documentation
7. **Version Tracking**: Version increments tracked per team for ROSTER and LINEUP changes

### Key Technical Patterns

**Package by Feature**: Related functionality grouped together for improved cohesion and maintainability
**Redis Repository Pattern**: Spring Data Redis repositories with custom queries and indexing
**Record-Based DTOs**: Modern Java record types (`PlayerResponse`, `LineupResponse`, `CheerSongResponse`) for immutable data transfer
**Service Pattern**: Business logic encapsulated in service classes with proper error handling
**MVC Pattern**: Clear separation between controllers, services, and repositories
**Scheduling & Async**: `@EnableScheduling` and `@EnableAsync` for automated background crawling tasks
**Configuration Management**: Centralized Redis configuration with custom serialization setup
**Resource Management**: Classpath resource serving for cheer song audio files with JAR compatibility

### Important Implementation Notes

- **Redis Migration**: Successfully migrated from H2 database to Redis for improved performance and scalability
- **Data Initialization**: `RedisDataLoader` ensures proper data loading on application startup with pre-loaded team, player, and cheer song data
- **Player Management**: Crawler updates existing player batting orders rather than creating new players
- **Audio File Management**: Cheer song audio files stored in `src/main/resources/cheersongs/audio/` with JAR-compatible resource resolution
- **Error Handling**: Robust error handling in crawlers with proper logging and graceful degradation
- **Composite Keys**: Redis entities use meaningful composite keys (e.g., "teamCode:backNumber" for players)
- **API Design**: Modern record-based DTOs with clean separation between internal entities and external API contracts
- **Version Management**: Comprehensive version tracking system for both roster and lineup changes per team

### API Endpoints

**Public REST API**:
- `GET /api/players` - Returns all players with embedded cheer songs (PlayerResponse DTOs)
- `GET /api/players/{teamCode}` - Returns players by team with batting order (PlayerResponse DTOs)
- `GET /api/lineups/{teamCode}` - Returns team lineup with metadata (LineupResponse DTO)
- `GET /api/cheersongs/{code}` - Returns cheer song audio files (Resource serving)
- `GET /api/version/roster/{teamCode}/number` - Returns roster version number
- `GET /api/version/lineup/{teamCode}/number` - Returns lineup version number
- `GET /api/crawl` - Manual crawling trigger for testing

**Admin Web Interface**:
- `GET /admin` - Dashboard with statistics and team overview
- `GET /admin/players` - Player management with team filtering and batting order updates
- `GET /admin/lineup` - Lineup visualization and management by team
- `GET /admin/cheersongs` - CheerSong file management and validation
- `GET /admin/versions` - Version management with manual increment/update capabilities
- `GET /admin/api-docs` - **NEW**: Interactive API documentation with examples
- `POST /admin/player/{id}/batting-order` - Update player batting order
- `POST /admin/version/{teamCode}/{type}/update` - Set specific version number
- `POST /admin/version/{teamCode}/{type}/increment` - Increment version with description

### Module Integration
- **Cross-Module References**: Modules reference each other directly through package imports
- **Configuration**: Centralized configuration in `config` package with Redis setup
- **Data Consistency**: Services coordinate data updates across Redis repositories
- **DTO Composition**: Response DTOs aggregate data from multiple modules (e.g., PlayerResponse includes CheerSongResponse)

### Configuration & Infrastructure
- **Application**: Runs on port 8080 with scheduling and async processing enabled
- **Redis**: Port 6379 with Docker Compose orchestration and custom serialization
- **Logging**: Configured to `logs/kbolineup.log` with appropriate log levels
- **Admin Interface**: Comprehensive Thymeleaf-based management console
- **Resource Management**: Audio files served from classpath with JAR deployment support
- **Docker**: Full containerization with Redis service dependencies
- **Environment**: Production deployment via `update.sh` script

### Code Style Guidelines
- **No Comments**: This codebase follows a no-comments policy. Code should be self-documenting through clear naming and structure
- **Modern Java**: Uses Java 17 features including record types for DTOs and improved pattern matching
- **Lombok Integration**: Reduces boilerplate with @Data, @RequiredArgsConstructor, @Slf4j annotations
- **Clean Architecture**: Clear separation between entities, repositories, services, controllers, and DTOs
- **Error Handling**: Comprehensive error handling with proper logging and graceful degradation
- **Testing**: Unit tests for controllers, services, and integration logic with proper mocking