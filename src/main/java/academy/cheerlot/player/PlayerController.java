package academy.cheerlot.player;

import academy.cheerlot.cheersong.CheerSong;
import academy.cheerlot.cheersong.CheerSongResponse;
import academy.cheerlot.cheersong.CheerSongRepository;
import academy.cheerlot.team.Team;
import academy.cheerlot.team.TeamRepository;
import lombok.RequiredArgsConstructor;
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
public class PlayerController {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final CheerSongRepository cheerSongRepository;

    @GetMapping("/players")
    public ResponseEntity<List<PlayerResponse>> getAllPlayers() {
        List<Player> players = (List<Player>) playerRepository.findAll();
        List<PlayerResponse> playerResponses = players.stream()
                .map(this::convertToResponse)
                .toList();
        
        return ResponseEntity.ok(playerResponses);
    }

    @GetMapping("/players/{teamCode}")
    public ResponseEntity<List<PlayerResponse>> getPlayersByTeam(@PathVariable String teamCode) {
        Optional<Team> teamOpt = teamRepository.findById(teamCode);
        
        if (teamOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Player> players = playerRepository.findByTeamCodeOrderByBatsOrder(teamCode);
        List<PlayerResponse> playerResponses = players.stream()
                .map(this::convertToResponse)
                .toList();
        
        return ResponseEntity.ok(playerResponses);
    }
    
    private PlayerResponse convertToResponse(Player player) {
        List<CheerSong> cheerSongs = cheerSongRepository.findByPlayerId(player.getPlayerId());
        List<CheerSongResponse> cheerSongResponses = cheerSongs.stream()
                .map(CheerSongResponse::from)
                .toList();
        
        return PlayerResponse.from(player, cheerSongResponses);
    }
}
