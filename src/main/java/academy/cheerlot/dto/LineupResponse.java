package academy.cheerlot.dto;

import java.util.List;

public record LineupResponse(
        String updated,
        String opponent,
        List<PlayerDto> players
) {
}
