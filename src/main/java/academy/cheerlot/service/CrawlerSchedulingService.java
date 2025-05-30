package academy.cheerlot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlerSchedulingService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final ScheduleCrawlerService scheduleCrawlerService;
    private final LineupCrawlerService lineupCrawlerService;

    @Scheduled(cron = "0 0 */2 * * *")
    public void executeCrawling() {
        String currentTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        log.info("=== 자동 크롤링 시작 - {} ===", currentTime);
        
        try {
            log.info("1단계: 게임 스케줄 크롤링 시작");
            scheduleCrawlerService.crawlingGameId();
            log.info("1단계: 게임 스케줄 크롤링 완료");
            
            log.info("2단계: 라인업 크롤링 시작");
            lineupCrawlerService.crawlAllLineups();
            log.info("2단계: 라인업 크롤링 완료");
            
            String completionTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            log.info("=== 자동 크롤링 성공 완료 - {} ===", completionTime);
            
        } catch (Exception e) {
            String errorTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            log.error("=== 자동 크롤링 실행 중 오류 발생 - {} ===", errorTime);
            log.error("오류 내용: {}", e.getMessage(), e);
            log.error("다음 스케줄링 시간(2시간 후 정각)에 다시 시도됩니다.");
        }
    }
} 
