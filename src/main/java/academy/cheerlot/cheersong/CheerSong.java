package academy.cheerlot.cheersong;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("cheersong")
@Data
@NoArgsConstructor
public class CheerSong {

    @Id
    private String id;

    private String title;
    
    private String lyrics;
    
    @Indexed
    private String audioFileName;
    
    @Indexed
    private String playerId; // playerId 문자열로 저장
    
    public CheerSong(String id, String title, String lyrics, String audioFileName, String playerId) {
        this.id = id;
        this.title = title;
        this.lyrics = lyrics;
        this.audioFileName = audioFileName;
        this.playerId = playerId;
    }
}
