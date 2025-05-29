package academy.cheerlot.dto;

import academy.cheerlot.domain.Player;

import java.time.LocalDate;
import java.util.List;

public record LineupResponse(
        LocalDate updated,
        String Opponent,
        List<Player> players
) {
}
