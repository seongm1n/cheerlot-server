package academy.cheerlot.version;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@RedisHash("version")
@Data
@NoArgsConstructor
public class Version {

    @Id
    private String id;

    @Indexed
    private String teamCode;

    @Indexed
    private VersionType type;

    private LocalDateTime lastUpdated;

    private Long versionNumber;

    private String description;

    public enum VersionType {
        ROSTER,
        LINEUP
    }

    public Version(String teamCode, VersionType type, String description) {
        this.id = teamCode + ":" + type.name();
        this.teamCode = teamCode;
        this.type = type;
        this.description = description;
        this.lastUpdated = LocalDateTime.now();
        this.versionNumber = 0L;
    }

    public void updateVersion(String description, Long newVersionNumber) {
        this.lastUpdated = LocalDateTime.now();
        this.versionNumber = newVersionNumber;
        this.description = description;
    }
}
