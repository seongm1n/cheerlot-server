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

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameRepository gameRepository;

    public void crawlingGameId() {
        String scheduleJson = getSchedule();
        if (scheduleJson == null) {
            log.error("일정 정보를 가져오는데 실패했습니다.");
            return;
        }
        
        List<String> todayGameIds = getTodayGameIds(scheduleJson);
        log.info("오늘 총 {} 개의 게임 ID를 찾았습니다.", todayGameIds.size());
        
        List<String> filteredGameIds = filterGameIds(todayGameIds);
        log.info("필터링 후 {} 개의 게임 ID가 남았습니다.", filteredGameIds.size());
        
        int savedCount = 0;
        for (String gameId : filteredGameIds) {
            if (gameRepository.existsById(gameId)) {
                log.info("이미 존재하는 게임 ID입니다: {}", gameId);
                continue;
            }
            
            Game game = new Game();
            game.setGameId(gameId);
            gameRepository.save(game);
            savedCount++;
            
            log.info("게임 ID 저장: {}", gameId);
        }
        
        log.info("총 {}개의 새로운 게임 ID가 저장되었습니다.", savedCount);
    }

     private String getSchedule() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FORMATTER);
        log.info("네이버 스포츠 일정 API 호출: {}", dateStr);

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("upperCategoryId", "kbaseball")
                    .queryParam("categoryIds", "kbo,kbaseballetc,premier12,apbc")
                    .queryParam("date", dateStr);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36");
            headers.set("Referer", "https://m.sports.naver.com");
            headers.set("Origin", "https://m.sports.naver.com");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
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
    
    private List<String> getTodayGameIds(String scheduleJson) {
        List<String> gameIds = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(scheduleJson);
            
            if (rootNode.get("code").asInt() != 200 || !rootNode.get("success").asBoolean()) {
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
            e.printStackTrace();
        }
        
        return gameIds;
    }

    private List<String> filterGameIds(List<String> gameIds) {
        List<String> kboGameIds = new ArrayList<>();
        
        for (String gameId : gameIds) {
            if (gameId.length() >= 14) {
                kboGameIds.add(gameId);
            }
        }

        return kboGameIds;
    }
}
