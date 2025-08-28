package academy.cheerlot.lineup;

import academy.cheerlot.player.PlayerDto;

import java.util.List;

public record LineupResponse(
        String updated,
        String opponent,
        List<PlayerDto> players
) {
}
