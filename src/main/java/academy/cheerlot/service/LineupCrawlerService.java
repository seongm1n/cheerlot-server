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
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LineupCrawlerService {

    private static final String PREVIEW_URL = "https://api-gw.sports.naver.com/schedule/games/{game_id}/preview";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    public int crawlAllLineups() {
        log.info("모든 경기의 라인업 정보를 크롤링을 시작합니다.");
        
        long playerCount = playerRepository.count();
        if (playerCount > 0) {
            log.info("기존 선수 데이터 {}개를 삭제합니다.", playerCount);
            playerRepository.deleteAll();
            log.info("선수 데이터 삭제 완료");
        }
        
        List<Game> games = gameRepository.findAll();
        
        if (games.isEmpty()) {
            log.warn("크롤링할 게임이 없습니다. 먼저 게임 데이터를 수집해주세요.");
            return 0;
        }
        
        int totalSaved = 0;
        
        for (Game game : games) {
            String gameId = game.getGameId();
            log.info("게임 ID: {}의 라인업 정보를 크롤링합니다.", gameId);
            
            String previewJson = getPreview(gameId);
            if (previewJson == null) {
                log.error("게임 ID: {}의 프리뷰 정보를 가져오는데 실패했습니다. 다음 게임으로 넘어갑니다.", gameId);
                continue;
            }
            
            try {
                JsonNode rootNode = objectMapper.readTree(previewJson);
                
                if (rootNode.get("code").asInt() != 200 || !rootNode.get("success").asBoolean()) {
                    log.error("게임 ID: {}의 API 응답이 올바르지 않습니다. 다음 게임으로 넘어갑니다.", gameId);
                    continue;
                }
                
                JsonNode previewData = rootNode.get("result").get("previewData");
                
                JsonNode gameInfo = previewData.get("gameInfo");
                String homeTeamCode = gameInfo.get("hCode").asText();
                String awayTeamCode = gameInfo.get("aCode").asText();
                
                Optional<Team> homeTeam = teamRepository.findById(homeTeamCode);
                Optional<Team> awayTeam = teamRepository.findById(awayTeamCode);
                
                if (homeTeam.isEmpty() || awayTeam.isEmpty()) {
                    log.error("게임 ID: {}의 홈팀 또는 원정팀 정보를 찾을 수 없습니다. 다음 게임으로 넘어갑니다.", gameId);
                    continue;
                }
                
                List<Player> savedPlayers = new ArrayList<>();
                
                JsonNode homeLineup = previewData.get("homeTeamLineUp").get("fullLineUp");
                savedPlayers.addAll(saveLineup(homeLineup, homeTeam.get()));
                
                JsonNode awayLineup = previewData.get("awayTeamLineUp").get("fullLineUp");
                savedPlayers.addAll(saveLineup(awayLineup, awayTeam.get()));
                
                totalSaved += savedPlayers.size();
                log.info("게임 ID: {}에서 {}명의 선수 정보가 저장되었습니다.", gameId, savedPlayers.size());
                
            } catch (IOException e) {
                log.error("게임 ID: {}의 JSON 파싱 중 오류 발생: {}. 다음 게임으로 넘어갑니다.", gameId, e.getMessage());
            } catch (Exception e) {
                log.error("게임 ID: {}의 처리 중 예상치 못한 오류 발생: {}. 다음 게임으로 넘어갑니다.", gameId, e.getMessage());
            }
        }
        
        log.info("총 {}개 게임의 라인업 정보 크롤링 완료. 총 {}명의 선수 정보가 저장되었습니다.", games.size(), totalSaved);
        return totalSaved;
    }
    
    private List<Player> saveLineup(JsonNode lineupNode, Team team) {
        List<Player> savedPlayers = new ArrayList<>();
        
        for (JsonNode playerNode : lineupNode) {
            if (playerNode.has("batorder")) {
                String playerCode = playerNode.get("playerCode").asText();
                String playerName = playerNode.get("playerName").asText();
                String position = playerNode.get("position").asText();
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
                
                player = playerRepository.save(player);
                savedPlayers.add(player);
                
                log.info("선수 저장: {} ({}번) - {} / {}", playerName, backNumber, positionName, team.getName());
            }
        }
        
        return savedPlayers;
    }
    
    private String getPreview(String gameId) {
        log.info("게임 ID: {}의 프리뷰 정보를 요청합니다.", gameId);
        
        try {
            String url = PREVIEW_URL.replace("{game_id}", gameId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36");
            headers.set("Referer", "https://m.sports.naver.com");
            headers.set("Origin", "https://m.sports.naver.com");
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
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
}
