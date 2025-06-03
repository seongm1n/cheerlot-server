package academy.cheerlot.service;

import academy.cheerlot.domain.Game;
import academy.cheerlot.domain.Player;
import academy.cheerlot.domain.Team;
import academy.cheerlot.repository.GameRepository;
import academy.cheerlot.repository.PlayerRepository;
import academy.cheerlot.repository.TeamRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        clearTeamPlayers(homeTeam, awayTeam);

        List<Player> homePlayers = new ArrayList<>(saveLineup(homeLineup, homeTeam));
        List<Player> awayPlayers = new ArrayList<>(saveLineup(awayLineup, awayTeam));
        
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
    
    private void clearTeamPlayers(Team homeTeam, Team awayTeam) {
        log.info("홈팀({})과 원정팀({})의 기존 선수 데이터를 삭제합니다.", homeTeam.getName(), awayTeam.getName());
        
        playerRepository.deleteByTeam(homeTeam);
        playerRepository.deleteByTeam(awayTeam);
        
        log.info("팀별 선수 데이터 삭제 완료");
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
    
    private List<Player> saveLineup(JsonNode lineupNode, Team team) {
        List<Player> savedPlayers = new ArrayList<>();
        
        for (JsonNode playerNode : lineupNode) {
            if (playerNode.has("batorder")) {
                Player player = createPlayerFromNode(playerNode, team);
                player = playerRepository.save(player);
                savedPlayers.add(player);
                
                log.info("선수 저장: {} ({}번) - {} / {}", 
                        player.getName(), 
                        player.getBackNumber(), 
                        player.getPosition(), 
                        team.getName());
            }
        }
        
        return savedPlayers;
    }

    private boolean checkLineup(JsonNode lineupNode) {
        for (JsonNode playerNode : lineupNode) {
            if (playerNode.has("batorder")) {
                return true;
            }
        }
        return false;
    }
    
    private Player createPlayerFromNode(JsonNode playerNode, Team team) {
        String playerName = playerNode.get("playerName").asText();
        String positionName = playerNode.get("positionName").asText();
        String batsThrows = playerNode.get("batsThrows").asText();
        String backNumber = playerNode.get("backnum").asText();
        String batOrder = playerNode.has("batorder") ? playerNode.get("batorder").asText() : "";
        
        Player player = new Player();
        player.setName(playerName);
        player.setBackNumber(backNumber);
        player.setPosition(positionName);
        player.setBatsThrows(batsThrows);
        player.setBatsOrder(batOrder);
        player.setTeam(team);
        
        return player;
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
