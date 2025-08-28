package academy.cheerlot.lineup;

import academy.cheerlot.player.Player;
import academy.cheerlot.player.PlayerDto;
import academy.cheerlot.player.PlayerRepository;
import academy.cheerlot.team.Team;
import academy.cheerlot.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LineupController {

    private final ScheduleCrawlerService scheduleCrawlerService;
    private final LineupCrawlerService lineupCrawlerService;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    @GetMapping("/crawl")
    public ResponseEntity<String> getSchedule() {
        scheduleCrawlerService.crawlingGameId();
        lineupCrawlerService.crawlAllLineups();
        return new ResponseEntity<>("크롤링 완료", HttpStatus.OK);
    }

    @GetMapping("/lineups/{teamCode}")
    public ResponseEntity<LineupResponse> getLineup(@PathVariable String teamCode) {
        Optional<Team> teamOpt = teamRepository.findById(teamCode);
        
        if (teamOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Team team = teamOpt.get();
        List<Player> players = playerRepository.findByTeamOrderByBatsOrder(team);
        List<PlayerDto> playerDtos = players.stream()
                .map(PlayerDto::from)
                .toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일");
        String lastUpdated = team.getLastUpdated().format(formatter);

        LineupResponse response = new LineupResponse(
                lastUpdated,
                team.getLastOpponent(),
                playerDtos
        );
        
        return ResponseEntity.ok(response);
    }
}
