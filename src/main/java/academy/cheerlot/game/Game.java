package academy.cheerlot.game;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("game")
@Data
@NoArgsConstructor
public class Game {

    @Id
    private String gameId;
    
    public Game(String gameId) {
        this.gameId = gameId;
    }
}
