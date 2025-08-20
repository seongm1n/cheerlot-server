package academy.cheerlot.domain.lineup.infrastructure.web.dto;

import java.util.List;

public record LineupResponse(
        String updated,
        String opponent,
        List<academy.cheerlot.domain.player.infrastructure.web.dto.PlayerDto> players
) {
}
