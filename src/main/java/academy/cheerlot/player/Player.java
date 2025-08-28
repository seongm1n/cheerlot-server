package academy.cheerlot.player;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("player")
@Data
@NoArgsConstructor
public class Player {

    @Id
    private String playerId; // teamCode:backNumber 형태

    @Indexed
    private String name;
    private String backNumber;
    private String position;
    private String batsThrows;
    private String batsOrder;

    @Indexed
    private String teamCode;

    public Player(String name, String backNumber, String position, String teamCode, String batsOrder) {
        this.playerId = teamCode + ":" + backNumber;
        this.name = name;
        this.backNumber = backNumber;
        this.position = position;
        this.teamCode = teamCode;
        this.batsOrder = batsOrder;
    }
}
