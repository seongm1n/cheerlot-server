package academy.cheerlot.game;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game")
@Data
@NoArgsConstructor
public class Game {

    @Id
    private String gameId;
}
