package academy.cheerlot.admin;

import academy.cheerlot.player.Player;
import academy.cheerlot.player.PlayerRepository;
import academy.cheerlot.team.Team;
import academy.cheerlot.team.TeamRepository;
import academy.cheerlot.version.Version;
import academy.cheerlot.version.VersionRepository;
import academy.cheerlot.version.VersionService;
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

    @Autowired
    private VersionService versionService;

    @Autowired
    private VersionRepository versionRepository;

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

    @GetMapping("/versions")
    public String versions(Model model) {
        List<Team> teams = (List<Team>) teamRepository.findAll();
        
        for (Team team : teams) {
            ensureVersionExists(team.getTeamCode(), Version.VersionType.ROSTER);
            ensureVersionExists(team.getTeamCode(), Version.VersionType.LINEUP);
        }
        
        List<Version> allVersions = (List<Version>) versionRepository.findAll();
        
        Map<String, Map<String, Version>> versionMap = new HashMap<>();
        for (Version version : allVersions) {
            versionMap.computeIfAbsent(version.getTeamCode(), k -> new HashMap<>())
                      .put(version.getType().name(), version);
        }
        
        model.addAttribute("teams", teams);
        model.addAttribute("versionMap", versionMap);
        
        return "admin/versions";
    }

    private void ensureVersionExists(String teamCode, Version.VersionType type) {
        Optional<Version> existingVersion = versionRepository.findByTeamCodeAndType(teamCode, type);
        if (!existingVersion.isPresent()) {
            Version initialVersion = new Version(teamCode, type, "초기 버전");
            versionRepository.save(initialVersion);
        }
    }

    @PostMapping("/version/{teamCode}/{type}/update")
    @ResponseBody
    public Map<String, String> updateVersion(@PathVariable String teamCode, 
                                           @PathVariable String type, 
                                           @RequestParam String description,
                                           @RequestParam Long versionNumber) {
        Map<String, String> response = new HashMap<>();
        try {
            if (versionNumber < 0) {
                response.put("status", "error");
                response.put("message", "버전 번호는 0 이상이어야 합니다.");
                return response;
            }
            
            Version.VersionType versionType = Version.VersionType.valueOf(type.toUpperCase());
            versionService.setVersionNumber(teamCode, versionType, description, versionNumber);
            
            response.put("status", "success");
            response.put("message", "버전이 성공적으로 업데이트되었습니다.");
        } catch (NumberFormatException e) {
            response.put("status", "error");
            response.put("message", "버전 번호는 숫자여야 합니다.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "버전 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/version/{teamCode}/{type}/increment")
    @ResponseBody
    public Map<String, String> incrementVersion(@PathVariable String teamCode, 
                                              @PathVariable String type, 
                                              @RequestParam String description) {
        Map<String, String> response = new HashMap<>();
        try {
            Version.VersionType versionType = Version.VersionType.valueOf(type.toUpperCase());
            versionService.updateVersion(teamCode, versionType, description);
            
            response.put("status", "success");
            response.put("message", "버전이 성공적으로 증가되었습니다.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "버전 업데이트 중 오류가 발생했습니다: " + e.getMessage());
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
                if (resource.getURI().toString().startsWith("jar:")) {
                    // JAR 파일 내부에서 실행될 때 (Docker 컨테이너)
                    // 실제로는 리소스 폴더의 파일 목록을 하드코딩하거나 다른 방식으로 관리해야 함
                    // 임시로 빈 맵 반환 (응원가 파일이 없다고 표시됨)
                    System.out.println("Running in JAR mode - cheersong file listing not available");
                } else {
                    // 개발 환경에서 실행될 때
                    Path audioPath = Paths.get(resource.getURI());
                    Files.list(audioPath)
                        .filter(path -> path.toString().toLowerCase().endsWith(".mp3") || 
                                       path.toString().toLowerCase().endsWith(".mp4"))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            fileMap.put(fileName, fileName);
                        });
                }
            }
        } catch (IOException e) {
            System.out.println("Error accessing cheersong files: " + e.getMessage());
        }
        return fileMap;
    }
}
