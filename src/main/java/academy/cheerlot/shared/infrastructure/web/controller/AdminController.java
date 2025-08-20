package academy.cheerlot.shared.infrastructure.web.controller;

import academy.cheerlot.domain.player.domain.Player;
import academy.cheerlot.domain.team.domain.Team;
import academy.cheerlot.domain.player.infrastructure.persistence.PlayerRepository;
import academy.cheerlot.domain.team.infrastructure.persistence.TeamRepository;
import academy.cheerlot.domain.version.application.RosterVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final RosterVersionService rosterVersionService;

    @GetMapping
    public String adminMain(Model model) {
        model.addAttribute("pageTitle", "관리자 대시보드");
        return "admin/main";
    }

    @GetMapping("/players")
    public String listPlayers(@RequestParam(value = "teamCode", required = false) String teamCode, Model model) {
        List<Team> allTeams = teamRepository.findAll();
        model.addAttribute("teams", allTeams);
        
        List<Player> players;
        if (teamCode != null && !teamCode.isEmpty()) {
            Optional<Team> team = teamRepository.findById(teamCode);
            if (team.isPresent()) {
                players = playerRepository.findByTeamOrderByBatsOrder(team.get());
                model.addAttribute("selectedTeam", team.get());
            } else {
                players = playerRepository.findAll();
            }
        } else {
            players = playerRepository.findAll();
        }
        
        model.addAttribute("players", players);
        model.addAttribute("selectedTeamCode", teamCode);
        model.addAttribute("pageTitle", "선수 관리");
        
        return "admin/players";
    }

    @GetMapping("/players/{id}/edit")
    public String editPlayerForm(@PathVariable Long id, Model model) {
        Optional<Player> playerOpt = playerRepository.findById(id);
        if (playerOpt.isEmpty()) {
            return "redirect:/admin/players";
        }
        
        Player player = playerOpt.get();
        List<Team> teams = teamRepository.findAll();
        
        model.addAttribute("player", player);
        model.addAttribute("teams", teams);
        model.addAttribute("pageTitle", "선수 수정 - " + player.getName());
        model.addAttribute("showBackButton", true);
        model.addAttribute("backUrl", "/admin/players");
        
        return "admin/edit-player";
    }

    @PostMapping("/players/{id}")
    public String updatePlayer(@PathVariable Long id, 
                             @ModelAttribute Player player,
                             @RequestParam String teamCode,
                             RedirectAttributes redirectAttributes) {
        try {
            Optional<Player> existingPlayerOpt = playerRepository.findById(id);
            if (existingPlayerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "선수를 찾을 수 없습니다.");
                return "redirect:/admin/players";
            }
            
            Optional<Team> teamOpt = teamRepository.findById(teamCode);
            if (teamOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "팀을 찾을 수 없습니다.");
                return "redirect:/admin/players/" + id + "/edit";
            }
            
            Player existingPlayer = existingPlayerOpt.get();
            existingPlayer.setName(player.getName());
            existingPlayer.setBackNumber(player.getBackNumber());
            existingPlayer.setPosition(player.getPosition());
            existingPlayer.setBatsThrows(player.getBatsThrows());
            existingPlayer.setBatsOrder(player.getBatsOrder());
            existingPlayer.setTeam(teamOpt.get());
            
            playerRepository.save(existingPlayer);
            
            rosterVersionService.updateOnPlayerModified(existingPlayer.getTeam().getTeamCode(), existingPlayer.getName());
            
            log.info("선수 정보 업데이트 완료: {} (ID: {})", existingPlayer.getName(), id);
            redirectAttributes.addFlashAttribute("success", "선수 정보가 성공적으로 업데이트되었습니다.");
            
        } catch (Exception e) {
            log.error("선수 정보 업데이트 중 오류 발생: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "선수 정보 업데이트 중 오류가 발생했습니다.");
        }
        
        return "redirect:/admin/players";
    }

    @PostMapping("/players/{id}/delete")
    public String deletePlayer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Player> playerOpt = playerRepository.findById(id);
            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();
                String teamCode = player.getTeam().getTeamCode();
                String playerName = player.getName();
                
                playerRepository.deleteById(id);
                
                rosterVersionService.updateOnPlayerDeleted(teamCode, playerName);
                
                log.info("선수 삭제 완료: {} (ID: {})", playerName, id);
                redirectAttributes.addFlashAttribute("success", "선수가 성공적으로 삭제되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "삭제할 선수를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("선수 삭제 중 오류 발생: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "선수 삭제 중 오류가 발생했습니다.");
        }
        
        return "redirect:/admin/players";
    }

    @GetMapping("/players/new")
    public String newPlayerForm(Model model) {
        List<Team> teams = teamRepository.findAll();
        model.addAttribute("player", new Player());
        model.addAttribute("teams", teams);
        model.addAttribute("pageTitle", "새 선수 추가");
        model.addAttribute("showBackButton", true);
        model.addAttribute("backUrl", "/admin/players");
        return "admin/new-player";
    }

    @PostMapping("/players")
    public String createPlayer(@ModelAttribute Player player,
                             @RequestParam String teamCode,
                             RedirectAttributes redirectAttributes) {
        try {
            Optional<Team> teamOpt = teamRepository.findById(teamCode);
            if (teamOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "팀을 찾을 수 없습니다.");
                return "redirect:/admin/players/new";
            }
            
            player.setTeam(teamOpt.get());
            playerRepository.save(player);
            
            rosterVersionService.updateOnPlayerAdded(teamCode, player.getName());
            
            log.info("새 선수 추가 완료: {}", player.getName());
            redirectAttributes.addFlashAttribute("success", "새 선수가 성공적으로 추가되었습니다.");
            
        } catch (Exception e) {
            log.error("선수 추가 중 오류 발생: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "선수 추가 중 오류가 발생했습니다.");
        }
        
        return "redirect:/admin/players";
    }

    @GetMapping("/teams")
    public String listTeams(Model model) {
        List<Team> teams = teamRepository.findAll();
        
        for (Team team : teams) {
            int playerCount = playerRepository.findByTeamOrderByBatsOrder(team).size();
            team.setPlayerCount(playerCount);
        }
        
        model.addAttribute("teams", teams);
        model.addAttribute("pageTitle", "팀 관리");
        
        return "admin/teams";
    }

    @PostMapping("/teams/{teamCode}/update")
    public String updateTeam(@PathVariable String teamCode,
                           @RequestParam(required = false) String lastOpponent,
                           @RequestParam(required = false) String lastUpdated,
                           RedirectAttributes redirectAttributes) {
        try {
            Optional<Team> teamOpt = teamRepository.findById(teamCode);
            if (teamOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "팀을 찾을 수 없습니다.");
                return "redirect:/admin/teams";
            }
            
            Team team = teamOpt.get();
            
            if (lastOpponent != null && !lastOpponent.trim().isEmpty()) {
                team.setLastOpponent(lastOpponent.trim());
            }
            
            if (lastUpdated != null && !lastUpdated.trim().isEmpty()) {
                try {
                    team.setLastUpdated(LocalDate.parse(lastUpdated));
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)");
                    return "redirect:/admin/teams";
                }
            } else {
                team.setLastUpdated(LocalDate.now());
            }
            
            teamRepository.save(team);
            
            log.info("팀 정보 업데이트 완료: {} (코드: {})", team.getName(), teamCode);
            redirectAttributes.addFlashAttribute("success", "팀 정보가 성공적으로 업데이트되었습니다.");
            
        } catch (Exception e) {
            log.error("팀 정보 업데이트 중 오류 발생: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "팀 정보 업데이트 중 오류가 발생했습니다.");
        }
        
        return "redirect:/admin/teams";
    }
} 
