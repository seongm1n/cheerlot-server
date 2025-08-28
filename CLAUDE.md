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
```

### Database Access
- **H2 Console**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:kbodb`
- **Username**: `sa`
- **Password**: (empty)

### Deployment
```bash
# Production deployment script
./update.sh
```

## Architecture Overview

### Package by Feature Structure
This is a Spring Boot application that provides KBO (Korean Baseball Organization) lineup and cheer song data via REST API for iOS app consumption. The application follows a simplified Package by Feature architecture for improved maintainability and developer productivity.

**Package Structure**:
```
src/main/java/academy/cheerlot/
├── CheerlotApplication.java
├── config/                          # Configuration classes
│   └── RestTemplateConfig.java
├── player/                          # Player feature module
│   ├── Player.java                  # Entity
│   ├── PlayerRepository.java        # Repository
│   ├── PlayerController.java        # REST API
│   └── PlayerDto.java               # Data Transfer Object
├── team/                           # Team feature module
│   ├── Team.java
│   └── TeamRepository.java
├── cheersong/                      # CheerSong feature module
│   ├── CheerSong.java
│   ├── CheerSongRepository.java
│   └── CheerSongController.java
├── game/                           # Game feature module
│   ├── Game.java
│   └── GameRepository.java
├── lineup/                         # Lineup and Crawler feature module
│   ├── LineupController.java
│   ├── LineupCrawlerService.java
│   ├── ScheduleCrawlerService.java
│   ├── CrawlerSchedulingService.java
│   └── LineupResponse.java
├── version/                        # Version management feature module
│   ├── Version.java
│   ├── VersionRepository.java
│   ├── VersionController.java
│   ├── VersionService.java
│   └── RosterVersionService.java
└── admin/                          # Admin interface feature module
    └── AdminController.java
```

### Core Domain Model

**Domain Entities**:
- `Player`: KBO player information with lineup position, batting order, and associated cheer songs
- `Team`: KBO team information containing player roster and metadata  
- `Game`: Game information used for lineup crawling and data synchronization
- `CheerSong`: Individual player cheer songs with lyrics and audio file references

**Key Relationships**:
- Player belongs to Team (many-to-one)
- Player has CheerSong (one-to-one or one-to-many)
- Game contains lineup data for crawling purposes

### Feature Modules

**Player Module** (`academy.cheerlot.player`):
- **Entity**: `Player` - KBO player with lineup position and batting order
- **Repository**: `PlayerRepository` - Team-based player queries and batting order updates
- **Controller**: `PlayerController` - Player API endpoints
- **DTO**: `PlayerDto` - Data transfer for API responses

**Team Module** (`academy.cheerlot.team`):
- **Entity**: `Team` - KBO team information with roster metadata
- **Repository**: `TeamRepository` - Team data management

**CheerSong Module** (`academy.cheerlot.cheersong`):
- **Entity**: `CheerSong` - Player-specific cheer songs with lyrics and audio
- **Repository**: `CheerSongRepository` - Cheer song data access
- **Controller**: `CheerSongController` - Audio file serving API

**Game Module** (`academy.cheerlot.game`):
- **Entity**: `Game` - Game information for lineup crawling
- **Repository**: `GameRepository` - Game schedule storage

**Lineup Module** (`academy.cheerlot.lineup`):
- **Services**:
  - `LineupCrawlerService` - Updates player batting orders via Naver Sports API
  - `ScheduleCrawlerService` - Collects game schedule data
  - `CrawlerSchedulingService` - Orchestrates automated crawling with `@Scheduled`
- **Controller**: `LineupController` - Lineup API endpoints and manual crawling triggers
- **DTO**: `LineupResponse` - Lineup data transfer object

**Version Module** (`academy.cheerlot.version`):
- **Entity**: `Version` - Version tracking for roster and lineup changes
- **Repository**: `VersionRepository` - Version data access
- **Services**: `VersionService`, `RosterVersionService` - Version management
- **Controller**: `VersionController` - Version API endpoints

**Admin Module** (`academy.cheerlot.admin`):
- **Controller**: `AdminController` - Web-based admin interface using Thymeleaf

### Data Flow
1. `ScheduleCrawlerService` collects game schedule data from external API
2. `LineupCrawlerService` processes games to update player batting orders (not create players)
3. Cheer songs are pre-loaded from `/resources/cheersongs/audio/` directory
4. REST endpoints serve structured data to iOS app via `PlayerDto` and `LineupResponse`
5. Admin interface allows manual data management through Thymeleaf templates

### Key Technical Patterns

**Package by Feature**: Related functionality grouped together for improved cohesion and maintainability
**Repository Pattern**: JPA repositories with custom queries (e.g., team-based player lookups)
**DTO Pattern**: Web layer uses DTOs (`PlayerDto`, `LineupResponse`) for clean API contracts
**Service Pattern**: Business logic encapsulated in service classes (e.g., crawler coordination)
**MVC Pattern**: Clear separation between controllers, services, and repositories
**Scheduling**: `@EnableScheduling` and `@EnableAsync` for automated background tasks

### Important Implementation Notes

- **Feature Organization**: Each feature module contains all related classes for better maintainability
- **Player Creation vs Update**: The crawler updates existing player batting orders rather than creating new players
- **Audio File Management**: Cheer song audio files are stored in `src/main/resources/cheersongs/audio/` and served as static resources
- **Error Handling**: Crawlers implement graceful error handling to prevent data corruption
- **Database**: Uses H2 in-memory database with JPA/Hibernate for development and testing

### API Endpoints
- `GET /api/lineups/{teamCode}` - Returns team lineup with batting order (Lineup module)
- `GET /api/players` - Returns all players (Player module)
- `GET /api/players/{teamCode}` - Returns players by team (Player module)
- `GET /api/cheersongs/{code}` - Returns cheer song audio files (CheerSong module)
- `GET /api/version/roster/{teamCode}/number` - Returns roster version number (Version module)
- `GET /api/version/lineup/{teamCode}/number` - Returns lineup version number (Version module)
- `GET /api/crawl` - Manual crawling trigger (Lineup module)
- `GET /admin/*` - Web-based admin interface using Thymeleaf (Admin module)

### Module Integration
- **Cross-Module References**: Modules reference each other directly through package imports
- **Configuration**: Common configuration classes in `config` package
- **Data Consistency**: Services coordinate data updates across modules

### Configuration
- Application runs on port 8080
- H2 console enabled for development at `/h2-console`
- Logging configured to `logs/kbolineup.log`
- JPA configured with `ddl-auto=update` for schema management
- Simplified package structure improves code navigation and maintenance

### Code Style Guidelines
- **No Comments**: This codebase follows a no-comments policy. Code should be self-documenting through clear naming and structure
- Focus on expressive method names, variable names, and class design rather than explanatory comments