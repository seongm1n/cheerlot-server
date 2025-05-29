package academy.cheerlot.controller;

import academy.cheerlot.domain.Team;
import academy.cheerlot.repository.TeamRepository;
import academy.cheerlot.service.LineupCrawlerService;
import academy.cheerlot.service.ScheduleCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LineupController {

    private final ScheduleCrawlerService scheduleCrawlerService;
    private final LineupCrawlerService lineupCrawlerService;
    private final TeamRepository teamRepository;

    @GetMapping("/crawl")
    public ResponseEntity<String> getSchedule() {
        scheduleCrawlerService.crawlingGameId();
        lineupCrawlerService.crawlAllLineups();
        return new ResponseEntity<>("크롤링 완료", HttpStatus.OK);
    }
    
    @GetMapping("/teams")
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return ResponseEntity.ok(teams);
    }
    
    @GetMapping("/teams/{teamCode}")
    public ResponseEntity<Team> getTeam(@PathVariable String teamCode) {
        Optional<Team> team = teamRepository.findById(teamCode);
        return team.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
}
