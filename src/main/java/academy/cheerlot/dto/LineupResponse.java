package academy.cheerlot.dto;

import academy.cheerlot.domain.Player;

import java.util.List;

public record LineupResponse(
        String updated,
        String opponent,
        List<Player> players
) {
}
