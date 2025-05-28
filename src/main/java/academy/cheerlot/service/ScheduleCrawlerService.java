package academy.cheerlot.service;

import academy.cheerlot.domain.Game;
import academy.cheerlot.repository.GameRepository;
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
    private static final int MIN_GAME_ID_LENGTH = 14;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameRepository gameRepository;

    public void crawlingGameId() {
        clearExistingGames();
        
        String scheduleJson = fetchScheduleData();
        if (scheduleJson == null) {
            log.error("일정 정보를 가져오는데 실패했습니다.");
            return;
        }
        
        List<String> todayGameIds = extractTodayGameIds(scheduleJson);
        log.info("오늘 총 {} 개의 게임 ID를 찾았습니다.", todayGameIds.size());
        
        List<String> filteredGameIds = filterKboGameIds(todayGameIds);
        log.info("필터링 후 {} 개의 게임 ID가 남았습니다.", filteredGameIds.size());
        
        saveGameIds(filteredGameIds);
    }

    private void clearExistingGames() {
        long gameCount = gameRepository.count();
        if (gameCount > 0) {
            log.info("기존 게임 데이터 {}개를 삭제합니다.", gameCount);
            gameRepository.deleteAll();
            log.info("기존 게임 데이터 삭제 완료");
        }
    }

    private String fetchScheduleData() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FORMATTER);
        log.info("네이버 스포츠 일정 API 호출: {}", dateStr);

        try {
            String url = buildScheduleUrl(dateStr);
            HttpEntity<?> requestEntity = createRequestEntity();
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("API 호출 성공");
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
                .filter(gameId -> gameId.length() >= MIN_GAME_ID_LENGTH)
                .toList();
    }
    
    private void saveGameIds(List<String> gameIds) {
        int savedCount = 0;
        for (String gameId : gameIds) {
            Game game = new Game();
            game.setGameId(gameId);
            gameRepository.save(game);
            savedCount++;
            
            log.info("게임 ID 저장: {}", gameId);
        }
        
        log.info("총 {}개의 새로운 게임 ID가 저장되었습니다.", savedCount);
    }
}
