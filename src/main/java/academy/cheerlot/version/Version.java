package academy.cheerlot.version;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "version")
@Data
@NoArgsConstructor
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String teamCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VersionType type;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @Column(nullable = false)
    private Long versionNumber;

    private String description;

    public enum VersionType {
        ROSTER,    // 선수명단 버전
        LINEUP     // 라인업 버전
    }

    public Version(String teamCode, VersionType type, String description) {
        this.teamCode = teamCode;
        this.type = type;
        this.description = description;
        this.lastUpdated = LocalDateTime.now();
        this.versionNumber = System.currentTimeMillis();
    }

    public void updateVersion(String description) {
        this.lastUpdated = LocalDateTime.now();
        this.versionNumber = System.currentTimeMillis();
        this.description = description;
    }
}
