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

### DDD (Domain-Driven Design) Structure
This is a Spring Boot application following Domain-Driven Design principles that provides KBO (Korean Baseball Organization) lineup and cheer song data via REST API for iOS app consumption.

**Package Structure**:
```
src/main/java/academy/cheerlot/
├── CheerlotApplication.java
├── shared/                          # Cross-cutting concerns
│   ├── config/                      # Configuration classes
│   └── infrastructure/web/controller/ # Admin controllers
└── domain/                          # Domain-specific modules
    ├── player/                      # Player aggregate
    │   ├── domain/                  # Core domain entities
    │   ├── application/             # Application services
    │   └── infrastructure/          # Infrastructure layer
    │       ├── persistence/         # Data access
    │       └── web/                 # REST controllers & DTOs
    ├── team/                        # Team aggregate
    ├── cheersong/                   # CheerSong aggregate
    ├── game/                        # Game aggregate
    └── lineup/                      # Lineup aggregate (orchestration)
        ├── application/             # Crawler services
        └── infrastructure/web/      # Lineup API
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

### Domain Boundaries

**Player Domain** (`academy.cheerlot.domain.player`):
- **Entities**: `Player` - KBO player with lineup position and batting order
- **Repository**: `PlayerRepository` - Team-based player queries and batting order updates
- **DTOs**: `PlayerDto` - Data transfer for API responses

**Team Domain** (`academy.cheerlot.domain.team`):
- **Entities**: `Team` - KBO team information with roster metadata
- **Repository**: `TeamRepository` - Team data management

**CheerSong Domain** (`academy.cheerlot.domain.cheersong`):
- **Entities**: `CheerSong` - Player-specific cheer songs with lyrics and audio
- **Repository**: `CheerSongRepository` - Cheer song data access
- **Controller**: `CheerSongController` - Audio file serving API

**Game Domain** (`academy.cheerlot.domain.game`):
- **Entities**: `Game` - Game information for lineup crawling
- **Repository**: `GameRepository` - Game schedule storage

**Lineup Domain** (`academy.cheerlot.domain.lineup`):
- **Application Services**:
  - `LineupCrawlerService` - Updates player batting orders via Naver Sports API
  - `ScheduleCrawlerService` - Collects game schedule data
  - `CrawlerSchedulingService` - Orchestrates automated crawling with `@Scheduled`
- **Web Layer**: `LineupController` - Lineup API endpoints and manual crawling triggers

### Data Flow
1. `ScheduleCrawlerService` collects game schedule data from external API
2. `LineupCrawlerService` processes games to update player batting orders (not create players)
3. Cheer songs are pre-loaded from `/resources/cheersongs/audio/` directory
4. REST endpoints serve structured data to iOS app via `PlayerDto` and `LineupResponse`
5. Admin interface allows manual data management through Thymeleaf templates

### Key Technical Patterns

**Domain-Driven Design**: Clear separation between domain logic and infrastructure concerns
**Repository Pattern**: Domain-specific repositories with custom queries (e.g., team-based player lookups)
**DTO Pattern**: Web layer uses DTOs (`PlayerDto`, `LineupResponse`) to decouple domain entities from API contracts
**Application Service Pattern**: Orchestration logic in application services (e.g., crawler coordination)
**Clean Architecture**: Dependencies point inward toward domain core
**Scheduling**: `@EnableScheduling` and `@EnableAsync` for automated background tasks

### Important Implementation Notes

- **Domain Separation**: Each domain manages its own data lifecycle and business rules
- **Player Creation vs Update**: The crawler updates existing player batting orders rather than creating new players
- **Audio File Management**: Cheer song audio files are stored in `src/main/resources/cheersongs/audio/` and served as static resources
- **Error Handling**: Crawlers implement graceful error handling to prevent data corruption
- **Database**: Uses H2 in-memory database with JPA/Hibernate for development and testing

### API Endpoints
- `GET /api/lineups/{teamCode}` - Returns team lineup with batting order (Lineup domain)
- `GET /api/cheersongs/{code}` - Returns cheer song audio files (CheerSong domain)
- `GET /api/crawl` - Manual crawling trigger (Lineup domain)
- `GET /admin/*` - Web-based admin interface using Thymeleaf (Shared infrastructure)

### Domain Integration
- **Cross-Domain References**: Domains reference each other through their public interfaces
- **Shared Kernel**: Common configuration and infrastructure in `shared` package
- **Anti-Corruption Layer**: DTOs prevent external API changes from affecting domain model

### Configuration
- Application runs on port 8080
- H2 console enabled for development at `/h2-console`
- Logging configured to `logs/kbolineup.log`
- JPA configured with `ddl-auto=update` for schema management
- Domain entities use full package names for cross-domain references to maintain loose coupling

### Code Style Guidelines
- **No Comments**: This codebase follows a no-comments policy. Code should be self-documenting through clear naming and structure
- Focus on expressive method names, variable names, and class design rather than explanatory comments