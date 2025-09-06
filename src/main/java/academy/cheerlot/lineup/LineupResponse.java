package academy.cheerlot.lineup;

import academy.cheerlot.player.PlayerResponse;

import java.util.List;

public record LineupResponse(
        String updated,
        String opponent,
        List<PlayerResponse> players
) {
}
