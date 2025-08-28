package academy.cheerlot.team;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "team")
@Data
@NoArgsConstructor
public class Team {

    @Id
    private String teamCode;

    private String name;

    private LocalDate lastUpdated;

    private String lastOpponent;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<academy.cheerlot.player.Player> players;
    
    @Transient
    private int playerCount;
}
