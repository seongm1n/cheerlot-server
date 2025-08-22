package academy.cheerlot.domain.version.infrastructure.web;

import academy.cheerlot.domain.version.application.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/version")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

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
