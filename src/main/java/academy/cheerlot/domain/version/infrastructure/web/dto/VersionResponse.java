package academy.cheerlot.domain.version.infrastructure.web.dto;

import academy.cheerlot.domain.version.domain.Version;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record VersionResponse(
        String teamCode,
        String type,
        Long versionNumber,
        String lastUpdated,
        String description
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static VersionResponse from(Version version) {
        return new VersionResponse(
                version.getTeamCode(),
                version.getType().name().toLowerCase(),
                version.getVersionNumber(),
                version.getLastUpdated().format(FORMATTER),
                version.getDescription()
        );
    }

    public static VersionResponse empty(String teamCode, String type) {
        return new VersionResponse(
                teamCode,
                type,
                0L,
                LocalDateTime.now().format(FORMATTER),
                "No version information available"
        );
    }
}
