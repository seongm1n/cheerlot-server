package academy.cheerlot.domain.cheersong.infrastructure.web;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cheersongs")
public class CheerSongController {

    private static final String AUDIO_BASE_PATH = "/cheersongs/audio/";

    @GetMapping("/{code}")
    public ResponseEntity<Resource> getCheerSongAudio(
            @PathVariable String code
    ) {

        try {
            ClassPathResource resource = new ClassPathResource(AUDIO_BASE_PATH + code);

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/mpeg"))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
