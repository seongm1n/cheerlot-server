package academy.cheerlot.lineup;

import academy.cheerlot.cheersong.CheerSong;
import academy.cheerlot.cheersong.CheerSongResponse;
import academy.cheerlot.cheersong.CheerSongRepository;
import academy.cheerlot.player.Player;
import academy.cheerlot.player.PlayerResponse;
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
    private final CheerSongRepository cheerSongRepository;

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
        List<Player> allPlayers = playerRepository.findByTeamCodeOrderByBatsOrder(teamCode);
        List<PlayerResponse> playerResponses = allPlayers.stream()
                .filter(player -> !"0".equals(player.getBatsOrder()))
                .map(this::convertToResponse)
                .toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일");
        String lastUpdated = team.getLastUpdated().format(formatter);

        LineupResponse response = new LineupResponse(
                lastUpdated,
                team.getLastOpponent(),
                team.getHasGameToday(),
                playerResponses
        );
        
        return ResponseEntity.ok(response);
    }
    
    private PlayerResponse convertToResponse(Player player) {
        List<CheerSong> cheerSongs = cheerSongRepository.findByPlayerId(player.getPlayerId());
        List<CheerSongResponse> cheerSongResponses = cheerSongs.stream()
                .map(CheerSongResponse::from)
                .toList();
        
        return PlayerResponse.from(player, cheerSongResponses);
    }
}
