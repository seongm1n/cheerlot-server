package academy.cheerlot.domain.version.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RosterVersionService {

    private final VersionService versionService;

    public void updateOnPlayerAdded(String teamCode, String playerName) {
        String description = String.format("선수 %s 추가", playerName);
        versionService.updateRosterVersion(teamCode, description);
    }

    public void updateOnPlayerModified(String teamCode, String playerName) {
        String description = String.format("선수 %s 정보 수정", playerName);
        versionService.updateRosterVersion(teamCode, description);
    }

    public void updateOnPlayerDeleted(String teamCode, String playerName) {
        String description = String.format("선수 %s 삭제", playerName);
        versionService.updateRosterVersion(teamCode, description);
    }

    public void updateOnBulkPlayerChange(String teamCode, String description) {
        versionService.updateRosterVersion(teamCode, description);
    }
}
