package academy.cheerlot.lineup;

import academy.cheerlot.game.Game;
import academy.cheerlot.game.GameRepository;
import academy.cheerlot.team.Team;
import academy.cheerlot.team.TeamRepository;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleCrawlerService {

    private static final String BASE_URL = "https://api-gw.sports.naver.com/schedule/calendar";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    private static final String REFERER = "https://m.sports.naver.com";
    private static final String ORIGIN = "https://m.sports.naver.com";
    private static final int GAME_ID_LENGTH = 17;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;

    public void crawlingGameId() {
        clearExistingGames();
        
        String scheduleJson = fetchScheduleData();
        if (scheduleJson == null) {
            log.error("일정 정보를 가져오는데 실패했습니다.");
            return;
        }
        
        List<String> todayGameIds = extractTodayGameIds(scheduleJson);
        
        List<String> filteredGameIds = filterKboGameIds(todayGameIds);
        
        saveGameIds(filteredGameIds);
        
        updateTeamsHasGameToday(filteredGameIds);
    }

    private void clearExistingGames() {
        long gameCount = gameRepository.count();
        if (gameCount > 0) {
            gameRepository.deleteAll();
        }
    }

    private String fetchScheduleData() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FORMATTER);

        try {
            String url = buildScheduleUrl(dateStr);
            HttpEntity<?> requestEntity = createRequestEntity();
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("API 요청 실패: 상태 코드 {}", response.getStatusCodeValue());
                return null;
            }
        } catch (Exception e) {
            log.error("API 요청 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private String buildScheduleUrl(String dateStr) {
        return UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("upperCategoryId", "kbaseball")
                .queryParam("categoryIds", "kbo,kbaseballetc,premier12,apbc")
                .queryParam("date", dateStr)
                .toUriString();
    }
    
    private HttpEntity<?> createRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);
        headers.set("Referer", REFERER);
        headers.set("Origin", ORIGIN);
        return new HttpEntity<>(headers);
    }
    
    private List<String> extractTodayGameIds(String scheduleJson) {
        List<String> gameIds = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(scheduleJson);
            
            if (!isSuccessResponse(rootNode)) {
                return gameIds;
            }
            
            JsonNode resultNode = rootNode.get("result");
            String today = resultNode.get("today").asText();
            
            JsonNode datesNode = resultNode.get("dates");
            for (JsonNode dateNode : datesNode) {
                if (dateNode.get("ymd").asText().equals(today)) {
                    JsonNode gameIdsNode = dateNode.get("gameIds");
                    for (JsonNode gameIdNode : gameIdsNode) {
                        gameIds.add(gameIdNode.asText());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            log.error("JSON 파싱 중 오류 발생", e);
        }
        
        return gameIds;
    }
    
    private boolean isSuccessResponse(JsonNode rootNode) {
        return rootNode.get("code").asInt() == 200 && rootNode.get("success").asBoolean();
    }

    private List<String> filterKboGameIds(List<String> gameIds) {
        return gameIds.stream()
                .filter(gameId -> gameId.length() == GAME_ID_LENGTH)
                .toList();
    }
    
    private void saveGameIds(List<String> gameIds) {
        int savedCount = 0;
        for (String gameId : gameIds) {
            Game game = new Game();
            game.setGameId(gameId);
            gameRepository.save(game);
            savedCount++;
            
        }
        
    }
    
    private void updateTeamsHasGameToday(List<String> gameIds) {
        List<Team> allTeams = teamRepository.findAll();
        
        for (Team team : allTeams) {
            team.setHasGameToday(false);
        }
        
        for (String gameId : gameIds) {
            try {
                updateTeamGameStatus(gameId, allTeams);
            } catch (Exception e) {
                log.error("게임 ID {}에서 팀 정보를 가져오는 중 오류 발생: {}", gameId, e.getMessage());
            }
        }
        
        teamRepository.saveAll(allTeams);
    }
    
    private void updateTeamGameStatus(String gameId, List<Team> allTeams) {
        String previewJson = fetchPreviewData(gameId);
        if (previewJson == null) {
            return;
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(previewJson);
            if (!isSuccessResponse(rootNode)) {
                return;
            }
            
            JsonNode gameInfo = rootNode.get("result").get("previewData").get("gameInfo");
            String homeTeamCode = gameInfo.get("hCode").asText();
            String awayTeamCode = gameInfo.get("aCode").asText();
            
            for (Team team : allTeams) {
                if (homeTeamCode.equals(team.getTeamCode()) || awayTeamCode.equals(team.getTeamCode())) {
                    team.setHasGameToday(true);
                }
            }
        } catch (Exception e) {
            log.error("게임 ID {}의 JSON 파싱 중 오류 발생: {}", gameId, e.getMessage());
        }
    }
    
    private String fetchPreviewData(String gameId) {
        try {
            String url = "https://api-gw.sports.naver.com/schedule/games/" + gameId + "/preview";
            HttpEntity<?> requestEntity = createRequestEntity();
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("프리뷰 API 요청 중 오류 발생: {}", e.getMessage());
        }
        return null;
    }
}
