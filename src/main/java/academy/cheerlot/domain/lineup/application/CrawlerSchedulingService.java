package academy.cheerlot.domain.lineup.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
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

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void initializeCrawling() {
        
        try {
            Thread.sleep(10000);
            
            scheduleCrawlerService.crawlingGameId();
            lineupCrawlerService.crawlAllLineups();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("초기 크롤링 중 인터럽트 발생");
        } catch (Exception e) {
            log.error("초기 크롤링 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void executeCrawling() {
        try {
            scheduleCrawlerService.crawlingGameId();
            lineupCrawlerService.crawlAllLineups();
            
        } catch (Exception e) {
            log.error("자동 크롤링 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }
} 
