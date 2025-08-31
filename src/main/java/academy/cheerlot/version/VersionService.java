package academy.cheerlot.version;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VersionService {

    private final VersionRepository versionRepository;


    @Transactional(readOnly = true)
    public Optional<Long> getVersionNumber(String teamCode, Version.VersionType type) {
        return versionRepository.findVersionNumberByTeamCodeAndType(teamCode, type);
    }

    public Version updateVersion(String teamCode, Version.VersionType type, String description) {
        Optional<Version> existingVersion = versionRepository.findByTeamCodeAndType(teamCode, type);
        
        if (existingVersion.isPresent()) {
            Version version = existingVersion.get();
            Long nextVersionNumber = version.getVersionNumber() + 1;
            version.updateVersion(description, nextVersionNumber);
            return versionRepository.save(version);
        } else {
            Version newVersion = new Version(teamCode, type, description);
            return versionRepository.save(newVersion);
        }
    }

    public Version updateRosterVersion(String teamCode, String description) {
        return updateVersion(teamCode, Version.VersionType.ROSTER, description);
    }

    public Version updateLineupVersion(String teamCode, String description) {
        return updateVersion(teamCode, Version.VersionType.LINEUP, description);
    }

    @Transactional(readOnly = true)
    public Long getRosterVersionNumber(String teamCode) {
        return getVersionNumber(teamCode, Version.VersionType.ROSTER).orElse(0L);
    }

    @Transactional(readOnly = true)
    public Long getLineupVersionNumber(String teamCode) {
        return getVersionNumber(teamCode, Version.VersionType.LINEUP).orElse(0L);
    }

    public Version setVersionNumber(String teamCode, Version.VersionType type, String description, Long versionNumber) {
        Optional<Version> existingVersion = versionRepository.findByTeamCodeAndType(teamCode, type);
        
        if (existingVersion.isPresent()) {
            Version version = existingVersion.get();
            version.updateVersion(description, versionNumber);
            return versionRepository.save(version);
        } else {
            Version newVersion = new Version(teamCode, type, description);
            newVersion.updateVersion(description, versionNumber);
            return versionRepository.save(newVersion);
        }
    }
}
