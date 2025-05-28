package academy.cheerlot.controller;

import academy.cheerlot.service.ScheduleCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LineupController {

    private final ScheduleCrawlerService scheduleCrawlerService;

    @GetMapping("/schedule")
    public ResponseEntity<String> getSchedule() {
        scheduleCrawlerService.crawlingGameId();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
