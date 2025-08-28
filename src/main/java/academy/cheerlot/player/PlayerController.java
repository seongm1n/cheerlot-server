package academy.cheerlot.player;

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

    @GetMapping("/players")
    public ResponseEntity<List<PlayerDto>> getAllPlayers() {
        List<Player> players = playerRepository.findAll();
        List<PlayerDto> playerDtos = players.stream()
                .map(PlayerDto::from)
                .toList();
        
        return ResponseEntity.ok(playerDtos);
    }

    @GetMapping("/players/{teamCode}")
    public ResponseEntity<List<PlayerDto>> getPlayersByTeam(@PathVariable String teamCode) {
        Optional<Team> teamOpt = teamRepository.findById(teamCode);
        
        if (teamOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Player> players = playerRepository.findByTeamOrderByBatsOrder(teamOpt.get());
        List<PlayerDto> playerDtos = players.stream()
                .map(PlayerDto::from)
                .toList();
        
        return ResponseEntity.ok(playerDtos);
    }
}
