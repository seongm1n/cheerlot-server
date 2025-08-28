package academy.cheerlot.admin;

import academy.cheerlot.player.Player;
import academy.cheerlot.player.PlayerRepository;
import academy.cheerlot.team.Team;
import academy.cheerlot.team.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @GetMapping("")
    public String dashboard(Model model) {
        List<Team> teams = (List<Team>) teamRepository.findAll();
        long totalPlayers = playerRepository.count();
        long playersWithCheersongs = getPlayersWithCheersongs().size();
        
        model.addAttribute("teams", teams);
        model.addAttribute("totalPlayers", totalPlayers);
        model.addAttribute("playersWithCheersongs", playersWithCheersongs);
        
        return "admin/dashboard";
    }

    @GetMapping("/players")
    public String players(@RequestParam(required = false) String teamCode, Model model) {
        List<Player> players;
        List<Team> teams = (List<Team>) teamRepository.findAll();
        
        if (teamCode != null && !teamCode.isEmpty()) {
            players = playerRepository.findByTeamCodeOrderByBatsOrder(teamCode);
        } else {
            players = (List<Player>) playerRepository.findAll();
        }
        
        Map<String, String> cheerSongFiles = getCheersongFileMap();
        
        model.addAttribute("players", players);
        model.addAttribute("teams", teams);
        model.addAttribute("selectedTeam", teamCode);
        model.addAttribute("cheerSongFiles", cheerSongFiles);
        
        return "admin/players";
    }

    @GetMapping("/lineup")
    public String lineup(@RequestParam(required = false) String teamCode, Model model) {
        List<Team> teams = (List<Team>) teamRepository.findAll();
        List<Player> startingLineup = new ArrayList<>();
        Team selectedTeam = null;
        
        if (teamCode != null && !teamCode.isEmpty()) {
            selectedTeam = teamRepository.findById(teamCode).orElse(null);
            if (selectedTeam != null) {
                startingLineup = playerRepository.findByTeamCodeOrderByBatsOrder(teamCode)
                    .stream()
                    .filter(player -> player.getBatsOrder() != null && 
                                    !player.getBatsOrder().equals("0") && 
                                    !player.getBatsOrder().trim().isEmpty())
                    .sorted((p1, p2) -> {
                        try {
                            return Integer.compare(Integer.parseInt(p1.getBatsOrder()), 
                                                 Integer.parseInt(p2.getBatsOrder()));
                        } catch (NumberFormatException e) {
                            return p1.getBatsOrder().compareTo(p2.getBatsOrder());
                        }
                    })
                    .collect(Collectors.toList());
            }
        }
        
        model.addAttribute("teams", teams);
        model.addAttribute("selectedTeam", teamCode);
        model.addAttribute("selectedTeamName", selectedTeam != null ? selectedTeam.getName() : null);
        model.addAttribute("startingLineup", startingLineup);
        
        return "admin/lineup";
    }

    @GetMapping("/cheersongs")
    public String cheersongs(Model model) {
        List<Player> playersWithCheersongs = getPlayersWithCheersongs();
        Map<String, String> cheerSongFiles = getCheersongFileMap();
        
        model.addAttribute("playersWithCheersongs", playersWithCheersongs);
        model.addAttribute("cheerSongFiles", cheerSongFiles);
        
        return "admin/cheersongs";
    }

    @PostMapping("/player/{id}/batting-order")
    @ResponseBody
    public Map<String, String> updateBattingOrder(@PathVariable String id, @RequestParam String battingOrder) {
        Map<String, String> response = new HashMap<>();
        try {
            Player player = playerRepository.findById(id).orElse(null);
            if (player != null) {
                player.setBatsOrder(battingOrder);
                playerRepository.save(player);
                response.put("status", "success");
                response.put("message", "타순이 성공적으로 업데이트되었습니다.");
            } else {
                response.put("status", "error");
                response.put("message", "선수를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
        return response;
    }

    private List<Player> getPlayersWithCheersongs() {
        Map<String, String> cheerSongFiles = getCheersongFileMap();
        return ((List<Player>) playerRepository.findAll()).stream()
            .filter(player -> {
                String teamCode = player.getTeamCode().toLowerCase();
                String backNumber = player.getBackNumber();
                String expectedFileName = teamCode + backNumber + ".mp3";
                return cheerSongFiles.containsKey(expectedFileName);
            })
            .collect(Collectors.toList());
    }

    private Map<String, String> getCheersongFileMap() {
        Map<String, String> fileMap = new HashMap<>();
        try {
            ClassPathResource resource = new ClassPathResource("cheersongs/audio");
            if (resource.exists()) {
                Path audioPath = Paths.get(resource.getURI());
                Files.list(audioPath)
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp3") || 
                                   path.toString().toLowerCase().endsWith(".mp4"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        fileMap.put(fileName, fileName);
                    });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileMap;
    }
}
