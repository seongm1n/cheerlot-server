package academy.cheerlot.team;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDate;

@RedisHash("team")
@Data
@NoArgsConstructor
public class Team {

    @Id
    private String teamCode;

    @Indexed
    private String name;

    private LocalDate lastUpdated;

    private String lastOpponent;
    
    private int playerCount;
    
    private Boolean hasGameToday;
    
    private Boolean isSeasonActive;

    public Team(String teamCode, String name, LocalDate lastUpdated, String lastOpponent) {
        this.teamCode = teamCode;
        this.name = name;
        this.lastUpdated = lastUpdated;
        this.lastOpponent = lastOpponent;
        this.playerCount = 0;
        this.hasGameToday = false;
        this.isSeasonActive = true;
    }
}
