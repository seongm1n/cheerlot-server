package academy.cheerlot.player;

import academy.cheerlot.cheersong.CheerSongResponse;
import java.util.List;

public record PlayerResponse(
        String playerId,
        String name,
        String backNumber,
        String position,
        String batsThrows,
        String batsOrder,
        String teamCode,
        List<CheerSongResponse> cheerSongs
) {
    public static PlayerResponse from(Player player, List<CheerSongResponse> cheerSongs) {
        return new PlayerResponse(
                player.getPlayerId(),
                player.getName(),
                player.getBackNumber(),
                player.getPosition(),
                player.getBatsThrows(),
                player.getBatsOrder(),
                player.getTeamCode(),
                cheerSongs
        );
    }
}