package academy.cheerlot.domain.lineup.application;

import academy.cheerlot.domain.game.domain.Game;
import academy.cheerlot.domain.player.domain.Player;
import academy.cheerlot.domain.team.domain.Team;
import academy.cheerlot.domain.game.infrastructure.persistence.GameRepository;
import academy.cheerlot.domain.player.infrastructure.persistence.PlayerRepository;
import academy.cheerlot.domain.team.infrastructure.persistence.TeamRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LineupCrawlerService {

    private static final String PREVIEW_URL = "https://api-gw.sports.naver.com/schedule/games/{game_id}/preview";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    private static final String REFERER = "https://m.sports.naver.com";
    private static final String ORIGIN = "https://m.sports.naver.com";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    public void crawlAllLineups() {
        log.info("모든 경기의 라인업 정보를 크롤링을 시작합니다.");
        
        List<Game> games = gameRepository.findAll();
        if (games.isEmpty()) {
            log.warn("크롤링할 게임이 없습니다. 먼저 게임 데이터를 수집해주세요.");
            return;
        }
        
        processAllGames(games);
    }
    
    private void processAllGames(List<Game> games) {
        int totalSaved = 0;
        
        for (Game game : games) {
            String gameId = game.getGameId();
            log.info("게임 ID: {}의 라인업 정보를 크롤링합니다.", gameId);
            
            try {
                int savedCount = processGame(gameId);
                totalSaved += savedCount;
                log.info("게임 ID: {}에서 {}명의 선수 정보가 저장되었습니다.", gameId, savedCount);
            } catch (Exception e) {
                log.error("게임 ID: {}의 처리 중 예상치 못한 오류 발생: {}. 다음 게임으로 넘어갑니다.", gameId, e.getMessage());
            }
        }
        
        log.info("총 {}개 게임의 라인업 정보 크롤링 완료. 총 {}명의 선수 정보가 저장되었습니다.", games.size(), totalSaved);
    }
    
    private int processGame(String gameId) throws IOException {
        String previewJson = fetchPreviewData(gameId);
        if (previewJson == null) {
            log.error("게임 ID: {}의 프리뷰 정보를 가져오는데 실패했습니다.", gameId);
            return 0;
        }
        
        JsonNode rootNode = objectMapper.readTree(previewJson);
        if (!isSuccessResponse(rootNode)) {
            log.error("게임 ID: {}의 API 응답이 올바르지 않습니다.", gameId);
            return 0;
        }
        
        JsonNode previewData = rootNode.get("result").get("previewData");
        
        Team homeTeam = getTeam(previewData, "hCode");
        Team awayTeam = getTeam(previewData, "aCode");
        
        if (homeTeam == null || awayTeam == null) {
            log.error("게임 ID: {}의 홈팀 또는 원정팀 정보를 찾을 수 없습니다.", gameId);
            return 0;
        }

        JsonNode homeLineup = previewData.get("homeTeamLineUp").get("fullLineUp");
        JsonNode awayLineup = previewData.get("awayTeamLineUp").get("fullLineUp");

        if (!checkLineup(homeLineup) || !checkLineup(awayLineup)) {
            log.error("아직 라인업이 나오지 않았습니다.");
            return 0;
        }

        resetTeamPlayersBatsOrder(homeTeam, awayTeam);

        List<Player> homePlayers = new ArrayList<>(updateLineupBatsOrder(homeLineup, homeTeam));
        List<Player> awayPlayers = new ArrayList<>(updateLineupBatsOrder(awayLineup, awayTeam));
        
        if (!homePlayers.isEmpty() && !awayPlayers.isEmpty()) {
            updateTeamInfo(homeTeam, awayTeam);
            log.info("게임 ID: {}에서 홈팀 {}명, 원정팀 {}명 선수 저장 후 팀 정보 업데이트 완료", 
                    gameId, homePlayers.size(), awayPlayers.size());
        } else {
            log.warn("게임 ID: {}에서 선수 저장 실패 - 홈팀: {}명, 원정팀: {}명. 팀 정보 업데이트 건너뜀", 
                    gameId, homePlayers.size(), awayPlayers.size());
        }
        
        return homePlayers.size() + awayPlayers.size();
    }
    
    private void resetTeamPlayersBatsOrder(Team homeTeam, Team awayTeam) {
        log.info("홈팀({})과 원정팀({})의 선수 타순을 초기화합니다.", homeTeam.getName(), awayTeam.getName());
        
        playerRepository.updateBatsOrderToZeroByTeam(homeTeam);
        playerRepository.updateBatsOrderToZeroByTeam(awayTeam);
        
        log.info("팀별 선수 타순 초기화 완료");
    }
    
    private boolean isSuccessResponse(JsonNode rootNode) {
        return rootNode.get("code").asInt() == 200 && rootNode.get("success").asBoolean();
    }
    
    private Team getTeam(JsonNode previewData, String codeField) {
        JsonNode gameInfo = previewData.get("gameInfo");
        String teamCode = gameInfo.get(codeField).asText();
        
        Optional<Team> team = teamRepository.findById(teamCode);
        return team.orElse(null);
    }
    
    private List<Player> updateLineupBatsOrder(JsonNode lineupNode, Team team) {
        List<Player> updatedPlayers = new ArrayList<>();
        
        for (JsonNode playerNode : lineupNode) {
            if (playerNode.has("batorder")) {
                String playerName = playerNode.get("playerName").asText();
                String batOrder = playerNode.get("batorder").asText();
                
                Optional<Player> existingPlayerOpt = playerRepository.findByNameAndTeam(playerName, team);
                
                if (existingPlayerOpt.isPresent()) {
                    Player existingPlayer = existingPlayerOpt.get();
                    existingPlayer.setBatsOrder(batOrder);
                    
                    // 추가 정보도 업데이트 (포지션이나 등번호가 바뀔 수 있음)
                    existingPlayer.setPosition(playerNode.get("positionName").asText());
                    if (playerNode.has("backnum")) {
                        existingPlayer.setBackNumber(playerNode.get("backnum").asText());
                    }
                    if (playerNode.has("batsThrows")) {
                        existingPlayer.setBatsThrows(playerNode.get("batsThrows").asText());
                    }
                    
                    existingPlayer = playerRepository.save(existingPlayer);
                    updatedPlayers.add(existingPlayer);
                    
                    log.info("선수 타순 업데이트: {} ({}번) - 타순: {} / {}", 
                            existingPlayer.getName(), 
                            existingPlayer.getBackNumber(), 
                            batOrder,
                            team.getName());
                } else {
                    log.error("팀 {}에서 선수 '{}'를 찾을 수 없습니다. 해당 선수는 라인업 업데이트에서 제외됩니다.", team.getName(), playerName);
                }
            }
        }
        
        return updatedPlayers;
    }

    private boolean checkLineup(JsonNode lineupNode) {
        for (JsonNode playerNode : lineupNode) {
            if (playerNode.has("batorder")) {
                return true;
            }
        }
        return false;
    }
    
    private String fetchPreviewData(String gameId) {
        log.info("게임 ID: {}의 프리뷰 정보를 요청합니다.", gameId);
        
        try {
            String url = PREVIEW_URL.replace("{game_id}", gameId);
            HttpEntity<?> requestEntity = createRequestEntity();
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("프리뷰 API 호출 성공");
                return response.getBody();
            } else {
                log.error("프리뷰 API 요청 실패: 상태 코드 {}", response.getStatusCodeValue());
                return null;
            }
            
        } catch (Exception e) {
            log.error("프리뷰 API 요청 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private HttpEntity<?> createRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);
        headers.set("Referer", REFERER);
        headers.set("Origin", ORIGIN);
        return new HttpEntity<>(headers);
    }
    
    private void updateTeamInfo(Team homeTeam, Team awayTeam) {
        LocalDate today = LocalDate.now();
        
        homeTeam.setLastUpdated(today);
        homeTeam.setLastOpponent(awayTeam.getName());
        teamRepository.save(homeTeam);
        
        awayTeam.setLastUpdated(today);
        awayTeam.setLastOpponent(homeTeam.getName());
        teamRepository.save(awayTeam);
        
        log.info("팀 정보 업데이트 완료: {} vs {} ({})", homeTeam.getName(), awayTeam.getName(), today);
    }
}
