package academy.cheerlot.domain.version.infrastructure.web;

import academy.cheerlot.domain.version.application.VersionService;
import academy.cheerlot.domain.version.domain.Version;
import academy.cheerlot.domain.version.infrastructure.web.dto.VersionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/version")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @GetMapping("/roster/{teamCode}")
    public ResponseEntity<VersionResponse> getRosterVersion(@PathVariable String teamCode) {
        Optional<Version> version = versionService.getVersion(teamCode, Version.VersionType.ROSTER);
        
        if (version.isPresent()) {
            return ResponseEntity.ok(VersionResponse.from(version.get()));
        } else {
            return ResponseEntity.ok(VersionResponse.empty(teamCode, "roster"));
        }
    }

    @GetMapping("/lineup/{teamCode}")
    public ResponseEntity<VersionResponse> getLineupVersion(@PathVariable String teamCode) {
        Optional<Version> version = versionService.getVersion(teamCode, Version.VersionType.LINEUP);
        
        if (version.isPresent()) {
            return ResponseEntity.ok(VersionResponse.from(version.get()));
        } else {
            return ResponseEntity.ok(VersionResponse.empty(teamCode, "lineup"));
        }
    }

    @GetMapping("/roster/{teamCode}/number")
    public ResponseEntity<Long> getRosterVersionNumber(@PathVariable String teamCode) {
        Long versionNumber = versionService.getRosterVersionNumber(teamCode);
        return ResponseEntity.ok(versionNumber);
    }

    @GetMapping("/lineup/{teamCode}/number")
    public ResponseEntity<Long> getLineupVersionNumber(@PathVariable String teamCode) {
        Long versionNumber = versionService.getLineupVersionNumber(teamCode);
        return ResponseEntity.ok(versionNumber);
    }
}
